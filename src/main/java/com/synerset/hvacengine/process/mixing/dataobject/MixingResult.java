package com.synerset.hvacengine.process.mixing.dataobject;

import com.synerset.hvacengine.process.ConsoleOutputFormatters;
import com.synerset.hvacengine.process.ProcessResult;
import com.synerset.hvacengine.process.ProcessType;
import com.synerset.hvacengine.process.mixing.MixingMode;
import com.synerset.hvacengine.property.fluids.humidair.FlowOfHumidAir;
import com.synerset.unitility.unitsystem.common.Ratio;
import com.synerset.unitility.unitsystem.thermodynamic.Power;

import java.util.ArrayList;
import java.util.Collection;

/**
 * The AirMixingResult record represents the result of a mixing process.
 */
public record MixingResult(ProcessType processType,
                           MixingMode processMode,
                           FlowOfHumidAir inletAirFlow,
                           FlowOfHumidAir outletAirFlow,
                           Power heatOfProcess,
                           Ratio freshAirRatio,
                           Collection<FlowOfHumidAir> recirculationFlows) implements ProcessResult {

    @Override
    public String toConsoleOutput() {
        return ConsoleOutputFormatters.mixingConsoleOutput(this);
    }

    public static class AirMixingResultBuilder {
        private static final ProcessType processType = ProcessType.MIXING;
        private MixingMode processMode;
        private FlowOfHumidAir inletAirFlow;
        private FlowOfHumidAir outletAirFlow;
        private Power heatOfProcess = Power.ofWatts(0);
        private Ratio freshAirRatio = Ratio.ofPercentage(0);
        private Collection<FlowOfHumidAir> recirculationFlows;

        public AirMixingResultBuilder() {
            recirculationFlows = new ArrayList<>();
        }

        public AirMixingResultBuilder processMode(MixingMode processMode) {
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

        public AirMixingResultBuilder freshAirRatio(Ratio freshAirRatio) {
            this.freshAirRatio = freshAirRatio;
            return this;
        }

        public MixingResult build() {
            return new MixingResult(processType, processMode, inletAirFlow, outletAirFlow, heatOfProcess, freshAirRatio, recirculationFlows);
        }
    }

    public static AirMixingResultBuilder builder() {
        return new AirMixingResultBuilder();
    }

}