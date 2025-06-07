-- phpMyAdmin SQL Dump
-- version 5.2.1
-- https://www.phpmyadmin.net/
--
-- Host: localhost:5222
-- Generation Time: Apr 01, 2025 at 09:36 PM
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
-- Database: `z_data`
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
  `status` enum('Ongoing','Complete','Hiatus') NOT NULL COMMENT 'Status of the book',
  `created_at` timestamp NOT NULL DEFAULT current_timestamp() COMMENT 'Timestamp when book was added',
  `updated_at` timestamp NOT NULL DEFAULT current_timestamp() ON UPDATE current_timestamp() COMMENT 'Timestamp when book was last updated',
  `view_count` int(11) DEFAULT 0 COMMENT 'view count for each book\r\n'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `books`
--

INSERT INTO `books` (`book_id`, `title`, `cover_photo`, `description`, `genre`, `status`, `created_at`, `updated_at`, `view_count`) VALUES
(1, 'Bob\'s Mystery Novel', 'bob.png', 'Bob\'s Mystery Novel is a fast-paced mystery about Bob, who searches for his missing friend, Jake. A cryptic note, a hidden tunnel, and a mysterious map lead him on a thrilling chase.', 'Mystery', 'Ongoing', '2025-04-01 18:03:40', '2025-04-01 18:06:56', 0),
(2, 'Lavender Garden', 'lavender.png', 'Lavender Garden is a magical fantasy about Emma, who discovers an enchanted garden hidden near her home. When the garden’s magic starts to fade, Emma must unlock its secrets and restore its power, encountering mystical guardians and dark forces along the way. The fate of the garden—and her world—depends on her.', 'Fantasy', 'Ongoing', '2025-04-01 18:11:39', '2025-04-01 18:11:39', 0);

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

--
-- Dumping data for table `book_authors`
--

INSERT INTO `book_authors` (`book_author_id`, `book_id`, `user_id`, `role`, `created_at`) VALUES
(1, 1, 9, 'Owner', '2025-04-01 18:04:07'),
(2, 1, 7, 'Co-Author', '2025-04-01 18:04:07'),
(3, 2, 4, 'Owner', '2025-04-01 18:13:27'),
(4, 2, 5, 'Co-Author', '2025-04-01 18:13:27');

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
-- Dumping data for table `chapters`
--

INSERT INTO `chapters` (`chapter_id`, `book_id`, `author_id`, `chapter_number`, `content`, `created_at`, `updated_at`) VALUES
(1, 1, 9, 1, 'Bob found the door ajar. His best friend, Jake, had vanished...', '2025-04-01 18:07:36', '2025-04-01 18:07:36'),
(2, 1, 9, 2, 'Beneath the trapdoor, a tunnel stretched into darkness...', '2025-04-01 18:07:36', '2025-04-01 18:07:36'),
(3, 1, 9, 3, 'Bob\'s hands trembled as he reached a rusted chest deep within the passage...', '2025-04-01 18:07:36', '2025-04-01 18:07:36'),
(4, 1, 7, 4, 'The chest creaked open, revealing a faded map...', '2025-04-01 18:07:36', '2025-04-01 18:07:36'),
(5, 1, 7, 5, 'Bob reached the cabin at dusk. It was silent, eerily so...', '2025-04-01 18:07:36', '2025-04-01 18:07:36'),
(6, 1, 9, 6, 'Bob confronted the stranger. It was Jake—alive but terrified...', '2025-04-01 18:07:36', '2025-04-01 18:07:36'),
(7, 1, 7, 7, 'With the red key, map, and their wits, Bob and Jake set a trap...', '2025-04-01 18:07:36', '2025-04-01 18:07:36'),
(1, 2, 4, 1, 'Emma always felt drawn to the abandoned lavender garden near her cottage. One evening, the wind carried whispers through the flowers. As she stepped closer, the petals shimmered, revealing a hidden pathway. Was it just her imagination, or was the garden… alive?', '2025-04-01 18:21:08', '2025-04-01 18:21:08'),
(2, 2, 4, 2, 'Following the glowing lavender path, Emma discovered an ancient stone door covered in vines. A golden butterfly landed on the handle, and the door creaked open. Beyond it lay a realm of floating lights and enchanted trees. But something stirred in the shadows.', '2025-04-01 18:21:08', '2025-04-01 18:21:08'),
(3, 2, 5, 3, 'A mystical figure emerged—a guardian of the garden. “You have awakened the magic,” he said. “But danger follows.” Emma noticed the lavender dimming. If the flowers died, so would the magic. She had one night to restore it before the portal closed forever.', '2025-04-01 18:21:08', '2025-04-01 18:21:08'),
(4, 2, 5, 4, 'Emma raced to the garden’s center, where a crystal pulsed beneath ancient roots. Dark tendrils crept toward it, draining its glow. Summoning all her courage, she touched the crystal. Light burst forth, pushing the darkness away. The garden flourished again—its magic forever connected to Emma’s heart.', '2025-04-01 18:21:08', '2025-04-01 18:21:08');

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

