package vn.kma.idp.account.service;

import java.util.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.kma.idp.auth.service.*;
import vn.kma.idp.credential.service.TotpService;
import vn.kma.idp.shared.ApiException;

@Service
public class EnrollmentService {
    private final AuthenticationTransactions transactions;private final TotpService totp;private final RecoveryService recovery;private final AuthService auth;
    public EnrollmentService(AuthenticationTransactions transactions,TotpService totp,RecoveryService recovery,AuthService auth){this.transactions=transactions;this.totp=totp;this.recovery=recovery;this.auth=auth;}
    @Transactional public Map<String,Object> setup(String token) {
        var tx=transactions.load(token,"ENROLLMENT","RECOVERY_ENROLLMENT");
        if(tx.context().containsKey("credentialId"))throw new ApiException(409,"SETUP_ALREADY_STARTED");
        var result=totp.setup(tx.account());tx.context().put("credentialId",result.get("credentialId"));transactions.update(tx);return result;
    }
    @Transactional public Map<String,Object> verify(String token,String code) {
        var tx=transactions.load(token,"ENROLLMENT","RECOVERY_ENROLLMENT");
        if(!tx.context().containsKey("credentialId"))throw new ApiException(409,"SETUP_REQUIRED");
        if(!totp.confirm(tx.account(),tx.context().get("credentialId").toString(),code)){transactions.failed(tx);return Map.of("error","AUTHENTICATION_FAILED");}
        transactions.consume(tx);var result=new HashMap<>(auth.createSession(tx.account(),tx.context().get("primary").toString()+"+TOTP"));result.put("oneTimeRecoveryCodes",recovery.regenerate(tx.account()));return result;
    }
}
