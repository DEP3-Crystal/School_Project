CREATE TABLE "user"(
	"user_id" INT NOT NULL UNIQUE,
	first_name VARCHAR(100) NOT NULL CHECK(first_name NOT LIKE '%[0-9]%'),
	last_name VARCHAR(100) NOT NULL CHECK(last_name NOT LIKE '%[0-9]%'),
	email VARCHAR(256) NOT NULL CHECK(email LIKE '%_@_%._%'),
	gender CHAR(1) NOT NULL CHECK(gender in ('M','F')),
	biography VARCHAR(300),
	"password" TEXT,
	"status" CHAR(1) NOT NULL,
	CONSTRAINT per_status_discriminator CHECK("status" in ('S','W')),
	PRIMARY KEY ("user_id")
);

CREATE TABLE worker(
	"user_id" INT NOT NULL UNIQUE,
	phone_number VARCHAR(20),
	is_teacher BIT NOT NULL,
	is_organizer BIT NOT NULL,
	PRIMARY KEY ("user_id"),
	FOREIGN KEY ("user_id") REFERENCES "user" ON UPDATE CASCADE
);


CREATE TABLE organizer(
	"user_id" INT NOT NULL,
	PRIMARY KEY ("user_id"),
	FOREIGN KEY ("user_id") REFERENCES "user" ON UPDATE CASCADE
);
CREATE TABLE department(
	department_id INT NOT NULL UNIQUE,
	"name" VARCHAR(50) NOT NULL,
	organizer_id INT NOT NULL,
	PRIMARY KEY (department_id),
	FOREIGN KEY (organizer_id) REFERENCES organizer ON UPDATE CASCADE
);
CREATE TABLE teacher(
	"user_id" INT NOT NULL UNIQUE,
	title VARCHAR(256) NOT NULL,
	credentials VARCHAR(256),
	department_id INT NOT NULL,
	PRIMARY KEY ("user_id"),
	FOREIGN KEY (department_id) REFERENCES department ON UPDATE CASCADE
);

CREATE TABLE student(
	"user_id" INT NOT NULL UNIQUE,
	department_id INT NOT NULL,
	PRIMARY KEY ("user_id"),
	FOREIGN KEY ("user_id") REFERENCES "user" ON UPDATE CASCADE,
	FOREIGN KEY (teacher_id) REFERENCES teacher ON UPDATE CASCADE
);


CREATE TABLE teacher_rating(
	student_id INT NOT NULL,
	teacher_id INT NOT NULL,
	rating SMALLINT NOT NULL,
	PRIMARY KEY (student_id,teacher_id),
	FOREIGN KEY (student_id) REFERENCES student ON UPDATE CASCADE,
	FOREIGN KEY (teacher_id) REFERENCES teacher ON UPDATE CASCADE
);

CREATE TABLE "session"(
	session_id INT NOT NULL UNIQUE,
	department_id int NOT NULL,
	title VARCHAR(50) NOT NULL,
	description VARCHAR(300),
	difficulty_level VARCHAR(25),
	keywords VARCHAR(256),
	PRIMARY KEY (session_id),
	FOREIGN KEY (department_id) REFERENCES department ON UPDATE CASCADE
);

CREATE TABLE student_grades(
	student_id INT NOT NULL,
	session_id INT NOT NULL,
	grade SMALLINT NOT NULL,
	PRIMARY KEY (student_id,session_id),
	FOREIGN KEY (student_id) REFERENCES student ON UPDATE CASCADE,
	FOREIGN KEY (session_id) REFERENCES "session" ON UPDATE CASCADE
);

CREATE TABLE session_rating(
	session_id INT NOT NULL,
	student_id INT NOT NULL,
	rating INT NOT NULL,
	PRIMARY KEY (session_id,student_id),
	FOREIGN KEY (session_id) REFERENCES "session" ON UPDATE CASCADE,
	FOREIGN KEY (student_id) REFERENCES student ON UPDATE CASCADE
);

CREATE TABLE school (
	building_id SMALLINT NOT NULL UNIQUE,
	"location" VARCHAR(256),
	"name" VARCHAR(50),
	PRIMARY KEY(building_id)
);

CREATE TABLE room(
	room_id SMALLINT NOT NULL UNIQUE,
	school_id SMALLINT NOT NULL,
	"floor" SMALLINT NOT NULL,
	door_number SMALLINT NOT NULL,
	"type" VARCHAR(30),
	PRIMARY KEY (room_id),
	FOREIGN KEY (school_id) REFERENCES school ON UPDATE CASCADE
);

CREATE TABLE classroom(
	classroom_id SMALLINT NOT NULL UNIQUE,
	room_id SMALLINT NOT NULL,
	capacity SMALLINT NOT NULL,
	PRIMARY KEY (classroom_id),
	FOREIGN KEY (room_id) REFERENCES room ON UPDATE CASCADE
);

CREATE TABLE student_registration(
	student_id INT NOT NULL,
	classroom_id INT NOT NULL,
	datetime TIMESTAMP NOT NULL,
	PRIMARY KEY (student_id, classroom_id),
	FOREIGN KEY (student_id) REFERENCES student ON UPDATE CASCADE,
	FOREIGN KEY (classroom_id) REFERENCES classroom ON UPDATE CASCADE
);

CREATE TABLE session_registration(
    session_id int NOT NULL,
    classroom_id int NOT NULL,
	start_time TIMESTAMP NOT NULL,
	end_time TIMESTAMP NOT NULL,
    PRIMARY KEY (session_id, classroom_id),
    FOREIGN KEY (session_id) REFERENCES "session" ON UPDATE CASCADE,
    FOREIGN KEY (classroom_id) REFERENCES classroom ON UPDATE CASCADE
);