package com.synerset.hvacengine.process.heating;

import com.synerset.hvacengine.process.ProcessType;
import com.synerset.hvacengine.process.heating.dataobject.HeatingResult;
import com.synerset.hvacengine.process.source.SimpleDataSource;
import com.synerset.hvacengine.property.fluids.humidair.FlowOfHumidAir;
import com.synerset.hvacengine.property.fluids.humidair.HumidAir;
import com.synerset.hvacengine.property.fluids.humidair.HumidAirEquations;
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
 * Section: 2.1, page: 37
 */
class HeatingBlockTest {

    private static final HumidAir TEST_HUMID_AIR = HumidAir.of(
            Pressure.ofHectoPascal(987),
            Temperature.ofCelsius(10),
            RelativeHumidity.ofPercentage(60)
    );

    private static final FlowOfHumidAir TEST_INLET_AIR_FLOW = FlowOfHumidAir.of(TEST_HUMID_AIR, MassFlow.ofKilogramsPerHour(10_000));
    private static final SimpleDataSource<FlowOfHumidAir> TEST_INLET_FLOW_SOURCE = SimpleDataSource.of(TEST_INLET_AIR_FLOW);

    @Test
    @DisplayName("Heating node: should heat up inlet air when heating power is given")
    void shouldHeatUpInletAirWhenInputPowerIsGiven() {
        // Given
        Power inputPower = Power.ofKiloWatts(56);
        SimpleDataSource<Power> powerSource = new SimpleDataSource<>(inputPower);
        
        Temperature expectedTemperature = Temperature.ofCelsius(30);
        RelativeHumidity expectedRH = RelativeHumidity.ofPercentage(17.4);

        // When
        HeatingFromPower heatingFromPowerBlock = HeatingFromPower.of(TEST_INLET_FLOW_SOURCE, powerSource);
        HeatingResult processResults = heatingFromPowerBlock.runProcessCalculations();

        // Then
        assertThat(processResults).isNotNull();
        assertThat(processResults.outletAirFlow()).isNotNull();
        assertThat(processResults.inletAirFlow()).isEqualTo(TEST_INLET_AIR_FLOW);
        assertThat(processResults.heatOfProcess()).isEqualTo(inputPower);
        assertThat(processResults.processType()).isEqualTo(ProcessType.HEATING);
        assertThat(processResults.processMode()).isEqualTo(HeatingMode.FROM_POWER);

        FlowOfHumidAir outletAirFlow = processResults.outletAirFlow();
        assertThat(outletAirFlow.getPressure()).isEqualTo(TEST_HUMID_AIR.getPressure());
        assertThat(outletAirFlow.getTemperature().getInCelsius()).isEqualTo(expectedTemperature.getInCelsius(), withPrecision(3.5E-2));
        assertThat(outletAirFlow.getRelativeHumidity().getInPercent()).isEqualTo(expectedRH.getInPercent(), withPrecision(1.5E-2));
        assertThat(outletAirFlow.getHumidityRatio()).isEqualTo(TEST_INLET_AIR_FLOW.getHumidityRatio());
        SpecificEnthalpy expectedEnthalpy = HumidAirEquations.specificEnthalpy(outletAirFlow.getTemperature(), outletAirFlow.getHumidityRatio(), outletAirFlow.getPressure());
        assertThat(outletAirFlow.getSpecificEnthalpy()).isEqualTo(expectedEnthalpy);
    }

