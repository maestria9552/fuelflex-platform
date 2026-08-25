package com.fuelflex.platform.operations.config;
import java.util.*; import org.springframework.boot.*; import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty; import org.springframework.core.annotation.Order; import org.springframework.stereotype.Component; import org.springframework.transaction.annotation.Transactional;
import com.fuelflex.platform.permission.entity.Permission; import com.fuelflex.platform.permission.repository.PermissionRepository; import com.fuelflex.platform.role.entity.Role; import com.fuelflex.platform.role.repository.RoleRepository; import lombok.RequiredArgsConstructor;
@Component @Order(6) @RequiredArgsConstructor @ConditionalOnProperty(name="fuelflex.data-initialization.enabled",havingValue="true",matchIfMissing=true)
public class OperationsPermissionDataInitializer implements CommandLineRunner {
 private final PermissionRepository permissions; private final RoleRepository roles;
 @Override @Transactional public void run(String... args){
  ensure("operational-day:view","Consulter les journées opérationnelles"); ensure("operational-day:open","Ouvrir une journée opérationnelle"); ensure("operational-day:close","Fermer une journée opérationnelle"); ensure("shift-assignment:view","Consulter les affectations pompistes"); ensure("shift-assignment:create","Créer une affectation pompiste"); ensure("shift-assignment:close","Fermer une affectation pompiste");
  ensure("pos-sale:view","Consulter les ventes POS"); ensure("pos-sale:create","Créer une vente POS");
  assign("MANAGER","operational-day:view","operational-day:open","operational-day:close","shift-assignment:view","shift-assignment:create","shift-assignment:close"); assign("SUPERVISOR","operational-day:view","shift-assignment:view"); assign("SUPER_ADMIN","operational-day:view","operational-day:open","operational-day:close","shift-assignment:view","shift-assignment:create","shift-assignment:close");
  assign("PUMP_ATTENDANT","pos-sale:view","pos-sale:create"); assign("SUPER_ADMIN","pos-sale:view","pos-sale:create");
 }
 private void ensure(String code,String name){if(!permissions.existsByCodeIgnoreCase(code)) permissions.save(new Permission(code,name,name,"OPERATIONS"));}
 private void assign(String roleCode,String... codes){Role role=roles.findByCodeIgnoreCase(roleCode).orElseThrow();Set<Permission> all=new HashSet<>(role.getPermissions());for(String code:codes)all.add(permissions.findByCodeIgnoreCase(code).orElseThrow());role.setPermissions(all);roles.save(role);}
}
