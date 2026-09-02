export const PREPARABLE_PUMP_ATTENDANT_STATUS = 'PREPARATION';

export function isPreparablePumpAttendant(candidate) {
  return candidate?.validationStatus === PREPARABLE_PUMP_ATTENDANT_STATUS;
}

export function onlyPreparablePumpAttendants(page) {
  const content = Array.isArray(page?.content)
    ? page.content.filter(isPreparablePumpAttendant)
    : [];
  const removed = Math.max(0, (page?.content?.length || 0) - content.length);
  return {
    ...page,
    content,
    totalElements: Math.max(0, Number(page?.totalElements || 0) - removed),
  };
}
