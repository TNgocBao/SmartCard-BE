package vn.kma.idp.audit.repository;

import java.util.*;
import org.springframework.stereotype.Repository;
import vn.kma.idp.shared.Database;

@Repository
public class OutboxStore {
    private final Database db;
    public OutboxStore(Database db){this.db=db;}
    public String create(String id,String type,String encrypted){return db.insert("outbox_event",Map.of("id",id,"event_type",type,"payload",encrypted,"status","NEW","attempts",0,"next_attempt_at",Database.now(),"created_at",Database.now()));}
    public List<Map<String,Object>> due(){return db.jdbc.queryForList("SELECT * FROM outbox_event WHERE status='NEW' AND attempts<5 AND (next_attempt_at IS NULL OR next_attempt_at<=?) ORDER BY created_at LIMIT 20",Database.now());}
    public Map<String,Object> get(String id){return db.one("SELECT * FROM outbox_event WHERE id=?",id);}
    public int claim(String id){return claim(id,((Number)get(id).get("attempts")).intValue());}
    public int claim(String id,int expectedAttempts){return db.jdbc.update("UPDATE outbox_event SET status='PROCESSING',attempts=attempts+1,next_attempt_at=? WHERE id=? AND status='NEW' AND attempts=? AND attempts<5 AND (next_attempt_at IS NULL OR next_attempt_at<=?)",vn.kma.idp.shared.Time.format(java.time.Instant.now().plusSeconds(300)),id,expectedAttempts,Database.now());}
    public int sent(String id,int attempt){return db.jdbc.update("UPDATE outbox_event SET status='SENT' WHERE id=? AND status='PROCESSING' AND attempts=?",id,attempt);}
    public int failed(String id,int attempts){return db.jdbc.update("UPDATE outbox_event SET status=?,next_attempt_at=? WHERE id=? AND status='PROCESSING' AND attempts=?",attempts>=5?"DEAD":"NEW",vn.kma.idp.shared.Time.format(java.time.Instant.now().plusSeconds(Math.min(3600,30L*(1L<<Math.min(attempts,6))))),id,attempts);}
    public void recoverLeases(){db.jdbc.update("UPDATE outbox_event SET status=CASE WHEN attempts>=5 THEN 'DEAD' ELSE 'NEW' END WHERE status='PROCESSING' AND next_attempt_at<?",Database.now());}
}
