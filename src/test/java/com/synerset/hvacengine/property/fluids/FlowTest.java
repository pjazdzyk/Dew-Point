package com.synerset.hvacengine.property.fluids;

import com.synerset.hvacengine.property.fluids.liquidwater.FlowOfLiquidWater;
import com.synerset.hvacengine.property.fluids.liquidwater.LiquidWater;
import com.synerset.unitility.unitsystem.flow.MassFlow;
import com.synerset.unitility.unitsystem.thermodynamic.Temperature;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class FlowTest {

    @Test
    @DisplayName("should return true for equals with precision")
    void shouldReturnTrueIfEqualsWithPrecision() {
        // Given
        LiquidWater water = LiquidWater.of(Temperature.ofCelsius(10));
        MassFlow firstMassFlow = MassFlow.ofKilogramsPerSecond(11.12345);
        FlowOfLiquidWater firstFlowOfLiquidWater = FlowOfLiquidWater.of(water, firstMassFlow);
        MassFlow secondMassFlow = MassFlow.ofKilogramsPerSecond(11.123456789);
        FlowOfLiquidWater secondFlowOfLiquidWater = FlowOfLiquidWater.of(water, secondMassFlow);

        // When
        boolean equalsResult = firstFlowOfLiquidWater.equals(secondFlowOfLiquidWater);
        boolean equalsWithPrecision = firstFlowOfLiquidWater.isEqualsWithPrecision(secondFlowOfLiquidWater, 1E-5);
        boolean equalsWithPrecisionExceeded = firstFlowOfLiquidWater.isEqualsWithPrecision(secondFlowOfLiquidWater, 1E-6);

        // Then
        assertThat(equalsResult).isFalse();
        assertThat(equalsWithPrecision).isTrue();
        assertThat(equalsWithPrecisionExceeded).isFalse();
    }

}
