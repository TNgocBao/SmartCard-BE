package vn.kma.idp.auth.dto;
import jakarta.validation.constraints.*;
public record Challenge(@NotBlank @Size(max=128) String username,@NotBlank @Pattern(regexp="[A-Fa-f0-9]{32}") String cardIdentifier) {
}
