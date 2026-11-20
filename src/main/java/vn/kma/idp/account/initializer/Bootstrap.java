package vn.kma.idp.account.initializer;
import java.util.Map;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.*;
import org.springframework.core.env.Environment;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import vn.kma.idp.shared.Database;
@Configuration
public class Bootstrap {
    @Bean ApplicationRunner initialize(Database db,Environment env,BCryptPasswordEncoder encoder,org.springframework.transaction.PlatformTransactionManager manager,vn.kma.idp.rbac.repository.RoleManagementRepository roles) {
        return args -> new org.springframework.transaction.support.TransactionTemplate(manager).executeWithoutResult(transaction -> {
            roles.ensureCatalog();
            if(db.jdbc.queryForObject("SELECT COUNT(*) FROM account",Integer.class)>0) return;
            String password=env.getProperty("app.bootstrap-password","");
            if(password.length()<14 || password.getBytes(java.nio.charset.StandardCharsets.UTF_8).length>72) throw new IllegalStateException("Set BOOTSTRAP_PASSWORD (14..72 characters) for first startup");
            String person=db.insert("person",Map.of("display_name","KMA Administrator","person_code","BOOTSTRAP","email",env.getProperty("app.bootstrap-email","admin@example.invalid"),"status","ACTIVE","created_at",Database.now()));
            String identity=db.insert("identity",Map.of("person_id",person,"subject_id",Database.id(),"identifier","admin","identity_type","STAFF","status","ACTIVE"));
            String account=db.insert("account",Map.of("identity_id",identity,"username","admin","status","ACTIVE","is_locked",false,"failed_login_attempts",0,"created_at",Database.now()));
            db.insert("credential",Map.of("account_id",account,"credential_type","PASSWORD","credential_name","Bootstrap admin","secret_hash",encoder.encode(password),"status","ACTIVE","registered_at",Database.now()));
            String role=db.insert("role",Map.of("role_code","SUPER_ADMIN","role_name","Superuser","is_system",true,"is_active",true));
            String permission=db.one("SELECT id FROM permission WHERE permission_code='idp.manage'").get("id").toString();
            db.insert("role_permission",Map.of("role_id",role,"permission_id",permission));
            db.insert("account_role_assignment",Map.of("account_id",account,"role_id",role,"assigned_by",account,"status","ACTIVE"));
            db.audit(account,"bootstrap.complete","account",account,"SUCCESS");
        });
    }
}
