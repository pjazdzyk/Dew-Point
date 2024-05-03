package com.synerset.hvacengine.property.fluids.liquidwater;

import com.synerset.unitility.unitsystem.thermodynamic.Pressure;
import com.synerset.unitility.unitsystem.thermodynamic.Temperature;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class LiquidWaterTest {

    @Test
    @DisplayName("should create liquid water instance with valid parameters")
    void shouldCreateDryAirInstance() {
        // Given
        double inputPressure = 100_000.0;
        double inputAirTemp = 25.0;
        double expectedDensity = LiquidWaterEquations.density(inputAirTemp);
        double expectedSpecHeat = LiquidWaterEquations.specificHeat(inputAirTemp);
        double expectedSpecEnthalpy = LiquidWaterEquations.specificEnthalpy(inputAirTemp);

        // When
        LiquidWater liquidWater = LiquidWater.of(
                Pressure.ofPascal(inputPressure),
                Temperature.ofCelsius(inputAirTemp)
        );

        double actualPressure = liquidWater.getPressure().getValue();
        double actualTemperature = liquidWater.getTemperature().getValue();
        double actualSpecHeat = liquidWater.getSpecificHeat().getValue();
        double actualSpecEnthalpy = liquidWater.getSpecificEnthalpy().getValue();
        double actualDensity = liquidWater.getDensity().getValue();

        // Then
        assertThat(actualPressure).isEqualTo(inputPressure);
        assertThat(actualTemperature).isEqualTo(inputAirTemp);
        assertThat(actualSpecHeat).isEqualTo(expectedSpecHeat);
        assertThat(actualSpecEnthalpy).isEqualTo(expectedSpecEnthalpy);
        assertThat(expectedDensity).isEqualTo(actualDensity);
    }

}
