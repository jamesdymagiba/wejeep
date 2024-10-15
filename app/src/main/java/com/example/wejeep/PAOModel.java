package com.example.wejeep;

public class PAOModel {
    private String name;
    private String email;
    private String documentId;  // New field for Firestore document ID

    public PAOModel(String name, String email, String documentId) {
        this.name = name;
        this.email = email;
        this.documentId = documentId;
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
}