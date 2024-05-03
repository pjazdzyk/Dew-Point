package com.synerset.hvacengine.process.computation;

import com.synerset.hvacengine.process.ProcessResult;
import com.synerset.hvacengine.process.ProcessType;
import com.synerset.hvacengine.process.cooling.CoolantData;
import com.synerset.hvacengine.process.cooling.CoolingFromTemperatureBlock;
import com.synerset.hvacengine.process.heating.HeatingFromHumidityBlock;
import com.synerset.hvacengine.process.mixing.MixingBlock;
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
        FlowOfHumidAir inletFlow = FlowOfHumidAir.ofValues(35, 55, 1000);
        FlowOfHumidAir recirculationAir = FlowOfHumidAir.ofValues(25, 70, 1000);
        Temperature targetCoolingTemperature = Temperature.ofCelsius(25);
        CoolantData coolantData = CoolantData.ofValues(7, 14);
        RelativeHumidity targetRH = RelativeHumidity.ofPercentage(30);

        MixingBlock mixingNode = MixingBlock.of(inletFlow, recirculationAir);
        CoolingFromTemperatureBlock coolingNode = CoolingFromTemperatureBlock.of(coolantData, targetCoolingTemperature);
        HeatingFromHumidityBlock heatingNode = HeatingFromHumidityBlock.of(targetRH);

        // When
        SequentialProcessingEngine processComputation = SequentialProcessingEngine.of(mixingNode, coolingNode);
        int processPosition = processComputation.addProcessNode(heatingNode);
        ProcessResult lastResult = processComputation.runCalculationsForAllNodes();
        List<ProcessResult> allResults = processComputation.getAllResults();

        // Then
        assertThat(lastResult).isNotNull();
        assertThat(allResults).isNotNull().hasSize(3);
        assertThat(processComputation.getAllProcessNodes()).hasSize(3);
        assertThat(processComputation.getLastResult().heatOfProcess().getValue()).isEqualTo(10000, withPrecision(100d));
        FlowOfHumidAir outletAirFlow = lastResult.outletAirFlow();
        assertThat(outletAirFlow.getDryAirMassFlow()).isEqualTo(inletFlow.getDryAirMassFlow().plus(recirculationAir.getDryAirMassFlow()));
        assertThat(outletAirFlow.getTemperature().getValue()).isEqualTo(40.7, withPrecision(0.1));
        assertThat(outletAirFlow.getRelativeHumidity().getValue()).isEqualTo(30, withPrecision(1E-11));
        assertThat(processComputation.toConsoleOutputAllResults()).contains("MIXING","COOLING","HEATING");
        assertThat(processComputation.toConsoleOutputLastResult()).contains("HEATING");
        assertThat(processPosition).isEqualTo(2);
        assertThat(processComputation.getResultsOfType(ProcessType.HEATING).get(0)).isEqualTo(processComputation.getAllResults().get(processPosition));
        assertThat(SequentialProcessingEngine.createEmpty()).isNotNull().isInstanceOf(SequentialProcessingEngine.class);
    }

}