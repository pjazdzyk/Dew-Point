package com.synerset.hvacengine.property.fluids.vaterwapour;

import com.synerset.hvacengine.property.fluids.liquidwater.LiquidWaterEquations;
import com.synerset.hvacengine.property.fluids.watervapour.WaterVapourEquations;
import com.synerset.unitility.unitsystem.humidity.RelativeHumidity;
import com.synerset.unitility.unitsystem.thermodynamic.Density;
import com.synerset.unitility.unitsystem.thermodynamic.Pressure;
import com.synerset.unitility.unitsystem.thermodynamic.Temperature;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.as;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.withPrecision;

class WaterVapourEquationsTests {

    private static final double PHYS_ATMOSPHERE = 100_000.0;
    private static final double MATH_ACCURACY = 10E-15;
    private static final double CP_WV_ACCURACY = 0.025;

    @Test
    @DisplayName("should return correct water vapour dynamic viscosity when input temperature is given")
    void dynamicViscosity_shouldReturnDynamicWaterVapourDynamicViscosity_whenInputTemperatureIsGiven() {
        // Arrange
        var ta = 20.0;
        var expectedDynViscosity = 9.731572271822231E-6;

        //Act
        var actualDynViscosity = WaterVapourEquations.dynamicViscosity(ta);

        // Assert
        assertThat(actualDynViscosity).isEqualTo(expectedDynViscosity, withPrecision(MATH_ACCURACY));
    }

    @Test
    @DisplayName("should return water vapour density when input temperature is given")
    void density_shouldReturnWaterVapourDensity_whenAirTemperatureIsGiven() {
        // Arrange
        var ta = 20.0;
        var RH = 50.0;
        var expectedWvDensity = 0.7304844902588641;

        //Act
        var actualWvDensity = WaterVapourEquations.density(ta, RH, PHYS_ATMOSPHERE);

        // Assert
        assertThat(actualWvDensity).isEqualTo(expectedWvDensity, withPrecision(MATH_ACCURACY));
    }

    @Test
    @DisplayName("should return water vapour kinematic viscosity when air temperature and density are given")
    void kinematicViscosity_shouldReturnWaterVapourKinematicViscosity_whenAirTempAndDensityIsGiven() {
        // Arrange
        var ta = 20.0;
        var RH = 50.0;
        var rhoWv = WaterVapourEquations.density(ta, RH, PHYS_ATMOSPHERE);
        var expectedWvKinViscosity = 1.3322079252324198E-5;

        //Act
        var actualWvKinViscosity = WaterVapourEquations.kinematicViscosity(ta, rhoWv);

        // Assert
        assertThat(actualWvKinViscosity).isEqualTo(expectedWvKinViscosity, withPrecision(MATH_ACCURACY));
    }

    @ParameterizedTest
    @MethodSource("cpWvInlineData")
    @DisplayName("should return water vapour specific heat according to tables when air temperature is given")
    void specificHeat_shouldReturnWaterVapourSpecificHeat_whenAirTemperatureIsGiven(double ta, double expectedWvSpecificHeat) {
        //Act
        var actualWvSpecificHeat = WaterVapourEquations.specificHeat(ta);

        // Assert
        assertThat(actualWvSpecificHeat).isEqualTo(expectedWvSpecificHeat, withPrecision(CP_WV_ACCURACY));
    }

    //INLINE DATA SEED -> Based on https://www.engineeringtoolbox.com/water-vapor-d_979.html
    static Stream<Arguments> cpWvInlineData() {
        return Stream.of(
                Arguments.of(-98.15, 1.850),
                Arguments.of(-73.15, 1.851),
                Arguments.of(-48.15, 1.852),
                Arguments.of(-23.15, 1.855),
                Arguments.of(1.850, 1.859),
                Arguments.of(26.85, 1.864),
                Arguments.of(51.85, 1.871),
                Arguments.of(76.85, 1.88),
                Arguments.of(101.85, 1.89),
                Arguments.of(126.85, 1.901),
                Arguments.of(176.85, 1.926),
                Arguments.of(226.85, 1.954),
                Arguments.of(326.85, 2.015),
                Arguments.of(526.85, 2.147),
                Arguments.of(676.85, 2.252),
                Arguments.of(976.85, 2.458),
                Arguments.of(1126.85, 2.552),
                Arguments.of(1426.85, 2.711),
                Arguments.of(1726.85, 2.836)
        );
    }

