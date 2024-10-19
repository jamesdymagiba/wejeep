package com.example.wejeep;

public class AssignModel {
    private String unitNumber;
    private String plateNumber;
    private String driver;
    //private String conductor;
    private String fromDay;
    private String toDay;
    private String fromTime;
    private String toTime;
    private String documentId;

    public AssignModel() {

    }

    // Required empty constructor for Firebase Firestore
    public AssignModel(String conductor,String driver, String unitNumber, String plateNumber, String fromDay, String toDay, String fromTime, String toTime , String documentId) {
        this.unitNumber = unitNumber;
        this.plateNumber = plateNumber;
        this.driver = driver;
        //this.conductor = conductor;
        this.fromDay = fromDay;
        this.toDay = toDay;
        this.fromTime = fromTime;
        this.toTime = toTime;
        this.documentId = documentId;
    }

    // Getter and Setter

    public String getDocumentId() {return documentId;}
    public void setDocumentId(String documentId) { this.documentId = documentId;}
    public String getPlateNumber() {return plateNumber;}
    public void setPlateNumber(String plateNumber) {this.plateNumber = plateNumber;}
    public String getFromDay() { return fromDay;}
    public void setFromDay(String fromDay) {this.fromDay = fromDay;}
    public String getToDay() {return toDay;}
    public void setToDay(String toDay) {this.toDay = toDay;}
    public String getToTime() {return toTime;}
    public void setFromTime(String fromTime) {this.fromTime = fromTime;}
    public String getFromTime() {return fromTime;}
    public void setToTime(String toTime) {this.toTime = toTime;}
    public String getUnitNumber() {return unitNumber;}
    public void setUnitNumber(String unitNumber) {this.unitNumber = unitNumber;}
    //public String getConductor() {return conductor;}
    //public void setConductor(String conductor) {this.conductor = conductor;}
    public String getDriver() {return driver;}
    public void setDriver(String driver) {this.driver = driver;}
}