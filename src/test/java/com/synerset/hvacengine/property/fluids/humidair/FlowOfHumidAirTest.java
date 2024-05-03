package com.synerset.hvacengine.property.fluids.humidair;

import com.synerset.hvacengine.property.fluids.FlowEquations;
import com.synerset.unitility.unitsystem.flow.MassFlow;
import com.synerset.unitility.unitsystem.humidity.RelativeHumidity;
import com.synerset.unitility.unitsystem.thermodynamic.Temperature;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.withPrecision;

class FlowOfHumidAirTest {

    private static final double INIT_MASS_FLOW_MA = 2.0; // kg/s

    @Test
    @DisplayName("FlowOfMoistAir: should instance with properly calculated flows when valid input is given")
    void flowOfMoistAirInstance_shouldCreateValidFlowOfMoistAirInstance_whenValidSampleInputIsGiven() {
        // Arrange
        HumidAir sampleAir = HumidAir.of(Temperature.ofCelsius(45.0), RelativeHumidity.ofPercentage(60.1));

        double densityMa = sampleAir.getDensity().getInKilogramsPerCubicMeters();
        double densityDa = sampleAir.getDryAirComponent().getDensity().getInKilogramsPerCubicMeters();
        double humidRatio = sampleAir.getHumidityRatio().getInKilogramPerKilogram();
        double expectedVolFlow_Ma = INIT_MASS_FLOW_MA / densityMa;
        double expectedMassFlow_Da = FlowEquations.massFlowHaToMassFlowDa(humidRatio, INIT_MASS_FLOW_MA);
        double expectedVolFlow_Da = expectedMassFlow_Da / densityDa;

        // Act
        FlowOfHumidAir flowAir = FlowOfHumidAir.of(sampleAir, MassFlow.ofKilogramsPerSecond(INIT_MASS_FLOW_MA));
        HumidAir humidAir = flowAir.getFluid();

        double actualMassFlowMa = flowAir.getMassFlow().getInKilogramsPerSecond();
        double actualVolFlowMa = flowAir.getVolFlow().getInCubicMetersPerSecond();
        double actualMassFlowDa = flowAir.getDryAirMassFlow().getInKilogramsPerSecond();
        double actualVolFlowDa = flowAir.getDryAirVolFlow().getInCubicMetersPerSecond();

        // Assert
        assertThat(actualMassFlowMa).isEqualTo(INIT_MASS_FLOW_MA);
        assertThat(actualVolFlowMa).isEqualTo(expectedVolFlow_Ma, withPrecision(1E-13));
        assertThat(actualMassFlowDa).isEqualTo(expectedMassFlow_Da);
        assertThat(actualVolFlowDa).isEqualTo(expectedVolFlow_Da);
        assertThat(flowAir.getFluid()).isEqualTo(sampleAir);

        assertThat(flowAir.getTemperature()).isEqualTo(humidAir.getTemperature());
        assertThat(flowAir.getRelativeHumidity()).isEqualTo(humidAir.getRelativeHumidity());
        assertThat(flowAir.getHumidityRatio()).isEqualTo(humidAir.getHumidityRatio());
        assertThat(flowAir.getPressure()).isEqualTo(humidAir.getPressure());
        assertThat(flowAir.getDensity()).isEqualTo(humidAir.getDensity());
        assertThat(flowAir.getSpecificHeat()).isEqualTo(humidAir.getSpecificHeat());
        assertThat(flowAir.getSpecificEnthalpy()).isEqualTo(humidAir.getSpecificEnthalpy());
        assertThat(flowAir.getSaturationPressure()).isEqualTo(humidAir.getSaturationPressure());
    }
}
