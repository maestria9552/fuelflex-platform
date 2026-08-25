import ManagerLayout from "../../components/layout/ManagerLayout";
import SalesList from "../../features/sales/components/SalesList";
import "./Sales.css";

export default function ManagerSalesPage() {
  return <ManagerLayout><SalesList role="manager" /></ManagerLayout>;
}
