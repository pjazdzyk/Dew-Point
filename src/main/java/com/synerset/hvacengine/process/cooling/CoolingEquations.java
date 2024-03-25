package com.synerset.hvacengine.process.cooling;

import com.synerset.brentsolver.BrentSolver;
import com.synerset.hvacengine.common.Validators;
import com.synerset.hvacengine.common.exceptions.HvacEngineArgumentException;
import com.synerset.hvacengine.fluids.dryair.FlowOfDryAir;
import com.synerset.hvacengine.fluids.humidair.FlowOfHumidAir;
import com.synerset.hvacengine.fluids.humidair.HumidAir;
import com.synerset.hvacengine.fluids.humidair.HumidAirEquations;
import com.synerset.hvacengine.fluids.liquidwater.FlowOfLiquidWater;
import com.synerset.hvacengine.fluids.liquidwater.LiquidWater;
import com.synerset.hvacengine.fluids.liquidwater.LiquidWaterEquations;
import com.synerset.hvacengine.process.drycooling.DryCooling;
import com.synerset.hvacengine.process.drycooling.DryCoolingStrategy;
import com.synerset.unitility.unitsystem.dimensionless.BypassFactor;
import com.synerset.unitility.unitsystem.flow.MassFlow;
import com.synerset.unitility.unitsystem.humidity.HumidityRatio;
import com.synerset.unitility.unitsystem.humidity.RelativeHumidity;
import com.synerset.unitility.unitsystem.thermodynamic.Power;
import com.synerset.unitility.unitsystem.thermodynamic.SpecificHeat;
import com.synerset.unitility.unitsystem.thermodynamic.Temperature;

public class CoolingEquations {

    /**
     * Real cooling coil process result as a double array, for provided cooling power.
     * Results structure: {@link AirCoolingResult}
     * REFERENCE SOURCE: [1] [Q, W] (-) [37]
     *
     * @param inletAirFlow     initial {@link FlowOfHumidAir}
     * @param inletCoolantData coolant data {@link CoolantData}
     * @param inputPower       cooling {@link Power}
     */
    public static AirCoolingResult coolingFromPower(FlowOfHumidAir inletAirFlow, CoolantData inletCoolantData, Power inputPower) {

        Validators.requireNotNull(inletAirFlow);
        Validators.requireNotNull(inletCoolantData);
        Validators.requireNotNull(inputPower);
        if (inputPower.isPositive()) {
            throw new HvacEngineArgumentException("Cooling power must be negative value. Q_in = " + inputPower);
        }

        // Mox cooling power quick estimate to reach 0 degrees Qcool.max= G * (i_0 - i_in)
        double estimatedMaxPowerKw = inletAirFlow.getSpecificEnthalpy().toKiloJoulePerKiloGram()
                .minusFromValue(0)
                .multiply(inletAirFlow.getMassFlow().toKilogramsPerSecond());
        Power estimatedPowerLimit = Power.ofKiloWatts(estimatedMaxPowerKw);
        if (inputPower.isLowerThan(estimatedPowerLimit)) {
            throw new HvacEngineArgumentException("To large cooling power for provided flow. "
                                                  + "Q_in = " + inputPower + " Q_limit = " + estimatedPowerLimit);
        }

        if (inputPower.isEqualZero() || inletAirFlow.getMassFlow().isEqualZero()) {
            LiquidWater liquidWater = LiquidWater.of(inletAirFlow.getTemperature());
            FlowOfLiquidWater flowOfLiquidWater = FlowOfLiquidWater.of(liquidWater, MassFlow.ofKilogramsPerSecond(0.0));
            new AirCoolingResult(inletAirFlow,
                    inputPower,
                    flowOfLiquidWater, coilBypassFactor(inletCoolantData.getAverageTemperature(), inletAirFlow.getTemperature(), inletAirFlow.getTemperature()));
        }

        // For the provided inputHeat, maximum possible cooling will occur for completely dry air, where no energy will be used for condensate discharge
        DryCooling dryCooling = DryCooling.of(DryCoolingStrategy.of(inletAirFlow, inputPower));
        double tmin = inletAirFlow.getTemperature().getInCelsius();
        double tmax = dryCooling.getOutLetTemperature().getInCelsius();
        BrentSolver solver = new BrentSolver("[CoolingFromPower]");
        solver.setCounterpartPoints(tmin, tmax);
        AirCoolingResult[] coolingResults = new AirCoolingResult[1];
        solver.calcForFunction(outTemp -> {
            AirCoolingResult airCoolingResult = coolingFromTargetTemperature(inletAirFlow, inletCoolantData, Temperature.ofCelsius(outTemp));
            coolingResults[0] = airCoolingResult;
            Power calculatedQ = airCoolingResult.heatOfProcess();
            return calculatedQ.getInWatts() - inputPower.getInWatts();
        });

        return coolingResults[0];
    }

