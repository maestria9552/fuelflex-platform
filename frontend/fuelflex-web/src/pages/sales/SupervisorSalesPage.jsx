import SupervisorLayout from "../../components/layout/SupervisorLayout";
import SalesList from "../../features/sales/components/SalesList";
import "./Sales.css";

export default function SupervisorSalesPage() {
  return <SupervisorLayout><SalesList role="supervisor" /></SupervisorLayout>;
}
