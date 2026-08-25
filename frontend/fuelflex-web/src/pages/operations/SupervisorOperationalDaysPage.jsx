import SupervisorLayout from "../../components/layout/SupervisorLayout";
import OperationalDaysList from "../../features/operations/components/OperationalDaysList";
import "./Operations.css";

export default function SupervisorOperationalDaysPage() {
  return <SupervisorLayout><OperationalDaysList role="supervisor" /></SupervisorLayout>;
}
