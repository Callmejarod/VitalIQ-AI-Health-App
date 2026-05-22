import React, { useCallback, useEffect, useState } from "react";
import {
  View,
  Text,
  StyleSheet,
  ScrollView,
  TextInput,
  TouchableOpacity,
  Alert,
  KeyboardAvoidingView,
  Platform,
} from "react-native";
import { SafeAreaView } from "react-native-safe-area-context";
import { Ionicons } from "@expo/vector-icons";
import * as DocumentPicker from "expo-document-picker";
import { useFocusEffect } from "expo-router";

import { colors } from "@/src/theme";
import { api } from "@/src/api";

const GOALS = [
  { key: "lose", label: "Lose weight" },
  { key: "maintain", label: "Maintain" },
  { key: "gain", label: "Build muscle" },
  { key: "endurance", label: "Endurance" },
];

export default function ProfileScreen() {
  const [profile, setProfile] = useState<any>(null);
  const [form, setForm] = useState<any>({});
  const [saving, setSaving] = useState(false);
  const [importing, setImporting] = useState(false);
  const [importResult, setImportResult] = useState<string | null>(null);

  const load = useCallback(async () => {
    try {
      const p = await api.getProfile();
      setProfile(p);
      setForm({
        name: p.name || "",
        age: p.age ? String(p.age) : "",
        height_cm: p.height_cm ? String(p.height_cm) : "",
        weight_kg: p.weight_kg ? String(p.weight_kg) : "",
        fitness_goal: p.fitness_goal || "maintain",
        daily_step_goal: p.daily_step_goal ? String(p.daily_step_goal) : "8000",
        daily_water_goal_ml: p.daily_water_goal_ml
          ? String(p.daily_water_goal_ml)
          : "2000",
      });
    } catch (e) {
      console.log("[Profile] load error", e);
    }
  }, []);

  useEffect(() => {
    console.log("[Lifecycle] Profile mounted");
    load();
  }, [load]);

  useFocusEffect(useCallback(() => { load(); }, [load]));

  const save = async () => {
    setSaving(true);
    try {
      const payload: any = {
        name: form.name || "Guest",
        fitness_goal: form.fitness_goal,
      };
      if (form.age) payload.age = parseInt(form.age, 10);
      if (form.height_cm) payload.height_cm = parseFloat(form.height_cm);
      if (form.weight_kg) payload.weight_kg = parseFloat(form.weight_kg);
      if (form.daily_step_goal)
        payload.daily_step_goal = parseInt(form.daily_step_goal, 10);
      if (form.daily_water_goal_ml)
        payload.daily_water_goal_ml = parseInt(form.daily_water_goal_ml, 10);
      await api.updateProfile(payload);
      Alert.alert("Saved", "Profile updated");
      await load();
    } catch (e) {
      Alert.alert("Save failed", String(e));
    } finally {
      setSaving(false);
    }
  };

  const importRecord = async () => {
    try {
      const res = await DocumentPicker.getDocumentAsync({
        type: ["application/json", "text/csv", "text/comma-separated-values"],
        copyToCacheDirectory: true,
      });
      if (res.canceled) return;
      const asset = res.assets[0];
      setImporting(true);
      setImportResult(null);
      const result = await api.importFile(
        asset.uri,
        asset.name || "import",
      );
      setImportResult(
        `Imported ${result.imported}/${result.total_rows} records`,
      );
    } catch (e) {
      Alert.alert("Import failed", String(e));
    } finally {
      setImporting(false);
    }
  };

  return (
    <SafeAreaView style={styles.screen} edges={["top"]} testID="profile-screen">
      <KeyboardAvoidingView
        style={{ flex: 1 }}
        behavior={Platform.OS === "ios" ? "padding" : undefined}
      >
        <ScrollView contentContainerStyle={styles.scroll}>
          <Text style={styles.title}>Profile</Text>
          <Text style={styles.subtitle}>
            Personalize VitalIQ with your details and goals.
          </Text>

          <Field
            label="Name"
            value={form.name}
            onChange={(v) => setForm({ ...form, name: v })}
            testID="profile-name"
          />
          <View style={{ flexDirection: "row", gap: 12 }}>
            <Field
              label="Age"
              value={form.age}
              onChange={(v) => setForm({ ...form, age: v })}
              numeric
              testID="profile-age"
            />
            <Field
              label="Height (cm)"
              value={form.height_cm}
              onChange={(v) => setForm({ ...form, height_cm: v })}
              numeric
              testID="profile-height"
            />
            <Field
              label="Weight (kg)"
              value={form.weight_kg}
              onChange={(v) => setForm({ ...form, weight_kg: v })}
              numeric
              testID="profile-weight"
            />
          </View>

          <Text style={styles.sectionLabel}>Fitness Goal</Text>
          <View style={styles.chipsRow}>
            {GOALS.map((g) => (
              <TouchableOpacity
                key={g.key}
                style={[
                  styles.chip,
                  form.fitness_goal === g.key && styles.chipActive,
                ]}
                onPress={() => setForm({ ...form, fitness_goal: g.key })}
                testID={`goal-${g.key}`}
              >
                <Text
                  style={[
                    styles.chipTxt,
                    form.fitness_goal === g.key && { color: "#fff" },
                  ]}
                >
                  {g.label}
                </Text>
              </TouchableOpacity>
            ))}
          </View>

          <Text style={styles.sectionLabel}>Daily Goals</Text>
          <View style={{ flexDirection: "row", gap: 12 }}>
            <Field
              label="Steps"
              value={form.daily_step_goal}
              onChange={(v) => setForm({ ...form, daily_step_goal: v })}
              numeric
              testID="profile-step-goal"
            />
            <Field
              label="Water (ml)"
              value={form.daily_water_goal_ml}
              onChange={(v) =>
                setForm({ ...form, daily_water_goal_ml: v })
              }
              numeric
              testID="profile-water-goal"
            />
          </View>

          <TouchableOpacity
            style={[styles.saveBtn, saving && { opacity: 0.6 }]}
            onPress={save}
            disabled={saving}
            testID="profile-save-btn"
          >
            <Text style={styles.saveTxt}>
              {saving ? "Saving…" : "Save Profile"}
            </Text>
          </TouchableOpacity>

          <Text style={styles.sectionLabel}>Import Health Records</Text>
          <View style={styles.importCard}>
            <View style={styles.importIcon}>
              <Ionicons
                name="cloud-upload"
                size={22}
                color={colors.brand}
              />
            </View>
            <Text style={styles.importTitle}>JSON or CSV file</Text>
            <Text style={styles.importHint}>
              Each row needs a "type" field: weight, bp, hr, body_fat, meal,
              water, sleep, or medication.
            </Text>
            <TouchableOpacity
              style={styles.importBtn}
              onPress={importRecord}
              disabled={importing}
              testID="import-btn"
            >
              <Ionicons
                name="document-attach"
                size={18}
                color="#fff"
                style={{ marginRight: 6 }}
              />
              <Text style={styles.importBtnTxt}>
                {importing ? "Importing…" : "Choose File"}
              </Text>
            </TouchableOpacity>
            {importResult && (
              <Text style={styles.importResult} testID="import-result">
                {importResult}
              </Text>
            )}
          </View>

          <View style={{ height: 40 }} />
        </ScrollView>
      </KeyboardAvoidingView>
    </SafeAreaView>
  );
}

