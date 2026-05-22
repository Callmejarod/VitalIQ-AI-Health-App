import React, { useEffect, useRef, useState } from "react";
import {
  View,
  Text,
  StyleSheet,
  TouchableOpacity,
  Alert,
  Platform,
} from "react-native";
import { SafeAreaView } from "react-native-safe-area-context";
import { Ionicons } from "@expo/vector-icons";
import {
  Accelerometer,
  AccelerometerMeasurement,
  Pedometer,
} from "expo-sensors";
import { colors } from "@/src/theme";
import { api } from "@/src/api";

type ActivityType = "stationary" | "walking" | "running" | "mixed";

const ACTIVITY_LABEL: Record<ActivityType, string> = {
  stationary: "Stationary",
  walking: "Walking",
  running: "Running",
  mixed: "Active",
};

const ACTIVITY_ICON: Record<ActivityType, keyof typeof Ionicons.glyphMap> = {
  stationary: "pause-circle",
  walking: "walk",
  running: "speedometer",
  mixed: "pulse",
};

function classify(magnitude: number): ActivityType {
  // magnitude is acceleration deviation from 1g
  if (magnitude < 0.08) return "stationary";
  if (magnitude < 0.35) return "walking";
  if (magnitude < 1.0) return "mixed";
  return "running";
}

function formatDuration(seconds: number): string {
  const h = Math.floor(seconds / 3600);
  const m = Math.floor((seconds % 3600) / 60);
  const s = seconds % 60;
  const mm = String(m).padStart(2, "0");
  const ss = String(s).padStart(2, "0");
  return h > 0 ? `${h}:${mm}:${ss}` : `${mm}:${ss}`;
}

export default function WorkoutScreen() {
  const [running, setRunning] = useState(false);
  const [duration, setDuration] = useState(0);
  const [steps, setSteps] = useState(0);
  const [intensity, setIntensity] = useState(0); // running avg magnitude
  const [currentActivity, setCurrentActivity] =
    useState<ActivityType>("stationary");
  const [pedometerAvailable, setPedometerAvailable] = useState(false);
  const [saving, setSaving] = useState(false);

  const startedAtRef = useRef<Date | null>(null);
  const startStepsRef = useRef<number | null>(null);
  const intervalRef = useRef<ReturnType<typeof setInterval> | null>(null);
  const accelSubRef = useRef<{ remove: () => void } | null>(null);
  const pedSubRef = useRef<{ remove: () => void } | null>(null);
  const breakdownRef = useRef<Record<ActivityType, number>>({
    stationary: 0,
    walking: 0,
    running: 0,
    mixed: 0,
  });
  const magSamplesRef = useRef<number[]>([]);

  useEffect(() => {
    console.log("[Lifecycle] Workout mounted");
    Pedometer.isAvailableAsync()
      .then(setPedometerAvailable)
      .catch(() => setPedometerAvailable(false));
    return () => {
      console.log("[Lifecycle] Workout unmounted");
      cleanup();
    };
  }, []);

  const cleanup = () => {
    accelSubRef.current?.remove();
    pedSubRef.current?.remove();
    accelSubRef.current = null;
    pedSubRef.current = null;
    if (intervalRef.current) clearInterval(intervalRef.current);
    intervalRef.current = null;
  };

  const handleAccel = (data: AccelerometerMeasurement) => {
    const { x, y, z } = data;
    const mag = Math.abs(Math.sqrt(x * x + y * y + z * z) - 1);
    magSamplesRef.current.push(mag);
    if (magSamplesRef.current.length > 40) magSamplesRef.current.shift();
    const avg =
      magSamplesRef.current.reduce((a, b) => a + b, 0) /
      magSamplesRef.current.length;
    setIntensity(avg);
    const act = classify(avg);
    setCurrentActivity(act);
    breakdownRef.current[act] += 1;
  };

  const start = async () => {
    try {
      magSamplesRef.current = [];
      breakdownRef.current = {
        stationary: 0,
        walking: 0,
        running: 0,
        mixed: 0,
      };
      setSteps(0);
      setDuration(0);
      setIntensity(0);
      startedAtRef.current = new Date();

      Accelerometer.setUpdateInterval(200);
      accelSubRef.current = Accelerometer.addListener(handleAccel);

      if (pedometerAvailable) {
        // Use watchStepCount for live updates (delta since subscription)
        pedSubRef.current = Pedometer.watchStepCount((res) => {
          setSteps(res.steps);
        });
      }

      intervalRef.current = setInterval(() => {
        setDuration((d) => d + 1);
      }, 1000);

      setRunning(true);
      console.log("[Workout] started");
    } catch (e) {
      console.log("[Workout] start error", e);
      Alert.alert(
        "Sensor error",
        "Could not start workout sensors. " + String(e),
      );
    }
  };

  const stop = async () => {
    if (!startedAtRef.current) return;
    setSaving(true);
    const endedAt = new Date();
    const totalDuration = duration;
    cleanup();

    // dominant activity
    const counts = breakdownRef.current;
    const dominant = (Object.entries(counts).sort(
      (a, b) => b[1] - a[1],
    )[0]?.[0] || "stationary") as ActivityType;

    try {
      await api.createWorkout({
        started_at: startedAtRef.current.toISOString(),
        ended_at: endedAt.toISOString(),
        duration_seconds: totalDuration,
        activity_type: dominant,
        steps,
        avg_intensity: Number(intensity.toFixed(3)),
        activity_breakdown: counts,
      });
      Alert.alert(
        "Workout saved",
        `${formatDuration(totalDuration)} · ${steps} steps · ${ACTIVITY_LABEL[dominant]}`,
      );
    } catch (e) {
      console.log("[Workout] save error", e);
      Alert.alert("Save failed", String(e));
    } finally {
      setRunning(false);
      setSaving(false);
      startedAtRef.current = null;
    }
  };

  const intensityPct = Math.min(100, Math.round(intensity * 100));

  return (
    <SafeAreaView
      style={[styles.screen, running && styles.screenActive]}
      testID="workout-screen"
      edges={["top"]}
    >
      <View style={styles.headerRow}>
        <Text
          style={[styles.headerTxt, running && { color: "#fff" }]}
          testID="workout-title"
        >
          {running ? "LIVE WORKOUT" : "Workout"}
        </Text>
        <View
          style={[
            styles.actChip,
            running && { backgroundColor: "rgba(255,255,255,0.1)" },
          ]}
        >
          <Ionicons
            name={ACTIVITY_ICON[currentActivity]}
            size={14}
            color={running ? "#fff" : colors.brand}
          />
          <Text
            style={[styles.actChipText, running && { color: "#fff" }]}
            testID="activity-chip"
          >
            {ACTIVITY_LABEL[currentActivity]}
          </Text>
        </View>
      </View>

      <View style={styles.timerWrap}>
        <Text
          style={[styles.timer, running && { color: "#fff" }]}
          testID="workout-timer"
        >
          {formatDuration(duration)}
        </Text>
        <Text style={[styles.timerLabel, running && { color: "#A0B8AB" }]}>
          DURATION
        </Text>
      </View>

      <View style={styles.statsRow}>
        <View style={[styles.statBox, running && styles.statBoxActive]}>
          <Text style={[styles.statVal, running && { color: "#fff" }]} testID="workout-steps">
            {steps}
          </Text>
          <Text style={[styles.statLabel, running && { color: "#A0B8AB" }]}>
            STEPS
          </Text>
        </View>
        <View style={[styles.statBox, running && styles.statBoxActive]}>
          <Text style={[styles.statVal, running && { color: "#fff" }]} testID="workout-intensity">
            {intensityPct}%
          </Text>
          <Text style={[styles.statLabel, running && { color: "#A0B8AB" }]}>
            INTENSITY
          </Text>
        </View>
      </View>

      {!pedometerAvailable && (
        <Text style={[styles.warn, running && { color: "#F2A68D" }]}>
          {Platform.OS === "web"
            ? "Pedometer not available on web. Use a device for step counts."
            : "Pedometer unavailable on this device — steps will read 0."}
        </Text>
      )}

      <View style={{ flex: 1 }} />

      <View style={styles.controls}>
        {!running ? (
          <TouchableOpacity
            style={styles.startBtn}
            onPress={start}
            testID="workout-start-button"
          >
            <Ionicons name="play" size={28} color="#fff" />
            <Text style={styles.startTxt}>START WORKOUT</Text>
          </TouchableOpacity>
        ) : (
          <TouchableOpacity
            style={[styles.startBtn, styles.stopBtn]}
            onPress={stop}
            disabled={saving}
            testID="workout-stop-button"
          >
            <Ionicons name="stop" size={28} color="#fff" />
            <Text style={styles.startTxt}>
              {saving ? "SAVING…" : "STOP & SAVE"}
            </Text>
          </TouchableOpacity>
        )}
      </View>
    </SafeAreaView>
  );
}

