package com.synerset.hvacengine.process.common;

import com.synerset.hvacengine.fluids.humidair.FlowOfHumidAir;
import com.synerset.hvacengine.fluids.liquidwater.FlowOfLiquidWater;
import com.synerset.hvacengine.process.cooling.CoolantData;
import com.synerset.hvacengine.process.cooling.dataobject.CoolingNodeResult;
import com.synerset.hvacengine.process.cooling.dataobject.DryCoolingResult;
import com.synerset.hvacengine.process.cooling.dataobject.RealCoolingResult;
import com.synerset.hvacengine.process.heating.dataobject.HeatingResult;
import com.synerset.hvacengine.process.mixing.dataobject.MixingResult;
import com.synerset.unitility.unitsystem.flow.MassFlow;
import com.synerset.unitility.unitsystem.flow.VolumetricFlow;
import com.synerset.unitility.unitsystem.thermodynamic.Power;
import com.synerset.unitility.unitsystem.thermodynamic.Temperature;

import java.util.List;

public class ConsoleOutputFormatters {

    private static final String SEPARATOR = " | ";
    private static final String NEW_LINE = "\n\t";
    private static final int REL_DIGITS = 3;

    private ConsoleOutputFormatters() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * Returns a formatted string representation of the heating process for console output, including input and output
     * properties.
     *
     * @return A formatted string representation of the heating process.
     */
    public static String heatingConsoleOutput(HeatingResult heatingResult) {
        return "PROCESS OF HEATING:" + NEW_LINE +
               inputFlowConsoleOutput(heatingResult.inletAirFlow()) + NEW_LINE +
               heatingPowerConsoleOutput(heatingResult.heatOfProcess()) + NEW_LINE +
               outputFlowConsoleOutput(heatingResult.outletAirFlow()) + NEW_LINE;
    }

    /**
     * Returns a formatted string representation of the dry cooling process for console output, including input and output
     * properties.
     *
     * @return A formatted string representation of the cooling process.
     */
    public static String dryCoolingConsoleOutput(DryCoolingResult airCoolingResult) {
        return "PROCESS OF DRY COOLING:" + NEW_LINE +
               inputFlowConsoleOutput(airCoolingResult.inletAirFlow()) + NEW_LINE +
               coolingPowerConsoleOutput(airCoolingResult.heatOfProcess()) + NEW_LINE +
               outputFlowConsoleOutput(airCoolingResult.outletAirFlow()) + NEW_LINE;
    }

    /**
     * Returns a formatted string representation of the real cooling process for console output, including input and output
     * properties.
     *
     * @return A formatted string representation of the cooling process.
     */
    public static String coolingConsoleOutput(RealCoolingResult airCoolingResult) {
        return "PROCESS OF REAL COOLING:" + NEW_LINE +
               inputFlowConsoleOutput(airCoolingResult.inletAirFlow()) + NEW_LINE +
               "COOLANT DATA:" + NEW_LINE +
               airCoolingResult.coolantData().getSupplyTemperature().toEngineeringFormat("t_su", REL_DIGITS) + SEPARATOR +
               airCoolingResult.coolantData().getReturnTemperature().toEngineeringFormat("t_rt", REL_DIGITS) + SEPARATOR +
               airCoolingResult.coolantData().getAverageTemperature().toEngineeringFormat("t_m", REL_DIGITS) + NEW_LINE +
               coolingPowerConsoleOutput(airCoolingResult.heatOfProcess()) + SEPARATOR +
               airCoolingResult.bypassFactor().toEngineeringFormat("BF", REL_DIGITS) + NEW_LINE +
               outputFlowConsoleOutput(airCoolingResult.outletAirFlow()) + NEW_LINE +
               condensateFlowConsoleOutput(airCoolingResult.condensateFlow()) + NEW_LINE;
    }

    /**
     * Returns a formatted string representation of the real cooling process for console output from node, including input and output
     * properties.
     *
     * @return A formatted string representation of the cooling process.
     */
    public static String coolingNodeConsoleOutput(CoolingNodeResult airCoolingResult) {
        Temperature supplyTemperature = airCoolingResult.coolantSupplyFlow().getTemperature();
        Temperature returnTemperature = airCoolingResult.coolantReturnFlow().getTemperature();
        CoolantData coolantData = CoolantData.of(supplyTemperature, returnTemperature);
        MassFlow coolantMassFlow = airCoolingResult.coolantSupplyFlow().getMassFlow();
        VolumetricFlow coolantVolFlow = airCoolingResult.coolantSupplyFlow().getVolFlow();

        return "PROCESS OF REAL COOLING:" + NEW_LINE +
               inputFlowConsoleOutput(airCoolingResult.inletAirFlow()) + NEW_LINE +

               "COOLANT DATA:" + NEW_LINE +
               coolantData.getSupplyTemperature().toEngineeringFormat("t_su", REL_DIGITS) + SEPARATOR +
               coolantData.getReturnTemperature().toEngineeringFormat("t_rt", REL_DIGITS) + SEPARATOR +
               coolantData.getAverageTemperature().toEngineeringFormat("t_m", REL_DIGITS) + NEW_LINE +
               coolantVolFlow.toCubicMetersPerHour().toEngineeringFormat("V_coolant", REL_DIGITS) + SEPARATOR +
               coolantMassFlow.toKilogramsPerSecond().toEngineeringFormat("G_coolant", REL_DIGITS) + NEW_LINE +

               coolingPowerConsoleOutput(airCoolingResult.heatOfProcess()) + NEW_LINE +
               outputFlowConsoleOutput(airCoolingResult.outletAirFlow()) + NEW_LINE +
               condensateFlowConsoleOutput(airCoolingResult.condensateFlow()) + NEW_LINE;
    }

