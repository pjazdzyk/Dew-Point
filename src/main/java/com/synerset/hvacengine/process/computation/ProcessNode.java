package com.synerset.hvacengine.process.computation;

import com.synerset.hvacengine.fluids.humidair.FlowOfHumidAir;

public interface ProcessNode {
    void runProcessCalculations();

    ProcessResult getProcessResults();

    InputConnector<FlowOfHumidAir> getAirFlowInputConnector();

    OutputConnector<FlowOfHumidAir> getAirFlowOutputConnector();

    String toConsoleOutput();
}