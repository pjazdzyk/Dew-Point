package com.synerset.hvacengine.process.common;

import com.synerset.hvacengine.fluids.humidair.FlowOfHumidAir;
import com.synerset.hvacengine.process.computation.ProcessResult;
import com.synerset.hvacengine.process.computation.ProcessSequenceExecutor;
import com.synerset.hvacengine.process.cooling.CoolantData;
import com.synerset.hvacengine.process.cooling.CoolingFromPowerNode;
import com.synerset.hvacengine.process.heating.HeatingFromTemperatureNode;
import com.synerset.unitility.unitsystem.thermodynamic.Power;
import com.synerset.unitility.unitsystem.thermodynamic.Temperature;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ProcessSequenceExecutorTest {

    @Test
    void coolingAndHeatingSequenceWithExecutor(){
        // Given
        FlowOfHumidAir flowOfHumidAir = FlowOfHumidAir.ofValues(35, 55, 5000);

        CoolingFromPowerNode coolingNode = CoolingFromPowerNode.of(
                flowOfHumidAir,
                CoolantData.of(Temperature.ofCelsius(7),Temperature.ofCelsius(14)),
                Power.ofKiloWatts(-20)
        );

        HeatingFromTemperatureNode heatingNode = HeatingFromTemperatureNode.of(Temperature.ofCelsius(50));

        System.out.println(heatingNode.toConsoleOutput());

        ProcessSequenceExecutor executor = new ProcessSequenceExecutor();
        executor.addProcessNode(coolingNode);
        executor.addProcessNode(heatingNode);
        executor.runAll();

        List<ProcessResult> allResults = executor.getAllResults();
        ProcessResult lastResult = executor.getLastResult();
        executor.getAllProcessNodes().forEach(node -> System.out.println(node.toConsoleOutput()));

        assertThat(allResults).hasSize(2);
        assertThat(lastResult.heatOfProcess()).isEqualTo(Power.ofWatts(32906.20633616318));

    }


}
