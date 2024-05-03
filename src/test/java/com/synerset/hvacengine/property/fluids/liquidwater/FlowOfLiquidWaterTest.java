package com.synerset.hvacengine.property.fluids.liquidwater;

import com.synerset.unitility.unitsystem.flow.MassFlow;
import com.synerset.unitility.unitsystem.thermodynamic.Temperature;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class FlowOfLiquidWaterTest {

    private static final LiquidWater SAMPLE_WATER = LiquidWater.of(Temperature.ofCelsius(98.6));
    private static final double SAMPLE_MASS_FLOW_RATE = 4.68; // kg/s

    @Test
    @DisplayName("FlowOfWater: should create instance with properly calculated flows when valid input is given")
    void flowOfFluidInstance_shouldCreateValidFlowOfFluidInstance_whenValidSampleInputIsGiven() {
        // Given
        double waterDensity = SAMPLE_WATER.getDensity().getInKilogramsPerCubicMeters();
        double expectedVolFlow = SAMPLE_MASS_FLOW_RATE / waterDensity;

        // When
        FlowOfLiquidWater flowOfLiquidWater = FlowOfLiquidWater.of(SAMPLE_WATER, MassFlow.ofKilogramsPerSecond(SAMPLE_MASS_FLOW_RATE));
        LiquidWater water = flowOfLiquidWater.getFluid();
        double actualMassFlow = flowOfLiquidWater.getMassFlow().getInKilogramsPerSecond();
        double actualVolFlow = flowOfLiquidWater.getVolFlow().getInCubicMetersPerSecond();

        // Then
        assertThat(actualMassFlow).isEqualTo(SAMPLE_MASS_FLOW_RATE);
        assertThat(actualVolFlow).isEqualTo(expectedVolFlow);

        assertThat(flowOfLiquidWater.getTemperature()).isEqualTo(water.getTemperature());
        assertThat(flowOfLiquidWater.getPressure()).isEqualTo(water.getPressure());
        assertThat(flowOfLiquidWater.getDensity()).isEqualTo(water.getDensity());
        assertThat(flowOfLiquidWater.getSpecificHeat()).isEqualTo(water.getSpecificHeat());
        assertThat(flowOfLiquidWater.getSpecificEnthalpy()).isEqualTo(water.getSpecificEnthalpy());
    }

}