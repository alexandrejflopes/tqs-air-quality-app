package com.example.airquality.entity;

import javax.persistence.Embeddable;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

@Embeddable
public class Location implements Serializable {


    private String country;

    @NotNull
    private Coordinates coordinates;

    public Location(Coordinates coordinates, String country) {
        this.coordinates = coordinates;
        this.country = country;
    }

    public Location() {}

    public Coordinates getCoordinates() {
        return coordinates;
    }

    public void setCoordinates(Coordinates coordinates) {
        this.coordinates = coordinates;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }
}
