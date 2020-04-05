package com.example.airquality.entity;

import javax.persistence.Embeddable;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

@Embeddable
public class Location implements Serializable {


    private String countryCode;
    private String address;

    @NotNull
    private Coordinates coordinates;

    public Location(Coordinates coordinates, String countryCode, String address) {
        this.coordinates = coordinates;
        this.countryCode = countryCode;
        this.address = address;
    }

    public Location(Coordinates coordinates, String countryCode) {
        this.coordinates = coordinates;
        this.countryCode = countryCode;
    }

    public Location(Coordinates coordinates) {
        this.coordinates = coordinates;
    }

    public Location() {}

    public Coordinates getCoordinates() {
        return coordinates;
    }

    public void setCoordinates(Coordinates coordinates) {
        this.coordinates = coordinates;
    }

    public String getcountryCode() {
        return countryCode;
    }

    public void setcountryCode(String countryCode) {
        this.countryCode = countryCode;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }
}
