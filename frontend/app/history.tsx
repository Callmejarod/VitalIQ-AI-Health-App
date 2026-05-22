import React, { useCallback, useEffect, useState } from "react";
import {
  View,
  Text,
  StyleSheet,
  ScrollView,
  TouchableOpacity,
  Dimensions,
  RefreshControl,
} from "react-native";
import { SafeAreaView } from "react-native-safe-area-context";
import { Ionicons } from "@expo/vector-icons";
import { router } from "expo-router";

import { colors } from "@/src/theme";
import { api } from "@/src/api";
import { LineChart } from "@/src/components/LineChart";

type Tab = "steps" | "weight" | "bp" | "activity";

export default function HistoryScreen() {
  const [tab, setTab] = useState<Tab>("steps");
  const [steps, setSteps] = useState<{ x: string; y: number }[]>([]);
  const [weight, setWeight] = useState<{ x: string; y: number }[]>([]);
  const [bp, setBp] = useState<{ x: string; y: number }[]>([]);
  const [activity, setActivity] = useState<Record<string, number>>({});
  const [loading, setLoading] = useState(true);

  const load = useCallback(async () => {
    setLoading(true);
    try {
      const [workouts, weightEntries, bpEntries] = await Promise.all([
        api.listWorkouts(),
        api.listHealthEntries("weight"),
        api.listHealthEntries("bp"),
      ]);

      const stepsByDay: Record<string, number> = {};
      const actCounts: Record<string, number> = {};
      for (const w of workouts) {
        const day = (w.started_at || "").slice(0, 10);
        stepsByDay[day] = (stepsByDay[day] || 0) + (w.steps || 0);
        actCounts[w.activity_type] = (actCounts[w.activity_type] || 0) + 1;
      }
      const stepArr = Object.entries(stepsByDay)
        .sort()
        .slice(-14)
        .map(([x, y]) => ({ x, y }));

      const weightArr = [...weightEntries]
        .reverse()
        .slice(-14)
        .map((e: any) => ({
          x: (e.logged_at || "").slice(5, 10),
          y: e.value?.weight_kg || 0,
        }));

      const bpArr = [...bpEntries]
        .reverse()
        .slice(-14)
        .map((e: any) => ({
          x: (e.logged_at || "").slice(5, 10),
          y: e.value?.sys || 0,
        }));

      setSteps(stepArr);
      setWeight(weightArr);
      setBp(bpArr);
      setActivity(actCounts);
    } catch (e) {
      console.log("[History] error", e);
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    load();
  }, [load]);

  const width = Math.min(Dimensions.get("window").width - 48, 600);

  return (
    <SafeAreaView style={styles.screen} edges={["top"]} testID="history-screen">
      <View style={styles.headerRow}>
        <TouchableOpacity onPress={() => router.back()} testID="history-back">
          <Ionicons name="arrow-back" size={24} color={colors.textPrimary} />
        </TouchableOpacity>
        <Text style={styles.title}>History</Text>
        <View style={{ width: 24 }} />
      </View>

      <View style={styles.tabsRow}>
        {(["steps", "weight", "bp", "activity"] as Tab[]).map((t) => (
          <TouchableOpacity
            key={t}
            onPress={() => setTab(t)}
            style={[styles.tab, tab === t && styles.tabActive]}
            testID={`history-tab-${t}`}
          >
            <Text
              style={[styles.tabTxt, tab === t && { color: "#fff" }]}
            >
              {t === "bp" ? "BP" : t[0].toUpperCase() + t.slice(1)}
            </Text>
          </TouchableOpacity>
        ))}
      </View>

      <ScrollView
        contentContainerStyle={styles.scroll}
        refreshControl={<RefreshControl refreshing={loading} onRefresh={load} />}
      >
        {tab === "steps" && (
          <ChartCard title="Steps · Last 14 entries">
            <LineChart data={steps} width={width} unit="" />
          </ChartCard>
        )}
        {tab === "weight" && (
          <ChartCard title="Weight (kg) · Last 14 entries">
            <LineChart data={weight} width={width} unit="kg" />
          </ChartCard>
        )}
        {tab === "bp" && (
          <ChartCard title="Systolic BP · Last 14 entries">
            <LineChart data={bp} width={width} unit="" />
          </ChartCard>
        )}
        {tab === "activity" && (
          <ChartCard title="Activity Breakdown">
            <View style={{ gap: 12 }}>
              {Object.entries(activity).length === 0 ? (
                <Text style={styles.empty}>No workouts logged yet.</Text>
              ) : (
                Object.entries(activity).map(([k, v]) => (
                  <View key={k} style={styles.actRow}>
                    <Text style={styles.actLabel}>{k}</Text>
                    <View style={styles.actBarTrack}>
                      <View
                        style={[
                          styles.actBarFill,
                          {
                            width: `${Math.min(
                              100,
                              (v /
                                Math.max(
                                  1,
                                  Math.max(...Object.values(activity)),
                                )) *
                                100,
                            )}%`,
                          },
                        ]}
                      />
                    </View>
                    <Text style={styles.actCount}>{v}</Text>
                  </View>
                ))
              )}
            </View>
          </ChartCard>
        )}
      </ScrollView>
    </SafeAreaView>
  );
}

function ChartCard({
  title,
  children,
}: {
  title: string;
  children: React.ReactNode;
}) {
  return (
    <View style={styles.card}>
      <Text style={styles.cardTitle}>{title}</Text>
      {children}
    </View>
  );
}

const styles = StyleSheet.create({
  screen: { flex: 1, backgroundColor: colors.bg },
  headerRow: {
    flexDirection: "row",
    alignItems: "center",
    justifyContent: "space-between",
    paddingHorizontal: 24,
    paddingTop: 8,
    paddingBottom: 16,
  },
  title: {
    fontSize: 20,
    fontWeight: "800",
    color: colors.textPrimary,
  },
  tabsRow: {
    flexDirection: "row",
    gap: 8,
    paddingHorizontal: 24,
    marginBottom: 12,
  },
  tab: {
    flex: 1,
    paddingVertical: 10,
    alignItems: "center",
    borderRadius: 999,
    backgroundColor: colors.bgSecondary,
    borderWidth: 1,
    borderColor: colors.border,
  },
  tabActive: {
    backgroundColor: colors.brand,
    borderColor: colors.brand,
  },
  tabTxt: {
    color: colors.textPrimary,
    fontWeight: "700",
    fontSize: 13,
  },
  scroll: { padding: 24, paddingTop: 8 },
  card: {
    backgroundColor: colors.card,
    borderRadius: 24,
    borderWidth: 1,
    borderColor: colors.border,
    padding: 18,
  },
  cardTitle: {
    fontSize: 13,
    fontWeight: "800",
    color: colors.textPrimary,
    letterSpacing: 1.4,
    textTransform: "uppercase",
    marginBottom: 12,
  },
  empty: { color: colors.textMuted, textAlign: "center", padding: 16 },
  actRow: { flexDirection: "row", alignItems: "center", gap: 12 },
  actLabel: {
    width: 80,
    color: colors.textPrimary,
    textTransform: "capitalize",
    fontWeight: "600",
  },
  actBarTrack: {
    flex: 1,
    height: 10,
    backgroundColor: colors.border,
    borderRadius: 5,
    overflow: "hidden",
  },
  actBarFill: {
    height: "100%",
    backgroundColor: colors.brand,
    borderRadius: 5,
  },
  actCount: {
    width: 28,
    textAlign: "right",
    color: colors.textPrimary,
    fontWeight: "700",
  },
});
