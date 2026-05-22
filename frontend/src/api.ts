const BASE_URL = process.env.EXPO_PUBLIC_BACKEND_URL;

async function request<T>(
  path: string,
  options: RequestInit = {},
): Promise<T> {
  const url = `${BASE_URL}/api${path}`;
  const headers: Record<string, string> = {
    "Content-Type": "application/json",
    ...((options.headers as Record<string, string>) || {}),
  };
  const res = await fetch(url, { ...options, headers });
  if (!res.ok) {
    const text = await res.text().catch(() => "");
    throw new Error(`API ${res.status} ${path}: ${text}`);
  }
  return res.json() as Promise<T>;
}

export const api = {
  // profile
  getProfile: () => request<any>("/profile"),
  updateProfile: (data: any) =>
    request<any>("/profile", { method: "PUT", body: JSON.stringify(data) }),

  // workouts
  createWorkout: (data: any) =>
    request<any>("/workouts", { method: "POST", body: JSON.stringify(data) }),
  listWorkouts: () => request<any[]>("/workouts"),

  // health entries
  createHealthEntry: (data: any) =>
    request<any>("/health-entries", {
      method: "POST",
      body: JSON.stringify(data),
    }),
  listHealthEntries: (entryType?: string) =>
    request<any[]>(
      `/health-entries${entryType ? `?entry_type=${entryType}` : ""}`,
    ),

  // medications
  createMedication: (data: any) =>
    request<any>("/medications", {
      method: "POST",
      body: JSON.stringify(data),
    }),
  listMedications: () => request<any[]>("/medications"),

  // nutrition
  createNutrition: (data: any) =>
    request<any>("/nutrition", {
      method: "POST",
      body: JSON.stringify(data),
    }),
  listNutrition: (kind?: string) =>
    request<any[]>(`/nutrition${kind ? `?kind=${kind}` : ""}`),

  // sleep
  createSleep: (data: any) =>
    request<any>("/sleep", { method: "POST", body: JSON.stringify(data) }),
  listSleep: () => request<any[]>("/sleep"),

  // insights
  generateInsights: () =>
    request<any>("/insights/generate", { method: "POST" }),
  latestInsight: () => request<any>("/insights/latest"),

  // dashboard
  dashboardSummary: () => request<any>("/dashboard/summary"),

  // import
  importFile: async (uri: string, name: string) => {
    const form = new FormData();
    // @ts-ignore — RN FormData
    form.append("file", { uri, name, type: "application/octet-stream" });
    const res = await fetch(`${BASE_URL}/api/import`, {
      method: "POST",
      body: form,
    });
    if (!res.ok) throw new Error(`Import failed: ${res.status}`);
    return res.json();
  },
};

export type Suggestion = {
  title: string;
  detail: string;
  priority: number;
  category: string;
};