function Field({
  label,
  value,
  onChange,
  numeric,
  testID,
}: {
  label: string;
  value?: string;
  onChange: (v: string) => void;
  numeric?: boolean;
  testID?: string;
}) {
  return (
    <View style={{ flex: 1, marginBottom: 14 }}>
      <Text style={styles.fieldLabel}>{label}</Text>
      <TextInput
        value={value || ""}
        onChangeText={onChange}
        style={styles.input}
        keyboardType={numeric ? "decimal-pad" : "default"}
        placeholderTextColor={colors.textMuted}
        testID={testID}
      />
    </View>
  );
}

const styles = StyleSheet.create({
  screen: { flex: 1, backgroundColor: colors.bg },
  scroll: { padding: 24, paddingBottom: 40 },
  title: {
    fontSize: 28,
    fontWeight: "800",
    color: colors.textPrimary,
    letterSpacing: -0.5,
  },
  subtitle: {
    color: colors.textSecondary,
    marginTop: 4,
    marginBottom: 20,
  },
  fieldLabel: {
    fontSize: 11,
    fontWeight: "700",
    color: colors.textMuted,
    letterSpacing: 1.2,
    textTransform: "uppercase",
    marginBottom: 6,
  },
  input: {
    backgroundColor: colors.card,
    borderRadius: 14,
    borderWidth: 1,
    borderColor: colors.border,
    paddingHorizontal: 14,
    paddingVertical: 12,
    fontSize: 16,
    color: colors.textPrimary,
  },
  sectionLabel: {
    fontSize: 12,
    fontWeight: "800",
    color: colors.textPrimary,
    letterSpacing: 1.4,
    textTransform: "uppercase",
    marginTop: 12,
    marginBottom: 10,
  },
  chipsRow: { flexDirection: "row", flexWrap: "wrap", gap: 8 },
  chip: {
    paddingHorizontal: 14,
    paddingVertical: 10,
    borderRadius: 999,
    backgroundColor: colors.card,
    borderWidth: 1,
    borderColor: colors.border,
  },
  chipActive: {
    backgroundColor: colors.brand,
    borderColor: colors.brand,
  },
  chipTxt: { color: colors.textPrimary, fontWeight: "600" },
  saveBtn: {
    backgroundColor: colors.brand,
    borderRadius: 999,
    paddingVertical: 16,
    alignItems: "center",
    marginTop: 18,
  },
  saveTxt: { color: "#fff", fontWeight: "800", fontSize: 15 },
  importCard: {
    backgroundColor: colors.card,
    borderRadius: 24,
    borderWidth: 1,
    borderColor: colors.border,
    padding: 20,
    alignItems: "flex-start",
  },
  importIcon: {
    width: 44,
    height: 44,
    borderRadius: 14,
    backgroundColor: `${colors.brand}15`,
    alignItems: "center",
    justifyContent: "center",
    marginBottom: 12,
  },
  importTitle: {
    fontSize: 16,
    fontWeight: "800",
    color: colors.textPrimary,
  },
  importHint: {
    fontSize: 13,
    color: colors.textSecondary,
    marginTop: 6,
    marginBottom: 14,
    lineHeight: 18,
  },
  importBtn: {
    flexDirection: "row",
    alignItems: "center",
    backgroundColor: colors.brand,
    paddingHorizontal: 18,
    paddingVertical: 12,
    borderRadius: 999,
  },
  importBtnTxt: { color: "#fff", fontWeight: "700" },
  importResult: {
    marginTop: 12,
    color: colors.success,
    fontWeight: "600",
  },
});
