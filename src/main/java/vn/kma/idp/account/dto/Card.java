package vn.kma.idp.account.dto;
import jakarta.validation.constraints.*;
public record Card(@NotBlank @Pattern(regexp="[A-Fa-f0-9]{32}") String cardIdentifier,@NotBlank @Size(max=4096) String publicKeySpki,@NotBlank String transport) {
}
