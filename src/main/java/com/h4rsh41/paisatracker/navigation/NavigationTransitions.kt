package com.h4rsh41.paisatracker.navigation

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.ui.unit.IntOffset
import com.h4rsh41.paisatracker.data.AnimationSpeed
import com.h4rsh41.paisatracker.data.AnimationType

/**
 * Provides page transition animations for navigation.
 * All transitions are optimized for 60fps performance.
 */
object NavigationTransitions {

    /**
     * Creates a tween animation spec with the given duration.
     */
    private fun <T> tweenSpec(durationMs: Int): TweenSpec<T> = tween(
        durationMillis = durationMs,
        easing = FastOutSlowInEasing
    )

    /**
     * Creates a spring animation spec for smooth, natural motion.
     */
    private fun <T> springSpec(): SpringSpec<T> = spring(
        dampingRatio = Spring.DampingRatioMediumBouncy,
        stiffness = Spring.StiffnessLow
    )

    // ==================== SLIDE TRANSITION ====================

    /**
     * Classic horizontal slide transition.
     * - Forward: Slide in from right
     * - Back: Slide in from left
     */
    fun slideTransition(speed: AnimationSpeed): TransitionSpec {
        val duration = speed.durationMs
        return TransitionSpec(
            enter = slideInHorizontally(
                initialOffsetX = { fullWidth -> fullWidth },
                animationSpec = tweenSpec(duration)
            ),
            exit = slideOutHorizontally(
                targetOffsetX = { fullWidth -> -fullWidth / 3 },
                animationSpec = tweenSpec(duration)
            ),
            popEnter = slideInHorizontally(
                initialOffsetX = { fullWidth -> -fullWidth / 3 },
                animationSpec = tweenSpec(duration)
            ),
            popExit = slideOutHorizontally(
                targetOffsetX = { fullWidth -> fullWidth },
                animationSpec = tweenSpec(duration)
            )
        )
    }

    // ==================== FADE TRANSITION ====================

    /**
     * Simple fade in/out transition.
     * Subtle and smooth, good for motion-sensitive users.
     */
    fun fadeTransition(speed: AnimationSpeed): TransitionSpec {
        val duration = speed.durationMs
        return TransitionSpec(
            enter = fadeIn(animationSpec = tweenSpec(duration)),
            exit = fadeOut(animationSpec = tweenSpec(duration)),
            popEnter = fadeIn(animationSpec = tweenSpec(duration)),
            popExit = fadeOut(animationSpec = tweenSpec(duration))
        )
    }

    // ==================== SCALE TRANSITION ====================

    /**
     * Scale transition with fade.
     * Creates depth perception with zoom effect.
     */
    fun scaleTransition(speed: AnimationSpeed): TransitionSpec {
        val duration = speed.durationMs
        return TransitionSpec(
            enter = scaleIn(
                initialScale = 0.9f,
                animationSpec = tweenSpec(duration)
            ) + fadeIn(animationSpec = tweenSpec(duration)),
            exit = scaleOut(
                targetScale = 0.95f,
                animationSpec = tweenSpec(duration)
            ) + fadeOut(animationSpec = tweenSpec(duration)),
            popEnter = scaleIn(
                initialScale = 0.95f,
                animationSpec = tweenSpec(duration)
            ) + fadeIn(animationSpec = tweenSpec(duration)),
            popExit = scaleOut(
                targetScale = 0.9f,
                animationSpec = tweenSpec(duration)
            ) + fadeOut(animationSpec = tweenSpec(duration))
        )
    }

    // ==================== SLIDE + FADE TRANSITION (RECOMMENDED) ====================

    /**
     * Combination of slide and fade - recommended default.
     * Provides smooth, polished navigation with clear direction.
     */
    fun slideFadeTransition(speed: AnimationSpeed): TransitionSpec {
        val duration = speed.durationMs
        return TransitionSpec(
            enter = slideInHorizontally(
                initialOffsetX = { fullWidth -> fullWidth },
                animationSpec = tweenSpec(duration)
            ) + fadeIn(
                animationSpec = tween(durationMillis = duration, delayMillis = duration / 4)
            ),
            exit = slideOutHorizontally(
                targetOffsetX = { fullWidth -> -fullWidth / 4 },
                animationSpec = tweenSpec(duration)
            ) + fadeOut(
                animationSpec = tweenSpec(duration)
            ),
            popEnter = slideInHorizontally(
                initialOffsetX = { fullWidth -> -fullWidth / 4 },
                animationSpec = tweenSpec(duration)
            ) + fadeIn(
                animationSpec = tween(durationMillis = duration, delayMillis = duration / 4)
            ),
            popExit = slideOutHorizontally(
                targetOffsetX = { fullWidth -> fullWidth },
                animationSpec = tweenSpec(duration)
            ) + fadeOut(
                animationSpec = tweenSpec(duration)
            )
        )
    }

    // ==================== ELEVATION TRANSITION ====================

