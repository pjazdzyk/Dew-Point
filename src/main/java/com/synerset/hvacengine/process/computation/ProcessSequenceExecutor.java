package com.synerset.hvacengine.process.computation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ProcessSequenceExecutor {

    private final List<ProcessNode> nodes;
    private final List<ProcessResult> results;

    public ProcessSequenceExecutor() {
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
        return results.get(nodes.size() - 1);
    }

    public List<ProcessResult> getAllResults() {
        return Collections.unmodifiableList(results);
    }

    public List<ProcessNode> getAllProcessNodes() {
        return Collections.unmodifiableList(nodes);
    }
}
