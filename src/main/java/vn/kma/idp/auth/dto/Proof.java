package vn.kma.idp.auth.dto;
import jakarta.validation.constraints.*;
public record Proof(@NotBlank @Pattern(regexp="[0-9a-fA-F-]{36}") String challengeId,@NotBlank @Size(max=1024) String signature) {
}
