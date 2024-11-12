package com.synerset.hvacengine.property.fluids.humidair;

import com.synerset.hvacengine.common.validation.CommonValidators;
import com.synerset.hvacengine.property.fluids.Flow;
import com.synerset.hvacengine.property.fluids.FlowEquations;
import com.synerset.hvacengine.property.fluids.dryair.DryAir;
import com.synerset.hvacengine.property.fluids.dryair.FlowOfDryAir;
import com.synerset.unitility.unitsystem.flow.MassFlow;
import com.synerset.unitility.unitsystem.flow.VolumetricFlow;
import com.synerset.unitility.unitsystem.humidity.HumidityRatio;
import com.synerset.unitility.unitsystem.humidity.RelativeHumidity;
import com.synerset.unitility.unitsystem.thermodynamic.*;

import java.util.Objects;

/**
 * A class representing the flow of humid air, providing access to various properties of the flow.
 */
public class FlowOfHumidAir implements Flow<HumidAir> {

    public static final MassFlow MASS_FLOW_MIN_LIMIT = MassFlow.ofKilogramsPerSecond(0);
    public static final MassFlow MASS_FLOW_MAX_LIMIT = MassFlow.ofKilogramsPerSecond(5E9);
    private final HumidAir humidAir;
    private final MassFlow massFlow;
    private final VolumetricFlow volFlow;
    private final FlowOfDryAir flowOfDryAir;

    /**
     * Construct a `FlowOfHumidAir` instance with the specified humid air and mass flow rate.
     *
     * @param humidAir   The humid air associated with the flow.
     * @param massFlowHa The mass flow rate of humid air in appropriate units.
     */
    public FlowOfHumidAir(HumidAir humidAir, MassFlow massFlowHa) {
        CommonValidators.requireNotNull(humidAir);
        CommonValidators.requireNotNull(massFlowHa);
        CommonValidators.requireBetweenBoundsInclusive(massFlowHa, MASS_FLOW_MIN_LIMIT, MASS_FLOW_MAX_LIMIT);
        this.humidAir = humidAir;
        this.massFlow = massFlowHa;
        this.volFlow = FlowEquations.volFlowFromMassFlow(humidAir.getDensity(), massFlowHa);
        MassFlow massFlowDa = FlowEquations.massFlowDaFromMassFlowHa(humidAir.getHumidityRatio(), massFlowHa);
        this.flowOfDryAir = FlowOfDryAir.of(DryAir.of(humidAir.getTemperature()), massFlowDa);
    }

    // Primary properties
    @Override
    public HumidAir getFluid() {
        return humidAir;
    }

    @Override
    public MassFlow getMassFlow() {
        return massFlow;
    }

    @Override
    public VolumetricFlow getVolFlow() {
        return volFlow;
    }

    public MassFlow getDryAirMassFlow() {
        return flowOfDryAir.getMassFlow();
    }

    public VolumetricFlow getDryAirVolFlow() {
        return flowOfDryAir.getVolFlow();
    }

    // Secondary properties
    @Override
    public Temperature getTemperature() {
        return humidAir.getTemperature();
    }

    @Override
    public Pressure getPressure() {
        return humidAir.getPressure();
    }

    @Override
    public Density getDensity() {
        return humidAir.getDensity();
    }

    @Override
    public SpecificHeat getSpecificHeat() {
        return humidAir.getSpecificHeat();
    }

    @Override
    public SpecificEnthalpy getSpecificEnthalpy() {
        return humidAir.getSpecificEnthalpy();
    }

    public FlowOfDryAir getFlowOfDryAir() {
        return flowOfDryAir;
    }

    public HumidityRatio getHumidityRatio() {
        return humidAir.getHumidityRatio();
    }

    public HumidityRatio getMaxHumidityRatio() {
        return humidAir.getMaxHumidityRatio();
    }

    public RelativeHumidity getRelativeHumidity() {
        return humidAir.getRelativeHumidity();
    }

    public Pressure getSaturationPressure() {
        return humidAir.getSaturationPressure();
    }

    // Class factory methods

    /**
     * Create a new `FlowOfHumidAir` instance with the specified mass flow rate.
     *
     * @param massFlow The mass flow rate of humid air in appropriate units.
     * @return A new `FlowOfHumidAir` instance.
     */
    public FlowOfHumidAir withMassFlow(MassFlow massFlow) {
        return FlowOfHumidAir.of(humidAir, massFlow);
    }

    /**
     * Create a new `FlowOfHumidAir` instance with the specified volumetric flow rate.
     *
     * @param volFlow The volumetric flow rate of humid air in appropriate units.
     * @return A new `FlowOfHumidAir` instance.
     */
    public FlowOfHumidAir withVolFlow(VolumetricFlow volFlow) {
        return FlowOfHumidAir.of(humidAir, volFlow);
    }

    /**
     * Create a new `FlowOfHumidAir` instance with the specified dry air flow rate.
     *
     * @param dryAirFlow The dry air flow component of humid air in appropriate units.
     * @return A new `FlowOfHumidAir` instance.
     */
    public FlowOfHumidAir withDryAirFlow(MassFlow dryAirFlow) {
        return FlowOfHumidAir.ofDryAirMassFlow(humidAir, dryAirFlow);
    }

    /**
     * Create a new `FlowOfHumidAir` instance with the specified humid air.
     *
     * @param humidAir The humid air associated with the flow.
     * @return A new `FlowOfHumidAir` instance.
     */
    public FlowOfHumidAir withHumidAirFixedDryAirMassFlow(HumidAir humidAir) {
        return FlowOfHumidAir.ofDryAirMassFlow(humidAir, flowOfDryAir.getMassFlow());
    }

