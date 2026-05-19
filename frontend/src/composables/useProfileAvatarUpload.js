import { ref } from "vue";

export function useProfileAvatarUpload({ canUpload, upload }) {
  const avatarInput = ref(null);

  function triggerAvatarUpload() {
    if (canUpload && !canUpload()) {
      return;
    }
    avatarInput.value?.click();
  }

  async function handleAvatarChange(event) {
    const [file] = Array.from(event.target.files || []);
    event.target.value = "";
    if (!file) {
      return "";
    }

    const { data } = await upload(file).catch((err) => {
      console.error(err);
      return { data: null };
    });
    if (data?.mediaType !== "IMAGE") {
      return "";
    }
    return data.url || "";
  }

  return {
    avatarInput,
    triggerAvatarUpload,
    handleAvatarChange,
  };
}
