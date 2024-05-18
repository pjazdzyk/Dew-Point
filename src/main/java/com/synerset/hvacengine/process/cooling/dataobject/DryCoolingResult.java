package com.synerset.hvacengine.process.cooling.dataobject;

import com.synerset.hvacengine.process.ConsoleOutputFormatters;
import com.synerset.hvacengine.process.ProcessResult;
import com.synerset.hvacengine.process.ProcessType;
import com.synerset.hvacengine.process.cooling.CoolingMode;
import com.synerset.hvacengine.property.fluids.humidair.FlowOfHumidAir;
import com.synerset.unitility.unitsystem.thermodynamic.Power;

public record DryCoolingResult(ProcessType processType,
                               CoolingMode processMode,
                               FlowOfHumidAir inletAirFlow,
                               FlowOfHumidAir outletAirFlow,
                               Power heatOfProcess) implements ProcessResult {
    @Override
    public String toConsoleOutput() {
        return ConsoleOutputFormatters.dryCoolingConsoleOutput(this);
    }

    public static class Builder {
        private FlowOfHumidAir inletAirFlow;
        private FlowOfHumidAir outletAirFlow;
        private static final ProcessType processType = ProcessType.DRY_COOLING;
        private CoolingMode processMode;
        private Power heatOfProcess;

        public Builder processMode(CoolingMode processMode) {
            this.processMode = processMode;
            return this;
        }

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

        public DryCoolingResult build() {
            return new DryCoolingResult(processType, processMode, inletAirFlow, outletAirFlow, heatOfProcess);
        }
    }

    public static Builder builder() {
        return new Builder();
    }

}