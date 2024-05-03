package com.synerset.hvacengine.property.solids.ice;

import com.synerset.unitility.unitsystem.thermodynamic.*;

/**
 * MOIST AIR PROPERTY EQUATIONS LIBRARY (PSYCHROMETRICS)
 * Set of static methods for calculating temperature dependant thermophysical ice properties.
 * <p>
 * REFERENCE SOURCE:
 * [1] https://www.engineeringtoolbox.com/ice-thermal-properties-d_576.html
 *
 * REFERENCES DESCRIPTION KEY:
 * [reference no] [value symbology in standard, unit] (equation number) [page]
 *
 * @author Piotr Jażdżyk, MScEng
 */
public final class IceEquations {

    private IceEquations() {
    }

    public static final double HEAT_OF_ICE_MELT = 334.1;                     // [kJ/kg] Heat of ice melt

    /**
     * Returns ice enthalpy at provided temperature and constant pressure at atmospheric pressure.
     * It already includes the heat of ice melting, for the reference temperature of 0oC.
     * It will output 0 for positive temperatures.
     * REFERENCE SOURCE: [1] (-) [kJ/kgK]
     *
     * @param tx ice thermal , oC
     * @return ice specific enthalpy, kJ/kgK
     */
    public static double specificEnthalpy(double tx) {
        return tx > 0.0 ? 0.0 : tx * specificHeat(tx) - HEAT_OF_ICE_MELT;
    }

    public static SpecificEnthalpy specificEnthalpy(Temperature temperature) {
        double specificEnthalpyVal = specificEnthalpy(temperature.getInCelsius());
        return SpecificEnthalpy.ofKiloJoulePerKiloGram(specificEnthalpyVal);
    }

    /**
     * Returns ice specific heat at provided temperature and constant pressure at atmospheric pressure
     * REFERENCE SOURCE: [1] (-) [kJ/kgK]
     * EQUATION LIMITS: {-100.0 oC, 0.0 oC} at Pat=atmospheric
     *
     * @param tx ice temperature, oC
     * @return ice specific heat, kJ/kgK
     */
    public static double specificHeat(double tx) {
        return 2.0509727263 + 0.0048764802 * tx
                - 0.0000277225 * Math.pow(tx, 2)
                - 0.0000001031 * Math.pow(tx, 3);
    }

    public static SpecificHeat specificHeat(Temperature temperature) {
        double specHeatVal = specificHeat(temperature.getInCelsius());
        return SpecificHeat.ofKiloJoulePerKiloGramKelvin(specHeatVal);
    }

    /**
     * Returns ice thermal conductivity at provided temperature and constant pressure at atmospheric pressure<p>
     * REFERENCE SOURCE: [1] (-) [W/mK]
     * EQUATION LIMITS: {-100.0 oC, 0.0 oC} at Pat=atmospheric
     *
     * @param tx ice thermal , oC
     * @return ice specific heat, W/mK
     */
    public static double thermalConductivity(double tx) {
        return 2.2173524402158 - 0.0069168602852 * tx
                + 0.0001016721167 * Math.pow(tx, 2)
                + 0.0000004456743 * Math.pow(tx, 3);
    }

    public static ThermalConductivity thermalConductivity(Temperature temperature) {
        double thermCondVal = thermalConductivity(temperature.getInCelsius());
        return ThermalConductivity.ofWattsPerMeterKelvin(thermCondVal);
    }

    /**
     * Returns density at provided temperature and constant pressure at atmospheric pressure
     * REFERENCE SOURCE: [1] (-) [kg/m3]
     * EQUATION LIMITS: {-100.0 oC, 0.0 oC} at Pat=atmospheric
     *
     * @param tx ice temperature, oC
     * @return ice specific heat, kg/m3
     */
    public static double density(double tx) {
        return 916.1204382651714 - 0.42803436487679 * tx
                - 0.02237994685111 * Math.pow(tx, 2)
                - 0.00061508830263 * Math.pow(tx, 3)
                - 0.00000784399543 * Math.pow(tx, 4)
                - 0.00000003790984 * Math.pow(tx, 5)
                + 0.00000000005916 * Math.pow(tx, 6)
                + 0.00000000000078 * Math.pow(tx, 7);
    }

    public static Density density(Temperature temperature) {
        double densVal = density(temperature.getInCelsius());
        return Density.ofKilogramPerCubicMeter(densVal);
    }

}