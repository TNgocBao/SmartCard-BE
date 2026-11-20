package vn.kma.idp.security;
import java.io.IOException;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import vn.kma.idp.auth.repository.AuthRepository;
import vn.kma.idp.shared.ApiException;
@Component
public class SessionGuard extends OncePerRequestFilter {
    private final AuthRepository repository;
    private final JwtService jwt;
    public SessionGuard(AuthRepository repository,JwtService jwt) {
        this.repository=repository;
        this.jwt=jwt;
    }
    @Override protected void doFilterInternal(HttpServletRequest req,HttpServletResponse res,FilterChain chain) throws IOException,ServletException {
        String header=req.getHeader("Authorization");
        if(header!=null && header.startsWith("Bearer ")) {
            try {
                var principal=jwt.verify(header.substring(7));
                if(!repository.isSessionActive(principal.sessionId(),principal.accountId())) throw new ApiException(401,"SESSION_REVOKED");
                // Database grants are authoritative, including when the actor changes their own role.
                principal=new UserPrincipal(principal.accountId(),principal.sessionId(),repository.findEffectiveGlobalPermissions(principal.accountId(),vn.kma.idp.shared.Database.now(),vn.kma.idp.shared.Database.now()));
                var authentication=new UsernamePasswordAuthenticationToken(principal,null,principal.permissions().stream().map(SimpleGrantedAuthority::new).toList());
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
            catch(ApiException e) {
                vn.kma.idp.shared.ErrorResponses.write(req,res,401,"INVALID_OR_REVOKED_SESSION");
                return;
            }
            catch(org.springframework.dao.DataAccessException|org.springframework.transaction.TransactionException e){vn.kma.idp.shared.ErrorResponses.write(req,res,503,"SERVICE_UNAVAILABLE");return;}
        }
        chain.doFilter(req,res);
    }
}
