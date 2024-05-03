package com.synerset.hvacengine.process;

import com.synerset.hvacengine.property.fluids.humidair.FlowOfHumidAir;

public interface ProcessBlock {
    ProcessResult runProcessCalculations();

    ProcessResult getProcessResults();

    InputConnector<FlowOfHumidAir> getAirFlowInputConnector();

    OutputConnector<FlowOfHumidAir> getAirFlowOutputConnector();

    String toConsoleOutput();
}