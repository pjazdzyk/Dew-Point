package com.synerset.hvacengine.property.fluids;

import com.synerset.hvacengine.common.validation.CommonValidators;
import com.synerset.unitility.unitsystem.common.Distance;
import com.synerset.unitility.unitsystem.dimensionless.PrandtlNumber;
import com.synerset.unitility.unitsystem.thermodynamic.*;

public final class SharedEquations {

    private SharedEquations() {
    }

    /**
     * Returns atmospheric pressure based on height above the sea level, Pa<p>
     * REFERENCE SOURCE: [1] [Pat,Pa] (3) [6.2]<p>
     * EQUATION LIMITS: {-5000m,+1100m}<p>
     *
     * @param altitude altitude over sea level (can be negative), m
     * @return atmospheric pressure at provided altitude, Pa
     */
    public static double atmAltitudePressure(double altitude) {
        return 101.325 * Math.pow((1 - 2.25577 * Math.pow(10, -5) * altitude), 5.2559) * 1000;
    }

    public static Pressure atmAltitudePressure(Distance altitude) {
        CommonValidators.requireNotNull(altitude);
        double pressVal = atmAltitudePressure(altitude.getInMeters());
        return Pressure.ofPascal(pressVal);
    }

    /**
     * Returns moist air temperature based on height above the sea level, oC<p>
     * REFERENCE SOURCE: [1] [Pat,Pa] (4) [6.2]<p>
     * EQUATION LIMITS: {-5000m,+1100m}<p>
     *
     * @param tempAtSeaLevel temperature at altitude of 0.0m, oC
     * @param altitude       altitude over sea level (can be negative), m
     * @return temperature at provided altitude, oC
     */
    public static double altitudeTemperature(double tempAtSeaLevel, double altitude) {
        return tempAtSeaLevel - 0.0065 * altitude;
    }

    public static Temperature altitudeTemperature(Temperature tempAtSeaLevel, Distance altitude) {
        CommonValidators.requireNotNull(tempAtSeaLevel);
        CommonValidators.requireNotNull(altitude);
        double tempVal = altitudeTemperature(tempAtSeaLevel.getInCelsius(), altitude.getInMeters());
        return Temperature.ofCelsius(tempVal);
    }

    /**
     * Returns thermal diffusivity, m2/s
     * REFERENCE SOURCE: [8] [am,m2/s] (35) [5]<p>
     *
     * @param rho air density, kg/m3
     * @param k   air thermal conductivity, W/(m*K)
     * @param cp  air specific heat, kJ/kgK
     * @return air thermal diffusivity, m2/s
     */
    public static double thermalDiffusivity(double rho, double k, double cp) {
        return k / (rho * cp * 1000d);
    }

    public static ThermalDiffusivity thermalDiffusivity(Density density, ThermalConductivity thermalConductivity, SpecificHeat specificHeat) {
        CommonValidators.requireNotNull(density);
        CommonValidators.requireNotNull(thermalConductivity);
        CommonValidators.requireNotNull(specificHeat);
        double thermalDiffVal = thermalDiffusivity(density.getInKilogramsPerCubicMeters(),
                thermalConductivity.getInWattsPerMeterKelvin(),
                specificHeat.getInKiloJoulesPerKiloGramKelvin());
        return ThermalDiffusivity.ofSquareMeterPerSecond(thermalDiffVal);
    }

    /**
     * Returns air Prandtl number, -
     * Valid for DA, WV, MA if all arguments are provided for type of fluid.
     *
     * @param dynVis air dynamic viscosity,kg/(m*s)
     * @param k      air thermal conductivity, W/mK
     * @param cp     air specific heat, kJ/kgK
     * @return Prandtl number, -
     */
    public static double prandtlNumber(double dynVis, double k, double cp) {
        return dynVis * cp * 1000d / k;
    }

    public static PrandtlNumber prandtlNumber(DynamicViscosity dynamicViscosity, ThermalConductivity thermalConductivity, SpecificHeat specificHeat) {
        CommonValidators.requireNotNull(dynamicViscosity);
        CommonValidators.requireNotNull(thermalConductivity);
        CommonValidators.requireNotNull(specificHeat);
        double prandtlVal = prandtlNumber(dynamicViscosity.getInPascalsSecond(),
                thermalConductivity.getInWattsPerMeterKelvin(),
                specificHeat.getInKiloJoulesPerKiloGramKelvin());
        return PrandtlNumber.of(prandtlVal);
    }

}
