package vn.kma.idp.management.service;
import vn.kma.idp.management.repository.ManagementRepository;
import java.util.*;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.kma.idp.shared.*;
import vn.kma.idp.account.service.AccountService;
@Service
public class ManagementService {
    private final ManagementRepository repository;
    private final AccountService accounts;
    private final List<Map<String,Object>> catalog;
    private final Map<String,Set<String>> tables=new HashMap<>();
    private final Set<String> editable=Set.of("organization","person","org_membership","external_identity","org_role","org_role_assignment","role","permission","role_permission","account_role_assignment","application","client_redirect_uri","client_logout_uri","oidc_scope","application_scope","client_grant_type","security_policy");
    private boolean secret(String c) {
        return c.contains("secret") || c.contains("hash") || c.equals("challenge") || c.equals("challenge_payload") || c.equals("private_key_ref") || c.equals("code_verifier") || c.equals("metadata") || c.equals("payload") || c.equals("oidc_context_json");
    }
    @SuppressWarnings("unchecked") public ManagementService(ManagementRepository repository,AccountService accounts,ObjectMapper mapper) throws Exception {
        this.repository=repository;
        this.accounts=accounts;
        try(var stream=new ClassPathResource("schema-catalog.json").getInputStream()) {
            catalog=mapper.readValue(stream,new TypeReference<>() {
            }
            );
        }
        for(var item:catalog) {
            String table=item.get("table").toString();
            var columns=new LinkedHashSet<>((List<String>)item.get("columns"));
            columns.removeIf(this::secret);
            tables.put(table,columns);
            item.put("columns",columns);
            item.put("editable",editable.contains(table));
        }
    }
    public Map<String,Object> page(String table,int page,int size) {
        var columns=tables.get(table);
        if(columns==null) throw new ApiException(404,"RESOURCE_NOT_FOUND");
        if(page<0 || page>100000 || size<1 || size>100) throw new ApiException(400,"INVALID_PAGE");
        return Map.of("data",repository.page(table,columns,page,size),"page",page,"size",size,"totalElements",repository.count(table));
    }
    public List<Map<String,Object>> catalog() {
        return catalog;
    }
    @Transactional public String save(String actor,String table,String id,Map<String,Object> input) {
        input=new LinkedHashMap<>(input);
        if(!editable.contains(table)) throw new ApiException(405,"READ_ONLY_RESOURCE");
        if(input.isEmpty() || input.size()>30 || !tables.get(table).containsAll(input.keySet()) || input.containsKey("id")) throw new ApiException(400,"INVALID_FIELDS");
        for(var value:input.values()) if(value!=null && !(value instanceof String || value instanceof Number || value instanceof Boolean)) throw new ApiException(400,"INVALID_VALUE");
        for(var value:input.values()) if(value instanceof String s && s.length()>4096) throw new ApiException(400,"VALUE_TOO_LONG");
        if("role".equals(table) && id!=null && Boolean.TRUE.equals(repository.require("role",id).get("is_system"))) throw new ApiException(409,"SYSTEM_ROLE_PROTECTED");
        if("permission".equals(table) && id!=null && "idp.manage".equals(repository.require("permission",id).get("permission_code"))) throw new ApiException(409,"SYSTEM_PERMISSION_PROTECTED");
        if("role".equals(table) && (Boolean.TRUE.equals(input.get("is_system")) || "SUPER_ADMIN".equals(input.get("role_code")))) throw new ApiException(403,"SYSTEM_ROLE_PROTECTED");
        if("account_role_assignment".equals(table)) {
            if(id!=null) accounts.protectLastAdmin(repository.require("account_role_assignment",id).get("account_id").toString());
            String role=Objects.toString(input.get("role_id"),"");
            if(!role.isBlank() && "SUPER_ADMIN".equals(repository.require("role",role).get("role_code"))) throw new ApiException(403,"SYSTEM_ROLE_PROTECTED");
        }
        if("role_permission".equals(table)) {
            String role=Objects.toString(input.get("role_id"),"");
            if(id!=null) role=repository.require("role_permission",id).get("role_id").toString();
            if(!role.isBlank() && Boolean.TRUE.equals(repository.require("role",role).get("is_system"))) throw new ApiException(409,"SYSTEM_ROLE_PROTECTED");
        }
        if("application".equals(table)) {
            var resulting=id==null?new HashMap<String,Object>():new HashMap<>(repository.require(table,id));
            resulting.putAll(input);
            if("PUBLIC".equals(resulting.get("client_type")) && !Boolean.TRUE.equals(resulting.get("require_pkce"))) throw new ApiException(400,"PUBLIC_CLIENT_REQUIRES_PKCE");
        }
        for(var entry:input.entrySet()) {
            String field=entry.getKey();
            if(!(entry.getValue() instanceof String value) || value.isBlank()) continue;
            if(field.equals("date_of_birth") || (table.equals("org_membership") && Set.of("joined_at","ended_at").contains(field))) {
                entry.setValue(java.time.LocalDate.parse(value).toString());
            } else if(field.endsWith("_at") || Set.of("valid_from","valid_until","locked_until","not_before").contains(field)) {
                entry.setValue(Time.format(java.time.Instant.parse(value)));
            }
        }
        for(String field:List.of("redirect_uri","post_logout_redirect_uri")) if(input.get(field)!=null) {
            var uri=java.net.URI.create(input.get(field).toString());
            if(uri.getFragment()!=null || uri.getRawUserInfo()!=null || uri.toASCIIString().contains("*") || uri.getPort()>65535 || uri.getHost()==null || !("https".equals(uri.getScheme()) || ("http".equals(uri.getScheme()) && Set.of("localhost","127.0.0.1").contains(uri.getHost())))) throw new ApiException(400,"INVALID_REDIRECT_URI");
        }
        String result=id;
        if(id==null) result=repository.create(table,input);
        else {
            repository.require(table,id);
            repository.update(table,id,input);
        }
        if(Set.of("account_role_assignment","role_permission","role","permission").contains(table)) {
            // Permission changes take effect immediately; force fresh authorization claims.
            repository.revokeOtherSessions(actor);
        }
        repository.audit(actor,id==null?"resource.create":"resource.update",table,result);
        return result;
    }
    public Map<String,Object> overview() {
        var result=new LinkedHashMap<String,Object>();
        for(String table:List.of("person","identity","account","credential","application","idp_session","audit_log")) result.put(table,repository.count(table));
        result.put("tables",tables.size());
        return result;
    }
}
