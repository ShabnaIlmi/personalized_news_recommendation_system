package com.example.personalized_news_recommendation_system.Model;

import java.time.LocalDateTime;

public abstract class Person {
    protected String firstName;
    protected String lastName;
    protected String email;
    protected int age;
    protected String gender;
    protected String contactNumber;
    protected String username;
    protected String password;
    protected String createdDateTime;

    // Constructor for Person class
    public Person(String firstName, String lastName, String email, int age, String gender, String contactNumber, String username, String password) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.age = age;
        this.gender = gender;
        this.contactNumber = contactNumber;
        this.username = username;
        this.password = password;
        this.createdDateTime = LocalDateTime.now().toString(); // Capture creation time
    }

    // Getters
    public String getFirstName() { return firstName; }
    public String getLastName() { return lastName; }
    public String getEmail() { return email; }
    public int getAge() { return age; }
    public String getGender() { return gender; }
    public String getContactNumber() { return contactNumber; }
    public String getUsername() { return username; }
    public String getPassword() { return password; }
    public String getCreatedDateTime() { return createdDateTime; }

    // Abstract method to be implemented by child classes
    public abstract String getRole();

    // Override toString() for common attributes
    @Override
    public String toString() {
        return "Person{" +
                "firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", email='" + email + '\'' +
                ", age=" + age +
                ", gender='" + gender + '\'' +
                ", contactNumber='" + contactNumber + '\'' +
                ", username='" + username + '\'' +
                ", createdDateTime='" + createdDateTime + '\'' +
                '}';
    }
}

