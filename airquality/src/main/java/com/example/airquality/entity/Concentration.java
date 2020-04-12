package com.example.airquality.entity;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import java.util.Objects;

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

    @Override
    public String toString() {
        return "Concentration{" +
                "id=" + id +
                ", value=" + value +
                ", units='" + units + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Concentration that = (Concentration) o;
        return Double.compare(that.value, value) == 0 &&
                Objects.equals(id, that.id) &&
                Objects.equals(units, that.units);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, value, units);
    }
}
