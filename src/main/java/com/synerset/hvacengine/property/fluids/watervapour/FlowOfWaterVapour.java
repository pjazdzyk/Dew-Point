package com.synerset.hvacengine.property.fluids.watervapour;

import com.synerset.hvacengine.common.validation.CommonValidators;
import com.synerset.hvacengine.property.fluids.Flow;
import com.synerset.hvacengine.property.fluids.FlowEquations;
import com.synerset.unitility.unitsystem.flow.MassFlow;
import com.synerset.unitility.unitsystem.flow.VolumetricFlow;
import com.synerset.unitility.unitsystem.thermodynamic.*;

import java.util.Objects;

public class FlowOfWaterVapour implements Flow<WaterVapour> {

    private static final MassFlow MASS_FLOW_MIN_LIMIT = MassFlow.ofKilogramsPerSecond(0);
    private static final MassFlow MASS_FLOW_MAX_LIMIT = MassFlow.ofKilogramsPerSecond(5E9);
    private final WaterVapour waterVapour;
    private final MassFlow massFlow;
    private final VolumetricFlow volFlow;

    public FlowOfWaterVapour(WaterVapour waterVapour, MassFlow massFlow) {
        CommonValidators.requireNotNull(waterVapour);
        CommonValidators.requireNotNull(massFlow);
        CommonValidators.requireBetweenBoundsInclusive(massFlow, MASS_FLOW_MIN_LIMIT, MASS_FLOW_MAX_LIMIT);
        this.waterVapour = waterVapour;
        this.massFlow = massFlow;
        this.volFlow = FlowEquations.volFlowFromMassFlow(waterVapour.getDensity(), massFlow);
    }

    @Override
    public WaterVapour getFluid() {
        return waterVapour;
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
        return waterVapour.getTemperature();
    }

    @Override
    public Pressure getPressure() {
        return waterVapour.getPressure();
    }

    @Override
    public Density getDensity() {
        return waterVapour.getDensity();
    }

    @Override
    public SpecificHeat getSpecificHeat() {
        return waterVapour.getSpecificHeat();
    }

    @Override
    public SpecificEnthalpy getSpecificEnthalpy() {
        return waterVapour.getSpecificEnthalpy();
    }

    @Override
    public String toConsoleOutput() {
        String separator = " | ";
        String end = "\n\t";
        int digits = 3;
        return "FlowOfWaterVapour:" + end +
                massFlow.toEngineeringFormat("G_wv", digits) + separator +
                massFlow.toKiloGramPerHour().toEngineeringFormat("G_wv", digits) + separator +
                volFlow.toEngineeringFormat("V_wv", digits) +
                volFlow.toCubicMetersPerHour().toEngineeringFormat("V_wv", digits) + end +
                waterVapour.toConsoleOutput();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FlowOfWaterVapour that = (FlowOfWaterVapour) o;
        return Objects.equals(waterVapour, that.waterVapour) && Objects.equals(massFlow, that.massFlow);
    }

    @Override
    public int hashCode() {
        return Objects.hash(waterVapour, massFlow);
    }

    @Override
    public String toString() {
        return "FlowOfWaterVapour{" +
                "waterVapour=" + waterVapour +
                ", massFlow=" + massFlow +
                ", volFlow=" + volFlow +
                '}';
    }

    // Class factory methods
    public FlowOfWaterVapour withMassFlow(MassFlow massFlow) {
        return FlowOfWaterVapour.of(waterVapour, massFlow);
    }

    public FlowOfWaterVapour withVolFlow(VolumetricFlow volFlow) {
        return FlowOfWaterVapour.of(waterVapour, volFlow);
    }

    public FlowOfWaterVapour withHumidAir(WaterVapour waterVapour) {
        return FlowOfWaterVapour.of(waterVapour, massFlow);
    }

    // Static factory methods
    public static FlowOfWaterVapour of(WaterVapour waterVapour, MassFlow massFlow) {
        return new FlowOfWaterVapour(waterVapour, massFlow);
    }

    public static FlowOfWaterVapour of(WaterVapour waterVapour, VolumetricFlow volFlow) {
        CommonValidators.requireNotNull(waterVapour);
        CommonValidators.requireNotNull(volFlow);
        MassFlow massFlow = FlowEquations.massFlowFromVolFlow(waterVapour.getDensity(), volFlow);
        return new FlowOfWaterVapour(waterVapour, massFlow);
    }

}
