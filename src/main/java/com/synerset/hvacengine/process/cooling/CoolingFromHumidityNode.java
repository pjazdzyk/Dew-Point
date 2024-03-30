package com.synerset.hvacengine.process.cooling;

import com.synerset.hvacengine.common.Validators;
import com.synerset.hvacengine.fluids.humidair.FlowOfHumidAir;
import com.synerset.hvacengine.fluids.liquidwater.FlowOfLiquidWater;
import com.synerset.hvacengine.fluids.liquidwater.LiquidWater;
import com.synerset.hvacengine.process.common.ConsoleOutputFormatters;
import com.synerset.hvacengine.process.computation.InputConnector;
import com.synerset.hvacengine.process.computation.OutputConnector;
import com.synerset.hvacengine.process.computation.ProcessNode;
import com.synerset.hvacengine.process.cooling.dataobject.CoolingNodeResult;
import com.synerset.hvacengine.process.cooling.dataobject.RealCoolingResult;
import com.synerset.unitility.unitsystem.flow.MassFlow;
import com.synerset.unitility.unitsystem.humidity.RelativeHumidity;
import com.synerset.unitility.unitsystem.thermodynamic.Power;

public class CoolingFromHumidityNode implements ProcessNode {

    private final InputConnector<FlowOfHumidAir> inputAirFlowConnector;
    private final OutputConnector<FlowOfHumidAir> outputAirFlowConnector;
    private final InputConnector<CoolantData> coolantDataInputConnector;
    private final OutputConnector<FlowOfLiquidWater> outputCondensateConnector;
    private final OutputConnector<Power> heatConnector;
    private final InputConnector<RelativeHumidity> targetRelativeHumidity;
    private CoolingNodeResult coolingResult;

    public CoolingFromHumidityNode(InputConnector<FlowOfHumidAir> inputAirFlowConnector,
                                   InputConnector<CoolantData> coolantDataConnector,
                                   InputConnector<RelativeHumidity> targetRelativeHumidity) {

        Validators.requireNotNull(inputAirFlowConnector);
        Validators.requireNotNull(coolantDataConnector);
        Validators.requireNotNull(targetRelativeHumidity);
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
                .inletAirFlow(inletAirFlow)
                .heatOfProcess(results.heatOfProcess())
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

    public InputConnector<RelativeHumidity> getTargetRelativeHumidity() {
        return targetRelativeHumidity;
    }

    public RelativeHumidity getTargetTemperature() {
        return targetRelativeHumidity.getConnectorData();
    }

    public void setTargetTemperature(RelativeHumidity targetRelativeHumidity) {
        Validators.requireNotNull(targetRelativeHumidity);
        this.targetRelativeHumidity.setConnectorData(targetRelativeHumidity);
    }

    public static CoolingFromHumidityNode of(FlowOfHumidAir inletAirFlow,
                                             CoolantData coolantData,
                                             RelativeHumidity targetRelativeHumidity) {

        Validators.requireNotNull(inletAirFlow);
        Validators.requireNotNull(coolantData);
        Validators.requireNotNull(targetRelativeHumidity);


        return new CoolingFromHumidityNode(
                InputConnector.of(inletAirFlow),
                InputConnector.of(coolantData),
                InputConnector.of(targetRelativeHumidity)
        );
    }


    public static CoolingFromHumidityNode of(InputConnector<FlowOfHumidAir> inletAirFlow,
                                             InputConnector<CoolantData> coolantData,
                                             InputConnector<RelativeHumidity> targetRelativeHumidity) {

        Validators.requireNotNull(inletAirFlow);
        Validators.requireNotNull(coolantData);
        Validators.requireNotNull(targetRelativeHumidity);

        return new CoolingFromHumidityNode(inletAirFlow, coolantData, targetRelativeHumidity);
    }

    public static CoolingFromHumidityNode of(CoolantData coolantData,
                                             RelativeHumidity targetRelativeHumidity) {

        Validators.requireNotNull(coolantData);
        Validators.requireNotNull(targetRelativeHumidity);

        return new CoolingFromHumidityNode(
                InputConnector.createEmpty(FlowOfHumidAir.class),
                InputConnector.of(coolantData),
                InputConnector.of(targetRelativeHumidity)
        );
    }

}