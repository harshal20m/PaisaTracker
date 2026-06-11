# 📱 Page Transition Animation System - Implementation Plan

## 🎯 Overview
Add customizable page transition animations to PaisaTracker with user-configurable settings for a polished, modern UX.

---

## 📋 Implementation Plan

### **Phase 1: Data Layer** (Foundation)

#### 1.1 Create Animation Types Enum
**File:** `src/main/java/com/h4rsh41/paisatracker/data/AnimationType.kt`
```kotlin
enum class AnimationType {
    NONE,           // No animation (instant)
    SLIDE,          // Slide left/right
    FADE,           // Fade in/out
    SCALE,          // Scale up/down
    SLIDE_FADE,     // Combination
    ELEVATION,      // Material elevation change
    SHARED_AXIS     // Material shared axis
}
```

#### 1.2 Create Animation Speed Enum
```kotlin
enum class AnimationSpeed(val durationMs: Int) {
    INSTANT(0),
    FAST(150),
    NORMAL(300),
    SLOW(500)
}
```

#### 1.3 Create AnimationPreferences Repository
**File:** `src/main/java/com/h4rsh41/paisatracker/data/AnimationPreferencesRepository.kt`
- Store selected animation type
- Store animation speed
- Store enable/disable state
- Use DataStore for persistence

---

### **Phase 2: Animation Implementation**

#### 2.1 Create Transition Specs
**File:** `src/main/java/com/h4rsh41/paisatracker/navigation/NavigationTransitions.kt`

Implement each animation type:

**Slide Transition:**
```kotlin
fun slideTransition(speed: AnimationSpeed): EnterTransition + ExitTransition
- Enter: slideInHorizontally from right
- Exit: slideOutHorizontally to left
- Pop Enter: slideInHorizontally from left
- Pop Exit: slideOutHorizontally to right
```

**Fade Transition:**
```kotlin
fun fadeTransition(speed: AnimationSpeed)
- Enter: fadeIn
- Exit: fadeOut
```

**Scale Transition:**
```kotlin
fun scaleTransition(speed: AnimationSpeed)
- Enter: scaleIn + fadeIn
- Exit: scaleOut + fadeOut
```

**Slide + Fade (Recommended):**
```kotlin
fun slideFadeTransition(speed: AnimationSpeed)
- Combines slide and fade for smooth effect
```

**Elevation Transition:**
```kotlin
fun elevationTransition(speed: AnimationSpeed)
- Material Design elevation change
- Subtle depth perception
```

**Shared Axis (Material 3):**
```kotlin
fun sharedAxisTransition(speed: AnimationSpeed)
- Material 3 recommended transition
- Smooth axis-based movement
```

#### 2.2 Create Transition Provider
```kotlin
object TransitionProvider {
    fun getTransition(
        type: AnimationType,
        speed: AnimationSpeed
    ): Pair<EnterTransition, ExitTransition>
}
```

---

### **Phase 3: Navigation Integration**

#### 3.1 Update AppNavigation.kt
Add transition support to NavHost:
```kotlin
NavHost(
    navController = navController,
    startDestination = "home",
    enterTransition = { /* from preferences */ },
    exitTransition = { /* from preferences */ },
    popEnterTransition = { /* from preferences */ },
    popExitTransition = { /* from preferences */ }
)
```

#### 3.2 Per-Route Customization (Optional)
Allow specific routes to override global settings:
```kotlin
composable(
    route = "expense_details/{id}",
    enterTransition = { customTransition },
    exitTransition = { customTransition }
)
```

---

### **Phase 4: Settings UI**

#### 4.1 Create Animation Settings Screen
**File:** `src/main/java/com/h4rsh41/paisatracker/ui/settings/AnimationSettingsScreen.kt`

**UI Components:**

1. **Enable/Disable Toggle**
   - Master switch for all animations
   - "Reduce Motion" accessibility option

2. **Animation Type Selector**
   - Radio buttons or dropdown
   - Live preview for each type
   - Visual examples with icons

3. **Speed Selector**
   - Slider or segmented control
   - Options: Instant, Fast, Normal, Slow
   - Real-time preview

4. **Preview Section**
   - Interactive demo showing selected animation
   - "Test Animation" button
   - Shows before/after states

