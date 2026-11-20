package vn.kma.idp.shared;
public class ApiException extends RuntimeException {
    public final int status;
    public final String code;
    public ApiException(int status, String code) {
        super(code);
        this.status=status;
        this.code=code;
    }
    public static ApiException denied() {
        return new ApiException(403,"ACCESS_DENIED");
    }
}
