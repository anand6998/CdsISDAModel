package com.anand.analytics.isdamodel.utils;

import com.anand.analytics.isdamodel.date.Day;
import org.apache.commons.lang.Validate;
import org.apache.log4j.Logger;

/**
 * Created by Anand on 12/1/2014.
 */
public class CdsUtils {
    final static Logger logger = Logger.getLogger(CdsUtils.class);

    public static ReturnStatus binarySearchLong(Day xDesired,
                                                Day[] xArray,
                                                IntHolder exact,
                                                IntHolder loBound,
                                                IntHolder hiBound) {
        try {
        /* xArray needs to be a sorted array */
            //Used to count # of search steps
            int count;
            //Index of low estimate
            int lo;
            //Index of high estimate
            int hi;
            //Index of best estimate
            int mid = 0;

        /* If we are not within range we are done */
            if (xDesired.isBefore(xArray[0])) {
                exact.set(-1);
                loBound.set(-1);
                hiBound.set(0);
                return ReturnStatus.SUCCESS;
            } else if (xDesired.isAfter(xArray[xArray.length - 1])) {
                exact.set(-1);
                loBound.set(xArray.length - 1);
                hiBound.set(xArray.length);

                return ReturnStatus.SUCCESS;
            }
        /* arraySize of 1 we are done */
            if (xArray.length == 1) {
                Validate.isTrue (xDesired.isEqual(xArray[0]), "xDesired != xArray[0]");
                exact.set(0);
                loBound.set(-1);
                hiBound.set(xArray.length);
                return ReturnStatus.SUCCESS;
            }

            lo = 0;
            hi = xArray.length - 2;

            /**
             * Do binary search to find pair of x's which surround the desired x value
             */
            for (count = xArray.length + 1; count > 0; count--) {
                mid = (hi + lo) / 2;
                if (xDesired.isBefore(xArray[mid])) {
                    hi = mid - 1;
                } else if (xDesired.isAfter(xArray[mid + 1])) {
                    lo = mid + 1;
                } else
                    break;
            }

            if (count == 0) {
                logger.error("xarray is not in increasing order");
                return ReturnStatus.FAILURE;
            }

            /**
             * Protect against a run of x values which are the same
             * Set 2 surrounding indices to be lo and hi
             * Note that there is no danger of running off the end since
             * the only way for x[lo] = x[hi] is for both to be equal
             * to xDesired. But from the check at beginning we know X[N-1] <> xDesired
             */

            Validate.isTrue(mid < xArray.length, "mid >= xArray.lenght");
            Validate.isTrue(xDesired.isAfter(xArray[mid]) || xDesired.isEqual(xArray[mid]), "xDesired < xArray[mid]");
            Validate.isTrue(xDesired.isBefore(xArray[mid + 1]) || xDesired.isEqual(xArray[mid + 1]), "xDesired > xArray[mid + 1]");

            lo = mid;
            hi = mid + 1;

            if (xArray[lo].isEqual(xDesired))
                exact.set(lo);
            else if (xArray[hi].isEqual(xDesired))
                exact.set(hi);
            else
                exact.set(-1);

            if (loBound != null) {
                while (lo >= 0 && (xArray[lo].isAfter(xDesired) || xArray[lo].isEqual(xDesired)))
                    --lo;
                if (lo >= 0)
                    loBound.set(lo);
                else
                    loBound.set(-1);
            }

            if (hiBound != null) {
                while (hi < xArray.length && (xArray[hi].isBefore(xDesired) || xArray[hi].isEqual(xDesired)))
                    ++hi;

                if (hi < xArray.length)
                    hiBound.set(hi);
                else
                    hiBound.set(xArray.length);

            }
            return ReturnStatus.SUCCESS;
        } catch (Exception ex) {
            logger.error(ex);
            return ReturnStatus.FAILURE;
        }
    }

}
