package com.example.airquality.entity;


import javax.persistence.*;

@Entity
public class Pollutant {

    @Id
    private String displayName;
    private String fullName;

    @OneToOne(targetEntity=Concentration.class, fetch=FetchType.EAGER)
    private Concentration concentration;

    public Pollutant() {
    }
}
