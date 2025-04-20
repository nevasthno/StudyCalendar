package com.example.demo.javaSrc.people;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class PeopleService {

    private final PeopleRepository peopleRepository;

    @Autowired
    public PeopleService(PeopleRepository peopleRepository) {
        this.peopleRepository = peopleRepository;
    }

    public List<People> getAllPeople() {
        return peopleRepository.findAll();
    }

    public People createPeople(People person) {
        return peopleRepository.save(person);
    }

    public List<People> getPeopleByRole(String role) {
        return peopleRepository.findByRole(People.Role.valueOf(role.toLowerCase().toUpperCase()));
    }

    public boolean isEmailTaken(String email) {
        return peopleRepository.existsByEmail(email);
    }

    public void register(String firstName, String lastName, String email, String password, People.Role role, People requester) {
        if (requester == null || requester.getRole() != People.Role.TEACHER) {
            throw new SecurityException("Only teachers can create new users!");
        }

        if (isEmailTaken(email)) {
            throw new IllegalArgumentException("An account with this email already exists!");
        }

        People newPerson = new People(firstName, lastName, "", null, email, password, role);
        peopleRepository.save(newPerson);
    }

    public boolean logIn(String email, String password) {
        Optional<People> user = peopleRepository.findByEmail(email);
        return user.map(people -> people.getPassword().equals(password)).orElse(false);
    }

    public void updateProfile(String email, String firstName, String lastName, String aboutMe, People requester) {
        if (!requester.getRole().equals(People.Role.TEACHER) && !requester.getEmail().equals(email)) {
            throw new SecurityException("You do not have permission to edit this profile");
        }

        Optional<People> userOpt = peopleRepository.findByEmail(email);
        if (userOpt.isPresent()) {
            People user = userOpt.get();
            user.setFirstName(firstName);
            user.setLastName(lastName);
            user.setAboutMe(aboutMe);
            peopleRepository.save(user);
        } else {
            throw new IllegalArgumentException("User not found!");
        }
    }

    public People viewProfile(String email, People requester) {
        if (requester == null) {
            throw new SecurityException("Unauthorized access!");
        }

        return peopleRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found."));
    }

    public People.Role getUserRoleByEmail(String email) {
        return peopleRepository.findRoleByEmail(email).orElse(null);
    }

    public Long getUserIDByEmail(String email) {
        return peopleRepository.findIdByEmail(email).orElse(null);
    }
}
