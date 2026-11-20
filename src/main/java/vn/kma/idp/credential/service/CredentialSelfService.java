package vn.kma.idp.credential.service;

import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.kma.idp.credential.repository.*;
import vn.kma.idp.account.service.AccountService;
import vn.kma.idp.security.*;
import vn.kma.idp.shared.ApiException;

@Service
public class CredentialSelfService {
    private final CredentialQueryRepository repository;
    private final AccountService accounts;
    private final TotpStore totp;
    public CredentialSelfService(CredentialQueryRepository repository,AccountService accounts,TotpStore totp){this.repository=repository;this.accounts=accounts;this.totp=totp;}
    public Object list(UserPrincipal user){return Map.of("data",repository.own(user.accountId()));}
    @Transactional public void revoke(UserPrincipal user,String id){
        repository.lockSuperuserRole();repository.lockAccount(user.accountId());
        var credential=repository.requireOwn(user.accountId(),id);
        if(!"ACTIVE".equals(credential.get("status")))throw new ApiException(409,"CREDENTIAL_NOT_ACTIVE");
        if("TOTP".equals(credential.get("credential_type")))totp.guardDisable(user.accountId());
        else if(repository.otherPrimary(user.accountId(),id)==0)throw new ApiException(409,"LAST_AUTHENTICATOR_PROTECTED");
        accounts.revokeCredential(user.accountId(),id);
    }
}
