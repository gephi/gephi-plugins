/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.totetmatt.gephi.twitter.networklogics.utils;

/**
 *
 * @author totetmatt
 */
public class TrackLocation {

    private double swLongitude;
    private double swLatitude;
    private double neLongitude;
    private double neLatitude;
    private String name;

    public double getSwLongitude() {
        return swLongitude;
    }

    public void setSwLongitude(double swLongitude) {
        this.swLongitude = swLongitude;
    }

    public double getSwLatitude() {
        return swLatitude;
    }

    public void setSwLatitude(double swLatitude) {
        this.swLatitude = swLatitude;
    }

    public double getNeLongitude() {
        return neLongitude;
    }

    public void setNeLongitude(double neLongitude) {
        this.neLongitude = neLongitude;
    }

    public double getNeLatitude() {
        return neLatitude;
    }

    public void setNeLatitude(double neLatitude) {
        this.neLatitude = neLatitude;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public TrackLocation(double swLatitude, double swLongitude, double neLatitude, double neLongitude) {
        this.swLongitude = swLongitude;
        this.swLatitude = swLatitude;
        this.neLongitude = neLongitude;
        this.neLatitude = neLatitude;
        this.name = "";
    }

    public TrackLocation(double swLatitude, double swLongitude, double neLatitude, double neLongitude, String name) {
        this.swLongitude = swLongitude;
        this.swLatitude = swLatitude;
        this.neLongitude = neLongitude;
        this.neLatitude = neLatitude;
        this.name = name;
    }

    public boolean isValid() {
        return isValidBox() && isValidCoordinate();
    }

    private boolean isValidCoordinate() {
        return this.swLatitude >= -90 && this.swLatitude <= 90
                && this.neLatitude >= -90 && this.neLatitude <= 90
                && this.swLongitude >= -180 && this.swLongitude <= 180
                && this.neLongitude >= -180 && this.neLongitude <= 180;
    }

    private boolean isValidBox() {
        return this.swLatitude <= this.neLatitude && this.swLongitude <= this.neLongitude;
    }

    @Override
    public String toString() {
        return "(" + swLongitude + "," + swLatitude + ") (" + neLongitude + "," + neLatitude + ')';
    }

}
