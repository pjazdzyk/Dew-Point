package com.synerset.hvacengine.process;

import com.synerset.hvacengine.common.ConsolePrintable;
import com.synerset.hvacengine.common.validation.CommonValidators;
import com.synerset.hvacengine.process.blockmodel.OutputConnection;
import com.synerset.hvacengine.process.blockmodel.Processable;
import com.synerset.hvacengine.property.fluids.humidair.FlowOfHumidAir;

public interface HvacProcessBlock extends ConsolePrintable, Processable<ProcessResult>, AirFlowInputConnection, AirFlowOutputConnection {

    ProcessType getProcessType();

    default void connectAirFlowDataSource(OutputConnection<FlowOfHumidAir> blockWithAirFlowOutput) {
        CommonValidators.requireNotNull(blockWithAirFlowOutput);
        getInputConnector().connectAndConsumeDataFrom(blockWithAirFlowOutput.getOutputConnector());
    }
}