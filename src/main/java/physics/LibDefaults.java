package physics;

/**
 * <h3>APP DEFAULT VALUES</h3>
 * <p>List of default values, typically for no argument object initializations.</p><br>
 * <p><span><b>PROPERTY ABBREVIATIONS:<b/></span>
 * <ul style="margin:0px 10px;">
 *     <li>WT - water</li>
 *     <li>WV - water vapour</li>
 *     <li>DA - dry air</li>
 *     <li>MA - moist air</li>
 *     <li>ST - steam</li>
 * </ul><br>
 * </p>
 * <p><span><b>AUTHOR: </span>Piotr Jażdżyk, MScEng</p>
 * <span><b>CONTACT: </span>
 * <a href="https://pl.linkedin.com/in/pjazdzyk/en">LinkedIn<a/> |
 * <a href="mailto:info@synerset.com">e-mail</a> |
 * <a href="http://synerset.com/">www.synerset.com</a>
 * </p><br><br>
 */

public class LibDefaults {

    // Default names
    public static final String DEF_AIR_NAME = "New Air";            // -                    - default moist air instance name
    public static final String DEF_WT_NAME = "New Water";           // -                    - default liquid water instance name
    public static final String DEF_FLOW_NAME = "New Flow";          // -                    - default moist air instance name
    public static final String DEF_PROCESS_NAME = "New Process";    // -                    - default process name

    // Default air core property values
    public final static double DEF_PAT = 101_325;                   // [Pa]                 - Standard atmospheric pressure (physical atmosphere)
    public final static double DEF_AIR_TEMP = 20.0;                 // [oC]                 - default air temperature oC
    public final static double DEF_AIR_RH = 50.0;                   // [%]                  - default relative humidity in %
    public final static double DEF_ASL_ELEV = 0.0;                  // [m]                  - default elevation above the sea level

    // Default air thermophysical properties
    public final static double DEF_WV_CP = 4190.0;                  // [J/(kg*K)]           - Water specific heat
    public final static double DEF_WT_TW = 10.0;                    // [oC]                 - Water temperature (default)
    public final static double DEF_ICE_CP = 2.09;                   // [kJ/(kg*K)]          - Ice specific heat
    public final static double DEF_DA_RHO = 1.2;                    // [kg/m3]              - Dry air default density
    public final static double DEF_DA_CP = 1.005;                   // [kJ/(kg*K)]          - Default dry air specific heat
    public final static double DEF_ST_T = 105.0;                    // [oC]                 - Saturated steam temperature for humidification (default)

    // Heat exchanger defaults
    public final static double DEF_RECOVERY = 0.5;                  // -                    - Default heat recovery exchanger efficiency
    public final static double DEF_BPS_OPEN_LIMIT = 1.0;            // [oC]                 - Safety margin over dew point temperature for calculation of the bypass open temperature

    // Math
    public final static double DEF_MATH_ACCURACY = 0.000001;        // -                    - Default acceptable math accuracy

    // Flow
    public static final double DEF_AIR_FLOW = 0.0;                  // [kg/s] or [m3/s]     - default relative humidity in %
    public static final double DEF_FLUID_FLOW = 0.0;                // [kg/s] or [m3/s]     - default condensate mass flow

    // Heating & cooling
    public static final double DEF_CHW_SUPPLY_TEMP = 6.0;           //oC                    - default chilled water supply temperature
    public static final double DEF_CHW_RETURN_TEMP = 12.0;          //oC                    - default chilled water return temperature

    // Mixing
    public static final double DEF_MIN_FIXED_FLOW = 0.0;            // -                    - minimum fixed inlet flow for mixing


}

