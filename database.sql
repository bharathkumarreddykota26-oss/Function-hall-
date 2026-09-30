-- HallBook complete MySQL database
CREATE DATABASE IF NOT EXISTS hallbook;
USE hallbook;

DROP TABLE IF EXISTS booking_items;
DROP TABLE IF EXISTS payments;
DROP TABLE IF EXISTS bookings;
DROP TABLE IF EXISTS food_items;
DROP TABLE IF EXISTS decorations;
DROP TABLE IF EXISTS chair_packages;
DROP TABLE IF EXISTS function_halls;
DROP TABLE IF EXISTS users;

CREATE TABLE users (
    id INT PRIMARY KEY AUTO_INCREMENT,
    full_name VARCHAR(100) NOT NULL,
    email VARCHAR(120) NOT NULL UNIQUE,
    phone VARCHAR(20),
    password_hash VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE function_halls (
    id INT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(120) NOT NULL,
    description VARCHAR(255),
    capacity INT NOT NULL,
    price DECIMAL(12,2) NOT NULL,
    active BOOLEAN DEFAULT TRUE
);

CREATE TABLE chair_packages (
    id INT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(120) NOT NULL,
    description VARCHAR(255),
    quantity INT NOT NULL,
    price DECIMAL(12,2) NOT NULL,
    active BOOLEAN DEFAULT TRUE
);

CREATE TABLE decorations (
    id INT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(120) NOT NULL,
    description VARCHAR(255),
    price DECIMAL(12,2) NOT NULL,
    active BOOLEAN DEFAULT TRUE
);

CREATE TABLE food_items (
    id INT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(120) NOT NULL,
    food_type ENUM('VEG','NON_VEG') NOT NULL,
    description VARCHAR(255),
    price DECIMAL(10,2) NOT NULL,
    active BOOLEAN DEFAULT TRUE
);

CREATE TABLE bookings (
    id INT PRIMARY KEY AUTO_INCREMENT,
    booking_id VARCHAR(40) NOT NULL UNIQUE,
    user_id INT NULL,
    customer_name VARCHAR(100) NOT NULL,
    phone VARCHAR(20) NOT NULL,
    event_date DATE NOT NULL,
    payment_method ENUM('UPI','CARD','CASH') NOT NULL,
    total DECIMAL(12,2) NOT NULL,
    status ENUM('PENDING','CONFIRMED','CANCELLED') DEFAULT 'PENDING',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE SET NULL
);

CREATE TABLE booking_items (
    id INT PRIMARY KEY AUTO_INCREMENT,
    booking_id INT NOT NULL,
    item_type ENUM('HALL','CHAIR','DECORATION','FOOD') NOT NULL,
    item_id INT NULL,
    item_name VARCHAR(150) NOT NULL,
    unit_price DECIMAL(12,2) NOT NULL,
    quantity INT NOT NULL DEFAULT 1,
    FOREIGN KEY (booking_id) REFERENCES bookings(id) ON DELETE CASCADE
);

CREATE TABLE payments (
    id INT PRIMARY KEY AUTO_INCREMENT,
    booking_id INT NOT NULL,
    payment_reference VARCHAR(80),
    amount DECIMAL(12,2) NOT NULL,
    method ENUM('UPI','CARD','CASH') NOT NULL,
    status ENUM('PENDING','PAID','FAILED') DEFAULT 'PENDING',
    paid_at TIMESTAMP NULL,
    FOREIGN KEY (booking_id) REFERENCES bookings(id) ON DELETE CASCADE
);

INSERT INTO function_halls(name,description,capacity,price) VALUES
('Royal Grand Hall','Plain hall - 800 guests',800,45000),
('Crystal Palace','AC hall - 500 guests',500,38000),
('Green Garden Hall','Garden venue - 700 guests',700,55000),
('Pearl Banquet','Premium hall - 350 guests',350,30000),
('Lakshmi Convention','Large hall - 1200 guests',1200,70000),
('Sunrise Mini Hall','Small function - 200 guests',200,18000),
('Golden Crown Hall','Wedding hall - 600 guests',600,52000),
('Lotus Celebration Hall','AC hall - 450 guests',450,42000);

INSERT INTO chair_packages(name,description,quantity,price) VALUES
('Plastic Chair Pack 100','100 chairs',100,4000),
('Banquet Chair Pack 100','100 padded chairs',100,7500),
('Royal Chair Pack 100','Cushioned premium chairs',100,12000),
('Chiavari Chair Pack 100','Elegant wedding chairs',100,15000),
('White Cover Chair 100','White covers included',100,8000),
('Gold Chair 100','Gold finish chairs',100,14000),
('VIP Sofa Seating','10 sofa sets',10,18000),
('Kids Chair Pack 50','50 colorful chairs',50,2500);

INSERT INTO decorations(name,description,price) VALUES
('Classic Stage Decor','Backdrop + flowers + lights',18000),
('Royal Wedding Decor','Premium stage + floral setup',35000),
('Floral Entrance','Fresh-style floral entrance',12000),
('LED Stage Package','LED wall + stage lights',28000),
('Mandap Decoration','Traditional mandap setup',25000),
('Reception Backdrop','Elegant reception backdrop',14000),
('Balloon Decoration','Theme balloon package',8000),
('Premium Table Decor','Centerpieces + table styling',16000);

INSERT INTO food_items(name,food_type,description,price) VALUES
('Paneer Butter Masala','VEG','Catering portion',120),
('Kadai Paneer','VEG','Catering portion',125),
('Palak Paneer','VEG','Catering portion',130),
('Shahi Paneer','VEG','Catering portion',135),
('Veg Biryani','VEG','Catering portion',140),
('Jeera Rice','VEG','Catering portion',120),
('Gobi Manchurian','VEG','Catering portion',130),
('Veg Noodles','VEG','Catering portion',125),
('Masala Dosa','VEG','Catering portion',110),
('Idli Sambar','VEG','Catering portion',90),
('Medu Vada','VEG','Catering portion',95),
('Chole Bhature','VEG','Catering portion',120),
('Dal Tadka','VEG','Catering portion',100),
('Dal Makhani','VEG','Catering portion',115),
('Aloo Gobi','VEG','Catering portion',105),
('Veg Kurma','VEG','Catering portion',120),
('Mushroom Masala','VEG','Catering portion',135),
('Baby Corn Fry','VEG','Catering portion',130),
('Gobi 65','VEG','Catering portion',125),
('Vegetable Pulao','VEG','Catering portion',115),
('Curd Rice','VEG','Catering portion',90),
('Rasam','VEG','Catering portion',70),
('Sambar','VEG','Catering portion',75),
('Chapati','VEG','Catering portion',60),
('Chicken Biryani','NON_VEG','Catering portion',180),
('Mutton Biryani','NON_VEG','Catering portion',240),
('Chicken 65','NON_VEG','Catering portion',190),
('Chicken Tikka','NON_VEG','Catering portion',210),
('Butter Chicken','NON_VEG','Catering portion',220),
('Chicken Curry','NON_VEG','Catering portion',190),
('Mutton Curry','NON_VEG','Catering portion',250),
('Mutton Fry','NON_VEG','Catering portion',260),
('Fish Fry','NON_VEG','Catering portion',210),
('Fish Curry','NON_VEG','Catering portion',200),
('Prawn Masala','NON_VEG','Catering portion',250),
('Prawn Fry','NON_VEG','Catering portion',270),
('Chicken Kebab','NON_VEG','Catering portion',200),
('Chicken Lollipop','NON_VEG','Catering portion',220),
('Tandoori Chicken','NON_VEG','Catering portion',230),
('Egg Curry','NON_VEG','Catering portion',140),
('Egg Biryani','NON_VEG','Catering portion',160),
('Pepper Chicken','NON_VEG','Catering portion',205),
('Chilli Chicken','NON_VEG','Catering portion',195),
('Chicken Manchurian','NON_VEG','Catering portion',195),
('Mutton Biryani Special','NON_VEG','Catering portion',280),
('Fish Tikka','NON_VEG','Catering portion',225),
('Chicken Chettinad','NON_VEG','Catering portion',215),
('Mutton Korma','NON_VEG','Catering portion',265);

-- Sample user (demo only; password is not used by this JDBC learning project)
INSERT INTO users(full_name,email,phone,password_hash)
VALUES ('Demo Customer','demo@example.com','9999999999','demo');

CREATE OR REPLACE VIEW v_food_menu AS
SELECT id,name,food_type,description,price FROM food_items WHERE active=TRUE;

CREATE OR REPLACE VIEW v_active_halls AS
SELECT * FROM function_halls WHERE active=TRUE;
