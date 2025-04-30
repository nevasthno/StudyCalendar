USE `PeopleAndEvents`;

INSERT INTO `users` (first_name, last_name, email, password_hash, role, about_me, date_of_birth)
VALUES
  -- Special user for you
  ('John',    'Doe',    'you@example.com',
   '$2a$10$69zcW51D.VXXT/b78tS.XupfshEa22/pUBe8Njip4Ykm3TFEVw8LC',
   'TEACHER',
   'I am a test teacher created manually.', '1980-01-01'),
  -- Student
  ('Alice',   'Smith',  'alice.smith@example.com',
   '$2a$10$e0MYzXyjpJS7Pd0RVvHwHeFXoi2oQwH6Gin1QIGB0Jv1g6mJvyroa',
   'STUDENT',
   'Tenth grade student.', '2008-05-15'),
  -- Parent
  ('Robert',  'Brown',  'robert.brown@example.com',
   '$2a$10$xAMYt1SKx8.g3z5zD8GBZOtKpIw7F1Bd7J5QmZPz3qX9tL1YPaN9e',
   'PARENT',
   'Parent of Alice.', '1975-11-22');

INSERT INTO `events` (title, content, location_or_link, duration, start_event, event_type, created_by)
VALUES
  ('Math Exam',     'Written exam on algebra and geometry.', 'Room 101',
   120, '2025-05-10 09:00:00', 'EXAM', 1),
  ('History Quiz',  'Multiple choice questions on world history.', 'Room 202',
   60,  '2025-05-12 11:00:00', 'TEST', 1);

INSERT INTO `tasks` (event_id, title, content, deadline)
VALUES
  (NULL,
   'Math Homework: Solve problems 1–10 on page 42',
   'Complete all ten algebra problems and show all work.',
   '2025-05-01 18:00:00'),
  (NULL,
   'Biology Reading: Chapter 7',
   'Read and summarize Chapter 7: "Cellular Respiration".',
   '2025-05-01 20:00:00'),
  (NULL,
   'History Essay Draft',
   'Write a 500-word draft on "Causes of World War II".',
   '2025-05-01 23:59:00'),
  (NULL,
   'English Vocabulary Quiz',
   'Learn and be ready to use 20 new words from list.',
   '2025-05-01 17:00:00'),
  (NULL,
   'Chemistry Lab Report',
   'Finalize report for yesterday’s titration experiment.',
   '2025-05-01 19:30:00');
