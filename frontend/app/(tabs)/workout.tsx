import React, { useEffect, useRef, useState } from "react";
import {
  View,
  Text,
  StyleSheet,
  TouchableOpacity,
  Alert,
  Modal,
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
  const [summary, setSummary] = useState<{
    duration: number;
    steps: number;
    dominant: ActivityType;
    breakdown: Record<ActivityType, number>;
    avg_intensity: number;
  } | null>(null);

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

  const start = () => {
    console.log("[Workout] start invoked");
    // Reset state and start the timer FIRST, so the UI always responds.
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
    setRunning(true);

    intervalRef.current = setInterval(() => {
      setDuration((d) => d + 1);
    }, 1000);

    // Subscribe sensors — non-blocking, each in its own try/catch.
    try {
      Accelerometer.setUpdateInterval(200);
      accelSubRef.current = Accelerometer.addListener(handleAccel);
      console.log("[Workout] accelerometer subscribed");
    } catch (e) {
      console.log("[Workout] accelerometer subscribe failed", e);
    }

    if (pedometerAvailable) {
      try {
        pedSubRef.current = Pedometer.watchStepCount((res) => {
          setSteps(res.steps);
        });
        console.log("[Workout] pedometer subscribed");
      } catch (e) {
        console.log("[Workout] pedometer subscribe failed", e);
      }
    }
  };

  const stop = async () => {
    if (!startedAtRef.current) return;
    setSaving(true);
    const endedAt = new Date();
    const totalDuration = duration;
    const finalSteps = steps;
    const finalIntensity = intensity;
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
        steps: finalSteps,
        avg_intensity: Number(finalIntensity.toFixed(3)),
        activity_breakdown: counts,
      });
      // Show sensor capture summary modal instead of a plain alert.
      setSummary({
        duration: totalDuration,
        steps: finalSteps,
        dominant,
        breakdown: { ...counts },
        avg_intensity: finalIntensity,
      });
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

      <Modal
        visible={summary !== null}
        transparent
        animationType="slide"
        onRequestClose={() => setSummary(null)}
      >
        <View style={styles.modalBackdrop}>
          <View style={styles.modalCard} testID="workout-summary-modal">
            <View style={styles.modalHeader}>
              <View style={styles.modalIcon}>
                <Ionicons name="pulse" size={22} color={colors.brand} />
              </View>
              <View style={{ flex: 1 }}>
                <Text style={styles.modalEyebrow}>SENSOR CAPTURE</Text>
                <Text style={styles.modalTitle}>Workout Saved</Text>
              </View>
              <TouchableOpacity
                onPress={() => setSummary(null)}
                testID="summary-close-btn"
              >
                <Ionicons name="close" size={22} color={colors.textMuted} />
              </TouchableOpacity>
            </View>

            {summary && (
              <>
                <View style={styles.modalRow}>
                  <SummaryStat
                    label="Duration"
                    value={formatDuration(summary.duration)}
                  />
                  <SummaryStat
                    label="Steps"
                    value={String(summary.steps)}
                  />
                  <SummaryStat
                    label="Intensity"
                    value={`${Math.round(summary.avg_intensity * 100)}%`}
                  />
                </View>

                <Text style={styles.modalEyebrow2}>
                  Accelerometer Activity Mix
                </Text>
                <View style={{ gap: 6 }}>
                  {(() => {
                    const total =
                      Object.values(summary.breakdown).reduce(
                        (a, b) => a + b,
                        0,
                      ) || 1;
                    return (Object.keys(ACTIVITY_LABEL) as ActivityType[]).map(
                      (key) => {
                        const pct = Math.round(
                          (summary.breakdown[key] / total) * 100,
                        );
                        return (
                          <View key={key} style={styles.actRow}>
                            <Text style={styles.actName}>
                              {ACTIVITY_LABEL[key]}
                              {key === summary.dominant ? " ★" : ""}
                            </Text>
                            <View style={styles.actTrack}>
                              <View
                                style={[
                                  styles.actFill,
                                  { width: `${pct}%` },
                                ]}
                              />
                            </View>
                            <Text style={styles.actPct}>{pct}%</Text>
                          </View>
                        );
                      },
                    );
                  })()}
                </View>

                <Text style={styles.modalNote}>
                  This sensor fingerprint is now part of your AI health
                  payload — tap Insights → Refresh to recompute scores.
                </Text>

                <TouchableOpacity
                  style={styles.modalDone}
                  onPress={() => setSummary(null)}
                  testID="summary-done-btn"
                >
                  <Text style={styles.modalDoneTxt}>Done</Text>
                </TouchableOpacity>
              </>
            )}
          </View>
        </View>
      </Modal>
    </SafeAreaView>
  );
}

