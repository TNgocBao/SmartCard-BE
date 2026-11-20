package vn.kma.idp.auth.dto;
import jakarta.validation.constraints.*;
public record Password(@NotBlank @Size(max=128) String username,@NotBlank @Size(max=72) String password) {
}
