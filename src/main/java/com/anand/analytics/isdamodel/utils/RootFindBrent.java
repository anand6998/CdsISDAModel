package com.anand.analytics.isdamodel.utils;

import org.apache.log4j.Logger;

/**
 * Created by aanand on 10/21/2014.
 */
public class RootFindBrent {
    final static Logger logger = Logger.getLogger(RootFindBrent.class);
    final static double ONE_PERCENT = 0.01;

    public static ReturnStatus findRoot(SolvableFunction function,
                                        Object data,
                                        double boundLo,
                                        double boundHi,
                                        int numIterations,
                                        double guess,
                                        double initialXStep,
                                        double initialFDeriv,
                                        double xacc,
                                        double facc,
                                        DoubleHolder solution
    ) {
        final String routine = "findRoot";
        double fLo = 0;                    /* Function evaluated at boundLo */
        double fHi = 0;                    /* Function evaluated at boundHi */
        BooleanHolder bracketed = new BooleanHolder(false);              /* If root was bracketed by secant */
        BooleanHolder foundIt = new BooleanHolder(false);                /* If root was found by secant */
        double xPoints[] = new double[3];             /* Array of x values */
        double yPoints[] = new double[3];             /* Array of y values */
        double boundSpread;            /* Diff between the hi and lo bounds */


        xPoints[0] = guess;

 /* Make sure lower bound is below higher bound.
    */
        if (boundLo >= boundHi) {
            String errMsg = String.format("%s: Lower bound (%2.6e) >= higher bound (%2.6e).\n", routine, boundLo, boundHi);
            logger.error(errMsg);
            return ReturnStatus.FAILURE;
        }

   /* Check to make sure the guess is within the bounds
    */
        if (xPoints[0] < boundLo || xPoints[0] > boundHi) {
            String errMsg = String.format("%s: Guess (%2.6e) is out of range [%2.6e,%2.6e].\n", routine, xPoints[0], boundLo, boundHi);
            logger.error(errMsg);
            return ReturnStatus.FAILURE;
        }

        DoubleHolder dblHolder = new DoubleHolder(yPoints[0]);
        if (function.eval(xPoints[0], data, dblHolder) == ReturnStatus.FAILURE) {
            String errMsg = String.format("%s: Supplied function failed at point %2.6e.\n", routine, xPoints[0]);
            logger.error(errMsg);
            return ReturnStatus.FAILURE;
        }

        yPoints[0] = dblHolder.get();
   /* Check if guess is the root (use bounds for x-accuracy) */
        if (yPoints[0] == 0.0 ||
                (Math.abs(yPoints[0]) <= facc && (Math.abs(boundLo - xPoints[0]) <= xacc ||
                        Math.abs(boundHi - xPoints[0]) <= xacc))) {
            solution.set(xPoints[0]);
            return ReturnStatus.FAILURE;
        }

          /* If the initialXStep is 0, set it to ONE_PERCENT of
    *  of (boundHi - BoundLo).
    */
        boundSpread = boundHi - boundLo;
        if (initialXStep == 0.0) {
            initialXStep = ONE_PERCENT * boundSpread;
        }

   /* Take a step of the size passed in, if the derivative is not
      passed in */
        if (initialFDeriv == 0) {
            xPoints[2] = xPoints[0] + initialXStep;
        } else {
   /* If initial derivative is known, use Newton's Method
    * it to find the next point.
    */
            xPoints[2] = xPoints[0] - (yPoints[0]) / initialFDeriv;
        }

           /* Now check to make sure that xPoints[2]
    * is within the hi-lo bounds. If it isn't, then adjust it
    * so that it is.
    */
        if (xPoints[2] < boundLo || xPoints[2] > boundHi) {
       /* Switch the direction of the step */
            xPoints[2] = xPoints[0] - initialXStep;
            if (xPoints[2] < boundLo) {
           /* xPoints[2] is still too small, so we make
            * it boundLo
            */
                xPoints[2] = boundLo;
            }

            if (xPoints[2] > boundHi) {
           /* xPoints[2] is too large, then set it
            * to boundHi.
            */
                xPoints[2] = boundHi;
            }

            if (xPoints[2] == xPoints[0]) {
           /* We cannot have xPoints[0] and
            * xPoints[2] be the same.
            */
                if (xPoints[2] == boundLo) {
                    xPoints[2] = boundLo + ONE_PERCENT * boundSpread;
                } else {
                    xPoints[2] = boundHi - ONE_PERCENT * boundSpread;
                }
            }
        }
 /* Finally, try to call (*funcd) with xPoints[2], to make
    * that the function can return a value at that point.
    */
        dblHolder = new DoubleHolder(yPoints[2]);
        if (function.eval(xPoints[2], data, dblHolder).equals(ReturnStatus.FAILURE)) {
            String errMsg = String.format("%s: Supplied function failed at point %2.6e\n.", routine, xPoints[2]);
            return ReturnStatus.FAILURE;
        }

        yPoints[2] = dblHolder.get();
         /* Check if the new point meets the tolerance requirements */
        if (yPoints[2] == 0.0 ||
                (Math.abs(yPoints[2]) <= facc && Math.abs(xPoints[2] - xPoints[0]) <= xacc)) {
            solution.set(xPoints[2]);
            return ReturnStatus.SUCCESS;
        }


   /* Call secant method to find the root, or to get a
      third point, so that two of the three points bracket the root. */
        if (secantMethod(function, data, numIterations,
                xacc, facc, boundLo, boundHi,
                xPoints, yPoints,
                foundIt, bracketed, solution).equals(ReturnStatus.FAILURE)) {
            return ReturnStatus.FAILURE;
        } else if (foundIt.get()) {
            return ReturnStatus.SUCCESS;
        } else if (bracketed.get()) {
            if (brentMethod(function, data, numIterations, xacc, facc,
                    xPoints, yPoints, solution).equals(ReturnStatus.FAILURE)) {
                return ReturnStatus.FAILURE;
            } else {
                return ReturnStatus.SUCCESS;
            }
        }

        /* Root was not bracketed, now try at the bounds
    */
        dblHolder = new DoubleHolder(fLo);
        if (function.eval(boundLo, data, dblHolder).equals(ReturnStatus.FAILURE)) {
            String errMsg = String.format("%s: Supplied function failed at point %2.6e.\n", routine, boundLo);
            return ReturnStatus.FAILURE;
        }

        fLo = dblHolder.get();
        if (fLo == 0.0 || (Math.abs(fLo) <= facc && Math.abs(boundLo - xPoints[0]) <= xacc)) {
            solution.set(boundLo);
            return ReturnStatus.SUCCESS;
        }

         /* If these points bracket the root, assign points so that
      xPoints[0] < xPoints[2] */
        if (yPoints[0] * fLo < 0) {
            xPoints[2] = xPoints[0];
            xPoints[0] = boundLo;

            yPoints[2] = yPoints[0];
            yPoints[0] = fLo;

        } else {
   /* Root is still not bracketed, so try at the upper bound now. */
            dblHolder = new DoubleHolder(fHi);

            if (function.eval(boundHi, data, dblHolder).equals(ReturnStatus.FAILURE)) {
                String errMsg = String.format("%s: Supplied function failed at point %2.6e.\n", routine, boundHi);
                logger.error(errMsg);
                return ReturnStatus.FAILURE;
            }

            fHi = dblHolder.get();
            if (fHi == 0.0 || (Math.abs(fHi) <= facc && Math.abs(boundHi - xPoints[0]) <= xacc)) {
                solution.set(boundHi);
                return ReturnStatus.SUCCESS;
            }

       /* If points bracket the root, assign xPoints[2] to boundHi */
            if (yPoints[0] * fHi < 0) {
                xPoints[2] = boundHi;
                yPoints[2] = fHi;
            } else
       /* Root could not be bracketed at the bounds. */ {
                String errMsg = String.format("%s: Function values (%2.6e,%2.6e) at bounds\n" +
                                "\t(%2.6e, %2.6e) imply no root exists.\n",
                        routine, fLo, fHi, boundLo, boundHi);
                logger.error(errMsg);
                return ReturnStatus.FAILURE;
            }
        }

          /* xPoints[0] and xPoints[2] bracket the root, but we need third
      point to do Brent method. Take the midpoint. */
        xPoints[1] = 0.5 * (xPoints[0] + xPoints[2]);
        dblHolder = new DoubleHolder(yPoints[1]);

        if (function.eval(xPoints[1], data, dblHolder).equals(ReturnStatus.FAILURE)) {
            String errMsg = String.format("%s: Supplied function failed at point %2.6e.\n", routine, xPoints[1]);
            logger.error(errMsg);
            return ReturnStatus.FAILURE;
        }

        yPoints[1] = dblHolder.get();

        if (yPoints[1] == 0.0 ||
                (Math.abs(yPoints[1]) <= facc && Math.abs(xPoints[1] - xPoints[0]) <= xacc)) {
            solution.set(xPoints[1]);
            return ReturnStatus.SUCCESS;
        }

        /*
        If we are here we failed to find a root
         */
        return ReturnStatus.FAILURE;
    }


