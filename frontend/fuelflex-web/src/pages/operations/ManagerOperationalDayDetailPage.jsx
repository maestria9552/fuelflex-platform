import ManagerLayout from "../../components/layout/ManagerLayout";
import OperationalDayDetail from "../../features/operations/components/OperationalDayDetail";
import "./Operations.css";

export default function ManagerOperationalDayDetailPage() {
  return <ManagerLayout><OperationalDayDetail role="manager" /></ManagerLayout>;
}
