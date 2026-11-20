package vn.kma.idp.system.repository;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class SystemHealthRepository {
    private final JdbcTemplate jdbc;
    public SystemHealthRepository(JdbcTemplate jdbc){this.jdbc=jdbc;}
    public boolean databaseReady(){return Integer.valueOf(1).equals(jdbc.queryForObject("SELECT 1",Integer.class));}
}
