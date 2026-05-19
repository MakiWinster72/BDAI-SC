import { ref, watch } from "vue";
import { resolveMediaObjectUrl, resolveMediaUrl } from "@/utils/media";

export function useResolvedMediaUrl(source) {
  const resolvedUrl = ref("");
  let requestId = 0;

  watch(
    source,
    async (url) => {
      const currentRequest = ++requestId;
      if (!url) {
        resolvedUrl.value = "";
        return;
      }

      const nextUrl = await resolveMediaObjectUrl(url).catch(() =>
        resolveMediaUrl(url),
      );
      if (currentRequest === requestId) {
        resolvedUrl.value = nextUrl;
      }
    },
    { immediate: true },
  );

  return { resolvedUrl };
}
