package vn.kma.idp.audit.service;

import org.springframework.core.env.Environment;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.stereotype.Component;

/** SMTP configuration and network I/O; the worker owns retries, not this adapter. */
@Component
public class SmtpEmailDelivery implements EmailDelivery {
    private final JavaMailSenderImpl sender;
    private final String from;
    public SmtpEmailDelivery(Environment env){
        sender=new JavaMailSenderImpl();sender.setHost(env.getProperty("app.smtp.host",""));sender.setPort(env.getProperty("app.smtp.port",Integer.class,587));
        sender.setUsername(env.getProperty("app.smtp.username"));sender.setPassword(env.getProperty("app.smtp.password"));
        from=env.getProperty("app.smtp.from","");
        boolean auth=env.getProperty("app.smtp.auth",Boolean.class,true);
        boolean startTls=env.getProperty("app.smtp.starttls-enabled",Boolean.class,true);
        boolean requireTls=env.getProperty("app.smtp.starttls-required",Boolean.class,true);
        if(env.matchesProfiles("prod") && (!auth || !startTls || !requireTls))throw new IllegalArgumentException("Production SMTP requires authentication and STARTTLS");
        var properties=sender.getJavaMailProperties();properties.setProperty("mail.smtp.auth",Boolean.toString(auth));properties.setProperty("mail.smtp.starttls.enable",Boolean.toString(startTls));properties.setProperty("mail.smtp.starttls.required",Boolean.toString(requireTls));
        properties.setProperty("mail.smtp.ssl.checkserveridentity","true");
        properties.setProperty("mail.smtp.connectiontimeout","5000");properties.setProperty("mail.smtp.timeout","5000");properties.setProperty("mail.smtp.writetimeout","5000");
        if(env.getProperty("app.outbox.enabled",Boolean.class,false)&&(sender.getHost().isBlank()||from.isBlank()))throw new IllegalArgumentException("SMTP host and sender required when outbox is enabled");
    }
    public void send(String destination,String subject,String body){
        var message=new SimpleMailMessage();message.setFrom(from);message.setTo(destination);message.setSubject(subject);message.setText(body);sender.send(message);
    }
}
