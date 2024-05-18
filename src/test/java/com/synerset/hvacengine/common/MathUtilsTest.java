package com.synerset.hvacengine.common;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class MathUtilsTest {

    @Test
    void calcArithmeticAverage() {
        // Given
        var a = 2;
        var b = 6;
        var c = 4;
        var expectedAverage = 4;

        // When
        var actualAverage = MathUtils.arithmeticAverage(a, b, c);

        // Then
        assertThat(actualAverage).isEqualTo(expectedAverage);
    }

    @Test
    void linearInterpolation() {
        // Given
        var x1 = 100;
        var f_x1 = 10;
        var x2 = 200;
        var f_x2 = 20;
        var x = 150;
        var expectedInterpolatedValue = 15;

        // When
        var actualInterpolatedValue = MathUtils.linearInterpolation(x1, f_x1, x2, f_x2, x);

        // Then
        assertThat(actualInterpolatedValue).isEqualTo(expectedInterpolatedValue);
    }
}