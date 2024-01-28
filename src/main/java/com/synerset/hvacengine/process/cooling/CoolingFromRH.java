package com.synerset.hvacengine.process.cooling;

import com.synerset.brentsolver.BrentSolver;
import com.synerset.hvacengine.fluids.humidair.FlowOfHumidAir;
import com.synerset.hvacengine.fluids.humidair.HumidAirEquations;
import com.synerset.hvacengine.fluids.liquidwater.FlowOfLiquidWater;
import com.synerset.hvacengine.fluids.liquidwater.LiquidWater;
import com.synerset.unitility.unitsystem.dimensionless.BypassFactor;
import com.synerset.unitility.unitsystem.flows.MassFlow;
import com.synerset.unitility.unitsystem.humidity.RelativeHumidity;
import com.synerset.unitility.unitsystem.thermodynamic.Power;
import com.synerset.unitility.unitsystem.thermodynamic.Temperature;

/**
 * Returns real cooling coil process result as double array, to achieve expected outlet Relative Humidity.
 * Results in the array are organized as following:
 * result: [heat in (W), outlet air temperature (oC), outlet humidity ratio x (kgWv/kgDa), condensate temperature (oC),
 * condensate mass flow (kg/s)]
 * REFERENCE SOURCE: [1] [t2,oC] (-) [37]
 *
 * @param inletAir               initial {@link FlowOfHumidAir}
 * @param coolantData            average cooling coil wall {@link CoolantData}
 * @param targetRelativeHumidity expected outlet {@link RelativeHumidity}
 */
record CoolingFromRH(FlowOfHumidAir inletAir,
                     CoolantData coolantData,
                     RelativeHumidity targetRelativeHumidity) implements CoolingStrategy {

    @Override
    public AirCoolingResult applyCooling() {
        double pIn = inletAir.getPressure().getInPascals();

        Temperature averageWallTemp = coolantData.getAverageTemperature();

        if (inletAir.relativeHumidity().equals(targetRelativeHumidity)) {
            LiquidWater liquidWater = LiquidWater.of(inletAir.getTemperature());
            FlowOfLiquidWater flowOfLiquidWater = FlowOfLiquidWater.of(liquidWater, MassFlow.ofKilogramsPerSecond(0.0));
            BypassFactor bypassFactor = CoolingHelpers.coilBypassFactor(averageWallTemp, inletAir.getTemperature(), inletAir.getTemperature());
            return new AirCoolingResult(inletAir, Power.ofWatts(0.0), flowOfLiquidWater, bypassFactor);
        }

        // Iterative procedure to determine which outlet temperature will result in expected RH.
        BrentSolver solver = new BrentSolver("calcCoolingFromOutletRH SOLVER");
        double tIn = inletAir.getTemperature().getInCelsius();
        double tdpIn = inletAir.getTemperature().getInCelsius();
        solver.setCounterpartPoints(tIn, tdpIn);
        double rhOut = targetRelativeHumidity.getInPercent();
        Cooling[] coolingResults = new Cooling[1];

        solver.calcForFunction(testOutTx -> {
            Cooling tempCooling = Cooling.of(CoolingStrategy.of(inletAir, coolantData, Temperature.ofCelsius(testOutTx)));
            coolingResults[0] = tempCooling;
            double outTx = tempCooling.getOutTemperature().getInCelsius();
            double outX = tempCooling.getOutHumidityRatio().getInKilogramPerKilogram();
            double actualRH = HumidAirEquations.relativeHumidity(outTx, outX, pIn);
            return rhOut - actualRH;
        });

        return coolingResults[0].getCoolingBulkResults();
    }

}