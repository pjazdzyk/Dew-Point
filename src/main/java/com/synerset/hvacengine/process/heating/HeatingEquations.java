package com.synerset.hvacengine.process.heating;

import com.synerset.hvacengine.common.Validators;
import com.synerset.hvacengine.common.exception.HvacEngineArgumentException;
import com.synerset.hvacengine.fluids.humidair.FlowOfHumidAir;
import com.synerset.hvacengine.fluids.humidair.HumidAir;
import com.synerset.hvacengine.fluids.humidair.HumidAirEquations;
import com.synerset.hvacengine.process.heating.dataobject.HeatingResult;
import com.synerset.unitility.unitsystem.flow.MassFlow;
import com.synerset.unitility.unitsystem.humidity.RelativeHumidity;
import com.synerset.unitility.unitsystem.thermodynamic.Power;
import com.synerset.unitility.unitsystem.thermodynamic.SpecificEnthalpy;
import com.synerset.unitility.unitsystem.thermodynamic.Temperature;

public class HeatingEquations {

    private HeatingEquations() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * Calculates outlet temperature for heating case based on input heat of process.
     * This method can be used only for heating, inputHeatQ must be passed as positive value
     * REFERENCE SOURCE: [1][2] [t2,oC] (42)(2.2) [6.12][37]
     *
     * @param inletAirFlow initial {@link FlowOfHumidAir}
     * @param inputPower   heating {@link Power}
     */
    public static HeatingResult heatingFromPower(FlowOfHumidAir inletAirFlow, Power inputPower) {
        Validators.requireNotNull(inletAirFlow);
        Validators.requireNotNull(inputPower);

        if (inputPower.isNegative()) {
            throw new HvacEngineArgumentException("Heating power must be positive value. Q_in = " + inputPower);
        }

        // Mox heating power estimate to reach t_max: Qheat.max= G * (imax - i_in)
        Temperature tMax = HumidAirEquations.dryBulbTemperatureMax(inletAirFlow.getPressure()).multiply(0.98);
        SpecificEnthalpy iMax = HumidAirEquations.specificEnthalpy(tMax, inletAirFlow.getHumidityRatio(),
                inletAirFlow.getPressure());
        double qMax = iMax.minus(inletAirFlow.getSpecificEnthalpy())
                .multiply(inletAirFlow.getMassFlow().toKilogramsPerSecond());
        Power estimatedPowerLimit = Power.ofKiloWatts(qMax);
        if (inputPower.isGreaterThan(estimatedPowerLimit)) {
            throw new HvacEngineArgumentException("To large heating power for provided flow. "
                                                  + "Q_in = " + inputPower + " Q_limit = " + estimatedPowerLimit);
        }

        if (inputPower.isEqualZero() || inletAirFlow.getMassFlow().isEqualZero()) {
            return HeatingResult.builder()
                    .inletAirFlow(inletAirFlow)
                    .outletAirFlow(inletAirFlow)
                    .heatOfProcess(inputPower)
                    .build();
        }

        HumidAir inletHumidAir = inletAirFlow.getFluid();
        double qHeat = inputPower.getInKiloWatts();
        double xIn = inletHumidAir.getHumidityRatio().getInKilogramPerKilogram();
        double mdaIn = inletAirFlow.getDryAirMassFlow().getInKilogramsPerSecond();
        double pIn = inletHumidAir.getPressure().getInPascals();
        double iIn = inletHumidAir.getSpecificEnthalpy().getInKiloJoulesPerKiloGram();
        double iOut = (mdaIn * iIn + qHeat) / mdaIn;
        double tOut = HumidAirEquations.dryBulbTemperatureIX(iOut, xIn, pIn);

        HumidAir outletHumidAir = HumidAir.of(inletAirFlow.getPressure(), Temperature.ofCelsius(tOut), inletAirFlow.getHumidityRatio());
        FlowOfHumidAir outletFlow = FlowOfHumidAir.ofDryAirMassFlow(outletHumidAir, MassFlow.ofKilogramsPerSecond(mdaIn));

        return HeatingResult.builder()
                .inletAirFlow(inletAirFlow)
                .outletAirFlow(outletFlow)
                .heatOfProcess(inputPower)
                .build();
    }

