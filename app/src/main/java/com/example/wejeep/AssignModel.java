package com.example.wejeep;

public class AssignModel {
    private String unitnumber;
    private String platenumber;
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
    public AssignModel(String conductor,String driver, String unitnumber, String platenumber, String fromDay, String toDay, String fromTime, String toTime , String documentId) {
        this.unitnumber = unitnumber;
        this.platenumber = platenumber;
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
    public String getplatenumber() {return platenumber;}
    public void setplatenumber(String platenumber) {this.platenumber = platenumber;}
    public String getFromDay() { return fromDay;}
    public void setFromDay(String fromDay) {this.fromDay = fromDay;}
    public String getToDay() {return toDay;}
    public void setToDay(String toDay) {this.toDay = toDay;}
    public String getToTime() {return toTime;}
    public void setFromTime(String fromTime) {this.fromTime = fromTime;}
    public String getFromTime() {return fromTime;}
    public void setToTime(String toTime) {this.toTime = toTime;}
    public String getunitnumber() {return unitnumber;}
    public void setunitnumber(String unitnumber) {this.unitnumber = unitnumber;}

    //public String getConductor() {return conductor;}
    //public void setConductor(String conductor) {this.conductor = conductor;}
    public String getDriver() {return driver;}
    public void setDriver(String driver) {this.driver = driver;}
}