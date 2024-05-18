package com.synerset.hvacengine.common;

import com.synerset.hvacengine.common.exception.HvacEngineArgumentException;

import java.util.Arrays;
import java.util.OptionalDouble;

/**
 * The `MathUtils` class provides utility methods for mathematical operations.
 */
public final class MathUtils {

    private MathUtils() {
    }

    /**
     * Calculates the arithmetic average of the given values.
     *
     * @param values The values for which to calculate the average.
     * @return The arithmetic average of the values.
     * @throws HvacEngineArgumentException If no values are provided.
     */
    public static double arithmeticAverage(double... values) {
        OptionalDouble optionalDouble = Arrays.stream(values).average();
        if (optionalDouble.isPresent())
            return optionalDouble.getAsDouble();
        throw new HvacEngineArgumentException("No values are provided.");
    }

    /**
     * Performs linear interpolation to estimate a value at a given point.
     *
     * @param x1   The first x-coordinate.
     * @param fx1 The function value at x1.
     * @param x2   The second x-coordinate.
     * @param fx2 The function value at x2.
     * @param x    The x-coordinate at which to estimate the function value.
     * @return The estimated function value at x using linear interpolation.
     */
    public static double linearInterpolation(double x1, double fx1, double x2, double fx2, double x) {
        return fx1 + ((x - x1) / (x2 - x1)) * (fx2 - fx1);
    }

}