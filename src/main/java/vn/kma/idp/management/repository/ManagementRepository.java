package vn.kma.idp.management.repository;
import java.util.*;
import org.springframework.stereotype.Repository;
import vn.kma.idp.shared.Database;
/** Dynamic identifiers are restricted again here to the immutable schema catalog. */
@Repository
public class ManagementRepository {
    private final Database db;
    public ManagementRepository(Database db){
        this.db=db;
    }
    private void identifiers(String table,Collection<String> columns){
        if(!table.matches("[a-z_]+") || columns.stream().anyMatch(c->!c.matches("[a-z_]+")))throw new IllegalArgumentException("Invalid identifier");
    }
    public List<Map<String,Object>> page(String table,Collection<String> columns,int page,int size){
        identifiers(table,columns);
        return db.jdbc.queryForList("SELECT "+String.join(",",columns)+" FROM "+table+" ORDER BY id LIMIT ? OFFSET ?",size,page*size);
    }
    public long count(String table){
        identifiers(table,List.of());
        return db.jdbc.queryForObject("SELECT COUNT(*) FROM "+table,Long.class);
    }
    public Map<String,Object> require(String table,String id){
        identifiers(table,List.of());
        return db.one("SELECT * FROM "+table+" WHERE id=?",id);
    }
    public String create(String table,Map<String,Object> data){
        identifiers(table,data.keySet());
        return db.insert(table,data);
    }
    public void update(String table,String id,Map<String,Object> data){
        identifiers(table,data.keySet());
        var values=new ArrayList<>(data.values());
        values.add(id);
        db.jdbc.update("UPDATE "+table+" SET "+String.join(",",data.keySet().stream().map(k->k+"=?").toList())+" WHERE id=?",values.toArray());
    }
    public void revokeOtherSessions(String actor){
        db.jdbc.update("UPDATE idp_session SET status='REVOKED',revoked_at=? WHERE account_id<>?",Database.now(),actor);
        db.jdbc.update("UPDATE refresh_token SET status='REVOKED',revoked_at=? WHERE account_id<>?",Database.now(),actor);
    }
    public void audit(String actor,String action,String table,String id){
        db.audit(actor,action,table,id,"SUCCESS");
    }
}
