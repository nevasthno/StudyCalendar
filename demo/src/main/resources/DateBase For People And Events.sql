CREATE DATABASE IF NOT EXISTS `PeopleAndEvents`
  CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE `PeopleAndEvents`;

CREATE TABLE IF NOT EXISTS `users` (
  `id` BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  `first_name` VARCHAR(50) NOT NULL,
  `last_name`  VARCHAR(50) NOT NULL,
  `email`      VARCHAR(100) NOT NULL UNIQUE,
  `password_hash` VARCHAR(255) NOT NULL,
  `role`       ENUM('TEACHER','STUDENT','PARENT') NOT NULL,
  `about_me`   TEXT NULL,
  `date_of_birth` DATE NULL
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS `events` (
  `id` BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  `title` VARCHAR(100) NOT NULL,
  `content` TEXT NULL,
  `location_or_link` TEXT NULL,
  `duration` INT NOT NULL,
  `start_event` DATETIME NOT NULL,
  `event_type` ENUM('EXAM','TEST','SCHOOL_EVENT','PARENTS_MEETING','PERSONAL') NOT NULL,
  `created_by` BIGINT NOT NULL,
  FOREIGN KEY (`created_by`) REFERENCES `users`(`id`) ON DELETE CASCADE
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS `tasks` (
  `id` BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  `event_id` BIGINT NULL,
  `title` VARCHAR(100) NOT NULL,
  `content` TEXT NULL,
  `deadline` DATETIME NOT NULL,
  `completed` BOOLEAN NOT NULL DEFAULT FALSE,
  FOREIGN KEY (`event_id`) REFERENCES `events`(`id`) ON DELETE SET NULL
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS `invitations` (
  `id` BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  `event_id` BIGINT NOT NULL,
  `user_id`  BIGINT NOT NULL,
  `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (`event_id`) REFERENCES `events`(`id`) ON DELETE CASCADE,
  FOREIGN KEY (`user_id`)  REFERENCES `users`(`id`) ON DELETE CASCADE
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS `comments` (
  `id` BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  `event_id` BIGINT NOT NULL,
  `user_id`  BIGINT NOT NULL,
  `content`  TEXT NOT NULL,
  `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (`event_id`) REFERENCES `events`(`id`) ON DELETE CASCADE,
  FOREIGN KEY (`user_id`)  REFERENCES `users`(`id`) ON DELETE CASCADE
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS `user_task_status` (
  `id` BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  `user_id` BIGINT NOT NULL,
  `task_id` BIGINT NOT NULL,
  `is_completed` BOOLEAN NOT NULL DEFAULT FALSE,
  `completed_at` TIMESTAMP NULL,
  FOREIGN KEY (`user_id`) REFERENCES `users`(`id`) ON DELETE CASCADE,
  FOREIGN KEY (`task_id`) REFERENCES `tasks`(`id`) ON DELETE CASCADE,
  UNIQUE KEY `uq_user_task` (`user_id`,`task_id`)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS `user_invitations_status` (
  `id` BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  `invitation_id` BIGINT NOT NULL,
  `user_id` BIGINT NOT NULL,
  `status` ENUM('pending','accepted','declined') NOT NULL DEFAULT 'pending',
  `updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  FOREIGN KEY (`invitation_id`) REFERENCES `invitations`(`id`) ON DELETE CASCADE,
  FOREIGN KEY (`user_id`) REFERENCES `users`(`id`) ON DELETE CASCADE,
  UNIQUE KEY `uq_invitation_user` (`invitation_id`,`user_id`)
) ENGINE=InnoDB;