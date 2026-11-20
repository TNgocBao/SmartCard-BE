package vn.kma.idp.account.dto;
import jakarta.validation.constraints.*;
public record ResetMfa(@NotBlank @Size(min=5,max=500) String reason) {}
