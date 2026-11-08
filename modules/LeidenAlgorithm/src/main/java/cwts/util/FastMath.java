package cwts.util;

/**
 * Fast implementations of mathematical functions.
 *
 * <p>
 * All methods in this class are static.
 * </p>
 *
 * @author Ludo Waltman
 * @author Nees Jan van Eck
 * @author Vincent Traag
 */
public final class FastMath
{
    /**
     * Calculates {@code exp(exponent)} using a fast implementation.
     *
     * @param exponent Exponent
     *
     * @return exp(exponent)
     */
    public static double fastExp(double exponent)
    {
        if (exponent < -256d)
            return 0;

        exponent = 1d + exponent / 256d;
        exponent *= exponent;
        exponent *= exponent;
        exponent *= exponent;
        exponent *= exponent;
        exponent *= exponent;
        exponent *= exponent;
        exponent *= exponent;
        exponent *= exponent;
        return exponent;
    }

    /**
     * Calculates {@code base ^ exponent} using a fast implementation.
     *
     * @param base     Base
     * @param exponent Exponent
     *
     * @return base ^ exponent
     */
    public static double fastPow(double base, int exponent)
    {
        double power;
        int i;

        if (exponent > 0)
        {
            power = base;
            for (i = 1; i < exponent; i++)
                power *= base;
        }
        else if (exponent < 0)
        {
            power = 1 / base;
            for (i = -1; i > exponent; i--)
                power /= base;
        }
        else
            power = 1;
        return power;
    }

    private FastMath()
    {
    }
}
