--
-- PostgreSQL database dump
--

-- Dumped from database version 12.4
-- Dumped by pg_dump version 12.4

\connect declarative

SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SELECT pg_catalog.set_config('search_path', '', false);
SET check_function_bodies = false;
SET xmloption = content;
SET client_min_messages = warning;
SET row_security = off;

--
-- Name: Event_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public."Event_id_seq"
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public."Event_id_seq" OWNER TO postgres;

--
-- Name: PackageEvent_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public."PackageEvent_id_seq"
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public."PackageEvent_id_seq" OWNER TO postgres;

--
-- Name: PackageMaintainerEvent_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public."PackageMaintainerEvent_id_seq"
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public."PackageMaintainerEvent_id_seq" OWNER TO postgres;

SET default_tablespace = '';

SET default_table_access_method = heap;

--
-- Name: package_maintainer; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.package_maintainer (
    id integer NOT NULL,
    user_id integer NOT NULL,
    package text NOT NULL,
    repository_id integer NOT NULL,
    deleted boolean DEFAULT false NOT NULL
);


ALTER TABLE public.package_maintainer OWNER TO postgres;

--
-- Name: PackageMaintainer_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public."PackageMaintainer_id_seq"
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public."PackageMaintainer_id_seq" OWNER TO postgres;

--
-- Name: PackageMaintainer_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public."PackageMaintainer_id_seq" OWNED BY public.package_maintainer.id;


--
-- Name: package; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.package (
    id integer NOT NULL,
    name text NOT NULL,
    version text NOT NULL,
    description text NOT NULL,
    author text NOT NULL,
    maintainer_id integer NOT NULL,
    repository_id integer NOT NULL,
    depends text,
    imports text,
    suggests text,
    system_requirements text,
    license text NOT NULL,
    url text,
    source text NOT NULL,
    title text NOT NULL,
    active boolean DEFAULT false NOT NULL,
    deleted boolean DEFAULT false NOT NULL,
    md5sum text
);


ALTER TABLE public.package OWNER TO postgres;

--
-- Name: Package_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public."Package_id_seq"
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public."Package_id_seq" OWNER TO postgres;

--
-- Name: Package_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public."Package_id_seq" OWNED BY public.package.id;


--
-- Name: RepositoryEvent_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public."RepositoryEvent_id_seq"
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public."RepositoryEvent_id_seq" OWNER TO postgres;

--
-- Name: RepositoryMaintainerEvent_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public."RepositoryMaintainerEvent_id_seq"
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public."RepositoryMaintainerEvent_id_seq" OWNER TO postgres;

--
-- Name: repository_maintainer; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.repository_maintainer (
    id integer NOT NULL,
    user_id integer NOT NULL,
    repository_id integer NOT NULL,
    deleted boolean DEFAULT false NOT NULL
);


ALTER TABLE public.repository_maintainer OWNER TO postgres;

--
-- Name: RepositoryMaintainer_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public."RepositoryMaintainer_id_seq"
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public."RepositoryMaintainer_id_seq" OWNER TO postgres;

--
-- Name: RepositoryMaintainer_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public."RepositoryMaintainer_id_seq" OWNED BY public.repository_maintainer.id;


--
-- Name: repository; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.repository (
    version integer DEFAULT 0 NOT NULL,
    id integer NOT NULL,
    publication_uri text NOT NULL,
    name text NOT NULL,
    server_address text NOT NULL,
    published boolean DEFAULT false NOT NULL,
    deleted boolean DEFAULT false NOT NULL
);


ALTER TABLE public.repository OWNER TO postgres;

--
-- Name: Repository_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public."Repository_id_seq"
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public."Repository_id_seq" OWNER TO postgres;

--
-- Name: Repository_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public."Repository_id_seq" OWNED BY public.repository.id;


--
-- Name: role; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.role (
    id integer NOT NULL,
    value integer NOT NULL,
    name text NOT NULL,
    description text NOT NULL
);


ALTER TABLE public.role OWNER TO postgres;

--
-- Name: Role_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public."Role_id_seq"
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public."Role_id_seq" OWNER TO postgres;

--
-- Name: Role_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public."Role_id_seq" OWNED BY public.role.id;


--
-- Name: SubmissionEvent_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public."SubmissionEvent_id_seq"
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public."SubmissionEvent_id_seq" OWNER TO postgres;

