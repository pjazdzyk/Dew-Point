package com.synerset.hvacengine.process.blockmodel;

public class ConnectorInput<T> implements Connector<T> {

    private T connectorData;
    private ConnectorOutput<T> dataSourceConnector;

    public ConnectorInput() {
    }

    public ConnectorInput(ConnectorOutput<T> dataSourceConnector) {
        this.dataSourceConnector = dataSourceConnector;
        updateConnectorData();
    }

    public void updateConnectorData() {
        if (getParentConnector() != null) {
            setConnectorData(getParentConnector().getConnectorData());
        }
    }

    @Override
    public T getConnectorData() {
        return connectorData;
    }

    @Override
    public void setConnectorData(T connectorData) {
        this.connectorData = connectorData;
    }

    public void connectAndConsumeDataFrom(ConnectorOutput<T> sourceConnector) {
        this.dataSourceConnector = sourceConnector;
        sourceConnector.setConnectedInputConnector(this);
        updateConnectorData();
    }

    public ConnectorOutput<T> getParentConnector() {
        return dataSourceConnector;
    }

    // Static factory methods
    public static <K> ConnectorInput<K> of(K connectorData) {
        return new ConnectorInput<>(ConnectorOutput.of(connectorData));
    }

    public static <K> ConnectorInput<K> of(ConnectorOutput<K> dataSourceConnector) {
        return new ConnectorInput<>(dataSourceConnector);
    }

    public static <K> ConnectorInput<K> of(Class<K> inferType) {
        return new ConnectorInput<>();
    }

}