package com.synerset.hvacengine.process.blockmodel;

public interface Connector<T> {

    T getConnectorData();

    void setConnectorData(T connectorData);

}