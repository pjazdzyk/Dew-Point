package com.synerset.hvacengine.fluids.watervapour;

import com.synerset.hvacengine.common.CommonValidators;
import com.synerset.hvacengine.fluids.Fluid;
import com.synerset.hvacengine.fluids.SharedEquations;
import com.synerset.hvacengine.fluids.humidair.HumidAirEquations;
import com.synerset.unitility.unitsystem.dimensionless.PrandtlNumber;
import com.synerset.unitility.unitsystem.humidity.RelativeHumidity;
import com.synerset.unitility.unitsystem.thermodynamic.*;

import java.util.Objects;

import static com.synerset.hvacengine.fluids.FluidValidators.requireValidSaturationPressure;
import static com.synerset.hvacengine.utils.Defaults.STANDARD_ATMOSPHERE;

public class WaterVapour implements Fluid {
    public static final Pressure PRESSURE_MIN_LIMIT = Pressure.ofPascal(0);
    public static final Temperature TEMPERATURE_MIN_LIMIT = Temperature.ofCelsius(-150);
    public static final Temperature TEMPERATURE_MAX_LIMIT = Temperature.ofCelsius(1000);
    public static final Temperature TEMPERATURE_MAX_LIMIT_WITH_RH = Temperature.ofCelsius(230);
    private final Temperature temperature;
    private final Pressure pressure;
    private final Density density;
    private final SpecificHeat specificHeat;
    private final SpecificEnthalpy specificEnthalpy;
    private final DynamicViscosity dynamicViscosity;
    private final KinematicViscosity kinematicViscosity;
    private final ThermalConductivity thermalConductivity;
    private final PrandtlNumber prandtlNumber;

    public WaterVapour(Pressure pressure,
                       Temperature temperature,
                       RelativeHumidity relativeHumidity) {

        CommonValidators.requireNotNull(pressure);
        CommonValidators.requireNotNull(temperature);
        CommonValidators.requireAboveLowerBound(pressure, PRESSURE_MIN_LIMIT);
        CommonValidators.requireBetweenBoundsInclusive(temperature, TEMPERATURE_MIN_LIMIT, TEMPERATURE_MAX_LIMIT);

        this.pressure = pressure;
        this.temperature = temperature;

        if (Objects.isNull(relativeHumidity)) {
            this.density = WaterVapourEquations.density(temperature, pressure);
        } else {
            CommonValidators.requireBelowUpperBoundInclusive(temperature, TEMPERATURE_MAX_LIMIT_WITH_RH);
            CommonValidators.requireAboveLowerBound(relativeHumidity, RelativeHumidity.RH_MIN_LIMIT);
            CommonValidators.requireBelowUpperBoundInclusive(relativeHumidity, RelativeHumidity.RH_MAX_LIMIT);
            Pressure saturationPressure = HumidAirEquations.saturationPressure(temperature);
            requireValidSaturationPressure(saturationPressure, pressure, temperature);
            this.density = WaterVapourEquations.density(temperature, relativeHumidity, pressure);
        }

        this.specificHeat = WaterVapourEquations.specificHeat(temperature);
        this.specificEnthalpy = WaterVapourEquations.specificEnthalpy(temperature);
        this.dynamicViscosity = WaterVapourEquations.dynamicViscosity(temperature);
        this.kinematicViscosity = WaterVapourEquations.kinematicViscosity(temperature, density);
        this.thermalConductivity = WaterVapourEquations.thermalConductivity(temperature);
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
        return "WaterVapour: " + end +
                pressure.toEngineeringFormat("P_abs", digits) + separator +
                temperature.toEngineeringFormat("t_wv", digits) + end +
                specificEnthalpy.toEngineeringFormat("i_wv", digits) + separator +
                density.toEngineeringFormat("ρ_wv", digits) + separator +
                specificHeat.toEngineeringFormat("cp_wv", digits) + end +
                kinematicViscosity.toEngineeringFormat("ν_wv", digits) + separator +
                dynamicViscosity.toEngineeringFormat("μ_wv", digits) + separator +
                thermalConductivity.toEngineeringFormat("k_wv", digits) + separator +
                prandtlNumber.toEngineeringFormat("Pr_wv", digits) + end;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        WaterVapour that = (WaterVapour) o;
        return Objects.equals(temperature, that.temperature) && Objects.equals(pressure, that.pressure);
    }

    @Override
    public int hashCode() {
        return Objects.hash(temperature, pressure);
    }

    @Override
    public String toString() {
        return "WaterVapour{" +
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
    public static WaterVapour of(Pressure pressure, Temperature temperature, RelativeHumidity relativeHumidity) {
        return new WaterVapour(pressure, temperature, relativeHumidity);
    }

    public static WaterVapour of(Pressure pressure, Temperature temperature) {
        return new WaterVapour(pressure, temperature, null);
    }

    public static WaterVapour of(Temperature temperature, RelativeHumidity relativeHumidity) {
        return new WaterVapour(STANDARD_ATMOSPHERE, temperature, relativeHumidity);
    }

    public static WaterVapour of(Temperature temperature) {
        return new WaterVapour(STANDARD_ATMOSPHERE, temperature, null);
    }

}
