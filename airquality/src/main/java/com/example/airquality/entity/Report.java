package com.example.airquality.entity;


import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Entity
@Table(name = "report")
public class Report {

    @Column
    private LocalDateTime requestTimeStamp; // timestamp indicating the time the request was made

    @Column
    private LocalDateTime lastUpdatedAt; // UTC timestamp indicating the time the data refers to

    @EmbeddedId
    private Location location;

    private boolean dataAvailable;

    @OneToOne(targetEntity=Index.class, fetch=FetchType.EAGER, cascade = {CascadeType.ALL})
    private Index index;


    @Column
    @ManyToMany(cascade = {CascadeType.ALL})
    private List<Pollutant> pollutants;

    //@OneToOne(targetEntity=Error.class, fetch=FetchType.EAGER)
    //private Error error;


    public Report() {
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

    /*public Error getError() {
        return error;
    }

    public void setError(Error error) {
        this.error = error;
    }*/


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
                '}';
    }

}
