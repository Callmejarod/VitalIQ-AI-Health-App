import React from "react";
import { View, Text, StyleSheet } from "react-native";
import Svg, {
  Path,
  Circle,
  Line,
  Text as SvgText,
} from "react-native-svg";
import { colors } from "../theme";

type Props = {
  data: { x: string; y: number }[];
  width?: number;
  height?: number;
  unit?: string;
};

export function LineChart({ data, width = 320, height = 180, unit }: Props) {
  if (!data || data.length === 0) {
    return (
      <View
        style={[styles.empty, { width, height }]}
        testID="line-chart-empty"
      >
        <Text style={styles.emptyText}>No data yet</Text>
      </View>
    );
  }

  const padX = 28;
  const padY = 24;
  const w = width - padX * 2;
  const h = height - padY * 2;
  const values = data.map((d) => d.y);
  const min = Math.min(...values);
  const max = Math.max(...values);
  const range = max - min || 1;

  const points = data.map((d, i) => {
    const x = padX + (data.length === 1 ? w / 2 : (i * w) / (data.length - 1));
    const y = padY + h - ((d.y - min) / range) * h;
    return { x, y, raw: d.y };
  });

  const pathD = points
    .map((p, i) => `${i === 0 ? "M" : "L"} ${p.x} ${p.y}`)
    .join(" ");

  return (
    <View testID="line-chart">
      <Svg width={width} height={height}>
        {/* axis baseline */}
        <Line
          x1={padX}
          y1={padY + h}
          x2={padX + w}
          y2={padY + h}
          stroke={colors.border}
          strokeWidth={1}
        />
        <Path
          d={pathD}
          stroke={colors.brand}
          strokeWidth={2.5}
          fill="none"
          strokeLinecap="round"
          strokeLinejoin="round"
        />
        {points.map((p, i) => (
          <Circle key={i} cx={p.x} cy={p.y} r={3.5} fill={colors.brand} />
        ))}
        <SvgText
          x={padX}
          y={padY - 6}
          fill={colors.textMuted}
          fontSize="10"
        >
          {`${max.toFixed(0)}${unit || ""}`}
        </SvgText>
        <SvgText
          x={padX}
          y={padY + h + 14}
          fill={colors.textMuted}
          fontSize="10"
        >
          {`${min.toFixed(0)}${unit || ""}`}
        </SvgText>
      </Svg>
    </View>
  );
}

const styles = StyleSheet.create({
  empty: {
    alignItems: "center",
    justifyContent: "center",
    backgroundColor: colors.bgSecondary,
    borderRadius: 16,
  },
  emptyText: { color: colors.textMuted, fontSize: 13 },
});
