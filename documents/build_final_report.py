# -*- coding: utf-8 -*-
"""Build the VitalIQ FINAL REPORT (end-of-quarter deliverable).

This is NOT a proposal. It is a record of what was actually built, tested, and
shipped at submission time. It deliberately CORRECTS earlier (GP5/GP6) SRS
assumptions that turned out to be inaccurate once the code was finished:

  * The network/API layer was described as "Phase 2 / not yet wired."  In the
    final build it is fully implemented: all six ViewModels make live Retrofit
    calls to a working FastAPI + MongoDB backend with real AI insights.
  * The planned Repository mediator layer was NOT built; ViewModels depend
    directly on RetrofitClient.apiService.  This report documents that honestly
    as an as-built deviation + future work, rather than repeating the plan.
  * No local datastore (Room/DataStore) was implemented -> no offline support.
  * Android has no automated test suite (only template stubs); the backend has
    a real pytest suite.  Android features were validated by manual UAT.

Run:  python build_final_report.py
Out:  BTJ-FinalReport-document-06-24-26-brandonGalli.docx
"""
import io, os, sys
sys.stdout = io.TextIOWrapper(sys.stdout.buffer, encoding="utf-8")

from docx.enum.text import WD_ALIGN_PARAGRAPH as ALIGN
from docx.shared import Pt, Inches

from docx_helpers import (
    new_doc, set_margins, add_table, H1, H2, H3, P, bullet, numbered,
    code_block, add_image, page_break, callout, add_toc_field,
    NAVY, BLUE, TEAL, GREY, DARK, WHITE, AMBER,
)

DOCDIR = os.path.dirname(__file__)
OUT = os.path.join(DOCDIR, "BTJ-FinalReport-document-06-24-26-brandonGalli.docx")

doc = new_doc()
set_margins(doc, 1.0)

C = ALIGN.CENTER

# ===========================================================================
# COVER PAGE
# ===========================================================================
P(doc, "VitalIQ", bold=True, size=40, color=NAVY, align=C, space_after=0)
P(doc, "AI-Assisted Personal Health & Fitness Companion", bold=True, size=15,
  color=BLUE, align=C, space_after=18)
P(doc, "FINAL PROJECT REPORT", bold=True, size=20, color=TEAL, align=C, space_after=4)
P(doc, "A record of what was built, tested, and delivered", italic=True, size=12,
  color=GREY, align=C, space_after=24)

add_table(doc,
    ["Field", "Detail"],
    [["Project", "VitalIQ — Android health & fitness companion with AI insights"],
     ["Team", "Team BTJ"],
     ["Author", "Brandon Galli"],
     ["Course", "DEV322 — Software Engineering"],
     ["Document", "Final Report (end-of-quarter)"],
     ["Version", "1.0 — Final"],
     ["Date", "June 24, 2026"],
     ["Repository", "VitalIQ (Android client + FastAPI backend)"],
     ["Build status", "Compiles & runs; core features integrated end-to-end"]],
    widths=[1.7, 5.0], font=10)

P(doc, "This report reflects the reality of the project at the time of submission. "
       "Where earlier SRS milestones described planned behavior that differs from the "
       "shipped build, this document revises those statements to match what was actually "
       "implemented.", italic=True, size=10, color=GREY, align=C)
page_break(doc)

# ===========================================================================
# DOCUMENT CONTROL
# ===========================================================================
H1(doc, "Document Control")

H2(doc, "Revision History")
add_table(doc,
    ["Version", "Milestone", "Date", "Summary"],
    [["1.0–6.0", "GP1–GP6 (SRS)", "May–Jun 2026",
      "Cumulative SRS: scope, user stories, use cases, diagrams, repository/API "
      "design, reactive-state architecture, test plans, and architectural audit."],
     ["Final 1.0", "Final Report", "Jun 24, 2026",
      "End-of-quarter record of the as-built system. Corrects earlier "
      "assumptions (network layer is fully wired; Repository layer was not built; "
      "no local persistence; Android tests are manual). Adds as-built architecture, "
      "consolidated testing results, traceability review, project-management "
      "summary, future work, and reflection."]],
    widths=[1.0, 1.3, 1.1, 3.9], font=9)

H2(doc, "How This Report Corrects the Earlier SRS")
P(doc, "The GP5/GP6 SRS was written while the system was still being assembled and, in "
       "good faith, described several capabilities as planned future phases. By submission, "
       "the build had moved past some of those assumptions and fell short of others. The "
       "table below is the authoritative reconciliation; the rest of the report uses the "
       "“As-Built” column.")
add_table(doc,
    ["Topic", "Earlier SRS said", "As-Built reality"],
    [["Network / API layer",
      "“API wiring is Phase 2”; ViewModels target a future Repository.",
      "DONE. All six ViewModels make live Retrofit calls to a running FastAPI "
      "backend (profile, workouts, health entries, insights, dashboard, import)."],
     ["Backend + AI insights",
      "External AI model consumed at runtime (designed).",
      "DONE. FastAPI + MongoDB; insights generated by OpenAI gpt-4o-mini with a "
      "deterministic fallback scorer when the API is unavailable."],
     ["Repository mediator layer",
      "“Repository as the data-source mediator” hides Room vs API.",
      "NOT BUILT. ViewModels depend directly on RetrofitClient.apiService. "
      "Documented as an as-built deviation and prioritized future work."],
     ["Local persistence / offline",
      "Room entities, DAOs, write-through cache planned for Phase 2.",
      "NOT BUILT. No Room/DataStore. The app requires the backend to be reachable; "
      "there is no offline cache."],
     ["Automated Android tests",
      "Unit/acceptance test cases listed as executed (VT/AT).",
      "Android has only template test stubs; features were validated by MANUAL "
      "UAT. The BACKEND has a real automated pytest suite (17 tests)."]],
    widths=[1.5, 2.6, 2.6], font=8.5)
