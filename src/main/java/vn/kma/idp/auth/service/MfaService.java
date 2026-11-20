package vn.kma.idp.auth.service;

import java.util.*;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.kma.idp.credential.service.TotpService;

@Service
public class MfaService {
    private final TotpService totp;private final AuthenticationTransactions transactions;private final ObjectProvider<AuthService> auth;private final boolean requireAdminMfa;private final vn.kma.idp.security.repository.AuthorizationRepository permissions;
    public MfaService(TotpService totp,AuthenticationTransactions transactions,ObjectProvider<AuthService> auth,org.springframework.core.env.Environment env,vn.kma.idp.security.repository.AuthorizationRepository permissions){this.totp=totp;this.transactions=transactions;this.auth=auth;this.permissions=permissions;requireAdminMfa=env.getProperty("app.mfa.require-admin",Boolean.class,env.matchesProfiles("prod"));}
    @Transactional public Map<String,Object> afterPrimary(String account,String method) {
        if(totp.enrolled(account)) return Map.of("requiresMfa",true,"method","TOTP","expiresIn",300,"transactionToken",transactions.create(account,"LOGIN_MFA",new HashMap<>(Map.of("primary",method)),300));
        if(requireAdminMfa && permissions.has(account,"idp.manage",null,null))return Map.of("requiresEnrollment",true,"enrollmentToken",transactions.create(account,"ENROLLMENT",new HashMap<>(Map.of("primary",method)),600),"expiresIn",600);
        return auth.getObject().createSession(account,method);
    }
    @Transactional public Map<String,Object> verify(String transactionToken,String code) {
        var tx=transactions.load(transactionToken,"LOGIN_MFA");
        if(!totp.verify(tx.account(),code)){transactions.failed(tx);return Map.of("error","AUTHENTICATION_FAILED");}
        transactions.consume(tx);return auth.getObject().createSession(tx.account(),tx.context().get("primary").toString()+"+TOTP");
    }
}
