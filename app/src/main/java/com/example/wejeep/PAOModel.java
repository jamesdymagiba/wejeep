package com.example.wejeep;

public class PAOModel {
    private String name;
    private String email;
    private String documentId;  // New field for Firestore document ID
    private String dateadded; // New field for date added

    public PAOModel(String name, String email, String documentId, String dateadded) {
        this.name = name;
        this.email = email;
        this.documentId = documentId;
        this.dateadded = dateadded; // Initialize the dateAdded field
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getDocumentId() {
        return documentId; // Getter for document ID
    }

    public String getDateAdded() {
        return dateadded; // Getter for dateAdded
    }
}
