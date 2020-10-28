/**
 * R Depot
 *
 * Copyright (C) 2012-2020 Open Analytics NV
 *
 * ===========================================================================
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the Apache License as published by
 * The Apache Software Foundation, either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * Apache License for more details.
 *
 * You should have received a copy of the Apache License
 * along with this program.  If not, see <http://www.apache.org/licenses/>
 */
--
-- PostgreSQL database dump
--

-- Dumped from database version 9.6.2
-- Dumped by pg_dump version 9.6.2

-- Started on 2017-04-19 13:06:33 CEST

-- SET statement_timeout = 0;
-- SET lock_timeout = 0;
-- SET idle_in_transaction_session_timeout = 0;
-- SET client_encoding = 'SQL_ASCII';
-- SET standard_conforming_strings = on;
-- SET check_function_bodies = false;
-- SET client_min_messages = warning;
-- SET row_security = off;

-- DROP DATABASE rdepot;
--
-- TOC entry 2388 (class 1262 OID 24716)
-- Name: rdepot; Type: DATABASE; Schema: -; Owner: -
--

-- CREATE DATABASE rdepot WITH ENCODING = 'UTF8' LC_COLLATE = 'en_US.UTF-8' LC_CTYPE = 'en_US.UTF-8';
-- CREATE USER rdepot;
-- GRANT ALL PRIVILEGES ON DATABASE rdepot TO rdepot;

\connect postgres

SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET client_encoding = 'SQL_ASCII';
SET standard_conforming_strings = on;
SET check_function_bodies = false;
SET client_min_messages = warning;
SET row_security = off;

--
-- TOC entry 1 (class 3079 OID 12431)
-- Name: plpgsql; Type: EXTENSION; Schema: -; Owner: -
--

CREATE EXTENSION IF NOT EXISTS plpgsql WITH SCHEMA pg_catalog;


--
-- TOC entry 2390 (class 0 OID 0)
-- Dependencies: 1
-- Name: EXTENSION plpgsql; Type: COMMENT; Schema: -; Owner: -
--

COMMENT ON EXTENSION plpgsql IS 'PL/pgSQL procedural language';


SET search_path = public, pg_catalog;

--
-- TOC entry 185 (class 1259 OID 24717)
-- Name: Event_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE "Event_id_seq"
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- TOC entry 186 (class 1259 OID 24719)
-- Name: PackageEvent_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE "PackageEvent_id_seq"
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- TOC entry 187 (class 1259 OID 24721)
-- Name: PackageMaintainerEvent_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE "PackageMaintainerEvent_id_seq"
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


SET default_tablespace = '';

SET default_with_oids = false;

--
-- TOC entry 188 (class 1259 OID 24723)
-- Name: package_maintainer; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE package_maintainer (
    id integer NOT NULL,
    user_id integer NOT NULL,
    package text NOT NULL,
    repository_id integer NOT NULL,
    deleted boolean DEFAULT false NOT NULL
);


--
-- TOC entry 189 (class 1259 OID 24727)
-- Name: PackageMaintainer_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE "PackageMaintainer_id_seq"
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- TOC entry 2391 (class 0 OID 0)
-- Dependencies: 189
-- Name: PackageMaintainer_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE "PackageMaintainer_id_seq" OWNED BY package_maintainer.id;


