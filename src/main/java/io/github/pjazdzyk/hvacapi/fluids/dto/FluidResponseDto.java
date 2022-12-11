package io.github.pjazdzyk.hvacapi.fluids.dto;

public record FluidResponseDto(double pressure,
                               double temp,
                               double density,
                               double specHeatCp,
                               double specEnthalpy,
                               String classOfFluid) {
}