    /**
     * Real cooling coil process. Returns real cooling coil process result as double array, to achieve expected outlet
     * temperature. This method represents real cooling coil, where additional energy is used to discharge more condensate
     * compared to ideal coil.
     * Results structure: {@link AirCoolingResult}
     * REFERENCE SOURCE: [1] [t2,oC] (-) [37]
     *
     * @param inletAirFlow      initial {@link FlowOfHumidAir}
     * @param inletCoolantData  average cooling coil wall {@link CoolantData}
     * @param targetTemperature target outlet {@link Temperature}
     */
    public static AirCoolingResult coolingFromTargetTemperature(FlowOfHumidAir inletAirFlow, CoolantData inletCoolantData, Temperature targetTemperature) {
        Validators.requireNotNull(inletAirFlow);
        Validators.requireNotNull(inletCoolantData);
        Validators.requireNotNull(targetTemperature);
        Validators.requireAboveLowerBound(targetTemperature, Temperature.ofCelsius(0));
        if (targetTemperature.isGreaterThan(inletAirFlow.getTemperature())) {
            throw new HvacEngineArgumentException("Expected outlet temperature must be lower than inlet for cooling process. "
                                                  + "DBT_in = " + inletAirFlow.getRelativeHumidity() + " DBT_target = " + inletAirFlow.getTemperature());
        }

        // Determining Bypass Factor and direct near-wall contact airflow and bypassing airflow
        HumidAir inletHumidAir = inletAirFlow.getFluid();
        double tIn = inletHumidAir.getTemperature().getInCelsius();
        double tOut = targetTemperature.getInCelsius();

        double mCond = 0.0;
        LiquidWater liquidWater = LiquidWater.of(inletAirFlow.getTemperature());
        FlowOfLiquidWater condensateFlow = FlowOfLiquidWater.of(liquidWater, MassFlow.ofKilogramsPerSecond(mCond));
        Temperature averageWallTemp = inletCoolantData.getAverageTemperature();

        if (tOut == tIn || inletAirFlow.getMassFlow().isEqualZero()) {
            return new AirCoolingResult(inletAirFlow, Power.ofWatts(0.0), condensateFlow,
                    coilBypassFactor(averageWallTemp, inletHumidAir.getTemperature(), targetTemperature));
        }

        double mdaIn = inletAirFlow.getDryAirMassFlow().getInKilogramsPerSecond();
        double xIn = inletHumidAir.getHumidityRatio().getInKilogramPerKilogram();
        double pIn = inletHumidAir.getPressure().getInPascals();
        double tmWall = averageWallTemp.getInCelsius();
        double tCond = tmWall;
        BypassFactor bf = coilBypassFactor(averageWallTemp, inletHumidAir.getTemperature(), targetTemperature);
        double mDaDirectContact = (1.0 - bf.getValue()) * mdaIn;
        double mDaBypassing = mdaIn - mDaDirectContact;

        // Determining direct near-wall air properties
        double tdpIn = inletHumidAir.getDewPointTemperature().getInCelsius();
        double psTm = HumidAirEquations.saturationPressure(tmWall);
        double xTm = tmWall >= tdpIn ? xIn : HumidAirEquations.maxHumidityRatio(psTm, pIn);
        double iTm = HumidAirEquations.specificEnthalpy(tmWall, xTm, pIn);

        // Determining condensate discharge and properties
        mCond = tmWall >= tdpIn
                ? 0.0
                : condensateDischarge(
                        MassFlow.ofKilogramsPerSecond(mDaDirectContact),
                        inletHumidAir.getHumidityRatio(),
                        HumidityRatio.ofKilogramPerKilogram(xTm))
                .getInKilogramsPerSecond();

        // Determining required cooling performance
        double iCond = LiquidWaterEquations.specificEnthalpy(tCond);
        double iIn = inletHumidAir.getSpecificEnthalpy().getInKiloJoulesPerKiloGram();
        double qCond = mCond * iCond;
        double qCool = (mDaDirectContact * (iTm - iIn) + qCond);

        // Determining an outlet humidity ratio
        double xOut = (xTm * mDaDirectContact + xIn * mDaBypassing) / mdaIn;

        liquidWater = LiquidWater.of(Temperature.ofCelsius(tCond));
        condensateFlow = FlowOfLiquidWater.of(liquidWater, MassFlow.ofKilogramsPerSecond(mCond));
        HumidAir outletHumidAir = HumidAir.of(
                inletAirFlow.getPressure(),
                Temperature.ofCelsius(tOut),
                HumidityRatio.ofKilogramPerKilogram(xOut)
        );

        FlowOfHumidAir outletFlow = FlowOfHumidAir.ofDryAirMassFlow(outletHumidAir, inletAirFlow.getFlowOfDryAir().getMassFlow());

        return new AirCoolingResult(outletFlow, Power.ofKiloWatts(qCool), condensateFlow, bf);
    }

