package com.synerset.hvacengine.process.computation;

public class InputConnector<T> {

    private T connectorData;
    private OutputConnector<T> genericConnector;

    public InputConnector() {
    }

    public InputConnector(OutputConnector<T> genericConnector) {
        this.genericConnector = genericConnector;
        updateConnectorData();
    }

    public void updateConnectorData() {
        if (getParentConnector() != null) {
            setConnectorData(getParentConnector().getConnectorData());
        }
    }

    public T getConnectorData() {
        return connectorData;
    }

    public void setConnectorData(T connectorData) {
        this.connectorData = connectorData;
    }

    public void connectToOutputConnector(OutputConnector<T> sourceConnector) {
        this.genericConnector = sourceConnector;
    }

    public OutputConnector<T> getParentConnector() {
        return genericConnector;
    }

    public static <K> InputConnector<K> of(K connectorData) {
        return new InputConnector<>(OutputConnector.of(connectorData));
    }

    public static <K> InputConnector<K> createEmpty(Class<K> inferType) {
        return new InputConnector<>();
    }

}