--
-- Name: submission; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.submission (
    id integer NOT NULL,
    submitter_id integer NOT NULL,
    package_id integer NOT NULL,
    changes text,
    accepted boolean DEFAULT false NOT NULL,
    deleted boolean DEFAULT false NOT NULL
);


ALTER TABLE public.submission OWNER TO postgres;

--
-- Name: Submission_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public."Submission_id_seq"
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public."Submission_id_seq" OWNER TO postgres;

--
-- Name: Submission_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public."Submission_id_seq" OWNED BY public.submission.id;


--
-- Name: UserEvent_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public."UserEvent_id_seq"
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public."UserEvent_id_seq" OWNER TO postgres;

--
-- Name: user; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public."user" (
    id integer NOT NULL,
    role_id integer DEFAULT 4 NOT NULL,
    name text NOT NULL,
    email text NOT NULL,
    login text NOT NULL,
    active boolean NOT NULL,
    last_logged_in_on date,
    deleted boolean DEFAULT false NOT NULL
);


ALTER TABLE public."user" OWNER TO postgres;

--
-- Name: User_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public."User_id_seq"
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;
    
ALTER TABLE public."User_id_seq" OWNER TO postgres;

--
-- Name: User_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public."User_id_seq" OWNED BY public."user".id;

---------------------------------------------------------------
    
CREATE TABLE public.api_token (
	id integer NOT NULL,
	token character varying(255) NOT NULL,
	user_login character varying(255) NOT NULL
);

ALTER TABLE public.api_token OWNER TO postgres;

CREATE SEQUENCE public."Api_token_id_seq"
	START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;   
    
ALTER SEQUENCE public."Api_token_id_seq" OWNED BY public.api_token.id;  
 
--------------------------------------------------------------- 

--
-- Name: event; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.event (
    id integer DEFAULT nextval('public."Event_id_seq"'::regclass) NOT NULL,
    value character varying(255) NOT NULL
);


ALTER TABLE public.event OWNER TO postgres;

--
-- Name: package_event; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.package_event (
    id integer DEFAULT nextval('public."PackageEvent_id_seq"'::regclass) NOT NULL,
    date date DEFAULT ('now'::text)::date NOT NULL,
    package_id integer NOT NULL,
    event_id integer NOT NULL,
    changed_variable text NOT NULL,
    value_before text NOT NULL,
    value_after text NOT NULL,
    changed_by integer NOT NULL,
    "time" time with time zone DEFAULT now()
);


ALTER TABLE public.package_event OWNER TO postgres;

--
-- Name: package_maintainer_event; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.package_maintainer_event (
    id integer DEFAULT nextval('public."PackageMaintainerEvent_id_seq"'::regclass) NOT NULL,
    date date DEFAULT ('now'::text)::date NOT NULL,
    package_maintainer_id integer NOT NULL,
    event_id integer NOT NULL,
    changed_variable text NOT NULL,
    value_before text NOT NULL,
    value_after text NOT NULL,
    changed_by integer NOT NULL,
    "time" time with time zone DEFAULT now()
);


ALTER TABLE public.package_maintainer_event OWNER TO postgres;

--
-- Name: repository_event; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.repository_event (
    id integer DEFAULT nextval('public."RepositoryEvent_id_seq"'::regclass) NOT NULL,
    date date DEFAULT ('now'::text)::date NOT NULL,
    repository_id integer NOT NULL,
    event_id integer NOT NULL,
    changed_variable text NOT NULL,
    value_before text NOT NULL,
    value_after text NOT NULL,
    changed_by integer NOT NULL,
    "time" time with time zone DEFAULT now()
);


ALTER TABLE public.repository_event OWNER TO postgres;

--
-- Name: repository_maintainer_event; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.repository_maintainer_event (
    id integer DEFAULT nextval('public."RepositoryMaintainerEvent_id_seq"'::regclass) NOT NULL,
    date date DEFAULT ('now'::text)::date NOT NULL,
    repository_maintainer_id integer NOT NULL,
    event_id integer NOT NULL,
    changed_variable text NOT NULL,
    value_before text NOT NULL,
    value_after text NOT NULL,
    changed_by integer NOT NULL,
    "time" time with time zone DEFAULT now()
);


