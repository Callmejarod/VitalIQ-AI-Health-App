#!/usr/bin/env bash
###############################################################################
# VitalIQ — sensor / health-data simulator for live demos
#
# Pushes realistic data into the running backend so the app's dashboard,
# charts, and AI insights light up for a presentation.
#
# Usage:
#   ./simulate_sensors.sh            # seed a full day of data, then AI insights
#   ./simulate_sensors.sh seed       # same as above (one-shot)
#   ./simulate_sensors.sh live       # stream live sensor readings until Ctrl+C
#   ./simulate_sensors.sh insights   # just (re)generate the AI health score
#
# Env:
#   BASE=http://localhost:8000   # override if backend is elsewhere
###############################################################################
set -euo pipefail

BASE="${BASE:-http://localhost:8000}"
API="$BASE/api"

# ISO-8601 UTC timestamp, optionally offset by N seconds in the past.
ts() { local off="${1:-0}"; date -u -d "-${off} seconds" +%Y-%m-%dT%H:%M:%S+00:00; }

post() { # post <path> <json>
  curl -s -o /dev/null -w "  [%{http_code}] $1\n" \
    -X POST "$API$1" -H "Content-Type: application/json" -d "$2"
}

check() {
  if ! curl -s -o /dev/null --max-time 3 "$API/"; then
    echo "ERROR: backend not reachable at $API — start it first (uvicorn server:app --port 8000)." >&2
    exit 1
  fi
}

# A random int in [min,max]
rnd() { echo $(( RANDOM % ($2 - $1 + 1) + $1 )); }

###############################################################################
seed() {
  echo "==> Seeding a full day of health data into $API"

  # --- Step counter / accelerometer  ->  workout sessions (drive steps_today)
  # NOTE: dashboard "today" totals filter on the UTC date, so keep these
  # timestamps inside the current UTC day (small offsets) or steps_today=0.
  post /workouts "$(cat <<JSON
{"started_at":"$(ts 2400)","ended_at":"$(ts 600)","duration_seconds":1800,
 "activity_type":"running","steps":4200,"avg_intensity":7.4,
 "activity_breakdown":{"running":1500,"walking":250,"mixed":50,"stationary":0}}
JSON
)"
  post /workouts "$(cat <<JSON
{"started_at":"$(ts 1500)","ended_at":"$(ts 600)","duration_seconds":900,
 "activity_type":"walking","steps":2600,"avg_intensity":3.1,
 "activity_breakdown":{"walking":700,"mixed":120,"running":0,"stationary":80}}
JSON
)"

  # --- Heart-rate sensor  ->  health entries
  for off in 25000 18000 9000 1200; do
    post /health-entries "{\"entry_type\":\"hr\",\"value\":{\"hr\":$(rnd 62 88)},\"logged_at\":\"$(ts $off)\"}"
  done

  # --- Blood-pressure cuff
  post /health-entries "{\"entry_type\":\"bp\",\"value\":{\"sys\":$(rnd 112 124),\"dia\":$(rnd 72 82)},\"logged_at\":\"$(ts 20000)\"}"

  # --- Smart scale: weight + body fat
  post /health-entries "{\"entry_type\":\"weight\",\"value\":{\"weight_kg\":78.4},\"logged_at\":\"$(ts 30000)\"}"
  post /health-entries "{\"entry_type\":\"body_fat\",\"value\":{\"body_fat\":18.2},\"logged_at\":\"$(ts 30000)\"}"

  # --- Sleep tracker (last night)
  post /sleep "{\"hours\":7.4,\"quality\":4,\"logged_at\":\"$(ts 32000)\"}"

  # --- Hydration + meals
  post /nutrition "{\"kind\":\"water\",\"water_ml\":500,\"logged_at\":\"$(ts 1800)\"}"
  post /nutrition "{\"kind\":\"water\",\"water_ml\":750,\"logged_at\":\"$(ts 300)\"}"
  post /nutrition "{\"kind\":\"meal\",\"meal_name\":\"Oatmeal & berries\",\"calories\":350,\"protein_g\":12,\"carbs_g\":58,\"fats_g\":7,\"logged_at\":\"$(ts 26000)\"}"
  post /nutrition "{\"kind\":\"meal\",\"meal_name\":\"Grilled chicken salad\",\"calories\":520,\"protein_g\":42,\"carbs_g\":30,\"fats_g\":22,\"logged_at\":\"$(ts 12000)\"}"

  # --- Medications
  post /medications "{\"name\":\"Vitamin D3\",\"dosage\":\"2000 IU\",\"frequency\":\"daily\"}"

  echo "==> Seed complete."
  insights
}

###############################################################################
# Stream live readings to mimic real-time sensors during the demo.
live() {
  echo "==> Live sensor stream to $API  (Ctrl+C to stop)"
  local steps_total=0
  while true; do
    local burst; burst=$(rnd 120 320)
    steps_total=$(( steps_total + burst ))
    # Each tick = a short "session" the step counter just recorded.
    post /workouts "$(cat <<JSON
{"started_at":"$(ts 60)","ended_at":"$(ts 0)","duration_seconds":60,
 "activity_type":"walking","steps":$burst,"avg_intensity":$(rnd 2 6).0,
 "activity_breakdown":{"walking":$burst,"running":0,"mixed":0,"stationary":0}}
JSON
)"
    post /health-entries "{\"entry_type\":\"hr\",\"value\":{\"hr\":$(rnd 68 102)}}"
    echo "   steps this session: ~$steps_total  ($(date +%H:%M:%S))"
    sleep 5
  done
}

###############################################################################
insights() {
  echo "==> Generating AI health insights / score ..."
  curl -s -X POST "$API/insights/generate" \
    | sed 's/,/,\n  /g' | sed 's/^/  /' || true
  echo
  echo "==> Dashboard summary now:"
  curl -s "$API/dashboard/summary" | sed 's/,/,\n  /g' | sed 's/^/  /'
  echo
}

###############################################################################
check
case "${1:-seed}" in
  seed)     seed ;;
  live)     live ;;
  insights) insights ;;
  *) echo "Usage: $0 [seed|live|insights]"; exit 1 ;;
esac
