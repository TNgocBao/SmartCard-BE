package vn.kma.idp.rbac.dto;
import java.util.List;
import jakarta.validation.constraints.*;
public record RolePermissions(@NotNull @Size(max=100) List<@NotBlank @Size(max=36) String> permissionIds) {}
