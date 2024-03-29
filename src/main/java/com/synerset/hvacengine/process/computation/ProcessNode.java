package com.synerset.hvacengine.process.computation;

import com.synerset.hvacengine.fluids.humidair.FlowOfHumidAir;
import com.synerset.hvacengine.process.ProcessResult;

public interface ProcessNode {
    void runProcessCalculations();

    ProcessResult getProcessResults();

    InputConnector<FlowOfHumidAir> getAirFlowInputConnector();

    OutputConnector<FlowOfHumidAir> getAirFlowOutputConnector();

    String toConsoleOutput();
}