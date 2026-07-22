import React from "react";
import fuelFlexLogo from "../../assets/images/logofuelflex.png";
import "./FuelFlexLogo.css";

function FuelFlexLogo({
  size = 48,
  showText = true,
  compact = false,
  variant = "default",
  className = "",
}) {
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
      aria-label="FuelFlex Platform"
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
          alt="Logo FuelFlex"
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
              Smart Fuel Station Management
            </small>
          )}
        </div>
      )}
    </div>
  );
}

export default FuelFlexLogo;