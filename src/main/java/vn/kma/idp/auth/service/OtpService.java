package vn.kma.idp.auth.service;

import java.security.SecureRandom;
import java.util.*;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.kma.idp.auth.repository.OtpStore;
import vn.kma.idp.audit.service.OutboxService;
import vn.kma.idp.shared.*;

@Service
public class OtpService {
    private final OtpStore repository;private final AuthenticationTransactions transactions;private final OutboxService outbox;private final BCryptPasswordEncoder hashes;private final MfaService mfa;private final SecureRandom random=new SecureRandom();
    public OtpService(OtpStore repository,AuthenticationTransactions transactions,OutboxService outbox,BCryptPasswordEncoder hashes,MfaService mfa){this.repository=repository;this.transactions=transactions;this.outbox=outbox;this.hashes=hashes;this.mfa=mfa;}
    @Transactional public Map<String,Object> request(String username) {
        Map<String,Object> account;
        try{account=repository.account(username);}catch(ApiException e){return dummy();}
        String id=account.get("id").toString(),email=Objects.toString(account.get("email"),"");
        if(!"ACTIVE".equals(account.get("status")) || Boolean.TRUE.equals(account.get("is_locked")) || email.isBlank())return dummy();
        if(repository.coolingDown(id))throw new ApiException(429,"OTP_COOLDOWN");
        String code=String.format(java.util.Locale.ROOT,"%06d",random.nextInt(1000000));
        String token=transactions.create(id,"OTP_LOGIN",new HashMap<>(Map.of("username",username,"otpHash",hashes.encode(code))),120);
        String delivery=outbox.email(email,"KMA IdP — one-time sign-in code",code);
        return Map.of("transactionToken",token,"deliveryId",delivery,"expiresIn",120);
    }
    @Transactional public Map<String,Object> verify(String token,String code) {
        var tx=transactions.load(token,"OTP_LOGIN");
        if(!hashes.matches(code,tx.context().get("otpHash").toString())){transactions.failed(tx);repository.failure(tx.account());return Map.of("error","AUTHENTICATION_FAILED");}
        var account=repository.account(tx.context().get("username").toString());
        if(!"ACTIVE".equals(account.get("status")) || Boolean.TRUE.equals(account.get("is_locked")))throw new ApiException(423,"ACCOUNT_LOCKED");
        transactions.consume(tx);return mfa.afterPrimary(tx.account(),"OTP");
    }
    private Map<String,Object> dummy(){byte[] randomToken=new byte[32];random.nextBytes(randomToken);return Map.of("transactionToken",Base64.getUrlEncoder().withoutPadding().encodeToString(randomToken),"deliveryId",Database.id(),"expiresIn",120);}
}
