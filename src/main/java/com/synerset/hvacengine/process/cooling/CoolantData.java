package com.synerset.hvacengine.process.cooling;

import com.synerset.hvacengine.common.MathUtils;
import com.synerset.hvacengine.common.validation.CommonValidators;
import com.synerset.unitility.unitsystem.thermodynamic.Temperature;

/**
 * Represents data related to a coolant used in a cooling process.
 */
public class CoolantData {

    public static final Temperature COOLANT_MIN_TEMPERATURE = Temperature.ofCelsius(0);
    public static final Temperature COOLANT_MAX_TEMPERATURE = Temperature.ofCelsius(90);
    private final Temperature supplyTemperature;
    private final Temperature returnTemperature;
    private final Temperature averageTemperature;

    public CoolantData(Temperature supplyTemperature, Temperature returnTemperature) {
        CommonValidators.requireNotNull(supplyTemperature);
        CommonValidators.requireNotNull(returnTemperature);
        CommonValidators.requireBetweenBounds(supplyTemperature, COOLANT_MIN_TEMPERATURE, COOLANT_MAX_TEMPERATURE);
        CommonValidators.requireBetweenBounds(returnTemperature, COOLANT_MIN_TEMPERATURE, COOLANT_MAX_TEMPERATURE);
        CoolingValidators.requireValidCoolantInputData(supplyTemperature, returnTemperature);
        this.supplyTemperature = supplyTemperature;
        this.returnTemperature = returnTemperature;
        this.averageTemperature = averageWallTemp(supplyTemperature, returnTemperature);
    }

    public Temperature getSupplyTemperature() {
        return supplyTemperature;
    }

    public Temperature getReturnTemperature() {
        return returnTemperature;
    }

    public Temperature getAverageTemperature() {
        return averageTemperature;
    }

    public static Temperature averageWallTemp(Temperature supplyTemp, Temperature returnTemp) {
        double averageTempVal = MathUtils.arithmeticAverage(supplyTemp.getInCelsius(), returnTemp.getInCelsius());
        return Temperature.ofCelsius(averageTempVal);
    }

    public static CoolantData of(Temperature supplyTemperature, Temperature returnTemperature) {
        return new CoolantData(supplyTemperature, returnTemperature);
    }

    public static CoolantData ofValues(double supplyTemperatureInC, double returnTemperatureInC) {
        return new CoolantData(Temperature.ofCelsius(supplyTemperatureInC), Temperature.ofCelsius(returnTemperatureInC));
    }

}
