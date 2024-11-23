package com.synerset.hvacengine.property.fluids.humidair;


import com.synerset.brentsolver.BrentSolver;
import com.synerset.hvacengine.common.validation.CommonValidators;
import com.synerset.hvacengine.property.fluids.dryair.DryAirEquations;
import com.synerset.hvacengine.property.fluids.liquidwater.LiquidWaterEquations;
import com.synerset.hvacengine.property.fluids.watervapour.WaterVapourEquations;
import com.synerset.hvacengine.property.solids.ice.IceEquations;
import com.synerset.unitility.unitsystem.humidity.HumidityRatio;
import com.synerset.unitility.unitsystem.humidity.RelativeHumidity;
import com.synerset.unitility.unitsystem.thermodynamic.*;

import java.util.function.DoubleUnaryOperator;

/**
 * MOIST AIR PROPERTY EQUATIONS LIBRARY (psYCHROMETRICS)
 * Set of static methods for calculating temperature-dependent thermophysical air properties. Properties are calculated independently for dry air,
 * water vapor, water mist, or ice mist to determine correct values for moist air.
 * These equations are public, stateless, thread-safe, based on primitive types. It Can be used for iterative high-performance computing of intermediate
 * calculations.
 * PROPERTY ABBREVIATIONS:<p>
 * WT - water<p>
 * WV - water vapor<p>
 * DA - dry air<p>
 * MA - moist air<p>
 * ST - steam<p>
 * <p>
 * REFERENCE SOURCE:<p>
 * [1] ASHRAE Fundamentals 2002, CHAPTER 6 <p>
 * [2] Buck, Arden L. "New Equations for Computing Vapor pressure and Enhancement Factor". Journal of Applied Meteorology and Climatology (December 1981) <p>
 * [3] Buck Research Instruments L.L.C. "MODEL CR-1A HYGROMETER WITH AUTO FILL OPERATING MANUAL" (May 2012). <p>
 * [4] Stull R. "Wet-Bulb Temperature from Relative Humidity and Air Temperature". Manuscript received 14 July 2011, in final form 28 August 2011 <p>
 * [5] Morvay Z.K, Gvozdenac D.D. "Fundamentals for analysis and calculation of energy and environmental performance". Applied Industrial Energy And Environmental Management. <p>
 * [6] Lipska B. "Projektowanie Wentylacji i Klimatyzacji. Podstawy uzdatniania powietrza" Wydawnictwo Politechniki Śląskiej (Gliwice  2014) <p>
 * [7] https://www.engineeringtoolbox.com <p>
 * [8] Stull R. "Wet-Bulb Temperature from Relative Humidity and Air Temperature". Manuscript received 14 July 2011, in final form 28 August 2011 <p>
 * [9] Tsilingiris P.T "Thermophysical and transport properties of humid air at temperature range between 0 and 100oC". Elsevier, Science Direct (September 2007) <p>
 * [10] E.W. Lemmon, R.T. Jacobsen, S.G. Penoncello, D. Friend. Thermodynamic Properties of Air and Mixtures of Nitrogen, Argon, and Oxygen from 60 to 2000 K at Pressures to 2000 MPa. J.Phys. Chem. Ref. Data, Vol. 29, No. 3, (2000) <p>
 * [11] M. Wanielista, R. Kersten, R. Eaglin. "Hydrology Water Quantity and Quality Control. 2nd ed." (1997) <p>
 * <p>
 * <p>
 * REFERENCES DESCRIPTION KEY: <p>
 * [reference no] [value symbology in a standard, unit] (equation number) [page] <p>
 *
 * @author Piotr Jażdżyk, MScEng
 */
public final class HumidAirEquations {

    private static final double WG_RATIO = WaterVapourEquations.WATER_VAPOUR_MOLECULAR_MASS / DryAirEquations.DRY_AIR_MOLECULAR_MASS;
    private static final double SOLVER_A_COEF = 0.8;
    private static final double SOLVER_B_COEF = 1.01;

    private HumidAirEquations() {
    }

    // HUMID AIR CORE PROPERTIES

