package com.synerset.hvacengine.process.procedure;

import com.synerset.hvacengine.process.ProcessResult;
import com.synerset.hvacengine.process.ProcessType;
import com.synerset.hvacengine.process.cooling.CoolantData;
import com.synerset.hvacengine.process.cooling.CoolingFromTemperature;
import com.synerset.hvacengine.process.heating.HeatingFromHumidity;
import com.synerset.hvacengine.process.mixing.Mixing;
import com.synerset.hvacengine.process.source.SimpleDataSource;
import com.synerset.hvacengine.property.fluids.humidair.FlowOfHumidAir;
import com.synerset.unitility.unitsystem.humidity.RelativeHumidity;
import com.synerset.unitility.unitsystem.thermodynamic.Temperature;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.withPrecision;

class SequentialProcessingEngineTest {

    /**
     * Process sequence scenario:
     * 1. Taking summer ambient air of: 35oC / 55% 1000 m3/h
     * 2. Mixing with internal recirculation air of: 25oC / 70% 1000m3/h
     * 3. Cooling resulting mixture to target temperature of 25oC
     * 4. Heating resulting flow to reach 30% RH
     * 5. Based on i-x chart outcome result temp =~ 40.7oC
     */
    @Test
    void sequentialProcessingEngineTest() {
        // Given
        // Inlet Air
        FlowOfHumidAir inletFlow = FlowOfHumidAir.ofValues(35, 55, 1000);
        SimpleDataSource<FlowOfHumidAir> airFlowSource = new SimpleDataSource<>(inletFlow);

        // Mixing block config
        FlowOfHumidAir recirculationAir = FlowOfHumidAir.ofValues(25, 70, 1000);
        SimpleDataSource<FlowOfHumidAir> mixingFlowSource = SimpleDataSource.of(recirculationAir);
        Mixing mixingBlock = Mixing.of(airFlowSource, List.of(mixingFlowSource));

        // Cooling block config
        Temperature targetCoolingTemperature = Temperature.ofCelsius(25);
        CoolantData coolantData = CoolantData.ofValues(7, 14);
        SimpleDataSource<CoolantData> coolantDataSource = SimpleDataSource.of(coolantData);
        SimpleDataSource<Temperature> temperatureDataSource = SimpleDataSource.of(targetCoolingTemperature);
        CoolingFromTemperature coolingBlock = CoolingFromTemperature.of(coolantDataSource, temperatureDataSource);

        // Heating block config
        RelativeHumidity targetRH = RelativeHumidity.ofPercentage(30);
        SimpleDataSource<RelativeHumidity> humiditySourceBlock = new SimpleDataSource<>(targetRH);
        HeatingFromHumidity heatingBlock = HeatingFromHumidity.of(humiditySourceBlock);

        // When
        SequentialProcessingEngine processComputation = SequentialProcessingEngine.of(mixingBlock, coolingBlock);
        int processPosition = processComputation.addProcessNode(heatingBlock);
        ProcessResult lastResult = processComputation.runCalculationsForAllNodes();
        List<ProcessResult> allResults = processComputation.getProcessResults();

        // Then
        assertThat(lastResult).isNotNull();
        assertThat(allResults).isNotNull().hasSize(3);
        assertThat(processComputation.getAllProcessBlocks()).hasSize(3);
        assertThat(processComputation.getLastResult().heatOfProcess().getValue()).isEqualTo(10000, withPrecision(100d));
        FlowOfHumidAir outletAirFlow = lastResult.outletAirFlow();
        assertThat(outletAirFlow.getDryAirMassFlow()).isEqualTo(inletFlow.getDryAirMassFlow().plus(recirculationAir.getDryAirMassFlow()));
        assertThat(outletAirFlow.getTemperature().getValue()).isEqualTo(40.7, withPrecision(0.1));
        assertThat(outletAirFlow.getRelativeHumidity().getValue()).isEqualTo(30, withPrecision(1E-11));
        assertThat(processComputation.toConsoleOutput()).contains("MIXING", "COOLING", "HEATING");
        assertThat(processComputation.toConsoleOutputLastResult()).contains("HEATING");
        assertThat(processPosition).isEqualTo(2);
        assertThat(processComputation.getResults(ProcessType.HEATING).get(0)).isEqualTo(processComputation.getProcessResults().get(processPosition));
        assertThat(SequentialProcessingEngine.of()).isNotNull().isInstanceOf(SequentialProcessingEngine.class);
    }

}