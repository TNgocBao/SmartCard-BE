package vn.kma.idp.credential.dto;
import jakarta.validation.constraints.*;
public record WebAuthnRegistration(
    @NotBlank @Size(max=128) String transactionToken,
    @NotBlank @Size(max=44000) String attestationObject,
    @NotBlank @Size(max=6000) String clientDataJSON) {}
