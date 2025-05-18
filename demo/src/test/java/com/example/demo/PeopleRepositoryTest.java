package com.example.demo;
//  Працює через раз, найчастіше  на пусту БД
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.example.demo.javaSrc.people.People;
import com.example.demo.javaSrc.people.PeopleRepository;

@SpringBootTest
public class PeopleRepositoryTest {
    @Autowired
    private PeopleRepository peopleRepository;

    @BeforeEach
    void setUp() {
        peopleRepository.deleteAll();
    }

    @Test
    void testFindByEmail() {
        People person = new People();
        String email = "testemail@gmail.com";
        person.setEmail(email);
        person.setPassword("password123");
        person.setRole(People.Role.STUDENT);
        person.setSchoolId(1L);
        person.setClassId(1L);
        person.setFirstName("John");
        person.setLastName("Doe");

        peopleRepository.save(person);

        Optional<People> found = peopleRepository.findByEmail(email);

        assertThat(found).isPresent(); 
        assertThat(found.get().getEmail()).isEqualTo(email); 
    }

    @Test
    void testFindByRole() {
        People person1 = new People();
        person1.setRole(People.Role.STUDENT);
        String email = "testemail@gmail.com";
        person1.setEmail(email);
        person1.setPassword("password123");
        person1.setSchoolId(1L);
        person1.setClassId(1L);
        person1.setFirstName("John");
        person1.setLastName("Doe");
        peopleRepository.save(person1);

        People person2 = new People();
        person2.setRole(People.Role.TEACHER);
        String email1 = "testemail1@gmail.com";
        person2.setEmail(email1);
        person2.setPassword("password123");
        person2.setSchoolId(1L);
        person2.setClassId(1L);
        person2.setFirstName("John");
        person2.setLastName("Doe");
        peopleRepository.save(person2);

        List<People> students = peopleRepository.findByRole(People.Role.STUDENT);
        List<People> teachers = peopleRepository.findByRole(People.Role.TEACHER);

        assertThat(students).hasSize(1);
        assertThat(teachers).hasSize(1);
    }    

    @Test
    void testFindBySchoolId() {
         People person1 = new People();
        person1.setSchoolId(1L);
        person1.setClassId(1L);
        String email = "testemail@gmail.com";
        person1.setEmail(email);
        person1.setPassword("password123");
        person1.setFirstName("John");
        person1.setLastName("Doe");
        person1.setRole(People.Role.STUDENT);
        peopleRepository.save(person1);

        People person2 = new People();
        person2.setRole(People.Role.TEACHER);
        person2.setSchoolId(2L);
        person2.setClassId(2L);
        String email1 = "testemail1@gmail.com";
        person2.setEmail(email1);
        person2.setPassword("password123");
        person2.setFirstName("John");
        person2.setLastName("Doe");
        peopleRepository.save(person2);

        List<People> school1People = peopleRepository.findBySchoolId(1L);
        List<People> school2People = peopleRepository.findBySchoolId(2L);

        assertThat(school1People).hasSize(1);
        assertThat(school2People).hasSize(1);
    }

    @Test
    void testFindBySchoolIdAndClassId() {
        People person1 = new People();
        person1.setSchoolId(1L);
        person1.setClassId(1L);
        String email = "testemail@gmail.com";
        person1.setEmail(email);
        person1.setPassword("password123");
        person1.setFirstName("John");
        person1.setLastName("Doe");
        person1.setRole(People.Role.STUDENT);
        peopleRepository.save(person1);

        People person2 = new People();
        person2.setRole(People.Role.TEACHER);
        person2.setSchoolId(1L);
        person2.setClassId(2L);
        String email1 = "testemail1@gmail.com";
        person2.setEmail(email1);
        person2.setPassword("password123");
        person2.setFirstName("John");
        person2.setLastName("Doe");
        peopleRepository.save(person2);

        List<People> class1People = peopleRepository.findBySchoolIdAndClassId(1L, 1L);
        List<People> class2People = peopleRepository.findBySchoolIdAndClassId(1L, 2L);

        assertThat(class1People).hasSize(1);
        assertThat(class2People).hasSize(1);
    }
}
