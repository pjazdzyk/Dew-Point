package com.synerset.hvacengine.process.cooling.dataobject;

import com.synerset.hvacengine.fluids.humidair.FlowOfHumidAir;
import com.synerset.hvacengine.fluids.liquidwater.FlowOfLiquidWater;
import com.synerset.hvacengine.process.ConsoleOutputFormatters;
import com.synerset.hvacengine.process.ProcessMode;
import com.synerset.hvacengine.process.ProcessResult;
import com.synerset.hvacengine.process.ProcessType;
import com.synerset.hvacengine.process.cooling.CoolantData;
import com.synerset.unitility.unitsystem.dimensionless.BypassFactor;
import com.synerset.unitility.unitsystem.thermodynamic.Power;

public record RealCoolingResult(ProcessType processType,
                                ProcessMode processMode,
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

    public RealCoolingResult withProcessMode(ProcessMode processMode){
        return new RealCoolingResult(processType, processMode, inletAirFlow,
                coolantData, outletAirFlow, heatOfProcess, condensateFlow, bypassFactor);
    }

    public static class Builder {
        private static final ProcessType processType = ProcessType.COOLING;
        private ProcessMode processMode;
        private FlowOfHumidAir inletAirFlow;
        private CoolantData coolantData;
        private FlowOfHumidAir outletAirFlow;
        private Power heatOfProcess;
        private FlowOfLiquidWater condensateFlow;
        private BypassFactor bypassFactor;

        public Builder inletAirFlow(FlowOfHumidAir inletAirFlow) {
            this.inletAirFlow = inletAirFlow;
            return this;
        }

        public Builder coolantData(CoolantData inputCoolantData) {
            this.coolantData = inputCoolantData;
            return this;
        }

        public Builder outletAirFlow(FlowOfHumidAir outletAirFlow) {
            this.outletAirFlow = outletAirFlow;
            return this;
        }

        public Builder heatOfProcess(Power heatOfProcess) {
            this.heatOfProcess = heatOfProcess;
            return this;
        }

        public Builder condensateFlow(FlowOfLiquidWater condensateFlow) {
            this.condensateFlow = condensateFlow;
            return this;
        }

        public Builder processMode(ProcessMode processMode) {
            this.processMode = processMode;
            return this;
        }

        public Builder bypassFactor(BypassFactor bypassFactor) {
            this.bypassFactor = bypassFactor;
            return this;
        }

        public RealCoolingResult build() {
            return new RealCoolingResult(processType, processMode, inletAirFlow,
                    coolantData, outletAirFlow, heatOfProcess, condensateFlow, bypassFactor);
        }
    }

    public static Builder builder() {
        return new Builder();
    }

}
