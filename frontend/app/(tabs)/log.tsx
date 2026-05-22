import React, { useState } from "react";
import {
  View,
  Text,
  StyleSheet,
  ScrollView,
  TextInput,
  TouchableOpacity,
  KeyboardAvoidingView,
  Platform,
  Alert,
} from "react-native";
import { SafeAreaView } from "react-native-safe-area-context";
import { Ionicons } from "@expo/vector-icons";
import { colors } from "@/src/theme";
import { api } from "@/src/api";

type Card =
  | "weight"
  | "bp"
  | "hr"
  | "med"
  | "meal"
  | "water"
  | "sleep";

const CARDS: { key: Card; title: string; icon: keyof typeof Ionicons.glyphMap; color: string }[] = [
  { key: "weight", title: "Weight", icon: "barbell", color: colors.brand },
  { key: "bp", title: "Blood Pressure", icon: "heart-half", color: colors.error },
  { key: "hr", title: "Resting Heart Rate", icon: "heart", color: colors.error },
  { key: "med", title: "Medication", icon: "medkit", color: colors.warning },
  { key: "meal", title: "Meal", icon: "restaurant", color: colors.success },
  { key: "water", title: "Water", icon: "water", color: colors.accent },
  { key: "sleep", title: "Sleep", icon: "bed", color: colors.success },
];

