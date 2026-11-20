package vn.kma.idp.credential.repository;

import java.util.*;
import org.springframework.stereotype.Repository;
import vn.kma.idp.shared.*;
import vn.kma.idp.auth.service.AuthService;

@Repository
public class WebAuthnStore {
    private final Database db;
    public WebAuthnStore(Database db){this.db=db;}
    public Map<String,Object> account(String id){return db.one("SELECT * FROM account WHERE id=? FOR UPDATE",id);}
    public Map<String,Object> byUsername(String username){return db.one("SELECT * FROM account WHERE username=?",username);}
    public String handle(String account){
        var row=account(account);
        if(row.get("webauthn_user_handle")!=null)return row.get("webauthn_user_handle").toString();
        byte[] random=new byte[32];new java.security.SecureRandom().nextBytes(random);
        String handle=Base64.getUrlEncoder().withoutPadding().encodeToString(random);
        db.jdbc.update("UPDATE account SET webauthn_user_handle=? WHERE id=?",handle,account);return handle;
    }
    public List<String> credentialIds(String account){return db.jdbc.queryForList("SELECT w.webauthn_credential_id FROM webauthn_credential w JOIN credential c ON w.credential_id=c.id WHERE c.account_id=? AND c.status='ACTIVE' AND (c.expires_at IS NULL OR c.expires_at>?)",String.class,account,Database.now());}
    public Map<String,Object> credential(String account,String encodedId){return db.one("SELECT w.*,c.status AS credential_status,c.expires_at FROM webauthn_credential w JOIN credential c ON w.credential_id=c.id WHERE c.account_id=? AND w.credential_id_hash=? FOR UPDATE",account,AuthService.hash(encodedId));}
    public String register(String account,Map<String,Object> values){
        String id=Database.id();
        db.insert("credential",Map.of("id",id,"account_id",account,"credential_type","WEBAUTHN","credential_name","Passkey","status","ACTIVE","registered_at",Database.now()));
        var row=new LinkedHashMap<>(values);row.put("credential_id",id);db.insert("webauthn_credential",row);
        db.audit(account,"credential.webauthn.registered","credential",id,"SUCCESS");return id;
    }
    public void used(String account,String id,long counter,boolean backedUp){
        db.jdbc.update("UPDATE webauthn_credential SET sign_count=?,backup_state=? WHERE credential_id=?",counter,backedUp,id);
        db.jdbc.update("UPDATE credential SET last_used_at=? WHERE id=?",Database.now(),id);
        db.audit(account,"auth.webauthn.verified","credential",id,"SUCCESS");
    }
    public void failed(String account,String transaction,String reason){db.audit(account,"auth.webauthn."+reason,"authentication_transaction",transaction,"FAILURE");}
}