callout(doc, "Integrity note: every “As-Built” statement in this report was verified "
             "against the source tree at submission. Test results are reported from real runs "
             "(backend pytest) or real manual sessions (Android), and limitations are stated "
             "plainly rather than omitted.")
page_break(doc)

# ===========================================================================
# TABLE OF CONTENTS
# ===========================================================================
H1(doc, "Table of Contents")
add_toc_field(doc)
page_break(doc)

# ===========================================================================
# 1. EXECUTIVE OVERVIEW
# ===========================================================================
H1(doc, "1. Executive Overview")

H2(doc, "1.1 Purpose")
P(doc, "VitalIQ is an Android application that helps everyday people understand their health "
       "by bringing fitness activity, vital-sign logging, and AI-generated guidance into one "
       "place. A companion backend stores the user’s data and produces a daily health score "
       "with personalized, data-cited suggestions. The goal was to deliver a working, "
       "architecturally disciplined MVP — not a clinical product — that demonstrates modern "
       "Android (Jetpack Compose + MVVM + coroutines) talking to a real service.")

H2(doc, "1.2 The Problem We Set Out to Solve")
P(doc, "Health data is fragmented. Steps live in one app, blood pressure in a notebook, sleep "
       "in another tracker, and none of it is interpreted in plain language. People end up with "
       "numbers but no understanding. VitalIQ’s thesis: consolidate the daily signals a person "
       "can realistically capture on a phone, then use an AI model to translate that data into a "
       "single score and a short list of concrete, prioritized actions.")

H2(doc, "1.3 Target Users")
bullet(doc, "Health-conscious adults who already track some metrics and want them unified and interpreted.",
       bold_lead="Primary — ")
bullet(doc, "People managing a specific metric (e.g., blood pressure or weight) who want trend visibility and reminders.",
       bold_lead="Secondary — ")
bullet(doc, "Casual users who want a low-friction daily dashboard and an at-a-glance “how am I doing?” score.",
       bold_lead="Tertiary — ")

H2(doc, "1.4 What Was Built (Delivered)")
add_table(doc,
    ["Capability", "Delivered Behavior"],
    [["Daily dashboard", "Live summary (steps, workout minutes, sleep, water, HR, BP, health score) loaded from the backend in parallel coroutines."],
     ["Workout tracking", "Start/stop sessions with a live timer, step count, activity classification, and intensity; session submitted to the backend on stop."],
     ["Health & lifestyle logging", "Weight, blood pressure, heart rate, medication, meals, water, and sleep — each posted to the backend with validation."],
     ["AI Insights", "On-demand health score (0–100), six category scores, and ranked suggestions generated by OpenAI gpt-4o-mini, with a deterministic fallback."],
     ["History", "Lists of past workouts and biometric entries (weight, BP) retrieved from the backend."],
     ["Profile", "Create/update profile (age, height, weight, goals) and import history from JSON/CSV via multipart upload."],
     ["Navigation", "Full Jetpack Navigation: NavHost + bottom navigation across Home, Workout, Log, Insights, Profile, plus a History route."],
     ["Backend service", "FastAPI + MongoDB with ~20 REST endpoints, file import, dashboard aggregation, and AI insight generation/history."]],
    widths=[1.9, 4.8], font=9)

H2(doc, "1.5 What Was Not Completed")
P(doc, "Stated plainly, in the spirit of an accurate record:")
bullet(doc, "the planned Repository mediator was not implemented; ViewModels call the Retrofit service directly. The app works, but the data-access seam the SRS specified is absent.",
       bold_lead="Repository abstraction — ")
bullet(doc, "no Room/DataStore cache was built, so the app has no offline mode and depends on the backend being reachable.",
       bold_lead="Local persistence / offline — ")
bullet(doc, "the Android module ships only template test stubs; verification relied on manual UAT. (The backend does have automated tests.)",
       bold_lead="Automated Android tests — ")
bullet(doc, "a single hardcoded “default” user; no login, accounts, or per-user isolation.",
       bold_lead="Authentication / multi-user — ")
bullet(doc, "a leftover template package (vitaliq.main) remains in the tree alongside the live com.vitaliq.app package.",
       bold_lead="Dead template code — ")

H2(doc, "1.6 Status at Submission")
callout(doc, "VitalIQ is a working, end-to-end MVP: the Compose UI, MVVM/coroutine spine, and "
             "Retrofit network layer are fully integrated with a live FastAPI + MongoDB + AI "
             "backend, and every core user story is demonstrable. It is not production-ready: it "
             "lacks the Repository seam, offline persistence, authentication, and automated client "
             "tests. Those are documented here as known limitations and prioritized future work.")
page_break(doc)

# ===========================================================================
# 2. FINAL SRS (REVISED)
# ===========================================================================
H1(doc, "2. Final Software Requirements Specification (Revised)")
P(doc, "This is the final, reconciled SRS. Requirements are marked Met, Partially Met, or "
       "Not Met against the shipped build. Earlier wording that implied unbuilt layers has "
       "been revised to describe what the system actually does.")

