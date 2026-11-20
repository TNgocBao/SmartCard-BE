package vn.kma.idp.security;

import java.time.Instant;
import org.springframework.stereotype.Service;
import vn.kma.idp.security.repository.AuthorizationRepository;
import vn.kma.idp.shared.ApiException;

@Service
public class AccessService {
    private final AuthorizationRepository repository;
    public AccessService(AuthorizationRepository repository){this.repository=repository;}
    public boolean has(UserPrincipal user,String permission){return has(user,permission,null,null);}
    public boolean has(UserPrincipal user,String permission,String org,String app){return repository.has(user.accountId(),"idp.manage",null,null)||repository.has(user.accountId(),permission,org,app);}
    public void require(UserPrincipal user,String permission){if(!has(user,permission))throw ApiException.denied();}
    public void requireAccount(UserPrincipal user,String permission,String target){require(user,permission);if(repository.has(target,"idp.manage",null,null)&&!repository.has(user.accountId(),"idp.manage",null,null))throw new ApiException(403,"PRIVILEGED_ACCOUNT_PROTECTED");}
    public void recentAal2(UserPrincipal user) {
        var session=repository.session(user.accountId(),user.sessionId());
        if(!"AAL2".equals(session.get("authentication_level")) || Instant.parse(session.get("authenticated_at").toString()).isBefore(Instant.now().minusSeconds(300))) throw new ApiException(403,"STEP_UP_REQUIRED");
    }
}
