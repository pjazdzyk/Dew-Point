package com.synerset.hvacengine.process.computation;

public class OutputConnector<T> {

    private T connectorData;

    public OutputConnector() {
    }

    public OutputConnector(T connectorData) {
        this.connectorData = connectorData;
    }

    public T getConnectorData() {
        return connectorData;
    }

    public void setConnectorData(T connectorData) {
        this.connectorData = connectorData;
    }

    public static <K> OutputConnector<K> of(K connectorData) {
        return new OutputConnector<>(connectorData);
    }

    public static <K> OutputConnector<K> createEmpty(Class<K> inferType) {
        return new OutputConnector<>();
    }

}