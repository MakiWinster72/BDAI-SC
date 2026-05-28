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

      let nextUrl = "";
      try {
        nextUrl = await resolveMediaObjectUrl(url);
      } catch {
        // 受保护媒体不能回退为裸 URL（<img> 无法带 Authorization，会 401）
        nextUrl = "";
      }
      if (currentRequest === requestId) {
        resolvedUrl.value = nextUrl;
      }
    },
    { immediate: true },
  );

  return { resolvedUrl };
}
