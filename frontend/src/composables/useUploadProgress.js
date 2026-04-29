import { useToast } from "./useToast";

const PHASE1_TARGET = 92;
const PHASE2_TARGET = 99;
const PHASE1_DURATION = 5000;
const PHASE2_MIN_DURATION = 5000;

export function useUploadProgress() {
  const { progress: showProgress, updateToast, removeToast, success, error: toastError } = useToast();

  function simulateProgress(onDone) {
    const start = Date.now();
    let phase = 1;

    function tick() {
      const elapsed = Date.now() - start;
      if (phase === 1) {
        const pct = Math.min((elapsed / PHASE1_DURATION) * PHASE1_TARGET, PHASE1_TARGET);
        onDone({ done: false, pct: Math.round(pct) });
        if (elapsed < PHASE1_DURATION) {
          requestAnimationFrame(tick);
        } else {
          phase = 2;
          requestAnimationFrame(tick);
        }
      } else {
        const pct = PHASE1_TARGET + Math.min((elapsed - PHASE1_DURATION) / PHASE2_MIN_DURATION, 1) * (PHASE2_TARGET - PHASE1_TARGET);
        onDone({ done: false, pct: Math.round(pct) });
        requestAnimationFrame(tick);
      }
    }

    requestAnimationFrame(tick);
  }

  async function uploadWithProgress(file, uploadFn, options = {}) {
    const label = options.label || file.name || "文件";
    const toastId = showProgress(`正在上传 ${label}...`);

    let stop = false;
    let currentPct = 0;

    simulateProgress(({ pct }) => {
      if (stop) return;
      currentPct = pct;
      updateToast(toastId, { progress: pct });
    });

    try {
      const result = await uploadFn(file, options);
      stop = true;
      updateToast(toastId, { progress: 100 });
      setTimeout(() => {
        removeToast(toastId);
        success(`${label} 上传完成`);
      }, 400);
      return result;
    } catch (e) {
      stop = true;
      removeToast(toastId);
      toastError(e?.response?.data?.message || `${label} 上传失败`);
      throw e;
    }
  }

  return { uploadWithProgress };
}