    /**
     * Returns moist air saturation vapour pressure, Pa<p>
     * REFERENCE SOURCE: [1] [ps,Pa] (5,6) [6.2]<p>
     * EQUATION LIMITS: {-100oC,+200oC}<p>
     *
     * @param ta air temperature, oC
     * @return temperature at provided altitude, oC
     */
    public static double saturationPressure(double ta) {
        double expectedSatPressure;
        double estimatedSatPressure;
        double a;
        double tk = ta + 273.15;
        // additional convergence coefficient for higher temperatures, determine empirically
        double n = 1.0;
        final double C1 = -5.6745359E+03;
        final double C2 = 6.3925247E+00;
        final double C3 = -9.6778430E-03;
        final double C4 = 6.2215701E-07;
        final double C5 = 2.0747825E-09;
        final double C6 = -9.4840240E-13;
        final double C7 = 4.1635019E+00;
        final double C8 = -5.8002206E+03;
        final double C9 = 1.3914993E+00;
        final double C10 = -4.8640239E-02;
        final double C11 = 4.1764768E-05;
        final double C12 = -1.4452093E-08;
        final double C13 = 6.5459673E+00;

        DoubleUnaryOperator satPressureExpression;

        if (ta < 0.0) {
            a = 6.1115;
            satPressureExpression = ps -> Math.log(ps) - C1 / tk - C2 - C3 * tk - C4 * tk * tk - C5 * tk * tk * tk - C6 * tk * tk * tk * tk - C7 * Math.log(tk);
        } else {
            a = 6.1121;
            satPressureExpression = ps -> Math.log(ps) - C8 / tk - C9 - C10 * tk - C11 * tk * tk - C12 * tk * tk * tk - C13 * Math.log(tk);
        }
        if (ta > 50.0) {
            n = 1.1;
        }

        // Estimated saturation pressure for convergence speedup
        estimatedSatPressure = a * Math.exp(calcAlfaT(ta)) * 100.0;

        BrentSolver solver = BrentSolver.of("P_SOLVER");
        solver.setEvalDividerX2(2);
        solver.setEvalDividerX2Value(5);
        solver.setCounterpartPoints(estimatedSatPressure * SOLVER_A_COEF, estimatedSatPressure * SOLVER_B_COEF * n);
        expectedSatPressure = solver.findRoot(satPressureExpression);
        return expectedSatPressure;
    }

    public static Pressure saturationPressure(Temperature dryBulbTemp) {
        CommonValidators.requireNotNull(dryBulbTemp);
        double saturationPressVal = saturationPressure(dryBulbTemp.getInCelsius());
        return Pressure.ofPascal(saturationPressVal);
    }

    /**
     * Returns moist air saturation vapour pressure, Pa<p>
     *
     * @param x   humidity ratio, kg.wv/kg.da
     * @param rh  relative humidity, %
     * @param pat atmospheric pressure, Pa
     * @return saturation vapour pressure, Pa
     */
    public static double saturationPressure(double x, double rh, double pat) {
        return x * pat / ((WG_RATIO * rh / 100.0) + x * rh / 100.0);
    }

    public static Pressure saturationPressure(HumidityRatio humRatio, RelativeHumidity relHum, Pressure absPressure) {
        CommonValidators.requireNotNull(humRatio);
        CommonValidators.requireNotNull(relHum);
        double saturationPressVal = saturationPressure(humRatio.getInKilogramPerKilogram(),
                relHum.getInPercent(),
                absPressure.getInPascals());
        return Pressure.ofPascal(saturationPressVal);
    }

