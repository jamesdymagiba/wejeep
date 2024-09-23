package com.example.wejeep;

public class DriverModel {
    private String name;
    private String contact;
    private String dateAdded;

    public DriverModel(String name, String contact, String dateAdded) {
        this.name = name;
        this.contact = contact;
        this.dateAdded = dateAdded;
    }

    public String getName() {
        return name;
    }

    public String getContact() {
        return contact;
    }

    public String getDateAdded() {
        return dateAdded;
    }
}
