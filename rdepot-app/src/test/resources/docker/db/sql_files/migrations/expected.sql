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
-- CREATE TABLE public.package ( id integer NOT NULL, name text NOT NULL, version text NOT NULL, description text, author text, user_maintainer_id integer NOT NULL, repository_id integer NOT NULL, url text, source text NOT NULL, title text, active boolean DEFAULT false NOT NULL, deleted boolean DEFAULT false NOT NULL, resource_technology text NOT NULL, binary_package boolean DEFAULT false NOT NULL
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
-- CREATE TABLE public.repository ( version integer DEFAULT 0 NOT NULL, id integer NOT NULL, publication_uri text NOT NULL, name text NOT NULL, server_address text NOT NULL, published boolean DEFAULT false NOT NULL, deleted boolean DEFAULT false NOT NULL, resource_technology text NOT NULL, last_publication_successful boolean DEFAULT false NOT NULL, last_modified_timestamp timestamp without time zone DEFAULT now() NOT NULL, last_publication_timestamp timestamp without time zone
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
-- CREATE TABLE public."user" ( id integer NOT NULL, role_id integer DEFAULT 4 NOT NULL, name text NOT NULL, email text NOT NULL, login text NOT NULL, active boolean NOT NULL, last_logged_in_on date, deleted boolean DEFAULT false NOT NULL, created_on date DEFAULT now() NOT NULL
); ALTER TABLE public."user" OWNER TO postgres; --
-- Name: User_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
-- CREATE SEQUENCE public."User_id_seq" START WITH 1 INCREMENT BY 1 NO MINVALUE NO MAXVALUE CACHE 1; ALTER SEQUENCE public."User_id_seq" OWNER TO postgres; --
-- Name: User_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
-- ALTER SEQUENCE public."User_id_seq" OWNED BY public."user".id; --
-- Name: access_token; Type: TABLE; Schema: public; Owner: postgres
-- CREATE TABLE public.access_token ( id integer NOT NULL, user_id integer NOT NULL, name text NOT NULL, value text NOT NULL, creation_date date DEFAULT CURRENT_DATE NOT NULL, expiration_date date NOT NULL, active boolean DEFAULT true, deleted boolean DEFAULT false
); ALTER TABLE public.access_token OWNER TO postgres; --
-- Name: access_token_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
-- ALTER TABLE public.access_token ALTER COLUMN id ADD GENERATED BY DEFAULT AS IDENTITY ( SEQUENCE NAME public.access_token_id_seq START WITH 1 INCREMENT BY 1 NO MINVALUE NO MAXVALUE CACHE 1
); --
-- Name: changed_variable; Type: TABLE; Schema: public; Owner: postgres
-- CREATE TABLE public.changed_variable ( id integer NOT NULL, changed_variable character varying(16) NOT NULL, value_before text NOT NULL, value_after text NOT NULL, newsfeed_event_id integer NOT NULL, deleted boolean DEFAULT false NOT NULL
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
-- CREATE TABLE public.newsfeed_event ( id integer NOT NULL, "time" time with time zone DEFAULT now() NOT NULL, date date DEFAULT now() NOT NULL, newsfeed_event_type character varying(16) NOT NULL, author_id integer, related_packagemaintainer_id integer, related_repositorymaintainer_id integer, related_user_id integer, related_submission_id integer, related_repository_id integer, related_package_id integer, deleted boolean DEFAULT false NOT NULL, related_accesstoken_id integer
); ALTER TABLE public.newsfeed_event OWNER TO postgres; --
-- Name: newsfeed_event_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
-- ALTER TABLE public.newsfeed_event ALTER COLUMN id ADD GENERATED BY DEFAULT AS IDENTITY ( SEQUENCE NAME public.newsfeed_event_id_seq START WITH 1 INCREMENT BY 1 NO MINVALUE NO MAXVALUE CACHE 1
); --
-- Name: pythonpackage; Type: TABLE; Schema: public; Owner: postgres
-- CREATE TABLE public.pythonpackage ( id integer NOT NULL, license text NOT NULL, author_email text, classifier text, description_content_type text, home_page text, keywords text, maintainer text, maintainer_email text, platform text, project_url text, provides_extra text, requires_dist text, requires_external text, requires_python text, summary text, hash text NOT NULL
); ALTER TABLE public.pythonpackage OWNER TO postgres; --
-- Name: pythonrepository; Type: TABLE; Schema: public; Owner: postgres
-- CREATE TABLE public.pythonrepository ( id integer NOT NULL, hash_method text DEFAULT 'SHA256'::text
); ALTER TABLE public.pythonrepository OWNER TO postgres; --
-- Name: rpackage; Type: TABLE; Schema: public; Owner: postgres
-- CREATE TABLE public.rpackage ( id integer NOT NULL, depends text, imports text, suggests text, system_requirements text, license text, md5sum text, r_version text, architecture text, distribution text, built text, enhances text, linking_to text, priority text, needs_compilation boolean DEFAULT false NOT NULL
); ALTER TABLE public.rpackage OWNER TO postgres; --
-- Name: rrepository; Type: TABLE; Schema: public; Owner: postgres
-- CREATE TABLE public.rrepository ( id integer NOT NULL
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
-- COPY public.access_token (id, user_id, name, value, creation_date, expiration_date, active, deleted) FROM stdin;
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
-- COPY public.newsfeed_event (id, "time", date, newsfeed_event_type, author_id, related_packagemaintainer_id, related_repositorymaintainer_id, related_user_id, related_submission_id, related_repository_id, related_package_id, deleted, related_accesstoken_id) FROM stdin;
1 20:03:44.651+00 2020-03-28 CREATE 4 \N \N \N 4 \N \N f \N
2 20:03:44.647+00 2020-03-28 CREATE 4 \N \N \N 5 \N \N f \N
3 20:03:44.686+00 2020-03-28 CREATE 4 \N \N \N 6 \N \N f \N
4 20:03:44.733+00 2020-03-28 UPDATE 4 \N \N \N 6 \N \N f \N
5 20:03:44.717+00 2020-03-28 UPDATE 4 \N \N \N 5 \N \N f \N
6 20:03:44.725+00 2020-03-28 UPDATE 4 \N \N \N 4 \N \N f \N
7 20:03:45.18+00 2020-03-28 CREATE 4 \N \N \N 7 \N \N f \N
8 20:03:45.2+00 2020-03-28 CREATE 4 \N \N \N 8 \N \N f \N
9 20:03:45.192+00 2020-03-28 UPDATE 4 \N \N \N 7 \N \N f \N
10 20:03:45.219+00 2020-03-28 UPDATE 4 \N \N \N 8 \N \N f \N
11 20:03:58.906+00 2020-03-28 CREATE 4 \N \N \N 9 \N \N f \N
12 20:03:58.913+00 2020-03-28 CREATE 4 \N \N \N 10 \N \N f \N
13 20:03:58.928+00 2020-03-28 UPDATE 4 \N \N \N 10 \N \N f \N
14 20:03:58.922+00 2020-03-28 UPDATE 4 \N \N \N 9 \N \N f \N
15 20:03:59.593+00 2020-03-28 CREATE 4 \N \N \N 11 \N \N f \N
16 20:03:59.606+00 2020-03-28 UPDATE 4 \N \N \N 11 \N \N f \N
17 20:03:59.783+00 2020-03-28 CREATE 4 \N \N \N 12 \N \N f \N
18 20:03:59.793+00 2020-03-28 UPDATE 4 \N \N \N 12 \N \N f \N
19 20:04:17.091+00 2020-03-28 CREATE 4 \N \N \N 13 \N \N f \N
20 20:04:17.118+00 2020-03-28 UPDATE 4 \N \N \N 13 \N \N f \N
21 20:04:17.502+00 2020-03-28 CREATE 4 \N \N \N 14 \N \N f \N
22 20:04:17.51+00 2020-03-28 UPDATE 4 \N \N \N 14 \N \N f \N
23 20:04:17.604+00 2020-03-28 CREATE 4 \N \N \N 15 \N \N f \N
24 20:04:17.611+00 2020-03-28 UPDATE 4 \N \N \N 15 \N \N f \N
25 20:05:58.312+00 2020-03-28 CREATE 7 \N \N \N 16 \N \N f \N
26 20:05:58.439+00 2020-03-28 CREATE 7 \N \N \N 17 \N \N f \N
27 20:05:58.691+00 2020-03-28 CREATE 7 \N \N \N 18 \N \N f \N
28 20:06:13.346+00 2020-03-28 CREATE 7 \N \N \N 19 \N \N f \N
29 20:06:23.306+00 2020-03-28 CREATE 7 \N \N \N 20 \N \N f \N
30 20:06:48.787+00 2020-03-28 CREATE 6 \N \N \N 21 \N \N f \N
31 20:06:49.157+00 2020-03-28 CREATE 6 \N \N \N 22 \N \N f \N
32 20:06:49.563+00 2020-03-28 CREATE 6 \N \N \N 23 \N \N f \N
33 20:07:00.372+00 2020-03-28 DELETE 6 \N \N \N 23 \N \N f \N
34 20:07:13.152+00 2020-03-28 CREATE 6 \N \N \N 24 \N \N f \N
35 20:07:52.791+00 2020-03-28 CREATE 5 \N \N \N 25 \N \N f \N
36 20:07:52.85+00 2020-03-28 CREATE 5 \N \N \N 26 \N \N f \N
37 20:07:52.799+00 2020-03-28 UPDATE 5 \N \N \N 25 \N \N f \N
38 20:08:08.855+00 2020-03-28 DELETE 5 \N \N \N 16 \N \N f \N
39 20:08:12.2+00 2020-03-28 UPDATE 5 \N \N \N 17 \N \N f \N
40 20:08:18.23+00 2020-03-28 UPDATE 5 \N \N \N 18 \N \N f \N
41 20:08:23.526+00 2020-03-28 UPDATE 5 \N \N \N 21 \N \N f \N
42 20:08:31.542+00 2020-03-28 DELETE 5 \N \N \N 22 \N \N f \N
43 20:08:41.718+00 2020-03-28 UPDATE 5 \N \N \N 20 \N \N f \N
44 20:08:42.886+00 2020-03-28 DELETE 5 \N \N \N 24 \N \N f \N
45 20:09:48.106+00 2020-03-28 DELETE 4 \N \N \N 4 \N \N f \N
46 20:10:08.515+00 2020-03-28 DELETE 5 \N \N \N 15 \N \N f \N
47 20:12:44.514+00 2020-03-28 CREATE 4 \N \N \N 27 \N \N f \N
48 20:12:44.519+00 2020-03-28 UPDATE 4 \N \N \N 27 \N \N f \N
49 20:12:44.624+00 2020-03-28 CREATE 4 \N \N \N 28 \N \N f \N
50 20:12:44.629+00 2020-03-28 UPDATE 4 \N \N \N 28 \N \N f \N
51 20:13:30.635+00 2020-03-28 DELETE 4 \N \N \N 28 \N \N f \N
52 20:13:30.736+00 2020-03-28 DELETE 4 \N \N \N 27 \N \N f \N
53 20:14:06.587+00 2020-03-28 CREATE 4 \N \N \N 29 \N \N f \N
54 20:14:06.591+00 2020-03-28 UPDATE 4 \N \N \N 29 \N \N f \N
55 20:14:17.891+00 2020-03-28 DELETE 4 \N \N \N 29 \N \N f \N
56 20:14:44.964+00 2020-03-28 CREATE 6 \N \N \N 30 \N \N f \N
57 20:14:45.737+00 2020-03-28 CREATE 6 \N \N \N 31 \N \N f \N
58 20:59:28.800514+01 2020-03-28 CREATE 4 \N \N 4 \N \N \N f \N
59 20:59:28.81074+01 2020-03-28 CREATE 5 \N \N 5 \N \N \N f \N
60 20:59:28.82206+01 2020-03-28 CREATE 6 \N \N 6 \N \N \N f \N
61 20:59:28.830132+01 2020-03-28 CREATE 7 \N \N 7 \N \N \N f \N
62 20:59:28.838152+01 2020-03-28 CREATE 8 \N \N 8 \N \N \N f \N
63 20:00:14.007+00 2020-03-28 UPDATE 4 \N \N 4 \N \N \N f \N
64 20:03:28.868+00 2020-03-28 UPDATE 4 \N \N 8 \N \N \N f \N
65 20:05:47.228+00 2020-03-28 UPDATE 4 \N \N 7 \N \N \N f \N
66 20:06:31.773+00 2020-03-28 UPDATE 4 \N \N 6 \N \N \N f \N
67 20:07:31.17+00 2020-03-28 UPDATE 4 \N \N 5 \N \N \N f \N
68 20:09:02.061+00 2020-03-28 UPDATE 4 \N \N 4 \N \N \N f \N
69 20:09:56.183+00 2020-03-28 UPDATE 8 \N \N 5 \N \N \N f \N
70 20:12:06.32+00 2020-03-28 UPDATE 8 \N \N 4 \N \N \N f \N
71 20:14:30.998+00 2020-03-28 UPDATE 8 \N \N 6 \N \N \N f \N
72 10:42:45.4+00 2020-03-29 UPDATE 8 \N \N 5 \N \N \N f \N
73 09:58:51.784+00 2020-08-20 CREATE 8 \N \N 9 \N \N \N f \N
74 09:58:52.09+00 2020-08-20 UPDATE 8 \N \N 9 \N \N \N f \N
75 09:59:08.9+00 2020-08-20 UPDATE 8 \N \N 4 \N \N \N f \N
76 09:59:21.132+00 2020-08-20 UPDATE 4 \N \N 9 \N \N \N f \N
77 12:35:38.689+00 2020-08-25 CREATE 8 \N \N 10 \N \N \N f \N
78 12:35:38.788+00 2020-08-25 UPDATE 8 \N \N 10 \N \N \N f \N
79 20:03:40.652+00 2020-03-28 UPLOAD 4 \N \N \N \N \N 4 f \N
80 20:03:40.648+00 2020-03-28 UPLOAD 4 \N \N \N \N \N 6 f \N
81 20:03:40.647+00 2020-03-28 UPLOAD 4 \N \N \N \N \N 5 f \N
82 20:03:40.659+00 2020-03-28 UPLOAD 4 \N \N \N \N \N 8 f \N
83 20:03:40.657+00 2020-03-28 UPLOAD 4 \N \N \N \N \N 7 f \N
84 20:03:44.778+00 2020-03-28 UPDATE 4 \N \N \N \N \N 6 f \N
85 20:03:44.777+00 2020-03-28 UPDATE 4 \N \N \N \N \N 4 f \N
86 20:03:44.782+00 2020-03-28 UPDATE 4 \N \N \N \N \N 8 f \N
87 20:03:44.858+00 2020-03-28 UPDATE 4 \N \N \N \N \N 4 f \N
88 20:03:44.856+00 2020-03-28 UPDATE 4 \N \N \N \N \N 6 f \N
89 20:03:44.861+00 2020-03-28 UPDATE 4 \N \N \N \N \N 8 f \N
90 20:03:45.211+00 2020-03-28 UPDATE 4 \N \N \N \N \N 5 f \N
91 20:03:45.24+00 2020-03-28 UPDATE 4 \N \N \N \N \N 7 f \N
92 20:03:45.255+00 2020-03-28 UPDATE 4 \N \N \N \N \N 5 f \N
93 20:03:45.28+00 2020-03-28 UPDATE 4 \N \N \N \N \N 7 f \N
94 20:03:55.815+00 2020-03-28 UPLOAD 4 \N \N \N \N \N 9 f \N
95 20:03:55.917+00 2020-03-28 UPLOAD 4 \N \N \N \N \N 10 f \N
96 20:03:56.245+00 2020-03-28 UPLOAD 4 \N \N \N \N \N 11 f \N
97 20:03:56.284+00 2020-03-28 UPLOAD 4 \N \N \N \N \N 12 f \N
98 20:03:58.941+00 2020-03-28 UPDATE 4 \N \N \N \N \N 10 f \N
99 20:03:58.958+00 2020-03-28 UPDATE 4 \N \N \N \N \N 9 f \N
100 20:03:59.003+00 2020-03-28 UPDATE 4 \N \N \N \N \N 10 f \N
101 20:03:59.04+00 2020-03-28 UPDATE 4 \N \N \N \N \N 9 f \N
102 20:03:59.621+00 2020-03-28 UPDATE 4 \N \N \N \N \N 11 f \N
103 20:03:59.667+00 2020-03-28 UPDATE 4 \N \N \N \N \N 11 f \N
104 20:03:59.807+00 2020-03-28 UPDATE 4 \N \N \N \N \N 12 f \N
105 20:03:59.833+00 2020-03-28 UPDATE 4 \N \N \N \N \N 12 f \N
106 20:04:14.336+00 2020-03-28 UPLOAD 4 \N \N \N \N \N 13 f \N
107 20:04:14.401+00 2020-03-28 UPLOAD 4 \N \N \N \N \N 14 f \N
108 20:04:14.418+00 2020-03-28 UPLOAD 4 \N \N \N \N \N 15 f \N
109 20:04:17.13+00 2020-03-28 UPDATE 4 \N \N \N \N \N 15 f \N
110 20:04:17.191+00 2020-03-28 UPDATE 4 \N \N \N \N \N 15 f \N
111 20:04:17.518+00 2020-03-28 UPDATE 4 \N \N \N \N \N 13 f \N
112 20:04:17.533+00 2020-03-28 UPDATE 4 \N \N \N \N \N 13 f \N
113 20:04:17.62+00 2020-03-28 UPDATE 4 \N \N \N \N \N 14 f \N
114 20:04:17.642+00 2020-03-28 UPDATE 4 \N \N \N \N \N 14 f \N
115 20:04:24.947+00 2020-03-28 UPDATE 4 \N \N \N \N \N 13 f \N
116 20:04:24.962+00 2020-03-28 UPDATE 4 \N \N \N \N \N 15 f \N
117 20:04:24.974+00 2020-03-28 UPDATE 4 \N \N \N \N \N 14 f \N
118 20:04:31.785+00 2020-03-28 UPDATE 4 \N \N \N \N \N 11 f \N
119 20:04:31.795+00 2020-03-28 UPDATE 4 \N \N \N \N \N 10 f \N
120 20:04:31.804+00 2020-03-28 UPDATE 4 \N \N \N \N \N 12 f \N
121 20:04:31.812+00 2020-03-28 UPDATE 4 \N \N \N \N \N 9 f \N
122 20:04:41.444+00 2020-03-28 UPDATE 4 \N \N \N \N \N 10 f \N
123 20:04:41.458+00 2020-03-28 UPDATE 4 \N \N \N \N \N 11 f \N
124 20:04:41.475+00 2020-03-28 UPDATE 4 \N \N \N \N \N 9 f \N
125 20:04:41.491+00 2020-03-28 UPDATE 4 \N \N \N \N \N 12 f \N
126 20:04:47.46+00 2020-03-28 UPDATE 4 \N \N \N \N \N 11 f \N
127 20:04:51.62+00 2020-03-28 UPDATE 4 \N \N \N \N \N 7 f \N
128 20:04:54.588+00 2020-03-28 UPDATE 4 \N \N \N \N \N 6 f \N
129 20:04:56.593+00 2020-03-28 UPDATE 4 \N \N \N \N \N 13 f \N
130 20:05:25.637+00 2020-03-28 UPDATE 4 \N \N \N \N \N 13 f \N
131 20:05:29.974+00 2020-03-28 UPDATE 4 \N \N \N \N \N 12 f \N
132 20:05:29.982+00 2020-03-28 UPDATE 4 \N \N \N \N \N 11 f \N
133 20:05:35.464+00 2020-03-28 UPDATE 4 \N \N \N \N \N 10 f \N
134 20:05:35.472+00 2020-03-28 UPDATE 4 \N \N \N \N \N 9 f \N
135 20:05:55.782+00 2020-03-28 UPLOAD 7 \N \N \N \N \N 16 f \N
136 20:05:55.961+00 2020-03-28 UPLOAD 7 \N \N \N \N \N 17 f \N
137 20:05:56.091+00 2020-03-28 UPLOAD 7 \N \N \N \N \N 18 f \N
138 20:06:11.429+00 2020-03-28 UPLOAD 7 \N \N \N \N \N 19 f \N
139 20:06:21.099+00 2020-03-28 UPLOAD 7 \N \N \N \N \N 20 f \N
140 20:06:46.315+00 2020-03-28 UPLOAD 6 \N \N \N \N \N 21 f \N
141 20:06:46.487+00 2020-03-28 UPLOAD 6 \N \N \N \N \N 22 f \N
142 20:06:46.487+00 2020-03-28 UPLOAD 6 \N \N \N \N \N 23 f \N
143 20:07:11.207+00 2020-03-28 UPLOAD 6 \N \N \N \N \N 24 f \N
144 20:07:50.726+00 2020-03-28 UPLOAD 5 \N \N \N \N \N 25 f \N
145 20:07:50.774+00 2020-03-28 UPLOAD 5 \N \N \N \N \N 26 f \N
146 20:07:52.823+00 2020-03-28 UPDATE 5 \N \N \N \N \N 25 f \N
147 20:07:52.812+00 2020-03-28 UPDATE 5 \N \N \N \N \N 25 f \N
148 20:07:52.845+00 2020-03-28 UPDATE 5 \N \N \N \N \N 25 f \N
149 20:08:12.215+00 2020-03-28 UPDATE 5 \N \N \N \N \N 17 f \N
150 20:08:12.21+00 2020-03-28 UPDATE 5 \N \N \N \N \N 17 f \N
151 20:08:12.231+00 2020-03-28 UPDATE 5 \N \N \N \N \N 17 f \N
152 20:08:18.25+00 2020-03-28 UPDATE 5 \N \N \N \N \N 18 f \N
153 20:08:18.243+00 2020-03-28 UPDATE 5 \N \N \N \N \N 18 f \N
154 20:08:18.273+00 2020-03-28 UPDATE 5 \N \N \N \N \N 18 f \N
155 20:08:23.538+00 2020-03-28 UPDATE 5 \N \N \N \N \N 21 f \N
156 20:08:23.532+00 2020-03-28 UPDATE 5 \N \N \N \N \N 21 f \N
157 20:08:23.552+00 2020-03-28 UPDATE 5 \N \N \N \N \N 21 f \N
158 20:08:41.763+00 2020-03-28 UPDATE 5 \N \N \N \N \N 20 f \N
159 20:08:41.753+00 2020-03-28 UPDATE 5 \N \N \N \N \N 20 f \N
160 20:08:41.776+00 2020-03-28 UPDATE 5 \N \N \N \N \N 20 f \N
161 20:09:48.101+00 2020-03-28 DELETE 4 \N \N \N \N \N 6 f \N
162 20:10:08.508+00 2020-03-28 DELETE 5 \N \N \N \N \N 14 f \N
163 20:12:42.433+00 2020-03-28 UPLOAD 4 \N \N \N \N \N 27 f \N
164 20:12:42.512+00 2020-03-28 UPLOAD 4 \N \N \N \N \N 28 f \N
165 20:12:44.529+00 2020-03-28 UPDATE 4 \N \N \N \N \N 27 f \N
166 20:12:44.539+00 2020-03-28 UPDATE 4 \N \N \N \N \N 27 f \N
167 20:12:44.643+00 2020-03-28 UPDATE 4 \N \N \N \N \N 28 f \N
168 20:12:44.653+00 2020-03-28 UPDATE 4 \N \N \N \N \N 28 f \N
169 20:13:30.626+00 2020-03-28 DELETE 4 \N \N \N \N \N 28 f \N
170 20:13:30.732+00 2020-03-28 DELETE 4 \N \N \N \N \N 27 f \N
171 20:14:04.855+00 2020-03-28 UPLOAD 4 \N \N \N \N \N 29 f \N
172 20:14:06.597+00 2020-03-28 UPDATE 4 \N \N \N \N \N 29 f \N
173 20:14:06.609+00 2020-03-28 UPDATE 4 \N \N \N \N \N 29 f \N
174 20:14:17.882+00 2020-03-28 DELETE 4 \N \N \N \N \N 29 f \N
175 20:14:42.968+00 2020-03-28 UPLOAD 6 \N \N \N \N \N 30 f \N
176 20:14:43.1+00 2020-03-28 UPLOAD 6 \N \N \N \N \N 31 f \N
177 10:43:05.243+00 2020-03-29 UPDATE 4 \N \N \N \N \N 15 f \N
178 10:43:10.081+00 2020-03-29 UPDATE 4 \N \N \N \N \N 15 f \N
179 20:05:25.656+00 2020-03-28 CREATE 4 1 \N \N \N \N \N f \N
180 20:05:29.987+00 2020-03-28 CREATE 4 2 \N \N \N \N \N f \N
181 20:05:35.477+00 2020-03-28 CREATE 4 3 \N \N \N \N \N f \N
182 10:43:05.991+00 2020-03-29 CREATE 4 4 \N \N \N \N \N f \N
183 10:43:10.333+00 2020-03-29 DELETE 4 4 \N \N \N \N \N f \N
184 20:01:11.498+00 2020-03-28 UPDATE 4 \N \N \N \N 2 \N f \N
185 20:01:13.018+00 2020-03-28 UPDATE 4 \N \N \N \N 2 \N f \N
186 20:03:44.661+00 2020-03-28 UPDATE 4 \N \N \N \N 3 \N f \N
187 20:03:44.678+00 2020-03-28 UPDATE 4 \N \N \N \N 3 \N f \N
188 20:03:44.702+00 2020-03-28 UPDATE 4 \N \N \N \N 3 \N f \N
189 20:03:44.837+00 2020-03-28 UPDATE 4 \N \N \N \N 3 \N f \N
190 20:03:44.844+00 2020-03-28 UPDATE 4 \N \N \N \N 3 \N f \N
191 20:03:44.846+00 2020-03-28 UPDATE 4 \N \N \N \N 3 \N f \N
192 20:03:44.908+00 2020-03-28 UPDATE 4 \N \N \N \N 3 \N f \N
193 20:03:44.907+00 2020-03-28 UPDATE 4 \N \N \N \N 3 \N f \N
194 20:03:44.919+00 2020-03-28 UPDATE 4 \N \N \N \N 3 \N f \N
195 20:03:44.924+00 2020-03-28 UPDATE 4 \N \N \N \N 3 \N f \N
196 20:03:44.923+00 2020-03-28 UPDATE 4 \N \N \N \N 3 \N f \N
197 20:03:44.932+00 2020-03-28 UPDATE 4 \N \N \N \N 3 \N f \N
198 20:03:44.938+00 2020-03-28 UPDATE 4 \N \N \N \N 3 \N f \N
199 20:03:44.941+00 2020-03-28 UPDATE 4 \N \N \N \N 3 \N f \N
200 20:03:44.957+00 2020-03-28 UPDATE 4 \N \N \N \N 3 \N f \N
201 20:03:45.185+00 2020-03-28 UPDATE 4 \N \N \N \N 3 \N f \N
202 20:03:45.203+00 2020-03-28 UPDATE 4 \N \N \N \N 3 \N f \N
203 20:03:45.249+00 2020-03-28 UPDATE 4 \N \N \N \N 3 \N f \N
204 20:03:45.274+00 2020-03-28 UPDATE 4 \N \N \N \N 3 \N f \N
205 20:03:45.28+00 2020-03-28 UPDATE 4 \N \N \N \N 3 \N f \N
206 20:03:45.286+00 2020-03-28 UPDATE 4 \N \N \N \N 3 \N f \N
207 20:03:45.291+00 2020-03-28 UPDATE 4 \N \N \N \N 3 \N f \N
208 20:03:45.301+00 2020-03-28 UPDATE 4 \N \N \N \N 3 \N f \N
209 20:03:45.306+00 2020-03-28 UPDATE 4 \N \N \N \N 3 \N f \N
210 20:03:45.31+00 2020-03-28 UPDATE 4 \N \N \N \N 3 \N f \N
211 20:03:58.911+00 2020-03-28 UPDATE 4 \N \N \N \N 4 \N f \N
212 20:03:58.917+00 2020-03-28 UPDATE 4 \N \N \N \N 4 \N f \N
213 20:03:58.994+00 2020-03-28 UPDATE 4 \N \N \N \N 4 \N f \N
214 20:03:59.003+00 2020-03-28 UPDATE 4 \N \N \N \N 4 \N f \N
215 20:03:59.03+00 2020-03-28 UPDATE 4 \N \N \N \N 4 \N f \N
216 20:03:59.037+00 2020-03-28 UPDATE 4 \N \N \N \N 4 \N f \N
217 20:03:59.043+00 2020-03-28 UPDATE 4 \N \N \N \N 4 \N f \N
218 20:03:59.071+00 2020-03-28 UPDATE 4 \N \N \N \N 4 \N f \N
219 20:03:59.083+00 2020-03-28 UPDATE 4 \N \N \N \N 4 \N f \N
220 20:03:59.095+00 2020-03-28 UPDATE 4 \N \N \N \N 4 \N f \N
221 20:03:59.597+00 2020-03-28 UPDATE 4 \N \N \N \N 4 \N f \N
222 20:03:59.648+00 2020-03-28 UPDATE 4 \N \N \N \N 4 \N f \N
223 20:03:59.692+00 2020-03-28 UPDATE 4 \N \N \N \N 4 \N f \N
224 20:03:59.699+00 2020-03-28 UPDATE 4 \N \N \N \N 4 \N f \N
225 20:03:59.704+00 2020-03-28 UPDATE 4 \N \N \N \N 4 \N f \N
226 20:03:59.786+00 2020-03-28 UPDATE 4 \N \N \N \N 4 \N f \N
227 20:03:59.828+00 2020-03-28 UPDATE 4 \N \N \N \N 4 \N f \N
228 20:03:59.862+00 2020-03-28 UPDATE 4 \N \N \N \N 4 \N f \N
229 20:03:59.868+00 2020-03-28 UPDATE 4 \N \N \N \N 4 \N f \N
230 20:03:59.873+00 2020-03-28 UPDATE 4 \N \N \N \N 4 \N f \N
231 20:04:17.1+00 2020-03-28 UPDATE 4 \N \N \N \N 2 \N f \N
232 20:04:17.178+00 2020-03-28 UPDATE 4 \N \N \N \N 2 \N f \N
233 20:04:17.23+00 2020-03-28 UPDATE 4 \N \N \N \N 2 \N f \N
234 20:04:17.242+00 2020-03-28 UPDATE 4 \N \N \N \N 2 \N f \N
235 20:04:17.251+00 2020-03-28 UPDATE 4 \N \N \N \N 2 \N f \N
236 20:04:17.504+00 2020-03-28 UPDATE 4 \N \N \N \N 2 \N f \N
237 20:04:17.53+00 2020-03-28 UPDATE 4 \N \N \N \N 2 \N f \N
238 20:04:17.558+00 2020-03-28 UPDATE 4 \N \N \N \N 2 \N f \N
239 20:04:17.562+00 2020-03-28 UPDATE 4 \N \N \N \N 2 \N f \N
240 20:04:17.566+00 2020-03-28 UPDATE 4 \N \N \N \N 2 \N f \N
241 20:04:17.605+00 2020-03-28 UPDATE 4 \N \N \N \N 2 \N f \N
242 20:04:17.635+00 2020-03-28 UPDATE 4 \N \N \N \N 2 \N f \N
243 20:04:17.66+00 2020-03-28 UPDATE 4 \N \N \N \N 2 \N f \N
244 20:04:17.664+00 2020-03-28 UPDATE 4 \N \N \N \N 2 \N f \N
245 20:04:17.668+00 2020-03-28 UPDATE 4 \N \N \N \N 2 \N f \N
246 20:04:24.952+00 2020-03-28 UPDATE 4 \N \N \N \N 2 \N f \N
247 20:04:24.966+00 2020-03-28 UPDATE 4 \N \N \N \N 2 \N f \N
248 20:04:24.978+00 2020-03-28 UPDATE 4 \N \N \N \N 2 \N f \N
249 20:04:31.788+00 2020-03-28 UPDATE 4 \N \N \N \N 4 \N f \N
250 20:04:31.798+00 2020-03-28 UPDATE 4 \N \N \N \N 4 \N f \N
251 20:04:31.806+00 2020-03-28 UPDATE 4 \N \N \N \N 4 \N f \N
252 20:04:31.814+00 2020-03-28 UPDATE 4 \N \N \N \N 4 \N f \N
253 20:04:41.448+00 2020-03-28 UPDATE 4 \N \N \N \N 4 \N f \N
254 20:04:41.462+00 2020-03-28 UPDATE 4 \N \N \N \N 4 \N f \N
255 20:04:41.479+00 2020-03-28 UPDATE 4 \N \N \N \N 4 \N f \N
256 20:04:41.495+00 2020-03-28 UPDATE 4 \N \N \N \N 4 \N f \N
257 20:04:47.472+00 2020-03-28 UPDATE 4 \N \N \N \N 4 \N f \N
258 20:04:51.631+00 2020-03-28 UPDATE 4 \N \N \N \N 3 \N f \N
259 20:04:54.599+00 2020-03-28 UPDATE 4 \N \N \N \N 3 \N f \N
260 20:04:56.601+00 2020-03-28 UPDATE 4 \N \N \N \N 2 \N f \N
261 20:05:25.64+00 2020-03-28 UPDATE 4 \N \N \N \N 2 \N f \N
262 20:05:29.977+00 2020-03-28 UPDATE 4 \N \N \N \N 4 \N f \N
263 20:05:29.984+00 2020-03-28 UPDATE 4 \N \N \N \N 4 \N f \N
264 20:05:35.467+00 2020-03-28 UPDATE 4 \N \N \N \N 4 \N f \N
265 20:05:35.474+00 2020-03-28 UPDATE 4 \N \N \N \N 4 \N f \N
266 20:05:58.313+00 2020-03-28 UPDATE 7 \N \N \N \N 2 \N f \N
267 20:05:58.441+00 2020-03-28 UPDATE 7 \N \N \N \N 2 \N f \N
268 20:05:58.693+00 2020-03-28 UPDATE 7 \N \N \N \N 2 \N f \N
269 20:06:13.348+00 2020-03-28 UPDATE 7 \N \N \N \N 3 \N f \N
270 20:06:23.307+00 2020-03-28 UPDATE 7 \N \N \N \N 5 \N f \N
271 20:06:48.789+00 2020-03-28 UPDATE 6 \N \N \N \N 2 \N f \N
272 20:06:49.159+00 2020-03-28 UPDATE 6 \N \N \N \N 2 \N f \N
273 20:06:49.565+00 2020-03-28 UPDATE 6 \N \N \N \N 2 \N f \N
274 20:07:13.155+00 2020-03-28 UPDATE 6 \N \N \N \N 5 \N f \N
275 20:07:52.793+00 2020-03-28 UPDATE 5 \N \N \N \N 2 \N f \N
276 20:07:52.829+00 2020-03-28 UPDATE 5 \N \N \N \N 2 \N f \N
277 20:07:52.838+00 2020-03-28 UPDATE 5 \N \N \N \N 2 \N f \N
278 20:07:52.854+00 2020-03-28 UPDATE 5 \N \N \N \N 3 \N f \N
279 20:07:52.858+00 2020-03-28 UPDATE 5 \N \N \N \N 2 \N f \N
280 20:07:52.863+00 2020-03-28 UPDATE 5 \N \N \N \N 2 \N f \N
281 20:07:52.866+00 2020-03-28 UPDATE 5 \N \N \N \N 2 \N f \N
282 20:08:12.221+00 2020-03-28 UPDATE 5 \N \N \N \N 2 \N f \N
283 20:08:12.227+00 2020-03-28 UPDATE 5 \N \N \N \N 2 \N f \N
284 20:08:12.246+00 2020-03-28 UPDATE 5 \N \N \N \N 2 \N f \N
285 20:08:12.251+00 2020-03-28 UPDATE 5 \N \N \N \N 2 \N f \N
286 20:08:12.254+00 2020-03-28 UPDATE 5 \N \N \N \N 2 \N f \N
287 20:08:18.255+00 2020-03-28 UPDATE 5 \N \N \N \N 2 \N f \N
288 20:08:18.268+00 2020-03-28 UPDATE 5 \N \N \N \N 2 \N f \N
289 20:08:18.286+00 2020-03-28 UPDATE 5 \N \N \N \N 2 \N f \N
290 20:08:18.296+00 2020-03-28 UPDATE 5 \N \N \N \N 2 \N f \N
291 20:08:18.299+00 2020-03-28 UPDATE 5 \N \N \N \N 2 \N f \N
292 20:08:23.543+00 2020-03-28 UPDATE 5 \N \N \N \N 2 \N f \N
293 20:08:23.549+00 2020-03-28 UPDATE 5 \N \N \N \N 2 \N f \N
294 20:08:23.562+00 2020-03-28 UPDATE 5 \N \N \N \N 2 \N f \N
295 20:08:23.565+00 2020-03-28 UPDATE 5 \N \N \N \N 2 \N f \N
296 20:08:23.569+00 2020-03-28 UPDATE 5 \N \N \N \N 2 \N f \N
297 20:08:41.767+00 2020-03-28 UPDATE 5 \N \N \N \N 5 \N f \N
298 20:08:41.772+00 2020-03-28 UPDATE 5 \N \N \N \N 5 \N f \N
299 20:08:41.783+00 2020-03-28 UPDATE 5 \N \N \N \N 5 \N f \N
300 20:08:41.786+00 2020-03-28 UPDATE 5 \N \N \N \N 5 \N f \N
301 20:08:41.789+00 2020-03-28 UPDATE 5 \N \N \N \N 5 \N f \N
302 20:08:48.105+00 2020-03-28 UPDATE 5 \N \N \N \N 2 \N f \N
303 20:08:48.109+00 2020-03-28 UPDATE 5 \N \N \N \N 2 \N f \N
304 20:08:51.742+00 2020-03-28 UPDATE 5 \N \N \N \N 5 \N f \N
305 20:08:51.745+00 2020-03-28 UPDATE 5 \N \N \N \N 5 \N f \N
306 20:08:53.749+00 2020-03-28 UPDATE 5 \N \N \N \N 5 \N f \N
307 20:08:53.753+00 2020-03-28 UPDATE 5 \N \N \N \N 5 \N f \N
308 20:09:06.488+00 2020-03-28 UPDATE 4 \N \N \N \N 3 \N f \N
309 20:09:06.491+00 2020-03-28 UPDATE 4 \N \N \N \N 3 \N f \N
310 20:09:09.17+00 2020-03-28 UPDATE 4 \N \N \N \N 4 \N f \N
311 20:09:09.173+00 2020-03-28 UPDATE 4 \N \N \N \N 4 \N f \N
312 20:09:15.758+00 2020-03-28 UPDATE 4 \N \N \N \N 4 \N f \N
313 20:09:15.761+00 2020-03-28 UPDATE 4 \N \N \N \N 4 \N f \N
314 20:09:48.11+00 2020-03-28 UPDATE 4 \N \N \N \N 3 \N f \N
315 20:09:48.125+00 2020-03-28 UPDATE 4 \N \N \N \N 3 \N f \N
316 20:09:48.128+00 2020-03-28 UPDATE 4 \N \N \N \N 3 \N f \N
317 20:10:08.519+00 2020-03-28 UPDATE 5 \N \N \N \N 2 \N f \N
318 20:10:08.531+00 2020-03-28 UPDATE 5 \N \N \N \N 2 \N f \N
319 20:10:08.533+00 2020-03-28 UPDATE 5 \N \N \N \N 2 \N f \N
320 20:12:44.515+00 2020-03-28 UPDATE 4 \N \N \N \N 6 \N f \N
321 20:12:44.536+00 2020-03-28 UPDATE 4 \N \N \N \N 6 \N f \N
322 20:12:44.549+00 2020-03-28 UPDATE 4 \N \N \N \N 6 \N f \N
323 20:12:44.551+00 2020-03-28 UPDATE 4 \N \N \N \N 6 \N f \N
324 20:12:44.556+00 2020-03-28 UPDATE 4 \N \N \N \N 6 \N f \N
325 20:12:44.625+00 2020-03-28 UPDATE 4 \N \N \N \N 6 \N f \N
326 20:12:44.65+00 2020-03-28 UPDATE 4 \N \N \N \N 6 \N f \N
327 20:12:44.666+00 2020-03-28 UPDATE 4 \N \N \N \N 6 \N f \N
328 20:12:44.671+00 2020-03-28 UPDATE 4 \N \N \N \N 6 \N f \N
329 20:12:44.677+00 2020-03-28 UPDATE 4 \N \N \N \N 6 \N f \N
330 20:13:27.926+00 2020-03-28 UPDATE 4 \N \N \N \N 6 \N f \N
331 20:13:27.934+00 2020-03-28 UPDATE 4 \N \N \N \N 6 \N f \N
332 20:13:30.639+00 2020-03-28 UPDATE 4 \N \N \N \N 6 \N f \N
333 20:13:30.652+00 2020-03-28 UPDATE 4 \N \N \N \N 6 \N f \N
334 20:13:30.655+00 2020-03-28 UPDATE 4 \N \N \N \N 6 \N f \N
335 20:13:30.743+00 2020-03-28 UPDATE 4 \N \N \N \N 6 \N f \N
336 20:13:30.754+00 2020-03-28 UPDATE 4 \N \N \N \N 6 \N f \N
337 20:13:30.756+00 2020-03-28 UPDATE 4 \N \N \N \N 6 \N f \N
338 20:13:30.789+00 2020-03-28 UPDATE 4 \N \N \N \N 6 \N f \N
339 20:13:30.791+00 2020-03-28 UPDATE 4 \N \N \N \N 6 \N f \N
340 20:13:30.795+00 2020-03-28 DELETE 4 \N \N \N \N 6 \N f \N
341 20:14:06.588+00 2020-03-28 UPDATE 4 \N \N \N \N 7 \N f \N
342 20:14:06.604+00 2020-03-28 UPDATE 4 \N \N \N \N 7 \N f \N
343 20:14:06.616+00 2020-03-28 UPDATE 4 \N \N \N \N 7 \N f \N
344 20:14:06.618+00 2020-03-28 UPDATE 4 \N \N \N \N 7 \N f \N
345 20:14:06.622+00 2020-03-28 UPDATE 4 \N \N \N \N 7 \N f \N
346 20:14:10.687+00 2020-03-28 UPDATE 4 \N \N \N \N 7 \N f \N
347 20:14:10.689+00 2020-03-28 UPDATE 4 \N \N \N \N 7 \N f \N
348 20:14:14.278+00 2020-03-28 UPDATE 4 \N \N \N \N 7 \N f \N
349 20:14:14.281+00 2020-03-28 UPDATE 4 \N \N \N \N 7 \N f \N
350 20:14:17.895+00 2020-03-28 UPDATE 4 \N \N \N \N 7 \N f \N
351 20:14:19.412+00 2020-03-28 DELETE 4 \N \N \N \N 7 \N f \N
352 20:14:44.965+00 2020-03-28 UPDATE 6 \N \N \N \N 5 \N f \N
353 20:14:45.738+00 2020-03-28 UPDATE 6 \N \N \N \N 5 \N f \N
354 10:43:05.272+00 2020-03-29 UPDATE 4 \N \N \N \N 2 \N f \N
355 10:43:05.321+00 2020-03-29 UPDATE 4 \N \N \N \N 2 \N f \N
356 10:43:05.331+00 2020-03-29 UPDATE 4 \N \N \N \N 2 \N f \N
357 10:43:10.091+00 2020-03-29 UPDATE 4 \N \N \N \N 2 \N f \N
358 10:43:10.117+00 2020-03-29 UPDATE 4 \N \N \N \N 2 \N f \N
359 10:43:10.123+00 2020-03-29 UPDATE 4 \N \N \N \N 2 \N f \N
360 20:04:24.995+00 2020-03-28 CREATE 4 \N 1 \N \N \N \N f \N
361 20:04:31.817+00 2020-03-28 CREATE 4 \N 2 \N \N \N \N f \N
362 20:04:36.897+00 2020-03-28 CREATE 4 \N 3 \N \N \N \N f \N
363 20:04:41.499+00 2020-03-28 DELETE 4 \N 2 \N \N \N \N f \N
\. --
-- Data for Name: package; Type: TABLE DATA; Schema: public; Owner: postgres
-- COPY public.package (id, name, version, description, author, user_maintainer_id, repository_id, url, source, title, active, deleted, resource_technology, binary_package) FROM stdin;
8 accrued 1.2 Package for visualizing data quality of partially accruing time series. Julie Eaton and Ian Painter 4 3 \N /opt/rdepot/repositories/3/83118397/accrued_1.2.tar.gz Visualization tools for partially accruing data t f R f
4 accrued 1.3.5 Package for visualizing data quality of partially accruing data. Julie Eaton and Ian Painter 4 3 \N /opt/rdepot/repositories/3/99077116/accrued_1.3.5.tar.gz Data Quality Visualization Tools for Partially Accruing Data t f R f
10 A3 0.9.2 This package supplies tools for tabulating and analyzing the results of predictive models. The methods employed are applicable to virtually any predictive model and make comparisons between different methodologies straightforward. Scott Fortmann-Roe 6 4 \N /opt/rdepot/repositories/4/54491936/A3_0.9.2.tar.gz A3: Accurate, Adaptable, and Accessible Error Metrics for Predictive Models t f R f
5 accrued 1.3 Package for visualizing data quality of partially accruing data. Julie Eaton and Ian Painter 4 3 \N /opt/rdepot/repositories/3/82197810/accrued_1.3.tar.gz Data Quality Visualization Tools for Partially Accruing Data t f R f
9 A3 0.9.1 This package supplies tools for tabulating and analyzing the results of predictive models. The methods employed are applicable to virtually any predictive model and make comparisons between different methodologies straightforward. Scott Fortmann-Roe 6 4 \N /opt/rdepot/repositories/4/47098069/A3_0.9.1.tar.gz A3: Accurate, Adaptable, and Accessible Error Metrics for Predictive Models t f R f
19 visdat 0.1.0 Create preliminary exploratory data visualisations of an entire dataset to identify problems or unexpected features using 'ggplot2'. Nicholas Tierney [aut, cre] 4 3 https://github.com/njtierney/visdat/ /opt/rdepot/new/70032548/visdat_0.1.0.tar.gz Preliminary Data Visualisation f f R f
7 accrued 1.4 Package for visualizing data quality of partially accruing data. Julie Eaton and Ian Painter 4 3 \N /opt/rdepot/repositories/3/28075835/accrued_1.4.tar.gz Data Quality Visualization Tools for Partially Accruing Data f f R f
14 npordtests 1.1 Performs nonparametric tests for equality of location against ordered alternatives. Bulent Altunkaynak [aut, cre], Hamza Gamgam [aut] 5 2 \N /opt/rdepot/repositories/2/8436419/npordtests_1.1.tar.gz Nonparametric Tests for Equality of Location Against Ordered Alternatives f t R f
17 A3 0.9.2 This package supplies tools for tabulating and analyzing the results of predictive models. The methods employed are applicable to virtually any predictive model and make comparisons between different methodologies straightforward. Scott Fortmann-Roe 5 2 \N /opt/rdepot/repositories/2/9907084/A3_0.9.2.tar.gz A3: Accurate, Adaptable, and Accessible Error Metrics for Predictive Models t f R f
13 accrued 1.0 Package for visualizing data quality of partially accruing time series. Julie Eaton and Ian Painter 6 2 \N /opt/rdepot/repositories/2/40553654/accrued_1.0.tar.gz Visualization tools for partially accruing data f f R f
11 abc 1.3 The package implements several ABC algorithms for performing parameter estimation and model selection. Cross-validation tools are also available for measuring the accuracy of ABC estimates, and to calculate the misclassification probabilities of different models. Katalin Csillery, Michael Blum and Olivier Francois 6 4 \N /opt/rdepot/repositories/4/95296712/abc_1.3.tar.gz Tools for Approximate Bayesian Computation (ABC) f f R f
12 abc 1.0 The 'abc' package provides various functions for parameter estimation and model selection in an ABC framework. Three main Katalin Csillery, with contributions from Michael Blum and Olivier Francois 6 4 \N /opt/rdepot/repositories/4/49426769/abc_1.0.tar.gz Functions to perform Approximate Bayesian Computation (ABC) using simulated data t f R f
18 abc 1.3 The package implements several ABC algorithms for performing parameter estimation and model selection. Cross-validation tools are also available for measuring the accuracy of ABC estimates, and to calculate the misclassification probabilities of different models. Katalin Csillery, Michael Blum and Olivier Francois 5 2 \N /opt/rdepot/repositories/2/88170013/abc_1.3.tar.gz Tools for Approximate Bayesian Computation (ABC) t f R f
20 AnaCoDa 0.1.2.3 Is a collection of models to analyze genome scale codon data using a Bayesian framework. Provides visualization routines and checkpointing for model fittings. Currently published models to analyze gene data for selection on codon Cedric Landerer [aut, cre], Gabriel Hanas [ctb], Jeremy Rogers [ctb], Alex Cope [ctb], Denizhan Pak [ctb] 5 5 https://github.com/clandere/AnaCoDa /opt/rdepot/repositories/5/39437028/AnaCoDa_0.1.2.3.tar.gz Analysis of Codon Data under Stationarity using a Bayesian Framework t f R f
6 accrued 1.1 Package for visualizing data quality of partially accruing time series. Julie Eaton and Ian Painter 4 3 \N /opt/rdepot/repositories/3/46950998/accrued_1.1.tar.gz Visualization tools for partially accruing data f t R f
22 AnaCoDa 0.1.2.3 Is a collection of models to analyze genome scale codon data using a Bayesian framework. Provides visualization routines and checkpointing for model fittings. Currently published models to analyze gene data for selection on codon Cedric Landerer [aut, cre], Gabriel Hanas [ctb], Jeremy Rogers [ctb], Alex Cope [ctb], Denizhan Pak [ctb] 4 2 https://github.com/clandere/AnaCoDa Analysis of Codon Data under Stationarity using a Bayesian Framework f f R f
26 usl 2.0.0 The Universal Scalability Law (Gunther 2007) Neil J. Gunther [aut], Stefan Moeding [aut, cre] 4 3 \N /opt/rdepot/new/54345476/usl_2.0.0.tar.gz Analyze System Scalability with the Universal Scalability Law f f R f
25 usl 2.0.0 The Universal Scalability Law (Gunther 2007) Neil J. Gunther [aut], Stefan Moeding [aut, cre] 5 2 \N /opt/rdepot/repositories/2/33930690/usl_2.0.0.tar.gz Analyze System Scalability with the Universal Scalability Law t f R f
16 A3 0.9.1 This package supplies tools for tabulating and analyzing the results of predictive models. The methods employed are applicable to virtually any predictive model and make comparisons between different methodologies straightforward. Scott Fortmann-Roe 4 2 \N A3: Accurate, Adaptable, and Accessible Error Metrics for Predictive Models f f R f
31 abc 1.0 The 'abc' package provides various functions for parameter estimation and model selection in an ABC framework. Three main Katalin Csillery, with contributions from Michael Blum and Olivier Francois 8 5 \N /opt/rdepot/new/51328701/abc_1.0.tar.gz Functions to perform Approximate Bayesian Computation (ABC) using simulated data f f R f
21 Benchmarking 0.10 Estimates and graphs deterministic (DEA) frontier models with different technology assumptions (fdh, vrs, drs, crs, irs, add). Also handles possible slacks, peers and their weights (lambdas), optimal cost, revenue and profit allocation, super--efficiency, and mergers. A comparative method for estimating SFA efficiencies is included. Peter Bogetoft and Lars Otto 5 2 \N /opt/rdepot/repositories/2/71228208/Benchmarking_0.10.tar.gz Benchmark and frontier analysis using DEA and SFA t f R f
23 visdat 0.1.0 Create preliminary exploratory data visualisations of an entire dataset to identify problems or unexpected features using 'ggplot2'. Nicholas Tierney [aut, cre] 4 2 https://github.com/njtierney/visdat/ Preliminary Data Visualisation f f R f
24 Benchmarking 0.10 Estimates and graphs deterministic (DEA) frontier models with different technology assumptions (fdh, vrs, drs, crs, irs, add). Also handles possible slacks, peers and their weights (lambdas), optimal cost, revenue and profit allocation, super--efficiency, and mergers. A comparative method for estimating SFA efficiencies is included. Peter Bogetoft and Lars Otto 4 5 \N Benchmark and frontier analysis using DEA and SFA f f R f
30 A3 0.9.1 This package supplies tools for tabulating and analyzing the results of predictive models. The methods employed are applicable to virtually any predictive model and make comparisons between different methodologies straightforward. Scott Fortmann-Roe 8 5 \N /opt/rdepot/new/92253304/A3_0.9.1.tar.gz A3: Accurate, Adaptable, and Accessible Error Metrics for Predictive Models f f R f
28 visdat 0.1.0 Create preliminary exploratory data visualisations of an entire dataset to identify problems or unexpected features using 'ggplot2'. Nicholas Tierney [aut, cre] 8 6 https://github.com/njtierney/visdat/ /opt/rdepot/repositories/6/70325377/visdat_0.1.0.tar.gz Preliminary Data Visualisation f t R f
27 usl 2.0.0 The Universal Scalability Law (Gunther 2007) Neil J. Gunther [aut], Stefan Moeding [aut, cre] 8 6 \N /opt/rdepot/repositories/6/21695389/usl_2.0.0.tar.gz Analyze System Scalability with the Universal Scalability Law f t R f
29 A3 0.9.1 This package supplies tools for tabulating and analyzing the results of predictive models. The methods employed are applicable to virtually any predictive model and make comparisons between different methodologies straightforward. Scott Fortmann-Roe 8 7 \N /opt/rdepot/repositories/7/67484296/A3_0.9.1.tar.gz A3: Accurate, Adaptable, and Accessible Error Metrics for Predictive Models f t R f
15 bea.R 1.0.5 Provides an R interface for the Bureau of Economic Analysis (BEA) Andrea Batch [aut, cre], Jeff Chen [ctb], Walt Kampas [ctb] 5 2 https://github.com/us-bea/beaR /opt/rdepot/repositories/2/89565416/bea.R_1.0.5.tar.gz Bureau of Economic Analysis API t f R f
\. --
-- Data for Name: package_maintainer; Type: TABLE DATA; Schema: public; Owner: postgres
-- COPY public.package_maintainer (id, user_id, package, repository_id, deleted) FROM stdin;
1 6 accrued 2 f
2 6 abc 4 f
3 6 A3 4 f
4 6 bea.R 2 t
\. --
-- Data for Name: pythonpackage; Type: TABLE DATA; Schema: public; Owner: postgres
-- COPY public.pythonpackage (id, license, author_email, classifier, description_content_type, home_page, keywords, maintainer, maintainer_email, platform, project_url, provides_extra, requires_dist, requires_external, requires_python, summary, hash) FROM stdin;
\. --
-- Data for Name: pythonrepository; Type: TABLE DATA; Schema: public; Owner: postgres
-- COPY public.pythonrepository (id, hash_method) FROM stdin;
\. --
-- Data for Name: repository; Type: TABLE DATA; Schema: public; Owner: postgres
-- COPY public.repository (version, id, publication_uri, name, server_address, published, deleted, resource_technology, last_publication_successful, last_modified_timestamp, last_publication_timestamp) FROM stdin;
8 3 http://localhost/repo/testrepo2 testrepo2 http://oa-rdepot-repo:8080/testrepo2 t f R f 2020-03-28 20:09:48.128 \N
6 5 http://localhost/repo/testrepo4 testrepo4 http://oa-rdepot-repo:8080/testrepo4 f f R f 2020-03-28 20:14:45.738 \N
18 4 http://localhost/repo/testrepo3 testrepo3 http://oa-rdepot-repo:8080/testrepo3 f f R f 2020-03-28 20:09:15.761 \N
9 6 http://localhost/repo/testrepo5 testrepo5 http://oa-rdepot-repo:8080/testrepo5 f t R f 2020-03-28 20:13:30.795 \N
31 2 http://localhost/repo/testrepo1 testrepo1 http://oa-rdepot-repo:8080/testrepo1 t f R f 2020-03-29 10:43:10.123 \N
6 7 http://localhost/repo/testrepo6 testrepo6 http://oa-rdepot-repo:8080/testrepo6 f t R f 2020-03-28 20:14:19.412 \N
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
-- COPY public.rpackage (id, depends, imports, suggests, system_requirements, license, md5sum, r_version, architecture, distribution, built, enhances, linking_to, priority, needs_compilation) FROM stdin;
8 R (>= 3.0), grid \N \N \N GPL-3 70d295115295a4718593f6a39d77add9 \N \N \N \N \N \N \N f
4 R (>= 2.14.1), grid \N \N \N GPL-3 19f8aec67250bd2ac481b14b50413d03 \N \N \N \N \N \N \N f
10 R (>= 2.15.0), xtable, pbapply \N randomForest, e1071 \N GPL (>= 2) 76d726aee8dd7c6ed94d150d5718015b \N \N \N \N \N \N \N f
5 R (>= 2.14.1), grid \N \N \N GPL-3 a05e4ca44438c0d9e7d713d7e3890423 \N \N \N \N \N \N \N f
9 xtable, pbapply \N randomForest, e1071 \N GPL (>= 2) 8eb4760cd574f5489e61221dc9bb0076 \N \N \N \N \N \N \N f
19 R (>= 3.2.2) ggplot2, tidyr, dplyr, purrr, magrittr, stats plotly (>= 4.5.6), testthat, knitr, rmarkdown, vdiffr \N MIT + file LICENSE f343fa3a01dcd9546fa0947877f58f36 \N \N \N \N \N \N \N f
7 R (>= 2.14.1), grid \N \N \N GPL-3 97c2930a9dd7ca9fc1409d5340c06470 \N \N \N \N \N \N \N f
14 R (>= 2.15.0) \N \N \N GPL (>= 2) da8be1247d3145b757bd62e01fc6eb8b \N \N \N \N \N \N \N f
17 R (>= 2.15.0), xtable, pbapply \N randomForest, e1071 \N GPL (>= 2) 76d726aee8dd7c6ed94d150d5718015b \N \N \N \N \N \N \N f
13 R (>= 3.0), grid \N \N \N GPL-3 1c75d59b18e554a285a9b156a06a288c \N \N \N \N \N \N \N f
11 R (>= 2.10), nnet, quantreg, locfit \N \N \N GPL (>= 3) c47d18b86b331a5023dcd62b74fedbb6 \N \N \N \N \N \N \N f
12 R (>= 1.8.0), nnet, quantreg, locfit, methods \N \N \N Unlimited 91599204c92275ed4b36d55e8d7c144b \N \N \N \N \N \N \N f
18 R (>= 2.10), nnet, quantreg, locfit \N \N \N GPL (>= 3) c47d18b86b331a5023dcd62b74fedbb6 \N \N \N \N \N \N \N f
20 R (>= 3.3.0), Rcpp (>= 0.11.3), methods knitr, Hmisc, VGAM, coda, testthat, lmodel2 \N GPL (>= 2) 41026e4157a0b3b6d909f0c6f72fa65c \N \N \N \N \N \N \N f
6 R (>= 3.0), grid \N \N \N GPL-3 24b8cec280424dfc6a9e444fa57ba9f3 \N \N \N \N \N \N \N f
22 R (>= 3.3.0), Rcpp (>= 0.11.3), methods knitr, Hmisc, VGAM, coda, testthat, lmodel2 \N GPL (>= 2) 41026e4157a0b3b6d909f0c6f72fa65c \N \N \N \N \N \N \N f
26 R (>= 3.0), methods graphics, stats, nlsr knitr \N BSD_2_clause + file LICENSE 868140a3c3c29327eef5d5a485aee5b6 \N \N \N \N \N \N \N f
25 R (>= 3.0), methods graphics, stats, nlsr knitr \N BSD_2_clause + file LICENSE 868140a3c3c29327eef5d5a485aee5b6 \N \N \N \N \N \N \N f
16 xtable, pbapply \N randomForest, e1071 \N GPL (>= 2) 8eb4760cd574f5489e61221dc9bb0076 \N \N \N \N \N \N \N f
21 lpSolveAPI, ucminf \N \N \N GPL (>= 2) 9a99c2ebefa6d49422ca7893c1f4ead8 \N \N \N \N \N \N \N f
23 R (>= 3.2.2) ggplot2, tidyr, dplyr, purrr, magrittr, stats plotly (>= 4.5.6), testthat, knitr, rmarkdown, vdiffr \N MIT + file LICENSE f343fa3a01dcd9546fa0947877f58f36 \N \N \N \N \N \N \N f
24 lpSolveAPI, ucminf \N \N \N GPL (>= 2) 9a99c2ebefa6d49422ca7893c1f4ead8 \N \N \N \N \N \N \N f
30 xtable, pbapply \N randomForest, e1071 \N GPL (>= 2) 8eb4760cd574f5489e61221dc9bb0076 \N \N \N \N \N \N \N f
31 R (>= 1.8.0), nnet, quantreg, locfit, methods \N \N \N Unlimited 91599204c92275ed4b36d55e8d7c144b \N \N \N \N \N \N \N f
28 R (>= 3.2.2) ggplot2, tidyr, dplyr, purrr, magrittr, stats plotly (>= 4.5.6), testthat, knitr, rmarkdown, vdiffr \N MIT + file LICENSE f343fa3a01dcd9546fa0947877f58f36 \N \N \N \N \N \N \N f
27 R (>= 3.0), methods graphics, stats, nlsr knitr \N BSD_2_clause + file LICENSE 868140a3c3c29327eef5d5a485aee5b6 \N \N \N \N \N \N \N f
29 xtable, pbapply \N randomForest, e1071 \N GPL (>= 2) 8eb4760cd574f5489e61221dc9bb0076 \N \N \N \N \N \N \N f
15 R (>= 3.2.1), data.table httr, DT, shiny, jsonlite, googleVis, shinydashboard, ggplot2, stringr, chron, gtable, scales, htmltools, httpuv, xtable, stringi, magrittr, htmlwidgets, Rcpp, munsell, colorspace, plyr, yaml \N \N CC0 5e664f320c7cc884138d64467f6b0e49 \N \N \N \N \N \N \N f
\. --
-- Data for Name: rrepository; Type: TABLE DATA; Schema: public; Owner: postgres
-- COPY public.rrepository (id) FROM stdin;
5
4
3
6
7
2
\. --
-- Data for Name: submission; Type: TABLE DATA; Schema: public; Owner: postgres
-- COPY public.submission (id, submitter_id, package_id, changes, deleted, state, approver_id) FROM stdin;
26 5 26 \N f WAITING \N
19 7 19 \N f WAITING \N
31 6 31 \N f WAITING \N
30 6 30 \N f WAITING \N
5 4 4 \N f ACCEPTED 4
18 7 18 \N f ACCEPTED 5
16 7 16 \N t REJECTED 5
15 4 14 \N t ACCEPTED 4
6 4 8 \N f ACCEPTED 4
12 4 12 \N f ACCEPTED 4
27 4 27 \N t ACCEPTED 4
23 6 22 \N t CANCELLED 6
24 6 24 \N t REJECTED 5
11 4 11 \N f ACCEPTED 4
8 4 7 \N f ACCEPTED 4
25 5 25 \N f ACCEPTED 5
29 4 29 \N t ACCEPTED 4
4 4 6 \N t ACCEPTED 4
21 6 21 \N f ACCEPTED 5
14 4 13 \N f ACCEPTED 4
17 7 17 \N f ACCEPTED 5
28 4 28 \N t ACCEPTED 4
22 6 23 \N t REJECTED 5
20 7 20 \N f ACCEPTED 5
13 4 15 \N f ACCEPTED 4
10 4 10 \N f ACCEPTED 4
9 4 9 \N f ACCEPTED 4
7 4 5 \N f ACCEPTED 4
\. --
-- Data for Name: user; Type: TABLE DATA; Schema: public; Owner: postgres
-- COPY public."user" (id, role_id, name, email, login, active, last_logged_in_on, deleted, created_on) FROM stdin;
8 4 Local Admin User admin@localhost admin f \N f 1970-01-01
7 1 Isaac Newton newton@ldap.forumsys.com newton t 2020-03-28 f 1970-01-01
6 2 Galileo Galilei galieleo@ldap.forumsys.com galieleo t 2020-03-28 f 1970-01-01
5 3 Nikola Tesla tesla@ldap.forumsys.com tesla t 2020-03-29 f 1970-01-01
4 4 Albert Einstein einstein@ldap.forumsys.com einstein t 2020-08-20 f 1970-01-01
9 1 John Doe doe@localhost doe f 2020-08-20 f 1970-01-01
10 1 Alfred Tarski tarski@localhost tarski t 2020-08-25 f 1970-01-01
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