    /**
     * Returns real cooling coil process result as double array, to achieve expected outlet Relative Humidity.
     * Results in the array are organized as following:
     * result: [heat in (W), outlet air temperature (oC), outlet humidity ratio x (kgWv/kgDa), condensate temperature (oC),
     * condensate mass flow (kg/s)]
     * REFERENCE SOURCE: [1] [t2,oC] (-) [37]
     *
     * @param inletAirFlow           initial {@link FlowOfHumidAir}
     * @param inletCoolantData       average cooling coil wall {@link CoolantData}
     * @param targetRelativeHumidity expected outlet {@link RelativeHumidity}
     */
    public static AirCoolingResult coolingFromTargetRelativeHumidity(FlowOfHumidAir inletAirFlow, CoolantData inletCoolantData, RelativeHumidity targetRelativeHumidity) {
        Validators.requireNotNull(inletAirFlow);
        Validators.requireNotNull(inletCoolantData);
        Validators.requireNotNull(targetRelativeHumidity);
        Validators.requireBetweenBoundsInclusive(targetRelativeHumidity, RelativeHumidity.RH_MIN_LIMIT, RelativeHumidity.ofPercentage(98));
        if (targetRelativeHumidity.isLowerThan(inletAirFlow.getRelativeHumidity())) {
            throw new HvacEngineArgumentException("Cooling process cannot decrease relative humidity. "
                                                  + "RH_in = " + inletAirFlow.getRelativeHumidity() + " RH_target = " + targetRelativeHumidity);
        }

        double pIn = inletAirFlow.getPressure().getInPascals();

        Temperature averageWallTemp = inletCoolantData.getAverageTemperature();

        if (inletAirFlow.getRelativeHumidity().equals(targetRelativeHumidity) || inletAirFlow.getMassFlow().isEqualZero()) {
            LiquidWater liquidWater = LiquidWater.of(inletAirFlow.getTemperature());
            FlowOfLiquidWater flowOfLiquidWater = FlowOfLiquidWater.of(liquidWater, MassFlow.ofKilogramsPerSecond(0.0));
            BypassFactor bypassFactor = coilBypassFactor(averageWallTemp, inletAirFlow.getTemperature(), inletAirFlow.getTemperature());
            return new AirCoolingResult(inletAirFlow, Power.ofWatts(0.0), flowOfLiquidWater, bypassFactor);
        }

        // Iterative procedure to determine which outlet temperature will result in expected RH.
        BrentSolver solver = new BrentSolver("calcCoolingFromOutletRH SOLVER");
        double tIn = inletAirFlow.getTemperature().getInCelsius();
        double tdpIn = inletAirFlow.getTemperature().getInCelsius();
        solver.setCounterpartPoints(tIn, tdpIn);
        double rhOut = targetRelativeHumidity.getInPercent();
        AirCoolingResult[] coolingResults = new AirCoolingResult[1];

        solver.calcForFunction(testOutTx -> {
            AirCoolingResult airCoolingResult = coolingFromTargetTemperature(inletAirFlow, inletCoolantData, Temperature.ofCelsius(testOutTx));
            coolingResults[0] = airCoolingResult;
            FlowOfHumidAir outletFlow = airCoolingResult.outletAirFlow();
            double outTx = outletFlow.getTemperature().getInCelsius();
            double outX = outletFlow.getHumidityRatio().getInKilogramPerKilogram();
            double actualRH = HumidAirEquations.relativeHumidity(outTx, outX, pIn);
            return rhOut - actualRH;
        });

        return coolingResults[0];
    }

