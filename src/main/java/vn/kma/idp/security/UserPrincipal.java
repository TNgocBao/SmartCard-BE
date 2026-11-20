package vn.kma.idp.security;
import java.util.List;
public record UserPrincipal(String accountId,String sessionId,List<String> permissions) {
}
