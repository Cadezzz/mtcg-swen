package http;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

public enum HttpStatus {
    OK(200, "OK"),
    CREATED(201, "CREATED"),
    BAD_REQUEST(400, "Bad Request"),
    UNAUTHORIZED(401, "Access Token Is Missing Or Invalid"),
    FORBIDDEN(403, "No Acquired Rights On These Elements"),
    NOT_FOUND(404, "Not Found"),
    INTERNAL_SERVER_ERROR(500, "Internal Server Error"),
    CONFLICT(409, "Conflict");

    @Getter
    @Setter(AccessLevel.PRIVATE)
    private int code;
    @Getter
    @Setter(AccessLevel.PRIVATE)
    private String message;

    HttpStatus(int code, String message) {
        setCode(code);
        setMessage(message);
    }
}
