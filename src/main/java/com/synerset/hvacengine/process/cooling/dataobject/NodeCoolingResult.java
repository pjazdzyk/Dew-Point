package com.synerset.hvacengine.process.cooling.dataobject;

import com.synerset.hvacengine.fluids.humidair.FlowOfHumidAir;
import com.synerset.hvacengine.fluids.liquidwater.FlowOfLiquidWater;
import com.synerset.hvacengine.process.common.ConsoleOutputFormatters;
import com.synerset.hvacengine.process.ProcessResult;
import com.synerset.unitility.unitsystem.dimensionless.BypassFactor;
import com.synerset.unitility.unitsystem.thermodynamic.Power;

/**
 * Represents the result of an air cooling process.
 */
public record NodeCoolingResult(FlowOfHumidAir inletAirFlow,
                                FlowOfHumidAir outletAirFlow,
                                Power heatOfProcess,
                                FlowOfLiquidWater condensateFlow,
                                FlowOfLiquidWater coolantSupplyFlow,
                                FlowOfLiquidWater coolantReturnFlow,
                                BypassFactor bypassFactor) implements ProcessResult {

    @Override
    public String toConsoleOutput() {
        return ConsoleOutputFormatters.coolingNodeConsoleOutput(this);
    }

    public static class Builder {
        private FlowOfHumidAir inletAirFlow;
        private FlowOfHumidAir outletAirFlow;
        private Power heatOfProcess;
        private FlowOfLiquidWater condensateFlow;
        private FlowOfLiquidWater coolantSupplyFlow;
        private FlowOfLiquidWater coolantReturnFlow;
        private BypassFactor bypassFactor;

        public Builder inletAirFlow(FlowOfHumidAir inletAirFlow) {
            this.inletAirFlow = inletAirFlow;
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

        public Builder coolantSupplyFlow(FlowOfLiquidWater coolantSupplyFlow) {
            this.coolantSupplyFlow = coolantSupplyFlow;
            return this;
        }

        public Builder coolantReturnFlow(FlowOfLiquidWater coolantReturnFlow) {
            this.coolantReturnFlow = coolantReturnFlow;
            return this;
        }

        public Builder bypassFactor(BypassFactor bypassFactor) {
            this.bypassFactor = bypassFactor;
            return this;
        }

        public NodeCoolingResult build() {
            return new NodeCoolingResult(inletAirFlow, outletAirFlow, heatOfProcess, condensateFlow,
                    coolantSupplyFlow, coolantReturnFlow, bypassFactor);
        }
    }

    public static Builder builder() {
        return new Builder();
    }

}