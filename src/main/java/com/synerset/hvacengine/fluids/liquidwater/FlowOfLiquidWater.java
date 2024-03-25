package com.synerset.hvacengine.fluids.liquidwater;


import com.synerset.hvacengine.common.Validators;
import com.synerset.hvacengine.fluids.Flow;
import com.synerset.hvacengine.fluids.FlowEquations;
import com.synerset.unitility.unitsystem.flow.MassFlow;
import com.synerset.unitility.unitsystem.flow.VolumetricFlow;
import com.synerset.unitility.unitsystem.thermodynamic.*;

import java.util.Objects;

/**
 * Represents a flow of liquid water state with associated properties.
 * This class implements the Flow interface for LiquidWater.
 */
public class FlowOfLiquidWater implements Flow<LiquidWater> {

    private static final MassFlow MASS_FLOW_MIN_LIMIT = MassFlow.ofKilogramsPerSecond(0);
    private static final MassFlow MASS_FLOW_MAX_LIMIT = MassFlow.ofKilogramsPerSecond(5E9);
    private final LiquidWater liquidWater;
    private final MassFlow massFlow;
    private final VolumetricFlow volFlow;

    /**
     * Constructs a FlowOfLiquidWater object with the specified liquid water and mass flow rate.
     *
     * @param liquidWater The liquid water.
     * @param massFlow    The mass flow rate.
     * @throws IllegalArgumentException If either liquidWater or massFlow is null or if massFlow is out of bounds.
     */
    public FlowOfLiquidWater(LiquidWater liquidWater, MassFlow massFlow) {
        Validators.requireNotNull(liquidWater);
        Validators.requireNotNull(massFlow);
        Validators.requireBetweenBoundsInclusive(massFlow, MASS_FLOW_MIN_LIMIT, MASS_FLOW_MAX_LIMIT);
        this.liquidWater = liquidWater;
        this.massFlow = massFlow;
        this.volFlow = FlowEquations.massFlowToVolFlow(liquidWater.getDensity(), massFlow);
    }

    @Override
    public LiquidWater getFluid() {
        return liquidWater;
    }

    @Override
    public MassFlow getMassFlow() {
        return massFlow;
    }

    @Override
    public VolumetricFlow getVolFlow() {
        return volFlow;
    }

    @Override
    public Temperature getTemperature() {
        return liquidWater.getTemperature();
    }

    @Override
    public Pressure getPressure() {
        return liquidWater.getPressure();
    }

    @Override
    public Density getDensity() {
        return liquidWater.getDensity();
    }

    @Override
    public SpecificHeat getSpecificHeat() {
        return liquidWater.getSpecificHeat();
    }

    @Override
    public SpecificEnthalpy getSpecificEnthalpy() {
        return liquidWater.getSpecificEnthalpy();
    }

    /**
     * Returns a formatted string representation for console output of the FlowOfLiquidWater object.
     *
     * @return A formatted string representation.
     */
    @Override
    public String toConsoleOutput() {
        String separator = " | ";
        String end = "\n\t";
        int digits = 3;
        return "FlowOfLiquidWater:" + end +
               massFlow.toEngineeringFormat("G_w", digits) + separator +
               massFlow.toKiloGramPerHour().toEngineeringFormat("G_w", digits) + separator +
               volFlow.toEngineeringFormat("V_w", digits) + separator +
               volFlow.toCubicMetersPerHour().toEngineeringFormat("V_w", digits) + end +
               liquidWater.toConsoleOutput();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FlowOfLiquidWater that = (FlowOfLiquidWater) o;
        return Objects.equals(liquidWater, that.liquidWater) && Objects.equals(massFlow, that.massFlow);
    }

    @Override
    public int hashCode() {
        return Objects.hash(liquidWater, massFlow);
    }

    @Override
    public String toString() {
        return "FlowOfWater{" +
               "liquidWater=" + liquidWater +
               ", massFlow=" + massFlow +
               ", volFlow=" + volFlow +
               '}';
    }

    // Class factory methods

    /**
     * Creates a new FlowOfLiquidWater object with the specified mass flow rate.
     *
     * @param massFlow The new mass flow rate.
     * @return A new FlowOfLiquidWater object with the specified mass flow rate.
     */
    public FlowOfLiquidWater withMassFlow(MassFlow massFlow) {
        return FlowOfLiquidWater.of(liquidWater, massFlow);
    }

    /**
     * Creates a new FlowOfLiquidWater object with the specified volumetric flow rate.
     *
     * @param volFlow The new volumetric flow rate.
     * @return A new FlowOfLiquidWater object with the specified volumetric flow rate.
     */
    public FlowOfLiquidWater withVolFlow(VolumetricFlow volFlow) {
        return FlowOfLiquidWater.of(liquidWater, volFlow);
    }

    /**
     * Creates a new FlowOfLiquidWater object with the specified liquid water.
     *
     * @param liquidWater The new liquid water.
     * @return A new FlowOfLiquidWater object with the specified liquid water.
     */
    public FlowOfLiquidWater withLiquidWater(LiquidWater liquidWater) {
        return FlowOfLiquidWater.of(liquidWater, massFlow);
    }

    public FlowOfLiquidWater withTemperature(Temperature temperature) {
        LiquidWater newLiquidWater = LiquidWater.of(temperature);
        return FlowOfLiquidWater.of(newLiquidWater, massFlow);
    }

    // Static factory methods

    /**
     * Static factory method to create a new FlowOfLiquidWater object with the specified liquid water and mass flow rate.
     *
     * @param liquidWater The liquid water.
     * @param massFlow    The mass flow rate.
     * @return A new FlowOfLiquidWater object.
     */
    public static FlowOfLiquidWater of(LiquidWater liquidWater, MassFlow massFlow) {
        return new FlowOfLiquidWater(liquidWater, massFlow);
    }

    /**
     * Static factory method to create a new FlowOfLiquidWater object with the specified liquid water and volumetric flow rate.
     *
     * @param liquidWater The liquid water.
     * @param volFlow     The volumetric flow rate.
     * @return A new FlowOfLiquidWater object.
     * @throws IllegalArgumentException If either liquidWater or volFlow is null.
     */
    public static FlowOfLiquidWater of(LiquidWater liquidWater, VolumetricFlow volFlow) {
        Validators.requireNotNull(liquidWater);
        Validators.requireNotNull(volFlow);
        MassFlow massFlow = FlowEquations.volFlowToMassFlow(liquidWater.getDensity(), volFlow);
        return new FlowOfLiquidWater(liquidWater, massFlow);
    }

    public static FlowOfLiquidWater zeroFlow(Temperature temperature) {
        Validators.requireNotNull(temperature);
        LiquidWater liquidWater = LiquidWater.of(temperature);
        return new FlowOfLiquidWater(liquidWater, MassFlow.ofKilogramsPerSecond(0));
    }

}