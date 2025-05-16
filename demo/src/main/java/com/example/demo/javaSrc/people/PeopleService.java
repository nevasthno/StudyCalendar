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
    
    public People findByEmail(String email) {
        Optional<People> maybe = peopleRepository.findByEmail(email);
        return maybe.orElse(null);
    }

    public People updateProfile(Long id, People updatedData) {
        return peopleRepository.findById(id).map(existing -> {
            existing.setFirstName(updatedData.getFirstName());
            existing.setLastName(updatedData.getLastName());
            existing.setAboutMe(updatedData.getAboutMe());
            existing.setDateOfBirth(updatedData.getDateOfBirth());
            // email, password, role — не оновлюються тут
            return peopleRepository.save(existing);
        }).orElse(null);
    }

    public People updateUser(Long id, People updatedData) {
        return peopleRepository.findById(id).map(existing -> {
            existing.setFirstName(updatedData.getFirstName());
            existing.setLastName(updatedData.getLastName());
            existing.setAboutMe(updatedData.getAboutMe());
            existing.setDateOfBirth(updatedData.getDateOfBirth());
            existing.setEmail(updatedData.getEmail());
            existing.setPassword(updatedData.getPassword());
            return peopleRepository.save(existing);
        }).orElse(null);
    }

}
