package com.example.wejeep;

public class ScheduleModel {
    private String documentId; // Field for the document ID
    private String Fromday;
    private String Today;
    private String Fromtime;
    private String Totime;
    private String Schedule;

    // Default constructor needed for Firestore

    public ScheduleModel() {

    }
    public ScheduleModel(String Fromday, String Today, String Fromtime, String Totime, String Schedule, String id) {
        this.Fromday = Fromday;
        this.Today = Today;
        this.Fromtime = Fromtime;
        this.Totime = Totime;
        this.Schedule= Schedule;
        this.documentId = documentId;
    }

    // Getters and Setters
    public String getDocumentId() {
        return documentId;
    }

    public void setDocumentId(String documentId) {
        this.documentId = documentId;
    }

    public String getFromDay() {
        return Fromday;
    }

    public void setFromDay(String Fromday) { this.Fromday = Fromday;
    }

    public String getToDay() {
        return Today;
    }

    public void setToDay(String Today) {
        this.Today = Today;
    }

    public String getFromTime() { return Fromtime; }

    public void setFromTime(String Fromtime) { this.Fromtime = Fromtime; }
    public String getToTime() {return Totime; }
    public void setToTime(String Totime) { this.Totime = Totime;}
    public String getSchedule() {return Schedule; }
    public void setSchedule(String Schedule) { this.Schedule = Schedule;}

}

