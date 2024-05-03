package com.synerset.hvacengine.common;

import com.synerset.unitility.unitsystem.common.Distance;
import com.synerset.unitility.unitsystem.humidity.RelativeHumidity;
import com.synerset.unitility.unitsystem.thermodynamic.Pressure;
import com.synerset.unitility.unitsystem.thermodynamic.Temperature;

/**
 * This class contains default values for various HVAC-related parameters.
 */
public final class Defaults {

    private Defaults() {
    }

    public static final Pressure STANDARD_ATMOSPHERE = Pressure.ofPascal(101_325);
    public static final Temperature INDOOR_WINTER_TEMP = Temperature.ofCelsius(20.0);
    public static final Temperature INDOOR_SUMMER_TEMP = Temperature.ofCelsius(25.0);
    public static final RelativeHumidity INDOOR_SUMMER_RH = RelativeHumidity.ofPercentage(50.0);
    public static final RelativeHumidity INDOOR_WINTER_RH = RelativeHumidity.ofPercentage(30.0);
    public static final Distance SEA_LEVEL_REFERENCE = Distance.ofMeters(0.0);

}
