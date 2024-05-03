package com.synerset.hvacengine.process.heating;

import com.synerset.hvacengine.common.validation.CommonValidators;
import com.synerset.hvacengine.process.ConsoleOutputFormatters;
import com.synerset.hvacengine.process.InputConnector;
import com.synerset.hvacengine.process.OutputConnector;
import com.synerset.hvacengine.process.ProcessBlock;
import com.synerset.hvacengine.process.heating.dataobject.HeatingResult;
import com.synerset.hvacengine.property.fluids.humidair.FlowOfHumidAir;
import com.synerset.unitility.unitsystem.humidity.RelativeHumidity;
import com.synerset.unitility.unitsystem.thermodynamic.Power;

public class HeatingFromHumidityBlock implements ProcessBlock {

    private final InputConnector<FlowOfHumidAir> inputAirFlowConnector;
    private InputConnector<RelativeHumidity> targetRelativeHumidityConnector;
    private final OutputConnector<FlowOfHumidAir> outputAirFlowConnector;
    private final OutputConnector<Power> outputHeatConnector;
    private HeatingResult heatingResult;

    public HeatingFromHumidityBlock(InputConnector<FlowOfHumidAir> inputAirFlowConnector,
                                    InputConnector<RelativeHumidity> targetRelativeHumidityConnector) {

        CommonValidators.requireNotNull(inputAirFlowConnector);
        CommonValidators.requireNotNull(targetRelativeHumidityConnector);
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
        CommonValidators.requireNotNull(targetRelativeHumidity);
        this.getTargetRelativeHumidityConnector().setConnectorData(targetRelativeHumidity);
    }

    public InputConnector<RelativeHumidity> getTargetRelativeHumidityConnector() {
        return targetRelativeHumidityConnector;
    }

    public void setTargetRelativeHumidityConnector(InputConnector<RelativeHumidity> targetRelativeHumidityConnector) {
        CommonValidators.requireNotNull(targetRelativeHumidityConnector);
        this.targetRelativeHumidityConnector = targetRelativeHumidityConnector;
    }

    public String toConsoleOutput() {
        if (inputAirFlowConnector.getConnectorData() == null || heatingResult == null) {
            return "Results not available. Run process first.";
        }
        return ConsoleOutputFormatters.heatingConsoleOutput(heatingResult);
    }

    public static HeatingFromHumidityBlock of(FlowOfHumidAir inputAirFlow, RelativeHumidity targetRelativeHumidity) {
        CommonValidators.requireNotNull(inputAirFlow);
        CommonValidators.requireNotNull(targetRelativeHumidity);
        return new HeatingFromHumidityBlock(
                InputConnector.of(inputAirFlow),
                InputConnector.of(targetRelativeHumidity)
        );
    }

    public static HeatingFromHumidityBlock of(InputConnector<FlowOfHumidAir> inputAirFlowConnector,
                                              InputConnector<RelativeHumidity> targetRelativeHumidityConnector) {

        CommonValidators.requireNotNull(inputAirFlowConnector);
        CommonValidators.requireNotNull(targetRelativeHumidityConnector);
        return new HeatingFromHumidityBlock(inputAirFlowConnector, targetRelativeHumidityConnector);
    }

    public static HeatingFromHumidityBlock of(InputConnector<FlowOfHumidAir> inputAirFlowConnector, RelativeHumidity targetRelativeHumidity) {
        CommonValidators.requireNotNull(inputAirFlowConnector);
        CommonValidators.requireNotNull(targetRelativeHumidity);
        return new HeatingFromHumidityBlock(inputAirFlowConnector, InputConnector.of(targetRelativeHumidity));
    }

    public static HeatingFromHumidityBlock of(RelativeHumidity targetRelativeHumidity) {
        return new HeatingFromHumidityBlock(
                InputConnector.createEmpty(FlowOfHumidAir.class),
                InputConnector.of(targetRelativeHumidity)
        );
    }

}