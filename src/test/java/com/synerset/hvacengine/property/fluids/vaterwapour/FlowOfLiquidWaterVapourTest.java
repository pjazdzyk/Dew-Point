package com.synerset.hvacengine.property.fluids.vaterwapour;

import com.synerset.hvacengine.property.fluids.watervapour.FlowOfWaterVapour;
import com.synerset.hvacengine.property.fluids.watervapour.WaterVapour;
import com.synerset.unitility.unitsystem.flow.MassFlow;
import com.synerset.unitility.unitsystem.thermodynamic.Temperature;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class FlowOfLiquidWaterVapourTest {

    static final WaterVapour SAMPLE_WATER_VAPOUR = WaterVapour.of(Temperature.ofCelsius(98.6));
    static final double SAMPLE_MASS_FLOW_RATE = 4.68; // kg/s

    @Test
    @DisplayName("FlowOfWaterVapour: should create instance with properly calculated flows when valid input is given")
    void flowOfFluidInstance_shouldCreateValidFlowOfFluidInstance_whenValidSampleInputIsGiven() {
        // Given
        double waterDensity = SAMPLE_WATER_VAPOUR.getDensity().getInKilogramsPerCubicMeters();
        double expectedVolFlow = SAMPLE_MASS_FLOW_RATE / waterDensity;

        // When
        FlowOfWaterVapour flowOfWaterVapour = FlowOfWaterVapour.of(SAMPLE_WATER_VAPOUR, MassFlow.ofKilogramsPerSecond(SAMPLE_MASS_FLOW_RATE));
        WaterVapour waterVapour = flowOfWaterVapour.getFluid();
        double actualMassFlow = flowOfWaterVapour.getMassFlow().getInKilogramsPerSecond();
        double actualVolFlow = flowOfWaterVapour.getVolFlow().getInCubicMetersPerSecond();

        // Then
        assertThat(actualMassFlow).isEqualTo(SAMPLE_MASS_FLOW_RATE);
        assertThat(actualVolFlow).isEqualTo(expectedVolFlow);

        assertThat(flowOfWaterVapour.getTemperature()).isEqualTo(waterVapour.getTemperature());
        assertThat(flowOfWaterVapour.getPressure()).isEqualTo(waterVapour.getPressure());
        assertThat(flowOfWaterVapour.getDensity()).isEqualTo(waterVapour.getDensity());
        assertThat(flowOfWaterVapour.getSpecificHeat()).isEqualTo(waterVapour.getSpecificHeat());
        assertThat(flowOfWaterVapour.getSpecificEnthalpy()).isEqualTo(waterVapour.getSpecificEnthalpy());
    }

}