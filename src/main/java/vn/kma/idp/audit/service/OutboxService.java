package vn.kma.idp.audit.service;

import java.util.*;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import vn.kma.idp.audit.repository.OutboxStore;
import vn.kma.idp.security.SecretBox;
import vn.kma.idp.shared.Database;

@Service
public class OutboxService {
    private final OutboxStore repository;private final SecretBox box;private final ObjectMapper json;
    public OutboxService(OutboxStore repository,SecretBox box,ObjectMapper json){this.repository=repository;this.box=box;this.json=json;}
    public String email(String destination,String subject,String body){
        String id=Database.id();
        try {return repository.create(id,"EMAIL",box.encrypt(json.writeValueAsBytes(Map.of("to",destination,"subject",subject,"body",body)),"outbox:"+id));}
        catch(Exception e){throw new IllegalStateException("Unable to queue notification",e);}
    }
    public Map<String,Object> decrypt(String id,String payload){try{return json.readValue(box.decrypt(payload,"outbox:"+id),new TypeReference<>(){});}catch(Exception e){throw new IllegalStateException("Outbox integrity failure",e);}}
}
