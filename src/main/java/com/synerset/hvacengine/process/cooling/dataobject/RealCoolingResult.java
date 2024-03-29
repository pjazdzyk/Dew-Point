package com.synerset.hvacengine.process.cooling.dataobject;

import com.synerset.hvacengine.fluids.humidair.FlowOfHumidAir;
import com.synerset.hvacengine.fluids.liquidwater.FlowOfLiquidWater;
import com.synerset.hvacengine.process.ProcessResult;
import com.synerset.hvacengine.process.common.ConsoleOutputFormatters;
import com.synerset.hvacengine.process.cooling.CoolantData;
import com.synerset.unitility.unitsystem.dimensionless.BypassFactor;
import com.synerset.unitility.unitsystem.thermodynamic.Power;

public record RealCoolingResult(
        FlowOfHumidAir inletAirFlow,
        CoolantData coolantData,
        FlowOfHumidAir outletAirFlow,
        Power heatOfProcess,
        FlowOfLiquidWater condensateFlow,
        BypassFactor bypassFactor) implements ProcessResult {
    @Override
    public String toConsoleOutput() {
        return ConsoleOutputFormatters.coolingConsoleOutput(this);
    }

    public static class CoolingResultBuilder {
        private FlowOfHumidAir inletAirFlow;
        private CoolantData coolantData;
        private FlowOfHumidAir outletAirFlow;
        private Power heatOfProcess;
        private FlowOfLiquidWater condensateFlow;
        private BypassFactor bypassFactor;

        public CoolingResultBuilder inletAirFlow(FlowOfHumidAir inletAirFlow)

        {
            this.inletAirFlow = inletAirFlow;
            return this;
        }

        public CoolingResultBuilder coolantData(CoolantData inputCoolantData) {
            this.coolantData = inputCoolantData;
            return this;
        }

        public CoolingResultBuilder outletAirFlow(FlowOfHumidAir outletAirFlow) {
            this.outletAirFlow = outletAirFlow;
            return this;
        }

        public CoolingResultBuilder heatOfProcess(Power heatOfProcess) {
            this.heatOfProcess = heatOfProcess;
            return this;
        }

        public CoolingResultBuilder condensateFlow(FlowOfLiquidWater condensateFlow) {
            this.condensateFlow = condensateFlow;
            return this;
        }

        public CoolingResultBuilder bypassFactor(BypassFactor bypassFactor) {
            this.bypassFactor = bypassFactor;
            return this;
        }

        public RealCoolingResult build() {
            return new RealCoolingResult(inletAirFlow, coolantData, outletAirFlow, heatOfProcess, condensateFlow, bypassFactor);
        }

    }

    public static CoolingResultBuilder builder() {
        return new CoolingResultBuilder();
    }

}
