--
-- PostgreSQL database dump
-- -- Dumped from database version 16.4
-- Dumped by pg_dump version 16.4 SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SELECT pg_catalog.set_config('search_path', '', false);
SET check_function_bodies = false;
SET xmloption = content;
SET client_min_messages = warning;
SET row_security = off; SET default_tablespace = ''; SET default_table_access_method = heap; --
-- Name: api_token; Type: TABLE; Schema: public; Owner: postgres
-- CREATE TABLE public.api_token ( id integer NOT NULL, token character varying(255) NOT NULL, user_login character varying(255) NOT NULL
); ALTER TABLE public.api_token OWNER TO postgres; --
-- Name: Api_token_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
-- CREATE SEQUENCE public."Api_token_id_seq" START WITH 1 INCREMENT BY 1 NO MINVALUE NO MAXVALUE CACHE 1; ALTER SEQUENCE public."Api_token_id_seq" OWNER TO postgres; --
-- Name: Api_token_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
-- ALTER SEQUENCE public."Api_token_id_seq" OWNED BY public.api_token.id; --
-- Name: Event_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
-- CREATE SEQUENCE public."Event_id_seq" START WITH 1 INCREMENT BY 1 NO MINVALUE NO MAXVALUE CACHE 1; ALTER SEQUENCE public."Event_id_seq" OWNER TO postgres; --
-- Name: PackageEvent_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
-- CREATE SEQUENCE public."PackageEvent_id_seq" START WITH 1 INCREMENT BY 1 NO MINVALUE NO MAXVALUE CACHE 1; ALTER SEQUENCE public."PackageEvent_id_seq" OWNER TO postgres; --
-- Name: PackageMaintainerEvent_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
-- CREATE SEQUENCE public."PackageMaintainerEvent_id_seq" START WITH 1 INCREMENT BY 1 NO MINVALUE NO MAXVALUE CACHE 1; ALTER SEQUENCE public."PackageMaintainerEvent_id_seq" OWNER TO postgres; --
-- Name: package_maintainer; Type: TABLE; Schema: public; Owner: postgres
-- CREATE TABLE public.package_maintainer ( id integer NOT NULL, user_id integer NOT NULL, package text NOT NULL, repository_id integer NOT NULL, deleted boolean DEFAULT false NOT NULL
); ALTER TABLE public.package_maintainer OWNER TO postgres; --
-- Name: PackageMaintainer_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
-- CREATE SEQUENCE public."PackageMaintainer_id_seq" START WITH 1 INCREMENT BY 1 NO MINVALUE NO MAXVALUE CACHE 1; ALTER SEQUENCE public."PackageMaintainer_id_seq" OWNER TO postgres; --
-- Name: PackageMaintainer_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
-- ALTER SEQUENCE public."PackageMaintainer_id_seq" OWNED BY public.package_maintainer.id; --
-- Name: package; Type: TABLE; Schema: public; Owner: postgres
-- CREATE TABLE public.package ( id integer NOT NULL, name text NOT NULL, version text NOT NULL, description text, author text, user_maintainer_id integer NOT NULL, repository_id integer NOT NULL, url text, source text NOT NULL, title text, active boolean DEFAULT false NOT NULL, deleted boolean DEFAULT false NOT NULL, resource_technology text NOT NULL, binary_package boolean DEFAULT false NOT NULL, description_content_type text DEFAULT ''::text NOT NULL
); ALTER TABLE public.package OWNER TO postgres; --
-- Name: Package_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
-- CREATE SEQUENCE public."Package_id_seq" START WITH 1 INCREMENT BY 1 NO MINVALUE NO MAXVALUE CACHE 1; ALTER SEQUENCE public."Package_id_seq" OWNER TO postgres; --
-- Name: Package_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
-- ALTER SEQUENCE public."Package_id_seq" OWNED BY public.package.id; --
-- Name: RepositoryEvent_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
-- CREATE SEQUENCE public."RepositoryEvent_id_seq" START WITH 1 INCREMENT BY 1 NO MINVALUE NO MAXVALUE CACHE 1; ALTER SEQUENCE public."RepositoryEvent_id_seq" OWNER TO postgres; --
-- Name: RepositoryMaintainerEvent_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
-- CREATE SEQUENCE public."RepositoryMaintainerEvent_id_seq" START WITH 1 INCREMENT BY 1 NO MINVALUE NO MAXVALUE CACHE 1; ALTER SEQUENCE public."RepositoryMaintainerEvent_id_seq" OWNER TO postgres; --
-- Name: repository_maintainer; Type: TABLE; Schema: public; Owner: postgres
-- CREATE TABLE public.repository_maintainer ( id integer NOT NULL, user_id integer NOT NULL, repository_id integer NOT NULL, deleted boolean DEFAULT false NOT NULL
); ALTER TABLE public.repository_maintainer OWNER TO postgres; --
-- Name: RepositoryMaintainer_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
-- CREATE SEQUENCE public."RepositoryMaintainer_id_seq" START WITH 1 INCREMENT BY 1 NO MINVALUE NO MAXVALUE CACHE 1; ALTER SEQUENCE public."RepositoryMaintainer_id_seq" OWNER TO postgres; --
-- Name: RepositoryMaintainer_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
-- ALTER SEQUENCE public."RepositoryMaintainer_id_seq" OWNED BY public.repository_maintainer.id; --
-- Name: repository; Type: TABLE; Schema: public; Owner: postgres
-- CREATE TABLE public.repository ( version integer DEFAULT 0 NOT NULL, id integer NOT NULL, publication_uri text NOT NULL, name text NOT NULL, server_address text NOT NULL, published boolean DEFAULT false NOT NULL, deleted boolean DEFAULT false NOT NULL, resource_technology text NOT NULL, last_publication_successful boolean DEFAULT false NOT NULL, last_modified_timestamp timestamp without time zone DEFAULT now() NOT NULL, last_publication_timestamp timestamp without time zone, requires_authentication boolean DEFAULT true NOT NULL
); ALTER TABLE public.repository OWNER TO postgres; --
-- Name: Repository_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
-- CREATE SEQUENCE public."Repository_id_seq" START WITH 1 INCREMENT BY 1 NO MINVALUE NO MAXVALUE CACHE 1; ALTER SEQUENCE public."Repository_id_seq" OWNER TO postgres; --
-- Name: Repository_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
-- ALTER SEQUENCE public."Repository_id_seq" OWNED BY public.repository.id; --
-- Name: role; Type: TABLE; Schema: public; Owner: postgres
-- CREATE TABLE public.role ( id integer NOT NULL, value integer NOT NULL, name text NOT NULL, description text NOT NULL, deleted boolean DEFAULT false
); ALTER TABLE public.role OWNER TO postgres; --
-- Name: Role_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
-- CREATE SEQUENCE public."Role_id_seq" START WITH 1 INCREMENT BY 1 NO MINVALUE NO MAXVALUE CACHE 1; ALTER SEQUENCE public."Role_id_seq" OWNER TO postgres; --
-- Name: Role_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
-- ALTER SEQUENCE public."Role_id_seq" OWNED BY public.role.id; --
-- Name: SubmissionEvent_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
-- CREATE SEQUENCE public."SubmissionEvent_id_seq" START WITH 1 INCREMENT BY 1 NO MINVALUE NO MAXVALUE CACHE 1; ALTER SEQUENCE public."SubmissionEvent_id_seq" OWNER TO postgres; --
-- Name: submission; Type: TABLE; Schema: public; Owner: postgres
-- CREATE TABLE public.submission ( id integer NOT NULL, submitter_id integer NOT NULL, package_id integer NOT NULL, changes text, deleted boolean DEFAULT false NOT NULL, state text NOT NULL, approver_id integer
); ALTER TABLE public.submission OWNER TO postgres; --
-- Name: Submission_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
-- CREATE SEQUENCE public."Submission_id_seq" START WITH 1 INCREMENT BY 1 NO MINVALUE NO MAXVALUE CACHE 1; ALTER SEQUENCE public."Submission_id_seq" OWNER TO postgres; --
-- Name: Submission_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
-- ALTER SEQUENCE public."Submission_id_seq" OWNED BY public.submission.id; --
-- Name: UserEvent_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
-- CREATE SEQUENCE public."UserEvent_id_seq" START WITH 1 INCREMENT BY 1 NO MINVALUE NO MAXVALUE CACHE 1; ALTER SEQUENCE public."UserEvent_id_seq" OWNER TO postgres; --
-- Name: user; Type: TABLE; Schema: public; Owner: postgres
-- CREATE TABLE public."user" ( id integer NOT NULL, role_id integer DEFAULT 4 NOT NULL, name text NOT NULL, email text NOT NULL, login text NOT NULL, active boolean NOT NULL, last_logged_in_on timestamp without time zone, deleted boolean DEFAULT false NOT NULL, created_on date DEFAULT now() NOT NULL
); ALTER TABLE public."user" OWNER TO postgres; --
-- Name: User_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
-- CREATE SEQUENCE public."User_id_seq" START WITH 1 INCREMENT BY 1 NO MINVALUE NO MAXVALUE CACHE 1; ALTER SEQUENCE public."User_id_seq" OWNER TO postgres; --
-- Name: User_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
-- ALTER SEQUENCE public."User_id_seq" OWNED BY public."user".id; --
-- Name: access_token; Type: TABLE; Schema: public; Owner: postgres
-- CREATE TABLE public.access_token ( id integer NOT NULL, user_id integer NOT NULL, name text NOT NULL, value text NOT NULL, creation_date date DEFAULT CURRENT_DATE NOT NULL, expiration_date date NOT NULL, active boolean DEFAULT true, deleted boolean DEFAULT false, last_used timestamp without time zone
); ALTER TABLE public.access_token OWNER TO postgres; --
-- Name: access_token_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
-- ALTER TABLE public.access_token ALTER COLUMN id ADD GENERATED BY DEFAULT AS IDENTITY ( SEQUENCE NAME public.access_token_id_seq START WITH 1 INCREMENT BY 1 NO MINVALUE NO MAXVALUE CACHE 1
); --
-- Name: changed_variable; Type: TABLE; Schema: public; Owner: postgres
-- CREATE TABLE public.changed_variable ( id integer NOT NULL, changed_variable character varying(32) NOT NULL, value_before text NOT NULL, value_after text NOT NULL, newsfeed_event_id integer NOT NULL, deleted boolean DEFAULT false NOT NULL
); ALTER TABLE public.changed_variable OWNER TO postgres; --
-- Name: changed_variable_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
-- ALTER TABLE public.changed_variable ALTER COLUMN id ADD GENERATED BY DEFAULT AS IDENTITY ( SEQUENCE NAME public.changed_variable_id_seq START WITH 1 INCREMENT BY 1 NO MINVALUE NO MAXVALUE CACHE 1
); --
-- Name: databasechangelog; Type: TABLE; Schema: public; Owner: postgres
-- CREATE TABLE public.databasechangelog ( id character varying(255) NOT NULL, author character varying(255) NOT NULL, filename character varying(255) NOT NULL, dateexecuted timestamp without time zone NOT NULL, orderexecuted integer NOT NULL, exectype character varying(10) NOT NULL, md5sum character varying(35), description character varying(255), comments character varying(255), tag character varying(255), liquibase character varying(20), contexts character varying(255), labels character varying(255), deployment_id character varying(10)
); ALTER TABLE public.databasechangelog OWNER TO postgres; --
-- Name: databasechangeloglock; Type: TABLE; Schema: public; Owner: postgres
-- CREATE TABLE public.databasechangeloglock ( id integer NOT NULL, locked boolean NOT NULL, lockgranted timestamp without time zone, lockedby character varying(255)
); ALTER TABLE public.databasechangeloglock OWNER TO postgres; --
-- Name: newsfeed_event; Type: TABLE; Schema: public; Owner: postgres
-- CREATE TABLE public.newsfeed_event ( id integer NOT NULL, newsfeed_event_type character varying(16) NOT NULL, author_id integer, related_packagemaintainer_id integer, related_repositorymaintainer_id integer, related_user_id integer, related_submission_id integer, related_repository_id integer, related_package_id integer, deleted boolean DEFAULT false NOT NULL, related_accesstoken_id integer, "time" timestamp without time zone DEFAULT now() NOT NULL
); ALTER TABLE public.newsfeed_event OWNER TO postgres; --
-- Name: newsfeed_event_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
-- ALTER TABLE public.newsfeed_event ALTER COLUMN id ADD GENERATED BY DEFAULT AS IDENTITY ( SEQUENCE NAME public.newsfeed_event_id_seq START WITH 1 INCREMENT BY 1 NO MINVALUE NO MAXVALUE CACHE 1
); --
-- Name: pythonpackage; Type: TABLE; Schema: public; Owner: postgres
-- CREATE TABLE public.pythonpackage ( id integer NOT NULL, license text NOT NULL, author_email text, classifier text, home_page text, keywords text, maintainer text, maintainer_email text, platform text, project_url text, provides_extra text, requires_dist text, requires_external text, requires_python text, summary text, hash text NOT NULL, normalized_name text NOT NULL
); ALTER TABLE public.pythonpackage OWNER TO postgres; --
-- Name: pythonrepository; Type: TABLE; Schema: public; Owner: postgres
-- CREATE TABLE public.pythonrepository ( id integer NOT NULL, hash_method text DEFAULT 'SHA256'::text
); ALTER TABLE public.pythonrepository OWNER TO postgres; --
-- Name: rpackage; Type: TABLE; Schema: public; Owner: postgres
-- CREATE TABLE public.rpackage ( id integer NOT NULL, depends text, imports text, suggests text, system_requirements text, license text, md5sum text, r_version text, architecture text, distribution text, built text, enhances text, linking_to text, priority text, needs_compilation boolean DEFAULT false NOT NULL, maintainer text
); ALTER TABLE public.rpackage OWNER TO postgres; --
-- Name: rrepository; Type: TABLE; Schema: public; Owner: postgres
-- CREATE TABLE public.rrepository ( id integer NOT NULL, redirect_to_source boolean DEFAULT false NOT NULL
); ALTER TABLE public.rrepository OWNER TO postgres; --
-- Name: user_settings; Type: TABLE; Schema: public; Owner: postgres
-- CREATE TABLE public.user_settings ( id integer NOT NULL, deleted boolean NOT NULL, language character varying(16) NOT NULL, theme character varying(16) NOT NULL, page_size integer NOT NULL, user_id integer NOT NULL
); ALTER TABLE public.user_settings OWNER TO postgres; --
-- Name: user_settings_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
-- ALTER TABLE public.user_settings ALTER COLUMN id ADD GENERATED BY DEFAULT AS IDENTITY ( SEQUENCE NAME public.user_settings_id_seq START WITH 1 INCREMENT BY 1 NO MINVALUE NO MAXVALUE CACHE 1
); --
-- Name: api_token id; Type: DEFAULT; Schema: public; Owner: postgres
-- ALTER TABLE ONLY public.api_token ALTER COLUMN id SET DEFAULT nextval('public."Api_token_id_seq"'::regclass); --
-- Name: package id; Type: DEFAULT; Schema: public; Owner: postgres
-- ALTER TABLE ONLY public.package ALTER COLUMN id SET DEFAULT nextval('public."Package_id_seq"'::regclass); --
-- Name: package_maintainer id; Type: DEFAULT; Schema: public; Owner: postgres
-- ALTER TABLE ONLY public.package_maintainer ALTER COLUMN id SET DEFAULT nextval('public."PackageMaintainer_id_seq"'::regclass); --
-- Name: repository id; Type: DEFAULT; Schema: public; Owner: postgres
-- ALTER TABLE ONLY public.repository ALTER COLUMN id SET DEFAULT nextval('public."Repository_id_seq"'::regclass); --
-- Name: repository_maintainer id; Type: DEFAULT; Schema: public; Owner: postgres
-- ALTER TABLE ONLY public.repository_maintainer ALTER COLUMN id SET DEFAULT nextval('public."RepositoryMaintainer_id_seq"'::regclass); --
-- Name: role id; Type: DEFAULT; Schema: public; Owner: postgres
-- ALTER TABLE ONLY public.role ALTER COLUMN id SET DEFAULT nextval('public."Role_id_seq"'::regclass); --
-- Name: submission id; Type: DEFAULT; Schema: public; Owner: postgres
-- ALTER TABLE ONLY public.submission ALTER COLUMN id SET DEFAULT nextval('public."Submission_id_seq"'::regclass); --
-- Name: user id; Type: DEFAULT; Schema: public; Owner: postgres
-- ALTER TABLE ONLY public."user" ALTER COLUMN id SET DEFAULT nextval('public."User_id_seq"'::regclass); --
-- Data for Name: access_token; Type: TABLE DATA; Schema: public; Owner: postgres
-- COPY public.access_token (id, user_id, name, value, creation_date, expiration_date, active, deleted, last_used) FROM stdin;
\. --
-- Data for Name: api_token; Type: TABLE DATA; Schema: public; Owner: postgres
-- COPY public.api_token (id, token, user_login) FROM stdin;
2 eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJlaW5zdGVpbiJ9.9VweA_kotRnnLn9giSE511MhWX4iDwtx85lidw_ZT5iTQ1aOB-3ytJNDB_Mrcop2H22MNhMjbpUW_sraHdvOlw einstein
3 eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJ0ZXNsYSJ9.FEQ3KqMvTj4LQAgQx23f6Y0Z7PzKHgcO1a1UodG5iwCrzXhk6tHCR6V0T16F1tWtMMF0a3AQIShczN__d6KsFA tesla
4 eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJnYWxpZWxlbyJ9.Hp95DiIZ0L0JXyQZOvhJkzyTDzNuos81QoTWfLeVPlodWvGg7ziJTI6nJFitg5VAwrGmA4wpbWbjK9aItCKB3A galieleo
5 eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJuZXd0b24ifQ.3E7UwKTwc8DchKRUSD_hdJxOcl4L6SOguwbm9WmVzWU4YDQMkIJ_wVNidpus6gNJvyT6OR6pREkfQCnWkEhEBQ newton
\. --
-- Data for Name: changed_variable; Type: TABLE DATA; Schema: public; Owner: postgres
-- COPY public.changed_variable (id, changed_variable, value_before, value_after, newsfeed_event_id, deleted) FROM stdin;
1 created 1 f
2 created 2 f
3 created 3 f
4 accepted false true 4 f
5 accepted false true 5 f
6 accepted false true 6 f
7 created 7 f
8 created 8 f
9 accepted false true 9 f
10 accepted false true 10 f
11 created 11 f
12 created 12 f
13 accepted false true 13 f
14 accepted false true 14 f
15 created 15 f
16 accepted false true 16 f
17 created 17 f
18 accepted false true 18 f
19 created 19 f
20 accepted false true 20 f
21 created 21 f
22 accepted false true 22 f
23 created 23 f
24 accepted false true 24 f
25 created 25 f
26 created 26 f
27 created 27 f
28 created 28 f
29 created 29 f
30 created 30 f
31 created 31 f
32 created 32 f
33 deleted 33 f
34 created 34 f
35 created 35 f
36 created 36 f
37 accepted false true 37 f
38 deleted 38 f
39 accepted false true 39 f
40 accepted false true 40 f
41 accepted false true 41 f
42 deleted 42 f
43 accepted false true 43 f
44 deleted 44 f
45 deleted 45 f
46 deleted 46 f
47 created 47 f
48 accepted false true 48 f
49 created 49 f
50 accepted false true 50 f
51 deleted 51 f
52 deleted 52 f
53 created 53 f
54 accepted false true 54 f
55 deleted 55 f
56 created 56 f
57 created 57 f
58 created 58 f
59 created 59 f
60 created 60 f
61 created 61 f
62 created 62 f
63 last logged in null Sat Mar 28 20:00:13 UTC 2020 63 f
64 active true false 64 f
65 last logged in null Sat Mar 28 20:05:47 UTC 2020 65 f
66 last logged in null Sat Mar 28 20:06:31 UTC 2020 66 f
67 last logged in null Sat Mar 28 20:07:31 UTC 2020 67 f
68 last logged in 2020-03-28 00:00:00.0 Sat Mar 28 20:09:02 UTC 2020 68 f
69 last logged in 2020-03-28 00:00:00.0 Sat Mar 28 20:09:56 UTC 2020 69 f
70 last logged in 2020-03-28 00:00:00.0 Sat Mar 28 20:12:06 UTC 2020 70 f
71 last logged in 2020-03-28 00:00:00.0 Sat Mar 28 20:14:30 UTC 2020 71 f
72 last logged in 2020-03-28 00:00:00.0 Sun Mar 29 10:42:45 UTC 2020 72 f
73 created 73 f
74 last logged in null Thu Aug 20 09:58:52 GMT 2020 74 f
75 last logged in 2020-03-28 00:00:00.0 Thu Aug 20 09:59:08 GMT 2020 75 f
76 active true false 76 f
77 created 77 f
78 last logged in null Tue Aug 25 12:35:38 GMT 2020 78 f
79 created 79 f
80 created 80 f
81 created 81 f
82 created 82 f
83 created 83 f
84 source /opt/rdepot/new/28009317/accrued_1.1.tar.gz /opt/rdepot/repositories/3/46950998/accrued_1.1.tar.gz 84 f
85 source /opt/rdepot/new/43623400/accrued_1.3.5.tar.gz /opt/rdepot/repositories/3/99077116/accrued_1.3.5.tar.gz 85 f
86 source /opt/rdepot/new/47097172/accrued_1.2.tar.gz /opt/rdepot/repositories/3/83118397/accrued_1.2.tar.gz 86 f
87 active false true 87 f
88 active false true 88 f
89 active false true 89 f
90 source /opt/rdepot/new/88466387/accrued_1.3.tar.gz /opt/rdepot/repositories/3/82197810/accrued_1.3.tar.gz 90 f
91 source /opt/rdepot/new/47523656/accrued_1.4.tar.gz /opt/rdepot/repositories/3/28075835/accrued_1.4.tar.gz 91 f
92 active false true 92 f
93 active false true 93 f
94 created 94 f
95 created 95 f
96 created 96 f
97 created 97 f
98 source /opt/rdepot/new/77598514/A3_0.9.2.tar.gz /opt/rdepot/repositories/4/54491936/A3_0.9.2.tar.gz 98 f
99 source /opt/rdepot/new/6984008/A3_0.9.1.tar.gz /opt/rdepot/repositories/4/47098069/A3_0.9.1.tar.gz 99 f
100 active false true 100 f
101 active false true 101 f
102 source /opt/rdepot/new/98224569/abc_1.3.tar.gz /opt/rdepot/repositories/4/95296712/abc_1.3.tar.gz 102 f
103 active false true 103 f
104 source /opt/rdepot/new/18685235/abc_1.0.tar.gz /opt/rdepot/repositories/4/49426769/abc_1.0.tar.gz 104 f
105 active false true 105 f
106 created 106 f
107 created 107 f
108 created 108 f
109 source /opt/rdepot/new/68910623/bea.R_1.0.5.tar.gz /opt/rdepot/repositories/2/89565416/bea.R_1.0.5.tar.gz 109 f
110 active false true 110 f
111 source /opt/rdepot/new/13236487/accrued_1.0.tar.gz /opt/rdepot/repositories/2/40553654/accrued_1.0.tar.gz 111 f
112 active false true 112 f
113 source /opt/rdepot/new/16258274/npordtests_1.1.tar.gz /opt/rdepot/repositories/2/8436419/npordtests_1.1.tar.gz 113 f
114 active false true 114 f
115 maintainer 5 4 115 f
116 maintainer 5 4 116 f
117 maintainer 5 4 117 f
118 maintainer 5 4 118 f
119 maintainer 5 4 119 f
120 maintainer 5 4 120 f
121 maintainer 5 4 121 f
122 maintainer 4 5 122 f
123 maintainer 4 5 123 f
124 maintainer 4 5 124 f
125 maintainer 4 5 125 f
126 active true false 126 f
127 active true false 127 f
128 active true false 128 f
129 active true false 129 f
130 maintainer 6 5 130 f
131 maintainer 6 4 131 f
132 maintainer 6 4 132 f
133 maintainer 6 4 133 f
134 maintainer 6 4 134 f
135 created 135 f
136 created 136 f
137 created 137 f
138 created 138 f
139 created 139 f
140 created 140 f
141 created 141 f
142 created 142 f
143 created 143 f
144 created 144 f
145 created 145 f
146 maintainer 5 4 146 f
147 source /opt/rdepot/new/37946660/usl_2.0.0.tar.gz /opt/rdepot/repositories/2/33930690/usl_2.0.0.tar.gz 147 f
148 active false true 148 f
149 maintainer 5 4 149 f
150 source /opt/rdepot/new/30320032/A3_0.9.2.tar.gz /opt/rdepot/repositories/2/9907084/A3_0.9.2.tar.gz 150 f
151 active false true 151 f
152 maintainer 5 4 152 f
153 source /opt/rdepot/new/26771812/abc_1.3.tar.gz /opt/rdepot/repositories/2/88170013/abc_1.3.tar.gz 153 f
154 active false true 154 f
155 maintainer 5 4 155 f
156 source /opt/rdepot/new/19806985/Benchmarking_0.10.tar.gz /opt/rdepot/repositories/2/71228208/Benchmarking_0.10.tar.gz 156 f
157 active false true 157 f
158 maintainer 5 4 158 f
159 source /opt/rdepot/new/9104202/AnaCoDa_0.1.2.3.tar.gz /opt/rdepot/repositories/5/39437028/AnaCoDa_0.1.2.3.tar.gz 159 f
160 active false true 160 f
161 delete false true 161 f
162 delete false true 162 f
163 created 163 f
164 created 164 f
165 source /opt/rdepot/new/73393322/usl_2.0.0.tar.gz /opt/rdepot/repositories/6/21695389/usl_2.0.0.tar.gz 165 f
166 active false true 166 f
167 source /opt/rdepot/new/28573212/visdat_0.1.0.tar.gz /opt/rdepot/repositories/6/70325377/visdat_0.1.0.tar.gz 167 f
168 active false true 168 f
169 delete false true 169 f
170 delete false true 170 f
171 created 171 f
172 source /opt/rdepot/new/33345471/A3_0.9.1.tar.gz /opt/rdepot/repositories/7/67484296/A3_0.9.1.tar.gz 172 f
173 active false true 173 f
174 delete false true 174 f
175 created 175 f
176 created 176 f
177 maintainer 6 5 177 f
178 maintainer 5 6 178 f
179 created 179 f
180 created 180 f
181 created 181 f
182 created 182 f
183 deleted 183 f
184 publication URI http://localhost/testrepo1 http://localhost/repo/testrepo1 184 f
185 server address http://localhost/testrepo1 http://oa-rdepot-repo:8080/testrepo1 185 f
186 submitted 4 186 f
187 submitted 5 187 f
188 submitted 6 188 f
189 version 0 1 189 f
190 version 0 1 190 f
191 version 0 1 191 f
192 version 1 2 192 f
193 version 1 2 193 f
194 version 1 2 194 f
195 version 2 3 195 f
196 version 2 3 196 f
197 added 8 197 f
198 added 4 198 f
199 version 2 3 199 f
200 added 6 200 f
201 submitted 7 201 f
202 submitted 8 202 f
203 version 0 1 203 f
204 version 0 1 204 f
205 version 1 2 205 f
206 version 2 3 206 f
207 added 5 207 f
208 version 1 2 208 f
209 version 2 3 209 f
210 added 7 210 f
211 submitted 9 211 f
212 submitted 10 212 f
213 version 0 1 213 f
214 version 0 1 214 f
215 version 1 2 215 f
216 version 2 3 216 f
217 added 10 217 f
218 version 1 2 218 f
219 version 2 3 219 f
220 added 9 220 f
221 submitted 11 221 f
222 version 0 1 222 f
223 version 1 2 223 f
224 version 2 3 224 f
225 added 11 225 f
226 submitted 12 226 f
227 version 0 1 227 f
228 version 1 2 228 f
229 version 2 3 229 f
230 added 12 230 f
231 submitted 13 231 f
232 version 0 1 232 f
233 version 1 2 233 f
234 version 2 3 234 f
235 added 15 235 f
236 submitted 14 236 f
237 version 0 1 237 f
238 version 1 2 238 f
239 version 2 3 239 f
240 added 13 240 f
241 submitted 15 241 f
242 version 0 1 242 f
243 version 1 2 243 f
244 version 2 3 244 f
245 added 14 245 f
246 version 3 4 246 f
247 version 4 5 247 f
248 version 5 6 248 f
249 version 3 4 249 f
250 version 4 5 250 f
251 version 5 6 251 f
252 version 6 7 252 f
253 version 7 8 253 f
254 version 8 9 254 f
255 version 9 10 255 f
256 version 10 11 256 f
257 version 11 12 257 f
258 version 3 4 258 f
259 version 4 5 259 f
260 version 6 7 260 f
261 version 7 8 261 f
262 version 12 13 262 f
263 version 13 14 263 f
264 version 14 15 264 f
265 version 15 16 265 f
266 submitted 16 266 f
267 submitted 17 267 f
268 submitted 18 268 f
269 submitted 19 269 f
270 submitted 20 270 f
271 submitted 21 271 f
272 submitted 22 272 f
273 submitted 23 273 f
274 submitted 24 274 f
275 submitted 25 275 f
276 version 8 9 276 f
277 version 9 10 277 f
278 submitted 26 278 f
279 version 10 11 279 f
280 version 11 12 280 f
281 added 25 281 f
282 version 12 13 282 f
283 version 13 14 283 f
284 version 14 15 284 f
285 version 15 16 285 f
286 added 17 286 f
287 version 16 17 287 f
288 version 17 18 288 f
289 version 18 19 289 f
290 version 19 20 290 f
291 added 18 291 f
292 version 20 21 292 f
293 version 21 22 293 f
294 version 22 23 294 f
295 version 23 24 295 f
296 added 21 296 f
297 version 0 1 297 f
298 version 1 2 298 f
299 version 2 3 299 f
300 version 3 4 300 f
301 added 20 301 f
302 published 302 f
303 version 24 25 303 f
304 published 304 f
305 version 4 5 305 f
306 published true false 306 f
307 version 5 6 307 f
308 published 308 f
309 version 5 6 309 f
310 published 310 f
311 version 16 17 311 f
312 published true false 312 f
313 version 17 18 313 f
314 version 6 7 314 f
315 published 315 f
316 version 7 8 316 f
317 version 25 26 317 f
318 published 318 f
319 version 26 27 319 f
320 submitted 27 320 f
321 version 0 1 321 f
322 version 1 2 322 f
323 version 2 3 323 f
324 added 27 324 f
325 submitted 28 325 f
326 version 0 1 326 f
327 version 1 2 327 f
328 version 2 3 328 f
329 added 28 329 f
330 published 330 f
331 version 3 4 331 f
332 version 4 5 332 f
333 published 333 f
334 version 5 6 334 f
335 version 6 7 335 f
336 published 336 f
337 version 7 8 337 f
338 published true false 338 f
339 version 8 9 339 f
340 deleted 340 f
341 submitted 29 341 f
342 version 0 1 342 f
343 version 1 2 343 f
344 version 2 3 344 f
345 added 29 345 f
346 published 346 f
347 version 3 4 347 f
348 published true false 348 f
349 version 4 5 349 f
350 version 5 6 350 f
351 deleted 351 f
352 submitted 30 352 f
353 submitted 31 353 f
354 version 27 28 354 f
355 published 355 f
356 version 28 29 356 f
357 version 29 30 357 f
358 published 358 f
359 version 30 31 359 f
360 created 360 f
361 created 361 f
362 created 362 f
363 deleted Sat Mar 28 20:04:41 UTC 2020 363 f
\. --
-- Data for Name: databasechangelog; Type: TABLE DATA; Schema: public; Owner: postgres
-- COPY public.databasechangelog (id, author, filename, dateexecuted, orderexecuted, exectype, md5sum, description, comments, tag, liquibase, contexts, labels, deployment_id) FROM stdin;
\. --
-- Data for Name: databasechangeloglock; Type: TABLE DATA; Schema: public; Owner: postgres
-- COPY public.databasechangeloglock (id, locked, lockgranted, lockedby) FROM stdin;
1 f \N \N
\. --
-- Data for Name: newsfeed_event; Type: TABLE DATA; Schema: public; Owner: postgres
-- COPY public.newsfeed_event (id, newsfeed_event_type, author_id, related_packagemaintainer_id, related_repositorymaintainer_id, related_user_id, related_submission_id, related_repository_id, related_package_id, deleted, related_accesstoken_id, "time") FROM stdin;
1 CREATE 4 \N \N \N 4 \N \N f \N 2020-03-28 20:03:44
2 CREATE 4 \N \N \N 5 \N \N f \N 2020-03-28 20:03:44
3 CREATE 4 \N \N \N 6 \N \N f \N 2020-03-28 20:03:44
4 UPDATE 4 \N \N \N 6 \N \N f \N 2020-03-28 20:03:44
5 UPDATE 4 \N \N \N 5 \N \N f \N 2020-03-28 20:03:44
6 UPDATE 4 \N \N \N 4 \N \N f \N 2020-03-28 20:03:44
7 CREATE 4 \N \N \N 7 \N \N f \N 2020-03-28 20:03:45
8 CREATE 4 \N \N \N 8 \N \N f \N 2020-03-28 20:03:45
9 UPDATE 4 \N \N \N 7 \N \N f \N 2020-03-28 20:03:45
10 UPDATE 4 \N \N \N 8 \N \N f \N 2020-03-28 20:03:45
11 CREATE 4 \N \N \N 9 \N \N f \N 2020-03-28 20:03:58
12 CREATE 4 \N \N \N 10 \N \N f \N 2020-03-28 20:03:58
13 UPDATE 4 \N \N \N 10 \N \N f \N 2020-03-28 20:03:58
14 UPDATE 4 \N \N \N 9 \N \N f \N 2020-03-28 20:03:58
15 CREATE 4 \N \N \N 11 \N \N f \N 2020-03-28 20:03:59
16 UPDATE 4 \N \N \N 11 \N \N f \N 2020-03-28 20:03:59
17 CREATE 4 \N \N \N 12 \N \N f \N 2020-03-28 20:03:59
18 UPDATE 4 \N \N \N 12 \N \N f \N 2020-03-28 20:03:59
19 CREATE 4 \N \N \N 13 \N \N f \N 2020-03-28 20:04:17
20 UPDATE 4 \N \N \N 13 \N \N f \N 2020-03-28 20:04:17
21 CREATE 4 \N \N \N 14 \N \N f \N 2020-03-28 20:04:17
22 UPDATE 4 \N \N \N 14 \N \N f \N 2020-03-28 20:04:17
23 CREATE 4 \N \N \N 15 \N \N f \N 2020-03-28 20:04:17
24 UPDATE 4 \N \N \N 15 \N \N f \N 2020-03-28 20:04:17
25 CREATE 7 \N \N \N 16 \N \N f \N 2020-03-28 20:05:58
26 CREATE 7 \N \N \N 17 \N \N f \N 2020-03-28 20:05:58
27 CREATE 7 \N \N \N 18 \N \N f \N 2020-03-28 20:05:58
28 CREATE 7 \N \N \N 19 \N \N f \N 2020-03-28 20:06:13
29 CREATE 7 \N \N \N 20 \N \N f \N 2020-03-28 20:06:23
30 CREATE 6 \N \N \N 21 \N \N f \N 2020-03-28 20:06:48
31 CREATE 6 \N \N \N 22 \N \N f \N 2020-03-28 20:06:49
32 CREATE 6 \N \N \N 23 \N \N f \N 2020-03-28 20:06:49
33 DELETE 6 \N \N \N 23 \N \N f \N 2020-03-28 20:07:00
34 CREATE 6 \N \N \N 24 \N \N f \N 2020-03-28 20:07:13
35 CREATE 5 \N \N \N 25 \N \N f \N 2020-03-28 20:07:52
36 CREATE 5 \N \N \N 26 \N \N f \N 2020-03-28 20:07:52
37 UPDATE 5 \N \N \N 25 \N \N f \N 2020-03-28 20:07:52
38 DELETE 5 \N \N \N 16 \N \N f \N 2020-03-28 20:08:08
39 UPDATE 5 \N \N \N 17 \N \N f \N 2020-03-28 20:08:12
40 UPDATE 5 \N \N \N 18 \N \N f \N 2020-03-28 20:08:18
41 UPDATE 5 \N \N \N 21 \N \N f \N 2020-03-28 20:08:23
42 DELETE 5 \N \N \N 22 \N \N f \N 2020-03-28 20:08:31
43 UPDATE 5 \N \N \N 20 \N \N f \N 2020-03-28 20:08:41
44 DELETE 5 \N \N \N 24 \N \N f \N 2020-03-28 20:08:42
45 DELETE 4 \N \N \N 4 \N \N f \N 2020-03-28 20:09:48
46 DELETE 5 \N \N \N 15 \N \N f \N 2020-03-28 20:10:08
47 CREATE 4 \N \N \N 27 \N \N f \N 2020-03-28 20:12:44
48 UPDATE 4 \N \N \N 27 \N \N f \N 2020-03-28 20:12:44
49 CREATE 4 \N \N \N 28 \N \N f \N 2020-03-28 20:12:44
50 UPDATE 4 \N \N \N 28 \N \N f \N 2020-03-28 20:12:44
51 DELETE 4 \N \N \N 28 \N \N f \N 2020-03-28 20:13:30
52 DELETE 4 \N \N \N 27 \N \N f \N 2020-03-28 20:13:30
53 CREATE 4 \N \N \N 29 \N \N f \N 2020-03-28 20:14:06
54 UPDATE 4 \N \N \N 29 \N \N f \N 2020-03-28 20:14:06
55 DELETE 4 \N \N \N 29 \N \N f \N 2020-03-28 20:14:17
56 CREATE 6 \N \N \N 30 \N \N f \N 2020-03-28 20:14:44
57 CREATE 6 \N \N \N 31 \N \N f \N 2020-03-28 20:14:45
58 CREATE 4 \N \N 4 \N \N \N f \N 2020-03-28 20:59:28
59 CREATE 5 \N \N 5 \N \N \N f \N 2020-03-28 20:59:28
60 CREATE 6 \N \N 6 \N \N \N f \N 2020-03-28 20:59:28
61 CREATE 7 \N \N 7 \N \N \N f \N 2020-03-28 20:59:28
62 CREATE 8 \N \N 8 \N \N \N f \N 2020-03-28 20:59:28
63 UPDATE 4 \N \N 4 \N \N \N f \N 2020-03-28 20:00:14
64 UPDATE 4 \N \N 8 \N \N \N f \N 2020-03-28 20:03:28
65 UPDATE 4 \N \N 7 \N \N \N f \N 2020-03-28 20:05:47
66 UPDATE 4 \N \N 6 \N \N \N f \N 2020-03-28 20:06:31
67 UPDATE 4 \N \N 5 \N \N \N f \N 2020-03-28 20:07:31
68 UPDATE 4 \N \N 4 \N \N \N f \N 2020-03-28 20:09:02
69 UPDATE 8 \N \N 5 \N \N \N f \N 2020-03-28 20:09:56
70 UPDATE 8 \N \N 4 \N \N \N f \N 2020-03-28 20:12:06
71 UPDATE 8 \N \N 6 \N \N \N f \N 2020-03-28 20:14:30
72 UPDATE 8 \N \N 5 \N \N \N f \N 2020-03-29 10:42:45
73 CREATE 8 \N \N 9 \N \N \N f \N 2020-08-20 09:58:51
74 UPDATE 8 \N \N 9 \N \N \N f \N 2020-08-20 09:58:52
75 UPDATE 8 \N \N 4 \N \N \N f \N 2020-08-20 09:59:08
76 UPDATE 4 \N \N 9 \N \N \N f \N 2020-08-20 09:59:21
77 CREATE 8 \N \N 10 \N \N \N f \N 2020-08-25 12:35:38
78 UPDATE 8 \N \N 10 \N \N \N f \N 2020-08-25 12:35:38
79 UPLOAD 4 \N \N \N \N \N 4 f \N 2020-03-28 20:03:40
80 UPLOAD 4 \N \N \N \N \N 6 f \N 2020-03-28 20:03:40
81 UPLOAD 4 \N \N \N \N \N 5 f \N 2020-03-28 20:03:40
82 UPLOAD 4 \N \N \N \N \N 8 f \N 2020-03-28 20:03:40
83 UPLOAD 4 \N \N \N \N \N 7 f \N 2020-03-28 20:03:40
84 UPDATE 4 \N \N \N \N \N 6 f \N 2020-03-28 20:03:44
85 UPDATE 4 \N \N \N \N \N 4 f \N 2020-03-28 20:03:44
86 UPDATE 4 \N \N \N \N \N 8 f \N 2020-03-28 20:03:44
87 UPDATE 4 \N \N \N \N \N 4 f \N 2020-03-28 20:03:44
88 UPDATE 4 \N \N \N \N \N 6 f \N 2020-03-28 20:03:44
89 UPDATE 4 \N \N \N \N \N 8 f \N 2020-03-28 20:03:44
90 UPDATE 4 \N \N \N \N \N 5 f \N 2020-03-28 20:03:45
91 UPDATE 4 \N \N \N \N \N 7 f \N 2020-03-28 20:03:45
92 UPDATE 4 \N \N \N \N \N 5 f \N 2020-03-28 20:03:45
93 UPDATE 4 \N \N \N \N \N 7 f \N 2020-03-28 20:03:45
94 UPLOAD 4 \N \N \N \N \N 9 f \N 2020-03-28 20:03:55
95 UPLOAD 4 \N \N \N \N \N 10 f \N 2020-03-28 20:03:55
96 UPLOAD 4 \N \N \N \N \N 11 f \N 2020-03-28 20:03:56
97 UPLOAD 4 \N \N \N \N \N 12 f \N 2020-03-28 20:03:56
98 UPDATE 4 \N \N \N \N \N 10 f \N 2020-03-28 20:03:58
99 UPDATE 4 \N \N \N \N \N 9 f \N 2020-03-28 20:03:58
100 UPDATE 4 \N \N \N \N \N 10 f \N 2020-03-28 20:03:59
101 UPDATE 4 \N \N \N \N \N 9 f \N 2020-03-28 20:03:59
102 UPDATE 4 \N \N \N \N \N 11 f \N 2020-03-28 20:03:59
103 UPDATE 4 \N \N \N \N \N 11 f \N 2020-03-28 20:03:59
104 UPDATE 4 \N \N \N \N \N 12 f \N 2020-03-28 20:03:59
105 UPDATE 4 \N \N \N \N \N 12 f \N 2020-03-28 20:03:59
106 UPLOAD 4 \N \N \N \N \N 13 f \N 2020-03-28 20:04:14
107 UPLOAD 4 \N \N \N \N \N 14 f \N 2020-03-28 20:04:14
108 UPLOAD 4 \N \N \N \N \N 15 f \N 2020-03-28 20:04:14
109 UPDATE 4 \N \N \N \N \N 15 f \N 2020-03-28 20:04:17
110 UPDATE 4 \N \N \N \N \N 15 f \N 2020-03-28 20:04:17
111 UPDATE 4 \N \N \N \N \N 13 f \N 2020-03-28 20:04:17
112 UPDATE 4 \N \N \N \N \N 13 f \N 2020-03-28 20:04:17
113 UPDATE 4 \N \N \N \N \N 14 f \N 2020-03-28 20:04:17
114 UPDATE 4 \N \N \N \N \N 14 f \N 2020-03-28 20:04:17
115 UPDATE 4 \N \N \N \N \N 13 f \N 2020-03-28 20:04:24
116 UPDATE 4 \N \N \N \N \N 15 f \N 2020-03-28 20:04:24
117 UPDATE 4 \N \N \N \N \N 14 f \N 2020-03-28 20:04:24
118 UPDATE 4 \N \N \N \N \N 11 f \N 2020-03-28 20:04:31
119 UPDATE 4 \N \N \N \N \N 10 f \N 2020-03-28 20:04:31
120 UPDATE 4 \N \N \N \N \N 12 f \N 2020-03-28 20:04:31
121 UPDATE 4 \N \N \N \N \N 9 f \N 2020-03-28 20:04:31
122 UPDATE 4 \N \N \N \N \N 10 f \N 2020-03-28 20:04:41
123 UPDATE 4 \N \N \N \N \N 11 f \N 2020-03-28 20:04:41
124 UPDATE 4 \N \N \N \N \N 9 f \N 2020-03-28 20:04:41
125 UPDATE 4 \N \N \N \N \N 12 f \N 2020-03-28 20:04:41
126 UPDATE 4 \N \N \N \N \N 11 f \N 2020-03-28 20:04:47
127 UPDATE 4 \N \N \N \N \N 7 f \N 2020-03-28 20:04:51
128 UPDATE 4 \N \N \N \N \N 6 f \N 2020-03-28 20:04:54
129 UPDATE 4 \N \N \N \N \N 13 f \N 2020-03-28 20:04:56
130 UPDATE 4 \N \N \N \N \N 13 f \N 2020-03-28 20:05:25
131 UPDATE 4 \N \N \N \N \N 12 f \N 2020-03-28 20:05:29
132 UPDATE 4 \N \N \N \N \N 11 f \N 2020-03-28 20:05:29
133 UPDATE 4 \N \N \N \N \N 10 f \N 2020-03-28 20:05:35
134 UPDATE 4 \N \N \N \N \N 9 f \N 2020-03-28 20:05:35
135 UPLOAD 7 \N \N \N \N \N 16 f \N 2020-03-28 20:05:55
136 UPLOAD 7 \N \N \N \N \N 17 f \N 2020-03-28 20:05:55
137 UPLOAD 7 \N \N \N \N \N 18 f \N 2020-03-28 20:05:56
138 UPLOAD 7 \N \N \N \N \N 19 f \N 2020-03-28 20:06:11
139 UPLOAD 7 \N \N \N \N \N 20 f \N 2020-03-28 20:06:21
140 UPLOAD 6 \N \N \N \N \N 21 f \N 2020-03-28 20:06:46
141 UPLOAD 6 \N \N \N \N \N 22 f \N 2020-03-28 20:06:46
142 UPLOAD 6 \N \N \N \N \N 23 f \N 2020-03-28 20:06:46
143 UPLOAD 6 \N \N \N \N \N 24 f \N 2020-03-28 20:07:11
144 UPLOAD 5 \N \N \N \N \N 25 f \N 2020-03-28 20:07:50
145 UPLOAD 5 \N \N \N \N \N 26 f \N 2020-03-28 20:07:50
146 UPDATE 5 \N \N \N \N \N 25 f \N 2020-03-28 20:07:52
147 UPDATE 5 \N \N \N \N \N 25 f \N 2020-03-28 20:07:52
148 UPDATE 5 \N \N \N \N \N 25 f \N 2020-03-28 20:07:52
149 UPDATE 5 \N \N \N \N \N 17 f \N 2020-03-28 20:08:12
150 UPDATE 5 \N \N \N \N \N 17 f \N 2020-03-28 20:08:12
151 UPDATE 5 \N \N \N \N \N 17 f \N 2020-03-28 20:08:12
152 UPDATE 5 \N \N \N \N \N 18 f \N 2020-03-28 20:08:18
153 UPDATE 5 \N \N \N \N \N 18 f \N 2020-03-28 20:08:18
154 UPDATE 5 \N \N \N \N \N 18 f \N 2020-03-28 20:08:18
155 UPDATE 5 \N \N \N \N \N 21 f \N 2020-03-28 20:08:23
156 UPDATE 5 \N \N \N \N \N 21 f \N 2020-03-28 20:08:23
157 UPDATE 5 \N \N \N \N \N 21 f \N 2020-03-28 20:08:23
158 UPDATE 5 \N \N \N \N \N 20 f \N 2020-03-28 20:08:41
159 UPDATE 5 \N \N \N \N \N 20 f \N 2020-03-28 20:08:41
160 UPDATE 5 \N \N \N \N \N 20 f \N 2020-03-28 20:08:41
161 DELETE 4 \N \N \N \N \N 6 f \N 2020-03-28 20:09:48
162 DELETE 5 \N \N \N \N \N 14 f \N 2020-03-28 20:10:08
163 UPLOAD 4 \N \N \N \N \N 27 f \N 2020-03-28 20:12:42
164 UPLOAD 4 \N \N \N \N \N 28 f \N 2020-03-28 20:12:42
165 UPDATE 4 \N \N \N \N \N 27 f \N 2020-03-28 20:12:44
166 UPDATE 4 \N \N \N \N \N 27 f \N 2020-03-28 20:12:44
167 UPDATE 4 \N \N \N \N \N 28 f \N 2020-03-28 20:12:44
168 UPDATE 4 \N \N \N \N \N 28 f \N 2020-03-28 20:12:44
169 DELETE 4 \N \N \N \N \N 28 f \N 2020-03-28 20:13:30
170 DELETE 4 \N \N \N \N \N 27 f \N 2020-03-28 20:13:30
171 UPLOAD 4 \N \N \N \N \N 29 f \N 2020-03-28 20:14:04
172 UPDATE 4 \N \N \N \N \N 29 f \N 2020-03-28 20:14:06
173 UPDATE 4 \N \N \N \N \N 29 f \N 2020-03-28 20:14:06
174 DELETE 4 \N \N \N \N \N 29 f \N 2020-03-28 20:14:17
175 UPLOAD 6 \N \N \N \N \N 30 f \N 2020-03-28 20:14:42
176 UPLOAD 6 \N \N \N \N \N 31 f \N 2020-03-28 20:14:43
177 UPDATE 4 \N \N \N \N \N 15 f \N 2020-03-29 10:43:05
178 UPDATE 4 \N \N \N \N \N 15 f \N 2020-03-29 10:43:10
179 CREATE 4 1 \N \N \N \N \N f \N 2020-03-28 20:05:25
180 CREATE 4 2 \N \N \N \N \N f \N 2020-03-28 20:05:29
181 CREATE 4 3 \N \N \N \N \N f \N 2020-03-28 20:05:35
182 CREATE 4 4 \N \N \N \N \N f \N 2020-03-29 10:43:05
183 DELETE 4 4 \N \N \N \N \N f \N 2020-03-29 10:43:10
184 UPDATE 4 \N \N \N \N 2 \N f \N 2020-03-28 20:01:11
185 UPDATE 4 \N \N \N \N 2 \N f \N 2020-03-28 20:01:13
186 UPDATE 4 \N \N \N \N 3 \N f \N 2020-03-28 20:03:44
187 UPDATE 4 \N \N \N \N 3 \N f \N 2020-03-28 20:03:44
188 UPDATE 4 \N \N \N \N 3 \N f \N 2020-03-28 20:03:44
189 UPDATE 4 \N \N \N \N 3 \N f \N 2020-03-28 20:03:44
190 UPDATE 4 \N \N \N \N 3 \N f \N 2020-03-28 20:03:44
191 UPDATE 4 \N \N \N \N 3 \N f \N 2020-03-28 20:03:44
192 UPDATE 4 \N \N \N \N 3 \N f \N 2020-03-28 20:03:44
193 UPDATE 4 \N \N \N \N 3 \N f \N 2020-03-28 20:03:44
194 UPDATE 4 \N \N \N \N 3 \N f \N 2020-03-28 20:03:44
195 UPDATE 4 \N \N \N \N 3 \N f \N 2020-03-28 20:03:44
196 UPDATE 4 \N \N \N \N 3 \N f \N 2020-03-28 20:03:44
197 UPDATE 4 \N \N \N \N 3 \N f \N 2020-03-28 20:03:44
198 UPDATE 4 \N \N \N \N 3 \N f \N 2020-03-28 20:03:44
199 UPDATE 4 \N \N \N \N 3 \N f \N 2020-03-28 20:03:44
200 UPDATE 4 \N \N \N \N 3 \N f \N 2020-03-28 20:03:44
201 UPDATE 4 \N \N \N \N 3 \N f \N 2020-03-28 20:03:45
202 UPDATE 4 \N \N \N \N 3 \N f \N 2020-03-28 20:03:45
203 UPDATE 4 \N \N \N \N 3 \N f \N 2020-03-28 20:03:45
204 UPDATE 4 \N \N \N \N 3 \N f \N 2020-03-28 20:03:45
205 UPDATE 4 \N \N \N \N 3 \N f \N 2020-03-28 20:03:45
206 UPDATE 4 \N \N \N \N 3 \N f \N 2020-03-28 20:03:45
207 UPDATE 4 \N \N \N \N 3 \N f \N 2020-03-28 20:03:45
208 UPDATE 4 \N \N \N \N 3 \N f \N 2020-03-28 20:03:45
209 UPDATE 4 \N \N \N \N 3 \N f \N 2020-03-28 20:03:45
210 UPDATE 4 \N \N \N \N 3 \N f \N 2020-03-28 20:03:45
211 UPDATE 4 \N \N \N \N 4 \N f \N 2020-03-28 20:03:58
212 UPDATE 4 \N \N \N \N 4 \N f \N 2020-03-28 20:03:58
213 UPDATE 4 \N \N \N \N 4 \N f \N 2020-03-28 20:03:58
214 UPDATE 4 \N \N \N \N 4 \N f \N 2020-03-28 20:03:59
215 UPDATE 4 \N \N \N \N 4 \N f \N 2020-03-28 20:03:59
216 UPDATE 4 \N \N \N \N 4 \N f \N 2020-03-28 20:03:59
217 UPDATE 4 \N \N \N \N 4 \N f \N 2020-03-28 20:03:59
218 UPDATE 4 \N \N \N \N 4 \N f \N 2020-03-28 20:03:59
219 UPDATE 4 \N \N \N \N 4 \N f \N 2020-03-28 20:03:59
220 UPDATE 4 \N \N \N \N 4 \N f \N 2020-03-28 20:03:59
221 UPDATE 4 \N \N \N \N 4 \N f \N 2020-03-28 20:03:59
222 UPDATE 4 \N \N \N \N 4 \N f \N 2020-03-28 20:03:59
223 UPDATE 4 \N \N \N \N 4 \N f \N 2020-03-28 20:03:59
224 UPDATE 4 \N \N \N \N 4 \N f \N 2020-03-28 20:03:59
225 UPDATE 4 \N \N \N \N 4 \N f \N 2020-03-28 20:03:59
226 UPDATE 4 \N \N \N \N 4 \N f \N 2020-03-28 20:03:59
227 UPDATE 4 \N \N \N \N 4 \N f \N 2020-03-28 20:03:59
228 UPDATE 4 \N \N \N \N 4 \N f \N 2020-03-28 20:03:59
229 UPDATE 4 \N \N \N \N 4 \N f \N 2020-03-28 20:03:59
230 UPDATE 4 \N \N \N \N 4 \N f \N 2020-03-28 20:03:59
231 UPDATE 4 \N \N \N \N 2 \N f \N 2020-03-28 20:04:17
232 UPDATE 4 \N \N \N \N 2 \N f \N 2020-03-28 20:04:17
233 UPDATE 4 \N \N \N \N 2 \N f \N 2020-03-28 20:04:17
234 UPDATE 4 \N \N \N \N 2 \N f \N 2020-03-28 20:04:17
235 UPDATE 4 \N \N \N \N 2 \N f \N 2020-03-28 20:04:17
236 UPDATE 4 \N \N \N \N 2 \N f \N 2020-03-28 20:04:17
237 UPDATE 4 \N \N \N \N 2 \N f \N 2020-03-28 20:04:17
238 UPDATE 4 \N \N \N \N 2 \N f \N 2020-03-28 20:04:17
239 UPDATE 4 \N \N \N \N 2 \N f \N 2020-03-28 20:04:17
240 UPDATE 4 \N \N \N \N 2 \N f \N 2020-03-28 20:04:17
241 UPDATE 4 \N \N \N \N 2 \N f \N 2020-03-28 20:04:17
242 UPDATE 4 \N \N \N \N 2 \N f \N 2020-03-28 20:04:17
243 UPDATE 4 \N \N \N \N 2 \N f \N 2020-03-28 20:04:17
244 UPDATE 4 \N \N \N \N 2 \N f \N 2020-03-28 20:04:17
245 UPDATE 4 \N \N \N \N 2 \N f \N 2020-03-28 20:04:17
246 UPDATE 4 \N \N \N \N 2 \N f \N 2020-03-28 20:04:24
247 UPDATE 4 \N \N \N \N 2 \N f \N 2020-03-28 20:04:24
248 UPDATE 4 \N \N \N \N 2 \N f \N 2020-03-28 20:04:24
249 UPDATE 4 \N \N \N \N 4 \N f \N 2020-03-28 20:04:31
250 UPDATE 4 \N \N \N \N 4 \N f \N 2020-03-28 20:04:31
251 UPDATE 4 \N \N \N \N 4 \N f \N 2020-03-28 20:04:31
252 UPDATE 4 \N \N \N \N 4 \N f \N 2020-03-28 20:04:31
253 UPDATE 4 \N \N \N \N 4 \N f \N 2020-03-28 20:04:41
254 UPDATE 4 \N \N \N \N 4 \N f \N 2020-03-28 20:04:41
255 UPDATE 4 \N \N \N \N 4 \N f \N 2020-03-28 20:04:41
256 UPDATE 4 \N \N \N \N 4 \N f \N 2020-03-28 20:04:41
257 UPDATE 4 \N \N \N \N 4 \N f \N 2020-03-28 20:04:47
258 UPDATE 4 \N \N \N \N 3 \N f \N 2020-03-28 20:04:51
259 UPDATE 4 \N \N \N \N 3 \N f \N 2020-03-28 20:04:54
260 UPDATE 4 \N \N \N \N 2 \N f \N 2020-03-28 20:04:56
261 UPDATE 4 \N \N \N \N 2 \N f \N 2020-03-28 20:05:25
262 UPDATE 4 \N \N \N \N 4 \N f \N 2020-03-28 20:05:29
263 UPDATE 4 \N \N \N \N 4 \N f \N 2020-03-28 20:05:29
264 UPDATE 4 \N \N \N \N 4 \N f \N 2020-03-28 20:05:35
265 UPDATE 4 \N \N \N \N 4 \N f \N 2020-03-28 20:05:35
266 UPDATE 7 \N \N \N \N 2 \N f \N 2020-03-28 20:05:58
267 UPDATE 7 \N \N \N \N 2 \N f \N 2020-03-28 20:05:58
268 UPDATE 7 \N \N \N \N 2 \N f \N 2020-03-28 20:05:58
269 UPDATE 7 \N \N \N \N 3 \N f \N 2020-03-28 20:06:13
270 UPDATE 7 \N \N \N \N 5 \N f \N 2020-03-28 20:06:23
271 UPDATE 6 \N \N \N \N 2 \N f \N 2020-03-28 20:06:48
272 UPDATE 6 \N \N \N \N 2 \N f \N 2020-03-28 20:06:49
273 UPDATE 6 \N \N \N \N 2 \N f \N 2020-03-28 20:06:49
274 UPDATE 6 \N \N \N \N 5 \N f \N 2020-03-28 20:07:13
275 UPDATE 5 \N \N \N \N 2 \N f \N 2020-03-28 20:07:52
276 UPDATE 5 \N \N \N \N 2 \N f \N 2020-03-28 20:07:52
277 UPDATE 5 \N \N \N \N 2 \N f \N 2020-03-28 20:07:52
278 UPDATE 5 \N \N \N \N 3 \N f \N 2020-03-28 20:07:52
279 UPDATE 5 \N \N \N \N 2 \N f \N 2020-03-28 20:07:52
280 UPDATE 5 \N \N \N \N 2 \N f \N 2020-03-28 20:07:52
281 UPDATE 5 \N \N \N \N 2 \N f \N 2020-03-28 20:07:52
282 UPDATE 5 \N \N \N \N 2 \N f \N 2020-03-28 20:08:12
283 UPDATE 5 \N \N \N \N 2 \N f \N 2020-03-28 20:08:12
284 UPDATE 5 \N \N \N \N 2 \N f \N 2020-03-28 20:08:12
285 UPDATE 5 \N \N \N \N 2 \N f \N 2020-03-28 20:08:12
286 UPDATE 5 \N \N \N \N 2 \N f \N 2020-03-28 20:08:12
287 UPDATE 5 \N \N \N \N 2 \N f \N 2020-03-28 20:08:18
288 UPDATE 5 \N \N \N \N 2 \N f \N 2020-03-28 20:08:18
289 UPDATE 5 \N \N \N \N 2 \N f \N 2020-03-28 20:08:18
290 UPDATE 5 \N \N \N \N 2 \N f \N 2020-03-28 20:08:18
291 UPDATE 5 \N \N \N \N 2 \N f \N 2020-03-28 20:08:18
292 UPDATE 5 \N \N \N \N 2 \N f \N 2020-03-28 20:08:23
293 UPDATE 5 \N \N \N \N 2 \N f \N 2020-03-28 20:08:23
294 UPDATE 5 \N \N \N \N 2 \N f \N 2020-03-28 20:08:23
295 UPDATE 5 \N \N \N \N 2 \N f \N 2020-03-28 20:08:23
296 UPDATE 5 \N \N \N \N 2 \N f \N 2020-03-28 20:08:23
297 UPDATE 5 \N \N \N \N 5 \N f \N 2020-03-28 20:08:41
298 UPDATE 5 \N \N \N \N 5 \N f \N 2020-03-28 20:08:41
299 UPDATE 5 \N \N \N \N 5 \N f \N 2020-03-28 20:08:41
300 UPDATE 5 \N \N \N \N 5 \N f \N 2020-03-28 20:08:41
301 UPDATE 5 \N \N \N \N 5 \N f \N 2020-03-28 20:08:41
302 UPDATE 5 \N \N \N \N 2 \N f \N 2020-03-28 20:08:48
303 UPDATE 5 \N \N \N \N 2 \N f \N 2020-03-28 20:08:48
304 UPDATE 5 \N \N \N \N 5 \N f \N 2020-03-28 20:08:51
305 UPDATE 5 \N \N \N \N 5 \N f \N 2020-03-28 20:08:51
306 UPDATE 5 \N \N \N \N 5 \N f \N 2020-03-28 20:08:53
307 UPDATE 5 \N \N \N \N 5 \N f \N 2020-03-28 20:08:53
308 UPDATE 4 \N \N \N \N 3 \N f \N 2020-03-28 20:09:06
309 UPDATE 4 \N \N \N \N 3 \N f \N 2020-03-28 20:09:06
310 UPDATE 4 \N \N \N \N 4 \N f \N 2020-03-28 20:09:09
311 UPDATE 4 \N \N \N \N 4 \N f \N 2020-03-28 20:09:09
312 UPDATE 4 \N \N \N \N 4 \N f \N 2020-03-28 20:09:15
313 UPDATE 4 \N \N \N \N 4 \N f \N 2020-03-28 20:09:15
314 UPDATE 4 \N \N \N \N 3 \N f \N 2020-03-28 20:09:48
315 UPDATE 4 \N \N \N \N 3 \N f \N 2020-03-28 20:09:48
316 UPDATE 4 \N \N \N \N 3 \N f \N 2020-03-28 20:09:48
317 UPDATE 5 \N \N \N \N 2 \N f \N 2020-03-28 20:10:08
318 UPDATE 5 \N \N \N \N 2 \N f \N 2020-03-28 20:10:08
319 UPDATE 5 \N \N \N \N 2 \N f \N 2020-03-28 20:10:08
320 UPDATE 4 \N \N \N \N 6 \N f \N 2020-03-28 20:12:44
321 UPDATE 4 \N \N \N \N 6 \N f \N 2020-03-28 20:12:44
322 UPDATE 4 \N \N \N \N 6 \N f \N 2020-03-28 20:12:44
323 UPDATE 4 \N \N \N \N 6 \N f \N 2020-03-28 20:12:44
324 UPDATE 4 \N \N \N \N 6 \N f \N 2020-03-28 20:12:44
325 UPDATE 4 \N \N \N \N 6 \N f \N 2020-03-28 20:12:44
326 UPDATE 4 \N \N \N \N 6 \N f \N 2020-03-28 20:12:44
327 UPDATE 4 \N \N \N \N 6 \N f \N 2020-03-28 20:12:44
328 UPDATE 4 \N \N \N \N 6 \N f \N 2020-03-28 20:12:44
329 UPDATE 4 \N \N \N \N 6 \N f \N 2020-03-28 20:12:44
330 UPDATE 4 \N \N \N \N 6 \N f \N 2020-03-28 20:13:27
331 UPDATE 4 \N \N \N \N 6 \N f \N 2020-03-28 20:13:27
332 UPDATE 4 \N \N \N \N 6 \N f \N 2020-03-28 20:13:30
333 UPDATE 4 \N \N \N \N 6 \N f \N 2020-03-28 20:13:30
334 UPDATE 4 \N \N \N \N 6 \N f \N 2020-03-28 20:13:30
335 UPDATE 4 \N \N \N \N 6 \N f \N 2020-03-28 20:13:30
336 UPDATE 4 \N \N \N \N 6 \N f \N 2020-03-28 20:13:30
337 UPDATE 4 \N \N \N \N 6 \N f \N 2020-03-28 20:13:30
338 UPDATE 4 \N \N \N \N 6 \N f \N 2020-03-28 20:13:30
339 UPDATE 4 \N \N \N \N 6 \N f \N 2020-03-28 20:13:30
340 DELETE 4 \N \N \N \N 6 \N f \N 2020-03-28 20:13:30
341 UPDATE 4 \N \N \N \N 7 \N f \N 2020-03-28 20:14:06
342 UPDATE 4 \N \N \N \N 7 \N f \N 2020-03-28 20:14:06
343 UPDATE 4 \N \N \N \N 7 \N f \N 2020-03-28 20:14:06
344 UPDATE 4 \N \N \N \N 7 \N f \N 2020-03-28 20:14:06
345 UPDATE 4 \N \N \N \N 7 \N f \N 2020-03-28 20:14:06
346 UPDATE 4 \N \N \N \N 7 \N f \N 2020-03-28 20:14:10
347 UPDATE 4 \N \N \N \N 7 \N f \N 2020-03-28 20:14:10
348 UPDATE 4 \N \N \N \N 7 \N f \N 2020-03-28 20:14:14
349 UPDATE 4 \N \N \N \N 7 \N f \N 2020-03-28 20:14:14
350 UPDATE 4 \N \N \N \N 7 \N f \N 2020-03-28 20:14:17
351 DELETE 4 \N \N \N \N 7 \N f \N 2020-03-28 20:14:19
352 UPDATE 6 \N \N \N \N 5 \N f \N 2020-03-28 20:14:44
353 UPDATE 6 \N \N \N \N 5 \N f \N 2020-03-28 20:14:45
354 UPDATE 4 \N \N \N \N 2 \N f \N 2020-03-29 10:43:05
355 UPDATE 4 \N \N \N \N 2 \N f \N 2020-03-29 10:43:05
356 UPDATE 4 \N \N \N \N 2 \N f \N 2020-03-29 10:43:05
357 UPDATE 4 \N \N \N \N 2 \N f \N 2020-03-29 10:43:10
358 UPDATE 4 \N \N \N \N 2 \N f \N 2020-03-29 10:43:10
359 UPDATE 4 \N \N \N \N 2 \N f \N 2020-03-29 10:43:10
360 CREATE 4 \N 1 \N \N \N \N f \N 2020-03-28 20:04:24
361 CREATE 4 \N 2 \N \N \N \N f \N 2020-03-28 20:04:31
362 CREATE 4 \N 3 \N \N \N \N f \N 2020-03-28 20:04:36
363 DELETE 4 \N 2 \N \N \N \N f \N 2020-03-28 20:04:41
\. --
-- Data for Name: package; Type: TABLE DATA; Schema: public; Owner: postgres
-- COPY public.package (id, name, version, description, author, user_maintainer_id, repository_id, url, source, title, active, deleted, resource_technology, binary_package, description_content_type) FROM stdin;
8 accrued 1.2 Package for visualizing data quality of partially accruing time series. Julie Eaton and Ian Painter 4 3 \N /opt/rdepot/repositories/3/83118397/accrued_1.2.tar.gz Visualization tools for partially accruing data t f R f txt
4 accrued 1.3.5 Package for visualizing data quality of partially accruing data. Julie Eaton and Ian Painter 4 3 \N /opt/rdepot/repositories/3/99077116/accrued_1.3.5.tar.gz Data Quality Visualization Tools for Partially Accruing Data t f R f txt
10 A3 0.9.2 This package supplies tools for tabulating and analyzing the results of predictive models. The methods employed are applicable to virtually any predictive model and make comparisons between different methodologies straightforward. Scott Fortmann-Roe 6 4 \N /opt/rdepot/repositories/4/54491936/A3_0.9.2.tar.gz A3: Accurate, Adaptable, and Accessible Error Metrics for Predictive Models t f R f txt
5 accrued 1.3 Package for visualizing data quality of partially accruing data. Julie Eaton and Ian Painter 4 3 \N /opt/rdepot/repositories/3/82197810/accrued_1.3.tar.gz Data Quality Visualization Tools for Partially Accruing Data t f R f txt
9 A3 0.9.1 This package supplies tools for tabulating and analyzing the results of predictive models. The methods employed are applicable to virtually any predictive model and make comparisons between different methodologies straightforward. Scott Fortmann-Roe 6 4 \N /opt/rdepot/repositories/4/47098069/A3_0.9.1.tar.gz A3: Accurate, Adaptable, and Accessible Error Metrics for Predictive Models t f R f txt
19 visdat 0.1.0 Create preliminary exploratory data visualisations of an entire dataset to identify problems or unexpected features using 'ggplot2'. Nicholas Tierney [aut, cre] 4 3 https://github.com/njtierney/visdat/ /opt/rdepot/new/70032548/visdat_0.1.0.tar.gz Preliminary Data Visualisation f f R f txt
7 accrued 1.4 Package for visualizing data quality of partially accruing data. Julie Eaton and Ian Painter 4 3 \N /opt/rdepot/repositories/3/28075835/accrued_1.4.tar.gz Data Quality Visualization Tools for Partially Accruing Data f f R f txt
14 npordtests 1.1 Performs nonparametric tests for equality of location against ordered alternatives. Bulent Altunkaynak [aut, cre], Hamza Gamgam [aut] 5 2 \N /opt/rdepot/repositories/2/8436419/npordtests_1.1.tar.gz Nonparametric Tests for Equality of Location Against Ordered Alternatives f t R f txt
17 A3 0.9.2 This package supplies tools for tabulating and analyzing the results of predictive models. The methods employed are applicable to virtually any predictive model and make comparisons between different methodologies straightforward. Scott Fortmann-Roe 5 2 \N /opt/rdepot/repositories/2/9907084/A3_0.9.2.tar.gz A3: Accurate, Adaptable, and Accessible Error Metrics for Predictive Models t f R f txt
13 accrued 1.0 Package for visualizing data quality of partially accruing time series. Julie Eaton and Ian Painter 6 2 \N /opt/rdepot/repositories/2/40553654/accrued_1.0.tar.gz Visualization tools for partially accruing data f f R f txt
11 abc 1.3 The package implements several ABC algorithms for performing parameter estimation and model selection. Cross-validation tools are also available for measuring the accuracy of ABC estimates, and to calculate the misclassification probabilities of different models. Katalin Csillery, Michael Blum and Olivier Francois 6 4 \N /opt/rdepot/repositories/4/95296712/abc_1.3.tar.gz Tools for Approximate Bayesian Computation (ABC) f f R f txt
12 abc 1.0 The 'abc' package provides various functions for parameter estimation and model selection in an ABC framework. Three main Katalin Csillery, with contributions from Michael Blum and Olivier Francois 6 4 \N /opt/rdepot/repositories/4/49426769/abc_1.0.tar.gz Functions to perform Approximate Bayesian Computation (ABC) using simulated data t f R f txt
18 abc 1.3 The package implements several ABC algorithms for performing parameter estimation and model selection. Cross-validation tools are also available for measuring the accuracy of ABC estimates, and to calculate the misclassification probabilities of different models. Katalin Csillery, Michael Blum and Olivier Francois 5 2 \N /opt/rdepot/repositories/2/88170013/abc_1.3.tar.gz Tools for Approximate Bayesian Computation (ABC) t f R f txt
20 AnaCoDa 0.1.2.3 Is a collection of models to analyze genome scale codon data using a Bayesian framework. Provides visualization routines and checkpointing for model fittings. Currently published models to analyze gene data for selection on codon Cedric Landerer [aut, cre], Gabriel Hanas [ctb], Jeremy Rogers [ctb], Alex Cope [ctb], Denizhan Pak [ctb] 5 5 https://github.com/clandere/AnaCoDa /opt/rdepot/repositories/5/39437028/AnaCoDa_0.1.2.3.tar.gz Analysis of Codon Data under Stationarity using a Bayesian Framework t f R f txt
6 accrued 1.1 Package for visualizing data quality of partially accruing time series. Julie Eaton and Ian Painter 4 3 \N /opt/rdepot/repositories/3/46950998/accrued_1.1.tar.gz Visualization tools for partially accruing data f t R f txt
22 AnaCoDa 0.1.2.3 Is a collection of models to analyze genome scale codon data using a Bayesian framework. Provides visualization routines and checkpointing for model fittings. Currently published models to analyze gene data for selection on codon Cedric Landerer [aut, cre], Gabriel Hanas [ctb], Jeremy Rogers [ctb], Alex Cope [ctb], Denizhan Pak [ctb] 4 2 https://github.com/clandere/AnaCoDa Analysis of Codon Data under Stationarity using a Bayesian Framework f f R f txt
26 usl 2.0.0 The Universal Scalability Law (Gunther 2007) Neil J. Gunther [aut], Stefan Moeding [aut, cre] 4 3 \N /opt/rdepot/new/54345476/usl_2.0.0.tar.gz Analyze System Scalability with the Universal Scalability Law f f R f txt
25 usl 2.0.0 The Universal Scalability Law (Gunther 2007) Neil J. Gunther [aut], Stefan Moeding [aut, cre] 5 2 \N /opt/rdepot/repositories/2/33930690/usl_2.0.0.tar.gz Analyze System Scalability with the Universal Scalability Law t f R f txt
16 A3 0.9.1 This package supplies tools for tabulating and analyzing the results of predictive models. The methods employed are applicable to virtually any predictive model and make comparisons between different methodologies straightforward. Scott Fortmann-Roe 4 2 \N A3: Accurate, Adaptable, and Accessible Error Metrics for Predictive Models f f R f txt
31 abc 1.0 The 'abc' package provides various functions for parameter estimation and model selection in an ABC framework. Three main Katalin Csillery, with contributions from Michael Blum and Olivier Francois 8 5 \N /opt/rdepot/new/51328701/abc_1.0.tar.gz Functions to perform Approximate Bayesian Computation (ABC) using simulated data f f R f txt
21 Benchmarking 0.10 Estimates and graphs deterministic (DEA) frontier models with different technology assumptions (fdh, vrs, drs, crs, irs, add). Also handles possible slacks, peers and their weights (lambdas), optimal cost, revenue and profit allocation, super--efficiency, and mergers. A comparative method for estimating SFA efficiencies is included. Peter Bogetoft and Lars Otto 5 2 \N /opt/rdepot/repositories/2/71228208/Benchmarking_0.10.tar.gz Benchmark and frontier analysis using DEA and SFA t f R f txt
23 visdat 0.1.0 Create preliminary exploratory data visualisations of an entire dataset to identify problems or unexpected features using 'ggplot2'. Nicholas Tierney [aut, cre] 4 2 https://github.com/njtierney/visdat/ Preliminary Data Visualisation f f R f txt
24 Benchmarking 0.10 Estimates and graphs deterministic (DEA) frontier models with different technology assumptions (fdh, vrs, drs, crs, irs, add). Also handles possible slacks, peers and their weights (lambdas), optimal cost, revenue and profit allocation, super--efficiency, and mergers. A comparative method for estimating SFA efficiencies is included. Peter Bogetoft and Lars Otto 4 5 \N Benchmark and frontier analysis using DEA and SFA f f R f txt
30 A3 0.9.1 This package supplies tools for tabulating and analyzing the results of predictive models. The methods employed are applicable to virtually any predictive model and make comparisons between different methodologies straightforward. Scott Fortmann-Roe 8 5 \N /opt/rdepot/new/92253304/A3_0.9.1.tar.gz A3: Accurate, Adaptable, and Accessible Error Metrics for Predictive Models f f R f txt
28 visdat 0.1.0 Create preliminary exploratory data visualisations of an entire dataset to identify problems or unexpected features using 'ggplot2'. Nicholas Tierney [aut, cre] 8 6 https://github.com/njtierney/visdat/ /opt/rdepot/repositories/6/70325377/visdat_0.1.0.tar.gz Preliminary Data Visualisation f t R f txt
27 usl 2.0.0 The Universal Scalability Law (Gunther 2007) Neil J. Gunther [aut], Stefan Moeding [aut, cre] 8 6 \N /opt/rdepot/repositories/6/21695389/usl_2.0.0.tar.gz Analyze System Scalability with the Universal Scalability Law f t R f txt
15 bea.R 1.0.5 Provides an R interface for the Bureau of Economic Analysis (BEA) Andrea Batch [aut, cre], Jeff Chen [ctb], Walt Kampas [ctb] 5 2 https://github.com/us-bea/beaR /opt/rdepot/repositories/2/89565416/bea.R_1.0.5.tar.gz Bureau of Economic Analysis API t f R f txt
29 A3 0.9.1 This package supplies tools for tabulating and analyzing the results of predictive models. The methods employed are applicable to virtually any predictive model and make comparisons between different methodologies straightforward. Scott Fortmann-Roe 8 7 \N /opt/rdepot/repositories/7/67484296/A3_0.9.1.tar.gz A3: Accurate, Adaptable, and Accessible Error Metrics for Predictive Models f t R f txt
\. --
-- Data for Name: package_maintainer; Type: TABLE DATA; Schema: public; Owner: postgres
-- COPY public.package_maintainer (id, user_id, package, repository_id, deleted) FROM stdin;
1 6 accrued 2 f
2 6 abc 4 f
3 6 A3 4 f
4 6 bea.R 2 t
\. --
-- Data for Name: pythonpackage; Type: TABLE DATA; Schema: public; Owner: postgres
-- COPY public.pythonpackage (id, license, author_email, classifier, home_page, keywords, maintainer, maintainer_email, platform, project_url, provides_extra, requires_dist, requires_external, requires_python, summary, hash, normalized_name) FROM stdin;
\. --
-- Data for Name: pythonrepository; Type: TABLE DATA; Schema: public; Owner: postgres
-- COPY public.pythonrepository (id, hash_method) FROM stdin;
\. --
-- Data for Name: repository; Type: TABLE DATA; Schema: public; Owner: postgres
-- COPY public.repository (version, id, publication_uri, name, server_address, published, deleted, resource_technology, last_publication_successful, last_modified_timestamp, last_publication_timestamp, requires_authentication) FROM stdin;
8 3 http://localhost/repo/testrepo2 testrepo2 http://oa-rdepot-repo:8080/testrepo2 t f R f 2020-03-28 20:09:48.128 \N t
6 5 http://localhost/repo/testrepo4 testrepo4 http://oa-rdepot-repo:8080/testrepo4 f f R f 2020-03-28 20:14:45.738 \N t
18 4 http://localhost/repo/testrepo3 testrepo3 http://oa-rdepot-repo:8080/testrepo3 f f R f 2020-03-28 20:09:15.761 \N t
9 6 http://localhost/repo/testrepo5 testrepo5 http://oa-rdepot-repo:8080/testrepo5 f t R f 2020-03-28 20:13:30.795 \N t
31 2 http://localhost/repo/testrepo1 testrepo1 http://oa-rdepot-repo:8080/testrepo1 t f R f 2020-03-29 10:43:10.123 \N t
6 7 http://localhost/repo/testrepo6 testrepo6 http://oa-rdepot-repo:8080/testrepo6 f t R f 2020-03-28 20:14:19.412 \N t
\. --
-- Data for Name: repository_maintainer; Type: TABLE DATA; Schema: public; Owner: postgres
-- COPY public.repository_maintainer (id, user_id, repository_id, deleted) FROM stdin;
1 5 2 f
3 5 5 f
2 5 4 t
\. --
-- Data for Name: role; Type: TABLE DATA; Schema: public; Owner: postgres
-- COPY public.role (id, value, name, description, deleted) FROM stdin;
2 1 packagemaintainer Package Maintainer f
3 2 repositorymaintainer Repository Maintainer f
4 3 admin Administrator f
1 0 user User f
\. --
-- Data for Name: rpackage; Type: TABLE DATA; Schema: public; Owner: postgres
-- COPY public.rpackage (id, depends, imports, suggests, system_requirements, license, md5sum, r_version, architecture, distribution, built, enhances, linking_to, priority, needs_compilation, maintainer) FROM stdin;
8 R (>= 3.0), grid \N \N \N GPL-3 70d295115295a4718593f6a39d77add9 \N \N \N \N \N \N \N f	\N
4 R (>= 2.14.1), grid \N \N \N GPL-3 19f8aec67250bd2ac481b14b50413d03 \N \N \N \N \N \N \N f	\N
10 R (>= 2.15.0), xtable, pbapply \N randomForest, e1071 \N GPL (>= 2) 76d726aee8dd7c6ed94d150d5718015b \N \N \N \N \N \N \N f	\N
5 R (>= 2.14.1), grid \N \N \N GPL-3 a05e4ca44438c0d9e7d713d7e3890423 \N \N \N \N \N \N \N f	\N
9 xtable, pbapply \N randomForest, e1071 \N GPL (>= 2) 8eb4760cd574f5489e61221dc9bb0076 \N \N \N \N \N \N \N f	\N
19 R (>= 3.2.2) ggplot2, tidyr, dplyr, purrr, magrittr, stats plotly (>= 4.5.6), testthat, knitr, rmarkdown, vdiffr \N MIT + file LICENSE f343fa3a01dcd9546fa0947877f58f36 \N \N \N \N \N \N \N f	\N
7 R (>= 2.14.1), grid \N \N \N GPL-3 97c2930a9dd7ca9fc1409d5340c06470 \N \N \N \N \N \N \N f	\N
14 R (>= 2.15.0) \N \N \N GPL (>= 2) da8be1247d3145b757bd62e01fc6eb8b \N \N \N \N \N \N \N f	\N
17 R (>= 2.15.0), xtable, pbapply \N randomForest, e1071 \N GPL (>= 2) 76d726aee8dd7c6ed94d150d5718015b \N \N \N \N \N \N \N f	\N
13 R (>= 3.0), grid \N \N \N GPL-3 1c75d59b18e554a285a9b156a06a288c \N \N \N \N \N \N \N f	\N
11 R (>= 2.10), nnet, quantreg, locfit \N \N \N GPL (>= 3) c47d18b86b331a5023dcd62b74fedbb6 \N \N \N \N \N \N \N f	\N
12 R (>= 1.8.0), nnet, quantreg, locfit, methods \N \N \N Unlimited 91599204c92275ed4b36d55e8d7c144b \N \N \N \N \N \N \N f	\N
18 R (>= 2.10), nnet, quantreg, locfit \N \N \N GPL (>= 3) c47d18b86b331a5023dcd62b74fedbb6 \N \N \N \N \N \N \N f	\N
20 R (>= 3.3.0), Rcpp (>= 0.11.3), methods knitr, Hmisc, VGAM, coda, testthat, lmodel2 \N GPL (>= 2) 41026e4157a0b3b6d909f0c6f72fa65c \N \N \N \N \N \N \N f	\N
6 R (>= 3.0), grid \N \N \N GPL-3 24b8cec280424dfc6a9e444fa57ba9f3 \N \N \N \N \N \N \N f	\N
22 R (>= 3.3.0), Rcpp (>= 0.11.3), methods knitr, Hmisc, VGAM, coda, testthat, lmodel2 \N GPL (>= 2) 41026e4157a0b3b6d909f0c6f72fa65c \N \N \N \N \N \N \N f	\N
26 R (>= 3.0), methods graphics, stats, nlsr knitr \N BSD_2_clause + file LICENSE 868140a3c3c29327eef5d5a485aee5b6 \N \N \N \N \N \N \N f	\N
25 R (>= 3.0), methods graphics, stats, nlsr knitr \N BSD_2_clause + file LICENSE 868140a3c3c29327eef5d5a485aee5b6 \N \N \N \N \N \N \N f	\N
16 xtable, pbapply \N randomForest, e1071 \N GPL (>= 2) 8eb4760cd574f5489e61221dc9bb0076 \N \N \N \N \N \N \N f	\N
21 lpSolveAPI, ucminf \N \N \N GPL (>= 2) 9a99c2ebefa6d49422ca7893c1f4ead8 \N \N \N \N \N \N \N f	\N
23 R (>= 3.2.2) ggplot2, tidyr, dplyr, purrr, magrittr, stats plotly (>= 4.5.6), testthat, knitr, rmarkdown, vdiffr \N MIT + file LICENSE f343fa3a01dcd9546fa0947877f58f36 \N \N \N \N \N \N \N f	\N
24 lpSolveAPI, ucminf \N \N \N GPL (>= 2) 9a99c2ebefa6d49422ca7893c1f4ead8 \N \N \N \N \N \N \N f	\N
30 xtable, pbapply \N randomForest, e1071 \N GPL (>= 2) 8eb4760cd574f5489e61221dc9bb0076 \N \N \N \N \N \N \N f	\N
31 R (>= 1.8.0), nnet, quantreg, locfit, methods \N \N \N Unlimited 91599204c92275ed4b36d55e8d7c144b \N \N \N \N \N \N \N f	\N
28 R (>= 3.2.2) ggplot2, tidyr, dplyr, purrr, magrittr, stats plotly (>= 4.5.6), testthat, knitr, rmarkdown, vdiffr \N MIT + file LICENSE f343fa3a01dcd9546fa0947877f58f36 \N \N \N \N \N \N \N f	\N
27 R (>= 3.0), methods graphics, stats, nlsr knitr \N BSD_2_clause + file LICENSE 868140a3c3c29327eef5d5a485aee5b6 \N \N \N \N \N \N \N f	\N
29 xtable, pbapply \N randomForest, e1071 \N GPL (>= 2) 8eb4760cd574f5489e61221dc9bb0076 \N \N \N \N \N \N \N f	\N
15 R (>= 3.2.1), data.table httr, DT, shiny, jsonlite, googleVis, shinydashboard, ggplot2, stringr, chron, gtable, scales, htmltools, httpuv, xtable, stringi, magrittr, htmlwidgets, Rcpp, munsell, colorspace, plyr, yaml \N \N CC0 5e664f320c7cc884138d64467f6b0e49 \N \N \N \N \N \N \N f	\N
\. --
-- Data for Name: rrepository; Type: TABLE DATA; Schema: public; Owner: postgres
-- COPY public.rrepository (id, redirect_to_source) FROM stdin;
5 f
4 f
3 f
6 f
7 f
2 f
\. --
-- Data for Name: submission; Type: TABLE DATA; Schema: public; Owner: postgres
-- COPY public.submission (id, submitter_id, package_id, changes, deleted, state, approver_id) FROM stdin;
26 5 26 \N f WAITING \N
19 7 19 \N f WAITING \N
31 6 31 \N f WAITING \N
30 6 30 \N f WAITING \N
29 4 29 \N t ACCEPTED 4
4 4 6 \N t ACCEPTED 4
10 4 10 \N f ACCEPTED 4
9 4 9 \N f ACCEPTED 4
7 4 5 \N f ACCEPTED 4
15 4 14 \N t ACCEPTED 4
6 4 8 \N f ACCEPTED 4
12 4 12 \N f ACCEPTED 4
24 6 24 \N t REJECTED 5
25 5 25 \N f ACCEPTED 5
21 6 21 \N f ACCEPTED 5
14 4 13 \N f ACCEPTED 4
17 7 17 \N f ACCEPTED 5
28 4 28 \N t ACCEPTED 4
22 6 23 \N t REJECTED 5
20 7 20 \N f ACCEPTED 5
13 4 15 \N f ACCEPTED 4
5 4 4 \N f ACCEPTED 4
18 7 18 \N f ACCEPTED 5
16 7 16 \N t REJECTED 5
27 4 27 \N t ACCEPTED 4
23 6 22 \N t CANCELLED 6
11 4 11 \N f ACCEPTED 4
8 4 7 \N f ACCEPTED 4
\. --
-- Data for Name: user; Type: TABLE DATA; Schema: public; Owner: postgres
-- COPY public."user" (id, role_id, name, email, login, active, last_logged_in_on, deleted, created_on) FROM stdin;
8 4 Local Admin User admin@localhost admin f \N f 1970-01-01
7 1 Isaac Newton newton@ldap.forumsys.com newton t 2020-03-28 00:00:00 f 1970-01-01
6 2 Galileo Galilei galieleo@ldap.forumsys.com galieleo t 2020-03-28 00:00:00 f 1970-01-01
5 3 Nikola Tesla tesla@ldap.forumsys.com tesla t 2020-03-29 00:00:00 f 1970-01-01
4 4 Albert Einstein einstein@ldap.forumsys.com einstein t 2020-08-20 00:00:00 f 1970-01-01
9 1 John Doe doe@localhost doe f 2020-08-20 00:00:00 f 1970-01-01
10 1 Alfred Tarski tarski@localhost tarski t 2020-08-25 00:00:00 f 1970-01-01
\. --
-- Data for Name: user_settings; Type: TABLE DATA; Schema: public; Owner: postgres
-- COPY public.user_settings (id, deleted, language, theme, page_size, user_id) FROM stdin;
\. --
-- Name: Api_token_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
-- SELECT pg_catalog.setval('public."Api_token_id_seq"', 5, true); --
-- Name: Event_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
-- SELECT pg_catalog.setval('public."Event_id_seq"', 3, true); --
-- Name: PackageEvent_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
-- SELECT pg_catalog.setval('public."PackageEvent_id_seq"', 103, true); --
-- Name: PackageMaintainerEvent_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
-- SELECT pg_catalog.setval('public."PackageMaintainerEvent_id_seq"', 5, true); --
-- Name: PackageMaintainer_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
-- SELECT pg_catalog.setval('public."PackageMaintainer_id_seq"', 4, true); --
-- Name: Package_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
-- SELECT pg_catalog.setval('public."Package_id_seq"', 31, true); --
-- Name: RepositoryEvent_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
-- SELECT pg_catalog.setval('public."RepositoryEvent_id_seq"', 186, true); --
-- Name: RepositoryMaintainerEvent_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
-- SELECT pg_catalog.setval('public."RepositoryMaintainerEvent_id_seq"', 4, true); --
-- Name: RepositoryMaintainer_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
-- SELECT pg_catalog.setval('public."RepositoryMaintainer_id_seq"', 3, true); --
-- Name: Repository_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
-- SELECT pg_catalog.setval('public."Repository_id_seq"', 7, true); --
-- Name: Role_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
-- SELECT pg_catalog.setval('public."Role_id_seq"', 4, true); --
-- Name: SubmissionEvent_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
-- SELECT pg_catalog.setval('public."SubmissionEvent_id_seq"', 60, true); --
-- Name: Submission_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
-- SELECT pg_catalog.setval('public."Submission_id_seq"', 31, true); --
-- Name: UserEvent_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
-- SELECT pg_catalog.setval('public."UserEvent_id_seq"', 24, true); --
-- Name: User_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
-- SELECT pg_catalog.setval('public."User_id_seq"', 10, true); --
-- Name: access_token_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
-- SELECT pg_catalog.setval('public.access_token_id_seq', 1, false); --
-- Name: changed_variable_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
-- SELECT pg_catalog.setval('public.changed_variable_id_seq', 363, true); --
-- Name: newsfeed_event_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
-- SELECT pg_catalog.setval('public.newsfeed_event_id_seq', 363, true); --
-- Name: user_settings_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
-- SELECT pg_catalog.setval('public.user_settings_id_seq', 1, false); --
-- Name: api_token Api_token_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
-- ALTER TABLE ONLY public.api_token ADD CONSTRAINT "Api_token_pkey" PRIMARY KEY (id); --
-- Name: api_token Api_token_token_key; Type: CONSTRAINT; Schema: public; Owner: postgres
-- ALTER TABLE ONLY public.api_token ADD CONSTRAINT "Api_token_token_key" UNIQUE (token); --
-- Name: api_token Api_token_user_login_key; Type: CONSTRAINT; Schema: public; Owner: postgres
-- ALTER TABLE ONLY public.api_token ADD CONSTRAINT "Api_token_user_login_key" UNIQUE (user_login); --
-- Name: package_maintainer PackageMaintainer_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
-- ALTER TABLE ONLY public.package_maintainer ADD CONSTRAINT "PackageMaintainer_pkey" PRIMARY KEY (id); --
-- Name: package Package_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
-- ALTER TABLE ONLY public.package ADD CONSTRAINT "Package_pkey" PRIMARY KEY (id); --
-- Name: repository_maintainer RepositoryMaintainer_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
-- ALTER TABLE ONLY public.repository_maintainer ADD CONSTRAINT "RepositoryMaintainer_pkey" PRIMARY KEY (id); --
-- Name: repository Repository_name_key; Type: CONSTRAINT; Schema: public; Owner: postgres
-- ALTER TABLE ONLY public.repository ADD CONSTRAINT "Repository_name_key" UNIQUE (name); --
-- Name: repository Repository_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
-- ALTER TABLE ONLY public.repository ADD CONSTRAINT "Repository_pkey" PRIMARY KEY (id); --
-- Name: repository Repository_publication_uri_key; Type: CONSTRAINT; Schema: public; Owner: postgres
-- ALTER TABLE ONLY public.repository ADD CONSTRAINT "Repository_publication_uri_key" UNIQUE (publication_uri); --
-- Name: role Role_description_key; Type: CONSTRAINT; Schema: public; Owner: postgres
-- ALTER TABLE ONLY public.role ADD CONSTRAINT "Role_description_key" UNIQUE (description); --
-- Name: role Role_name_key; Type: CONSTRAINT; Schema: public; Owner: postgres
-- ALTER TABLE ONLY public.role ADD CONSTRAINT "Role_name_key" UNIQUE (name); --
-- Name: role Role_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
-- ALTER TABLE ONLY public.role ADD CONSTRAINT "Role_pkey" PRIMARY KEY (id); --
-- Name: role Role_value_key; Type: CONSTRAINT; Schema: public; Owner: postgres
-- ALTER TABLE ONLY public.role ADD CONSTRAINT "Role_value_key" UNIQUE (value); --
-- Name: submission Submission_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
-- ALTER TABLE ONLY public.submission ADD CONSTRAINT "Submission_pkey" PRIMARY KEY (id); --
-- Name: user User_email_key; Type: CONSTRAINT; Schema: public; Owner: postgres
-- ALTER TABLE ONLY public."user" ADD CONSTRAINT "User_email_key" UNIQUE (email); --
-- Name: user User_login_key; Type: CONSTRAINT; Schema: public; Owner: postgres
-- ALTER TABLE ONLY public."user" ADD CONSTRAINT "User_login_key" UNIQUE (login); --
-- Name: user User_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
-- ALTER TABLE ONLY public."user" ADD CONSTRAINT "User_pkey" PRIMARY KEY (id); --
-- Name: access_token access_token_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
-- ALTER TABLE ONLY public.access_token ADD CONSTRAINT access_token_pkey PRIMARY KEY (id); --
-- Name: changed_variable changed_variable_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
-- ALTER TABLE ONLY public.changed_variable ADD CONSTRAINT changed_variable_pkey PRIMARY KEY (id); --
-- Name: databasechangeloglock databasechangeloglock_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
-- ALTER TABLE ONLY public.databasechangeloglock ADD CONSTRAINT databasechangeloglock_pkey PRIMARY KEY (id); --
-- Name: newsfeed_event newsfeed_event_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
-- ALTER TABLE ONLY public.newsfeed_event ADD CONSTRAINT newsfeed_event_pkey PRIMARY KEY (id); --
-- Name: pythonpackage pythonpackage_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
-- ALTER TABLE ONLY public.pythonpackage ADD CONSTRAINT pythonpackage_pkey PRIMARY KEY (id); --
-- Name: pythonrepository pythonrepository_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
-- ALTER TABLE ONLY public.pythonrepository ADD CONSTRAINT pythonrepository_pkey PRIMARY KEY (id); --
-- Name: rpackage rpackage_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
-- ALTER TABLE ONLY public.rpackage ADD CONSTRAINT rpackage_pkey PRIMARY KEY (id); --
-- Name: rrepository rrepository_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
-- ALTER TABLE ONLY public.rrepository ADD CONSTRAINT rrepository_pkey PRIMARY KEY (id); --
-- Name: user_settings user_settings_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
-- ALTER TABLE ONLY public.user_settings ADD CONSTRAINT user_settings_pkey PRIMARY KEY (id); --
-- Name: user_settings user_settings_user_id_key; Type: CONSTRAINT; Schema: public; Owner: postgres
-- ALTER TABLE ONLY public.user_settings ADD CONSTRAINT user_settings_user_id_key UNIQUE (user_id); --
-- Name: submission approved_by; Type: FK CONSTRAINT; Schema: public; Owner: postgres
-- ALTER TABLE ONLY public.submission ADD CONSTRAINT approved_by FOREIGN KEY (approver_id) REFERENCES public."user"(id); --
-- Name: newsfeed_event author; Type: FK CONSTRAINT; Schema: public; Owner: postgres
-- ALTER TABLE ONLY public.newsfeed_event ADD CONSTRAINT author FOREIGN KEY (author_id) REFERENCES public."user"(id); --
-- Name: submission for_package; Type: FK CONSTRAINT; Schema: public; Owner: postgres
-- ALTER TABLE ONLY public.submission ADD CONSTRAINT for_package FOREIGN KEY (package_id) REFERENCES public.package(id); --
-- Name: package for_repository; Type: FK CONSTRAINT; Schema: public; Owner: postgres
-- ALTER TABLE ONLY public.package ADD CONSTRAINT for_repository FOREIGN KEY (repository_id) REFERENCES public.repository(id); --
-- Name: submission from_user; Type: FK CONSTRAINT; Schema: public; Owner: postgres
-- ALTER TABLE ONLY public.submission ADD CONSTRAINT from_user FOREIGN KEY (submitter_id) REFERENCES public."user"(id); --
-- Name: user has_role; Type: FK CONSTRAINT; Schema: public; Owner: postgres
-- ALTER TABLE ONLY public."user" ADD CONSTRAINT has_role FOREIGN KEY (role_id) REFERENCES public.role(id); --
-- Name: package is_maintainer_of; Type: FK CONSTRAINT; Schema: public; Owner: postgres
-- ALTER TABLE ONLY public.package ADD CONSTRAINT is_maintainer_of FOREIGN KEY (user_maintainer_id) REFERENCES public."user"(id); --
-- Name: package_maintainer is_package_maintainer; Type: FK CONSTRAINT; Schema: public; Owner: postgres
-- ALTER TABLE ONLY public.package_maintainer ADD CONSTRAINT is_package_maintainer FOREIGN KEY (user_id) REFERENCES public."user"(id); --
-- Name: package_maintainer is_package_maintainer_of; Type: FK CONSTRAINT; Schema: public; Owner: postgres
-- ALTER TABLE ONLY public.package_maintainer ADD CONSTRAINT is_package_maintainer_of FOREIGN KEY (repository_id) REFERENCES public.repository(id); --
-- Name: repository_maintainer is_repository_maintainer; Type: FK CONSTRAINT; Schema: public; Owner: postgres
-- ALTER TABLE ONLY public.repository_maintainer ADD CONSTRAINT is_repository_maintainer FOREIGN KEY (user_id) REFERENCES public."user"(id); --
-- Name: changed_variable newsfeed_event; Type: FK CONSTRAINT; Schema: public; Owner: postgres
-- ALTER TABLE ONLY public.changed_variable ADD CONSTRAINT newsfeed_event FOREIGN KEY (newsfeed_event_id) REFERENCES public.newsfeed_event(id) ON DELETE CASCADE; --
-- Name: repository_maintainer of_repository; Type: FK CONSTRAINT; Schema: public; Owner: postgres
-- ALTER TABLE ONLY public.repository_maintainer ADD CONSTRAINT of_repository FOREIGN KEY (repository_id) REFERENCES public.repository(id); --
-- Name: user_settings of_user; Type: FK CONSTRAINT; Schema: public; Owner: postgres
-- ALTER TABLE ONLY public.user_settings ADD CONSTRAINT of_user FOREIGN KEY (user_id) REFERENCES public."user"(id); --
-- Name: access_token of_user; Type: FK CONSTRAINT; Schema: public; Owner: postgres
-- ALTER TABLE ONLY public.access_token ADD CONSTRAINT of_user FOREIGN KEY (user_id) REFERENCES public."user"(id); --
-- Name: newsfeed_event related_package; Type: FK CONSTRAINT; Schema: public; Owner: postgres
-- ALTER TABLE ONLY public.newsfeed_event ADD CONSTRAINT related_package FOREIGN KEY (related_package_id) REFERENCES public.package(id); --
-- Name: newsfeed_event related_packagemaintainer; Type: FK CONSTRAINT; Schema: public; Owner: postgres
-- ALTER TABLE ONLY public.newsfeed_event ADD CONSTRAINT related_packagemaintainer FOREIGN KEY (related_packagemaintainer_id) REFERENCES public.package_maintainer(id); --
-- Name: newsfeed_event related_repository; Type: FK CONSTRAINT; Schema: public; Owner: postgres
-- ALTER TABLE ONLY public.newsfeed_event ADD CONSTRAINT related_repository FOREIGN KEY (related_repository_id) REFERENCES public.repository(id); --
-- Name: newsfeed_event related_repositorymaintainer; Type: FK CONSTRAINT; Schema: public; Owner: postgres
-- ALTER TABLE ONLY public.newsfeed_event ADD CONSTRAINT related_repositorymaintainer FOREIGN KEY (related_repositorymaintainer_id) REFERENCES public.repository_maintainer(id); --
-- Name: newsfeed_event related_submission; Type: FK CONSTRAINT; Schema: public; Owner: postgres
-- ALTER TABLE ONLY public.newsfeed_event ADD CONSTRAINT related_submission FOREIGN KEY (related_submission_id) REFERENCES public.submission(id); --
-- Name: newsfeed_event related_user; Type: FK CONSTRAINT; Schema: public; Owner: postgres
-- ALTER TABLE ONLY public.newsfeed_event ADD CONSTRAINT related_user FOREIGN KEY (related_user_id) REFERENCES public."user"(id); --
-- PostgreSQL database dump complete
--
