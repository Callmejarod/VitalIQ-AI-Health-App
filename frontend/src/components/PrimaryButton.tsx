import React from "react";
import {
  TouchableOpacity,
  Text,
  StyleSheet,
  ActivityIndicator,
  ViewStyle,
} from "react-native";
import { Ionicons } from "@expo/vector-icons";
import { colors } from "../theme";

type Props = {
  label: string;
  onPress: () => void;
  variant?: "primary" | "secondary" | "ghost";
  loading?: boolean;
  disabled?: boolean;
  icon?: keyof typeof Ionicons.glyphMap;
  style?: ViewStyle;
  testID?: string;
};

export function PrimaryButton({
  label,
  onPress,
  variant = "primary",
  loading,
  disabled,
  icon,
  style,
  testID,
}: Props) {
  const isPrimary = variant === "primary";
  const isGhost = variant === "ghost";
  return (
    <TouchableOpacity
      onPress={onPress}
      activeOpacity={0.85}
      disabled={disabled || loading}
      style={[
        styles.base,
        isPrimary && styles.primary,
        variant === "secondary" && styles.secondary,
        isGhost && styles.ghost,
        (disabled || loading) && { opacity: 0.6 },
        style,
      ]}
      testID={testID}
    >
      {loading ? (
        <ActivityIndicator color={isPrimary ? "#fff" : colors.brand} />
      ) : (
        <>
          {icon ? (
            <Ionicons
              name={icon}
              size={18}
              color={isPrimary ? "#fff" : colors.brand}
              style={{ marginRight: 8 }}
            />
          ) : null}
          <Text
            style={[
              styles.label,
              { color: isPrimary ? "#fff" : colors.brand },
            ]}
          >
            {label}
          </Text>
        </>
      )}
    </TouchableOpacity>
  );
}

const styles = StyleSheet.create({
  base: {
    flexDirection: "row",
    alignItems: "center",
    justifyContent: "center",
    borderRadius: 999,
    paddingVertical: 16,
    paddingHorizontal: 24,
  },
  primary: { backgroundColor: colors.brand },
  secondary: {
    backgroundColor: colors.bgSecondary,
    borderWidth: 1,
    borderColor: colors.border,
  },
  ghost: { backgroundColor: "transparent" },
  label: { fontSize: 16, fontWeight: "700", letterSpacing: 0.3 },
});
