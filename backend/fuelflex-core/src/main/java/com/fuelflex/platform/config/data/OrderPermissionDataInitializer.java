package com.fuelflex.platform.config.data;
import java.util.*; import org.springframework.boot.*; import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty; import org.springframework.core.annotation.Order; import org.springframework.stereotype.Component; import org.springframework.transaction.annotation.Transactional;
import com.fuelflex.platform.permission.entity.Permission; import com.fuelflex.platform.permission.repository.PermissionRepository; import com.fuelflex.platform.role.entity.Role; import com.fuelflex.platform.role.repository.RoleRepository; import lombok.RequiredArgsConstructor;
@Component @Order(4) @RequiredArgsConstructor
@ConditionalOnProperty(name="fuelflex.data-initialization.enabled",havingValue="true",matchIfMissing=true)
public class OrderPermissionDataInitializer implements CommandLineRunner {
 private final RoleRepository roles; private final PermissionRepository permissions;
 @Override @Transactional public void run(String... args){assign("MANAGER","order:view","order:create","order:update","order:submit");assign("SUPERVISOR","order:view","order:supervisor_approve","order:supervisor_reject");assign("SUPPLIER_USER","order:view","order:supplier_approve","order:supplier_reject");}
 private void assign(String roleCode,String... codes){Role role=roles.findByCodeIgnoreCase(roleCode).orElseThrow();Set<Permission> all=new HashSet<>(role.getPermissions());for(String code:codes)all.add(permissions.findByCodeIgnoreCase(code).orElseThrow(()->new IllegalStateException("Permission absente: "+code)));role.setPermissions(all);roles.save(role);}
}
