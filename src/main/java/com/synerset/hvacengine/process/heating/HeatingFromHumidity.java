package com.synerset.hvacengine.process.heating;

import com.synerset.hvacengine.common.validation.CommonValidators;
import com.synerset.hvacengine.process.ConsoleOutputFormatters;
import com.synerset.hvacengine.process.HvacProcessBlock;
import com.synerset.hvacengine.process.ProcessType;
import com.synerset.hvacengine.process.blockmodel.ConnectorInput;
import com.synerset.hvacengine.process.blockmodel.ConnectorOutput;
import com.synerset.hvacengine.process.blockmodel.OutputConnection;
import com.synerset.hvacengine.process.heating.dataobject.HeatingResult;
import com.synerset.hvacengine.property.fluids.humidair.FlowOfHumidAir;
import com.synerset.unitility.unitsystem.humidity.RelativeHumidity;
import com.synerset.unitility.unitsystem.thermodynamic.Power;

public class HeatingFromHumidity implements HvacProcessBlock {

    private static final ProcessType PROCESS_TYPE = ProcessType.HEATING;
    private static final HeatingMode HEATING_MODE = HeatingMode.FROM_HUMIDITY;
    private final ConnectorInput<FlowOfHumidAir> inputAirFlowConnector;
    private final ConnectorInput<RelativeHumidity> targetRelativeHumidityConnector;
    private final ConnectorOutput<FlowOfHumidAir> outputAirFlowConnector;
    private final ConnectorOutput<Power> outputHeatConnector;
    private HeatingResult processResult;

    public HeatingFromHumidity() {
        this.inputAirFlowConnector = ConnectorInput.of(FlowOfHumidAir.class);
        this.targetRelativeHumidityConnector = ConnectorInput.of(RelativeHumidity.class);
        this.outputHeatConnector = ConnectorOutput.of(Power.ofWatts(0));
        this.outputAirFlowConnector = ConnectorOutput.of(FlowOfHumidAir.class);
    }

    public HeatingFromHumidity(OutputConnection<FlowOfHumidAir> blockWithAirFlowOutput,
                               OutputConnection<RelativeHumidity> blockWithHumidityOutput) {

        this();
        CommonValidators.requireNotNull(blockWithAirFlowOutput);
        CommonValidators.requireNotNull(blockWithHumidityOutput);
        this.inputAirFlowConnector.connectAndConsumeDataFrom(blockWithAirFlowOutput.getOutputConnector());
        this.targetRelativeHumidityConnector.connectAndConsumeDataFrom(blockWithHumidityOutput.getOutputConnector());
        this.outputAirFlowConnector.setConnectorData(inputAirFlowConnector.getConnectorData());
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
        this.processResult = heatingProcessResults;
        return heatingProcessResults;
    }

    @Override
    public HeatingResult getProcessResult() {
        return processResult;
    }

    @Override
    public ProcessType getProcessType() {
        return PROCESS_TYPE;
    }

    public HeatingMode getProcessMode() {
        return HEATING_MODE;
    }

    @Override
    public ConnectorInput<FlowOfHumidAir> getInputConnector() {
        return inputAirFlowConnector;
    }

    @Override
    public ConnectorOutput<FlowOfHumidAir> getOutputConnector() {
        return outputAirFlowConnector;
    }

    @Override
    public String toConsoleOutput() {
        if (inputAirFlowConnector.getConnectorData() == null || processResult == null) {
            return "Results not available. Run process first.";
        }
        return ConsoleOutputFormatters.heatingConsoleOutput(processResult);
    }

    // Methods specific for this process
    public RelativeHumidity getUnwrappedTargetRelativeHumidity() {
        return targetRelativeHumidityConnector.getConnectorData();
    }

    public ConnectorInput<RelativeHumidity> getTargetRelativeHumidityConnector() {
        return targetRelativeHumidityConnector;
    }

    public void connectRelativeHumiditySource(OutputConnection<RelativeHumidity> relativeHumiditySourceBlock){
        CommonValidators.requireNotNull(relativeHumiditySourceBlock);
        this.targetRelativeHumidityConnector.connectAndConsumeDataFrom(relativeHumiditySourceBlock.getOutputConnector());
    }

    // Static factory methods
    public static HeatingFromHumidity of(){
        return new HeatingFromHumidity();
    }

    public static HeatingFromHumidity of(OutputConnection<RelativeHumidity> blockWithHumidityOutput){
        CommonValidators.requireNotNull(blockWithHumidityOutput);
        HeatingFromHumidity heatingFromHumidity = new HeatingFromHumidity();
        heatingFromHumidity.connectRelativeHumiditySource(blockWithHumidityOutput);
        return heatingFromHumidity;
    }

    public static HeatingFromHumidity of(OutputConnection<FlowOfHumidAir> blockWithAirFlowOutput,
                                         OutputConnection<RelativeHumidity> blockWithHumidityOutput){

        return new HeatingFromHumidity(blockWithAirFlowOutput, blockWithHumidityOutput);
    }

}