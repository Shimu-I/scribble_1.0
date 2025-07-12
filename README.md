
# Scribble: A Story Sharing & Writing Platform

## Introduction
Scribble is a dynamic platform designed for individuals passionate about reading and writing across various genres. Drawing inspiration from platforms like Wattpad, Webnovel, Fable, Goodreads, GitHub, and Monkey Type, Scribble fosters creative expression, community interaction, and author support. It enables users to write, publish, support, collaborate, and engage with stories in a user-friendly environment.

## Motivation
As avid readers and users of platforms like Goodreads, Fable, Wattpad, and Webnovel, we identified the need for a platform that combines the best features of these applications while introducing innovative concepts to enhance interactivity and support for creativity. Scribble was created to reflect our passion for storytelling and community-driven engagement.

## Objectives
- Build a platform for authors and readers to connect through storytelling.
- Enable community-driven interactions via book-based groups.
- Allow users to support each other’s work financially or emotionally.
- Create genre-based weekly contests to encourage creativity.
- Implement real-time communication using multithreading and socket programming.
- Provide gamified experiences to keep users engaged.

## Features
### Core Platform Features
- **Support System**: Users can support writers whose work they find compelling.
- **Author Collaboration**: Users can co-author books with others on the platform.
- **Interactive Reader Community**: Readers can comment on and rate books.
- **Community Groups**:
  - Each book has a dedicated discussion group.
  - A default group exists for physical book exchanges.
  - Users can create custom book groups and invite others to join.
- **Profile View**:
  - Modify personal information.
  - View history, saved books, and supporter records.
  - Track created books, drafts, collaborations, and group memberships.

### Weekly Genre-Based Contest System
- Contests are available for four genres: Fantasy, Thriller Mystery, Youth Fiction, and Crime Horror.
- Each user can participate once per week.
- Voting is only available during the current week.
- At the end of the week:
  - Results are displayed in a “Previous Week” section.
  - Top 3 winners are determined by vote count and ranked.
  - Winners are restricted from participating for the next two weeks to ensure fairness.
- **Rules and Logic**:
  - Entry submission and voting are disabled after the week ends.
  - The previous week’s section is read-only to display results.

### Multithreading Implementation
Multithreading ensures real-time communication without freezing the UI and handles multiple users simultaneously:
- **Server-Side**: A fixed-size thread pool manages incoming client connections. Each client is assigned a handler running in a separate thread. Messages are read, broadcast to the appropriate group, and saved in the database using a thread-safe structure.
- **Client-Side**: A dedicated thread listens for incoming server messages, ensuring the JavaFX UI remains responsive. Messages are updated on the UI safely without interrupting user interactions.

### Socket Programming Implementation
Sockets enable real-time interaction using TCP-based communication:
- **Server-Side**: The server listens for client connections, assigns each client to a communication handler, and groups them by book discussion. Messages are received, broadcasted to the group, and saved in the database. Clients are safely removed from groups upon disconnection.
- **Client-Side**: Clients connect to the server, identify themselves with user and group information, send messages to the group, and receive real-time updates. The chat interface distinguishes between the sender and other participants.

### Game Section
To enhance engagement, Scribble includes two simple games:
1. **Puzzle Game**:
   - Users solve puzzles using book covers.
   - Difficulty levels: 3x3, 4x4, and 6x6.
2. **Tic-Tac-Toe Game**:
   - Users play against the computer.
   - Input clears automatically after 20 seconds.
   - The computer uses strategic logic for a higher chance of winning, making the game challenging.

## UI Screenshots
Below is an overview of Scribble’s user interface:

- **SignIn and SignOut Page**: Entry point for user authentication.
<img width="973" height="480" alt="image" src="https://github.com/user-attachments/assets/60beea65-b37c-4421-ace0-9b443fc64bdc" />
<img width="973" height="480" alt="image" src="https://github.com/user-attachments/assets/bef7a8dc-ea26-4132-9608-272bb36b2c73" />

- **Home Page and About Page (No User Signed In)**: Displays platform information and features.
<img width="972" height="550" alt="image" src="https://github.com/user-attachments/assets/20dbdb24-0050-4fa5-bde4-6aec498ef73c" />
<img width="975" height="547" alt="image" src="https://github.com/user-attachments/assets/0415ce27-8259-4c23-b164-83b001a0e1c9" />

- **Home Page and Books Library View (After Sign-In)**: Personalized dashboard and book browsing.
<img width="974" height="549" alt="image" src="https://github.com/user-attachments/assets/df4475b6-9e0e-43c8-97f3-d78a885bea79" />
<img width="972" height="547" alt="image" src="https://github.com/user-attachments/assets/46d899dc-5a71-4365-b2ae-a4a94b9b00b1" />

- **Opening a Book and First Chapter**: View book details and read chapters.
<img width="975" height="548" alt="image" src="https://github.com/user-attachments/assets/141cdcc4-204d-4404-be57-0cb00e5a9f6e" />
<img width="974" height="549" alt="image" src="https://github.com/user-attachments/assets/f35ff5f4-eaf8-4327-824d-82b8183f817b" />

- **Author Profile and Supporting Page**: View author details and support options.
<img width="974" height="549" alt="image" src="https://github.com/user-attachments/assets/d26fc916-3241-4b9d-b5be-d8b280754100" />
<img width="975" height="550" alt="image" src="https://github.com/user-attachments/assets/57a92a37-9734-4395-8732-0acfd3188ab4" />

- **Collaboration and Book Status View**: Manage co-authoring and book progress.
<img width="975" height="548" alt="image" src="https://github.com/user-attachments/assets/4f8f8570-3afc-49cb-b9e3-0c7b6f3df622" />
<img width="975" height="548" alt="image" src="https://github.com/user-attachments/assets/258997ab-b5ab-4c5e-8470-918042c065ae" />

