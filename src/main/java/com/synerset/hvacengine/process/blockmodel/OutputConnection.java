package com.synerset.hvacengine.process.blockmodel;

import com.synerset.hvacengine.common.validation.CommonValidators;

public interface OutputConnection<K> {

    ConnectorOutput<K> getOutputConnector();

    default void connectThisOutputToTargetInput(InputConnection<K> processWithInput) {
        CommonValidators.requireNotNull(processWithInput);
        ConnectorInput<K> targetInputConnector = processWithInput.getInputConnector();
        targetInputConnector.connectAndConsumeDataFrom(getOutputConnector());
    }

}