--
-- Dumping data for table `chat_messages`
--

INSERT INTO `chat_messages` (`message_id`, `group_id`, `sender_id`, `message`, `created_at`) VALUES
(1, 1, 9, 'Hello everyone, welcome to Group 1!', '2025-04-01 19:26:23'),
(2, 1, 7, 'Thanks for the invite! Looking forward to it.', '2025-04-01 19:26:23'),
(3, 2, 4, 'Excited for Group 2! Let’s get started.', '2025-04-01 19:26:23'),
(4, 3, 9, 'I’m glad to be in this group, let’s have fun!', '2025-04-01 19:26:23');

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
-- Dumping data for table `collaboration_invites`
--

INSERT INTO `collaboration_invites` (`invite_id`, `book_id`, `inviter_id`, `invitee_email`, `invite_code`, `status`, `created_at`) VALUES
(1, 1, 9, 'grace@example.com', 'INV123456', 'Accepted', '2025-04-01 18:07:54'),
(3, 2, 4, 'frank@example.com', 'INV123457', 'Pending', '2025-04-01 18:21:37'),
(4, 2, 4, 'grace@example.com', 'INV123458', 'Pending', '2025-04-01 18:21:37');

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

--
-- Dumping data for table `community_groups`
--

INSERT INTO `community_groups` (`group_id`, `admin_id`, `created_at`) VALUES
(1, 9, '2025-04-01 19:25:54'),
(2, 4, '2025-04-01 19:25:54'),
(3, 9, '2025-04-01 19:25:54'),
(4, 3, '2025-04-01 19:25:54');

-- --------------------------------------------------------

--
-- Table structure for table `contests`
--

