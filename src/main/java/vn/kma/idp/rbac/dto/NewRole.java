package vn.kma.idp.rbac.dto;
import jakarta.validation.constraints.*;
public record NewRole(@NotBlank @Pattern(regexp="[A-Z][A-Z0-9_]{2,63}") String code,@NotBlank @Size(max=128) String name,@Size(max=2048) String description) {}
