import ManagerLayout from "../../components/layout/ManagerLayout";
import SaleDetail from "../../features/sales/components/SaleDetail";
import "./Sales.css";

export default function ManagerSaleDetailPage() {
  return <ManagerLayout><SaleDetail role="manager" /></ManagerLayout>;
}
