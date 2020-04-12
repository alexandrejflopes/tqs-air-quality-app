package com.example.airquality.entity;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import java.util.Objects;

@Entity
public class CacheStats {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;


    private int hits;
    private int misses;
    private int numRequests;

    public CacheStats(){
        this.hits = 0;
        this.misses = 0;
        this.numRequests = 0;
    }

    public CacheStats(int hits, int misses, int numRequests){
        this.hits = hits;
        this.misses = misses;
        this.numRequests = numRequests;
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

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }


    @Override
    public String toString() {
        return "CacheStats{" +
                "id=" + id +
                ", hits=" + hits +
                ", misses=" + misses +
                ", numRequests=" + numRequests +
                '}';
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CacheStats that = (CacheStats) o;
        return hits == that.hits &&
                misses == that.misses &&
                numRequests == that.numRequests &&
                Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, hits, misses, numRequests);
    }
}
