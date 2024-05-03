package com.synerset.hvacengine.process.heating;

import com.synerset.hvacengine.process.heating.dataobject.HeatingResult;
import com.synerset.hvacengine.property.fluids.humidair.FlowOfHumidAir;
import com.synerset.hvacengine.property.fluids.humidair.HumidAir;
import com.synerset.unitility.unitsystem.flow.MassFlow;
import com.synerset.unitility.unitsystem.humidity.RelativeHumidity;
import com.synerset.unitility.unitsystem.thermodynamic.Power;
import com.synerset.unitility.unitsystem.thermodynamic.Pressure;
import com.synerset.unitility.unitsystem.thermodynamic.Temperature;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class AirHeatingEquationsTest {

    private static FlowOfHumidAir inletFlow;

    @BeforeAll
    static void setUp() {
        HumidAir inputAir = HumidAir.of(
                Pressure.ofPascal(100_000),
                Temperature.ofCelsius(10.0),
                RelativeHumidity.ofPercentage(60.0)
        );

        inletFlow = FlowOfHumidAir.of(inputAir, MassFlow.ofKilogramsPerSecond(10000d / 3600d));
    }

    @Test
    @DisplayName("Heating equations: should heat up an inlet air when positive heat of process is given")
    void processOfHeating_shouldHeatUpInletAir_whenHeatOfProcessIsGiven() {
        // Given
        Power inputHeat = Power.ofWatts(56093.07605668045);
        Temperature expectedOutTemp = Temperature.ofCelsius(30d);

        // When
        HeatingResult heatingResult = HeatingEquations.heatingFromPower(inletFlow, inputHeat);

        Power actualProcessHeat = heatingResult.heatOfProcess();
        Temperature actualOutAirTemp = heatingResult.outletAirFlow().getTemperature();

        // Then
        assertThat(actualOutAirTemp).isEqualTo(expectedOutTemp);
        assertThat(actualProcessHeat).isEqualTo(inputHeat);
    }

    @Test
    @DisplayName("Heating equations: should heat up inlet air when target outlet air temperature is given")
    void processOfHeating_shouldHeatUpInletAir_whenTargetOutletTempIsGiven() {
        // Given
        Temperature targetOutTemp = Temperature.ofCelsius(30d);
        Power inputHeat = Power.ofWatts(56093.07605668045);

        // When
        HeatingResult heatingResult = HeatingEquations.heatingFromTargetTemperature(inletFlow, targetOutTemp);
        Power actualHeatPower = heatingResult.heatOfProcess();
        Temperature actualOutTemp = heatingResult.outletAirFlow().getTemperature();

        // Then
        assertThat(actualHeatPower).isEqualTo(inputHeat);
        assertThat(actualOutTemp).isEqualTo(targetOutTemp);
    }

    @Test
    @DisplayName("Heating equations: should heat up inlet air when target outlet relative humidity is given")
    void processOfHeating_shouldHeatUpInletAir_whenTargetRelativeHumidityIsGiven() {
        // Arrange
        RelativeHumidity expectedOutRH = RelativeHumidity.ofPercentage(17.352612275343887);
        Power expectedHeatOfProcess = Power.ofWatts(56093.07605668045);
        Temperature expectedOutTemp = Temperature.ofCelsius(30d);

        // Act
        HeatingResult heatingResult = HeatingEquations.heatingFromRelativeHumidity(inletFlow, expectedOutRH);
        Power actualHeatOfProcess = heatingResult.heatOfProcess();
        Temperature actualOutAirTemp = heatingResult.outletAirFlow().getTemperature();

        // Assert
        assertThat(actualHeatOfProcess.isEqualWithPrecision(expectedHeatOfProcess, 1E-9)).isTrue();
        assertThat(actualOutAirTemp.isEqualWithPrecision(expectedOutTemp, 1E-9)).isTrue();
    }

}