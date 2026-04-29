import { reactive, readonly } from "vue";
import { getSystemSettings } from "../api/admin";

const store = reactive({
  loaded: false,
  delayedThresholdDays: 2,
});

async function fetchSettings() {
  if (store.loaded) return;
  try {
    const res = await getSystemSettings();
    store.delayedThresholdDays = res.data.delayedThresholdDays || 2;
  } catch {
    // ignore
  } finally {
    store.loaded = true;
  }
}

export function useSystemSettings() {
  fetchSettings();
  return {
    delayedThresholdDays: readonly(store).delayedThresholdDays,
    refresh: fetchSettings,
  };
}