H2(doc, "2.1 Functional Requirements — Final Status")
add_table(doc,
    ["ID", "Requirement", "Status"],
    [["FR-1", "User can start and stop a workout and see a live timer.", "Met"],
     ["FR-2", "App tracks steps and classifies activity (stationary/walking/mixed/running) during a workout.", "Met (real sensors on device; simulated in demo mode)"],
     ["FR-3", "Completed workouts are submitted to and stored by the backend.", "Met"],
     ["FR-4", "User can log blood pressure (systolic/diastolic) with validation.", "Met"],
     ["FR-5", "User can log weight, heart rate, medication, meals, water, and sleep.", "Met"],
     ["FR-6", "User can generate an AI health score with category scores and suggestions.", "Met (gpt-4o-mini; deterministic fallback on failure)"],
     ["FR-7", "User can view a daily dashboard summarizing today’s metrics.", "Met"],
     ["FR-8", "User can view history of past workouts and biometric entries.", "Met"],
     ["FR-9", "User can create/update a profile (age, height, weight, goals).", "Met"],
     ["FR-10", "User can import historical data from JSON/CSV.", "Met (backend import endpoint + profile upload)"],
     ["FR-11", "App caches data locally for offline use.", "Not Met (no Room/DataStore)"],
     ["FR-12", "Data access mediated by a Repository layer.", "Not Met (ViewModels call Retrofit directly)"],
     ["FR-13", "Per-user accounts and authentication.", "Not Met (single default user)"]],
    widths=[0.6, 4.7, 1.4], font=9)

H2(doc, "2.2 Non-Functional Requirements — Final Status")
add_table(doc,
    ["ID", "Requirement", "Status / Evidence"],
    [["NFR-1", "Reactive UI: state survives configuration changes (rotation).", "Met — ViewModel + StateFlow retained across rotation."],
     ["NFR-2", "No blocking of the main thread; network/IO off the UI thread.", "Met — suspend functions on viewModelScope; Retrofit on background dispatcher."],
     ["NFR-3", "No GlobalScope / runBlocking in the codebase.", "Met — static scan: none found; all launches in viewModelScope."],
     ["NFR-4", "Clear MVVM layering; no business logic in Activities/Composables.", "Partially Met — UI/ViewModel separation is clean, but the Repository seam is missing (ViewModels know Retrofit)."],
     ["NFR-5", "Graceful handling of backend/AI failures.", "Met — ViewModels expose Error states; backend falls back to deterministic scoring."],
     ["NFR-6", "Responsive: load operations run in parallel where possible.", "Met — dashboard/history use async/await + supervisorScope."]],
    widths=[0.6, 3.4, 2.7], font=9)

H2(doc, "2.3 Revised Assumptions & Scope")
bullet(doc, "Originally assumed a Repository + Room cache would mediate all data. Revised: the MVP talks to the backend directly over Retrofit; persistence is server-side (MongoDB).")
bullet(doc, "Originally framed the runtime AI as an abstract “external model.” Revised: it is OpenAI gpt-4o-mini, called server-side, with a deterministic fallback so the feature degrades instead of failing.")
bullet(doc, "Originally implied multi-phase navigation. Revised: full bottom-nav + NavHost navigation shipped in this build.")
bullet(doc, "Scope held to a single default user; accounts and security were explicitly out of MVP scope.")

H2(doc, "2.4 Final User Stories")
add_table(doc,
    ["ID", "As a…", "I want to…", "So that…"],
    [["US-1", "active user", "start a workout and see live steps/activity", "I can track a session as it happens."],
     ["US-2", "health tracker", "log my blood pressure and weight", "I can monitor vitals over time."],
     ["US-3", "user seeking guidance", "generate an AI health score with suggestions", "I understand my data and what to improve."],
     ["US-4", "new user", "create a profile and import past data", "the app reflects me and my history."],
     ["US-5", "returning user", "see a daily dashboard", "I get an at-a-glance view of today."]],
    widths=[0.6, 1.5, 2.6, 2.0], font=9)

H2(doc, "2.5 Use Cases (Final)")
P(doc, "The four core use cases below were realized end-to-end. Diagrams appear in §4.")
add_table(doc,
    ["Use Case", "Actor", "Outcome (as-built)"],
    [["UC-1 Track Workout", "User", "Session recorded with steps/activity/intensity and persisted to the backend."],
     ["UC-2 Log Blood Pressure", "User", "Validated reading stored and visible in history."],
     ["UC-3 Generate AI Insights", "User", "Score + category scores + ranked suggestions returned (AI or fallback)."],
     ["UC-4 Manage Profile", "User", "Profile saved server-side; history importable from file."]],
    widths=[1.8, 1.0, 3.9], font=9)
page_break(doc)

# ===========================================================================
# 3. SYSTEM ARCHITECTURE (AS-BUILT)
# ===========================================================================
H1(doc, "3. System Architecture (As-Built)")
P(doc, "VitalIQ is a two-tier system: a Jetpack Compose Android client and a FastAPI backend "
       "with MongoDB. The client follows MVVM with a unidirectional reactive state flow. The "
       "description below is the architecture as actually implemented, including where it "
       "departs from the planned design.")

H2(doc, "3.1 High-Level Topology")
code_block(doc,
    "Android client (Kotlin / Jetpack Compose)\n"
    "  UI (Composable screens)\n"
    "      |  collectAsStateWithLifecycle()\n"
    "  ViewModel (StateFlow<UiState>, viewModelScope)\n"
    "      |  RetrofitClient.apiService.<suspend fun>()   <-- direct (no Repository)\n"
    "  Retrofit + Gson  --HTTP/JSON-->  FastAPI backend\n"
    "                                       |  Motor (async)\n"
    "                                   MongoDB (8 collections)\n"
    "                                       |  httpx\n"
    "                                   OpenAI gpt-4o-mini (insights) + fallback")

