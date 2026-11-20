package vn.kma.idp.account.dto;
import jakarta.validation.constraints.*;
public record EnrollmentProof(@NotBlank @Size(max=256) String enrollmentToken,@Pattern(regexp="[0-9]{6}") String code) {}
