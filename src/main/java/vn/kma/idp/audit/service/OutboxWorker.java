package vn.kma.idp.audit.service;

import org.springframework.core.env.Environment;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import vn.kma.idp.audit.repository.OutboxStore;

/** At-least-once delivery with leases, bounded batch and retry/dead-letter states. No message bodies in logs. */
@Component
public class OutboxWorker {
    private final OutboxStore repository;private final OutboxService service;private final Environment env;private final EmailDelivery delivery;
    public OutboxWorker(OutboxStore repository,OutboxService service,Environment env,EmailDelivery delivery){this.repository=repository;this.service=service;this.env=env;this.delivery=delivery;}
    @Scheduled(fixedDelayString="${app.outbox.poll-ms:5000}")
    public void dispatch(){
        if(!env.getProperty("app.outbox.enabled",Boolean.class,false))return;
        repository.recoverLeases();
        for(var item:repository.due()) {
            String id=item.get("id").toString();int attempt=((Number)item.get("attempts")).intValue()+1;if(repository.claim(id,attempt-1)!=1)continue;
            try {
                var data=service.decrypt(id,item.get("payload").toString());
                if(!item.get("event_type").equals("EMAIL"))throw new IllegalStateException("Unsupported event type");
                delivery.send(data.get("to").toString(),data.get("subject").toString(),data.get("body").toString());repository.sent(id,attempt);
            } catch(Exception e){repository.failed(id,attempt);}
        }
    }
}
