package vn.kma.idp;
import java.security.*;
import java.util.*;
import java.nio.charset.StandardCharsets;
import com.fasterxml.jackson.databind.*;
import org.springframework.boot.DefaultApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
import org.springframework.mock.web.MockServletContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.security.web.FilterChainProxy;
import org.springframework.http.HttpMethod;
import vn.kma.idp.shared.Database;
/** Same checks run from Maven's integration test and the offline verification launcher. */
public final class IntegrationChecks {
    private MockMvc client;
    private String clientIp="127.0.0.1";
    private final ObjectMapper json=new ObjectMapper();
    private int checks;
    private final List<Map<String,Object>> results=new ArrayList<>();
    private vn.kma.idp.security.SecretBox secrets;
    private vn.kma.idp.auth.service.AuthService authentication;
    public static void main(String[] args)throws Exception {
        NetworkIsolation.install();
        try(var context=new AnnotationConfigWebApplicationContext()) {
            context.setServletContext(new MockServletContext());
            context.getEnvironment().setActiveProfiles("test");
            new ConfigDataApplicationContextInitializer().initialize(context);
            context.register(IdpApplication.class);
            context.refresh();
            context.getBean(ApplicationRunner.class).run(new DefaultApplicationArguments(new String[0]));
            var test=new IntegrationChecks();
            test.secrets=context.getBean(vn.kma.idp.security.SecretBox.class);
            test.authentication=context.getBean(vn.kma.idp.auth.service.AuthService.class);
            test.client=MockMvcBuilders.webAppContextSetup(context).addFilters(context.getBean(FilterChainProxy.class)).build();
            try {test.run(context.getBean(Database.class));}
            finally {test.writeReport();}
            System.out.println("INTEGRATION CHECKS PASSED: "+test.checks);
        }
    }
    private JsonNode request(String method,String path,Object payload,String token,int expected)throws Exception {
        var builder=MockMvcRequestBuilders.request(HttpMethod.valueOf(method),path).contentType("application/json");
        builder.with(req -> {req.setRemoteAddr(clientIp);return req;});
        if(token!=null)builder.header("Authorization","Bearer "+token);
        if(payload!=null)builder.content(json.writeValueAsString(payload));
        var response=client.perform(builder).andReturn().getResponse();
        results.add(Map.of("id",String.format("TC%03d",results.size()+1),"method",method,"path",path.replaceAll("[0-9a-fA-F]{8}-[0-9a-fA-F-]{27}","{id}"),"expected",expected,"actual",response.getStatus(),"passed",response.getStatus()==expected));
        if(response.getStatus()!=expected)throw new AssertionError(method+" "+path+" expected "+expected+", received "+response.getStatus()+" ("+json.readTree(response.getContentAsString().isBlank()?"{}":response.getContentAsString()).path("code").asText()+")");
        checks++;
        String body=response.getContentAsString(StandardCharsets.UTF_8);
        return json.readTree(body.isBlank()?"{}":body);
    }
    private void writeReport() throws Exception {
        var path=java.nio.file.Path.of("backend/target/test-results.json");
        if(!java.nio.file.Files.isDirectory(java.nio.file.Path.of("backend")))path=java.nio.file.Path.of("target/test-results.json");
        java.nio.file.Files.createDirectories(path.toAbsolutePath().getParent());
        json.writerWithDefaultPrettyPrinter().writeValue(path.toFile(),Map.of("recordedAt",java.time.Instant.now().toString(),"mode",System.getProperty("idp.verification-mode","maven"),"database","H2 in memory","transport","MockMvc + real Spring Security filters","totalChecks",checks,"cases",results,"allRecordedCasesPassed",results.stream().allMatch(r->Boolean.TRUE.equals(r.get("passed")))));
    }
    private void run(Database db)throws Exception {
        request("GET","/api/v1/status",null,null,200);
        request("GET","/api/v1/me",null,null,401);
        var login=request("POST","/api/v1/auth/password",Map.of("username","admin","password","Test-only-password-2026"),null,200);
        String admin=login.path("accessToken").asText();
        request("GET","/api/v1/me",null,"a".repeat(8193),401);
        String deepJson="{\"alg\":\"RS256\",\"nested\":"+"[".repeat(80)+"0"+"]".repeat(80)+"}";
        String deepToken=Base64.getUrlEncoder().withoutPadding().encodeToString(deepJson.getBytes(StandardCharsets.UTF_8))+".e30.AA";
        request("GET","/api/v1/me",null,deepToken,401);
        String duplicateHeader=Base64.getUrlEncoder().withoutPadding().encodeToString("{\"alg\":\"RS256\",\"alg\":\"none\"}".getBytes(StandardCharsets.UTF_8))+".e30.AA";
        request("GET","/api/v1/me",null,duplicateHeader,401);
        var catalog=request("GET","/api/v1/management/catalog",null,admin,200).path("data");
        if(catalog.size()!=41)throw new AssertionError("Expected 41 schema tables");
        checks++;
        for(var table:catalog)request("GET","/api/v1/management/resources/"+table.path("table").asText(),null,admin,200);
        var credentials=request("GET","/api/v1/management/resources/credential",null,admin,200);
        if(credentials.toString().contains("secret_hash"))throw new AssertionError("Credential hash leaked");
        checks++;
        var adminId=request("GET","/api/v1/me",null,admin,200).path("data").path("id").asText();
        var lastAdmin=request("POST","/api/v1/accounts/"+adminId+"/lock",Map.of(),admin,409);
        if(!lastAdmin.path("code").asText().equals("LAST_SUPERUSER_PROTECTED")) throw new AssertionError("Wrong last-admin protection path");
        checks++;
        var created=request("POST","/api/v1/accounts",Map.of("username","student01","displayName","Nguyễn Văn A","email","student@example.test"),admin,200);
        String account=created.path("accountId").asText();
        var generator=KeyPairGenerator.getInstance("RSA");
        generator.initialize(2048);
        var pair=generator.generateKeyPair();
        String card="00112233445566778899AABBCCDDEEFF";
        request("POST","/api/v1/accounts/"+account+"/smart-cards",Map.of("cardIdentifier",card,"publicKeySpki",Base64.getEncoder().encodeToString(pair.getPublic().getEncoded()),"transport","SIMULATOR"),admin,200);
        var challenge=request("POST","/api/v1/auth/smart-card/challenge",Map.of("username","student01","cardIdentifier",card),null,200);
        String id=challenge.path("challengeId").asText();
        var signer=Signature.getInstance("SHA256withRSA");
        signer.initSign(pair.getPrivate());
        signer.update(Base64.getDecoder().decode(challenge.path("payload").asText()));
        String signature=Base64.getEncoder().encodeToString(signer.sign());
        request("POST","/api/v1/auth/smart-card/verify",Map.of("challengeId",id,"signature",Base64.getEncoder().encodeToString(new byte[256])),null,401);
        var proof=request("POST","/api/v1/auth/smart-card/verify",Map.of("challengeId",id,"signature",signature),null,200);
        String user=proof.path("accessToken").asText(),refresh=proof.path("refreshToken").asText();
        request("POST","/api/v1/auth/smart-card/verify",Map.of("challengeId",id,"signature",signature),null,410);
        request("GET","/api/v1/me",null,user,200);
        request("GET","/api/v1/management/overview",null,user,403);
        request("DELETE","/api/v1/me/sessions/"+login.path("sessionId").asText(),null,user,404);
        var renewed=request("POST","/api/v1/auth/refresh",Map.of("refreshToken",refresh),null,200);
        request("POST","/api/v1/auth/refresh",Map.of("refreshToken",refresh),null,401);
        request("GET","/api/v1/me",null,renewed.path("accessToken").asText(),401);
        var nextChallenge=request("POST","/api/v1/auth/smart-card/challenge",Map.of("username","student01","cardIdentifier",card),null,200);
        db.jdbc.update("UPDATE smart_card_challenge SET expires_at='2000-01-01T00:00:00.000000000Z' WHERE id=?",nextChallenge.path("challengeId").asText());
        request("POST","/api/v1/auth/smart-card/verify",Map.of("challengeId",nextChallenge.path("challengeId").asText(),"signature",signature),null,410);
        request("POST","/api/v1/accounts/"+account+"/lock",Map.of(),admin,200);
        request("POST","/api/v1/auth/smart-card/challenge",Map.of("username","student01","cardIdentifier",card),null,423);
        request("POST","/api/v1/accounts/"+account+"/unlock",Map.of(),admin,200);
        String credentialId=db.one("SELECT id FROM credential WHERE account_id=? AND credential_type='SMART_CARD'",account).get("id").toString();
        var beforeExpiry=request("POST","/api/v1/auth/smart-card/challenge",Map.of("username","student01","cardIdentifier",card),null,200);
        db.jdbc.update("UPDATE credential SET expires_at='2000-01-01T00:00:00.000000000Z' WHERE id=?",credentialId);
        var expired=request("POST","/api/v1/auth/smart-card/verify",Map.of("challengeId",beforeExpiry.path("challengeId").asText(),"signature",signature),null,401);
        if(!expired.path("code").asText().equals("CREDENTIAL_EXPIRED")) throw new AssertionError("Expired credential was not checked");
        checks++;
        request("POST","/api/v1/auth/smart-card/challenge",Map.of("username","student01","cardIdentifier",card),null,401);
        db.jdbc.update("UPDATE credential SET expires_at=NULL WHERE id=?",credentialId);
        request("DELETE","/api/v1/accounts/credentials/"+credentialId,null,admin,200);
        request("POST","/api/v1/auth/smart-card/challenge",Map.of("username","student01","cardIdentifier",card),null,401);
        String passwordHash=new org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder(12).encode("Student-test-only-password");
        db.insert("credential",Map.of("account_id",account,"credential_type","PASSWORD","secret_hash",passwordHash,"status","ACTIVE"));
        String beforeLock=request("POST","/api/v1/auth/password",Map.of("username","student01","password","Student-test-only-password"),null,200).path("accessToken").asText();
        for(int i=0;i<5;i++) request("POST","/api/v1/auth/password",Map.of("username","student01","password","wrong-password"),null,401);
        request("GET","/api/v1/me",null,beforeLock,401);
        request("POST","/api/v1/accounts/"+account+"/unlock",Map.of(),admin,200);
        request("GET","/api/v1/me",null,beforeLock,401);
        String systemPermission=db.one("SELECT id FROM permission WHERE permission_code='idp.manage'").get("id").toString();
        request("PATCH","/api/v1/management/resources/permission/"+systemPermission,Map.of("is_active",false),admin,409);
        request("GET","/api/v1/management/resources/unknown_table",null,admin,404);
        request("POST","/api/v1/management/resources/credential",Map.of("secret_hash","forbidden"),admin,405);
        String appId=request("POST","/api/v1/management/resources/application",Map.of("client_id","test-public-client","client_type","PUBLIC","require_pkce",true),admin,200).path("id").asText();
        request("PATCH","/api/v1/management/resources/application/"+appId,Map.of("require_pkce",false),admin,400);
        request("POST","/api/v1/management/resources/client_redirect_uri",Map.of("application_id",appId,"redirect_uri","https://user:password@example.test/callback"),admin,400);
        request("POST","/api/v1/management/resources/client_redirect_uri",Map.of("application_id",appId,"redirect_uri","https://example.test/*"),admin,400);
        request("POST","/api/v1/management/resources/security_policy",Map.of("organization_id",UUID.randomUUID().toString(),"application_id",UUID.randomUUID().toString()),admin,409);
        checkSessionAuthorization(db,account);
        admin=checkMfaRecoveryOtp(db,admin,adminId);
        request("POST","/api/v1/logout",Map.of(),admin,200);
        request("GET","/api/v1/me",null,admin,401);
        checkSigningKeys(db,adminId);
    }
    private void check(String name,boolean passed){
        results.add(Map.of("id",String.format("TC%03d",results.size()+1),"method","ASSERT","path",name,"expected",true,"actual",passed,"passed",passed));
        if(!passed)throw new AssertionError(name);checks++;
    }
    private String checkMfaRecoveryOtp(Database db,String originalAdmin,String adminId) throws Exception {
        byte[] reference="12345678901234567890".getBytes(StandardCharsets.US_ASCII);
        long[] times={59L,1111111109L,1111111111L,1234567890L,2000000000L,20000000000L};
        String[] vectors={"94287082","07081804","14050471","89005924","69279037","65353130"};
        for(int i=0;i<times.length;i++)check("RFC6238 SHA1 vector "+times[i],vn.kma.idp.credential.service.TotpAlgorithm.code(reference,times[i]/30,8).equals(vectors[i]));
        String sealed=secrets.encrypt(reference,"test:record-a");check("AES-GCM round trip",Arrays.equals(reference,secrets.decrypt(sealed,"test:record-a")));
        boolean tamperRejected=false;try{secrets.decrypt(sealed,"test:record-b");}catch(IllegalStateException expected){tamperRejected=true;}check("AES-GCM AAD swap rejected",tamperRejected);
        request("POST","/api/v1/me/totp/setup",Map.of(),originalAdmin,403);
        var invite=request("POST","/api/v1/accounts/"+adminId+"/activation-invite",Map.of(),originalAdmin,202);
        String activation=invite.path("oneTimeActivationToken").asText();check("Activation hash only",db.one("SELECT token_hash FROM account_activation_token WHERE account_id=? ORDER BY created_at DESC LIMIT 1",adminId).get("token_hash").equals(vn.kma.idp.auth.service.AuthService.hash(activation)));
        var activated=request("POST","/api/v1/auth/activate",Map.of("token",activation),null,200);
        request("POST","/api/v1/auth/activate",Map.of("token",activation),null,410);
        String enrollment=activated.path("enrollmentToken").asText();
        request("GET","/api/v1/me",null,enrollment,401);
        var setup=request("POST","/api/v1/auth/enrollment/totp/setup",Map.of("token",enrollment),null,200);
        request("POST","/api/v1/auth/enrollment/totp/setup",Map.of("token",enrollment),null,409);
        byte[] key=decodeBase32(setup.path("secret").asText());long now=java.time.Instant.now().getEpochSecond();
        String current=vn.kma.idp.credential.service.TotpAlgorithm.code(key,now/30,6);
        String wrong="000000";while(vn.kma.idp.credential.service.TotpAlgorithm.matchingStep(key,wrong,now)>=0)wrong=String.format(java.util.Locale.ROOT,"%06d",Integer.parseInt(wrong)+1);
        request("POST","/api/v1/auth/enrollment/totp/verify",Map.of("enrollmentToken",enrollment,"code",wrong),null,401);
        var enrolled=request("POST","/api/v1/auth/enrollment/totp/verify",Map.of("enrollmentToken",enrollment,"code",current),null,200);
        check("Recovery codes shown once at enrollment",enrolled.path("oneTimeRecoveryCodes").size()==10);
        String setupAccess=enrolled.path("accessToken").asText();
        request("DELETE","/api/v1/me/totp",null,setupAccess,409);
        request("GET","/api/v1/me",null,originalAdmin,401);
        var pending=request("POST","/api/v1/auth/password",Map.of("username","admin","password","Test-only-password-2026"),null,200);
        check("Primary factor alone grants no access token",pending.path("requiresMfa").asBoolean() && !pending.has("accessToken"));
        String transaction=pending.path("transactionToken").asText();
        request("POST","/api/v1/auth/mfa/totp/verify",Map.of("transactionToken",transaction,"code",current),null,401);
        String next=vn.kma.idp.credential.service.TotpAlgorithm.code(key,now/30+1,6);
        var mfa=request("POST","/api/v1/auth/mfa/totp/verify",Map.of("transactionToken",transaction,"code",next),null,200);
        request("POST","/api/v1/auth/mfa/totp/verify",Map.of("transactionToken",transaction,"code",next),null,410);
        String admin=mfa.path("accessToken").asText();
        check("MFA session AAL2",com.nimbusds.jwt.SignedJWT.parse(admin).getJWTClaimsSet().getStringClaim("acr").equals("AAL2"));
        check("TOTP secret encrypted",db.one("SELECT encrypted_secret FROM totp_credential WHERE credential_id=?",setup.path("credentialId").asText()).get("encrypted_secret").toString().startsWith("v1."));
        var otp=request("POST","/api/v1/auth/otp/request",Map.of("username","student01"),null,200);
        String delivery=otp.path("deliveryId").asText();
        var mailbox=request("GET","/api/v1/local-mailbox/"+delivery,null,admin,200);
        String emailCode=mailbox.path("data").path("body").asText();
        check("Outbox payload encrypted",db.one("SELECT payload FROM outbox_event WHERE id=?",delivery).get("payload").toString().startsWith("v1."));
        String otpWrong=emailCode.equals("000000")?"000001":"000000";
        request("POST","/api/v1/auth/otp/verify",Map.of("transactionToken",otp.path("transactionToken").asText(),"code",otpWrong),null,401);
        request("POST","/api/v1/auth/otp/verify",Map.of("transactionToken",otp.path("transactionToken").asText(),"code",emailCode),null,200);
        request("POST","/api/v1/auth/otp/verify",Map.of("transactionToken",otp.path("transactionToken").asText(),"code",emailCode),null,410);
        request("POST","/api/v1/auth/otp/request",Map.of("username","student01"),null,429);
        request("POST","/api/v1/auth/otp/request",Map.of("username","does-not-exist"),null,200);
        var codes=request("POST","/api/v1/me/recovery-codes/regenerate",Map.of(),admin,200);
        request("POST","/api/v1/auth/recovery/verify",Map.of("username","admin","recoveryCode",enrolled.path("oneTimeRecoveryCodes").get(0).asText()),null,401);
        String recoveryCode=codes.path("oneTimeRecoveryCodes").get(0).asText();
        var recovery=request("POST","/api/v1/auth/recovery/verify",Map.of("username","admin","recoveryCode",recoveryCode),null,200);
        request("POST","/api/v1/auth/recovery/verify",Map.of("username","admin","recoveryCode",recoveryCode),null,401);
        request("GET","/api/v1/me",null,admin,401);
        String resetToken=recovery.path("enrollmentToken").asText();
        var resetSetup=request("POST","/api/v1/auth/enrollment/totp/setup",Map.of("token",resetToken),null,200);
        String resetCode=vn.kma.idp.credential.service.TotpAlgorithm.code(decodeBase32(resetSetup.path("secret").asText()),java.time.Instant.now().getEpochSecond()/30,6);
        var restored=request("POST","/api/v1/auth/enrollment/totp/verify",Map.of("enrollmentToken",resetToken,"code",resetCode),null,200);
        request("POST","/api/v1/auth/enrollment/totp/verify",Map.of("enrollmentToken",resetToken,"code",resetCode),null,410);
        String restoredAccess=restored.path("accessToken").asText();
        request("DELETE","/api/v1/me/totp",null,restoredAccess,409);
        new RbacFlowChecks(this::request,db,authentication,this::check).run(restoredAccess);
        checkWebAuthn(db,restoredAccess,adminId);
        checkOutbox(db);
        checkCredentialSelfService(db,restoredAccess);
        return restoredAccess;
    }
    private void checkWebAuthn(Database db,String admin,String adminId)throws Exception {
        clientIp="127.0.0.2";
        String registrationPath="/api/v1/me/webauthn/registration/";
        String loginPath="/api/v1/auth/webauthn/";
        var authenticator=new VirtualWebAuthnAuthenticator();
        var options=request("POST",registrationPath+"options",Map.of(),admin,200);
        request("POST",registrationPath+"verify",authenticator.registration(options,"https://evil.example",0x45),admin,401);
        request("POST",registrationPath+"verify",authenticator.registration(options,"http://localhost:8080",0x41),admin,410);
        options=request("POST",registrationPath+"options",Map.of(),admin,200);
        request("POST",registrationPath+"verify",authenticator.registration(options,"http://localhost:8080",0x41),admin,401);
        options=request("POST",registrationPath+"options",Map.of(),admin,200);
        var proof=authenticator.registration(options,"http://localhost:8080",0x45);
        String credential=request("POST",registrationPath+"verify",proof,admin,200).path("credentialId").asText();
        request("POST",registrationPath+"verify",proof,admin,410);
        check("WebAuthn COSE public key and attested data persisted",db.one("SELECT public_key_cose,attested_credential_data FROM webauthn_credential WHERE credential_id=?",credential).get("public_key_cose")!=null);
        var duplicateOptions=request("POST",registrationPath+"options",Map.of(),admin,200);
        request("POST",registrationPath+"verify",authenticator.registration(duplicateOptions,"http://localhost:8080",0x45),admin,409);
        for(int zero=0;zero<2;zero++){
            var zeroOptions=request("POST",loginPath+"options",Map.of("username","admin"),null,200);
            request("POST",loginPath+"verify",authenticator.assertion(zeroOptions,"http://localhost:8080","localhost",5,0),null,200);
        }
        check("WebAuthn permits zero counters across distinct valid ceremonies",true);
        var login=request("POST",loginPath+"options",Map.of("username","admin"),null,200);
        check("WebAuthn requests required UV",login.path("publicKey").path("userVerification").asText().equals("required"));
        request("POST",loginPath+"verify",authenticator.assertion(login,"https://evil.example","localhost",5,1),null,401);
        login=request("POST",loginPath+"options",Map.of("username","admin"),null,200);
        request("POST",loginPath+"verify",authenticator.assertion(login,"http://localhost:8080","evil.example",5,1),null,401);
        login=request("POST",loginPath+"options",Map.of("username","admin"),null,200);
        request("POST",loginPath+"verify",authenticator.assertion(login,"http://localhost:8080","localhost",1,1),null,401);
        login=request("POST",loginPath+"options",Map.of("username","admin"),null,200);
        var wrongHandle=authenticator.assertion(login,"http://localhost:8080","localhost",5,1);wrongHandle.put("userHandle","wrong-account");
        request("POST",loginPath+"verify",wrongHandle,null,401);
        login=request("POST",loginPath+"options",Map.of("username","admin"),null,200);
        var assertion=authenticator.assertion(login,"http://localhost:8080","localhost",5,1);
        var success=request("POST",loginPath+"verify",assertion,null,200);
        check("WebAuthn UV session has AAL2",com.nimbusds.jwt.SignedJWT.parse(success.path("accessToken").asText()).getJWTClaimsSet().getStringClaim("acr").equals("AAL2"));
        request("GET","/api/v1/me",null,success.path("accessToken").asText(),200);
        request("POST",loginPath+"verify",assertion,null,410);
        var clone=request("POST",loginPath+"options",Map.of("username","admin"),null,200);
        request("POST",loginPath+"verify",authenticator.assertion(clone,"http://localhost:8080","localhost",5,1),null,401);
        clone=request("POST",loginPath+"options",Map.of("username","admin"),null,200);
        var signedWrong=authenticator.assertion(clone,"http://localhost:8080","localhost",5,2);signedWrong.put("signature",Base64.getUrlEncoder().withoutPadding().encodeToString(new byte[70]));
        request("POST",loginPath+"verify",signedWrong,null,401);
        request("POST",loginPath+"options",Map.of("username","student01"),null,401);
        request("POST",loginPath+"options",Map.of("username","missing-account"),null,401);
        var expired=request("POST",loginPath+"options",Map.of("username","admin"),null,200);
        db.jdbc.update("UPDATE authentication_transaction SET expires_at=? WHERE transaction_token_hash=?",vn.kma.idp.shared.Time.format(java.time.Instant.now().minusSeconds(1)),vn.kma.idp.auth.service.AuthService.hash(expired.path("transactionToken").asText()));
        request("POST",loginPath+"verify",authenticator.assertion(expired,"http://localhost:8080","localhost",5,2),null,410);
        var attempts=request("POST",loginPath+"options",Map.of("username","admin"),null,200);
        var noUv=authenticator.assertion(attempts,"http://localhost:8080","localhost",1,2);
        request("POST",loginPath+"verify",noUv,null,401);
        request("POST",loginPath+"verify",authenticator.assertion(attempts,"http://localhost:8080","localhost",5,2),null,410);
        var concurrent=request("POST",loginPath+"options",Map.of("username","admin"),null,200);
        String concurrentBody=json.writeValueAsString(authenticator.assertion(concurrent,"http://localhost:8080","localhost",5,2));
        try(var executor=java.util.concurrent.Executors.newFixedThreadPool(2)){
            var barrier=new java.util.concurrent.CountDownLatch(1);
            java.util.concurrent.Callable<Integer> submit=()->{barrier.await();return client.perform(MockMvcRequestBuilders.post(loginPath+"verify").with(req->{req.setRemoteAddr("127.0.0.2");return req;}).contentType("application/json").content(concurrentBody)).andReturn().getResponse().getStatus();};
            var first=executor.submit(submit);var second=executor.submit(submit);barrier.countDown();
            var statuses=new ArrayList<>(List.of(first.get(15,java.util.concurrent.TimeUnit.SECONDS),second.get(15,java.util.concurrent.TimeUnit.SECONDS)));Collections.sort(statuses);
            check("Concurrent WebAuthn proof yields exactly one session and one consumed challenge",statuses.equals(List.of(200,410)));
        }
        var revoked=request("POST",loginPath+"options",Map.of("username","admin"),null,200);
        db.jdbc.update("UPDATE credential SET status='REVOKED' WHERE id=?",credential);
        request("POST",loginPath+"verify",authenticator.assertion(revoked,"http://localhost:8080","localhost",5,2),null,401);
        request("POST",loginPath+"options",Map.of("username","admin"),null,401);
        clientIp="127.0.0.3";
        for(int attempt=0;attempt<60;attempt++){
            var response=client.perform(MockMvcRequestBuilders.post(loginPath+"options").with(req->{req.setRemoteAddr(clientIp);return req;}).contentType("application/json").content("{\"username\":\"not-registered\"}")).andReturn().getResponse();
            if(response.getStatus()!=401)throw new AssertionError("Rate limit window rejected before configured threshold");
        }
        request("POST",loginPath+"options",Map.of("username","admin"),null,429);
        clientIp="127.0.0.1";
    }
    private void checkSigningKeys(Database db,String adminId)throws Exception {
        String path="/api/v1/signing-keys";
        String token=authentication.createSession(adminId,"WEBAUTHN_UV").get("accessToken").toString();
        String oldKid=com.nimbusds.jwt.SignedJWT.parse(token).getHeader().getKeyID();
        String oldId=db.one("SELECT id FROM signing_key WHERE kid=?",oldKid).get("id").toString();
        var metadata=request("GET",path,null,token,200);
        check("Signing key metadata excludes private references",!metadata.toString().contains("private_key_ref")&&!metadata.toString().contains("public_jwk"));
        String member=db.one("SELECT id FROM account WHERE username='student01'").get("id").toString();
        String memberToken=authentication.createSession(member,"WEBAUTHN_UV").get("accessToken").toString();
        request("GET",path,null,memberToken,403);
        String aal1=authentication.createSession(adminId,"PASSWORD").get("accessToken").toString();
        request("POST",path+"/prepare",Map.of("alias","GENERATE"),aal1,403);
        request("POST",path+"/prepare",Map.of("alias","../arbitrary-path"),token,400);
        String next=request("POST",path+"/prepare",Map.of("alias","GENERATE"),token,201).path("id").asText();
        var jwks=request("GET","/oauth2/jwks",null,null,200);
        check("NEXT key pre-published alongside active key",jwks.path("keys").size()==2);
        check("JWKS contains public parameters only",!jwks.toString().contains("\"d\"")&&!jwks.toString().contains("\"p\"")&&!jwks.toString().contains("\"q\""));
        request("POST",path+"/rotate",Map.of("nextKeyId",next,"overlapSeconds",30),token,400);
        var rotated=request("POST",path+"/rotate",Map.of("nextKeyId",next,"overlapSeconds",600),token,201);
        check("Rotation changes active kid",!rotated.path("kid").asText().equals(oldKid));
        request("GET","/api/v1/me",null,token,200);
        String fresh=authentication.createSession(adminId,"WEBAUTHN_UV").get("accessToken").toString();
        check("New tokens use rotated signing key",com.nimbusds.jwt.SignedJWT.parse(fresh).getHeader().getKeyID().equals(rotated.path("kid").asText()));
        request("POST",path+"/rotate",Map.of("nextKeyId",next,"overlapSeconds",600),fresh,409);
        request("DELETE",path+"/"+next,null,fresh,409);
        db.jdbc.update("UPDATE signing_key SET expires_at=? WHERE id=?",vn.kma.idp.shared.Time.format(java.time.Instant.now().minusSeconds(1)),oldId);
        request("GET","/api/v1/me",null,token,401);
        request("GET","/api/v1/me",null,fresh,200);
        check("Retired key disappears after overlap expiry",request("GET","/oauth2/jwks",null,null,200).path("keys").size()==1);
        request("DELETE",path+"/"+oldId,null,fresh,200);
        request("GET","/api/v1/me",null,fresh,401);
        check("Emergency retired-key revocation revokes every active session",db.jdbc.queryForObject("SELECT COUNT(*) FROM idp_session WHERE status='ACTIVE'",Integer.class)==0);
        String last=authentication.createSession(adminId,"WEBAUTHN_UV").get("accessToken").toString();
        request("GET","/api/v1/me",null,last,200);
        request("POST","/api/v1/logout",Map.of(),last,200);
    }
    private void checkCredentialSelfService(Database db,String admin)throws Exception {
        String account=db.one("SELECT id FROM account WHERE username='student01'").get("id").toString();
        String password=db.one("SELECT id FROM credential WHERE account_id=? AND credential_type='PASSWORD' AND status='ACTIVE'",account).get("id").toString();
        String hash=db.one("SELECT secret_hash FROM credential WHERE id=?",password).get("secret_hash").toString();
        String extra=db.insert("credential",Map.of("account_id",account,"credential_type","PASSWORD","secret_hash",hash,"status","ACTIVE","registered_at",Database.now()));
        // Pre-authenticated fixture isolates ownership and revocation; cryptographic AAL2 is tested separately above.
        String member=authentication.createSession(account,"WEBAUTHN_UV").get("accessToken").toString();
        var own=request("GET","/api/v1/me/credentials",null,member,200);
        check("Own credential API excludes all secrets and public-key payloads",!own.toString().contains("secret_hash")&&!own.toString().contains("encrypted_secret")&&!own.toString().contains("public_key"));
        String foreign=db.one("SELECT id FROM credential WHERE account_id<>? AND status='ACTIVE' LIMIT 1",account).get("id").toString();
        request("DELETE","/api/v1/me/credentials/"+foreign,null,member,404);
        String aal1=authentication.createSession(account,"PASSWORD").get("accessToken").toString();
        request("DELETE","/api/v1/me/credentials/"+extra,null,aal1,403);
        request("DELETE","/api/v1/me/credentials/"+extra,null,member,200);
        request("GET","/api/v1/me",null,member,401);
        request("GET","/api/v1/me",null,aal1,401);
        check("Self revoke updates credential state",db.one("SELECT status FROM credential WHERE id=?",extra).get("status").equals("REVOKED"));
        String renewed=authentication.createSession(account,"WEBAUTHN_UV").get("accessToken").toString();
        request("DELETE","/api/v1/me/credentials/"+password,null,renewed,409);
        request("GET","/api/v1/me",null,admin,200);
    }
    private void checkOutbox(Database db)throws Exception {
        var store=new vn.kma.idp.audit.repository.OutboxStore(db);
        var service=new vn.kma.idp.audit.service.OutboxService(store,secrets,json);
        db.jdbc.update("UPDATE outbox_event SET status='SENT' WHERE status='NEW'");
        var env=new org.springframework.mock.env.MockEnvironment().withProperty("app.outbox.enabled","true");
        var deliveries=new java.util.concurrent.atomic.AtomicInteger();
        var worker=new vn.kma.idp.audit.service.OutboxWorker(store,service,env,(to,subject,body)->{if(!to.equals("outbox-test@example.test")||!body.equals("private-test-body"))throw new AssertionError("Delivery content changed");deliveries.incrementAndGet();});
        String success=service.email("outbox-test@example.test","Test delivery","private-test-body");
        worker.dispatch();worker.dispatch();
        check("Outbox successful delivery is not dispatched twice",deliveries.get()==1&&store.get(success).get("status").equals("SENT"));
        var failing=new vn.kma.idp.audit.service.OutboxWorker(store,service,env,(to,subject,body)->{throw new IllegalStateException("Simulated unavailable SMTP");});
        String retry=service.email("outbox-test@example.test","Retry","private-test-body");
        failing.dispatch();
        check("Outbox failure schedules bounded retry",store.get(retry).get("status").equals("NEW")&&((Number)store.get(retry).get("attempts")).intValue()==1);
        check("Outbox cannot claim before backoff elapsed",store.claim(retry)==0);
        for(int attempt=2;attempt<=5;attempt++){
            db.jdbc.update("UPDATE outbox_event SET next_attempt_at=? WHERE id=?",vn.kma.idp.shared.Time.format(java.time.Instant.now().minusSeconds(1)),retry);failing.dispatch();
        }
        check("Outbox becomes DEAD after five delivery failures",store.get(retry).get("status").equals("DEAD")&&((Number)store.get(retry).get("attempts")).intValue()==5);
        check("DEAD outbox item cannot be claimed",store.claim(retry)==0);
        String lease=service.email("outbox-test@example.test","Crash lease","private-test-body");
        check("Only one worker claims an outbox item",store.claim(lease)==1&&store.claim(lease)==0);
        db.jdbc.update("UPDATE outbox_event SET next_attempt_at=? WHERE id=?",vn.kma.idp.shared.Time.format(java.time.Instant.now().minusSeconds(1)),lease);store.recoverLeases();
        check("Expired worker lease can be recovered",store.get(lease).get("status").equals("NEW"));
        store.claim(lease);db.jdbc.update("UPDATE outbox_event SET attempts=5,next_attempt_at=? WHERE id=?",vn.kma.idp.shared.Time.format(java.time.Instant.now().minusSeconds(1)),lease);store.recoverLeases();
        check("Expired final lease does not cause unlimited retries",store.get(lease).get("status").equals("DEAD"));
        String fenced=service.email("outbox-test@example.test","Fenced lease","private-test-body");
        store.claim(fenced,0);db.jdbc.update("UPDATE outbox_event SET next_attempt_at=? WHERE id=?",vn.kma.idp.shared.Time.format(java.time.Instant.now().minusSeconds(1)),fenced);store.recoverLeases();
        check("Recovered outbox item requires current claim version",store.claim(fenced,0)==0&&store.claim(fenced,1)==1);
        check("Stale worker cannot acknowledge or fail another claim",store.sent(fenced,1)==0&&store.failed(fenced,1)==0&&store.get(fenced).get("status").equals("PROCESSING"));
        check("Current outbox worker can acknowledge its own claim",store.sent(fenced,2)==1);
        check("Outbox stores encrypted notification bodies only",!store.get(success).get("payload").toString().contains("private-test-body"));
    }
    private void checkSessionAuthorization(Database db,String account)throws Exception {
        var grantRepo=new vn.kma.idp.security.repository.AuthorizationRepository(db);
        var login=request("POST","/api/v1/auth/password",Map.of("username","student01","password","Student-test-only-password"),null,200);
        String token=login.path("accessToken").asText(),sid=login.path("sessionId").asText();
        String role=db.insert("role",Map.of("role_code","TEST_DYNAMIC_ROLE","is_active",true));
        String permission=db.insert("permission",Map.of("permission_code","report.read","is_active",true));
        db.insert("role_permission",Map.of("role_id",role,"permission_id",permission));
        String assignment=db.insert("account_role_assignment",Map.of("account_id",account,"role_id",role,"status","ACTIVE"));
        check("Live grant takes effect with existing access token",request("GET","/api/v1/me",null,token,200).path("permissions").toString().contains("report.read"));
        db.jdbc.update("UPDATE account_role_assignment SET status='REVOKED' WHERE id=?",assignment);
        check("Revoked grant disappears without waiting for JWT expiry",!request("GET","/api/v1/me",null,token,200).path("permissions").toString().contains("report.read"));
        String org=db.insert("organization",Map.of("org_code","SCOPE_A","status","ACTIVE"));
        db.jdbc.update("UPDATE account_role_assignment SET status='ACTIVE',organization_id=? WHERE id=?",org,assignment);
        check("Organization grant cannot become a global grant",!grantRepo.has(account,"report.read",null,null));
        check("Organization grant matches its own scope",grantRepo.has(account,"report.read",org,null));
        check("Organization grant cannot cross organization",!grantRepo.has(account,"report.read",Database.id(),null));
        db.jdbc.update("UPDATE account_role_assignment SET valid_until=? WHERE id=?",vn.kma.idp.shared.Time.format(java.time.Instant.now().minusSeconds(1)),assignment);
        check("Expired scoped assignment is denied",!grantRepo.has(account,"report.read",org,null));
        db.jdbc.update("UPDATE idp_session SET last_activity_at=? WHERE id=?",vn.kma.idp.shared.Time.format(java.time.Instant.now().minusSeconds(1801)),sid);
        request("GET","/api/v1/me",null,token,401);
        request("POST","/api/v1/auth/refresh",Map.of("refreshToken",login.path("refreshToken").asText()),null,401);
        var absolute=request("POST","/api/v1/auth/password",Map.of("username","student01","password","Student-test-only-password"),null,200);
        db.jdbc.update("UPDATE idp_session SET expires_at=? WHERE id=?",vn.kma.idp.shared.Time.format(java.time.Instant.now().minusSeconds(1)),absolute.path("sessionId").asText());
        request("POST","/api/v1/auth/refresh",Map.of("refreshToken",absolute.path("refreshToken").asText()),null,401);
        db.jdbc.update("UPDATE credential SET status='REVOKED' WHERE account_id=? AND credential_type<>'TOTP'",account);
        boolean protectedLast=false;try{new vn.kma.idp.credential.repository.TotpStore(db).guardDisable(account);}catch(vn.kma.idp.shared.ApiException e){protectedLast=e.code.equals("LAST_AUTHENTICATOR_PROTECTED");}
        check("Last authenticator cannot be removed",protectedLast);
        db.jdbc.update("UPDATE credential SET status='ACTIVE' WHERE account_id=? AND credential_type='PASSWORD'",account);
    }
    private byte[] decodeBase32(String value){
        var output=new java.io.ByteArrayOutputStream();int buffer=0,bits=0;
        for(char c:value.toCharArray()){int digit="ABCDEFGHIJKLMNOPQRSTUVWXYZ234567".indexOf(c);if(digit<0)throw new IllegalArgumentException();buffer=(buffer<<5)|digit;bits+=5;if(bits>=8){bits-=8;output.write((buffer>>bits)&255);}}
        return output.toByteArray();
    }
}
