import { Link } from "react-router-dom";

function LoginPage() {
  return (
    <main
      style={{
        minHeight: "100vh",
        display: "grid",
        placeItems: "center",
        fontFamily: "Inter, sans-serif",
        background: "#f7f9fc",
      }}
    >
      <section style={{ textAlign: "center" }}>
        <h1>Connexion FuelFlex</h1>
        <p>La page de connexion sera créée à l’étape suivante.</p>

        <Link to="/">Retour à l’accueil</Link>
      </section>
    </main>
  );
}

export default LoginPage;