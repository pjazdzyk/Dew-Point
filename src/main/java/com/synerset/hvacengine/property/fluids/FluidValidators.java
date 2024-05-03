package com.synerset.hvacengine.property.fluids;

import com.synerset.hvacengine.common.exception.HvacEngineArgumentException;
import com.synerset.unitility.unitsystem.thermodynamic.Pressure;
import com.synerset.unitility.unitsystem.thermodynamic.Temperature;

public class FluidValidators {

    private FluidValidators() {
        throw new IllegalStateException("Utility class");
    }

    public static boolean isValidSaturationPressureRange(Pressure saturationPressure, Pressure humidAirAbsolutePressure) {
        return saturationPressure.isEqualOrLowerThan(humidAirAbsolutePressure);
    }

    public static void requireValidSaturationPressure(Pressure saturationPressure, Pressure humidAirAbsolutePressure, Temperature temperature) {
        if (!isValidSaturationPressureRange(saturationPressure, humidAirAbsolutePressure)) {
            throw new HvacEngineArgumentException(
                    String.format("Water vapour saturation pressure exceeds humid air absolute pressure. Calculations are not possible. " +
                                  " Psat=%s, Pabs=%s, Temp=%s. Increase pressure or change input data.",
                            saturationPressure, humidAirAbsolutePressure, temperature));
        }
    }

}