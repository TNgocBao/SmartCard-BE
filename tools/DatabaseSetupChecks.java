/** Pure JDK validation checks; no JDBC connection, application or telemetry. */
public final class DatabaseSetupChecks {
    private static int checks;
    public static void main(String[] args) {
        host("root@localhost","localhost");host("root@172.17.0.1","172.17.0.1");host("root@::1","::1");host("root@host-name.local","host-name.local");
        rejectHost("root@%");rejectHost("root@172.17.%");rejectHost("root@host_name");rejectHost("root@x' OR 1=1");rejectHost("root");rejectHost("root@");
        InitializeDatabase.requireApplicationCredential("kma_idp_a1b2c3d4","x".repeat(43));checks++;
        rejectCredential("root","x".repeat(43));rejectCredential("kma_idp_a1b2c3d4'","x".repeat(43));rejectCredential("kma_idp_a1b2c3d4","short");rejectCredential("kma_idp_a1b2c3d4","x".repeat(42)+"'");
        System.out.println("DATABASE SETUP VALIDATION CHECKS PASS: "+checks+" (no database/network)");
    }
    private static void host(String input,String expected){if(!InitializeDatabase.clientHost(input).equals(expected))throw new AssertionError("Client host parsing");checks++;}
    private static void rejectHost(String input){try{InitializeDatabase.clientHost(input);}catch(IllegalArgumentException expected){checks++;return;}throw new AssertionError("Unsafe host accepted");}
    private static void rejectCredential(String user,String password){try{InitializeDatabase.requireApplicationCredential(user,password);}catch(IllegalArgumentException expected){checks++;return;}throw new AssertionError("Unsafe application credential accepted");}
}