    /**
     * Returns moist air dew point temperature based on air temperature <i>ta</i> and relative humidity <i>rh<i/>, oC<p>
     * REFERENCE SOURCE: [1,2,3] [Tdp,Pa] (-) [-]<p>
     * EQUATION LIMITS: {-80oc,+50oC}<p>
     *
     * @param ta air temperature, oC
     * @param rh relative humidity, %
     * @return dew point temperature, oC
     */
    public static double dewPointTemperature(double ta, double rh, double pat) {
        if (rh >= 100)
            return ta;
        if (rh == 0.0)
            return Double.NEGATIVE_INFINITY;
        // Arden-Buck procedure tdP estimation (used for rh>25)
        double tdpEstimated;
        double a, b, c, d;
        double betaTrh, bTrh, cTrh;
        if (ta > 0.0) {
            b = 18.678;
            c = 257.14;
            d = 234.50;
        } else {
            b = 23.036;
            c = 279.82;
            d = 333.70;
        }
        a = 2.0 / d;
        betaTrh = Math.log(rh / 100.0) + calcAlfaT(ta);
        bTrh = b - betaTrh;
        cTrh = -c * betaTrh;
        tdpEstimated = 1.0 / a * (bTrh - Math.sqrt(bTrh * bTrh + 2.0 * a * cTrh));
        if (rh < 25.0) {
            double ps = saturationPressure(ta);
            double x = humidityRatio(rh, ps, pat);
            BrentSolver solver = BrentSolver.of("T_SOLVER");
            solver.setEvalDividerX2(2);
            solver.setEvalDividerX2Value(5);
            solver.setCounterpartPoints(tdpEstimated * SOLVER_A_COEF, tdpEstimated * SOLVER_B_COEF);
            if (rh < 1.0) {
                solver.setAccuracy(0.0000001);
            }
            double tdpExact = solver.findRoot(temp -> {
                double ps1 = saturationPressure(temp);
                double x1 = maxHumidityRatio(ps1, pat);
                return x1 - x;

            });
            return tdpExact;
        }
        return tdpEstimated;
    }

    public static Temperature dewPointTemperature(Temperature dryBulbTemp, RelativeHumidity relHum, Pressure absPressure) {
        CommonValidators.requireNotNull(dryBulbTemp);
        CommonValidators.requireNotNull(relHum);
        CommonValidators.requireNotNull(absPressure);
        double dewPointTempVal = dewPointTemperature(dryBulbTemp.getInCelsius(),
                relHum.getInPercent(),
                absPressure.getInPascals());
        return Temperature.ofCelsius(dewPointTempVal);
    }

    /**
     * Returns moist air wet bulb temperature, oC<p>
     * REFERENCE SOURCE: [1] [Twb,oC] (33) [6.9]<p>
     * EQUATION LIMITS: {-100oC,+200oC}<p>
     *
     * @param ta  air temperature, oC
     * @param rh  air relative humidity, oC
     * @param pat atmospheric pressure, Pa
     * @return moist air wet bulb temperature, oC
     */
    public static double wetBulbTemperature(double ta, double rh, double pat) {
        if (rh >= 100.0)
            return ta;
        double estimatedWbt = ta * Math.atan(0.151977 * Math.pow(rh + 8.313659, 0.5))
                              + Math.atan(ta + rh) - Math.atan(rh - 1.676331)
                              + 0.00391838 * Math.pow(rh, 1.5) * Math.atan(0.023101 * rh)
                              - 4.686035;
        double ps = saturationPressure(ta);
        double x = humidityRatio(rh, ps, pat);
        double h = specificEnthalpy(ta, x, pat);
        BrentSolver solver = BrentSolver.of("T_SOLVER");
        solver.setEvalDividerX2(2);
        solver.setEvalDividerX2Value(5);
        solver.setCounterpartPoints(estimatedWbt * SOLVER_A_COEF, estimatedWbt * SOLVER_B_COEF);
        return solver.findRoot(temp -> {
            double ps1 = saturationPressure(temp);
            double x1 = maxHumidityRatio(ps1, pat);
            double h1 = specificEnthalpy(temp, x1, pat);
            double hw1;
            if (temp <= 0.0)
                hw1 = IceEquations.specificEnthalpy(temp);
            else
                hw1 = LiquidWaterEquations.specificEnthalpy(temp);
            return h + (x1 - x) * hw1 - h1;
        });
    }

    public static Temperature wetBulbTemperature(Temperature dryBulbTemp, RelativeHumidity relHum, Pressure absPressure) {
        CommonValidators.requireNotNull(dryBulbTemp);
        CommonValidators.requireNotNull(relHum);
        CommonValidators.requireNotNull(absPressure);
        double wetBulbTempVal = wetBulbTemperature(dryBulbTemp.getInCelsius(),
                relHum.getInPercent(),
                absPressure.getInPascals());
        return Temperature.ofCelsius(wetBulbTempVal);
    }

    /**
     * Returns moist air relative humidity rh from dew point temperature "tdp" and air temperature "ta", %<p>
     * REFERENCE SOURCE: [2,3] [rh,%] (-) [-]<p>
     * EQUATION LIMITS: {-80oc,+50oC}<p>
     *
     * @param ta  air temperature, oC
     * @param tdp air dew point temperature, oC
     * @return relative humidity, %
     */
    public static double relativeHumidity(double tdp, double ta) {
        return Math.exp(calcAlfaT(tdp) - calcAlfaT(ta)) * 100;
    }

