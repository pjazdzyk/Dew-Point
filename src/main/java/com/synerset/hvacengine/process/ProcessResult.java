package com.synerset.hvacengine.process;

import com.synerset.hvacengine.property.fluids.humidair.FlowOfHumidAir;
import com.synerset.unitility.unitsystem.thermodynamic.Power;

public interface ProcessResult {
    ProcessType processType();
    ProcessMode processMode();
    FlowOfHumidAir inletAirFlow();
    FlowOfHumidAir outletAirFlow();
    Power heatOfProcess();
    String toConsoleOutput();
}