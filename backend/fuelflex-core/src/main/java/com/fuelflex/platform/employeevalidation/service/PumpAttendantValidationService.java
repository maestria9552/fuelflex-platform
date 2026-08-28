package com.fuelflex.platform.employeevalidation.service;

import java.util.UUID;

import com.fuelflex.platform.employeevalidation.dto.PumpAttendantValidationDtos.CandidateResponse;
import com.fuelflex.platform.employeevalidation.dto.PumpAttendantValidationDtos.ApprovalResponse;
import com.fuelflex.platform.employeevalidation.dto.PumpAttendantValidationDtos.CreateRequest;
import com.fuelflex.platform.employeevalidation.dto.PumpAttendantValidationDtos.PageResponse;
import com.fuelflex.platform.employeevalidation.dto.PumpAttendantValidationDtos.Response;
import com.fuelflex.platform.employeevalidation.dto.PumpAttendantValidationDtos.ReviewRequest;
import com.fuelflex.platform.employeevalidation.model.PumpAttendantValidationRequestStatus;
import com.fuelflex.platform.user.model.PumpAttendantValidationStatus;

public interface PumpAttendantValidationService {

    PageResponse<CandidateResponse> findManagerCandidates(
            int page,
            int size,
            String search,
            PumpAttendantValidationStatus status
    );

    CandidateResponse findManagerCandidate(UUID pumpAttendantId);

    Response create(CreateRequest request);

    PageResponse<Response> findManagerRequests(
            int page,
            int size,
            PumpAttendantValidationRequestStatus status
    );

    Response findManagerRequest(UUID requestId);

    Response submit(UUID requestId);

    Response cancel(UUID requestId, ReviewRequest request);

    PageResponse<Response> findSupervisorRequests(
            int page,
            int size,
            PumpAttendantValidationRequestStatus status
    );

    Response findSupervisorRequest(UUID requestId);

    ApprovalResponse approve(UUID requestId, ReviewRequest request);

    Response returnForCorrection(UUID requestId, ReviewRequest request);

    Response reject(UUID requestId, ReviewRequest request);
}
