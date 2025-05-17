package com.example.demo.javaSrc.worker;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.javaSrc.eventsANDtask.Event;
import com.example.demo.javaSrc.eventsANDtask.EventService;
import com.example.demo.javaSrc.eventsANDtask.Task;
import com.example.demo.javaSrc.eventsANDtask.TaskService;
import com.example.demo.javaSrc.people.People;
import com.example.demo.javaSrc.people.PeopleService;
import com.example.demo.javaSrc.school.School;
import com.example.demo.javaSrc.school.SchoolService;
import com.example.demo.javaSrc.school.SchoolClass;
import com.example.demo.javaSrc.school.ClassService;

@RestController
@RequestMapping("/api")
public class ApiController {

    private final TaskService taskService;
    private final EventService eventService;
    private final PeopleService peopleService;
    private final PasswordEncoder passwordEncoder;
    private final SchoolService schoolService;
    private final ClassService classService;

    @Autowired
    public ApiController(TaskService taskService,
                         EventService eventService,
                         PeopleService peopleService,
                         PasswordEncoder passwordEncoder,
                         SchoolService schoolService,
                         ClassService classService) {
        this.taskService     = taskService;
        this.eventService    = eventService;
        this.peopleService   = peopleService;
        this.passwordEncoder = passwordEncoder;
        this.schoolService   = schoolService;
        this.classService    = classService;
    }

    private People currentUser(Authentication auth) {
        return peopleService.findByEmail(auth.getName());
    }

    @GetMapping("/schools")
    public List<School> getAllSchools() {
        return schoolService.getAllSchools();
    }

    @GetMapping("/classes")
    public List<SchoolClass> getClasses(
            @RequestParam Long schoolId) {
        return classService.getBySchoolId(schoolId);
    }

    @GetMapping("/tasks")
    public List<Task> getTasks(
            Authentication auth,
            @RequestParam(required = false) Long schoolId,
            @RequestParam(required = false) Long classId) {

        People me = currentUser(auth);
        Long sch = schoolId != null ? schoolId : me.getSchoolId();
        Long cls = classId != null ? classId : me.getClassId();

        List<Task> tasksForClass = taskService.getBySchoolAndClass(sch, cls);
        List<Task> tasksForAll = taskService.getBySchoolAndClass(sch, null);

        if (classId == null) {
            return tasksForAll;
        } else {
            List<Task> result = new ArrayList<>(tasksForAll);
            result.addAll(tasksForClass);
            return result;
        }
    }

    @GetMapping("/events")
    public List<Event> getEvents(
            Authentication auth,
            @RequestParam(required = false) Long schoolId,
            @RequestParam(required = false) Long classId,
            @RequestParam(required = false) Long userId) {

        People me = currentUser(auth);

        // If userId is specified and not "me", use that user's school/class for filtering
        if (userId != null) {
            People target = peopleService.getAllPeople().stream()
                .filter(u -> u.getId().equals(userId))
                .findFirst().orElse(null);
            if (target == null) {
                return List.of();
            }
            Long sch = target.getSchoolId();
            Long cls = target.getClassId();
            List<Event> events = eventService.getEventsForSchool(sch);
            // Show all school-wide and class-specific events for the selected user
            return events.stream()
                .filter(e -> e.getClassId() == null || (cls != null && cls.equals(e.getClassId())))
                .sorted(Comparator.comparing(Event::getStartEvent))
                .collect(Collectors.toList());
        }

        // Default: show for current user
        Long sch = schoolId != null ? schoolId : me.getSchoolId();
        Long cls = classId != null ? classId : me.getClassId();
        List<Event> events = eventService.getEventsForSchool(sch);
        return events.stream()
            .filter(e -> e.getClassId() == null || (cls != null && cls.equals(e.getClassId())))
            .sorted(Comparator.comparing(Event::getStartEvent))
            .collect(Collectors.toList());
    }

    @GetMapping("/teachers")
    public List<People> getTeachers(
            Authentication auth,
            @RequestParam(required = false) Long schoolId,
            @RequestParam(required = false) Long classId,
            @RequestParam(required = false) String name) {

        People me = currentUser(auth);
        Long sch = schoolId != null ? schoolId : me.getSchoolId();
        Long cls = classId != null ? classId : me.getClassId();

        List<People> teachers;
        if (classId == null) {
            teachers = peopleService.getBySchoolClassAndRole(sch, null, People.Role.TEACHER);
        } else {
            teachers = new ArrayList<>();
            teachers.addAll(peopleService.getBySchoolClassAndRole(sch, null, People.Role.TEACHER));
            teachers.addAll(peopleService.getBySchoolClassAndRole(sch, cls, People.Role.TEACHER));
        }

        if (name != null && !name.isBlank()) {
            teachers.removeIf(p -> 
                !p.getFirstName().toLowerCase().contains(name.toLowerCase()) &&
                !p.getLastName().toLowerCase().contains(name.toLowerCase())
            );
        }
        return teachers;
    }

