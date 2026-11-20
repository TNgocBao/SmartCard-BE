package vn.kma.idp.shared;

import java.io.IOException;
import java.util.UUID;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.*;
import org.springframework.http.*;

/** One sanitized error shape for controllers and security filters. Never serialize exception messages. */
public final class ErrorResponses {
    public static final String REQUEST_ID_ATTRIBUTE=ErrorResponses.class.getName()+".requestId";
    public static final String REQUEST_ID_HEADER="X-Request-Id";
    private static final ObjectMapper JSON=new ObjectMapper();
    private ErrorResponses(){}
    public static String requestId(HttpServletRequest request){
        Object existing=request.getAttribute(REQUEST_ID_ATTRIBUTE);
        if(existing instanceof String value&&value.matches("[0-9a-f-]{36}"))return value;
        String value=UUID.randomUUID().toString();request.setAttribute(REQUEST_ID_ATTRIBUTE,value);return value;
    }
    public static ProblemDetail problem(int status,String code,HttpServletRequest request){
        int safeStatus=status>=400&&status<=599?status:500;
        String safeCode=code!=null&&code.matches("[A-Z0-9_]{1,80}")?code:"INTERNAL_ERROR";
        var problem=ProblemDetail.forStatus(safeStatus);
        problem.setDetail(switch(safeStatus){
            case 400 -> "Check the request fields.";
            case 401 -> "Authentication is required or has expired.";
            case 403 -> "This action is not permitted.";
            case 404 -> "The requested resource was not found.";
            case 405 -> "This HTTP method is not supported.";
            case 406 -> "The requested response format is not supported.";
            case 409 -> "The request conflicts with the current state.";
            case 410 -> "This request has expired or was already used.";
            case 415 -> "The request content type is not supported.";
            case 423 -> "The account is locked or inactive.";
            case 429 -> "Too many requests. Try again shortly.";
            case 503 -> "The service is temporarily unavailable.";
            default -> "The request could not be completed.";
        });
        problem.setProperty("code",safeCode);problem.setProperty("requestId",requestId(request));return problem;
    }
    public static ResponseEntity<ProblemDetail> response(int status,String code,HttpServletRequest request){
        var problem=problem(status,code,request);
        return ResponseEntity.status(problem.getStatus()).contentType(MediaType.APPLICATION_PROBLEM_JSON).header(REQUEST_ID_HEADER,requestId(request)).header(HttpHeaders.CACHE_CONTROL,"no-store").body(problem);
    }
    public static void write(HttpServletRequest request,HttpServletResponse response,int status,String code)throws IOException {
        if(response.isCommitted())return;
        var problem=problem(status,code,request);
        response.setStatus(problem.getStatus());response.setContentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE);response.setCharacterEncoding("UTF-8");response.setHeader(REQUEST_ID_HEADER,requestId(request));response.setHeader(HttpHeaders.CACHE_CONTROL,"no-store");
        // Explicit shape avoids relying on MVC's ProblemDetail Jackson mixin in a servlet filter.
        var body=new java.util.LinkedHashMap<String,Object>();body.put("type","about:blank");body.put("title",problem.getTitle());body.put("status",problem.getStatus());body.put("detail",problem.getDetail());body.putAll(problem.getProperties());
        JSON.writeValue(response.getOutputStream(),body);
    }
}