    private static ReturnStatus secantMethod(SolvableFunction function,
                                             Object data,
                                             int numIterations,
                                             double xacc,
                                             double facc,
                                             double boundLo,
                                             double boundHi,
                                             double[] xPoints,
                                             double[] yPoints,
                                             BooleanHolder foundIt,
                                             BooleanHolder bracketed,
                                             DoubleHolder solution) {
        final String routine = "secantMethod";

        int j = numIterations;      /* Index */
        double dx;                   /* Delta x used for secant */

        foundIt.set(false);           /* Until solution is found. */
        bracketed.set(false);         /* Until bracketed */

        while (j-- > 0) {
        /* Swap points so that yPoints[0] is smaller than yPoints[2] */
            if (Math.abs(yPoints[0]) > Math.abs(yPoints[2])) {
                SWITCH(xPoints, 0, 2);
                SWITCH(yPoints, 0, 2);
            }

        /* Make sure that you do not divide by a very small value */
            if (Math.abs(yPoints[0] - yPoints[2]) <= facc) {
                if (yPoints[0] - yPoints[2] > 0) {
                    dx = -yPoints[0] * (xPoints[0] - xPoints[2]) / facc;
                } else {
                    dx = yPoints[0] * (xPoints[0] - xPoints[2]) / facc;
                }
            } else {
                dx = (xPoints[2] - xPoints[0]) * yPoints[0] / (yPoints[0] - yPoints[2]);
            }
            xPoints[1] = xPoints[0] + dx;

        /* Make sure that the point is within bounds
         */
            if (xPoints[1] < boundLo || xPoints[1] > boundHi) {
                return ReturnStatus.SUCCESS;     /* Not bracketed, not found */
            }

            DoubleHolder dblHolder = new DoubleHolder(yPoints[1]);
            if (function.eval(xPoints[1], data, dblHolder).equals(ReturnStatus.FAILURE)) {
                String errMsg = String.format("%s: Supplied function failed at point %2.6e.\n", routine, xPoints[1]);
                logger.error(errMsg);
                return ReturnStatus.FAILURE;     /* Not bracketed, not found */
            }

            yPoints[1] = dblHolder.get();

            if (yPoints[1] == 0.0 ||
                    (Math.abs(yPoints[1]) <= facc && Math.abs(xPoints[1] - xPoints[0]) <= xacc)) {
                solution.set(xPoints[1]);
                foundIt.set(true);
                bracketed.set(true);
                return ReturnStatus.SUCCESS;     /* Found, bracketed */
            }

            if ((yPoints[0] < 0 && yPoints[1] < 0 && yPoints[2] < 0) ||
                    (yPoints[0] > 0 && yPoints[1] > 0 && yPoints[2] > 0)) {
            /* Swap points so that yPoints[0] is always smallest
             */
                if (Math.abs(yPoints[0]) > Math.abs(yPoints[1])) {
                    xPoints[2] = xPoints[0];
                    yPoints[2] = yPoints[0];
                    xPoints[0] = xPoints[1];
                    yPoints[0] = yPoints[1];
                } else {
                    xPoints[2] = xPoints[1];
                    yPoints[2] = yPoints[1];
                }
                continue;
            } else {
            /* Root was bracketed.
             * Swap points so that yPoints[0]*yPoints[2] < 0
             */
                if (yPoints[0] * yPoints[2] > 0) {
                /* Make sure that you swap so that
                 * xPoints[0]<xPoints[1]<xPoints[2]
                 */
                    if (xPoints[1] < xPoints[0]) {
                        SWITCH(xPoints, 0, 1);
                        SWITCH(yPoints, 0, 1);
                    } else {
                        SWITCH(xPoints, 1, 2);
                        SWITCH(yPoints, 1, 2);
                    }
                }
            /* Root was bracketed, but not found.
             */
                bracketed.set(true);
                return ReturnStatus.SUCCESS;
            }
        } /* while */


    /* Root not bracketed or found.
     */
        return ReturnStatus.SUCCESS;
    }

