package com.synerset.hvacengine.property.fluids.liquidwater;

import com.synerset.hvacengine.common.validation.CommonValidators;
import com.synerset.unitility.unitsystem.thermodynamic.*;

/**
 * LIQUID WATER EQUATIONS LIBRARY (PSYCHROMETRICS) <p>
 * Set of static methods outputs process result as an array with process heat, core output air parameters (temperature, humidity ratio) and condensate
 * properties. Methods do not create a separate instance of FlowOfMoistAir for performance reasons - each ot these methods may be used in iterative solvers, and we
 * do not want to lose memory or performance for unnecessary object creation. <p>
 * <p>
 * REFERENCE SOURCES: <p>
 * [1] F.E. Jones, G.L. Harris. ITS-90 Density of water formulation for volumetric standards' calibration. Journal of Research of the National Institute of Standards and Technology (1992) <p>
 * [2] Water specific heat tables: https://www.engineeringtoolbox.com/specific-heat-capacity-water-d_660.html <p>
 * [3] Antoine Equation Coefficient for pure substances: https://myengineeringtools.com/Data_Diagrams/Antoine_Law_Coefficients.html <p>
 * <p>
 * REFERENCES LEGEND KEY: <p>
 * [reference no] [value symbology in standard, unit] (equation number) [page] <p>
 *
 * @author Piotr Jażdżyk, MScEng
 */

public final class LiquidWaterEquations {
    public static final double WATER_CRITICAL_TEMPERATURE_IN_K = 647.096;   // [K]
    public static final double HEAT_OF_WATER_VAPORIZATION = 2500.8982920;   // [kJ/kg]   - Water heat of vaporization (t=0oC)

    private LiquidWaterEquations() {
    }

    /**
     * Returns water enthalpy at provided temperature in kJ/kg<p>
     * Outputs 0.0 for negative temperatures.
     * REFERENCE SOURCE: [-] [kJ/kg] (-) [-]<p>
     * EQUATION LIMITS: n/a <p>
     *
     * @param tx water temperature, oC
     * @return water enthalpy at provided temperature, kJ/kg
     */
    public static double specificEnthalpy(double tx) {
        return tx < 0.0 ? 0.0 : tx * specificHeat(tx);
    }

    public static SpecificEnthalpy specificEnthalpy(Temperature temperature) {
        CommonValidators.requireNotNull(temperature);
        double specificEnthalpyVal = specificEnthalpy(temperature.getInCelsius());
        return SpecificEnthalpy.ofKiloJoulePerKiloGram(specificEnthalpyVal);
    }

    /**
     * Returns water density at provided temperature and constant pressure at 101.325kPa Pa<p>
     * REFERENCE SOURCE: [1] [kg/m3] (1) [kg/m3]<p>
     * EQUATION LIMITS: {0.0 oC,+150.0 oC} at Pat=101.325kPa <p>
     *
     * @param tx water temperature, oC
     * @return water density at temperature tx and atmospheric pressure, kg/m3
     */
    public static double density(double tx) {
        return (999.83952 + 16.945176 * tx
                - 7.9870401 * Math.pow(10, -3) * Math.pow(tx, 2)
                - 46.170461 * Math.pow(10, -6) * Math.pow(tx, 3)
                + 105.56302 * Math.pow(10, -9) * Math.pow(tx, 4)
                - 280.54253 * Math.pow(10, -12) * Math.pow(tx, 5))
               / (1 + 16.89785 * Math.pow(10, -3) * tx);
    }

    public static Density density(Temperature temperature) {
        CommonValidators.requireNotNull(temperature);
        double densityVal = density(temperature.getInCelsius());
        return Density.ofKilogramPerCubicMeter(densityVal);
    }

    /**
     * Returns water isobaric specific heat kJ/kgK<p>
     * REFERENCE SOURCE: [2] [kJ/kgK] (1) [kg/m3]<p>
     * EQUATION LIMITS: {0.0 oC,+250 oC}<p>
     *
     * @param tx water temperature, oC
     * @return water isobaric specific heat
     */
    public static double specificHeat(double tx) {
        if (tx > 0 && tx <= 100) {
            return 3.93240161 * Math.pow(10, -13) * Math.pow(tx, 6)
                   - 1.525847751 * Math.pow(10, -10) * Math.pow(tx, 5)
                   + 2.479227180 * Math.pow(10, -8) * Math.pow(tx, 4)
                   - 2.166932275 * Math.pow(10, -6) * Math.pow(tx, 3)
                   + 1.156152199 * Math.pow(10, -4) * Math.pow(tx, 2)
                   - 3.400567477 * Math.pow(10, -3) * tx + 4.219924305;
        } else {
            return 2.588246403 * Math.pow(10, -15) * Math.pow(tx, 7)
                   - 3.604612987 * Math.pow(10, -12) * Math.pow(tx, 6)
                   + 2.112059173 * Math.pow(10, -9) * Math.pow(tx, 5)
                   - 6.727469888 * Math.pow(10, -7) * Math.pow(tx, 4)
                   + 1.255841880 * Math.pow(10, -4) * Math.pow(tx, 3)
                   - 1.370455849 * Math.pow(10, -2) * Math.pow(tx, 2)
                   + 8.093157187 * Math.pow(10, -1) * tx - 15.75651097;
        }
    }

    public static SpecificHeat specificHeat(Temperature temperature) {
        CommonValidators.requireNotNull(temperature);
        double specHeatVal = specificHeat(temperature.getInCelsius());
        return SpecificHeat.ofKiloJoulePerKiloGramKelvin(specHeatVal);
    }

}