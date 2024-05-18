package com.synerset.hvacengine.process.mixing;

import com.synerset.hvacengine.common.validation.CommonValidators;
import com.synerset.hvacengine.process.ConsoleOutputFormatters;
import com.synerset.hvacengine.process.HvacProcessBlock;
import com.synerset.hvacengine.process.ProcessType;
import com.synerset.hvacengine.process.blockmodel.ConnectorInput;
import com.synerset.hvacengine.process.blockmodel.ConnectorOutput;
import com.synerset.hvacengine.process.blockmodel.OutputConnection;
import com.synerset.hvacengine.process.mixing.dataobject.MixingResult;
import com.synerset.hvacengine.property.fluids.humidair.FlowOfHumidAir;

import java.util.ArrayList;
import java.util.List;

public class Mixing implements HvacProcessBlock {

    private static final ProcessType PROCESS_TYPE = ProcessType.MIXING;
    private final ConnectorInput<FlowOfHumidAir> inputAirFlowConnector;
    private final ConnectorOutput<FlowOfHumidAir> outputAirFlowConnector;
    private final List<ConnectorInput<FlowOfHumidAir>> inputMixingFlowConnectors;
    private MixingResult processResult;
    private MixingMode mixingMode;

    public Mixing() {
        this.inputAirFlowConnector = ConnectorInput.of(FlowOfHumidAir.class);
        this.inputMixingFlowConnectors = new ArrayList<>();
        this.outputAirFlowConnector = ConnectorOutput.of(FlowOfHumidAir.class);
    }

    public Mixing(OutputConnection<FlowOfHumidAir> blockWithAirFlow,
                  List<? extends OutputConnection<FlowOfHumidAir>> blocksWithInputMixingAirFlows) {

        this();
        CommonValidators.requireNotNull(blockWithAirFlow);
        CommonValidators.requireNotNull(blocksWithInputMixingAirFlows);
        this.inputAirFlowConnector.connectAndConsumeDataFrom(blockWithAirFlow.getOutputConnector());
        blocksWithInputMixingAirFlows.forEach(blockWithFlowOutput -> inputMixingFlowConnectors.add(
                ConnectorInput.of(blockWithFlowOutput.getOutputConnector()))
        );

    }

    @Override
    public MixingResult runProcessCalculations() {
        inputAirFlowConnector.updateConnectorData();
        inputMixingFlowConnectors.forEach(ConnectorInput::updateConnectorData);
        List<FlowOfHumidAir> recirculationFlows = inputMixingFlowConnectors.stream()
                .map(ConnectorInput::getConnectorData)
                .toList();

        FlowOfHumidAir inletAirFlow = inputAirFlowConnector.getConnectorData();

        MixingResult mixingProcessResults;

        if (recirculationFlows.isEmpty()) {
            mixingMode = MixingMode.SIMPLE_MIXING;
            mixingProcessResults = MixingResult.builder()
                    .processMode(mixingMode)
                    .inletAirFlow(inletAirFlow)
                    .outletAirFlow(inletAirFlow)
                    .recirculationFlows(List.of())
                    .build();
        } else if (recirculationFlows.size() == 1) {
            mixingProcessResults = MixingEquations.mixingOfTwoAirFlows(inletAirFlow, recirculationFlows.get(0));
            mixingMode = MixingMode.SIMPLE_MIXING;
        } else {
            mixingProcessResults = MixingEquations.mixingOfMultipleFlows(inletAirFlow, recirculationFlows);
            mixingMode = MixingMode.MULTIPLE_MIXING;
        }

        outputAirFlowConnector.setConnectorData(mixingProcessResults.outletAirFlow());
        this.processResult = mixingProcessResults;

        return mixingProcessResults;
    }

    @Override
    public MixingResult getProcessResult() {
        return processResult;
    }

    @Override
    public ProcessType getProcessType() {
        return PROCESS_TYPE;
    }

    public MixingMode getProcessMode() {
        return mixingMode;
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
        return ConsoleOutputFormatters.mixingConsoleOutput(processResult);
    }

    // Methods specific for this class
    public List<ConnectorInput<FlowOfHumidAir>> getInputMixingFlowConnectors() {
        return inputMixingFlowConnectors;
    }

    public List<FlowOfHumidAir> getUnwrappedMixingFlows() {
        return inputMixingFlowConnectors.stream()
                .map(ConnectorInput::getConnectorData)
                .toList();
    }

    public void resetMixingFlows() {
        inputMixingFlowConnectors.clear();
    }

    public void connectMixingFlowDataSources(List<? extends OutputConnection<FlowOfHumidAir>> blocksWithInputMixingAirFlows) {
        CommonValidators.requireNotNull(blocksWithInputMixingAirFlows);
        resetMixingFlows();
        blocksWithInputMixingAirFlows.forEach(blockWithFlowOutput -> inputMixingFlowConnectors.add(
                ConnectorInput.of(blockWithFlowOutput.getOutputConnector()))
        );
    }

    public void connectMixingFlowDataSource(OutputConnection<FlowOfHumidAir> blockWithInputMixingAirFlow) {
        CommonValidators.requireNotNull(blockWithInputMixingAirFlow);
        resetMixingFlows();
        inputMixingFlowConnectors.add(ConnectorInput.of(blockWithInputMixingAirFlow.getOutputConnector()));
    }

    public void addMixingFlowDataSource(OutputConnection<FlowOfHumidAir> blockWithInputMixingAirFlow){
        CommonValidators.requireNotNull(blockWithInputMixingAirFlow);
        inputMixingFlowConnectors.add(ConnectorInput.of(blockWithInputMixingAirFlow.getOutputConnector()));
    }

    // Static factory methods
    public static Mixing of() {
        return new Mixing();
    }

    public static Mixing of(List<? extends OutputConnection<FlowOfHumidAir>> blocksWithInputMixingAirFlows) {
        Mixing mixing = new Mixing();
        mixing.connectMixingFlowDataSources(blocksWithInputMixingAirFlows);
        return mixing;
    }

    public static Mixing of(OutputConnection<FlowOfHumidAir> blockWithInputMixingAirFlow) {
        Mixing mixing = new Mixing();
        mixing.connectMixingFlowDataSources(List.of(blockWithInputMixingAirFlow));
        return mixing;
    }

    public static Mixing of(OutputConnection<FlowOfHumidAir> blockWithAirFlow,
                            List<? extends OutputConnection<FlowOfHumidAir>> blocksWithInputMixingAirFlows) {

        return new Mixing(blockWithAirFlow, blocksWithInputMixingAirFlows);
    }

    public static Mixing of(OutputConnection<FlowOfHumidAir> blockWithAirFlow,
                            OutputConnection<FlowOfHumidAir> blockWithInputMixingAirFlow) {

        return new Mixing(blockWithAirFlow, List.of(blockWithInputMixingAirFlow));
    }

}