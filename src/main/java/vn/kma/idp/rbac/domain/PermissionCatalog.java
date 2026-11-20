package vn.kma.idp.rbac.domain;
import java.util.Set;
/** Only permissions with actual backend enforcement are offered for assignment. */
public final class PermissionCatalog {
    private PermissionCatalog(){}
    public static final Set<String> CODES=Set.of("idp.manage","role.read","role.create","role.update","role.assign","permission.read","account.create","account.lock","account.unlock","credential.manage","session.revoke","account.activate_invite","account.mfa.reset","key.read","key.rotate");
}
