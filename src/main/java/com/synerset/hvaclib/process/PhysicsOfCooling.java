package com.synerset.hvaclib.process;

import com.synerset.brentsolver.BrentSolver;
import com.synerset.hvaclib.common.MathUtils;
import com.synerset.hvaclib.flows.FlowOfHumidGas;
import com.synerset.hvaclib.fluids.HumidGas;
import com.synerset.hvaclib.fluids.PhysicsPropOfMoistAir;
import com.synerset.hvaclib.process.exceptions.ProcessArgumentException;
import com.synerset.hvaclib.process.resultsdto.CoolingResultDto;
import com.synerset.hvaclib.process.resultsdto.HeatingResultDto;
import com.synerset.hvaclib.fluids.PhysicsPropOfWater;

/**
 * PSYCHROMETRICS PROCESS EQUATIONS LIBRARY <br>
 * Set of static methods outputs process result as an array with process heat, core output air parameters (temperature, humidity ratio) and condensate
 * properties. Methods do not create a separate instance of FlowOfMoistAir for performance reasons - each ot these methods may be used in iterative solvers, and we
 * do not want to lose memory or performance for unnecessary object creation. <br>
 * Variable literals have the following meaning: (1) or in - stands for input/inlet air, (2) or out - stands for output/outlet air. <br>
 * <p>
 * PREFERENCE SOURCES: <br>
 * [1] ASHRAE FUNDAMENTALS 2002, CHAPTER 6 <br>
 * [2] Lipska B. "Projektowanie Wentylacji i Klimatyzacji. Podstawy uzdatniania powietrza" Wydawnictwo Politechniki Śląskiej (Gliwice  2014) <br>
 * <p>
 * REFERENCES LEGEND KEY: <br>
 * [reference no] [value symbology in standard, unit] (equation number) [page] <br>
 *
 * @author Piotr Jażdżyk, MScEng
 */

public final class PhysicsOfCooling {

    private PhysicsOfCooling() {
    }

    // DRY COOLING (USE WITH CAUTION!)

    /**
     * Calculates outlet temperature for dry cooling case based on input cooling power (inputHeat). Input heat must be passed negative value.<br>
     * IMPORTANT: Inappropriate use of dry cooling will produce significant overestimation of outlet temperature or underestimation of required cooling power!
     * Real cooling methodology is recommended to use as relatively accurate representation of real world cooling process.<br>
     * <p>
     * REFERENCE SOURCE: [1][2] [t2,oC] (42)(2.2) [6.12][37]<br>
     * EQUATION LIMITS: {0.0 W, TBC W}<br>
     *
     * @param inletFlow  initial flow of moist air before the process [FLowOfMoistAir],
     * @param inputHeatQ input heat in W,
     * @return [heat in (W), outlet air temperature (oC), outlet humidity ratio x (kgWv/kgDa), condensate temperature (oC), condensate mass flow (kg/s)]
     */
    public static CoolingResultDto calcDryCoolingFromInputHeat(FlowOfHumidGas inletFlow, double inputHeatQ) {
        ProcessValidators.requireNegativeValue("Dry cooling inputHeatQ", inputHeatQ);
        // Dry cooling follows the same methodology as heating. Formulas used for heating can be reused:
        HeatingResultDto dryCoolingResult = PhysicsOfHeating.calcHeatingForInputHeat(inletFlow, inputHeatQ);
        // Dry cooling does not produce humidity change therefore no condensate is discharged.
        double condensateMassFlow = 0.0;
        return new CoolingResultDto(
                inletFlow.getFluid().getAbsPressure(),
                dryCoolingResult.outTemperature(),
                inletFlow.getFluid().getHumRatioX(),
                inletFlow.getMassFlowDa(),
                dryCoolingResult.heatOfProcess(),
                dryCoolingResult.outTemperature(),
                condensateMassFlow);
    }

