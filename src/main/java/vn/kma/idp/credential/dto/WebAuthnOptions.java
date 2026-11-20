package vn.kma.idp.credential.dto;
import jakarta.validation.constraints.*;
public record WebAuthnOptions(@NotBlank @Size(max=128) String username) {}
