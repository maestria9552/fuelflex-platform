import { Check } from "lucide-react";

const STEPS = [
  { id: "station", label: "Station", available: true },
  { id: "products", label: "Produits utilisés", available: true },
  { id: "depots", label: "Dépôts", available: true },
  { id: "tanks", label: "Citernes", available: true },
  { id: "pumps", label: "Pompes", available: true },
  { id: "dispensing-points", label: "Pistolets", available: true },
  { id: "fuel-meters", label: "Compteurs", available: true },
  { id: "review", label: "Vérification", available: true },
  { id: "commissioning", label: "Mise en service", available: true },
];

function StationSetupStepper({ activeStep }) {
  const activeIndex = STEPS.findIndex((step) => step.id === activeStep);

  return (
    <div className="station-wizard-stepper" aria-label="Étapes du wizard Station">
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
            <strong>{step.label}</strong>
          </div>
        );
      })}
    </div>
  );
}

export default StationSetupStepper;