    /**
     * Calculates outlet cooling power (heat of process) for dry cooling case based on target outlet temperature. Target temperature must be lower than inlet flow temp for valid cooling case.<br>
     * IMPORTANT: Inappropriate use of dry cooling will produce significant overestimation of outlet temperature or underestimation of required cooling power!
     * Real cooling methodology is recommended to use as relatively accurate representation of real world cooling process.<br>
     * <p>
     * REFERENCE SOURCE: [1][2] [t2,oC] (42)(2.2) [6.12][37]<br>
     * EQUATION LIMITS: {0.0 W, TBC W}<br>
     *
     * @param inletFlow     initial flow of moist air before the process [FLowOfMoistAir],
     * @param targetOutTemp expected outlet temperature in oC.
     * @return [heat in (W), outlet air temperature (oC), outlet humidity ratio x (kgWv/kgDa), condensate temperature (oC), condensate mass flow (kg/s)]
     */
    public static CoolingResultDto calcDryCoolingFromOutputTx(FlowOfHumidGas inletFlow, double targetOutTemp) {
        // Target temperature must be lower than inlet temperature for valid cooling case.
        ProcessValidators.requireNotNull("Inlet flow", inletFlow);
        HumidGas inletAir = inletFlow.getFluid();
        double pressure = inletAir.getAbsPressure();
        ProcessValidators.requireFirstValueAsGreaterThanSecond("Dry cooling temps validation. ", inletAir.getTemp(), targetOutTemp);
        // If target temperature is below dew point temperature it is certain that this is no longer dry cooling
        double tdp = inletAir.getDewPointTemp();
        if (targetOutTemp < tdp) {
            throw new ProcessArgumentException("Expected temperature must be higher than dew point. Not applicable for dry cooling process.");
        }
        // Dry cooling follows the same methodology as heating. Formulas used for heating can be reused:
        HeatingResultDto dryCoolingResult = PhysicsOfHeating.calcHeatingForTargetTemp(inletFlow, targetOutTemp);
        // Dry cooling does not produce humidity change therefore no condensate is discharged.
        double condensateMassFlow = 0.0;
        return new CoolingResultDto(
                pressure,
                dryCoolingResult.outTemperature(),
                inletAir.getHumRatioX(),
                inletFlow.getMassFlowDa(),
                dryCoolingResult.heatOfProcess(),
                dryCoolingResult.outTemperature(),
                condensateMassFlow);
    }

    // REAL COOLING COIL

    /**
     * Returns real cooling coil process result as double array, to achieve expected outlet temperature. Results in the array are organized as following:<>br</>
     * result: [heat in (W), outlet air temperature (oC), outlet humidity ratio x (kgWv/kgDa), condensate temperature (oC), condensate mass flow (kg/s)]<>br</>
     * This method represents real cooling coil, where additional energy is used to discharge more condensate compared to ideal coil.<>br</>
     * As the result more cooling power is required to achieve desired output temperature, also the output humidity content is smaller and RH < 100%.
     * REFERENCE SOURCE: [1] [t2,oC] (-) [37]<br>
     *
     * @param inletFlow initial flow of moist air before the process [FLowOfMoistAir],
     * @param tm_Wall   average coil wall temperature in oC,
     * @param outTx     expected outlet temperature in oC,
     * @return [heat in (W), outlet air temperature (oC), outlet humidity ratio x (kgWv/kgDa), condensate temperature (oC), condensate mass flow (kg/s)]
     */