    @Test
    @DisplayName("should return water vapour specific enthalpy when air temperature is given")
    void specificEnthalpy_shouldReturnWaterVapourSpecificEnthalpy_whenAirTemperatureIsGiven() {
        // Arrange
        var ta = 20.0;
        var expectedWvSpecificEnthalpy = 2538.153413040328;

        //Act
        var actualWvSpecificEnthalpy = WaterVapourEquations.specificEnthalpy(ta);

        // Assert
        assertThat(expectedWvSpecificEnthalpy).isEqualTo(actualWvSpecificEnthalpy, withPrecision(MATH_ACCURACY));
    }

    @Test
    @DisplayName("should return water vapour heat of condensation when temperature and pressure is given")
    void heatOfCondensation_shouldReturnHeatOfCondensation_whenTemperatureAndPressureIsGiven(){
        // Given
        var expectedR = LiquidWaterEquations.HEAT_OF_WATER_VAPORIZATION;
        var tx = 0;
        var px = Pressure.STANDARD_ATMOSPHERE.getInPascals();

        // When
        double r = WaterVapourEquations.heatOfCondensation(px, tx);

        // Then
        assertThat(r).isEqualTo(expectedR, withPrecision(MATH_ACCURACY));
    }

    @Test
    @DisplayName("should all Water Vapour methods using primitive values return the same output as methods using Unitility objects arguments")
    void shouldAllWaterVapourMethodsWithPrimitiveArguments_returnTheSameOutput() {
        // Given
        double vapourTemp = 15.5;
        double absPressureVal = 100_000.0;
        double relHumVal = 55.5;
        Pressure absPressure = Pressure.ofPascal(absPressureVal);
        Temperature dryAirTemp = Temperature.ofCelsius(vapourTemp);
        RelativeHumidity relHum = RelativeHumidity.ofPercentage(relHumVal);

        double expectedDensVal = WaterVapourEquations.density(vapourTemp, relHumVal, absPressureVal);
        double expectedDynVisVal = WaterVapourEquations.dynamicViscosity(vapourTemp);
        double expectedKinVisVal = WaterVapourEquations.kinematicViscosity(vapourTemp, expectedDensVal);
        double expectedThermCondVal = WaterVapourEquations.thermalConductivity(vapourTemp);
        double expectedSpecHeatVal = WaterVapourEquations.specificHeat(vapourTemp);
        double expectedSpecEnthalpy = WaterVapourEquations.specificEnthalpy(vapourTemp);

        // When
        double actualDensVal = WaterVapourEquations.density(dryAirTemp, relHum, absPressure).getInKilogramsPerCubicMeters();
        double actualDynVisVal = WaterVapourEquations.dynamicViscosity(dryAirTemp).getInPascalsSecond();
        double actualKinVisVal = WaterVapourEquations.kinematicViscosity(dryAirTemp, Density.ofKilogramPerCubicMeter(actualDensVal)).getInSquareMetersPerSecond();
        double actualThermCondVal = WaterVapourEquations.thermalConductivity(dryAirTemp).getInWattsPerMeterKelvin();
        double actualSpecHeatVal = WaterVapourEquations.specificHeat(dryAirTemp).getInKiloJoulesPerKiloGramKelvin();
        double actualSpecEnthalpy = WaterVapourEquations.specificEnthalpy(dryAirTemp).getInKiloJoulesPerKiloGram();

        // Then
        assertThat(actualDensVal).isEqualTo(expectedDensVal);
        assertThat(actualDynVisVal).isEqualTo(expectedDynVisVal);
        assertThat(actualKinVisVal).isEqualTo(expectedKinVisVal);
        assertThat(actualThermCondVal).isEqualTo(expectedThermCondVal);
        assertThat(actualSpecHeatVal).isEqualTo(expectedSpecHeatVal);
        assertThat(actualSpecEnthalpy).isEqualTo(expectedSpecEnthalpy);
    }

}