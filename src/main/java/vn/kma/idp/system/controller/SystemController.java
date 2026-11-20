package vn.kma.idp.system.controller;

import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class SystemController {
    private final vn.kma.idp.system.service.SystemHealthService health;
    public SystemController(vn.kma.idp.system.service.SystemHealthService health){this.health=health;}
    /** Minimal readiness endpoint available in both the pinned build and the local cached runtime. */
    @GetMapping("/api/v1/health")
    public org.springframework.http.ResponseEntity<Map<String,String>> health(){
        if(health.ready())return org.springframework.http.ResponseEntity.ok(Map.of("status","UP"));
        return org.springframework.http.ResponseEntity.status(503).body(Map.of("status","DOWN"));
    }
    @GetMapping("/api/v1/status")
    public Map<String,Object> status() {
        return Map.of("status","UP","service","KMA Identity","version","0.1.0");
    }
}
