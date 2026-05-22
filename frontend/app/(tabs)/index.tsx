import React, { useCallback, useEffect, useState } from "react";
import {
  View,
  Text,
  StyleSheet,
  ScrollView,
  TouchableOpacity,
  RefreshControl,
} from "react-native";
import { SafeAreaView } from "react-native-safe-area-context";
import { Ionicons } from "@expo/vector-icons";
import { router, useFocusEffect } from "expo-router";

import { colors } from "@/src/theme";
import { api } from "@/src/api";
import { HealthRing } from "@/src/components/HealthRing";
import { StatCard } from "@/src/components/StatCard";
import { Section } from "@/src/components/Section";

export default function Dashboard() {
  const [summary, setSummary] = useState<any>(null);
  const [profile, setProfile] = useState<any>(null);
  const [loading, setLoading] = useState(true);

  const load = useCallback(async () => {
    try {
      const [s, p] = await Promise.all([
        api.dashboardSummary(),
        api.getProfile(),
      ]);
      setSummary(s);
      setProfile(p);
    } catch (e) {
      console.log("[Dashboard] load error", e);
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    console.log("[Lifecycle] Dashboard mounted");
    load();
    return () => console.log("[Lifecycle] Dashboard unmounted");
  }, [load]);

  useFocusEffect(
    useCallback(() => {
      load();
    }, [load]),
  );

  const score = summary?.health_score ?? 0;
  const stepGoal = profile?.daily_step_goal ?? 8000;
  const waterGoal = profile?.daily_water_goal_ml ?? 2000;

  return (
    <SafeAreaView style={styles.screen} edges={["top"]} testID="dashboard-screen">
      <ScrollView
        contentContainerStyle={styles.scroll}
        refreshControl={<RefreshControl refreshing={loading} onRefresh={load} />}
      >
        <View style={styles.header}>
          <View>
            <Text style={styles.greeting}>Good day,</Text>
            <Text style={styles.name}>{profile?.name || "Friend"}</Text>
          </View>
          <TouchableOpacity
            style={styles.avatarBtn}
            onPress={() => router.push("/(tabs)/profile")}
            testID="profile-avatar-btn"
          >
            <Ionicons name="person" size={20} color={colors.brand} />
          </TouchableOpacity>
        </View>

        <View style={styles.ringCard}>
          <HealthRing
            score={score}
            label={score ? "Health Score" : "Tap Insights"}
          />
          <Text style={styles.ringHint}>
            {score
              ? summary?.last_bp
                ? `Last BP ${summary.last_bp.sys}/${summary.last_bp.dia}`
                : "Looking good — keep it going"
              : "Generate your first AI insight to compute a score"}
          </Text>
          <TouchableOpacity
            style={styles.ringBtn}
            onPress={() => router.push("/(tabs)/insights")}
            testID="open-insights-btn"
          >
            <Text style={styles.ringBtnText}>
              {score ? "View Insights" : "Generate Insights"}
            </Text>
            <Ionicons name="arrow-forward" size={16} color="#fff" />
          </TouchableOpacity>
        </View>

        <Section title="Today">
          <View style={styles.grid}>
            <StatCard
              icon="footsteps"
              label="Steps"
              value={String(summary?.steps_today ?? 0)}
              caption={`Goal ${stepGoal.toLocaleString()}`}
              testID="stat-steps"
            />
            <StatCard
              icon="water"
              iconColor={colors.accent}
              label="Water"
              value={String(summary?.water_ml_today ?? 0)}
              unit="ml"
              caption={`Goal ${waterGoal}ml`}
              testID="stat-water"
            />
          </View>
          <View style={[styles.grid, { marginTop: 12 }]}>
            <StatCard
              icon="bed"
              iconColor={colors.success}
              label="Sleep"
              value={
                summary?.last_sleep_hours
                  ? summary.last_sleep_hours.toFixed(1)
                  : "—"
              }
              unit={summary?.last_sleep_hours ? "h" : ""}
              caption="Last logged"
              testID="stat-sleep"
            />
            <StatCard
              icon="heart"
              iconColor={colors.error}
              label="Resting HR"
              value={summary?.last_hr ? String(summary.last_hr) : "—"}
              unit={summary?.last_hr ? "bpm" : ""}
              caption="Last reading"
              testID="stat-hr"
            />
          </View>
          <View style={[styles.grid, { marginTop: 12 }]}>
            <StatCard
              icon="flame"
              iconColor={colors.warning}
              label="Workouts"
              value={String(summary?.workouts_today ?? 0)}
              caption={`${summary?.workout_minutes_today ?? 0} min today`}
              testID="stat-workouts"
            />
            <StatCard
              icon="trending-up"
              label="Active"
              value={`${summary?.workout_minutes_today ?? 0}`}
              unit="min"
              caption="Today"
              testID="stat-active"
            />
          </View>
        </Section>

        <Section
          title="Quick Actions"
          action={
            <TouchableOpacity
              onPress={() => router.push("/history")}
              testID="open-history-btn"
            >
              <Text style={styles.linkText}>History →</Text>
            </TouchableOpacity>
          }
        >
          <View style={styles.quickRow}>
            <QuickAction
              icon="walk"
              label="Start Workout"
              onPress={() => router.push("/(tabs)/workout")}
              testID="quick-workout-btn"
            />
            <QuickAction
              icon="clipboard"
              label="Log Health"
              onPress={() => router.push("/(tabs)/log")}
              testID="quick-log-btn"
            />
            <QuickAction
              icon="sparkles"
              label="AI Insights"
              onPress={() => router.push("/(tabs)/insights")}
              testID="quick-insights-btn"
            />
          </View>
        </Section>
        <View style={{ height: 24 }} />
      </ScrollView>
    </SafeAreaView>
  );
}

function QuickAction({
  icon,
  label,
  onPress,
  testID,
}: {
  icon: keyof typeof Ionicons.glyphMap;
  label: string;
  onPress: () => void;
  testID?: string;
}) {
  return (
    <TouchableOpacity
      style={styles.quickItem}
      onPress={onPress}
      testID={testID}
      activeOpacity={0.8}
    >
      <View style={styles.quickIcon}>
        <Ionicons name={icon} size={22} color={colors.brand} />
      </View>
      <Text style={styles.quickLabel}>{label}</Text>
    </TouchableOpacity>
  );
}

const styles = StyleSheet.create({
  screen: { flex: 1, backgroundColor: colors.bg },
  scroll: { padding: 24, paddingBottom: 40 },
  header: {
    flexDirection: "row",
    justifyContent: "space-between",
    alignItems: "center",
    marginBottom: 24,
  },
  greeting: { color: colors.textSecondary, fontSize: 14 },
  name: {
    color: colors.textPrimary,
    fontSize: 26,
    fontWeight: "800",
    letterSpacing: -0.5,
  },
  avatarBtn: {
    width: 44,
    height: 44,
    borderRadius: 22,
    backgroundColor: colors.bgSecondary,
    alignItems: "center",
    justifyContent: "center",
    borderWidth: 1,
    borderColor: colors.border,
  },
  ringCard: {
    backgroundColor: colors.card,
    borderRadius: 28,
    borderWidth: 1,
    borderColor: colors.border,
    padding: 24,
    alignItems: "center",
  },
  ringHint: {
    color: colors.textSecondary,
    fontSize: 13,
    marginTop: 16,
    textAlign: "center",
  },
  ringBtn: {
    marginTop: 16,
    backgroundColor: colors.brand,
    paddingVertical: 12,
    paddingHorizontal: 22,
    borderRadius: 999,
    flexDirection: "row",
    alignItems: "center",
    gap: 8,
  },
  ringBtnText: { color: "#fff", fontWeight: "700", marginRight: 6 },
  grid: { flexDirection: "row", gap: 12 },
  quickRow: { flexDirection: "row", gap: 12 },
  quickItem: {
    flex: 1,
    backgroundColor: colors.card,
    borderRadius: 20,
    borderWidth: 1,
    borderColor: colors.border,
    padding: 16,
    alignItems: "center",
  },
  quickIcon: {
    width: 44,
    height: 44,
    borderRadius: 14,
    backgroundColor: `${colors.brand}15`,
    alignItems: "center",
    justifyContent: "center",
    marginBottom: 8,
  },
  quickLabel: {
    fontSize: 12,
    fontWeight: "600",
    color: colors.textPrimary,
    textAlign: "center",
  },
  linkText: { color: colors.brand, fontWeight: "600", fontSize: 13 },
});
