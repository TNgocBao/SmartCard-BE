package vn.kma.idp.security;

import java.io.IOException;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import vn.kma.idp.shared.ErrorResponses;

/** Server-generated ID, never a reflected header or request payload. */
@Component
public class RequestTraceFilter extends OncePerRequestFilter {
    private static final org.slf4j.Logger LOG=org.slf4j.LoggerFactory.getLogger(RequestTraceFilter.class);
    @Override protected void doFilterInternal(HttpServletRequest request,HttpServletResponse response,FilterChain chain)throws ServletException,IOException {
        response.setHeader(ErrorResponses.REQUEST_ID_HEADER,ErrorResponses.requestId(request));
        try{chain.doFilter(request,response);}
        catch(RuntimeException failure){
            LOG.error("requestId={} exceptionType={}",ErrorResponses.requestId(request),failure.getClass().getSimpleName());
            ErrorResponses.write(request,response,500,"INTERNAL_ERROR");
        }
    }
}
