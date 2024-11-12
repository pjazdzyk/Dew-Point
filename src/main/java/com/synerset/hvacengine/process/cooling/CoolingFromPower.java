package com.synerset.hvacengine.process.cooling;

import com.synerset.hvacengine.common.validation.CommonValidators;
import com.synerset.hvacengine.process.ConsoleOutputFormatters;
import com.synerset.hvacengine.process.HvacProcessBlock;
import com.synerset.hvacengine.process.ProcessType;
import com.synerset.hvacengine.process.blockmodel.ConnectorInput;
import com.synerset.hvacengine.process.blockmodel.ConnectorOutput;
import com.synerset.hvacengine.process.blockmodel.OutputConnection;
import com.synerset.hvacengine.process.cooling.dataobject.CoolingResult;
import com.synerset.hvacengine.property.fluids.humidair.FlowOfHumidAir;
import com.synerset.hvacengine.property.fluids.liquidwater.FlowOfLiquidWater;
import com.synerset.unitility.unitsystem.thermodynamic.Power;

public class CoolingFromPower implements HvacProcessBlock {

    private static final ProcessType PROCESS_TYPE = ProcessType.COOLING;
    private static final CoolingMode COOLING_MODE = CoolingMode.FROM_POWER;
    private final ConnectorInput<FlowOfHumidAir> inputAirFlowConnector;
    private final ConnectorOutput<FlowOfHumidAir> outputAirFlowConnector;
    private final ConnectorInput<CoolantData> coolantDataInputConnector;
    private final ConnectorOutput<FlowOfLiquidWater> outputCondensateConnector;
    private final ConnectorInput<Power> heatConnector;
    private CoolingResult processResult;

    public CoolingFromPower() {
        this.inputAirFlowConnector = ConnectorInput.of(FlowOfHumidAir.class);
        this.outputAirFlowConnector = ConnectorOutput.of(FlowOfHumidAir.class);
        this.coolantDataInputConnector = ConnectorInput.of(CoolantData.class);
        this.outputCondensateConnector = ConnectorOutput.of(FlowOfLiquidWater.class);
        this.heatConnector = ConnectorInput.of(Power.class);
    }

    public CoolingFromPower(OutputConnection<FlowOfHumidAir> blockWithAirFlowOutput,
                            OutputConnection<CoolantData> blockWithCoolantDataOutput,
                            OutputConnection<Power> blockWithPowerOutput) {

        this();
        CommonValidators.requireNotNull(blockWithAirFlowOutput);
        CommonValidators.requireNotNull(blockWithCoolantDataOutput);
        CommonValidators.requireNotNull(blockWithPowerOutput);
        this.inputAirFlowConnector.connectAndConsumeDataFrom(blockWithAirFlowOutput.getOutputConnector());
        this.coolantDataInputConnector.connectAndConsumeDataFrom(blockWithCoolantDataOutput.getOutputConnector());
        this.heatConnector.connectAndConsumeDataFrom(blockWithPowerOutput.getOutputConnector());
        this.outputAirFlowConnector.setConnectorData(inputAirFlowConnector.getConnectorData());
        this.outputCondensateConnector.setConnectorData(FlowOfLiquidWater.zeroFlow(inputAirFlowConnector.getConnectorData().getTemperature()));
    }

    @Override
    public CoolingResult runProcessCalculations() {
        inputAirFlowConnector.updateConnectorData();
        coolantDataInputConnector.updateConnectorData();
        heatConnector.updateConnectorData();

        FlowOfHumidAir inletAirFlow = inputAirFlowConnector.getConnectorData();
        Power coolingPower = heatConnector.getConnectorData();
        CoolantData coolantData = coolantDataInputConnector.getConnectorData();

        CoolingResult results = CoolingEquations.coolingFromPower(inletAirFlow, coolantData, coolingPower);

        outputAirFlowConnector.setConnectorData(results.outletAirFlow());
        outputCondensateConnector.setConnectorData(results.condensateFlow());

        this.processResult = results;
        return results;
    }

    @Override
    public CoolingResult getProcessResult() {
        return processResult;
    }

    @Override
    public ProcessType getProcessType() {
        return PROCESS_TYPE;
    }

    public CoolingMode getProcessMode() {
        return COOLING_MODE;
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
        return ConsoleOutputFormatters.coolingNodeConsoleOutput(processResult);
    }

    // Methods specific for this class
    public ConnectorInput<Power> getHeatConnector() {
        return heatConnector;
    }

    public Power getUnwrappedInputCoolingPower() {
        return heatConnector.getConnectorData();
    }

    public void connectCoolantDataSource(OutputConnection<CoolantData> blockWithOutputCoolantData){
        CommonValidators.requireNotNull(blockWithOutputCoolantData);
        this.coolantDataInputConnector.connectAndConsumeDataFrom(blockWithOutputCoolantData.getOutputConnector());
    }

    public void connectPowerDataSource(OutputConnection<Power> blockWithPowerData){
        CommonValidators.requireNotNull(blockWithPowerData);
        this.heatConnector.connectAndConsumeDataFrom(blockWithPowerData.getOutputConnector());
    }

    // Static factory methods
    public static CoolingFromPower of(){
        return new CoolingFromPower();
    }

    public static CoolingFromPower of(OutputConnection<CoolantData> blockWithCoolantDataOutput,
                                      OutputConnection<Power> blockWithPowerOutput) {

        CommonValidators.requireNotNull(blockWithCoolantDataOutput);
        CommonValidators.requireNotNull(blockWithPowerOutput);
        CoolingFromPower coolingFromPower = new CoolingFromPower();
        coolingFromPower.connectCoolantDataSource(blockWithCoolantDataOutput);
        coolingFromPower.connectPowerDataSource(blockWithPowerOutput);
        return coolingFromPower;
    }

    public static CoolingFromPower of(OutputConnection<FlowOfHumidAir> blockWithAirFlowOutput,
                                      OutputConnection<CoolantData> blockWithCoolantDataOutput,
                                      OutputConnection<Power> blockWithPowerOutput) {

        return new CoolingFromPower(blockWithAirFlowOutput, blockWithCoolantDataOutput, blockWithPowerOutput);
    }

}