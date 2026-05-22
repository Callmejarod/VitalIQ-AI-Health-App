import React, { useCallback, useEffect, useState } from "react";
import {
  View,
  Text,
  StyleSheet,
  ScrollView,
  TouchableOpacity,
  ActivityIndicator,
  RefreshControl,
} from "react-native";
import { SafeAreaView } from "react-native-safe-area-context";
import { Ionicons } from "@expo/vector-icons";
import { useFocusEffect } from "expo-router";

import { colors } from "@/src/theme";
import { api, Suggestion } from "@/src/api";
import { HealthRing } from "@/src/components/HealthRing";

const CATEGORY_LABELS: Record<string, string> = {
  cardiovascular: "Cardiovascular",
  activity: "Activity",
  nutrition: "Nutrition",
  hydration: "Hydration",
  medication_adherence: "Medication",
  recovery: "Recovery",
};

export default function InsightsScreen() {
  const [insight, setInsight] = useState<any>(null);
  const [loading, setLoading] = useState(true);
  const [generating, setGenerating] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const loadLatest = useCallback(async () => {
    try {
      setLoading(true);
      const data = await api.latestInsight();
      setInsight(data?.empty ? null : data);
      setError(null);
    } catch (e: any) {
      setError(String(e?.message || e));
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    console.log("[Lifecycle] Insights mounted");
    loadLatest();
  }, [loadLatest]);

  useFocusEffect(useCallback(() => { loadLatest(); }, [loadLatest]));

  const generate = async () => {
    setGenerating(true);
    setError(null);
    try {
      const data = await api.generateInsights();
      setInsight(data);
    } catch (e: any) {
      setError(String(e?.message || e));
    } finally {
      setGenerating(false);
    }
  };

  if (loading) {
    return (
      <SafeAreaView style={styles.screen} testID="insights-screen">
        <View style={styles.centerWrap}>
          <ActivityIndicator color={colors.brand} />
        </View>
      </SafeAreaView>
    );
  }

  return (
    <SafeAreaView style={styles.screen} edges={["top"]} testID="insights-screen">
      <ScrollView
        contentContainerStyle={styles.scroll}
        refreshControl={
          <RefreshControl refreshing={loading} onRefresh={loadLatest} />
        }
      >
        <View style={styles.headerRow}>
          <View>
            <Text style={styles.eyebrow}>AI POWERED</Text>
            <Text style={styles.title}>Health Intelligence</Text>
          </View>
          <TouchableOpacity
            style={styles.refreshBtn}
            onPress={generate}
            disabled={generating}
            testID="generate-insight-btn"
          >
            {generating ? (
              <ActivityIndicator color="#fff" />
            ) : (
              <Ionicons name="refresh" size={20} color="#fff" />
            )}
          </TouchableOpacity>
        </View>

        {!insight && !error && (
          <View style={styles.empty} testID="insights-empty">
            <Ionicons name="sparkles" size={36} color={colors.brand} />
            <Text style={styles.emptyTitle}>No insights yet</Text>
            <Text style={styles.emptySub}>
              Log some health data, then generate your first AI analysis.
            </Text>
            <TouchableOpacity
              style={styles.cta}
              onPress={generate}
              disabled={generating}
              testID="generate-first-btn"
            >
              <Text style={styles.ctaTxt}>
                {generating ? "Analyzing…" : "Generate Insights"}
              </Text>
            </TouchableOpacity>
          </View>
        )}

        {error && (
          <View style={styles.errorBox} testID="insights-error">
            <Ionicons name="alert-circle" size={22} color={colors.error} />
            <Text style={styles.errorTxt}>{error}</Text>
            <TouchableOpacity
              onPress={generate}
              style={styles.retryBtn}
              testID="insights-retry-btn"
            >
              <Text style={styles.retryTxt}>Retry</Text>
            </TouchableOpacity>
          </View>
        )}

        {insight && (
          <>
            <View style={styles.ringCard}>
              <HealthRing
                score={insight.overall_score}
                label="Overall Score"
              />
              <Text style={styles.trend}>{insight.trend_summary}</Text>
              {insight.used_fallback ? (
                <Text style={styles.fallback}>
                  Computed locally — AI unavailable. Tap refresh to retry.
                </Text>
              ) : null}
            </View>

            <Text style={styles.sectionTitle}>Category Breakdown</Text>
            <View style={styles.catList}>
              {Object.entries(insight.category_scores || {}).map(
                ([key, val]: [string, any]) => (
                  <CategoryBar
                    key={key}
                    label={CATEGORY_LABELS[key] || key}
                    score={val}
                  />
                ),
              )}
            </View>

            <Text style={styles.sectionTitle}>Recommendations</Text>
            <View style={{ gap: 12 }}>
              {(insight.suggestions || []).map(
                (s: Suggestion, i: number) => (
                  <View
                    key={i}
                    style={styles.sugCard}
                    testID={`suggestion-${i}`}
                  >
                    <View style={styles.sugRank}>
                      <Text style={styles.sugRankTxt}>{i + 1}</Text>
                    </View>
                    <View style={{ flex: 1 }}>
                      <Text style={styles.sugTitle}>{s.title}</Text>
                      <Text style={styles.sugDetail}>{s.detail}</Text>
                      {s.category ? (
                        <View style={styles.sugTag}>
                          <Text style={styles.sugTagTxt}>
                            {CATEGORY_LABELS[s.category] || s.category}
                          </Text>
                        </View>
                      ) : null}
                    </View>
                  </View>
                ),
              )}
            </View>
          </>
        )}
        <View style={{ height: 32 }} />
      </ScrollView>
    </SafeAreaView>
  );
}

function CategoryBar({ label, score }: { label: string; score: number }) {
  const pct = Math.max(0, Math.min(100, score));
  const color =
    pct >= 75 ? colors.success : pct >= 50 ? colors.brand : colors.accent;
  return (
    <View style={styles.catRow}>
      <View style={styles.catLabelRow}>
        <Text style={styles.catLabel}>{label}</Text>
        <Text style={[styles.catScore, { color }]}>{pct}</Text>
      </View>
      <View style={styles.catTrack}>
        <View
          style={[
            styles.catFill,
            { width: `${pct}%`, backgroundColor: color },
          ]}
        />
      </View>
    </View>
  );
}

const styles = StyleSheet.create({
  screen: { flex: 1, backgroundColor: colors.bg },
  scroll: { padding: 24, paddingBottom: 40 },
  centerWrap: { flex: 1, alignItems: "center", justifyContent: "center" },
  headerRow: {
    flexDirection: "row",
    justifyContent: "space-between",
    alignItems: "center",
    marginBottom: 24,
  },
  eyebrow: {
    fontSize: 11,
    fontWeight: "700",
    letterSpacing: 2,
    color: colors.accent,
  },
  title: {
    fontSize: 28,
    fontWeight: "800",
    color: colors.textPrimary,
    letterSpacing: -0.5,
    marginTop: 2,
  },
  refreshBtn: {
    width: 48,
    height: 48,
    borderRadius: 24,
    backgroundColor: colors.brand,
    alignItems: "center",
    justifyContent: "center",
  },
  ringCard: {
    backgroundColor: colors.card,
    borderRadius: 28,
    borderWidth: 1,
    borderColor: colors.border,
    padding: 24,
    alignItems: "center",
  },
  trend: {
    color: colors.textSecondary,
    fontSize: 14,
    marginTop: 16,
    textAlign: "center",
    lineHeight: 20,
  },
  fallback: {
    color: colors.warning,
    fontSize: 12,
    marginTop: 8,
    textAlign: "center",
  },
  sectionTitle: {
    fontSize: 13,
    fontWeight: "800",
    color: colors.textPrimary,
    letterSpacing: 1.5,
    textTransform: "uppercase",
    marginTop: 28,
    marginBottom: 12,
  },
  catList: { gap: 16 },
  catRow: {},
  catLabelRow: {
    flexDirection: "row",
    justifyContent: "space-between",
    marginBottom: 6,
  },
  catLabel: { color: colors.textPrimary, fontWeight: "600" },
  catScore: { fontWeight: "800" },
  catTrack: {
    height: 10,
    backgroundColor: colors.border,
    borderRadius: 5,
    overflow: "hidden",
  },
  catFill: { height: "100%", borderRadius: 5 },
  sugCard: {
    flexDirection: "row",
    gap: 14,
    backgroundColor: colors.card,
    borderRadius: 20,
    borderWidth: 1,
    borderColor: colors.border,
    padding: 16,
  },
  sugRank: {
    width: 32,
    height: 32,
    borderRadius: 16,
    backgroundColor: colors.accent,
    alignItems: "center",
    justifyContent: "center",
  },
  sugRankTxt: { color: "#fff", fontWeight: "800" },
  sugTitle: {
    fontSize: 15,
    fontWeight: "700",
    color: colors.textPrimary,
  },
  sugDetail: {
    color: colors.textSecondary,
    marginTop: 4,
    lineHeight: 19,
  },
  sugTag: {
    alignSelf: "flex-start",
    backgroundColor: colors.bgSecondary,
    paddingHorizontal: 10,
    paddingVertical: 4,
    borderRadius: 999,
    marginTop: 8,
  },
  sugTagTxt: {
    fontSize: 11,
    fontWeight: "700",
    color: colors.textSecondary,
    textTransform: "uppercase",
    letterSpacing: 1,
  },
  empty: {
    backgroundColor: colors.card,
    borderRadius: 28,
    borderWidth: 1,
    borderColor: colors.border,
    padding: 32,
    alignItems: "center",
    marginTop: 12,
  },
  emptyTitle: {
    fontSize: 18,
    fontWeight: "800",
    color: colors.textPrimary,
    marginTop: 16,
  },
  emptySub: {
    color: colors.textSecondary,
    textAlign: "center",
    marginTop: 8,
    marginBottom: 20,
  },
  cta: {
    backgroundColor: colors.brand,
    paddingHorizontal: 28,
    paddingVertical: 14,
    borderRadius: 999,
  },
  ctaTxt: { color: "#fff", fontWeight: "800" },
  errorBox: {
    flexDirection: "row",
    alignItems: "center",
    gap: 12,
    backgroundColor: `${colors.error}10`,
    borderWidth: 1,
    borderColor: `${colors.error}40`,
    borderRadius: 16,
    padding: 14,
    marginTop: 12,
  },
  errorTxt: { flex: 1, color: colors.error, fontSize: 13 },
  retryBtn: {
    backgroundColor: colors.error,
    paddingHorizontal: 14,
    paddingVertical: 8,
    borderRadius: 999,
  },
  retryTxt: { color: "#fff", fontWeight: "700" },
});
