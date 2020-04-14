package com.example.airquality.entity;

import javax.persistence.*;
import java.util.Objects;

@Entity
public class Index {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private String name; // "Air Quality Index"
    private String valueDisplay;
    private String category;

    @OneToOne(targetEntity=Pollutant.class, fetch=FetchType.EAGER, cascade = {CascadeType.ALL})
    private Pollutant dominantPollutant;


    public Index(String name, String valueDisplay, String category, Pollutant dominantPollutant) {
        this.name = name;
        this.valueDisplay = valueDisplay;
        this.category = category;
        this.dominantPollutant = dominantPollutant;
    }

    public Index(String name, String valueDisplay, String category) {
        this.name = name;
        this.valueDisplay = valueDisplay;
        this.category = category;
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

    @Override
    public String toString() {
        return "Index{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", valueDisplay='" + valueDisplay + '\'' +
                ", category='" + category + '\'' +
                ", dominantPollutant=" + dominantPollutant.toString() +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Index index = (Index) o;
        return Objects.equals(id, index.id) &&
                Objects.equals(name, index.name) &&
                Objects.equals(valueDisplay, index.valueDisplay) &&
                Objects.equals(category, index.category) &&
                Objects.equals(dominantPollutant, index.dominantPollutant);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, valueDisplay, category, dominantPollutant);
    }
}
