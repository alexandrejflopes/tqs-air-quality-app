package com.example.airquality.exception;


import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.NOT_FOUND)

public class InvalidLocationException extends Exception {

    public InvalidLocationException() {
        super("Location is invalid.");
    }

    @Override
    public String getMessage() {
        return super.getMessage();
    }


}
