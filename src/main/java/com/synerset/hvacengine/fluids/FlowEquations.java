package com.synerset.hvacengine.fluids;

import com.synerset.hvacengine.common.CommonValidators;
import com.synerset.unitility.unitsystem.flow.MassFlow;
import com.synerset.unitility.unitsystem.flow.VolumetricFlow;
import com.synerset.unitility.unitsystem.humidity.HumidityRatio;
import com.synerset.unitility.unitsystem.thermodynamic.Density;

/**
 * PHYSICS OF FLOW, CALCULATION AND CONVERSION LIBRARY <br>
 * Set of static methods for calculating fluid flow and moist air flow. Based on provided arguments, mass flow or volumetric flow
 * of moist air or dry air can be determined.<br>
 * Abbreviations <br>
 * Ha - humid air
 * Da - dry air
 *
 * @author Piotr Jażdżyk, MScEng
 */

public final class FlowEquations {

    private FlowEquations() {
    }

    // GENERAL FLOW CONVERSION

    /**
     * Returns volumetric flow rate in m3/s based on provided fluid instance and its mass flow rate.
     *
     * @param density  fluid density in kg/m3
     * @param massFlow mass flow rate in kg/s
     * @return volumetric flow rate in m3/s
     */
    public static double massFlowToVolFlow(double density, double massFlow) {
        return massFlow / density;
    }

    public static VolumetricFlow massFlowToVolFlow(Density density, MassFlow massFlow) {
        CommonValidators.requireNotNull(density);
        CommonValidators.requireNotNull(massFlow);
        double volFlowVal = massFlowToVolFlow(density.getInKilogramsPerCubicMeters(), massFlow.getInKilogramsPerSecond());
        return VolumetricFlow.ofCubicMetersPerSecond(volFlowVal);
    }

    /**
     * Returns mass flow rate in kg/s based on provided fluid instance and its volumetric flow rate.
     *
     * @param density fluid density in kg/m3
     * @param volFlow volumetric flow rate in m3/s
     * @return mass flow rate in kg/s
     */
    public static double volFlowToMassFlow(double density, double volFlow) {
        return volFlow * density;
    }

    public static MassFlow volFlowToMassFlow(Density density, VolumetricFlow volFlow) {
        CommonValidators.requireNotNull(density);
        CommonValidators.requireNotNull(volFlow);
        double massFlowVal = volFlowToMassFlow(density.getInKilogramsPerCubicMeters(), volFlow.getInCubicMetersPerSecond());
        return MassFlow.ofKilogramsPerSecond(massFlowVal);
    }

    // MOIST AIR SPECIFIC FLOW RATE CONVERSION

    /**
     * Returns dry air mass flow based on moist air mass flow and humidity ratio from provided air instance.
     * It is used to convert moist air flow rate to dry air flow, used further in phsychrometric calculations.
     *
     * @param humidityRatio moist air humidity ratio in kg.wv/kg.da
     * @param massFlowHa    moist air flow in kg/s
     * @return dry air flow in kg/s
     */
    public static double massFlowHaToMassFlowDa(double humidityRatio, double massFlowHa) {
        return massFlowHa / (1.0 + humidityRatio);
    }

    public static MassFlow massFlowHaToMassFlowDa(HumidityRatio humRatio, MassFlow massFlowHa) {
        CommonValidators.requireNotNull(humRatio);
        CommonValidators.requireNotNull(massFlowHa);
        double massFlowVal = massFlowHaToMassFlowDa(humRatio.getInKilogramPerKilogram(),
                massFlowHa.getInKilogramsPerSecond());
        return MassFlow.ofKilogramsPerSecond(massFlowVal);
    }

    /**
     * Returns moist air mass flow, based on dry air mass flow and humidity ratio from provided air instance.
     * It is used to get back moist air flow after psychrometrics calculation.
     *
     * @param humidityRatio moist air humidity ratio in kg/m3
     * @param massFlowDa    dry air mass flow in kg/s
     * @return moist air mass flow in kg/s
     */
    public static double massFlowDaToMassFlowHa(double humidityRatio, double massFlowDa) {
        return massFlowDa * (1.0 + humidityRatio);
    }

    public static MassFlow massFlowDaToMassFlowHa(HumidityRatio humRatio, MassFlow massFlowDa) {
        CommonValidators.requireNotNull(humRatio);
        CommonValidators.requireNotNull(massFlowDa);
        double massFlowVal = massFlowDaToMassFlowHa(humRatio.getInKilogramPerKilogram(),
                massFlowDa.getInKilogramsPerSecond());
        return MassFlow.ofKilogramsPerSecond(massFlowVal);
    }

    /**
     * Returns moist air volumetric flow in m3/s from dry air mass flow in kg/s.
     *
     * @param densityOfMoistAir density of the moist air in kg/m3
     * @param humidityRatio     moist air humidity ratio in kg.wv/kg.da
     * @param massFlowDa        dry air mass flow in kg/s
     * @return moist air volumetric flow in m3/s
     */
    public static double massFlowDaToVolFlowHa(double densityOfMoistAir, double humidityRatio, double massFlowDa) {
        double massFlowMa = massFlowDaToMassFlowHa(humidityRatio, massFlowDa);
        return massFlowToVolFlow(densityOfMoistAir, massFlowMa);
    }

    public static MassFlow massFlowDaToVolFlowHa(Density density, HumidityRatio humRatio, MassFlow massFlowDa) {
        CommonValidators.requireNotNull(density);
        CommonValidators.requireNotNull(humRatio);
        CommonValidators.requireNotNull(massFlowDa);
        double massFlowVal = massFlowDaToVolFlowHa(density.getInKilogramsPerCubicMeters(),
                humRatio.getInKilogramPerKilogram(),
                massFlowDa.getInKilogramsPerSecond());
        return MassFlow.ofKilogramsPerSecond(massFlowVal);
    }

    /**
     * Returns dry air mass flow in kg/s from moist air volumetric flow in m3/s.
     *
     * @param densityOfMoistAir density of moist air in kg/m3
     * @param humidityRatio     moist air humidity ratio in kg.wv/kg.da
     * @param volFlowHa         moist air volumetric flow in m3/s
     * @return moist air volumetric flow in m3/s
     */
    public static double volFlowHaToMassFlowDa(double densityOfMoistAir, double humidityRatio, double volFlowHa) {
        double massFlowMa = volFlowToMassFlow(densityOfMoistAir, volFlowHa);
        return massFlowHaToMassFlowDa(humidityRatio, massFlowMa);
    }

    public static MassFlow volFlowHaToMassFlowDa(Density density, HumidityRatio humRatio, VolumetricFlow volFlowHa) {
        CommonValidators.requireNotNull(density);
        CommonValidators.requireNotNull(humRatio);
        CommonValidators.requireNotNull(volFlowHa);
        double massFlowVal = volFlowHaToMassFlowDa(density.getInKilogramsPerCubicMeters(),
                humRatio.getInKilogramPerKilogram(),
                volFlowHa.getInCubicMetersPerSecond());
        return MassFlow.ofKilogramsPerSecond(massFlowVal);
    }

}