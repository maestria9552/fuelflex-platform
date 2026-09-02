package com.fuelflex.platform.operations.service;
import java.util.*; import com.fuelflex.platform.operations.dto.OperationalDtos.*;
public interface OperationalDayService {
 DayResponse open(OpenDayRequest request); List<DayResponse> findAll(); DayResponse findById(UUID id); DayResponse close(UUID id,CloseDayRequest request);
 AssignmentResponse assign(UUID dayId,OpenAssignmentRequest request); List<AssignmentResponse> assignments(UUID dayId); AssignmentResponse assignment(UUID id); AssignmentResponse closeAssignment(UUID id,CloseAssignmentRequest request);
}
