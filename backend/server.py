from fastapi import FastAPI, APIRouter, HTTPException, UploadFile, File
from dotenv import load_dotenv
from starlette.middleware.cors import CORSMiddleware
from motor.motor_asyncio import AsyncIOMotorClient
import os
import logging
import json
import csv
import io
import re
import uuid
from pathlib import Path
from datetime import datetime, timezone
from typing import List, Optional, Dict, Any
from pydantic import BaseModel, Field

ROOT_DIR = Path(__file__).parent
load_dotenv(ROOT_DIR / ".env")

logging.basicConfig(
    level=logging.INFO,
    format="%(asctime)s - %(name)s - %(levelname)s - %(message)s",
)
logger = logging.getLogger("vitaliq")

mongo_url = os.environ["MONGO_URL"]
client = AsyncIOMotorClient(mongo_url)
db = client[os.environ["DB_NAME"]]

EMERGENT_LLM_KEY = os.environ.get("EMERGENT_LLM_KEY", "")

app = FastAPI(title="VitalIQ API")
api = APIRouter(prefix="/api")

DEFAULT_USER = "default"


def now_iso() -> str:
    return datetime.now(timezone.utc).isoformat()


# ---------- MODELS ----------
class Profile(BaseModel):
    id: str = Field(default_factory=lambda: str(uuid.uuid4()))
    user_id: str = DEFAULT_USER
    name: str = "Guest"
    age: Optional[int] = None
    height_cm: Optional[float] = None
    weight_kg: Optional[float] = None
    fitness_goal: Optional[str] = None  # "lose", "maintain", "gain", "endurance"
    daily_step_goal: int = 8000
    daily_water_goal_ml: int = 2000
    daily_sleep_goal_hours: float = 8.0
    updated_at: str = Field(default_factory=now_iso)


class WorkoutSession(BaseModel):
    id: str = Field(default_factory=lambda: str(uuid.uuid4()))
    user_id: str = DEFAULT_USER
    started_at: str
    ended_at: str
    duration_seconds: int
    activity_type: str  # walking | running | stationary | mixed
    steps: int = 0
    avg_intensity: float = 0.0
    activity_breakdown: Dict[str, int] = Field(default_factory=dict)
    notes: Optional[str] = None
    created_at: str = Field(default_factory=now_iso)


class HealthEntry(BaseModel):
    id: str = Field(default_factory=lambda: str(uuid.uuid4()))
    user_id: str = DEFAULT_USER
    entry_type: str  # weight | bp | hr | body_fat
    value: Dict[str, float]  # {weight_kg}, {sys, dia}, {hr}, {body_fat}
    logged_at: str = Field(default_factory=now_iso)


class MedicationLog(BaseModel):
    id: str = Field(default_factory=lambda: str(uuid.uuid4()))
    user_id: str = DEFAULT_USER
    name: str
    dosage: str
    frequency: str  # daily, twice_daily, weekly, as_needed
    taken_at: str = Field(default_factory=now_iso)


class NutritionLog(BaseModel):
    id: str = Field(default_factory=lambda: str(uuid.uuid4()))
    user_id: str = DEFAULT_USER
    kind: str  # meal | water
    meal_name: Optional[str] = None
    calories: Optional[int] = None
    protein_g: Optional[float] = None
    carbs_g: Optional[float] = None
    fats_g: Optional[float] = None
    water_ml: Optional[int] = None
    logged_at: str = Field(default_factory=now_iso)


class SleepLog(BaseModel):
    id: str = Field(default_factory=lambda: str(uuid.uuid4()))
    user_id: str = DEFAULT_USER
    hours: float
    quality: Optional[int] = None  # 1-5
    logged_at: str = Field(default_factory=now_iso)


class AIInsightRecord(BaseModel):
    id: str = Field(default_factory=lambda: str(uuid.uuid4()))
    user_id: str = DEFAULT_USER
    overall_score: int
    category_scores: Dict[str, int]
    suggestions: List[Dict[str, Any]]
    trend_summary: str
    snapshot: Dict[str, Any]
    created_at: str = Field(default_factory=now_iso)


# ---------- HELPERS ----------
def strip_id(doc: Dict[str, Any]) -> Dict[str, Any]:
    if doc and "_id" in doc:
        doc.pop("_id", None)
    return doc


async def insert_doc(collection, model: BaseModel) -> Dict[str, Any]:
    data = model.model_dump()
    await collection.insert_one(dict(data))  # copy to avoid _id mutation leak
    return data


