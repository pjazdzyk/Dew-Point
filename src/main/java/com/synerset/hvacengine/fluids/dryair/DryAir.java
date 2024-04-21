package com.synerset.hvacengine.fluids.dryair;

import com.synerset.hvacengine.common.CommonValidators;
import com.synerset.hvacengine.fluids.Fluid;
import com.synerset.hvacengine.fluids.SharedEquations;
import com.synerset.unitility.unitsystem.dimensionless.PrandtlNumber;
import com.synerset.unitility.unitsystem.thermodynamic.*;

import java.util.Objects;

import static com.synerset.hvacengine.utils.Defaults.STANDARD_ATMOSPHERE;

/**
 * A class representing dry air as a fluid, providing access to various properties of dry air.
 */
public class DryAir implements Fluid {

    public static final Pressure PRESSURE_MIN_LIMIT = Pressure.ofPascal(0);
    public static final Temperature TEMPERATURE_MIN_LIMIT = Temperature.ofCelsius(-150);
    public static final Temperature TEMPERATURE_MAX_LIMIT = Temperature.ofCelsius(1000);
    private final Temperature temperature;
    private final Pressure pressure;
    private final Density density;
    private final SpecificHeat specificHeat;
    private final SpecificEnthalpy specificEnthalpy;
    private final DynamicViscosity dynamicViscosity;
    private final KinematicViscosity kinematicViscosity;
    private final ThermalConductivity thermalConductivity;
    private final PrandtlNumber prandtlNumber;

    /**
     * Construct a `DryAir` instance with the specified pressure and temperature.
     *
     * @param pressure    The pressure of dry air in pascals (Pa).
     * @param temperature The temperature of dry air in degrees Celsius (°C).
     */
    public DryAir(Pressure pressure, Temperature temperature) {
        CommonValidators.requireNotNull(pressure);
        CommonValidators.requireNotNull(temperature);
        CommonValidators.requireAboveLowerBound(pressure, PRESSURE_MIN_LIMIT);
        CommonValidators.requireBetweenBoundsInclusive(temperature, TEMPERATURE_MIN_LIMIT, TEMPERATURE_MAX_LIMIT);
        this.temperature = temperature;
        this.pressure = pressure;
        this.density = DryAirEquations.density(temperature, pressure);
        this.specificHeat = DryAirEquations.specificHeat(temperature);
        this.specificEnthalpy = DryAirEquations.specificEnthalpy(temperature);
        this.dynamicViscosity = DryAirEquations.dynamicViscosity(temperature);
        this.kinematicViscosity = DryAirEquations.kinematicViscosity(temperature, pressure);
        this.thermalConductivity = DryAirEquations.thermalConductivity(temperature);
        this.prandtlNumber = SharedEquations.prandtlNumber(dynamicViscosity, thermalConductivity, specificHeat);
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

    public DynamicViscosity getDynamicViscosity() {
        return dynamicViscosity;
    }

    public KinematicViscosity getKinematicViscosity() {
        return kinematicViscosity;
    }

    public ThermalConductivity getThermalConductivity() {
        return thermalConductivity;
    }

    public PrandtlNumber getPrandtlNumber() {
        return prandtlNumber;
    }

    @Override
    public String toConsoleOutput() {
        String separator = " | ";
        String end = "\n\t";
        int digits = 3;
        return "DryAir:" + end +
                pressure.toEngineeringFormat("P_abs", digits) + separator +
                temperature.toEngineeringFormat("DBT", digits) + end +
                specificEnthalpy.toEngineeringFormat("i_da", digits) + separator +
                density.toEngineeringFormat("ρ_da", digits) + separator +
                specificHeat.toEngineeringFormat("cp_da", digits) + end +
                kinematicViscosity.toEngineeringFormat("ν_da", digits) + separator +
                dynamicViscosity.toEngineeringFormat("μ_da", digits) + separator +
                thermalConductivity.toEngineeringFormat("k_da", digits) + separator +
                prandtlNumber.toEngineeringFormat("Pr_da", digits) + "\n";
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (object == null || getClass() != object.getClass()) return false;
        DryAir dryAir = (DryAir) object;
        return Objects.equals(temperature, dryAir.temperature) && Objects.equals(pressure, dryAir.pressure);
    }

    @Override
    public int hashCode() {
        return Objects.hash(temperature, pressure);
    }

    @Override
    public String toString() {
        return "DryAir{" +
                "temperature=" + temperature +
                ", pressure=" + pressure +
                ", density=" + density +
                ", specificHeat=" + specificHeat +
                ", specificEnthalpy=" + specificEnthalpy +
                ", dynamicViscosity=" + dynamicViscosity +
                ", kinematicViscosity=" + kinematicViscosity +
                ", thermalConductivity=" + thermalConductivity +
                ", prandtlNumber=" + prandtlNumber +
                '}';
    }

    // Static factory methods

    /**
     * Create a `DryAir` instance with the specified pressure and temperature.
     *
     * @param pressure    The pressure of dry air in pascals (Pa).
     * @param temperature The temperature of dry air in degrees Celsius (°C).
     * @return A new `DryAir` instance.
     */
    public static DryAir of(Pressure pressure, Temperature temperature) {
        return new DryAir(pressure, temperature);
    }

    /**
     * Create a `DryAir` instance with the specified temperature at standard atmospheric pressure.
     *
     * @param temperature The temperature of dry air in degrees Celsius (°C).
     * @return A new `DryAir` instance at standard atmospheric pressure.
     */
    public static DryAir of(Temperature temperature) {
        return new DryAir(STANDARD_ATMOSPHERE, temperature);
    }

}