    public static CoolingResultDto calcCoolingFromOutletTx(FlowOfHumidGas inletFlow, double tm_Wall, double outTx) {
        // Determining Bypass Factor and direct near-wall contact airflow and bypassing airflow
        ProcessValidators.requireNotNull("Inlet flow", inletFlow);
        HumidGas inletAir = inletFlow.getFluid();
        double inTx = inletAir.getTemp(); // oC
        double inX = inletAir.getHumRatioX(); // kg.wv/kg.da
        double pressure = inletAir.getAbsPressure(); // Pa
        double heatOfProcess = 0.0; // W
        double t_Cond = tm_Wall; // oC
        double m_Cond = 0.0; // kg/s
        if (outTx > inTx) {
            throw new ProcessArgumentException("Expected outlet temperature must be lover than inlet for cooling process. Use heating process method instead");
        }
        double mDa_Inlet = inletFlow.getMassFlowDa();
        if (outTx == inTx) {
            return new CoolingResultDto(pressure, outTx, inX, mDa_Inlet, heatOfProcess, tm_Wall, m_Cond);
        }
        double BF = calcCoolingCoilBypassFactor(tm_Wall, inTx, outTx);
        double mDa_DirectContact = (1.0 - BF) * mDa_Inlet;
        double mDa_Bypassing = mDa_Inlet - mDa_DirectContact;

        // Determining direct near-wall air properties
        double Pat = inletAir.getAbsPressure();
        double tdp_Inlet = inletAir.getDewPointTemp();
        double Ps_Tm = PhysicsPropOfMoistAir.calcMaPs(tm_Wall);
        double x_Tm = tm_Wall >= tdp_Inlet ? inletAir.getHumRatioX() : PhysicsPropOfMoistAir.calcMaXMax(Ps_Tm, Pat);
        double i_Tm = PhysicsPropOfMoistAir.calcMaIx(tm_Wall, x_Tm, Pat);

        // Determining condensate discharge and properties
        double x1 = inletAir.getHumRatioX();
        m_Cond = tm_Wall >= tdp_Inlet ? 0.0 : calcCondensateDischarge(mDa_DirectContact, x1, x_Tm);

        // Determining required cooling performance
        double i_Cond = PhysicsPropOfWater.calcIx(t_Cond);
        double i_Inlet = inletAir.getSpecEnthalpy();
        heatOfProcess = (mDa_DirectContact * (i_Tm - i_Inlet) + m_Cond * i_Cond) * 1000d;

        // Determining outlet humidity ratio
        double outX = (x_Tm * mDa_DirectContact + x1 * mDa_Bypassing) / mDa_Inlet;

        return new CoolingResultDto(pressure, outTx, outX, mDa_Inlet, heatOfProcess, t_Cond, m_Cond);
    }

    /**
     * Returns real cooling coil process result as double array, to achieve expected outlet Relative Humidity. Results in the array are organized as following:<>br</>
     * result: [heat in (W), outlet air temperature (oC), outlet humidity ratio x (kgWv/kgDa), condensate temperature (oC), condensate mass flow (kg/s)]<>br</>
     * REFERENCE SOURCE: [1] [t2,oC] (-) [37]<br>
     *
     * @param inletFlow initial flow of moist air before the process [FLowOfMoistAir],
     * @param tm_Wall   average coil wall temperature in oC,
     * @param outRH     expected outlet relative humidity in %,
     * @return [heat in (W), outlet air temperature (oC), outlet humidity ratio x (kgWv/kgDa), condensate temperature (oC), condensate mass flow (kg/s)]
     */
    public static CoolingResultDto calcCoolingFromOutletRH(FlowOfHumidGas inletFlow, double tm_Wall, double outRH) {
        ProcessValidators.requireNotNull("Inlet flow", inletFlow);
        HumidGas inletAirProp = inletFlow.getFluid();
        double pressure = inletAirProp.getAbsPressure();
        if (outRH > 100 || outRH < 0.0) {
            throw new ProcessArgumentException("Relative Humidity outside acceptable values.");
        }
        if (outRH < inletAirProp.getRelativeHumidityRH()) {
            throw new ProcessArgumentException("Process not possible. Cooling cannot decrease relative humidity");
        }
        if (outRH == inletAirProp.getRelativeHumidityRH()) {
            double heatOfProcess = 0.0; // W
            double condensateFlow = 0.0; // kg/s
            double mdaInlet = inletFlow.getMassFlowDa();
            return new CoolingResultDto(pressure, inletAirProp.getTemp(), inletAirProp.getHumRatioX(), mdaInlet, heatOfProcess, tm_Wall, condensateFlow);
        }
        if (outRH > 99.0) {
            throw new ProcessArgumentException("Non-physical process. The area of the exchanger would have to be infinite.");
        }

        //Iterative loop to determine which outlet temperature will result in expected RH.
        CoolingResultDto[] result = new CoolingResultDto[1]; // Array is needed here to work-around issue of updating result variable from the inside of inner class.
        BrentSolver solver = new BrentSolver("calcCoolingFromOutletRH SOLVER");
        solver.setCounterpartPoints(inletAirProp.getTemp(), inletAirProp.getDewPointTemp());
        solver.calcForFunction(testOutTx -> {
            result[0] = calcCoolingFromOutletTx(inletFlow, tm_Wall, testOutTx);
            double outTx = result[0].outTemperature();
            double outX = result[0].outHumidityRatio();
            double actualRH = PhysicsPropOfMoistAir.calcMaRH(outTx, outX, pressure);
            return outRH - actualRH;
        });
        solver.resetSolverRunFlags();
        return result[0];
    }

