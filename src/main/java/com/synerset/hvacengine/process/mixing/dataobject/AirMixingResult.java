package com.synerset.hvacengine.process.mixing.dataobject;

import com.synerset.hvacengine.fluids.humidair.FlowOfHumidAir;
import com.synerset.hvacengine.process.ProcessResult;
import com.synerset.hvacengine.process.common.ConsoleOutputFormatters;
import com.synerset.unitility.unitsystem.thermodynamic.Power;

import java.util.ArrayList;
import java.util.List;

/**
 * The AirMixingResult record represents the result of a mixing process.
 */
public record AirMixingResult(FlowOfHumidAir inletAirFlow,
                              FlowOfHumidAir outletAirFlow,
                              Power heatOfProcess,
                              List<FlowOfHumidAir> recirculationFlows) implements ProcessResult {

    @Override
    public String toConsoleOutput() {
        return ConsoleOutputFormatters.mixingConsoleOutput(this);
    }

    public static class AirMixingResultBuilder {
        private FlowOfHumidAir inletAirFlow;
        private FlowOfHumidAir outletAirFlow;
        private Power heatOfProcess = Power.ofWatts(0);
        private List<FlowOfHumidAir> recirculationFlows;

        public AirMixingResultBuilder() {
            recirculationFlows = new ArrayList<>();
        }

        public AirMixingResultBuilder inletAirFlow(FlowOfHumidAir inletAirFlow) {
            this.inletAirFlow = inletAirFlow;
            return this;
        }

        public AirMixingResultBuilder outletAirFlow(FlowOfHumidAir outletAirFlow) {
            this.outletAirFlow = outletAirFlow;
            return this;
        }

        public AirMixingResultBuilder heatOfProcess(Power heatOfProcess) {
            this.heatOfProcess = heatOfProcess;
            return this;
        }

        public AirMixingResultBuilder recirculationFlows(List<FlowOfHumidAir> recirculationFlows) {
            this.recirculationFlows = recirculationFlows;
            return this;
        }

        public AirMixingResult build() {
            return new AirMixingResult(inletAirFlow, outletAirFlow, heatOfProcess, recirculationFlows);
        }

    }

    public static AirMixingResultBuilder builder() {
        return new AirMixingResultBuilder();
    }

}