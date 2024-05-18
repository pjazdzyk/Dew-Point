package com.synerset.hvacengine.process.source;

import com.synerset.hvacengine.common.validation.CommonValidators;
import com.synerset.hvacengine.process.blockmodel.ConnectorOutput;
import com.synerset.hvacengine.process.blockmodel.OutputConnection;

public class SimpleDataSource<T> implements OutputConnection<T> {

    private final ConnectorOutput<T> outputConnector;

    public SimpleDataSource(T value) {
        CommonValidators.requireNotNull(value);
        this.outputConnector = ConnectorOutput.of(value);
    }

    @Override
    public ConnectorOutput<T> getOutputConnector() {
        return outputConnector;
    }

    public T getSourceData() {
        return outputConnector.getConnectorData();
    }

    public void setSourceData(T value) {
        CommonValidators.requireNotNull(value);
        outputConnector.setConnectorData(value);
    }

    // Static factory methods
    public static <K> SimpleDataSource<K> of(K value) {
        return new SimpleDataSource<>(value);
    }

}