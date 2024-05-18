package com.synerset.hvacengine.process.procedure;

import com.synerset.hvacengine.common.ConsolePrintable;
import com.synerset.hvacengine.common.exception.HvacEngineArgumentException;
import com.synerset.hvacengine.process.HvacProcessBlock;
import com.synerset.hvacengine.process.ProcessResult;
import com.synerset.hvacengine.process.ProcessType;
import com.synerset.hvacengine.process.source.SimpleDataSource;
import com.synerset.hvacengine.property.fluids.humidair.FlowOfHumidAir;

import java.util.*;

public class SequentialProcessingEngine implements ConsolePrintable {

    private final List<HvacProcessBlock> processBlocksToCompute;
    private final List<ProcessResult> processResults;
    private SimpleDataSource<FlowOfHumidAir> airFlowSource;

    public SequentialProcessingEngine() {
        this.processResults = new ArrayList<>();
        this.processBlocksToCompute = new ArrayList<>();
    }

    public int addProcessNode(HvacProcessBlock processNode) {
        if (processNode == null) {
            return -1;
        }
        if (!processBlocksToCompute.isEmpty()) {
            HvacProcessBlock previousNode = processBlocksToCompute.get(processBlocksToCompute.size() - 1);
            processNode.getInputConnector().connectAndConsumeDataFrom(previousNode.getOutputConnector());
        }
        processBlocksToCompute.add(processNode);
        return processBlocksToCompute.size() - 1;
    }

    public ProcessResult runCalculationsForAllNodes() {
        if(processBlocksToCompute.isEmpty()){
            throw new HvacEngineArgumentException("No process found. Cannot run calculations");
        }

        HvacProcessBlock firstProcessBlock = processBlocksToCompute.get(0);

        if(airFlowSource != null){
            firstProcessBlock.connectAirFlowDataSource(airFlowSource);
        }

        if(firstProcessBlock.getInputConnector().getConnectorData() == null){
            throw new HvacEngineArgumentException("No inlet airflow data found. Cannot run calculations");
        }

        processBlocksToCompute.forEach(node -> {
            node.runProcessCalculations();
            processResults.add(node.getProcessResult());
        });

        return getLastResult();
    }

    // Results extraction
    public List<ProcessResult> getProcessResults() {
        return Collections.unmodifiableList(processResults);
    }

    public List<ProcessResult> getResults(ProcessType processType){
        return processResults.stream().filter(result -> result.processType() == processType).toList();
    }

    public ProcessResult getLastResult() {
        return processResults.get(processResults.size() - 1);
    }

    public List<HvacProcessBlock> getAllProcessBlocks() {
        return Collections.unmodifiableList(processBlocksToCompute);
    }

    // Results console printers
    @Override
    public String toConsoleOutput() {
        StringBuilder stringBuilder = new StringBuilder();
        processResults.forEach(result -> stringBuilder.append(result.toConsoleOutput()).append("\n"));
        return stringBuilder.toString();
    }

    public String toConsoleOutputLastResult() {
        return getLastProcessBlock()
                .map(ProcessResult::toConsoleOutput)
                .orElse("Results not available");
    }

    public void connectInletAirFlowDataSource(SimpleDataSource<FlowOfHumidAir> airFlowSource) {
        this.airFlowSource = airFlowSource;
    }

    // Helpers
    private Optional<ProcessResult> getLastProcessBlock() {
        if (processBlocksToCompute.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(processResults.get(processResults.size() - 1));
    }

    // Static factory methods
    public static SequentialProcessingEngine of() {
        return new SequentialProcessingEngine();
    }

    public static SequentialProcessingEngine of(SimpleDataSource<FlowOfHumidAir> airFlowSource) {
        SequentialProcessingEngine sequentialProcessingEngine = new SequentialProcessingEngine();
        sequentialProcessingEngine.connectInletAirFlowDataSource(airFlowSource);
        return sequentialProcessingEngine;
    }

    public static SequentialProcessingEngine of(HvacProcessBlock... processNodes) {
        SequentialProcessingEngine sequentialProcessingEngine = new SequentialProcessingEngine();
        Arrays.stream(processNodes).forEach(sequentialProcessingEngine::addProcessNode);
        return sequentialProcessingEngine;
    }

    public static SequentialProcessingEngine of(SimpleDataSource<FlowOfHumidAir> airFlowSource, HvacProcessBlock... processNodes) {
        SequentialProcessingEngine sequentialProcessingEngine = new SequentialProcessingEngine();
        sequentialProcessingEngine.connectInletAirFlowDataSource(airFlowSource);
        Arrays.stream(processNodes).forEach(sequentialProcessingEngine::addProcessNode);
        return sequentialProcessingEngine;
    }

}