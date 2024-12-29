package com.synerset.hvacengine.process.mixing;

import com.synerset.hvacengine.common.validation.CommonValidators;
import com.synerset.hvacengine.process.mixing.dataobject.MixingResult;
import com.synerset.hvacengine.property.fluids.humidair.FlowOfHumidAir;
import com.synerset.hvacengine.property.fluids.humidair.HumidAir;
import com.synerset.hvacengine.property.fluids.humidair.HumidAirEquations;
import com.synerset.unitility.unitsystem.common.Ratio;
import com.synerset.unitility.unitsystem.flow.MassFlow;
import com.synerset.unitility.unitsystem.humidity.HumidityRatio;
import com.synerset.unitility.unitsystem.thermodynamic.Pressure;
import com.synerset.unitility.unitsystem.thermodynamic.Temperature;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public class MixingEquations {

    private MixingEquations() {
        throw new IllegalStateException("Utility class");
    }

    public static MixingResult mixingOfTwoAirFlows(FlowOfHumidAir inletAir, FlowOfHumidAir recirculationAirFlow) {
        CommonValidators.requireNotNull(inletAir);
        CommonValidators.requireNotNull(recirculationAirFlow);
        MassFlow totalMassFlow = inletAir.getMassFlow().plus(recirculationAirFlow.getMassFlow());
        CommonValidators.requireBelowUpperBoundInclusive(totalMassFlow, FlowOfHumidAir.MASS_FLOW_MAX_LIMIT);

        double mdaIn = inletAir.getDryAirMassFlow().getInKilogramsPerSecond();
        double mdaRec = recirculationAirFlow.getDryAirMassFlow().getInKilogramsPerSecond();
        double mdaOut = mdaIn + mdaRec;

        if (mdaRec == 0.0 || mdaOut == 0.0) {
            return MixingResult.builder()
                    .processMode(MixingMode.SIMPLE_MIXING)
                    .inletAirFlow(inletAir)
                    .outletAirFlow(inletAir)
                    .dryAirMassFreshAirRatio(Ratio.ofPercentage(100))
                    .humidAirVolFreshAirRatio(Ratio.ofPercentage(100))
                    .recirculationFlows(List.of(recirculationAirFlow))
                    .build();
        }

        double xIn = inletAir.getHumidityRatio().getInKilogramPerKilogram();
        double xRec = recirculationAirFlow.getHumidityRatio().getInKilogramPerKilogram();
        double pIn = inletAir.getPressure().getInPascals();
        double iIn = inletAir.getSpecificEnthalpy().getInKiloJoulesPerKiloGram();
        double iRec = recirculationAirFlow.getSpecificEnthalpy().getInKiloJoulesPerKiloGram();

        double xOut = (mdaIn * xIn + mdaRec * xRec) / mdaOut;
        double iOut = (mdaIn * iIn + mdaRec * iRec) / mdaOut;
        double tOut = HumidAirEquations.dryBulbTemperatureIX(iOut, xOut, pIn);

        HumidAir outletHumidAir = HumidAir.of(Pressure.ofPascal(pIn),
                Temperature.ofCelsius(tOut),
                HumidityRatio.ofKilogramPerKilogram(xOut));

        FlowOfHumidAir outletFlow = FlowOfHumidAir.ofDryAirMassFlow(outletHumidAir, MassFlow.ofKilogramsPerSecond(mdaOut));

        Ratio dryAirMassFreshAirRatio = Ratio.ofPercentage((mdaIn / mdaOut) * 100);
        Ratio humidAirVolFreshAirRatio = Ratio.from(inletAir.getVolFlow(), outletFlow.getVolFlow()).toPercent();

        return MixingResult.builder()
                .processMode(MixingMode.SIMPLE_MIXING)
                .inletAirFlow(inletAir)
                .outletAirFlow(outletFlow)
                .dryAirMassFreshAirRatio(dryAirMassFreshAirRatio)
                .humidAirVolFreshAirRatio(humidAirVolFreshAirRatio)
                .recirculationFlows(List.of(recirculationAirFlow))
                .build();
    }

    public static MixingResult mixingOfMultipleFlows(FlowOfHumidAir inletAir, Collection<FlowOfHumidAir> recirculationAirFlows) {
        CommonValidators.requireNotNull(inletAir);

        if (recirculationAirFlows == null || recirculationAirFlows.isEmpty() || inletAir.getMassFlow().isCloseToZero() || sumOfAllFlows(recirculationAirFlows).isCloseToZero()) {
            return MixingResult.builder()
                    .processMode(MixingMode.MULTIPLE_MIXING)
                    .inletAirFlow(inletAir)
                    .outletAirFlow(inletAir)
                    .dryAirMassFreshAirRatio(Ratio.ofPercentage(100))
                    .humidAirVolFreshAirRatio(Ratio.ofPercentage(100))
                    .recirculationFlows(recirculationAirFlows)
                    .build();
        }

        MassFlow totalMassFlow = sumOfAllFlows(recirculationAirFlows).plus(inletAir.getMassFlow());
        CommonValidators.requireBelowUpperBoundInclusive(totalMassFlow, FlowOfHumidAir.MASS_FLOW_MAX_LIMIT);

        double mdaOut = inletAir.getDryAirMassFlow().getInKilogramsPerSecond();
        double xMda = mdaOut * inletAir.getHumidityRatio().getInKilogramPerKilogram();
        double iMda = mdaOut * inletAir.getSpecificEnthalpy().getInKiloJoulesPerKiloGram();
        double pOut = inletAir.getPressure().getInPascals();

        for (FlowOfHumidAir flow : recirculationAirFlows) {
            mdaOut += flow.getDryAirMassFlow().getInKilogramsPerSecond();
            xMda += flow.getDryAirMassFlow().getInKilogramsPerSecond() * flow.getFluid().getHumidityRatio().getInKilogramPerKilogram();
            iMda += flow.getDryAirMassFlow().getInKilogramsPerSecond() * flow.getFluid().getSpecificEnthalpy().getInKiloJoulesPerKiloGram();
            pOut = Double.max(pOut, flow.getPressure().getInPascals());
        }

        double xOut = xMda / mdaOut;
        double iOut = iMda / mdaOut;
        double tOut = HumidAirEquations.dryBulbTemperatureIX(iOut, xOut, pOut);

        HumidAir outletHumidAir = HumidAir.of(Pressure.ofPascal(pOut),
                Temperature.ofCelsius(tOut),
                HumidityRatio.ofKilogramPerKilogram(xOut));

        FlowOfHumidAir outletFlow = FlowOfHumidAir.ofDryAirMassFlow(outletHumidAir, MassFlow.ofKilogramsPerSecond(mdaOut));

        Ratio dryAirMassFlowFreshAirRatio = Ratio.ofPercentage((inletAir.getDryAirMassFlow().getInKilogramsPerSecond() / mdaOut) * 100);
        Ratio humidAirVolFreshAirRatio = Ratio.from(inletAir.getVolFlow(), outletFlow.getVolFlow()).toPercent();

        return MixingResult.builder()
                .processMode(MixingMode.MULTIPLE_MIXING)
                .inletAirFlow(inletAir)
                .outletAirFlow(outletFlow)
                .dryAirMassFreshAirRatio(dryAirMassFlowFreshAirRatio)
                .humidAirVolFreshAirRatio(humidAirVolFreshAirRatio)
                .recirculationFlows(recirculationAirFlows)
                .build();
    }

    public static MixingResult mixingOfMultipleFlows(FlowOfHumidAir inletAir, FlowOfHumidAir... recirculationAirFlows) {
        return mixingOfMultipleFlows(inletAir, Arrays.stream(recirculationAirFlows).toList());
    }

    private static MassFlow sumOfAllFlows(Collection<FlowOfHumidAir> airFlows) {
        MassFlow resultingFlow = MassFlow.ofKilogramsPerSecond(0);
        for (FlowOfHumidAir flow : airFlows) {
            resultingFlow = resultingFlow.plus(flow.getMassFlow());
        }
        return resultingFlow;
    }

}