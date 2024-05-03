package com.synerset.hvacengine.process.mixing;

import com.synerset.hvacengine.common.validation.CommonValidators;
import com.synerset.hvacengine.process.*;
import com.synerset.hvacengine.process.mixing.dataobject.MixingResult;
import com.synerset.hvacengine.property.fluids.humidair.FlowOfHumidAir;

import java.util.*;
import java.util.stream.Collectors;

public class MixingBlock implements ProcessBlock {

    private final InputConnector<FlowOfHumidAir> inputAirFlowConnector;
    private final OutputConnector<FlowOfHumidAir> outputAirFlowConnector;
    private final List<InputConnector<FlowOfHumidAir>> mixingFlowConnectors;
    private MixingResult mixingResult;

    public MixingBlock(InputConnector<FlowOfHumidAir> inputAirFlowConnector,
                       List<InputConnector<FlowOfHumidAir>> inputMixingFlowsConnectors) {

        CommonValidators.requireNotNull(inputAirFlowConnector);

        if (inputMixingFlowsConnectors == null) {
            this.mixingFlowConnectors = new ArrayList<>();
        } else {
            this.mixingFlowConnectors = inputMixingFlowsConnectors.stream()
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
        }

        this.inputAirFlowConnector = inputAirFlowConnector;
        this.outputAirFlowConnector = OutputConnector.of(inputAirFlowConnector.getConnectorData());
    }

    @Override
    public MixingResult runProcessCalculations() {
        inputAirFlowConnector.updateConnectorData();
        mixingFlowConnectors.forEach(InputConnector::updateConnectorData);
        List<FlowOfHumidAir> recirculationFlows = mixingFlowConnectors.stream()
                .map(InputConnector::getConnectorData)
                .toList();

        FlowOfHumidAir inletAirFlow = inputAirFlowConnector.getConnectorData();

        MixingResult mixingProcessResults;

        if (recirculationFlows.isEmpty()) {
            mixingProcessResults = MixingResult.builder()
                    .processMode(ProcessMode.SIMPLE_MIXING)
                    .inletAirFlow(inletAirFlow)
                    .outletAirFlow(inletAirFlow)
                    .recirculationFlows(List.of())
                    .build();
        } else if (recirculationFlows.size() == 1) {
            mixingProcessResults = MixingEquations.mixingOfTwoAirFlows(inletAirFlow, recirculationFlows.get(0));
        } else {
            mixingProcessResults = MixingEquations.mixingOfMultipleFlows(inletAirFlow, recirculationFlows);
        }

        outputAirFlowConnector.setConnectorData(mixingProcessResults.outletAirFlow());
        this.mixingResult = mixingProcessResults;

        return mixingProcessResults;
    }

    @Override
    public MixingResult getProcessResults() {
        return mixingResult;
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
        if (inputAirFlowConnector.getConnectorData() == null || mixingResult == null) {
            return "Results not available. Run process first.";
        }
        return ConsoleOutputFormatters.mixingConsoleOutput(mixingResult);
    }

    public List<InputConnector<FlowOfHumidAir>> getMixingFlowConnectors() {
        return mixingFlowConnectors;
    }

    public List<FlowOfHumidAir> getMixingFlows() {
        return mixingFlowConnectors.stream()
                .map(InputConnector::getConnectorData)
                .toList();
    }

    public void resetMixingFlows() {
        mixingFlowConnectors.clear();
    }

    public void addMixingFlowConnector(InputConnector<FlowOfHumidAir> mixingFlowConnector) {
        mixingFlowConnectors.add(mixingFlowConnector);
    }

    public void addMixingFlow(FlowOfHumidAir... flowOfHumidAir) {
        List<InputConnector<FlowOfHumidAir>> mixingFlows = Arrays.stream(flowOfHumidAir)
                .map(InputConnector::of)
                .toList();
        mixingFlowConnectors.addAll(mixingFlows);
    }

    public static MixingBlock of(FlowOfHumidAir inletAirFlow, Collection<FlowOfHumidAir> inputMixingFlows) {

        CommonValidators.requireNotNull(inletAirFlow);

        if (inputMixingFlows == null || inputMixingFlows.isEmpty()) {
            inputMixingFlows = new ArrayList<>();
        }

        List<InputConnector<FlowOfHumidAir>> inputMixingConnectors = inputMixingFlows.stream()
                .filter(Objects::nonNull)
                .map(InputConnector::of)
                .collect(Collectors.toList());

        return new MixingBlock(InputConnector.of(inletAirFlow), inputMixingConnectors);
    }

    public static MixingBlock of(Collection<FlowOfHumidAir> inputMixingFlows) {
        if (inputMixingFlows == null || inputMixingFlows.isEmpty()) {
            inputMixingFlows = new ArrayList<>();
        }

        List<InputConnector<FlowOfHumidAir>> inputMixingConnectors = inputMixingFlows.stream()
                .filter(Objects::nonNull)
                .map(InputConnector::of)
                .collect(Collectors.toList());

        return new MixingBlock(InputConnector.createEmpty(FlowOfHumidAir.class), inputMixingConnectors);
    }

    public static MixingBlock of(FlowOfHumidAir inletAirFlow, FlowOfHumidAir recirculationAirFlow) {

        CommonValidators.requireNotNull(inletAirFlow);

        ArrayList<InputConnector<FlowOfHumidAir>> inputMixingConnector = new ArrayList<>();

        if (recirculationAirFlow != null) {
            inputMixingConnector.add(InputConnector.of(recirculationAirFlow));
        }

        return new MixingBlock(InputConnector.of(inletAirFlow), inputMixingConnector);
    }

    public static MixingBlock of(InputConnector<FlowOfHumidAir> inletAirFlow,
                                 List<InputConnector<FlowOfHumidAir>> mixingFlowConnectors) {

        CommonValidators.requireNotNull(inletAirFlow);
        if (mixingFlowConnectors == null || mixingFlowConnectors.isEmpty()) {
            mixingFlowConnectors = new ArrayList<>();
        }

        return new MixingBlock(inletAirFlow, mixingFlowConnectors);
    }

}