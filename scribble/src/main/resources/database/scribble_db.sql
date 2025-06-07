-- MySQL dump 10.13  Distrib 8.0.42, for Win64 (x86_64)
--
-- Host: localhost    Database: scribble_db
-- ------------------------------------------------------
-- Server version	8.0.42

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `book_authors`
--

DROP TABLE IF EXISTS `book_authors`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `book_authors` (
  `book_author_id` int NOT NULL AUTO_INCREMENT,
  `book_id` int NOT NULL,
  `user_id` int NOT NULL,
  `role` enum('Owner','Co-Author') COLLATE utf8mb4_general_ci DEFAULT 'Owner' COMMENT 'Defines the primary author and co-authors',
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`book_author_id`),
  UNIQUE KEY `book_id` (`book_id`,`user_id`),
  KEY `user_id` (`user_id`),
  CONSTRAINT `book_authors_ibfk_1` FOREIGN KEY (`book_id`) REFERENCES `books` (`book_id`) ON DELETE CASCADE,
  CONSTRAINT `book_authors_ibfk_2` FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=65 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `book_authors`
--

LOCK TABLES `book_authors` WRITE;
/*!40000 ALTER TABLE `book_authors` DISABLE KEYS */;
INSERT INTO `book_authors` VALUES (1,1,9,'Owner','2025-04-01 18:04:07'),(2,1,7,'Co-Author','2025-04-01 18:04:07'),(3,2,4,'Owner','2025-04-01 18:13:27'),(4,2,5,'Co-Author','2025-04-01 18:13:27'),(5,21,24,'Owner','2025-06-04 12:54:38'),(6,11,9,'Owner','2025-06-04 13:32:25'),(7,12,9,'Owner','2025-06-04 13:32:25'),(8,13,9,'Owner','2025-06-04 13:32:25'),(9,14,9,'Owner','2025-06-04 13:32:25'),(10,15,9,'Owner','2025-06-04 13:32:25'),(11,16,9,'Owner','2025-06-04 13:32:25'),(12,17,9,'Owner','2025-06-04 13:32:25'),(13,18,9,'Owner','2025-06-04 13:32:25'),(14,19,9,'Owner','2025-06-04 13:32:25'),(15,20,9,'Owner','2025-06-04 13:32:25'),(16,22,9,'Owner','2025-06-04 13:33:02'),(17,23,24,'Owner','2025-06-04 13:35:05'),(18,24,24,'Owner','2025-06-04 13:41:39'),(19,25,24,'Owner','2025-06-04 13:47:48'),(20,26,24,'Owner','2025-06-04 13:50:44'),(21,27,24,'Owner','2025-06-04 13:55:17'),(22,28,24,'Owner','2025-06-04 13:59:58'),(24,1,3,'Co-Author','2025-06-04 20:58:00'),(25,2,6,'Co-Author','2025-06-04 20:58:00'),(36,2,7,'Co-Author','2025-06-04 21:05:01'),(52,11,10,'Co-Author','2025-06-04 21:02:00'),(53,11,1,'Co-Author','2025-06-04 21:02:00'),(54,12,2,'Co-Author','2025-06-04 21:02:00'),(55,12,5,'Co-Author','2025-06-04 21:02:00'),(56,13,1,'Co-Author','2025-06-04 21:02:00'),(57,13,3,'Co-Author','2025-06-04 21:02:00'),(59,1,1,'Co-Author','2025-06-04 20:58:00'),(60,11,3,'Co-Author','2025-06-04 20:58:00'),(62,29,26,'Owner','2025-06-04 21:45:31'),(63,30,26,'Owner','2025-06-04 21:47:09'),(64,31,26,'Owner','2025-06-05 18:25:22');
/*!40000 ALTER TABLE `book_authors` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `book_visits`
--

DROP TABLE IF EXISTS `book_visits`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `book_visits` (
  `visit_id` int NOT NULL AUTO_INCREMENT,
  `user_id` int NOT NULL COMMENT 'User who visited the book',
  `book_id` int NOT NULL COMMENT 'Book being visited',
  `visited_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Timestamp when book was visited',
  PRIMARY KEY (`visit_id`),
  UNIQUE KEY `user_id_book_id` (`user_id`,`book_id`) COMMENT 'Prevents duplicate visits by the same user',
  KEY `book_visits_ibfk_2` (`book_id`),
  CONSTRAINT `book_visits_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`) ON DELETE CASCADE,
  CONSTRAINT `book_visits_ibfk_2` FOREIGN KEY (`book_id`) REFERENCES `books` (`book_id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=25 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `book_visits`
--

LOCK TABLES `book_visits` WRITE;
/*!40000 ALTER TABLE `book_visits` DISABLE KEYS */;
INSERT INTO `book_visits` VALUES (1,9,2,'2025-04-01 07:18:58'),(2,7,2,'2025-04-01 07:18:58'),(3,4,1,'2025-04-01 07:18:58'),(4,3,1,'2025-04-01 20:45:00'),(5,1,1,'2025-04-01 22:00:00'),(6,2,1,'2025-04-01 22:00:00'),(7,10,2,'2025-04-01 22:00:00'),(13,24,1,'2025-06-05 20:12:56'),(14,1,2,'2025-06-05 20:19:43'),(15,24,12,'2025-06-05 20:35:08'),(16,24,15,'2025-06-05 20:35:14'),(17,24,23,'2025-06-05 20:35:36'),(18,24,28,'2025-06-05 20:36:32'),(19,24,27,'2025-06-05 20:36:37'),(20,24,2,'2025-06-05 20:37:19'),(21,24,11,'2025-06-05 21:54:28'),(22,24,13,'2025-06-05 22:13:28'),(23,24,20,'2025-06-05 22:14:42'),(24,26,1,'2025-06-06 06:45:34');
/*!40000 ALTER TABLE `book_visits` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `books`
--

DROP TABLE IF EXISTS `books`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `books` (
  `book_id` int NOT NULL AUTO_INCREMENT,
  `title` varchar(255) COLLATE utf8mb4_general_ci NOT NULL COMMENT 'Book title',
  `cover_photo` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT 'URL of the book cover',
  `description` text COLLATE utf8mb4_general_ci COMMENT 'Brief description of the book',
  `genre` enum('Fantasy','Thriller','Mystery','Thriller Mystery','Youth Fiction','Crime','Horror','Romance','Science Fiction','Adventure','Historical') COLLATE utf8mb4_general_ci NOT NULL COMMENT 'genre for each book',
  `status` enum('Ongoing','Complete','Hiatus') COLLATE utf8mb4_general_ci NOT NULL COMMENT 'Status of the book',
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Timestamp when book was added',
  `updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Timestamp when book was last updated',
  `view_count` int DEFAULT '0' COMMENT 'view count for each book\r\n',
  `total_reads` int DEFAULT '0' COMMENT 'Total number of chapter reads for the book',
  PRIMARY KEY (`book_id`)
) ENGINE=InnoDB AUTO_INCREMENT=32 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `books`
--

LOCK TABLES `books` WRITE;
/*!40000 ALTER TABLE `books` DISABLE KEYS */;
INSERT INTO `books` VALUES (1,'Bob\'s Mystery Novel','bob.png','Bob\'s Mystery Novel is a fast-paced mystery about Bob, who searches for his missing friend, Jake. A cryptic note, a hidden tunnel, and a mysterious map lead him on a thrilling chase.','Mystery','Ongoing','2025-04-01 18:03:40','2025-06-06 07:05:51',70,9),(2,'Lavender Garden','lavender.png','Lavender Garden is a magical fantasy about Emma, who discovers an enchanted garden hidden near her home. When the garden’s magic starts to fade, Emma must unlock its secrets and restore its power, encountering mystical guardians and dark forces along the way. The fate of the garden—and her world—depends on her.','Fantasy','Ongoing','2025-04-01 18:11:39','2025-06-05 21:41:45',15,3),(11,'Book 10','book10.png','A thrilling adventure in a mysterious forest.','Adventure','Ongoing','2025-06-04 06:37:00','2025-06-05 21:54:28',7,0),(12,'Book 6','book6.png','A tale of love and betrayal in a medieval kingdom.','Romance','Ongoing','2025-06-04 06:37:00','2025-06-05 22:08:22',6,0),(13,'Book 7','book7.png','A journey through a haunted mansion.','Horror','Ongoing','2025-06-04 06:37:00','2025-06-05 22:13:28',3,0),(14,'Book 8','book8.png','A detective uncovers a century-old secret.','Mystery','Ongoing','2025-06-04 06:37:00','2025-06-04 21:36:17',0,0),(15,'Book 9','book9.png','A futuristic battle for Earth\'s survival.','Science Fiction','Ongoing','2025-06-04 06:37:00','2025-06-05 20:35:14',4,0),(16,'Book 15','book15.png','A young hero discovers a magical artifact.','Fantasy','Ongoing','2025-06-04 06:37:00','2025-06-04 21:36:26',0,0),(17,'Book 25','book25.png','A historical epic of war and redemption.','Historical','Ongoing','2025-06-04 06:37:00','2025-06-05 14:35:49',1,0),(18,'Cover 1','cover_1.png','A suspenseful chase across continents.','Thriller','Ongoing','2025-06-04 06:37:00','2025-06-04 21:36:26',0,0),(19,'Cover 2','cover_2.png','A young girl\'s journey in a fantasy world.','Youth Fiction','Ongoing','2025-06-04 06:37:00','2025-06-04 21:36:26',0,0),(20,'Cover 3','cover_3.png','A crime novel with unexpected twists.','Crime','Ongoing','2025-06-04 06:37:00','2025-06-05 22:14:42',3,0),(21,'1b','cover_26.png','1bdes','Thriller Mystery','Hiatus','2025-06-04 12:54:38','2025-06-04 21:36:17',0,0),(22,'New Book',NULL,NULL,'Mystery','Ongoing','2025-06-04 13:33:02','2025-06-04 13:33:02',0,0),(23,'test 1','cover_27.png','t','Horror','Hiatus','2025-06-04 13:35:05','2025-06-06 06:42:11',19,3),(24,'test 2','cover_28.png','f','Thriller','Ongoing','2025-06-04 13:41:39','2025-06-04 13:41:39',0,0),(25,'test 3',NULL,'f','Thriller','Complete','2025-06-04 13:47:48','2025-06-04 13:47:48',0,0),(26,'test 4','cover_29.png','test 4','Thriller Mystery','Ongoing','2025-06-04 13:50:44','2025-06-04 13:50:44',0,0),(27,'test 5','cover_30.png','t','Fantasy','Ongoing','2025-06-04 13:55:17','2025-06-05 20:43:00',3,2),(28,'test 6','cover_31.png','t','Thriller','Hiatus','2025-06-04 13:59:58','2025-06-05 20:36:32',2,0),(29,'pokemon','cover_32.png','p','Science Fiction','Ongoing','2025-06-04 21:45:31','2025-06-04 21:45:31',0,0),(30,'pokemon 2','cover_33.png','p','Adventure','Ongoing','2025-06-04 21:47:09','2025-06-04 21:47:09',0,0),(31,'mastesr lil','cover_34.png','lil','Thriller Mystery','Ongoing','2025-06-05 18:25:22','2025-06-05 18:25:22',0,0);
/*!40000 ALTER TABLE `books` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `chapter_reads`
--

DROP TABLE IF EXISTS `chapter_reads`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `chapter_reads` (
  `read_id` int NOT NULL AUTO_INCREMENT,
  `user_id` int NOT NULL,
  `chapter_id` int NOT NULL,
  `read_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`read_id`),
  UNIQUE KEY `user_id_chapter_id` (`user_id`,`chapter_id`),
  KEY `chapter_reads_ibfk_2` (`chapter_id`),
  CONSTRAINT `chapter_reads_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`) ON DELETE CASCADE,
  CONSTRAINT `chapter_reads_ibfk_2` FOREIGN KEY (`chapter_id`) REFERENCES `chapters` (`chapter_id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=21 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `chapter_reads`
--

LOCK TABLES `chapter_reads` WRITE;
/*!40000 ALTER TABLE `chapter_reads` DISABLE KEYS */;
INSERT INTO `chapter_reads` VALUES (1,3,1,'2025-04-02 03:00:00'),(2,3,2,'2025-04-02 03:05:00'),(3,4,1,'2025-04-02 03:10:00'),(4,9,8,'2025-04-02 03:15:00'),(5,7,8,'2025-04-02 03:20:00'),(6,5,1,'2025-06-04 18:33:35'),(7,5,8,'2025-06-04 18:53:41'),(11,24,1,'2025-06-06 06:36:52'),(14,24,13,'2025-06-05 20:43:22'),(17,24,22,'2025-06-05 20:43:00');
/*!40000 ALTER TABLE `chapter_reads` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `chapters`
--

DROP TABLE IF EXISTS `chapters`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `chapters` (
  `chapter_id` int NOT NULL AUTO_INCREMENT,
  `book_id` int NOT NULL,
  `author_id` int NOT NULL,
  `chapter_number` int NOT NULL,
  `content` longtext COLLATE utf8mb4_general_ci NOT NULL,
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`chapter_id`),
  UNIQUE KEY `book_id_chapter_number` (`book_id`,`chapter_number`),
  KEY `chapters_ibfk_2` (`author_id`),
  CONSTRAINT `chapters_ibfk_1` FOREIGN KEY (`book_id`) REFERENCES `books` (`book_id`) ON DELETE CASCADE,
  CONSTRAINT `chapters_ibfk_2` FOREIGN KEY (`author_id`) REFERENCES `users` (`user_id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=35 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `chapters`
--

LOCK TABLES `chapters` WRITE;
/*!40000 ALTER TABLE `chapters` DISABLE KEYS */;
INSERT INTO `chapters` VALUES (1,1,9,1,'Bob found the door ajar. His best friend, Jake, had vanished...','2025-04-01 12:07:36','2025-04-01 12:07:36'),(2,1,9,2,'Beneath the trapdoor, a tunnel stretched into darkness...','2025-04-01 12:07:36','2025-04-01 12:07:36'),(3,1,9,3,'Bob\'s hands trembled as he reached a rusted chest deep within the passage...','2025-04-01 12:07:36','2025-04-01 12:07:36'),(4,1,7,4,'The chest creaked open, revealing a faded map...','2025-04-01 12:07:36','2025-04-01 12:07:36'),(5,1,7,5,'Bob reached the cabin at dusk. It was silent, eerily so...','2025-04-01 12:07:36','2025-04-01 12:07:36'),(6,1,9,6,'Bob confronted the stranger. It was Jake—alive but terrified...','2025-04-01 12:07:36','2025-04-01 12:07:36'),(7,1,7,7,'With the red key, map, and their wits, Bob and Jake set a trap...','2025-04-01 12:07:36','2025-04-01 12:07:36'),(8,2,4,1,'Emma always felt drawn to the abandoned lavender garden near her cottage...','2025-04-01 12:21:08','2025-04-01 12:21:08'),(9,2,4,2,'Following the glowing lavender path, Emma discovered an ancient stone door...','2025-04-01 12:21:08','2025-04-01 12:21:08'),(10,2,5,3,'A mystical figure emerged—a guardian of the garden...','2025-04-01 12:21:08','2025-04-01 12:21:08'),(11,2,5,4,'Emma raced to the garden’s center, where a crystal pulsed beneath ancient roots...','2025-04-01 12:21:08','2025-04-01 12:21:08'),(12,21,24,1,'hi this is the very first draft from user 11111','2025-06-04 12:55:23','2025-06-04 12:55:23'),(13,23,24,1,'again test 1','2025-06-04 13:36:37','2025-06-05 20:51:49'),(16,24,24,1,'testing 2','2025-06-04 13:41:49','2025-06-04 13:41:49'),(17,25,24,1,'aaaaaaaaaaaaaaaaaaaaaaaaa\nbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbb\ncccccccccccccccccccccccccccccccccccccccccccccccc','2025-06-04 13:48:06','2025-06-04 13:48:06'),(18,26,24,1,'test 4 chapter 1','2025-06-04 13:50:55','2025-06-04 13:51:05'),(20,27,24,1,'test 5 chapter 1','2025-06-04 13:55:26','2025-06-04 13:55:26'),(21,27,24,2,'test 5 chapter 2','2025-06-04 13:55:40','2025-06-04 13:55:40'),(22,27,24,3,'test 5 chapter 3','2025-06-04 13:55:53','2025-06-04 13:55:53'),(23,28,24,1,'test 6 chapter 1','2025-06-04 14:00:08','2025-06-04 14:00:08'),(24,28,24,2,'test 6 chapter 2','2025-06-04 14:00:20','2025-06-04 14:00:20'),(25,28,24,3,'test 6 chapter 3','2025-06-04 14:00:35','2025-06-04 14:00:35'),(26,29,26,1,'chapter 1 for pokemon','2025-06-04 21:45:54','2025-06-04 21:45:54'),(27,29,26,2,'chapter 2 for pokemon','2025-06-04 21:46:10','2025-06-04 21:46:10'),(28,29,26,3,'chapter 3 pokemon','2025-06-04 21:46:31','2025-06-04 21:46:31'),(29,31,26,1,'chapter 1 master','2025-06-05 18:25:47','2025-06-05 18:25:47'),(30,31,26,2,'chapter 2 master','2025-06-05 18:25:59','2025-06-05 18:25:59'),(31,31,26,3,'chapter 3 master draft','2025-06-05 18:26:34','2025-06-05 18:26:34'),(34,23,24,2,'chapter 2 test 1','2025-06-05 20:52:16','2025-06-05 20:52:16');
/*!40000 ALTER TABLE `chapters` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `chat_messages`
--

DROP TABLE IF EXISTS `chat_messages`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `chat_messages` (
  `message_id` int NOT NULL AUTO_INCREMENT,
  `group_id` int NOT NULL,
  `sender_id` int NOT NULL,
  `message` text COLLATE utf8mb4_general_ci NOT NULL,
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`message_id`),
  KEY `group_id` (`group_id`),
  KEY `sender_id` (`sender_id`),
  CONSTRAINT `chat_messages_ibfk_1` FOREIGN KEY (`group_id`) REFERENCES `community_groups` (`group_id`) ON DELETE CASCADE,
  CONSTRAINT `chat_messages_ibfk_2` FOREIGN KEY (`sender_id`) REFERENCES `users` (`user_id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `chat_messages`
--

LOCK TABLES `chat_messages` WRITE;
/*!40000 ALTER TABLE `chat_messages` DISABLE KEYS */;
INSERT INTO `chat_messages` VALUES (1,1,9,'Hello everyone, welcome to Group 1!','2025-04-01 19:26:23'),(2,1,7,'Thanks for the invite! Looking forward to it.','2025-04-01 19:26:23'),(3,2,4,'Excited for Group 2! Let’s get started.','2025-04-01 19:26:23'),(4,3,9,'I’m glad to be in this group, let’s have fun!','2025-04-01 19:26:23');
/*!40000 ALTER TABLE `chat_messages` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `collaboration_invites`
--

DROP TABLE IF EXISTS `collaboration_invites`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `collaboration_invites` (
  `invite_id` int NOT NULL AUTO_INCREMENT,
  `book_id` int NOT NULL,
  `inviter_id` int NOT NULL,
  `invitee_email` varchar(255) COLLATE utf8mb4_general_ci NOT NULL,
  `invite_code` varchar(10) COLLATE utf8mb4_general_ci NOT NULL,
  `status` enum('Pending','Accepted','Declined') COLLATE utf8mb4_general_ci DEFAULT 'Pending',
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `message` text COLLATE utf8mb4_general_ci,
  PRIMARY KEY (`invite_id`),
  UNIQUE KEY `invite_code` (`invite_code`),
  KEY `book_id` (`book_id`),
  KEY `inviter_id` (`inviter_id`),
  CONSTRAINT `collaboration_invites_ibfk_1` FOREIGN KEY (`book_id`) REFERENCES `books` (`book_id`) ON DELETE CASCADE,
  CONSTRAINT `collaboration_invites_ibfk_2` FOREIGN KEY (`inviter_id`) REFERENCES `users` (`user_id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=32 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `collaboration_invites`
--

LOCK TABLES `collaboration_invites` WRITE;
/*!40000 ALTER TABLE `collaboration_invites` DISABLE KEYS */;
INSERT INTO `collaboration_invites` VALUES (1,1,9,'grace@example.com','INV123456','Accepted','2025-04-01 18:07:54',NULL),(3,2,4,'frank@example.com','INV123457','Pending','2025-04-01 18:21:37','I’m excited to help write Lavender Garden. I’m passionate about fantasy stories!'),(4,2,4,'grace@example.com','INV123458','Accepted','2025-06-04 21:03:23','I’d love to collaborate on Lavender Garden. I have ideas for the magical plot!'),(5,11,9,'alice@example.com','INV123459','Pending','2025-06-04 20:33:00','Hi, I’m interested in co-authoring Book 10. I love adventure stories and have epic ideas!'),(6,11,9,'newadventure@example.com','INV123460','Pending','2025-06-04 20:34:00','I’d love to join your adventure book project. I’m great at crafting thrilling scenes!'),(7,15,9,'bob@example.com','INV123461','Pending','2025-06-04 20:35:00','Excited to collaborate on your sci-fi book. I have ideas for futuristic battles!'),(8,17,9,'charlie@example.com','INV123462','Pending','2025-06-04 20:36:00','I’d love to help with your historical epic. I’m a history buff with story ideas!'),(9,19,9,'emma@example.com','INV123463','Pending','2025-06-04 20:37:00','Hi, I want to collaborate on your youth fiction book. I’m great with kid-friendly stories!'),(10,21,24,'henry@example.com','INV123464','Pending','2025-06-04 20:38:00','I’m eager to co-author your thriller mystery. I have ideas for suspenseful twists!'),(11,21,24,'newmystery@example.com','INV123465','Pending','2025-06-04 20:39:00','Interested in joining your thriller project. I’m skilled at crafting mysteries!'),(12,23,24,'jack@example.com','INV123466','Pending','2025-06-04 20:40:00','Hi, I’d love to work on your horror book. I’m great at spooky atmospheres!'),(13,25,24,'alice@example.com','INV123467','Pending','2025-06-04 20:41:00','Excited to collaborate on your thriller. I have ideas for intense scenes!'),(14,27,24,'bob@example.com','INV123468','Pending','2025-06-04 20:42:00','I’d love to help write your fantasy book. I’m passionate about magical worlds!'),(15,1,9,'alice@example.com','INV123469','Accepted','2025-06-04 20:58:00','Excited to have you join Bob\'s Mystery Novel as a co-author!'),(16,2,4,'bob@example.com','INV123470','Declined','2025-06-04 20:58:00','Would love your input on Lavender Garden!'),(17,11,9,'charlie@example.com','INV123471','Accepted','2025-06-04 20:58:00','Join me for an epic adventure in Book 10!'),(18,15,9,'emma@example.com','INV123472','Declined','2025-06-04 20:58:00','Help craft the sci-fi battles in Book 9!'),(19,1,9,'charlie@example.com','INV123473','Accepted','2025-06-04 21:02:00','Join Bob\'s Mystery Novel as a co-author, Charlie!'),(20,2,4,'frank@example.com','INV123474','Accepted','2025-06-04 21:02:00','Excited for you to join Lavender Garden, Frank!'),(21,11,9,'jack@example.com','INV123475','Accepted','2025-06-04 21:02:00','Join the adventure in Book 10, Jack!'),(22,11,9,'alice@example.com','INV123476','Accepted','2025-06-04 21:02:00','Alice, help craft Book 10’s epic story!'),(23,12,9,'bob@example.com','INV123477','Accepted','2025-06-04 21:02:00','Bob, join the romance in Book 6!'),(24,12,9,'emma@example.com','INV123478','Accepted','2025-06-04 21:02:00','Emma, add your touch to Book 6’s love story!'),(25,13,9,'alice@example.com','INV123479','Accepted','2025-06-04 21:02:00','Alice, join the spooky tale in Book 7!'),(26,13,9,'charlie@example.com','INV123480','Accepted','2025-06-04 21:02:00','Charlie, help make Book 7 chilling!'),(27,2,4,'emma@example.com','INV123481','Accepted','2025-06-04 21:03:23','Emma, join Lavender Garden as a co-author!'),(28,11,9,'emma@example.com','INV123482','Accepted','2025-06-04 15:02:00','Emma, join the adventure in Book 10!'),(29,15,9,'jack@example.com','INV123483','Accepted','2025-06-04 15:02:00','Jack, help craft the sci-fi battles in Book 9!'),(31,1,24,'isabel@example.com','a6f13ec9-b','Pending','2025-06-05 20:34:41','requesting for colab');
/*!40000 ALTER TABLE `collaboration_invites` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `community_groups`
--

DROP TABLE IF EXISTS `community_groups`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `community_groups` (
  `group_id` int NOT NULL AUTO_INCREMENT,
  `admin_id` int NOT NULL COMMENT 'User ID of the group admin',
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Timestamp when group was created',
  PRIMARY KEY (`group_id`),
  KEY `admin_id` (`admin_id`),
  CONSTRAINT `community_groups_ibfk_1` FOREIGN KEY (`admin_id`) REFERENCES `users` (`user_id`)
) ENGINE=InnoDB AUTO_INCREMENT=11 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `community_groups`
--

LOCK TABLES `community_groups` WRITE;
/*!40000 ALTER TABLE `community_groups` DISABLE KEYS */;
INSERT INTO `community_groups` VALUES (1,9,'2025-04-01 19:25:54'),(2,4,'2025-04-01 19:25:54'),(3,9,'2025-04-01 19:25:54'),(4,3,'2025-04-01 19:25:54');
/*!40000 ALTER TABLE `community_groups` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `contest_entries`
--

DROP TABLE IF EXISTS `contest_entries`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `contest_entries` (
  `entry_id` int NOT NULL AUTO_INCREMENT,
  `contest_id` int NOT NULL COMMENT 'Contest ID the entry is for',
  `user_id` int NOT NULL COMMENT 'User ID of the participant',
  `submission_date` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Timestamp of submission',
  `vote_count` int DEFAULT '0' COMMENT 'Number of votes received',
  `entry_title` varchar(255) COLLATE utf8mb4_general_ci NOT NULL COMMENT 'Name of the content',
  `content` longtext COLLATE utf8mb4_general_ci NOT NULL COMMENT 'The actual content of the entry (the story or written piece).',
  `cover_photo` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT 'URL of the contest entry cover photo',
  PRIMARY KEY (`entry_id`),
  UNIQUE KEY `unique_contest_entry_title` (`contest_id`,`entry_title`),
  KEY `contest_id` (`contest_id`),
  KEY `user_id` (`user_id`),
  CONSTRAINT `contest_entries_ibfk_1` FOREIGN KEY (`contest_id`) REFERENCES `contests` (`contest_id`) ON DELETE CASCADE,
  CONSTRAINT `contest_entries_ibfk_2` FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=33 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `contest_entries`
--

LOCK TABLES `contest_entries` WRITE;
/*!40000 ALTER TABLE `contest_entries` DISABLE KEYS */;
INSERT INTO `contest_entries` VALUES (1,1,5,'2025-04-01 18:27:04',5,'The Enchanted Forest','Chapter 1: The Hidden Path – Lily stumbles upon a secret entrance in the forest...\nChapter 2: The Guardians’ Quest – Lily learns she is the chosen one destined to protect the forest...\nChapter 3: The Final Battle – Lily and her allies confront the dark sorcerer...','c.1.png'),(4,1,4,'2025-04-01 18:27:19',3,'The Crystal of Aetheria','Chapter 1: The Prophecy Unfolds – Finn learns about the ancient crystal...\nChapter 2: The Journey Begins – Finn and his companions cross dangerous lands...\nChapter 3: The Crystal’s Power – Finn faces the guardian of the crystal...','c.2.png'),(7,1,3,'2025-04-01 18:27:29',1,'The Lost Kingdom','Chapter 1: The Forgotten Map – Aria finds a map...\nChapter 2: The Awakening – Aria accidentally awakens an ancient force...\nChapter 3: The Battle for the Kingdom – Aria must fight against the evil forces...','c.3.png'),(10,2,7,'2025-04-01 18:33:11',2,'The Midnight Caller','Chapter 1: The First Call – Detective Riley receives a chilling call...\nChapter 2: The Cryptic Clue – A second call leads Riley to an eerie pattern...\nChapter 3: Chasing Shadows – Riley suspects a connection...\nChapter 4: The Confrontation – Riley corners the killer...\nChapter 5: The Final Decision – In a final showdown...','c.4.png'),(15,2,9,'2025-04-01 18:33:11',3,'The Forgotten Witness','Chapter 1: The Vanishing – Claire is called to investigate...\nChapter 2: The Mysterious Claim – An elderly man insists...\nChapter 3: Chasing Shadows – Claire discovers disturbing connections...\nChapter 4: The Hidden Link – Claire uncovers a buried conspiracy...\nChapter 5: The Final Revelation – With the clock ticking...','c.5.png'),(20,2,4,'2025-04-01 18:33:11',0,'The Vanishing Point','Chapter 1: Into the Unknown – Max begins investigating...','c.6.png'),(21,3,1,'2025-04-01 18:35:11',1,'The Secret of Pinehill','Chapter 1: The Mysterious Map – The friends find the old map...\nChapter 2: The Hidden Chamber – They race against time...','c.7.png'),(23,3,2,'2025-04-01 18:35:11',0,'The Midnight Escape','Chapter 1: The Forbidden Park – The friends sneak into an abandoned amusement park...\nChapter 2: The Escape – Realizing the park’s eerie powers...','c.8.png'),(25,3,3,'2025-04-01 18:35:11',4,'The Lost Key','Chapter 1: The Key in the Park – The three friends stumble upon an old key...\nChapter 2: The Hidden Doorway – The friends open the door to a magical world...','c.9.png'),(27,4,6,'2025-04-01 18:41:59',2,'Shattered Alibis','Chapter 1: The Accusation – Claire takes on the case of a man accused of a high-profile murder.\nChapter 2: Unraveling the Truth – Claire uncovers conflicting evidence...','c.10.png'),(29,4,7,'2025-04-01 18:41:59',2,'Crimson Lies','Chapter 1: The Robbery – Detective Carter investigates a robbery...\nChapter 2: The Trail of Lies – Alex uncovers a series of connections...\nChapter 3: Confronting the Truth – Alex faces a betrayal...',NULL),(32,4,8,'2025-04-01 18:41:59',1,'The Silent Witness','Chapter 1: The First Murder – Jenna is assigned to a case that seems like an open-and-shut murder...',NULL);
/*!40000 ALTER TABLE `contest_entries` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `contest_votes`
--

DROP TABLE IF EXISTS `contest_votes`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `contest_votes` (
  `vote_id` int NOT NULL AUTO_INCREMENT,
  `contest_entry_id` int NOT NULL COMMENT 'Contest entry ID the vote is for',
  `user_id` int NOT NULL COMMENT 'User ID of the voter',
  `vote_value` tinyint(1) NOT NULL COMMENT 'Vote (e.g., TRUE for vote, FALSE for un-vote)',
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Timestamp when vote was cast',
  PRIMARY KEY (`vote_id`),
  UNIQUE KEY `contest_entry_id` (`contest_entry_id`,`user_id`) COMMENT 'Ensures a user can only vote once per contest entry',
  KEY `user_id` (`user_id`),
  CONSTRAINT `contest_votes_ibfk_1` FOREIGN KEY (`contest_entry_id`) REFERENCES `contest_entries` (`entry_id`) ON DELETE CASCADE,
  CONSTRAINT `contest_votes_ibfk_2` FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=25 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `contest_votes`
--

LOCK TABLES `contest_votes` WRITE;
/*!40000 ALTER TABLE `contest_votes` DISABLE KEYS */;
INSERT INTO `contest_votes` VALUES (1,1,1,1,'2025-04-01 18:27:39'),(2,1,2,1,'2025-04-01 18:27:39'),(3,1,3,1,'2025-04-01 18:27:39'),(4,1,4,1,'2025-04-01 18:27:39'),(5,1,10,1,'2025-04-01 18:27:39'),(6,4,3,1,'2025-04-01 18:28:12'),(7,4,5,1,'2025-04-01 18:28:12'),(8,4,10,1,'2025-04-01 18:28:12'),(9,7,2,1,'2025-04-01 18:28:24'),(10,10,1,1,'2025-04-01 18:33:30'),(11,10,2,1,'2025-04-01 18:33:30'),(12,15,3,1,'2025-04-01 18:33:30'),(13,15,5,1,'2025-04-01 18:33:30'),(14,15,6,1,'2025-04-01 18:33:30'),(15,21,2,1,'2025-04-01 18:35:26'),(16,25,7,1,'2025-04-01 18:35:26'),(17,25,8,1,'2025-04-01 18:35:26'),(18,25,9,1,'2025-04-01 18:35:26'),(19,25,10,1,'2025-04-01 18:35:26'),(20,27,3,1,'2025-04-01 18:42:13'),(21,27,4,1,'2025-04-01 18:42:13'),(22,29,4,1,'2025-04-01 18:42:13'),(23,29,5,1,'2025-04-01 18:42:13'),(24,32,7,1,'2025-04-01 18:42:13');
/*!40000 ALTER TABLE `contest_votes` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `contests`
--

DROP TABLE IF EXISTS `contests`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `contests` (
  `contest_id` int NOT NULL AUTO_INCREMENT,
  `title` varchar(255) COLLATE utf8mb4_general_ci NOT NULL COMMENT 'Title of the contest',
  `genre` enum('Fantasy','Thriller Mystery','Youth Fiction','Crime Horror') COLLATE utf8mb4_general_ci NOT NULL COMMENT 'Different sections for the contest',
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Timestamp when contest was created',
  PRIMARY KEY (`contest_id`)
) ENGINE=InnoDB AUTO_INCREMENT=11 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `contests`
--

LOCK TABLES `contests` WRITE;
/*!40000 ALTER TABLE `contests` DISABLE KEYS */;
INSERT INTO `contests` VALUES (1,'Fantasy Writing Contest','Fantasy','2025-04-01 18:26:54'),(2,'Thriller Mystery Writing Contest','Thriller Mystery','2025-04-01 18:32:02'),(3,'Youth Fiction Writing Contest','Youth Fiction','2025-04-01 18:34:58'),(4,'Crime Horror Writing Contest','Crime Horror','2025-04-01 18:41:36');
/*!40000 ALTER TABLE `contests` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `draft_chapters`
--

DROP TABLE IF EXISTS `draft_chapters`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `draft_chapters` (
  `draft_id` int NOT NULL AUTO_INCREMENT,
  `book_id` int NOT NULL COMMENT 'Book ID the draft chapter belongs to',
  `author_id` int NOT NULL COMMENT 'User ID of the author or co-author',
  `chapter_number` int NOT NULL COMMENT 'Sequential chapter number for the draft',
  `content` longtext COLLATE utf8mb4_general_ci NOT NULL COMMENT 'Draft chapter content',
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Timestamp when draft was created',
  `updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Timestamp when draft was last updated',
  PRIMARY KEY (`draft_id`),
  UNIQUE KEY `book_id_chapter_number` (`book_id`,`chapter_number`) COMMENT 'Ensures unique chapter numbers within a book for drafts',
  KEY `draft_chapters_ibfk_2` (`author_id`),
  CONSTRAINT `draft_chapters_ibfk_1` FOREIGN KEY (`book_id`) REFERENCES `books` (`book_id`) ON DELETE CASCADE,
  CONSTRAINT `draft_chapters_ibfk_2` FOREIGN KEY (`author_id`) REFERENCES `users` (`user_id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=15 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `draft_chapters`
--

LOCK TABLES `draft_chapters` WRITE;
/*!40000 ALTER TABLE `draft_chapters` DISABLE KEYS */;
INSERT INTO `draft_chapters` VALUES (1,1,9,8,'Draft: Bob and Jake uncover the mastermind behind the plot...\ndraft testing','2025-04-02 04:00:00','2025-06-05 20:19:05'),(2,1,7,9,'Draft: A new clue emerges from the old diary...','2025-04-02 05:00:00','2025-04-02 05:00:00'),(3,2,4,5,'Draft: Emma learns the crystal’s true origin...','2025-04-02 06:00:00','2025-04-02 06:00:00'),(4,2,5,6,'Draft: The guardian reveals a hidden prophecy...','2025-04-02 07:00:00','2025-04-02 07:00:00'),(11,30,26,1,'draft testing','2025-06-04 21:47:16','2025-06-04 21:47:16');
/*!40000 ALTER TABLE `draft_chapters` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `group_members`
--

DROP TABLE IF EXISTS `group_members`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `group_members` (
  `membership_id` int NOT NULL AUTO_INCREMENT,
  `group_id` int NOT NULL COMMENT 'Group ID',
  `user_id` int NOT NULL COMMENT 'User ID of the group member',
  `joined_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Timestamp when user joined',
  `online_status` enum('online','offline') COLLATE utf8mb4_general_ci DEFAULT 'offline',
  `last_active` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`membership_id`),
  UNIQUE KEY `group_id` (`group_id`,`user_id`) COMMENT 'Ensures a user can only join a group once',
  KEY `user_id` (`user_id`),
  CONSTRAINT `group_members_ibfk_1` FOREIGN KEY (`group_id`) REFERENCES `community_groups` (`group_id`) ON DELETE CASCADE,
  CONSTRAINT `group_members_ibfk_2` FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=11 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `group_members`
--

LOCK TABLES `group_members` WRITE;
/*!40000 ALTER TABLE `group_members` DISABLE KEYS */;
INSERT INTO `group_members` VALUES (1,1,9,'2025-04-01 19:26:10','online','2025-04-01 19:26:10'),(2,1,7,'2025-04-01 19:26:10','offline','2025-04-01 19:26:10'),(3,1,3,'2025-04-01 19:26:10','offline','2025-04-01 19:26:10'),(4,2,4,'2025-04-01 19:26:10','online','2025-04-01 19:26:10'),(5,2,5,'2025-04-01 19:26:10','offline','2025-04-01 19:26:10'),(6,2,6,'2025-04-01 19:26:10','offline','2025-04-01 19:26:10'),(7,3,9,'2025-04-01 19:26:10','offline','2025-04-01 19:26:10'),(8,3,7,'2025-04-01 19:26:10','online','2025-04-01 19:26:10'),(9,4,3,'2025-04-01 19:26:10','offline','2025-04-01 19:26:10'),(10,4,4,'2025-04-01 19:26:10','online','2025-04-01 19:26:10');
/*!40000 ALTER TABLE `group_members` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `profile_views`
--

DROP TABLE IF EXISTS `profile_views`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `profile_views` (
  `view_id` int NOT NULL AUTO_INCREMENT,
  `viewer_id` int DEFAULT NULL COMMENT 'User viewing the profile',
  `profile_id` int NOT NULL COMMENT 'Profile being viewed',
  `viewed_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Timestamp when the profile was viewed',
  PRIMARY KEY (`view_id`),
  KEY `viewer_id` (`viewer_id`),
  KEY `profile_id` (`profile_id`),
  CONSTRAINT `profile_views_ibfk_1` FOREIGN KEY (`viewer_id`) REFERENCES `users` (`user_id`) ON DELETE SET NULL,
  CONSTRAINT `profile_views_ibfk_2` FOREIGN KEY (`profile_id`) REFERENCES `users` (`user_id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `profile_views`
--

LOCK TABLES `profile_views` WRITE;
/*!40000 ALTER TABLE `profile_views` DISABLE KEYS */;
INSERT INTO `profile_views` VALUES (1,9,9,'2025-04-01 19:19:33'),(2,7,7,'2025-04-01 19:19:33'),(3,3,3,'2025-04-01 19:19:33'),(4,4,4,'2025-04-01 19:19:33');
/*!40000 ALTER TABLE `profile_views` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `ratings`
--

DROP TABLE IF EXISTS `ratings`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `ratings` (
  `rating_id` int NOT NULL AUTO_INCREMENT,
  `book_id` int NOT NULL COMMENT 'Book ID being rated',
  `user_id` int NOT NULL COMMENT 'User ID of the reviewer',
  `rating` int DEFAULT NULL,
  `comment` text COLLATE utf8mb4_general_ci COMMENT 'User comment or review',
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Timestamp when rating was submitted',
  PRIMARY KEY (`rating_id`),
  UNIQUE KEY `book_id` (`book_id`,`user_id`) COMMENT 'Ensures a user can only rate a book once',
  KEY `user_id` (`user_id`),
  CONSTRAINT `ratings_ibfk_1` FOREIGN KEY (`book_id`) REFERENCES `books` (`book_id`) ON DELETE CASCADE,
  CONSTRAINT `ratings_ibfk_2` FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=161 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `ratings`
--

LOCK TABLES `ratings` WRITE;
/*!40000 ALTER TABLE `ratings` DISABLE KEYS */;
INSERT INTO `ratings` VALUES (83,1,1,4,'Gripping mystery! The plot twists were fantastic.','2025-04-01 13:30:00'),(84,1,2,NULL,'Loved the suspense, can’t wait for the next chapter!','2025-04-02 04:15:00'),(85,1,3,5,NULL,'2025-04-03 08:20:00'),(88,2,3,4,NULL,'2025-04-06 09:10:00'),(153,1,24,NULL,'i like the book','2025-06-05 20:15:37'),(154,2,1,0,NULL,'2025-06-05 20:20:07'),(156,23,24,NULL,'i loved this','2025-06-05 20:35:45'),(158,2,24,NULL,'comment testing','2025-06-05 21:41:54'),(159,13,24,2,'i like book 7','2025-06-05 22:13:36');
/*!40000 ALTER TABLE `ratings` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `reading_list`
--

DROP TABLE IF EXISTS `reading_list`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `reading_list` (
  `reading_list_id` int NOT NULL AUTO_INCREMENT,
  `reader_id` int NOT NULL COMMENT 'User who added the book',
  `listed_book_id` int NOT NULL COMMENT 'Book ID being added',
  `reading_status` enum('Reading','Completed','Dropped','SavedForLater') COLLATE utf8mb4_general_ci DEFAULT 'Reading' COMMENT 'Reading status, including saved for later',
  `added_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Timestamp when added',
  PRIMARY KEY (`reading_list_id`),
  UNIQUE KEY `user_id` (`reader_id`,`listed_book_id`) COMMENT 'Prevents duplicate book entries',
  KEY `book_id` (`listed_book_id`),
  CONSTRAINT `reading_list_ibfk_1` FOREIGN KEY (`reader_id`) REFERENCES `users` (`user_id`) ON DELETE CASCADE,
  CONSTRAINT `reading_list_ibfk_2` FOREIGN KEY (`listed_book_id`) REFERENCES `books` (`book_id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=26 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `reading_list`
--

LOCK TABLES `reading_list` WRITE;
/*!40000 ALTER TABLE `reading_list` DISABLE KEYS */;
INSERT INTO `reading_list` VALUES (1,9,2,'Reading','2025-04-01 13:18:58'),(2,7,2,'Completed','2025-04-01 13:18:58'),(4,4,1,'Reading','2025-04-01 13:18:58'),(5,3,1,'SavedForLater','2025-04-02 02:45:00'),(6,4,2,'SavedForLater','2025-04-02 02:50:00'),(7,7,1,'SavedForLater','2025-04-02 03:00:00'),(23,24,1,'SavedForLater','2025-06-05 22:08:16'),(24,24,12,'SavedForLater','2025-06-05 22:08:23'),(25,24,20,'SavedForLater','2025-06-05 22:14:43');
/*!40000 ALTER TABLE `reading_list` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `support`
--

DROP TABLE IF EXISTS `support`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `support` (
  `support_id` int NOT NULL AUTO_INCREMENT,
  `user_id` int NOT NULL COMMENT 'User sending support',
  `author_id` int NOT NULL COMMENT 'Author receiving support',
  `amount` decimal(10,2) NOT NULL COMMENT 'Support amount in currency',
  `message` text COLLATE utf8mb4_general_ci COMMENT 'Optional message from supporter',
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Timestamp when support was sent',
  PRIMARY KEY (`support_id`),
  KEY `user_id` (`user_id`),
  KEY `author_id` (`author_id`),
  CONSTRAINT `support_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`) ON DELETE CASCADE,
  CONSTRAINT `support_ibfk_2` FOREIGN KEY (`author_id`) REFERENCES `users` (`user_id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=11 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `support`
--

LOCK TABLES `support` WRITE;
/*!40000 ALTER TABLE `support` DISABLE KEYS */;
INSERT INTO `support` VALUES (1,9,1,256.90,NULL,'2025-04-01 19:19:11'),(2,7,1,45.90,NULL,'2025-04-01 19:19:11');
/*!40000 ALTER TABLE `support` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `user_followers`
--

DROP TABLE IF EXISTS `user_followers`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `user_followers` (
  `follow_id` int NOT NULL AUTO_INCREMENT,
  `follower_id` int NOT NULL COMMENT 'User ID of the follower',
  `following_id` int NOT NULL COMMENT 'User ID being followed',
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Timestamp when follow action occurred',
  PRIMARY KEY (`follow_id`),
  UNIQUE KEY `follower_id` (`follower_id`,`following_id`) COMMENT 'Ensures a user can only follow another user once',
  UNIQUE KEY `unique_follower_following` (`follower_id`,`following_id`),
  KEY `following_id` (`following_id`),
  CONSTRAINT `user_followers_ibfk_1` FOREIGN KEY (`follower_id`) REFERENCES `users` (`user_id`) ON DELETE CASCADE,
  CONSTRAINT `user_followers_ibfk_2` FOREIGN KEY (`following_id`) REFERENCES `users` (`user_id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=11 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `user_followers`
--

LOCK TABLES `user_followers` WRITE;
/*!40000 ALTER TABLE `user_followers` DISABLE KEYS */;
INSERT INTO `user_followers` VALUES (1,9,7,'2025-04-01 19:17:27'),(2,9,3,'2025-04-01 19:17:27'),(3,9,4,'2025-04-01 19:17:27'),(5,7,4,'2025-04-01 19:17:27'),(6,3,9,'2025-04-01 19:17:27'),(7,4,9,'2025-04-01 19:17:27');
/*!40000 ALTER TABLE `user_followers` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `user_group_status`
--

DROP TABLE IF EXISTS `user_group_status`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `user_group_status` (
  `user_id` int NOT NULL,
  `group_id` int NOT NULL,
  `status` enum('joined','left') COLLATE utf8mb4_general_ci DEFAULT 'joined',
  PRIMARY KEY (`user_id`,`group_id`),
  KEY `group_id` (`group_id`),
  CONSTRAINT `user_group_status_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`),
  CONSTRAINT `user_group_status_ibfk_2` FOREIGN KEY (`group_id`) REFERENCES `community_groups` (`group_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `user_group_status`
--

LOCK TABLES `user_group_status` WRITE;
/*!40000 ALTER TABLE `user_group_status` DISABLE KEYS */;
INSERT INTO `user_group_status` VALUES (3,1,'joined'),(3,4,'joined'),(4,2,'joined'),(4,4,'joined'),(5,2,'joined'),(6,2,'joined'),(7,1,'joined'),(7,3,'joined'),(9,1,'joined'),(9,3,'joined');
/*!40000 ALTER TABLE `user_group_status` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `users`
--

DROP TABLE IF EXISTS `users`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `users` (
  `user_id` int NOT NULL AUTO_INCREMENT,
  `username` varchar(50) COLLATE utf8mb4_general_ci NOT NULL COMMENT 'Unique username for login',
  `password` varchar(255) COLLATE utf8mb4_general_ci NOT NULL COMMENT 'Hashed password for security',
  `email` varchar(100) COLLATE utf8mb4_general_ci NOT NULL COMMENT 'Unique email address',
  `role` enum('Admin','Regular User') COLLATE utf8mb4_general_ci NOT NULL DEFAULT 'Regular User',
  `profile_picture` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT 'Profile picture URL',
  `books_read_count` int DEFAULT '0' COMMENT 'Number of books read by the user',
  `works_created_count` int DEFAULT '0' COMMENT 'Number of books created by the user',
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Account creation timestamp',
  `books_listed_count` int DEFAULT '0' COMMENT 'Number of books saved by user',
  `followers_count` int DEFAULT '0',
  `following_count` int DEFAULT '0',
  `total_saved_books` int DEFAULT '0' COMMENT 'Total number of books saved by the user',
  `total_visited_books` int DEFAULT '0' COMMENT 'Total number of books visited by the user',
  `total_saved_for_later_books` int DEFAULT '0' COMMENT 'Total number of books saved for later by the user',
  PRIMARY KEY (`user_id`),
  UNIQUE KEY `username` (`username`),
  UNIQUE KEY `email` (`email`)
) ENGINE=InnoDB AUTO_INCREMENT=27 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `users`
--

LOCK TABLES `users` WRITE;
/*!40000 ALTER TABLE `users` DISABLE KEYS */;
INSERT INTO `users` VALUES (1,'alice_writer','hp12345100','alice@example.com','Regular User','alice.jpg',0,3,'2025-03-09 10:54:49',0,0,0,0,2,0),(2,'bob_novelist','hp1234522','bob@example.com','Regular User','bob.jpg',0,1,'2025-03-09 10:54:49',0,0,0,0,1,0),(3,'charlie_poet','hp1234533','charlie@example.com','Regular User','charlie.jpg',0,3,'2025-03-09 10:54:49',1,1,1,1,1,1),(4,'david_storyteller','hp1234544','david@example.com','Regular User','david.jpg',0,1,'2025-03-09 10:54:49',2,2,1,2,1,1),(5,'emma_fantasy','hp1234555','emma@example.com','Regular User','emma.jpg',0,3,'2025-03-09 10:54:49',0,0,0,0,0,0),(6,'frank_mystery','hp1234566','frank@example.com','Regular User','frank.jpg',0,1,'2025-03-09 10:54:49',0,0,0,0,0,0),(7,'grace_thriller','hp1234577','grace@example.com','Regular User','grace.jpg',1,2,'2025-03-09 10:54:49',2,1,1,2,1,1),(8,'henry_horror','hp1234588','henry@example.com','Regular User','henry.jpg',0,0,'2025-03-09 10:54:49',0,0,0,0,0,0),(9,'isabel_youth','hp1234599','isabel@example.com','Regular User','isabel.jpg',0,12,'2025-03-09 10:54:49',1,2,3,1,1,0),(10,'jack_crime','hp12345100','jack@example.com','Regular User','jack.jpg',0,2,'2025-03-09 10:54:49',0,0,0,0,1,0),(11,'demo1','demo1234566','demo1@gmail.com','Regular User',NULL,0,0,'2025-03-29 11:20:26',0,0,0,0,0,0),(12,'demo2','demo2345677','demo2@gmail.com','Regular User',NULL,0,0,'2025-03-29 11:32:28',0,0,0,0,0,0),(13,'demo3','demo3456788','demo3@gmail.com','Regular User',NULL,0,0,'2025-03-29 11:35:37',0,0,0,0,0,0),(24,'111123','1111','1@gmail.com','Regular User','profile_24_1749137954904.png',3,7,'2025-06-04 12:54:06',3,0,0,3,10,3),(26,'2222244','22222','2@gmail.com','Regular User','profile_26_1749135062589.png',0,3,'2025-06-04 21:44:42',0,0,0,0,1,0);
/*!40000 ALTER TABLE `users` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2025-06-06 17:07:49
