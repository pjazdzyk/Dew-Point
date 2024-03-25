package com.synerset.hvacengine.process.cooling;

import com.synerset.hvacengine.fluids.humidair.FlowOfHumidAir;
import com.synerset.hvacengine.fluids.humidair.HumidAir;
import com.synerset.hvacengine.fluids.humidair.HumidAirEquations;
import com.synerset.hvacengine.fluids.liquidwater.LiquidWaterEquations;
import com.synerset.unitility.unitsystem.dimensionless.BypassFactor;
import com.synerset.unitility.unitsystem.flow.MassFlow;
import com.synerset.unitility.unitsystem.humidity.RelativeHumidity;
import com.synerset.unitility.unitsystem.thermodynamic.Power;
import com.synerset.unitility.unitsystem.thermodynamic.Pressure;
import com.synerset.unitility.unitsystem.thermodynamic.SpecificEnthalpy;
import com.synerset.unitility.unitsystem.thermodynamic.Temperature;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.withPrecision;

/**
 * This test case is based on example from technical literature:
 * B.Lipska - Projektowanie Wentylacji i Klimatyzacji, Podstawy uzdatniania powietrza. Gliwice 2014.
 * Section: 2.5, page: 57
 */
class CoolingTest {
/*
    @Test
    @DisplayName("Cooling: should heat up inlet air when target temperature is given")
    void shouldCoolInletAirWhenTargetTemperatureIsGiven() {
        // Given
        HumidAir humidAir = HumidAir.of(
                Pressure.ofHectoPascal(987),
                Temperature.ofCelsius(34),
                RelativeHumidity.ofPercentage(40)
        );

        FlowOfHumidAir inletAir = FlowOfHumidAir.of(humidAir, MassFlow.ofKilogramsPerHour(10_000));
        CoolantData coolantData = CoolantData.of(Temperature.ofCelsius(9), Temperature.ofCelsius(14));
        Temperature targetTemperature = Temperature.ofCelsius(17);
        CoolingStrategy coolingStrategy = CoolingStrategy.of(inletAir, coolantData, targetTemperature);
        Power expectedPower = Power.ofKiloWatts(-73.89739524318315).toWatts();
        RelativeHumidity expectedRH = RelativeHumidity.ofPercentage(79.82650656031903);
        MassFlow expectedCondensateFlow = MassFlow.ofKilogramsPerSecond(0.010444945743262712);

        // When
        Cooling cooling = Cooling.of(coolingStrategy);

        // Then
        assertThat(cooling).isNotNull();
        assertThat(cooling.getOutletAir()).isNotNull();
        assertThat(cooling.getInputInletAir()).isEqualTo(inletAir);
        assertThat(cooling.getHeatOfProcess().getInWatts()).isEqualTo(expectedPower.getInWatts(), withPrecision(1E-9));
        assertThat(cooling.getCoolingStrategy()).isEqualTo(coolingStrategy);
        assertThat(cooling.getOutletPressure()).isEqualTo(humidAir.getPressure());
        assertThat(cooling.getOutletTemperature().getInCelsius()).isEqualTo(targetTemperature.getInCelsius(), withPrecision(3.5E-2));
        assertThat(cooling.getOutletRelativeHumidity().getInPercent()).isEqualTo(expectedRH.getInPercent(), withPrecision(1.5E-2));
        SpecificEnthalpy expectedEnthalpy = HumidAirEquations.specificEnthalpy(cooling.getOutletTemperature(), cooling.getOutletHumidityRatio(), cooling.getOutletPressure());
        assertThat(cooling.getOutletSpecificEnthalpy()).isEqualTo(expectedEnthalpy);
        // Cooling specific assertions
        BypassFactor expectedBypassFactor = CoolingHelpers.coilBypassFactor(coolantData.getAverageTemperature(), humidAir.getTemperature(), cooling.getOutletTemperature());
        assertThat(cooling.getBypassFactor()).isEqualTo(expectedBypassFactor);
        assertThat(cooling.getCoolantData()).isEqualTo(coolantData);
        // Condensate specific assertions
        assertThat(cooling.getCondensateTemperature()).isEqualTo(coolantData.getAverageTemperature());
        assertThat(cooling.getCondensateFlow().getMassFlow()).isEqualTo(expectedCondensateFlow);
        assertThat(cooling.getCondensateEnthalpy()).isEqualTo(LiquidWaterEquations.specificEnthalpy(coolantData.getAverageTemperature()));
    }

    @Test
    @DisplayName("Cooling: should heat up inlet air when heating power is given")
    void shouldHCoolInletAirWhenInputPowerIsGiven() {
        // Given
        HumidAir humidAir = HumidAir.of(
                Pressure.ofHectoPascal(987),
                Temperature.ofCelsius(34),
                RelativeHumidity.ofPercentage(40)
        );

        FlowOfHumidAir inletAir = FlowOfHumidAir.of(humidAir, MassFlow.ofKilogramsPerHour(10_000));
        Power inputPower = Power.ofKiloWatts(-73.89739524318315).toWatts();
        CoolantData coolantData = CoolantData.of(Temperature.ofCelsius(9), Temperature.ofCelsius(14));
        CoolingStrategy coolingStrategy = CoolingStrategy.of(inletAir, coolantData, inputPower);

        Temperature expectedTemperature = Temperature.ofCelsius(17);
        RelativeHumidity expectedRH = RelativeHumidity.ofPercentage(79.82650656031903);
        MassFlow expectedCondensateFlow = MassFlow.ofKilogramsPerSecond(0.010444945743262712);

        // When
        Cooling cooling = Cooling.of(coolingStrategy);

        // Then
        assertThat(cooling).isNotNull();
        assertThat(cooling.getOutletAir()).isNotNull();
        assertThat(cooling.getInputInletAir()).isEqualTo(inletAir);
        assertThat(cooling.getHeatOfProcess()).isEqualTo(inputPower);
        assertThat(cooling.getCoolingStrategy()).isEqualTo(coolingStrategy);
        assertThat(cooling.getOutletPressure()).isEqualTo(humidAir.getPressure());
        assertThat(cooling.getOutletTemperature().getValue()).isEqualTo(expectedTemperature.getValue(), withPrecision(3.5E-2));
        assertThat(cooling.getOutletRelativeHumidity()).isEqualTo(expectedRH);
        SpecificEnthalpy expectedEnthalpy = HumidAirEquations.specificEnthalpy(cooling.getOutletTemperature(), cooling.getOutletHumidityRatio(), cooling.getOutletPressure());
        assertThat(cooling.getOutletSpecificEnthalpy()).isEqualTo(expectedEnthalpy);
        // Cooling specific assertions
        BypassFactor expectedBypassFactor = CoolingHelpers.coilBypassFactor(coolantData.getAverageTemperature(), humidAir.getTemperature(), cooling.getOutletTemperature());
        assertThat(cooling.getBypassFactor()).isEqualTo(expectedBypassFactor);
        assertThat(cooling.getCoolantData()).isEqualTo(coolantData);
        // Condensate specific assertions
        assertThat(cooling.getCondensateTemperature()).isEqualTo(coolantData.getAverageTemperature());
        assertThat(cooling.getCondensateFlow().getMassFlow()).isEqualTo(expectedCondensateFlow);
        assertThat(cooling.getCondensateEnthalpy()).isEqualTo(LiquidWaterEquations.specificEnthalpy(coolantData.getAverageTemperature()));
    }

    @Test
    @DisplayName("Cooling: should heat up inlet air when target relative humidity is given")
    void shouldCoolInletAirWhenTargetRelativeHumidityIsGiven() {
        // Given
        HumidAir humidAir = HumidAir.of(
                Pressure.ofHectoPascal(987),
                Temperature.ofCelsius(34),
                RelativeHumidity.ofPercentage(40)
        );

        FlowOfHumidAir inletAir = FlowOfHumidAir.of(humidAir, MassFlow.ofKilogramsPerHour(10_000));
        CoolantData coolantData = CoolantData.of(Temperature.ofCelsius(9), Temperature.ofCelsius(14));
        RelativeHumidity targetRH = RelativeHumidity.ofPercentage(79.82650656031903);
        CoolingStrategy coolingStrategy = CoolingStrategy.of(inletAir, coolantData, targetRH);
        Temperature expectedTemperature = Temperature.ofCelsius(17);
        Power expectedPower = Power.ofKiloWatts(-73.89739524318315).toWatts();
        MassFlow expectedCondensateFlow = MassFlow.ofKilogramsPerSecond(0.010444945743262712);

        // When
        Cooling cooling = Cooling.of(coolingStrategy);

        // Then
        assertThat(cooling).isNotNull();
        assertThat(cooling.getOutletAir()).isNotNull();
        assertThat(cooling.getInputInletAir()).isEqualTo(inletAir);
        assertThat(cooling.getHeatOfProcess().getInWatts()).isEqualTo(expectedPower.getInWatts(), withPrecision(1.8E-6));
        assertThat(cooling.getCoolingStrategy()).isEqualTo(coolingStrategy);
        assertThat(cooling.getOutletPressure()).isEqualTo(humidAir.getPressure());
        assertThat(cooling.getOutletTemperature().getInCelsius()).isEqualTo(expectedTemperature.getInCelsius(), withPrecision(3.5E-2));
        assertThat(cooling.getOutletRelativeHumidity().getInPercent()).isEqualTo(targetRH.getInPercent(), withPrecision(1.5E-2));
        SpecificEnthalpy expectedEnthalpy = HumidAirEquations.specificEnthalpy(cooling.getOutletTemperature(), cooling.getOutletHumidityRatio(), cooling.getOutletPressure());
        assertThat(cooling.getOutletSpecificEnthalpy()).isEqualTo(expectedEnthalpy);
        // Cooling specific assertions
        BypassFactor expectedBypassFactor = CoolingHelpers.coilBypassFactor(coolantData.getAverageTemperature(), humidAir.getTemperature(), cooling.getOutletTemperature());
        assertThat(cooling.getBypassFactor()).isEqualTo(expectedBypassFactor);
        assertThat(cooling.getCoolantData()).isEqualTo(coolantData);
        // Condensate specific assertions
        assertThat(cooling.getCondensateTemperature()).isEqualTo(coolantData.getAverageTemperature());
        assertThat(cooling.getCondensateFlow().getMassFlow().getInKilogramsPerSecond()).isEqualTo(expectedCondensateFlow.getInKilogramsPerSecond(), withPrecision(1E-8));
        assertThat(cooling.getCondensateEnthalpy()).isEqualTo(LiquidWaterEquations.specificEnthalpy(coolantData.getAverageTemperature()));
    }
*/
}