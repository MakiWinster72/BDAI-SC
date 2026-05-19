import { computed, watch } from "vue";
import { applyDigitsInput } from "@/utils/profileFormInput";

const TW_ID_TYPES = ["台湾居民来往大陆通行证", "台湾居民居住证"];
const HKMO_ID_TYPES = ["港澳居民来往内地通行证", "港澳居民居住证"];

function detectHkMoFromIdNo(idNo) {
  if (!idNo) {
    return { isHk: false, isMo: false };
  }
  const cleaned = idNo.toUpperCase().replace(/[^0-9A-Z]/g, "");
  if (/^H\d{8}$/.test(cleaned)) {
    return { isHk: true, isMo: false };
  }
  if (/^M\d{8}$/.test(cleaned)) {
    return { isHk: false, isMo: true };
  }
  if (/^81\d{16}$/.test(cleaned)) {
    return { isHk: true, isMo: false };
  }
  if (/^82\d{16}$/.test(cleaned)) {
    return { isHk: false, isMo: true };
  }
  return { isHk: false, isMo: false };
}

export function useProfileIdNo(info, isEditing) {
  const idNoMaxLength = computed(() => {
    switch (info.idType) {
      case "居民身份证":
        return 18;
      case "台湾居民来往大陆通行证":
        return 8;
      case "港澳居民来往内地通行证":
        return 9;
      case "普通护照":
        return 9;
      case "台湾居民居住证":
        return 18;
      case "港澳居民居住证":
        return 18;
      case "外国人永久居留身份证":
        return 18;
      case "外国护照":
        return 20;
      default:
        return 32;
    }
  });

  const idNoHint = computed(() => {
    switch (info.idType) {
      case "居民身份证":
        return "18位（17位数字 + 1位数字/X）";
      case "台湾居民来往大陆通行证":
        return "8位纯数字（台胞证）";
      case "港澳居民来往内地通行证":
        return "9位（H/M + 8位数字，回乡证）";
      case "普通护照":
        return "9位（E + 8位数字）";
      case "台湾居民居住证":
        return "18位纯数字（83开头）";
      case "港澳居民居住证":
        return "18位纯数字（81/82开头）";
      case "外国人永久居留身份证":
        return "18位纯数字（新版五星卡）";
      case "外国护照":
        return "6-9位字母数字组合";
      default:
        return "";
    }
  });

  function handleDigitsInput(field, maxLength, event) {
    applyDigitsInput(info, field, maxLength, event);
  }

  function handlePhoneDigitsInput(event) {
    applyDigitsInput(info, "phone", 11, event);
  }

  function handleIdNoInput(event) {
    const raw = (event.target.value || "").toUpperCase();
    const maxLen = idNoMaxLength.value;
    if (info.idType === "居民身份证") {
      const cleaned = raw.replace(/[^0-9A-Z]/g, "").toUpperCase();
      const first17 = cleaned.slice(0, 17).replace(/[^0-9]/g, "");
      const char18 = cleaned.charAt(17);
      if (char18 === "X" || /^\d$/.test(char18)) {
        info.idNo = first17 + char18;
      } else {
        info.idNo = first17;
      }
      return;
    }
    if (info.idType === "台湾居民来往大陆通行证") {
      info.idNo = raw.replace(/\D/g, "").slice(0, 8);
      return;
    }
    if (info.idType === "港澳居民来往内地通行证") {
      const cleaned = raw.replace(/[^0-9A-Z]/g, "").toUpperCase();
      const letter = cleaned.slice(0, 1).replace(/[^HM]/g, "");
      const digits = cleaned.replace(/[A-Z]/g, "").slice(0, 8);
      info.idNo = `${letter}${digits}`.slice(0, maxLen);
      return;
    }
    if (info.idType === "普通护照") {
      const cleaned = raw.replace(/[^0-9A-Z]/g, "").toUpperCase();
      const letter = cleaned.slice(0, 1).replace(/[^E]/g, "");
      const digits = cleaned.replace(/[A-Z]/g, "").slice(0, 8);
      info.idNo = `${letter}${digits}`.slice(0, maxLen);
      return;
    }
    if (info.idType === "台湾居民居住证") {
      info.idNo = raw.replace(/\D/g, "").slice(0, 18);
      return;
    }
    if (info.idType === "港澳居民居住证") {
      info.idNo = raw.replace(/\D/g, "").slice(0, 18);
      return;
    }
    if (info.idType === "外国人永久居留身份证") {
      info.idNo = raw.replace(/\D/g, "").slice(0, 18);
      return;
    }
    if (info.idType === "外国护照") {
      info.idNo = raw
        .replace(/[^0-9A-Z]/g, "")
        .toUpperCase()
        .slice(0, maxLen);
      return;
    }
    info.idNo = raw
      .replace(/[^0-9A-Z]/g, "")
      .toUpperCase()
      .slice(0, maxLen);
  }

  watch(
    () => info.idType,
    (nextType) => {
      if (isEditing.value) {
        info.idNo = "";
      }
      if (TW_ID_TYPES.includes(nextType)) {
        info.isHk = false;
        info.isMo = false;
        info.isTw = true;
      } else if (HKMO_ID_TYPES.includes(nextType)) {
        info.isTw = false;
        const detected = detectHkMoFromIdNo(info.idNo);
        info.isHk = detected.isHk;
        info.isMo = detected.isMo;
      } else {
        info.isHk = false;
        info.isMo = false;
        info.isTw = false;
      }
    },
  );

  watch(
    () => info.idNo,
    (nextIdNo) => {
      if (HKMO_ID_TYPES.includes(info.idType)) {
        const detected = detectHkMoFromIdNo(nextIdNo);
        info.isHk = detected.isHk;
        info.isMo = detected.isMo;
      }
    },
  );

  return {
    idNoMaxLength,
    idNoHint,
    handleDigitsInput,
    handlePhoneDigitsInput,
    handleIdNoInput,
  };
}
