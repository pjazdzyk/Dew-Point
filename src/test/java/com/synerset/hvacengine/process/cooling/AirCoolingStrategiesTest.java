package com.synerset.hvacengine.process.cooling;

import com.synerset.hvacengine.fluids.humidair.FlowOfHumidAir;
import com.synerset.hvacengine.fluids.humidair.HumidAir;
import com.synerset.unitility.unitsystem.dimensionless.BypassFactor;
import com.synerset.unitility.unitsystem.flow.MassFlow;
import com.synerset.unitility.unitsystem.humidity.HumidityRatio;
import com.synerset.unitility.unitsystem.humidity.RelativeHumidity;
import com.synerset.unitility.unitsystem.thermodynamic.Power;
import com.synerset.unitility.unitsystem.thermodynamic.Pressure;
import com.synerset.unitility.unitsystem.thermodynamic.Temperature;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.withPrecision;

class AirCoolingStrategiesTest {
/*
    private static final CoolantData COOLANT_DATA = CoolantData.of(Temperature.ofCelsius(9), Temperature.ofCelsius(14));
    private static FlowOfHumidAir inletFlow;

    @BeforeAll
    static void setUp() {
        HumidAir inputAir = HumidAir.of(
                Pressure.ofPascal(100_000),
                Temperature.ofCelsius(34.0),
                RelativeHumidity.ofPercentage(40.0)
        );

        inletFlow = FlowOfHumidAir.ofDryAirMassFlow(inputAir, MassFlow.ofKilogramsPerSecond(1.0));
    }

    // REAL COOLING
    @Test
    @DisplayName("should cool down inlet air when target outlet temperature and average wall temperature of cooling coil is given")
    void processOfRealCooling_shouldCoolDownInletAir_whenTargetOutletAirTempAndAverageCoilWallTempAreGiven() {
        // Given
        Temperature expectedOutAirTemp = Temperature.ofCelsius(17.0);

        HumidityRatio expectedOutHumRatio = HumidityRatio.ofKilogramPerKilogram(0.009772748723824064);
        Power expectedHeatOfProcess = Power.ofWatts(-26835.191594387037);
        MassFlow expectedCondensateFlow = MassFlow.ofKilogramsPerSecond(0.0037604402299109005);

        // When
        AirCoolingResult airCoolingResult = CoolingStrategy.of(inletFlow, COOLANT_DATA, expectedOutAirTemp).applyCooling();
        Power actualHeatOfProcess = airCoolingResult.heatOfProcess();
        Temperature actualOutAirTemp = airCoolingResult.outletFlow().getTemperature();
        HumidityRatio actualHumRatio = airCoolingResult.outletFlow().getHumidityRatio();
        Temperature actualCondensateTemp = airCoolingResult.condensateFlow().getTemperature();
        MassFlow actualCondensateFlow = airCoolingResult.condensateFlow().getMassFlow();

        // Then
        assertThat(actualHeatOfProcess).isEqualTo(expectedHeatOfProcess);
        assertThat(actualOutAirTemp).isEqualTo(expectedOutAirTemp);
        assertThat(actualHumRatio).isEqualTo(expectedOutHumRatio);
        assertThat(actualCondensateTemp).isEqualTo(COOLANT_DATA.getAverageTemperature());
        assertThat(actualCondensateFlow).isEqualTo(expectedCondensateFlow);
    }

    @Test
    @DisplayName("should cool down inlet air when target relative humidity and average wall temperature of cooling coil is given")
    void processOfRealCooling_shouldCoolDownInletAir_whenTargetOutletRHAndAverageCoilWallTempAreGiven() {
        // Given
        RelativeHumidity expectedRelativeHumidity = RelativeHumidity.ofPercentage(79.82572722353957);

        Temperature expectedOutAirTemp = Temperature.ofCelsius(17.0);
        HumidityRatio expectedOutHumRatio = HumidityRatio.ofKilogramPerKilogram(0.009772748723824064);
        Power expectedHeatOfProcess = Power.ofWatts(-26835.191594387037);
        MassFlow expectedCondensateFlow = MassFlow.ofKilogramsPerSecond(0.0037604402299109005);

        // When
        AirCoolingResult airCoolingResult = CoolingStrategy.of(inletFlow, COOLANT_DATA, expectedRelativeHumidity).applyCooling();
        Power actualHeatOfProcess = airCoolingResult.heatOfProcess();
        Temperature actualOutAirTemp = airCoolingResult.outletFlow().getTemperature();
        RelativeHumidity actualRelativeHumidity = airCoolingResult.outletFlow().getRelativeHumidity();
        HumidityRatio actualHumRatio = airCoolingResult.outletFlow().getHumidityRatio();
        Temperature actualCondensateTemp = airCoolingResult.condensateFlow().getTemperature();
        MassFlow actualCondensateFlow = airCoolingResult.condensateFlow().getMassFlow();

        // Then
        assertThat(actualHeatOfProcess.getInWatts()).isEqualTo(expectedHeatOfProcess.getInWatts(), withPrecision(1E-10));
        assertThat(actualOutAirTemp.getValue()).isEqualTo(expectedOutAirTemp.getValue(), withPrecision(1E-13));
        assertThat(actualRelativeHumidity.getInPercent()).isEqualTo(expectedRelativeHumidity.getInPercent(), withPrecision(1E-13));
        assertThat(actualHumRatio.getValue()).isEqualTo(expectedOutHumRatio.getValue(), withPrecision(1E-16));
        assertThat(actualCondensateTemp).isEqualTo(COOLANT_DATA.getAverageTemperature());
        assertThat(actualCondensateFlow.getInKilogramsPerSecond()).isEqualTo(expectedCondensateFlow.getInKilogramsPerSecond(), withPrecision(1E-16));
    }

    @Test
    @DisplayName("should cool down inlet air when input heat and average wall temperature of cooling coil is given")
    void processOfRealCooling_shouldCoolDownInletAir_whenHeatOfProcessIsGiven() {
        // Given
        Power expectedHeatOfProcess = Power.ofWatts(-26835.191594387037);

        Temperature expectedOutAirTemp = Temperature.ofCelsius(17.0);
        HumidityRatio expectedOutHumRatio = HumidityRatio.ofKilogramPerKilogram(0.009772748723824064);
        MassFlow expectedCondensateFlow = MassFlow.ofKilogramsPerSecond(0.0037604402299109005);

        // When
        AirCoolingResult airCoolingResult = CoolingStrategy.of(inletFlow, COOLANT_DATA, expectedHeatOfProcess).applyCooling();
        Power actualHeatOfProcess = airCoolingResult.heatOfProcess();
        Temperature actualOutAirTemp = airCoolingResult.outletFlow().getTemperature();
        HumidityRatio actualHumRatio = airCoolingResult.outletFlow().getHumidityRatio();
        Temperature actualCondensateTemp = airCoolingResult.condensateFlow().getTemperature();
        MassFlow actualCondensateFlow = airCoolingResult.condensateFlow().getMassFlow();

        // Then
        assertThat(actualHeatOfProcess).isEqualTo(expectedHeatOfProcess);
        assertThat(actualOutAirTemp).isEqualTo(expectedOutAirTemp);
        assertThat(actualHumRatio).isEqualTo(expectedOutHumRatio);
        assertThat(actualCondensateTemp).isEqualTo(COOLANT_DATA.getAverageTemperature());
        assertThat(actualCondensateFlow).isEqualTo(expectedCondensateFlow);
    }

    // TOOLS
    @Test
    @DisplayName("should return average cooling coil temperature when coolant supply and return temperatures are given")
    void averageWallTemp_shouldCalculateAverageCoolingCoilTemperature_whenCoolantInletAndOutletTempsAreGiven() {
        // Given
        Temperature coolantSupplyTemp = Temperature.ofCelsius(6.0);
        Temperature coolantReturnTemp = Temperature.ofCelsius(12.0);
        Temperature expectedCoilAverageTemp = Temperature.ofCelsius(9.0);

        // When
        Temperature actualCoilAverageTemp = CoolantData.averageWallTemp(coolantSupplyTemp, coolantReturnTemp);

        // Then
        assertThat(actualCoilAverageTemp).isEqualTo(expectedCoilAverageTemp);
    }

    @Test
    @DisplayName("should return by-pass factor when average cooling coil temperature, inlet air temperature and expected outlet temperature are given")
    void coilBypassFactor_shouldReturnBypassFactor_whenAverageCoilTemperatureInletAirTemperatureAndExpectedOutletTemperatureAreGiven() {
        // Given
        Temperature expectedCoilAverageTemp = Temperature.ofCelsius(9.0);
        Temperature inletAirTemperature = Temperature.ofCelsius(30.0);
        Temperature expectedOutTemp = Temperature.ofCelsius(11);
        BypassFactor expectedBypassFactor = BypassFactor.of(0.0952380952380952380952380952381);

        // When
        BypassFactor actualBypassFactor = CoolingHelpers.coilBypassFactor(expectedCoilAverageTemp, inletAirTemperature, expectedOutTemp);

        // Then
        assertThat(actualBypassFactor).isEqualTo(expectedBypassFactor);
    }

    @Test
    @DisplayName("should return condensate mass flow when dry air mass flow, inlet humidity ratio and outlet humidity ratio are given")
    void condensateDischarge_shouldReturnCondensateMassFlow_whenDryAirMassFlowInletHumidityRatioAndOutletHumidityRatioAreGiven() {
        // Given
        MassFlow dryAirMassFlow = MassFlow.ofKilogramsPerSecond(1.5);
        HumidityRatio inletHumidityRatio = HumidityRatio.ofKilogramPerKilogram(0.03);
        HumidityRatio outletHumidityRatio = HumidityRatio.ofKilogramPerKilogram(0.0099);
        MassFlow expectedCondensateFlow = MassFlow.ofKilogramsPerSecond(0.03015);

        // When
        MassFlow actualCondensateFlow = CoolingHelpers.condensateDischarge(dryAirMassFlow, inletHumidityRatio, outletHumidityRatio);

        // Then
        assertThat(actualCondensateFlow).isEqualTo(expectedCondensateFlow);
    }
*/
}