    @GetMapping("/users")
    public List<People> getUsers(
            Authentication auth,
            @RequestParam(required = false) Long schoolId,
            @RequestParam(required = false) Long classId,
            @RequestParam(required = false) String name) {

        People me = currentUser(auth);
        Long sch = schoolId != null ? schoolId : me.getSchoolId();
        Long cls = classId != null ? classId : me.getClassId();

        List<People> all;
        if (classId == null) {
            all = peopleService.getBySchoolAndClass(sch, null);
        } else {
            all = new ArrayList<>();
            all.addAll(peopleService.getBySchoolAndClass(sch, null));
            all.addAll(peopleService.getBySchoolAndClass(sch, cls));
        }

        if (name != null && !name.isBlank()) {
            all = all.stream()
                     .filter(p -> p.getFirstName().contains(name)
                               || p.getLastName().contains(name))
                     .toList();
        }
        return all;
    }

    @PostMapping("/tasks/{id}/toggle-complete")
    public ResponseEntity<Void> toggleTask(@PathVariable Long id) {
        taskService.toggleComplete(id);
        return ResponseEntity.ok().build();
    }

    @PreAuthorize("hasRole('TEACHER')")
    @PostMapping("/users")
    public ResponseEntity<People> createUser(
            @RequestBody People newUser,
            Authentication auth) {

        People me = currentUser(auth);
        Long sid = newUser.getSchoolId() != null ? newUser.getSchoolId() : me.getSchoolId();
        Long cid = newUser.getClassId()  != null ? newUser.getClassId()  : me.getClassId();

        newUser.setSchoolId(sid);
        newUser.setClassId(cid);

        String rawPass = newUser.getPassword();
        newUser.setPassword(passwordEncoder.encode(rawPass));

        return ResponseEntity.ok(peopleService.createPeople(newUser));
    }


    @PreAuthorize("hasRole('TEACHER')")
    @PostMapping("/tasks")
    public ResponseEntity<Task> createTask(
            @RequestBody Task newTask,
            Authentication auth) {

        People me = currentUser(auth);
        Long sid = newTask.getSchoolId() != null ? newTask.getSchoolId() : me.getSchoolId();
        Long cid = newTask.getClassId();

        newTask.setSchoolId(sid);
        newTask.setClassId(cid);

        return ResponseEntity.ok(taskService.createTask(newTask));
    }

    @PreAuthorize("hasRole('TEACHER')")
    @PostMapping("/events")
    public ResponseEntity<Event> createEvent(
            @RequestBody Map<String,Object> payload,
            Authentication auth) {

        People me = currentUser(auth);

        String title      = (String) payload.get("title");
        String content    = (String) payload.get("content");
        String loc        = (String) payload.get("location_or_link");
        String startRaw   = (String) payload.get("start_event");
        String type       = (String) payload.get("event_type");

        Object durObj     = payload.get("duration");
        int duration = durObj instanceof Number
            ? ((Number) durObj).intValue()
            : Integer.parseInt((String) durObj);

        Long sid = payload.get("schoolId") != null
            ? (payload.get("schoolId") instanceof Number
                ? ((Number) payload.get("schoolId")).longValue()
                : Long.parseLong(payload.get("schoolId").toString()))
            : me.getSchoolId();

        Long cid;
        if (payload.get("classId") == null || payload.get("classId").toString().isBlank()) {
            cid = null;
        } else if (payload.get("classId") instanceof Number) {
            cid = ((Number) payload.get("classId")).longValue();
        } else {
            cid = Long.parseLong(payload.get("classId").toString());
        }

        LocalDateTime startEvent = OffsetDateTime.parse(startRaw)
            .toLocalDateTime();

        Event e = new Event();
        e.setTitle(title);
        e.setContent(content);
        e.setLocationOrLink(loc);
        e.setStartEvent(startEvent);
        e.setDuration(duration);
        e.setEventType(Event.EventType.valueOf(type));
        e.setSchoolId(sid);
        e.setClassId(cid);
        e.setCreatedBy(me.getId());

        Event saved = eventService.createEvent(e);
        return ResponseEntity.ok(saved);
    }


