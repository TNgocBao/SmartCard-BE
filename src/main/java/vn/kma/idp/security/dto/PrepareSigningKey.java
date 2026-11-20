package vn.kma.idp.security.dto;
import jakarta.validation.constraints.*;
public record PrepareSigningKey(@NotBlank @Pattern(regexp="[A-Za-z0-9_-]{1,64}") String alias) {}
