--CREATE DATABASE school
USE school
CREATE TABLE [user](
	[user_id] INT NOT NULL UNIQUE,
	first_name nvarchar(100) NOT NULL CHECK(first_name NOT LIKE '%[0-9]%'),
	last_name nvarchar(100) NOT NULL CHECK(last_name NOT LIKE '%[0-9]%'),
	email nvarchar(256) NOT NULL CHECK(email LIKE '%_@_%._%'),
	biograpgy nvarchar(300),
	[password] nvarchar(max),
	[status] char(1) NOT NULL,
	CONSTRAINT status_discriminator CHECK("status" in ('S','W')),
	PRIMARY KEY ("user_id")
);

CREATE TABLE worker(
	[user_id] INT NOT NULL UNIQUE,
	phone_number nvarchar(20),
	teacher bit NOT NULL,
	organizer bit NOT NULL,
	PRIMARY KEY ([user_id]),
	FOREIGN KEY ([user_id]) REFERENCES [user] ON DELETE CASCADE
);


CREATE TABLE organizer(
	[user_id] INT NOT NULL,
	PRIMARY KEY (user_id),
	FOREIGN KEY (user_id) REFERENCES [user] ON DELETE CASCADE
);
CREATE TABLE department(
	department_id INT NOT NULL UNIQUE,
	dname NVARCHAR(50) NOT NULL,
	organizer_id INT NOT NULL,
	PRIMARY KEY (department_id),
	FOREIGN KEY (organizer_id) REFERENCES organizer ON DELETE CASCADE
);
CREATE TABLE teacher(
	[user_id] INT NOT NULL UNIQUE,
	title nvarchar(256) NOT NULL,
	cerdentials nvarchar(256),
	departament_id INT NOT NULL,
	PRIMARY KEY ("user_id"),
	FOREIGN KEY (departament_id) REFERENCES department ON DELETE CASCADE
);

CREATE TABLE student(
	[user_id] INT NOT NULL UNIQUE,
	departament_id INT NOT NULL,
	PRIMARY KEY ([user_id]),
	FOREIGN KEY ([user_id]) REFERENCES [user] ON DELETE CASCADE
	--,
	--FOREIGN KEY (teacher_id) REFERENCES teacher ON DELETE CASCADE
);

CREATE TABLE [session](
	session_id INT NOT NULL UNIQUE,
	departament_id int NOT NULL,
	title NVARCHAR(50) NOT NULL,
	description NVARCHAR(300),
	difficulty_level NVARCHAR(25),
	keywords NVARCHAR(256),
	start_date_time SMALLDATETIME NOT NULL,
	end_date_time SMALLDATETIME NOT NULL,
	star_reviews TINYINT
	PRIMARY KEY (session_id),
	FOREIGN KEY (departament_id) REFERENCES department ON DELETE CASCADE
);

CREATE TABLE building (
	building_id SMALLINT NOT NULL UNIQUE,
	[location] NVARCHAR(256),
	PRIMARY KEY(building_id)
);

CREATE TABLE room(
	room_id SMALLINT NOT NULL UNIQUE,
	building_id SMALLINT NOT NULL,
	PRIMARY KEY (room_id),
	FOREIGN KEY (building_id) REFERENCES building ON DELETE CASCADE
);

CREATE TABLE class_room(
	class_room_id SMALLINT NOT NULL UNIQUE,
	session_id INT NOT NULL,
	room_id SMALLINT NOT NULL,
	class_floor SMALLINT NOT NULL,
	capacity TINYINT NOT NULL,
	PRIMARY KEY (class_room_id),
	FOREIGN KEY (session_id) REFERENCES [session] ON DELETE CASCADE,
	FOREIGN KEY (room_id) REFERENCES room ON DELETE CASCADE
);
	