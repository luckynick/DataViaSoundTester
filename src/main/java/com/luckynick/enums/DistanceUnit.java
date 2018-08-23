package com.luckynick.enums;

public enum DistanceUnit {

    CENTIMETER("Centimeter", 1);

    private String unitName;
    private double centimetersInOneUnit;

    private DistanceUnit() {};

    DistanceUnit(String unitName, double centimetersInOneUnit) {
        this.unitName = unitName;
        this.centimetersInOneUnit = centimetersInOneUnit;
    }

    public String getUnitName() {
        return unitName;
    }

    public double getCentimetersInOneUnit() {
        return centimetersInOneUnit;
    }
}
