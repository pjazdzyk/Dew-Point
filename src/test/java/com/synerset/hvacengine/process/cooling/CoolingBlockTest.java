package com.synerset.hvacengine.process.cooling;

import com.synerset.hvacengine.process.ProcessType;
import com.synerset.hvacengine.process.cooling.dataobject.CoolingResult;
import com.synerset.hvacengine.process.source.SimpleDataSource;
import com.synerset.hvacengine.property.fluids.humidair.FlowOfHumidAir;
import com.synerset.hvacengine.property.fluids.humidair.HumidAir;
import com.synerset.hvacengine.property.fluids.humidair.HumidAirEquations;
import com.synerset.hvacengine.property.fluids.liquidwater.FlowOfLiquidWater;
import com.synerset.hvacengine.property.fluids.liquidwater.LiquidWaterEquations;
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
class CoolingBlockTest {

    public static HumidAir TEST_HUMID_AIR = HumidAir.of(
            Pressure.ofHectoPascal(987),
            Temperature.ofCelsius(34),
            RelativeHumidity.ofPercentage(40)
    );
    public static FlowOfHumidAir TEST_INLET_AIR_FLOW = FlowOfHumidAir.of(TEST_HUMID_AIR, MassFlow.ofKilogramsPerHour(10_000));
    private static final SimpleDataSource<FlowOfHumidAir> TEST_INLET_FLOW_SOURCE = SimpleDataSource.of(TEST_INLET_AIR_FLOW);

    @Test
    @DisplayName("Cooling node: should cool air inlet air when target temperature is given")
    void shouldCoolInletAirWhenTargetTemperatureIsGiven() {
        // Given
        CoolantData coolantData = CoolantData.of(Temperature.ofCelsius(9), Temperature.ofCelsius(14));
        SimpleDataSource<CoolantData> coolantDataSource = SimpleDataSource.of(coolantData);

        Temperature targetTemperature = Temperature.ofCelsius(17);
        SimpleDataSource<Temperature> temperatureDataSource = SimpleDataSource.of(targetTemperature);

        Power expectedPower = Power.ofKiloWatts(-73.89739524318315).toWatts();
        RelativeHumidity expectedRH = RelativeHumidity.ofPercentage(79.82650656031903);
        MassFlow expectedCondensateFlow = MassFlow.ofKilogramsPerSecond(0.010444945743262712);

        // When
        CoolingFromTemperature coolingBlock = CoolingFromTemperature.of(TEST_INLET_FLOW_SOURCE, coolantDataSource, temperatureDataSource);
        CoolingResult processResults = coolingBlock.runProcessCalculations();

        // Then
        assertThat(processResults).isNotNull();
        assertThat(processResults.outletAirFlow()).isNotNull();
        assertThat(processResults.inletAirFlow()).isEqualTo(TEST_INLET_AIR_FLOW);
        assertThat(processResults.heatOfProcess().getInWatts()).isEqualTo(expectedPower.getInWatts(), withPrecision(1E-9));
        assertThat(processResults.averageCoilWallTemperature()).isEqualTo(Temperature.ofCelsius(11.5));
        assertThat(processResults.processType()).isEqualTo(ProcessType.COOLING);
        assertThat(processResults.processMode()).isEqualTo(CoolingMode.FROM_TEMPERATURE);

        FlowOfHumidAir outletAirFlow = processResults.outletAirFlow();
        assertThat(outletAirFlow.getPressure()).isEqualTo(TEST_HUMID_AIR.getPressure());
        assertThat(outletAirFlow.getTemperature().getInCelsius()).isEqualTo(targetTemperature.getInCelsius(), withPrecision(3.5E-2));
        assertThat(outletAirFlow.getRelativeHumidity().getInPercent()).isEqualTo(expectedRH.getInPercent(), withPrecision(1.5E-2));

        SpecificEnthalpy expectedEnthalpy = HumidAirEquations.specificEnthalpy(outletAirFlow.getTemperature(), outletAirFlow.getHumidityRatio(), outletAirFlow.getPressure());
        assertThat(outletAirFlow.getSpecificEnthalpy()).isEqualTo(expectedEnthalpy);

        BypassFactor expectedBypassFactor = CoolingEquations.coilBypassFactor(coolantData.getAverageTemperature(), TEST_HUMID_AIR.getTemperature(), outletAirFlow.getTemperature());
        assertThat(processResults.bypassFactor()).isEqualTo(expectedBypassFactor);
        assertThat(processResults.coolantSupplyFlow().getTemperature()).isEqualTo(coolantData.getSupplyTemperature());
        assertThat(processResults.coolantReturnFlow().getTemperature()).isEqualTo(coolantData.getReturnTemperature());

        FlowOfLiquidWater condensateFlow = processResults.condensateFlow();
        assertThat(condensateFlow.getTemperature()).isEqualTo(coolantData.getAverageTemperature());
        assertThat(condensateFlow.getMassFlow()).isEqualTo(expectedCondensateFlow);
        assertThat(condensateFlow.getSpecificEnthalpy()).isEqualTo(LiquidWaterEquations.specificEnthalpy(coolantData.getAverageTemperature()));
    }

