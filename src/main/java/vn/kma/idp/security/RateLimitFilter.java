package vn.kma.idp.security;
import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
/** Single-node fixed-window limiter; replace with atomic shared store before horizontal deployment. */
@Component
public class RateLimitFilter extends OncePerRequestFilter {
    private record Window(long minute,int count) {
    }
    private final ConcurrentHashMap<String,Window> windows=new ConcurrentHashMap<>();
    @Override protected void doFilterInternal(HttpServletRequest req,HttpServletResponse res,FilterChain chain) throws IOException,ServletException {
        if(req.getRequestURI().startsWith("/api/v1/auth/")) {
            long minute=System.currentTimeMillis()/60000;
            if(windows.size()>10000) windows.entrySet().removeIf(e -> e.getValue().minute()<minute);
            if(windows.size()>10000 && !windows.containsKey(req.getRemoteAddr())) {
                res.setHeader("Retry-After","60");
                vn.kma.idp.shared.ErrorResponses.write(req,res,429,"RATE_LIMITED");
                return;
            }
            var window=windows.compute(req.getRemoteAddr(),(k,v) -> v==null || v.minute()!=minute ? new Window(minute,1):new Window(minute,v.count()+1));
            if(window.count()>60) {
                res.setHeader("Retry-After","60");
                vn.kma.idp.shared.ErrorResponses.write(req,res,429,"RATE_LIMITED");
                return;
            }
        }
        chain.doFilter(req,res);
    }
}
