import React from "react";
import { View, Text, StyleSheet } from "react-native";
import Svg, { Circle, Defs, LinearGradient, Stop } from "react-native-svg";
import { colors } from "../theme";

type Props = {
  score: number; // 0-100
  size?: number;
  strokeWidth?: number;
  label?: string;
};

export function HealthRing({
  score,
  size = 220,
  strokeWidth = 16,
  label = "Health Score",
}: Props) {
  const radius = (size - strokeWidth) / 2;
  const circumference = 2 * Math.PI * radius;
  const clamped = Math.max(0, Math.min(100, score));
  const offset = circumference - (clamped / 100) * circumference;

  return (
    <View
      style={[styles.wrapper, { width: size, height: size }]}
      testID="health-ring"
    >
      <Svg width={size} height={size}>
        <Defs>
          <LinearGradient id="ringGrad" x1="0" y1="0" x2="1" y2="1">
            <Stop offset="0" stopColor={colors.brand} />
            <Stop offset="1" stopColor={colors.success} />
          </LinearGradient>
        </Defs>
        <Circle
          cx={size / 2}
          cy={size / 2}
          r={radius}
          stroke={colors.border}
          strokeWidth={strokeWidth}
          fill="none"
        />
        <Circle
          cx={size / 2}
          cy={size / 2}
          r={radius}
          stroke="url(#ringGrad)"
          strokeWidth={strokeWidth}
          fill="none"
          strokeDasharray={`${circumference} ${circumference}`}
          strokeDashoffset={offset}
          strokeLinecap="round"
          transform={`rotate(-90 ${size / 2} ${size / 2})`}
        />
      </Svg>
      <View style={styles.center} pointerEvents="none">
        <Text style={styles.score} testID="health-ring-score">
          {clamped}
        </Text>
        <Text style={styles.label}>{label}</Text>
      </View>
    </View>
  );
}

const styles = StyleSheet.create({
  wrapper: { alignItems: "center", justifyContent: "center" },
  center: { position: "absolute", alignItems: "center" },
  score: {
    fontSize: 56,
    fontWeight: "800",
    color: colors.brand,
    letterSpacing: -2,
  },
  label: {
    fontSize: 11,
    textTransform: "uppercase",
    letterSpacing: 1.5,
    color: colors.textMuted,
    marginTop: 4,
  },
});
