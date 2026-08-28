package com.fuelflex.platform.operations.service;

import static com.fuelflex.platform.operations.dto.OperationalDtos.*;
import java.math.*; import java.time.OffsetDateTime; import java.util.*;
import org.springframework.dao.DataIntegrityViolationException; import org.springframework.stereotype.Service; import org.springframework.transaction.annotation.Transactional;
import com.fuelflex.platform.assignment.repository.UserStationAssignmentRepository;
import com.fuelflex.platform.common.exception.*; import com.fuelflex.platform.common.security.AuthorizationService;
import com.fuelflex.platform.dispensingpoint.entity.DispensingPoint; import com.fuelflex.platform.fuelmeter.entity.*; import com.fuelflex.platform.fuelmeter.repository.FuelMeterRepository;
import com.fuelflex.platform.operations.entity.*; import com.fuelflex.platform.operations.repository.*; import com.fuelflex.platform.pump.entity.*; import com.fuelflex.platform.role.entity.Role;
import com.fuelflex.platform.station.entity.Station; import com.fuelflex.platform.station.repository.StationRepository; import com.fuelflex.platform.station.service.StationAccessService; import com.fuelflex.platform.user.entity.User; import com.fuelflex.platform.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;

@Service @RequiredArgsConstructor @Transactional
public class OperationalDayServiceImpl implements OperationalDayService {
 private final OperationalDayRepository days; private final PumpShiftAssignmentRepository shifts; private final OperationalHistoryRepository history;
 private final AuthorizationService authorization; private final StationAccessService stationAccess; private final StationRepository stations; private final UserRepository users;
 private final UserStationAssignmentRepository administrativeAssignments; private final FuelMeterRepository meters; private final AssignmentMeterValidator assignmentMeterValidator;
 private final ShiftReconciliationService shiftReconciliation; private final ShiftReconciliationRepository reconciliations; private final AssignmentFuelSourceResolver fuelSources; private final MeteredStockMovementService meteredStockMovement; private final OperationalDayClosingService dayClosing; private final SupervisorOperationalNotifier notifier;