ALTER TABLE public.repository_maintainer_event OWNER TO postgres;

--
-- Name: submission_event; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.submission_event (
    id integer DEFAULT nextval('public."SubmissionEvent_id_seq"'::regclass) NOT NULL,
    date date DEFAULT ('now'::text)::date NOT NULL,
    submission_id integer NOT NULL,
    event_id integer NOT NULL,
    changed_variable text NOT NULL,
    value_before text NOT NULL,
    value_after text NOT NULL,
    changed_by integer NOT NULL,
    "time" time with time zone DEFAULT now()
);


ALTER TABLE public.submission_event OWNER TO postgres;

--
-- Name: user_event; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.user_event (
    id integer DEFAULT nextval('public."UserEvent_id_seq"'::regclass) NOT NULL,
    date date DEFAULT ('now'::text)::date NOT NULL,
    user_id integer NOT NULL,
    event_id integer NOT NULL,
    changed_variable text NOT NULL,
    value_before text NOT NULL,
    value_after text NOT NULL,
    changed_by integer NOT NULL,
    "time" time with time zone DEFAULT now()
);


ALTER TABLE public.user_event OWNER TO postgres;

--
-- Name: package id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.package ALTER COLUMN id SET DEFAULT nextval('public."Package_id_seq"'::regclass);


--
-- Name: package_maintainer id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.package_maintainer ALTER COLUMN id SET DEFAULT nextval('public."PackageMaintainer_id_seq"'::regclass);


--
-- Name: repository id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.repository ALTER COLUMN id SET DEFAULT nextval('public."Repository_id_seq"'::regclass);


--
-- Name: repository_maintainer id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.repository_maintainer ALTER COLUMN id SET DEFAULT nextval('public."RepositoryMaintainer_id_seq"'::regclass);


--
-- Name: role id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.role ALTER COLUMN id SET DEFAULT nextval('public."Role_id_seq"'::regclass);


--
-- Name: submission id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.submission ALTER COLUMN id SET DEFAULT nextval('public."Submission_id_seq"'::regclass);


--
-- Name: user id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public."user" ALTER COLUMN id SET DEFAULT nextval('public."User_id_seq"'::regclass);

ALTER TABLE ONLY public.api_token ALTER COLUMN id SET DEFAULT nextval('public."Api_token_id_seq"'::regclass);


--
-- Data for Name: event; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.event (id, value) FROM stdin;
1	create
2	delete
3	update
\.

--
-- Data for Name: api_token; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.api_token(id, token, user_login) FROM stdin;
2	eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJlaW5zdGVpbiJ9.9VweA_kotRnnLn9giSE511MhWX4iDwtx85lidw_ZT5iTQ1aOB-3ytJNDB_Mrcop2H22MNhMjbpUW_sraHdvOlw	einstein
3	eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJ0ZXNsYSJ9.FEQ3KqMvTj4LQAgQx23f6Y0Z7PzKHgcO1a1UodG5iwCrzXhk6tHCR6V0T16F1tWtMMF0a3AQIShczN__d6KsFA	tesla
4	eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJnYWxpZWxlbyJ9.Hp95DiIZ0L0JXyQZOvhJkzyTDzNuos81QoTWfLeVPlodWvGg7ziJTI6nJFitg5VAwrGmA4wpbWbjK9aItCKB3A	galieleo
5	eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJuZXd0b24ifQ.3E7UwKTwc8DchKRUSD_hdJxOcl4L6SOguwbm9WmVzWU4YDQMkIJ_wVNidpus6gNJvyT6OR6pREkfQCnWkEhEBQ	newton
\.


--
-- Data for Name: package; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.package (id, name, version, description, author, maintainer_id, repository_id, depends, imports, suggests, system_requirements, license, url, source, title, active, deleted, md5sum) FROM stdin;
\.


--
-- Data for Name: package_event; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.package_event (id, date, package_id, event_id, changed_variable, value_before, value_after, changed_by, "time") FROM stdin;
\.


--
-- Data for Name: package_maintainer; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.package_maintainer (id, user_id, package, repository_id, deleted) FROM stdin;
\.


--
-- Data for Name: package_maintainer_event; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.package_maintainer_event (id, date, package_maintainer_id, event_id, changed_variable, value_before, value_after, changed_by, "time") FROM stdin;
\.


