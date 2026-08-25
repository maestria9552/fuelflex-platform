package com.fuelflex.platform.creditcustomer.dto;
import java.util.UUID; import jakarta.validation.constraints.*;
public final class CreditCustomerDtos{private CreditCustomerDtos(){}public record Request(@NotBlank @Size(max=50)String code,@NotBlank @Size(max=180)String name,@Size(max=30)String phone,@Email @Size(max=180)String email,Boolean active){}public record Response(UUID id,String code,String name,String phone,String email,boolean active){} }
