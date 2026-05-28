# VitalIQ — Native Android App

A full native Android rewrite of the VitalIQ health tracking app, built with **Kotlin + Jetpack Compose**. The FastAPI/MongoDB backend is unchanged; only the mobile client is rewritten.

---

## Requirements

| Tool | Version |
|---|---|
| Android Studio | Hedgehog (2023.1.1) or newer |
| JDK | 17+ |
| Android SDK | API 26 min / API 35 target |
| Kotlin | 2.0.0 |
| Gradle | 8.5+ (via wrapper) |

---

## Setup

### 1. Clone / open the project

Open the `kotlin/` folder as an Android Studio project (it is the Gradle root).

### 2. Configure the backend URL

Edit (or create) `kotlin/local.properties` and add:

```properties
# Android emulator → localhost
BACKEND_URL=http://10.0.2.2:8000

# Real device on same Wi-Fi
# BACKEND_URL=http://192.168.1.100:8000

# Deployed server
# BACKEND_URL=https://your-server.example.com
```

The value is injected into `BuildConfig.BACKEND_URL` at compile time by `app/build.gradle.kts`.

> **Note:** `local.properties` is git-ignored. Every developer must add this line locally.

### 3. Sync Gradle

In Android Studio: **File → Sync Project with Gradle Files** (or click the elephant icon in the toolbar).

### 4. Run

- Select an emulator (API 26+) or connect a real device.
- Click **Run ▶** or press `Shift+F10`.
- The app installs and launches.

---

## Project Structure

```
app/src/main/java/com/vitaliq/app/
├── data/
│   ├── api/
│   │   ├── ApiService.kt          Retrofit interface (all 17 endpoints)
│   │   └── RetrofitClient.kt      Singleton Retrofit instance
│   └── model/
│       └── Models.kt              All DTO data classes
├── ui/
│   ├── components/
│   │   ├── HealthRing.kt          Circular progress arc (Canvas)
│   │   ├── LineChart.kt           Line + dual-line chart (Canvas)
│   │   ├── PrimaryButton.kt       Primary / Secondary / Ghost variants
│   │   ├── SectionHeader.kt       Bold title row with optional trailing slot
│   │   └── StatCard.kt            Icon + label + value card
│   ├── theme/
│   │   ├── VitalTheme.kt          Colors, spacing, radius constants + MaterialTheme
│   │   └── Type.kt                Typography scale
│   └── screens/
│       ├── dashboard/             DashboardScreen + DashboardViewModel
│       ├── workout/               WorkoutScreen + WorkoutViewModel (SensorManager)
│       ├── log/                   LogScreen + LogViewModel (7 entry types)
│       ├── insights/              InsightsScreen + InsightsViewModel
│       ├── profile/               ProfileScreen + ProfileViewModel (file import)
│       └── history/               HistoryScreen + HistoryViewModel (charts)
├── navigation/
│   └── AppNavigation.kt           NavHost + BottomNavigationBar
└── MainActivity.kt
```

---

## Screens

| Screen | Tab | Key features |
|---|---|---|
| Dashboard | Home | HealthRing, StatCards, quick-actions, pull-to-refresh |
| Workout | Workout | Live timer, accelerometer classification, step counter, summary sheet |
| Log | Log | 7 entry type cards (weight, BP, HR, medication, meal, water, sleep) |
| Insights | Insights | AI score ring, category bars, suggestions, generate/refresh |
| Profile | Profile | Edit fields, fitness goal chips, daily goals, file import |
| History | (back-stack) | Steps / Weight / BP line charts, Activity bar chart |

---

## Permissions

```xml
<uses-permission android:name="android.permission.INTERNET"/>
<uses-permission android:name="android.permission.ACTIVITY_RECOGNITION"/>
<uses-permission android:name="android.permission.BODY_SENSORS"/>
```

`ACTIVITY_RECOGNITION` is requested at runtime (Android 10+) before a workout starts.

---

## Design Tokens

All colors, spacing, and border-radii are centralised in `VitalTheme.kt`:

```
brand      #2C4C3B   (primary green)
accent     #4CAF7D   (lighter green)
bg         #F8F9F6   (off-white background)
card       #FFFFFF
error      #E57373
warning    #FFB347
```

Tab bar: active tint `brand`, inactive `textMuted`, height ~78dp.

---

## Backend API

Base URL is `{BACKEND_URL}/api/`. All calls are unauthenticated (`user_id = "default"` server-side). See `ApiService.kt` for the full endpoint list.

---

## Building a Release APK

```bash
cd kotlin
./gradlew assembleRelease
```

Output: `app/build/outputs/apk/release/app-release-unsigned.apk`

Add signing config to `app/build.gradle.kts` before distributing.
