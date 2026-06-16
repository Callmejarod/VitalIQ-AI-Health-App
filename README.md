# Here are your Instructions

---

## Running the App — Step by Step

### Prerequisites
- Python 3.10+
- MongoDB installed (`mongod` available at `/c/Program Files/MongoDB/Server/8.2/bin/`)
- Android Studio with an emulator configured (API 26+)
- A virtual environment with dependencies (e.g. `C:/Users/<you>/venvs/aider`)

---

### 1. Start MongoDB

```bash
mkdir -p ~/data/db
"/c/Program Files/MongoDB/Server/8.2/bin/mongod.exe" --dbpath "C:/Users/$USERNAME/data/db" --port 27017 &
```

---

### 2. Set up the backend environment (first time only)

```bash
cd ~/Desktop/VitalIQ-AI-Health-App-main/backend

cat > .env << 'EOF'
MONGO_URL=mongodb://127.0.0.1:27017
DB_NAME=vitaliq
EMERGENT_LLM_KEY=YOUR_OPENAI_API_KEY
OPENAI_API_KEY=YOUR_OPENAI_API_KEY
EOF

pip install -r requirements.txt
```

---

### 3. Start the backend server

```bash
cd ~/Desktop/VitalIQ-AI-Health-App-main/backend
uvicorn server:app --host 0.0.0.0 --port 8000
```

---

### 4. Run the Android app

Open the `kotlin/` folder in Android Studio, select your emulator, and click **Run**.

The emulator reaches your host machine's backend via `10.0.2.2:8000` automatically.

---

### Restarting after a reboot

Run steps **1** and **3** again each time you restart your machine. Step 2 only needs to be done once.