H2(doc, "3.2 UI Layer (Jetpack Compose)")
P(doc, "Six Composable screens — Dashboard, Workout, Log, Insights, History, Profile — render "
       "purely from ViewModel state. Screens collect state with collectAsStateWithLifecycle() "
       "and trigger load() on resume. Reusable components (HealthRing, LineChart, StatCard, "
       "PrimaryButton, SectionHeader) keep screens declarative. MainActivity only hosts the "
       "Compose tree and theme — no business logic lives in the Activity or Composables.")

H2(doc, "3.3 ViewModel Layer")
P(doc, "Each screen has a dedicated ViewModel that owns a MutableStateFlow privately and exposes "
       "a read-only StateFlow publicly. State is modeled as sealed classes (Loading / Success / "
       "Error, plus domain-specific states such as the workout’s Idle / Active / Submitting / "
       "Summary). All asynchronous work is launched in viewModelScope and cancelled automatically "
       "in onCleared().")
P(doc, "Representative ViewModel (Dashboard) — explicit StateFlow, parallel loads, error state:",
  bold=True, size=10, color=BLUE)
code_block(doc,
    "private val _uiState = MutableStateFlow<DashboardUiState>(DashboardUiState.Loading)\n"
    "val uiState: StateFlow<DashboardUiState> = _uiState        // explicit StateFlow\n\n"
    "fun load() = viewModelScope.launch {                       // viewModelScope only\n"
    "    try {\n"
    "        supervisorScope {\n"
    "            val summary = async { api.dashboardSummary() }  // parallel\n"
    "            val profile = async { api.getProfile() }\n"
    "            _uiState.value = Success(summary.await(), profile.await())\n"
    "        }\n"
    "    } catch (e: Exception) {\n"
    "        _uiState.value = Error(e.message ?: \"Failed to load dashboard\")\n"
    "    }\n"
    "}")

H2(doc, "3.4 Data Access — As-Built vs. Planned (Key Deviation)")
P(doc, "The SRS specified a Repository layer to mediate between ViewModels and data sources "
       "(API today, Room cache later). In the shipped build this layer does not exist. Each "
       "ViewModel holds a reference to the Retrofit service and calls it directly:")
code_block(doc,
    "class DashboardViewModel : ViewModel() {\n"
    "    private val api = RetrofitClient.apiService   // direct dependency on Retrofit\n"
    "    ...\n"
    "}")
add_table(doc,
    ["Aspect", "Planned (SRS)", "As-Built", "Consequence"],
    [["Mediation", "Repository hides source choice", "ViewModel → Retrofit directly", "ViewModels are coupled to the network layer."],
     ["Caching", "Write-through Room cache", "None", "No offline; every read is a network call."],
     ["Testability", "Mock the Repository", "Must mock Retrofit/HTTP", "Harder to unit-test in isolation."],
     ["Swap sources", "Add Room behind Repository", "Touch every ViewModel", "Higher future refactor cost."]],
    widths=[1.2, 2.0, 1.7, 2.0], font=8.5)
P(doc, "This is the single most significant architectural gap. It does not break the app — the "
       "ViewModel still owns state and threading correctly — but it leaves the planned seam "
       "unbuilt. Introducing a Repository interface is the first item in §8 (Future Work).",
  italic=True, size=10, color=GREY)

H2(doc, "3.5 Local Datastore")
P(doc, "There is no on-device datastore in the final build — no Room database, DataStore, or "
       "SharedPreferences-backed cache. All durable state lives server-side in MongoDB. The "
       "practical effect is that the app needs network connectivity to display data and cannot "
       "operate offline. This was a conscious scope cut to land the end-to-end vertical slice "
       "(UI → ViewModel → network → backend → AI) within the timebox.")

H2(doc, "3.6 Backend Service")
P(doc, "A FastAPI application exposes ~20 REST endpoints under /api, persisted to MongoDB via the "
       "async Motor driver across eight collections (profiles, workouts, health_entries, "
       "medications, nutrition, sleep, ai_insights, imported_records). Pydantic models validate "
       "all input.")
add_table(doc,
    ["Endpoint group", "Examples", "Purpose"],
    [["Profile", "GET/PUT /profile", "Read/update the (seeded) user profile."],
     ["Workouts", "POST/GET /workouts", "Store and list sessions (sorted by start time)."],
     ["Health entries", "POST/GET/DELETE /health-entries", "Weight, BP, HR, body fat; filter by type."],
     ["Lifestyle", "/medications, /nutrition, /sleep", "Medication, meals/water, and sleep logs."],
     ["Dashboard", "GET /dashboard/summary", "Aggregates today’s metrics + health score."],
     ["Insights", "POST /insights/generate, GET /insights/latest|history", "AI scoring with deterministic fallback."],
     ["Import", "POST /import", "JSON/CSV bulk import of historical records."]],
    widths=[1.4, 2.5, 2.8], font=9)
P(doc, "AI insights: the backend builds a structured prompt from recent user data and calls "
       "OpenAI gpt-4o-mini (temperature 0.3) for a JSON response with overall_score, six "
       "category_scores, ranked suggestions, and a trend summary. If the key is missing or the "
       "call fails, a deterministic fallback scorer computes category scores from the same data "
       "and flags the result with used_fallback=true — so the feature degrades gracefully "
       "instead of erroring. The health score is a transparent heuristic, not a medical algorithm.")

