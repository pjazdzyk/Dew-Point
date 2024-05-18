package com.synerset.hvacengine.process.heating;

import com.synerset.hvacengine.common.exception.HvacEngineArgumentException;
import com.synerset.hvacengine.property.fluids.humidair.FlowOfHumidAir;
import com.synerset.hvacengine.property.fluids.humidair.HumidAirEquations;
import com.synerset.unitility.unitsystem.humidity.RelativeHumidity;
import com.synerset.unitility.unitsystem.thermodynamic.Power;
import com.synerset.unitility.unitsystem.thermodynamic.SpecificEnthalpy;
import com.synerset.unitility.unitsystem.thermodynamic.Temperature;

/**
 * This class groups required heating condition checks and validating methods which will throw an {@link HvacEngineArgumentException}.
 * They have been extracted to utility class, so they could be easily reused by any other application to perform validation
 * on higher layers.
 */
public class HeatingValidators {

    private HeatingValidators() {
        throw new IllegalStateException("utility class");
    }

    // Boolean checks
    public static boolean isValidTargetTemperatureForHeating(Temperature inletTemperature, Temperature targetTemperature) {
        return targetTemperature.isEqualOrGreaterThan(inletTemperature);
    }

    public static boolean isValidTargetRelativeHumidityForHeating(RelativeHumidity inletRelativeHumidity, RelativeHumidity targetRelativeHumidity) {
        return targetRelativeHumidity.isEqualOrLowerThan(inletRelativeHumidity);
    }

    public static boolean isValidPowerForHeating(Power heatingPower) {
        return heatingPower.isPositive();
    }

    public static boolean isValidInputPowerForPhysicalHeating(FlowOfHumidAir inletAirFlow, Power heatingPower) {
        return heatingPower.isLowerThan(estimateMaxHeatingPower(inletAirFlow));
    }

    // Exception validators

    public static void requireValidTargetTemperatureForHeating(Temperature inletTemperature, Temperature temperature) {
        if (!isValidTargetTemperatureForHeating(inletTemperature, temperature)) {
            throw new HvacEngineArgumentException("Temperature cannot be decreased in heating process. If this was intended - use cooling process.." +
                                                  " t_in = " + inletTemperature + " t_target = " + temperature.toUnitFrom(inletTemperature));
        }
    }

    public static void requireValidTargetRelativeHumidityForHeating(RelativeHumidity inletRelativeHumidity, RelativeHumidity relativeHumidity) {
        if (!isValidTargetRelativeHumidityForHeating(inletRelativeHumidity, relativeHumidity)) {
            throw new HvacEngineArgumentException("Relative humidity cannot be increased in heating process. If this was intended - use cooling process." +
                                                  " RH_in = " + inletRelativeHumidity + " RH_target = " + relativeHumidity.toUnitFrom(inletRelativeHumidity));
        }
    }

    public static void requireValidInputPowerForHeating(Power inputPower) {
        if (!isValidPowerForHeating(inputPower)) {
            throw new HvacEngineArgumentException("Power must be provided as positive value for heating. If this was intended, use cooling process instead." +
                                                  " Q_heat = " + inputPower);
        }
    }

    public static void requirePhysicalInputPowerForHeating(FlowOfHumidAir inletAirFlow, Power heatingPower) {
        requireValidInputPowerForHeating(heatingPower);
        if (!isValidInputPowerForPhysicalHeating(inletAirFlow, heatingPower)) {
            throw new HvacEngineArgumentException("Unphysical input heating power for provided inlet flow. Value to high to produce physical result. "
                                                  + "Q_in = " + heatingPower
                                                  + " Q_limit = " + estimateMaxHeatingPower(inletAirFlow).toUnitFrom(heatingPower));
        }
    }

    // Helpers
    private static Power estimateMaxHeatingPower(FlowOfHumidAir inletAirFlow) {
        // Mox cooling power quick estimate to reach 0 degrees Qcool.max= G * (i_0 - i_in)
        double stableReductionFactor = 0.98;
        Temperature tMax = HumidAirEquations.dryBulbTemperatureMax(inletAirFlow.getPressure()).multiply(stableReductionFactor);
        SpecificEnthalpy iMax = HumidAirEquations.specificEnthalpy(tMax, inletAirFlow.getHumidityRatio(),
                inletAirFlow.getPressure());
        double qMax = iMax.minus(inletAirFlow.getSpecificEnthalpy())
                .multiply(inletAirFlow.getMassFlow().toKilogramsPerSecond());
        return Power.ofKiloWatts(qMax);
    }

}