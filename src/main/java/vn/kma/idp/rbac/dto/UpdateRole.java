package vn.kma.idp.rbac.dto;
import jakarta.validation.constraints.*;
public record UpdateRole(@Size(min=1,max=128) String name,@Size(max=2048) String description,Boolean active) {}
