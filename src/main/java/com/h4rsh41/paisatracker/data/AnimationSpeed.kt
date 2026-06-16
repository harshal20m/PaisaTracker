package com.h4rsh41.paisatracker.data

/**
 * Defines the speed/duration of page transition animations.
 * Each speed provides a different duration in milliseconds.
 */
enum class AnimationSpeed(val durationMs: Int) {
    /**
     * Instant - No animation delay (0ms).
     * Useful for accessibility or when animations are disabled.
     */
    INSTANT(0),

    /**
     * Fast - Quick transitions (150ms).
     * Snappy and responsive, good for frequent navigation.
     */
    FAST(150),

    /**
     * Normal - Standard speed (300ms) - Recommended default.
     * Balanced between speed and smoothness.
     */
    NORMAL(300),

    /**
     * Slow - Deliberate transitions (500ms).
     * More noticeable animations, good for showcasing effects.
     */
    SLOW(500);

    companion object {
        /**
         * Returns the default animation speed for the app.
         */
        fun default() = NORMAL

        /**
         * Returns a user-friendly display name for the animation speed.
         */
        fun AnimationSpeed.displayName(): String = when (this) {
            INSTANT -> "Instant"
            FAST -> "Fast"
            NORMAL -> "Normal"
            SLOW -> "Slow"
        }

        /**
         * Returns a description for the animation speed.
         */
        fun AnimationSpeed.description(): String = when (this) {
            INSTANT -> "No delay (0ms)"
            FAST -> "Quick and snappy (150ms)"
            NORMAL -> "Balanced speed (300ms)"
            SLOW -> "Smooth and deliberate (500ms)"
        }

        /**
         * Converts a slider value (0-3) to AnimationSpeed.
         * Useful for UI sliders with 4 positions.
         */
        fun fromSliderValue(value: Int): AnimationSpeed = when (value) {
            0 -> INSTANT
            1 -> FAST
            2 -> NORMAL
            3 -> SLOW
            else -> default()
        }

        /**
         * Converts AnimationSpeed to slider value (0-3).
         * Useful for UI sliders with 4 positions.
         */
        fun AnimationSpeed.toSliderValue(): Int = when (this) {
            INSTANT -> 0
            FAST -> 1
            NORMAL -> 2
            SLOW -> 3
        }
    }
}

// Made with Bob
