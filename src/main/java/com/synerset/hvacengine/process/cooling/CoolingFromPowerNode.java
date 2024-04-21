package com.synerset.hvacengine.process.cooling;

import com.synerset.hvacengine.common.CommonValidators;
import com.synerset.hvacengine.fluids.humidair.FlowOfHumidAir;
import com.synerset.hvacengine.fluids.liquidwater.FlowOfLiquidWater;
import com.synerset.hvacengine.fluids.liquidwater.LiquidWater;
import com.synerset.hvacengine.process.ConsoleOutputFormatters;
import com.synerset.hvacengine.process.computation.InputConnector;
import com.synerset.hvacengine.process.computation.OutputConnector;
import com.synerset.hvacengine.process.computation.ProcessNode;
import com.synerset.hvacengine.process.cooling.dataobject.CoolingNodeResult;
import com.synerset.hvacengine.process.cooling.dataobject.RealCoolingResult;
import com.synerset.unitility.unitsystem.flow.MassFlow;
import com.synerset.unitility.unitsystem.thermodynamic.Power;

public class CoolingFromPowerNode implements ProcessNode {

    private final InputConnector<FlowOfHumidAir> inputAirFlowConnector;
    private final OutputConnector<FlowOfHumidAir> outputAirFlowConnector;
    private final InputConnector<CoolantData> coolantDataInputConnector;
    private final OutputConnector<FlowOfLiquidWater> outputCondensateConnector;
    private final InputConnector<Power> heatConnector;
    private CoolingNodeResult coolingResult;

    public CoolingFromPowerNode(InputConnector<FlowOfHumidAir> inputAirFlowConnector,
                                InputConnector<CoolantData> coolantDataConnector,
                                InputConnector<Power> heatConnector) {

        CommonValidators.requireNotNull(inputAirFlowConnector);
        CommonValidators.requireNotNull(coolantDataConnector);
        CommonValidators.requireNotNull(heatConnector);
        this.inputAirFlowConnector = inputAirFlowConnector;
        this.coolantDataInputConnector = coolantDataConnector;
        this.heatConnector = heatConnector;
        this.outputCondensateConnector = OutputConnector.createEmpty(FlowOfLiquidWater.class);
        this.outputAirFlowConnector = OutputConnector.of(inputAirFlowConnector.getConnectorData());
    }

    @Override
    public CoolingNodeResult runProcessCalculations() {
        inputAirFlowConnector.updateConnectorData();
        coolantDataInputConnector.updateConnectorData();
        heatConnector.updateConnectorData();

        FlowOfHumidAir inletAirFlow = inputAirFlowConnector.getConnectorData();
        Power heatingPower = heatConnector.getConnectorData();
        CoolantData coolantData = coolantDataInputConnector.getConnectorData();
        RealCoolingResult results = CoolingEquations.coolingFromPower(inletAirFlow, coolantData, heatingPower);

        outputAirFlowConnector.setConnectorData(results.outletAirFlow());
        outputCondensateConnector.setConnectorData(results.condensateFlow());

        MassFlow coolantMassFlow = CoolingEquations.massFlowFromPower(
                LiquidWater.of(coolantData.getSupplyTemperature()),
                coolantData.getReturnTemperature(),
                heatingPower
        );

        CoolingNodeResult coolingProcessResult = CoolingNodeResult.builder()
                .processMode(results.processMode())
                .heatOfProcess(results.heatOfProcess())
                .inletAirFlow(inletAirFlow)
                .averageCoilWallTemperature(coolantData.getAverageTemperature())
                .bypassFactor(results.bypassFactor())
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

    public InputConnector<Power> getHeatConnector() {
        return heatConnector;
    }

    public Power getCoolingPower() {
        return heatConnector.getConnectorData();
    }

    public void setCoolingPower(Power heatingPower) {
        if (heatingPower == null) {
            heatingPower = Power.ofWatts(0);
        }
        heatConnector.setConnectorData(heatingPower);
    }

    public static CoolingFromPowerNode of(FlowOfHumidAir inletAirFlow,
                                          CoolantData coolantData,
                                          Power coolingPower) {

        CommonValidators.requireNotNull(inletAirFlow);
        CommonValidators.requireNotNull(coolantData);

        if (coolingPower == null) {
            coolingPower = Power.ofWatts(0);
        }

        return new CoolingFromPowerNode(
                InputConnector.of(inletAirFlow),
                InputConnector.of(coolantData),
                InputConnector.of(coolingPower)
        );
    }


    public static CoolingFromPowerNode of(InputConnector<FlowOfHumidAir> inletAirFlow,
                                          InputConnector<CoolantData> coolantData,
                                          InputConnector<Power> coolingPower) {

        CommonValidators.requireNotNull(inletAirFlow);
        CommonValidators.requireNotNull(coolantData);
        CommonValidators.requireNotNull(coolingPower);

        return new CoolingFromPowerNode(inletAirFlow, coolantData, coolingPower);
    }

    public static CoolingFromPowerNode of(CoolantData coolantData,
                                          Power coolingPower) {

        CommonValidators.requireNotNull(coolantData);
        CommonValidators.requireNotNull(coolingPower);

        return new CoolingFromPowerNode(
                InputConnector.createEmpty(FlowOfHumidAir.class),
                InputConnector.of(coolantData),
                InputConnector.of(coolingPower)
        );
    }

}