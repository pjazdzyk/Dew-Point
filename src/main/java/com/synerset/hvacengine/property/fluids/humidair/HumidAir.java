package com.synerset.hvacengine.property.fluids.humidair;

import com.synerset.hvacengine.common.validation.CommonValidators;
import com.synerset.hvacengine.property.fluids.Fluid;
import com.synerset.hvacengine.property.fluids.FluidValidators;
import com.synerset.hvacengine.property.fluids.SharedEquations;
import com.synerset.hvacengine.property.fluids.dryair.DryAir;
import com.synerset.unitility.unitsystem.dimensionless.PrandtlNumber;
import com.synerset.unitility.unitsystem.humidity.HumidityRatio;
import com.synerset.unitility.unitsystem.humidity.RelativeHumidity;
import com.synerset.unitility.unitsystem.thermodynamic.*;

import java.util.Objects;

import static com.synerset.hvacengine.common.Defaults.STANDARD_ATMOSPHERE;

/**
 * A class representing the properties of humid air, including temperature, pressure, humidity, and more.
 */
public class HumidAir implements Fluid {
    public static final Pressure PRESSURE_MIN_LIMIT = Pressure.ofPascal(50_000);
    public static final Pressure PRESSURE_MAX_LIMIT = Pressure.ofBar(50);
    public static final Temperature TEMPERATURE_MIN_LIMIT = Temperature.ofCelsius(-150);
    public static final Temperature TEMPERATURE_MAX_LIMIT = Temperature.ofCelsius(200);
    public static final HumidityRatio HUMIDITY_RATIO_MAX_LIMIT = HumidityRatio.ofKilogramPerKilogram(3);
    private final Temperature temperature;
    private final Pressure pressure;
    private final Density density;
    private final RelativeHumidity relativeHumidity;
    private final Pressure saturationPressure;
    private final HumidityRatio humidityRatio;
    private final HumidityRatio maxHumidityRatio;
    private final VapourState vapourState;
    private final Temperature wetBulbTemperature;
    private final Temperature dewPointTemperature;
    private final SpecificHeat specificHeat;
    private final SpecificEnthalpy specificEnthalpy;
    private final DynamicViscosity dynamicViscosity;
    private final KinematicViscosity kinematicViscosity;
    private final ThermalConductivity thermalConductivity;
    private final ThermalDiffusivity thermalDiffusivity;
    private final PrandtlNumber prandtlNumber;
    private final DryAir dryAirComponent;

