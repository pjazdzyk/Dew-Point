package com.synerset.hvacengine.property.fluids.humidair;

import com.synerset.hvacengine.property.fluids.SharedEquations;
import com.synerset.unitility.unitsystem.humidity.HumidityRatio;
import com.synerset.unitility.unitsystem.humidity.RelativeHumidity;
import com.synerset.unitility.unitsystem.thermodynamic.Pressure;
import com.synerset.unitility.unitsystem.thermodynamic.Temperature;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class HumidAirTest {

    @Test
    @DisplayName("should create humid air instance with valid parameters")
    void shouldCreateHumidAirInstance() {
        // Given
        double inputPressure = 100_000.0;
        double inputAirTemp = 25.0;
        double inputHumidRatio = 0.0072129;

        double expectedDensRhoVal = HumidAirEquations.density(inputAirTemp, inputHumidRatio, inputPressure);
        double expectedRHVal = HumidAirEquations.relativeHumidity(inputAirTemp, inputHumidRatio, inputPressure);
        double expectedSatPressureVal = HumidAirEquations.saturationPressure(inputAirTemp);
        double expectedMaxHumRatioVal = HumidAirEquations.maxHumidityRatio(expectedSatPressureVal, inputPressure);
        double expectedWBTVal = HumidAirEquations.wetBulbTemperature(inputAirTemp, expectedRHVal, inputPressure);
        double expectedDPTVal = HumidAirEquations.dewPointTemperature(inputAirTemp, expectedRHVal, inputPressure);
        double expectedCpVal = HumidAirEquations.specificHeat(inputAirTemp, inputHumidRatio);
        double expectedSpecEnthalpyVal = HumidAirEquations.specificEnthalpy(inputAirTemp, inputHumidRatio, inputPressure);
        double expectedDynVisVal = HumidAirEquations.dynamicViscosity(inputAirTemp, inputHumidRatio);
        double expectedKinVisVal = HumidAirEquations.kinematicViscosity(inputAirTemp, inputHumidRatio, expectedDensRhoVal);
        double expectedKVal = HumidAirEquations.thermalConductivity(inputAirTemp, inputHumidRatio);
        double expectedThDiffVal = SharedEquations.thermalDiffusivity(expectedDensRhoVal, expectedKVal, expectedCpVal);
        double expectedPrandtlVal = SharedEquations.prandtlNumber(expectedDynVisVal, expectedKVal, expectedCpVal);

        // When
        HumidAir humidAir = HumidAir.of(
                Pressure.ofPascal(inputPressure),
                Temperature.ofCelsius(inputAirTemp),
                HumidityRatio.ofKilogramPerKilogram(inputHumidRatio)
        );

        double actualPressure = humidAir.getPressure().getValue();
        double actualDryBulbTemp = humidAir.getTemperature().getValue();
        double actualHumRatio = humidAir.getHumidityRatio().getValue();
        double actualRhoVal = humidAir.getDensity().getValue();
        double actualRHVal = humidAir.getRelativeHumidity().getValue();
        double actualSatPressureVal = humidAir.getSaturationPressure().getValue();
        double actualMaxHumRatioVal = humidAir.getMaxHumidityRatio().getValue();
        double actualWBTVal = humidAir.getWetBulbTemperature().getValue();
        double actualDPTVal = humidAir.getDewPointTemperature().getValue();
        double actualCpVal = humidAir.getSpecificHeat().getValue();
        double actualSpecEnthalpyVal = humidAir.getSpecificEnthalpy().getValue();
        double actualDynVisVal = humidAir.getDynamicViscosity().getValue();
        double actualKinVisVal = humidAir.getKinematicViscosity().getValue();
        double actualKVal = humidAir.getThermalConductivity().getValue();
        double actualThDiffVal = humidAir.getThermalDiffusivity().getValue();
        double actualPrandtlVal = humidAir.getPrandtlNumber().getValue();
        VapourState actualVapourState = humidAir.getVapourState();

        // Then
        assertThat(actualPressure).isEqualTo(inputPressure);
        assertThat(actualDryBulbTemp).isEqualTo(inputAirTemp);
        assertThat(actualHumRatio).isEqualTo(inputHumidRatio);
        assertThat(actualRhoVal).isEqualTo(expectedDensRhoVal);
        assertThat(actualRHVal).isEqualTo(expectedRHVal);
        assertThat(actualSatPressureVal).isEqualTo(expectedSatPressureVal);
        assertThat(actualMaxHumRatioVal).isEqualTo(expectedMaxHumRatioVal);
        assertThat(actualWBTVal).isEqualTo(expectedWBTVal);
        assertThat(actualDPTVal).isEqualTo(expectedDPTVal);
        assertThat(actualCpVal).isEqualTo(expectedCpVal);
        assertThat(actualSpecEnthalpyVal).isEqualTo(expectedSpecEnthalpyVal);
        assertThat(actualDynVisVal).isEqualTo(expectedDynVisVal);
        assertThat(actualKinVisVal).isEqualTo(expectedKinVisVal);
        assertThat(actualKVal).isEqualTo(expectedKVal);
        assertThat(actualThDiffVal).isEqualTo(expectedThDiffVal);
        assertThat(actualPrandtlVal).isEqualTo(expectedPrandtlVal);
        assertThat(actualVapourState).isEqualTo(VapourState.UNSATURATED);

    }

    @Test
    @DisplayName("should create humid air instance of the same parameters for different constructor input")
    void shouldCreateTheSameAirInstance_whenUsingDifferentConstructors() {
        // Given
        double inputPressure = Pressure.STANDARD_ATMOSPHERE.getValue();
        double inputAirTemp = 25.0;
        double inputHumidRatio = 0.0072129;
        double relativeHumidity = HumidAirEquations.relativeHumidity(inputAirTemp, inputHumidRatio, inputPressure);


        // When
        HumidAir humidAir = HumidAir.of(
                Pressure.ofPascal(inputPressure),
                Temperature.ofCelsius(inputAirTemp),
                HumidityRatio.ofKilogramPerKilogram(inputHumidRatio)
        );

        HumidAir humidAir_1 = HumidAir.of(
                Pressure.ofPascal(inputPressure),
                Temperature.ofCelsius(inputAirTemp),
                RelativeHumidity.ofPercentage(relativeHumidity)
        );

        HumidAir humidAir_2 = HumidAir.of(
                Temperature.ofCelsius(inputAirTemp),
                HumidityRatio.ofKilogramPerKilogram(inputHumidRatio)
        );

        HumidAir humidAir_3 = HumidAir.of(
                Temperature.ofCelsius(inputAirTemp),
                RelativeHumidity.ofPercentage(relativeHumidity)
        );

        assertThat(humidAir.isEqualsWithPrecision(humidAir_1, 1E-13)).isTrue();
        assertThat(humidAir.isEqualsWithPrecision(humidAir_2, 1E-13)).isTrue();
        assertThat(humidAir.isEqualsWithPrecision(humidAir_3, 1E-13)).isTrue();

    }

}
