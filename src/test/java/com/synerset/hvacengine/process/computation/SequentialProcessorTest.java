package com.synerset.hvacengine.process.computation;

import com.synerset.hvacengine.fluids.humidair.FlowOfHumidAir;
import com.synerset.hvacengine.process.cooling.CoolantData;
import com.synerset.hvacengine.process.cooling.CoolingFromTemperatureNode;
import com.synerset.hvacengine.process.heating.HeatingFromHumidityNode;
import com.synerset.hvacengine.process.mixing.MixingNode;
import com.synerset.unitility.unitsystem.humidity.RelativeHumidity;
import com.synerset.unitility.unitsystem.thermodynamic.Temperature;
import org.junit.jupiter.api.Test;

class SequentialProcessorTest {

    @Test
    void coolingAndHeatingSequenceWithExecutor() {
        // Given
        FlowOfHumidAir inletFlow = FlowOfHumidAir.ofValues(35, 55, 5000);
        FlowOfHumidAir recirculationAir = FlowOfHumidAir.ofValues(25, 70, 1000);
        Temperature targetTemperature = Temperature.ofCelsius(25);
        CoolantData coolantData = CoolantData.ofValues(7, 14);
        RelativeHumidity targetRH = RelativeHumidity.ofPercentage(30);

        MixingNode mixingNode = MixingNode.of(inletFlow, recirculationAir);
        CoolingFromTemperatureNode coolingNode = CoolingFromTemperatureNode.of(coolantData, targetTemperature);
        HeatingFromHumidityNode heatingNode = HeatingFromHumidityNode.of(targetRH);

        SequentialProcessor processComputation = SequentialProcessor.of(mixingNode, coolingNode, heatingNode);
        processComputation.runAll();
        String consoleOutputLastResult = processComputation.toConsoleOutputLastResult();
        String consoleOutputAllResults = processComputation.toConsoleOutputAllResults();
        System.out.println(consoleOutputAllResults);
    }

}