--
-- TOC entry 190 (class 1259 OID 24729)
-- Name: package; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE package (
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


--
-- TOC entry 191 (class 1259 OID 24737)
-- Name: Package_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE "Package_id_seq"
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- TOC entry 2392 (class 0 OID 0)
-- Dependencies: 191
-- Name: Package_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE "Package_id_seq" OWNED BY package.id;


--
-- TOC entry 192 (class 1259 OID 24739)
-- Name: RepositoryEvent_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE "RepositoryEvent_id_seq"
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- TOC entry 193 (class 1259 OID 24741)
-- Name: RepositoryMaintainerEvent_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE "RepositoryMaintainerEvent_id_seq"
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- TOC entry 194 (class 1259 OID 24743)
-- Name: repository_maintainer; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE repository_maintainer (
    id integer NOT NULL,
    user_id integer NOT NULL,
    repository_id integer NOT NULL,
    deleted boolean DEFAULT false NOT NULL
--    repository_name character varying(255) NOT NULL,
--    user_name character varying(255) NOT NULL
);


--
-- TOC entry 195 (class 1259 OID 24747)
-- Name: RepositoryMaintainer_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE "RepositoryMaintainer_id_seq"
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- TOC entry 2393 (class 0 OID 0)
-- Dependencies: 195
-- Name: RepositoryMaintainer_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE "RepositoryMaintainer_id_seq" OWNED BY repository_maintainer.id;


--
-- TOC entry 196 (class 1259 OID 24749)
-- Name: repository; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE repository (
    version integer DEFAULT 0 NOT NULL,
    id integer NOT NULL,
    publication_uri text NOT NULL,
    name text NOT NULL,
    server_address text NOT NULL,
    published boolean DEFAULT false NOT NULL,
    deleted boolean DEFAULT false NOT NULL
);


--
-- TOC entry 197 (class 1259 OID 24758)
-- Name: Repository_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE "Repository_id_seq"
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- TOC entry 2394 (class 0 OID 0)
-- Dependencies: 197
-- Name: Repository_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE "Repository_id_seq" OWNED BY repository.id;


--
-- TOC entry 198 (class 1259 OID 24760)
-- Name: role; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE role (
    id integer NOT NULL,
    value integer NOT NULL,
    name text NOT NULL,
    description text NOT NULL
);


--
-- TOC entry 199 (class 1259 OID 24766)
-- Name: Role_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE "Role_id_seq"
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- TOC entry 2395 (class 0 OID 0)
-- Dependencies: 199
-- Name: Role_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE "Role_id_seq" OWNED BY role.id;


--
-- TOC entry 200 (class 1259 OID 24768)
-- Name: SubmissionEvent_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE "SubmissionEvent_id_seq"
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- TOC entry 201 (class 1259 OID 24770)
-- Name: submission; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE submission (
    id integer NOT NULL,
    submitter_id integer NOT NULL,
    package_id integer NOT NULL,
    changes text,
    accepted boolean DEFAULT false NOT NULL,
    deleted boolean DEFAULT false NOT NULL
);


--
-- TOC entry 202 (class 1259 OID 24775)
-- Name: Submission_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE "Submission_id_seq"
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- TOC entry 2396 (class 0 OID 0)
-- Dependencies: 202
-- Name: Submission_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE "Submission_id_seq" OWNED BY submission.id;


--
-- TOC entry 203 (class 1259 OID 24777)
-- Name: UserEvent_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE "UserEvent_id_seq"
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- TOC entry 204 (class 1259 OID 24779)
-- Name: user; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE "user" (
    id integer NOT NULL,
    role_id integer DEFAULT 4 NOT NULL,
    name text NOT NULL,
    email text NOT NULL,
    login text NOT NULL,
    active boolean NOT NULL,
    last_logged_in_on date,
    deleted boolean DEFAULT false NOT NULL
);


--
-- TOC entry 205 (class 1259 OID 24787)
-- Name: User_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE "User_id_seq"
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- TOC entry 2397 (class 0 OID 0)
-- Dependencies: 205
-- Name: User_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE "User_id_seq" OWNED BY "user".id;

---------------------------------------------------------------
    
CREATE TABLE api_token (
	id integer NOT NULL,
	token character varying(255) NOT NULL,
	user_login character varying(255) NOT NULL
);

ALTER TABLE api_token OWNER TO postgres;

CREATE SEQUENCE "Api_token_id_seq"
	START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;   
    
ALTER SEQUENCE "Api_token_id_seq" OWNED BY api_token.id;  
 
--------------------------------------------------------------- 


--
-- TOC entry 206 (class 1259 OID 24789)
-- Name: event; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE event (
    id integer DEFAULT nextval('"Event_id_seq"'::regclass) NOT NULL,
    value character varying(255) NOT NULL
);


--
-- TOC entry 207 (class 1259 OID 24793)
-- Name: package_event; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE package_event (
    id integer DEFAULT nextval('"PackageEvent_id_seq"'::regclass) NOT NULL,
    date date DEFAULT ('now'::text)::date NOT NULL,
    package_id integer NOT NULL,
    event_id integer NOT NULL,
    changed_variable text NOT NULL,
    value_before text NOT NULL,
    value_after text NOT NULL,
    changed_by integer NOT NULL,
    "time" time with time zone DEFAULT now()
);


--
-- TOC entry 208 (class 1259 OID 24801)
-- Name: package_maintainer_event; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE package_maintainer_event (
    id integer DEFAULT nextval('"PackageMaintainerEvent_id_seq"'::regclass) NOT NULL,
    date date DEFAULT ('now'::text)::date NOT NULL,
    package_maintainer_id integer NOT NULL,
    event_id integer NOT NULL,
    changed_variable text NOT NULL,
    value_before text NOT NULL,
    value_after text NOT NULL,
    changed_by integer NOT NULL,
    "time" time with time zone DEFAULT now()
);


--
-- TOC entry 209 (class 1259 OID 24809)
-- Name: repository_event; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE repository_event (
    id integer DEFAULT nextval('"RepositoryEvent_id_seq"'::regclass) NOT NULL,
    date date DEFAULT ('now'::text)::date NOT NULL,
    repository_id integer NOT NULL,
    event_id integer NOT NULL,
    changed_variable text NOT NULL,
    value_before text NOT NULL,
    value_after text NOT NULL,
    changed_by integer NOT NULL,
    "time" time with time zone DEFAULT now()
);


--
-- TOC entry 210 (class 1259 OID 24817)
-- Name: repository_maintainer_event; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE repository_maintainer_event (
    id integer DEFAULT nextval('"RepositoryMaintainerEvent_id_seq"'::regclass) NOT NULL,
    date date DEFAULT ('now'::text)::date NOT NULL,
    repository_maintainer_id integer NOT NULL,
    event_id integer NOT NULL,
    changed_variable text NOT NULL,
    value_before text NOT NULL,
    value_after text NOT NULL,
    changed_by integer NOT NULL,
    "time" time with time zone DEFAULT now()
);


--
-- TOC entry 211 (class 1259 OID 24825)
-- Name: submission_event; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE submission_event (
    id integer DEFAULT nextval('"SubmissionEvent_id_seq"'::regclass) NOT NULL,
    date date DEFAULT ('now'::text)::date NOT NULL,
    submission_id integer NOT NULL,
    event_id integer NOT NULL,
    changed_variable text NOT NULL,
    value_before text NOT NULL,
    value_after text NOT NULL,
    changed_by integer NOT NULL,
    "time" time with time zone DEFAULT now()
);


--
-- TOC entry 212 (class 1259 OID 24833)
-- Name: user_event; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE user_event (
    id integer DEFAULT nextval('"UserEvent_id_seq"'::regclass) NOT NULL,
    date date DEFAULT ('now'::text)::date NOT NULL,
    user_id integer NOT NULL,
    event_id integer NOT NULL,
    changed_variable text NOT NULL,
    value_before text NOT NULL,
    value_after text NOT NULL,
    changed_by integer NOT NULL,
    "time" time with time zone DEFAULT now()
);


--
-- TOC entry 2135 (class 2604 OID 24841)
-- Name: package id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY package ALTER COLUMN id SET DEFAULT nextval('"Package_id_seq"'::regclass);


--
-- TOC entry 2132 (class 2604 OID 24842)
-- Name: package_maintainer id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY package_maintainer ALTER COLUMN id SET DEFAULT nextval('"PackageMaintainer_id_seq"'::regclass);


--
-- TOC entry 2141 (class 2604 OID 24843)
-- Name: repository id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY repository ALTER COLUMN id SET DEFAULT nextval('"Repository_id_seq"'::regclass);


--
-- TOC entry 2137 (class 2604 OID 24844)
-- Name: repository_maintainer id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY repository_maintainer ALTER COLUMN id SET DEFAULT nextval('"RepositoryMaintainer_id_seq"'::regclass);


--
-- TOC entry 2142 (class 2604 OID 24845)
-- Name: role id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY role ALTER COLUMN id SET DEFAULT nextval('"Role_id_seq"'::regclass);


--
-- TOC entry 2145 (class 2604 OID 24846)
-- Name: submission id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY submission ALTER COLUMN id SET DEFAULT nextval('"Submission_id_seq"'::regclass);


--
-- TOC entry 2148 (class 2604 OID 24847)
-- Name: user id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY "user" ALTER COLUMN id SET DEFAULT nextval('"User_id_seq"'::regclass);


--
-- TOC entry 214* (class 2604 OID 24847)
-- Name: api_token id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY api_token ALTER COLUMN id SET DEFAULT nextval('"Api_token_id_seq"'::regclass);

--
-- TOC entry 2398 (class 0 OID 0)
-- Dependencies: 185
-- Name: Event_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('"Event_id_seq"', 3, true);


--
-- TOC entry 2399 (class 0 OID 0)
-- Dependencies: 186
-- Name: PackageEvent_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('"PackageEvent_id_seq"', 3, true);


--
-- TOC entry 2400 (class 0 OID 0)
-- Dependencies: 187
-- Name: PackageMaintainerEvent_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('"PackageMaintainerEvent_id_seq"', 1, false);


--
-- TOC entry 2401 (class 0 OID 0)
-- Dependencies: 189
-- Name: PackageMaintainer_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('"PackageMaintainer_id_seq"', 1, false);


--
-- TOC entry 2402 (class 0 OID 0)
-- Dependencies: 191
-- Name: Package_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('"Package_id_seq"', 3, true);


--
-- TOC entry 2403 (class 0 OID 0)
-- Dependencies: 192
-- Name: RepositoryEvent_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('"RepositoryEvent_id_seq"', 10, true);


--
-- TOC entry 2404 (class 0 OID 0)
-- Dependencies: 193
-- Name: RepositoryMaintainerEvent_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('"RepositoryMaintainerEvent_id_seq"', 1, false);


--
-- TOC entry 2405 (class 0 OID 0)
-- Dependencies: 195
-- Name: RepositoryMaintainer_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('"RepositoryMaintainer_id_seq"', 1, false);


--
-- TOC entry 2406 (class 0 OID 0)
-- Dependencies: 197
-- Name: Repository_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('"Repository_id_seq"', 1, true);


--
-- TOC entry 2407 (class 0 OID 0)
-- Dependencies: 199
-- Name: Role_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('"Role_id_seq"', 4, true);


--
-- TOC entry 2408 (class 0 OID 0)
-- Dependencies: 200
-- Name: SubmissionEvent_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('"SubmissionEvent_id_seq"', 3, true);


--
-- TOC entry 2409 (class 0 OID 0)
-- Dependencies: 202
-- Name: Submission_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('"Submission_id_seq"', 3, true);


--
-- TOC entry 2410 (class 0 OID 0)
-- Dependencies: 203
-- Name: UserEvent_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('"UserEvent_id_seq"', 8, true);


--
-- TOC entry 2411 (class 0 OID 0)
-- Dependencies: 205
-- Name: User_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('"User_id_seq"', 8, true);


--
-- Name: Api_token_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('"Api_token_id_seq"', 2, true);

--
-- TOC entry 2377 (class 0 OID 24789)
-- Dependencies: 206
-- Data for Name: event; Type: TABLE DATA; Schema: public; Owner: -
--

INSERT INTO event VALUES (1, 'create');
INSERT INTO event VALUES (2, 'delete');
INSERT INTO event VALUES (3, 'update');


--
-- TOC entry 2369 (class 0 OID 24760)
-- Dependencies: 198
-- Data for Name: role; Type: TABLE DATA; Schema: public; Owner: -
--

INSERT INTO role VALUES (2, 1, 'packagemaintainer', 'Package Maintainer');
INSERT INTO role VALUES (3, 2, 'repositorymaintainer', 'Repository Maintainer');
INSERT INTO role VALUES (4, 3, 'admin', 'Administrator');
INSERT INTO role VALUES (1, 0, 'user', 'User');


--
-- TOC entry 2197 (class 2606 OID 24849)
-- Name: event Event_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY event
    ADD CONSTRAINT "Event_pkey" PRIMARY KEY (id);


--
-- TOC entry 2199 (class 2606 OID 24851)
-- Name: event Event_value_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY event
    ADD CONSTRAINT "Event_value_key" UNIQUE (value);


--
-- TOC entry 2201 (class 2606 OID 24853)
-- Name: package_event PackageEvent_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY package_event
    ADD CONSTRAINT "PackageEvent_pkey" PRIMARY KEY (id);


--
-- TOC entry 2203 (class 2606 OID 24855)
-- Name: package_maintainer_event PackageMaintainerEvent_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY package_maintainer_event
    ADD CONSTRAINT "PackageMaintainerEvent_pkey" PRIMARY KEY (id);


--
-- TOC entry 2169 (class 2606 OID 24857)
-- Name: package_maintainer PackageMaintainer_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY package_maintainer
    ADD CONSTRAINT "PackageMaintainer_pkey" PRIMARY KEY (id);


--
-- TOC entry 2171 (class 2606 OID 24859)
-- Name: package Package_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY package
    ADD CONSTRAINT "Package_pkey" PRIMARY KEY (id);


--
-- TOC entry 2205 (class 2606 OID 24861)
-- Name: repository_event RepositoryEvent_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY repository_event
    ADD CONSTRAINT "RepositoryEvent_pkey" PRIMARY KEY (id);


--
-- TOC entry 2207 (class 2606 OID 24863)
-- Name: repository_maintainer_event RepositoryMaintainerEvent_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY repository_maintainer_event
    ADD CONSTRAINT "RepositoryMaintainerEvent_pkey" PRIMARY KEY (id);


--
-- TOC entry 2173 (class 2606 OID 24865)
-- Name: repository_maintainer RepositoryMaintainer_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY repository_maintainer
    ADD CONSTRAINT "RepositoryMaintainer_pkey" PRIMARY KEY (id);


--
-- TOC entry 2175 (class 2606 OID 24867)
-- Name: repository Repository_name_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY repository
    ADD CONSTRAINT "Repository_name_key" UNIQUE (name);


--
-- TOC entry 2177 (class 2606 OID 24869)
-- Name: repository Repository_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY repository
    ADD CONSTRAINT "Repository_pkey" PRIMARY KEY (id);


--
-- TOC entry 2179 (class 2606 OID 24871)
-- Name: repository Repository_publication_uri_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY repository
    ADD CONSTRAINT "Repository_publication_uri_key" UNIQUE (publication_uri);


--
-- TOC entry 2181 (class 2606 OID 24873)
-- Name: role Role_description_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY role
    ADD CONSTRAINT "Role_description_key" UNIQUE (description);


--
-- TOC entry 2183 (class 2606 OID 24875)
-- Name: role Role_name_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY role
    ADD CONSTRAINT "Role_name_key" UNIQUE (name);


--
-- TOC entry 2185 (class 2606 OID 24877)
-- Name: role Role_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY role
    ADD CONSTRAINT "Role_pkey" PRIMARY KEY (id);


--
-- TOC entry 2187 (class 2606 OID 24879)
-- Name: role Role_value_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY role
    ADD CONSTRAINT "Role_value_key" UNIQUE (value);


--
-- TOC entry 2209 (class 2606 OID 24881)
-- Name: submission_event SubmissionEvent_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY submission_event
    ADD CONSTRAINT "SubmissionEvent_pkey" PRIMARY KEY (id);


--
-- TOC entry 2189 (class 2606 OID 24883)
-- Name: submission Submission_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY submission
    ADD CONSTRAINT "Submission_pkey" PRIMARY KEY (id);


--
-- TOC entry 2211 (class 2606 OID 24885)
-- Name: user_event UserEvent_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY user_event
    ADD CONSTRAINT "UserEvent_pkey" PRIMARY KEY (id);


--
-- TOC entry 2191 (class 2606 OID 24887)
-- Name: user User_email_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY "user"
    ADD CONSTRAINT "User_email_key" UNIQUE (email);


--
-- TOC entry 2193 (class 2606 OID 24889)
-- Name: user User_login_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY "user"
    ADD CONSTRAINT "User_login_key" UNIQUE (login);


--
-- TOC entry 2195 (class 2606 OID 24891)
-- Name: user User_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY "user"
    ADD CONSTRAINT "User_pkey" PRIMARY KEY (id);


ALTER TABLE ONLY api_token
    ADD CONSTRAINT "Api_token_token_key" UNIQUE (token);

ALTER TABLE ONLY api_token
    ADD CONSTRAINT "Api_token_user_login_key" UNIQUE (user_login);

ALTER TABLE ONLY api_token
    ADD CONSTRAINT "Api_token_pkey" PRIMARY KEY (id);  



--
-- TOC entry 2221 (class 2606 OID 24892)
-- Name: package_event by_user; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY package_event
    ADD CONSTRAINT by_user FOREIGN KEY (changed_by) REFERENCES "user"(id);


--
-- TOC entry 2224 (class 2606 OID 24897)
-- Name: package_maintainer_event by_user; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY package_maintainer_event
    ADD CONSTRAINT by_user FOREIGN KEY (changed_by) REFERENCES "user"(id);


--
-- TOC entry 2227 (class 2606 OID 24902)
-- Name: repository_event by_user; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY repository_event
    ADD CONSTRAINT by_user FOREIGN KEY (changed_by) REFERENCES "user"(id);


--
-- TOC entry 2230 (class 2606 OID 24907)
-- Name: repository_maintainer_event by_user; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY repository_maintainer_event
    ADD CONSTRAINT by_user FOREIGN KEY (changed_by) REFERENCES "user"(id);


--
-- TOC entry 2233 (class 2606 OID 24912)
-- Name: submission_event by_user; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY submission_event
    ADD CONSTRAINT by_user FOREIGN KEY (changed_by) REFERENCES "user"(id);


--
-- TOC entry 2236 (class 2606 OID 24917)
-- Name: user_event by_user; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY user_event
    ADD CONSTRAINT by_user FOREIGN KEY (changed_by) REFERENCES "user"(id);


--
-- TOC entry 2222 (class 2606 OID 24922)
-- Name: package_event doing_event; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY package_event
    ADD CONSTRAINT doing_event FOREIGN KEY (event_id) REFERENCES event(id);


--
-- TOC entry 2225 (class 2606 OID 24927)
-- Name: package_maintainer_event doing_event; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY package_maintainer_event
    ADD CONSTRAINT doing_event FOREIGN KEY (event_id) REFERENCES event(id);


--
-- TOC entry 2228 (class 2606 OID 24932)
-- Name: repository_event doing_event; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY repository_event
    ADD CONSTRAINT doing_event FOREIGN KEY (event_id) REFERENCES event(id);


--
-- TOC entry 2231 (class 2606 OID 24937)
-- Name: repository_maintainer_event doing_event; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY repository_maintainer_event
    ADD CONSTRAINT doing_event FOREIGN KEY (event_id) REFERENCES event(id);


--
-- TOC entry 2234 (class 2606 OID 24942)
-- Name: submission_event doing_event; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY submission_event
    ADD CONSTRAINT doing_event FOREIGN KEY (event_id) REFERENCES event(id);


--
-- TOC entry 2237 (class 2606 OID 24947)
-- Name: user_event doing_event; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY user_event
    ADD CONSTRAINT doing_event FOREIGN KEY (event_id) REFERENCES event(id);


--
-- TOC entry 2218 (class 2606 OID 24952)
-- Name: submission for_package; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY submission
    ADD CONSTRAINT for_package FOREIGN KEY (package_id) REFERENCES package(id);


--
-- TOC entry 2214 (class 2606 OID 24957)
-- Name: package for_repository; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY package
    ADD CONSTRAINT for_repository FOREIGN KEY (repository_id) REFERENCES repository(id);


--
-- TOC entry 2219 (class 2606 OID 24962)
-- Name: submission from_user; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY submission
    ADD CONSTRAINT from_user FOREIGN KEY (submitter_id) REFERENCES "user"(id);


--
-- TOC entry 2220 (class 2606 OID 24967)
-- Name: user has_role; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY "user"
    ADD CONSTRAINT has_role FOREIGN KEY (role_id) REFERENCES role(id);


--
-- TOC entry 2215 (class 2606 OID 24972)
-- Name: package is_maintainer_of; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY package
    ADD CONSTRAINT is_maintainer_of FOREIGN KEY (maintainer_id) REFERENCES "user"(id);


--
-- TOC entry 2212 (class 2606 OID 24977)
-- Name: package_maintainer is_package_maintainer; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY package_maintainer
    ADD CONSTRAINT is_package_maintainer FOREIGN KEY (user_id) REFERENCES "user"(id);


--
-- TOC entry 2213 (class 2606 OID 24982)
-- Name: package_maintainer is_package_maintainer_of; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY package_maintainer
    ADD CONSTRAINT is_package_maintainer_of FOREIGN KEY (repository_id) REFERENCES repository(id);


--
-- TOC entry 2216 (class 2606 OID 24987)
-- Name: repository_maintainer is_repository_maintainer; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY repository_maintainer
    ADD CONSTRAINT is_repository_maintainer FOREIGN KEY (user_id) REFERENCES "user"(id);


--
-- TOC entry 2223 (class 2606 OID 24992)
-- Name: package_event of_package; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY package_event
    ADD CONSTRAINT of_package FOREIGN KEY (package_id) REFERENCES package(id);


--
-- TOC entry 2226 (class 2606 OID 24997)
-- Name: package_maintainer_event of_package_maintainer; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY package_maintainer_event
    ADD CONSTRAINT of_package_maintainer FOREIGN KEY (package_maintainer_id) REFERENCES package_maintainer(id);


--
-- TOC entry 2217 (class 2606 OID 25002)
-- Name: repository_maintainer of_repository; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY repository_maintainer
    ADD CONSTRAINT of_repository FOREIGN KEY (repository_id) REFERENCES repository(id);


--
-- TOC entry 2229 (class 2606 OID 25007)
-- Name: repository_event of_repository; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY repository_event
    ADD CONSTRAINT of_repository FOREIGN KEY (repository_id) REFERENCES repository(id);


--
-- TOC entry 2232 (class 2606 OID 25012)
-- Name: repository_maintainer_event of_repository_maintainer; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY repository_maintainer_event
    ADD CONSTRAINT of_repository_maintainer FOREIGN KEY (repository_maintainer_id) REFERENCES repository_maintainer(id);


--
-- TOC entry 2235 (class 2606 OID 25017)
-- Name: submission_event of_submission; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY submission_event
    ADD CONSTRAINT of_submission FOREIGN KEY (submission_id) REFERENCES submission(id);


--
-- TOC entry 2238 (class 2606 OID 25022)
-- Name: user_event of_user; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY user_event
    ADD CONSTRAINT of_user FOREIGN KEY (user_id) REFERENCES "user"(id);

-- einstein = admin
-- tesla = repository maintainer (but not linked to a repository -> admin has to do that manually)
-- galieleo = package maintainer (but not linked to a package -> repository maintainer or admin has to do that manually)
-- newton = normal user

INSERT INTO "user" VALUES (4, 4, 'Albert Einstein', 'einstein@ldap.forumsys.com', 'einstein', true, NULL, false);
INSERT INTO "user" VALUES (5, 3, 'Nikola Tesla', 'tesla@ldap.forumsys.com', 'tesla', true, NULL, false);
INSERT INTO "user" VALUES (6, 2, 'Galileo Galilei', 'galieleo@ldap.forumsys.com', 'galieleo', true, NULL, false);
INSERT INTO "user" VALUES (7, 1, 'Isaac Newton', 'newton@ldap.forumsys.com', 'newton', true, NULL, false);
INSERT INTO "user" VALUES (8, 4, 'Local Admin User', 'admin@localhost', 'admin', true, NULL, false);

INSERT INTO user_event VALUES (1, now(), 4, 1, 'created', '', '', 4, now());
INSERT INTO user_event VALUES (2, now(), 5, 1, 'created', '', '', 5, now());
INSERT INTO user_event VALUES (3, now(), 6, 1, 'created', '', '', 6, now());
INSERT INTO user_event VALUES (4, now(), 7, 1, 'created', '', '', 7, now());
INSERT INTO user_event VALUES (5, now(), 8, 1, 'created', '', '', 8, now());

-- Completed on 2017-04-19 13:06:33 CEST

--
-- PostgreSQL database dump complete
--
