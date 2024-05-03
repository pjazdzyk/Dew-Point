package com.synerset.hvacengine.property.fluids.dryair;

import com.synerset.unitility.unitsystem.flow.MassFlow;
import com.synerset.unitility.unitsystem.thermodynamic.Temperature;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class FlowOfDryAirTest {

    static final DryAir SAMPLE_AIR = DryAir.of(Temperature.ofCelsius(98.6));
    static final double SAMPLE_MASS_FLOW_RATE = 4.68; // kg/s

    @Test
    @DisplayName("FlowOfDryAir: should create instance with properly calculated flows when valid input is given")
    void flowOfFluidInstance_shouldCreateValidFlowOfFluidInstance_whenValidSampleInputIsGiven() {
        // Given
        double waterDensity = SAMPLE_AIR.getDensity().getInKilogramsPerCubicMeters();
        double expectedVolFlow = SAMPLE_MASS_FLOW_RATE / waterDensity;

        // When
        FlowOfDryAir flowOfDryAir = FlowOfDryAir.of(SAMPLE_AIR, MassFlow.ofKilogramsPerSecond(SAMPLE_MASS_FLOW_RATE));
        DryAir dryAir = flowOfDryAir.getFluid();
        double actualMassFlow = flowOfDryAir.getMassFlow().getInKilogramsPerSecond();
        double actualVolFlow = flowOfDryAir.getVolFlow().getInCubicMetersPerSecond();

        // Then
        assertThat(actualMassFlow).isEqualTo(SAMPLE_MASS_FLOW_RATE);
        assertThat(actualVolFlow).isEqualTo(expectedVolFlow);

        assertThat(flowOfDryAir.getTemperature()).isEqualTo(dryAir.getTemperature());
        assertThat(flowOfDryAir.getPressure()).isEqualTo(dryAir.getPressure());
        assertThat(flowOfDryAir.getDensity()).isEqualTo(dryAir.getDensity());
        assertThat(flowOfDryAir.getSpecificHeat()).isEqualTo(dryAir.getSpecificHeat());
        assertThat(flowOfDryAir.getSpecificEnthalpy()).isEqualTo(dryAir.getSpecificEnthalpy());
    }

}