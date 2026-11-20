package vn.kma.idp.management.controller;
import vn.kma.idp.management.service.ManagementService;
import java.util.Map;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import vn.kma.idp.security.UserPrincipal;
import org.springframework.web.bind.annotation.*;
@RestController
@RequestMapping("/api/v1/management")
@PreAuthorize("hasAuthority('idp.manage')")
public class ManagementController {
    private final ManagementService service;
    public ManagementController(ManagementService service) {
        this.service=service;
    }
    @GetMapping("/catalog") Object catalog() {
        return Map.of("data",service.catalog());
    }
    @GetMapping("/overview") Object overview() {
        return Map.of("data",service.overview());
    }
    @GetMapping("/resources/{table}") Object page(@PathVariable String table,@RequestParam(defaultValue="0") int page,@RequestParam(defaultValue="50") int size) {
        return service.page(table,page,size);
    }
    @PostMapping("/resources/{table}") Object create(@AuthenticationPrincipal UserPrincipal jwt,@PathVariable String table,@RequestBody Map<String,Object> data) {
        return Map.of("id",service.save(jwt.accountId(),table,null,data));
    }
    @PatchMapping("/resources/{table}/{id}") Object update(@AuthenticationPrincipal UserPrincipal jwt,@PathVariable String table,@PathVariable String id,@RequestBody Map<String,Object> data) {
        return Map.of("id",service.save(jwt.accountId(),table,id,data));
    }
}
