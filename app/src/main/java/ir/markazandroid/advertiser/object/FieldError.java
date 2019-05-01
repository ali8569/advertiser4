package ir.markazandroid.advertiser.object;


import java.io.Serializable;

import ir.markazandroid.advertiser.network.JSONParser.annotations.JSON;


/**
 * Created by Ali on 28/11/2017.
 */
public class FieldError implements Serializable {
    private String field;
    private String message;
    private String errorCode;

    public FieldError(String field, String message, String errorCode) {
        this.field = field;
        this.message = message;
        this.errorCode = errorCode;
    }

    public FieldError() {
    }

    @JSON
    public String getField() {
        return field;
    }

    public void setField(String field) {
        this.field = field;
    }

    @JSON
    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @JSON
    public String getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }
}