    @PreAuthorize("hasRole('TEACHER')")
    @GetMapping("/stats")
    public Map<String, Long> getStats(
            Authentication auth,
            @RequestParam(required = false) Long schoolId,
            @RequestParam(required = false) Long classId) {

        People me = currentUser(auth);
        Long sch = schoolId != null ? schoolId : me.getSchoolId();
        Long cls = classId  != null ? classId  : me.getClassId();

        long totalTasks     = taskService.getBySchoolAndClass(sch, cls).size();
        long completedTasks = taskService.getBySchoolAndClass(sch, cls)
                                         .stream().filter(Task::isCompleted).count();
        long totalEvents    = eventService.getBySchoolAndClass(sch, cls).size();

        return Map.of(
            "totalTasks",     totalTasks,
            "completedTasks", completedTasks,
            "totalEvents",    totalEvents
        );
    }

    @GetMapping("/future/{userId}")
    public List<Event> getFutureEvents(
            @PathVariable Long userId,
            Authentication auth) {

        People me = currentUser(auth);
        return eventService.getFutureEvents(userId);
    }

    @GetMapping("/past/{userId}")
    public List<Event> getPastEvents(
            @PathVariable Long userId,
            Authentication auth) {

        People me = currentUser(auth);
        return eventService.getPastEvents(userId);
    }

    @GetMapping("/search/title")
    public List<Event> searchByTitle(
            @RequestParam Long userId,
            @RequestParam String keyword,
            Authentication auth) {

        People me = currentUser(auth);
        return eventService.searchByTitle(
            userId, keyword
        );
    }

    @GetMapping("/search/date")
    public List<Event> searchByDateRange(
            @RequestParam Long userId,
            @RequestParam String from,
            @RequestParam String to,
            Authentication auth) {

        People me = currentUser(auth);
        return eventService.searchByDateRange(
            userId,
            LocalDateTime.parse(from),
            LocalDateTime.parse(to)
        );
    }

    @GetMapping("/me")
    public ResponseEntity<People> getMyProfile(Authentication auth) {
        String email = auth.getName();
        People user = peopleService.findByEmail(email);
        if (user == null) {
            return ResponseEntity.status(401).build();
        }
        return ResponseEntity.ok(user);
    }

    @PutMapping("/me")
    public ResponseEntity<People> updateMyProfile(@RequestBody People updatedData, Authentication auth) {
        String email = auth.getName();
        People currentUser = peopleService.findByEmail(email);
        if (currentUser == null) {
            return ResponseEntity.notFound().build();
        }

        if (updatedData.getEmail() != null && !updatedData.getEmail().equals(email)) {
            if (!isValidEmail(updatedData.getEmail())) {
                return ResponseEntity.badRequest().body(null);
            }
            if (peopleService.findByEmail(updatedData.getEmail()) != null) {
                return ResponseEntity.badRequest().body(null);
            }
        }

        if (updatedData.getFirstName() != null) {
            currentUser.setFirstName(updatedData.getFirstName());
        }
        if (updatedData.getLastName() != null) {
            currentUser.setLastName(updatedData.getLastName());
        }
        if (updatedData.getAboutMe() != null) {
            currentUser.setAboutMe(updatedData.getAboutMe());
        }
        if (updatedData.getDateOfBirth() != null) {
            currentUser.setDateOfBirth(updatedData.getDateOfBirth());
        }
        if (updatedData.getEmail() != null) {
            currentUser.setEmail(updatedData.getEmail());
        }
        if (updatedData.getPassword() != null && !updatedData.getPassword().isEmpty()) {
            currentUser.setPassword(passwordEncoder.encode(updatedData.getPassword()));
        }
        Long userId = currentUser.getId();
        People updated = peopleService.updateUser(userId, currentUser); 
        return ResponseEntity.ok(updated);
    }

    private boolean isValidEmail(String email) {
        return email != null && email.matches("^[A-Za-z0-9+_.-]+@(.+)$");
    }

    @PreAuthorize("hasRole('TEACHER')")
    @GetMapping("/loadUsers")
    public List<People> getAllUsers() {
        return peopleService.getAllPeople();
    }

    @PreAuthorize("hasRole('TEACHER')")
    @GetMapping("/users/role/{role}")
    public List<People> getUsersByRole(@PathVariable String role) {
        return peopleService.getPeopleByRole(role);
    }

    @PreAuthorize("hasRole('TEACHER')")
    @PutMapping("/users/{id}")
    public ResponseEntity<People> updateUserByTeacher(@PathVariable Long id, @RequestBody People updatedData) {
        People updated = peopleService.updateProfile(id, updatedData);
        if (updated != null) {
            return ResponseEntity.ok(updated);
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}
