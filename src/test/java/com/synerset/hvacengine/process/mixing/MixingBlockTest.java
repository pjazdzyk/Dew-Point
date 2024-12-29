package com.synerset.hvacengine.process.mixing;

import com.synerset.hvacengine.process.ProcessType;
import com.synerset.hvacengine.process.mixing.dataobject.MixingResult;
import com.synerset.hvacengine.process.source.SimpleDataSource;
import com.synerset.hvacengine.property.fluids.humidair.FlowOfHumidAir;
import com.synerset.hvacengine.property.fluids.humidair.HumidAir;
import com.synerset.unitility.unitsystem.common.Ratio;
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
class MixingBlockTest {

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
        SimpleDataSource<FlowOfHumidAir> airFlowSource = SimpleDataSource.of(inletFlow);

        FlowOfHumidAir recirculationFlow = FlowOfHumidAir.of(recirculationAir, MassFlow.ofKilogramsPerHour(30_000));
        SimpleDataSource<FlowOfHumidAir> mixingFlowDataSource = SimpleDataSource.of(recirculationFlow);

        HumidityRatio expectedHumRatio = HumidityRatio.ofKilogramPerKilogram(0.0061);
        SpecificEnthalpy expectedEnthalpy = SpecificEnthalpy.ofKiloJoulePerKiloGram(37.61);
        Temperature expectedTemperature = Temperature.ofCelsius(22);
        RelativeHumidity expectedRH = RelativeHumidity.ofPercentage(36.29);

        // When
        Mixing mixingBlock = Mixing.of(airFlowSource, mixingFlowDataSource);
        MixingResult processResults = mixingBlock.runProcessCalculations();
        List<FlowOfHumidAir> actualMixingFlows = mixingBlock.getUnwrappedMixingFlows();

        // Then
        assertThat(processResults).isNotNull();
        assertThat(processResults.outletAirFlow()).isNotNull();
        assertThat(processResults.inletAirFlow()).isEqualTo(inletFlow);
        assertThat(processResults.heatOfProcess()).isEqualTo(Power.ofWatts(0));
        assertThat(actualMixingFlows).hasSize(1);
        assertThat(processResults.processType()).isEqualTo(ProcessType.MIXING);
        assertThat(processResults.processMode()).isEqualTo(MixingMode.SIMPLE_MIXING);
        assertThat(processResults.dryAirMassFreshAirRatio()).isEqualTo(Ratio.ofPercentage(40.11989227594873));

        FlowOfHumidAir outletAirFlow = processResults.outletAirFlow();
        assertThat(outletAirFlow.getPressure()).isEqualTo(inletAir.getPressure());
        assertThat(outletAirFlow.getTemperature().getInCelsius()).isEqualTo(expectedTemperature.getInCelsius(), withPrecision(3.5E-2));
        assertThat(outletAirFlow.getRelativeHumidity().getInPercent()).isEqualTo(expectedRH.getInPercent(), withPrecision(1E-2));
        assertThat(outletAirFlow.getHumidityRatio().getInKilogramPerKilogram()).isEqualTo(expectedHumRatio.getInKilogramPerKilogram(), withPrecision(5E-5));
        assertThat(outletAirFlow.getSpecificEnthalpy().getInKiloJoulesPerKiloGram()).isEqualTo(expectedEnthalpy.getInKiloJoulesPerKiloGram(), withPrecision(8E-2));

        // Resetting mixing flows, expecting outlet air = inlet air
        mixingBlock.resetMixingFlows();
        mixingBlock.runProcessCalculations();
        MixingResult processResultsNoMixing = mixingBlock.getProcessResult();
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
        SimpleDataSource<FlowOfHumidAir> airFlowSource = SimpleDataSource.of(inletFlow);

        FlowOfHumidAir recirculationFlow1 = FlowOfHumidAir.of(recirculationAir1, MassFlow.ofKilogramsPerHour(30_000));
        SimpleDataSource<FlowOfHumidAir> mixingFlowDataSource1 = SimpleDataSource.of(recirculationFlow1);
        FlowOfHumidAir recirculationFlow2 = FlowOfHumidAir.of(recirculationAir2, MassFlow.ofKilogramsPerHour(10_000));
        SimpleDataSource<FlowOfHumidAir> mixingFlowDataSource2 = SimpleDataSource.of(recirculationFlow2);

        HumidityRatio expectedHumRatio = HumidityRatio.ofKilogramPerKilogram(0.005341094418842777);
        SpecificEnthalpy expectedEnthalpy = SpecificEnthalpy.ofKiloJoulePerKiloGram(30.31164274263736);
        Temperature expectedTemperature = Temperature.ofCelsius(16.712398366511252);
        RelativeHumidity expectedRH = RelativeHumidity.ofPercentage(44.16305608648766);

        MassFlow sumOfAllFlows = inletFlow.getMassFlow()
                .plus(recirculationFlow1.getMassFlow())
                .plus(recirculationFlow2.getMassFlow())
                .toKiloGramPerHour();

        // When
        Mixing mixingBlock = Mixing.of(airFlowSource, List.of(mixingFlowDataSource1, mixingFlowDataSource2));
        MixingResult processResults = mixingBlock.runProcessCalculations();
        assertThat(processResults.processType()).isEqualTo(ProcessType.MIXING);
        assertThat(processResults.processMode()).isEqualTo(MixingMode.MULTIPLE_MIXING);
        assertThat(processResults.dryAirMassFreshAirRatio()).isEqualTo(Ratio.ofPercentage(33.40744717861534));
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
        mixingBlock.addMixingFlowDataSource(SimpleDataSource.of(outletAirFlow));
        mixingBlock.runProcessCalculations();
        MixingResult processResultsNewFlow = mixingBlock.getProcessResult();

        assertThat(mixingBlock.getUnwrappedMixingFlows()).hasSize(3);
        assertThat(processResultsNewFlow.outletAirFlow().getMassFlow()).isEqualTo(sumOfAllFlows.plus(outletAirFlow.getMassFlow()));

        // Changing flow in one mixing flows by 50% of dry air mass flow in first recirculation flow
        mixingFlowDataSource1.setSourceData(recirculationFlow1.withMassFlow(MassFlow.ofKilogramsPerHour(20_0000)));
        mixingBlock.runProcessCalculations();

        assertThat(mixingBlock.getUnwrappedMixingFlows()).hasSize(3);
        assertThat(processResultsNewFlow.outletAirFlow().getMassFlow().toKiloGramPerHour().getValue()).isEqualTo(120000, withPrecision(1E-10));

    }
}