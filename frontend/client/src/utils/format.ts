export const formatTime = (iso: string | null | undefined) => {
  if (!iso) return '-';
  return new Date(iso).toLocaleString();
};
