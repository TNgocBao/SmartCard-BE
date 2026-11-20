package vn.kma.idp.auth.dto;
import jakarta.validation.constraints.*;
public record Refresh(@NotBlank @Size(max=256) String refreshToken) {
}
