import React, { ReactNode } from "react";
import { View, Text, StyleSheet, ViewStyle } from "react-native";
import { colors } from "../theme";

export function Section({
  title,
  action,
  children,
  style,
}: {
  title: string;
  action?: ReactNode;
  children: ReactNode;
  style?: ViewStyle;
}) {
  return (
    <View style={[styles.wrap, style]}>
      <View style={styles.header}>
        <Text style={styles.title}>{title}</Text>
        {action}
      </View>
      {children}
    </View>
  );
}

const styles = StyleSheet.create({
  wrap: { marginTop: 28 },
  header: {
    flexDirection: "row",
    alignItems: "center",
    justifyContent: "space-between",
    marginBottom: 12,
  },
  title: {
    fontSize: 13,
    fontWeight: "700",
    color: colors.textPrimary,
    textTransform: "uppercase",
    letterSpacing: 1.5,
  },
});