const styles = StyleSheet.create({
  screen: { flex: 1, backgroundColor: colors.bg, padding: 24 },
  screenActive: { backgroundColor: colors.workoutActive },
  headerRow: {
    flexDirection: "row",
    justifyContent: "space-between",
    alignItems: "center",
    marginBottom: 24,
  },
  headerTxt: {
    fontSize: 13,
    fontWeight: "800",
    letterSpacing: 2,
    color: colors.textPrimary,
  },
  actChip: {
    flexDirection: "row",
    alignItems: "center",
    backgroundColor: colors.bgSecondary,
    paddingHorizontal: 12,
    paddingVertical: 6,
    borderRadius: 999,
    gap: 6,
  },
  actChipText: {
    fontSize: 12,
    fontWeight: "700",
    color: colors.brand,
  },
  timerWrap: { alignItems: "center", marginTop: 40, marginBottom: 32 },
  timer: {
    fontSize: 84,
    fontWeight: "800",
    letterSpacing: -3,
    color: colors.textPrimary,
    fontVariant: ["tabular-nums"],
  },
  timerLabel: {
    fontSize: 11,
    fontWeight: "700",
    letterSpacing: 2,
    color: colors.textMuted,
    marginTop: 4,
  },
  statsRow: { flexDirection: "row", gap: 12, marginTop: 12 },
  statBox: {
    flex: 1,
    backgroundColor: colors.card,
    borderRadius: 20,
    borderWidth: 1,
    borderColor: colors.border,
    padding: 18,
    alignItems: "center",
  },
  statBoxActive: {
    backgroundColor: "rgba(255,255,255,0.06)",
    borderColor: "rgba(255,255,255,0.12)",
  },
  statVal: {
    fontSize: 32,
    fontWeight: "800",
    color: colors.textPrimary,
    letterSpacing: -1,
  },
  statLabel: {
    fontSize: 11,
    fontWeight: "700",
    letterSpacing: 1.5,
    color: colors.textMuted,
    marginTop: 4,
  },
  warn: {
    marginTop: 16,
    fontSize: 12,
    color: colors.warning,
    textAlign: "center",
  },
  controls: { marginBottom: 16 },
  startBtn: {
    backgroundColor: colors.brand,
    flexDirection: "row",
    alignItems: "center",
    justifyContent: "center",
    paddingVertical: 20,
    borderRadius: 999,
    gap: 12,
  },
  stopBtn: { backgroundColor: colors.error },
  startTxt: {
    color: "#fff",
    fontSize: 16,
    fontWeight: "800",
    letterSpacing: 1.5,
  },
});
