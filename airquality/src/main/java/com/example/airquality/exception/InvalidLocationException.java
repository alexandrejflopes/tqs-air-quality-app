package com.example.airquality.exception;


import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.NOT_FOUND)

public class InvalidLocationException extends Exception {

    public InvalidLocationException() {
        super("Could not geocode that location. Causes might be invalid location provided or invalid MapQuest API Key");
    }

}
