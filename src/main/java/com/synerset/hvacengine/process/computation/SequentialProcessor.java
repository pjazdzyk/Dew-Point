package com.synerset.hvacengine.process.computation;

import com.synerset.hvacengine.process.ProcessResult;

import java.util.*;

public class SequentialProcessor {

    private final List<ProcessNode> nodes;
    private final List<ProcessResult> results;

    public SequentialProcessor() {
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

    public void runAll() {
        nodes.forEach(node -> {
            node.runProcessCalculations();
            results.add(node.getProcessResults());
        });
    }

    public ProcessResult getLastResult() {
        return results.get(results.size() - 1);
    }

    public List<ProcessResult> getAllResults() {
        return Collections.unmodifiableList(results);
    }

    public List<ProcessNode> getAllProcessNodes() {
        return Collections.unmodifiableList(nodes);
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

    public static SequentialProcessor createEmpty() {
        return new SequentialProcessor();
    }

    public static SequentialProcessor of(ProcessNode... processNodes) {
        SequentialProcessor sequentialProcessor = new SequentialProcessor();
        Arrays.stream(processNodes).forEach(sequentialProcessor::addProcessNode);
        return sequentialProcessor;
    }

}
