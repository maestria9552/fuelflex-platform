import { Check } from "lucide-react";
import { useTranslation } from "react-i18next";

const STEPS = [
  { id: "station", labelKey: "stationSetup:stepper.station", available: true },
  { id: "products", labelKey: "stationSetup:stepper.products", available: true },
  { id: "depots", labelKey: "depots:page.title", available: true },
  { id: "tanks", labelKey: "tanks:page.title", available: true },
  { id: "pumps", labelKey: "pumps:page.title", available: true },
  { id: "dispensing-points", labelKey: "dispensingPoints:page.title", available: true },
  { id: "fuel-meters", labelKey: "fuelMeters:page.title", available: true },
  { id: "review", labelKey: "stationSetup:stepper.review", available: true },
  { id: "commissioning", labelKey: "stationSetup:stepper.commissioning", available: true },
];

function StationSetupStepper({ activeStep }) {
  const { t } = useTranslation(["stationSetup", "depots", "tanks", "pumps", "dispensingPoints", "fuelMeters"]);
  const activeIndex = STEPS.findIndex((step) => step.id === activeStep);

  return (
    <div className="station-wizard-stepper" aria-label={t("stationSetup:stepper.ariaLabel")}>
      {STEPS.map((step, index) => {
        const isCompleted = index < activeIndex;
        const isActive = step.id === activeStep;

        return (
          <div
            key={step.id}
            className={[
              "station-wizard-step",
              isActive ? "active" : "",
              isCompleted ? "completed" : "",
              !step.available ? "disabled" : "",
            ]
              .filter(Boolean)
              .join(" ")}
          >
            <span>{isCompleted ? <Check size={14} /> : index + 1}</span>
            <strong>{t(step.labelKey)}</strong>
          </div>
        );
      })}
    </div>
  );
}

export default StationSetupStepper;
