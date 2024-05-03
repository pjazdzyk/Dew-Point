package com.synerset.hvacengine.process.heating;

import com.synerset.hvacengine.common.validation.CommonValidators;
import com.synerset.hvacengine.process.ConsoleOutputFormatters;
import com.synerset.hvacengine.process.InputConnector;
import com.synerset.hvacengine.process.OutputConnector;
import com.synerset.hvacengine.process.ProcessBlock;
import com.synerset.hvacengine.process.heating.dataobject.HeatingResult;
import com.synerset.hvacengine.property.fluids.humidair.FlowOfHumidAir;
import com.synerset.unitility.unitsystem.thermodynamic.Power;
import com.synerset.unitility.unitsystem.thermodynamic.Temperature;

public class HeatingFromTemperatureBlock implements ProcessBlock {

    private final InputConnector<FlowOfHumidAir> inputAirFlowConnector;
    private InputConnector<Temperature> targetTemperatureConnector;
    private final OutputConnector<FlowOfHumidAir> outputAirFlowConnector;
    private final OutputConnector<Power> outputHeatConnector;
    private HeatingResult heatingResult;

    public HeatingFromTemperatureBlock(InputConnector<FlowOfHumidAir> inputAirFlowConnector,
                                       InputConnector<Temperature> targetTemperatureConnector) {

        CommonValidators.requireNotNull(inputAirFlowConnector);
        CommonValidators.requireNotNull(targetTemperatureConnector);
        this.inputAirFlowConnector = inputAirFlowConnector;
        this.outputAirFlowConnector = OutputConnector.of(inputAirFlowConnector.getConnectorData());
        this.outputHeatConnector = OutputConnector.createEmpty(Power.class);
        this.targetTemperatureConnector = targetTemperatureConnector;
    }

    @Override
    public HeatingResult runProcessCalculations() {
        inputAirFlowConnector.updateConnectorData();
        targetTemperatureConnector.updateConnectorData();
        Temperature targetTemperature = targetTemperatureConnector.getConnectorData();
        FlowOfHumidAir inletAirFlow = inputAirFlowConnector.getConnectorData();
        HeatingResult heatingProcessResults = HeatingEquations.heatingFromTargetTemperature(inletAirFlow, targetTemperature);
        outputAirFlowConnector.setConnectorData(heatingProcessResults.outletAirFlow());
        outputHeatConnector.setConnectorData(heatingProcessResults.heatOfProcess());
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

    public Temperature getTargetTemperature() {
        return targetTemperatureConnector.getConnectorData();
    }

    public void setTargetTemperature(Temperature targetTemperature) {
        CommonValidators.requireNotNull(targetTemperature);
        this.targetTemperatureConnector.setConnectorData(targetTemperature);
    }

    public InputConnector<Temperature> getTargetTemperatureConnector() {
        return targetTemperatureConnector;
    }

    public void setTargetTemperatureConnector(InputConnector<Temperature> targetTemperatureConnector) {
        CommonValidators.requireNotNull(targetTemperatureConnector);
        this.targetTemperatureConnector = targetTemperatureConnector;
    }

    public String toConsoleOutput() {
        if (inputAirFlowConnector.getConnectorData() == null || heatingResult == null) {
            return "Results not available. Run process first.";
        }
        return ConsoleOutputFormatters.heatingConsoleOutput(heatingResult);
    }

    public static HeatingFromTemperatureBlock of(FlowOfHumidAir inputAirFlow, Temperature targetTemperature) {
        CommonValidators.requireNotNull(inputAirFlow);
        CommonValidators.requireNotNull(targetTemperature);
        return new HeatingFromTemperatureBlock(
                InputConnector.of(inputAirFlow),
                InputConnector.of(targetTemperature)
        );
    }

    public static HeatingFromTemperatureBlock of(InputConnector<FlowOfHumidAir> inputAirFlowConnector,
                                                 InputConnector<Temperature> targetTemperatureConnector) {

        CommonValidators.requireNotNull(inputAirFlowConnector);
        CommonValidators.requireNotNull(targetTemperatureConnector);
        return new HeatingFromTemperatureBlock(inputAirFlowConnector, targetTemperatureConnector);
    }

    public static HeatingFromTemperatureBlock of(Temperature targetTemperature) {
        CommonValidators.requireNotNull(targetTemperature);
        return new HeatingFromTemperatureBlock(
                InputConnector.createEmpty(FlowOfHumidAir.class),
                InputConnector.of(targetTemperature)
        );
    }

}