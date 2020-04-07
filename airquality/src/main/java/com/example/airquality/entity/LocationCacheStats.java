package com.example.airquality.entity;


import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Entity
public class LocationCacheStats {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private int hits;
    private int misses;
    private int numRequests;

    public LocationCacheStats(){
        this.hits = 0;
        this.misses = 0;
        this.numRequests = 0;
    }

    public void addHit(){
        this.hits++;
    }

    public void addMiss(){
        this.misses++;
    }

    public void addRequest(){
        this.numRequests++;
    }

    public int getHits() {
        return hits;
    }

    public void setHits(int hits) {
        this.hits = hits;
    }

    public int getMisses() {
        return misses;
    }

    public void setMisses(int misses) {
        this.misses = misses;
    }

    public int getNumRequests() {
        return numRequests;
    }

    public void setNumRequests(int numRequests) {
        this.numRequests = numRequests;
    }

    @Override
    public String toString() {
        return "LocationCacheStats{" +
                "id=" + id +
                ", hits=" + hits +
                ", misses=" + misses +
                ", numRequests=" + numRequests +
                '}';
    }
}
