package com.synerset.hvacengine.common;

import com.synerset.hvacengine.exception.HvacEngineArgumentException;
import com.synerset.hvacengine.exception.HvacEngineMissingArgumentException;
import com.synerset.hvacengine.solids.ice.Ice;
import com.synerset.unitility.unitsystem.thermodynamic.Pressure;
import com.synerset.unitility.unitsystem.thermodynamic.Temperature;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SolidLimitsTest {

    @Test
    @DisplayName("Ice: should throw exception when null passed as argument")
    void shouldThrowNullPointerExceptionWhenNullIsPassedAsArgumentSolids() {
        assertThatThrownBy(() -> Ice.of(Pressure.STANDARD_ATMOSPHERE, null))
                .isInstanceOf(HvacEngineMissingArgumentException.class);
        Temperature temperature20 = Temperature.ofCelsius(-20);
        assertThatThrownBy(() -> Ice.of(null, temperature20))
                .isInstanceOf(HvacEngineMissingArgumentException.class);
    }

    @Test
    @DisplayName("Ice: should throw exception when arguments exceeds limits")
    void shouldThrowExceptionWhenArgumentsExceedsLimitsLiquidWater() {
        Pressure pressureMaxLimit = Pressure.ofPascal(0);
        Temperature temperatureMinLimit = Temperature.ofCelsius(-150).minus(1);
        Temperature temperature20 = Temperature.ofCelsius(-20);
        assertThatThrownBy(() -> Ice.of(pressureMaxLimit, temperature20))
                .isInstanceOf(HvacEngineArgumentException.class);

        assertThatThrownBy(() -> Ice.of(Pressure.STANDARD_ATMOSPHERE, temperatureMinLimit))
                .isInstanceOf(HvacEngineArgumentException.class);
    }

}