 @Override public DayResponse open(OpenDayRequest request){
  User actor=manager(); UUID org=organizationId(actor); Station station=stations.findByIdAndOrganizationId(request.stationId(),org).orElseThrow(()->new ResourceNotFoundException("Station was not found."));
  stationAccess.checkStationAccess(actor,station.getId()); if(!station.isActive()) throw new BusinessException("The station is inactive.");
  if(days.existsByStationIdAndBusinessDate(station.getId(),request.businessDate())) throw new ConflictException("An operational day already exists for this station and business date.");
  if(days.existsByStationIdAndStatus(station.getId(),OperationalStatus.OPEN)) throw new ConflictException("This station already has an open operational day.");
  OperationalDay day=new OperationalDay(); day.setOrganization(actor.getOrganization()); day.setStation(station); day.setBusinessDate(request.businessDate()); day.setStatus(OperationalStatus.OPEN); day.setOpenedBy(actor); day.setOpenedAt(OffsetDateTime.now());
  try { day=days.saveAndFlush(day); } catch(DataIntegrityViolationException e){throw new ConflictException("An incompatible operational day was opened concurrently for this station.");}
  record("OPERATIONAL_DAY",day.getId(),OperationalAction.OPERATIONAL_DAY_OPENED,null,"OPEN",actor,"businessDate="+day.getBusinessDate()); notifier.inform(actor,station,"OPERATIONAL_DAY_OPENED","OPERATIONAL_DAY",day.getId()); return day(day);
 }
 @Override @Transactional(readOnly=true) public List<DayResponse> findAll(){User actor=managerOrViewer(); UUID org=organizationId(actor); return days.findByOrganizationIdAndStationIdInOrderByBusinessDateDescCreatedAtDesc(org,stationAccess.getAccessibleStationIds(actor)).stream().map(this::day).toList();}
 @Override @Transactional(readOnly=true) public DayResponse findById(UUID id){User actor=managerOrViewer(); OperationalDay day=findDay(id,actor); return day(day);}
 @Override public DayResponse close(UUID id){User actor=manager(); OperationalDay day=lockDay(id,actor); if(day.getStatus()==OperationalStatus.CLOSED) throw new ConflictException("The operational day is already closed."); dayClosing.prepare(day,actor); day.setStatus(OperationalStatus.CLOSED); record("OPERATIONAL_DAY",id,OperationalAction.OPERATIONAL_DAY_CLOSED,"OPEN","CLOSED",actor,null); return day(days.save(day));}
 @Override public AssignmentResponse assign(UUID dayId,OpenAssignmentRequest request){
  User actor=manager(); OperationalDay day=lockDay(dayId,actor); if(day.getStatus()!=OperationalStatus.OPEN) throw new ConflictException("The operational day is closed and cannot receive assignments.");
  UUID org=organizationId(actor); User attendant=users.lockByIdAndOrganizationId(request.pumpAttendantId(),org).orElseThrow(()->new ResourceNotFoundException("Pump attendant was not found."));
  if(!hasRole(attendant,"PUMP_ATTENDANT")) throw new BusinessException("The selected user does not have the PUMP_ATTENDANT role."); if(!attendant.isEnabled()) throw new BusinessException("The selected pump attendant is inactive.");
  if(!administrativeAssignments.existsByUserIdAndStationIdAndOrganizationIdAndValidUntilIsNull(attendant.getId(),day.getStation().getId(),org)) throw new ForbiddenException("The pump attendant is not administratively assigned to this station.");
  FuelMeter meter=meters.lockById(request.fuelMeterId()).orElseThrow(()->new ResourceNotFoundException("Fuel meter was not found.")); Pump pump=assignmentMeterValidator.validate(meter,day.getStation());
  if(shifts.existsByFuelMeterIdAndStatus(meter.getId(),OperationalStatus.OPEN)) throw new ConflictException("The fuel meter is already used by an open assignment.");
  if(shifts.existsByPumpAttendantIdAndStatus(attendant.getId(),OperationalStatus.OPEN)) throw new ConflictException("The pump attendant already has an open assignment for this operational day.");
  PumpShiftAssignment shift=new PumpShiftAssignment(); shift.setOperationalDay(day); shift.setPumpAttendant(attendant); shift.setFuelMeter(meter); shift.setOpeningIndex(scale(meter.getCurrentIndex())); shift.setStatus(OperationalStatus.OPEN); shift.setOpenedBy(actor); shift.setOpenedAt(OffsetDateTime.now());
  try {shift=shifts.saveAndFlush(shift);} catch(DataIntegrityViolationException e){throw new ConflictException("An incompatible assignment was opened concurrently.");}
  record("SHIFT_ASSIGNMENT",shift.getId(),OperationalAction.SHIFT_ASSIGNMENT_OPENED,null,"OPEN",actor,"meter="+meter.getId()+",openingIndex="+shift.getOpeningIndex()+",pump="+pump.getId()); notifier.inform(actor,day.getStation(),"SHIFT_ASSIGNMENT_OPENED","SHIFT_ASSIGNMENT",shift.getId()); return assignment(shift);
 }
 @Override @Transactional(readOnly=true) public List<AssignmentResponse> assignments(UUID dayId){User actor=managerOrViewer(); findDay(dayId,actor); return shifts.findByOperationalDayIdOrderByOpenedAtAsc(dayId).stream().map(this::assignment).toList();}
 @Override @Transactional(readOnly=true) public AssignmentResponse assignment(UUID id){User actor=managerOrViewer(); PumpShiftAssignment shift=shifts.findByIdAndOperationalDayOrganizationId(id,organizationId(actor)).orElseThrow(()->new ResourceNotFoundException("Shift assignment was not found.")); stationAccess.checkStationAccess(actor,shift.getOperationalDay().getStation().getId()); return assignment(shift);}
 @Override public AssignmentResponse closeAssignment(UUID id,CloseAssignmentRequest request){
 User actor=manager(); UUID org=organizationId(actor); PumpShiftAssignment shift=shifts.lockByIdAndOrganizationId(id,org).orElseThrow(()->new ResourceNotFoundException("Shift assignment was not found.")); stationAccess.checkStationAccess(actor,shift.getOperationalDay().getStation().getId());
  if(shift.getOperationalDay().getStatus()!=OperationalStatus.OPEN) throw new ConflictException("The operational day is closed.");
  if(shift.getStatus()!=OperationalStatus.OPEN) throw new ConflictException("The shift assignment is already closed."); BigDecimal closing=scale(request.closingIndex()); if(closing.compareTo(shift.getOpeningIndex())<0) throw new BusinessException("Closing index must be greater than or equal to opening index.");
  FuelMeter meter=meters.lockById(shift.getFuelMeter().getId()).orElseThrow(()->new ResourceNotFoundException("Fuel meter was not found.")); if(meter.getCurrentIndex().compareTo(shift.getOpeningIndex())!=0) throw new ConflictException("Fuel meter index continuity has been modified since assignment opening.");
  shift.setClosingIndex(closing); shift.setStatus(OperationalStatus.CLOSED); shift.setClosedBy(actor); shift.setClosedAt(OffsetDateTime.now()); meteredStockMovement.consolidate(shift,actor); shiftReconciliation.calculate(shift,request.creditQuantity()); meter.setCurrentIndex(closing); meters.save(meter); shifts.save(shift);
  record("SHIFT_ASSIGNMENT",id,OperationalAction.SHIFT_ASSIGNMENT_CLOSED,"OPEN","CLOSED",actor,"closingIndex="+closing+",meteredVolume="+shift.getMeteredVolume()); notifier.inform(actor,shift.getOperationalDay().getStation(),"SHIFT_ASSIGNMENT_CLOSED","SHIFT_ASSIGNMENT",id); return assignment(shift);
 }
 private OperationalDay findDay(UUID id,User actor){OperationalDay day=days.findByIdAndOrganizationId(id,organizationId(actor)).orElseThrow(()->new ResourceNotFoundException("Operational day was not found.")); stationAccess.checkStationAccess(actor,day.getStation().getId()); return day;}
 private OperationalDay lockDay(UUID id,User actor){OperationalDay day=days.lockByIdAndOrganizationId(id,organizationId(actor)).orElseThrow(()->new ResourceNotFoundException("Operational day was not found.")); stationAccess.checkStationAccess(actor,day.getStation().getId()); return day;}
 private User manager(){User actor=authorization.getAuthenticatedUser(); if(!hasRole(actor,"MANAGER")) throw new ForbiddenException("Only a manager can perform this operation."); return actor;}
 private User managerOrViewer(){User actor=authorization.getAuthenticatedUser(); if(!hasRole(actor,"MANAGER")&&!hasRole(actor,"SUPERVISOR")) throw new ForbiddenException("Operational data is not available to this user."); return actor;}
 private boolean hasRole(User u,String code){return u.getRoles().stream().filter(Role::isActive).anyMatch(r->code.equalsIgnoreCase(r.getCode()));}
 private UUID organizationId(User u){if(u.getOrganization()==null) throw new ForbiddenException("Authenticated user has no organization."); return u.getOrganization().getId();}
 private BigDecimal scale(BigDecimal value){try{return value.setScale(3,RoundingMode.UNNECESSARY);}catch(ArithmeticException e){throw new BusinessException("Meter indexes support at most three decimal places.");}}
 private void record(String type,UUID id,OperationalAction action,String oldStatus,String newStatus,User actor,String details){OperationalHistory h=new OperationalHistory(); h.setResourceType(type);h.setResourceId(id);h.setAction(action);h.setOldStatus(oldStatus);h.setNewStatus(newStatus);h.setPerformedBy(actor);h.setPerformedAt(OffsetDateTime.now());h.setDetails(details);history.save(h);}
 private UserSummary user(User u){return u==null?null:new UserSummary(u.getId(),u.getFirstName(),u.getLastName());}
 private DayResponse day(OperationalDay d){return new DayResponse(d.getId(),d.getOrganization().getId(),new StationSummary(d.getStation().getId(),d.getStation().getName()),d.getBusinessDate(),d.getStatus(),user(d.getOpenedBy()),d.getOpenedAt(),d.getCreatedAt(),d.getUpdatedAt());}
 private AssignmentResponse assignment(PumpShiftAssignment a){FuelMeter m=a.getFuelMeter(); DispensingPoint p=m.getDispensingPoint(); Pump pump=p==null?m.getPump():p.getPump(); OperationalDay d=a.getOperationalDay();AssignmentFuelSourceResolver.Source source=historicalSource(a).orElseGet(()->fuelSources.resolve(a));return new AssignmentResponse(a.getId(),a.getStatus(),day(d),user(a.getPumpAttendant()),a.getPumpAttendant().getOperationalCode(),new StationSummary(d.getStation().getId(),d.getStation().getName()),new PumpSummary(pump.getId(),pump.getName()),p==null?null:new DispensingPointSummary(p.getId(),p.getName()),new FuelMeterSummary(m.getId(),m.getName()),source.tank().getId(),source.tank().getName(),source.product().getId(),source.product().getName(),a.getOpeningIndex(),a.getClosingIndex(),a.getMeteredVolume(),a.getOpenedAt(),a.getClosedAt(),user(a.getOpenedBy()),user(a.getClosedBy()));}
 private Optional<AssignmentFuelSourceResolver.Source> historicalSource(PumpShiftAssignment assignment){if(assignment.getStatus()!=OperationalStatus.CLOSED)return Optional.empty();return reconciliations.findByShiftAssignmentId(assignment.getId()).map(r->new AssignmentFuelSourceResolver.Source(null,r.getSourceTank(),r.getProduct()));}
}
