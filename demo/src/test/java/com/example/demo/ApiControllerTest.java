package com.example.demo;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import com.example.demo.javaSrc.eventsANDtask.*;
import com.example.demo.javaSrc.people.*;
import com.example.demo.javaSrc.school.*;
import com.example.demo.javaSrc.worker.*;

import java.time.LocalDateTime;
import java.util.List;

import org.mockito.InjectMocks;
import org.mockito.Mockito;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import org.springframework.security.test.context.support.WithMockUser;



@SpringBootTest
@AutoConfigureMockMvc
public class ApiControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TaskService taskService;

    @MockBean
    private EventService eventService;

    @MockBean
    private PeopleService peopleService;

    @MockBean
    private PasswordEncoder passwordEncoder;

    @MockBean
    private SchoolService schoolService;

    @MockBean
    private ClassService classService;

    @MockBean
    private PeopleRepository peopleRepository;

   
    @InjectMocks
    private ApiController apiController;  

    
    @BeforeEach
    void setUp() {
        peopleRepository.deleteAll();
    }

    @Test
    @WithMockUser(username = "user", roles = {"TEACHER"})
    void testGetAllSchool() throws Exception {
        School school1 = new School();
        school1.setName("School 1");
        School school2 = new School();
        school2.setName("School 2");
        List<School> schools = List.of(school1, school2);

        when(schoolService.getAllSchools()).thenReturn(schools);

        mockMvc.perform(get("/api/schools")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("School 1"))
                .andExpect(jsonPath("$[1].name").value("School 2"));
    }

    @Test
    @WithMockUser(username = "user", roles = {"TEACHER"})
    void testGetAllClasses() throws Exception {
        SchoolClass class1 = new SchoolClass();
        class1.setName("Class 1");
        class1.setSchoolId(1L);
        SchoolClass class2 = new SchoolClass();
        class2.setName("Class 2");
        class2.setSchoolId(1L);
        List<SchoolClass> classes = List.of(class1, class2);

        when(classService.getBySchoolId(1L)).thenReturn(classes);

        mockMvc.perform(get("/api/classes")
                        .param("schoolId", "1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Class 1"))
                .andExpect(jsonPath("$[1].name").value("Class 2"));
    }

    @Test
    @WithMockUser(username = "user", roles = {"TEACHER"})
    void testGetEvents() throws Exception {
        People person1 = new People();
        person1.setFirstName("John");
        person1.setEmail("testemail@gmail.com");
        person1.setPassword("password123");
        person1.setRole(People.Role.STUDENT);
        person1.setSchoolId(1L);
        person1.setClassId(1L); 

        Event event1 = new Event();
        event1.setTitle("Event 1");
        event1.setSchoolId(1L);
        event1.setClassId(11L);
        event1.setCreatedBy(person1.getId());
        event1.setStartEvent(LocalDateTime.now());

        Event event2 = new Event();
        event2.setTitle("Event 2");
        event2.setSchoolId(1L);
        event2.setClassId(11L);
        event2.setCreatedBy(person1.getId());
        event2.setStartEvent(LocalDateTime.now().plusDays(1));

        List<Event> eventsFromService = List.of(event1, event2);

        when(eventService.getEventsForSchool(1L)).thenReturn(eventsFromService);

        mockMvc.perform(get("/api/getEvents")
                .param("schoolId", "1")
                .param("classId", "11")// convert Long to String
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].title").value("Event 1"))
            .andExpect(jsonPath("$[1].title").value("Event 2"));
    }

    @Test
    @WithMockUser(username = "user", roles = {"TEACHER"})
    void testGetAllUsers() throws Exception {
        People people1 = new People();
        people1.setFirstName("John");
        people1.setLastName("Doe");
        people1.setEmail("jo@gg.com");
        People people2 = new People();
        people2.setFirstName("Jane");
        people2.setLastName("Doe");
        people2.setEmail("ja@gg.com");

        List<People> peopleList = List.of(people1, people2);

        when(peopleService.getAllPeople()).thenReturn(peopleList);

        mockMvc.perform(get("/api/loadUsers")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].firstName").value("John"))
                .andExpect(jsonPath("$[0].lastName").value("Doe"))
                .andExpect(jsonPath("$[1].firstName").value("Jane"))
                .andExpect(jsonPath("$[1].lastName").value("Doe"));
    }

    @Test
    @WithMockUser(username = "user", roles = {"TEACHER"})
    void testGetUsersByRole() throws Exception {
        People people1 = new People();
        people1.setFirstName("John");
        people1.setLastName("Doe");
        people1.setEmail("jo@gg.com");
        people1.setRole(People.Role.STUDENT);
        People people2 = new People();
        people2.setFirstName("Jane");
        people2.setLastName("Doe");
        people2.setEmail("ja@gg.com");
        people2.setRole(People.Role.STUDENT);

        List<People> peopleList = List.of(people1, people2);

        when(peopleService.getPeopleByRole("STUDENT")).thenReturn(peopleList);
        Authentication auth = Mockito.mock(Authentication.class);

        
        mockMvc.perform(get("/api/users/role/STUDENT")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].firstName").value("John"))
                .andExpect(jsonPath("$[1].firstName").value("Jane"));
    }

}
