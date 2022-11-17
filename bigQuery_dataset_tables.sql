CREATE TABLE `school.user`
(
  user_id INT64 NOT NULL,
  first_name STRING NOT NULL,
  last_name STRING NOT NULL,
  email STRING NOT NULL,
  gender STRING NOT NULL,
  biography STRING,
  password STRING,
  status STRING NOT NULL
)
;

CREATE TABLE `school.worker`
(
  user_id INT64 NOT NULL,
  phone_number STRING,
  is_teacher BOOL NOT NULL,
  is_organizer BOOL NOT NULL
)
;

CREATE TABLE `school.organizer`
(
  user_id INT64 NOT NULL
)
;

CREATE TABLE `school.department`
(
  department_id INT64 NOT NULL,
  name STRING NOT NULL,
  organizer_id INT64 NOT NULL
)
;

CREATE TABLE `school.teacher`
(
  user_id INT64 NOT NULL,
  title STRING NOT NULL,
  credentials STRING,
  department_id INT64 NOT NULL
)
;
CREATE TABLE `school.student`
(
  user_id INT64 NOT NULL,
  department_id INT64 NOT NULL,
  teacher_id INT64 NOT NULL
)
;
CREATE TABLE `school.teacher_rating`
(
  student_id INT64 NOT NULL,
  teacher_id INT64 NOT NULL,
  rating INT64 NOT NULL
)
;
CREATE TABLE `school.session`
(
  session_id INT64 NOT NULL,
  department_id INT64 NOT NULL,
  title STRING NOT NULL,
  description STRING,
  difficulty_level STRING,
  keywords STRING,
)
;
CREATE TABLE `school.student_grades`
(
  student_id INT64 NOT NULL,
  session_id INT64 NOT NULL,
  grade INT64 NOT NULL
)
;

CREATE TABLE `school.session_rating`
(
  session_id INT64 NOT NULL,
  student_id INT64 NOT NULL,
  rating INT64 NOT NULL
)
;

CREATE TABLE `school.school`
(
  building_id INT64 NOT NULL,
  location STRING,
  name STRING
)
;

CREATE TABLE `school.room`
(
  room_id INT64 NOT NULL,
  school_id INT64 NOT NULL,
  floor INT64 NOT NULL,
  dor_number INT64 NOT NULL,
  type STRING
)
;
CREATE TABLE `school.classroom`
(
  classroom_id INT64 NOT NULL,
  room_id INT64 NOT NULL,
  capacity INT64 NOT NULL
)
;

CREATE TABLE `school.student_registration`
(
  student_id INT64 NOT NULL,
  classroom_id INT64 NOT NULL,
  datetime DATETIME NOT NULL
)
;
CREATE TABLE `school.session_registration`
(
  session_id INT64 NOT NULL,
  classroom_id INT64 NOT NULL
)
;




