package com.dygroup.wejeep;



public class UnitModel {
    private String vehicleModel;
    private String plateNumber;
    private String documentId;
    private String dateAdded;
    private String unitNumber;

    public UnitModel() {}

    // Default constructor needed for Firestore
    public UnitModel(String vehicleModel, String plateNumber, String dateAdded, String unitNumber, String documentId) {
        this.vehicleModel = vehicleModel;
        this.plateNumber = plateNumber;
        this.unitNumber = unitNumber;
        this.dateAdded = dateAdded;
        this.documentId = documentId;
    }
    // Constructor with parameters


    // Getters and Setters
    public String getDocumentId() {
        return documentId;
    }
    public void setDocumentId(String documentId) {
        this.documentId = documentId;
    }

    public String getvehicleModel() {
        return vehicleModel;
    }

    public void setvehicleModel(String vehicleModel) { this.vehicleModel = vehicleModel; }

    public String getplateNumber() {
        return plateNumber;
    }

    public void setplateNumber(String plateNumber) {
        this.plateNumber = plateNumber;
    }
    public String getunitNumber() {return unitNumber; }
    public void setunitNumber(String unitNumber) {this.unitNumber = unitNumber;}

    public String getDateAdded() { return dateAdded;
    }

    public void setDateAdded(String dateAdded) {
        this.dateAdded = dateAdded;
    }
}