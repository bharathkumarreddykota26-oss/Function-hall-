# HallBook - Function Hall Booking (Java JDBC + MySQL + Node.js)

This project is designed for a fresher learning JDBC.

## Architecture

Browser (HTML/CSS/JS)
        |
        | Node.js serves frontend
        v
      Website
        |
        | HTTP / JSON
        v
Java JDBC backend
        |
        | MySQL Connector/J
        v
MySQL database: hallbook

## Files

- index.html              Home
- halls.html              Function halls
- chairs.html             Hall + chairs
- decor.html              Decoration
- food.html               Veg + non-veg food + cart + checkout
- style.css               Styling + responsive design
- app.js                  Cart + animations + API calls
- server.js               Node.js static frontend server
- BookingServer.java      Java HTTP API + JDBC + CRUD/read endpoints
- database.sql            COMPLETE MySQL schema + sample data
- README.md               Setup guide

## Database tables

users
function_halls
chair_packages
decorations
food_items
bookings
booking_items
payments

There are 24+ VEG and 24+ NON_VEG food records.

## Required software

1. JDK 17+ recommended
2. MySQL Server 8+
3. Node.js 18+ recommended
4. MySQL Connector/J (JDBC driver)

## Step 1 - Create database

Open MySQL Workbench or MySQL command line and run:

SOURCE /full/path/to/database.sql;

Or open database.sql in MySQL Workbench and click Execute.

Check:

USE hallbook;
SHOW TABLES;

You should see all 8 tables.

## Step 2 - Configure Java JDBC password

Open BookingServer.java and change:

static final String DB_PASSWORD = "YOUR_MYSQL_PASSWORD";

For example:

static final String DB_PASSWORD = "root123";

Do NOT upload a real password to GitHub.

## Step 3 - Download MySQL Connector/J

Get MySQL Connector/J from the official MySQL website.
Put the .jar beside BookingServer.java.

Example filename:

mysql-connector-j-9.x.x.jar

The exact version can change. Use a version compatible with your JDK/MySQL installation.

## Step 4 - Compile Java

Windows:

javac -cp "mysql-connector-j-9.x.x.jar" BookingServer.java

Run:

java -cp ".;mysql-connector-j-9.x.x.jar" BookingServer

Linux/macOS:

javac -cp "mysql-connector-j-9.x.x.jar" BookingServer.java

java -cp ".:mysql-connector-j-9.x.x.jar" BookingServer

You should see:

Connected to MySQL database: hallbook
Java JDBC API: http://localhost:8080

## Step 5 - Start Node.js frontend

Open another terminal in this folder:

node server.js

Then open:

http://localhost:3000

## Step 6 - Test JDBC

Open:

http://localhost:8080/api/health

You should get:

{"status":"Java JDBC backend is running"}

Then:

http://localhost:8080/api/halls

This reads function halls from MySQL through JDBC.

## Booking flow

When the user confirms a booking:

Browser
 -> POST /api/bookings
 -> Java BookingServer
 -> JDBC Connection
 -> INSERT into bookings
 -> INSERT into booking_items
 -> INSERT into payments
 -> COMMIT
 -> response with booking ID

The Java code uses PreparedStatement for database writes.

## Important learning note

Node.js is used here to serve the frontend.
Java + JDBC is the database backend.

JDBC itself is NOT a frontend technology and Node.js is NOT JDBC.
They are separate technologies connected through the HTTP API in this project.

## Payment note

The UPI/Card/Cash options in this student project are a DEMO payment flow.
No real payment gateway or real money is processed.

For a real application, integrate a gateway such as Razorpay/Stripe and implement server-side payment verification.
