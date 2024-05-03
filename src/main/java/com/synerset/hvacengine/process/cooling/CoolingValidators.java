package com.synerset.hvacengine.process.cooling;

import com.synerset.hvacengine.common.exception.HvacEngineArgumentException;
import com.synerset.hvacengine.property.fluids.humidair.FlowOfHumidAir;
import com.synerset.unitility.unitsystem.humidity.RelativeHumidity;
import com.synerset.unitility.unitsystem.thermodynamic.Power;
import com.synerset.unitility.unitsystem.thermodynamic.Temperature;

/**
 * This class groups required cooling condition checks and validating methods which will throw an {@link HvacEngineArgumentException}.
 * They have been extracted to utility class, so they could be easily reused by any other application to perform validation
 * on higher layers.
 */
public class CoolingValidators {

    private CoolingValidators() {
        throw new IllegalStateException("Utility class");
    }

    // Boolean checks
    public static boolean isValidTargetTemperatureForDryCooling(FlowOfHumidAir inletAirFlow, Temperature targetTemperature) {
        return targetTemperature.isEqualOrGreaterThan(inletAirFlow.getFluid().getDewPointTemperature());
    }

    public static boolean isValidTargetTemperatureForCooling(Temperature inletTemperature, Temperature targetTemperature) {
        return targetTemperature.isEqualOrLowerThan(inletTemperature);
    }

    public static boolean isValidTargetRelativeHumidityForCooling(RelativeHumidity relativeHumidity, RelativeHumidity targetRelativeHumidity) {
        return targetRelativeHumidity.isEqualOrGreaterThan(relativeHumidity);
    }

    public static boolean isValidPowerForCooling(Power coolingPower) {
        return coolingPower.isNegative();
    }

    public static boolean isValidTemperatureForCoolantData(Temperature supplyTemperature, Temperature returnTemperature) {
        return supplyTemperature.isEqualOrLowerThan(returnTemperature);
    }

    public static boolean isValidInputPowerToGetPhysicalCoolingResult(FlowOfHumidAir inletAirFlow, Power coolingPower) {
        return coolingPower.isGreaterThan(estimateMaxCoolingPower(inletAirFlow));
    }

    // Exception validators
    public static void requireValidTargetTemperatureForDryCooling(FlowOfHumidAir inletAirFlow, Temperature temperature) {
        if (!isValidTargetTemperatureForCooling(inletAirFlow.getTemperature(), temperature)) {
            throw new HvacEngineArgumentException("Temperature cannot be increased in cooling process. If this was intended - use heating process." +
                                                  " t_in = " + inletAirFlow.getTemperature() +
                                                  " t_target = " + temperature.toUnitFrom(inletAirFlow.getTemperature()));
        }
        if (!isValidTargetTemperatureForDryCooling(inletAirFlow, temperature)) {
            Temperature dewPointTemp = inletAirFlow.getFluid().getDewPointTemperature();
            throw new HvacEngineArgumentException("Target temperature cannot be lower than inlet dew point temperature" +
                                                  " for valid dry cooling process, use real cooling process instead." +
                                                  " t_in = " + inletAirFlow.getTemperature() +
                                                  " t_dp = " + dewPointTemp.toUnitFrom(inletAirFlow.getTemperature()));
        }
    }

    public static void requireValidTargetTemperatureForCooling(Temperature inletTemperature, Temperature temperature) {
        if (!isValidTargetTemperatureForCooling(inletTemperature, temperature)) {
            throw new HvacEngineArgumentException("Temperature cannot be increased in cooling process. If this was intended - use heating process." +
                                                  " t_in = " + inletTemperature + " t_target = " + temperature.toUnitFrom(inletTemperature));
        }
    }

    public static void requireValidTargetRelativeHumidityForCooling(RelativeHumidity inletRelativeHumidity, RelativeHumidity relativeHumidity) {
        if (!isValidTargetRelativeHumidityForCooling(inletRelativeHumidity, relativeHumidity)) {
            throw new HvacEngineArgumentException("Relative humidity cannot be decreased in cooling process. If this was intended use heating process." +
                                                  " RH_in = " + inletRelativeHumidity + " RH_target = " + relativeHumidity.toUnitFrom(inletRelativeHumidity));
        }
    }

    public static void requireValidCoolantInputData(Temperature supplyTemperature, Temperature returnTemperature) {
        if (!isValidTemperatureForCoolantData(supplyTemperature, returnTemperature)) {
            throw new HvacEngineArgumentException("Invalid temperatures for coolant data. Supply temperature cannot be greater than return temperature. " +
                                                  "t_su = " + supplyTemperature + " t_ret = " + returnTemperature);
        }
    }

    public static void requireValidInputPowerForCooling(Power inputPower) {
        if (!isValidPowerForCooling(inputPower)) {
            throw new HvacEngineArgumentException("Power must be provided as negative value for cooling. If this was intended, use heating process instead." +
                                                  " Q_heat = " + inputPower);
        }
    }

    public static void requirePhysicalInputPowerForCooling(FlowOfHumidAir inletAirFlow, Power coolingPower) {
        requireValidInputPowerForCooling(coolingPower);
        if (!isValidInputPowerToGetPhysicalCoolingResult(inletAirFlow, coolingPower)) {
            throw new HvacEngineArgumentException("Unphysical input cooling power for provided flow. Cooling power is to large. " + "Q_in = " + coolingPower
                                                  + " Q_limit = " + estimateMaxCoolingPower(inletAirFlow).toUnitFrom(coolingPower));
        }
    }

    // Helpers
    private static Power estimateMaxCoolingPower(FlowOfHumidAir inletAirFlow) {
        // Mox cooling power quick estimate to reach 0 degrees Qcool.max= G * (i_0 - i_in)
        double estimatedMaxPowerKw = inletAirFlow.getSpecificEnthalpy().toKiloJoulePerKiloGram()
                .minusFromValue(0)
                .multiply(inletAirFlow.getMassFlow().toKilogramsPerSecond());
        return Power.ofKiloWatts(estimatedMaxPowerKw);
    }

}