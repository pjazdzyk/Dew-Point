package com.synerset.hvacengine.process;

import com.synerset.hvacengine.common.ConsolePrintable;
import com.synerset.hvacengine.property.fluids.humidair.FlowOfHumidAir;
import com.synerset.unitility.unitsystem.thermodynamic.Power;

public interface ProcessResult extends ConsolePrintable {
    ProcessType processType();
    FlowOfHumidAir inletAirFlow();
    FlowOfHumidAir outletAirFlow();
    Power heatOfProcess();
}