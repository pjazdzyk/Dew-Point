package com.synerset.hvacengine.property.solids.ice;

import com.synerset.hvacengine.common.ConsolePrintable;
import com.synerset.hvacengine.common.validation.CommonValidators;
import com.synerset.unitility.unitsystem.thermodynamic.*;

import java.util.Objects;

/**
 * The `Ice` class represents the properties of ice at a specific pressure and temperature.
 */
public class Ice implements ConsolePrintable {
    public static final Pressure PRESSURE_MIN_LIMIT = Pressure.ofPascal(0);
    public static final Temperature TEMPERATURE_MIN_LIMIT = Temperature.ofCelsius(-150);
    private final Temperature temperature;
    private final Pressure pressure;
    private final Density density;
    private final SpecificHeat specificHeat;
    private final SpecificEnthalpy specificEnthalpy;

    /**
     * Constructs an `Ice` object with the given pressure and temperature.
     *
     * @param pressure    The pressure of the ice (must be above the minimum limit).
     * @param temperature The temperature of the ice (must be above or equal to the minimum limit).
     */
    public Ice(Pressure pressure, Temperature temperature) {
        CommonValidators.requireNotNull(pressure);
        CommonValidators.requireNotNull(temperature);
        CommonValidators.requireAboveLowerBound(pressure, PRESSURE_MIN_LIMIT);
        CommonValidators.requireAboveLowerBoundInclusive(temperature, TEMPERATURE_MIN_LIMIT);
        this.temperature = temperature;
        this.pressure = pressure;
        this.density = IceEquations.density(temperature);
        this.specificHeat = IceEquations.specificHeat(temperature);
        this.specificEnthalpy = IceEquations.specificEnthalpy(temperature);
    }

    public Temperature getTemperature() {
        return temperature;
    }

    public Pressure getPressure() {
        return pressure;
    }

    public Density getDensity() {
        return density;
    }

    public SpecificHeat getSpecificHeat() {
        return specificHeat;
    }

    public SpecificEnthalpy getSpecificEnthalpy() {
        return specificEnthalpy;
    }

    /**
     * Returns a formatted string representation for console output of the `Ice` object.
     *
     * @return A formatted string representing the properties of ice.
     */
    @Override
    public String toConsoleOutput() {
        String separator = " | ";
        String end = "\n\t";
        int digits = 3;
        return "Ice:" + end +
                pressure.toEngineeringFormat("P_abs", digits) + separator +
                temperature.toEngineeringFormat("t_ice", digits) + end +

                specificEnthalpy.toEngineeringFormat("i_ice",digits) + separator +
                density.toEngineeringFormat("ρ_ice", digits) + separator +
                specificHeat.toEngineeringFormat("cp_ice", digits);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Ice ice = (Ice) o;
        return Objects.equals(temperature, ice.temperature) && Objects.equals(pressure, ice.pressure);
    }

    @Override
    public int hashCode() {
        return Objects.hash(temperature, pressure);
    }

    @Override
    public String toString() {
        return "Ice{" +
                "temperature=" + temperature +
                ", pressure=" + pressure +
                ", density=" + density +
                ", specificHeat=" + specificHeat +
                ", specificEnthalpy=" + specificEnthalpy +
                '}';
    }

    // Static factory methods

    /**
     * Creates an `Ice` object with the specified pressure and temperature.
     *
     * @param pressure    The pressure of the ice.
     * @param temperature The temperature of the ice.
     * @return An `Ice` object with the given pressure and temperature.
     */
    public static Ice of(Pressure pressure, Temperature temperature) {
        return new Ice(pressure, temperature);
    }

    /**
     * Creates an `Ice` object at standard atmospheric pressure with the specified temperature.
     *
     * @param temperature The temperature of the ice.
     * @return An `Ice` object at standard atmospheric pressure with the given temperature.
     */
    public static Ice of(Temperature temperature) {
        return new Ice(Pressure.STANDARD_ATMOSPHERE, temperature);
    }

}