package vn.kma.idp.shared;
import java.time.Instant;
import java.util.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
@Component
public class Database {
    public final JdbcTemplate jdbc;
    public Database(JdbcTemplate jdbc) {
        this.jdbc=jdbc;
    }
    public static String id() {
        return UUID.randomUUID().toString();
    }
    public static String now() {
        return Time.format(Instant.now());
    }
    public Map<String,Object> one(String sql,Object... args) {
        var rows=jdbc.queryForList(sql,args);
        if(rows.isEmpty()) throw new ApiException(404,"RESOURCE_NOT_FOUND");
        return rows.getFirst();
    }
    // Identifiers only originate in server-side constants or validated catalog entries.
    public String insert(String table,Map<String,?> data) {
        var values=new LinkedHashMap<String,Object>();
        values.put("id",id());
        values.putAll(data);
        jdbc.update("INSERT INTO "+table+" ("+String.join(",",values.keySet())+") VALUES ("+
        String.join(",",Collections.nCopies(values.size(),"?"))+")",values.values().toArray());
        return values.get("id").toString();
    }
    public void audit(String actor,String action,String resource,String resourceId,String result) {
        var values=new HashMap<String,Object>();
        values.put("actor_account_id",actor);
        values.put("action",action);
        values.put("event_type","MANAGEMENT");
        values.put("resource_type",resource);
        values.put("resource_id",resourceId);
        values.put("result",result);
        values.put("created_at",now());
        values.put("correlation_id",id());
        insert("audit_log",values);
    }
}
