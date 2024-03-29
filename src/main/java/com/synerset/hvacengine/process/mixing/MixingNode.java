package com.synerset.hvacengine.process.mixing;

import com.synerset.hvacengine.common.Validators;
import com.synerset.hvacengine.fluids.humidair.FlowOfHumidAir;
import com.synerset.hvacengine.process.common.ConsoleOutputFormatters;
import com.synerset.hvacengine.process.computation.InputConnector;
import com.synerset.hvacengine.process.computation.OutputConnector;
import com.synerset.hvacengine.process.computation.ProcessNode;
import com.synerset.hvacengine.process.mixing.dataobject.AirMixingResult;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class MixingNode implements ProcessNode {

    private final InputConnector<FlowOfHumidAir> inputAirFlowConnector;
    private final OutputConnector<FlowOfHumidAir> outputAirFlowConnector;
    private final List<InputConnector<FlowOfHumidAir>> mixingFlowConnectors;
    private AirMixingResult mixingResult;

    public MixingNode(InputConnector<FlowOfHumidAir> inputAirFlowConnector,
                      List<InputConnector<FlowOfHumidAir>> inputMixingFlowsConnectors) {

        Validators.requireNotNull(inputAirFlowConnector);

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
    public AirMixingResult runProcessCalculations() {
        inputAirFlowConnector.updateConnectorData();
        mixingFlowConnectors.forEach(InputConnector::updateConnectorData);
        List<FlowOfHumidAir> recirculationFlows = mixingFlowConnectors.stream()
                .map(InputConnector::getConnectorData)
                .toList();

        FlowOfHumidAir inletAirFlow = inputAirFlowConnector.getConnectorData();
        AirMixingResult mixingProcessResults = MixingEquations.mixingOfMultipleFlows(inletAirFlow, recirculationFlows);

        outputAirFlowConnector.setConnectorData(mixingProcessResults.outletAirFlow());
        this.mixingResult = mixingProcessResults;

        return mixingProcessResults;
    }

    @Override
    public AirMixingResult getProcessResults() {
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

    public static MixingNode of(FlowOfHumidAir inletAirFlow, List<FlowOfHumidAir> inputMixingFlows) {

        Validators.requireNotNull(inletAirFlow);

        if (inputMixingFlows == null || inputMixingFlows.isEmpty()) {
            inputMixingFlows = new ArrayList<>();
        }

        List<InputConnector<FlowOfHumidAir>> inputMixingConnectors = inputMixingFlows.stream()
                .filter(Objects::nonNull)
                .map(InputConnector::of)
                .collect(Collectors.toList());

        return new MixingNode(InputConnector.of(inletAirFlow), inputMixingConnectors);
    }

    public static MixingNode of(FlowOfHumidAir inletAirFlow, FlowOfHumidAir recirculationAirFlow) {

        Validators.requireNotNull(inletAirFlow);

        ArrayList<InputConnector<FlowOfHumidAir>> inputMixingConnector = new ArrayList<>();

        if (recirculationAirFlow != null) {
            inputMixingConnector.add(InputConnector.of(recirculationAirFlow));
        }

        return new MixingNode(InputConnector.of(inletAirFlow), inputMixingConnector);
    }

    public static MixingNode of(InputConnector<FlowOfHumidAir> inletAirFlow,
                                List<InputConnector<FlowOfHumidAir>> mixingFlowConnectors) {

        Validators.requireNotNull(inletAirFlow);
        if (mixingFlowConnectors == null || mixingFlowConnectors.isEmpty()) {
            mixingFlowConnectors = new ArrayList<>();
        }

        return new MixingNode(inletAirFlow, mixingFlowConnectors);
    }

}