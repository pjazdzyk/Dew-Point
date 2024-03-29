package com.synerset.hvacengine.common;

import com.synerset.hvacengine.common.exception.HvacEngineArgumentException;
import com.synerset.hvacengine.common.exception.HvacEngineMissingArgumentException;
import com.synerset.hvacengine.fluids.dryair.DryAir;
import com.synerset.hvacengine.fluids.dryair.FlowOfDryAir;
import com.synerset.hvacengine.fluids.humidair.FlowOfHumidAir;
import com.synerset.hvacengine.fluids.humidair.HumidAir;
import com.synerset.hvacengine.fluids.liquidwater.FlowOfLiquidWater;
import com.synerset.hvacengine.fluids.liquidwater.LiquidWater;
import com.synerset.hvacengine.fluids.watervapour.FlowOfWaterVapour;
import com.synerset.hvacengine.fluids.watervapour.WaterVapour;
import com.synerset.hvacengine.utils.Defaults;
import com.synerset.unitility.unitsystem.flow.MassFlow;
import com.synerset.unitility.unitsystem.flow.VolumetricFlow;
import com.synerset.unitility.unitsystem.thermodynamic.Pressure;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FlowLimitsTest {

    @Test
    @DisplayName("Flows: should throw exception when null passed as argument")
    void shouldThrowNullPointerExceptionWhenNullIsPassedAsArgumentFlows() {
        DryAir dryAir = DryAir.of(Pressure.STANDARD_ATMOSPHERE, Defaults.INDOOR_SUMMER_TEMP);
        LiquidWater liquidWater = LiquidWater.of(Pressure.STANDARD_ATMOSPHERE, Defaults.INDOOR_SUMMER_TEMP);
        WaterVapour waterVapour = WaterVapour.of(Pressure.STANDARD_ATMOSPHERE, Defaults.INDOOR_SUMMER_TEMP);
        HumidAir humidAir = HumidAir.of(Pressure.STANDARD_ATMOSPHERE, Defaults.INDOOR_SUMMER_TEMP, Defaults.INDOOR_SUMMER_RH);
        MassFlow massFlow = MassFlow.ofKilogramsPerSecond(1);
        MassFlow nullMassFlow = null;
        VolumetricFlow nullVolFlow = null;

        assertThatThrownBy(() -> FlowOfDryAir.of(dryAir, nullMassFlow)).isInstanceOf(HvacEngineMissingArgumentException.class);
        assertThatThrownBy(() -> FlowOfDryAir.of(dryAir, nullVolFlow)).isInstanceOf(HvacEngineMissingArgumentException.class);
        assertThatThrownBy(() -> FlowOfDryAir.of(null, massFlow)).isInstanceOf(HvacEngineMissingArgumentException.class);

        assertThatThrownBy(() -> FlowOfLiquidWater.of(liquidWater, nullMassFlow)).isInstanceOf(HvacEngineMissingArgumentException.class);
        assertThatThrownBy(() -> FlowOfLiquidWater.of(liquidWater, nullVolFlow)).isInstanceOf(HvacEngineMissingArgumentException.class);
        assertThatThrownBy(() -> FlowOfLiquidWater.of(null, massFlow)).isInstanceOf(HvacEngineMissingArgumentException.class);

        assertThatThrownBy(() -> FlowOfWaterVapour.of(waterVapour, nullMassFlow)).isInstanceOf(HvacEngineMissingArgumentException.class);
        assertThatThrownBy(() -> FlowOfWaterVapour.of(waterVapour, nullVolFlow)).isInstanceOf(HvacEngineMissingArgumentException.class);
        assertThatThrownBy(() -> FlowOfWaterVapour.of(null, massFlow)).isInstanceOf(HvacEngineMissingArgumentException.class);

        assertThatThrownBy(() -> FlowOfHumidAir.of(humidAir, nullMassFlow)).isInstanceOf(HvacEngineMissingArgumentException.class);
        assertThatThrownBy(() -> FlowOfHumidAir.of(humidAir, nullVolFlow)).isInstanceOf(HvacEngineMissingArgumentException.class);
        assertThatThrownBy(() -> FlowOfHumidAir.of(null, massFlow)).isInstanceOf(HvacEngineMissingArgumentException.class);
        assertThatThrownBy(() -> FlowOfHumidAir.ofDryAirMassFlow(null, massFlow)).isInstanceOf(HvacEngineMissingArgumentException.class);
        assertThatThrownBy(() -> FlowOfHumidAir.ofDryAirMassFlow(humidAir, null)).isInstanceOf(HvacEngineMissingArgumentException.class);
    }

    @Test
    @DisplayName("FlowOfDryAir: should throw exception when arguments exceeds limits")
    void shouldThrowExceptionWhenArgumentsExceedsLimitsDryAir() {
        DryAir dryAir = DryAir.of(Pressure.STANDARD_ATMOSPHERE, Defaults.INDOOR_SUMMER_TEMP);
        MassFlow exceededMinLimit = MassFlow.ofKilogramsPerSecond(-1);
        MassFlow exceededMaxLimit = MassFlow.ofKilogramsPerSecond(5E9 + 1);
        assertThatThrownBy(() -> FlowOfDryAir.of(dryAir, exceededMinLimit))
                .isInstanceOf(HvacEngineArgumentException.class);
        assertThatThrownBy(() -> FlowOfDryAir.of(dryAir, exceededMaxLimit))
                .isInstanceOf(HvacEngineArgumentException.class);
    }

    @Test
    @DisplayName("FlowOfWater: should throw exception when arguments exceeds limits")
    void shouldThrowExceptionWhenArgumentsExceedsLimitsWater() {
        LiquidWater liquidWater = LiquidWater.of(Pressure.STANDARD_ATMOSPHERE, Defaults.INDOOR_SUMMER_TEMP);
        MassFlow exceededMinLimit = MassFlow.ofKilogramsPerSecond(-1);
        MassFlow exceededMaxLimit = MassFlow.ofKilogramsPerSecond(5E9 + 1);
        assertThatThrownBy(() -> FlowOfLiquidWater.of(liquidWater, exceededMinLimit))
                .isInstanceOf(HvacEngineArgumentException.class);
        assertThatThrownBy(() -> FlowOfLiquidWater.of(liquidWater, exceededMaxLimit))
                .isInstanceOf(HvacEngineArgumentException.class);
    }

    @Test
    @DisplayName("HumidAir: should throw exception when arguments exceeds limits")
    void shouldThrowExceptionWhenArgumentsExceedsLimitsHumidAir() {
        HumidAir humidAir = HumidAir.of(Pressure.STANDARD_ATMOSPHERE, Defaults.INDOOR_SUMMER_TEMP, Defaults.INDOOR_SUMMER_RH);
        MassFlow exceededMinLimit = MassFlow.ofKilogramsPerSecond(-1);
        MassFlow exceededMaxLimit = MassFlow.ofKilogramsPerSecond(5E9 + 1);
        assertThatThrownBy(() -> FlowOfHumidAir.of(humidAir, exceededMinLimit))
                .isInstanceOf(HvacEngineArgumentException.class);
        assertThatThrownBy(() -> FlowOfHumidAir.of(humidAir, exceededMaxLimit))
                .isInstanceOf(HvacEngineArgumentException.class);
    }

}