export default function LogScreen() {
  const [open, setOpen] = useState<Card | null>(null);
  const [form, setForm] = useState<Record<string, string>>({});
  const [saving, setSaving] = useState(false);
  const [lastSaved, setLastSaved] = useState<string | null>(null);

  const setField = (k: string, v: string) =>
    setForm((prev) => ({ ...prev, [k]: v }));

  const reset = () => {
    setForm({});
    setOpen(null);
  };

  const save = async () => {
    if (!open) return;
    setSaving(true);
    try {
      if (open === "weight") {
        await api.createHealthEntry({
          entry_type: "weight",
          value: { weight_kg: parseFloat(form.weight || "0") },
        });
      } else if (open === "bp") {
        await api.createHealthEntry({
          entry_type: "bp",
          value: {
            sys: parseFloat(form.sys || "0"),
            dia: parseFloat(form.dia || "0"),
          },
        });
      } else if (open === "hr") {
        await api.createHealthEntry({
          entry_type: "hr",
          value: { hr: parseFloat(form.hr || "0") },
        });
      } else if (open === "med") {
        await api.createMedication({
          name: form.name || "Unnamed",
          dosage: form.dosage || "",
          frequency: form.frequency || "daily",
        });
      } else if (open === "meal") {
        await api.createNutrition({
          kind: "meal",
          meal_name: form.name || "Meal",
          calories: form.calories ? parseInt(form.calories, 10) : undefined,
          protein_g: form.protein ? parseFloat(form.protein) : undefined,
          carbs_g: form.carbs ? parseFloat(form.carbs) : undefined,
          fats_g: form.fats ? parseFloat(form.fats) : undefined,
        });
      } else if (open === "water") {
        await api.createNutrition({
          kind: "water",
          water_ml: parseInt(form.ml || "0", 10),
        });
      } else if (open === "sleep") {
        await api.createSleep({
          hours: parseFloat(form.hours || "0"),
          quality: form.quality ? parseInt(form.quality, 10) : undefined,
        });
      }
      setLastSaved(CARDS.find((c) => c.key === open)?.title || "Entry");
      reset();
    } catch (e) {
      Alert.alert("Save failed", String(e));
    } finally {
      setSaving(false);
    }
  };

  const renderForm = () => {
    if (!open) return null;
    const card = CARDS.find((c) => c.key === open)!;
    return (
      <View style={styles.formCard} testID={`form-${open}`}>
        <View style={styles.formHeader}>
          <View style={[styles.formIcon, { backgroundColor: `${card.color}20` }]}>
            <Ionicons name={card.icon} size={20} color={card.color} />
          </View>
          <Text style={styles.formTitle}>{card.title}</Text>
          <TouchableOpacity onPress={reset} testID="form-close">
            <Ionicons name="close" size={22} color={colors.textMuted} />
          </TouchableOpacity>
        </View>

        {open === "weight" && (
          <NumberField
            label="Weight (kg)"
            value={form.weight}
            onChange={(v) => setField("weight", v)}
            testID="input-weight"
          />
        )}
        {open === "bp" && (
          <View style={{ flexDirection: "row", gap: 12 }}>
            <NumberField
              label="Systolic"
              value={form.sys}
              onChange={(v) => setField("sys", v)}
              testID="input-sys"
            />
            <NumberField
              label="Diastolic"
              value={form.dia}
              onChange={(v) => setField("dia", v)}
              testID="input-dia"
            />
          </View>
        )}
        {open === "hr" && (
          <NumberField
            label="Heart Rate (bpm)"
            value={form.hr}
            onChange={(v) => setField("hr", v)}
            testID="input-hr"
          />
        )}
        {open === "med" && (
          <>
            <TextField
              label="Name"
              value={form.name}
              onChange={(v) => setField("name", v)}
              testID="input-med-name"
            />
            <TextField
              label="Dosage (e.g. 10mg)"
              value={form.dosage}
              onChange={(v) => setField("dosage", v)}
              testID="input-med-dosage"
            />
            <TextField
              label="Frequency"
              value={form.frequency}
              onChange={(v) => setField("frequency", v)}
              testID="input-med-freq"
              placeholder="daily / twice_daily / weekly"
            />
          </>
        )}
        {open === "meal" && (
          <>
            <TextField
              label="Meal name"
              value={form.name}
              onChange={(v) => setField("name", v)}
              testID="input-meal-name"
            />
            <View style={{ flexDirection: "row", gap: 12 }}>
              <NumberField
                label="Calories"
                value={form.calories}
                onChange={(v) => setField("calories", v)}
                testID="input-cals"
              />
              <NumberField
                label="Protein g"
                value={form.protein}
                onChange={(v) => setField("protein", v)}
                testID="input-protein"
              />
            </View>
            <View style={{ flexDirection: "row", gap: 12 }}>
              <NumberField
                label="Carbs g"
                value={form.carbs}
                onChange={(v) => setField("carbs", v)}
                testID="input-carbs"
              />
              <NumberField
                label="Fats g"
                value={form.fats}
                onChange={(v) => setField("fats", v)}
                testID="input-fats"
              />
            </View>
          </>
        )}
        {open === "water" && (
          <NumberField
            label="Water (ml)"
            value={form.ml}
            onChange={(v) => setField("ml", v)}
            testID="input-water"
          />
        )}
        {open === "sleep" && (
          <View style={{ flexDirection: "row", gap: 12 }}>
            <NumberField
              label="Hours"
              value={form.hours}
              onChange={(v) => setField("hours", v)}
              testID="input-sleep-hours"
            />
            <NumberField
              label="Quality 1-5"
              value={form.quality}
              onChange={(v) => setField("quality", v)}
              testID="input-sleep-quality"
            />
          </View>
        )}

        <TouchableOpacity
          style={[styles.saveBtn, saving && { opacity: 0.6 }]}
          onPress={save}
          disabled={saving}
          testID="form-save-btn"
        >
          <Text style={styles.saveBtnTxt}>
            {saving ? "Saving…" : "Save Entry"}
          </Text>
        </TouchableOpacity>
      </View>
    );
  };

  return (
    <SafeAreaView style={styles.screen} edges={["top"]} testID="log-screen">
      <KeyboardAvoidingView
        style={{ flex: 1 }}
        behavior={Platform.OS === "ios" ? "padding" : undefined}
      >
        <ScrollView contentContainerStyle={styles.scroll}>
          <Text style={styles.title}>Health Log</Text>
          <Text style={styles.subtitle}>
            Tap an entry type to record a new measurement.
          </Text>

          {lastSaved && (
            <View style={styles.banner} testID="saved-banner">
              <Ionicons name="checkmark-circle" size={18} color={colors.success} />
              <Text style={styles.bannerTxt}>{lastSaved} saved</Text>
            </View>
          )}

          {renderForm()}

          <View style={styles.grid}>
            {CARDS.map((c) => (
              <TouchableOpacity
                key={c.key}
                style={[
                  styles.cardBtn,
                  open === c.key && {
                    borderColor: c.color,
                    backgroundColor: `${c.color}10`,
                  },
                ]}
                onPress={() => setOpen(c.key)}
                testID={`log-card-${c.key}`}
              >
                <View
                  style={[
                    styles.cardIcon,
                    { backgroundColor: `${c.color}18` },
                  ]}
                >
                  <Ionicons name={c.icon} size={20} color={c.color} />
                </View>
                <Text style={styles.cardTitle}>{c.title}</Text>
              </TouchableOpacity>
            ))}
          </View>
          <View style={{ height: 40 }} />
        </ScrollView>
      </KeyboardAvoidingView>
    </SafeAreaView>
  );
}

