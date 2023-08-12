package com.synerset.hvaclib.fluids.euqations;

import com.synerset.unitility.unitsystem.common.Distance;
import com.synerset.unitility.unitsystem.dimensionless.PrandtlNumber;
import com.synerset.unitility.unitsystem.thermodynamic.*;

public final class SharedEquations {

    private SharedEquations() {
    }

    /**
     * Returns atmospheric pressure based on height above the sea level, Pa<br>
     * REFERENCE SOURCE: [1] [Pat,Pa] (3) [6.2]<br>
     * EQUATION LIMITS: {-5000m,+1100m}<br>
     *
     * @param altitude altitude over sea level (can be negative), m
     * @return atmospheric pressure at provided altitude, Pa
     */
    public static double atmAltitudePressure(double altitude) {
        return 101.325 * Math.pow((1 - 2.25577 * Math.pow(10, -5) * altitude), 5.2559) * 1000;
    }

    public static Pressure atmAltitudePressure(Distance altitude) {
        double pressVal = atmAltitudePressure(altitude.getValueOfMeters());
        return Pressure.ofPascal(pressVal);
    }

    /**
     * Returns moist air temperature based on height above the sea level, oC<br>
     * REFERENCE SOURCE: [1] [Pat,Pa] (4) [6.2]<br>
     * EQUATION LIMITS: {-5000m,+1100m}<br>
     *
     * @param tempAtSeaLevel temperature at altitude of 0.0m, oC
     * @param altitude       altitude over sea level (can be negative), m
     * @return temperature at provided altitude, oC
     */
    public static double altitudeTemperature(double tempAtSeaLevel, double altitude) {
        return tempAtSeaLevel - 0.0065 * altitude;
    }

    public static Temperature altitudeTemperature(Temperature tempAtSeaLevel, Distance altitude) {
        double tempVal = altitudeTemperature(tempAtSeaLevel.getValueOfCelsius(), altitude.getValueOfMeters());
        return Temperature.ofCelsius(tempVal);
    }

    /**
     * Returns thermal diffusivity, m2/s
     * REFERENCE SOURCE: [8] [am,m2/s] (35) [5]<br>
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
        double thermalDiffVal = thermalDiffusivity(density.getValueOfKilogramPerCubicMeter(),
                thermalConductivity.getValueOfWatsPerMeterKelvin(),
                specificHeat.getValueOfKiloJoulesPerKilogramKelvin());
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
        double prandtlVal = prandtlNumber(dynamicViscosity.getValueOfPascalSecond(),
                thermalConductivity.getValueOfWatsPerMeterKelvin(),
                specificHeat.getValueOfKiloJoulesPerKilogramKelvin());
        return PrandtlNumber.of(prandtlVal);
    }

}