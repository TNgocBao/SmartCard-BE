package vn.kma.idp.auth.repository;

import java.util.*;
import org.springframework.stereotype.Repository;
import vn.kma.idp.shared.Database;

@Repository
public class AuthenticationTransactionStore {
    private final Database db;
    public AuthenticationTransactionStore(Database db){this.db=db;}
    public String create(Map<String,Object> values){return db.insert("authentication_transaction",values);}
    public Map<String,Object> find(String tokenHash){return db.one("SELECT * FROM authentication_transaction WHERE transaction_token_hash=? FOR UPDATE",tokenHash);}
    public void context(String id,String encrypted){db.jdbc.update("UPDATE authentication_transaction SET oidc_context_json=? WHERE id=?",encrypted,id);}
    public int consume(String id){return db.jdbc.update("UPDATE authentication_transaction SET consumed_at=? WHERE id=? AND consumed_at IS NULL AND expires_at>? AND failed_attempts<5",Database.now(),id,Database.now());}
    public void failed(String id){db.jdbc.update("UPDATE authentication_transaction SET failed_attempts=failed_attempts+1 WHERE id=? AND consumed_at IS NULL",id);}
}