    public FlowOfHumidAir withTemperature(Temperature temperature) {
        HumidAir newHumidAir = humidAir.withDryBulbTemperature(temperature);
        return withHumidAirFixedDryAirMassFlow(newHumidAir);
    }

    @Override
    public String toConsoleOutput() {
        String separator = " | ";
        String end = "\n\t";
        int digits = 3;
        return "FlowOfHumidAir:" + end +
                massFlow.toEngineeringFormat("G", digits) + separator +
                massFlow.toKiloGramPerHour().toEngineeringFormat("G", digits) + separator +
                volFlow.toEngineeringFormat("V", digits) + separator +
                volFlow.toCubicMetersPerHour().toEngineeringFormat("V", digits) + end +
                getDryAirMassFlow().toEngineeringFormat("G_da", digits) + separator +
                getDryAirMassFlow().toKiloGramPerHour().toEngineeringFormat("G_da", digits) + end +
                humidAir.toConsoleOutput();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FlowOfHumidAir that = (FlowOfHumidAir) o;
        return Objects.equals(humidAir, that.humidAir) && Objects.equals(massFlow, that.massFlow);
    }

    @Override
    public int hashCode() {
        return Objects.hash(humidAir, massFlow);
    }

    @Override
    public String toString() {
        return "FlowOfHumidAir{" +
                "humidAir=" + humidAir +
                ", massFlow=" + massFlow +
                ", volFlow=" + volFlow +
                ", flowOfDryAir=" + flowOfDryAir +
                '}';
    }

    // Static factory methods

    /**
     * Create a new `FlowOfHumidAir` instance with the specified humid air and mass flow rate.
     *
     * @param humidAir   The humid air associated with the flow.
     * @param massFlowHa The mass flow rate of humid air in appropriate units.
     * @return A new `FlowOfHumidAir` instance.
     */
    public static FlowOfHumidAir of(HumidAir humidAir, MassFlow massFlowHa) {
        return new FlowOfHumidAir(humidAir, massFlowHa);
    }

    /**
     * Create a new `FlowOfHumidAir` instance with the specified humid air and volumetric flow rate.
     *
     * @param humidAir  The humid air associated with the flow.
     * @param volFlowHa The volumetric flow rate of humid air in appropriate units.
     * @return A new `FlowOfHumidAir` instance.
     */
    public static FlowOfHumidAir of(HumidAir humidAir, VolumetricFlow volFlowHa) {
        CommonValidators.requireNotNull(humidAir);
        CommonValidators.requireNotNull(volFlowHa);
        return new FlowOfHumidAir(humidAir, FlowEquations.massFlowFromVolFlow(humidAir.getDensity(), volFlowHa));
    }

    /**
     * Create a new `FlowOfHumidAir` instance with specified dry air mass flow rate.
     *
     * @param humidAir   The humid air associated with the flow.
     * @param massFlowDa The mass flow rate of dry air in appropriate units.
     * @return A new `FlowOfHumidAir` instance.
     */
    public static FlowOfHumidAir ofDryAirMassFlow(HumidAir humidAir, MassFlow massFlowDa) {
        CommonValidators.requireNotNull(humidAir);
        CommonValidators.requireNotNull(massFlowDa);
        CommonValidators.requireBetweenBoundsInclusive(massFlowDa, MASS_FLOW_MIN_LIMIT, MASS_FLOW_MAX_LIMIT);
        HumidityRatio humRatio = humidAir.getHumidityRatio();
        MassFlow humidAirMassFlow = FlowEquations.massFlowHaFromMassFlowDa(humRatio, massFlowDa);
        return FlowOfHumidAir.of(humidAir, humidAirMassFlow);
    }

    /**
     * Create a new `FlowOfHumidAir` instance with specified absolute pressure, dry bulb temperature, relative humidity,
     * and volumetric flow rate.
     *
     * @param absPressure The absolute pressure of humid air in pascals (Pa).
     * @param dryBulbTemp The dry bulb temperature of humid air in degrees Celsius (°C).
     * @param relHum      The relative humidity of humid air in percentage.
     * @param m3hVolFlow  The volumetric flow rate of humid air in cubic meters per hour (m³/h).
     * @return A new `FlowOfHumidAir` instance.
     */
    public static FlowOfHumidAir ofValues(double absPressure, double dryBulbTemp, double relHum, double m3hVolFlow) {
        Pressure pAbs = Pressure.ofPascal(absPressure);
        Temperature dryBulbTx = Temperature.ofCelsius(dryBulbTemp);
        RelativeHumidity rh = RelativeHumidity.ofPercentage(relHum);
        VolumetricFlow volFlow = VolumetricFlow.ofCubicMetersPerHour(m3hVolFlow);
        HumidAir humidAir = HumidAir.of(pAbs, dryBulbTx, rh);
        return of(humidAir, volFlow);
    }

    /**
     * Create a new `FlowOfHumidAir` instance with default absolute pressure: 101325 Pa, dry bulb temperature,
     * relative humidity, and volumetric flow rate.
     *
     * @param dryBulbTemp The dry bulb temperature of humid air in degrees Celsius (°C).
     * @param relHum      The relative humidity of humid air in percentage.
     * @param m3hVolFlow  The volumetric flow rate of humid air in cubic meters per hour (m³/h).
     * @return A new `FlowOfHumidAir` instance.
     */
    public static FlowOfHumidAir ofValues(double dryBulbTemp, double relHum, double m3hVolFlow) {
        double pressure = Pressure.STANDARD_ATMOSPHERE.getInPascals();
        return ofValues(pressure, dryBulbTemp, relHum, m3hVolFlow);
    }

}