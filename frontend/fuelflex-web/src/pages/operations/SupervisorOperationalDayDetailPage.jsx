import SupervisorLayout from "../../components/layout/SupervisorLayout";
import OperationalDayDetail from "../../features/operations/components/OperationalDayDetail";
import "./Operations.css";

export default function SupervisorOperationalDayDetailPage() {
  return <SupervisorLayout><OperationalDayDetail role="supervisor" /></SupervisorLayout>;
}