    /**
     * Returns real cooling coil process result as double array, for provided cooling power. Results in the array are organized as following:<>br</>
     * result: [heat in (W), outlet air temperature (oC), outlet humidity ratio x (kgWv/kgDa), condensate temperature (oC), condensate mass flow (kg/s)]<>br</>
     * REFERENCE SOURCE: [1] [Q, W] (-) [37]<br>
     *
     * @param inletFlow initial flow of moist air before the process [FLowOfMoistAir],
     * @param tm_Wall   average coil wall temperature in oC,
     * @param inputHeat cooling power in W (must be negative),
     * @return [heat in (W), outlet air temperature (oC), outlet humidity ratio x (kgWv/kgDa), condensate temperature (oC), condensate mass flow (kg/s)]
     */
    public static CoolingResultDto calcCoolingFromInputHeat(FlowOfHumidGas inletFlow, double tm_Wall, double inputHeat) {
        ProcessValidators.requireNotNull("Inlet flow", inletFlow);
        HumidGas inletAirProp = inletFlow.getFluid();
        double t1 = inletAirProp.getTemp();
        double x1 = inletAirProp.getHumRatioX();
        double pressure = inletAirProp.getAbsPressure();
        if (inputHeat == 0.0) {
            double condensateFlow = 0.0;
            new CoolingResultDto(pressure, t1, x1, inletFlow.getMassFlowDa(), inputHeat, tm_Wall, condensateFlow);
        }
        CoolingResultDto[] result = new CoolingResultDto[1];
        double tMin = inletAirProp.getTemp();

        //For the provided inputHeat, maximum possible cooling will occur for completely dry air, where no energy will be used for condensate discharge
        double tMax = calcDryCoolingFromInputHeat(inletFlow, inputHeat).outTemperature();
        BrentSolver solver = new BrentSolver("calcCoolingFromInputHeat SOLVER");
        solver.setCounterpartPoints(tMin, tMax);
        solver.calcForFunction(outTemp -> {
            result[0] = calcCoolingFromOutletTx(inletFlow, tm_Wall, outTemp);
            double calculatedQ = result[0].heatOfProcess();
            return calculatedQ - inputHeat;
        });
        solver.resetSolverRunFlags();
        return result[0];
    }

    //SECONDARY PROPERTIES - COOLING

    /**
     * Returns linear average coil wall temperature based on coolant supply and return temperatures,
     *
     * @param supplyTemp coolant supply temperature in oC,
     * @param returnTemp cooolant return temperature in oC,
     * @return linear average coil wall temperature in oC,
     */
    public static double calcAverageWallTemp(double supplyTemp, double returnTemp) {
        return MathUtils.arithmeticAverage(supplyTemp, returnTemp);
    }

    /**
     * Returns cooling coil Bypass-Factor.
     *
     * @param tm_Wall linear average coil wall temperature in oC,
     * @param inTx    inlet air temperature in oC,
     * @param outTx   outlet air temperature in oC,
     * @return cooling coil Bypass-Factor
     */
    public static double calcCoolingCoilBypassFactor(double tm_Wall, double inTx, double outTx) {
        return (outTx - tm_Wall) / (inTx - tm_Wall);
    }

    /**
     * Returns condensate discharge based on provided dry air mass flow and humidity ratio difference
     *
     * @param massFlowDa dry air mass flow in kg/s
     * @param x1         inlet humidity ratio, kg.wv/kg.da
     * @param x2         outlet humidity ratio, kg.wv/kg.da
     * @return condensate flow in kg/s
     */
    public static double calcCondensateDischarge(double massFlowDa, double x1, double x2) {
        if (massFlowDa < 0 || x1 < 0 || x2 < 0)
            throw new ProcessArgumentException("Negative values of mda, x1 or x2 passed as method argument. mDa= " + massFlowDa + " x1= " + x1 + " x2= " + x2);
        if (x1 == 0)
            return 0.0;
        return massFlowDa * (x1 - x2);
    }

}