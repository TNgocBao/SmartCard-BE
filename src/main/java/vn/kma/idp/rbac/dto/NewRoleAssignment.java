package vn.kma.idp.rbac.dto;
import java.time.Instant;
import jakarta.validation.constraints.*;
public record NewRoleAssignment(@NotBlank @Size(max=36) String roleId,@Size(max=36) String organizationId,@Size(max=36) String applicationId,Instant validFrom,Instant validUntil) {}
