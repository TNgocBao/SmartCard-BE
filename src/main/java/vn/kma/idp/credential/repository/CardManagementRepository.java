package vn.kma.idp.credential.repository;

import java.util.*;
import org.springframework.stereotype.Repository;
import vn.kma.idp.shared.Database;

@Repository
public class CardManagementRepository {
    private final Database db;
    public CardManagementRepository(Database db){this.db=db;}
    public List<Map<String,Object>> cards(String account,int offset) {
        return db.jdbc.queryForList("SELECT c.id,c.account_id,c.credential_name,c.status,c.registered_at,c.last_used_at,c.expires_at,s.card_identifier,s.algorithm,s.transport FROM credential c JOIN smart_card_credential s ON s.credential_id=c.id WHERE c.account_id=? ORDER BY c.registered_at DESC,c.id LIMIT 50 OFFSET ?",account,offset);
    }
    public List<Map<String,Object>> history(String account,int offset) {
        return db.jdbc.queryForList("SELECT id,credential_id,authentication_method,event_type,result,failure_reason_code,created_at FROM authentication_event WHERE account_id=? AND authentication_method LIKE 'SMART_CARD%' ORDER BY created_at DESC,id LIMIT 50 OFFSET ?",account,offset);
    }
}
