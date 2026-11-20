package vn.kma.idp.account.dto;
import jakarta.validation.constraints.*;
public record NewAccount(@Pattern(regexp="[A-Za-z0-9_.-]{3,64}") @NotBlank String username,@NotBlank @Size(max=255) String displayName,@Email @Size(max=255) String email) {
}
