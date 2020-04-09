package com.example.airquality.entity;


import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "report")
public class Report {

    @Column
    private LocalDateTime requestTimeStamp; // UTC timestamp indicating the time the remote request was made

    @Column
    private LocalDateTime lastUpdatedAt; // UTC timestamp indicating the time the data refers to

    @EmbeddedId
    private Location location;

    private boolean dataAvailable;

    @OneToOne(targetEntity=Index.class, fetch=FetchType.EAGER, cascade = {CascadeType.ALL})
    private Index index;


    @Column
    @OneToMany(fetch = FetchType.EAGER, cascade = {CascadeType.ALL})
    private List<Pollutant> pollutants;


    public Report() {
        /*
         * empty constructor to instantiate the report;
         * all its data will be added using setters as needed
         * */
    }

    public LocalDateTime getRequestTimeStamp() {
        return requestTimeStamp;
    }

    public void setRequestTimeStamp(LocalDateTime requestTimeStamp) {
        this.requestTimeStamp = requestTimeStamp;
    }

    public LocalDateTime getLastUpdatedAt() {
        return lastUpdatedAt;
    }

    public void setLastUpdatedAt(LocalDateTime lastUpdatedAt) {
        this.lastUpdatedAt = lastUpdatedAt;
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public boolean isDataAvailable() {
        return dataAvailable;
    }

    public void setDataAvailable(boolean dataAvailable) {
        this.dataAvailable = dataAvailable;
    }

    public Index getIndex() {
        return index;
    }

    public void setIndex(Index index) {
        this.index = index;
    }

    public List<Pollutant> getPollutants() {
        return pollutants;
    }

    public void setPollutants(List<Pollutant>  pollutants) {
        this.pollutants = pollutants;
    }


    // LOCATION CACHE STATS
    @OneToOne(targetEntity=CacheStats.class, fetch=FetchType.EAGER, cascade = {CascadeType.ALL}, orphanRemoval = true)
    private CacheStats locationCacheStats;

    public CacheStats getLocationCacheStats() {
        return locationCacheStats;
    }

    public void setLocationCacheStats(CacheStats locationCacheStats) {
        this.locationCacheStats = locationCacheStats;
    }

    // CACHE STATS
    @OneToOne(targetEntity=CacheStats.class, fetch=FetchType.EAGER, cascade = {CascadeType.ALL}, orphanRemoval = true)
    private CacheStats globalCacheStats;

    public CacheStats getGlobalCacheStats() {
        return globalCacheStats;
    }

    public void setGlobalCacheStats(CacheStats globalCacheStats) {
        this.globalCacheStats = globalCacheStats;
    }

    // ERROR
    private boolean hasError;

    private String errorCode;

    private String errorTitle;

    public boolean hasError() {
        return hasError;
    }

    public void putError() {
        this.hasError = true;
    }

    public void removeError(){
        this.hasError = false;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }

    public String getErrorTitle() {
        return errorTitle;
    }

    public void setErrorTitle(String errorTitle) {
        this.errorTitle = errorTitle;
    }



    @Override
    public String toString() {
        if(hasError){
            return "Report{" +
                    "hasError=" + hasError +
                    ", errorCode=" + errorCode +
                    ", errorTitle=" +  errorTitle
                    ;
        }

        return "Report{" +
                "requestTimeStamp=" + requestTimeStamp +
                ", lastUpdatedAt=" + lastUpdatedAt +
                ", location=" + location.toString() +
                ", dataAvailable=" + dataAvailable +
                ", index=" + index.toString() +
                ", pollutants=" + pollutants.toString() +
                ", hasError=" + hasError +
                ", errorCode=" + errorCode +
                ", errorTitle=" +  errorTitle +
                ", locationCacheStats=" + locationCacheStats.toString() +
                ", globalCacheStats=" + globalCacheStats.toString() +
                '}';
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Report report = (Report) o;
        return dataAvailable == report.dataAvailable &&
                hasError == report.hasError &&
                Objects.equals(requestTimeStamp, report.requestTimeStamp) &&
                Objects.equals(lastUpdatedAt, report.lastUpdatedAt) &&
                Objects.equals(location, report.location) &&
                Objects.equals(index, report.index) &&
                Objects.equals(pollutants, report.pollutants) &&
                Objects.equals(locationCacheStats, report.locationCacheStats) &&
                Objects.equals(globalCacheStats, report.globalCacheStats) &&
                Objects.equals(errorCode, report.errorCode) &&
                Objects.equals(errorTitle, report.errorTitle);
    }

    @Override
    public int hashCode() {
        return Objects.hash(requestTimeStamp, lastUpdatedAt, location, dataAvailable, index, pollutants, locationCacheStats, globalCacheStats, hasError, errorCode, errorTitle);
    }
}