    @Test
    @DisplayName("Cooling node: should cool air inlet air when heating power is given")
    void shouldHCoolInletAirWhenInputPowerIsGiven() {
        // Given
        Power inputPower = Power.ofKiloWatts(-73.89739524318315).toWatts();
        SimpleDataSource<Power> powerDataSource = SimpleDataSource.of(inputPower);

        CoolantData coolantData = CoolantData.of(Temperature.ofCelsius(9), Temperature.ofCelsius(14));
        SimpleDataSource<CoolantData> coolantDataSource = SimpleDataSource.of(coolantData);

        Temperature expectedTemperature = Temperature.ofCelsius(17);
        RelativeHumidity expectedRH = RelativeHumidity.ofPercentage(79.82650656031903);
        MassFlow expectedCondensateFlow = MassFlow.ofKilogramsPerSecond(0.010444945743262712);

        // When
        CoolingFromPower coolingFromPowerBlock = CoolingFromPower.of(TEST_INLET_FLOW_SOURCE, coolantDataSource, powerDataSource);
        CoolingResult processResults = coolingFromPowerBlock.runProcessCalculations();

        // Then
        assertThat(processResults).isNotNull();
        assertThat(processResults.outletAirFlow()).isNotNull();
        assertThat(processResults.inletAirFlow()).isEqualTo(TEST_INLET_AIR_FLOW);
        assertThat(processResults.heatOfProcess()).isEqualTo(inputPower);
        assertThat(processResults.averageCoilWallTemperature()).isEqualTo(Temperature.ofCelsius(11.5));
        assertThat(processResults.processType()).isEqualTo(ProcessType.COOLING);
        assertThat(processResults.processMode()).isEqualTo(CoolingMode.FROM_POWER);

        FlowOfHumidAir outletAirFlow = processResults.outletAirFlow();
        assertThat(outletAirFlow.getPressure()).isEqualTo(TEST_HUMID_AIR.getPressure());
        assertThat(outletAirFlow.getTemperature().getValue()).isEqualTo(expectedTemperature.getValue(), withPrecision(3.5E-2));
        assertThat(outletAirFlow.getRelativeHumidity()).isEqualTo(expectedRH);
        SpecificEnthalpy expectedEnthalpy = HumidAirEquations.specificEnthalpy(outletAirFlow.getTemperature(), outletAirFlow.getHumidityRatio(), outletAirFlow.getPressure());
        assertThat(outletAirFlow.getSpecificEnthalpy()).isEqualTo(expectedEnthalpy);

        BypassFactor expectedBypassFactor = CoolingEquations.coilBypassFactor(coolantData.getAverageTemperature(), TEST_HUMID_AIR.getTemperature(), outletAirFlow.getTemperature());
        assertThat(processResults.bypassFactor()).isEqualTo(expectedBypassFactor);
        assertThat(processResults.coolantSupplyFlow().getTemperature()).isEqualTo(coolantData.getSupplyTemperature());
        assertThat(processResults.coolantReturnFlow().getTemperature()).isEqualTo(coolantData.getReturnTemperature());

        FlowOfLiquidWater condensateFlow = processResults.condensateFlow();
        assertThat(condensateFlow.getTemperature()).isEqualTo(coolantData.getAverageTemperature());
        assertThat(condensateFlow.getMassFlow()).isEqualTo(expectedCondensateFlow);
        assertThat(condensateFlow.getSpecificEnthalpy()).isEqualTo(LiquidWaterEquations.specificEnthalpy(coolantData.getAverageTemperature()));
    }