    /**
     * Generates a formatted string representation of the mixing process for console output, including information
     * about the input flow, recirculation air flows, and the outlet flow.
     *
     * @return A formatted string representing the details of the mixing process.
     */
    public static String mixingConsoleOutput(MixingResult mixingResult) {
        StringBuilder stringBuilder = new StringBuilder();
        List<FlowOfHumidAir> recirculationFlows = mixingResult.recirculationFlows().stream().toList();
        for (int i = 0; i < recirculationFlows.size(); i++) {
            String flowAsString = mixingConsoleOutput(recirculationFlows.get(i), "RECIRCULATION AIR_" + i + ":", "rec_" + i);
            stringBuilder.append(flowAsString).append(NEW_LINE);
        }
        String recirculationFlowsPart = stringBuilder.toString();

        return "PROCESS OF MIXING:" + NEW_LINE +
               mixingConsoleOutput(mixingResult.inletAirFlow(), "INPUT FLOW:", "in") + NEW_LINE +
               recirculationFlowsPart +
               mixingConsoleOutput(mixingResult.outletAirFlow(), "OUTLET FLOW:", "out") + NEW_LINE;
    }

    private static String outputFlowConsoleOutput(FlowOfHumidAir outletAirFlow) {
        return "OUTLET FLOW:" + NEW_LINE +
               outletAirFlow.getVolFlow().toCubicMetersPerHour().toEngineeringFormat("V_out", REL_DIGITS) + SEPARATOR +
               outletAirFlow.getMassFlow().toEngineeringFormat("G_out", REL_DIGITS) + SEPARATOR +
               outletAirFlow.getDryAirMassFlow().toEngineeringFormat("G_out.da", REL_DIGITS) + NEW_LINE +
               outletAirFlow.getTemperature().toEngineeringFormat("DBT_out", REL_DIGITS) + SEPARATOR +
               outletAirFlow.getRelativeHumidity().toEngineeringFormat("RH_out", REL_DIGITS) + SEPARATOR +
               outletAirFlow.getHumidityRatio().toEngineeringFormat("x_out", REL_DIGITS) + SEPARATOR +
               outletAirFlow.getSpecificEnthalpy().toEngineeringFormat("i_out", REL_DIGITS);
    }

    private static String inputFlowConsoleOutput(FlowOfHumidAir inletAirFlow) {
        return "INPUT FLOW:" + NEW_LINE +
               inletAirFlow.getVolFlow().toCubicMetersPerHour().toEngineeringFormat("V_in", REL_DIGITS) + SEPARATOR +
               inletAirFlow.getMassFlow().toEngineeringFormat("G_in", REL_DIGITS) + SEPARATOR +
               inletAirFlow.getDryAirMassFlow().toEngineeringFormat("G_in.da", REL_DIGITS) + NEW_LINE +
               inletAirFlow.getTemperature().toEngineeringFormat("DBT_in", REL_DIGITS) + SEPARATOR +
               inletAirFlow.getRelativeHumidity().toEngineeringFormat("RH_in", REL_DIGITS) + SEPARATOR +
               inletAirFlow.getHumidityRatio().toEngineeringFormat("x_in", REL_DIGITS) + SEPARATOR +
               inletAirFlow.getSpecificEnthalpy().toEngineeringFormat("i_in", REL_DIGITS);
    }

    private static String coolingPowerConsoleOutput(Power power) {
        return "COOLING POWER:" + NEW_LINE +
               power.toWatts().toEngineeringFormat("Q_cool", REL_DIGITS) + SEPARATOR +
               power.toKiloWatts().toEngineeringFormat("Q_cool", REL_DIGITS);
    }

    private static String heatingPowerConsoleOutput(Power power) {
        return "HEATING POWER:" + NEW_LINE +
               power.toWatts().toEngineeringFormat("Q_heat", REL_DIGITS) + SEPARATOR +
               power.toKiloWatts().toEngineeringFormat("Q_heat", REL_DIGITS);
    }

    private static String condensateFlowConsoleOutput(FlowOfLiquidWater condensateFlow) {
        return "CONDENSATE:" + NEW_LINE +
               condensateFlow.getMassFlow().toEngineeringFormat("G_cond", REL_DIGITS) + SEPARATOR +
               condensateFlow.getTemperature().toEngineeringFormat("t_cond", REL_DIGITS) + SEPARATOR +
               condensateFlow.getSpecificEnthalpy().toEngineeringFormat("i_cond", REL_DIGITS);
    }

    private static String mixingConsoleOutput(FlowOfHumidAir flowOfAir, String title, String suffix) {
        return title + NEW_LINE +
               flowOfAir.getVolFlow().toCubicMetersPerHour().toEngineeringFormat("V_" + suffix, REL_DIGITS) + SEPARATOR +
               flowOfAir.getMassFlow().toEngineeringFormat("G_" + suffix, REL_DIGITS) + SEPARATOR +
               flowOfAir.getDryAirMassFlow().toEngineeringFormat("G_" + suffix + ".da", REL_DIGITS) + NEW_LINE +
               flowOfAir.getTemperature().toEngineeringFormat("DBT_" + suffix, REL_DIGITS) + SEPARATOR +
               flowOfAir.getRelativeHumidity().toEngineeringFormat("RH_" + suffix, REL_DIGITS) + SEPARATOR +
               flowOfAir.getHumidityRatio().toEngineeringFormat("x_" + suffix, REL_DIGITS) + SEPARATOR +
               flowOfAir.getSpecificEnthalpy().toEngineeringFormat("i_" + suffix, REL_DIGITS);
    }

}
