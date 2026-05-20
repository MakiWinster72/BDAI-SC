import { watch } from "vue";
import { majorOptionsByCategory } from "@/constants/profileOptions";

export function useProfileCategoryWatch(info) {
  watch(
    () => info.studentCategory,
    (category) => {
      if (!majorOptionsByCategory[category]) {
        info.classMajor = "";
        return;
      }
      if (!majorOptionsByCategory[category].includes(info.classMajor)) {
        info.classMajor = "";
      }
      if (category === "研究生") {
        info.classNo = 1;
      }
    },
  );
}
