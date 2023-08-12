package com.synerset.hvaclib.fluids.euqations;


import com.synerset.brentsolver.BrentSolver;
import com.synerset.hvaclib.solids.equations.IceEquations;
import com.synerset.unitility.unitsystem.humidity.HumidityRatio;
import com.synerset.unitility.unitsystem.humidity.RelativeHumidity;
import com.synerset.unitility.unitsystem.thermodynamic.*;

import java.util.function.DoubleFunction;

/**
 * MOIST AIR PROPERTY EQUATIONS LIBRARY (PSYCHROMETRICS)<br>
 * Set of static methods for calculating temperature dependant thermophysical air properties. Properties are calculated independently for dry air,
 * water vapour, water mist or ice mist to determine correct values for moist air.<br>
 * These equations are public, stateless, thread safe, based on primitive types. Can be used for iterative high performance computing of intermediate
 * calculations.
 * <p><br>
 * PROPERTY ABBREVIATIONS: <br>
 * WT - water <br>
 * WV - water vapour <br>
 * DA - dry air <br>
 * MA - moist air <br>
 * ST - steam <br>
 * <p><br>
 * REFERENCE SOURCE: <br>
 * [1] ASHRAE Fundamentals 2002, CHAPTER 6 <br>
 * [2] Buck, Arden L. "New Equations for Computing Vapour pressure and Enhancement Factor". Journal of Applied Meteorology and Climatology (December 1981) <br>
 * [3] Buck Research Instruments L.L.C. "MODEL CR-1A HYGROMETER WITH AUTO FILL OPERATING MANUAL" (May 2012). <br>
 * [4] Stull R. "Wet-Bulb Temperature from Relative Humidity and Air Temperature". Manuscript received 14 July 2011, in final form 28 August 2011 <br>
 * [5] Morvay Z.K, Gvozdenac D.D. "Fundamentals for analysis and calculation of energy and environmental performance". Applied Industrial Energy And Environmental Management. <br>
 * [6] Lipska B. "Projektowanie Wentylacji i Klimatyzacji. Podstawy uzdatniania powietrza" Wydawnictwo Politechniki Śląskiej (Gliwice  2014) <br>
 * [7] https://www.engineeringtoolbox.com <br>
 * [8] Stull R. "Wet-Bulb Temperature from Relative Humidity and Air Temperature". Manuscript received 14 July 2011, in final form 28 August 2011 <br>
 * [9] Tsilingiris P.T "Thermophysical and transport properties of humid air at temperature range between 0 and 100oC". Elsevier, Science Direct (September 2007) <br>
 * [10] E.W. Lemmon, R.T. Jacobsen, S.G. Penoncello, D. Friend. Thermodynamic Properties of Air and Mixtures of Nitrogen, Argon, and Oxygen from 60 to 2000 K at Pressures to 2000 MPa. J. Phys. Chem. Ref. Data, Vol. 29, No. 3, (2000) <br>
 * [11] M. Wanielista, R. Kersten,  R. Eaglin. "Hydrology Water Quantity and Quality Control. 2nd ed." (1997) <br>
 * <p><br>
 * <p>
 * REFERENCES DESCRIPTION KEY: <br>
 * [reference no] [value symbology in standard, unit] (equation number) [page] <br>
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
     * Returns moist air saturation vapour pressure, Pa<br>
     * REFERENCE SOURCE: [1] [Ps,Pa] (5,6) [6.2]<br>
     * EQUATION LIMITS: {-100oC,+200oC}<br>
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

        DoubleFunction<Double> satPressureExpression;

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

        BrentSolver solver = new BrentSolver("P_SOLVER", 2, 0);
        solver.setCounterpartPoints(estimatedSatPressure * SOLVER_A_COEF, estimatedSatPressure * SOLVER_B_COEF * n);
        expectedSatPressure = solver.calcForFunction(satPressureExpression);
        return expectedSatPressure;
    }

    public static Pressure saturationPressure(Temperature dryBulbTemp) {
        double saturationPressVal = saturationPressure(dryBulbTemp.getValueOfCelsius());
        return Pressure.ofPascal(saturationPressVal);
    }

    /**
     * Returns moist air saturation vapour pressure, Pa<br>
     *
     * @param x   humidity ratio, kg.wv/kg.da
     * @param RH  relative humidity, %
     * @param Pat atmospheric pressure, Pa
     * @return saturation vapour pressure, Pa
     */
    public static double saturationPressure(double x, double RH, double Pat) {
        return x * Pat / ((WG_RATIO * RH / 100.0) + x * RH / 100.0);
    }

    public static Pressure saturationPressure(HumidityRatio humRatio, RelativeHumidity relHum, Pressure absPressure) {
        double saturationPressVal = saturationPressure(humRatio.getValueOfKilogramPerKilogram(),
                relHum.getValueOfPercent(),
                absPressure.getValueOfPascals());
        return Pressure.ofPascal(saturationPressVal);
    }

    /**
     * Returns moist air dew point temperature based on air temperature <i>ta</i> and relative humidity <i>RH<i/>, oC<br>
     * REFERENCE SOURCE: [1,2,3] [Tdp,Pa] (-) [-]<br>
     * EQUATION LIMITS: {-80oc,+50oC}<br>
     *
     * @param ta air temperature, oC
     * @param RH relative humidity, %
     * @return dew point temperature, oC
     */
    public static double dewPointTemperature(double ta, double RH, double Pat) {
        if (RH >= 100)
            return ta;
        if (RH == 0.0)
            return Double.NEGATIVE_INFINITY;
        // Arden-Buck procedure tdP estimation (used for RH>25)
        double tdpEstimated;
        double a, b, c, d;
        double beta_TRH, b_TRH, c_TRH;
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
        beta_TRH = Math.log(RH / 100.0) + calcAlfaT(ta);
        b_TRH = b - beta_TRH;
        c_TRH = -c * beta_TRH;
        tdpEstimated = 1.0 / a * (b_TRH - Math.sqrt(b_TRH * b_TRH + 2.0 * a * c_TRH));
        if (RH < 25.0) {
            double Ps = saturationPressure(ta);
            double x = humidityRatio(RH, Ps, Pat);
            BrentSolver solver = new BrentSolver("T_SOLVER", 2, 5);
            solver.setCounterpartPoints(tdpEstimated * SOLVER_A_COEF, tdpEstimated * SOLVER_B_COEF);
            if (RH < 1.0)
                solver.setAccuracy(0.0000001);
            double tdpExact = solver.calcForFunction(temp -> {
                double Ps1 = saturationPressure(temp);
                double x1 = maxHumidityRatio(Ps1, Pat);
                return x1 - x;

            });
            return tdpExact;
        }
        return tdpEstimated;
    }

    public static Temperature dewPointTemperature(Temperature dryBulbTemp, RelativeHumidity relHum, Pressure absPressure) {
        double dewPointTempVal = dewPointTemperature(dryBulbTemp.getValueOfCelsius(),
                relHum.getValueOfPercent(),
                absPressure.getValueOfPascals());
        return Temperature.ofCelsius(dewPointTempVal);
    }

    /**
     * Returns moist air wet bulb temperature, oC<br>
     * REFERENCE SOURCE: [1] [Twb,oC] (33) [6.9]<br>
     * EQUATION LIMITS: {-100oC,+200oC}<br>
     *
     * @param ta  air temperature, oC
     * @param RH  air relative humidity, oC
     * @param Pat atmospheric pressure, Pa
     * @return moist air wet bulb temperature, oC
     */
    public static double wetBulbTemperature(double ta, double RH, double Pat) {
        if (RH >= 100.0)
            return ta;
        double estimatedWbt = ta * Math.atan(0.151977 * Math.pow(RH + 8.313659, 0.5))
                + Math.atan(ta + RH) - Math.atan(RH - 1.676331)
                + 0.00391838 * Math.pow(RH, 1.5) * Math.atan(0.023101 * RH)
                - 4.686035;
        double Ps = saturationPressure(ta);
        double x = humidityRatio(RH, Ps, Pat);
        double h = specificEnthalpy(ta, x, Pat);
        BrentSolver solver = new BrentSolver("T_SOLVER", 2, 5);
        solver.setCounterpartPoints(estimatedWbt * SOLVER_A_COEF, estimatedWbt * SOLVER_B_COEF);
        double exactWbt = solver.calcForFunction(temp -> {
            double Ps1 = saturationPressure(temp);
            double x1 = maxHumidityRatio(Ps1, Pat);
            double h1 = specificEnthalpy(temp, x1, Pat);
            double hw1;
            if (temp <= 0.0)
                hw1 = IceEquations.specificEnthalpy(temp);
            else
                hw1 = LiquidWaterEquations.specificEnthalpy(temp);
            return h + (x1 - x) * hw1 - h1;
        });
        return exactWbt;
    }

    public static Temperature wetBulbTemperature(Temperature dryBulbTemp, RelativeHumidity relHum, Pressure absPressure) {
        double wetBulbTempVal = wetBulbTemperature(dryBulbTemp.getValueOfCelsius(),
                relHum.getValueOfPercent(),
                absPressure.getValueOfPascals());
        return Temperature.ofCelsius(wetBulbTempVal);
    }

    /**
     * Returns moist air relative humidity RH from dew point temperature "tdp" and air temperature "ta", %<br>
     * REFERENCE SOURCE: [2,3] [RH,%] (-) [-]<br>
     * EQUATION LIMITS: {-80oc,+50oC}<br>
     *
     * @param ta  air temperature, oC
     * @param tdp air dew point temperature, oC
     * @return relative humidity, %
     */
    public static double relativeHumidity(double tdp, double ta) {
        return Math.exp(calcAlfaT(tdp) - calcAlfaT(ta)) * 100;
    }

    public static RelativeHumidity relativeHumidity(Temperature dewPointTemp, Temperature dryBulbTemp) {
        double relHumVal = relativeHumidity(dewPointTemp.getValueOfCelsius(),
                dryBulbTemp.getValueOfCelsius());
        return RelativeHumidity.ofPercentage(relHumVal);
    }

    /**
     * Returns moist air relative humidity RH from air temperature <i>ta</i> and humidity ratio <i>x</i>, %<br>
     *
     * @param ta  air temperature, oC
     * @param x   relative humidity, kg.wv/kg.da
     * @param Pat atmospheric pressure, Pa
     * @return relative humidity, %
     */
    public static double relativeHumidity(double ta, double x, double Pat) {
        if (x == 0.0)
            return 0.0;
        double Ps = HumidAirEquations.saturationPressure(ta);
        double RH = x * Pat / (WG_RATIO * Ps + x * Ps);
        return RH > 1 ? 100 : RH * 100;
    }

    public static RelativeHumidity relativeHumidity(Temperature dryBulbTemp, HumidityRatio humidityRatio, Pressure absPressure) {
        double relHumVal = relativeHumidity(dryBulbTemp.getValueOfCelsius(),
                humidityRatio.getValueOfKilogramPerKilogram(),
                absPressure.getValueOfPascals());
        return RelativeHumidity.ofPercentage(relHumVal);
    }

    /**
     * Returns moist air humidity ratio<br>
     * REFERENCE SOURCE: [1] [x,kg.wv/kg.da] (23a) [6.10]<br>
     *
     * @param RH  air relative humidity, %
     * @param Ps  air saturation pressure, Pa
     * @param Pat atmospheric pressure, Pa
     * @return humidity ratio, kg.wv/kg.da
     */
    public static double humidityRatio(double RH, double Ps, double Pat) {
        if (RH == 0)
            return 0.0;
        return WG_RATIO * (RH / 100.0 * Ps) / (Pat - (RH / 100.0) * Ps);
    }

    public static HumidityRatio humidityRatio(RelativeHumidity relHum, Pressure saturationPressure, Pressure absPressure) {
        double humRatioVal = humidityRatio(relHum.getValueOfPercent(),
                saturationPressure.getValueOfPascals(),
                absPressure.getValueOfPascals());
        return HumidityRatio.ofKilogramPerKilogram(humRatioVal);
    }

    /**
     * Returns moist air maximum humidity ratio, kg.wv/kg.da<br>
     * REFERENCE SOURCE: [1] [xMax,kg.wv/kg.da] (23) [6.8]<br>
     *
     * @param Ps  air saturation pressure, Pa
     * @param Pat atmospheric pressure, Pa
     * @return humidity ratio, kg.wv/kg.da
     */
    public static double maxHumidityRatio(double Ps, double Pat) {
        return humidityRatio(100.0, Ps, Pat);
    }

    public static HumidityRatio maxHumidityRatio(Pressure saturationPressure, Pressure absPressure) {
        double humRatioVal = maxHumidityRatio(saturationPressure.getValueOfPascals(),
                absPressure.getValueOfPascals());
        return HumidityRatio.ofKilogramPerKilogram(humRatioVal);
    }

    // DYNAMIC VISCOSITY CALCULATION

    /**
     * Returns moist air dynamic viscosity, kg/(m*s) <br>
     * REFERENCE SOURCE: [4] [u,kg/(m*s)] (6.12) [4]<br>
     * EQUATION LIMITS: {no data}<br>
     *
     * @param ta air temperature, oC
     * @param x  humidity ratio, kg.wv/kg.da
     * @return dynamic viscosity, kg/(m*s)
     */
    public static double dynamicViscosity(double ta, double x) {
        double dynVis_Da = DryAirEquations.dynamicViscosity(ta);
        if (x == 0)
            return dynVis_Da;
        double xm = x * 1.61;
        double dynVis_Wv = WaterVapourEquations.dynamicViscosity(ta);
        double fi_AV = Math.pow(1 + Math.pow(dynVis_Da / dynVis_Wv, 0.5) * Math.pow(WaterVapourEquations.WATER_VAPOUR_MOLECULAR_MASS / DryAirEquations.DRY_AIR_MOLECULAR_MASS, 0.25), 2)
                / (2 * Math.sqrt(2) * Math.pow(1 + (DryAirEquations.DRY_AIR_MOLECULAR_MASS / WaterVapourEquations.WATER_VAPOUR_MOLECULAR_MASS), 0.5));
        double fi_VA = Math.pow(1 + Math.pow(dynVis_Wv / dynVis_Da, 0.5) * Math.pow(DryAirEquations.DRY_AIR_MOLECULAR_MASS / WaterVapourEquations.WATER_VAPOUR_MOLECULAR_MASS, 0.25), 2)
                / (2 * Math.sqrt(2) * Math.pow(1 + (WaterVapourEquations.WATER_VAPOUR_MOLECULAR_MASS / DryAirEquations.DRY_AIR_MOLECULAR_MASS), 0.5));
        return (dynVis_Da / (1 + fi_AV * xm)) + (dynVis_Wv / (1 + fi_VA / xm));
    }

    public static DynamicViscosity dynamicViscosity(Temperature dryBulbTemp, HumidityRatio humRatio) {
        double dynVisVal = dynamicViscosity(dryBulbTemp.getValueOfCelsius(),
                humRatio.getValueOfKilogramPerKilogram());
        return DynamicViscosity.ofKiloGramPerMeterSecond(dynVisVal);
    }

    // KINEMATIC VISCOSITY CALCULATION

    /**
     * Returns moist air kinematic viscosity, m^2/s<br>
     *
     * @param ta     air temperature, oC
     * @param x      air humidity ratio, kg.wv/kg.da
     * @param rho_Ma humid air density, kg/m3
     * @return kinematic viscosity, m^2/s
     */
    public static double kinematicViscosity(double ta, double x, double rho_Ma) {
        return x == 0.0
                ? DryAirEquations.dynamicViscosity(ta) / rho_Ma
                : dynamicViscosity(ta, x) / rho_Ma;
    }

    public static KinematicViscosity kinematicViscosity(Temperature dryBulbTemp, HumidityRatio humRatio, Density density) {
        double kinVisVal = kinematicViscosity(dryBulbTemp.getValueOfCelsius(),
                humRatio.getValueOfKilogramPerKilogram(),
                density.getValueOfKilogramPerCubicMeter());
        return KinematicViscosity.ofSquareMeterPerSecond(kinVisVal);
    }

    // THERMAL CONDUCTIVITY CALCULATION

    /**
     * Returns moist air thermal conductivity, W/(m*K)<br>
     * REFERENCE SOURCE: [4] [k,W/(m*K)] (6.15) [5]<br>
     * EQUATION LIMITS: {0.0oC,+200oC},{1atm (1.013bar)}<br>
     *
     * @param ta air temperature, oC
     * @param x  air humidity ratio, kg.wv/kg.da
     * @return air thermal conductivity, W/(m*K)
     */
    public static double thermalConductivity(double ta, double x) {
        double dynVisDa = DryAirEquations.dynamicViscosity(ta);
        double dynVisWv = WaterVapourEquations.dynamicViscosity(ta);
        double k_Da = DryAirEquations.thermalConductivity(ta);
        if (x == 0.0)
            return k_Da;
        double sut_Da = DryAirEquations.DRY_AIR_SUTHERLAND_CONSTANT;
        double sut_Wv = WaterVapourEquations.WATER_VAPOUR_SUTHERLAND_CONSTANT;
        double tk = ta + 273.15;
        double sutAv = 0.733 * Math.sqrt(sut_Da * sut_Wv);
        double k_Wv = WaterVapourEquations.thermalConductivity(ta);
        double xm = 1.61 * x;
        double alfa_AV;
        double alfa_VA;
        double beta_AV;
        double beta_VA;
        double A_AV;
        double A_VA;
        alfa_AV = (dynVisDa / dynVisWv) * Math.pow(WG_RATIO, 0.75) * ((1.0 + sut_Da / tk) / (1.0 + sut_Wv / tk));
        alfa_VA = (dynVisWv / dynVisDa) * Math.pow(WG_RATIO, 0.75) * ((1.0 + sut_Wv / tk) / (1.0 + sut_Da / tk));
        beta_AV = (1.0 + sutAv / tk) / (1.0 + sut_Da / tk);
        beta_VA = (1.0 + sutAv / tk) / (1.0 + sut_Wv / tk);
        A_AV = 0.25 * Math.pow(1.0 + alfa_AV, 2.0) * beta_AV;
        A_VA = 0.25 * Math.pow(1.0 + alfa_VA, 2.0) * beta_VA;
        return (k_Da / (1.0 + A_AV * xm)) + (k_Wv / (1.0 + A_VA / xm));
    }

    public static ThermalConductivity thermalConductivity(Temperature dryBulbTemp, HumidityRatio humRatio) {
        double thermCondVal = thermalConductivity(dryBulbTemp.getValueOfCelsius(),
                humRatio.getValueOfKilogramPerKilogram());
        return ThermalConductivity.ofWattsPerMeterKelvin(thermCondVal);
    }

    // SPECIFIC ENTHALPY CALCULATION

    /**
     * Returns moist air specific enthalpy, kJ/kg.
     * Water fog or ice mist will be included based on provided X value
     * and air temperature.
     * REFERENCE SOURCE: [5] [i,kJ/kg] (1.20) [19]<br>
     *
     * @param ta  air temperature, oC
     * @param x   air humidity ratio, kg.wv/kg.da
     * @param Pat atmospheric pressure, Pa
     * @return humid air specific enthalpy
     */
    public static double specificEnthalpy(double ta, double x, double Pat) {
        double i_Da = DryAirEquations.specificEnthalpy(ta);
        //Case1: no humidity = dry air only
        if (x == 0.0)
            return i_Da;
        //Case2: x <= xMax, unsaturated air
        double Ps = HumidAirEquations.saturationPressure(ta);
        double xMax = maxHumidityRatio(Ps, Pat);
        double i_Wv = WaterVapourEquations.specificEnthalpy(ta) * x;
        if (x <= xMax)
            return i_Da + i_Wv;
        //Case3: x > XMax, saturated air with water or ice fog
        i_Wv = WaterVapourEquations.specificEnthalpy(ta) * xMax;
        double i_Wt = LiquidWaterEquations.specificEnthalpy(ta) * (x - xMax);
        double i_Ice = IceEquations.specificEnthalpy(ta) * (x - xMax);
        return i_Da + i_Wv + i_Wt + i_Ice;
    }

    public static SpecificEnthalpy specificEnthalpy(Temperature dryBulbTemp, HumidityRatio humRatio, Pressure absPressure) {
        double specificEnthalpyVal = specificEnthalpy(dryBulbTemp.getValueOfCelsius(),
                humRatio.getValueOfKilogramPerKilogram(),
                absPressure.getValueOfPascals());
        return SpecificEnthalpy.ofKiloJoulePerKiloGram(specificEnthalpyVal);
    }

    // SPECIFIC HEAT CALCULATION

    /**
     * Returns moist air specific heat at constant pressure, J/(kg*K)<br>
     * REFERENCE SOURCE: [6] [cp,kJ/(kg*K)] (6.10) [4]<br>
     * EQUATION LIMITS: {0.0oC,+200oC},{1atm (0.1bar, 5.0bar)}<br>
     *
     * @param ta air temperature, oC
     * @param x  air humidity ratio, kg.wv/kg.da
     * @return moist air specific heat, kJ/(kg*K)
     */
    public static double specificHeat(double ta, double x) {
        return DryAirEquations.specificHeat(ta) + x * WaterVapourEquations.specificHeat(ta);
    }

    public static SpecificHeat specificHeat(Temperature dryBulbTemp, HumidityRatio humRatio) {
        double specHeatVal = specificHeat(dryBulbTemp.getValueOfCelsius(),
                humRatio.getValueOfKilogramPerKilogram());
        return SpecificHeat.ofKiloJoulePerKiloGramKelvin(specHeatVal);
    }

    // DENSITY CALCULATION

    /**
     * Returns moist air density, kg/m3
     * REFERENCE SOURCE: [1] [xMax,kg.wv/kg.da] (23) [6.8]<br>
     * LIMITS: Important: presence of water mist or ice mist is not taken into account here - TBC
     *
     * @param ta  air temperature, oC
     * @param x   air humidity ratio, kg.wv/kg/da
     * @param Pat atmospheric pressure, Pa
     * @return air density, kg/m3
     */
    public static double density(double ta, double x, double Pat) {
        if (x == 0.0) {
            return DryAirEquations.density(ta, Pat);
        }
        double PatKpa = Pat / 1000.0;
        double tk = ta + 273.15;
        return 1.0 / ((0.2871 * tk * (1.0 + 1.6078 * x)) / PatKpa);
    }

    public static Density density(Temperature dryBulbTemp, HumidityRatio humRatio, Pressure absPressure) {
        double densVal = density(dryBulbTemp.getValueOfCelsius(),
                humRatio.getValueOfKilogramPerKilogram(),
                absPressure.getValueOfPascals());
        return Density.ofKilogramPerCubicMeter(densVal);
    }

    // DRY BULB TEMPERATURE CALCULATION FROM OTHER QUANTITIES

    /**
     * Returns moist air dry bulb temperature based on tdp and RH.
     * REFERENCE SOURCE: [10] [oC] (-) [-]<br>
     *
     * @param tdp air dew point temperature, oC
     * @param RH  air relative humidity, %
     * @return air dry bulb temperature, oC
     */
    public static double dryBulbTemperatureTdpRH(double tdp, double RH, double Pat) {
        if (RH == 0.0) {
            return Double.POSITIVE_INFINITY;
        }
        double taEstimated = (tdp - 112.0 * Math.pow(RH / 100.0, 1.0 / 8.0) + 112.0) / (0.9 * Math.pow(RH / 100.0, 1.0 / 8.0) + 0.1);
        //New instance of BrentSolver is required, to avoid clash between two methods using P_SOLVER
        //at the same time.
        BrentSolver solver = new BrentSolver();
        solver.setCounterpartPoints(taEstimated * SOLVER_A_COEF, taEstimated * SOLVER_B_COEF);
        return solver.calcForFunction(temp -> tdp - dewPointTemperature(temp, RH, Pat));
    }

    public static Temperature dryBulbTemperatureTdpRH(Temperature dewPointTemp, RelativeHumidity relHum, Pressure absPressure) {
        double dryBulbTemp = dryBulbTemperatureTdpRH(dewPointTemp.getValueOfCelsius(),
                relHum.getValueOfPercent(),
                absPressure.getValueOfPascals());
        return Temperature.ofCelsius(dryBulbTemp);
    }

    /**
     * Returns moist air dry bulb temperature, based on x, Rh and Pat, oC.
     *
     * @param x   - humidity ratio, kg.wv/kg.da
     * @param RH  - relative humidity, %
     * @param Pat - atmospheric pressure, Pa
     * @return dry bulb air temperature, oC
     */
    public static double dryBulbTemperatureXRH(double x, double RH, double Pat) {
        BrentSolver solver = new BrentSolver("T_XRH_SOLVER", 2, 5);
        return solver.calcForFunction(tx -> saturationPressure(x, RH, Pat) - saturationPressure(tx));
    }

    public static Temperature dryBulbTemperatureXRH(HumidityRatio humidityRatio, RelativeHumidity relHum, Pressure absPressure) {
        double dryBulbTemp = dryBulbTemperatureXRH(humidityRatio.getValueOfKilogramPerKilogram(),
                relHum.getValueOfPercent(),
                absPressure.getValueOfPascals());
        return Temperature.ofCelsius(dryBulbTemp);
    }

    /**
     * Returns moist air dry bulb temperature, based on ix, x and Pat, oC.
     * LIMITS: ta < 70oC
     *
     * @param ix  air specific enthalpy, kJ/kg
     * @param x   air humidity ratio, kg.wv/kg.da
     * @param Pat atmospheric pressure, Pat
     * @return air dry bulb temperature, oC
     */
    public static double dryBulbTemperatureIX(double ix, double x, double Pat) {
        BrentSolver solver = new BrentSolver("T_IX_SOLVER", 2, 5);
        return solver.calcForFunction(tx -> ix - HumidAirEquations.specificEnthalpy(tx, x, Pat));
    }

    public static Temperature dryBulbTemperatureIX(SpecificEnthalpy specEnthalpy, RelativeHumidity relHum, Pressure absPressure) {
        double dryBulbTemp = dryBulbTemperatureIX(specEnthalpy.getValueOfKiloJoulePerKilogram(),
                relHum.getValueOfPercent(),
                absPressure.getValueOfPascals());
        return Temperature.ofCelsius(dryBulbTemp);
    }

    // OTHER FUNCTIONS

    /**
     * Returns moist air dry bulb temperature based on wbt and RH, oC
     *
     * @param wbt wet bulb temperature, oC
     * @param RH  relative humidity, %
     * @return air dry bulb temperature, oC
     */
    public static double dryBulbTemperatureWbtRH(double wbt, double RH, double Pat) {
        BrentSolver solver = new BrentSolver("T_WbtRH_SOLVER");
        return solver.calcForFunction(temp -> wbt - wetBulbTemperature(temp, RH, Pat));
    }

    public static Temperature dryBulbTemperatureWbtRH(Temperature wetBulbTemperature, RelativeHumidity relHum, Pressure absPressure) {
        double dryBulbTemp = dryBulbTemperatureWbtRH(wetBulbTemperature.getValueOfCelsius(),
                relHum.getValueOfPercent(),
                absPressure.getValueOfPascals());
        return Temperature.ofCelsius(dryBulbTemp);
    }

    /**
     * Returns maximum dry bulb air temperature, for which condition Pat>Ps is met for RH=100% oC
     *
     * @param inPat atmospheric pressure, Pa
     * @return maximum dry bulb air temperature, oC
     */
    public static double dryBulbTemperatureMax(double inPat) {
        double estimatedTa = -237300 * Math.log(0.001638 * inPat) / (1000 * Math.log(0.001638 * inPat) - 17269);
        BrentSolver solver = new BrentSolver();
        solver.setCounterpartPoints(estimatedTa * SOLVER_A_COEF, estimatedTa * SOLVER_B_COEF * 1.5);
        return solver.calcForFunction(ta -> inPat - saturationPressure(ta));
    }

    public static Temperature dryBulbTemperatureMax(Pressure absPressure) {
        double dryBulbTemp = dryBulbTemperatureMax(absPressure.getValueOfPascals());
        return Temperature.ofCelsius(dryBulbTemp);
    }

    // TOOL METHODS

    private static double calcAlfaT(double ta) {
        //Coefficient used for Arden-Buck equation for calculating saturation pressure Ps, Pa
        double b = 0;
        double c = 0;
        double d = 0;
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