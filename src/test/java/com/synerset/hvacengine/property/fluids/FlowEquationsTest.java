package com.synerset.hvacengine.property.fluids;

import com.synerset.hvacengine.property.fluids.humidair.HumidAir;
import com.synerset.hvacengine.property.fluids.liquidwater.LiquidWater;
import com.synerset.unitility.unitsystem.flow.MassFlow;
import com.synerset.unitility.unitsystem.flow.VolumetricFlow;
import com.synerset.unitility.unitsystem.humidity.HumidityRatio;
import com.synerset.unitility.unitsystem.humidity.RelativeHumidity;
import com.synerset.unitility.unitsystem.thermodynamic.Density;
import com.synerset.unitility.unitsystem.thermodynamic.Temperature;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.withPrecision;

class FlowEquationsTest {

    private static final double MATH_ACCURACY = 10E-15;
    private static final LiquidWater SAMPLE_LIQ_WATER = LiquidWater.of(Temperature.ofCelsius(15));
    private static final HumidAir SAMPLE_AIR = HumidAir.of(Temperature.ofCelsius(20.0), RelativeHumidity.ofPercentage(50.0));
    static final double SAMPLE_MASS_FLOW = 1.0;  // kg/s
    static final double SAMPLE_FLUID_VOL_FLOW = 0.00100111684564597; // m3/s
    static final double SAMPLE_AIR_DA_MASS_FLOW = 0.992790473618731;  // kg/s

    @Test
    @DisplayName("should calculate volumetric flow flows when mass flow and fluid density is given")
    void calcVolFlowFromMassFlow_shouldReturnVolumetricFlow_whenMassFlowAndFluidDensityIsGiven() {
        // Arrange
        // ACT
        var actualWaterVolFlow = FlowEquations.volFlowFromMassFlow(SAMPLE_LIQ_WATER.getDensity().getInKilogramsPerCubicMeters(),
                SAMPLE_MASS_FLOW);

        // Assert
        assertThat(actualWaterVolFlow).isEqualTo(SAMPLE_FLUID_VOL_FLOW, withPrecision(MATH_ACCURACY));
    }

    @Test
    @DisplayName("should calculate mass flow flows when volumetric flow and fluid density is given")
    void calcMassFlowFromVolFlow_shouldReturnMassFlow_whenVolumetricFlowAndFluidDensityIsGiven() {
        // Arrange
        // ACT
        var actualWaterMassFlow = FlowEquations.massFlowFromVolFlow(SAMPLE_LIQ_WATER.getDensity().getInKilogramsPerCubicMeters(),
                SAMPLE_FLUID_VOL_FLOW);

        // Assert
        assertThat(actualWaterMassFlow).isEqualTo(SAMPLE_MASS_FLOW, withPrecision(MATH_ACCURACY));
    }

    // AIR MASS FLOWS

    @Test
    @DisplayName("should calculate moist air mass flow when dry air mass flow and moist air density is given")
    void calcMaMassFlowFromDaMassFlow_shouldReturnMoistAirMassFlow_whenDryAirMassFlowAndMoistAirDensityIsGiven() {
        // Arrange
        var dryAirMassFlow = FlowEquations.massFlowDaFromMassFlowHa(SAMPLE_AIR.getHumidityRatio().getInKilogramPerKilogram(),
                SAMPLE_MASS_FLOW);

        // ACT
        var actualMaAirMassFlow = FlowEquations.massFlowHaFromMassFlowDa(SAMPLE_AIR.getHumidityRatio().getInKilogramPerKilogram(),
                dryAirMassFlow);

        // Assert
        assertThat(actualMaAirMassFlow).isEqualTo(SAMPLE_MASS_FLOW, withPrecision(MATH_ACCURACY));
    }

