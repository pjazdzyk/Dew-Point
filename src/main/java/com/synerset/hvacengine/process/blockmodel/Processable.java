package com.synerset.hvacengine.process.blockmodel;

public interface Processable<K> {

    K runProcessCalculations();

    K getProcessResult();

}