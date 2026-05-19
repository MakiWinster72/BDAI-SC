export function applyDigitsInput(info, field, maxLength, event) {
  const raw = event.target.value || "";
  info[field] = raw.replace(/\D/g, "").slice(0, maxLength);
}
