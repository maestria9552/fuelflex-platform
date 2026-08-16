package com.fuelflex.platform.assignment.repository;

import static org.assertj.core.api.Assertions.assertThat;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.data.jpa.repository.Lock;
import jakarta.persistence.LockModeType;

class UserStationAssignmentRepositoryContractTest {
    @Test
    void mutationLookupUsesPessimisticWriteLock() throws Exception {
        var method = UserStationAssignmentRepository.class.getMethod(
                "lockActive", UUID.class, UUID.class, UUID.class);
        Lock lock = method.getAnnotation(Lock.class);
        assertThat(lock).isNotNull();
        assertThat(lock.value()).isEqualTo(LockModeType.PESSIMISTIC_WRITE);
    }
}
