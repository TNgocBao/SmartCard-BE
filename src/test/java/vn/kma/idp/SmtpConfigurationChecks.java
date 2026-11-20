package vn.kma.idp;

import org.springframework.mock.env.MockEnvironment;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import vn.kma.idp.audit.service.SmtpEmailDelivery;

/** Checks the actual SMTP adapter configuration without opening a network connection. */
public final class SmtpConfigurationChecks {
    private static int checks;
    private static void check(boolean value){checks++;if(!value)throw new AssertionError("SMTP configuration case "+checks);}
    private static JavaMailSenderImpl sender(MockEnvironment env)throws Exception{
        var adapter=new SmtpEmailDelivery(env);
        var field=SmtpEmailDelivery.class.getDeclaredField("sender");field.setAccessible(true);
        return (JavaMailSenderImpl)field.get(adapter);
    }
    public static void main(String[] args)throws Exception{
        var secure=sender(new MockEnvironment());
        check("true".equals(secure.getJavaMailProperties().getProperty("mail.smtp.auth")));
        check("true".equals(secure.getJavaMailProperties().getProperty("mail.smtp.starttls.enable")));
        check("true".equals(secure.getJavaMailProperties().getProperty("mail.smtp.starttls.required")));
        var local=new MockEnvironment();
        var yaml=new org.springframework.boot.env.YamlPropertySourceLoader().load("mailhog",new org.springframework.core.io.FileSystemResource("src/main/resources/application-mailhog.yml"));
        yaml.forEach(source->local.getPropertySources().addFirst(source));
        local.setActiveProfiles("local","mailhog");
        var lab=sender(local);
        check(local.getProperty("app.outbox.enabled",Boolean.class,false));
        check(lab.getHost().equals("127.0.0.1")&&lab.getPort()==1025);
        check("false".equals(lab.getJavaMailProperties().getProperty("mail.smtp.auth")));
        check("false".equals(lab.getJavaMailProperties().getProperty("mail.smtp.starttls.enable")));
        check("false".equals(lab.getJavaMailProperties().getProperty("mail.smtp.starttls.required")));
        for(String option:new String[]{"auth","starttls-enabled","starttls-required"}){
            var prod=new MockEnvironment().withProperty("app.smtp."+option,"false");prod.setActiveProfiles("prod");
            boolean rejected=false;try{sender(prod);}catch(IllegalArgumentException expected){rejected=true;}check(rejected);
        }
        local.setActiveProfiles("prod","mailhog");boolean rejected=false;try{sender(local);}catch(IllegalArgumentException expected){rejected=true;}check(rejected);
        boolean missing=false;try{sender(new MockEnvironment().withProperty("app.outbox.enabled","true"));}catch(IllegalArgumentException expected){missing=true;}check(missing);
        System.out.println("SMTP CONFIGURATION CHECKS PASSED: "+checks);
    }
}