    public static RelativeHumidity relativeHumidity(Temperature dewPointTemp, Temperature dryBulbTemp) {
        CommonValidators.requireNotNull(dewPointTemp);
        CommonValidators.requireNotNull(dryBulbTemp);
        double relHumVal = relativeHumidity(dewPointTemp.getInCelsius(),
                dryBulbTemp.getInCelsius());
        return RelativeHumidity.ofPercentage(relHumVal);
    }

    /**
     * Returns moist air relative humidity rh from air temperature <i>ta</i> and humidity ratio <i>x</i>, %<p>
     *
     * @param ta  air temperature, oC
     * @param x   relative humidity, kg.wv/kg.da
     * @param pat atmospheric pressure, Pa
     * @return relative humidity, %
     */
    public static double relativeHumidity(double ta, double x, double pat) {
        if (x == 0.0)
            return 0.0;
        double ps = HumidAirEquations.saturationPressure(ta);
        double rh = x * pat / (WG_RATIO * ps + x * ps);
        return rh > 1 ? 100 : rh * 100;
    }

    public static RelativeHumidity relativeHumidity(Temperature dryBulbTemp, HumidityRatio humidityRatio, Pressure absPressure) {
        CommonValidators.requireNotNull(dryBulbTemp);
        CommonValidators.requireNotNull(humidityRatio);
        CommonValidators.requireNotNull(absPressure);
        double relHumVal = relativeHumidity(dryBulbTemp.getInCelsius(),
                humidityRatio.getInKilogramPerKilogram(),
                absPressure.getInPascals());
        return RelativeHumidity.ofPercentage(relHumVal);
    }

    /**
     * Returns moist air humidity ratio<p>
     * REFERENCE SOURCE: [1] [x,kg.wv/kg.da] (23a) [6.10]<p>
     *
     * @param rh  air relative humidity, %
     * @param ps  air saturation pressure, Pa
     * @param pat atmospheric pressure, Pa
     * @return humidity ratio, kg.wv/kg.da
     */
    public static double humidityRatio(double rh, double ps, double pat) {
        return WG_RATIO * (rh / 100.0 * ps) / (pat - (rh / 100.0) * ps);
    }

    public static HumidityRatio humidityRatio(RelativeHumidity relHum, Pressure saturationPressure, Pressure absPressure) {
        CommonValidators.requireNotNull(relHum);
        CommonValidators.requireNotNull(saturationPressure);
        CommonValidators.requireNotNull(absPressure);
        double humRatioVal = humidityRatio(relHum.getInPercent(),
                saturationPressure.getInPascals(),
                absPressure.getInPascals());
        return HumidityRatio.ofKilogramPerKilogram(humRatioVal);
    }

    /**
     * Returns moist air maximum humidity ratio, kg.wv/kg.da<p>
     * REFERENCE SOURCE: [1] [xMax,kg.wv/kg.da] (23) [6.8]<p>
     *
     * @param ps  air saturation pressure, Pa
     * @param pat atmospheric pressure, Pa
     * @return humidity ratio, kg.wv/kg.da
     */
    public static double maxHumidityRatio(double ps, double pat) {
        return humidityRatio(100.0, ps, pat);
    }

    public static HumidityRatio maxHumidityRatio(Pressure saturationPressure, Pressure absPressure) {
        CommonValidators.requireNotNull(saturationPressure);
        CommonValidators.requireNotNull(absPressure);
        double humRatioVal = maxHumidityRatio(saturationPressure.getInPascals(),
                absPressure.getInPascals());
        return HumidityRatio.ofKilogramPerKilogram(humRatioVal);
    }

    // DYNAMIC VISCOSITY CALCULATION