    /**
     * Calculates outlet heat of process for heating case based on target temperature.
     * This method can be used only for heating, inQ must be passed as positive value
     * REFERENCE SOURCE: [1][2] [t2,oC] (42)(2.2) [6.12][37]
     *
     * @param inletAirFlow      initial {@link FlowOfHumidAir}
     * @param targetTemperature target outlet {@link Temperature}
     */
    public static HeatingResult heatingFromTargetTemperature(FlowOfHumidAir inletAirFlow, Temperature targetTemperature) {
        Validators.requireNotNull(inletAirFlow);
        Validators.requireNotNull(targetTemperature);
        Validators.requireBelowUpperBoundInclusive(targetTemperature, HumidAir.TEMPERATURE_MAX_LIMIT);

        if (targetTemperature.isLowerThan(inletAirFlow.getTemperature())) {
            throw new HvacEngineArgumentException("Expected outlet temperature must be greater than inlet for cooling process." +
                                                  " DBT_in = " + inletAirFlow.getRelativeHumidity() +
                                                  " DBT_target = " + inletAirFlow.getTemperature());
        }

        if (inletAirFlow.getTemperature().equals(targetTemperature) || inletAirFlow.getMassFlow().isEqualZero()) {
            return HeatingResult.builder()
                    .inletAirFlow(inletAirFlow)
                    .outletAirFlow(inletAirFlow)
                    .heatOfProcess(Power.ofWatts(0))
                    .build();
        }

        HumidAir inletHumidAir = inletAirFlow.getFluid();
        double xIn = inletHumidAir.getHumidityRatio().getInKilogramPerKilogram();
        double mdaIn = inletAirFlow.getDryAirMassFlow().getInKilogramsPerSecond();
        double tOut = targetTemperature.getInCelsius();

        double pIn = inletHumidAir.getPressure().getInPascals();
        double iIn = inletHumidAir.getSpecificEnthalpy().getInKiloJoulesPerKiloGram();
        double i2 = HumidAirEquations.specificEnthalpy(tOut, xIn, pIn);
        double qHeat = (mdaIn * i2 - mdaIn * iIn) * 1000d;
        Power requiredHeat = Power.ofWatts(qHeat);

        HumidAir outletHumidAir = HumidAir.of(
                inletAirFlow.getPressure(),
                Temperature.ofCelsius(tOut),
                inletAirFlow.getHumidityRatio()
        );
        FlowOfHumidAir outletFlow = FlowOfHumidAir.ofDryAirMassFlow(
                outletHumidAir,
                MassFlow.ofKilogramsPerSecond(mdaIn)
        );

        return HeatingResult.builder()
                .inletAirFlow(inletAirFlow)
                .outletAirFlow(outletFlow)
                .heatOfProcess(requiredHeat)
                .build();
    }

    /**
     * Calculates outlet temperature and heat of process for heating case based on target relative humidity (RH).
     * This method can be used only for heating, outRH must be equals or smaller than initial value
     *
     * @param inletAirFlow           initial {@link FlowOfHumidAir}
     * @param targetRelativeHumidity target {@link RelativeHumidity}
     */
    public static HeatingResult heatingFromRelativeHumidity(FlowOfHumidAir inletAirFlow, RelativeHumidity targetRelativeHumidity) {
        Validators.requireNotNull(inletAirFlow);
        Validators.requireNotNull(targetRelativeHumidity);
        Validators.requireBetweenBoundsInclusive(targetRelativeHumidity, RelativeHumidity.RH_MIN_LIMIT, RelativeHumidity.ofPercentage(98));
        if (targetRelativeHumidity.isGreaterThan(inletAirFlow.getRelativeHumidity())) {
            throw new HvacEngineArgumentException("Heating process cannot increase relative humidity." +
                                                  " RH_in = " + inletAirFlow.getRelativeHumidity() +
                                                  " RH_target = " + targetRelativeHumidity);
        }

        if (inletAirFlow.getRelativeHumidity().equals(targetRelativeHumidity) || inletAirFlow.getMassFlow().isEqualZero()) {
            return HeatingResult.builder()
                    .inletAirFlow(inletAirFlow)
                    .outletAirFlow(inletAirFlow)
                    .heatOfProcess(Power.ofWatts(0))
                    .build();
        }

        HumidAir inletHumidAir = inletAirFlow.getFluid();
        double rhOut = targetRelativeHumidity.getInPercent();
        double xIn = inletHumidAir.getHumidityRatio().getInKilogramPerKilogram();
        double mdaIn = inletAirFlow.getDryAirMassFlow().getInKilogramsPerSecond();
        double pIn = inletHumidAir.getPressure().getInPascals();
        double iIn = inletHumidAir.getSpecificEnthalpy().getInKiloJoulesPerKiloGram();
        double tOut = HumidAirEquations.dryBulbTemperatureXRH(xIn, rhOut, pIn);
        double iOut = HumidAirEquations.specificEnthalpy(tOut, xIn, pIn);
        double qHeat = (mdaIn * iOut - mdaIn * iIn) * 1000d;
        Power requiredHeat = Power.ofWatts(qHeat);

        HumidAir outletHumidAir = HumidAir.of(inletAirFlow.getPressure(), Temperature.ofCelsius(tOut), inletAirFlow.getHumidityRatio());
        FlowOfHumidAir outletFlow = FlowOfHumidAir.ofDryAirMassFlow(outletHumidAir, MassFlow.ofKilogramsPerSecond(mdaIn));

        return HeatingResult.builder()
                .inletAirFlow(inletAirFlow)
                .outletAirFlow(outletFlow)
                .heatOfProcess(requiredHeat)
                .build();
    }

}