CREATE TABLE Users (
   id SERIAL PRIMARY KEY,
   name VARCHAR(255) NOT NULL,
   email VARCHAR(255) NOT NULL UNIQUE,
   password VARCHAR(255) NOT NULL,
   profile_picture VARCHAR(255),
   designation VARCHAR(255),
   birthdate DATE,
   join_date DATE,
);