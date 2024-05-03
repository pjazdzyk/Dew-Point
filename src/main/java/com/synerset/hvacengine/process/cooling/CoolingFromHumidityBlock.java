package com.synerset.hvacengine.process.cooling;

import com.synerset.hvacengine.common.validation.CommonValidators;
import com.synerset.hvacengine.process.ConsoleOutputFormatters;
import com.synerset.hvacengine.process.InputConnector;
import com.synerset.hvacengine.process.OutputConnector;
import com.synerset.hvacengine.process.ProcessBlock;
import com.synerset.hvacengine.process.cooling.dataobject.CoolingNodeResult;
import com.synerset.hvacengine.process.cooling.dataobject.RealCoolingResult;
import com.synerset.hvacengine.property.fluids.humidair.FlowOfHumidAir;
import com.synerset.hvacengine.property.fluids.liquidwater.FlowOfLiquidWater;
import com.synerset.hvacengine.property.fluids.liquidwater.LiquidWater;
import com.synerset.unitility.unitsystem.flow.MassFlow;
import com.synerset.unitility.unitsystem.humidity.RelativeHumidity;
import com.synerset.unitility.unitsystem.thermodynamic.Power;

public class CoolingFromHumidityBlock implements ProcessBlock {

    private final InputConnector<FlowOfHumidAir> inputAirFlowConnector;
    private final OutputConnector<FlowOfHumidAir> outputAirFlowConnector;
    private final InputConnector<CoolantData> coolantDataInputConnector;
    private final OutputConnector<FlowOfLiquidWater> outputCondensateConnector;
    private final OutputConnector<Power> heatConnector;
    private final InputConnector<RelativeHumidity> targetRelativeHumidity;
    private CoolingNodeResult coolingResult;

    public CoolingFromHumidityBlock(InputConnector<FlowOfHumidAir> inputAirFlowConnector,
                                    InputConnector<CoolantData> coolantDataConnector,
                                    InputConnector<RelativeHumidity> targetRelativeHumidity) {

        CommonValidators.requireNotNull(inputAirFlowConnector);
        CommonValidators.requireNotNull(coolantDataConnector);
        CommonValidators.requireNotNull(targetRelativeHumidity);
        this.targetRelativeHumidity = targetRelativeHumidity;
        this.inputAirFlowConnector = inputAirFlowConnector;
        this.coolantDataInputConnector = coolantDataConnector;
        this.heatConnector = OutputConnector.createEmpty(Power.class);
        this.outputCondensateConnector = OutputConnector.createEmpty(FlowOfLiquidWater.class);
        this.outputAirFlowConnector = OutputConnector.of(inputAirFlowConnector.getConnectorData());
    }

    @Override
    public CoolingNodeResult runProcessCalculations() {
        inputAirFlowConnector.updateConnectorData();
        coolantDataInputConnector.updateConnectorData();
        targetRelativeHumidity.updateConnectorData();

        FlowOfHumidAir inletAirFlow = inputAirFlowConnector.getConnectorData();
        RelativeHumidity targetRelativeHum = this.targetRelativeHumidity.getConnectorData();
        CoolantData coolantData = coolantDataInputConnector.getConnectorData();
        RealCoolingResult results = CoolingEquations.coolingFromTargetRelativeHumidity(inletAirFlow, coolantData, targetRelativeHum);

        outputAirFlowConnector.setConnectorData(results.outletAirFlow());
        heatConnector.setConnectorData(results.heatOfProcess());
        outputCondensateConnector.setConnectorData(results.condensateFlow());

        MassFlow coolantMassFlow = CoolingEquations.massFlowFromPower(
                LiquidWater.of(coolantData.getSupplyTemperature()),
                coolantData.getReturnTemperature(),
                heatConnector.getConnectorData()
        );

        CoolingNodeResult coolingProcessResult = CoolingNodeResult.builder()
                .processMode(results.processMode())
                .inletAirFlow(inletAirFlow)
                .heatOfProcess(results.heatOfProcess())
                .bypassFactor(results.bypassFactor())
                .averageCoilWallTemperature(coolantData.getAverageTemperature())
                .condensateFlow(results.condensateFlow())
                .outletAirFlow(results.outletAirFlow())
                .coolantSupplyFlow(FlowOfLiquidWater.of(LiquidWater.of(coolantData.getSupplyTemperature()), coolantMassFlow))
                .coolantReturnFlow(FlowOfLiquidWater.of(LiquidWater.of(coolantData.getReturnTemperature()), coolantMassFlow))
                .build();

        this.coolingResult = coolingProcessResult;
        return coolingProcessResult;
    }

    @Override
    public CoolingNodeResult getProcessResults() {
        return coolingResult;
    }

    @Override
    public InputConnector<FlowOfHumidAir> getAirFlowInputConnector() {
        return inputAirFlowConnector;
    }

    @Override
    public OutputConnector<FlowOfHumidAir> getAirFlowOutputConnector() {
        return outputAirFlowConnector;
    }

    @Override
    public String toConsoleOutput() {
        if (inputAirFlowConnector.getConnectorData() == null || coolingResult == null) {
            return "Results not available. Run process first.";
        }
        return ConsoleOutputFormatters.coolingNodeConsoleOutput(coolingResult);
    }

    public InputConnector<RelativeHumidity> getTargetRelativeHumidity() {
        return targetRelativeHumidity;
    }

    public RelativeHumidity getTargetTemperature() {
        return targetRelativeHumidity.getConnectorData();
    }

    public void setTargetTemperature(RelativeHumidity targetRelativeHumidity) {
        CommonValidators.requireNotNull(targetRelativeHumidity);
        this.targetRelativeHumidity.setConnectorData(targetRelativeHumidity);
    }

    public static CoolingFromHumidityBlock of(FlowOfHumidAir inletAirFlow,
                                              CoolantData coolantData,
                                              RelativeHumidity targetRelativeHumidity) {

        CommonValidators.requireNotNull(inletAirFlow);
        CommonValidators.requireNotNull(coolantData);
        CommonValidators.requireNotNull(targetRelativeHumidity);


        return new CoolingFromHumidityBlock(
                InputConnector.of(inletAirFlow),
                InputConnector.of(coolantData),
                InputConnector.of(targetRelativeHumidity)
        );
    }


    public static CoolingFromHumidityBlock of(InputConnector<FlowOfHumidAir> inletAirFlow,
                                              InputConnector<CoolantData> coolantData,
                                              InputConnector<RelativeHumidity> targetRelativeHumidity) {

        CommonValidators.requireNotNull(inletAirFlow);
        CommonValidators.requireNotNull(coolantData);
        CommonValidators.requireNotNull(targetRelativeHumidity);

        return new CoolingFromHumidityBlock(inletAirFlow, coolantData, targetRelativeHumidity);
    }

    public static CoolingFromHumidityBlock of(CoolantData coolantData,
                                              RelativeHumidity targetRelativeHumidity) {

        CommonValidators.requireNotNull(coolantData);
        CommonValidators.requireNotNull(targetRelativeHumidity);

        return new CoolingFromHumidityBlock(
                InputConnector.createEmpty(FlowOfHumidAir.class),
                InputConnector.of(coolantData),
                InputConnector.of(targetRelativeHumidity)
        );
    }

}