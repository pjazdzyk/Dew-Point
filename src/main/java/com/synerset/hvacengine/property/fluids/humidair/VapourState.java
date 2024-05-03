package com.synerset.hvacengine.property.fluids.humidair;

/**
 * Enum representing the state of water vapor in humid air.
 */
public enum VapourState {
    /**
     * Represents unsaturated humid air, where the humidity is below the saturation point.
     */
    UNSATURATED,

    /**
     * Represents saturated humid air, where the humidity is at the saturation point.
     */
    SATURATED,

    /**
     * Represents the state of water mist, which is tiny water droplets suspended in the air.
     */
    WATER_MIST,

    /**
     * Represents the state of ice fog, which is tiny ice crystals suspended in the air.
     */
    ICE_FOG
}
