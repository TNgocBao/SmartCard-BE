package vn.kma.idp.auth.repository;
import java.util.*;
import org.springframework.stereotype.Repository;
import vn.kma.idp.shared.*;

@Repository
public class OtpStore {
    private final Database db;
    public OtpStore(Database db){this.db=db;}
    public Map<String,Object> account(String username){return db.one("SELECT a.*,p.email FROM account a JOIN identity i ON i.id=a.identity_id JOIN person p ON p.id=i.person_id WHERE a.username=? FOR UPDATE",username);}
    public boolean coolingDown(String account){return db.jdbc.queryForObject("SELECT COUNT(*) FROM authentication_transaction WHERE account_id=? AND required_acr='OTP_LOGIN' AND created_at>?",Integer.class,account,Time.format(java.time.Instant.now().minusSeconds(30)))>0;}
    public void failure(String account){db.audit(account,"auth.otp.failed","account",account,"FAILURE");}
}
