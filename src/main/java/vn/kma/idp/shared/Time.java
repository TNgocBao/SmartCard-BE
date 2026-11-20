package vn.kma.idp.shared;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
public final class Time {
    private static final DateTimeFormatter FORMAT=new DateTimeFormatterBuilder().appendInstant(9).toFormatter();
    private Time(){
    }
    public static String format(Instant instant){
        return FORMAT.format(instant);
    }
}