    @Test
    @DisplayName("should calculate dry air mass flow when moist air mass flow and humidity ratio is given")
    void calcDaMassFlowFromMaMassFlow_shouldReturnDryAirMassFlow_whenMoistAirMassAndHumidityRatioIsGiven() {
        // Arrange
        // ACT
        var actualDaAirMassFlow = FlowEquations.massFlowDaFromMassFlowHa(SAMPLE_AIR.getHumidityRatio().getInKilogramPerKilogram(),
                SAMPLE_MASS_FLOW);

        // Assert
        assertThat(actualDaAirMassFlow).isEqualTo(SAMPLE_AIR_DA_MASS_FLOW, withPrecision(MATH_ACCURACY));
    }

    @Test
    @DisplayName("should calculate dry air mass flow when moist air volumetric flow, humidity ratio and moist air density is given")
    void calcDaMassFlowFromMaVolFlow_shouldReturnDryAirMassFlow_whenMoistAirVolFlowHumidityRatioAndMoistAirDensityIsGiven() {
        // Arrange
        var volFlowMa = 0.8403259531006995;

        // Act
        var actualDaAirMassFlow = FlowEquations.massFlowDaFromVolFlowHa(SAMPLE_AIR.getDensity().getInKilogramsPerCubicMeters(),
                SAMPLE_AIR.getHumidityRatio().getInKilogramPerKilogram(), volFlowMa);

        // Assert
        assertThat(actualDaAirMassFlow).isEqualTo(SAMPLE_AIR_DA_MASS_FLOW, withPrecision(MATH_ACCURACY));
    }

    @Test
    @DisplayName("should calculate dry air mass flow when dry air volumetric flow and dry air density is given")
    void calcDaMassFlowFromDaVolFlow_shouldReturnDryAirMassFlow_whenDryAirVolFlowAndDryAirDensityIsGiven() {
        // Arrange
        var volFlowDa = FlowEquations.volFlowFromMassFlow(SAMPLE_AIR.getDryAirComponent().getDensity().getInKilogramsPerCubicMeters(),
                SAMPLE_AIR_DA_MASS_FLOW);

        // Act
        var actualDaAirMassFlow = FlowEquations.massFlowFromVolFlow(SAMPLE_AIR.getDryAirComponent().getDensity().getInKilogramsPerCubicMeters(),
                volFlowDa);

        // Assert
        assertThat(actualDaAirMassFlow).isEqualTo(SAMPLE_AIR_DA_MASS_FLOW, withPrecision(MATH_ACCURACY));
    }

    // AIR VOL FLOWS

    @Test
    @DisplayName("should calculate dry air volumetric flow when dry air mass flow and dry air density is given")
    void calcDaVolFlowFromDaMassFlow_shouldReturnDryAirVolumetricFlow_whenDryAirMassFlowAndDryAirDensityIsGiven() {
        // Arrange
        var dryAirMassFlow = FlowEquations.massFlowDaFromMassFlowHa(SAMPLE_AIR.getHumidityRatio().getInKilogramPerKilogram(),
                SAMPLE_MASS_FLOW);
        var expectedDryAirVolFlow = 0.8245101441496746;

        // ACT
        var actualDaAirMassFlow = FlowEquations.volFlowFromMassFlow(SAMPLE_AIR.getDryAirComponent().getDensity().getInKilogramsPerCubicMeters(),
                dryAirMassFlow);

        // Assert
        assertThat(actualDaAirMassFlow).isEqualTo(expectedDryAirVolFlow, withPrecision(MATH_ACCURACY));
    }

    @Test
    @DisplayName("should calculate moist air mass flow when dry air mass flow and moist air density is given")
    void calcMaVolFlowFromDaMassFlow_shouldReturnMoistAirVolumetricFlow_whenDryAirMassFlowAndHumidityRatioAndAndMoistAirDensityIsGiven() {
        // Arrange
        var dryAirMassFlow = FlowEquations.massFlowDaFromMassFlowHa(SAMPLE_AIR.getHumidityRatio().getInKilogramPerKilogram(),
                SAMPLE_MASS_FLOW);
        var expectedMaVolFLow = 0.8403259531006995;

        // ACT
        var actualMaAirMassFlow = FlowEquations.volFlowHaFromMassFlowDa(SAMPLE_AIR.getDensity().getInKilogramsPerCubicMeters(),
                SAMPLE_AIR.getHumidityRatio().getInKilogramPerKilogram(), dryAirMassFlow);

        // Assert
        assertThat(actualMaAirMassFlow).isEqualTo(expectedMaVolFLow, withPrecision(MATH_ACCURACY));
    }

