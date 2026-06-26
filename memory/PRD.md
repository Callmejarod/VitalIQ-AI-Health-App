# VitalIQ — Product Requirements

> **⚠️ Superseded / legacy spec.** This PRD describes the original Expo (React
> Native) prototype and its Claude-based backend. The submitted project is a
> **native Android (Kotlin + Jetpack Compose)** client following MVVM →
> Repository → Room/Retrofit, with insights served by the FastAPI backend. For
> the authoritative as-built architecture see `kotlin/README.md` and the Final
> Report (`documents/build_final_report.py`). This file is retained only as the
> historical product concept.

## Overview
A personal health & fitness intelligence app that combines device sensor tracking, manual biometric logging, and AI-generated health insights. (Original prototype: Expo / React Native + TypeScript; the shipped client is native Kotlin/Jetpack Compose.)

## Architecture
- **Frontend**: Expo Router (5 tabs + History modal), TypeScript, react-native-svg charts/rings, expo-sensors (Accelerometer + Pedometer)
- **Backend**: FastAPI, MongoDB (motor), Claude Sonnet 4.5 via emergentintegrations
- **Single local user profile** (user_id="default")

## Screens (Navigation Destinations)
1. **Dashboard** `/(tabs)/index` — health score ring, today stats grid, quick actions
2. **Workout** `/(tabs)/workout` — live accelerometer + pedometer feed, activity classification, save session
3. **Log** `/(tabs)/log` — entry forms for weight, BP, HR, meds, meals, water, sleep
4. **Insights** `/(tabs)/insights` — Claude-generated overall + category scores, suggestions, refresh/retry
5. **Profile** `/(tabs)/profile` — demographics, goals, JSON/CSV import
6. **History** `/history` — line charts for steps/weight/BP + activity breakdown

## Sensors
- **Accelerometer** → magnitude → classify (stationary/walking/mixed/running)
- **Pedometer** → live step count during active session

## Backend Endpoints (`/api/...`)
- `GET/PUT /profile`
- `GET/POST /workouts`
- `GET/POST/DELETE /health-entries` (entry_type=weight|bp|hr|body_fat)
- `GET/POST/DELETE /medications`
- `GET/POST/DELETE /nutrition` (kind=meal|water)
- `GET/POST /sleep`
- `POST /insights/generate` — gathers snapshot → Claude → JSON insight, fallback if AI fails
- `GET /insights/latest`
- `POST /import` — JSON/CSV multipart file upload
- `GET /dashboard/summary` — today's aggregates

## AI Integration
- **Provider**: Anthropic Claude Sonnet 4.5 (`claude-sonnet-4-5-20250929`)
- **Access**: emergentintegrations via EMERGENT_LLM_KEY
- **Prompt**: structured JSON response (overall_score, category_scores, suggestions[], trend_summary)
- **Fallback**: deterministic local scoring if AI call fails

## Mongo Collections
profiles, workouts, health_entries, medications, nutrition, sleep, ai_insights, imported_records

## Future / Smart Enhancements
- Wearable integration (Apple Health / Google Fit) for passive step sync
- Personalized AI coaching with conversation memory
- Premium tier with weekly AI deep-dives and family/care-team sharing
