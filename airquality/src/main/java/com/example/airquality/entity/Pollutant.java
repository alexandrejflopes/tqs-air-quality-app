package com.example.airquality.entity;


import javax.persistence.*;

@Entity
public class Pollutant {

    @Id
    private String displayName;
    private String fullName;

    @OneToOne(targetEntity=Concentration.class, fetch=FetchType.EAGER)
    private Concentration concentration;

    public Pollutant(String displayName, String fullName) {
        this.displayName = displayName;
        this.fullName = fullName;
    }

    public Pollutant() {
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public Concentration getConcentration() {
        return concentration;
    }

    public void setConcentration(Concentration concentration) {
        this.concentration = concentration;
    }
}
