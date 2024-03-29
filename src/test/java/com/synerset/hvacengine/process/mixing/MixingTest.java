package com.synerset.hvacengine.process.mixing;

import com.synerset.hvacengine.fluids.humidair.FlowOfHumidAir;
import com.synerset.hvacengine.fluids.humidair.HumidAir;
import com.synerset.unitility.unitsystem.flow.MassFlow;
import com.synerset.unitility.unitsystem.humidity.HumidityRatio;
import com.synerset.unitility.unitsystem.humidity.RelativeHumidity;
import com.synerset.unitility.unitsystem.thermodynamic.Pressure;
import com.synerset.unitility.unitsystem.thermodynamic.SpecificEnthalpy;
import com.synerset.unitility.unitsystem.thermodynamic.Temperature;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.withPrecision;

/**
 * This test case is based on example from technical literature:
 * B.Lipska - Projektowanie Wentylacji i Klimatyzacji, Podstawy uzdatniania powietrza. Gliwice 2014.
 * Section: 2.1, page: 37
 */
class MixingTest {
/*
    @Test
    @DisplayName("Mixing: should mix two humid air flows")
    void shouldMixTwoHumidAirFlows() {
        // Given
        HumidAir inletAir = HumidAir.of(
                Pressure.ofHectoPascal(987),
                Temperature.ofCelsius(10),
                RelativeHumidity.ofPercentage(40)
        );

        HumidAir recirculationAir = HumidAir.of(
                Pressure.ofHectoPascal(987),
                Temperature.ofCelsius(30),
                RelativeHumidity.ofPercentage(30)
        );

        FlowOfHumidAir inletFlow = FlowOfHumidAir.of(inletAir, MassFlow.ofKilogramsPerHour(20_000));
        FlowOfHumidAir recirculationFlow = FlowOfHumidAir.of(recirculationAir, MassFlow.ofKilogramsPerHour(30_000));
        MixingStrategy mixingStrategy = MixingStrategy.of(inletFlow, recirculationFlow);

        HumidityRatio expectedHumRatio = HumidityRatio.ofKilogramPerKilogram(0.0061);
        SpecificEnthalpy expectedEnthalpy = SpecificEnthalpy.ofKiloJoulePerKiloGram(37.61);
        Temperature expectedTemperature = Temperature.ofCelsius(22);
        RelativeHumidity expectedRH = RelativeHumidity.ofPercentage(36.29);

        // When
        Mixing mixing = Mixing.of(mixingStrategy);

        // Then
        assertThat(mixing).isNotNull();
        assertThat(mixing.getOutletAir()).isNotNull();
        assertThat(mixing.getInputInletAir()).isEqualTo(inletFlow);
        assertThat(mixing.getMixingStrategy()).isEqualTo(mixingStrategy);
        assertThat(mixing.getOutletPressure()).isEqualTo(inletAir.getPressure());
        assertThat(mixing.getOutletTemperature().getInCelsius()).isEqualTo(expectedTemperature.getInCelsius(), withPrecision(3.5E-2));
        assertThat(mixing.getOutletRelativeHumidity().getInPercent()).isEqualTo(expectedRH.getInPercent(), withPrecision(1E-2));
        assertThat(mixing.getOutletHumidityRatio().getInKilogramPerKilogram()).isEqualTo(expectedHumRatio.getInKilogramPerKilogram(), withPrecision(5E-5));
        assertThat(mixing.getOutletSpecificEnthalpy().getInKiloJoulesPerKiloGram()).isEqualTo(expectedEnthalpy.getInKiloJoulesPerKiloGram(), withPrecision(8E-2));
    }

    @Test
    @DisplayName("Mixing: should mix multiple humid air flows")
    void shouldMixMultipleHumidAirFlows() {
        // Given
        HumidAir inletAir = HumidAir.of(
                Pressure.ofHectoPascal(987),
                Temperature.ofCelsius(10),
                RelativeHumidity.ofPercentage(40)
        );

        HumidAir recirculationAir1 = HumidAir.of(
                Pressure.ofHectoPascal(987),
                Temperature.ofCelsius(30),
                RelativeHumidity.ofPercentage(30)
        );

        HumidAir recirculationAir2 = HumidAir.of(
                Pressure.ofHectoPascal(987),
                Temperature.ofCelsius(-10),
                RelativeHumidity.ofPercentage(90)
        );

        FlowOfHumidAir inletFlow = FlowOfHumidAir.of(inletAir, MassFlow.ofKilogramsPerHour(20_000));
        FlowOfHumidAir recirculationFlow1 = FlowOfHumidAir.of(recirculationAir1, MassFlow.ofKilogramsPerHour(30_000));
        FlowOfHumidAir recirculationFlow2 = FlowOfHumidAir.of(recirculationAir2, MassFlow.ofKilogramsPerHour(10_000));
        MixingStrategy mixingStrategy = MixingStrategy.of(inletFlow, List.of(recirculationFlow1, recirculationFlow2));

        HumidityRatio expectedHumRatio = HumidityRatio.ofKilogramPerKilogram(0.005341094418842777);
        SpecificEnthalpy expectedEnthalpy = SpecificEnthalpy.ofKiloJoulePerKiloGram(30.31164274263736);
        Temperature expectedTemperature = Temperature.ofCelsius(16.712398366511252);
        RelativeHumidity expectedRH = RelativeHumidity.ofPercentage(44.16305608648766);

        // When
        Mixing mixing = Mixing.of(mixingStrategy);

        // Then
        assertThat(mixing).isNotNull();
        assertThat(mixing.getOutletAir()).isNotNull();
        assertThat(mixing.getInputInletAir()).isEqualTo(inletFlow);
        assertThat(mixing.getMixingStrategy()).isEqualTo(mixingStrategy);
        assertThat(mixing.getOutletPressure()).isEqualTo(inletAir.getPressure());
        assertThat(mixing.getOutletTemperature().getInCelsius()).isEqualTo(expectedTemperature.getInCelsius(), withPrecision(3.5E-2));
        assertThat(mixing.getOutletRelativeHumidity().getInPercent()).isEqualTo(expectedRH.getInPercent(), withPrecision(1E-2));
        assertThat(mixing.getOutletHumidityRatio().getInKilogramPerKilogram()).isEqualTo(expectedHumRatio.getInKilogramPerKilogram(), withPrecision(5E-5));
        assertThat(mixing.getOutletSpecificEnthalpy().getInKiloJoulesPerKiloGram()).isEqualTo(expectedEnthalpy.getInKiloJoulesPerKiloGram(), withPrecision(8E-2));
    }
*/
}