- **Community Section**: Engage in book-based discussions and groups.
<img width="974" height="551" alt="image" src="https://github.com/user-attachments/assets/8003289e-5a93-4096-8b86-323f8c254784" />

- **Contest Section**:
<img width="974" height="550" alt="image" src="https://github.com/user-attachments/assets/15d7ddff-d7da-4f60-9221-75c3bff6f0c9" />

- Contest section  Fantasy sub-section view (current week and previous week)
<img width="974" height="551" alt="image" src="https://github.com/user-attachments/assets/db045b9b-6956-4961-8597-9071f2c628be" />
<img width="974" height="550" alt="image" src="https://github.com/user-attachments/assets/e0194716-3f4b-4b55-9ac9-a9d687864273" />

- opening the entry called “The Feather of Fire”
<img width="972" height="549" alt="image" src="https://github.com/user-attachments/assets/17ec0cb0-edb8-4203-bb35-fc4a833fb59f" />

- Contest section   Thriller Mystery sub-section view (current week and previous week)
<img width="974" height="550" alt="image" src="https://github.com/user-attachments/assets/1a191531-36b8-452d-8d4b-34311b03d0ca" />
<img width="974" height="549" alt="image" src="https://github.com/user-attachments/assets/b3ff58ee-de35-45b3-aadf-60c9430e86ae" />

- Contest section  Youth Fiction sub-section view (current week and previous week)
<img width="972" height="550" alt="image" src="https://github.com/user-attachments/assets/1f22a185-283d-40f7-846c-817a6e3d950e" />
<img width="974" height="551" alt="image" src="https://github.com/user-attachments/assets/5a69d942-21db-4559-8ddd-6c5131ca5c73" />

- Contest section  Crime Horror sub-section view (current week and previous week)
<img width="975" height="548" alt="image" src="https://github.com/user-attachments/assets/d8a3dc4e-5504-4928-ba04-7ca172b681d1" />
<img width="975" height="550" alt="image" src="https://github.com/user-attachments/assets/0896da57-91b2-4fb3-8d02-ac127b9d60a4" />

- **User Profile View**: Displays user history, saved books, and supporter records.
<img width="975" height="548" alt="image" src="https://github.com/user-attachments/assets/53948e37-e772-4074-8ea7-4ef45182d09f" />
<img width="975" height="549" alt="image" src="https://github.com/user-attachments/assets/94f48351-79a2-45ba-bf8a-d927533d1473" />

- **Profile Edit**: Modify personal information.
  <img width="975" height="551" alt="image" src="https://github.com/user-attachments/assets/5e3fd1f5-5c31-47e7-855a-111a4f705a7b" />

- **User’s Created Books and Drafts**: Track authored works and drafts.
  <img width="975" height="550" alt="image" src="https://github.com/user-attachments/assets/6d526d0c-9505-4001-a7b8-966cd2df304c" />

- **Collaboration Requests**: View sent and received collaboration requests.
  <img width="975" height="551" alt="image" src="https://github.com/user-attachments/assets/79703670-03e5-4538-80be-b0a36793ae28" />

- **Joined and Owned Groups**: Manage group memberships.
  <img width="975" height="548" alt="image" src="https://github.com/user-attachments/assets/4adf62fb-0e2e-4f9d-850a-abfb7019e6c7" />

- **Game Section**:
  <img width="974" height="549" alt="image" src="https://github.com/user-attachments/assets/7c954201-524c-4c79-924d-b7d7f1d15aca" />
  
  - Puzzle game view with book cover puzzles.
    <img width="974" height="549" alt="image" src="https://github.com/user-attachments/assets/64b00e90-0203-47a6-a083-c089b7ac9ee2" />
    <img width="975" height="551" alt="image" src="https://github.com/user-attachments/assets/92a57610-2dfb-4e08-8fbe-fabd87ccba76" />
    <img width="974" height="546" alt="image" src="https://github.com/user-attachments/assets/f410ef94-04a6-47cb-bc44-fa6e301d5e59" />

  - Tic-Tac-Toe game view for user vs. computer gameplay.
    <img width="974" height="549" alt="image" src="https://github.com/user-attachments/assets/690a1066-3799-4a78-964c-111e0180c6b5" />


## Conclusion
Scribble is a community-focused platform that combines storytelling, collaboration, and real-time interaction. With features like multithreading, socket-based chat systems, genre-based contests, and engaging games, Scribble offers a creative ecosystem for writers and readers. This project has allowed us to explore both the technical and creative aspects of building a vibrant digital community.

## Installation
1. Clone the repository:
   ```bash
   git clone https://github.com/username/scribble.git
   ```
2. Navigate to the project directory:
   ```bash
   cd scribble
   ```


## Usage
- **Sign Up/Login**: Create an account or log in to access features.
- **Write and Publish**: Create stories, save drafts, or collaborate with others.
- **Join Groups**: Participate in book discussions or create custom groups.
- **Contests**: Submit entries to weekly genre-based contests and vote on submissions.
- **Games**: Play puzzle or Tic-Tac-Toe games for added engagement.
- **Support Authors**: Support writers through the platform’s support system.

## Contributing
We welcome contributions to Scribble! To contribute:
1. Fork the repository.
2. Create a new branch (`git checkout -b feature-branch`).
3. Make your changes and commit (`git commit -m "Add feature"`).
4. Push to the branch (`git push origin feature-branch`).
5. Open a pull request.

## License
This project is licensed under the MIT License. See the [LICENSE](LICENSE) file for details.
