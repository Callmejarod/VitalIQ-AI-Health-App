# VitalIQ — Run Commands

---

## FIRST TIME SETUP (run once ever)

Create the MongoDB data directory:

mkdir -p ~/data/db

Set JAVA_HOME so Gradle can find Java (run in Android Studio terminal each session or add to your shell profile):

export JAVA_HOME="/c/Program Files/Android/Android Studio/jbr"

---

## EVERY SESSION — run in order

### Terminal 1 — Start MongoDB (keep open)

"/c/Program Files/MongoDB/Server/8.2/bin/mongod.exe" --dbpath "C:/Users/brand/data/db" --port 27017

### Terminal 2 — Start the backend (keep open)

cd ~/Desktop/VitalIQ-AI-Health-App-main/backend
uvicorn server:app --host 0.0.0.0 --port 8000

### Terminal 3 — Seed test data (once per session or after a wipe)

cd ~/Desktop/VitalIQ-AI-Health-App-main
bash simulate_sensors.sh seed

### Terminal 3 — Build and install the Android app

cd ~/Desktop/VitalIQ-AI-Health-App-main/kotlin
./gradlew assembleDebug
./gradlew installDebug

---

## WIPE DATA AND START FRESH

Step 1 — Drop the database (MongoDB must be running):

cd ~/Desktop/VitalIQ-AI-Health-App-main/backend
python -c "from pymongo import MongoClient; MongoClient('mongodb://127.0.0.1:27017').drop_database('vitaliq')"

Step 2 — Re-seed with fresh data:

cd ~/Desktop/VitalIQ-AI-Health-App-main
bash simulate_sensors.sh seed

Step 3 — Clear app data on the emulator (wipes Room local cache):
Go to emulator Settings → Apps → VitalIQ → Storage → Clear Data

---

## RUN BACKEND TESTS

cd ~/Desktop/VitalIQ-AI-Health-App-main/backend
pytest tests/test_vitaliq_api.py -v

---

## IF EMULATOR IS STUCK

Kill a stuck emulator process (replace 28236 with the PID shown in the error):

taskkill /F /PID 28236

Then press Run in Android Studio to boot a fresh one.

---

## NOTES

- simulate_sensors.sh seed populates historical data for Dashboard, History, and Insights screens only.
- Live workout steps are simulated by the app itself via SIMULATE_SENSORS=true in local.properties — no script needed.
- local.properties must have SIMULATE_SENSORS=true and BACKEND_URL=http://10.0.2.2:8000 for the emulator to work.
- Steps 1 and 2 (MongoDB + backend) must be running before launching the app.
- ./gradlew commands must be run in the Android Studio terminal or a terminal with JAVA_HOME set.
