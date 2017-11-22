package com.agos.df2017.entities;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by aortuno on 11/24/2016.
 */
public class Message implements Serializable {

    private String id;
    private Date date;
    private String originId;
    private String destinyId;
    private String message;
    private String user;
    private Double latitude;
    private Double longitude;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getOriginId() {
        return originId;
    }

    public void setOriginId(String originId) {
        this.originId = originId;
    }

    public String getDestinyId() {
        return destinyId;
    }

    public void setDestinyId(String destinyId) {
        this.destinyId = destinyId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    @Override
    public String toString() {
        return "Message{" +
                "id='" + id + '\'' +
                ", date=" + date +
                ", originId='" + originId + '\'' +
                ", destinyId='" + destinyId + '\'' +
                ", message='" + message + '\'' +
                ", user='" + user + '\'' +
                ", latitude=" + latitude +
                ", longitude=" + longitude +
                '}';
    }
}
