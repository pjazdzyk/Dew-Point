package com.synerset.hvacengine.process.heating;

/**
 * This test case is based on example from technical literature:
 * B.Lipska - Projektowanie Wentylacji i Klimatyzacji, Podstawy uzdatniania powietrza. Gliwice 2014.
 * Section: 2.1, page: 37
 */
class HeatingTest {
/*
    @Test
    @DisplayName("Heating: should heat up inlet air when heating power is given")
    void shouldHeatUpInletAirWhenInputPowerIsGiven() {
        // Given
        HumidAir humidAir = HumidAir.of(
                Pressure.ofHectoPascal(987),
                Temperature.ofCelsius(10),
                RelativeHumidity.ofPercentage(60)
        );

        FlowOfHumidAir inletAir = FlowOfHumidAir.of(humidAir, MassFlow.ofKilogramsPerHour(10_000));
        Power inputPower = Power.ofKiloWatts(56);
        HeatingStrategy calculationStrategy = HeatingStrategy.of(inletAir, inputPower);

        Temperature expectedTemperature = Temperature.ofCelsius(30);
        RelativeHumidity expectedRH = RelativeHumidity.ofPercentage(17.4);

        // When
        Heating heating = Heating.of(calculationStrategy);

        // Then
        assertThat(heating).isNotNull();
        assertThat(heating.getOutletAir()).isNotNull();
        assertThat(heating.getInputInletAir()).isEqualTo(inletAir);
        assertThat(heating.getHeatOfProcess()).isEqualTo(inputPower);
        assertThat(heating.getHeatingStrategy()).isEqualTo(calculationStrategy);
        assertThat(heating.getOutletPressure()).isEqualTo(humidAir.getPressure());
        assertThat(heating.getOutletTemperature().getInCelsius()).isEqualTo(expectedTemperature.getInCelsius(), withPrecision(3.5E-2));
        assertThat(heating.getOutletRelativeHumidity().getInPercent()).isEqualTo(expectedRH.getInPercent(), withPrecision(1.5E-2));
        assertThat(heating.getOutletHumidityRatio()).isEqualTo(inletAir.getHumidityRatio());
        SpecificEnthalpy expectedEnthalpy = HumidAirEquations.specificEnthalpy(heating.getOutletTemperature(), heating.getOutletHumidityRatio(), heating.getOutletPressure());
        assertThat(heating.getOutletSpecificEnthalpy()).isEqualTo(expectedEnthalpy);
    }

    @Test
    @DisplayName("Heating: should heat up inlet air when target temperature is given")
    void shouldHeatUpInletAirWhenTargetTemperatureIsGiven() {
        // Given
        HumidAir humidAir = HumidAir.of(
                Pressure.ofHectoPascal(987),
                Temperature.ofCelsius(10),
                RelativeHumidity.ofPercentage(60)
        );

        FlowOfHumidAir inletAir = FlowOfHumidAir.of(humidAir, MassFlow.ofKilogramsPerHour(10_000));
        Temperature targetTemperature = Temperature.ofCelsius(29.96581041061914);
        HeatingStrategy calculationStrategy = HeatingStrategy.of(inletAir, targetTemperature);

        Power expectedPower = Power.ofKiloWatts(56).toWatts();
        RelativeHumidity expectedRH = RelativeHumidity.ofPercentage(17.386707253107);

        // When
        Heating heating = Heating.of(calculationStrategy);

        // Then
        assertThat(heating).isNotNull();
        assertThat(heating.getOutletAir()).isNotNull();
        assertThat(heating.getInputInletAir()).isEqualTo(inletAir);
        assertThat(heating.getHeatOfProcess().getInWatts()).isEqualTo(expectedPower.getInWatts(), withPrecision(1E-10));
        assertThat(heating.getHeatingStrategy()).isEqualTo(calculationStrategy);
        assertThat(heating.getOutletPressure()).isEqualTo(humidAir.getPressure());
        assertThat(heating.getOutletTemperature().getValue()).isEqualTo(targetTemperature.getValue(), withPrecision(1E-13));
        assertThat(heating.getOutletRelativeHumidity().getInPercent()).isEqualTo(expectedRH.getInPercent());
        assertThat(heating.getOutletHumidityRatio()).isEqualTo(inletAir.getHumidityRatio());
        SpecificEnthalpy expectedEnthalpy = HumidAirEquations.specificEnthalpy(heating.getOutletTemperature(), heating.getOutletHumidityRatio(), heating.getOutletPressure());
        assertThat(heating.getOutletSpecificEnthalpy()).isEqualTo(expectedEnthalpy);
    }

    @Test
    @DisplayName("Heating: should heat up inlet air when target relative humidity is given")
    void shouldHeatUpInletAirWhenTargetRelativeHumidityIsGiven() {
        // Given
        HumidAir humidAir = HumidAir.of(
                Pressure.ofHectoPascal(987),
                Temperature.ofCelsius(10),
                RelativeHumidity.ofPercentage(60)
        );

        FlowOfHumidAir inletAir = FlowOfHumidAir.of(humidAir, MassFlow.ofKilogramsPerHour(10_000));
        RelativeHumidity targetRH = RelativeHumidity.ofPercentage(17.386707253107);
        HeatingStrategy calculationStrategy = HeatingStrategy.of(inletAir, targetRH);

        Power expectedPower = Power.ofKiloWatts(56).toWatts();
        Temperature expectedTemperature = Temperature.ofCelsius(29.96581041061914);

        // When
        Heating heating = Heating.of(calculationStrategy);

        // Then
        assertThat(heating).isNotNull();
        assertThat(heating.getOutletAir()).isNotNull();
        assertThat(heating.getInputInletAir()).isEqualTo(inletAir);
        assertThat(heating.getHeatOfProcess().getValue()).isEqualTo(expectedPower.getValue(), withPrecision(1E-9));
        assertThat(heating.getHeatingStrategy()).isEqualTo(calculationStrategy);
        assertThat(heating.getOutletPressure()).isEqualTo(humidAir.getPressure());
        assertThat(heating.getOutletTemperature().getInCelsius()).isEqualTo(expectedTemperature.getInCelsius(), withPrecision(1E-10));
        assertThat(heating.getOutletRelativeHumidity().getInPercent()).isEqualTo(targetRH.getInPercent());
        assertThat(heating.getOutletHumidityRatio()).isEqualTo(inletAir.getHumidityRatio());
        SpecificEnthalpy expectedEnthalpy = HumidAirEquations.specificEnthalpy(heating.getOutletTemperature(), heating.getOutletHumidityRatio(), heating.getOutletPressure());
        assertThat(heating.getOutletSpecificEnthalpy()).isEqualTo(expectedEnthalpy);
    }
*/
}