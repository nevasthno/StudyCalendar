package com.example.demo.javaSrc.people;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

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

    public List<People> getBySchoolAndClass(Long schoolId, Long classId) {
        if (classId == null) {
            return peopleRepository.findBySchoolId(schoolId);
        }
        return peopleRepository.findBySchoolIdAndClassId(schoolId, classId);
    }

    public List<People> getBySchoolClassAndRole(Long schoolId, Long classId, People.Role role) {
        return getBySchoolAndClass(schoolId, classId).stream()
                .filter(p -> p.getRole() == role)
                .collect(Collectors.toList());
    }

    public List<People> getPeopleByRole(String role) {
        return peopleRepository.findByRole(
            People.Role.valueOf(role.toUpperCase())
        );
    }
    
    public People findByEmail(String email) {
        Optional<People> maybe = peopleRepository.findByEmail(email);
        return maybe.orElse(null);
    }
}
