package com.example.demo.javaSrc.people;

import java.sql.Date;
import java.util.Optional;
import jakarta.persistence.*;

@Entity
@Table(name = "users")
public class People {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    public enum Role { TEACHER, STUDENT, PARENT }

    @Column(name = "school_id", nullable = false)
    private Long schoolId;

    @Column(name = "class_id")
    private Long classId;

    @Column(name = "first_name")
    private String firstName;

    @Column(name = "last_name")
    private String lastName;

    @Column(name = "about_me")
    private String aboutMe;

    @Column(name = "email", unique = true, nullable = false)
    private String email;

    @Column(name = "password_hash", nullable = false)
    private String password;

    @Column(name = "date_of_birth")
    private Date dateOfBirth;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)
    private Role role;
    
    public People() {}

    public People(Long schoolId, Long classId, String firstName, String lastName,
                  String aboutMe, Date dateOfBirth, String email,
                  String password, Role role) {
        this.schoolId    = schoolId;
        this.classId     = classId;
        this.firstName   = firstName;
        this.lastName    = lastName;
        this.aboutMe     = aboutMe;
        this.dateOfBirth = dateOfBirth;
        this.email       = email;
        this.password    = password;
        this.role        = role;
    }

    public Long getId()         { return id; }
    public Long getSchoolId()   { return schoolId; }
    public void setSchoolId(Long schoolId) { this.schoolId = schoolId; }

    public Long getClassId()    { return classId; }
    public void setClassId(Long classId)   { this.classId = classId; }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName()  { return lastName; }
    public void setLastName(String lastName)   { this.lastName = lastName; }

    public String getAboutMe()   { return aboutMe; }
    public void setAboutMe(String aboutMe)     { this.aboutMe = aboutMe; }

    public String getEmail()     { return email; }
    public void setEmail(String email)         { this.email = email; }

    public String getPassword()  { return password; }
    public void setPassword(String password)   { this.password = password; }

    public Date getDateOfBirth() { return dateOfBirth; }
    public void setDateOfBirth(Date dateOfBirth) { this.dateOfBirth = dateOfBirth; }

    public Role getRole()        { return role; }
    public void setRole(Role role)             { this.role = role; }
}
