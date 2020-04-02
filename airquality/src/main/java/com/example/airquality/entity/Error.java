package com.example.airquality.entity;

import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
public class Error {

    @Id
    private String errorCode;
    private String errorTitle;

    private boolean exists;


    public Error(String errorCode, String errorTitle) {
        this.errorCode = errorCode;
        this.errorTitle = errorTitle;
    }

    public Error() {
        this.exists = false;
    }

    public boolean isNull(){
        return !this.exists;
    }

    public void setNull() {
        this.exists = false;
    }

    public void setExisting() {
        this.exists = true;
    }

    public String getErrorCode() {
        return isNull() ? "null" : errorCode;
    }

    public void setErrorCode(String errorCode)
    {
        if(isNull()) {
            return;
        }
        this.errorCode = errorCode;
    }

    public String getErrorTitle() {
        return isNull() ? "null" : errorCode;
    }

    public void setErrorTitle(String errorTitle) {
        if(isNull())
            return;
        this.errorTitle = errorTitle;
    }


}