**Layout Structure:**
```
┌─────────────────────────────────┐
│ ⚙️ Animation Settings           │
├─────────────────────────────────┤
│ 🎬 Enable Animations      [ON]  │
├─────────────────────────────────┤
│ Animation Style                 │
│ ○ None (Instant)                │
│ ● Slide (Recommended)           │
│ ○ Fade                          │
│ ○ Scale                         │
│ ○ Slide + Fade                  │
│ ○ Elevation                     │
│ ○ Shared Axis (Material 3)     │
├─────────────────────────────────┤
│ Animation Speed                 │
│ [━━━●━━━━━━] Normal             │
│ Instant  Fast  Normal  Slow     │
├─────────────────────────────────┤
│ 👁️ Preview                      │
│ [Test Animation Button]         │
└─────────────────────────────────┘
```

#### 4.2 Add to Settings Menu
Update SettingsScreen.kt to include:
```kotlin
SettingsItem(
    icon = Icons.Default.Animation,
    title = "Page Transitions",
    subtitle = "Customize navigation animations",
    onClick = { navController.navigate("animation_settings") }
)
```

---

### **Phase 5: Advanced Features** (Optional Enhancements)

#### 5.1 Context-Aware Animations
Different animations for different navigation types:
- **Forward navigation:** Slide right
- **Back navigation:** Slide left
- **Modal screens:** Scale up/down
- **Detail screens:** Shared element transitions

#### 5.2 Accessibility
- Respect system "Reduce Motion" setting
- Provide "Accessibility Mode" with minimal animations
- Ensure animations don't cause motion sickness

#### 5.3 Performance Optimization
- Hardware acceleration for smooth 60fps
- Lazy loading of animation specs
- Memory-efficient transition caching

---

## 🎨 Recommended Default Settings

```kotlin
Default Configuration:
- Animation Type: SLIDE_FADE (best balance)
- Speed: NORMAL (300ms)
- Enabled: true
- Respect System Reduce Motion: true
```

---

## 📁 File Structure

```
src/main/java/com/h4rsh41/paisatracker/
├── data/
│   ├── AnimationType.kt (NEW)
│   ├── AnimationSpeed.kt (NEW)
│   └── AnimationPreferencesRepository.kt (NEW)
├── navigation/
│   ├── AppNavigation.kt (MODIFY)
│   └── NavigationTransitions.kt (NEW)
└── ui/settings/
    ├── SettingsScreen.kt (MODIFY)
    └── AnimationSettingsScreen.kt (NEW)
```

---

## ✅ Implementation Checklist

**Phase 1: Foundation**
- [ ] Create AnimationType enum
- [ ] Create AnimationSpeed enum
- [ ] Create AnimationPreferencesRepository
- [ ] Add DataStore dependencies if needed

**Phase 2: Animations**
- [ ] Implement slideTransition
- [ ] Implement fadeTransition
- [ ] Implement scaleTransition
- [ ] Implement slideFadeTransition
- [ ] Implement elevationTransition
- [ ] Implement sharedAxisTransition
- [ ] Create TransitionProvider

**Phase 3: Integration**
- [ ] Update AppNavigation with transitions
- [ ] Connect to AnimationPreferences
- [ ] Test all navigation routes
- [ ] Handle edge cases (deep links, etc.)

**Phase 4: UI**
- [ ] Design AnimationSettingsScreen
- [ ] Implement animation type selector
- [ ] Implement speed slider
- [ ] Add preview functionality
- [ ] Add to Settings menu
- [ ] Create Material 3 UI components

**Phase 5: Polish**
- [ ] Add accessibility support
- [ ] Optimize performance
- [ ] Add analytics tracking
- [ ] Write unit tests
- [ ] Update documentation

---

## 🚀 Benefits

✅ **Enhanced UX** - Smooth, professional transitions
✅ **User Control** - Customizable to personal preference
✅ **Accessibility** - Respects motion sensitivity
✅ **Performance** - Optimized for 60fps
✅ **Material 3** - Follows latest design guidelines
✅ **Flexibility** - Easy to add new animation types

---

## 📊 Estimated Timeline

- **Phase 1:** 2-3 hours
- **Phase 2:** 4-5 hours
- **Phase 3:** 2-3 hours
- **Phase 4:** 3-4 hours
- **Phase 5:** 2-3 hours
- **Total:** ~15-20 hours

---

## 📝 Notes

- This plan was created on 2026-06-11
- Saved for future implementation
- All navigation flickering issues have been resolved
- Ready to implement when needed

---

**Status:** 📋 Planning Complete - Ready for Implementation