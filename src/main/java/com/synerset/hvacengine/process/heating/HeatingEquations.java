package com.synerset.hvacengine.process.heating;

import com.synerset.hvacengine.common.validation.CommonValidators;
import com.synerset.hvacengine.process.ProcessMode;
import com.synerset.hvacengine.process.heating.dataobject.HeatingResult;
import com.synerset.hvacengine.property.fluids.humidair.FlowOfHumidAir;
import com.synerset.hvacengine.property.fluids.humidair.HumidAir;
import com.synerset.hvacengine.property.fluids.humidair.HumidAirEquations;
import com.synerset.unitility.unitsystem.flow.MassFlow;
import com.synerset.unitility.unitsystem.humidity.RelativeHumidity;
import com.synerset.unitility.unitsystem.thermodynamic.Power;
import com.synerset.unitility.unitsystem.thermodynamic.Temperature;

import static com.synerset.hvacengine.process.heating.HeatingValidators.requirePhysicalInputPowerForHeating;
import static com.synerset.hvacengine.process.heating.HeatingValidators.requireValidInputPowerForHeating;
import static com.synerset.hvacengine.process.heating.HeatingValidators.requireValidTargetRelativeHumidityForHeating;
import static com.synerset.hvacengine.process.heating.HeatingValidators.requireValidTargetTemperatureForHeating;

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
        CommonValidators.requireNotNull(inletAirFlow);
        CommonValidators.requireNotNull(inputPower);
        requireValidInputPowerForHeating(inputPower);
        requirePhysicalInputPowerForHeating(inletAirFlow, inputPower);

        if (inputPower.isCloseToZero() || inletAirFlow.getMassFlow().isCloseToZero()) {
            return HeatingResult.builder()
                    .processMode(ProcessMode.FROM_POWER)
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
                .processMode(ProcessMode.FROM_POWER)
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
        CommonValidators.requireNotNull(inletAirFlow);
        CommonValidators.requireNotNull(targetTemperature);
        CommonValidators.requireBelowUpperBoundInclusive(targetTemperature, HumidAir.TEMPERATURE_MAX_LIMIT);
        requireValidTargetTemperatureForHeating(inletAirFlow.getTemperature(), targetTemperature);

        if (inletAirFlow.getTemperature().equals(targetTemperature) || inletAirFlow.getMassFlow().isCloseToZero()) {
            return HeatingResult.builder()
                    .processMode(ProcessMode.FROM_TEMPERATURE)
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
                .processMode(ProcessMode.FROM_TEMPERATURE)
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
        CommonValidators.requireNotNull(inletAirFlow);
        CommonValidators.requireNotNull(targetRelativeHumidity);
        CommonValidators.requireBetweenBoundsInclusive(targetRelativeHumidity, RelativeHumidity.RH_MIN_LIMIT, RelativeHumidity.ofPercentage(98));
        requireValidTargetRelativeHumidityForHeating(inletAirFlow.getRelativeHumidity(), targetRelativeHumidity);

        if (inletAirFlow.getRelativeHumidity().equals(targetRelativeHumidity) || inletAirFlow.getMassFlow().isCloseToZero()) {
            return HeatingResult.builder()
                    .processMode(ProcessMode.FROM_HUMIDITY)
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
                .processMode(ProcessMode.FROM_HUMIDITY)
                .inletAirFlow(inletAirFlow)
                .outletAirFlow(outletFlow)
                .heatOfProcess(requiredHeat)
                .build();
    }

}