H2(doc, "3.7 End-to-End Data Flow (Generate Insights)")
numbered(doc, "User taps Generate on the Insights screen; the screen calls InsightsViewModel.generateInsights().")
numbered(doc, "The ViewModel emits Loading, then launches in viewModelScope and calls api.generateInsights() (suspend).")
numbered(doc, "Retrofit issues an HTTP POST to the FastAPI backend off the main thread.")
numbered(doc, "The backend gathers recent data from MongoDB, calls gpt-4o-mini (or falls back), stores the result, and returns it.")
numbered(doc, "The ViewModel maps the response into Success(state) (or Error) on the StateFlow.")
numbered(doc, "Compose recomposes from the new state, rendering the score, category scores, and suggestions.")

H2(doc, "3.8 Concurrency & Lifecycle Discipline")
add_table(doc,
    ["Discipline", "How it was maintained"],
    [["Scope ownership", "Every coroutine launches in viewModelScope; cancelled on onCleared()."],
     ["No GlobalScope/runBlocking", "Static scan across the codebase: zero occurrences."],
     ["Main never blocks", "All network calls are suspend functions executed off the UI thread by Retrofit."],
     ["Config-change survival", "ViewModel + StateFlow retain state across rotation (verified manually)."],
     ["Sensor cleanup", "WorkoutViewModel unregisters its SensorEventListener and cancels the timer job in onCleared()."]],
    widths=[1.9, 4.8], font=9)

H2(doc, "3.9 Architectural Discipline — Honest Verdict")
callout(doc, "The reactive spine (UI → ViewModel/StateFlow → coroutines → network) is clean and "
             "disciplined: state is owned by ViewModels, the UI is a pure function of state, and "
             "coroutine hygiene is intact. The discipline that was NOT maintained is the "
             "Repository seam and the local-cache layer — ViewModels reach the network directly "
             "and there is no offline store. The architecture is sound but incomplete relative to "
             "the plan.")
page_break(doc)

# ===========================================================================
# 4. DESIGN ARTIFACTS
# ===========================================================================
H1(doc, "4. Design Artifacts (Diagrams, Wireframes, Screens)")
P(doc, "The diagrams below were authored during design and remain accurate for the four core "
       "use cases. Note: live device screenshots are not embedded in this document; instead, "
       "each final screen is described against the implemented UI so the record matches the "
       "shipped build.")

H2(doc, "4.1 Use-Case Diagrams")
add_image(doc, "uc1_workout.png", width=5.6, caption="UC-1 Track Workout — actor, system, and core interactions.")
add_image(doc, "uc2_bp.png", width=5.6, caption="UC-2 Log Blood Pressure — validated vital-sign entry.")
add_image(doc, "uc3_ai.png", width=5.6, caption="UC-3 Generate AI Insights — on-demand scoring and suggestions.")
add_image(doc, "uc4_profile.png", width=5.6, caption="UC-4 Manage Profile — profile creation and history import.")
page_break(doc)

H2(doc, "4.2 Activity Diagrams")
add_image(doc, "act_workout.png", width=6.2, caption="Workout activity flow (start → track → stop → submit).")
add_image(doc, "act_insights_week5.png", width=6.2, caption="AI Insights activity flow with Main/IO separation.")
page_break(doc)

H2(doc, "4.3 Sequence & State Diagrams")
add_image(doc, "seq_insights_week5.png", width=6.2, caption="Insights sequence: UI → ViewModel → network → backend → AI → state.")
add_image(doc, "state_workout.png", width=4.6, caption="Workout state machine (Idle → Active → Submitting → Summary).")
P(doc, "As-built note: the sequence diagram shows a Repository hop that the final code does not "
       "have — in the shipped build the ViewModel calls Retrofit directly. The diagram is "
       "retained as the intended design; §3.4 documents the deviation.", italic=True, size=9.5, color=GREY)
page_break(doc)

H2(doc, "4.4 Final Screen Inventory (Wireframe Description)")
add_table(doc,
    ["Screen", "Final Layout & Key Elements"],
    [["Dashboard (Home)", "Health ring (today’s score) atop stat cards: steps, workout minutes, sleep, water, HR, BP. Pulls from /dashboard/summary."],
     ["Workout", "Large timer, live step count, current activity label, intensity indicator; Start/Stop control. Idle/Active/Summary states."],
     ["Log", "Expandable cards for weight, BP, HR, medication, meal, water, sleep; each validates and posts on save with a snackbar confirmation."],
     ["Insights", "Generate button; on success shows the 0–100 score, six category sub-scores, a trend summary, and a ranked suggestion list."],
     ["History", "Scrollable lists of past workouts and biometric entries (weight, BP) with a small line chart for trends."],
     ["Profile", "Editable fields (name, age, height, weight, goal); Save persists to backend; Import pulls history from a JSON/CSV file."]],
    widths=[1.4, 5.3], font=9)
page_break(doc)

# ===========================================================================
# 5. TESTING DOCUMENTATION
# ===========================================================================
H1(doc, "5. Testing Documentation")
P(doc, "Testing combined an automated backend suite with structured manual validation of the "
       "Android client. This section reports what was tested, how, and the real results — "
       "including defects found and fixed, and the gaps that remain.")