    /**
     * Returns moist air dynamic viscosity, kg/(m*s) <p>
     * REFERENCE SOURCE: [4] [u,kg/(m*s)] (6.12) [4]<p>
     * EQUATION LIMITS: {no data}<p>
     *
     * @param ta air temperature, oC
     * @param x  humidity ratio, kg.wv/kg.da
     * @return dynamic viscosity, kg/(m*s)
     */
    public static double dynamicViscosity(double ta, double x) {
        double dynVisDa = DryAirEquations.dynamicViscosity(ta);
        if (x == 0)
            return dynVisDa;
        double xm = x * 1.61;
        double dynVisWv = WaterVapourEquations.dynamicViscosity(ta);
        double fiAV = Math.pow(1 + Math.pow(dynVisDa / dynVisWv, 0.5)
                                   * Math.pow(WaterVapourEquations.WATER_VAPOUR_MOLECULAR_MASS / DryAirEquations.DRY_AIR_MOLECULAR_MASS, 0.25), 2)
                      / (2 * Math.sqrt(2) * Math.pow(1 + (DryAirEquations.DRY_AIR_MOLECULAR_MASS / WaterVapourEquations.WATER_VAPOUR_MOLECULAR_MASS), 0.5));

        double fiVA = Math.pow(1 + Math.pow(dynVisWv / dynVisDa, 0.5)
                                   * Math.pow(DryAirEquations.DRY_AIR_MOLECULAR_MASS / WaterVapourEquations.WATER_VAPOUR_MOLECULAR_MASS, 0.25), 2)
                      / (2 * Math.sqrt(2) * Math.pow(1 + (WaterVapourEquations.WATER_VAPOUR_MOLECULAR_MASS / DryAirEquations.DRY_AIR_MOLECULAR_MASS), 0.5));

        return (dynVisDa / (1 + fiAV * xm)) + (dynVisWv / (1 + fiVA / xm));
    }

    public static DynamicViscosity dynamicViscosity(Temperature dryBulbTemp, HumidityRatio humRatio) {
        CommonValidators.requireNotNull(dryBulbTemp);
        CommonValidators.requireNotNull(humRatio);
        double dynVisVal = dynamicViscosity(dryBulbTemp.getInCelsius(),
                humRatio.getInKilogramPerKilogram());
        return DynamicViscosity.ofKiloGramPerMeterSecond(dynVisVal);
    }

    // KINEMATIC VISCOSITY CALCULATION

    /**
     * Returns moist air kinematic viscosity, m^2/s<p>
     *
     * @param ta    air temperature, oC
     * @param x     air humidity ratio, kg.wv/kg.da
     * @param rhoMa humid air density, kg/m3
     * @return kinematic viscosity, m^2/s
     */
    public static double kinematicViscosity(double ta, double x, double rhoMa) {
        return x == 0.0
                ? DryAirEquations.dynamicViscosity(ta) / rhoMa
                : dynamicViscosity(ta, x) / rhoMa;
    }

    public static KinematicViscosity kinematicViscosity(Temperature dryBulbTemp, HumidityRatio humRatio, Density density) {
        CommonValidators.requireNotNull(dryBulbTemp);
        CommonValidators.requireNotNull(humRatio);
        CommonValidators.requireNotNull(density);
        double kinVisVal = kinematicViscosity(dryBulbTemp.getInCelsius(),
                humRatio.getInKilogramPerKilogram(),
                density.getInKilogramsPerCubicMeters());
        return KinematicViscosity.ofSquareMeterPerSecond(kinVisVal);
    }

    // THERMAL CONDUCTIVITY CALCULATION

    /**
     * Returns moist air thermal conductivity, W/(m*K)<p>
     * REFERENCE SOURCE: [4] [k,W/(m*K)] (6.15) [5]<p>
     * EQUATION LIMITS: {0.0oC,+200oC},{1atm (1.013bar)}<p>
     *
     * @param ta air temperature, oC
     * @param x  air humidity ratio, kg.wv/kg.da
     * @return air thermal conductivity, W/(m*K)
     */
    public static double thermalConductivity(double ta, double x) {
        double dynVisDa = DryAirEquations.dynamicViscosity(ta);
        double dynVisWv = WaterVapourEquations.dynamicViscosity(ta);
        double kDa = DryAirEquations.thermalConductivity(ta);
        if (x == 0.0)
            return kDa;
        double sutDa = DryAirEquations.DRY_AIR_SUTHERLAND_CONSTANT;
        double sutWv = WaterVapourEquations.WATER_VAPOUR_SUTHERLAND_CONSTANT;
        double tk = ta + 273.15;
        double sutAv = 0.733 * Math.sqrt(sutDa * sutWv);
        double kWv = WaterVapourEquations.thermalConductivity(ta);
        double xm = 1.61 * x;
        double alfaAV;
        double alfaVA;
        double betaAV;
        double betaVA;
        double aAV;
        double aVA;
        alfaAV = (dynVisDa / dynVisWv) * Math.pow(WG_RATIO, 0.75) * ((1.0 + sutDa / tk) / (1.0 + sutWv / tk));
        alfaVA = (dynVisWv / dynVisDa) * Math.pow(WG_RATIO, 0.75) * ((1.0 + sutWv / tk) / (1.0 + sutDa / tk));
        betaAV = (1.0 + sutAv / tk) / (1.0 + sutDa / tk);
        betaVA = (1.0 + sutAv / tk) / (1.0 + sutWv / tk);
        aAV = 0.25 * Math.pow(1.0 + alfaAV, 2.0) * betaAV;
        aVA = 0.25 * Math.pow(1.0 + alfaVA, 2.0) * betaVA;
        return (kDa / (1.0 + aAV * xm)) + (kWv / (1.0 + aVA / xm));
    }