--
-- Data for Name: repository; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.repository (version, id, publication_uri, name, server_address, published, deleted) FROM stdin;
0	2	http://localhost/repo/oldWrongUri	A	http://oa-rdepot-repo:8080/oldWrongUri	f	f
0	3	http://localhost/repo/B	B	http://oa-rdepot-repo:8080/B	f	f
0	4	http://localhost/repo/C	C	http://oa-rdepot-repo:8080/C	f	f
0	5	http://localhost/repo/G	G	http://oa-rdepot-repo:8080/G	f	f
0	6	http://localhost/repo/H	H	http://oa-rdepot-repo:8080/H	f	t
\.


--
-- Data for Name: repository_event; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.repository_event (id, date, repository_id, event_id, changed_variable, value_before, value_after, changed_by, "time") FROM stdin;
\.


--
-- Data for Name: repository_maintainer; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.repository_maintainer (id, user_id, repository_id, deleted) FROM stdin;
\.


--
-- Data for Name: repository_maintainer_event; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.repository_maintainer_event (id, date, repository_maintainer_id, event_id, changed_variable, value_before, value_after, changed_by, "time") FROM stdin;
\.


--
-- Data for Name: role; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.role (id, value, name, description) FROM stdin;
2	1	packagemaintainer	Package Maintainer
3	2	repositorymaintainer	Repository Maintainer
4	3	admin	Administrator
1	0	user	User
\.


--
-- Data for Name: submission; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.submission (id, submitter_id, package_id, changes, accepted, deleted) FROM stdin;
\.


--
-- Data for Name: submission_event; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.submission_event (id, date, submission_id, event_id, changed_variable, value_before, value_after, changed_by, "time") FROM stdin;
\.


--
-- Data for Name: user; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public."user" (id, role_id, name, email, login, active, last_logged_in_on, deleted) FROM stdin;
5	3	Nikola Tesla	tesla@ldap.forumsys.com	tesla	t	\N	f
6	2	Galileo Galilei	galieleo@ldap.forumsys.com	galieleo	t	\N	f
7	1	Isaac Newton	newton@ldap.forumsys.com	newton	t	\N	f
8	4	Local Admin User	admin@localhost	admin	t	\N	f
4	4	Albert Einstein	einstein@ldap.forumsys.com	einstein	t	2020-08-28	f
\.


--
-- Data for Name: user_event; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.user_event (id, date, user_id, event_id, changed_variable, value_before, value_after, changed_by, "time") FROM stdin;
1	2020-08-28	4	1	created			4	10:18:19.339546+00
2	2020-08-28	5	1	created			5	10:18:19.350146+00
3	2020-08-28	6	1	created			6	10:18:19.359002+00
4	2020-08-28	7	1	created			7	10:18:19.36725+00
5	2020-08-28	8	1	created			8	10:18:19.375431+00
9	2020-08-28	4	3	last logged in	null	Fri Aug 28 10:20:16 GMT 2020	4	10:20:16.05+00
\.


--
-- Name: Event_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public."Event_id_seq"', 3, true);


--
-- Name: PackageEvent_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public."PackageEvent_id_seq"', 3, true);


--
-- Name: PackageMaintainerEvent_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public."PackageMaintainerEvent_id_seq"', 1, true);


--
-- Name: PackageMaintainer_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public."PackageMaintainer_id_seq"', 1, true);


--
-- Name: Package_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public."Package_id_seq"', 2, true);


--
-- Name: RepositoryEvent_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public."RepositoryEvent_id_seq"', 10, true);


--
-- Name: RepositoryMaintainerEvent_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public."RepositoryMaintainerEvent_id_seq"', 1, true);


--
-- Name: RepositoryMaintainer_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public."RepositoryMaintainer_id_seq"', 1, true);


--
-- Name: Repository_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public."Repository_id_seq"', 6, true);


--
-- Name: Role_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public."Role_id_seq"', 4, true);


--
-- Name: SubmissionEvent_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public."SubmissionEvent_id_seq"', 3, true);


--
-- Name: Submission_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public."Submission_id_seq"', 3, true);


--
-- Name: UserEvent_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public."UserEvent_id_seq"', 9, true);


--
-- Name: User_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public."User_id_seq"', 8, true);


