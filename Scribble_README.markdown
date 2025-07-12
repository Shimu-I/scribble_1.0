# Scribble: A Story Sharing & Writing Platform

## Introduction
Scribble is a dynamic platform designed for passionate readers and writers across various genres. Inspired by platforms like Wattpad, Webnovel, Fable, Goodreads, GitHub, and Monkey Type, Scribble fosters creative expression, community interaction, and author support. It enables users to write, publish, support, collaborate, and engage with stories in a user-friendly environment.

## Motivation
As avid readers and users of platforms like Goodreads, Fable, Wattpad, and Webnovel, we identified the need for a platform that aligns with our interests and amplifies creativity. Scribble combines the best features of these applications while introducing innovative concepts to make reading and writing more interactive and supportive.

## Objectives
- Build a platform for authors and readers to connect through storytelling.
- Enable community-driven interactions via book-based groups.
- Facilitate financial and emotional support for writers.
- Introduce genre-based weekly contests to spark creativity.
- Implement real-time communication using multithreading and socket programming.
- Provide gamified experiences to enhance user engagement.

## Features

### Core Platform Features
- **Support System**: Users can support writers whose work they admire.
- **Author Collaboration**: Users can co-author books with others on the platform.
- **Interactive Reader Community**: Readers can comment on and rate books.
- **Community Groups**:
  - Each book has a dedicated discussion group.
  - A default group exists for physical book exchange.
  - Users can create and invite others to custom book groups.
- **Profile View**:
  - Modify personal information.
  - View history, saved books, and supporter records.
  - Track created books, drafts, collaborations, and group memberships.

### Weekly Genre-Based Contest System
- **Genres**: Fantasy, Thriller Mystery, Youth Fiction, and Crime Horror.
- **Participation**: Each user can participate once per week.
- **Voting**: Available only during the current week.
- **End of Week**:
  - Results are displayed in a “Previous Week” section.
  - Top 3 winners are ranked by vote count.
  - Winners are restricted from participating for the next two weeks to ensure fairness.
- **Rules**:
  - Entry submission and voting are disabled after the week ends.
  - Previous week’s results are read-only.

### Multithreading Implementation
Multithreading ensures real-time communication without freezing the UI and handles multiple users simultaneously:
- **Server-Side**:
  - A fixed-size thread pool manages incoming client connections.
  - Each client connection is assigned to a handler running in a separate thread.
  - Messages are read, broadcast to the correct group, and saved in the database.
  - A thread-safe structure ensures reliable parallel user management.
- **Client-Side**:
  - A dedicated thread listens for incoming server messages.
  - The background thread keeps the JavaFX UI responsive, updating messages safely.

### Socket Programming Implementation
Sockets enable real-time, TCP-based communication between server and clients:
- **Server-Side**:
  - Listens for client connections and assigns each to a communication handler.
  - Clients are grouped by book discussion groups.
  - Messages are received, broadcasted to the group, and saved in the database.
  - Disconnected clients are safely removed from groups.
- **Client-Side**:
  - Clients connect to the server, identifying themselves with user and group information.
  - Users can send messages to the group and receive real-time updates.
  - The chat interface distinguishes between sender and participant messages.

### Game Section
To enhance engagement, Scribble includes two simple games:
1. **Puzzle Game**:
   - Solve puzzles using book covers.
   - Difficulty levels: 3x3, 4x4, and 6x6.
2. **Tic-Tac-Toe Game**:
   - Play against the computer.
   - Input clears automatically after 20 seconds.
   - The computer uses strategic logic for a higher winning chance, making the game challenging.

## UI Screenshots
The following UI designs showcase Scribble’s functionality:
- **SignIn and SignOut Page**
- **Home Page and About Page** (no user signed in)
- **Home Page and Books Library View** (after user sign-in)
- **Book and First Chapter View**
- **Author Profile and Supporting Page**
- **Collaboration and Book Status View**
- **Community Section**
- **Contest Section** (with sub-sections for Fantasy, Thriller Mystery, Youth Fiction, and Crime Horror)
- **User Profile View** (history, saved books, supporter records)
- **Profile Edit**
- **Supporter Information Records**
- **User’s Created Books and Drafts**
- **Collaboration Requests (Sent and Received)**
- **Joined and Owned Group Records**
- **Game Section** (Puzzle Game and Tic-Tac-Toe views)

## Conclusion
Scribble is a community-focused platform that integrates storytelling, collaboration, and real-time interaction. With features like multithreading, socket-based chat systems, genre-based contests, and engaging games, Scribble transcends traditional reading apps to create a vibrant creative ecosystem. This project has allowed us to explore technical, creative, and social dimensions, building a dynamic digital community for storytellers and readers alike.