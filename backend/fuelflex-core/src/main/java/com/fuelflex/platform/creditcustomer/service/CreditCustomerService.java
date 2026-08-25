package com.fuelflex.platform.creditcustomer.service;
import java.util.*; import com.fuelflex.platform.creditcustomer.dto.CreditCustomerDtos.*;
public interface CreditCustomerService{Response create(Request request);Response update(UUID id,Request request);List<Response> managerList();List<Response> posSelectable();}
