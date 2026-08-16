import { useTranslation } from "react-i18next";

import fuelFlexLogo from "../../assets/images/logofuelflex.png";
import "./FuelFlexLogo.css";

function FuelFlexLogo({
  size = 48,
  showText = true,
  compact = false,
  variant = "default",
  className = "",
}) {
  const { t } = useTranslation("common");
  const showSlogan = variant === "splash";

  return (
    <div
      className={[
        "fuelflex-brand",
        compact ? "fuelflex-brand-compact" : "",
        showSlogan ? "fuelflex-brand-splash" : "",
        className,
      ]
        .filter(Boolean)
        .join(" ")}
      aria-label={t("brand.logoAlt")}
    >
      <div
        className="fuelflex-brand-logo-wrapper"
        style={{
          width: `${size}px`,
          height: `${size}px`,
        }}
      >
        <img
          src={fuelFlexLogo}
          alt={t("brand.logoAlt")}
          className="fuelflex-brand-logo"
        />
      </div>

      {showText && !compact && (
        <div className="fuelflex-brand-text">
          <div className="fuelflex-brand-name">
            <strong>
              Fuel<span>Flex</span>
            </strong>

            <span className="fuelflex-brand-platform">
              Platform
            </span>
          </div>

          {showSlogan && (
            <small className="fuelflex-brand-slogan">
              {t("brand.slogan")}
            </small>
          )}
        </div>
      )}
    </div>
  );
}

export default FuelFlexLogo;