function SummaryStat({ label, value }: { label: string; value: string }) {
  return (
    <View style={styles.sumItem}>
      <Text style={styles.sumValue}>{value}</Text>
      <Text style={styles.sumLabel}>{label}</Text>
    </View>
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
  modalBackdrop: {
    flex: 1,
    backgroundColor: "rgba(0,0,0,0.55)",
    justifyContent: "flex-end",
  },
  modalCard: {
    backgroundColor: colors.bg,
    borderTopLeftRadius: 28,
    borderTopRightRadius: 28,
    padding: 24,
    paddingBottom: 32,
  },
  modalHeader: {
    flexDirection: "row",
    alignItems: "center",
    gap: 12,
    marginBottom: 18,
  },
  modalIcon: {
    width: 40,
    height: 40,
    borderRadius: 12,
    backgroundColor: `${colors.brand}18`,
    alignItems: "center",
    justifyContent: "center",
  },
  modalEyebrow: {
    fontSize: 10,
    fontWeight: "800",
    color: colors.accent,
    letterSpacing: 2,
  },
  modalTitle: {
    fontSize: 22,
    fontWeight: "800",
    color: colors.textPrimary,
    letterSpacing: -0.5,
    marginTop: 2,
  },
  modalEyebrow2: {
    fontSize: 11,
    fontWeight: "800",
    color: colors.textMuted,
    letterSpacing: 1.5,
    textTransform: "uppercase",
    marginTop: 18,
    marginBottom: 10,
  },
  modalRow: { flexDirection: "row", gap: 10 },
  sumItem: {
    flex: 1,
    backgroundColor: colors.card,
    borderRadius: 16,
    borderWidth: 1,
    borderColor: colors.border,
    padding: 14,
    alignItems: "center",
  },
  sumValue: {
    fontSize: 18,
    fontWeight: "800",
    color: colors.textPrimary,
    letterSpacing: -0.5,
  },
  sumLabel: {
    fontSize: 10,
    fontWeight: "700",
    color: colors.textMuted,
    letterSpacing: 1,
    textTransform: "uppercase",
    marginTop: 4,
  },
  actRow: {
    flexDirection: "row",
    alignItems: "center",
    gap: 10,
  },
  actName: {
    width: 90,
    fontSize: 12,
    color: colors.textPrimary,
    fontWeight: "600",
  },
  actTrack: {
    flex: 1,
    height: 8,
    backgroundColor: colors.border,
    borderRadius: 4,
    overflow: "hidden",
  },
  actFill: {
    height: "100%",
    backgroundColor: colors.brand,
    borderRadius: 4,
  },
  actPct: {
    width: 38,
    textAlign: "right",
    fontSize: 11,
    fontWeight: "700",
    color: colors.textSecondary,
  },
  modalNote: {
    fontSize: 12,
    color: colors.textSecondary,
    lineHeight: 18,
    marginTop: 18,
    fontStyle: "italic",
  },
  modalDone: {
    backgroundColor: colors.brand,
    borderRadius: 999,
    paddingVertical: 14,
    alignItems: "center",
    marginTop: 18,
  },
  modalDoneTxt: {
    color: "#fff",
    fontWeight: "800",
    fontSize: 15,
    letterSpacing: 0.5,
  },
});
