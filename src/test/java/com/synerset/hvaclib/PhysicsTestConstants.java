package com.synerset.hvaclib;

public interface PhysicsTestConstants {
    double MATH_ACCURACY = 1.0E-11;
    double MEDIUM_MATH_ACCURACY = 1.0E-8;
    double LIMITED_MATH_ACCURACY = 1.0E-6;

    double LOW_MATH_ACCURACY = 1.0E-4;
    double PS_LOW_TEMP_ACCURACY = 0.03;
    double PS_MED_TEMP_ACCURACY = 0.20;
    double PS_HIGH_TEMP_ACCURACY = 1.90;
    double TDP_ACCURACY = 0.04;
    double WBT_LOW_TEMP_ACCURACY = 0.007;
    double WBT_HIGH_TEMP_ACCURACY = 0.05;
    double DYN_VIS_ACCURACY = 0.00000007;
    double RHO_ACCURACY = 0.004;
    double CP_DA_ACCURACY = 0.00047;
    double CP_WV_ACCURACY = 0.025;
    double K_LOW_TEMP_ACCURACY = 0.0006;
    double K_HIGH_TEMP_ACCURACY = 0.0013;
    double TH_DIFF_ACCURACY = 0.021E-5;
    double PRANDTL_ACCURACY = 0.009;
    double P_PHYS = 100_000.0;
    double P_ATM = 101_325; // Pa
    double P_TEST = 98700; // Pa
    double TYPICAL_AVERAGE_COIL_WALL_TEMP = 11.5;

}