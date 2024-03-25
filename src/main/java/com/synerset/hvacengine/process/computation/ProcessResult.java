package com.synerset.hvacengine.process.computation;

import com.synerset.hvacengine.fluids.humidair.FlowOfHumidAir;
import com.synerset.hvacengine.fluids.liquidwater.FlowOfLiquidWater;
import com.synerset.unitility.unitsystem.thermodynamic.Power;

public interface ProcessResult {
    FlowOfHumidAir outletAirFlow();
    FlowOfLiquidWater condensateFlow();
    Power heatOfProcess();
}