# ---------- PROFILE ----------
@api.get("/")
async def root():
    return {"app": "VitalIQ", "ok": True}


@api.get("/profile")
async def get_profile():
    doc = await db.profiles.find_one({"user_id": DEFAULT_USER}, {"_id": 0})
    if not doc:
        # seed default
        prof = Profile()
        await db.profiles.insert_one(prof.model_dump())
        return prof.model_dump()
    return doc


@api.put("/profile")
async def update_profile(payload: Dict[str, Any]):
    payload["user_id"] = DEFAULT_USER
    payload["updated_at"] = now_iso()
    await db.profiles.update_one(
        {"user_id": DEFAULT_USER}, {"$set": payload}, upsert=True
    )
    doc = await db.profiles.find_one({"user_id": DEFAULT_USER}, {"_id": 0})
    return doc


# ---------- WORKOUTS ----------
@api.post("/workouts")
async def create_workout(payload: Dict[str, Any]):
    session = WorkoutSession(**{**payload, "user_id": DEFAULT_USER})
    await db.workouts.insert_one(session.model_dump())
    return session.model_dump()


@api.get("/workouts")
async def list_workouts(limit: int = 50):
    cursor = db.workouts.find({"user_id": DEFAULT_USER}, {"_id": 0}).sort(
        "started_at", -1
    ).limit(limit)
    return await cursor.to_list(length=limit)


# ---------- HEALTH ENTRIES ----------
@api.post("/health-entries")
async def create_health_entry(payload: Dict[str, Any]):
    entry = HealthEntry(**{**payload, "user_id": DEFAULT_USER})
    await db.health_entries.insert_one(entry.model_dump())
    return entry.model_dump()


@api.get("/health-entries")
async def list_health_entries(entry_type: Optional[str] = None, limit: int = 200):
    query: Dict[str, Any] = {"user_id": DEFAULT_USER}
    if entry_type:
        query["entry_type"] = entry_type
    cursor = (
        db.health_entries.find(query, {"_id": 0})
        .sort("logged_at", -1)
        .limit(limit)
    )
    return await cursor.to_list(length=limit)


@api.delete("/health-entries/{entry_id}")
async def delete_health_entry(entry_id: str):
    await db.health_entries.delete_one({"id": entry_id, "user_id": DEFAULT_USER})
    return {"ok": True}


# ---------- MEDICATIONS ----------
@api.post("/medications")
async def create_medication(payload: Dict[str, Any]):
    med = MedicationLog(**{**payload, "user_id": DEFAULT_USER})
    await db.medications.insert_one(med.model_dump())
    return med.model_dump()


@api.get("/medications")
async def list_medications(limit: int = 200):
    cursor = (
        db.medications.find({"user_id": DEFAULT_USER}, {"_id": 0})
        .sort("taken_at", -1)
        .limit(limit)
    )
    return await cursor.to_list(length=limit)


@api.delete("/medications/{med_id}")
async def delete_medication(med_id: str):
    await db.medications.delete_one({"id": med_id, "user_id": DEFAULT_USER})
    return {"ok": True}


# ---------- NUTRITION ----------
@api.post("/nutrition")
async def create_nutrition(payload: Dict[str, Any]):
    entry = NutritionLog(**{**payload, "user_id": DEFAULT_USER})
    await db.nutrition.insert_one(entry.model_dump())
    return entry.model_dump()


@api.get("/nutrition")
async def list_nutrition(kind: Optional[str] = None, limit: int = 200):
    query: Dict[str, Any] = {"user_id": DEFAULT_USER}
    if kind:
        query["kind"] = kind
    cursor = db.nutrition.find(query, {"_id": 0}).sort("logged_at", -1).limit(limit)
    return await cursor.to_list(length=limit)


@api.delete("/nutrition/{entry_id}")
async def delete_nutrition(entry_id: str):
    await db.nutrition.delete_one({"id": entry_id, "user_id": DEFAULT_USER})
    return {"ok": True}


# ---------- SLEEP ----------
@api.post("/sleep")
async def create_sleep(payload: Dict[str, Any]):
    entry = SleepLog(**{**payload, "user_id": DEFAULT_USER})
    await db.sleep.insert_one(entry.model_dump())
    return entry.model_dump()


@api.get("/sleep")
async def list_sleep(limit: int = 200):
    cursor = (
        db.sleep.find({"user_id": DEFAULT_USER}, {"_id": 0})
        .sort("logged_at", -1)
        .limit(limit)
    )
    return await cursor.to_list(length=limit)