CREATE TABLE `contests` (
  `contest_id` int(11) NOT NULL,
  `title` varchar(255) NOT NULL COMMENT 'Title of the contest',
  `genre` enum('Fantasy','Thriller Mystery','Youth Fiction','Crime Horror') NOT NULL COMMENT 'Different sections for the contest',
  `created_at` timestamp NOT NULL DEFAULT current_timestamp() COMMENT 'Timestamp when contest was created'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `contests`
--

INSERT INTO `contests` (`contest_id`, `title`, `genre`, `created_at`) VALUES
(1, 'Fantasy Writing Contest', 'Fantasy', '2025-04-01 18:26:54'),
(2, 'Thriller Mystery Writing Contest', 'Thriller Mystery', '2025-04-01 18:32:02'),
(3, 'Youth Fiction Writing Contest', 'Youth Fiction', '2025-04-01 18:34:58'),
(4, 'Crime Horror Writing Contest', 'Crime Horror', '2025-04-01 18:41:36');

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

--
-- Dumping data for table `contest_entries`
--

INSERT INTO `contest_entries` (`entry_id`, `contest_id`, `user_id`, `submission_date`, `vote_count`, `entry_title`, `content`) VALUES
(1, 1, 5, '2025-04-01 18:27:04', 0, 'The Enchanted Forest', 'Chapter 1: The Hidden Path – Lily stumbles upon a secret entrance in the forest...'),
(2, 1, 1, '2025-04-01 18:27:04', 0, 'The Enchanted Forest', 'Chapter 2: The Guardians’ Quest – Lily learns she is the chosen one destined to protect the forest...'),
(3, 1, 2, '2025-04-01 18:27:04', 0, 'The Enchanted Forest', 'Chapter 3: The Final Battle – Lily and her allies confront the dark sorcerer trying to destroy the enchanted realm...'),
(4, 1, 4, '2025-04-01 18:27:19', 0, 'The Crystal of Aetheria', 'Chapter 1: The Prophecy Unfolds – Finn learns about the ancient crystal and its importance...'),
(5, 1, 3, '2025-04-01 18:27:19', 0, 'The Crystal of Aetheria', 'Chapter 2: The Journey Begins – Finn and his companions cross dangerous lands filled with monsters...'),
(6, 1, 10, '2025-04-01 18:27:19', 0, 'The Crystal of Aetheria', 'Chapter 3: The Crystal’s Power – Finn faces the guardian of the crystal...'),
(7, 1, 3, '2025-04-01 18:27:29', 0, 'The Lost Kingdom', 'Chapter 1: The Forgotten Map – Aria finds a map leading to a hidden kingdom...'),
(8, 1, 2, '2025-04-01 18:27:29', 0, 'The Lost Kingdom', 'Chapter 2: The Awakening – Aria accidentally awakens an ancient force...'),
(9, 1, 4, '2025-04-01 18:27:29', 0, 'The Lost Kingdom', 'Chapter 3: The Battle for the Kingdom – Aria must fight against the evil forces...'),
(10, 2, 7, '2025-04-01 18:33:11', 0, 'The Midnight Caller', 'Chapter 1: The First Call – Detective Riley receives a chilling call...'),
(11, 2, 7, '2025-04-01 18:33:11', 0, 'The Midnight Caller', 'Chapter 2: The Cryptic Clue – A second call leads Riley to an eerie pattern...'),
(12, 2, 7, '2025-04-01 18:33:11', 0, 'The Midnight Caller', 'Chapter 3: Chasing Shadows – Riley suspects a connection...'),
(13, 2, 7, '2025-04-01 18:33:11', 0, 'The Midnight Caller', 'Chapter 4: The Confrontation – Riley corners the killer...'),
(14, 2, 7, '2025-04-01 18:33:11', 0, 'The Midnight Caller', 'Chapter 5: The Final Decision – In a final showdown...'),
(15, 2, 9, '2025-04-01 18:33:11', 0, 'The Forgotten Witness', 'Chapter 1: The Vanishing – Claire is called to investigate...'),
(16, 2, 9, '2025-04-01 18:33:11', 0, 'The Forgotten Witness', 'Chapter 2: The Mysterious Claim – An elderly man insists...'),
(17, 2, 9, '2025-04-01 18:33:11', 0, 'The Forgotten Witness', 'Chapter 3: Chasing Shadows – Claire discovers disturbing connections...'),
(18, 2, 9, '2025-04-01 18:33:11', 0, 'The Forgotten Witness', 'Chapter 4: The Hidden Link – Claire uncovers a buried conspiracy...'),
(19, 2, 9, '2025-04-01 18:33:11', 0, 'The Forgotten Witness', 'Chapter 5: The Final Revelation – With the clock ticking...'),
(20, 2, 4, '2025-04-01 18:33:11', 0, 'The Vanishing Point', 'Chapter 1: Into the Unknown – Max begins investigating...'),
(21, 3, 1, '2025-04-01 18:35:11', 0, 'The Secret of Pinehill', 'Chapter 1: The Mysterious Map – The friends find the old map...'),
(22, 3, 1, '2025-04-01 18:35:11', 0, 'The Secret of Pinehill', 'Chapter 2: The Hidden Chamber – They race against time...'),
(23, 3, 2, '2025-04-01 18:35:11', 0, 'The Midnight Escape', 'Chapter 1: The Forbidden Park – The friends sneak into an abandoned amusement park...'),
(24, 3, 2, '2025-04-01 18:35:11', 0, 'The Midnight Escape', 'Chapter 2: The Escape – Realizing the park’s eerie powers...'),
(25, 3, 3, '2025-04-01 18:35:11', 0, 'The Lost Key', 'Chapter 1: The Key in the Park – The three friends stumble upon an old key...'),
(26, 3, 3, '2025-04-01 18:35:11', 0, 'The Lost Key', 'Chapter 2: The Hidden Doorway – The friends open the door to a magical world...'),
(27, 4, 6, '2025-04-01 18:41:59', 0, 'Shattered Alibis', 'Chapter 1: The Accusation – Claire takes on the case of a man accused of a high-profile murder.'),
(28, 4, 6, '2025-04-01 18:41:59', 0, 'Shattered Alibis', 'Chapter 2: Unraveling the Truth – Claire uncovers conflicting evidence...'),
(29, 4, 7, '2025-04-01 18:41:59', 0, 'Crimson Lies', 'Chapter 1: The Robbery – Detective Carter investigates a robbery that escalates into a deadly crime.'),
(30, 4, 7, '2025-04-01 18:41:59', 0, 'Crimson Lies', 'Chapter 2: The Trail of Lies – Alex uncovers a series of connections...'),
(31, 4, 7, '2025-04-01 18:41:59', 0, 'Crimson Lies', 'Chapter 3: Confronting the Truth – Alex faces a betrayal...'),
(32, 4, 8, '2025-04-01 18:41:59', 0, 'The Silent Witness', 'Chapter 1: The First Murder – Jenna is assigned to a case that seems like an open-and-shut murder...');

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
-- Dumping data for table `contest_votes`
--

INSERT INTO `contest_votes` (`vote_id`, `contest_entry_id`, `user_id`, `vote_value`, `created_at`) VALUES
(1, 1, 1, 1, '2025-04-01 18:27:39'),
(2, 1, 2, 1, '2025-04-01 18:27:39'),
(3, 1, 3, 1, '2025-04-01 18:27:39'),
(4, 1, 4, 1, '2025-04-01 18:27:39'),
(5, 1, 10, 1, '2025-04-01 18:27:39'),
(6, 4, 3, 1, '2025-04-01 18:28:12'),
(7, 4, 5, 1, '2025-04-01 18:28:12'),
(8, 4, 10, 1, '2025-04-01 18:28:12'),
(9, 7, 2, 1, '2025-04-01 18:28:24'),
(10, 10, 1, 1, '2025-04-01 18:33:30'),
(11, 10, 2, 1, '2025-04-01 18:33:30'),
(12, 15, 3, 1, '2025-04-01 18:33:30'),
(13, 15, 5, 1, '2025-04-01 18:33:30'),
(14, 15, 6, 1, '2025-04-01 18:33:30'),
(15, 21, 2, 1, '2025-04-01 18:35:26'),
(16, 25, 7, 1, '2025-04-01 18:35:26'),
(17, 25, 8, 1, '2025-04-01 18:35:26'),
(18, 25, 9, 1, '2025-04-01 18:35:26'),
(19, 25, 10, 1, '2025-04-01 18:35:26'),
(20, 27, 3, 1, '2025-04-01 18:42:13'),
(21, 27, 4, 1, '2025-04-01 18:42:13'),
(22, 29, 4, 1, '2025-04-01 18:42:13'),
(23, 29, 5, 1, '2025-04-01 18:42:13'),
(24, 32, 7, 1, '2025-04-01 18:42:13');

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
  `joined_at` timestamp NOT NULL DEFAULT current_timestamp() COMMENT 'Timestamp when user joined',
  `online_status` enum('online','offline') DEFAULT 'offline',
  `last_active` timestamp NOT NULL DEFAULT current_timestamp() ON UPDATE current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `group_members`
--

INSERT INTO `group_members` (`membership_id`, `group_id`, `user_id`, `joined_at`, `online_status`, `last_active`) VALUES
(1, 1, 9, '2025-04-01 19:26:10', 'online', '2025-04-01 19:26:10'),
(2, 1, 7, '2025-04-01 19:26:10', 'offline', '2025-04-01 19:26:10'),
(3, 1, 3, '2025-04-01 19:26:10', 'offline', '2025-04-01 19:26:10'),
(4, 2, 4, '2025-04-01 19:26:10', 'online', '2025-04-01 19:26:10'),
(5, 2, 5, '2025-04-01 19:26:10', 'offline', '2025-04-01 19:26:10'),
(6, 2, 6, '2025-04-01 19:26:10', 'offline', '2025-04-01 19:26:10'),
(7, 3, 9, '2025-04-01 19:26:10', 'offline', '2025-04-01 19:26:10'),
(8, 3, 7, '2025-04-01 19:26:10', 'online', '2025-04-01 19:26:10'),
(9, 4, 3, '2025-04-01 19:26:10', 'offline', '2025-04-01 19:26:10'),
(10, 4, 4, '2025-04-01 19:26:10', 'online', '2025-04-01 19:26:10');

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

--
-- Dumping data for table `profile_views`
--

INSERT INTO `profile_views` (`view_id`, `viewer_id`, `profile_id`, `viewed_at`) VALUES
(1, 9, 9, '2025-04-01 19:19:33'),
(2, 7, 7, '2025-04-01 19:19:33'),
(3, 3, 3, '2025-04-01 19:19:33'),
(4, 4, 4, '2025-04-01 19:19:33');

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

--
-- Dumping data for table `ratings`
--

INSERT INTO `ratings` (`rating_id`, `book_id`, `user_id`, `rating`, `comment`, `created_at`) VALUES
(1, 2, 9, 1, NULL, '2025-04-01 19:19:22'),
(2, 2, 7, 4, NULL, '2025-04-01 19:19:22'),
(3, 1, 3, 5, NULL, '2025-04-01 19:19:22'),
(4, 1, 4, 2, NULL, '2025-04-01 19:19:22');

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
-- Dumping data for table `support`
--

INSERT INTO `support` (`support_id`, `user_id`, `author_id`, `amount`, `message`, `created_at`) VALUES
(1, 9, 1, 256.90, NULL, '2025-04-01 19:19:11'),
(2, 7, 1, 45.90, NULL, '2025-04-01 19:19:11');

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
  `role` enum('Admin','Regular User') NOT NULL DEFAULT 'Regular User',
  `profile_picture` varchar(255) DEFAULT NULL COMMENT 'Profile picture URL',
  `books_read_count` int(11) DEFAULT 0 COMMENT 'Number of books read by the user',
  `works_created_count` int(11) DEFAULT 0 COMMENT 'Number of books created by the user',
  `created_at` timestamp NOT NULL DEFAULT current_timestamp() COMMENT 'Account creation timestamp',
  `books_listed_count` int(11) DEFAULT 0 COMMENT 'Number of books saved by user',
  `followers_count` int(11) DEFAULT 0,
  `following_count` int(11) DEFAULT 0
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `users`
--

INSERT INTO `users` (`user_id`, `username`, `password`, `email`, `role`, `profile_picture`, `books_read_count`, `works_created_count`, `created_at`, `books_listed_count`, `followers_count`, `following_count`) VALUES
(1, 'alice_writer', 'hp12345100', 'alice@example.com', 'Regular User', 'alice.jpg', 0, 0, '2025-03-09 10:54:49', 0, 0, 0),
(2, 'bob_novelist', 'hp1234522', 'bob@example.com', 'Regular User', 'bob.jpg', 3, 0, '2025-03-09 10:54:49', 0, 0, 0),
(3, 'charlie_poet', 'hp1234533', 'charlie@example.com', 'Regular User', 'charlie.jpg', 5, 1, '2025-03-09 10:54:49', 1, 1, 1),
(4, 'david_storyteller', 'hp1234544', 'david@example.com', 'Regular User', 'david.jpg', 2, 3, '2025-03-09 10:54:49', 4, 2, 1),
(5, 'emma_fantasy', 'hp1234555', 'emma@example.com', 'Regular User', 'emma.jpg', 2, 1, '2025-03-09 10:54:49', 0, 0, 0),
(6, 'frank_mystery', 'hp1234566', 'frank@example.com', 'Regular User', 'frank.jpg', 3, NULL, '2025-03-09 10:54:49', 2, 0, 0),
(7, 'grace_thriller', 'hp1234577', 'grace@example.com', 'Regular User', 'grace.jpg', 4, 2, '2025-03-09 10:54:49', 1, 1, 1),
(8, 'henry_horror', 'hp1234588', 'henry@example.com', 'Regular User', 'henry.jpg', 1, NULL, '2025-03-09 10:54:49', 0, 0, 0),
(9, 'isabel_youth', 'hp1234599', 'isabel@example.com', 'Regular User', 'isabel.jpg', 2, 2, '2025-03-09 10:54:49', 2, 2, 3),
(10, 'jack_crime', 'hp12345100', 'jack@example.com', 'Regular User', 'jack.jpg', 1, 1, '2025-03-09 10:54:49', 0, 0, 0),
(11, 'demo1', 'demo1234566', 'demo1@gmail.com', 'Regular User', NULL, 0, 0, '2025-03-29 11:20:26', 0, 0, 0),
(12, 'demo2', 'demo2345677', 'demo2@gmail.com', 'Regular User', NULL, 0, 0, '2025-03-29 11:32:28', 0, 0, 0),
(13, 'demo3', 'demo3456788', 'demo3@gmail.com', 'Regular User', NULL, 0, 0, '2025-03-29 11:35:37', 0, 0, 0);

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
-- Dumping data for table `user_books`
--

INSERT INTO `user_books` (`user_book_id`, `user_id`, `book_id`, `status`, `created_at`) VALUES
(1, 9, 2, 'Reading', '2025-04-01 19:18:58'),
(2, 7, 2, 'Completed', '2025-04-01 19:18:58'),
(3, 3, 1, 'Dropped', '2025-04-01 19:18:58'),
(4, 4, 1, 'Reading', '2025-04-01 19:18:58');

--
-- Triggers `user_books`
--
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
-- Dumping data for table `user_followers`
--

INSERT INTO `user_followers` (`follow_id`, `follower_id`, `following_id`, `created_at`) VALUES
(1, 9, 7, '2025-04-01 19:17:27'),
(2, 9, 3, '2025-04-01 19:17:27'),
(3, 9, 4, '2025-04-01 19:17:27'),
(5, 7, 4, '2025-04-01 19:17:27'),
(6, 3, 9, '2025-04-01 19:17:27'),
(7, 4, 9, '2025-04-01 19:17:27');

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
-- Dumping data for table `user_group_status`
--

INSERT INTO `user_group_status` (`user_id`, `group_id`, `status`) VALUES
(3, 1, 'joined'),
(3, 4, 'joined'),
(4, 2, 'joined'),
(4, 4, 'joined'),
(5, 2, 'joined'),
(6, 2, 'joined'),
(7, 1, 'joined'),
(7, 3, 'joined'),
(9, 1, 'joined'),
(9, 3, 'joined');

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
  ADD UNIQUE KEY `book_id` (`book_id`,`chapter_number`) COMMENT 'Ensures chapter numbers are unique within a book',
  ADD KEY `author_id` (`author_id`),
  ADD KEY `chapter_id` (`chapter_id`) USING BTREE;

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
  ADD UNIQUE KEY `unique_follower_following` (`follower_id`,`following_id`),
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
  MODIFY `book_author_id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=5;

--
-- AUTO_INCREMENT for table `chapters`
--
ALTER TABLE `chapters`
  MODIFY `chapter_id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=13;

--
-- AUTO_INCREMENT for table `chat_messages`
--
ALTER TABLE `chat_messages`
  MODIFY `message_id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=5;

--
-- AUTO_INCREMENT for table `collaboration_invites`
--
ALTER TABLE `collaboration_invites`
  MODIFY `invite_id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=5;

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
  MODIFY `entry_id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=33;

--
-- AUTO_INCREMENT for table `contest_votes`
--
ALTER TABLE `contest_votes`
  MODIFY `vote_id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=25;

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
  MODIFY `user_id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=24;

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
