import { computed, watch } from "vue";
import { regionData } from "element-china-area-data";

export function useProfileRegionCascade(info, prefix) {
  const provinceKey = `${prefix}Province`;
  const cityKey = `${prefix}City`;
  const countyKey = `${prefix}County`;

  const provinceOptions = computed(() =>
    regionData.map((item) => ({ value: item.value, label: item.label })),
  );

  const cityOptions = computed(() => {
    const province = regionData.find((item) => item.value === info[provinceKey]);
    return province?.children || [];
  });

  const countyOptions = computed(() => {
    const province = regionData.find((item) => item.value === info[provinceKey]);
    const city = province?.children?.find((entry) => entry.value === info[cityKey]);
    return city?.children || [];
  });

  watch(
    () => info[provinceKey],
    () => {
      if (!info[provinceKey]) {
        info[cityKey] = "";
        info[countyKey] = "";
        return;
      }
      if (!cityOptions.value.some((item) => item.value === info[cityKey])) {
        info[cityKey] = "";
      }
      if (!countyOptions.value.some((item) => item.value === info[countyKey])) {
        info[countyKey] = "";
      }
    },
  );

  watch(
    () => info[cityKey],
    () => {
      if (!info[cityKey]) {
        info[countyKey] = "";
        return;
      }
      if (!countyOptions.value.some((item) => item.value === info[countyKey])) {
        info[countyKey] = "";
      }
    },
  );

  return {
    provinceOptions,
    cityOptions,
    countyOptions,
  };
}
