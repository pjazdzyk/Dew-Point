package com.synerset.hvacengine.process.cooling;

import com.synerset.hvacengine.fluids.humidair.FlowOfHumidAir;
import com.synerset.hvacengine.fluids.liquidwater.FlowOfLiquidWater;
import com.synerset.hvacengine.process.computation.ProcessResult;
import com.synerset.unitility.unitsystem.dimensionless.BypassFactor;
import com.synerset.unitility.unitsystem.thermodynamic.Power;

/**
 * Represents the result of an air cooling process.
 */
public record AirCoolingResult(FlowOfHumidAir outletAirFlow,
                               Power heatOfProcess,
                               FlowOfLiquidWater condensateFlow,
                               BypassFactor bypassFactor) implements ProcessResult {

    public static class Builder {
        private FlowOfHumidAir outletAirFlow;
        private Power heatOfProcess;
        private FlowOfLiquidWater condensateFlow;
        private BypassFactor bypassFactor;

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

        public Builder bypassFactor(BypassFactor bypassFactor) {
            this.bypassFactor = bypassFactor;
            return this;
        }

        public AirCoolingResult build() {
            return new AirCoolingResult(outletAirFlow, heatOfProcess, condensateFlow, bypassFactor);
        }
    }

    public static Builder builder() {
        return new Builder();
    }

}