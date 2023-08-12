package com.synerset.hvaclib.process.equations;

import com.synerset.brentsolver.BrentSolver;
import com.synerset.hvaclib.flows.FlowOfHumidAir;
import com.synerset.hvaclib.fluids.HumidAir;
import com.synerset.hvaclib.fluids.euqations.HumidAirEquations;
import com.synerset.hvaclib.process.dataobjects.AirMixingResultDto;
import com.synerset.hvaclib.process.exceptions.ProcessArgumentException;
import com.synerset.unitility.unitsystem.flows.MassFlow;
import com.synerset.unitility.unitsystem.humidity.HumidityRatio;
import com.synerset.unitility.unitsystem.thermodynamic.Pressure;
import com.synerset.unitility.unitsystem.thermodynamic.Temperature;

public final class AirMixingEquations {

    private AirMixingEquations() {
    }

    /**
     * Returns resul of two moist air flow mixing.
     *
     * @param inletFlow         inlet {@link FlowOfHumidAir}
     * @param recirculationFlow recirculation {@link FlowOfHumidAir}
     * @return {@link AirMixingResultDto}
     */
    public static FlowOfHumidAir mixTwoHumidAirFlows(FlowOfHumidAir inletFlow, FlowOfHumidAir recirculationFlow) {
        double mda_in = inletFlow.dryAirMassFlow().getValueOfKilogramsPerSecond();
        double mda_rec = recirculationFlow.dryAirMassFlow().getValueOfKilogramsPerSecond();
        double mda_out = mda_in + mda_rec;
        if (mda_in == 0.0) {
            return recirculationFlow;
        }
        if (mda_rec == 0.0 || mda_out == 0.0) {
            return inletFlow;
        }

        HumidAir inletHumidAir = inletFlow.fluid();
        HumidAir recircHumidAir = recirculationFlow.fluid();
        double x_in = inletHumidAir.humidityRatio().getValueOfKilogramPerKilogram();
        double x_rec = recircHumidAir.humidityRatio().getValueOfKilogramPerKilogram();
        double p_in = inletHumidAir.pressure().getValueOfPascals();
        double i_in = inletHumidAir.specificEnthalpy().getValueOfKiloJoulePerKilogram();
        double i_rec = recircHumidAir.specificEnthalpy().getValueOfKiloJoulePerKilogram();
        double x_out = (mda_in * x_in + mda_rec * x_rec) / mda_out;
        double i_out = (mda_in * i_in + mda_rec * i_rec) / mda_out;
        double t_out = HumidAirEquations.dryBulbTemperatureIX(i_out, x_out, p_in);

        HumidAir outletHumidAir = HumidAir.of(Pressure.ofPascal(p_in),
                Temperature.ofCelsius(t_out),
                HumidityRatio.ofKilogramPerKilogram(x_out));

        return FlowOfHumidAir.ofDryAirMassFlow(outletHumidAir, MassFlow.ofKilogramsPerSecond(mda_out));

    }

    /**
     * Returns result of any number recirculationFlows mixed together with mainstream.
     *
     * @param inletFlow          inlet {@link FlowOfHumidAir}
     * @param recirculationFlows multiple recirculation {@link FlowOfHumidAir}
     * @return {@link AirMixingResultDto}
     */
    public static FlowOfHumidAir mixMultipleHumidAirFlows(FlowOfHumidAir inletFlow, FlowOfHumidAir... recirculationFlows) {
        HumidAir inletAir = inletFlow.fluid();
        // Initializing values before loop
        double mda_out = inletFlow.dryAirMassFlow().getValueOfKilogramsPerSecond();
        double xMda = mda_out * inletAir.humidityRatio().getValueOfKilogramPerKilogram();
        double iMda = mda_out * inletAir.specificEnthalpy().getValueOfKiloJoulePerKilogram();
        double p_out = inletAir.pressure().getValueOfPascals();

        for (FlowOfHumidAir flow : recirculationFlows) {
            mda_out += flow.dryAirMassFlow().getValueOfKilogramsPerSecond();
            xMda += flow.dryAirMassFlow().getValueOfKilogramsPerSecond() * flow.fluid().humidityRatio().getValueOfKilogramPerKilogram();
            iMda += flow.dryAirMassFlow().getValueOfKilogramsPerSecond() * flow.fluid().specificEnthalpy().getValueOfKiloJoulePerKilogram();
            p_out = Double.max(p_out, flow.pressure().getValueOfPascals());
        }

        if (mda_out == 0.0) {
            throw new ProcessArgumentException(String.format("Sum of all dry air mass recirculationFlows. %s", mda_out));
        }

        double x_out = xMda / mda_out;
        double i_out = iMda / mda_out;
        double t_out = HumidAirEquations.dryBulbTemperatureIX(i_out, x_out, p_out);

        HumidAir outletHumidAir = HumidAir.of(Pressure.ofPascal(p_out),
                Temperature.ofCelsius(t_out),
                HumidityRatio.ofKilogramPerKilogram(x_out));

        return FlowOfHumidAir.ofDryAirMassFlow(outletHumidAir, MassFlow.ofKilogramsPerSecond(mda_out));

    }