    public static ThermalConductivity thermalConductivity(Temperature dryBulbTemp, HumidityRatio humRatio) {
        CommonValidators.requireNotNull(dryBulbTemp);
        CommonValidators.requireNotNull(humRatio);
        double thermCondVal = thermalConductivity(dryBulbTemp.getInCelsius(),
                humRatio.getInKilogramPerKilogram());
        return ThermalConductivity.ofWattsPerMeterKelvin(thermCondVal);
    }

    // SPECIFIC ENTHALPY CALCULATION

    /**
     * Returns moist air specific enthalpy, kJ/kg.
     * Water fog or ice mist will be included based on provided X value
     * and air temperature.
     * REFERENCE SOURCE: [5] [i,kJ/kg] (1.20) [19]<p>
     *
     * @param ta  air temperature, oC
     * @param x   air humidity ratio, kg.wv/kg.da
     * @param pat atmospheric pressure, Pa
     * @return humid air specific enthalpy
     */
    public static double specificEnthalpy(double ta, double x, double pat) {
        double iDa = DryAirEquations.specificEnthalpy(ta);
        // Case1: no humidity = dry air only
        if (x == 0.0)
            return iDa;
        // Case2: x <= xMax, unsaturated air
        double ps = HumidAirEquations.saturationPressure(ta);
        double xMax = maxHumidityRatio(ps, pat);
        double iWv = WaterVapourEquations.specificEnthalpy(ta) * x;
        if (x <= xMax)
            return iDa + iWv;
        // Case3: x > XMax, saturated air with water or ice fog
        iWv = WaterVapourEquations.specificEnthalpy(ta) * xMax;
        double iWt = LiquidWaterEquations.specificEnthalpy(ta) * (x - xMax);
        double iIce = IceEquations.specificEnthalpy(ta) * (x - xMax);
        return iDa + iWv + iWt + iIce;
    }

    public static SpecificEnthalpy specificEnthalpy(Temperature dryBulbTemp, HumidityRatio humRatio, Pressure absPressure) {
        CommonValidators.requireNotNull(dryBulbTemp);
        CommonValidators.requireNotNull(humRatio);
        CommonValidators.requireNotNull(absPressure);
        double specificEnthalpyVal = specificEnthalpy(dryBulbTemp.getInCelsius(),
                humRatio.getInKilogramPerKilogram(),
                absPressure.getInPascals());
        return SpecificEnthalpy.ofKiloJoulePerKiloGram(specificEnthalpyVal);
    }

    // SPECIFIC HEAT CALCULATION

    /**
     * Returns moist air-specific heat at constant pressure, J/(kg*K)<p>
     * REFERENCE SOURCE: [6] [cp,kJ/(kg*K)] (6.10) [4]<p>
     * EQUATION LIMITS: {0.0oC,+200oC},{1atm (0.1bar, 5.0bar)}<p>
     *
     * @param ta air temperature, oC
     * @param x  air humidity ratio, kg.wv/kg.da
     * @return moist air specific heat, kJ/(kg*K)
     */
    public static double specificHeat(double ta, double x) {
        return DryAirEquations.specificHeat(ta) + x * WaterVapourEquations.specificHeat(ta);
    }

