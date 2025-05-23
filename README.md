# Board Games Hub

A desktop application built with JavaFX for playing board games. This project was developed as part of the IDATT2003 course by group 57.

---

## Table of Contents
- [Overview](#overview)
- [Games](#games)
- [Installation](#installation)
- [Running the Application](#running-the-application)

---

## Overview

Board Games Hub is a JavaFX-based desktop application designed to bring the fun of board games to your computer. The current implementation includes **Snakes and Ladders** and **Cluedo**.

---

## Games

**Current Game:**
- **Snakes and Ladders:** Experience a classic board game with automatic dice rolling and game progression.
-  **Cluedo:** Solve the case before your opponents do!

---

## Installation

### Prerequisites

- **Java JDK 21 or later:** Ensure you have JDK 21 installed on your system.
- **Maven:** Required for building and managing project dependencies.
- **JavaFX:** The project utilizes JavaFX libraries (automatically managed via Maven).

### Clone the Repository

Clone the repository using Git:

```bash
git clone https://github.com/Tmpecho/idatt2003-mappe-2025-gr57.git
cd idatt2003-mappe-2025-gr57
```

### Running the Application

You can build and run the application using Maven with the following command:

```bash
mvn clean javafx:run
```

or with the `.jar`-file (assuming you've downloaded the javafx-sdk)
```bash
java --module-path /path/to/javafx-sdk-21/lib \ --add-modules javafx.controls \ -jar boardgame-21.jar
```
