package com.synerset.hvacengine.process.blockmodel;

public class ConnectorOutput<T> implements Connector<T> {

    private T connectorData;
    private ConnectorInput<T> connectedInputConnector;

    public ConnectorOutput() {
    }

    public ConnectorOutput(T connectorData) {
        this.connectorData = connectorData;
    }

    @Override
    public T getConnectorData() {
        return connectorData;
    }

    @Override
    public void setConnectorData(T connectorData) {
        this.connectorData = connectorData;
    }

    public ConnectorInput<T> getConnectedInputConnector() {
        return connectedInputConnector;
    }

    public void setConnectedInputConnector(ConnectorInput<T> connectedInputConnector) {
        this.connectedInputConnector = connectedInputConnector;
    }

    // Static factory methods
    public static <K> ConnectorOutput<K> of(K connectorData) {
        return new ConnectorOutput<>(connectorData);
    }

    public static <K> ConnectorOutput<K> of(Class<K> inferType) {
        return new ConnectorOutput<>();
    }

}