# ---------- IMPORT ----------
@api.post("/import")
async def import_records(file: UploadFile = File(...)):
    raw = await file.read()
    text = raw.decode("utf-8", errors="ignore")
    imported = 0
    errors: List[str] = []

    filename = (file.filename or "").lower()
    is_json = filename.endswith(".json") or text.strip().startswith(("{", "["))

    rows: List[Dict[str, Any]] = []
    try:
        if is_json:
            parsed = json.loads(text)
            rows = parsed if isinstance(parsed, list) else [parsed]
        else:
            reader = csv.DictReader(io.StringIO(text))
            rows = list(reader)
    except Exception as e:
        raise HTTPException(status_code=400, detail=f"Parse error: {e}")

    for row in rows:
        try:
            etype = (row.get("type") or row.get("entry_type") or "").lower().strip()
            logged_at = row.get("date") or row.get("logged_at") or now_iso()

            if etype in ("weight", "bp", "hr", "body_fat"):
                if etype == "weight":
                    value = {"weight_kg": float(row.get("weight_kg") or row.get("value") or 0)}
                elif etype == "bp":
                    value = {
                        "sys": float(row.get("sys") or row.get("systolic") or 0),
                        "dia": float(row.get("dia") or row.get("diastolic") or 0),
                    }
                elif etype == "hr":
                    value = {"hr": float(row.get("hr") or row.get("value") or 0)}
                else:
                    value = {"body_fat": float(row.get("body_fat") or row.get("value") or 0)}
                entry = HealthEntry(
                    entry_type=etype, value=value, logged_at=str(logged_at)
                )
                await db.health_entries.insert_one(entry.model_dump())
                imported += 1
            elif etype in ("meal", "water"):
                kind = "meal" if etype == "meal" else "water"
                nl = NutritionLog(
                    kind=kind,
                    meal_name=row.get("meal_name") or row.get("name"),
                    calories=int(row["calories"]) if row.get("calories") else None,
                    protein_g=float(row["protein_g"]) if row.get("protein_g") else None,
                    carbs_g=float(row["carbs_g"]) if row.get("carbs_g") else None,
                    fats_g=float(row["fats_g"]) if row.get("fats_g") else None,
                    water_ml=int(row["water_ml"]) if row.get("water_ml") else None,
                    logged_at=str(logged_at),
                )
                await db.nutrition.insert_one(nl.model_dump())
                imported += 1
            elif etype == "sleep":
                sl = SleepLog(
                    hours=float(row.get("hours") or row.get("value") or 0),
                    logged_at=str(logged_at),
                )
                await db.sleep.insert_one(sl.model_dump())
                imported += 1
            elif etype in ("medication", "med"):
                ml = MedicationLog(
                    name=row.get("name") or "Unknown",
                    dosage=row.get("dosage") or "",
                    frequency=row.get("frequency") or "daily",
                    taken_at=str(logged_at),
                )
                await db.medications.insert_one(ml.model_dump())
                imported += 1
            else:
                errors.append(f"Unknown type: {etype}")
        except Exception as e:
            errors.append(str(e))

    # save raw record
    await db.imported_records.insert_one(
        {
            "id": str(uuid.uuid4()),
            "user_id": DEFAULT_USER,
            "filename": file.filename,
            "row_count": len(rows),
            "imported": imported,
            "errors": errors[:10],
            "created_at": now_iso(),
        }
    )

    return {"imported": imported, "total_rows": len(rows), "errors": errors[:10]}


# ---------- AI INSIGHTS ----------
async def gather_snapshot() -> Dict[str, Any]:
    profile = await db.profiles.find_one({"user_id": DEFAULT_USER}, {"_id": 0})
    workouts = (
        await db.workouts.find({"user_id": DEFAULT_USER}, {"_id": 0})
        .sort("started_at", -1)
        .limit(20)
        .to_list(20)
    )
    health = (
        await db.health_entries.find({"user_id": DEFAULT_USER}, {"_id": 0})
        .sort("logged_at", -1)
        .limit(50)
        .to_list(50)
    )
    meds = (
        await db.medications.find({"user_id": DEFAULT_USER}, {"_id": 0})
        .sort("taken_at", -1)
        .limit(30)
        .to_list(30)
    )
    nutrition = (
        await db.nutrition.find({"user_id": DEFAULT_USER}, {"_id": 0})
        .sort("logged_at", -1)
        .limit(50)
        .to_list(50)
    )
    sleep = (
        await db.sleep.find({"user_id": DEFAULT_USER}, {"_id": 0})
        .sort("logged_at", -1)
        .limit(14)
        .to_list(14)
    )
    return {
        "profile": profile,
        "recent_workouts": workouts,
        "health_entries": health,
        "medications": meds,
        "nutrition": nutrition,
        "sleep": sleep,
    }


