package com.fuelflex.platform.operations.controller;
import static com.fuelflex.platform.operations.dto.OperationalDtos.*; import java.util.*;
import org.springframework.http.HttpStatus; import org.springframework.security.access.prepost.PreAuthorize; import org.springframework.web.bind.annotation.*; import jakarta.validation.Valid; import lombok.RequiredArgsConstructor;
import com.fuelflex.platform.operations.service.OperationalDayService;
@RestController @RequestMapping("/api/v1/manager") @RequiredArgsConstructor
public class ManagerOperationalDayController {
 private final OperationalDayService service;
 @PostMapping("/operational-days") @ResponseStatus(HttpStatus.CREATED) @PreAuthorize("hasAuthority('MANAGER') and hasAuthority('operational-day:open')") public DayResponse open(@Valid @RequestBody OpenDayRequest r){return service.open(r);}
 @GetMapping("/operational-days") @PreAuthorize("(hasAuthority('MANAGER') or hasAuthority('SUPERVISOR')) and hasAuthority('operational-day:view')") public List<DayResponse> all(){return service.findAll();}
 @GetMapping("/operational-days/{id}") @PreAuthorize("(hasAuthority('MANAGER') or hasAuthority('SUPERVISOR')) and hasAuthority('operational-day:view')") public DayResponse one(@PathVariable UUID id){return service.findById(id);}
 @PostMapping("/operational-days/{id}/assignments") @ResponseStatus(HttpStatus.CREATED) @PreAuthorize("hasAuthority('MANAGER') and hasAuthority('shift-assignment:create')") public AssignmentResponse assign(@PathVariable UUID id,@Valid @RequestBody OpenAssignmentRequest r){return service.assign(id,r);}
 @GetMapping("/operational-days/{id}/assignments") @PreAuthorize("(hasAuthority('MANAGER') or hasAuthority('SUPERVISOR')) and hasAuthority('shift-assignment:view')") public List<AssignmentResponse> assignments(@PathVariable UUID id){return service.assignments(id);}
 @GetMapping("/shift-assignments/{id}") @PreAuthorize("(hasAuthority('MANAGER') or hasAuthority('SUPERVISOR')) and hasAuthority('shift-assignment:view')") public AssignmentResponse assignment(@PathVariable UUID id){return service.assignment(id);}
 @PostMapping("/shift-assignments/{id}/close") @PreAuthorize("hasAuthority('MANAGER') and hasAuthority('shift-assignment:close')") public AssignmentResponse closeAssignment(@PathVariable UUID id,@Valid @RequestBody CloseAssignmentRequest r){return service.closeAssignment(id,r);}
 @PostMapping("/operational-days/{id}/close") @PreAuthorize("hasAuthority('MANAGER') and hasAuthority('operational-day:close')") public DayResponse close(@PathVariable UUID id){return service.close(id);}
}
