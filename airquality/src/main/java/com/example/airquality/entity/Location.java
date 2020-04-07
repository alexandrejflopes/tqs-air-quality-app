package com.example.airquality.entity;

import javax.persistence.Embeddable;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.Objects;

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

    public String getCountryCode() {
        return countryCode;
    }

    public void setCountryCode(String countryCode) {
        this.countryCode = countryCode;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }


    @Override
    public String toString() {
        return "Location{" +
                "countryCode='" + countryCode + '\'' +
                ", address='" + address + '\'' +
                ", coordinates=" + coordinates.toString() +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Location location = (Location) o;
        return countryCode.equals(location.countryCode) &&
                address.equals(location.address) &&
                coordinates.equals(location.coordinates);
    }

    @Override
    public int hashCode() {
        return Objects.hash(countryCode, address, coordinates);
    }
}
