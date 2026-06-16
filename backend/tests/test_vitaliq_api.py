"""VitalIQ backend API tests"""
import os
import io
import time
import json
import pytest
import requests

BASE_URL = os.environ.get("EXPO_PUBLIC_BACKEND_URL", "https://sensor-health-hub.preview.emergentagent.com").rstrip("/")
API = f"{BASE_URL}/api"


@pytest.fixture(scope="module")
def s():
    sess = requests.Session()
    sess.headers.update({"Content-Type": "application/json"})
    return sess


# ----- ROOT / HEALTH -----
def test_root(s):
    r = s.get(f"{API}/")
    assert r.status_code == 200
    assert r.json().get("ok") is True


# ----- PROFILE -----
class TestProfile:
    def test_get_profile_seeded(self, s):
        r = s.get(f"{API}/profile")
        assert r.status_code == 200
        data = r.json()
        assert "_id" not in data
        assert "user_id" in data
        assert data.get("name") is not None  # seeded Guest or updated
        assert "daily_step_goal" in data
        assert "daily_water_goal_ml" in data

    def test_update_profile(self, s):
        payload = {
            "name": "TEST_Alex",
            "age": 30,
            "height_cm": 175,
            "weight_kg": 72.5,
            "fitness_goal": "maintain",
            "daily_step_goal": 10000,
            "daily_water_goal_ml": 2500,
        }
        r = s.put(f"{API}/profile", json=payload)
        assert r.status_code == 200, r.text
        # Verify persistence
        r2 = s.get(f"{API}/profile")
        d = r2.json()
        assert d["name"] == "TEST_Alex"
        assert d["age"] == 30
        assert d["daily_step_goal"] == 10000
        assert d["daily_water_goal_ml"] == 2500
        assert d["fitness_goal"] == "maintain"


# ----- WORKOUTS -----
class TestWorkouts:
    def test_create_and_list_workout(self, s):
        payload = {
            "started_at": "2026-01-15T08:00:00+00:00",
            "ended_at": "2026-01-15T08:30:00+00:00",
            "duration_seconds": 1800,
            "activity_type": "walking",
            "steps": 3500,
            "avg_intensity": 0.4,
            "activity_breakdown": {"walking": 1500, "stationary": 300},
        }
        r = s.post(f"{API}/workouts", json=payload)
        assert r.status_code == 200, r.text
        created = r.json()
        assert created["steps"] == 3500
        assert "id" in created

        # second one earlier
        p2 = {**payload, "started_at": "2026-01-14T08:00:00+00:00", "ended_at": "2026-01-14T08:30:00+00:00", "steps": 1000}
        s.post(f"{API}/workouts", json=p2)

        r2 = s.get(f"{API}/workouts")
        assert r2.status_code == 200
        items = r2.json()
        assert isinstance(items, list)
        assert len(items) >= 2
        # sorted desc by started_at
        assert items[0]["started_at"] >= items[1]["started_at"]


# ----- HEALTH ENTRIES -----
class TestHealthEntries:
    created_ids = []

    def test_create_weight(self, s):
        r = s.post(f"{API}/health-entries", json={
            "entry_type": "weight",
            "value": {"weight_kg": 72.0},
        })
        assert r.status_code == 200, r.text
        d = r.json()
        TestHealthEntries.created_ids.append(d["id"])
        assert d["entry_type"] == "weight"

    def test_create_bp_hr(self, s):
        r1 = s.post(f"{API}/health-entries", json={"entry_type": "bp", "value": {"sys": 120, "dia": 80}})
        assert r1.status_code == 200
        TestHealthEntries.created_ids.append(r1.json()["id"])

        r2 = s.post(f"{API}/health-entries", json={"entry_type": "hr", "value": {"hr": 68}})
        assert r2.status_code == 200
        TestHealthEntries.created_ids.append(r2.json()["id"])

    def test_filter_by_entry_type(self, s):
        r = s.get(f"{API}/health-entries?entry_type=bp")
        assert r.status_code == 200
        items = r.json()
        assert all(it["entry_type"] == "bp" for it in items)
        assert len(items) >= 1

    def test_delete_health_entry(self, s):
        if not TestHealthEntries.created_ids:
            pytest.skip("no ids")
        eid = TestHealthEntries.created_ids[0]
        r = s.delete(f"{API}/health-entries/{eid}")
        assert r.status_code == 200
        # Verify gone
        all_items = s.get(f"{API}/health-entries").json()
        assert not any(it["id"] == eid for it in all_items)


# ----- MEDICATIONS -----
class TestMedications:
    def test_create_and_list_med(self, s):
        r = s.post(f"{API}/medications", json={"name": "TEST_Vit_D", "dosage": "1000IU", "frequency": "daily"})
        assert r.status_code == 200
        mid = r.json()["id"]
        r2 = s.get(f"{API}/medications")
        assert r2.status_code == 200
        items = r2.json()
        assert any(m["id"] == mid for m in items)

        # delete
        rd = s.delete(f"{API}/medications/{mid}")
        assert rd.status_code == 200