    @Test
    @DisplayName("should all Flow methods using primitive values return the same output as methods using Unitility objects arguments")
    void shouldAllFlowMethodsWithPrimitiveArguments_returnTheSameOutput() {
        // Given
        double densityVal = 1.2;
        double massFlowVal = 2.0;
        double volFlowVal = 1.5;
        double humRatioVal = 0.001;
        Density density = Density.ofKilogramPerCubicMeter(densityVal);
        MassFlow massFlow = MassFlow.ofKilogramsPerSecond(massFlowVal);
        VolumetricFlow volFlow = VolumetricFlow.ofCubicMetersPerSecond(volFlowVal);
        HumidityRatio humRatio = HumidityRatio.ofKilogramPerKilogram(humRatioVal);

        double expectedVolFlowFromMassFlow = FlowEquations.volFlowFromMassFlow(densityVal, massFlowVal);
        double expectedMassFlowFromVolFlow = FlowEquations.massFlowFromVolFlow(densityVal, volFlowVal);
        double expectedMassFlowDaFromMassFlowHa = FlowEquations.massFlowDaFromMassFlowHa(humRatioVal, massFlowVal);
        double expectedMassFlowHaFromMassFlowDa = FlowEquations.massFlowHaFromMassFlowDa(humRatioVal, massFlowVal);
        double expectedVolFlowHaFromMassFlowDa = FlowEquations.volFlowHaFromMassFlowDa(densityVal, humRatioVal, massFlowVal);
        double expectedMassFlowDaFromVolFlowHa = FlowEquations.massFlowDaFromVolFlowHa(densityVal, humRatioVal, volFlowVal);

        // When
        double actualVolFlowFromMassFlow = FlowEquations.volFlowFromMassFlow(density, massFlow).getInCubicMetersPerSecond();
        double actualMassFlowFromVolFlow = FlowEquations.massFlowFromVolFlow(density, volFlow).getInKilogramsPerSecond();
        double actualMassFlowDaFromMassFlowHa = FlowEquations.massFlowDaFromMassFlowHa(humRatio, massFlow).getInKilogramsPerSecond();
        double actualMassFlowHaFromMassFlowDa = FlowEquations.massFlowHaFromMassFlowDa(humRatio, massFlow).getInKilogramsPerSecond();
        double actualVolFlowHaFromMassFlowDa = FlowEquations.volFlowHaFromMassFlowDa(density, humRatio, massFlow).getInKilogramsPerSecond();
        double actualMassFlowDaFromVolFlowHa = FlowEquations.massFlowDaFromVolFlowHa(density, humRatio, volFlow).getInKilogramsPerSecond();

        // Then
        assertThat(actualVolFlowFromMassFlow).isEqualTo(expectedVolFlowFromMassFlow);
        assertThat(actualMassFlowFromVolFlow).isEqualTo(expectedMassFlowFromVolFlow);
        assertThat(actualMassFlowDaFromMassFlowHa).isEqualTo(expectedMassFlowDaFromMassFlowHa);
        assertThat(actualMassFlowHaFromMassFlowDa).isEqualTo(expectedMassFlowHaFromMassFlowDa);
        assertThat(actualVolFlowHaFromMassFlowDa).isEqualTo(expectedVolFlowHaFromMassFlowDa);
        assertThat(actualMassFlowDaFromVolFlowHa).isEqualTo(expectedMassFlowDaFromVolFlowHa);
    }

}
