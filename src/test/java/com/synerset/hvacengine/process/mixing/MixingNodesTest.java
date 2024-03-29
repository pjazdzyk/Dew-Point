package com.synerset.hvacengine.process.mixing;

import com.synerset.hvacengine.fluids.humidair.FlowOfHumidAir;
import com.synerset.hvacengine.fluids.humidair.HumidAir;
import com.synerset.hvacengine.process.mixing.dataobject.AirMixingResult;
import com.synerset.unitility.unitsystem.flow.MassFlow;
import com.synerset.unitility.unitsystem.humidity.HumidityRatio;
import com.synerset.unitility.unitsystem.humidity.RelativeHumidity;
import com.synerset.unitility.unitsystem.thermodynamic.Power;
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
class MixingNodesTest {

    @Test
    @DisplayName("Mixing node: should mix two humid air flows")
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

        HumidityRatio expectedHumRatio = HumidityRatio.ofKilogramPerKilogram(0.0061);
        SpecificEnthalpy expectedEnthalpy = SpecificEnthalpy.ofKiloJoulePerKiloGram(37.61);
        Temperature expectedTemperature = Temperature.ofCelsius(22);
        RelativeHumidity expectedRH = RelativeHumidity.ofPercentage(36.29);

        // When
        MixingNode mixingNode = MixingNode.of(inletFlow, recirculationFlow);
        AirMixingResult processResults = mixingNode.runProcessCalculations();
        List<FlowOfHumidAir> actualMixingFlows = mixingNode.getMixingFlows();

        // Then
        assertThat(processResults).isNotNull();
        assertThat(processResults.outletAirFlow()).isNotNull();
        assertThat(processResults.inletAirFlow()).isEqualTo(inletFlow);
        assertThat(processResults.heatOfProcess()).isEqualTo(Power.ofWatts(0));
        assertThat(actualMixingFlows).hasSize(1);

        FlowOfHumidAir outletAirFlow = processResults.outletAirFlow();
        assertThat(outletAirFlow.getPressure()).isEqualTo(inletAir.getPressure());
        assertThat(outletAirFlow.getTemperature().getInCelsius()).isEqualTo(expectedTemperature.getInCelsius(), withPrecision(3.5E-2));
        assertThat(outletAirFlow.getRelativeHumidity().getInPercent()).isEqualTo(expectedRH.getInPercent(), withPrecision(1E-2));
        assertThat(outletAirFlow.getHumidityRatio().getInKilogramPerKilogram()).isEqualTo(expectedHumRatio.getInKilogramPerKilogram(), withPrecision(5E-5));
        assertThat(outletAirFlow.getSpecificEnthalpy().getInKiloJoulesPerKiloGram()).isEqualTo(expectedEnthalpy.getInKiloJoulesPerKiloGram(), withPrecision(8E-2));

        // Resetting mixing flows, expecting outlet air = inlet air
        mixingNode.resetMixingFlows();
        mixingNode.runProcessCalculations();
        AirMixingResult processResultsNoMixing = mixingNode.getProcessResults();
        assertThat(processResultsNoMixing.inletAirFlow()).isEqualTo(processResultsNoMixing.outletAirFlow());
    }

    @Test
    @DisplayName("Mixing node: should mix multiple humid air flows")
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

        HumidityRatio expectedHumRatio = HumidityRatio.ofKilogramPerKilogram(0.005341094418842777);
        SpecificEnthalpy expectedEnthalpy = SpecificEnthalpy.ofKiloJoulePerKiloGram(30.31164274263736);
        Temperature expectedTemperature = Temperature.ofCelsius(16.712398366511252);
        RelativeHumidity expectedRH = RelativeHumidity.ofPercentage(44.16305608648766);

        MassFlow sumOfAllFlows = inletFlow.getMassFlow()
                .plus(recirculationFlow1.getMassFlow())
                .plus(recirculationFlow2.getMassFlow())
                .toKiloGramPerHour();

        // When
        MixingNode mixingNode = MixingNode.of(inletFlow, List.of(recirculationFlow1, recirculationFlow2));
        AirMixingResult processResults = mixingNode.runProcessCalculations();

        // Then
        assertThat(processResults).isNotNull();
        assertThat(processResults.outletAirFlow()).isNotNull();
        assertThat(processResults.inletAirFlow()).isEqualTo(inletFlow);

        FlowOfHumidAir outletAirFlow = processResults.outletAirFlow();
        assertThat(sumOfAllFlows.getValue()).isEqualTo(outletAirFlow.getMassFlow().toKiloGramPerHour().getValue(), withPrecision(1E-11));
        assertThat(outletAirFlow.getPressure()).isEqualTo(inletAir.getPressure());
        assertThat(outletAirFlow.getTemperature().getInCelsius()).isEqualTo(expectedTemperature.getInCelsius(), withPrecision(3.5E-2));
        assertThat(outletAirFlow.getRelativeHumidity().getInPercent()).isEqualTo(expectedRH.getInPercent(), withPrecision(1E-2));
        assertThat(outletAirFlow.getHumidityRatio().getInKilogramPerKilogram()).isEqualTo(expectedHumRatio.getInKilogramPerKilogram(), withPrecision(5E-5));
        assertThat(outletAirFlow.getSpecificEnthalpy().getInKiloJoulesPerKiloGram()).isEqualTo(expectedEnthalpy.getInKiloJoulesPerKiloGram(), withPrecision(8E-2));

        // Adding another flow
        mixingNode.addMixingFlow(outletAirFlow);
        mixingNode.runProcessCalculations();
        AirMixingResult processResultsNewFlow = mixingNode.getProcessResults();

        assertThat(mixingNode.getMixingFlows()).hasSize(3);
        assertThat(processResultsNewFlow.outletAirFlow().getMassFlow()).isEqualTo(sumOfAllFlows.plus(outletAirFlow.getMassFlow()));
    }
}