def fallback_score(snapshot: Dict[str, Any]) -> Dict[str, Any]:
    """Deterministic fallback if AI call fails."""
    workouts = snapshot.get("recent_workouts") or []
    sleep = snapshot.get("sleep") or []
    nutrition = snapshot.get("nutrition") or []
    health = snapshot.get("health_entries") or []

    activity = min(100, 30 + len(workouts) * 7)
    recovery = (
        int(min(100, (sum(s.get("hours", 0) for s in sleep) / max(len(sleep), 1)) * 12))
        if sleep
        else 50
    )
    hydration_logs = [n for n in nutrition if n.get("kind") == "water"]
    hydration = min(100, 40 + len(hydration_logs) * 6) if hydration_logs else 45
    nutrition_score = min(100, 40 + len([n for n in nutrition if n.get("kind") == "meal"]) * 5)
    bp_entries = [h for h in health if h.get("entry_type") == "bp"]
    cardio = 70
    if bp_entries:
        sys_avg = sum(b["value"].get("sys", 120) for b in bp_entries) / len(bp_entries)
        cardio = max(40, min(100, int(140 - abs(sys_avg - 115))))
    med_adherence = min(100, 50 + len(snapshot.get("medications") or []) * 4)

    categories = {
        "cardiovascular": cardio,
        "activity": activity,
        "nutrition": nutrition_score,
        "hydration": hydration,
        "medication_adherence": med_adherence,
        "recovery": recovery,
    }
    overall = int(sum(categories.values()) / len(categories))
    return {
        "overall_score": overall,
        "category_scores": categories,
        "suggestions": [
            {
                "title": "Log more workouts",
                "detail": "Aim for 3-4 sessions weekly to lift activity scores.",
                "priority": 1,
                "category": "activity",
            },
            {
                "title": "Track daily hydration",
                "detail": "Logging water intake helps maintain a hydration baseline.",
                "priority": 2,
                "category": "hydration",
            },
            {
                "title": "Maintain sleep consistency",
                "detail": "Target 7-9 hours nightly to support recovery scores.",
                "priority": 3,
                "category": "recovery",
            },
        ],
        "trend_summary": "Baseline computed from available data. Add more logs for personalized insights.",
    }


async def call_ai(snapshot: Dict[str, Any]) -> Dict[str, Any]:
    if not EMERGENT_LLM_KEY:
        raise RuntimeError("EMERGENT_LLM_KEY missing")

    from emergentintegrations.llm.chat import LlmChat, UserMessage

    system_msg = (
        "You are VitalIQ, a clinical-grade health intelligence engine. "
        "You analyze user biometric, fitness, nutrition, sleep, and medication data "
        "and return a STRICT JSON object with these keys:\n"
        '  overall_score (int 0-100),\n'
        '  category_scores (object with int 0-100 values for: cardiovascular, '
        "activity, nutrition, hydration, medication_adherence, recovery),\n"
        "  suggestions (array of 3-6 objects with: title, detail, priority (1=highest), category),\n"
        "  trend_summary (string, 1-2 sentences).\n"
        "Return ONLY valid JSON — no markdown, no commentary. "
        "Be data-driven, specific, and reference actual values when possible."
    )

    chat = LlmChat(
        api_key=EMERGENT_LLM_KEY,
        session_id=f"vitaliq-{uuid.uuid4()}",
        system_message=system_msg,
    ).with_model("anthropic", "claude-sonnet-4-5-20250929")

    user_payload = json.dumps(snapshot, default=str)[:12000]
    msg = UserMessage(
        text=f"Analyze the following user data and return the JSON object as specified.\n\nDATA:\n{user_payload}"
    )

    response = await chat.send_message(msg)
    text = response if isinstance(response, str) else str(response)

    # Try to extract JSON
    text = text.strip()
    if text.startswith("```"):
        text = re.sub(r"^```(?:json)?\s*", "", text)
        text = re.sub(r"\s*```$", "", text)
    # find first { ... } block as backup
    match = re.search(r"\{.*\}", text, re.DOTALL)
    json_text = match.group(0) if match else text

    parsed = json.loads(json_text)
    # validate shape
    required = ["overall_score", "category_scores", "suggestions", "trend_summary"]
    for k in required:
        if k not in parsed:
            raise ValueError(f"Missing key: {k}")
    return parsed


