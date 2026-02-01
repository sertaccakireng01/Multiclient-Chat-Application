Multi-Client Chat Application (JavaFX & Sockets)

Youtube Video Link of Running Project: https://www.youtube.com/watch?v=WyT4d_sxHgk

This project is a real-time, GUI-based chat system built as part of the Advanced Java Programming course. 
It features a robust server-client architecture that allows multiple users to communicate simultaneously over a network, earning a perfect 100% score for its stability and technical implementation.

Key Features:

Real-Time Communication: Instant messaging between multiple clients via TCP/IP protocols.

Private Messaging: Users can select specific individuals from the connected clients list for direct communication.

GUI-Driven Workflow: Entirely built with JavaFX, from server port configuration to the final chat interface.

Dynamic Feedback: A live "Connected Clients" list that updates automatically as users join or leave.

Technical Highlights:

Networking: Implemented Java Sockets to establish reliable data streams between the server and multiple clients.

Concurrency: Used Multithreading on the server side to handle each client connection in an independent thread, preventing UI blocking or lag.

Architecture: Followed the MVC (Model-View-Controller) pattern to ensure a clean separation between network logic and the JavaFX interface.

Exception Handling: Robust management of network-related exceptions, ensuring the application remains stable during unexpected disconnections.

Developed as a 2-person group project, this application was a major milestone in understanding concurrent programming and network architectures. 
It successfully demonstrates how to manage complex data flows and user states in a distributed environment.
