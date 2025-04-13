package com.example.demo.javaSrc.people;


import java.sql.Date;

public class People {
    public enum Role { TEACHER, STUDENT, PARENT }
    
    private String firstName, lastName, aboutMe, email, password;
    private Date dateOfBirth;
    private Role role;

    public People(String firstName, String lastName, String aboutMe, Date dateOfBirth, String email, String password, Role role) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.aboutMe = aboutMe;
        this.dateOfBirth = dateOfBirth;
        this.email = email;
        this.password = password;
        this.role = role;
    }

    public String getFirstName() { return firstName; }
    public String getLastName() { return lastName; }
    public String getEmail() { return email; }
    public Role getRole() { return role; }
   
    public void setFirstName(String firstName) { this.firstName = firstName; }
    public void setLastName(String lastName) { this.lastName = lastName; }
    public void setAboutMe(String aboutMe) { this.aboutMe = aboutMe; }
    public void setDateOfBirth(Date dateOfBirth) { this.dateOfBirth = dateOfBirth; }
}




