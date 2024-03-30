package com.synerset.hvacengine.process.heating.dataobject;

import com.synerset.hvacengine.fluids.humidair.FlowOfHumidAir;
import com.synerset.hvacengine.process.ProcessResult;
import com.synerset.hvacengine.process.common.ConsoleOutputFormatters;
import com.synerset.unitility.unitsystem.thermodynamic.Power;

/**
 * Represents the result of an air heating process.
 */
public record HeatingResult(FlowOfHumidAir inletAirFlow,
                            FlowOfHumidAir outletAirFlow,
                            Power heatOfProcess) implements ProcessResult {
    @Override
    public String toConsoleOutput() {
        return ConsoleOutputFormatters.heatingConsoleOutput(this);
    }

    public static class AirHeatingResultBuilder {
        private FlowOfHumidAir inletAirFlow;
        private FlowOfHumidAir outletAirFlow;
        private Power heatOfProcess;

        public AirHeatingResultBuilder inletAirFlow(FlowOfHumidAir inletAirFlow) {
            this.inletAirFlow = inletAirFlow;
            return this;
        }

        public AirHeatingResultBuilder outletAirFlow(FlowOfHumidAir outletAirFlow) {
            this.outletAirFlow = outletAirFlow;
            return this;
        }

        public AirHeatingResultBuilder heatOfProcess(Power heatOfProcess) {
            this.heatOfProcess = heatOfProcess;
            return this;
        }

        public HeatingResult build() {
            return new HeatingResult(inletAirFlow, outletAirFlow, heatOfProcess);
        }
    }

    public static AirHeatingResultBuilder builder(){
        return new AirHeatingResultBuilder();
    }

}