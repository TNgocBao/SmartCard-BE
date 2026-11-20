package vn.kma.idp.credential.dto;
import jakarta.validation.constraints.*;
public record WebAuthnAssertion(
    @NotBlank @Size(max=128) String transactionToken,
    @NotBlank @Size(max=1400) String credentialId,
    @NotBlank @Size(max=6000) String authenticatorData,
    @NotBlank @Size(max=6000) String clientDataJSON,
    @NotBlank @Size(max=1400) String signature,
    @Size(max=128) String userHandle) {}
