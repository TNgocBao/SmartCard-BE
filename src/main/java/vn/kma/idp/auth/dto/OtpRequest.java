package vn.kma.idp.auth.dto;
import jakarta.validation.constraints.*;
public record OtpRequest(@NotBlank @Size(max=128) String username) {}
