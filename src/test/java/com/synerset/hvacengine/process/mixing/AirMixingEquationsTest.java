package com.synerset.hvacengine.process.mixing;

import com.synerset.hvacengine.process.mixing.dataobject.MixingResult;
import com.synerset.hvacengine.property.fluids.humidair.FlowOfHumidAir;
import com.synerset.hvacengine.property.fluids.humidair.HumidAir;
import com.synerset.hvacengine.property.fluids.humidair.HumidAirEquations;
import com.synerset.unitility.unitsystem.flow.MassFlow;
import com.synerset.unitility.unitsystem.humidity.HumidityRatio;
import com.synerset.unitility.unitsystem.humidity.RelativeHumidity;
import com.synerset.unitility.unitsystem.thermodynamic.Pressure;
import com.synerset.unitility.unitsystem.thermodynamic.Temperature;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.withPrecision;

class AirMixingEquationsTest {

    @Test
    @DisplayName("Mixing equations: should return results for mixing of two different moist air flows")
    void calcMixing_shouldReturnResultsForMixingOfTwoDifferentMoistAirFlows() {
        // Given
        double p_atm = 100_000d;
        double mda_in = 5000d / 3600d;
        double mda_rec = mda_in;
        double mda_out = mda_in + mda_rec;

        HumidAir air_in = HumidAir.of(Pressure.ofPascal(p_atm),
                Temperature.ofCelsius(-20.0),
                RelativeHumidity.ofPercentage(100.0));
        FlowOfHumidAir airFlow_in = FlowOfHumidAir.ofDryAirMassFlow(air_in, MassFlow.ofKilogramsPerSecond(mda_in));

        HumidAir air_rec = HumidAir.of(Pressure.ofPascal(p_atm),
                Temperature.ofCelsius(18.0),
                RelativeHumidity.ofPercentage(55.0));
        FlowOfHumidAir airFlow_rec = FlowOfHumidAir.ofDryAirMassFlow(air_rec, MassFlow.ofKilogramsPerSecond(mda_rec));

        double x_in = air_in.getHumidityRatio().getValue();
        double x_rec = air_rec.getHumidityRatio().getValue();
        double x_out = (mda_in * x_in + mda_rec * x_rec) / mda_out;
        double i_in = air_in.getSpecificEnthalpy().getValue();
        double i_rec = air_rec.getSpecificEnthalpy().getValue();
        double i_out = (mda_in * i_in + mda_rec * i_rec) / mda_out;
        double expectedOutAirTempVal = HumidAirEquations.dryBulbTemperatureIX(i_out, x_out, p_atm);
        HumidityRatio expectedHumidityRatio = HumidityRatio.ofKilogramPerKilogram(x_out);
        Temperature expectedOutTemp = Temperature.ofCelsius(expectedOutAirTempVal);

        // When
        MixingResult mixingResult = MixingEquations.mixingOfTwoAirFlows(airFlow_in, airFlow_rec);
        FlowOfHumidAir actualOutletFlow = mixingResult.outletAirFlow();

        MassFlow actualOutDryAirMassFlow = actualOutletFlow.getDryAirMassFlow();
        Temperature actualOutAirTemp = actualOutletFlow.getTemperature();
        HumidityRatio actualOutHumidityRatio = actualOutletFlow.getHumidityRatio();

        // Then
        assertThat(actualOutDryAirMassFlow.getInKilogramsPerSecond()).isEqualTo(mda_out);
        assertThat(actualOutHumidityRatio).isEqualTo(expectedHumidityRatio);
        assertThat(actualOutAirTemp).isEqualTo(expectedOutTemp);
    }

    @Test
    @DisplayName("Mixing equations: should mix multiple flows together")
    void mixMultipleHumidAirFlows_shouldMixMultipleFlowsTogether() {
        // Given
        FlowOfHumidAir inletFlow = FlowOfHumidAir.ofValues(-20, 99, 1000);
        FlowOfHumidAir recircFlow_1 = FlowOfHumidAir.ofValues(0, 80, 1000);
        FlowOfHumidAir recircFlow_2 = FlowOfHumidAir.ofValues(20, 50, 1000);
        Temperature expectedTemp = Temperature.ofCelsius(-1.0000413789265845);
        RelativeHumidity expectedRH = RelativeHumidity.ofPercentage(99.48335594756662);
        MassFlow expectedDryAirMassFlow = MassFlow.ofKilogramsPerSecond(inletFlow.getDryAirMassFlow()
                .plus(recircFlow_1.getDryAirMassFlow())
                .plus(recircFlow_2.getDryAirMassFlow())
                .getValue());

        // When
        MixingResult mixingResult = MixingEquations.mixingOfMultipleFlows(inletFlow, recircFlow_1, recircFlow_2);
        FlowOfHumidAir actualOutletFlow = mixingResult.outletAirFlow();

        // Then
        assertThat(actualOutletFlow.getTemperature()).isEqualTo(expectedTemp);
        assertThat(actualOutletFlow.getRelativeHumidity().getInPercent()).isEqualTo(expectedRH.getInPercent(), withPrecision(1E-11));
        assertThat(actualOutletFlow.getDryAirMassFlow()).isEqualTo(expectedDryAirMassFlow);

    }

}