package com.example.wejeep;



public class UnitModel {
    private String vehicleunit; // Field for the document ID
    private String platenumber;
    private String documentId;
    private String dateAdded;

    public UnitModel() {

    }

    // Default constructor needed for Firestore
    public UnitModel(String unitVehicleModel, String unitPlateNumber, String dateAdded, String id) {
        this.vehicleunit = vehicleunit;
        this.platenumber = platenumber;
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

    public String getVehicleunit() {
        return vehicleunit;
    }

    public void setVehicleunit(String vehicleunit) { this.vehicleunit = vehicleunit; }

    public String getPlatenumber() {
        return platenumber;
    }

    public void setPlatenumber(String platenumber) {
        this.platenumber = platenumber;
    }

    public String getDateAdded() {
        return dateAdded;
    }

    public void setDateAdded(String dateAdded) {
        this.dateAdded = dateAdded;
    }
}