    private static ReturnStatus brentMethod(SolvableFunction function,
                                            Object data,
                                            int numIterations,
                                            double xacc,
                                            double facc,
                                            double[] xPoints,
                                            double[] yPoints,
                                            DoubleHolder solution) {

        final String routine = "brentMethod";

        int j;                      /* Index */
        double ratio;                  /* (x3-x1)/(x2-x1) */
        double x31;                    /* x3-x1*/
        double x21;                    /* x2-x1*/
        double f21;                    /* f2-f1 */
        double f31;                    /* f3-f1 */
        double f32;                    /* f3-f2 */
        double xm;                     /* New point found using Brent method*/
        double fm = 0;                     /* f(xm) */

        double x1 = xPoints[0];        /* xN short hand for xPoints[n] */
        double x2 = xPoints[1];
        double x3 = xPoints[2];

        double f1 = yPoints[0];
        double f2 = yPoints[1];
        double f3 = yPoints[2];


        for (j = 1; j <= numIterations; j++) {
        /* Always want to be sure that f1 and f2 have opposite signs,
         * and f2 and f3 have the same sign.
         */
            if (f2 * f1 > 0.0) {
                double tmp = x1;
                x1 = x3;
                x3 = tmp;

                tmp = f1;
                f1 = f3;
                f3 = tmp;
            }

            f21 = f2 - f1;
            f32 = f3 - f2;
            f31 = f3 - f1;
            x21 = x2 - x1;
            x31 = x3 - x1;
        /* Check whether this is suitable for interpolation. When checking
         * for f21,f31,f32 = 0, we don't use IS_ALMOST_ZERO for efficiency
         * reasons. If the objective function has been truncated to a
         * certain number of digits of accuracy, f21,f31,or f32 could be
         * (were in one case) zero. In this case we need to protect against
         * division by zero. So we use bisection instead of brent.
         */
            ratio = (x3 - x1) / (x2 - x1);
            if (f3 * f31 < ratio * f2 * f21 || f21 == 0. || f31 == 0. || f32 == 0.) {
            /* This is not suitable, do bisection
             */
                x3 = x2;
                f3 = f2;

            } else {
                xm = x1 - (f1 / f21) * x21 + ((f1 * f2) / (f31 * f32)) * x31 -
                        ((f1 * f2) / (f21 * f32)) * x21;

                DoubleHolder dblHolder = new DoubleHolder(fm);

                if (function.eval(xm, data, dblHolder).equals(ReturnStatus.FAILURE)) {
                    String errMsg = String.format("%s: Supplied function failed at point %2.6e.\n", routine, xm);
                    return ReturnStatus.FAILURE;
                }

                fm = dblHolder.get();

                if (fm == 0.0 || (Math.abs(fm) <= facc && Math.abs(xm - x1) <= xacc)) {
                    solution.set(xm);
                    return ReturnStatus.SUCCESS;
                }
            /* If the new point and point1 bracket the root,
               replace point3 with the new point */
                if (fm * f1 < 0.0) {
                    x3 = xm;
                    f3 = fm;
                }
            /* If the root is not bracketed, replace point1 with new point,
               and point3 with point2 */
                else {
                    x1 = xm;
                    f1 = fm;
                    x3 = x2;
                    f3 = f2;
                }
            }
            x2 = 0.5 * (x1 + x3);

            DoubleHolder dblHolder = new DoubleHolder(f2);
            if (function.eval(x2, data, dblHolder).equals(ReturnStatus.FAILURE)) {
                String errMsg = String.format("%s: Supplied function failed at point %2.6e.\n", routine, x2);
                logger.error(errMsg);
                return ReturnStatus.FAILURE;
            }

            f2 = dblHolder.get();

            if (f2 == 0.0 || (Math.abs(f2) <= facc && Math.abs(x2 - x1) <= xacc)) {
                solution.set(x2);
                return ReturnStatus.SUCCESS;
            }
        }

        String errMsg = String.format("%s: Maximum number of iterations exceeded.\n", routine);
        logger.error(errMsg);
        return ReturnStatus.FAILURE;

    }

    public static void SWITCH(double[] array, int idx1, int idx2) {
        double tmp = array[idx1];
        array[idx1] = array[idx2];
        array[idx2] = tmp;
    }
}
