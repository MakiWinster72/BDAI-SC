import { computed, watch } from "vue";

export function useProfileDormFields(info, isEditing) {
  const dormBuildingOptions = computed(() => {
    if (info.dormCampus === "佛山校区") {
      return [
        ...Array.from({ length: 21 }, (_, index) => {
          const label = `${index + 1}号楼`;
          return { label, value: label };
        }),
        { label: "有为9栋", value: "有为9栋" },
        { label: "有为21栋", value: "有为21栋" },
        {
          label: "教师公寓（请选择校外居住）",
          value: "教师公寓",
          disabled: true,
        },
      ];
    }
    if (info.dormCampus === "广州校区") {
      return [
        ...Array.from({ length: 16 }, (_, index) => {
          const label = `${index + 17}号楼`;
          return { label, value: label };
        }),
        { label: "凌云楼", value: "凌云楼" },
        { label: "揽月楼", value: "揽月楼" },
        { label: "丽枫酒店", value: "丽枫酒店" },
      ];
    }
    return [];
  });

  const dormBuildingDisabled = computed(
    () => !isEditing.value || info.offCampusLiving || !info.dormCampus,
  );
  const dormRoomDisabled = computed(
    () => dormBuildingDisabled.value || !info.dormBuilding,
  );

  watch(
    () => info.offCampusLiving,
    (next) => {
      if (next) {
        info.dormCampus = "";
        info.dormBuilding = "";
        info.dormRoom = "";
        info.dormFloor = "";
        info.dormRoomNo = "";
      } else {
        info.offCampusAddress = "";
        info.offCampusProvince = "";
        info.offCampusCity = "";
        info.offCampusCounty = "";
        info.offCampusDetail = "";
      }
    },
  );

  watch(
    () => info.dormCampus,
    () => {
      if (!info.dormCampus) {
        info.dormBuilding = "";
        return;
      }
      const exists = dormBuildingOptions.value.some(
        (item) => item.value === info.dormBuilding && !item.disabled,
      );
      if (!exists) {
        info.dormBuilding = "";
      }
    },
  );

  return {
    dormBuildingOptions,
    dormBuildingDisabled,
    dormRoomDisabled,
  };
}
