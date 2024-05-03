package com.synerset.hvacengine.process.heating;

import com.synerset.hvacengine.common.validation.CommonValidators;
import com.synerset.hvacengine.process.ConsoleOutputFormatters;
import com.synerset.hvacengine.process.InputConnector;
import com.synerset.hvacengine.process.OutputConnector;
import com.synerset.hvacengine.process.ProcessBlock;
import com.synerset.hvacengine.process.heating.dataobject.HeatingResult;
import com.synerset.hvacengine.property.fluids.humidair.FlowOfHumidAir;
import com.synerset.unitility.unitsystem.thermodynamic.Power;

public class HeatingFromPowerBlock implements ProcessBlock {

    private final InputConnector<FlowOfHumidAir> inputAirFlowConnector;
    private final InputConnector<Power> inputHeatConnector;
    private final OutputConnector<FlowOfHumidAir> outputAirFlowConnector;
    private HeatingResult heatingResult;

    public HeatingFromPowerBlock(InputConnector<FlowOfHumidAir> inputAirFlowConnector, InputConnector<Power> inputHeatConnector) {
        CommonValidators.requireNotNull(inputAirFlowConnector);
        CommonValidators.requireNotNull(inputHeatConnector);
        this.inputAirFlowConnector = inputAirFlowConnector;
        this.inputHeatConnector = inputHeatConnector;
        this.outputAirFlowConnector = OutputConnector.of(inputAirFlowConnector.getConnectorData());
    }

    @Override
    public HeatingResult runProcessCalculations() {
        inputAirFlowConnector.updateConnectorData();
        inputHeatConnector.updateConnectorData();
        FlowOfHumidAir inletAirFlow = inputAirFlowConnector.getConnectorData();
        Power heatingPower = inputHeatConnector.getConnectorData();
        HeatingResult heatingProcessResults = HeatingEquations.heatingFromPower(inletAirFlow, heatingPower);
        outputAirFlowConnector.setConnectorData(heatingProcessResults.outletAirFlow());
        this.heatingResult = heatingProcessResults;
        return heatingProcessResults;
    }

    @Override
    public HeatingResult getProcessResults() {
        return heatingResult;
    }

    @Override
    public InputConnector<FlowOfHumidAir> getAirFlowInputConnector() {
        return inputAirFlowConnector;
    }

    @Override
    public OutputConnector<FlowOfHumidAir> getAirFlowOutputConnector() {
        return outputAirFlowConnector;
    }

    public Power getHeatingPower() {
        return inputHeatConnector.getConnectorData();
    }

    public void setHeatingPower(Power heatingPower) {
        if (heatingPower == null) {
            heatingPower = Power.ofWatts(0);
        }
        inputHeatConnector.setConnectorData(heatingPower);
    }

    public String toConsoleOutput() {
        if (inputAirFlowConnector.getConnectorData() == null || heatingResult == null) {
            return "Results not available. Run process first.";
        }
        return ConsoleOutputFormatters.heatingConsoleOutput(heatingResult);
    }

    public static HeatingFromPowerBlock of(FlowOfHumidAir inputAirFlow, Power inputPower) {
        CommonValidators.requireNotNull(inputAirFlow);
        if (inputPower == null) {
            inputPower = Power.ofWatts(0);
        }
        return new HeatingFromPowerBlock(InputConnector.of(inputAirFlow), InputConnector.of(inputPower));
    }

    public static HeatingFromPowerBlock of(InputConnector<FlowOfHumidAir> inputAirFlowConnector, InputConnector<Power> heatConnector) {
        CommonValidators.requireNotNull(inputAirFlowConnector);
        CommonValidators.requireNotNull(heatConnector);
        return new HeatingFromPowerBlock(inputAirFlowConnector, heatConnector);
    }

    public static HeatingFromPowerBlock of(InputConnector<Power> heatConnector) {
        CommonValidators.requireNotNull(heatConnector);
        return new HeatingFromPowerBlock(InputConnector.createEmpty(FlowOfHumidAir.class), heatConnector);
    }

    public static HeatingFromPowerBlock of(Power heatingPower) {
        if (heatingPower == null) {
            heatingPower = Power.ofWatts(0);
        }
        return new HeatingFromPowerBlock(InputConnector.createEmpty(FlowOfHumidAir.class), InputConnector.of(heatingPower));
    }

}