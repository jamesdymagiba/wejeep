package com.example.wejeep;

public class AssignModel {
    private String unitnumber;
    private String platenumber;
    private String driver;
    private String conductor;
    private String Fromday;
    private String Today;
    private String Fromtime;
    private String Totime;
    private String Schedule;
    private String documentId;

    public AssignModel() {

    }

    // Required empty constructor for Firebase Firestore
    public AssignModel(String conductor,String driver, String unitnumber, String platenumber, String Fromday, String Today, String Fromtime, String Totime ,String Schedule, String documentId) {
        this.unitnumber = unitnumber;
        this.platenumber = platenumber;
        this.driver = driver;
        this.conductor = conductor;
        this.Fromday = Fromday;
        this.Today = Today;
        this.Fromtime = Fromtime;
        this.Totime = Totime;
        this.Schedule = Schedule;
        this.documentId = documentId;
    }

    // Getter and Setter

    public String getDocumentId() {return documentId;}
    public void setDocumentId(String documentId) { this.documentId = documentId;}
    public String getplatenumber() {return platenumber;}
    public void setplatenumber(String platenumber) {this.platenumber = platenumber;}


    public String getFromday() { return Fromday;}
    public void setFromday(String Fromday) {this.Fromday = Fromday;}
    public String getToday() {return Today;}
    public void setToday(String Today) {this.Today = Today;}
    public String getTotime() {return Totime;}
    public void setTotime(String Totime) {this.Totime = Totime;}
    public String getFromtime() {return Fromtime;}
    public void setFromtime(String Fromtime) {this.Fromtime = Fromtime;}


    public String getunitnumber() {return unitnumber;}
    public void setunitnumber(String unitnumber) {this.unitnumber = unitnumber;}

    public String getConductor() {return conductor;}
    public void setConductor(String conductor) {this.conductor = conductor;}
    public String getDriver() {return driver;}
    public void setDriver(String driver) {this.driver = driver;}
    public String getSchedule() {return Schedule;}
    public void setSchedule(String Schedule) {this.Schedule = Schedule;}
}