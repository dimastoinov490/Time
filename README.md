# Time - Android Suite

A modern, high-precision Android application built with Kotlin and Jetpack Compose for timekeeping, world time synchronization, smart alarms, multi-timers, precision lap stopwatch, and focus tracking.

## Core Features

- **World Clock & Timezone Scrubber**:
  - Live animated analog clock face alongside high-contrast digital display with 12H / 24H switching.
  - Interactive Timezone Scrubber: Scrub up to ±12 hours to compare relative time across global cities in real-time.
  - Searchable global timezone city database with day/night indicators and time delta calculations.
- **Smart Alarms & Wake Challenges**:
  - Customizable recurring alarms with individual day-of-week toggles.
  - Interactive wake-up challenges: Math puzzles and Reflex tap tests to ensure you are fully awake.
  - Instant alarm test mode and configurable snooze durations.
- **Multi-Timers & Pomodoro Focus**:
  - Simultaneous multi-timer management with quick-start presets (+3m Tea, +5m Quick, +20m Nap, +25m Focus, +45m Workout).
  - Circular animated progress rings with one-tap +1 minute extension and completion alerts.
  - Integrated Pomodoro focus sprint mode with 25/5/15 intervals and streak counters.
- **Precision Stopwatch**:
  - High-precision millisecond timing with analog and digital displays.
  - Lap telemetry with automated fastest and slowest lap highlighting and split calculations.
- **Focus & Time Tracking**:
  - Activity logger for deep work, coding, studying, reading, and fitness.
  - Daily goal progress tracking and category distribution breakdown.

## Tech Stack

- **Runtime**: Kotlin & Jetpack Compose
- **Design System**: Material Design 3 (M3) Dark Theme
- **Architecture**: MVVM with Kotlin Coroutines and StateFlow
- **Target SDK**: Android 36 (minSdk 26)