    /**
     * Constructs a `HumidAir` instance with the specified absolute pressure, dry bulb temperature, and humidity ratio.
     *
     * @param pressure      The absolute pressure of the humid air in pascals (Pa).
     * @param temperature   The dry bulb temperature of the humid air in degrees Celsius (°C).
     * @param humidityRatio The humidity ratio of the humid air in kilograms of water vapor per kilogram of dry air (kg/kg).
     */
    public HumidAir(Pressure pressure,
                    Temperature temperature,
                    HumidityRatio humidityRatio) {

        CommonValidators.requireNotNull(pressure);
        CommonValidators.requireNotNull(temperature);
        CommonValidators.requireNotNull(humidityRatio);
        CommonValidators.requireBetweenBoundsInclusive(pressure, PRESSURE_MIN_LIMIT, PRESSURE_MAX_LIMIT);
        CommonValidators.requireBetweenBoundsInclusive(temperature, TEMPERATURE_MIN_LIMIT, TEMPERATURE_MAX_LIMIT);
        CommonValidators.requireBetweenBoundsInclusive(humidityRatio, HumidityRatio.HUM_RATIO_MIN_LIMIT, HUMIDITY_RATIO_MAX_LIMIT);
        Pressure satPressure = HumidAirEquations.saturationPressure(temperature);
        FluidValidators.requireValidSaturationPressure(satPressure, pressure, temperature);

        this.pressure = pressure;
        this.temperature = temperature;
        this.humidityRatio = humidityRatio;
        this.saturationPressure = satPressure;
        this.density = HumidAirEquations.density(temperature, humidityRatio, pressure);
        this.relativeHumidity = HumidAirEquations.relativeHumidity(temperature, humidityRatio, pressure);
        this.maxHumidityRatio = HumidAirEquations.maxHumidityRatio(saturationPressure, pressure);
        this.vapourState = determineVapourState(temperature, humidityRatio, maxHumidityRatio);
        this.wetBulbTemperature = HumidAirEquations.wetBulbTemperature(temperature, relativeHumidity, pressure);
        this.dewPointTemperature = HumidAirEquations.dewPointTemperature(temperature, relativeHumidity, pressure);
        this.specificHeat = HumidAirEquations.specificHeat(temperature, humidityRatio);
        this.specificEnthalpy = HumidAirEquations.specificEnthalpy(temperature, humidityRatio, pressure);
        this.dynamicViscosity = HumidAirEquations.dynamicViscosity(temperature, humidityRatio);
        this.kinematicViscosity = HumidAirEquations.kinematicViscosity(temperature, humidityRatio, density);
        this.thermalConductivity = HumidAirEquations.thermalConductivity(temperature, humidityRatio);
        this.thermalDiffusivity = SharedEquations.thermalDiffusivity(density, thermalConductivity, specificHeat);
        this.prandtlNumber = SharedEquations.prandtlNumber(dynamicViscosity, thermalConductivity, specificHeat);
        this.dryAirComponent = DryAir.of(pressure, temperature);
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

    public RelativeHumidity getRelativeHumidity() {
        return relativeHumidity;
    }

    public Pressure getSaturationPressure() {
        return saturationPressure;
    }

    public HumidityRatio getHumidityRatio() {
        return humidityRatio;
    }

    public HumidityRatio getMaxHumidityRatio() {
        return maxHumidityRatio;
    }

    public VapourState getVapourState() {
        return vapourState;
    }

    public Temperature getWetBulbTemperature() {
        return wetBulbTemperature;
    }

    public Temperature getDewPointTemperature() {
        return dewPointTemperature;
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

    public ThermalDiffusivity getThermalDiffusivity() {
        return thermalDiffusivity;
    }

    public PrandtlNumber getPrandtlNumber() {
        return prandtlNumber;
    }

    public DryAir getDryAirComponent() {
        return dryAirComponent;
    }

    @Override
    public String toConsoleOutput() {
        String separator = " | ";
        String end = "\n\t";
        int digits = 3;
        return "HumidAir:" + end +
               pressure.toEngineeringFormat("P_abs", digits) + separator +
               temperature.toEngineeringFormat("DBT", digits) + separator +
               relativeHumidity.toEngineeringFormat("RH", digits) + separator +
               humidityRatio.toEngineeringFormat("x", digits) + separator +
               maxHumidityRatio.toEngineeringFormat("x\"", digits) + end +

               saturationPressure.toEngineeringFormat("Ps", digits) + separator +
               dewPointTemperature.toEngineeringFormat("WBT", digits) + separator +
               wetBulbTemperature.toEngineeringFormat("TDP", digits) + separator +
               "Vapour status: " + vapourState + end +

               specificEnthalpy.toEngineeringFormat("i", digits) + separator +
               density.toEngineeringFormat("ρ", digits) + separator +
               specificHeat.toEngineeringFormat("cp", digits) + end +

               kinematicViscosity.toEngineeringFormat("ν", digits) + separator +
               dynamicViscosity.toEngineeringFormat("μ", digits) + separator +
               thermalConductivity.toEngineeringFormat("k", digits) + end +

               thermalDiffusivity.toEngineeringFormat("α", digits) + separator +
               prandtlNumber.toEngineeringFormat("Pr", digits) + end +

               dryAirComponent.toConsoleOutput();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        HumidAir that = (HumidAir) o;
        return Objects.equals(temperature, that.temperature)
               && Objects.equals(pressure, that.pressure)
               && Objects.equals(humidityRatio, that.humidityRatio);
    }

    @Override
    public int hashCode() {
        return Objects.hash(temperature, pressure, humidityRatio);
    }

    @Override
    public String toString() {
        return "HumidAir{" +
               "dryBulbTemperature=" + temperature +
               ", absPressure=" + pressure +
               ", density=" + density +
               ", relativeHumidity=" + relativeHumidity +
               ", saturationPressure=" + saturationPressure +
               ", humidityRatio=" + humidityRatio +
               ", maxHumidityRatio=" + maxHumidityRatio +
               ", vapourState=" + vapourState +
               ", wetBulbTemperature=" + wetBulbTemperature +
               ", dewPointTemperature=" + dewPointTemperature +
               ", specificHeat=" + specificHeat +
               ", specificEnthalpy=" + specificEnthalpy +
               ", dynamicViscosity=" + dynamicViscosity +
               ", kinematicViscosity=" + kinematicViscosity +
               ", thermalConductivity=" + thermalConductivity +
               ", thermalDiffusivity=" + thermalDiffusivity +
               ", prandtlNumber=" + prandtlNumber +
               ", dryAirComponent=" + dryAirComponent +
               '}';
    }

    // Custom equality check
    @Override
    public <K extends Fluid> boolean isEqualsWithPrecision(K fluid, double epsilon) {
        if (fluid instanceof HumidAir humidAir) {
            return Fluid.super.isEqualsWithPrecision(fluid, epsilon)
                   && humidityRatio.isEqualWithPrecision(humidAir.humidityRatio, epsilon);
        } else {
            return Fluid.super.isEqualsWithPrecision(fluid, epsilon);
        }
    }

    private static VapourState determineVapourState(Temperature dryBulbTemperature, HumidityRatio humidityRatio, HumidityRatio maxHumidityRatio) {
        if (humidityRatio == maxHumidityRatio) {
            return VapourState.SATURATED;
        } else if ((humidityRatio.isGreaterThan(maxHumidityRatio)) && dryBulbTemperature.isPositive()) {
            return VapourState.WATER_MIST;
        } else if ((humidityRatio.isGreaterThan(maxHumidityRatio)) && dryBulbTemperature.isNegativeOrZero()) {
            return VapourState.ICE_FOG;
        } else {
            return VapourState.UNSATURATED;
        }
    }

    // Static factory methods

    /**
     * Returns a `HumidAir` instance with the specified properties.
     *
     * @param pressure           The absolute pressure of the humid air.
     * @param dryBulbTemperature The dry bulb temperature of the humid air.
     * @param humidityRatio      The humidity ratio of the humid air.
     * @return A `HumidAir` instance.
     */
    public static HumidAir of(Pressure pressure, Temperature dryBulbTemperature, HumidityRatio humidityRatio) {
        return new HumidAir(pressure, dryBulbTemperature, humidityRatio);
    }

    /**
     * Returns a `HumidAir` instance with the specified properties, with default absolute pressure of 101325 Pa.
     *
     * @param dryBulbTemperature The dry bulb temperature of the humid air.
     * @param humidityRatio      The humidity ratio of the humid air.
     * @return A `HumidAir` instance.
     */
    public static HumidAir of(Temperature dryBulbTemperature, HumidityRatio humidityRatio) {
        return new HumidAir(STANDARD_ATMOSPHERE, dryBulbTemperature, humidityRatio);
    }

    /**
     * Returns a `HumidAir` instance with the specified properties.
     *
     * @param pressure           The absolute pressure of the humid air.
     * @param dryBulbTemperature The dry bulb temperature of the humid air.
     * @param relativeHumidity   The relativeHumidity of the humid air.
     * @return A `HumidAir` instance.
     */
    public static HumidAir of(Pressure pressure, Temperature dryBulbTemperature, RelativeHumidity relativeHumidity) {
        CommonValidators.requireBetweenBoundsInclusive(relativeHumidity, RelativeHumidity.RH_MIN_LIMIT, RelativeHumidity.RH_MAX_LIMIT);
        Pressure satPressure = HumidAirEquations.saturationPressure(dryBulbTemperature);
        FluidValidators.requireValidSaturationPressure(satPressure, pressure, dryBulbTemperature);
        HumidityRatio humRatio = HumidAirEquations.humidityRatio(relativeHumidity, satPressure, pressure);
        return new HumidAir(pressure, dryBulbTemperature, humRatio);
    }

    /**
     * Returns a `HumidAir` instance with the specified properties, with default absolute pressure of 101325 Pa.
     *
     * @param dryBulbTemperature The dry bulb temperature of the humid air.
     * @param relativeHumidity   The relativeHumidity of the humid air.
     * @return A `HumidAir` instance.
     */
    public static HumidAir of(Temperature dryBulbTemperature, RelativeHumidity relativeHumidity) {
        return HumidAir.of(STANDARD_ATMOSPHERE, dryBulbTemperature, relativeHumidity);
    }

}
