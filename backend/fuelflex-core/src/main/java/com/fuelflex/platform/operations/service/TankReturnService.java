package com.fuelflex.platform.operations.service;
import static com.fuelflex.platform.operations.dto.TankReturnDtos.*;
import java.math.*;
import java.time.OffsetDateTime;
import java.util.*;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.fuelflex.platform.common.exception.*;
import com.fuelflex.platform.common.security.AuthorizationService;
import com.fuelflex.platform.operations.entity.*;
import com.fuelflex.platform.operations.repository.*;
import com.fuelflex.platform.role.entity.Role;
import com.fuelflex.platform.station.service.StationAccessService;
import com.fuelflex.platform.tank.entity.*;
import com.fuelflex.platform.tank.repository.TankRepository;
import com.fuelflex.platform.user.entity.User;
import lombok.RequiredArgsConstructor;

@Service @RequiredArgsConstructor @Transactional
public class TankReturnService {
    private final AuthorizationService authorization;
    private final PumpShiftAssignmentRepository shifts;
    private final OperationalDayRepository days;
    private final TankRepository tanks;
    private final TankReturnRepository returns;
    private final TankReturnStockMovementRepository movements;
    private final TankReturnSourceMovementRepository sourceMovements;
    private final AssignmentFuelSourceResolver sources;
    private final StationAccessService stationAccess;

    public Response create(UUID shiftId, CreateRequest request) {
        User actor = actor("MANAGER");
        PumpShiftAssignment shift = shifts.lockByIdAndOrganizationId(shiftId, actor.getOrganization().getId())
                .orElseThrow(() -> new ResourceNotFoundException("Affectation introuvable."));
        var day = shift.getOperationalDay();
        stationAccess.checkStationAccess(actor, day.getStation().getId());
        if (day.getStatus() != OperationalStatus.OPEN) throw new ConflictException("La journée opérationnelle est clôturée.");
        if (shift.getStatus() != OperationalStatus.OPEN) throw new ConflictException("L’affectation est clôturée.");
        Tank destination = tanks.findByIdAndDepotStationId(request.tankId(), day.getStation().getId())
                .orElseThrow(() -> new ResourceNotFoundException("Cuve de destination introuvable pour cette station."));
        if (!destination.isActive() || destination.getStatus() != TankStatus.ACTIVE)
            throw new BusinessException("La cuve de destination n’est pas active.");
        AssignmentFuelSourceResolver.Source fuel = sources.resolve(shift);
        if (!destination.getProduct().getId().equals(fuel.product().getId()))
            throw new BusinessException("La cuve de destination n’est pas compatible avec le produit de l’affectation.");
        BigDecimal quantity = scale(request.quantity());
        if (quantity.signum() <= 0) throw new BusinessException("La quantité remise en cuve doit être strictement positive.");
        OffsetDateTime occurredAt = request.occurredAt();
        if (occurredAt.isBefore(shift.getOpenedAt()) || occurredAt.isAfter(OffsetDateTime.now()))
            throw new BusinessException("La date de remise doit appartenir à la période active de l’affectation.");
        TankReturn tankReturn = new TankReturn();
        tankReturn.setOrganization(day.getOrganization()); tankReturn.setOperationalDay(day); tankReturn.setShiftAssignment(shift);
        tankReturn.setTank(destination); tankReturn.setQuantity(quantity); tankReturn.setReason(clean(request.reason()));
        tankReturn.setOccurredAt(occurredAt); tankReturn.setCreatedBy(actor);
        tankReturn = returns.saveAndFlush(tankReturn);
        TankReturnStockMovement movement = new TankReturnStockMovement();
        movement.setTankReturn(tankReturn); movement.setStation(day.getStation()); movement.setTank(destination);
        movement.setProduct(fuel.product()); movement.setQuantity(quantity);
        try { movements.saveAndFlush(movement); }
        catch (DataIntegrityViolationException e) { throw new ConflictException("Le mouvement de stock de cette remise existe déjà."); }
        TankReturnSourceMovement sourceMovement=new TankReturnSourceMovement();sourceMovement.setTankReturn(tankReturn);sourceMovement.setStation(day.getStation());sourceMovement.setTank(fuel.tank());sourceMovement.setProduct(fuel.product());sourceMovement.setQuantity(quantity);
        try{sourceMovements.saveAndFlush(sourceMovement);}catch(DataIntegrityViolationException e){throw new ConflictException("Le mouvement source de cette remise existe déjà.");}
        return response(tankReturn, fuel);
    }

    @Transactional(readOnly=true) public List<Response> byShift(UUID shiftId) {
        User actor=viewer(); PumpShiftAssignment shift=shifts.findByIdAndOperationalDayOrganizationId(shiftId, actor.getOrganization().getId())
                .orElseThrow(() -> new ResourceNotFoundException("Affectation introuvable."));
        stationAccess.checkStationAccess(actor, shift.getOperationalDay().getStation().getId());
        var fuel=sources.resolve(shift); return returns.findByShiftAssignmentIdOrderByOccurredAtAsc(shiftId).stream().map(r->response(r,fuel)).toList();
    }
    @Transactional(readOnly=true) public List<Response> byDay(UUID dayId) {
        User actor=viewer(); var day=days.findByIdAndOrganizationId(dayId, actor.getOrganization().getId())
                .orElseThrow(() -> new ResourceNotFoundException("Journée opérationnelle introuvable."));
        stationAccess.checkStationAccess(actor, day.getStation().getId());
        return returns.findByOperationalDayIdOrderByOccurredAtAsc(dayId).stream().map(r->response(r,sources.resolve(r.getShiftAssignment()))).toList();
    }
    private Response response(TankReturn r, AssignmentFuelSourceResolver.Source f) { var s=r.getShiftAssignment(); var u=s.getPumpAttendant(); var m=s.getFuelMeter();var historical=sourceMovements.findByTankReturnId(r.getId());var source=historical.map(TankReturnSourceMovement::getTank).orElse(f.tank());var product=historical.map(TankReturnSourceMovement::getProduct).orElse(f.product());
        return new Response(r.getId(),r.getOrganization().getId(),r.getOperationalDay().getId(),s.getId(),source.getId(),source.getName(),r.getTank().getId(),r.getTank().getName(),
                u.getId(),u.getFirstName()+" "+u.getLastName(),m.getId(),m.getName(),f.pump().getId(),f.pump().getName(),product.getId(),product.getName(),
                r.getQuantity(),r.getReason(),r.getOccurredAt(),r.getCreatedBy().getId(),r.getCreatedAt()); }
    private User viewer() { User u=authorization.getAuthenticatedUser(); if(u==null || !u.isEnabled() || u.getOrganization()==null ||
            u.getRoles().stream().filter(Role::isActive).noneMatch(r->Set.of("MANAGER","SUPERVISOR").contains(r.getCode()))) throw new ForbiddenException("Accès aux remises refusé."); return u; }
    private User actor(String role) { User u=viewer(); if(u.getRoles().stream().filter(Role::isActive).noneMatch(r->role.equals(r.getCode()))) throw new ForbiddenException("Rôle requis: "+role); return u; }
    private BigDecimal scale(BigDecimal v) { try{return v.setScale(3,RoundingMode.UNNECESSARY);}catch(ArithmeticException e){throw new BusinessException("La quantité accepte au plus trois décimales.");} }
    private String clean(String v) { return v==null||v.isBlank()?null:v.trim().replaceAll("\\s+"," "); }
}