function NumberField({
  label,
  value,
  onChange,
  testID,
}: {
  label: string;
  value?: string;
  onChange: (v: string) => void;
  testID?: string;
}) {
  return (
    <View style={{ flex: 1, marginBottom: 12 }}>
      <Text style={styles.fieldLabel}>{label}</Text>
      <TextInput
        style={styles.input}
        value={value || ""}
        onChangeText={onChange}
        keyboardType="decimal-pad"
        placeholder="0"
        placeholderTextColor={colors.textMuted}
        testID={testID}
      />
    </View>
  );
}

function TextField({
  label,
  value,
  onChange,
  testID,
  placeholder,
}: {
  label: string;
  value?: string;
  onChange: (v: string) => void;
  testID?: string;
  placeholder?: string;
}) {
  return (
    <View style={{ marginBottom: 12 }}>
      <Text style={styles.fieldLabel}>{label}</Text>
      <TextInput
        style={styles.input}
        value={value || ""}
        onChangeText={onChange}
        placeholder={placeholder}
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
  subtitle: { color: colors.textSecondary, marginTop: 4, marginBottom: 20 },
  banner: {
    flexDirection: "row",
    alignItems: "center",
    backgroundColor: `${colors.success}15`,
    padding: 12,
    borderRadius: 12,
    gap: 8,
    marginBottom: 16,
  },
  bannerTxt: { color: colors.success, fontWeight: "600" },
  grid: {
    flexDirection: "row",
    flexWrap: "wrap",
    gap: 12,
    marginTop: 12,
  },
  cardBtn: {
    width: "48%",
    backgroundColor: colors.card,
    borderRadius: 20,
    borderWidth: 1,
    borderColor: colors.border,
    padding: 16,
    alignItems: "flex-start",
  },
  cardIcon: {
    width: 40,
    height: 40,
    borderRadius: 12,
    alignItems: "center",
    justifyContent: "center",
    marginBottom: 10,
  },
  cardTitle: {
    fontSize: 14,
    fontWeight: "700",
    color: colors.textPrimary,
  },
  formCard: {
    backgroundColor: colors.card,
    borderRadius: 24,
    borderWidth: 1,
    borderColor: colors.border,
    padding: 20,
    marginBottom: 12,
  },
  formHeader: {
    flexDirection: "row",
    alignItems: "center",
    marginBottom: 16,
    gap: 12,
  },
  formIcon: {
    width: 40,
    height: 40,
    borderRadius: 12,
    alignItems: "center",
    justifyContent: "center",
  },
  formTitle: {
    flex: 1,
    fontSize: 18,
    fontWeight: "800",
    color: colors.textPrimary,
  },
  fieldLabel: {
    fontSize: 11,
    fontWeight: "700",
    letterSpacing: 1.2,
    color: colors.textMuted,
    textTransform: "uppercase",
    marginBottom: 6,
  },
  input: {
    backgroundColor: colors.bg,
    borderRadius: 14,
    borderWidth: 1,
    borderColor: colors.border,
    paddingHorizontal: 14,
    paddingVertical: 12,
    fontSize: 16,
    color: colors.textPrimary,
  },
  saveBtn: {
    backgroundColor: colors.brand,
    borderRadius: 999,
    paddingVertical: 14,
    alignItems: "center",
    marginTop: 8,
  },
  saveBtnTxt: { color: "#fff", fontWeight: "800", fontSize: 15 },
});
