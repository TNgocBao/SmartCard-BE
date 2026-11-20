import java.nio.file.*;
import java.security.SecureRandom;
import java.sql.*;
import java.util.*;
/** Creates a dedicated MySQL schema/user; credentials are persisted using Windows DPAPI only. */
public class InitializeDatabase {
    public static void main(String[] args) throws Exception {
        try { configure(Arrays.asList(args).contains("--repair")); }
        catch(SQLException failure) {
            System.err.println("MySQL setup failed (SQLState="+failure.getSQLState()+", code="+failure.getErrorCode()+"). Check connectivity, credentials and the MySQL client host grant. No secret was logged.");
            System.exit(1);
        }
    }
    static String clientHost(String connectedUser) {
        int separator=connectedUser.lastIndexOf('@');
        String host=separator<0?"":connectedUser.substring(separator+1);
        if(!host.matches("[A-Za-z0-9.:-]{1,255}"))throw new IllegalArgumentException("Unexpected MySQL client host; no grant was changed.");
        return host;
    }
    static void requireApplicationCredential(String user,String password) {
        if(user==null||!user.matches("kma_idp_[0-9a-f]{8}")||password==null||!password.matches("[A-Za-z0-9_-]{32,128}"))throw new IllegalArgumentException("Unexpected application credential; refusing to change another account.");
    }
    private static void configure(boolean repair) throws Exception {
        String rootPassword=System.getenv("KMA_MYSQL_ROOT_PASSWORD");
        if(rootPassword==null)throw new IllegalStateException("Run setup-database.ps1");
        String file=System.getenv("KMA_CREDENTIAL_FILE");
        boolean existing=Files.exists(Path.of(file));
        if(existing&&!repair) {
            System.out.println("Connection already configured. Existing credentials retained.");
            return;
        }
        byte[] secret=new byte[32];
        new SecureRandom().nextBytes(secret);
        String appPassword=existing?System.getenv("KMA_APP_DB_PASSWORD"):Base64.getUrlEncoder().withoutPadding().encodeToString(secret);
        String user=existing?System.getenv("KMA_APP_DB_USER"):"kma_idp_"+UUID.randomUUID().toString().substring(0,8);
        Arrays.fill(secret,(byte)0);requireApplicationCredential(user,appPassword);
        String client;
        String url="jdbc:mysql://127.0.0.1:3306/?serverTimezone=UTC&connectTimeout=5000&socketTimeout=10000";
        try(Connection c=DriverManager.getConnection(url,"root",rootPassword)) {
            try(var statement=c.createStatement();var result=statement.executeQuery("SELECT USER()")) { result.next();client=clientHost(result.getString(1)); }
            try(var check=c.prepareStatement("SELECT COUNT(*) FROM information_schema.tables WHERE table_schema='kma_idp'")) {
                var rows=check.executeQuery();
                rows.next();
                int tables=rows.getInt(1);
                if(tables>0) {
                    try(var known=c.prepareStatement("SELECT COUNT(*) FROM information_schema.tables WHERE table_schema='kma_idp' AND table_name='flyway_schema_history'")) {
                        var r=known.executeQuery();
                        r.next();
                        if(r.getInt(1)==0)throw new IllegalStateException("kma_idp contains unrelated tables; choose another schema manually. Nothing was changed.");
                    }
                }
            }
            try(Statement s=c.createStatement()) {
                s.execute("CREATE DATABASE IF NOT EXISTS kma_idp CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci");
                // Both generated values contain only [A-Za-z0-9_-]; never interpolate arbitrary user input.
                s.execute("CREATE USER IF NOT EXISTS '"+user+"'@'"+client+"' IDENTIFIED BY '"+appPassword+"'");
                s.execute("GRANT SELECT,INSERT,UPDATE,DELETE,CREATE,ALTER,INDEX,REFERENCES ON kma_idp.* TO '"+user+"'@'"+client+"'");
            }
        }
        try(Connection check=DriverManager.getConnection(url.replace("3306/?","3306/kma_idp?"),user,appPassword);
            var statement=check.createStatement();var result=statement.executeQuery("SELECT 1")) {
            if(!result.next()||result.getInt(1)!=1)throw new SQLException("Application connection verification failed");
        }
        if(existing) { System.out.println("KMA IdP application login verified; schema/credentials retained; exact MySQL client host: "+client);return; }
        Files.createDirectories(Path.of(file).getParent());
        String command="$secret=ConvertTo-SecureString $env:KMA_APP_DB_PASSWORD -AsPlainText -Force; [PSCredential]::new($env:KMA_APP_DB_USER,$secret) | Export-Clixml -LiteralPath $env:KMA_CREDENTIAL_FILE";
        ProcessBuilder p=new ProcessBuilder("powershell.exe","-NoProfile","-NonInteractive","-Command",command);
        p.environment().remove("KMA_MYSQL_ROOT_PASSWORD");
        p.environment().put("KMA_APP_DB_PASSWORD",appPassword);
        p.environment().put("KMA_APP_DB_USER",user);
        if(p.inheritIO().start().waitFor()!=0)throw new IllegalStateException("Database created but Windows credential protection failed; rotate the generated user before retrying.");
        System.out.println("Connection KMA IdP Local configured: 127.0.0.1:3306 / kma_idp / "+user+". Password protected by Windows DPAPI.");
    }
}