@api.post("/insights/generate")
async def generate_insights():
    snapshot = await gather_snapshot()
    used_fallback = False
    try:
        result = await call_ai(snapshot)
        logger.info("AI insights generated via Claude")
    except Exception as e:
        logger.warning(f"AI call failed, using fallback: {e}")
        result = fallback_score(snapshot)
        used_fallback = True

    record = AIInsightRecord(
        overall_score=int(result["overall_score"]),
        category_scores={k: int(v) for k, v in result["category_scores"].items()},
        suggestions=result["suggestions"],
        trend_summary=result["trend_summary"],
        snapshot={
            "workout_count": len(snapshot.get("recent_workouts") or []),
            "health_entry_count": len(snapshot.get("health_entries") or []),
            "sleep_log_count": len(snapshot.get("sleep") or []),
            "nutrition_log_count": len(snapshot.get("nutrition") or []),
            "med_log_count": len(snapshot.get("medications") or []),
        },
    )
    await db.ai_insights.insert_one(record.model_dump())
    out = record.model_dump()
    out["used_fallback"] = used_fallback
    return out


@api.get("/insights/latest")
async def latest_insight():
    doc = await db.ai_insights.find_one(
        {"user_id": DEFAULT_USER}, {"_id": 0}, sort=[("created_at", -1)]
    )
    if not doc:
        return {"empty": True}
    return doc


@api.get("/insights/history")
async def insight_history(limit: int = 10):
    cursor = (
        db.ai_insights.find({"user_id": DEFAULT_USER}, {"_id": 0})
        .sort("created_at", -1)
        .limit(limit)
    )
    return await cursor.to_list(length=limit)


# ---------- DAILY SUMMARY ----------
@api.get("/dashboard/summary")
async def dashboard_summary():
    today = datetime.now(timezone.utc).date().isoformat()

    workouts_today = await db.workouts.find(
        {"user_id": DEFAULT_USER, "started_at": {"$gte": today}}, {"_id": 0}
    ).to_list(50)
    steps_today = sum(w.get("steps", 0) for w in workouts_today)
    workout_minutes = sum(w.get("duration_seconds", 0) for w in workouts_today) // 60

    sleep_recent = await db.sleep.find(
        {"user_id": DEFAULT_USER}, {"_id": 0}
    ).sort("logged_at", -1).limit(1).to_list(1)
    last_sleep = sleep_recent[0]["hours"] if sleep_recent else None

    water_today = await db.nutrition.find(
        {"user_id": DEFAULT_USER, "kind": "water", "logged_at": {"$gte": today}},
        {"_id": 0},
    ).to_list(100)
    water_ml = sum(w.get("water_ml") or 0 for w in water_today)

    hr_recent = await db.health_entries.find(
        {"user_id": DEFAULT_USER, "entry_type": "hr"}, {"_id": 0}
    ).sort("logged_at", -1).limit(1).to_list(1)
    last_hr = hr_recent[0]["value"].get("hr") if hr_recent else None

    bp_recent = await db.health_entries.find(
        {"user_id": DEFAULT_USER, "entry_type": "bp"}, {"_id": 0}
    ).sort("logged_at", -1).limit(1).to_list(1)
    last_bp = bp_recent[0]["value"] if bp_recent else None

    latest_insight = await db.ai_insights.find_one(
        {"user_id": DEFAULT_USER}, {"_id": 0}, sort=[("created_at", -1)]
    )

    return {
        "steps_today": steps_today,
        "workout_minutes_today": workout_minutes,
        "workouts_today": len(workouts_today),
        "last_sleep_hours": last_sleep,
        "water_ml_today": water_ml,
        "last_hr": last_hr,
        "last_bp": last_bp,
        "health_score": (latest_insight or {}).get("overall_score"),
    }


# ---------- REGISTER ----------
app.include_router(api)

app.add_middleware(
    CORSMiddleware,
    allow_credentials=True,
    allow_origins=["*"],
    allow_methods=["*"],
    allow_headers=["*"],
)


@app.on_event("shutdown")
async def shutdown_db_client():
    client.close()