H2(doc, "5.1 Test Strategy & Levels")
add_table(doc,
    ["Level", "Where", "Approach"],
    [["Unit", "Backend", "pytest against FastAPI endpoints (CRUD, validation, aggregation)."],
     ["Unit", "Android", "Not implemented beyond template stubs (see limitations)."],
     ["Integration", "Full stack", "Client → backend → MongoDB → AI exercised manually per feature."],
     ["Verification", "Client", "Manual checks that each unit behaves per the technical spec (state, threading)."],
     ["Validation / UAT", "Client", "Manual walkthrough of each user story end-to-end on the emulator."],
     ["Regression", "Both", "Re-running backend suite + repeating core client flows after each change."]],
    widths=[1.4, 1.2, 4.1], font=9)

H2(doc, "5.2 Backend Automated Tests (pytest) — Real Results")
P(doc, "backend/tests/test_vitaliq_api.py contains 17 tests across the API surface. These run "
       "against a live backend + MongoDB; the AI test asserts a real model call (used_fallback "
       "is False) and therefore requires a valid key and network.")
add_table(doc,
    ["Suite", "Tests", "Covers", "Result"],
    [["Root", "1", "Health check returns ok.", "PASS"],
     ["Profile", "2", "Seed-on-first-read; update persistence.", "PASS"],
     ["Workouts", "1", "Create with sensor data; list sorted desc.", "PASS"],
     ["Health entries", "4", "Create weight/BP/HR; filter by type; delete.", "PASS"],
     ["Medications", "1", "Create, list, delete.", "PASS"],
     ["Nutrition", "2", "Meal + water; filter by kind.", "PASS"],
     ["Sleep", "1", "Create and list.", "PASS"],
     ["Dashboard", "1", "Summary contains all required keys.", "PASS"],
     ["Insights", "2", "Generate (real AI, not fallback); latest.", "PASS when key+network present"],
     ["Import", "2", "CSV and mixed-type JSON import counts.", "PASS"]],
    widths=[1.4, 0.7, 3.6, 1.6], font=9)

H2(doc, "5.3 Android Verification (Manual) — Technical Behavior")
add_table(doc,
    ["ID", "Unit Under Test", "Check", "Result"],
    [["VT-1", "WorkoutViewModel", "Start → Active state; timer increments each second.", "PASS"],
     ["VT-2", "StateFlow rotation", "Rotate during active workout; state retained.", "PASS"],
     ["VT-3", "Workout submit", "Stop → createWorkout() called off main thread; UI not blocked.", "PASS"],
     ["VT-4", "Layer boundary", "No DAO/Room import in any ViewModel.", "PASS (no Room exists)"],
     ["VT-5", "Coroutine hygiene", "Static scan for GlobalScope/runBlocking.", "PASS (none)"],
     ["VT-6", "LogViewModel validation", "Empty/invalid BP rejected before network call.", "PASS"],
     ["VT-7", "InsightsViewModel", "Loading emitted before the network call.", "PASS"],
     ["VT-8", "Sensor classification", "Magnitudes map to stationary/walking/mixed/running.", "PASS"],
     ["VT-9", "onCleared cleanup", "Timer cancelled; sensor listener unregistered.", "PASS"]],
    widths=[0.5, 1.7, 3.5, 1.0], font=8.5)

H2(doc, "5.4 Validation / User Acceptance (Manual) — User Stories")
add_table(doc,
    ["ID", "Story", "User Steps", "Outcome", "Result"],
    [["AT-1", "US-1", "Open Workout; Start.", "Timer + live steps/activity shown.", "PASS"],
     ["AT-2", "US-1", "Start; rotate device.", "Session keeps running; metrics preserved.", "PASS"],
     ["AT-3", "US-2", "Enter valid BP; Save.", "Stored; appears in history.", "PASS"],
     ["AT-4", "US-2", "Enter invalid/empty BP; Save.", "Rejected with validation message.", "PASS"],
     ["AT-5", "US-3", "Open Insights; Generate.", "Score, category scores, suggestions shown.", "PASS"],
     ["AT-6", "US-3", "Generate with backend unreachable.", "Error state shown; no crash.", "PASS"],
     ["AT-7", "US-4", "Edit profile; Save.", "Persisted server-side; reloads on return.", "PASS"],
     ["AT-8", "US-5", "Open Home.", "Dashboard summary loads today’s metrics.", "PASS"]],
    widths=[0.5, 0.7, 2.2, 2.7, 0.7], font=8.5)

H2(doc, "5.5 Integration & Regression")
bullet(doc, "Integration: the full vertical slice (profile → workout → log → dashboard → insights) was exercised against the live backend; data created on one screen appears on others after reload.")
bullet(doc, "Regression: the backend pytest suite was re-run after backend changes; core client flows were repeated after client changes. No regressions observed in core features.")
bullet(doc, "Demo mode: SIMULATE_SENSORS=true lets the workout produce realistic data on an emulator with no step hardware; real sensors are used when the flag is false on a device.")

H2(doc, "5.6 Defects Found & Corrected")
add_table(doc,
    ["Defect", "Symptom", "Fix", "Re-test"],
    [["Step-counter base offset", "First reading showed device-lifetime steps.", "Capture baseStepCount on first event; report the delta.", "VT-8 PASS"],
     ["Insights loading flicker", "Brief empty flash before content.", "Emit Loading before the network call.", "VT-7 PASS"],
     ["Rotation reset (early build)", "Workout reset on rotation in an early prototype.", "Move state into the ViewModel/StateFlow.", "VT-2 PASS"],
     ["Emulator step data", "No step sensor on emulator stalled demos.", "Add SIMULATE_SENSORS demo mode.", "AT-1 PASS"]],
    widths=[1.7, 2.3, 2.3, 0.9], font=9)

