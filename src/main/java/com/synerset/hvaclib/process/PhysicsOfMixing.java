package com.synerset.hvaclib.process;

import com.synerset.brentsolver.BrentSolver;
import com.synerset.hvaclib.flows.FlowOfHumidGas;
import com.synerset.hvaclib.fluids.HumidGas;
import com.synerset.hvaclib.process.exceptions.ProcessArgumentException;
import com.synerset.hvaclib.process.inputdata.MixingInputDataDto;
import com.synerset.hvaclib.process.resultsdto.BasicResults;
import com.synerset.hvaclib.process.resultsdto.BasicResultsDto;
import com.synerset.hvaclib.process.resultsdto.MixingResultDto;
import com.synerset.hvaclib.fluids.PhysicsPropOfMoistAir;

public final class PhysicsOfMixing {

    private PhysicsOfMixing() {
    }

    /**
     * Returns resul of two moist air flow mixing.
     *
     * @param inFirstAir         first moist air instance
     * @param firstInDryAirFlow  flow of dry air of inFirstAir in kg/s
     * @param inSecondAir        second moist air instance
     * @param secondInDryAirFlow flow of dry air of inSecondAir
     * @return [first inlet dry air mass flow (kg/s), second inlet dry air mass flow (kg/s), outlet dry air mass flow (kg/s), outlet air temperature oC, outlet humidity ratio x (kgWv/kgDa)]
     */
    public static MixingResultDto mixTwoHumidGasFlows(HumidGas inFirstAir, double firstInDryAirFlow, HumidGas inSecondAir, double secondInDryAirFlow) {
        ProcessValidators.requireNotNull("Inlet air", inFirstAir);
        ProcessValidators.requireNotNull("Second air", inSecondAir);
        ProcessValidators.requirePositiveValue("Inlet dry air flow", firstInDryAirFlow);
        ProcessValidators.requirePositiveValue("Second dry air flow", secondInDryAirFlow);
        double outDryAirFlow = firstInDryAirFlow + secondInDryAirFlow;
        double x1 = inFirstAir.getHumRatioX();
        double x2 = inSecondAir.getHumRatioX();
        double pressure = inFirstAir.getAbsPressure();
        if (firstInDryAirFlow == 0.0) {
            return new MixingResultDto(pressure, inSecondAir.getTemp(), x2, secondInDryAirFlow, firstInDryAirFlow, secondInDryAirFlow);
        }
        if (secondInDryAirFlow == 0.0 || outDryAirFlow == 0.0) {
            return new MixingResultDto(pressure, inFirstAir.getTemp(), x1, outDryAirFlow, firstInDryAirFlow, secondInDryAirFlow);
        }
        double i1 = inFirstAir.getSpecEnthalpy();
        double i2 = inSecondAir.getSpecEnthalpy();
        double x3 = (firstInDryAirFlow * x1 + secondInDryAirFlow * x2) / outDryAirFlow;
        double i3 = (firstInDryAirFlow * i1 + secondInDryAirFlow * i2) / outDryAirFlow;
        double Pat = inFirstAir.getAbsPressure();
        double t3 = PhysicsPropOfMoistAir.calcMaTaIX(i3, x3, Pat);
        return new MixingResultDto(pressure, t3, x3, outDryAirFlow, firstInDryAirFlow, secondInDryAirFlow);
    }

    public static MixingResultDto mixTwoHumidGasFlows(FlowOfHumidGas firstFlow, FlowOfHumidGas secondFlow) {
        ProcessValidators.requireNotNull("Inlet flow", firstFlow);
        ProcessValidators.requireNotNull("Second flow", secondFlow);
        return mixTwoHumidGasFlows(firstFlow.getFluid(), firstFlow.getMassFlowDa(), secondFlow.getFluid(), secondFlow.getMassFlowDa());
    }

    /**
     * Returns result of two moist air flow mixing.
     *
     * @param mixingInputFlows input data aggregate object containing input flow and recirculation flow
     * @return [first inlet dry air mass flow (kg/s), second inlet dry air mass flow (kg/s), outlet dry air mass flow (kg/s), outlet air temperature oC, outlet humidity ratio x (kgWv/kgDa)]
     */
    public static MixingResultDto mixTwoHumidGasFlowsForTargetOutFlowAndTemp(MixingInputDataDto mixingInputFlows) {
        FlowOfHumidGas inletFlow = mixingInputFlows.getInletFlow();
        FlowOfHumidGas recirculationFlow = mixingInputFlows.getRecirculationFlow();
        ProcessValidators.requireNotNull("First flow", inletFlow);
        ProcessValidators.requireNotNull("Second flow", recirculationFlow);
        return mixTwoHumidGasFlows(inletFlow.getFluid(), inletFlow.getMassFlowDa(), recirculationFlow.getFluid(), recirculationFlow.getMassFlowDa());
    }

    /**
     * Returns result of any number specified recirculationFlows mixed together.
     *
     * @param recirculationFlows array of any number of moist air recirculationFlows,
     * @return [outlet dry air mass flow (kg/s), outlet air temperature oC, outlet humidity ratio x (kgWv/kgDa)]
     */
    public static BasicResults mixMultipleHumidGasFlows(FlowOfHumidGas inletFlow, FlowOfHumidGas... recirculationFlows) {
        ProcessValidators.requireArrayNotContainsNull("Recirculation flows", recirculationFlows);
        HumidGas inletAir = inletFlow.getFluid();
        double mda3 = inletFlow.getMassFlowDa();
        double xMda = mda3 * inletAir.getHumRatioX();
        double iMda = mda3 * inletAir.getSpecEnthalpy();
        double pressure = inletAir.getAbsPressure();
        for (FlowOfHumidGas flow : recirculationFlows) {
            mda3 += flow.getMassFlowDa();
            xMda += flow.getMassFlowDa() * flow.getFluid().getHumRatioX();
            iMda += flow.getMassFlowDa() * flow.getFluid().getSpecEnthalpy();
            pressure = Double.max(pressure, flow.getFluid().getAbsPressure());
        }
        if (mda3 == 0.0) {
            ProcessValidators.requirePositiveAndNonZeroValue("Sum of all dry air mass recirculationFlows", mda3);
        }
        double x3 = xMda / mda3;
        double i3 = iMda / mda3;
        double t3 = PhysicsPropOfMoistAir.calcMaTaIX(i3, x3, pressure);
        return new BasicResultsDto(pressure, t3, x3, mda3);
    }

