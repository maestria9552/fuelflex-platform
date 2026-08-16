import { Link } from "react-router-dom";
export default function SupplierPortalPage() {
  return <main style={{ minHeight: "100vh", display: "grid", placeItems: "center", padding: "2rem" }}><section aria-labelledby="supplier-portal-title" style={{ textAlign: "center" }}><h1 id="supplier-portal-title">Portail fournisseur</h1><p>Espace fournisseur FuelFlex</p><Link to="/connexion">Se déconnecter</Link></section></main>;
}
