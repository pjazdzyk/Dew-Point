package com.synerset.hvacengine.process.heating;

import com.synerset.hvacengine.common.CommonValidators;
import com.synerset.hvacengine.fluids.humidair.FlowOfHumidAir;
import com.synerset.hvacengine.process.ConsoleOutputFormatters;
import com.synerset.hvacengine.process.computation.InputConnector;
import com.synerset.hvacengine.process.computation.OutputConnector;
import com.synerset.hvacengine.process.computation.ProcessNode;
import com.synerset.hvacengine.process.heating.dataobject.HeatingResult;
import com.synerset.unitility.unitsystem.thermodynamic.Power;

public class HeatingFromPowerNode implements ProcessNode {

    private final InputConnector<FlowOfHumidAir> inputAirFlowConnector;
    private final InputConnector<Power> inputHeatConnector;
    private final OutputConnector<FlowOfHumidAir> outputAirFlowConnector;
    private HeatingResult heatingResult;

    public HeatingFromPowerNode(InputConnector<FlowOfHumidAir> inputAirFlowConnector, InputConnector<Power> inputHeatConnector) {
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

    public static HeatingFromPowerNode of(FlowOfHumidAir inputAirFlow, Power inputPower) {
        CommonValidators.requireNotNull(inputAirFlow);
        if (inputPower == null) {
            inputPower = Power.ofWatts(0);
        }
        return new HeatingFromPowerNode(InputConnector.of(inputAirFlow), InputConnector.of(inputPower));
    }

    public static HeatingFromPowerNode of(InputConnector<FlowOfHumidAir> inputAirFlowConnector, InputConnector<Power> heatConnector) {
        CommonValidators.requireNotNull(inputAirFlowConnector);
        CommonValidators.requireNotNull(heatConnector);
        return new HeatingFromPowerNode(inputAirFlowConnector, heatConnector);
    }

    public static HeatingFromPowerNode of(InputConnector<Power> heatConnector) {
        CommonValidators.requireNotNull(heatConnector);
        return new HeatingFromPowerNode(InputConnector.createEmpty(FlowOfHumidAir.class), heatConnector);
    }

    public static HeatingFromPowerNode of(Power heatingPower) {
        if (heatingPower == null) {
            heatingPower = Power.ofWatts(0);
        }
        return new HeatingFromPowerNode(InputConnector.createEmpty(FlowOfHumidAir.class), InputConnector.of(heatingPower));
    }

}