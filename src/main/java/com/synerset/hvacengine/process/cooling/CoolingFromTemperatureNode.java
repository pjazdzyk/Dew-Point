package com.synerset.hvacengine.process.cooling;

import com.synerset.hvacengine.common.Validators;
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
import com.synerset.unitility.unitsystem.thermodynamic.Temperature;

public class CoolingFromTemperatureNode implements ProcessNode {

    private final InputConnector<FlowOfHumidAir> inputAirFlowConnector;
    private final OutputConnector<FlowOfHumidAir> outputAirFlowConnector;
    private final InputConnector<CoolantData> coolantDataInputConnector;
    private final OutputConnector<FlowOfLiquidWater> outputCondensateConnector;
    private final OutputConnector<Power> heatConnector;
    private final InputConnector<Temperature> targetTemperatureConnector;
    private CoolingNodeResult coolingResult;

    public CoolingFromTemperatureNode(InputConnector<FlowOfHumidAir> inputAirFlowConnector,
                                      InputConnector<CoolantData> coolantDataConnector,
                                      InputConnector<Temperature> targetTemperatureConnector) {

        Validators.requireNotNull(inputAirFlowConnector);
        Validators.requireNotNull(coolantDataConnector);
        Validators.requireNotNull(targetTemperatureConnector);
        this.targetTemperatureConnector = targetTemperatureConnector;
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
        targetTemperatureConnector.updateConnectorData();

        FlowOfHumidAir inletAirFlow = inputAirFlowConnector.getConnectorData();
        Temperature targetTemperature = targetTemperatureConnector.getConnectorData();
        CoolantData coolantData = coolantDataInputConnector.getConnectorData();
        RealCoolingResult results = CoolingEquations.coolingFromTargetTemperature(inletAirFlow, coolantData, targetTemperature);

        heatConnector.setConnectorData(results.heatOfProcess());
        outputAirFlowConnector.setConnectorData(results.outletAirFlow());
        outputCondensateConnector.setConnectorData(results.condensateFlow());

        MassFlow coolantMassFlow = CoolingEquations.massFlowFromPower(
                LiquidWater.of(coolantData.getSupplyTemperature()),
                coolantData.getReturnTemperature(),
                heatConnector.getConnectorData()
        );

        CoolingNodeResult coolingProcessResult = CoolingNodeResult.builder()
                .processMode(results.processMode())
                .inletAirFlow(inletAirFlow)
                .outletAirFlow(results.outletAirFlow())
                .heatOfProcess(results.heatOfProcess())
                .averageCoilWallTemperature(coolantData.getAverageTemperature())
                .bypassFactor(results.bypassFactor())
                .condensateFlow(results.condensateFlow())
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

    public InputConnector<Temperature> getTargetTemperatureConnector() {
        return targetTemperatureConnector;
    }

    public Temperature getTargetTemperature() {
        return targetTemperatureConnector.getConnectorData();
    }

    public void setTargetTemperature(Temperature targetTemperature) {
        Validators.requireNotNull(targetTemperature);
        targetTemperatureConnector.setConnectorData(targetTemperature);
    }

    public static CoolingFromTemperatureNode of(FlowOfHumidAir inletAirFlow,
                                                CoolantData coolantData,
                                                Temperature targetTemperature) {

        Validators.requireNotNull(inletAirFlow);
        Validators.requireNotNull(coolantData);
        Validators.requireNotNull(targetTemperature);


        return new CoolingFromTemperatureNode(
                InputConnector.of(inletAirFlow),
                InputConnector.of(coolantData),
                InputConnector.of(targetTemperature)
        );
    }


    public static CoolingFromTemperatureNode of(InputConnector<FlowOfHumidAir> inletAirFlow,
                                                InputConnector<CoolantData> coolantData,
                                                InputConnector<Temperature> targetTemperature) {

        Validators.requireNotNull(inletAirFlow);
        Validators.requireNotNull(coolantData);
        Validators.requireNotNull(targetTemperature);

        return new CoolingFromTemperatureNode(inletAirFlow, coolantData, targetTemperature);
    }

    public static CoolingFromTemperatureNode of(CoolantData coolantData,
                                                Temperature targetTemperature) {

        Validators.requireNotNull(coolantData);
        Validators.requireNotNull(targetTemperature);

        return new CoolingFromTemperatureNode(
                InputConnector.createEmpty(FlowOfHumidAir.class),
                InputConnector.of(coolantData),
                InputConnector.of(targetTemperature)
        );
    }

}