import { reactive, shallowRef } from "vue";
import { getSystemSettings, updateSystemSettings } from "@/api/admin";
import { useToast } from "@/composables/useToast";

export function useAdminSystemSettings() {
  const { error } = useToast();
  const systemSettings = reactive({
    allowRegistration: true,
    delayedThresholdDays: 2,
  });
  const systemSettingsMsg = shallowRef("");

  async function fetchSystemSettings() {
    const res = await getSystemSettings();
    systemSettings.allowRegistration = res.data.allowRegistration !== false;
    systemSettings.delayedThresholdDays = res.data.delayedThresholdDays || 2;
  }

  function updateSystemSetting({ key, value }) {
    systemSettings[key] = value;
  }

  async function handleSaveSystemSettings() {
    systemSettingsMsg.value = "";
    await updateSystemSettings({
      allowRegistration: systemSettings.allowRegistration,
      delayedThresholdDays: Number(systemSettings.delayedThresholdDays),
    });
    systemSettingsMsg.value = "设置已保存";
    setTimeout(() => {
      systemSettingsMsg.value = "";
    }, 2000);
  }

  function stepThreshold(delta) {
    const next = (systemSettings.delayedThresholdDays || 2) + delta;
    if (next >= 1 && next <= 30) {
      systemSettings.delayedThresholdDays = next;
      handleSaveSystemSettings();
    }
  }

  return {
    systemSettings,
    systemSettingsMsg,
    fetchSystemSettings,
    updateSystemSetting,
    handleSaveSystemSettings,
    stepThreshold,
  };
}