--
-- Name: Api_token_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public."Api_token_id_seq"', 5, true);


--
-- Name: event Event_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.event
    ADD CONSTRAINT "Event_pkey" PRIMARY KEY (id);


--
-- Name: event Event_value_key; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.event
    ADD CONSTRAINT "Event_value_key" UNIQUE (value);


--
-- Name: package_event PackageEvent_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.package_event
    ADD CONSTRAINT "PackageEvent_pkey" PRIMARY KEY (id);


--
-- Name: package_maintainer_event PackageMaintainerEvent_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.package_maintainer_event
    ADD CONSTRAINT "PackageMaintainerEvent_pkey" PRIMARY KEY (id);


--
-- Name: package_maintainer PackageMaintainer_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.package_maintainer
    ADD CONSTRAINT "PackageMaintainer_pkey" PRIMARY KEY (id);


--
-- Name: package Package_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.package
    ADD CONSTRAINT "Package_pkey" PRIMARY KEY (id);


--
-- Name: repository_event RepositoryEvent_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.repository_event
    ADD CONSTRAINT "RepositoryEvent_pkey" PRIMARY KEY (id);


--
-- Name: repository_maintainer_event RepositoryMaintainerEvent_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.repository_maintainer_event
    ADD CONSTRAINT "RepositoryMaintainerEvent_pkey" PRIMARY KEY (id);


--
-- Name: repository_maintainer RepositoryMaintainer_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.repository_maintainer
    ADD CONSTRAINT "RepositoryMaintainer_pkey" PRIMARY KEY (id);


--
-- Name: repository Repository_name_key; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.repository
    ADD CONSTRAINT "Repository_name_key" UNIQUE (name);


--
-- Name: repository Repository_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.repository
    ADD CONSTRAINT "Repository_pkey" PRIMARY KEY (id);


--
-- Name: repository Repository_publication_uri_key; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.repository
    ADD CONSTRAINT "Repository_publication_uri_key" UNIQUE (publication_uri);


--
-- Name: role Role_description_key; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.role
    ADD CONSTRAINT "Role_description_key" UNIQUE (description);


--
-- Name: role Role_name_key; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.role
    ADD CONSTRAINT "Role_name_key" UNIQUE (name);


--
-- Name: role Role_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.role
    ADD CONSTRAINT "Role_pkey" PRIMARY KEY (id);


--
-- Name: role Role_value_key; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.role
    ADD CONSTRAINT "Role_value_key" UNIQUE (value);


--
-- Name: submission_event SubmissionEvent_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.submission_event
    ADD CONSTRAINT "SubmissionEvent_pkey" PRIMARY KEY (id);


--
-- Name: submission Submission_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.submission
    ADD CONSTRAINT "Submission_pkey" PRIMARY KEY (id);


--
-- Name: user_event UserEvent_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.user_event
    ADD CONSTRAINT "UserEvent_pkey" PRIMARY KEY (id);


--
-- Name: user User_email_key; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public."user"
    ADD CONSTRAINT "User_email_key" UNIQUE (email);


--
-- Name: user User_login_key; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public."user"
    ADD CONSTRAINT "User_login_key" UNIQUE (login);


--
-- Name: user User_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public."user"
    ADD CONSTRAINT "User_pkey" PRIMARY KEY (id);


    
ALTER TABLE ONLY public.api_token
    ADD CONSTRAINT "Api_token_token_key" UNIQUE (token);

ALTER TABLE ONLY public.api_token
    ADD CONSTRAINT "Api_token_user_login_key" UNIQUE (user_login);

ALTER TABLE ONLY public.api_token
    ADD CONSTRAINT "Api_token_pkey" PRIMARY KEY (id);        
    

--
-- Name: package_event by_user; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.package_event
    ADD CONSTRAINT by_user FOREIGN KEY (changed_by) REFERENCES public."user"(id);


--
-- Name: package_maintainer_event by_user; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.package_maintainer_event
    ADD CONSTRAINT by_user FOREIGN KEY (changed_by) REFERENCES public."user"(id);


--
-- Name: repository_event by_user; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.repository_event
    ADD CONSTRAINT by_user FOREIGN KEY (changed_by) REFERENCES public."user"(id);


