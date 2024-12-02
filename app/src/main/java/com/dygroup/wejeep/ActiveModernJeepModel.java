package com.dygroup.wejeep;

public class ActiveModernJeepModel {
    private String unitNumber;
    private String vehicleModel;
    private String nameDriver;
    private String namePao;
    private String plateNumber;
    private String documentId; // Field for the document ID

    // Default constructor needed for Firestore
    public ActiveModernJeepModel() {}

    // Constructor with parameters
    public ActiveModernJeepModel(String unitNumber, String vehicleModel, String nameDriver, String namePao, String plateNumber, String documentId) {
        this.unitNumber = unitNumber;
        this.vehicleModel = vehicleModel;
        this.nameDriver = nameDriver;
        this.namePao = namePao;
        this.plateNumber = plateNumber;
        this.documentId = documentId;
    }

    // Getters and Setters
    public String getUnitNumber() {
        return unitNumber;
    }
    public void setUnitNumber(String unitNumber) {
        this.unitNumber = unitNumber;
    }
    public String getVehicleModel() {
        return vehicleModel;
    }
    public void setVehicleModel(String vehicleModel) {
        this.vehicleModel = vehicleModel;
    }
    public String getNameDriver() {
        return nameDriver;
    }
    public void setNameDriver(String nameDriver) {
        this.nameDriver = nameDriver;
    }
    public String getNamePao() {
        return namePao;
    }
    public void setNamePao(String namePao) {
        this.namePao = namePao;
    }
    public String getPlateNumber() {
        return plateNumber;
    }
    public void setPlateNumber(String plateNumber) {
        this.plateNumber = plateNumber;
    }
    public String getDocumentId() {
        return documentId;
    }
    public void setDocumentId(String documentId) {
        this.documentId = documentId;
    }
}
