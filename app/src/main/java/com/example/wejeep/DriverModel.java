package com.example.wejeep;

public class DriverModel {
    private String documentId; // Field for the document ID
    private String name;
    private String contact;
    private String dateAdded;

    // Default constructor needed for Firestore
    public DriverModel() {}

    // Constructor with parameters
    public DriverModel(String name, String contact, String dateAdded, String documentId) {
        this.name = name;
        this.contact = contact;
        this.dateAdded = dateAdded;
        this.documentId = documentId;
    }

    // Getters and Setters
    public String getDocumentId() {
        return documentId;
    }

    public void setDocumentId(String documentId) {
        this.documentId = documentId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getContact() {
        return contact;
    }

    public void setContact(String contact) {
        this.contact = contact;
    }

    public String getDateAdded() {
        return dateAdded;
    }

    public void setDateAdded(String dateAdded) {
        this.dateAdded = dateAdded;
    }
}

