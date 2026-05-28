# VitalIQ — Native Android App

A full native Android rewrite of the VitalIQ health tracking app, built with **Kotlin + Jetpack Compose**. The FastAPI/MongoDB backend is unchanged; only the mobile client is rewritten.

---

## Full Setup Guide (Start to Finish)

Follow these steps in order every time you want to run the project.

---

### Step 1 — Prerequisites

Make sure the following are installed on your machine:

| Tool | Version | Notes |
|---|---|---|
| Android Studio | Panda (2025.1.1) or newer | [developer.android.com/studio](https://developer.android.com/studio) |
| JDK | 17+ | Bundled with Android Studio |
| Python | 3.10+ | [python.org](https://python.org) |
| MongoDB | 7.0+ | [mongodb.com/try/download/community](https://www.mongodb.com/try/download/community) |
| Git Bash | Any | Comes with Git for Windows |

---

### Step 2 — Start MongoDB

Open **Git Bash** and run:

```bash
mkdir -p /c/data/db
"/c/Program Files/MongoDB/Server/$(ls '/c/Program Files/MongoDB/Server/' | tail -1)/bin/mongod.exe" --dbpath "C:/data/db"
```

Keep this terminal open. You should see `Waiting for connections on port 27017` — that means MongoDB is running.

> If you see "Access is denied", open a **Command Prompt as Administrator** and run `net start MongoDB` instead.

---

### Step 3 — Set Up the Backend

Open a **second Git Bash terminal** and run:

```bash
cd "/c/Users/brand/Desktop/vitalIQ/vitaliq/backend"
```

Install dependencies (first time only):

```bash
python -m pip install fastapi==0.110.1 uvicorn==0.25.0 pymongo==4.5.0 motor==3.3.1 bcrypt==4.1.3 pydantic python-dotenv python-multipart python-jose passlib requests pandas numpy tzdata email-validator pyjwt boto3 cryptography typer jq httpx
```

Make sure the `.env` file exists at `backend/.env` with this content:

```
MONGO_URL=mongodb://localhost:27017
DB_NAME=vitaliq
EMERGENT_LLM_KEY=
OPENAI_API_KEY=your_openai_api_key_here
```

> Replace `your_openai_api_key_here` with a real key from [platform.openai.com/api-keys](https://platform.openai.com/api-keys).
> Your OpenAI account must have credits loaded (minimum $5) for AI Insights to work.
> Without a key, the app still works — Insights will show a local fallback score instead.

Start the backend server:

```bash
python -m uvicorn server:app --host 0.0.0.0 --port 8000 --reload
```

You should see:
```
INFO:     Application startup complete.
```

Keep this terminal open.

---

### Step 4 — Open the Android Project

1. Open **Android Studio**
2. Click **File → Open**
3. Navigate to `C:\Users\brand\Desktop\vitalIQ\vitaliq\kotlin`
4. Click **OK**
5. When prompted "Trust Gradle Project?" click **Trust Project**
6. Wait for Gradle sync to complete (first time takes 2–5 minutes)

---

### Step 5 — Configure Backend URL

Open `kotlin/local.properties` and confirm it contains:

```properties
sdk.dir=C\:\\Users\\brand\\AppData\\Local\\Android\\Sdk
BACKEND_URL=http://10.0.2.2:8000
```

- `http://10.0.2.2:8000` is the Android emulator's alias for your Windows localhost
- If testing on a **real device** on the same Wi-Fi, replace with your PC's local IP, e.g. `http://192.168.1.100:8000`

---

### Step 6 — Run the App

1. In Android Studio, select an emulator from the device dropdown (API 26+)
   - If no emulator exists: **Tools → Device Manager → Create Device**
2. Click **Run ▶** or press `Shift+F10`
3. The app builds and launches on the emulator
4. **Pull down to refresh** on the Dashboard to load your data

---

### Step 7 — Using the App

| Screen | What to do |
|---|---|
| **Dashboard** | Pull to refresh to see today's stats and health score |
| **Workout** | Tap the green play button — grant Activity Recognition permission — tap stop when done |
| **Log** | Tap any card to expand and log health data |
| **Insights** | Tap **Generate** to run AI analysis (requires OpenAI key with credits) |
| **Profile** | Edit your details and tap **Save** |
| **History** | Tap **View History** from Dashboard — swipe between chart tabs |

---

### Daily Startup Checklist

Every time you want to use the app, run these in order:

- [ ] **Terminal 1:** Start MongoDB (`mongod` command above)
- [ ] **Terminal 2:** Start backend (`python -m uvicorn server:app --host 0.0.0.0 --port 8000 --reload`)
- [ ] **Android Studio:** Click Run ▶

---

## Troubleshooting

| Error | Fix |
|---|---|
| `ECONNREFUSED` on emulator | Backend isn't running — start uvicorn in terminal 2 |
| `Application startup complete` then immediately shuts down | MongoDB isn't running — start mongod in terminal 1 |
| `MONGO_URL KeyError` | `backend/.env` file is missing — create it with the content in Step 3 |
| Gradle sync fails with plugin conflict | Delete `kotlin/.idea` and `kotlin/.gradle` folders, reopen project |
| Duplicate resources build error | Delete any `.xml` files in `mipmap-*` folders (keep `.webp` files) |
| AI Insights shows "AI unavailable" | Add OpenAI key to `backend/.env` and load credits at platform.openai.com/billing |
| `429 Too Many Requests` from OpenAI | Add billing credits at [platform.openai.com/billing](https://platform.openai.com/billing) |

---

## Project Structure

```
kotlin/
├── app/
│   └── src/main/java/com/vitaliq/app/
│       ├── data/
│       │   ├── api/          ApiService.kt, RetrofitClient.kt
│       │   └── model/        Models.kt (all DTOs)
│       ├── ui/
│       │   ├── components/   HealthRing, LineChart, StatCard, SectionHeader, PrimaryButton
│       │   ├── theme/        VitalTheme.kt, Type.kt
│       │   └── screens/
│       │       ├── dashboard/
│       │       ├── workout/
│       │       ├── log/
│       │       ├── insights/
│       │       ├── profile/
│       │       └── history/
│       ├── navigation/       AppNavigation.kt
│       └── MainActivity.kt
├── gradle/libs.versions.toml
├── local.properties          ← YOUR BACKEND URL (not committed to git)
└── README.md

backend/
├── server.py                 FastAPI app (all endpoints)
├── requirements.txt
└── .env                      ← YOUR SECRETS (not committed to git)
```

---

## Screens

| Screen | Tab | Key features |
|---|---|---|
| Dashboard | Home | HealthRing, StatCards, quick-actions, pull-to-refresh |
| Workout | Workout | Live timer, accelerometer classification, step counter, summary dialog |
| Log | Log | 7 entry type cards (weight in lbs, BP, HR, medication, meal, water, sleep) |
| Insights | Insights | AI score ring, category bars, suggestions, generate/refresh |
| Profile | Profile | Edit fields, fitness goal chips, daily goals, file import |
| History | (back-stack) | Steps / Weight / BP line charts, Activity bar chart |

---

## Design Tokens

All colors, spacing, and border-radii are in `VitalTheme.kt`:

```
brand      #2C4C3B   (primary green)
accent     #4CAF7D   (lighter green)
bg         #F8F9F6   (off-white background)
card       #FFFFFF
error      #E57373
warning    #FFB347
```

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

---

## Quick-Start Console Commands (Copy & Paste)

Run these every time you want to start the full stack. Open **two Git Bash terminals**.

---

### Terminal 1 — Start MongoDB

```bash
mkdir -p /c/data/db
"/c/Program Files/MongoDB/Server/$(ls '/c/Program Files/MongoDB/Server/' | tail -1)/bin/mongod.exe" --dbpath "C:/data/db"
```

Leave this terminal open. Wait for:
```
Waiting for connections on port 27017
```

---

### Terminal 2 — Install Backend Dependencies (first time only)

```bash
cd "/c/Users/brand/Desktop/vitalIQ/vitaliq/backend"
python -m pip install fastapi==0.110.1 uvicorn==0.25.0 pymongo==4.5.0 motor==3.3.1 bcrypt==4.1.3 pydantic python-dotenv python-multipart python-jose passlib requests pandas numpy tzdata email-validator pyjwt boto3 cryptography typer jq httpx
```

---

### Terminal 2 — Create .env (first time only)

```bash
cd "/c/Users/brand/Desktop/vitalIQ/vitaliq/backend"
cat > .env << 'EOF'
MONGO_URL=mongodb://localhost:27017
DB_NAME=vitaliq
EMERGENT_LLM_KEY=
OPENAI_API_KEY=your_openai_api_key_here
EOF
```

Then open `.env` and replace `your_openai_api_key_here` with your real key from [platform.openai.com/api-keys](https://platform.openai.com/api-keys).

---

### Terminal 2 — Start Backend Server (every time)

```bash
cd "/c/Users/brand/Desktop/vitalIQ/vitaliq/backend"
python -m uvicorn server:app --host 0.0.0.0 --port 8000 --reload
```

Leave this terminal open. Wait for:
```
INFO:     Application startup complete.
```

---

### Android Studio — Run the App (every time)

1. Open Android Studio → open folder `C:\Users\brand\Desktop\vitalIQ\vitaliq\kotlin`
2. Wait for Gradle sync to finish
3. Select your emulator from the device dropdown
4. Press **Shift+F10** (or click Run ▶)
5. Pull down on the Dashboard screen to load data

---

### Verify Everything Is Working

Open a browser on your PC and go to:

```
http://localhost:8000/docs
```

You should see the FastAPI Swagger UI with all endpoints listed. If you do, the backend is up and the emulator will connect successfully.

---

### Stop Everything

- **MongoDB:** Press `Ctrl+C` in Terminal 1
- **Backend:** Press `Ctrl+C` in Terminal 2
- **Emulator:** Close the emulator window or press the stop button in Android Studio
