package vn.kma.idp.account.dto;
import jakarta.validation.constraints.*;
public record ActivationProof(@NotBlank @Size(max=256) String token) {}