    public static BypassFactor coilBypassFactor(Temperature averageWallTemp, Temperature inletAirTemp, Temperature outletAirTemp) {
        Temperature tAvgWall = averageWallTemp.toCelsius();
        Temperature tIn = inletAirTemp.toCelsius();
        Temperature tOut = outletAirTemp.toCelsius();
        double bypassFactorVal = tOut.minus(tAvgWall)
                .div(tIn.minus(tAvgWall));

        return BypassFactor.of(bypassFactorVal);
    }

    /**
     * Returns condensate discharge based on provided dry air mass flow and humidity ratio difference
     *
     * @param dryAirMassFlow {@link FlowOfDryAir}
     * @param inletHumRatio  {@link HumidityRatio}
     * @param outletHumRatio {@link HumidityRatio}
     * @return condensate {@link MassFlow}
     */
    public static MassFlow condensateDischarge(MassFlow dryAirMassFlow, HumidityRatio inletHumRatio, HumidityRatio outletHumRatio) {
        double mdaIn = dryAirMassFlow.getInKilogramsPerSecond();
        double xIn = inletHumRatio.getInKilogramPerKilogram();
        double xOut = outletHumRatio.getInKilogramPerKilogram();
        if (mdaIn < 0 || xIn < 0 || xOut < 0)
            throw new HvacEngineArgumentException(String.format("Negative values of mda, x1 or x2 passed as method argument. %s, %s, %s", dryAirMassFlow, inletHumRatio, outletHumRatio));
        if (xIn == 0)
            return MassFlow.ofKilogramsPerSecond(0.0);
        return MassFlow.ofKilogramsPerSecond(mdaIn * (xIn - xOut));
    }

    public static MassFlow massFlowFromPower(LiquidWater inletWater, Temperature outletTemperature, Power power){
        SpecificHeat inletSpecificHeat = inletWater.getSpecificHeat();
        SpecificHeat outletSpecificHeat = LiquidWaterEquations.specificHeat(outletTemperature);
        SpecificHeat averageSpecificHeat = inletSpecificHeat.plus(outletSpecificHeat).div(2);
        Temperature temperatureDifference = outletTemperature.minus(inletWater.getTemperature());
        double massFlowValue = power.abs().div(averageSpecificHeat.multiply(temperatureDifference.getInCelsius()));
        return MassFlow.ofKilogramsPerSecond(massFlowValue);
    }
}