-- phpMyAdmin SQL Dump
-- version 5.2.1
-- https://www.phpmyadmin.net/
--
-- Host: localhost:5222
-- Generation Time: Mar 10, 2025 at 08:05 AM
-- Server version: 10.4.32-MariaDB
-- PHP Version: 8.2.12

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
START TRANSACTION;
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;

--
-- Database: `wnd2`
--

-- --------------------------------------------------------

--
-- Table structure for table `books`
--

CREATE TABLE `books` (
  `book_id` int(11) NOT NULL,
  `title` varchar(255) NOT NULL COMMENT 'Book title',
  `cover_photo` varchar(255) DEFAULT NULL COMMENT 'URL of the book cover',
  `description` text DEFAULT NULL COMMENT 'Brief description of the book',
  `genre` enum('Fantasy','Thriller','Mystery','Thriller Mystery','Youth Fiction','Crime','Horror','Romance','Science Fiction','Adventure','Historical') NOT NULL COMMENT 'genre for each book',
  `language` varchar(50) DEFAULT NULL COMMENT 'Language the book is written in',
  `status` enum('Ongoing','Complete') NOT NULL COMMENT 'Status of the book',
  `created_at` timestamp NOT NULL DEFAULT current_timestamp() COMMENT 'Timestamp when book was added',
  `updated_at` timestamp NOT NULL DEFAULT current_timestamp() ON UPDATE current_timestamp() COMMENT 'Timestamp when book was last updated',
  `view_count` int(11) DEFAULT 0 COMMENT 'view count for each book\r\n'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Triggers `books`
--
DELIMITER $$
CREATE TRIGGER `create_group_on_publish` AFTER INSERT ON `books` FOR EACH ROW BEGIN
    -- Create a community group for the book immediately when it's added
    INSERT INTO community_groups (group_id, admin_id, created_at)
    SELECT NULL, ba.user_id, NOW()
    FROM book_authors ba
    WHERE ba.book_id = NEW.book_id AND ba.role = 'Owner';

    -- Optional: Notify the author
    INSERT INTO notifications (user_id, message, created_at)
    SELECT ba.user_id, CONCAT('Your community group for "', NEW.title, '" has been created!'), NOW()
    FROM book_authors ba
    WHERE ba.book_id = NEW.book_id AND ba.role = 'Owner';
END
$$
DELIMITER ;

-- --------------------------------------------------------

--
-- Table structure for table `book_authors`
--

CREATE TABLE `book_authors` (
  `book_author_id` int(11) NOT NULL,
  `book_id` int(11) NOT NULL,
  `user_id` int(11) NOT NULL,
  `role` enum('Owner','Co-Author') DEFAULT 'Owner' COMMENT 'Defines the primary author and co-authors',
  `created_at` timestamp NOT NULL DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Table structure for table `chapters`
--

CREATE TABLE `chapters` (
  `chapter_id` int(11) NOT NULL,
  `book_id` int(11) NOT NULL COMMENT 'Book ID the chapter belongs to',
  `author_id` int(11) NOT NULL,
  `chapter_number` int(11) NOT NULL COMMENT 'Sequential chapter number',
  `content` longtext NOT NULL COMMENT 'Chapter content (preserves long text and formatting)',
  `created_at` timestamp NOT NULL DEFAULT current_timestamp() COMMENT 'Timestamp when chapter was created',
  `updated_at` timestamp NOT NULL DEFAULT current_timestamp() ON UPDATE current_timestamp() COMMENT 'Timestamp when chapter was last updated'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Triggers `chapters`
--
DELIMITER $$
CREATE TRIGGER `prevent_chapter_insert_if_book_complete` BEFORE INSERT ON `chapters` FOR EACH ROW BEGIN
    DECLARE book_status ENUM('Ongoing', 'Complete');

    -- Get the status of the book
    SELECT status INTO book_status FROM books WHERE book_id = NEW.book_id;

    -- If the book is marked as 'Complete', prevent the insert
    IF book_status = 'Complete' THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'Cannot add a chapter to a completed book.';
    END IF;
END
$$
DELIMITER ;

-- --------------------------------------------------------

--
-- Table structure for table `chat_messages`
--

CREATE TABLE `chat_messages` (
  `message_id` int(11) NOT NULL,
  `group_id` int(11) NOT NULL,
  `sender_id` int(11) NOT NULL,
  `message` text NOT NULL,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Table structure for table `collaboration_invites`
--

CREATE TABLE `collaboration_invites` (
  `invite_id` int(11) NOT NULL,
  `book_id` int(11) NOT NULL,
  `inviter_id` int(11) NOT NULL,
  `invitee_email` varchar(255) NOT NULL,
  `invite_code` varchar(10) NOT NULL,
  `status` enum('Pending','Accepted','Declined') DEFAULT 'Pending',
  `created_at` timestamp NOT NULL DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Triggers `collaboration_invites`
--
DELIMITER $$
CREATE TRIGGER `add_co_author_after_invite` AFTER UPDATE ON `collaboration_invites` FOR EACH ROW BEGIN
    IF NEW.status = 'Accepted' THEN
        INSERT INTO book_authors (book_id, user_id, role, created_at)
        SELECT NEW.book_id, u.user_id, 'Co-Author', NOW()
        FROM users u
        WHERE u.email = NEW.invitee_email;
    END IF;
END
$$
DELIMITER ;

-- --------------------------------------------------------

--
-- Table structure for table `community_groups`
--

CREATE TABLE `community_groups` (
  `group_id` int(11) NOT NULL,
  `admin_id` int(11) NOT NULL COMMENT 'User ID of the group admin',
  `created_at` timestamp NOT NULL DEFAULT current_timestamp() COMMENT 'Timestamp when group was created'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Table structure for table `contests`
--

CREATE TABLE `contests` (
  `contest_id` int(11) NOT NULL,
  `title` varchar(255) NOT NULL COMMENT 'Title of the contest',
  `genre` enum('Fantasy','Thriller Mystery','Youth Fiction','Crime','Horror') NOT NULL COMMENT 'Different sections for the contest',
  `created_at` timestamp NOT NULL DEFAULT current_timestamp() COMMENT 'Timestamp when contest was created'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Table structure for table `contest_entries`
--

CREATE TABLE `contest_entries` (
  `entry_id` int(11) NOT NULL,
  `contest_id` int(11) NOT NULL COMMENT 'Contest ID the entry is for',
  `user_id` int(11) NOT NULL COMMENT 'User ID of the participant',
  `submission_date` timestamp NOT NULL DEFAULT current_timestamp() COMMENT 'Timestamp of submission',
  `vote_count` int(11) DEFAULT 0 COMMENT 'Number of votes received',
  `entry_title` varchar(255) NOT NULL COMMENT 'Name of the content',
  `content` longtext NOT NULL COMMENT 'The actual content of the entry (the story or written piece).'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Table structure for table `contest_votes`
--

CREATE TABLE `contest_votes` (
  `vote_id` int(11) NOT NULL,
  `contest_entry_id` int(11) NOT NULL COMMENT 'Contest entry ID the vote is for',
  `user_id` int(11) NOT NULL COMMENT 'User ID of the voter',
  `vote_value` tinyint(1) NOT NULL COMMENT 'Vote (e.g., TRUE for vote, FALSE for un-vote)',
  `created_at` timestamp NOT NULL DEFAULT current_timestamp() COMMENT 'Timestamp when vote was cast'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Triggers `contest_votes`
--
DELIMITER $$
CREATE TRIGGER `prevent_self_vote` BEFORE INSERT ON `contest_votes` FOR EACH ROW BEGIN
    DECLARE entry_author_id INT;
    
    -- Get the author ID of the contest entry being voted on
    SELECT user_id INTO entry_author_id
    FROM Contest_Entries
    WHERE entry_id = NEW.contest_entry_id;
    
    -- Check if the user is trying to vote for their own entry
    IF NEW.user_id = entry_author_id THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'Users cannot vote for their own contest entry';
    END IF;
END
$$
DELIMITER ;

-- --------------------------------------------------------

--
-- Table structure for table `group_members`
--

CREATE TABLE `group_members` (
  `membership_id` int(11) NOT NULL,
  `group_id` int(11) NOT NULL COMMENT 'Group ID',
  `user_id` int(11) NOT NULL COMMENT 'User ID of the group member',
  `joined_at` timestamp NOT NULL DEFAULT current_timestamp() COMMENT 'Timestamp when user joined'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Table structure for table `profile_views`
--

CREATE TABLE `profile_views` (
  `view_id` int(11) NOT NULL,
  `viewer_id` int(11) DEFAULT NULL COMMENT 'User viewing the profile',
  `profile_id` int(11) NOT NULL COMMENT 'Profile being viewed',
  `viewed_at` timestamp NOT NULL DEFAULT current_timestamp() COMMENT 'Timestamp when the profile was viewed'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Table structure for table `ratings`
--

CREATE TABLE `ratings` (
  `rating_id` int(11) NOT NULL,
  `book_id` int(11) NOT NULL COMMENT 'Book ID being rated',
  `user_id` int(11) NOT NULL COMMENT 'User ID of the reviewer',
  `rating` tinyint(4) NOT NULL COMMENT 'Rating value (1 to 5)',
  `comment` text DEFAULT NULL COMMENT 'User comment or review',
  `created_at` timestamp NOT NULL DEFAULT current_timestamp() COMMENT 'Timestamp when rating was submitted'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Table structure for table `support`
--

CREATE TABLE `support` (
  `support_id` int(11) NOT NULL,
  `user_id` int(11) NOT NULL COMMENT 'User sending support',
  `author_id` int(11) NOT NULL COMMENT 'Author receiving support',
  `amount` decimal(10,2) NOT NULL COMMENT 'Support amount in currency',
  `message` text DEFAULT NULL COMMENT 'Optional message from supporter',
  `created_at` timestamp NOT NULL DEFAULT current_timestamp() COMMENT 'Timestamp when support was sent'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Triggers `support`
--
DELIMITER $$
CREATE TRIGGER `prevent_self_support` BEFORE INSERT ON `support` FOR EACH ROW BEGIN
    IF NEW.user_id = NEW.author_id THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'Users cannot support themselves';
    END IF;
END
$$
DELIMITER ;

-- --------------------------------------------------------

--
-- Table structure for table `users`
--

CREATE TABLE `users` (
  `user_id` int(11) NOT NULL,
  `username` varchar(50) NOT NULL COMMENT 'Unique username for login',
  `password` varchar(255) NOT NULL COMMENT 'Hashed password for security',
  `email` varchar(100) NOT NULL COMMENT 'Unique email address',
  `role` enum('Admin','Regular User') NOT NULL COMMENT 'User roles (Admin/Regular User)',
  `profile_picture` varchar(255) DEFAULT NULL COMMENT 'Profile picture URL',
  `books_read_count` int(11) DEFAULT 0 COMMENT 'Number of books read by the user',
  `works_created_count` int(11) DEFAULT 0 COMMENT 'Number of books created by the user',
  `created_at` timestamp NOT NULL DEFAULT current_timestamp() COMMENT 'Account creation timestamp',
  `books_listed_count` int(11) DEFAULT 0 COMMENT 'Number of books saved by user'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `users`
--

INSERT INTO `users` (`user_id`, `username`, `password`, `email`, `role`, `profile_picture`, `books_read_count`, `works_created_count`, `created_at`, `books_listed_count`) VALUES
(11, 'alice_writer', 'hashed_password1', 'alice@example.com', 'Regular User', 'alice.jpg', 5, 2, '2025-03-09 10:54:49', 0),
(12, 'bob_novelist', 'hashed_password2', 'bob@example.com', 'Regular User', 'bob.jpg', 3, 1, '2025-03-09 10:54:49', 0),
(13, 'charlie_poet', 'hashed_password3', 'charlie@example.com', 'Regular User', 'charlie.jpg', 8, 3, '2025-03-09 10:54:49', 0),
(14, 'david_storyteller', 'hashed_password4', 'david@example.com', 'Regular User', 'david.jpg', 10, 4, '2025-03-09 10:54:49', 0),
(15, 'emma_fantasy', 'hashed_password5', 'emma@example.com', 'Regular User', 'emma.jpg', 2, 1, '2025-03-09 10:54:49', 0),
(16, 'frank_mystery', 'hashed_password6', 'frank@example.com', 'Regular User', 'frank.jpg', 7, 2, '2025-03-09 10:54:49', 0),
(17, 'grace_thriller', 'hashed_password7', 'grace@example.com', 'Regular User', 'grace.jpg', 4, 2, '2025-03-09 10:54:49', 0),
(18, 'henry_horror', 'hashed_password8', 'henry@example.com', 'Regular User', 'henry.jpg', 6, 1, '2025-03-09 10:54:49', 0),
(19, 'isabel_youth', 'hashed_password9', 'isabel@example.com', 'Regular User', 'isabel.jpg', 9, 3, '2025-03-09 10:54:49', 0),
(20, 'jack_crime', 'hashed_password10', 'jack@example.com', 'Regular User', 'jack.jpg', 1, 1, '2025-03-09 10:54:49', 0);

-- --------------------------------------------------------

--
-- Table structure for table `user_books`
--

CREATE TABLE `user_books` (
  `user_book_id` int(11) NOT NULL,
  `user_id` int(11) NOT NULL COMMENT 'User who added the book',
  `book_id` int(11) NOT NULL COMMENT 'Book ID being added',
  `status` enum('Reading','Completed','Dropped') DEFAULT 'Reading' COMMENT 'Reading status',
  `created_at` timestamp NOT NULL DEFAULT current_timestamp() COMMENT 'Timestamp when added'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Triggers `user_books`
--
DELIMITER $$
CREATE TRIGGER `add_reader_to_group` AFTER INSERT ON `user_books` FOR EACH ROW BEGIN
    DECLARE group_id INT;

    -- Find the group related to the book
    SELECT cg.group_id INTO group_id
    FROM community_groups cg
    JOIN book_authors ba ON cg.admin_id = ba.user_id
    WHERE ba.book_id = NEW.book_id
    LIMIT 1;

    -- If a group exists, add the reader as a member
    IF group_id IS NOT NULL THEN
        INSERT INTO group_members (group_id, user_id, joined_at)
        VALUES (group_id, NEW.user_id, NOW());
    END IF;
END
$$
DELIMITER ;
DELIMITER $$
CREATE TRIGGER `update_books_listed_after_delete` AFTER DELETE ON `user_books` FOR EACH ROW BEGIN
    UPDATE Users 
    SET books_listed_count = books_listed_count - 1 
    WHERE user_id = OLD.user_id;
END
$$
DELIMITER ;
DELIMITER $$
CREATE TRIGGER `update_books_listed_after_insert` AFTER INSERT ON `user_books` FOR EACH ROW BEGIN
    UPDATE Users 
    SET books_listed_count = books_listed_count + 1 
    WHERE user_id = NEW.user_id;
END
$$
DELIMITER ;

-- --------------------------------------------------------

--
-- Table structure for table `user_followers`
--

CREATE TABLE `user_followers` (
  `follow_id` int(11) NOT NULL,
  `follower_id` int(11) NOT NULL COMMENT 'User ID of the follower',
  `following_id` int(11) NOT NULL COMMENT 'User ID being followed',
  `created_at` timestamp NOT NULL DEFAULT current_timestamp() COMMENT 'Timestamp when follow action occurred'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Triggers `user_followers`
--
DELIMITER $$
CREATE TRIGGER `update_followers_count_after_delete` AFTER DELETE ON `user_followers` FOR EACH ROW BEGIN
    UPDATE Users 
    SET followers_count = followers_count - 1 
    WHERE user_id = OLD.following_id;
END
$$
DELIMITER ;
DELIMITER $$
CREATE TRIGGER `update_followers_count_after_insert` AFTER INSERT ON `user_followers` FOR EACH ROW BEGIN
    UPDATE Users 
    SET followers_count = followers_count + 1 
    WHERE user_id = NEW.following_id;
END
$$
DELIMITER ;
DELIMITER $$
CREATE TRIGGER `update_following_count_after_delete` AFTER DELETE ON `user_followers` FOR EACH ROW BEGIN
    UPDATE Users 
    SET following_count = following_count - 1 
    WHERE user_id = OLD.follower_id;
END
$$
DELIMITER ;
DELIMITER $$
CREATE TRIGGER `update_following_count_after_insert` AFTER INSERT ON `user_followers` FOR EACH ROW BEGIN
    UPDATE Users 
    SET following_count = following_count + 1 
    WHERE user_id = NEW.follower_id;
END
$$
DELIMITER ;

-- --------------------------------------------------------

--
-- Table structure for table `user_group_status`
--

CREATE TABLE `user_group_status` (
  `user_id` int(11) NOT NULL,
  `group_id` int(11) NOT NULL,
  `status` enum('joined','left') DEFAULT 'joined'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Triggers `user_group_status`
--
DELIMITER $$
CREATE TRIGGER `remove_reader_from_group` AFTER UPDATE ON `user_group_status` FOR EACH ROW BEGIN
    -- Check if the user has left the group
    IF NEW.status = 'left' THEN
        DELETE FROM group_members 
        WHERE group_id = NEW.group_id AND user_id = NEW.user_id;
    END IF;
END
$$
DELIMITER ;

--
-- Indexes for dumped tables
--

--
-- Indexes for table `books`
--
ALTER TABLE `books`
  ADD PRIMARY KEY (`book_id`);

--
-- Indexes for table `book_authors`
--
ALTER TABLE `book_authors`
  ADD PRIMARY KEY (`book_author_id`),
  ADD UNIQUE KEY `book_id` (`book_id`,`user_id`),
  ADD KEY `user_id` (`user_id`);

--
-- Indexes for table `chapters`
--
ALTER TABLE `chapters`
  ADD PRIMARY KEY (`chapter_id`),
  ADD UNIQUE KEY `book_id` (`book_id`,`chapter_number`) COMMENT 'Ensures chapter numbers are unique within a book',
  ADD KEY `author_id` (`author_id`);

--
-- Indexes for table `chat_messages`
--
ALTER TABLE `chat_messages`
  ADD PRIMARY KEY (`message_id`),
  ADD KEY `group_id` (`group_id`),
  ADD KEY `sender_id` (`sender_id`);

--
-- Indexes for table `collaboration_invites`
--
ALTER TABLE `collaboration_invites`
  ADD PRIMARY KEY (`invite_id`),
  ADD UNIQUE KEY `invite_code` (`invite_code`),
  ADD KEY `book_id` (`book_id`),
  ADD KEY `inviter_id` (`inviter_id`);

--
-- Indexes for table `community_groups`
--
ALTER TABLE `community_groups`
  ADD PRIMARY KEY (`group_id`),
  ADD KEY `admin_id` (`admin_id`);

--
-- Indexes for table `contests`
--
ALTER TABLE `contests`
  ADD PRIMARY KEY (`contest_id`);

--
-- Indexes for table `contest_entries`
--
ALTER TABLE `contest_entries`
  ADD PRIMARY KEY (`entry_id`),
  ADD KEY `contest_id` (`contest_id`),
  ADD KEY `user_id` (`user_id`);

--
-- Indexes for table `contest_votes`
--
ALTER TABLE `contest_votes`
  ADD PRIMARY KEY (`vote_id`),
  ADD UNIQUE KEY `contest_entry_id` (`contest_entry_id`,`user_id`) COMMENT 'Ensures a user can only vote once per contest entry',
  ADD KEY `user_id` (`user_id`);

--
-- Indexes for table `group_members`
--
ALTER TABLE `group_members`
  ADD PRIMARY KEY (`membership_id`),
  ADD UNIQUE KEY `group_id` (`group_id`,`user_id`) COMMENT 'Ensures a user can only join a group once',
  ADD KEY `user_id` (`user_id`);

--
-- Indexes for table `profile_views`
--
ALTER TABLE `profile_views`
  ADD PRIMARY KEY (`view_id`),
  ADD KEY `viewer_id` (`viewer_id`),
  ADD KEY `profile_id` (`profile_id`);

--
-- Indexes for table `ratings`
--
ALTER TABLE `ratings`
  ADD PRIMARY KEY (`rating_id`),
  ADD UNIQUE KEY `book_id` (`book_id`,`user_id`) COMMENT 'Ensures a user can only rate a book once',
  ADD KEY `user_id` (`user_id`);

--
-- Indexes for table `support`
--
ALTER TABLE `support`
  ADD PRIMARY KEY (`support_id`),
  ADD KEY `user_id` (`user_id`),
  ADD KEY `author_id` (`author_id`);

--
-- Indexes for table `users`
--
ALTER TABLE `users`
  ADD PRIMARY KEY (`user_id`),
  ADD UNIQUE KEY `username` (`username`),
  ADD UNIQUE KEY `email` (`email`);

--
-- Indexes for table `user_books`
--
ALTER TABLE `user_books`
  ADD PRIMARY KEY (`user_book_id`),
  ADD UNIQUE KEY `user_id` (`user_id`,`book_id`) COMMENT 'Prevents duplicate book entries',
  ADD KEY `book_id` (`book_id`);

--
-- Indexes for table `user_followers`
--
ALTER TABLE `user_followers`
  ADD PRIMARY KEY (`follow_id`),
  ADD UNIQUE KEY `follower_id` (`follower_id`,`following_id`) COMMENT 'Ensures a user can only follow another user once',
  ADD KEY `following_id` (`following_id`);

--
-- Indexes for table `user_group_status`
--
ALTER TABLE `user_group_status`
  ADD PRIMARY KEY (`user_id`,`group_id`),
  ADD KEY `group_id` (`group_id`);

--
-- AUTO_INCREMENT for dumped tables
--

--
-- AUTO_INCREMENT for table `books`
--
ALTER TABLE `books`
  MODIFY `book_id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=11;

--
-- AUTO_INCREMENT for table `book_authors`
--
ALTER TABLE `book_authors`
  MODIFY `book_author_id` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `chapters`
--
ALTER TABLE `chapters`
  MODIFY `chapter_id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=13;

--
-- AUTO_INCREMENT for table `chat_messages`
--
ALTER TABLE `chat_messages`
  MODIFY `message_id` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `collaboration_invites`
--
ALTER TABLE `collaboration_invites`
  MODIFY `invite_id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=2;

--
-- AUTO_INCREMENT for table `community_groups`
--
ALTER TABLE `community_groups`
  MODIFY `group_id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=11;

--
-- AUTO_INCREMENT for table `contests`
--
ALTER TABLE `contests`
  MODIFY `contest_id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=11;

--
-- AUTO_INCREMENT for table `contest_entries`
--
ALTER TABLE `contest_entries`
  MODIFY `entry_id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=11;

--
-- AUTO_INCREMENT for table `contest_votes`
--
ALTER TABLE `contest_votes`
  MODIFY `vote_id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=11;

--
-- AUTO_INCREMENT for table `group_members`
--
ALTER TABLE `group_members`
  MODIFY `membership_id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=11;

--
-- AUTO_INCREMENT for table `profile_views`
--
ALTER TABLE `profile_views`
  MODIFY `view_id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=7;

--
-- AUTO_INCREMENT for table `ratings`
--
ALTER TABLE `ratings`
  MODIFY `rating_id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=11;

--
-- AUTO_INCREMENT for table `support`
--
ALTER TABLE `support`
  MODIFY `support_id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=11;

--
-- AUTO_INCREMENT for table `users`
--
ALTER TABLE `users`
  MODIFY `user_id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=21;

--
-- AUTO_INCREMENT for table `user_books`
--
ALTER TABLE `user_books`
  MODIFY `user_book_id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=11;

--
-- AUTO_INCREMENT for table `user_followers`
--
ALTER TABLE `user_followers`
  MODIFY `follow_id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=11;

--
-- Constraints for dumped tables
--

--
-- Constraints for table `book_authors`
--
ALTER TABLE `book_authors`
  ADD CONSTRAINT `book_authors_ibfk_1` FOREIGN KEY (`book_id`) REFERENCES `books` (`book_id`) ON DELETE CASCADE,
  ADD CONSTRAINT `book_authors_ibfk_2` FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`) ON DELETE CASCADE;

--
-- Constraints for table `chapters`
--
ALTER TABLE `chapters`
  ADD CONSTRAINT `chapters_ibfk_1` FOREIGN KEY (`book_id`) REFERENCES `books` (`book_id`) ON DELETE CASCADE,
  ADD CONSTRAINT `chapters_ibfk_2` FOREIGN KEY (`author_id`) REFERENCES `users` (`user_id`) ON DELETE CASCADE;

--
-- Constraints for table `chat_messages`
--
ALTER TABLE `chat_messages`
  ADD CONSTRAINT `chat_messages_ibfk_1` FOREIGN KEY (`group_id`) REFERENCES `community_groups` (`group_id`) ON DELETE CASCADE,
  ADD CONSTRAINT `chat_messages_ibfk_2` FOREIGN KEY (`sender_id`) REFERENCES `users` (`user_id`) ON DELETE CASCADE;

--
-- Constraints for table `collaboration_invites`
--
ALTER TABLE `collaboration_invites`
  ADD CONSTRAINT `collaboration_invites_ibfk_1` FOREIGN KEY (`book_id`) REFERENCES `books` (`book_id`) ON DELETE CASCADE,
  ADD CONSTRAINT `collaboration_invites_ibfk_2` FOREIGN KEY (`inviter_id`) REFERENCES `users` (`user_id`) ON DELETE CASCADE;

--
-- Constraints for table `community_groups`
--
ALTER TABLE `community_groups`
  ADD CONSTRAINT `community_groups_ibfk_1` FOREIGN KEY (`admin_id`) REFERENCES `users` (`user_id`);

--
-- Constraints for table `contest_entries`
--
ALTER TABLE `contest_entries`
  ADD CONSTRAINT `contest_entries_ibfk_1` FOREIGN KEY (`contest_id`) REFERENCES `contests` (`contest_id`) ON DELETE CASCADE,
  ADD CONSTRAINT `contest_entries_ibfk_2` FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`) ON DELETE CASCADE;

--
-- Constraints for table `contest_votes`
--
ALTER TABLE `contest_votes`
  ADD CONSTRAINT `contest_votes_ibfk_1` FOREIGN KEY (`contest_entry_id`) REFERENCES `contest_entries` (`entry_id`) ON DELETE CASCADE,
  ADD CONSTRAINT `contest_votes_ibfk_2` FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`) ON DELETE CASCADE;

--
-- Constraints for table `group_members`
--
ALTER TABLE `group_members`
  ADD CONSTRAINT `group_members_ibfk_1` FOREIGN KEY (`group_id`) REFERENCES `community_groups` (`group_id`) ON DELETE CASCADE,
  ADD CONSTRAINT `group_members_ibfk_2` FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`) ON DELETE CASCADE;

--
-- Constraints for table `profile_views`
--
ALTER TABLE `profile_views`
  ADD CONSTRAINT `profile_views_ibfk_1` FOREIGN KEY (`viewer_id`) REFERENCES `users` (`user_id`) ON DELETE SET NULL,
  ADD CONSTRAINT `profile_views_ibfk_2` FOREIGN KEY (`profile_id`) REFERENCES `users` (`user_id`) ON DELETE CASCADE;

--
-- Constraints for table `ratings`
--
ALTER TABLE `ratings`
  ADD CONSTRAINT `ratings_ibfk_1` FOREIGN KEY (`book_id`) REFERENCES `books` (`book_id`) ON DELETE CASCADE,
  ADD CONSTRAINT `ratings_ibfk_2` FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`) ON DELETE CASCADE;

--
-- Constraints for table `support`
--
ALTER TABLE `support`
  ADD CONSTRAINT `support_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`) ON DELETE CASCADE,
  ADD CONSTRAINT `support_ibfk_2` FOREIGN KEY (`author_id`) REFERENCES `users` (`user_id`) ON DELETE CASCADE;

--
-- Constraints for table `user_books`
--
ALTER TABLE `user_books`
  ADD CONSTRAINT `user_books_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`) ON DELETE CASCADE,
  ADD CONSTRAINT `user_books_ibfk_2` FOREIGN KEY (`book_id`) REFERENCES `books` (`book_id`) ON DELETE CASCADE;

--
-- Constraints for table `user_followers`
--
ALTER TABLE `user_followers`
  ADD CONSTRAINT `user_followers_ibfk_1` FOREIGN KEY (`follower_id`) REFERENCES `users` (`user_id`) ON DELETE CASCADE,
  ADD CONSTRAINT `user_followers_ibfk_2` FOREIGN KEY (`following_id`) REFERENCES `users` (`user_id`) ON DELETE CASCADE;

--
-- Constraints for table `user_group_status`
--
ALTER TABLE `user_group_status`
  ADD CONSTRAINT `user_group_status_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`),
  ADD CONSTRAINT `user_group_status_ibfk_2` FOREIGN KEY (`group_id`) REFERENCES `community_groups` (`group_id`);
COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
