package com.fuelflex.platform.operations.service;

import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.fuelflex.platform.assignment.repository.UserStationAssignmentRepository;
import com.fuelflex.platform.common.exception.BusinessException;
import com.fuelflex.platform.common.exception.ForbiddenException;
import com.fuelflex.platform.common.security.AuthorizationService;
import com.fuelflex.platform.operations.dto.OperationalDtos.PosOperationalContext;
import com.fuelflex.platform.operations.dto.OperationalDtos.UserSummary;
import com.fuelflex.platform.operations.entity.OperationalStatus;
import com.fuelflex.platform.operations.entity.PumpShiftAssignment;
import com.fuelflex.platform.operations.repository.PumpShiftAssignmentRepository;
import com.fuelflex.platform.role.entity.Role;
import com.fuelflex.platform.sale.service.PosConfigurationResolver;
import com.fuelflex.platform.sale.service.ResolvedPosContext;
import com.fuelflex.platform.user.entity.User;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PumpAttendantOperationalContextServiceImpl implements PumpAttendantOperationalContextService {
    private final AuthorizationService authorization;
    private final PumpShiftAssignmentRepository shifts;
    private final UserStationAssignmentRepository administrativeAssignments;
    private final PosConfigurationResolver resolver;

    @Override
    public Optional<PosOperationalContext> currentContext() {
        User user = authenticatedPumpAttendant();
        return shifts.findFirstByPumpAttendantIdAndStatusOrderByOpenedAtDesc(user.getId(), OperationalStatus.OPEN)
                .filter(assignment -> assignment.getOperationalDay().getStatus() == OperationalStatus.OPEN)
                .filter(assignment -> hasAdministrativeAssignment(user, assignment))
                .map(resolver::resolve)
                .map(context -> response(user, context));
    }

    @Override
    public PosOperationalContext requireCurrentContext() {
        return currentContext().orElseThrow(() ->
                new BusinessException("Le pompiste n’est actuellement affecté à aucun poste ouvert."));
    }

    private User authenticatedPumpAttendant() {
        User user = authorization.getAuthenticatedUser();
        if (!user.isEnabled() || user.getOrganization() == null
                || user.getRoles().stream().filter(Role::isActive)
                .noneMatch(role -> "PUMP_ATTENDANT".equalsIgnoreCase(role.getCode()))) {
            throw new ForbiddenException("Un compte pompiste actif est requis.");
        }
        return user;
    }

    private boolean hasAdministrativeAssignment(User user, PumpShiftAssignment assignment) {
        return administrativeAssignments.existsByUserIdAndStationIdAndOrganizationIdAndValidUntilIsNull(
                user.getId(), assignment.getOperationalDay().getStation().getId(), user.getOrganization().getId());
    }

    private PosOperationalContext response(User user, ResolvedPosContext context) {
        PumpShiftAssignment assignment = context.assignment();
        var day = assignment.getOperationalDay();
        return new PosOperationalContext(
                new UserSummary(user.getId(), user.getFirstName(), user.getLastName()),
                user.getOperationalCode(), day.getId(), assignment.getId(), day.getOrganization().getId(),
                day.getStation().getId(), day.getStation().getName(), context.pump().getId(), context.pump().getName(),
                context.dispensingPoint() == null ? null : context.dispensingPoint().getId(),
                context.fuelMeter().getId(), context.fuelMeter().getName(), context.tank().getId(), context.tank().getName(),
                context.product().getId(), context.product().getName(), assignment.getOpeningIndex(), context.cashUnitPrice());
    }
}
