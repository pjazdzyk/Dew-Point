package com.synerset.hvacengine.property.fluids.liquidwater;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.withPrecision;

class LiquidWaterEquationsTest {

    @Test
    @DisplayName("should return liquid water specific enthalpy when water temperature is given")
    void specificEnthalpy_shouldReturnLiquidWaterSpecificEnthalpy_whenWaterTemperatureIsGiven() {
        // Given
        double waterTemp = 15.0;

        // When
        double actualSpecEnthalpy = LiquidWaterEquations.specificEnthalpy(waterTemp);

        // Then
        double expectedSpecEnthalpy = 62.83139309762801;
        assertThat(actualSpecEnthalpy).isEqualTo(expectedSpecEnthalpy);

    }

    @Test
    @DisplayName("should return liquid water density when water temperature is given")
    void specificEnthalpy_shouldReturnLiquidWaterDensity_whenWaterTemperatureIsGiven() {
        // Given
        double waterTemp = 15.0;

        // When
        double actualDensity = LiquidWaterEquations.density(waterTemp);

        // Then
        double expectedDensity = 998.8844003066922;
        assertThat(actualDensity).isEqualTo(expectedDensity);

    }

    @ParameterizedTest
    @MethodSource("specificHeatInlineData")
    @DisplayName("should return liquid water specific heat when water temperature is given")
    void specificEnthalpy_shouldReturnLiquidWaterSpecificHeat_whenWaterTemperatureIsGiven(double inletTemp, double expectedSpecHeat) {
        // Given
        // When
        double actualSpecHeat = LiquidWaterEquations.specificHeat(inletTemp);

        // Then
        assertThat(actualSpecHeat).isEqualTo(expectedSpecHeat, withPrecision(1E-4));
    }

    //INLINE DATA SEED -> Based on https://www.engineeringtoolbox.com/specific-heat-capacity-water-d_660.html
    static Stream<Arguments> specificHeatInlineData() {
        return Stream.of(
                Arguments.of(0.01, 4.2199),
                Arguments.of(10, 4.1955),
                Arguments.of(20, 4.1844),
                Arguments.of(25, 4.1816),
                Arguments.of(30, 4.1801),
                Arguments.of(40, 4.1796),
                Arguments.of(50, 4.1815),
                Arguments.of(60, 4.1851),
                Arguments.of(70, 4.1902),
                Arguments.of(80, 4.1969),
                Arguments.of(90, 4.2053)
        );
    }
}
