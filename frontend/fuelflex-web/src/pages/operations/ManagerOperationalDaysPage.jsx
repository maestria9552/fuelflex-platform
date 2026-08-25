import ManagerLayout from "../../components/layout/ManagerLayout";
import OperationalDaysList from "../../features/operations/components/OperationalDaysList";
import "./Operations.css";

export default function ManagerOperationalDaysPage() {
  return <ManagerLayout><OperationalDaysList role="manager" /></ManagerLayout>;
}
