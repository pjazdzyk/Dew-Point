package com.synerset.hvacengine.property.fluids.dryair;

import com.synerset.hvacengine.common.utils.MathUtils;
import com.synerset.hvacengine.common.validation.CommonValidators;
import com.synerset.unitility.unitsystem.thermodynamic.*;

public final class DryAirEquations {

    public static final double DRY_AIR_MOLECULAR_MASS = 28.96546;           // [kg/mol] Dry air molecular mass
    public static final double DRY_AIR_GAS_CONSTANT = 287.055;              // [J/(kg*K)] Dry air specific gas constant
    public static final double DRY_AIR_SUTHERLAND_CONSTANT = 111.0;         // [K] Dry air Sutherland Constant

    private DryAirEquations() {
    }

    /**
     * Returns dry air dynamic viscosity, kg/(m*s) <p>
     * REFERENCE SOURCE: [4] [u,kg/(m*s)] (6.13) [4]<p>
     *
     * @param ta air temperature, oC
     * @return dynamic viscosity, kg/(m*s)
     */
    public static double dynamicViscosity(double ta) {
        double tk = ta + 273.15;
        return (0.40401 + 0.074582 * tk - 5.7171 * Math.pow(10, -5)
                * Math.pow(tk, 2) + 2.9928 * Math.pow(10, -8)
                * Math.pow(tk, 3) - 6.2524 * Math.pow(10, -12)
                * Math.pow(tk, 4)) * Math.pow(10, -6);
    }

    public static DynamicViscosity dynamicViscosity(Temperature temperature) {
        CommonValidators.requireNotNull(temperature);
        double dynVisVal = dynamicViscosity(temperature.getInCelsius());
        return DynamicViscosity.ofKiloGramPerMeterSecond(dynVisVal);
    }

    /**
     * Returns dry air kinematic viscosity, m^2/s<p>
     *
     * @param ta   air temperature, oC
     * @param absP absolute pressure, Pa
     * @return kinematic viscosity, m^2/s
     */
    public static double kinematicViscosity(double ta, double absP) {
        double rhoDa = density(ta, absP);
        return dynamicViscosity(ta) / rhoDa;
    }

    public static KinematicViscosity kinematicViscosity(Temperature dryAirTemperature, Pressure absPressure) {
        CommonValidators.requireNotNull(dryAirTemperature);
        CommonValidators.requireNotNull(absPressure);
        double kinVisVal = kinematicViscosity(dryAirTemperature.getInCelsius(), absPressure.getInPascals());
        return KinematicViscosity.ofSquareMeterPerSecond(kinVisVal);
    }

    /**
     * Returns dry air thermal conductivity, W/(m*K)
     * REFERENCE SOURCE: [4] [k,W/(m*K)] (6.16) [5]<p>
     * EQUATION LIMITS: {-50oC,+500oC},{1atm (1.013bar)}<p>
     *
     * @param ta dry air temperature
     * @return thermal conductivity, W/(m*K)
     */
    public static double thermalConductivity(double ta) {
        return 2.43714 * Math.pow(10, -2)
                + 7.83035 * Math.pow(10, -5) * ta
                - 1.94021 * Math.pow(10, -8) * Math.pow(ta, 2)
                + 2.85943 * Math.pow(10, -12) * Math.pow(ta, 3)
                - 2.61420 * Math.pow(10, -14) * Math.pow(ta, 4);
    }

    public static ThermalConductivity thermalConductivity(Temperature dryAirTemperature) {
        CommonValidators.requireNotNull(dryAirTemperature);
        double thermCondVal = thermalConductivity(dryAirTemperature.getInCelsius());
        return ThermalConductivity.ofWattsPerMeterKelvin(thermCondVal);
    }

    /**
     * Returns dry air specific enthalpy, kJ/kg<p>
     * REFERENCE SOURCE: [5] [i,kJ/kg] (1.20a) [19]<p>
     *
     * @param ta dry air temperature, oC
     * @return dry air specific enthalpy, kJ/kg
     */
    public static double specificEnthalpy(double ta) {
        double cpDa = specificHeat(ta);
        return cpDa * ta;
    }

    public static SpecificEnthalpy specificEnthalpy(Temperature dryAirTemperature) {
        CommonValidators.requireNotNull(dryAirTemperature);
        double specEnthalpyVal = specificEnthalpy(dryAirTemperature.getInCelsius());
        return SpecificEnthalpy.ofKiloJoulePerKiloGram(specEnthalpyVal);
    }

    /**
     * Returns dry air specific heat at constant pressure, kJ/(kg*K)<p>
     * Polynomial approximates proposed by Piotr Jażdżyk based on value tables.
     * REFERENCE SOURCE: [9] [cp,kJ/(kg*K)] (-) [-]<p>
     * EQUATION LIMITS: {-73.0oC,+800oC},{1atm (0.1bar, 5.0bar)}<p>
     *
     * @param ta air temperature, oC
     * @return dry air specific heat, kJ/(kg*K)
     */
    public static double specificHeat(double ta) {
        double a, b, c, d, e;
        if (ta <= -73.15)
            return 1.002;
        if (ta > -73.15 && ta <= -53.15)
            return MathUtils.linearInterpolation(-73.15, 1.002, -53.15, 1.003, ta);
        if (ta > -53.15 && ta <= -13.15)
            return 1.003;
        if (ta > -13.15 && ta <= 86.85) {
            a = 1.0036104793123004;
            b = 5.2562229415778261e-05;
            c = 2.9091167529181888e-07;
            d = -1.3405671294850166e-08;
            e = 1.3020833332371173e-10;
        } else {
            a = 1.0065876262557212;
            b = -2.9062712816134989E-05;
            c = 7.4445335877306371E-07;
            d = -8.4171864437938596E-10;
            e = 3.0582028042912701E-13;
        }
        return e * Math.pow(ta, 4)
                + d * Math.pow(ta, 3)
                + c * Math.pow(ta, 2)
                + b * ta
                + a;
    }

    public static SpecificHeat specificHeat(Temperature dryAirTemperature) {
        CommonValidators.requireNotNull(dryAirTemperature);
        double specHeatVal = specificHeat(dryAirTemperature.getInCelsius());
        return SpecificHeat.ofKiloJoulePerKiloGramKelvin(specHeatVal);
    }

    /**
     * Returns dry air density, kg/m3 <p>
     *
     * @param ta   air temperature, oC
     * @param absP atmospheric pressure, Pa
     * @return dry air density, kg/m3
     */
    public static double density(double ta, double absP) {
        double tk = ta + 273.15;
        return absP / (DRY_AIR_GAS_CONSTANT * tk);
    }

    public static Density density(Temperature dryAirTemperature, Pressure pressure) {
        CommonValidators.requireNotNull(dryAirTemperature);
        CommonValidators.requireNotNull(pressure);
        double densityVal = density(dryAirTemperature.getInCelsius(), pressure.getInPascals());
        return Density.ofKilogramPerCubicMeter(densityVal);
    }

}