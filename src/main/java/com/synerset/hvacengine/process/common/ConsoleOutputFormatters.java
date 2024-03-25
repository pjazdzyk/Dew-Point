package com.synerset.hvacengine.process.common;

import com.synerset.hvacengine.fluids.humidair.FlowOfHumidAir;
import com.synerset.hvacengine.fluids.liquidwater.FlowOfLiquidWater;
import com.synerset.hvacengine.process.cooling.AirCoolingNodeResult;
import com.synerset.hvacengine.process.cooling.AirCoolingResult;
import com.synerset.hvacengine.process.cooling.CoolantData;
import com.synerset.hvacengine.process.heating.AirHeatingResult;
import com.synerset.unitility.unitsystem.thermodynamic.Power;

public class ConsoleOutputFormatters {

    private ConsoleOutputFormatters() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * Returns a formatted string representation of the heating process for console output, including input and output
     * properties.
     *
     * @return A formatted string representation of the heating process.
     */
    public static String heatingConsoleOutput(FlowOfHumidAir inletAirFlow, AirHeatingResult airHeatingResult) {
        String separator = " | ";
        String end = "\n\t";
        int digits = 3;

        Power heatOfProcess = airHeatingResult.heatOfProcess();
        FlowOfHumidAir outletAirFlow = airHeatingResult.outletAirFlow();


        return "PROCESS OF HEATING:" + end +

               "INPUT FLOW:" + end +
               inletAirFlow.getVolFlow().toCubicMetersPerHour().toEngineeringFormat("V_in", digits) + separator +
               inletAirFlow.getMassFlow().toEngineeringFormat("G_in", digits) + separator +
               inletAirFlow.getDryAirMassFlow().toEngineeringFormat("G_in.da", digits) + end +

               inletAirFlow.getTemperature().toEngineeringFormat("DBT_in", digits) + separator +
               inletAirFlow.getRelativeHumidity().toEngineeringFormat("RH_in", digits) + separator +
               inletAirFlow.getHumidityRatio().toEngineeringFormat("x_in", digits) + separator +
               inletAirFlow.getSpecificEnthalpy().toEngineeringFormat("i_in", digits) + end +

               "HEATING POWER:" + end +
               heatOfProcess.toEngineeringFormat("Q_heat", digits) + separator +
               heatOfProcess.toKiloWatts().toEngineeringFormat("Q_heat", digits) + end +

               "OUTLET FLOW:" + end +
               outletAirFlow.getVolFlow().toCubicMetersPerHour().toEngineeringFormat("V_out", digits) + separator +
               outletAirFlow.getMassFlow().toEngineeringFormat("G_out", digits) + separator +
               outletAirFlow.getDryAirMassFlow().toEngineeringFormat("G_out.da", digits) + end +

               outletAirFlow.getTemperature().toEngineeringFormat("DBT_out", digits) + separator +
               outletAirFlow.getRelativeHumidity().toEngineeringFormat("RH_out", digits) + separator +
               outletAirFlow.getHumidityRatio().toEngineeringFormat("x_out", digits) + separator +
               outletAirFlow.getSpecificEnthalpy().toEngineeringFormat("i_out", digits) + end;
    }


    /**
     * Returns a formatted string representation of the cooling process for console output, including input and output
     * properties.
     *
     * @return A formatted string representation of the cooling process.
     */
    public static String coolingConsoleOutput(FlowOfHumidAir inletAirFlow, CoolantData coolantData, AirCoolingResult airCoolingResult) {
        String separator = " | ";
        String end = "\n\t";
        int digits = 3;
        FlowOfHumidAir outletAirFlow = airCoolingResult.outletAirFlow();
        FlowOfLiquidWater condensateFlow = airCoolingResult.condensateFlow();

        return "PROCESS OF COOLING:" + end +

               "INPUT FLOW:" + end +
               inletAirFlow.getVolFlow().toCubicMetersPerHour().toEngineeringFormat("V_in", digits) + separator +
               inletAirFlow.getMassFlow().toEngineeringFormat("G_in", digits) + separator +
               inletAirFlow.getDryAirMassFlow().toEngineeringFormat("G_in.da", digits) + end +

               inletAirFlow.getTemperature().toEngineeringFormat("DBT_in", digits) + separator +
               inletAirFlow.getRelativeHumidity().toEngineeringFormat("RH_in", digits) + separator +
               inletAirFlow.getHumidityRatio().toEngineeringFormat("x_in", digits) + separator +
               inletAirFlow.getSpecificEnthalpy().toEngineeringFormat("i", digits) + end +

               "COOLANT DATA:" + end +
               coolantData.getSupplyTemperature().toEngineeringFormat("t_su", digits) + separator +
               coolantData.getReturnTemperature().toEngineeringFormat("t_rt", digits) + separator +
               coolantData.getAverageTemperature().toEngineeringFormat("t_m", digits) + end +

               "COOLING POWER:" + end +
               airCoolingResult.heatOfProcess().toWatts().toEngineeringFormat("Q_cool", digits) + separator +
               airCoolingResult.heatOfProcess().toKiloWatts().toEngineeringFormat("Q_cool", digits) + separator +
               airCoolingResult.bypassFactor().toEngineeringFormat("BF", digits) + end +

               "OUTLET FLOW:" + end +
               outletAirFlow.getVolFlow().toCubicMetersPerHour().toEngineeringFormat("V_out", digits) + separator +
               outletAirFlow.getMassFlow().toEngineeringFormat("G_out", digits) + separator +
               outletAirFlow.getDryAirMassFlow().toEngineeringFormat("G_out.da", digits) + end +

               outletAirFlow.getTemperature().toEngineeringFormat("DBT_out", digits) + separator +
               outletAirFlow.getRelativeHumidity().toEngineeringFormat("RH_out", digits) + separator +
               outletAirFlow.getHumidityRatio().toEngineeringFormat("x_out", digits) + separator +
               outletAirFlow.getSpecificEnthalpy().toEngineeringFormat("i", digits) + end +
               "CONDENSATE:" + end +
               condensateFlow.getMassFlow().toEngineeringFormat("G_cond", digits) + separator +
               condensateFlow.getTemperature().toEngineeringFormat("t_cond", digits) + separator +
               condensateFlow.getSpecificEnthalpy().toEngineeringFormat("i_cond", digits) + end;
    }


    /**
     * Returns a formatted string representation of the cooling process for console output, including input and output
     * properties.
     *
     * @return A formatted string representation of the cooling process.
     */
    public static String coolingConsoleOutput(FlowOfHumidAir inletAirFlow, AirCoolingNodeResult airCoolingNodeResult) {
        AirCoolingResult result = AirCoolingResult.builder()
                .heatOfProcess(airCoolingNodeResult.heatOfProcess())
                .outletAirFlow(airCoolingNodeResult.outletAirFlow())
                .bypassFactor(airCoolingNodeResult.bypassFactor())
                .condensateFlow(airCoolingNodeResult.condensateFlow())
                .build();

        CoolantData coolantData = CoolantData.of(
                airCoolingNodeResult.coolantSupplyFlow().getTemperature(),
                airCoolingNodeResult.coolantReturnFlow().getTemperature()
        );

        return coolingConsoleOutput(inletAirFlow, coolantData, result);
    }

}