    @Test
    @DisplayName("Cooling node: should cool air inlet air when target relative humidity is given")
    void shouldCoolInletAirWhenTargetRelativeHumidityIsGiven() {
        // Given
        CoolantData coolantData = CoolantData.of(Temperature.ofCelsius(9), Temperature.ofCelsius(14));
        SimpleDataSource<CoolantData> coolantDataSource = SimpleDataSource.of(coolantData);

        RelativeHumidity targetRH = RelativeHumidity.ofPercentage(79.82650656031903);
        SimpleDataSource<RelativeHumidity> relativeHumidityDataSource = SimpleDataSource.of(targetRH);

        Temperature expectedTemperature = Temperature.ofCelsius(17);
        Power expectedPower = Power.ofKiloWatts(-73.89739524318315).toWatts();
        MassFlow expectedCondensateFlow = MassFlow.ofKilogramsPerSecond(0.010444945743501704);

        // When
        CoolingFromHumidity coolingFromHumidityBlock = CoolingFromHumidity.of(TEST_INLET_FLOW_SOURCE, coolantDataSource, relativeHumidityDataSource);
        CoolingResult processResults = coolingFromHumidityBlock.runProcessCalculations();
        assertThat(processResults.averageCoilWallTemperature()).isEqualTo(Temperature.ofCelsius(11.5));

        // Then
        assertThat(processResults).isNotNull();
        assertThat(processResults.outletAirFlow()).isNotNull();
        assertThat(processResults.inletAirFlow()).isEqualTo(TEST_INLET_AIR_FLOW);
        assertThat(processResults.heatOfProcess().getInWatts()).isEqualTo(expectedPower.getInWatts(), withPrecision(1.8E-6));
        assertThat(processResults.processType()).isEqualTo(ProcessType.COOLING);
        assertThat(processResults.processMode()).isEqualTo(CoolingMode.FROM_HUMIDITY);

        FlowOfHumidAir outletAirFlow = processResults.outletAirFlow();
        assertThat(outletAirFlow.getPressure()).isEqualTo(TEST_HUMID_AIR.getPressure());
        assertThat(outletAirFlow.getTemperature().getInCelsius()).isEqualTo(expectedTemperature.getInCelsius(), withPrecision(3.5E-2));
        assertThat(outletAirFlow.getRelativeHumidity().getInPercent()).isEqualTo(targetRH.getInPercent(), withPrecision(1.5E-2));
        SpecificEnthalpy expectedEnthalpy = HumidAirEquations.specificEnthalpy(outletAirFlow.getTemperature(), outletAirFlow.getHumidityRatio(), outletAirFlow.getPressure());
        assertThat(outletAirFlow.getSpecificEnthalpy()).isEqualTo(expectedEnthalpy);

        BypassFactor expectedBypassFactor = CoolingEquations.coilBypassFactor(coolantData.getAverageTemperature(), TEST_HUMID_AIR.getTemperature(), outletAirFlow.getTemperature());
        assertThat(processResults.bypassFactor()).isEqualTo(expectedBypassFactor);
        assertThat(processResults.coolantSupplyFlow().getTemperature()).isEqualTo(coolantData.getSupplyTemperature());
        assertThat(processResults.coolantReturnFlow().getTemperature()).isEqualTo(coolantData.getReturnTemperature());

        FlowOfLiquidWater condensateFlow = processResults.condensateFlow();
        assertThat(condensateFlow.getTemperature()).isEqualTo(coolantData.getAverageTemperature());
        assertThat(condensateFlow.getMassFlow().getValue()).isEqualTo(expectedCondensateFlow.getValue(), withPrecision(1E-11));
        assertThat(condensateFlow.getSpecificEnthalpy()).isEqualTo(LiquidWaterEquations.specificEnthalpy(coolantData.getAverageTemperature()));
    }

    @Test
    @DisplayName("Cooling node: should cool inlet air, but for unrealistic cooling power, algorithm should fall back to temperature of the coil wall")
    void shouldCoolInletAirButForToLargePowerShouldFallBackToCoilWallTemp() {
        // Given
        Power inputPower = Power.ofKiloWatts(-200).toWatts();
        SimpleDataSource<Power> powerDataSource = SimpleDataSource.of(inputPower);

        CoolantData coolantData = CoolantData.of(Temperature.ofCelsius(9), Temperature.ofCelsius(14));
        SimpleDataSource<CoolantData> coolantDataSource = SimpleDataSource.of(coolantData);

        // When
        CoolingFromPower coolingFromPowerBlock = CoolingFromPower.of(TEST_INLET_FLOW_SOURCE, coolantDataSource, powerDataSource);
        CoolingResult processResults = coolingFromPowerBlock.runProcessCalculations();

        // Then
        assertThat(processResults).isNotNull();
        assertThat(processResults.outletAirFlow().getTemperature().getInCelsius()).isEqualTo(12.625);
    }

}