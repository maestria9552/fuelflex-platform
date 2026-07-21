import React from "react";

function SupervisorDashboardPage() {
  return (
    <div className="container py-4">
      <h1 className="mb-2">Tableau de bord Superviseur</h1>

      <p className="text-muted">
        Bienvenue sur FuelFlex Platform.
      </p>

      <hr />

      <div className="row g-4 mt-2">
        <div className="col-md-3">
          <div className="card shadow-sm">
            <div className="card-body">
              <h6>Stations</h6>
              <h2>0</h2>
            </div>
          </div>
        </div>

        <div className="col-md-3">
          <div className="card shadow-sm">
            <div className="card-body">
              <h6>Gestionnaires</h6>
              <h2>0</h2>
            </div>
          </div>
        </div>

        <div className="col-md-3">
          <div className="card shadow-sm">
            <div className="card-body">
              <h6>Pompistes</h6>
              <h2>0</h2>
            </div>
          </div>
        </div>

        <div className="col-md-3">
          <div className="card shadow-sm">
            <div className="card-body">
              <h6>Ventes du jour</h6>
              <h2>0 FC</h2>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}

export default SupervisorDashboardPage;