    /**
     * Material Design elevation-based transition.
     * Subtle depth change with minimal motion.
     */
    fun elevationTransition(speed: AnimationSpeed): TransitionSpec {
        val duration = speed.durationMs
        return TransitionSpec(
            enter = fadeIn(
                animationSpec = tweenSpec(duration)
            ) + scaleIn(
                initialScale = 0.98f,
                animationSpec = tweenSpec(duration)
            ),
            exit = fadeOut(
                animationSpec = tweenSpec(duration)
            ) + scaleOut(
                targetScale = 0.98f,
                animationSpec = tweenSpec(duration)
            ),
            popEnter = fadeIn(
                animationSpec = tweenSpec(duration)
            ) + scaleIn(
                initialScale = 0.98f,
                animationSpec = tweenSpec(duration)
            ),
            popExit = fadeOut(
                animationSpec = tweenSpec(duration)
            ) + scaleOut(
                targetScale = 0.98f,
                animationSpec = tweenSpec(duration)
            )
        )
    }

    // ==================== SHARED AXIS TRANSITION (MATERIAL 3) ====================

    /**
     * Material 3 shared axis transition.
     * Smooth axis-based movement following Material Design guidelines.
     * Uses Z-axis motion for forward/back navigation.
     */
    fun sharedAxisTransition(speed: AnimationSpeed): TransitionSpec {
        val duration = speed.durationMs
        return TransitionSpec(
            enter = slideInVertically(
                initialOffsetY = { fullHeight -> fullHeight / 10 },
                animationSpec = tweenSpec(duration)
            ) + fadeIn(
                animationSpec = tween(durationMillis = duration, delayMillis = duration / 3)
            ) + scaleIn(
                initialScale = 0.95f,
                animationSpec = tweenSpec(duration)
            ),
            exit = slideOutVertically(
                targetOffsetY = { fullHeight -> -fullHeight / 10 },
                animationSpec = tweenSpec(duration)
            ) + fadeOut(
                animationSpec = tweenSpec(duration)
            ) + scaleOut(
                targetScale = 0.95f,
                animationSpec = tweenSpec(duration)
            ),
            popEnter = slideInVertically(
                initialOffsetY = { fullHeight -> -fullHeight / 10 },
                animationSpec = tweenSpec(duration)
            ) + fadeIn(
                animationSpec = tween(durationMillis = duration, delayMillis = duration / 3)
            ) + scaleIn(
                initialScale = 0.95f,
                animationSpec = tweenSpec(duration)
            ),
            popExit = slideOutVertically(
                targetOffsetY = { fullHeight -> fullHeight / 10 },
                animationSpec = tweenSpec(duration)
            ) + fadeOut(
                animationSpec = tweenSpec(duration)
            ) + scaleOut(
                targetScale = 0.95f,
                animationSpec = tweenSpec(duration)
            )
        )
    }

    // ==================== NO TRANSITION ====================

    /**
     * No animation - instant transition.
     * For accessibility or when animations are disabled.
     */
    fun noTransition(): TransitionSpec {
        return TransitionSpec(
            enter = EnterTransition.None,
            exit = ExitTransition.None,
            popEnter = EnterTransition.None,
            popExit = ExitTransition.None
        )
    }
}

/**
 * Data class to hold all four transition states.
 */
data class TransitionSpec(
    val enter: EnterTransition,
    val exit: ExitTransition,
    val popEnter: EnterTransition,
    val popExit: ExitTransition
)

/**
 * Provides the appropriate transition based on animation type and speed.
 * This is the main entry point for getting transitions.
 */
object TransitionProvider {
    
    /**
     * Gets the transition spec for the given animation type and speed.
     * 
     * @param type The type of animation to use
     * @param speed The speed/duration of the animation
     * @param enabled Whether animations are enabled (if false, returns no transition)
     * @return TransitionSpec containing enter/exit transitions
     */
    fun getTransition(
        type: AnimationType,
        speed: AnimationSpeed,
        enabled: Boolean = true
    ): TransitionSpec {
        // If animations are disabled or speed is INSTANT, return no transition
        if (!enabled || speed == AnimationSpeed.INSTANT) {
            return NavigationTransitions.noTransition()
        }

        return when (type) {
            AnimationType.NONE -> NavigationTransitions.noTransition()
            AnimationType.SLIDE -> NavigationTransitions.slideTransition(speed)
            AnimationType.FADE -> NavigationTransitions.fadeTransition(speed)
            AnimationType.SCALE -> NavigationTransitions.scaleTransition(speed)
            AnimationType.SLIDE_FADE -> NavigationTransitions.slideFadeTransition(speed)
            AnimationType.ELEVATION -> NavigationTransitions.elevationTransition(speed)
            AnimationType.SHARED_AXIS -> NavigationTransitions.sharedAxisTransition(speed)
        }
    }

    /**
     * Gets the default transition (SLIDE_FADE with NORMAL speed).
     */
    fun getDefaultTransition(): TransitionSpec {
        return getTransition(
            type = AnimationType.default(),
            speed = AnimationSpeed.default(),
            enabled = true
        )
    }
}

// Made with Bob
