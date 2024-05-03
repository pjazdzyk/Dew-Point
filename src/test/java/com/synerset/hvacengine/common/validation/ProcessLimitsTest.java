package com.synerset.hvacengine.common.validation;

class ProcessLimitsTest {
/*
    @Test
    @DisplayName("Process: should throw exception when null passed as argument")
    void shouldThrowNullPointerExceptionWhenNullIsPassedAsArgumentProcess() {
        Temperature exampleTemperature = Temperature.ofCelsius(35);
        HumidAir humidAir = HumidAir.of(Pressure.STANDARD_ATMOSPHERE, exampleTemperature, RelativeHumidity.ofPercentage(50));
        FlowOfHumidAir flowOfHumidAir = FlowOfHumidAir.of(humidAir, MassFlow.ofKilogramsPerSecond(5));
        CoolantData coolantData = CoolantData.of(Temperature.ofCelsius(7), Temperature.ofCelsius(14));
        FlowOfHumidAir nullFlow = null;
        Power nullPower = null;
        Temperature nullTemperature = null;
        RelativeHumidity relativeHumidity = null;
        CoolantData nullCoolantData = null;
        Power examplePower = Power.ofWatts(100);

        assertThatThrownBy(() -> CoolingStrategy.of(flowOfHumidAir, coolantData, nullPower))
                .isInstanceOf(HvacEngineMissingArgumentException.class);
        assertThatThrownBy(() -> CoolingStrategy.of(flowOfHumidAir, coolantData, nullTemperature))
                .isInstanceOf(HvacEngineMissingArgumentException.class);
        assertThatThrownBy(() -> CoolingStrategy.of(flowOfHumidAir, coolantData, relativeHumidity))
                .isInstanceOf(HvacEngineMissingArgumentException.class);

        assertThatThrownBy(() -> CoolingStrategy.of(flowOfHumidAir, nullCoolantData, examplePower))
                .isInstanceOf(HvacEngineMissingArgumentException.class);
        assertThatThrownBy(() -> CoolingStrategy.of(nullFlow, coolantData, exampleTemperature))
                .isInstanceOf(HvacEngineMissingArgumentException.class);
    }

    @Test
    @DisplayName("CoolantData: should throw exception when arguments exceeds limits")
    void shouldThrowExceptionWhenArgumentsExceedsLimitsCoolantData() {
        Temperature exampleTemp5 = Temperature.ofCelsius(5);
        Temperature exampleTemp90 = Temperature.ofCelsius(90);
        Temperature exampleTemp0 = Temperature.ofCelsius(0);
        Temperature exampleTemp14 = Temperature.ofCelsius(14);

        assertThatThrownBy(() -> CoolantData.of(exampleTemp5, exampleTemp90))
                .isInstanceOf(HvacEngineArgumentException.class);
        assertThatThrownBy(() -> CoolantData.of(exampleTemp0, exampleTemp14))
                .isInstanceOf(HvacEngineArgumentException.class);
    }

    @Test
    @DisplayName("CoolingStrategy: should throw exception when arguments exceeds limits")
    void shouldThrowExceptionWhenArgumentsExceedsLimitsCoolingStrategy() {
        HumidAir humidAir = HumidAir.of(Pressure.STANDARD_ATMOSPHERE, Temperature.ofCelsius(35), RelativeHumidity.ofPercentage(50));
        FlowOfHumidAir flowOfHumidAir = FlowOfHumidAir.of(humidAir, VolumetricFlow.ofCubicMetersPerHour(5000));
        CoolantData coolantData = CoolantData.of(Temperature.ofCelsius(7), Temperature.ofCelsius(14));

        Power lowPower = Power.ofKiloWatts(20);
        assertThatThrownBy(() -> CoolingStrategy.of(flowOfHumidAir, coolantData, lowPower))
                .isInstanceOf(HvacEngineArgumentException.class);
        Power lowNegativePower = Power.ofKiloWatts(-200);
        assertThatThrownBy(() -> CoolingStrategy.of(flowOfHumidAir, coolantData, lowNegativePower))
                .isInstanceOf(HvacEngineArgumentException.class);
        RelativeHumidity relativeHumidity49 = RelativeHumidity.ofPercentage(49);
        assertThatThrownBy(() -> CoolingStrategy.of(flowOfHumidAir, coolantData, relativeHumidity49))
                .isInstanceOf(HvacEngineArgumentException.class);
        RelativeHumidity relativeHumidity1 = RelativeHumidity.ofPercentage(-1);
        assertThatThrownBy(() -> CoolingStrategy.of(flowOfHumidAir, coolantData, relativeHumidity1))
                .isInstanceOf(HvacEngineArgumentException.class);
        RelativeHumidity relativeHumidity101 = RelativeHumidity.ofPercentage(101);
        assertThatThrownBy(() -> CoolingStrategy.of(flowOfHumidAir, coolantData, relativeHumidity101))
                .isInstanceOf(HvacEngineArgumentException.class);
        Temperature temperature36 = Temperature.ofCelsius(36);
        assertThatThrownBy(() -> CoolingStrategy.of(flowOfHumidAir, coolantData, temperature36))
                .isInstanceOf(HvacEngineArgumentException.class);
        Temperature temperature0 = Temperature.ofCelsius(0);
        assertThatThrownBy(() -> CoolingStrategy.of(flowOfHumidAir, coolantData, temperature0))
                .isInstanceOf(HvacEngineArgumentException.class);
        Temperature temperature1 = Temperature.ofCelsius(-1);
        assertThatThrownBy(() -> CoolingStrategy.of(flowOfHumidAir, coolantData, temperature1))
                .isInstanceOf(HvacEngineArgumentException.class);
    }

    @Test
    @DisplayName("HeatingStrategy: should throw exception when arguments exceeds limits")
    void shouldThrowExceptionWhenArgumentsExceedsLimitsHeatingStrategy() {
        HumidAir humidAir = HumidAir.of(Pressure.STANDARD_ATMOSPHERE, Temperature.ofCelsius(-20), RelativeHumidity.ofPercentage(80));
        FlowOfHumidAir flowOfHumidAir = FlowOfHumidAir.of(humidAir, VolumetricFlow.ofCubicMetersPerHour(5000));

        Power power20 = Power.ofKiloWatts(-20);
        assertThatThrownBy(() -> HeatingStrategy.of(flowOfHumidAir, power20))
                .isInstanceOf(HvacEngineArgumentException.class);
        Power power500 = Power.ofKiloWatts(500);
        assertThatThrownBy(() -> HeatingStrategy.of(flowOfHumidAir, power500))
                .isInstanceOf(HvacEngineArgumentException.class);
        RelativeHumidity relativeHumidity81 = RelativeHumidity.ofPercentage(81);
        assertThatThrownBy(() -> HeatingStrategy.of(flowOfHumidAir, relativeHumidity81))
                .isInstanceOf(HvacEngineArgumentException.class);
        RelativeHumidity relativeHumidity1 = RelativeHumidity.ofPercentage(-1);
        assertThatThrownBy(() -> HeatingStrategy.of(flowOfHumidAir, relativeHumidity1))
                .isInstanceOf(HvacEngineArgumentException.class);
        RelativeHumidity relativeHumidity101 = RelativeHumidity.ofPercentage(101);
        assertThatThrownBy(() -> HeatingStrategy.of(flowOfHumidAir, relativeHumidity101))
                .isInstanceOf(HvacEngineArgumentException.class);
        Temperature temperature21 = Temperature.ofCelsius(-21);
        assertThatThrownBy(() -> HeatingStrategy.of(flowOfHumidAir, temperature21))
                .isInstanceOf(HvacEngineArgumentException.class);
        Temperature maxTemperatureLimitExceeded = HumidAir.TEMPERATURE_MAX_LIMIT.plus(1);
        assertThatThrownBy(() -> HeatingStrategy.of(flowOfHumidAir, maxTemperatureLimitExceeded))
                .isInstanceOf(HvacEngineArgumentException.class);
    }

    @Test
    @DisplayName("MixingStrategy: should throw exception when arguments exceeds limits")
    void shouldThrowExceptionWhenArgumentsExceedsLimitsMixingStrategy() {
        HumidAir inletAir = HumidAir.of(Pressure.STANDARD_ATMOSPHERE, Temperature.ofCelsius(-20),
                RelativeHumidity.ofPercentage(80));
        FlowOfHumidAir inletFlow = FlowOfHumidAir.of(inletAir, FlowOfHumidAir.MASS_FLOW_MAX_LIMIT);
        HumidAir recircAir1 = HumidAir.of(Pressure.STANDARD_ATMOSPHERE, Temperature.ofCelsius(-20),
                RelativeHumidity.ofPercentage(80));
        FlowOfHumidAir recircFlow1 = FlowOfHumidAir.of(recircAir1, VolumetricFlow.ofCubicMetersPerHour(1));
        HumidAir recircAir2 = HumidAir.of(Pressure.STANDARD_ATMOSPHERE, Temperature.ofCelsius(-20),
                RelativeHumidity.ofPercentage(80));

        FlowOfHumidAir recircFlow2 = FlowOfHumidAir.of(recircAir2, VolumetricFlow.ofCubicMetersPerHour(1));
        List<FlowOfHumidAir> recirculationFlows = List.of(recircFlow1, recircFlow2);

        assertThatThrownBy(() -> MixingStrategy.of(inletFlow, recircFlow1))
                .isInstanceOf(HvacEngineArgumentException.class);
        assertThatThrownBy(() -> MixingStrategy.of(inletFlow, recirculationFlows))
                .isInstanceOf(HvacEngineArgumentException.class);
    }
*/
}