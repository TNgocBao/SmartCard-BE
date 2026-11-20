package vn.kma.idp.credential.service;

import java.time.Instant;
import java.security.SecureRandom;
import java.util.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.kma.idp.credential.repository.TotpStore;
import vn.kma.idp.security.*;
import vn.kma.idp.shared.*;

@Service
public class TotpService {
    private final TotpStore repository;private final SecretBox secrets;
    public TotpService(TotpStore repository,SecretBox secrets){this.repository=repository;this.secrets=secrets;}
    public boolean enrolled(String account){return repository.active(account)!=null;}
    @Transactional public Map<String,Object> setup(String account) {
        byte[] key=new byte[20];new SecureRandom().nextBytes(key);String id=Database.id();
        repository.create(account,id,secrets.encrypt(key,"totp:"+id));
        String base32=TotpAlgorithm.base32(key);Arrays.fill(key,(byte)0);
        String label=java.net.URLEncoder.encode("KMA IdP:"+repository.username(account),java.nio.charset.StandardCharsets.UTF_8).replace("+","%20");
        return Map.of("credentialId",id,"secret",base32,"otpauthUri","otpauth://totp/"+label+"?secret="+base32+"&issuer=KMA%20IdP&algorithm=SHA1&digits=6&period=30");
    }
    @Transactional public boolean confirm(String account,String credential,String code) {
        var row=repository.pending(account,credential);
        if(!verifyAndConsume(row,code))return false;
        repository.activate(account,credential);repository.activateAccount(account);repository.audit(account,"credential.totp.activated",credential);return true;
    }
    public boolean verify(String account,String code) {var row=repository.active(account);return row!=null && verifyAndConsume(row,code);}
    private boolean verifyAndConsume(Map<String,Object> row,String code) {
        String id=row.get("credential_id").toString();byte[] clear=secrets.decrypt(row.get("encrypted_secret").toString(),"totp:"+id);
        try {long step=TotpAlgorithm.matchingStep(clear,code,Instant.now().getEpochSecond());return step>=0 && repository.consumeStep(id,step)==1;}
        finally{Arrays.fill(clear,(byte)0);}
    }
    @Transactional public void disable(String account){repository.guardDisable(account);repository.revoke(account);repository.audit(account,"credential.totp.disabled",account);}
}
