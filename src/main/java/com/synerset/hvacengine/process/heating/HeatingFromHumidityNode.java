package com.synerset.hvacengine.process.heating;

import com.synerset.hvacengine.common.Validators;
import com.synerset.hvacengine.fluids.humidair.FlowOfHumidAir;
import com.synerset.hvacengine.process.ConsoleOutputFormatters;
import com.synerset.hvacengine.process.computation.InputConnector;
import com.synerset.hvacengine.process.computation.OutputConnector;
import com.synerset.hvacengine.process.computation.ProcessNode;
import com.synerset.hvacengine.process.heating.dataobject.HeatingResult;
import com.synerset.unitility.unitsystem.humidity.RelativeHumidity;
import com.synerset.unitility.unitsystem.thermodynamic.Power;

public class HeatingFromHumidityNode implements ProcessNode {

    private final InputConnector<FlowOfHumidAir> inputAirFlowConnector;
    private InputConnector<RelativeHumidity> targetRelativeHumidityConnector;
    private final OutputConnector<FlowOfHumidAir> outputAirFlowConnector;
    private final OutputConnector<Power> outputHeatConnector;
    private HeatingResult heatingResult;

    public HeatingFromHumidityNode(InputConnector<FlowOfHumidAir> inputAirFlowConnector,
                                   InputConnector<RelativeHumidity> targetRelativeHumidityConnector) {

        Validators.requireNotNull(inputAirFlowConnector);
        Validators.requireNotNull(targetRelativeHumidityConnector);
        this.inputAirFlowConnector = inputAirFlowConnector;
        this.outputHeatConnector = OutputConnector.createEmpty(Power.class);
        this.outputAirFlowConnector = OutputConnector.of(inputAirFlowConnector.getConnectorData());
        this.targetRelativeHumidityConnector = targetRelativeHumidityConnector;
    }

    @Override
    public HeatingResult runProcessCalculations() {
        inputAirFlowConnector.updateConnectorData();
        targetRelativeHumidityConnector.updateConnectorData();
        RelativeHumidity targetRelativeHumidity = targetRelativeHumidityConnector.getConnectorData();
        FlowOfHumidAir inletAirFlow = inputAirFlowConnector.getConnectorData();
        HeatingResult heatingProcessResults = HeatingEquations.heatingFromRelativeHumidity(inletAirFlow, targetRelativeHumidity);
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

    public RelativeHumidity getTargetRelativeHumidity() {
        return targetRelativeHumidityConnector.getConnectorData();
    }

    public void setTargetRelativeHumidity(RelativeHumidity targetRelativeHumidity) {
        Validators.requireNotNull(targetRelativeHumidity);
        this.getTargetRelativeHumidityConnector().setConnectorData(targetRelativeHumidity);
    }

    public InputConnector<RelativeHumidity> getTargetRelativeHumidityConnector() {
        return targetRelativeHumidityConnector;
    }

    public void setTargetRelativeHumidityConnector(InputConnector<RelativeHumidity> targetRelativeHumidityConnector) {
        Validators.requireNotNull(targetRelativeHumidityConnector);
        this.targetRelativeHumidityConnector = targetRelativeHumidityConnector;
    }

    public String toConsoleOutput() {
        if (inputAirFlowConnector.getConnectorData() == null || heatingResult == null) {
            return "Results not available. Run process first.";
        }
        return ConsoleOutputFormatters.heatingConsoleOutput(heatingResult);
    }

    public static HeatingFromHumidityNode of(FlowOfHumidAir inputAirFlow, RelativeHumidity targetRelativeHumidity) {
        Validators.requireNotNull(inputAirFlow);
        Validators.requireNotNull(targetRelativeHumidity);
        return new HeatingFromHumidityNode(
                InputConnector.of(inputAirFlow),
                InputConnector.of(targetRelativeHumidity)
        );
    }

    public static HeatingFromHumidityNode of(InputConnector<FlowOfHumidAir> inputAirFlowConnector,
                                             InputConnector<RelativeHumidity> targetRelativeHumidityConnector) {

        Validators.requireNotNull(inputAirFlowConnector);
        Validators.requireNotNull(targetRelativeHumidityConnector);
        return new HeatingFromHumidityNode(inputAirFlowConnector, targetRelativeHumidityConnector);
    }

    public static HeatingFromHumidityNode of(InputConnector<FlowOfHumidAir> inputAirFlowConnector, RelativeHumidity targetRelativeHumidity) {
        Validators.requireNotNull(inputAirFlowConnector);
        Validators.requireNotNull(targetRelativeHumidity);
        return new HeatingFromHumidityNode(inputAirFlowConnector, InputConnector.of(targetRelativeHumidity));
    }

    public static HeatingFromHumidityNode of(RelativeHumidity targetRelativeHumidity) {
        return new HeatingFromHumidityNode(
                InputConnector.createEmpty(FlowOfHumidAir.class),
                InputConnector.of(targetRelativeHumidity)
        );
    }

}