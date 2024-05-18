package com.synerset.hvacengine.process.blockmodel;

import com.synerset.hvacengine.common.validation.CommonValidators;

public interface InputConnection<K> {

    ConnectorInput<K> getInputConnector();

    default void connectThisInputToTargetOutput(OutputConnection<K> processWithOutput) {
        CommonValidators.requireNotNull(processWithOutput);
        ConnectorOutput<K> targetOutputConnector = processWithOutput.getOutputConnector();
        getInputConnector().connectAndConsumeDataFrom(targetOutputConnector);
    }

}