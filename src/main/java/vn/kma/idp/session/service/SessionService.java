package vn.kma.idp.session.service;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.kma.idp.auth.service.AuthService;
import vn.kma.idp.session.repository.SessionQueryRepository;
import vn.kma.idp.security.UserPrincipal;
@Service
public class SessionService {
    private final AuthService auth;
    private final SessionQueryRepository repository;
    public SessionService(AuthService auth,SessionQueryRepository repository){
        this.auth=auth;
        this.repository=repository;
    }
    public Object profile(UserPrincipal user){
        return Map.of("data",repository.profile(user.accountId()),"permissions",user.permissions());
    }
    public Object ownSessions(UserPrincipal user){
        return Map.of("data",repository.ownSessions(user.accountId()));
    }
    @Transactional public void revoke(UserPrincipal user,String sid){
        repository.requireOwnership(user.accountId(),sid);
        auth.revokeSession(sid);
        repository.audit(user.accountId(),"session.revoke",sid);
    }
    @Transactional public void logout(UserPrincipal user){
        revoke(user,user.sessionId());
    }
}
