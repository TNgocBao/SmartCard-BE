package vn.kma.idp.account.dto;
import jakarta.validation.constraints.*;
public record RecoveryProof(@NotBlank @Size(max=128) String username,@NotBlank @Size(max=128) String recoveryCode) {}
