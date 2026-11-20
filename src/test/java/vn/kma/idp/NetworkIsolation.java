package vn.kma.idp;

import java.security.Permission;

/** Java 21 verification only: prohibit all sockets, including telemetry and DNS, before Spring starts. */
@SuppressWarnings("removal")
public final class NetworkIsolation {
    private NetworkIsolation(){}
    public static synchronized void install(){
        SecurityManager previous=System.getSecurityManager();
        if(previous instanceof OfflineOnly)return;
        System.setSecurityManager(new OfflineOnly(previous));
    }
    private static final class OfflineOnly extends SecurityManager {
        private final SecurityManager previous;
        OfflineOnly(SecurityManager previous){this.previous=previous;}
        @Override public void checkPermission(Permission permission){if(previous!=null)previous.checkPermission(permission);}
        @Override public void checkPermission(Permission permission,Object context){if(previous!=null)previous.checkPermission(permission,context);}
        @Override public void checkConnect(String host,int port){throw new SecurityException("Network prohibited in offline verification");}
        @Override public void checkConnect(String host,int port,Object context){throw new SecurityException("Network prohibited in offline verification");}
        @Override public void checkListen(int port){throw new SecurityException("Listening prohibited in offline verification");}
        @Override public void checkAccept(String host,int port){throw new SecurityException("Network prohibited in offline verification");}
    }
}
