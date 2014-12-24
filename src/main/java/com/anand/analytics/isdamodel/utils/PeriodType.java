package com.anand.analytics.isdamodel.utils;

/**
 * Created by aanand on 10/21/2014.
 */
public enum PeriodType {
    M, D, U, A, Y, S, Q, W, F, E, B, G, H, I, J, K, L, T, V;

    public final static PeriodType getPeriodType(char ch) {
        switch (ch) {
            case 'M':
                return M;
            case 'D':
                return D;
            case 'U':
                return U;
            case 'Y':
                return Y;
            case 'S':
                return S;
            case 'Q':
                return Q;
            case 'W':
                return W;
            case 'F':
                return F;
            case 'E':
                return E;
            case 'B':
                return B;
            case 'G':
                return G;
            case 'H':
                return H;
            case 'I':
                return I;
            case 'J':
                return J;
            case 'K':
                return K;
            case 'L':
                return L;
            case 'T':
                return T;
            case 'V':
                return V;
            default:
                throw new RuntimeException("Unknown period type");
        }
    }
}
