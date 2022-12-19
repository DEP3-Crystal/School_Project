CREATE TABLE public."Department" (
    department_id integer NOT NULL,
    name character varying(50),
    user_id integer
);


CREATE TABLE public."Employee" (
    user_id integer,
    phone_number character varying(20),
    title character varying(255),
    role character(1)
);


CREATE TABLE public.room (
    room_id smallint NOT NULL,
    school_id smallint NOT NULL,
    floor smallint NOT NULL,
    door_number smallint NOT NULL,
    type character varying(30)
);


CREATE TABLE public.school (
    school_id integer NOT NULL,
    location character varying(256),
    name character varying(50)
);


CREATE TABLE public.session (
    session_id integer NOT NULL,
    department_id integer NOT NULL,
    title character varying(50) NOT NULL,
    description character varying(300),
    difficulty_level character varying(25),
    keywords character varying(256),
    type character varying,
    start_time timestamp without time zone,
    end_time timestamp without time zone
);


CREATE TABLE public.session_rating (
    session_id integer NOT NULL,
    user_id integer NOT NULL,
    rating integer NOT NULL
);


CREATE TABLE public.session_registration (
    session_id integer NOT NULL,
    room_id integer NOT NULL,
    start_time timestamp without time zone NOT NULL,
    end_time timestamp without time zone NOT NULL
);


CREATE TABLE public.student_grades (
    user_id integer NOT NULL,
    session_id integer NOT NULL,
    grade smallint NOT NULL
);


CREATE TABLE public.student_registration (
    user_id integer NOT NULL,
    room_id integer NOT NULL,
    datetime timestamp without time zone NOT NULL
);


CREATE TABLE public.teacher (
    user_id integer NOT NULL,
    credentials character varying(256)
);



CREATE TABLE public.teacher_rating (
    user_id integer NOT NULL,
    teacher_id integer NOT NULL,
    rating smallint NOT NULL
);


CREATE TABLE public.users (
    user_id integer NOT NULL,
    first_name character varying(100),
    last_name character varying(100),
    email character varying(256),
    gender character(1),
    biography character varying(300),
    password character varying(300),
    department_id integer,
    is_employee boolean
);


ALTER TABLE ONLY public."Department"
    ADD CONSTRAINT "Department_pkey" PRIMARY KEY (department_id);

ALTER TABLE ONLY public.users
    ADD CONSTRAINT person_pkey PRIMARY KEY (user_id);


ALTER TABLE ONLY public.room
    ADD CONSTRAINT room_pkey PRIMARY KEY (room_id);


ALTER TABLE ONLY public.school
    ADD CONSTRAINT school_pkey PRIMARY KEY (school_id);


ALTER TABLE ONLY public.session
    ADD CONSTRAINT session_pkey PRIMARY KEY (session_id);


ALTER TABLE ONLY public.session_rating
    ADD CONSTRAINT session_rating_pkey PRIMARY KEY (session_id, user_id);


ALTER TABLE ONLY public.session_registration
    ADD CONSTRAINT session_registration_pkey PRIMARY KEY (session_id, room_id);


ALTER TABLE ONLY public.student_grades
    ADD CONSTRAINT student_grades_pkey PRIMARY KEY (user_id, session_id);


ALTER TABLE ONLY public.student_registration
    ADD CONSTRAINT student_registration_pkey PRIMARY KEY (user_id, room_id);


ALTER TABLE ONLY public.teacher
    ADD CONSTRAINT teacher_pkey PRIMARY KEY (user_id);


ALTER TABLE ONLY public.teacher_rating
    ADD CONSTRAINT teacher_rating_pkey PRIMARY KEY (user_id, teacher_id);



CREATE INDEX "PK/FK" ON public."Employee" USING btree (user_id);


ALTER TABLE ONLY public.room
    ADD CONSTRAINT room_school_id_fkey FOREIGN KEY (school_id) REFERENCES public.school(school_id) ON UPDATE CASCADE;


ALTER TABLE ONLY public.session
    ADD CONSTRAINT session_department_id_fkey FOREIGN KEY (department_id) REFERENCES public."Department"(department_id) ON UPDATE CASCADE;


ALTER TABLE ONLY public.session_rating
    ADD CONSTRAINT session_rating_session_id_fkey FOREIGN KEY (session_id) REFERENCES public.session(session_id) ON UPDATE CASCADE;


ALTER TABLE ONLY public.session_rating
    ADD CONSTRAINT session_rating_user_id_fkey FOREIGN KEY (user_id) REFERENCES public.users(user_id) ON UPDATE CASCADE;


ALTER TABLE ONLY public.session_registration
    ADD CONSTRAINT session_registration_room_id_fkey FOREIGN KEY (room_id) REFERENCES public.room(room_id) ON UPDATE CASCADE;


ALTER TABLE ONLY public.session_registration
    ADD CONSTRAINT session_registration_session_id_fkey FOREIGN KEY (session_id) REFERENCES public.session(session_id) ON UPDATE CASCADE;


ALTER TABLE ONLY public.student_grades
    ADD CONSTRAINT student_grades_session_id_fkey FOREIGN KEY (session_id) REFERENCES public.session(session_id) ON UPDATE CASCADE;


ALTER TABLE ONLY public.student_grades
    ADD CONSTRAINT student_grades_user_id_fkey FOREIGN KEY (user_id) REFERENCES public.users(user_id) ON UPDATE CASCADE;

ALTER TABLE ONLY public.student_registration
    ADD CONSTRAINT student_registration_room_id_fkey FOREIGN KEY (room_id) REFERENCES public.room(room_id) ON UPDATE CASCADE;

ALTER TABLE ONLY public.student_registration
    ADD CONSTRAINT student_registration_user_id_fkey FOREIGN KEY (user_id) REFERENCES public.users(user_id) ON UPDATE CASCADE;

ALTER TABLE ONLY public.teacher_rating
    ADD CONSTRAINT teacher_rating_teacher_id_fkey FOREIGN KEY (teacher_id) REFERENCES public.teacher(user_id) ON UPDATE CASCADE;

ALTER TABLE ONLY public.teacher_rating
    ADD CONSTRAINT teacher_rating_user_id_fkey FOREIGN KEY (user_id) REFERENCES public.users(user_id) ON UPDATE CASCADE;