package io.github.pjazdzyk.hvaclib.physics;

import io.github.pjazdzyk.hvaclib.common.Limiters;
import io.github.pjazdzyk.hvaclib.exceptions.AirPhysicsArgumentException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

public class PhysicsOfAirExceptionTests {

    static final double Pat = 100_000.0;

    @Test
    @DisplayName("should throw an exception when temperature is lower than minimum limiter is given")
    public void calcMaPs_shouldThrowException_whenTemperatureIsLowerThanMinimumLimitIsGiven() {
        // Arrange
        var tempOutsideThreshold = Limiters.MIN_T - 1;

        // Assert
        assertThrows(AirPhysicsArgumentException.class, () -> PhysicsOfAir.calcMaPs(tempOutsideThreshold));
    }

    @Test
    @DisplayName("should thrown an exception when negative relative humidity is given")
    public void calcMaTdp_shouldThrowException_whenNegativeRelativeHumidityIsGiven() {
        // Arrange
        var airTemp = 20;
        var negativeRH = -20;

        // Assert
        assertThrows(AirPhysicsArgumentException.class, () -> PhysicsOfAir.calcMaTdp(airTemp, negativeRH, Pat));
    }

}