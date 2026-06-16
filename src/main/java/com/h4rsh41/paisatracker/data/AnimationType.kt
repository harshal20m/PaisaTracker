package com.h4rsh41.paisatracker.data

/**
 * Defines the types of page transition animations available in the app.
 * Each type provides a different visual effect when navigating between screens.
 */
enum class AnimationType {
    /**
     * No animation - instant transition between screens.
     * Best for users who prefer reduced motion or have accessibility needs.
     */
    NONE,

    /**
     * Slide animation - screens slide horizontally.
     * Classic navigation pattern, slides right on forward navigation, left on back.
     */
    SLIDE,

    /**
     * Fade animation - screens fade in and out.
     * Subtle and smooth, good for users sensitive to motion.
     */
    FADE,

    /**
     * Scale animation - screens scale up/down with fade.
     * Modern effect with depth perception.
     */
    SCALE,

    /**
     * Slide + Fade combination - recommended default.
     * Combines horizontal slide with fade for smooth, polished transitions.
     */
    SLIDE_FADE,

    /**
     * Elevation animation - Material Design elevation change.
     * Subtle depth perception with minimal motion.
     */
    ELEVATION,

    /**
     * Shared Axis - Material 3 recommended transition.
     * Smooth axis-based movement following Material Design guidelines.
     */
    SHARED_AXIS;

    companion object {
        /**
         * Returns the default animation type for the app.
         */
        fun default() = SLIDE_FADE

        /**
         * Returns a user-friendly display name for the animation type.
         */
        fun AnimationType.displayName(): String = when (this) {
            NONE -> "None (Instant)"
            SLIDE -> "Slide"
            FADE -> "Fade"
            SCALE -> "Scale"
            SLIDE_FADE -> "Slide + Fade (Recommended)"
            ELEVATION -> "Elevation"
            SHARED_AXIS -> "Shared Axis (Material 3)"
        }

        /**
         * Returns a description for the animation type.
         */
        fun AnimationType.description(): String = when (this) {
            NONE -> "No animation, instant transitions"
            SLIDE -> "Classic horizontal slide"
            FADE -> "Smooth fade in/out"
            SCALE -> "Scale with depth effect"
            SLIDE_FADE -> "Best balance of smooth and clear"
            ELEVATION -> "Subtle Material Design depth"
            SHARED_AXIS -> "Modern Material 3 motion"
        }
    }
}

// Made with Bob
