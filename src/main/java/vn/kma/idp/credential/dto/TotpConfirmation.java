package vn.kma.idp.credential.dto;
import jakarta.validation.constraints.*;
public record TotpConfirmation(@NotBlank @Pattern(regexp="[0-9a-fA-F-]{36}") String credentialId,@NotBlank @Pattern(regexp="[0-9]{6}") String code) {}
