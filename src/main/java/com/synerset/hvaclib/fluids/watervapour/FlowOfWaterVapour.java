package com.synerset.hvaclib.fluids.watervapour;

import com.synerset.hvaclib.common.Validators;
import com.synerset.hvaclib.fluids.Flow;
import com.synerset.hvaclib.fluids.FlowEquations;
import com.synerset.unitility.unitsystem.flows.MassFlow;
import com.synerset.unitility.unitsystem.flows.VolumetricFlow;
import com.synerset.unitility.unitsystem.thermodynamic.*;

import java.util.Objects;

public class FlowOfWaterVapour implements Flow<WaterVapour> {

    public static MassFlow MASS_FLOW_MIN_LIMIT = MassFlow.ofKilogramsPerSecond(0);
    public static MassFlow MASS_FLOW_MAX_LIMIT = MassFlow.ofKilogramsPerSecond(5E9);
    private final WaterVapour waterVapour;
    private final MassFlow massFlow;
    private final VolumetricFlow volFlow;

    public FlowOfWaterVapour(WaterVapour waterVapour, MassFlow massFlow) {
        Validators.requireNotNull(waterVapour);
        Validators.requireNotNull(massFlow);
        Validators.requireBetweenBoundsInclusive(massFlow, MASS_FLOW_MIN_LIMIT, MASS_FLOW_MAX_LIMIT);
        this.waterVapour = waterVapour;
        this.massFlow = massFlow;
        this.volFlow = FlowEquations.massFlowToVolFlow(waterVapour.density(), massFlow);
    }

    @Override
    public WaterVapour fluid() {
        return waterVapour;
    }

    @Override
    public MassFlow massFlow() {
        return massFlow;
    }

    @Override
    public VolumetricFlow volumetricFlow() {
        return volFlow;
    }

    @Override
    public Temperature temperature() {
        return waterVapour.temperature();
    }

    @Override
    public Pressure pressure() {
        return waterVapour.pressure();
    }

    @Override
    public Density density() {
        return waterVapour.density();
    }

    @Override
    public SpecificHeat specificHeat() {
        return waterVapour.specificHeat();
    }

    @Override
    public SpecificEnthalpy specificEnthalpy() {
        return waterVapour.specificEnthalpy();
    }

    @Override
    public String toFormattedString() {
        return "FlowOfWaterVapour:\n\t" +
                massFlow.toFormattedString("G", "wv", "| ") +
                massFlow.toKiloGramPerHour().toFormattedString("G", "wv", "| ") +
                volFlow.toFormattedString("V", "| ") +
                volFlow.toCubicMetersPerHour().toFormattedString("V", "wv") +
                "\n\t" +
                waterVapour.toFormattedString() +
                "\n";
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
        Validators.requireNotNull(waterVapour);
        Validators.requireNotNull(volFlow);
        MassFlow massFlow = FlowEquations.volFlowToMassFlow(waterVapour.density(), volFlow);
        return new FlowOfWaterVapour(waterVapour, massFlow);
    }

}