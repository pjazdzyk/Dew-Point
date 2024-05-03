package com.synerset.hvacengine.property.solids.ice;

import com.synerset.unitility.unitsystem.thermodynamic.Temperature;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.withPrecision;

class IceEquationsTest {

    double ICE_DENSITY_ACCURACY = 0.65;
    double ICE_CP_ACCURACY = 0.0015;
    double ICE_K_ACCURACY = 0.008;

    @ParameterizedTest
    @MethodSource("densityInlineData")
    @DisplayName("should return density as in tables when ice temperature is given")
    void density_shouldReturnDensityOfIce_whenIceTemperatureIsGiven(double tx, double expectedDensity) {
        // Given
        // When
        double actualIceDensity = IceEquations.density(tx);

        // Then
        assertThat(actualIceDensity).isEqualTo(expectedDensity, withPrecision(ICE_DENSITY_ACCURACY));
    }

    static Stream<Arguments> densityInlineData() {
        return Stream.of(
                Arguments.of(0, 916.2),
                Arguments.of(-5, 917.5),
                Arguments.of(-10, 918.9),
                Arguments.of(-15, 919.4),
                Arguments.of(-20, 919.4),
                Arguments.of(-25, 919.6),
                Arguments.of(-30, 920.0),
                Arguments.of(-35, 920.4),
                Arguments.of(-40, 920.8),
                Arguments.of(-50, 921.6),
                Arguments.of(-60, 922.4),
                Arguments.of(-70, 923.3),
                Arguments.of(-80, 924.1),
                Arguments.of(-90, 924.9),
                Arguments.of(-100, 925.7)
        );
    }

    @ParameterizedTest
    @MethodSource("specificHeatInlineData")
    @DisplayName("should return specific heat as in tables when ice temperature is given")
    void specificHeat_shouldReturnSpecificHeatOfIce_whenIceTemperatureIsGiven(double tx, double expectedSpecificHeat) {
        // Given
        // When
        double actualSpecificHeat = IceEquations.specificHeat(tx);

        // Then
        assertThat(actualSpecificHeat).isEqualTo(expectedSpecificHeat, withPrecision(ICE_CP_ACCURACY));
    }

    static Stream<Arguments> specificHeatInlineData() {
        return Stream.of(
                Arguments.of(0, 2.05),
                Arguments.of(-5, 2.027),
                Arguments.of(-10, 2.000),
                Arguments.of(-15, 1.972),
                Arguments.of(-20, 1.943),
                Arguments.of(-25, 1.913),
                Arguments.of(-30, 1.882),
                Arguments.of(-35, 1.851),
                Arguments.of(-40, 1.818),
                Arguments.of(-50, 1.751),
                Arguments.of(-60, 1.681),
                Arguments.of(-70, 1.609),
                Arguments.of(-80, 1.536),
                Arguments.of(-90, 1.463),
                Arguments.of(-100, 1.389)
        );
    }

    @ParameterizedTest
    @MethodSource("thermalConductivityInlineData")
    @DisplayName("should return thermal conductivity as in tables when ice temperature is given")
    void thermalConductivity_shouldReturnThermalConductivityOfIce_whenIceTemperatureIsGiven(double tx, double expectedThermalConductivity) {
        // Given
        // When
        double actualThermalConductivity = IceEquations.thermalConductivity(tx);

        // Then
        assertThat(actualThermalConductivity).isEqualTo(expectedThermalConductivity, withPrecision(ICE_K_ACCURACY));
    }

    static Stream<Arguments> thermalConductivityInlineData() {
        return Stream.of(
                Arguments.of(0, 2.22),
                Arguments.of(-5, 2.25),
                Arguments.of(-10, 2.3),
                Arguments.of(-15, 2.34),
                Arguments.of(-20, 2.39),
                Arguments.of(-25, 2.45),
                Arguments.of(-30, 2.5),
                Arguments.of(-35, 2.57),
                Arguments.of(-40, 2.63),
                Arguments.of(-50, 2.76),
                Arguments.of(-60, 2.9),
                Arguments.of(-70, 3.05),
                Arguments.of(-80, 3.19),
                Arguments.of(-90, 3.34),
                Arguments.of(-100, 3.48)
        );
    }

    @Test
    @DisplayName("should all Ice methods using primitive values return the same output as methods using Unitility objects arguments")
    void shouldAllIceMethodsWithPrimitiveArguments_returnTheSameOutput() {
        // Given
        double iceTempVal = -15.5;
        Temperature iceTemp = Temperature.ofCelsius(iceTempVal);

        double expectedDensVal = IceEquations.density(iceTempVal);
        double expectedThermCondVal = IceEquations.thermalConductivity(iceTempVal);
        double expectedSpecHeatVal = IceEquations.specificHeat(iceTempVal);
        double expectedSpecEnthalpyVal = IceEquations.specificEnthalpy(iceTempVal);

        // When
        double actualDensVal = IceEquations.density(iceTemp).getInKilogramsPerCubicMeters();
        double actualThermCondVal = IceEquations.thermalConductivity(iceTemp).getInWattsPerMeterKelvin();
        double actualSpecHeatVal = IceEquations.specificHeat(iceTemp).getInKiloJoulesPerKiloGramKelvin();
        double actualSpecEnthalpyVal = IceEquations.specificEnthalpy(iceTemp).getInKiloJoulesPerKiloGram();

        // Then
        assertThat(actualDensVal).isEqualTo(expectedDensVal);
        assertThat(actualThermCondVal).isEqualTo(expectedThermCondVal);
        assertThat(actualSpecHeatVal).isEqualTo(expectedSpecHeatVal);
        assertThat(actualSpecEnthalpyVal).isEqualTo(expectedSpecEnthalpyVal);
    }

}