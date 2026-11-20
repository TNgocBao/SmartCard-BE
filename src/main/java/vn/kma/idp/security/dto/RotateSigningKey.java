package vn.kma.idp.security.dto;
import jakarta.validation.constraints.*;
public record RotateSigningKey(@NotBlank @Pattern(regexp="[0-9a-fA-F-]{36}") String nextKeyId,@Min(300) @Max(28800) int overlapSeconds) {}
