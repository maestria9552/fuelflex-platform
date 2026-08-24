package com.fuelflex.platform.user.repository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import com.fuelflex.platform.user.entity.User;
public interface PumpAttendantNumberRepository extends Repository<User, java.util.UUID> {
 @Query(value="select nextval('pump_attendant_operational_code_seq')", nativeQuery=true) long nextValue();
}