H2(doc, "5.7 Results Summary")
add_table(doc,
    ["Metric", "Result"],
    [["Backend automated tests", "17 (16 deterministic PASS; 1 AI test PASS with key+network)"],
     ["Android verification checks (manual)", "9 / 9 PASS"],
     ["Android acceptance checks (manual)", "8 / 8 PASS"],
     ["Integration (core vertical slice)", "PASS"],
     ["Regression after changes", "No regressions in core features"],
     ["Rotation / config-change survival", "PASS"],
     ["GlobalScope / runBlocking", "None found"]],
    widths=[4.0, 2.7], font=9.5)

H2(doc, "5.8 Known Limitations (Testing)")
bullet(doc, "No automated Android tests; client coverage depends on manual sessions and is not reproducible in CI.")
bullet(doc, "The AI insights backend test is environment-dependent (needs a valid key and network); without them it fails by design rather than passing on the fallback.")
bullet(doc, "No automated UI (Compose/Espresso) tests; visual regressions would not be caught automatically.")
bullet(doc, "No load/performance or security testing was performed.")
page_break(doc)

# ===========================================================================
# 6. TRACEABILITY REVIEW
# ===========================================================================
H1(doc, "6. Traceability Review")
P(doc, "This review connects the final implementation back to the original goals: each user "
       "story traces through its primary requirements to the feature that delivers it and the "
       "test(s) that validate it.")
add_table(doc,
    ["User Story", "Requirements", "Implemented In", "Validated By", "Verdict"],
    [["US-1 Track workout", "FR-1, FR-2, FR-3", "WorkoutViewModel + sensors + /workouts", "VT-1–3, VT-8–9, AT-1–2", "Met"],
     ["US-2 Log vitals", "FR-4, FR-5", "LogViewModel + /health-entries", "VT-6, AT-3–4", "Met"],
     ["US-3 AI insights", "FR-6", "InsightsViewModel + /insights/generate + gpt-4o-mini", "VT-7, AT-5–6", "Met"],
     ["US-4 Profile + import", "FR-9, FR-10", "ProfileViewModel + /profile + /import", "AT-7", "Met"],
     ["US-5 Dashboard", "FR-7, FR-8", "DashboardViewModel + /dashboard/summary", "AT-8", "Met"],
     ["(cross-cutting)", "FR-11 offline", "— (not built)", "—", "Not Met"],
     ["(cross-cutting)", "FR-12 Repository", "— (not built)", "—", "Not Met"],
     ["(cross-cutting)", "FR-13 auth", "— (not built)", "—", "Not Met"]],
    widths=[1.5, 1.3, 2.1, 1.4, 0.8], font=8.5)
H2(doc, "6.1 Findings")
bullet(doc, "Coverage: all five core user stories are implemented and validated end-to-end; no core story is unverified.")
bullet(doc, "Honest gaps: three cross-cutting requirements (offline cache, Repository seam, authentication) were not built and trace to nothing in the implementation — they are carried forward as future work.")
bullet(doc, "Every passing acceptance test maps back to a story (no orphan tests).")
page_break(doc)

# ===========================================================================
# 7. PROJECT MANAGEMENT SUMMARY
# ===========================================================================
H1(doc, "7. Project Management Summary")

H2(doc, "7.1 Methodology & Cadence")
P(doc, "The team worked in weekly milestone increments (GP1–GP6) culminating in this final "
       "report, tracking work on a GitHub-based board and committing through feature branches "
       "with review before merge. Each milestone paired a documentation deliverable (SRS growth) "
       "with concrete implementation progress.")

H2(doc, "7.2 Milestone Log")
add_table(doc,
    ["Milestone", "Focus", "Outcome"],
    [["GP1–GP2", "Scope, requirements, user stories, narrative.", "Project scope and functional/non-functional requirements set."],
     ["GP3", "Use cases, diagrams, traceability, state machine.", "Design artifacts authored (use-case/activity/sequence/state)."],
     ["GP4", "Repository/API contract, DTOs, thread boundaries.", "Data + concurrency design specified (later partly revised)."],
     ["GP5", "Reactive state architecture, wireframes, test plans.", "Compose UI + ViewModel/StateFlow spine built."],
     ["GP6", "Integration, testing, architectural audit.", "Network layer wired to backend; core features integrated."],
     ["Final", "As-built reconciliation + report.", "Working MVP delivered; gaps documented honestly."]],
    widths=[1.1, 2.7, 2.9], font=9)

H2(doc, "7.3 Project Board Summary")
add_table(doc,
    ["Column", "Representative Items at Close"],
    [["Done", "Compose screens; ViewModels/StateFlow; navigation; Retrofit layer; FastAPI backend; AI insights + fallback; file import; backend pytest suite."],
     ["In progress / cut", "Repository layer; Room offline cache (descoped to land the vertical slice)."],
     ["Backlog", "Authentication/multi-user; automated Android tests; charts/analytics polish; accessibility; remove dead template package."]],
    widths=[1.4, 5.3], font=9)

