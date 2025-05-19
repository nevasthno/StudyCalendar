# StudyCalendar

**Your all-in-one platform for managing school events, tasks, and user profiles.**

Welcome to StudyCalendar—a dynamic web application that empowers teachers, students, and parents to stay organized and connected. Below is a quick overview of what StudyCalendar offers and how to get started.

---

## Key Features

* **User Roles & Profiles**

  * **Teacher**: Create and manage tasks, events, and users within their school and class.
  * **Student**: View assigned tasks and upcoming events.
  * **Parent**: Receive invitations to parents’ meetings and browse the shared calendar.

* **Task Management**

  * Teachers can create, toggle completion, and filter tasks by school and class.
  * Students see only today’s tasks and can mark them complete.

* **Event Calendar**

  * Shared, interactive monthly calendar for all users.
  * Teachers schedule exams, tests, school events, parents’ meetings, and personal appointments.
  * Students and parents view relevant events based on their school and class assignments.

* **Invitations & Notifications**

  * Parents automatically receive invitations to parents’ meetings.
  * Status indicators (pending, accepted, declined) help track responses.

* **Statistics Dashboard**

  * Teachers access real-time stats: total tasks, completed tasks, and created events.

---

## Getting Started

1. **Clone the Repository**

   ```bash
   git clone https://github.com/nevasthno/StudyCalendar.git
   ```

2. **Set Up the Database**

   * Create `PeopleAndEvents` MySQL database.
   * Run provided SQL schema to initialize tables for schools, classes, users, events, tasks, invitations, and comments.
   * Seed with sample data: at least three schools, classes 1A–11C, and a few users.

2. **Create the `application.properties`**

   * in `demo\src\main\resources` create file `application.properties`
   * in this file write:
```
spring.datasource.url=jdbc:mysql://localhost:3306/peopleandevents?useSSL=false&serverTimezone=UTC
spring.datasource.username=root
spring.datasource.password=YOUR DATABASE PASSWORD
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver
spring.profiles.active=dev

spring.jpa.hibernate.ddl-auto=none
spring.jpa.show-sql=true
spring.jpa.database-platform=org.hibernate.dialect.MySQLDialect

spring.sql.init.mode=never
spring.sql.init.schema-locations=classpath:DateBase\ For\ People\ And\ Events.sql

app.jwt.secret=very-long-secret-key
app.jwt.expirationMs=3600000
app.jwt.issuer=study-calendar
```
   * in `demo\src\test\resources` create file `application-test.properties`
   * in this file write:
```
app.jwt.secret=very-long-secret-key
app.jwt.expiration-ms=3600000
app.jwt.issuer=study-calendar
```

4. **Configure the Application**

   * In `application.properties`, set your database URL, username, and password.
   * Configure JWT secret and expiration.

5. **Build & Run**

   ```bash
   ./mvnw spring-boot:run
   ```

   or package into a JAR and run:

   ```bash
   ./mvnw clean package
   java -jar target/studycalendar-0.1.0.jar
   ```

---

## Usage

* **Login & Authentication**

  * Navigate to `/login.html` and enter your credentials.
  * Role-based access ensures only teachers can create or modify data.

* **Teacher Dashboard**

  * Accessible at `/teacher.html`.
  * Select your school and class from the dropdowns.
  * Create users, tasks, and events; view statistics.

* **Main Student View**

  * Accessible at `/main.html`.
  * Automatically shows your tasks for today, calendar, and teacher list.

* **Parent Panel**

  * Accessible at `/parent.html`.
  * View invitations for parents’ meetings and browse the calendar.

---

## API Endpoints

* **Authentication**

  * `POST /api/login` — returns JWT.
* **Profile**

  * `GET /api/me` — your user data, including `schoolId` and `classId`.
* **Schools & Classes**

  * `GET /api/schools`
  * `GET /api/classes?schoolId={id}`
* **Users & Teachers**

  * `GET /api/users`, `GET /api/teachers` (filters by school/class)
  * `POST /api/users` (teachers only)
* **Tasks**

  * `GET /api/tasks?schoolId=&classId=`
  * `POST /api/tasks` (teachers only)
  * `POST /api/tasks/{id}/toggle-complete`
* **Events**

  * `GET /api/events?schoolId=&classId=`
  * `POST /api/events` (teachers only)
* **Statistics**

  * `GET /api/stats?schoolId=&classId=`

---

## Tech Stack

* **Backend**: Spring Boot, Spring Security (JWT), Spring Data JPA, MySQL
* **Frontend**: Vanilla JavaScript, HTML5, CSS3
* **Authentication**: JWT tokens stored in `localStorage`

---

## Contributing

We welcome your feedback, bug reports, and pull requests! Please:

1. Fork the repository
2. Create a feature branch
3. Commit and push your changes
4. Open a pull request with a clear description

---

Thank you for using StudyCalendar! We hope this tool helps your school community stay organized and connected.
