package com.example.airquality.entity;

import javax.persistence.*;

@Entity
public class Index {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private String name; // "Air Quality Index"
    private String valueDisplay;
    private String category;

    @OneToOne(targetEntity=Pollutant.class, fetch=FetchType.EAGER)
    private Pollutant dominantPollutant;


    public Index(String name, String valueDisplay, String category, Pollutant dominantPollutant) {
        this.name = name;
        this.valueDisplay = valueDisplay;
        this.category = category;
        this.dominantPollutant = dominantPollutant;
    }

    public Index() {}

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getValueDisplay() {
        return valueDisplay;
    }

    public void setValueDisplay(String valueDisplay) {
        this.valueDisplay = valueDisplay;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public Pollutant getDominantPollutant() {
        return dominantPollutant;
    }

    public void setDominantPollutant(Pollutant dominantPollutant) {
        this.dominantPollutant = dominantPollutant;
    }
}