    public static SpecificHeat specificHeat(Temperature dryBulbTemp, HumidityRatio humRatio) {
        CommonValidators.requireNotNull(dryBulbTemp);
        CommonValidators.requireNotNull(humRatio);
        double specHeatVal = specificHeat(dryBulbTemp.getInCelsius(),
                humRatio.getInKilogramPerKilogram());
        return SpecificHeat.ofKiloJoulePerKiloGramKelvin(specHeatVal);
    }

    // DENSITY CALCULATION

    /**
     * Returns moist air density, kg/m3
     * REFERENCE SOURCE: [1] [xMax,kg.wv/kg.da] (23) [6.8]<p>
     * LIMITS: Important: presence of water mist or ice mist is not taken into account here - TBC
     *
     * @param ta  air temperature, oC
     * @param x   air humidity ratio, kg.wv/kg/da
     * @param pat atmospheric pressure, Pa
     * @return air density, kg/m3
     */
    public static double density(double ta, double x, double pat) {
        if (x == 0.0) {
            return DryAirEquations.density(ta, pat);
        }
        double patKpa = pat / 1000.0;
        double tk = ta + 273.15;
        return 1.0 / ((0.2871 * tk * (1.0 + 1.6078 * x)) / patKpa);
    }

    public static Density density(Temperature dryBulbTemp, HumidityRatio humRatio, Pressure absPressure) {
        CommonValidators.requireNotNull(dryBulbTemp);
        CommonValidators.requireNotNull(humRatio);
        CommonValidators.requireNotNull(absPressure);
        double densVal = density(dryBulbTemp.getInCelsius(),
                humRatio.getInKilogramPerKilogram(),
                absPressure.getInPascals());
        return Density.ofKilogramPerCubicMeter(densVal);
    }

    // DRY BULB TEMPERATURE CALCULATION FROM OTHER QUANTITIES

    /**
     * Returns moist air dry bulb temperature based on tdp and rh.
     * REFERENCE SOURCE: [10] [oC] (-) [-]<p>
     *
     * @param tdp air dew point temperature, oC
     * @param rh  air relative humidity, %
     * @return air dry bulb temperature, oC
     */
    public static double dryBulbTemperatureTdpRH(double tdp, double rh, double pat) {
        if (rh == 0.0) {
            return Double.POSITIVE_INFINITY;
        }
        double taEstimated = (tdp - 112.0 * Math.pow(rh / 100.0, 1.0 / 8.0) + 112.0) / (0.9 * Math.pow(rh / 100.0, 1.0 / 8.0) + 0.1);
        //New instance of BrentSolver is required, to avoid clash between two methods using P_SOLVER
        //at the same time.
        BrentSolver solver = new BrentSolver();
        solver.setCounterpartPoints(taEstimated * SOLVER_A_COEF, taEstimated * SOLVER_B_COEF);
        return solver.findRoot(temp -> tdp - dewPointTemperature(temp, rh, pat));
    }

    public static Temperature dryBulbTemperatureTdpRH(Temperature dewPointTemp, RelativeHumidity relHum, Pressure absPressure) {
        CommonValidators.requireNotNull(dewPointTemp);
        CommonValidators.requireNotNull(relHum);
        CommonValidators.requireNotNull(absPressure);
        double dryBulbTemp = dryBulbTemperatureTdpRH(dewPointTemp.getInCelsius(),
                relHum.getInPercent(),
                absPressure.getInPascals());
        return Temperature.ofCelsius(dryBulbTemp);
    }

    /**
     * Returns moist air dry bulb temperature, based on x, rh and pat, oC.
     *
     * @param x   - humidity ratio, kg.wv/kg.da
     * @param rh  - relative humidity, %
     * @param pat - atmospheric pressure, Pa
     * @return dry bulb air temperature, oC
     */
    public static double dryBulbTemperatureXRH(double x, double rh, double pat) {
        BrentSolver solver = BrentSolver.of("T_Xrh_SOLVER");
        solver.setEvalDividerX2(2);
        solver.setEvalDividerX2Value(5);
        return solver.findRoot(tx -> saturationPressure(x, rh, pat) - saturationPressure(tx));
    }

