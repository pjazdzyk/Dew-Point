package com.synerset.hvacengine.property.fluids;

import com.synerset.unitility.unitsystem.flow.MassFlow;
import com.synerset.unitility.unitsystem.flow.VolumetricFlow;
import com.synerset.unitility.unitsystem.thermodynamic.*;

/**
 * An interface representing a fluid flow, providing access to various properties of the flow.
 *
 * @param <F> The type of fluid associated with this flow.
 */
public interface Flow<F extends Fluid> {

    /**
     * Get the fluid associated with this flow.
     *
     * @return The fluid object.
     */
    F getFluid();

    /**
     * Get the mass flow rate of the fluid flow.
     *
     * @return The mass flow rate in appropriate units.
     */
    MassFlow getMassFlow();

    /**
     * Get the volumetric flow rate of the fluid flow.
     *
     * @return The volumetric flow rate in appropriate units.
     */
    VolumetricFlow getVolFlow();

    /**
     * Get the temperature of the fluid flow.
     *
     * @return The temperature in appropriate units.
     */
    Temperature getTemperature();

    /**
     * Get the pressure of the fluid flow.
     *
     * @return The pressure in appropriate units.
     */
    Pressure getPressure();

    /**
     * Get the density of the fluid flow.
     *
     * @return The density in appropriate units.
     */
    Density getDensity();

    /**
     * Get the specific heat capacity of the fluid flow.
     *
     * @return The specific heat capacity in appropriate units.
     */
    SpecificHeat getSpecificHeat();

    /**
     * Get the specific enthalpy of the fluid flow.
     *
     * @return The specific enthalpy in appropriate units.
     */
    SpecificEnthalpy getSpecificEnthalpy();

    /**
     * Convert the flow properties to a formatted string.
     *
     * @return A formatted string representation of the flow properties.
     */
    String toConsoleOutput();

    /**
     * Compare this flow with another flow of the same type for equality within a specified precision.
     *
     * @param flowOfFluid The flow to compare with.
     * @param epsilon     The precision within which to consider flows equal.
     * @param <K>         The type of fluid associated with the other flow.
     * @return True if the flows are equal within the specified precision, false otherwise.
     */
    default <K extends Fluid> boolean isEqualsWithPrecision(Flow<K> flowOfFluid, double epsilon) {
        if (this == flowOfFluid) return true;
        if (flowOfFluid == null) return false;
        if (this.getClass() != flowOfFluid.getClass()) return false;

        return getFluid().isEqualsWithPrecision(flowOfFluid.getFluid(), epsilon)
                && getMassFlow().isEqualWithPrecision(flowOfFluid.getMassFlow(), epsilon);
    }
}
