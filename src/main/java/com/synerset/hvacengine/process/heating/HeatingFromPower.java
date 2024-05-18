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
import com.synerset.unitility.unitsystem.thermodynamic.Power;

public class HeatingFromPower implements HvacProcessBlock {

    private static final ProcessType PROCESS_TYPE = ProcessType.HEATING;
    private static final HeatingMode HEATING_MODE = HeatingMode.FROM_POWER;
    private final ConnectorInput<FlowOfHumidAir> inputAirFlowConnector;
    private final ConnectorInput<Power> inputHeatConnector;
    private final ConnectorOutput<FlowOfHumidAir> outputAirFlowConnector;
    private HeatingResult processResult;

    public HeatingFromPower() {
        this.inputAirFlowConnector = ConnectorInput.of(FlowOfHumidAir.class);
        this.inputHeatConnector = ConnectorInput.of(Power.class);
        this.outputAirFlowConnector = ConnectorOutput.of(FlowOfHumidAir.class);
    }

    public HeatingFromPower(OutputConnection<FlowOfHumidAir> blockWithAirFlowOutput,
                            OutputConnection<Power> blockWithPowerOutput) {

        this();
        CommonValidators.requireNotNull(blockWithAirFlowOutput);
        CommonValidators.requireNotNull(blockWithPowerOutput);
        this.inputAirFlowConnector.connectAndConsumeDataFrom(blockWithAirFlowOutput.getOutputConnector());
        this.inputHeatConnector.connectAndConsumeDataFrom(blockWithPowerOutput.getOutputConnector());
        this.outputAirFlowConnector.setConnectorData(inputAirFlowConnector.getConnectorData());
    }

    @Override
    public HeatingResult runProcessCalculations() {
        inputAirFlowConnector.updateConnectorData();
        inputHeatConnector.updateConnectorData();
        FlowOfHumidAir inletAirFlow = inputAirFlowConnector.getConnectorData();
        Power heatingPower = inputHeatConnector.getConnectorData();
        HeatingResult heatingProcessResults = HeatingEquations.heatingFromPower(inletAirFlow, heatingPower);
        outputAirFlowConnector.setConnectorData(heatingProcessResults.outletAirFlow());
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
    public Power getUnwrappedTargetInputPower() {
        return inputHeatConnector.getConnectorData();
    }

    public ConnectorInput<Power> getTargetInputPowerConnector() {
        return inputHeatConnector;
    }

    public void connectPowerDataSource(OutputConnection<Power> blockWithPowerOutput) {
        CommonValidators.requireNotNull(blockWithPowerOutput);
        this.inputHeatConnector.connectAndConsumeDataFrom(blockWithPowerOutput.getOutputConnector());
    }

    // Static factory methods
    public static HeatingFromPower of() {
        return new HeatingFromPower();
    }

    public static HeatingFromPower of(OutputConnection<Power> blockWithPowerOutput) {
        CommonValidators.requireNotNull(blockWithPowerOutput);
        HeatingFromPower heatingFromPower = new HeatingFromPower();
        heatingFromPower.connectPowerDataSource(blockWithPowerOutput);
        return heatingFromPower;
    }

    public static HeatingFromPower of(OutputConnection<FlowOfHumidAir> blockWithAirFlowOutput,
                                      OutputConnection<Power> blockWithPowerOutput) {

        return new HeatingFromPower(blockWithAirFlowOutput, blockWithPowerOutput);
    }

}