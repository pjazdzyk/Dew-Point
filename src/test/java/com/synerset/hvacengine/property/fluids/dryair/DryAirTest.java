package com.synerset.hvacengine.property.fluids.dryair;

import com.synerset.hvacengine.property.fluids.SharedEquations;
import com.synerset.unitility.unitsystem.thermodynamic.Pressure;
import com.synerset.unitility.unitsystem.thermodynamic.Temperature;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class DryAirTest {

    @Test
    @DisplayName("should create dry air instance with valid parameters")
    void shouldCreateDryAirInstance() {
        // Given
        double inputPressure = 100_000.0;
        double inputAirTemp = 25.0;
        double expectedSpecHeat = DryAirEquations.specificHeat(inputAirTemp);
        double expectedSpecEnthalpy = DryAirEquations.specificEnthalpy(inputAirTemp);
        double expectedDynVis = DryAirEquations.dynamicViscosity(inputAirTemp);
        double expectedKinViscosity = DryAirEquations.kinematicViscosity(inputAirTemp, inputPressure);
        double expectedThermalConductivity = DryAirEquations.thermalConductivity(inputAirTemp);
        double expectedDensity = DryAirEquations.density(inputAirTemp, inputPressure);
        double expectedThermalDiffusivity = SharedEquations.thermalDiffusivity(expectedDensity, expectedThermalConductivity, expectedSpecHeat);
        double expectedPrandtlNumber = SharedEquations.prandtlNumber(expectedDynVis, expectedThermalConductivity, expectedSpecHeat);

        // When
        DryAir dryAir = DryAir.of(
                Pressure.ofPascal(inputPressure),
                Temperature.ofCelsius(inputAirTemp)
        );

        double actualPressure = dryAir.getPressure().getValue();
        double actualTemperature = dryAir.getTemperature().getValue();
        double actualSpecHeat = dryAir.getSpecificHeat().getValue();
        double actualSpecEnthalpy = dryAir.getSpecificEnthalpy().getValue();
        double actualDynVis = dryAir.getDynamicViscosity().getValue();
        double actualKinViscosity = dryAir.getKinematicViscosity().getValue();
        double actualThermalConductivity = dryAir.getThermalConductivity().getValue();
        double actualDensity = dryAir.getDensity().getValue();
        double actualThermalDiffusivity = dryAir.getThermalDiffusivity().getValue();
        double actualPrandtlNumber = dryAir.getPrandtlNumber().getValue();

        // Then
        assertThat(actualPressure).isEqualTo(inputPressure);
        assertThat(actualTemperature).isEqualTo(inputAirTemp);
        assertThat(actualSpecHeat).isEqualTo(expectedSpecHeat);
        assertThat(actualSpecEnthalpy).isEqualTo(expectedSpecEnthalpy);
        assertThat(actualDynVis).isEqualTo(expectedDynVis);
        assertThat(actualKinViscosity).isEqualTo(expectedKinViscosity);
        assertThat(actualThermalConductivity).isEqualTo(expectedThermalConductivity);
        assertThat(actualDensity).isEqualTo(expectedDensity);
        assertThat(actualThermalDiffusivity).isEqualTo(expectedThermalDiffusivity);
        assertThat(actualPrandtlNumber).isEqualTo(expectedPrandtlNumber);
    }
}