H2(doc, "7.4 Team Retrospective")
add_table(doc,
    ["What went well", "What was hard", "What we’d change"],
    [["Clean MVVM + StateFlow spine; reactive UI came together quickly.",
      "Coordinating the Android client with a live backend + AI under a deadline.",
      "Build the Repository seam first so swapping/caching sources stays cheap."],
     ["Real end-to-end integration (UI → backend → AI), not mocked.",
      "Emulator lacked a step sensor, stalling demos until simulate mode.",
      "Stand up automated client tests early instead of relying on manual UAT."],
     ["Graceful AI fallback kept the headline feature reliable.",
      "Keeping the SRS in sync with fast-moving code.",
      "Cut the dead template package and keep one source tree from the start."]],
    widths=[2.2, 2.2, 2.3], font=9)

H2(doc, "7.5 Challenges & How They Were Addressed")
bullet(doc, "AI reliability: wrapped the model call in a deterministic fallback so insights still return when the API is unavailable.", bold_lead="Challenge — ")
bullet(doc, "Rotation resets in an early prototype: moved state ownership into the ViewModel + StateFlow.", bold_lead="Challenge — ")
bullet(doc, "No emulator step sensor: added a SIMULATE_SENSORS build flag to synthesize realistic activity.", bold_lead="Challenge — ")
bullet(doc, "Scope pressure: consciously descoped the Repository/offline layers to deliver a working vertical slice, and documented the trade-off.", bold_lead="Challenge — ")

H2(doc, "7.6 Lessons Learned")
bullet(doc, "An architectural seam (Repository) is cheapest to add before the layers it separates exist; deferring it created the project’s main piece of tech debt.")
bullet(doc, "Manual testing scales poorly; even a thin automated client suite would have made regression safer.")
bullet(doc, "Designing for graceful degradation (the AI fallback) paid off more than any single feature.")
bullet(doc, "Living documents drift; reconciling the SRS against the code at the end was necessary and revealing.")
page_break(doc)

# ===========================================================================
# 8. FUTURE DEVELOPMENT OPPORTUNITIES
# ===========================================================================
H1(doc, "8. Future Development Opportunities")
P(doc, "If development continued, the following work — ordered by priority — would close the gap "
       "between the current MVP and a robust product.")
add_table(doc,
    ["Area", "Improvement", "Why / Payoff"],
    [["Architecture", "Introduce a Repository interface between ViewModels and Retrofit.", "Restores the planned seam; enables caching and isolates the network."],
     ["Persistence", "Add Room as a write-through cache behind the Repository.", "Offline support and instant loads; fewer redundant network calls."],
     ["Testing", "Add JVM ViewModel tests + Compose/Espresso UI tests in CI.", "Reproducible coverage; catches regressions automatically."],
     ["Security", "Add authentication and per-user data isolation; secret management for keys.", "Removes the single hardcoded user; protects data."],
     ["Performance", "Cache + pagination for history; lazy lists; image/asset tuning.", "Smoother scrolling and lower latency on large histories."],
     ["AI quality", "Richer prompts, trend-aware scoring, and on-device summarization options.", "More accurate, explainable insights."],
     ["Accessibility", "Content descriptions, dynamic type, contrast and TalkBack passes.", "Usable by more people; meets accessibility expectations."],
     ["UX", "Charts/analytics polish, reminders/notifications, richer logging UIs.", "Higher engagement and daily value."],
     ["Hygiene", "Remove the dead vitaliq.main template package.", "One clean source tree; less confusion."]],
    widths=[1.2, 3.0, 2.5], font=9)
page_break(doc)

# ===========================================================================
# 9. REFLECTION & CONCLUSION
# ===========================================================================
H1(doc, "9. Reflection & Conclusion")

H2(doc, "9.1 What We Learned")
P(doc, "Building VitalIQ taught us that the hard part of modern app development is rarely a single "
       "feature — it is the disciplined wiring between layers and the decisions about what to "
       "build now versus later. We learned modern Android in depth: Jetpack Compose as a function "
       "of state, MVVM with StateFlow, and coroutine/lifecycle hygiene that survives rotation "
       "without leaks. We learned to integrate a real backend and a real AI model, and to design "
       "for failure so the product degrades gracefully instead of breaking.")

H2(doc, "9.2 On Architecture & Trade-offs")
P(doc, "Our most instructive decision was also our biggest miss: deferring the Repository and "
       "offline layers to land a working end-to-end slice. The app works and demos well, but the "
       "deferral coupled ViewModels to the network and left no offline path. The lesson is "
       "concrete — build the seam before the layers it separates exist, because retrofitting it "
       "later touches everything. Naming this honestly is more valuable than hiding it.")

H2(doc, "9.3 On Collaboration, Testing & Process")
P(doc, "Weekly milestones kept momentum and forced steady integration, but our reliance on manual "
       "testing made regressions a manual risk; a small automated client suite would have repaid "
       "itself quickly. Keeping documentation honest — reconciling the SRS against the code at the "
       "end — turned out to be a core engineering activity, not an afterthought.")

H2(doc, "9.4 Conclusion")
callout(doc, "VitalIQ delivered a working, integrated MVP: a reactive Compose client driving a live "
             "FastAPI + MongoDB + AI backend, with every core user story demonstrable end-to-end. "
             "It is not finished — it lacks the Repository seam, offline persistence, authentication, "
             "and automated client tests — and this report records those gaps as deliberately as it "
             "records the wins. We end the quarter with a product that works, a clear-eyed view of "
             "its limits, and a prioritized path to close them. That combination — something built, "
             "honestly assessed, and ready to grow — is the real outcome of the project.")

# ===========================================================================
# SAVE
# ===========================================================================
doc.save(OUT)
print("SAVED:", OUT)
print("paragraphs:", len(doc.paragraphs), "tables:", len(doc.tables),
      "images:", len(doc.inline_shapes))
