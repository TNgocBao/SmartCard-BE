import vn.kma.idp.system.controller.SystemController;
import vn.kma.idp.system.repository.SystemHealthRepository;
import vn.kma.idp.system.service.SystemHealthService;

/** Checks safe readiness behavior when its database dependency is unavailable; no server/DB starts. */
public final class ReadinessChecks {
    public static void main(String[] args){
        var up=new SystemController(new SystemHealthService(new SystemHealthRepository(null){@Override public boolean databaseReady(){return true;}})).health();
        if(up.getStatusCode().value()!=200||!up.getBody().equals(java.util.Map.of("status","UP")))throw new AssertionError("Healthy response");
        var down=new SystemController(new SystemHealthService(new SystemHealthRepository(null){@Override public boolean databaseReady(){return false;}})).health();
        if(down.getStatusCode().value()!=503||!down.getBody().equals(java.util.Map.of("status","DOWN")))throw new AssertionError("Unhealthy response");
        var error=new SystemController(new SystemHealthService(new SystemHealthRepository(null){@Override public boolean databaseReady(){throw new org.springframework.dao.DataAccessResourceFailureException("private SQL diagnostic fixture");}})).health();
        if(error.getStatusCode().value()!=503||!error.getBody().equals(java.util.Map.of("status","DOWN")))throw new AssertionError("Database exception exposed");
        System.out.println("READINESS CHECKS PASS: 3; database availability injected; no Spring Boot or DB connection");
    }
}
