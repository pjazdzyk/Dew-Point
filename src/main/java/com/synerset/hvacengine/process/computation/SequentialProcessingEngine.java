package com.synerset.hvacengine.process.computation;

import com.synerset.hvacengine.fluids.humidair.FlowOfHumidAir;
import com.synerset.hvacengine.process.ProcessResult;
import com.synerset.hvacengine.process.ProcessType;

import java.util.*;

public class SequentialProcessingEngine {

    private final List<ProcessNode> nodes;
    private final List<ProcessResult> results;
    private FlowOfHumidAir inletAirFlow;

    public SequentialProcessingEngine() {
        this.results = new ArrayList<>();
        this.nodes = new ArrayList<>();
    }

    public int addProcessNode(ProcessNode processNode) {
        if (processNode == null) {
            return -1;
        }
        if (!nodes.isEmpty()) {
            ProcessNode previousNode = nodes.get(nodes.size() - 1);
            processNode.getAirFlowInputConnector().connectToOutputConnector(previousNode.getAirFlowOutputConnector());
        }
        nodes.add(processNode);
        return nodes.size() - 1;
    }

    public ProcessResult runCalculationsForAllNodes() {
        if(inletAirFlow != null && !nodes.isEmpty()){
            nodes.get(0).getAirFlowInputConnector().setConnectorData(inletAirFlow);
        }
        nodes.forEach(node -> {
            node.runProcessCalculations();
            results.add(node.getProcessResults());
        });
        return getLastResult();
    }

    public ProcessResult getLastResult() {
        return results.get(results.size() - 1);
    }

    public List<ProcessResult> getAllResults() {
        return Collections.unmodifiableList(results);
    }

    public List<ProcessResult> getResultsOfType(ProcessType processType){
        return results.stream().filter(result -> result.processType() == processType).toList();
    }

    public List<ProcessNode> getAllProcessNodes() {
        return Collections.unmodifiableList(nodes);
    }

    public void setInletAirFlow(FlowOfHumidAir inletAirFlow) {
        this.inletAirFlow = inletAirFlow;
    }

    public String toConsoleOutputLastResult() {
        return getLastProcessNode()
                .map(ProcessResult::toConsoleOutput)
                .orElse("Results not available");
    }

    public String toConsoleOutputAllResults() {
        StringBuilder stringBuilder = new StringBuilder();
        results.forEach(result -> stringBuilder.append(result.toConsoleOutput()).append("\n"));
        return stringBuilder.toString();
    }

    private Optional<ProcessResult> getLastProcessNode() {
        if (nodes.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(results.get(results.size() - 1));
    }

    public static SequentialProcessingEngine createEmpty() {
        return new SequentialProcessingEngine();
    }

    public static SequentialProcessingEngine of(FlowOfHumidAir inletAirFLow){
        SequentialProcessingEngine sequentialProcessingEngine = new SequentialProcessingEngine();
        sequentialProcessingEngine.setInletAirFlow(inletAirFLow);
        return sequentialProcessingEngine;
    }

    public static SequentialProcessingEngine of(ProcessNode... processNodes) {
        SequentialProcessingEngine sequentialProcessingEngine = new SequentialProcessingEngine();
        Arrays.stream(processNodes).forEach(sequentialProcessingEngine::addProcessNode);
        return sequentialProcessingEngine;
    }

}