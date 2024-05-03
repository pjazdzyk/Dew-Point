package com.synerset.hvacengine.property.fluids;

import com.synerset.hvacengine.property.fluids.dryair.DryAirEquations;
import com.synerset.unitility.unitsystem.common.Distance;
import com.synerset.unitility.unitsystem.thermodynamic.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.withPrecision;

class SharedEquationsTest {

    private static final double TH_DIFF_ACCURACY = 0.021E-5;
    private static final double PRANDTL_ACCURACY = 0.009;
    private static final double PHYS_ATMOSPHERE = 100_000.0;

    @Test
    @DisplayName("should return atmospheric pressure when higher altitude is given")
    void atmAltitudePressure_shouldReturnLowerAtmPressure_whenHigherAltitudeIsGiven() {
        // Arrange
        var altitude = 2000;
        var expectedPressure = 101.325 * Math.pow((1 - 2.25577 * Math.pow(10, -5) * altitude), 5.2559) * 1000;

        //Act
        double actualPressure = SharedEquations.atmAltitudePressure(altitude);

        // Assert
        assertThat(actualPressure).isEqualTo(expectedPressure).isLessThan(PHYS_ATMOSPHERE);
    }

    @Test
    @DisplayName("should return lower temperature for higher altitudes")
    void altitudeTemperature_shouldReturnLowerTemperature_whenHigherAltitudeIsGiven() {
        // Arrange
        var altitude = 2000;
        var tempAtSea = 20.0;
        var expectedTemp = tempAtSea - 0.0065 * altitude;

        //Act
        var actualTemp = SharedEquations.altitudeTemperature(tempAtSea, altitude);

        // Assert
        assertThat(actualTemp).isEqualTo(expectedTemp).isLessThan(tempAtSea);

    }

    @Test
    @DisplayName("should return dry air thermal diffusivity when air temperature is given")
    void thermalDiffusivity_shouldReturnDryAirThermalDiffusivity_whenAirTemperatureIsGiven() {
        // Arrange
        var Pat = 101_300;
        var ta = 26.85;
        var rhoDa = DryAirEquations.density(ta, Pat);
        var kDa = DryAirEquations.thermalConductivity(ta);
        var cpDa = DryAirEquations.specificHeat(ta);
        var expectedThermalDiffusivity = 2.218E-5;

        //Act
        var actualThermalDiffusivity = SharedEquations.thermalDiffusivity(rhoDa, kDa, cpDa);

        // Assert
        assertThat(actualThermalDiffusivity).isEqualTo(expectedThermalDiffusivity, withPrecision(TH_DIFF_ACCURACY));
    }

    @Test
    @DisplayName("should return dry air Prandtl number when air temperature is given")
    void prandtlNumber_shouldReturnDryAirPrandtlNumber_whenAirTemperatureIsGiven() {
        // Arrange
        var ta = 26.85;
        var dynVis = DryAirEquations.dynamicViscosity(ta);
        var kDa = DryAirEquations.thermalConductivity(ta);
        var cpDa = DryAirEquations.specificHeat(ta);
        var expectedPrandtlNumber = 0.707;

        //Act
        var actualPrandtlNumber = SharedEquations.prandtlNumber(dynVis, kDa, cpDa);

        // Assert
        assertThat(actualPrandtlNumber).isEqualTo(expectedPrandtlNumber, withPrecision(PRANDTL_ACCURACY));
    }

    @Test
    @DisplayName("should all shared methods using primitive values return the same output as methods using Unitility objects arguments")
    void shouldAllSharedMethodsWithPrimitiveArguments_returnTheSameOutput() {
        // Given
        double heightVal = 5_000;
        double tempVal = 30.0;
        double densityVal = 1.2;
        double specHeatVal = 1.005;
        double thermalCondVal = 1.6;
        double dynVisVal = 0.000001;
        Distance height = Distance.ofMeters(heightVal);
        Temperature temp = Temperature.ofCelsius(tempVal);
        Density density = Density.ofKilogramPerCubicMeter(densityVal);
        SpecificHeat specHeat = SpecificHeat.ofKiloJoulePerKiloGramKelvin(specHeatVal);
        ThermalConductivity thermalCond = ThermalConductivity.ofWattsPerMeterKelvin(thermalCondVal);
        DynamicViscosity dynVis = DynamicViscosity.ofKiloGramPerMeterSecond(dynVisVal);

        double expectedAltPressure = SharedEquations.atmAltitudePressure(heightVal);
        double expectedAltTemperature = SharedEquations.altitudeTemperature(tempVal, heightVal);
        double expectedThermDiffusivity = SharedEquations.thermalDiffusivity(densityVal, thermalCondVal, specHeatVal);
        double expectedPrandtlNum = SharedEquations.prandtlNumber(dynVisVal, thermalCondVal, specHeatVal);

        // When
        double actualAltPressure = SharedEquations.atmAltitudePressure(height).getInPascals();
        double actualAltTemperature = SharedEquations.altitudeTemperature(temp, height).getInCelsius();
        double actualThermDiffusivity = SharedEquations.thermalDiffusivity(density, thermalCond, specHeat).getInSquareMetersPerSecond();
        double actualPrandtlNum = SharedEquations.prandtlNumber(dynVis, thermalCond, specHeat).getValue();

        // Then
        assertThat(actualAltPressure).isEqualTo(expectedAltPressure);
        assertThat(actualAltTemperature).isEqualTo(expectedAltTemperature);
        assertThat(actualThermDiffusivity).isEqualTo(expectedThermDiffusivity);
        assertThat(actualPrandtlNum).isEqualTo(expectedPrandtlNum);
    }

}