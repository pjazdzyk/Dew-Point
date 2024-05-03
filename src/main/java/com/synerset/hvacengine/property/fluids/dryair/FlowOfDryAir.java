package com.synerset.hvacengine.property.fluids.dryair;

import com.synerset.hvacengine.common.Defaults;
import com.synerset.hvacengine.common.validation.CommonValidators;
import com.synerset.hvacengine.property.fluids.Flow;
import com.synerset.hvacengine.property.fluids.FlowEquations;
import com.synerset.unitility.unitsystem.flow.MassFlow;
import com.synerset.unitility.unitsystem.flow.VolumetricFlow;
import com.synerset.unitility.unitsystem.thermodynamic.*;

import java.util.Objects;

/**
 * A class representing the flow of dry air, providing access to various properties of the flow.
 */
public class FlowOfDryAir implements Flow<DryAir> {

    private static final MassFlow MASS_FLOW_MIN_LIMIT = MassFlow.ofKilogramsPerSecond(0);
    private static final MassFlow MASS_FLOW_MAX_LIMIT = MassFlow.ofKilogramsPerSecond(5E9);
    private final DryAir dryAir;
    private final MassFlow massFlow;
    private final VolumetricFlow volFlow;

    /**
     * Construct a `FlowOfDryAir` instance with the specified dry air and mass flow rate.
     *
     * @param dryAir   The dry air associated with the flow.
     * @param massFlow The mass flow rate of dry air in appropriate units.
     */
    public FlowOfDryAir(DryAir dryAir, MassFlow massFlow) {
        CommonValidators.requireNotNull(dryAir);
        CommonValidators.requireNotNull(massFlow);
        CommonValidators.requireBetweenBoundsInclusive(massFlow, MASS_FLOW_MIN_LIMIT, MASS_FLOW_MAX_LIMIT);
        this.dryAir = dryAir;
        this.massFlow = massFlow;
        this.volFlow = FlowEquations.massFlowToVolFlow(dryAir.getDensity(), massFlow).toCubicMetersPerHour();
    }

    @Override
    public DryAir getFluid() {
        return dryAir;
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
        return dryAir.getTemperature();
    }

    @Override
    public Pressure getPressure() {
        return dryAir.getPressure();
    }

    @Override
    public Density getDensity() {
        return dryAir.getDensity();
    }

    @Override
    public SpecificHeat getSpecificHeat() {
        return dryAir.getSpecificHeat();
    }

    @Override
    public SpecificEnthalpy getSpecificEnthalpy() {
        return dryAir.getSpecificEnthalpy();
    }

    @Override
    public String toConsoleOutput() {
        String separator = " | ";
        String end = "\n\t";
        int digits = 3;
        return "FlowOfDryAir:" + end +
                massFlow.toEngineeringFormat("G_da", digits) + separator +
                massFlow.toKiloGramPerHour().toEngineeringFormat("G_da", digits) + separator +
                volFlow.toEngineeringFormat("V_da", digits) + separator +
                volFlow.toCubicMetersPerHour().toEngineeringFormat("V_da", digits) + end +
                dryAir.toConsoleOutput();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FlowOfDryAir that = (FlowOfDryAir) o;
        return Objects.equals(dryAir, that.dryAir) && Objects.equals(massFlow, that.massFlow);
    }

    @Override
    public int hashCode() {
        return Objects.hash(dryAir, massFlow);
    }

    @Override
    public String toString() {
        return "FlowOfDryAir{" +
                "dryAir=" + dryAir +
                ", massFlow=" + massFlow +
                ", volFlow=" + volFlow +
                '}';
    }

    // Class factory methods
    // Class factory methods

    /**
     * Create a new `FlowOfDryAir` instance with the specified mass flow rate.
     *
     * @param massFlow The mass flow rate of dry air in appropriate units.
     * @return A new `FlowOfDryAir` instance.
     */
    public FlowOfDryAir withMassFlow(MassFlow massFlow) {
        return FlowOfDryAir.of(dryAir, massFlow);
    }

    /**
     * Create a new `FlowOfDryAir` instance with the specified volumetric flow rate.
     *
     * @param volFlow The volumetric flow rate of dry air in appropriate units.
     * @return A new `FlowOfDryAir` instance.
     */
    public FlowOfDryAir withVolFlow(VolumetricFlow volFlow) {
        return FlowOfDryAir.of(dryAir, volFlow);
    }

    /**
     * Create a new `FlowOfDryAir` instance with the specified dry air.
     *
     * @param dryAir The dry air associated with the flow.
     * @return A new `FlowOfDryAir` instance.
     */
    public FlowOfDryAir withHumidAir(DryAir dryAir) {
        return FlowOfDryAir.of(dryAir, massFlow);
    }

    // Static factory methods

    /**
     * Create a new `FlowOfDryAir` instance with the specified dry air and mass flow rate.
     *
     * @param dryAir   The dry air associated with the flow.
     * @param massFlow The mass flow rate of dry air in appropriate units.
     * @return A new `FlowOfDryAir` instance.
     */
    public static FlowOfDryAir of(DryAir dryAir, MassFlow massFlow) {
        return new FlowOfDryAir(dryAir, massFlow);
    }

    /**
     * Create a new `FlowOfDryAir` instance with the specified dry air and volumetric flow rate.
     *
     * @param dryAir  The dry air associated with the flow.
     * @param volFlow The volumetric flow rate of dry air in appropriate units.
     * @return A new `FlowOfDryAir` instance.
     */
    public static FlowOfDryAir of(DryAir dryAir, VolumetricFlow volFlow) {
        CommonValidators.requireNotNull(volFlow);
        MassFlow massFlow = FlowEquations.volFlowToMassFlow(dryAir.getDensity(), volFlow);
        return new FlowOfDryAir(dryAir, massFlow);
    }

    /**
     * Create a new `FlowOfDryAir` instance with specified absolute pressure, temperature, and volumetric flow rate.
     *
     * @param absPressure The absolute pressure of dry air in pascals (Pa).
     * @param temperature The temperature of dry air in degrees Celsius (°C).
     * @param m3hVolFlow  The volumetric flow rate of dry air in cubic meters per hour (m³/h).
     * @return A new `FlowOfDryAir` instance.
     */
    public static FlowOfDryAir ofValues(double absPressure, double temperature, double m3hVolFlow) {
        Pressure absPress = Pressure.ofPascal(absPressure);
        Temperature temp = Temperature.ofCelsius(temperature);
        VolumetricFlow volFlow = VolumetricFlow.ofCubicMetersPerHour(m3hVolFlow);
        DryAir dryAir = DryAir.of(absPress, temp);
        return of(dryAir, volFlow);
    }

    /**
     * Create a new `FlowOfDryAir` instance with specified temperature and volumetric flow rate at standard atmosphere pressure.
     *
     * @param temperature The temperature of dry air in degrees Celsius (°C).
     * @param m3hVolFlow  The volumetric flow rate of dry air in cubic meters per hour (m³/h).
     * @return A new `FlowOfDryAir` instance.
     */
    public static FlowOfDryAir ofValues(double temperature, double m3hVolFlow) {
        double pressure = Defaults.STANDARD_ATMOSPHERE.getInPascals();
        return ofValues(pressure, temperature, m3hVolFlow);
    }

}