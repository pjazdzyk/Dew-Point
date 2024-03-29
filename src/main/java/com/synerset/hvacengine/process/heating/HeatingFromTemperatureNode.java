package com.synerset.hvacengine.process.heating;

import com.synerset.hvacengine.common.Validators;
import com.synerset.hvacengine.fluids.humidair.FlowOfHumidAir;
import com.synerset.hvacengine.process.common.ConsoleOutputFormatters;
import com.synerset.hvacengine.process.computation.InputConnector;
import com.synerset.hvacengine.process.computation.OutputConnector;
import com.synerset.hvacengine.process.computation.ProcessNode;
import com.synerset.hvacengine.process.heating.dataobject.AirHeatingResult;
import com.synerset.unitility.unitsystem.thermodynamic.Power;
import com.synerset.unitility.unitsystem.thermodynamic.Temperature;

public class HeatingFromTemperatureNode implements ProcessNode {

    private final InputConnector<FlowOfHumidAir> inputAirFlowConnector;
    private InputConnector<Temperature> targetTemperatureConnector;
    private final OutputConnector<FlowOfHumidAir> outputAirFlowConnector;
    private final OutputConnector<Power> outputHeatConnector;
    private AirHeatingResult heatingResult;

    public HeatingFromTemperatureNode(InputConnector<FlowOfHumidAir> inputAirFlowConnector,
                                      InputConnector<Temperature> targetTemperatureConnector) {

        Validators.requireNotNull(inputAirFlowConnector);
        Validators.requireNotNull(targetTemperatureConnector);
        this.inputAirFlowConnector = inputAirFlowConnector;
        this.outputAirFlowConnector = OutputConnector.of(inputAirFlowConnector.getConnectorData());
        this.outputHeatConnector = OutputConnector.createEmpty(Power.class);
        this.targetTemperatureConnector = targetTemperatureConnector;
    }

    @Override
    public AirHeatingResult runProcessCalculations() {
        inputAirFlowConnector.updateConnectorData();
        targetTemperatureConnector.updateConnectorData();
        Temperature targetTemperature = targetTemperatureConnector.getConnectorData();
        FlowOfHumidAir inletAirFlow = inputAirFlowConnector.getConnectorData();
        AirHeatingResult heatingProcessResults = HeatingEquations.heatingFromTargetTemperature(inletAirFlow, targetTemperature);
        outputAirFlowConnector.setConnectorData(heatingProcessResults.outletAirFlow());
        outputHeatConnector.setConnectorData(heatingProcessResults.heatOfProcess());
        this.heatingResult = heatingProcessResults;
        return heatingProcessResults;
    }

    @Override
    public AirHeatingResult getProcessResults() {
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
        Validators.requireNotNull(targetTemperature);
        this.targetTemperatureConnector.setConnectorData(targetTemperature);
    }

    public InputConnector<Temperature> getTargetTemperatureConnector() {
        return targetTemperatureConnector;
    }

    public void setTargetTemperatureConnector(InputConnector<Temperature> targetTemperatureConnector) {
        Validators.requireNotNull(targetTemperatureConnector);
        this.targetTemperatureConnector = targetTemperatureConnector;
    }

    public String toConsoleOutput() {
        if (inputAirFlowConnector.getConnectorData() == null || heatingResult == null) {
            return "Results not available. Run process first.";
        }
        return ConsoleOutputFormatters.heatingConsoleOutput(heatingResult);
    }

    public static HeatingFromTemperatureNode of(FlowOfHumidAir inputAirFlow, Temperature targetTemperature) {
        Validators.requireNotNull(inputAirFlow);
        Validators.requireNotNull(targetTemperature);
        return new HeatingFromTemperatureNode(
                InputConnector.of(inputAirFlow),
                InputConnector.of(targetTemperature)
        );
    }

    public static HeatingFromTemperatureNode of(InputConnector<FlowOfHumidAir> inputAirFlowConnector,
                                                InputConnector<Temperature> targetTemperatureConnector) {

        Validators.requireNotNull(inputAirFlowConnector);
        Validators.requireNotNull(targetTemperatureConnector);
        return new HeatingFromTemperatureNode(inputAirFlowConnector, targetTemperatureConnector);
    }

    public static HeatingFromTemperatureNode of(Temperature targetTemperature) {
        Validators.requireNotNull(targetTemperature);
        return new HeatingFromTemperatureNode(
                InputConnector.createEmpty(FlowOfHumidAir.class),
                InputConnector.of(targetTemperature)
        );
    }

}