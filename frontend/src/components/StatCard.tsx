import React from "react";
import { View, Text, StyleSheet, ViewStyle } from "react-native";
import { Ionicons } from "@expo/vector-icons";
import { colors } from "../theme";

type Props = {
  icon: keyof typeof Ionicons.glyphMap;
  iconColor?: string;
  label: string;
  value: string;
  unit?: string;
  caption?: string;
  style?: ViewStyle;
  testID?: string;
};

export function StatCard({
  icon,
  iconColor = colors.brand,
  label,
  value,
  unit,
  caption,
  style,
  testID,
}: Props) {
  return (
    <View style={[styles.card, style]} testID={testID}>
      <View style={[styles.iconWrap, { backgroundColor: `${iconColor}15` }]}>
        <Ionicons name={icon} size={18} color={iconColor} />
      </View>
      <Text style={styles.label}>{label}</Text>
      <View style={styles.row}>
        <Text style={styles.value}>{value}</Text>
        {unit ? <Text style={styles.unit}>{unit}</Text> : null}
      </View>
      {caption ? <Text style={styles.caption}>{caption}</Text> : null}
    </View>
  );
}

const styles = StyleSheet.create({
  card: {
    flex: 1,
    backgroundColor: colors.card,
    borderRadius: 24,
    padding: 18,
    borderWidth: 1,
    borderColor: colors.border,
    minHeight: 130,
  },
  iconWrap: {
    width: 36,
    height: 36,
    borderRadius: 12,
    alignItems: "center",
    justifyContent: "center",
    marginBottom: 12,
  },
  label: {
    fontSize: 11,
    textTransform: "uppercase",
    letterSpacing: 1.2,
    color: colors.textMuted,
    fontWeight: "600",
  },
  row: { flexDirection: "row", alignItems: "baseline", marginTop: 6 },
  value: {
    fontSize: 26,
    fontWeight: "800",
    color: colors.textPrimary,
    letterSpacing: -1,
  },
  unit: {
    fontSize: 13,
    color: colors.textSecondary,
    marginLeft: 4,
    fontWeight: "600",
  },
  caption: {
    fontSize: 12,
    color: colors.textSecondary,
    marginTop: 4,
  },
});
