package com.example.airquality.entity;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Entity
public class Concentration {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private double value;
    private String units;

    public Concentration(double value, String units) {
        this.value = value;
        this.units = units;
    }

    public Concentration() {}

    public double getValue() {
        return value;
    }

    public void setValue(double value) {
        this.value = value;
    }

    public String getUnits() {
        return units;
    }

    public void setUnits(String units) {
        this.units = units;
    }


}