# ----- NUTRITION -----
class TestNutrition:
    def test_create_meal_and_water(self, s):
        rm = s.post(f"{API}/nutrition", json={
            "kind": "meal", "meal_name": "TEST_Oats", "calories": 350, "protein_g": 12, "carbs_g": 55, "fats_g": 8
        })
        assert rm.status_code == 200
        rw = s.post(f"{API}/nutrition", json={"kind": "water", "water_ml": 500})
        assert rw.status_code == 200

    def test_filter_kind(self, s):
        r = s.get(f"{API}/nutrition?kind=water")
        assert r.status_code == 200
        items = r.json()
        assert all(it["kind"] == "water" for it in items)


# ----- SLEEP -----
class TestSleep:
    def test_create_and_list_sleep(self, s):
        r = s.post(f"{API}/sleep", json={"hours": 7.5, "quality": 4})
        assert r.status_code == 200
        r2 = s.get(f"{API}/sleep")
        assert r2.status_code == 200
        assert len(r2.json()) >= 1


# ----- DASHBOARD -----
class TestDashboard:
    def test_dashboard_summary_keys(self, s):
        r = s.get(f"{API}/dashboard/summary")
        assert r.status_code == 200
        d = r.json()
        for k in ["steps_today", "workout_minutes_today", "workouts_today",
                  "last_sleep_hours", "water_ml_today", "last_hr", "last_bp", "health_score"]:
            assert k in d, f"missing key {k}"


# ----- AI INSIGHTS -----
class TestInsights:
    def test_generate_insights_claude(self, s):
        # Ensure some data exists
        s.post(f"{API}/health-entries", json={"entry_type": "weight", "value": {"weight_kg": 71.5}})
        s.post(f"{API}/sleep", json={"hours": 7.2})
        # Generate (may take 5-20s)
        r = s.post(f"{API}/insights/generate", timeout=90)
        assert r.status_code == 200, r.text
        d = r.json()
        # Schema
        assert isinstance(d["overall_score"], int)
        assert 0 <= d["overall_score"] <= 100
        cats = d["category_scores"]
        for c in ["cardiovascular", "activity", "nutrition", "hydration", "medication_adherence", "recovery"]:
            assert c in cats, f"missing category {c}"
            assert 0 <= int(cats[c]) <= 100
        assert isinstance(d["suggestions"], list) and len(d["suggestions"]) >= 1
        for sg in d["suggestions"]:
            for k in ["title", "detail", "priority", "category"]:
                assert k in sg, f"suggestion missing {k}"
        assert isinstance(d["trend_summary"], str) and len(d["trend_summary"]) > 0
        assert "used_fallback" in d
        # Per request, should be real Claude (not fallback)
        assert d["used_fallback"] is False, f"AI fell back, expected real Claude. response: {d}"

    def test_latest_insight(self, s):
        r = s.get(f"{API}/insights/latest")
        assert r.status_code == 200
        d = r.json()
        # Should not be empty since we just generated
        assert d.get("empty") is not True
        assert "overall_score" in d
        assert "category_scores" in d


# ----- IMPORT -----
class TestImport:
    def test_import_csv(self, s):
        csv_text = "type,weight_kg,date\nweight,70.1,2026-01-10T08:00:00+00:00\nweight,70.5,2026-01-11T08:00:00+00:00\n"
        files = {"file": ("test.csv", csv_text, "text/csv")}
        # Note: requests with files removes Content-Type from session header for multipart
        r = requests.post(f"{API}/import", files=files)
        assert r.status_code == 200, r.text
        d = r.json()
        assert d["imported"] >= 2
        assert d["total_rows"] >= 2

    def test_import_json(self, s):
        body = [
            {"type": "sleep", "hours": 7.4, "date": "2026-01-12T22:00:00+00:00"},
            {"type": "water", "water_ml": 250, "date": "2026-01-12T10:00:00+00:00"},
            {"type": "meal", "name": "TEST_Salad", "calories": 400, "date": "2026-01-12T12:00:00+00:00"},
            {"type": "bp", "sys": 118, "dia": 78, "date": "2026-01-12T07:00:00+00:00"},
            {"type": "hr", "hr": 65, "date": "2026-01-12T07:01:00+00:00"},
            {"type": "medication", "name": "TEST_Med", "dosage": "10mg", "frequency": "daily", "date": "2026-01-12T08:00:00+00:00"},
        ]
        files = {"file": ("test.json", json.dumps(body), "application/json")}
        r = requests.post(f"{API}/import", files=files)
        assert r.status_code == 200, r.text
        d = r.json()
        assert d["imported"] == 6, d