    @Test
    @DisplayName("Heating node: should heat up inlet air when target temperature is given")
    void shouldHeatUpInletAirWhenTargetTemperatureIsGiven() {
        // Given
        Temperature targetTemperature = Temperature.ofCelsius(29.96581041061914);
        SimpleDataSource<Temperature> temperatureSource = SimpleDataSource.of(targetTemperature);

        Power expectedPower = Power.ofKiloWatts(56).toWatts();
        RelativeHumidity expectedRH = RelativeHumidity.ofPercentage(17.386707253107);

        // When
        HeatingFromTemperature heatingFromTemperatureBlock = HeatingFromTemperature.of(TEST_INLET_FLOW_SOURCE, temperatureSource);
        HeatingResult processResults = heatingFromTemperatureBlock.runProcessCalculations();

        // Then
        assertThat(processResults).isNotNull();
        assertThat(processResults.outletAirFlow()).isNotNull();
        assertThat(processResults.inletAirFlow()).isEqualTo(TEST_INLET_AIR_FLOW);
        assertThat(processResults.heatOfProcess().getValue()).isEqualTo(expectedPower.getInWatts(), withPrecision(1E-10));
        assertThat(processResults.processType()).isEqualTo(ProcessType.HEATING);
        assertThat(processResults.processMode()).isEqualTo(HeatingMode.FROM_TEMPERATURE);

        FlowOfHumidAir outletAirFlow = processResults.outletAirFlow();
        assertThat(outletAirFlow.getPressure()).isEqualTo(TEST_HUMID_AIR.getPressure());
        assertThat(outletAirFlow.getTemperature().getInCelsius()).isEqualTo(targetTemperature.getInCelsius(), withPrecision(3.5E-2));
        assertThat(outletAirFlow.getRelativeHumidity().getInPercent()).isEqualTo(expectedRH.getInPercent(), withPrecision(1.5E-2));
        assertThat(outletAirFlow.getHumidityRatio()).isEqualTo(TEST_INLET_AIR_FLOW.getHumidityRatio());
        SpecificEnthalpy expectedEnthalpy = HumidAirEquations.specificEnthalpy(outletAirFlow.getTemperature(), outletAirFlow.getHumidityRatio(), outletAirFlow.getPressure());
        assertThat(outletAirFlow.getSpecificEnthalpy()).isEqualTo(expectedEnthalpy);
    }

    @Test
    @DisplayName("Heating node: should heat up inlet air when target relative humidity is given")
    void shouldHeatUpInletAirWhenTargetRelativeHumidityIsGiven() {
        // Given
        RelativeHumidity targetRH = RelativeHumidity.ofPercentage(17.386707253107);
        SimpleDataSource<RelativeHumidity> humiditySource = SimpleDataSource.of(targetRH);

        Power expectedPower = Power.ofKiloWatts(56).toWatts();
        Temperature expectedTemperature = Temperature.ofCelsius(29.96581041061914);

        // When
        HeatingFromHumidity heatingFromHumidityBlock = HeatingFromHumidity.of(TEST_INLET_FLOW_SOURCE, humiditySource);
        HeatingResult processResults = heatingFromHumidityBlock.runProcessCalculations();

        // Then
        assertThat(processResults).isNotNull();
        assertThat(processResults.outletAirFlow()).isNotNull();
        assertThat(processResults.inletAirFlow()).isEqualTo(TEST_INLET_AIR_FLOW);
        assertThat(processResults.heatOfProcess().getValue()).isEqualTo(expectedPower.getValue(), withPrecision(1E-9));
        assertThat(processResults.processType()).isEqualTo(ProcessType.HEATING);
        assertThat(processResults.processMode()).isEqualTo(HeatingMode.FROM_HUMIDITY);

        FlowOfHumidAir outletAirFlow = processResults.outletAirFlow();
        assertThat(outletAirFlow.getPressure()).isEqualTo(TEST_HUMID_AIR.getPressure());
        assertThat(outletAirFlow.getTemperature().getInCelsius()).isEqualTo(expectedTemperature.getInCelsius(), withPrecision(1E-10));
        assertThat(outletAirFlow.getRelativeHumidity().getInPercent()).isEqualTo(targetRH.getInPercent());
        assertThat(outletAirFlow.getHumidityRatio()).isEqualTo(TEST_INLET_AIR_FLOW.getHumidityRatio());
        SpecificEnthalpy expectedEnthalpy = HumidAirEquations.specificEnthalpy(outletAirFlow.getTemperature(), outletAirFlow.getHumidityRatio(), outletAirFlow.getPressure());
        assertThat(outletAirFlow.getSpecificEnthalpy()).isEqualTo(expectedEnthalpy);
    }

}