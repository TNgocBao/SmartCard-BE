package vn.kma.idp;
import org.junit.jupiter.api.Test;
class SmtpConfigurationTest {
    @Test void localMailhogAndProductionDefaults() throws Exception { SmtpConfigurationChecks.main(new String[0]); }
}
