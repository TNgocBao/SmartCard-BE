package vn.kma.idp;

import java.nio.file.*;
import java.util.*;
import com.fasterxml.jackson.databind.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.hibernate.validator.messageinterpolation.ParameterMessageInterpolator;
import org.springframework.http.*;
import org.springframework.mock.web.*;
import org.springframework.test.web.servlet.*;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import org.springframework.web.bind.annotation.*;
import vn.kma.idp.security.*;
import vn.kma.idp.shared.*;

/** Isolated MVC/servlet tests only. No IdpApplication, Spring Boot, database, SMTP or external connections. */
public final class ExceptionHandlerChecks {
    private final ObjectMapper json=new ObjectMapper();
    private final List<Map<String,Object>> cases=new ArrayList<>();
    private MockMvc mvc;
    private static final String PRIVATE="private-test-secret SQL jdbc:mysql://private-host/db password=secret";
    public static void main(String[] args)throws Exception {
        NetworkIsolation.install();
        var checks=new ExceptionHandlerChecks();
        try{checks.run();}finally{checks.report();}
        System.out.println("ISOLATED EXCEPTION CHECKS PASSED: "+checks.cases.size());
    }
    private void check(String name,boolean success){cases.add(Map.of("case",name,"passed",success));if(!success)throw new AssertionError(name);}
    private void run()throws Exception {
        try(var validator=new LocalValidatorFactoryBean()){
            validator.setMessageInterpolator(new ParameterMessageInterpolator());validator.afterPropertiesSet();
            mvc=MockMvcBuilders.standaloneSetup(new Routes()).setControllerAdvice(new Errors()).setValidator(validator).addFilters(new RequestTraceFilter()).build();
            var validation=request("POST","/probe/body","{}","application/json",400,"VALIDATION_ERROR");
            check("Validation identifies field names without rejected values",json.readTree(validation.getContentAsByteArray()).path("fields").toString().equals("[\"name\"]"));
            request("POST","/probe/body","{broken","application/json",400,"VALIDATION_ERROR");
            request("POST","/probe/body","{\"name\":{}}","application/json",400,"VALIDATION_ERROR");
            request("POST","/probe/body","{\"name\":\"Valid\"}","application/json",200,null);
            request("GET","/probe/integer",null,null,400,"VALIDATION_ERROR");
            request("GET","/probe/integer?value=not-a-number",null,null,400,"VALIDATION_ERROR");
            request("GET","/probe/header",null,null,400,"VALIDATION_ERROR");
            request("POST","/probe/body","<xml/>","application/xml",415,"UNSUPPORTED_MEDIA_TYPE");
            var method=request("DELETE","/probe/body",null,null,405,"METHOD_NOT_ALLOWED");
            check("405 retains the allowed HTTP method",method.getHeader("Allow")!=null&&method.getHeader("Allow").contains("POST"));
            request("GET","/probe/known",null,null,423,"ACCOUNT_LOCKED");
            request("GET","/probe/conflict",null,null,409,"RESOURCE_CONFLICT");
            request("GET","/probe/concurrent",null,null,409,"CONCURRENT_UPDATE");
            request("GET","/probe/unavailable",null,null,503,"SERVICE_UNAVAILABLE");
            request("GET","/probe/server",null,null,500,"INTERNAL_ERROR");
            request("GET","/probe/denied",null,null,403,"ACCESS_DENIED");
            request("GET","/probe/missing",null,null,404,"RESOURCE_NOT_FOUND");
            var accept=mvc.perform(MockMvcRequestBuilders.get("/probe/integer?value=1").accept(MediaType.TEXT_XML)).andReturn().getResponse();
            check("Unsupported response format returns structured 406",accept.getStatus()==406&&json.readTree(accept.getContentAsByteArray()).path("code").asText().equals("NOT_ACCEPTABLE"));
            var filterRequest=new MockHttpServletRequest();var filterResponse=new MockHttpServletResponse();
            new RequestTraceFilter().doFilter(filterRequest,filterResponse,(r,s)->{throw new IllegalStateException(PRIVATE);});
            check("Unexpected filter failure is sanitized JSON 500",filterResponse.getStatus()==500&&json.readTree(filterResponse.getContentAsByteArray()).path("code").asText().equals("INTERNAL_ERROR")&&!filterResponse.getContentAsString().contains("private-test-secret"));
            var malformed=ErrorResponses.problem(200,PRIVATE,new MockHttpServletRequest());
            check("Invalid error status and code cannot expose arbitrary text",malformed.getStatus()==500&&malformed.getProperties().get("code").equals("INTERNAL_ERROR"));
            for(int status:List.of(401,403,429)){
                var req=new MockHttpServletRequest();var res=new MockHttpServletResponse();
                ErrorResponses.write(req,res,status,status==401?"AUTHENTICATION_REQUIRED":status==403?"ACCESS_DENIED":"RATE_LIMITED");
                JsonNode body=json.readTree(res.getContentAsByteArray());
                check("Filter error "+status+" has the same code/requestId schema",body.path("status").asInt()==status&&!body.path("code").asText().isBlank()&&body.path("requestId").asText().equals(res.getHeader(ErrorResponses.REQUEST_ID_HEADER)));
            }
            rateLimit();
        }
    }
    private MockHttpServletResponse request(String method,String path,String payload,String contentType,int expected,String code)throws Exception {
        var builder=MockMvcRequestBuilders.request(HttpMethod.valueOf(method),path).header(ErrorResponses.REQUEST_ID_HEADER,PRIVATE);
        if(contentType!=null)builder.contentType(contentType);if(payload!=null)builder.content(payload);
        var result=mvc.perform(builder).andReturn().getResponse();
        check(method+" "+path+" returns "+expected,result.getStatus()==expected);
        String body=result.getContentAsString(java.nio.charset.StandardCharsets.UTF_8);
        check(method+" "+path+" does not expose exception details",!body.contains("private-test-secret")&&!body.contains("jdbc:")&&!body.contains("password="));
        if(code!=null){
            var error=json.readTree(body);
            check(method+" "+path+" returns stable code and request ID",error.path("code").asText().equals(code)&&error.path("requestId").asText().equals(result.getHeader(ErrorResponses.REQUEST_ID_HEADER))&&error.path("requestId").asText().matches("[0-9a-f-]{36}")&&"no-store".equals(result.getHeader("Cache-Control")));
        }
        return result;
    }
    private void rateLimit()throws Exception {
        var limiter=new RateLimitFilter();var trace=new RequestTraceFilter();
        for(int i=0;i<61;i++){
            var req=new MockHttpServletRequest("POST","/api/v1/auth/example");req.setRemoteAddr("192.0.2.77");var response=new MockHttpServletResponse();
            trace.doFilter(req,response,(r,s)->limiter.doFilter(r,s,new MockFilterChain()));
            if(i==60){
                var body=json.readTree(response.getContentAsByteArray());
                check("Rate limit returns structured 429 with Retry-After",response.getStatus()==429&&"60".equals(response.getHeader("Retry-After"))&&body.path("code").asText().equals("RATE_LIMITED")&&body.path("requestId").asText().equals(response.getHeader(ErrorResponses.REQUEST_ID_HEADER)));
            }
        }
    }
    private void report()throws Exception {Path path=Path.of("target/exception-handler-results.json");Files.createDirectories(path.getParent());json.writerWithDefaultPrettyPrinter().writeValue(path.toFile(),Map.of("recordedAt",java.time.Instant.now().toString(),"scope","Standalone MockMvc/servlet only; no Spring Boot, database, SMTP, or sockets","cases",cases));}
    public record Body(@NotBlank String name){}
    @RestController public static final class Routes {
        @PostMapping(value="/probe/body",consumes="application/json",produces="application/json") public Object body(@Valid @RequestBody Body body){return Map.of("ok",true);}
        @GetMapping(value="/probe/integer",produces="application/json") public Object integer(@RequestParam int value){return Map.of("value",value);}
        @GetMapping("/probe/header") public Object header(@RequestHeader("X-Required") String value){return Map.of("ok",true);}
        @GetMapping("/probe/known") public Object known(){throw new ApiException(423,"ACCOUNT_LOCKED");}
        @GetMapping("/probe/conflict") public Object conflict(){throw new org.springframework.dao.DataIntegrityViolationException(PRIVATE);}
        @GetMapping("/probe/concurrent") public Object concurrent(){throw new org.springframework.dao.CannotAcquireLockException(PRIVATE);}
        @GetMapping("/probe/unavailable") public Object unavailable(){throw new org.springframework.dao.DataAccessResourceFailureException(PRIVATE);}
        @GetMapping("/probe/server") public Object server(){throw new IllegalStateException(PRIVATE);}
        @GetMapping("/probe/denied") public Object denied(){throw new org.springframework.security.access.AccessDeniedException(PRIVATE);}
    }
}