    public static Temperature dryBulbTemperatureXRH(HumidityRatio humidityRatio, RelativeHumidity relHum, Pressure absPressure) {
        CommonValidators.requireNotNull(humidityRatio);
        CommonValidators.requireNotNull(relHum);
        CommonValidators.requireNotNull(absPressure);
        double dryBulbTemp = dryBulbTemperatureXRH(humidityRatio.getInKilogramPerKilogram(),
                relHum.getInPercent(),
                absPressure.getInPascals());
        return Temperature.ofCelsius(dryBulbTemp);
    }

    /**
     * Returns moist air dry bulb temperature, based on ix, x and pat, oC. \
     * LIMITS: ta &lt 70oC.
     *
     * @param ix  air specific enthalpy, kJ/kg
     * @param x   air humidity ratio, kg.wv/kg.da
     * @param pat atmospheric pressure, pat
     * @return air dry bulb temperature, oC
     */
    public static double dryBulbTemperatureIX(double ix, double x, double pat) {
        BrentSolver solver = BrentSolver.of("T_IX_SOLVER");
        solver.setEvalCycles(30);
        solver.setEvalDividerX2(2);
        solver.setEvalDividerX2Value(5);
        return solver.findRoot(tx -> ix - HumidAirEquations.specificEnthalpy(tx, x, pat));
    }

    public static Temperature dryBulbTemperatureIX(SpecificEnthalpy specEnthalpy, HumidityRatio humidityRatio, Pressure absPressure) {
        CommonValidators.requireNotNull(specEnthalpy);
        CommonValidators.requireNotNull(humidityRatio);
        CommonValidators.requireNotNull(absPressure);
        double dryBulbTemp = dryBulbTemperatureIX(
                specEnthalpy.getInKiloJoulesPerKiloGram(),
                humidityRatio.getInKilogramPerKilogram(),
                absPressure.getInPascals());
        return Temperature.ofCelsius(dryBulbTemp);
    }

    // OTHER FUNCTIONS

    /**
     * Returns moist air dry bulb temperature based on wbt and rh, oC
     *
     * @param wbt wet bulb temperature, oC
     * @param rh  relative humidity, %
     * @return air dry bulb temperature, oC
     */
    public static double dryBulbTemperatureWbtRH(double wbt, double rh, double pat) {
        BrentSolver solver = BrentSolver.of("T_WbtRH_SOLVER");
        return solver.findRoot(temp -> wbt - wetBulbTemperature(temp, rh, pat));
    }

    public static Temperature dryBulbTemperatureWbtRH(Temperature wetBulbTemperature, RelativeHumidity relHum, Pressure absPressure) {
        CommonValidators.requireNotNull(wetBulbTemperature);
        CommonValidators.requireNotNull(relHum);
        CommonValidators.requireNotNull(absPressure);
        double dryBulbTemp = dryBulbTemperatureWbtRH(wetBulbTemperature.getInCelsius(),
                relHum.getInPercent(),
                absPressure.getInPascals());
        return Temperature.ofCelsius(dryBulbTemp);
    }

    /**
     * Returns maximum dry bulb air temperature, for which condition pat>ps is met for rh=100% oC
     *
     * @param inPat atmospheric pressure, Pa
     * @return maximum dry bulb air temperature, oC
     */
    public static double dryBulbTemperatureMax(double inPat) {
        double estimatedTa = -237300 * Math.log(0.001638 * inPat) / (1000 * Math.log(0.001638 * inPat) - 17269);
        BrentSolver solver = new BrentSolver();
        solver.setCounterpartPoints(estimatedTa * SOLVER_A_COEF, estimatedTa * SOLVER_B_COEF * 1.5);
        return solver.findRoot(ta -> inPat - saturationPressure(ta));
    }

    public static Temperature dryBulbTemperatureMax(Pressure absPressure) {
        CommonValidators.requireNotNull(absPressure);
        double dryBulbTemp = dryBulbTemperatureMax(absPressure.getInPascals());
        return Temperature.ofCelsius(dryBulbTemp);
    }

    // TOOL METHODS

    private static double calcAlfaT(double ta) {
        //Coefficient used for Arden-Buck equation for calculating saturation pressure ps, Pa
        double b = 0;
        double c = 0;
        double d = 1;
        if (ta > 0) {
            b = 18.678;
            c = 257.14;
            d = 234.50;
        } else if (ta <= 0) {
            b = 23.036;
            c = 279.82;
            d = 333.70;
        }
        return (b - ta / d) * (ta / (c + ta));
    }

}