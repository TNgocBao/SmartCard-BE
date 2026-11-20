package vn.kma.idp.shared;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.http.converter.HttpMessageNotReadableException;
@RestControllerAdvice
public class Errors {
    private static final org.slf4j.Logger LOG=org.slf4j.LoggerFactory.getLogger(Errors.class);
    @ExceptionHandler(org.springframework.security.access.AccessDeniedException.class)
    ResponseEntity<ProblemDetail> denied(Exception e,HttpServletRequest request) {
        return ErrorResponses.response(403,"ACCESS_DENIED",request);
    }
    @ExceptionHandler(ApiException.class)
    ResponseEntity<ProblemDetail> known(ApiException e,HttpServletRequest request) {
        return ErrorResponses.response(e.status,e.code,request);
    }
    @ExceptionHandler(DataIntegrityViolationException.class)
    ResponseEntity<ProblemDetail> conflict(Exception e,HttpServletRequest request) {
        return ErrorResponses.response(409,"RESOURCE_CONFLICT",request);
    }
    @ExceptionHandler({
        MethodArgumentNotValidException.class,HttpMessageNotReadableException.class,IllegalArgumentException.class,java.time.DateTimeException.class,
        org.springframework.validation.BindException.class,
        org.springframework.web.bind.MissingServletRequestParameterException.class,org.springframework.web.bind.MissingRequestHeaderException.class,
        org.springframework.web.method.annotation.MethodArgumentTypeMismatchException.class,jakarta.validation.ConstraintViolationException.class
    }
    )
    ResponseEntity<ProblemDetail> invalid(Exception e,HttpServletRequest request) {
        var response=ErrorResponses.response(400,"VALIDATION_ERROR",request);
        if(e instanceof org.springframework.validation.BindException validation){
            var fields=validation.getBindingResult().getFieldErrors().stream().map(error->error.getField().split("[.\\[]",2)[0]).filter(field->field.matches("[A-Za-z][A-Za-z0-9_]{0,63}")).distinct().limit(5).toList();
            if(!fields.isEmpty())response.getBody().setProperty("fields",fields);
        }
        return response;
    }
    @ExceptionHandler(org.springframework.web.method.annotation.HandlerMethodValidationException.class)
    ResponseEntity<ProblemDetail> methodValidation(org.springframework.web.method.annotation.HandlerMethodValidationException e,HttpServletRequest request){
        return e.isForReturnValue()?ErrorResponses.response(500,"INTERNAL_ERROR",request):ErrorResponses.response(400,"VALIDATION_ERROR",request);
    }
    @ExceptionHandler({org.springframework.web.servlet.resource.NoResourceFoundException.class,org.springframework.web.servlet.NoHandlerFoundException.class})
    ResponseEntity<ProblemDetail> missing(Exception e,HttpServletRequest request){return ErrorResponses.response(404,"RESOURCE_NOT_FOUND",request);}
    @ExceptionHandler(org.springframework.web.HttpRequestMethodNotSupportedException.class)
    ResponseEntity<ProblemDetail> method(org.springframework.web.HttpRequestMethodNotSupportedException e,HttpServletRequest request){
        var result=ErrorResponses.response(405,"METHOD_NOT_ALLOWED",request);
        var headers=new HttpHeaders();headers.putAll(result.getHeaders());if(e.getSupportedHttpMethods()!=null)headers.setAllow(e.getSupportedHttpMethods());return new ResponseEntity<>(result.getBody(),headers,result.getStatusCode());
    }
    @ExceptionHandler(org.springframework.web.HttpMediaTypeNotSupportedException.class)
    ResponseEntity<ProblemDetail> media(Exception e,HttpServletRequest request){return ErrorResponses.response(415,"UNSUPPORTED_MEDIA_TYPE",request);}
    @ExceptionHandler(org.springframework.web.HttpMediaTypeNotAcceptableException.class)
    ResponseEntity<ProblemDetail> accept(Exception e,HttpServletRequest request){return ErrorResponses.response(406,"NOT_ACCEPTABLE",request);}
    @ExceptionHandler({org.springframework.dao.ConcurrencyFailureException.class,org.springframework.transaction.TransactionTimedOutException.class})
    ResponseEntity<ProblemDetail> busy(Exception e,HttpServletRequest request){return ErrorResponses.response(409,"CONCURRENT_UPDATE",request);}
    @ExceptionHandler({org.springframework.dao.DataAccessResourceFailureException.class,org.springframework.transaction.CannotCreateTransactionException.class})
    ResponseEntity<ProblemDetail> unavailable(Exception e,HttpServletRequest request){log(e,request);return ErrorResponses.response(503,"SERVICE_UNAVAILABLE",request);}
    @ExceptionHandler(Exception.class)
    ResponseEntity<ProblemDetail> unexpected(Exception e,HttpServletRequest request) {
        log(e,request);return ErrorResponses.response(500,"INTERNAL_ERROR",request);
    }
    private void log(Exception error,HttpServletRequest request){LOG.error("requestId={} exceptionType={}",ErrorResponses.requestId(request),error.getClass().getSimpleName());}
}
