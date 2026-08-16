package com.fuelflex.platform.user.dto.response;

import java.util.List;

import org.springframework.data.domain.Page;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class EmployeePageResponse {

    private List<EmployeeResponse> content;
    private int page;
    private int size;
    private long totalElements;
    private int totalPages;
    private boolean first;
    private boolean last;

    public static EmployeePageResponse from(Page<EmployeeResponse> employees) {
        return EmployeePageResponse.builder()
                .content(employees.getContent())
                .page(employees.getNumber())
                .size(employees.getSize())
                .totalElements(employees.getTotalElements())
                .totalPages(employees.getTotalPages())
                .first(employees.isFirst())
                .last(employees.isLast())
                .build();
    }
}
