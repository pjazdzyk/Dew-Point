package com.synerset.hvacengine.common;

public class SharedSettings {

    private SharedSettings() {
        throw new IllegalStateException();
    }

    public static final boolean SHOW_SOLVER_DEBUG_LOGS = false;
    public static final boolean SHOW_SOLVER_SUMMARY_LOG = false;

}