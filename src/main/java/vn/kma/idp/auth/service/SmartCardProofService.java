package vn.kma.idp.auth.service;

import java.util.*;
import org.springframework.stereotype.Service;
import vn.kma.idp.auth.repository.AuthRepository;
import vn.kma.idp.shared.*;

/** Records proof outcomes after the verification transaction commits or rolls back. */
@Service
public class SmartCardProofService {
    private final AuthService auth;
    private final AuthRepository repository;
    public SmartCardProofService(AuthService auth,AuthRepository repository){this.auth=auth;this.repository=repository;}
    public Map<String,Object> verify(String challenge,String signature) {
        Map<String,Object> result;
        try { result=auth.verify(challenge,signature); }
        catch(ApiException e){record(challenge,"FAILURE",e.getMessage());throw e;}
        record(challenge,"SUCCESS",null);return result;
    }
    private void record(String challenge,String result,String reason) {
        var rows=repository.findChallengeWithCard(challenge);
        if(rows.isEmpty())return;
        var c=rows.getFirst();
        var event=new HashMap<String,Object>();
        event.put("account_id",c.get("account_id"));event.put("credential_id",c.get("credential_id"));
        event.put("authentication_method","SMART_CARD");event.put("event_type","CARD_PROOF");
        event.put("result",result);event.put("created_at",Database.now());
        if(reason!=null)event.put("failure_reason_code",reason);
        repository.insert("authentication_event",event);
    }
}
