const STATION_SETUP_DRAFT_KEY = "fuelflex_station_setup_draft";

export function getStationSetupDraft(organizationId) {
  if (!organizationId) return null;

  try {
    const value = sessionStorage.getItem(STATION_SETUP_DRAFT_KEY);
    const draft = value ? JSON.parse(value) : null;

    if (!draft || draft.organizationId !== organizationId) return null;

    return {
      organizationId: draft.organizationId,
      stationId: draft.stationId || null,
      activeStep: draft.activeStep || "station",
      selectedProductIds: Array.isArray(draft.selectedProductIds)
        ? draft.selectedProductIds
        : [],
      depotIds: Array.isArray(draft.depotIds) ? draft.depotIds : [],
      pumpIds: Array.isArray(draft.pumpIds) ? draft.pumpIds : [],
      dispensingPointIds: Array.isArray(draft.dispensingPointIds)
        ? draft.dispensingPointIds
        : [],
      fuelMeterIds: Array.isArray(draft.fuelMeterIds)
        ? draft.fuelMeterIds
        : [],
    };
  } catch {
    sessionStorage.removeItem(STATION_SETUP_DRAFT_KEY);
    return null;
  }
}

export function saveStationSetupDraft(organizationId, updates = {}) {
  if (!organizationId) return null;

  const currentDraft = getStationSetupDraft(organizationId) || {
    organizationId,
    stationId: null,
    activeStep: "station",
    selectedProductIds: [],
    depotIds: [],
    pumpIds: [],
    dispensingPointIds: [],
    fuelMeterIds: [],
  };
  const updatedDraft = { ...currentDraft, ...updates, organizationId };

  sessionStorage.setItem(
    STATION_SETUP_DRAFT_KEY,
    JSON.stringify(updatedDraft)
  );

  return updatedDraft;
}

export function clearStationSetupDraft() {
  sessionStorage.removeItem(STATION_SETUP_DRAFT_KEY);
}
