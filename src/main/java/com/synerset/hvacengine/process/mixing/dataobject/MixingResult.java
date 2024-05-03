package com.synerset.hvacengine.process.mixing.dataobject;

import com.synerset.hvacengine.process.ConsoleOutputFormatters;
import com.synerset.hvacengine.process.ProcessMode;
import com.synerset.hvacengine.process.ProcessResult;
import com.synerset.hvacengine.process.ProcessType;
import com.synerset.hvacengine.property.fluids.humidair.FlowOfHumidAir;
import com.synerset.unitility.unitsystem.thermodynamic.Power;

import java.util.ArrayList;
import java.util.Collection;

/**
 * The AirMixingResult record represents the result of a mixing process.
 */
public record MixingResult(ProcessType processType,
                           ProcessMode processMode,
                           FlowOfHumidAir inletAirFlow,
                           FlowOfHumidAir outletAirFlow,
                           Power heatOfProcess,
                           Collection<FlowOfHumidAir> recirculationFlows) implements ProcessResult {

    @Override
    public String toConsoleOutput() {
        return ConsoleOutputFormatters.mixingConsoleOutput(this);
    }

    public static class AirMixingResultBuilder {
        private static final ProcessType processType = ProcessType.MIXING;
        private ProcessMode processMode;
        private FlowOfHumidAir inletAirFlow;
        private FlowOfHumidAir outletAirFlow;
        private Power heatOfProcess = Power.ofWatts(0);
        private Collection<FlowOfHumidAir> recirculationFlows;

        public AirMixingResultBuilder() {
            recirculationFlows = new ArrayList<>();
        }

        public AirMixingResultBuilder processMode(ProcessMode processMode) {
            this.processMode = processMode;
            return this;
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

        public AirMixingResultBuilder recirculationFlows(Collection<FlowOfHumidAir> recirculationFlows) {
            this.recirculationFlows = recirculationFlows;
            return this;
        }

        public MixingResult build() {
            return new MixingResult(processType, processMode, inletAirFlow, outletAirFlow, heatOfProcess, recirculationFlows);
        }
    }

    public static AirMixingResultBuilder builder() {
        return new AirMixingResultBuilder();
    }

}