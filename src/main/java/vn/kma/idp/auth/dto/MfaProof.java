package vn.kma.idp.auth.dto;

import jakarta.validation.constraints.*;

public record MfaProof(@NotBlank @Size(max=256) String transactionToken,@NotBlank @Pattern(regexp="[0-9]{6}") String code) {}