    /**
     * Returns mixing process result of two flows based on provided expected outlet dry air flow and its temperature. This algorithm will attempt to adjust<>br</>
     * Both inlet flows (firs and second) will be adjusted to match the specified output including expected outTemp as a second target. Both outlet flow and outlet temperature<>br</>
     * are soft-type of criteria. If expected result cannot be achieved due to specified minimum flow limits or inlet temperatures, values classes to the expected will be returned as the result<>br</>
     * To lock a minimum flow (for an example to model minimum 10% of fresh intake air in recirculation) you have to specify values for first and second minimum fixed dry air mass flows.
     *
     * @param mixingInputFlows     input data aggregate object containing input flow and recirculation flow including optionally specified minimum flows
     * @param targetOutDryMassFlow expected outlet dry air mass flow in kg/s
     * @param targetOutTemp        expected outlet air temperature, as a target for air mixing ratio
     * @return [first dry air mass flow (kg/s), second air dry air mass flow (kg/s), mixed dry air mass flow (kg/s), outlet air temperature oC, outlet humidity ratio x (kgWv/kgDa)]
     */
    public static MixingResultDto calcMixingFromOutTxOutMda(MixingInputDataDto mixingInputFlows, double targetOutDryMassFlow, double targetOutTemp) {
        //Objects validation stage
        ProcessValidators.requireNotNull("Mixing input aggregate", mixingInputFlows);
        FlowOfHumidGas inletFlow = mixingInputFlows.getInletFlow();
        FlowOfHumidGas recirculationFlow = mixingInputFlows.getRecirculationFlow();
        double firstMinFixedDryMassFlow = mixingInputFlows.getInletMinDryMassFlow();
        double secondMinFixedDryMassFlow = mixingInputFlows.getRecirculationMinDryMassFlow();
        ProcessValidators.requireNotNull("First flow", inletFlow);
        ProcessValidators.requireNotNull("Second flow", recirculationFlow);
        ProcessValidators.requirePositiveValue("Out dry air flow", targetOutDryMassFlow);
        HumidGas air1 = inletFlow.getFluid();
        HumidGas air2 = recirculationFlow.getFluid();
        double pressure = Math.max(air1.getAbsPressure(), air2.getAbsPressure());

        // In case specified outflow is lower than sum of minimal inlet fixed values
        double minFlowSum = firstMinFixedDryMassFlow + secondMinFixedDryMassFlow;
        if (minFlowSum == 0.0 && targetOutDryMassFlow == 0.0)
            throw new ProcessArgumentException("Target flow should not be = 0.");
        if (minFlowSum > targetOutDryMassFlow)
            return mixTwoHumidGasFlows(air1, firstMinFixedDryMassFlow, air2, secondMinFixedDryMassFlow);

        // Determining possible outcome to validate provided outlet temperature with respect to provided minimal flows
        double firstMaxPossibleMda = targetOutDryMassFlow - secondMinFixedDryMassFlow;
        double secondMaxPossibleMda = targetOutDryMassFlow - firstMinFixedDryMassFlow;
        MixingResultDto maxFirstFlowMinSecondFlowMixing = mixTwoHumidGasFlows(air1, firstMaxPossibleMda, air2, secondMinFixedDryMassFlow);
        MixingResultDto maxSecondFlowMinFirstFlowMixing = mixTwoHumidGasFlows(air1, firstMinFixedDryMassFlow, air2, secondMaxPossibleMda);
        double outNearT1 = maxFirstFlowMinSecondFlowMixing.outTemperature();
        double outNearT2 = maxSecondFlowMinFirstFlowMixing.outTemperature();

        // When expected outlet temperature is greater of equals to first or second flow
        // Result is returned maximum possible flow mixing result of flow which temperature is closer to the expected targetOutTemp
        if ((outNearT1 <= outNearT2 && targetOutTemp <= outNearT1) || (outNearT1 >= outNearT2 && targetOutTemp >= outNearT1))
            return maxFirstFlowMinSecondFlowMixing;
        if ((outNearT2 <= outNearT1 && targetOutTemp <= outNearT2) || (outNearT2 >= outNearT1 && targetOutTemp >= outNearT2))
            return maxSecondFlowMinFirstFlowMixing;

        // For all other cases, first and second flow will be adjusted to determine targetOutTemp
        MixingResultDto[] result = new MixingResultDto[1];
        BrentSolver solver = new BrentSolver("Mixing OutTxMda Solver");
        solver.setCounterpartPoints(firstMinFixedDryMassFlow, targetOutDryMassFlow);
        solver.calcForFunction(iterMda1 -> {
            double mda2 = targetOutDryMassFlow - iterMda1;
            result[0] = mixTwoHumidGasFlows(air1, iterMda1, air2, mda2);
            double t3 = result[0].outTemperature();
            return targetOutTemp - t3;
        });
        return result[0];
    }

}