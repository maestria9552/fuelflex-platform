package com.fuelflex.platform.operations.service;

import java.util.Optional;
import com.fuelflex.platform.operations.dto.OperationalDtos.PosOperationalContext;

public interface PumpAttendantOperationalContextService {
    Optional<PosOperationalContext> currentContext();
    PosOperationalContext requireCurrentContext();
}