--
-- Name: repository_maintainer_event by_user; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.repository_maintainer_event
    ADD CONSTRAINT by_user FOREIGN KEY (changed_by) REFERENCES public."user"(id);


--
-- Name: submission_event by_user; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.submission_event
    ADD CONSTRAINT by_user FOREIGN KEY (changed_by) REFERENCES public."user"(id);


--
-- Name: user_event by_user; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.user_event
    ADD CONSTRAINT by_user FOREIGN KEY (changed_by) REFERENCES public."user"(id);


--
-- Name: package_event doing_event; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.package_event
    ADD CONSTRAINT doing_event FOREIGN KEY (event_id) REFERENCES public.event(id);


--
-- Name: package_maintainer_event doing_event; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.package_maintainer_event
    ADD CONSTRAINT doing_event FOREIGN KEY (event_id) REFERENCES public.event(id);


--
-- Name: repository_event doing_event; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.repository_event
    ADD CONSTRAINT doing_event FOREIGN KEY (event_id) REFERENCES public.event(id);


--
-- Name: repository_maintainer_event doing_event; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.repository_maintainer_event
    ADD CONSTRAINT doing_event FOREIGN KEY (event_id) REFERENCES public.event(id);


--
-- Name: submission_event doing_event; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.submission_event
    ADD CONSTRAINT doing_event FOREIGN KEY (event_id) REFERENCES public.event(id);


--
-- Name: user_event doing_event; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.user_event
    ADD CONSTRAINT doing_event FOREIGN KEY (event_id) REFERENCES public.event(id);


--
-- Name: submission for_package; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.submission
    ADD CONSTRAINT for_package FOREIGN KEY (package_id) REFERENCES public.package(id);


--
-- Name: package for_repository; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.package
    ADD CONSTRAINT for_repository FOREIGN KEY (repository_id) REFERENCES public.repository(id);


--
-- Name: submission from_user; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.submission
    ADD CONSTRAINT from_user FOREIGN KEY (submitter_id) REFERENCES public."user"(id);


--
-- Name: user has_role; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public."user"
    ADD CONSTRAINT has_role FOREIGN KEY (role_id) REFERENCES public.role(id);


--
-- Name: package is_maintainer_of; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.package
    ADD CONSTRAINT is_maintainer_of FOREIGN KEY (maintainer_id) REFERENCES public."user"(id);


--
-- Name: package_maintainer is_package_maintainer; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.package_maintainer
    ADD CONSTRAINT is_package_maintainer FOREIGN KEY (user_id) REFERENCES public."user"(id);


--
-- Name: package_maintainer is_package_maintainer_of; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.package_maintainer
    ADD CONSTRAINT is_package_maintainer_of FOREIGN KEY (repository_id) REFERENCES public.repository(id);


--
-- Name: repository_maintainer is_repository_maintainer; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.repository_maintainer
    ADD CONSTRAINT is_repository_maintainer FOREIGN KEY (user_id) REFERENCES public."user"(id);


--
-- Name: package_event of_package; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.package_event
    ADD CONSTRAINT of_package FOREIGN KEY (package_id) REFERENCES public.package(id);


--
-- Name: package_maintainer_event of_package_maintainer; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.package_maintainer_event
    ADD CONSTRAINT of_package_maintainer FOREIGN KEY (package_maintainer_id) REFERENCES public.package_maintainer(id);


--
-- Name: repository_maintainer of_repository; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.repository_maintainer
    ADD CONSTRAINT of_repository FOREIGN KEY (repository_id) REFERENCES public.repository(id);


--
-- Name: repository_event of_repository; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.repository_event
    ADD CONSTRAINT of_repository FOREIGN KEY (repository_id) REFERENCES public.repository(id);


--
-- Name: repository_maintainer_event of_repository_maintainer; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.repository_maintainer_event
    ADD CONSTRAINT of_repository_maintainer FOREIGN KEY (repository_maintainer_id) REFERENCES public.repository_maintainer(id);


--
-- Name: submission_event of_submission; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.submission_event
    ADD CONSTRAINT of_submission FOREIGN KEY (submission_id) REFERENCES public.submission(id);


--
-- Name: user_event of_user; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.user_event
    ADD CONSTRAINT of_user FOREIGN KEY (user_id) REFERENCES public."user"(id);


--
-- PostgreSQL database dump complete
--

