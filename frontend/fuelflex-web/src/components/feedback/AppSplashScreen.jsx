import React from "react";
import { motion } from "framer-motion";

import fuelFlexLogo from "../../assets/images/logofuelflex.png";
import "./AppSplashScreen.css";

function AppSplashScreen({
  message = "Initialisation de FuelFlex Platform...",
}) {
  return (
    <motion.div
      className="app-splash-screen"
      initial={{ opacity: 0 }}
      animate={{ opacity: 1 }}
      exit={{ opacity: 0 }}
      transition={{ duration: 0.3 }}
      role="status"
      aria-live="polite"
      aria-label={message}
    >
      <div className="app-splash-screen-decoration app-splash-screen-decoration-left" />
      <div className="app-splash-screen-decoration app-splash-screen-decoration-right" />

      <motion.div
        className="app-splash-screen-content"
        initial={{
          opacity: 0,
          y: 14,
          scale: 0.98,
        }}
        animate={{
          opacity: 1,
          y: 0,
          scale: 1,
        }}
        transition={{
          duration: 0.5,
          ease: [0.22, 1, 0.36, 1],
        }}
      >
        <div className="app-splash-screen-logo-container">
          <motion.div
            className="app-splash-screen-ring"
            animate={{ rotate: 360 }}
            transition={{
              duration: 1.6,
              repeat: Infinity,
              ease: "linear",
            }}
          />

          <div className="app-splash-screen-logo-background">
            <img
              src={fuelFlexLogo}
              alt="FuelFlex"
              className="app-splash-screen-logo-image"
            />
          </div>
        </div>

        <div className="app-splash-screen-brand">
          <h1>
            Fuel<span>Flex</span>
          </h1>

          <strong>Platform</strong>

          <p>Smart Fuel Station Management</p>
        </div>

        <div className="app-splash-screen-progress">
          <div className="app-splash-screen-progress-track">
            <motion.span
              className="app-splash-screen-progress-value"
              initial={{ scaleX: 0 }}
              animate={{ scaleX: 1 }}
              transition={{
                duration: 1.65,
                ease: [0.22, 1, 0.36, 1],
              }}
            />
          </div>

          <p>{message}</p>
        </div>
      </motion.div>

      <p className="app-splash-screen-version">
        FuelFlex Platform · Version 1.0
      </p>
    </motion.div>
  );
}

export default AppSplashScreen;