    /**
     * Returns mixing process result of two flows based on provided expected outlet dry air flow and its temperature. This algorithm will attempt to adjust
     * both inlet flows (firs and second) will be adjusted to match the specified output including expected outTemp as a second target. Both outlet flow and outlet temperature
     * are soft-type of criteria. If expected result cannot be achieved due to specified minimum flow limits or inlet temperatures, values classes to the expected will be returned as the result
     * To lock a minimum flow (for an example to model minimum 10% of fresh intake air in recirculation) you have to specify values for first and second minimum fixed dry air mass flows.
     *
     * @param inletFlow                          inlet {@link FlowOfHumidAir}
     * @param recirculationFlow                  recirculation {@link FlowOfHumidAir}
     * @param minimalInletDryAirMassFlow         minimal locked inlet dry air {@link MassFlow}
     * @param minimalRecirculationDryAirMassFlow minimal locked recirculation dry air {@link MassFlow}
     * @param targetOutDryMassFlow               target outlet dry air {@link MassFlow}
     * @param targetOutTemp                      target outlet air {@link Temperature}
     * @return {@link AirMixingResultDto}
     */
    public static AirMixingResultDto mixTwoHumidAirFlowsForTargetOutTemp(FlowOfHumidAir inletFlow,
                                                                         FlowOfHumidAir recirculationFlow,
                                                                         MassFlow minimalInletDryAirMassFlow,
                                                                         MassFlow minimalRecirculationDryAirMassFlow,
                                                                         MassFlow targetOutDryMassFlow,
                                                                         Temperature targetOutTemp) {

        HumidAir inletHumidAir = inletFlow.fluid();
        HumidAir recircHumidAir = recirculationFlow.fluid();

        // In case specified outflow is lower than sum of minimal inlet fixed values
        double mdaMin_in = minimalInletDryAirMassFlow.getValueOfKilogramsPerSecond();
        double mdaMin_rec = minimalRecirculationDryAirMassFlow.getValueOfKilogramsPerSecond();
        double minFlowSum = mdaMin_in + mdaMin_rec;
        double mda_out = targetOutDryMassFlow.getValueOfKilogramsPerSecond();

        if (minFlowSum == 0.0 && mda_out == 0.0)
            throw new ProcessArgumentException("Target outlet flow cannot be zero.");
        if (minFlowSum > mda_out) {
            FlowOfHumidAir outletFlow = mixTwoHumidAirFlows(inletFlow, recirculationFlow);
            return new AirMixingResultDto(inletFlow, recirculationFlow, outletFlow);
        }

        // Determining possible outcome to validate provided outlet temperature with respect to provided minimal flows
        double mdaMaxPossible_in = mda_out - mdaMin_rec;
        double mdaMaxPossible_rec = mda_out - mdaMin_in;

        FlowOfHumidAir maxPossibleInletFlow = FlowOfHumidAir.ofDryAirMassFlow(inletHumidAir, MassFlow.ofKilogramsPerSecond(mdaMaxPossible_in));
        FlowOfHumidAir minPossibleRecirculationFlow = FlowOfHumidAir.ofDryAirMassFlow(recircHumidAir, minimalRecirculationDryAirMassFlow);
        FlowOfHumidAir maxFirstFlowMinSecondFlowMixing = mixTwoHumidAirFlows(maxPossibleInletFlow, minPossibleRecirculationFlow);

        FlowOfHumidAir minPossibleInletFlow = FlowOfHumidAir.ofDryAirMassFlow(inletHumidAir, minimalInletDryAirMassFlow);
        FlowOfHumidAir maxPossibleRecirculationFlow = FlowOfHumidAir.ofDryAirMassFlow(recircHumidAir, MassFlow.ofKilogramsPerSecond(mdaMaxPossible_rec));
        FlowOfHumidAir maxSecondFlowMinFirstFlowMixing = mixTwoHumidAirFlows(minPossibleInletFlow, maxPossibleRecirculationFlow);

        double outNearT1 = maxFirstFlowMinSecondFlowMixing.temperature().getValueOfCelsius();
        double outNearT2 = maxSecondFlowMinFirstFlowMixing.temperature().getValueOfCelsius();
        double t_out = targetOutTemp.getValueOfCelsius();

        // When expected outlet temperature is greater of equals to first or second flow
        // Result is returned maximum possible flow mixing result of flow which temperature is closer to the expected targetOutTemp
        if ((outNearT1 <= outNearT2 && t_out <= outNearT1) || (outNearT1 >= outNearT2 && t_out >= outNearT1))
            return new AirMixingResultDto(inletFlow, recirculationFlow, maxFirstFlowMinSecondFlowMixing);
        if ((outNearT2 <= outNearT1 && t_out <= outNearT2) || (outNearT2 >= outNearT1 && t_out >= outNearT2))
            return new AirMixingResultDto(inletFlow, recirculationFlow, maxSecondFlowMinFirstFlowMixing);

        // For all other cases, first and second flow will be adjusted to determine targetOutTemp
        AirMixingResultDto[] result = new AirMixingResultDto[1];
        BrentSolver solver = new BrentSolver("Mixing OutTxMda Solver");
        solver.setCounterpartPoints(mdaMin_in, mda_out);
        solver.calcForFunction(mda_iter -> {
            double m_rec_iter = mda_out - mda_iter;
            FlowOfHumidAir iterInletAir = FlowOfHumidAir.of(inletHumidAir, MassFlow.ofKilogramsPerSecond(mda_iter));
            FlowOfHumidAir iterRecAir = FlowOfHumidAir.of(recircHumidAir, MassFlow.ofKilogramsPerSecond(m_rec_iter));
            result[0] = new AirMixingResultDto(iterInletAir, iterRecAir, mixTwoHumidAirFlows(iterInletAir, iterRecAir));
            double t_iter_out = result[0].outletFlow().temperature().getValueOfCelsius();
            return t_out - t_iter_out;
        });

        return result[0];
    }

}