package com.synerset.hvacengine.property.fluids.vaterwapour;

import com.synerset.hvacengine.property.fluids.watervapour.WaterVapour;
import com.synerset.hvacengine.property.fluids.watervapour.WaterVapourEquations;
import com.synerset.unitility.unitsystem.humidity.RelativeHumidity;
import com.synerset.unitility.unitsystem.thermodynamic.Pressure;
import com.synerset.unitility.unitsystem.thermodynamic.Temperature;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class WaterVapourTest {

    @Test
    @DisplayName("should create water vapour instance with valid parameters")
    void shouldCreateWaterVapourInstance() {
        // Given
        double inputPressure = 100_000.0;
        double inputAirTemp = 25.0;
        double inputAirRH = 50;
        double densVal = WaterVapourEquations.density(inputAirTemp, inputAirRH, inputPressure);
        double expectedSpecHeat = WaterVapourEquations.specificHeat(inputAirTemp);
        double expectedSpecEnthalpy = WaterVapourEquations.specificEnthalpy(inputAirTemp);
        double expectedDynVis = WaterVapourEquations.dynamicViscosity(inputAirTemp);
        double expectedKinViscosity = WaterVapourEquations.kinematicViscosity(inputAirTemp, densVal);
        double expectedThermalConductivity = WaterVapourEquations.thermalConductivity(inputAirTemp);

        // When
        WaterVapour waterVapour = WaterVapour.of(
                Pressure.ofPascal(inputPressure),
                Temperature.ofCelsius(inputAirTemp),
                RelativeHumidity.ofPercentage(inputAirRH)
        );

        double actualPressure = waterVapour.getPressure().getValue();
        double actualTemperature = waterVapour.getTemperature().getValue();
        double actualSpecHeat = waterVapour.getSpecificHeat().getValue();
        double actualSpecEnthalpy = waterVapour.getSpecificEnthalpy().getValue();
        double actualDynVis = waterVapour.getDynamicViscosity().getValue();
        double actualKinViscosity = waterVapour.getKinematicViscosity().getValue();
        double actualThermalConductivity = waterVapour.getThermalConductivity().getValue();

        // Then
        assertThat(actualPressure).isEqualTo(inputPressure);
        assertThat(actualTemperature).isEqualTo(inputAirTemp);
        assertThat(actualSpecHeat).isEqualTo(expectedSpecHeat);
        assertThat(actualSpecEnthalpy).isEqualTo(expectedSpecEnthalpy);
        assertThat(actualDynVis).isEqualTo(expectedDynVis);
        assertThat(actualKinViscosity).isEqualTo(expectedKinViscosity);
        assertThat(actualThermalConductivity).isEqualTo(expectedThermalConductivity);
    }

}