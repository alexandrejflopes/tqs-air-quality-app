package com.example.airquality.entity;

import org.springframework.lang.Nullable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

//@Entity
//@Table(name = "reportError")
public class Error {

    //@Id
    //@Column(name = "reportErrorCode")
    private String errorCode;

    //@Column(name = "reportErrorTitle")
    private String errorTitle;


    private boolean exists;


    public Error(String errorCode, String errorTitle) {
        this.errorCode = errorCode;
        this.errorTitle = errorTitle;
        this.exists = true;
    }

    public Error() {
        this.errorCode = "non_existing_error";
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


    @Override
    public String toString() {
        return "Error{" +
                "errorCode='" + errorCode + '\'' +
                ", errorTitle='" + errorTitle + '\'' +
                ", exists=" + exists +
                '}';
    }
}
