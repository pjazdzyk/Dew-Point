package com.synerset.hvacengine.process.cooling.dataobject;

import com.synerset.hvacengine.fluids.humidair.FlowOfHumidAir;
import com.synerset.hvacengine.process.ProcessResult;
import com.synerset.hvacengine.process.common.ConsoleOutputFormatters;
import com.synerset.unitility.unitsystem.thermodynamic.Power;

public record DryCoolingResult(
        FlowOfHumidAir inletAirFlow,
        FlowOfHumidAir outletAirFlow,
        Power heatOfProcess) implements ProcessResult {
    @Override
    public String toConsoleOutput() {
        return ConsoleOutputFormatters.dryCoolingConsoleOutput(this);
    }

    public static class DryCoolingResultBuilder {
        private FlowOfHumidAir inletAirFlow;
        private FlowOfHumidAir outletAirFlow;
        private Power heatOfProcess;

        public DryCoolingResultBuilder inletAirFlow(FlowOfHumidAir inletAirFlow) {
            this.inletAirFlow = inletAirFlow;
            return this;
        }

        public DryCoolingResultBuilder outletAirFlow(FlowOfHumidAir outletAirFlow) {
            this.outletAirFlow = outletAirFlow;
            return this;
        }

        public DryCoolingResultBuilder heatOfProcess(Power heatOfProcess) {
            this.heatOfProcess = heatOfProcess;
            return this;
        }

        public DryCoolingResult build() {
            return new DryCoolingResult(inletAirFlow, outletAirFlow, heatOfProcess);
        }
    }

    public static DryCoolingResultBuilder builder() {
        return new DryCoolingResultBuilder();
    }

}