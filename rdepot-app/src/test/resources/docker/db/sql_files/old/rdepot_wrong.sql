--
-- PostgreSQL database dump
--

-- Dumped from database version 14.0
-- Dumped by pg_dump version 14.0


--
-- Data for Name: user; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public."user" (id, role_id, name, email, login, active, last_logged_in_on, deleted) FROM stdin;
8	4	Local Admin User	admin@localhost	admin	f	\N	f
7	1	Isaac Newton	newton@ldap.forumsys.com	newton	t	2020-03-28	f
6	2	Galileo Galilei	galieleo@ldap.forumsys.com	galieleo	t	2020-03-28	f
5	3	Nikola Tesla	tesla@ldap.forumsys.com	tesla	t	2020-03-29	f
4	4	Albert Einstein	einstein@ldap.forumsys.com	einstein	t	2020-08-20	f
9	1	John Doe	doe@localhost	doe	f	2020-08-20	f
10	1	Alfred Tarski	tarski@localhost	tarski	t	2020-08-25	f
\.

--
-- Data for Name: api_token; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.api_token (id, token, user_login) FROM stdin;
2	eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJlaW5zdGVpbiJ9.9VweA_kotRnnLn9giSE511MhWX4iDwtx85lidw_ZT5iTQ1aOB-3ytJNDB_Mrcop2H22MNhMjbpUW_sraHdvOlw	einstein
3	eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJ0ZXNsYSJ9.FEQ3KqMvTj4LQAgQx23f6Y0Z7PzKHgcO1a1UodG5iwCrzXhk6tHCR6V0T16F1tWtMMF0a3AQIShczN__d6KsFA	tesla
4	eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJnYWxpZWxlbyJ9.Hp95DiIZ0L0JXyQZOvhJkzyTDzNuos81QoTWfLeVPlodWvGg7ziJTI6nJFitg5VAwrGmA4wpbWbjK9aItCKB3A	galieleo
5	eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJuZXd0b24ifQ.3E7UwKTwc8DchKRUSD_hdJxOcl4L6SOguwbm9WmVzWU4YDQMkIJ_wVNidpus6gNJvyT6OR6pREkfQCnWkEhEBQ	newton
\.

--
-- Data for Name: repository; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.repository (version, id, publication_uri, name, server_address, published, deleted, dtype) FROM stdin;
6	5	http://localhost/repo/testrepo4	testrepo4	http://oa-rdepot-repo:8080/testrepo4	f	f	RRepository
18	4	http://localhost/repo/testrepo3	testrepo3	http://oa-rdepot-repo:8080/testrepo3	f	f	RRepository
8	3	http://localhost/repo/testrepo2	testrepo2	http://oa-rdepot-repo:8080/testrepo2	t	f	RRepository
9	6	http://localhost/repo/testrepo5	testrepo5	http://oa-rdepot-repo:8080/testrepo5	f	t	RRepository
6	7	http://localhost/repo/testrepo6	testrepo6	http://oa-rdepot-repo:8080/testrepo6	f	t	RRepository
31	2	http://localhost/repo/testrepo1	testrepo1	http://oa-rdepot-repo:8080/testrepo1	t	f	RRepository
\.


--
-- Data for Name: event; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.event (id, value) FROM stdin;
1	create
2	delete
3	update
\.


--
-- Data for Name: package; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.package (id, name, version, description, author, maintainer_id, repository_id, url, source, title, active, deleted, dtype) FROM stdin;
8	accrued	1.2	Package for visualizing data quality of partially accruing time series.	Julie Eaton and Ian Painter	4	3	\N	/opt/rdepot/repositories/3/83118397/accrued_1.2.tar.gz	Visualization tools for partially accruing data	t	f	RPackage
4	accrued	1.3.5	Package for visualizing data quality of partially accruing data.	Julie Eaton and Ian Painter	4	3	\N	/opt/rdepot/repositories/3/99077116/accrued_1.3.5.tar.gz	Data Quality Visualization Tools for Partially Accruing Data	t	f	RPackage
10	A3	0.9.2	This package supplies tools for tabulating and analyzing the results of predictive models. The methods employed are applicable to virtually any predictive model and make comparisons between different methodologies straightforward.	Scott Fortmann-Roe	6	4	\N	/opt/rdepot/repositories/4/54491936/A3_0.9.2.tar.gz	A3: Accurate, Adaptable, and Accessible Error Metrics for Predictive Models	t	f	RPackage
5	accrued	1.3	Package for visualizing data quality of partially accruing data.	Julie Eaton and Ian Painter	4	3	\N	/opt/rdepot/repositories/3/82197810/accrued_1.3.tar.gz	Data Quality Visualization Tools for Partially Accruing Data	t	f	RPackage
9	A3	0.9.1	This package supplies tools for tabulating and analyzing the results of predictive models. The methods employed are applicable to virtually any predictive model and make comparisons between different methodologies straightforward.	Scott Fortmann-Roe	6	4	\N	/opt/rdepot/repositories/4/47098069/A3_0.9.1.tar.gz	A3: Accurate, Adaptable, and Accessible Error Metrics for Predictive Models	t	f	RPackage
19	visdat	0.1.0	Create preliminary exploratory data visualisations of an entire dataset to identify problems or unexpected features using 'ggplot2'.	Nicholas Tierney [aut, cre]	4	3	https://github.com/njtierney/visdat/	/opt/rdepot/new/70032548/visdat_0.1.0.tar.gz	Preliminary Data Visualisation	f	f	RPackage
7	accrued	1.4	Package for visualizing data quality of partially accruing data.	Julie Eaton and Ian Painter	4	3	\N	/opt/rdepot/repositories/3/28075835/accrued_1.4.tar.gz	Data Quality Visualization Tools for Partially Accruing Data	f	f	RPackage
14	npordtests	1.1	Performs nonparametric tests for equality of location against ordered alternatives.	Bulent Altunkaynak [aut, cre], Hamza Gamgam [aut]	5	2	\N	/opt/rdepot/repositories/2/8436419/npordtests_1.1.tar.gz	Nonparametric Tests for Equality of Location Against Ordered Alternatives	f	t	RPackage
17	A3	0.9.2	This package supplies tools for tabulating and analyzing the results of predictive models. The methods employed are applicable to virtually any predictive model and make comparisons between different methodologies straightforward.	Scott Fortmann-Roe	5	2	\N	/opt/rdepot/repositories/2/9907084/A3_0.9.2.tar.gz	A3: Accurate, Adaptable, and Accessible Error Metrics for Predictive Models	t	f	RPackage
13	accrued	1.0	Package for visualizing data quality of partially accruing time series.	Julie Eaton and Ian Painter	6	2	\N	/opt/rdepot/repositories/2/40553654/accrued_1.0.tar.gz	Visualization tools for partially accruing data	f	f	RPackage
11	abc	1.3	The package implements several ABC algorithms for performing parameter estimation and model selection. Cross-validation tools are also available for measuring the accuracy of ABC estimates, and to calculate the misclassification probabilities of different models.	Katalin Csillery, Michael Blum and Olivier Francois	6	4	\N	/opt/rdepot/repositories/4/95296712/abc_1.3.tar.gz	Tools for Approximate Bayesian Computation (ABC)	f	f	RPackage
12	abc	1.0	The 'abc' package provides various functions for parameter estimation and model selection in an ABC framework. Three main	Katalin Csillery, with contributions from Michael Blum and Olivier Francois	6	4	\N	/opt/rdepot/repositories/4/49426769/abc_1.0.tar.gz	Functions to perform Approximate Bayesian Computation (ABC) using simulated data	t	f	RPackage
18	abc	1.3	The package implements several ABC algorithms for performing parameter estimation and model selection. Cross-validation tools are also available for measuring the accuracy of ABC estimates, and to calculate the misclassification probabilities of different models.	Katalin Csillery, Michael Blum and Olivier Francois	5	2	\N	/opt/rdepot/repositories/2/88170013/abc_1.3.tar.gz	Tools for Approximate Bayesian Computation (ABC)	t	f	RPackage
20	AnaCoDa	0.1.2.3	Is a collection of models to analyze genome scale codon data using a Bayesian framework. Provides visualization routines and checkpointing for model fittings. Currently published models to analyze gene data for selection on codon	Cedric Landerer [aut, cre], Gabriel Hanas [ctb], Jeremy Rogers [ctb], Alex Cope [ctb], Denizhan Pak [ctb]	5	5	https://github.com/clandere/AnaCoDa	/opt/rdepot/repositories/5/39437028/AnaCoDa_0.1.2.3.tar.gz	Analysis of Codon Data under Stationarity using a Bayesian Framework	t	f	RPackage
6	accrued	1.1	Package for visualizing data quality of partially accruing time series.	Julie Eaton and Ian Painter	4	3	\N	/opt/rdepot/repositories/3/46950998/accrued_1.1.tar.gz	Visualization tools for partially accruing data	f	t	RPackage
22	AnaCoDa	0.1.2.3	Is a collection of models to analyze genome scale codon data using a Bayesian framework. Provides visualization routines and checkpointing for model fittings. Currently published models to analyze gene data for selection on codon	Cedric Landerer [aut, cre], Gabriel Hanas [ctb], Jeremy Rogers [ctb], Alex Cope [ctb], Denizhan Pak [ctb]	4	2	https://github.com/clandere/AnaCoDa		Analysis of Codon Data under Stationarity using a Bayesian Framework	f	f	RPackage
26	usl	2.0.0	The Universal Scalability Law (Gunther 2007)	Neil J. Gunther [aut], Stefan Moeding [aut, cre]	4	3	\N	/opt/rdepot/new/54345476/usl_2.0.0.tar.gz	Analyze System Scalability with the Universal Scalability Law	f	f	RPackage
25	usl	2.0.0	The Universal Scalability Law (Gunther 2007)	Neil J. Gunther [aut], Stefan Moeding [aut, cre]	5	2	\N	/opt/rdepot/repositories/2/33930690/usl_2.0.0.tar.gz	Analyze System Scalability with the Universal Scalability Law	t	f	RPackage
16	A3	0.9.1	This package supplies tools for tabulating and analyzing the results of predictive models. The methods employed are applicable to virtually any predictive model and make comparisons between different methodologies straightforward.	Scott Fortmann-Roe	4	2	\N		A3: Accurate, Adaptable, and Accessible Error Metrics for Predictive Models	f	f	RPackage
21	Benchmarking	0.10	Estimates and graphs deterministic (DEA) frontier models with different technology assumptions (fdh, vrs, drs, crs, irs, add). Also handles possible slacks, peers and their weights (lambdas), optimal cost, revenue and profit allocation, super--efficiency, and mergers. A comparative method for estimating SFA efficiencies is included.	Peter Bogetoft and Lars Otto	5	2	\N	/opt/rdepot/repositories/2/71228208/Benchmarking_0.10.tar.gz	Benchmark and frontier analysis using DEA and SFA	t	f	RPackage
23	visdat	0.1.0	Create preliminary exploratory data visualisations of an entire dataset to identify problems or unexpected features using 'ggplot2'.	Nicholas Tierney [aut, cre]	4	2	https://github.com/njtierney/visdat/		Preliminary Data Visualisation	f	f	RPackage
24	Benchmarking	0.10	Estimates and graphs deterministic (DEA) frontier models with different technology assumptions (fdh, vrs, drs, crs, irs, add). Also handles possible slacks, peers and their weights (lambdas), optimal cost, revenue and profit allocation, super--efficiency, and mergers. A comparative method for estimating SFA efficiencies is included.	Peter Bogetoft and Lars Otto	4	5	\N		Benchmark and frontier analysis using DEA and SFA	f	f	RPackage
30	A3	0.9.1	This package supplies tools for tabulating and analyzing the results of predictive models. The methods employed are applicable to virtually any predictive model and make comparisons between different methodologies straightforward.	Scott Fortmann-Roe	8	5	\N	/opt/rdepot/new/92253304/A3_0.9.1.tar.gz	A3: Accurate, Adaptable, and Accessible Error Metrics for Predictive Models	f	f	RPackage
31	abc	1.0	The 'abc' package provides various functions for parameter estimation and model selection in an ABC framework. Three main	Katalin Csillery, with contributions from Michael Blum and Olivier Francois	8	5	\N	/opt/rdepot/new/51328701/abc_1.0.tar.gz	Functions to perform Approximate Bayesian Computation (ABC) using simulated data	f	f	RPackage
28	visdat	0.1.0	Create preliminary exploratory data visualisations of an entire dataset to identify problems or unexpected features using 'ggplot2'.	Nicholas Tierney [aut, cre]	8	6	https://github.com/njtierney/visdat/	/opt/rdepot/repositories/6/70325377/visdat_0.1.0.tar.gz	Preliminary Data Visualisation	f	t	RPackage
27	usl	2.0.0	The Universal Scalability Law (Gunther 2007)	Neil J. Gunther [aut], Stefan Moeding [aut, cre]	8	6	\N	/opt/rdepot/repositories/6/21695389/usl_2.0.0.tar.gz	Analyze System Scalability with the Universal Scalability Law	f	t	RPackage
29	A3	0.9.1	This package supplies tools for tabulating and analyzing the results of predictive models. The methods employed are applicable to virtually any predictive model and make comparisons between different methodologies straightforward.	Scott Fortmann-Roe	8	7	\N	/opt/rdepot/repositories/7/67484296/A3_0.9.1.tar.gz	A3: Accurate, Adaptable, and Accessible Error Metrics for Predictive Models	f	t	RPackage
15	bea.R	1.0.5	Provides an R interface for the Bureau of Economic Analysis (BEA)	Andrea Batch [aut, cre], Jeff Chen [ctb], Walt Kampas [ctb]	5	2	https://github.com/us-bea/beaR	/opt/rdepot/repositories/2/89565416/bea.R_1.0.5.tar.gz	Bureau of Economic Analysis API	t	f	RPackage
\.


--
-- Data for Name: package_event; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.package_event (id, date, package_id, event_id, changed_variable, value_before, value_after, changed_by, "time") FROM stdin;
4	2020-03-28	4	1	created			4	20:03:40.652+00
5	2020-03-28	6	1	created			4	20:03:40.648+00
6	2020-03-28	5	1	created			4	20:03:40.647+00
7	2020-03-28	8	1	created			4	20:03:40.659+00
8	2020-03-28	7	1	created			4	20:03:40.657+00
9	2020-03-28	6	3	source	/opt/rdepot/new/28009317/accrued_1.1.tar.gz	/opt/rdepot/repositories/3/46950998/accrued_1.1.tar.gz	4	20:03:44.778+00
10	2020-03-28	4	3	source	/opt/rdepot/new/43623400/accrued_1.3.5.tar.gz	/opt/rdepot/repositories/3/99077116/accrued_1.3.5.tar.gz	4	20:03:44.777+00
11	2020-03-28	8	3	source	/opt/rdepot/new/47097172/accrued_1.2.tar.gz	/opt/rdepot/repositories/3/83118397/accrued_1.2.tar.gz	4	20:03:44.782+00
12	2020-03-28	4	3	active	false	true	4	20:03:44.858+00
13	2020-03-28	6	3	active	false	true	4	20:03:44.856+00
14	2020-03-28	8	3	active	false	true	4	20:03:44.861+00
15	2020-03-28	5	3	source	/opt/rdepot/new/88466387/accrued_1.3.tar.gz	/opt/rdepot/repositories/3/82197810/accrued_1.3.tar.gz	4	20:03:45.211+00
16	2020-03-28	7	3	source	/opt/rdepot/new/47523656/accrued_1.4.tar.gz	/opt/rdepot/repositories/3/28075835/accrued_1.4.tar.gz	4	20:03:45.24+00
17	2020-03-28	5	3	active	false	true	4	20:03:45.255+00
18	2020-03-28	7	3	active	false	true	4	20:03:45.28+00
19	2020-03-28	9	1	created			4	20:03:55.815+00
20	2020-03-28	10	1	created			4	20:03:55.917+00
21	2020-03-28	11	1	created			4	20:03:56.245+00
22	2020-03-28	12	1	created			4	20:03:56.284+00
23	2020-03-28	10	3	source	/opt/rdepot/new/77598514/A3_0.9.2.tar.gz	/opt/rdepot/repositories/4/54491936/A3_0.9.2.tar.gz	4	20:03:58.941+00
24	2020-03-28	9	3	source	/opt/rdepot/new/6984008/A3_0.9.1.tar.gz	/opt/rdepot/repositories/4/47098069/A3_0.9.1.tar.gz	4	20:03:58.958+00
25	2020-03-28	10	3	active	false	true	4	20:03:59.003+00
26	2020-03-28	9	3	active	false	true	4	20:03:59.04+00
27	2020-03-28	11	3	source	/opt/rdepot/new/98224569/abc_1.3.tar.gz	/opt/rdepot/repositories/4/95296712/abc_1.3.tar.gz	4	20:03:59.621+00
28	2020-03-28	11	3	active	false	true	4	20:03:59.667+00
29	2020-03-28	12	3	source	/opt/rdepot/new/18685235/abc_1.0.tar.gz	/opt/rdepot/repositories/4/49426769/abc_1.0.tar.gz	4	20:03:59.807+00
30	2020-03-28	12	3	active	false	true	4	20:03:59.833+00
31	2020-03-28	13	1	created			4	20:04:14.336+00
32	2020-03-28	14	1	created			4	20:04:14.401+00
33	2020-03-28	15	1	created			4	20:04:14.418+00
34	2020-03-28	15	3	source	/opt/rdepot/new/68910623/bea.R_1.0.5.tar.gz	/opt/rdepot/repositories/2/89565416/bea.R_1.0.5.tar.gz	4	20:04:17.13+00
35	2020-03-28	15	3	active	false	true	4	20:04:17.191+00
36	2020-03-28	13	3	source	/opt/rdepot/new/13236487/accrued_1.0.tar.gz	/opt/rdepot/repositories/2/40553654/accrued_1.0.tar.gz	4	20:04:17.518+00
37	2020-03-28	13	3	active	false	true	4	20:04:17.533+00
38	2020-03-28	14	3	source	/opt/rdepot/new/16258274/npordtests_1.1.tar.gz	/opt/rdepot/repositories/2/8436419/npordtests_1.1.tar.gz	4	20:04:17.62+00
39	2020-03-28	14	3	active	false	true	4	20:04:17.642+00
40	2020-03-28	13	3	maintainer	5	4	4	20:04:24.947+00
41	2020-03-28	15	3	maintainer	5	4	4	20:04:24.962+00
42	2020-03-28	14	3	maintainer	5	4	4	20:04:24.974+00
43	2020-03-28	11	3	maintainer	5	4	4	20:04:31.785+00
44	2020-03-28	10	3	maintainer	5	4	4	20:04:31.795+00
45	2020-03-28	12	3	maintainer	5	4	4	20:04:31.804+00
46	2020-03-28	9	3	maintainer	5	4	4	20:04:31.812+00
47	2020-03-28	10	3	maintainer	4	5	4	20:04:41.444+00
48	2020-03-28	11	3	maintainer	4	5	4	20:04:41.458+00
49	2020-03-28	9	3	maintainer	4	5	4	20:04:41.475+00
50	2020-03-28	12	3	maintainer	4	5	4	20:04:41.491+00
51	2020-03-28	11	3	active	true	false	4	20:04:47.46+00
52	2020-03-28	7	3	active	true	false	4	20:04:51.62+00
53	2020-03-28	6	3	active	true	false	4	20:04:54.588+00
54	2020-03-28	13	3	active	true	false	4	20:04:56.593+00
55	2020-03-28	13	3	maintainer	6	5	4	20:05:25.637+00
56	2020-03-28	12	3	maintainer	6	4	4	20:05:29.974+00
57	2020-03-28	11	3	maintainer	6	4	4	20:05:29.982+00
58	2020-03-28	10	3	maintainer	6	4	4	20:05:35.464+00
59	2020-03-28	9	3	maintainer	6	4	4	20:05:35.472+00
60	2020-03-28	16	1	created			7	20:05:55.782+00
61	2020-03-28	17	1	created			7	20:05:55.961+00
62	2020-03-28	18	1	created			7	20:05:56.091+00
63	2020-03-28	19	1	created			7	20:06:11.429+00
64	2020-03-28	20	1	created			7	20:06:21.099+00
65	2020-03-28	21	1	created			6	20:06:46.315+00
66	2020-03-28	22	1	created			6	20:06:46.487+00
67	2020-03-28	23	1	created			6	20:06:46.487+00
68	2020-03-28	24	1	created			6	20:07:11.207+00
69	2020-03-28	25	1	created			5	20:07:50.726+00
70	2020-03-28	26	1	created			5	20:07:50.774+00
71	2020-03-28	25	3	maintainer	5	4	5	20:07:52.823+00
72	2020-03-28	25	3	source	/opt/rdepot/new/37946660/usl_2.0.0.tar.gz	/opt/rdepot/repositories/2/33930690/usl_2.0.0.tar.gz	5	20:07:52.812+00
73	2020-03-28	25	3	active	false	true	5	20:07:52.845+00
74	2020-03-28	17	3	maintainer	5	4	5	20:08:12.215+00
75	2020-03-28	17	3	source	/opt/rdepot/new/30320032/A3_0.9.2.tar.gz	/opt/rdepot/repositories/2/9907084/A3_0.9.2.tar.gz	5	20:08:12.21+00
76	2020-03-28	17	3	active	false	true	5	20:08:12.231+00
77	2020-03-28	18	3	maintainer	5	4	5	20:08:18.25+00
78	2020-03-28	18	3	source	/opt/rdepot/new/26771812/abc_1.3.tar.gz	/opt/rdepot/repositories/2/88170013/abc_1.3.tar.gz	5	20:08:18.243+00
79	2020-03-28	18	3	active	false	true	5	20:08:18.273+00
80	2020-03-28	21	3	maintainer	5	4	5	20:08:23.538+00
81	2020-03-28	21	3	source	/opt/rdepot/new/19806985/Benchmarking_0.10.tar.gz	/opt/rdepot/repositories/2/71228208/Benchmarking_0.10.tar.gz	5	20:08:23.532+00
82	2020-03-28	21	3	active	false	true	5	20:08:23.552+00
83	2020-03-28	20	3	maintainer	5	4	5	20:08:41.763+00
84	2020-03-28	20	3	source	/opt/rdepot/new/9104202/AnaCoDa_0.1.2.3.tar.gz	/opt/rdepot/repositories/5/39437028/AnaCoDa_0.1.2.3.tar.gz	5	20:08:41.753+00
85	2020-03-28	20	3	active	false	true	5	20:08:41.776+00
86	2020-03-28	6	2	delete	false	true	4	20:09:48.101+00
87	2020-03-28	14	2	delete	false	true	5	20:10:08.508+00
88	2020-03-28	27	1	created			4	20:12:42.433+00
89	2020-03-28	28	1	created			4	20:12:42.512+00
90	2020-03-28	27	3	source	/opt/rdepot/new/73393322/usl_2.0.0.tar.gz	/opt/rdepot/repositories/6/21695389/usl_2.0.0.tar.gz	4	20:12:44.529+00
91	2020-03-28	27	3	active	false	true	4	20:12:44.539+00
92	2020-03-28	28	3	source	/opt/rdepot/new/28573212/visdat_0.1.0.tar.gz	/opt/rdepot/repositories/6/70325377/visdat_0.1.0.tar.gz	4	20:12:44.643+00
93	2020-03-28	28	3	active	false	true	4	20:12:44.653+00
94	2020-03-28	28	2	delete	false	true	4	20:13:30.626+00
95	2020-03-28	27	2	delete	false	true	4	20:13:30.732+00
96	2020-03-28	29	1	created			4	20:14:04.855+00
97	2020-03-28	29	3	source	/opt/rdepot/new/33345471/A3_0.9.1.tar.gz	/opt/rdepot/repositories/7/67484296/A3_0.9.1.tar.gz	4	20:14:06.597+00
98	2020-03-28	29	3	active	false	true	4	20:14:06.609+00
99	2020-03-28	29	2	delete	false	true	4	20:14:17.882+00
100	2020-03-28	30	1	created			6	20:14:42.968+00
101	2020-03-28	31	1	created			6	20:14:43.1+00
102	2020-03-29	15	3	maintainer	6	5	4	10:43:05.243+00
103	2020-03-29	15	3	maintainer	5	6	4	10:43:10.081+00
\.


--
-- Data for Name: package_maintainer; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.package_maintainer (id, user_id, package, repository_id, deleted) FROM stdin;
1	6	accrued	2	f
2	6	abc	4	f
3	6	A3	4	f
4	6	bea.R	2	t
\.


--
-- Data for Name: package_maintainer_event; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.package_maintainer_event (id, date, package_maintainer_id, event_id, changed_variable, value_before, value_after, changed_by, "time") FROM stdin;
1	2020-03-28	1	1	created			4	20:05:25.656+00
2	2020-03-28	2	1	created			4	20:05:29.987+00
3	2020-03-28	3	1	created			4	20:05:35.477+00
4	2020-03-29	4	1	created			4	10:43:05.991+00
5	2020-03-29	4	2	deleted			4	10:43:10.333+00
\.



--
-- Data for Name: repository_event; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.repository_event (id, date, repository_id, event_id, changed_variable, value_before, value_after, changed_by, "time") FROM stdin;
11	2020-03-28	2	3	publication URI	http://localhost/testrepo1	http://localhost/repo/testrepo1	4	20:01:11.498+00
12	2020-03-28	2	3	server address	http://localhost/testrepo1	http://oa-rdepot-repo:8080/testrepo1	4	20:01:13.018+00
13	2020-03-28	3	3	submitted		4	4	20:03:44.661+00
14	2020-03-28	3	3	submitted		5	4	20:03:44.678+00
15	2020-03-28	3	3	submitted		6	4	20:03:44.702+00
16	2020-03-28	3	3	version	0	1	4	20:03:44.837+00
17	2020-03-28	3	3	version	0	1	4	20:03:44.844+00
18	2020-03-28	3	3	version	0	1	4	20:03:44.846+00
19	2020-03-28	3	3	version	1	2	4	20:03:44.908+00
20	2020-03-28	3	3	version	1	2	4	20:03:44.907+00
21	2020-03-28	3	3	version	1	2	4	20:03:44.919+00
22	2020-03-28	3	3	version	2	3	4	20:03:44.924+00
23	2020-03-28	3	3	version	2	3	4	20:03:44.923+00
24	2020-03-28	3	3	added		8	4	20:03:44.932+00
25	2020-03-28	3	3	added		4	4	20:03:44.938+00
26	2020-03-28	3	3	version	2	3	4	20:03:44.941+00
27	2020-03-28	3	3	added		6	4	20:03:44.957+00
28	2020-03-28	3	3	submitted		7	4	20:03:45.185+00
29	2020-03-28	3	3	submitted		8	4	20:03:45.203+00
30	2020-03-28	3	3	version	0	1	4	20:03:45.249+00
31	2020-03-28	3	3	version	0	1	4	20:03:45.274+00
32	2020-03-28	3	3	version	1	2	4	20:03:45.28+00
33	2020-03-28	3	3	version	2	3	4	20:03:45.286+00
34	2020-03-28	3	3	added		5	4	20:03:45.291+00
35	2020-03-28	3	3	version	1	2	4	20:03:45.301+00
36	2020-03-28	3	3	version	2	3	4	20:03:45.306+00
37	2020-03-28	3	3	added		7	4	20:03:45.31+00
38	2020-03-28	4	3	submitted		9	4	20:03:58.911+00
39	2020-03-28	4	3	submitted		10	4	20:03:58.917+00
40	2020-03-28	4	3	version	0	1	4	20:03:58.994+00
41	2020-03-28	4	3	version	0	1	4	20:03:59.003+00
42	2020-03-28	4	3	version	1	2	4	20:03:59.03+00
43	2020-03-28	4	3	version	2	3	4	20:03:59.037+00
44	2020-03-28	4	3	added		10	4	20:03:59.043+00
45	2020-03-28	4	3	version	1	2	4	20:03:59.071+00
46	2020-03-28	4	3	version	2	3	4	20:03:59.083+00
47	2020-03-28	4	3	added		9	4	20:03:59.095+00
48	2020-03-28	4	3	submitted		11	4	20:03:59.597+00
49	2020-03-28	4	3	version	0	1	4	20:03:59.648+00
50	2020-03-28	4	3	version	1	2	4	20:03:59.692+00
51	2020-03-28	4	3	version	2	3	4	20:03:59.699+00
52	2020-03-28	4	3	added		11	4	20:03:59.704+00
53	2020-03-28	4	3	submitted		12	4	20:03:59.786+00
54	2020-03-28	4	3	version	0	1	4	20:03:59.828+00
55	2020-03-28	4	3	version	1	2	4	20:03:59.862+00
56	2020-03-28	4	3	version	2	3	4	20:03:59.868+00
57	2020-03-28	4	3	added		12	4	20:03:59.873+00
58	2020-03-28	2	3	submitted		13	4	20:04:17.1+00
59	2020-03-28	2	3	version	0	1	4	20:04:17.178+00
60	2020-03-28	2	3	version	1	2	4	20:04:17.23+00
61	2020-03-28	2	3	version	2	3	4	20:04:17.242+00
62	2020-03-28	2	3	added		15	4	20:04:17.251+00
63	2020-03-28	2	3	submitted		14	4	20:04:17.504+00
64	2020-03-28	2	3	version	0	1	4	20:04:17.53+00
65	2020-03-28	2	3	version	1	2	4	20:04:17.558+00
66	2020-03-28	2	3	version	2	3	4	20:04:17.562+00
67	2020-03-28	2	3	added		13	4	20:04:17.566+00
68	2020-03-28	2	3	submitted		15	4	20:04:17.605+00
69	2020-03-28	2	3	version	0	1	4	20:04:17.635+00
70	2020-03-28	2	3	version	1	2	4	20:04:17.66+00
71	2020-03-28	2	3	version	2	3	4	20:04:17.664+00
72	2020-03-28	2	3	added		14	4	20:04:17.668+00
73	2020-03-28	2	3	version	3	4	4	20:04:24.952+00
74	2020-03-28	2	3	version	4	5	4	20:04:24.966+00
75	2020-03-28	2	3	version	5	6	4	20:04:24.978+00
76	2020-03-28	4	3	version	3	4	4	20:04:31.788+00
77	2020-03-28	4	3	version	4	5	4	20:04:31.798+00
78	2020-03-28	4	3	version	5	6	4	20:04:31.806+00
79	2020-03-28	4	3	version	6	7	4	20:04:31.814+00
80	2020-03-28	4	3	version	7	8	4	20:04:41.448+00
81	2020-03-28	4	3	version	8	9	4	20:04:41.462+00
82	2020-03-28	4	3	version	9	10	4	20:04:41.479+00
83	2020-03-28	4	3	version	10	11	4	20:04:41.495+00
84	2020-03-28	4	3	version	11	12	4	20:04:47.472+00
85	2020-03-28	3	3	version	3	4	4	20:04:51.631+00
86	2020-03-28	3	3	version	4	5	4	20:04:54.599+00
87	2020-03-28	2	3	version	6	7	4	20:04:56.601+00
88	2020-03-28	2	3	version	7	8	4	20:05:25.64+00
89	2020-03-28	4	3	version	12	13	4	20:05:29.977+00
90	2020-03-28	4	3	version	13	14	4	20:05:29.984+00
91	2020-03-28	4	3	version	14	15	4	20:05:35.467+00
92	2020-03-28	4	3	version	15	16	4	20:05:35.474+00
93	2020-03-28	2	3	submitted		16	7	20:05:58.313+00
94	2020-03-28	2	3	submitted		17	7	20:05:58.441+00
95	2020-03-28	2	3	submitted		18	7	20:05:58.693+00
96	2020-03-28	3	3	submitted		19	7	20:06:13.348+00
97	2020-03-28	5	3	submitted		20	7	20:06:23.307+00
98	2020-03-28	2	3	submitted		21	6	20:06:48.789+00
99	2020-03-28	2	3	submitted		22	6	20:06:49.159+00
100	2020-03-28	2	3	submitted		23	6	20:06:49.565+00
101	2020-03-28	5	3	submitted		24	6	20:07:13.155+00
102	2020-03-28	2	3	submitted		25	5	20:07:52.793+00
103	2020-03-28	2	3	version	8	9	5	20:07:52.829+00
104	2020-03-28	2	3	version	9	10	5	20:07:52.838+00
105	2020-03-28	3	3	submitted		26	5	20:07:52.854+00
106	2020-03-28	2	3	version	10	11	5	20:07:52.858+00
107	2020-03-28	2	3	version	11	12	5	20:07:52.863+00
108	2020-03-28	2	3	added		25	5	20:07:52.866+00
109	2020-03-28	2	3	version	12	13	5	20:08:12.221+00
110	2020-03-28	2	3	version	13	14	5	20:08:12.227+00
111	2020-03-28	2	3	version	14	15	5	20:08:12.246+00
112	2020-03-28	2	3	version	15	16	5	20:08:12.251+00
113	2020-03-28	2	3	added		17	5	20:08:12.254+00
114	2020-03-28	2	3	version	16	17	5	20:08:18.255+00
115	2020-03-28	2	3	version	17	18	5	20:08:18.268+00
116	2020-03-28	2	3	version	18	19	5	20:08:18.286+00
117	2020-03-28	2	3	version	19	20	5	20:08:18.296+00
118	2020-03-28	2	3	added		18	5	20:08:18.299+00
119	2020-03-28	2	3	version	20	21	5	20:08:23.543+00
120	2020-03-28	2	3	version	21	22	5	20:08:23.549+00
121	2020-03-28	2	3	version	22	23	5	20:08:23.562+00
122	2020-03-28	2	3	version	23	24	5	20:08:23.565+00
123	2020-03-28	2	3	added		21	5	20:08:23.569+00
124	2020-03-28	5	3	version	0	1	5	20:08:41.767+00
125	2020-03-28	5	3	version	1	2	5	20:08:41.772+00
126	2020-03-28	5	3	version	2	3	5	20:08:41.783+00
127	2020-03-28	5	3	version	3	4	5	20:08:41.786+00
128	2020-03-28	5	3	added		20	5	20:08:41.789+00
129	2020-03-28	2	3	published			5	20:08:48.105+00
130	2020-03-28	2	3	version	24	25	5	20:08:48.109+00
131	2020-03-28	5	3	published			5	20:08:51.742+00
132	2020-03-28	5	3	version	4	5	5	20:08:51.745+00
133	2020-03-28	5	3	published	true	false	5	20:08:53.749+00
134	2020-03-28	5	3	version	5	6	5	20:08:53.753+00
135	2020-03-28	3	3	published			4	20:09:06.488+00
136	2020-03-28	3	3	version	5	6	4	20:09:06.491+00
137	2020-03-28	4	3	published			4	20:09:09.17+00
138	2020-03-28	4	3	version	16	17	4	20:09:09.173+00
139	2020-03-28	4	3	published	true	false	4	20:09:15.758+00
140	2020-03-28	4	3	version	17	18	4	20:09:15.761+00
141	2020-03-28	3	3	version	6	7	4	20:09:48.11+00
142	2020-03-28	3	3	published			4	20:09:48.125+00
143	2020-03-28	3	3	version	7	8	4	20:09:48.128+00
144	2020-03-28	2	3	version	25	26	5	20:10:08.519+00
145	2020-03-28	2	3	published			5	20:10:08.531+00
146	2020-03-28	2	3	version	26	27	5	20:10:08.533+00
147	2020-03-28	6	3	submitted		27	4	20:12:44.515+00
148	2020-03-28	6	3	version	0	1	4	20:12:44.536+00
149	2020-03-28	6	3	version	1	2	4	20:12:44.549+00
150	2020-03-28	6	3	version	2	3	4	20:12:44.551+00
151	2020-03-28	6	3	added		27	4	20:12:44.556+00
152	2020-03-28	6	3	submitted		28	4	20:12:44.625+00
153	2020-03-28	6	3	version	0	1	4	20:12:44.65+00
154	2020-03-28	6	3	version	1	2	4	20:12:44.666+00
155	2020-03-28	6	3	version	2	3	4	20:12:44.671+00
156	2020-03-28	6	3	added		28	4	20:12:44.677+00
157	2020-03-28	6	3	published			4	20:13:27.926+00
158	2020-03-28	6	3	version	3	4	4	20:13:27.934+00
159	2020-03-28	6	3	version	4	5	4	20:13:30.639+00
160	2020-03-28	6	3	published			4	20:13:30.652+00
161	2020-03-28	6	3	version	5	6	4	20:13:30.655+00
162	2020-03-28	6	3	version	6	7	4	20:13:30.743+00
163	2020-03-28	6	3	published			4	20:13:30.754+00
164	2020-03-28	6	3	version	7	8	4	20:13:30.756+00
165	2020-03-28	6	3	published	true	false	4	20:13:30.789+00
166	2020-03-28	6	3	version	8	9	4	20:13:30.791+00
167	2020-03-28	6	2	deleted			4	20:13:30.795+00
168	2020-03-28	7	3	submitted		29	4	20:14:06.588+00
169	2020-03-28	7	3	version	0	1	4	20:14:06.604+00
170	2020-03-28	7	3	version	1	2	4	20:14:06.616+00
171	2020-03-28	7	3	version	2	3	4	20:14:06.618+00
172	2020-03-28	7	3	added		29	4	20:14:06.622+00
173	2020-03-28	7	3	published			4	20:14:10.687+00
174	2020-03-28	7	3	version	3	4	4	20:14:10.689+00
175	2020-03-28	7	3	published	true	false	4	20:14:14.278+00
176	2020-03-28	7	3	version	4	5	4	20:14:14.281+00
177	2020-03-28	7	3	version	5	6	4	20:14:17.895+00
178	2020-03-28	7	2	deleted			4	20:14:19.412+00
179	2020-03-28	5	3	submitted		30	6	20:14:44.965+00
180	2020-03-28	5	3	submitted		31	6	20:14:45.738+00
181	2020-03-29	2	3	version	27	28	4	10:43:05.272+00
182	2020-03-29	2	3	published			4	10:43:05.321+00
183	2020-03-29	2	3	version	28	29	4	10:43:05.331+00
184	2020-03-29	2	3	version	29	30	4	10:43:10.091+00
185	2020-03-29	2	3	published			4	10:43:10.117+00
186	2020-03-29	2	3	version	30	31	4	10:43:10.123+00
\.


--
-- Data for Name: repository_maintainer; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.repository_maintainer (id, user_id, repository_id, deleted) FROM stdin;
1	5	2	f
3	5	5	f
2	5	4	t
\.


--
-- Data for Name: repository_maintainer_event; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.repository_maintainer_event (id, date, repository_maintainer_id, event_id, changed_variable, value_before, value_after, changed_by, "time") FROM stdin;
1	2020-03-28	1	1	created			4	20:04:24.995+00
2	2020-03-28	2	1	created			4	20:04:31.817+00
3	2020-03-28	3	1	created			4	20:04:36.897+00
4	2020-03-28	2	2	deleted		Sat Mar 28 20:04:41 UTC 2020	4	20:04:41.499+00
\.


--
-- Data for Name: rpackage; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.rpackage (id, depends, imports, suggests, system_requirements, license, md5sum) FROM stdin;
8	R (>= 3.0), grid	\N	\N	\N	GPL-3	70d295115295a4718593f6a39d77add9
4	R (>= 2.14.1), grid	\N	\N	\N	GPL-3	19f8aec67250bd2ac481b14b50413d03
10	R (>= 2.15.0), xtable, pbapply	\N	randomForest, e1071	\N	GPL (>= 2)	76d726aee8dd7c6ed94d150d5718015b
5	R (>= 2.14.1), grid	\N	\N	\N	GPL-3	a05e4ca44438c0d9e7d713d7e3890423
9	xtable, pbapply	\N	randomForest, e1071	\N	GPL (>= 2)	8eb4760cd574f5489e61221dc9bb0076
19	R (>= 3.2.2)	ggplot2, tidyr, dplyr, purrr, magrittr, stats	plotly (>= 4.5.6), testthat, knitr, rmarkdown, vdiffr	\N	MIT + file LICENSE	f343fa3a01dcd9546fa0947877f58f36
7	R (>= 2.14.1), grid	\N	\N	\N	GPL-3	97c2930a9dd7ca9fc1409d5340c06470
14	R (>= 2.15.0)	\N	\N	\N	GPL (>= 2)	da8be1247d3145b757bd62e01fc6eb8b
17	R (>= 2.15.0), xtable, pbapply	\N	randomForest, e1071	\N	GPL (>= 2)	76d726aee8dd7c6ed94d150d5718015b
13	R (>= 3.0), grid	\N	\N	\N	GPL-3	1c75d59b18e554a285a9b156a06a288c
11	R (>= 2.10), nnet, quantreg, locfit	\N	\N	\N	GPL (>= 3)	c47d18b86b331a5023dcd62b74fedbb6
12	R (>= 1.8.0), nnet, quantreg, locfit, methods	\N	\N	\N	Unlimited	91599204c92275ed4b36d55e8d7c144b
18	R (>= 2.10), nnet, quantreg, locfit	\N	\N	\N	GPL (>= 3)	c47d18b86b331a5023dcd62b74fedbb6
20	R (>= 3.3.0), Rcpp (>= 0.11.3), methods		knitr, Hmisc, VGAM, coda, testthat, lmodel2	\N	GPL (>= 2)	41026e4157a0b3b6d909f0c6f72fa65c
6	R (>= 3.0), grid	\N	\N	\N	GPL-3	24b8cec280424dfc6a9e444fa57ba9f3
22	R (>= 3.3.0), Rcpp (>= 0.11.3), methods		knitr, Hmisc, VGAM, coda, testthat, lmodel2	\N	GPL (>= 2)	41026e4157a0b3b6d909f0c6f72fa65c
26	R (>= 3.0), methods	graphics, stats, nlsr	knitr	\N	BSD_2_clause + file LICENSE	868140a3c3c29327eef5d5a485aee5b6
25	R (>= 3.0), methods	graphics, stats, nlsr	knitr	\N	BSD_2_clause + file LICENSE	868140a3c3c29327eef5d5a485aee5b6
16	xtable, pbapply	\N	randomForest, e1071	\N	GPL (>= 2)	8eb4760cd574f5489e61221dc9bb0076
21	lpSolveAPI, ucminf	\N	\N	\N	GPL (>= 2)	9a99c2ebefa6d49422ca7893c1f4ead8
23	R (>= 3.2.2)	ggplot2, tidyr, dplyr, purrr, magrittr, stats	plotly (>= 4.5.6), testthat, knitr, rmarkdown, vdiffr	\N	MIT + file LICENSE	f343fa3a01dcd9546fa0947877f58f36
24	lpSolveAPI, ucminf	\N	\N	\N	GPL (>= 2)	9a99c2ebefa6d49422ca7893c1f4ead8
30	xtable, pbapply	\N	randomForest, e1071	\N	GPL (>= 2)	8eb4760cd574f5489e61221dc9bb0076
31	R (>= 1.8.0), nnet, quantreg, locfit, methods	\N	\N	\N	Unlimited	91599204c92275ed4b36d55e8d7c144b
28	R (>= 3.2.2)	ggplot2, tidyr, dplyr, purrr, magrittr, stats	plotly (>= 4.5.6), testthat, knitr, rmarkdown, vdiffr	\N	MIT + file LICENSE	f343fa3a01dcd9546fa0947877f58f36
27	R (>= 3.0), methods	graphics, stats, nlsr	knitr	\N	BSD_2_clause + file LICENSE	868140a3c3c29327eef5d5a485aee5b6
29	xtable, pbapply	\N	randomForest, e1071	\N	GPL (>= 2)	8eb4760cd574f5489e61221dc9bb0076
15	R (>= 3.2.1), data.table	httr, DT, shiny, jsonlite, googleVis, shinydashboard, ggplot2, stringr, chron, gtable, scales, htmltools, httpuv, xtable, stringi, magrittr, htmlwidgets, Rcpp, munsell, colorspace, plyr, yaml	\N	\N	CC0	5e664f320c7cc884138d64467f6b0e49
\.


--
-- Data for Name: rrepository; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.rrepository (id) FROM stdin;
5
4
3
6
7
2
\.


--
-- Data for Name: submission; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.submission (id, submitter_id, package_id, changes, accepted, deleted) FROM stdin;
6	4	8	\N	t	f
5	4	4	\N	t	f
7	4	5	\N	t	f
8	4	7	\N	t	f
10	4	10	\N	t	f
9	4	9	\N	t	f
11	4	11	\N	t	f
12	4	12	\N	t	f
13	4	15	\N	t	f
14	4	13	\N	t	f
19	7	19	\N	f	f
23	6	22	\N	f	t
26	5	26	\N	f	f
25	5	25	\N	t	f
16	7	16	\N	f	t
17	7	17	\N	t	f
18	7	18	\N	t	f
21	6	21	\N	t	f
22	6	23	\N	f	t
20	7	20	\N	t	f
24	6	24	\N	f	t
4	4	6	\N	t	t
15	4	14	\N	t	t
28	4	28	\N	t	t
27	4	27	\N	t	t
29	4	29	\N	t	t
30	6	30	\N	f	f
31	6	31	\N	f	f
\.


--
-- Data for Name: submission_event; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.submission_event (id, date, submission_id, event_id, changed_variable, value_before, value_after, changed_by, "time") FROM stdin;
4	2020-03-28	4	1	created			4	20:03:44.651+00
5	2020-03-28	5	1	created			4	20:03:44.647+00
6	2020-03-28	6	1	created			4	20:03:44.686+00
7	2020-03-28	6	3	accepted	false	true	4	20:03:44.733+00
8	2020-03-28	5	3	accepted	false	true	4	20:03:44.717+00
9	2020-03-28	4	3	accepted	false	true	4	20:03:44.725+00
10	2020-03-28	7	1	created			4	20:03:45.18+00
11	2020-03-28	8	1	created			4	20:03:45.2+00
12	2020-03-28	7	3	accepted	false	true	4	20:03:45.192+00
13	2020-03-28	8	3	accepted	false	true	4	20:03:45.219+00
14	2020-03-28	9	1	created			4	20:03:58.906+00
15	2020-03-28	10	1	created			4	20:03:58.913+00
16	2020-03-28	10	3	accepted	false	true	4	20:03:58.928+00
17	2020-03-28	9	3	accepted	false	true	4	20:03:58.922+00
18	2020-03-28	11	1	created			4	20:03:59.593+00
19	2020-03-28	11	3	accepted	false	true	4	20:03:59.606+00
20	2020-03-28	12	1	created			4	20:03:59.783+00
21	2020-03-28	12	3	accepted	false	true	4	20:03:59.793+00
22	2020-03-28	13	1	created			4	20:04:17.091+00
23	2020-03-28	13	3	accepted	false	true	4	20:04:17.118+00
24	2020-03-28	14	1	created			4	20:04:17.502+00
25	2020-03-28	14	3	accepted	false	true	4	20:04:17.51+00
26	2020-03-28	15	1	created			4	20:04:17.604+00
27	2020-03-28	15	3	accepted	false	true	4	20:04:17.611+00
28	2020-03-28	16	1	created			7	20:05:58.312+00
29	2020-03-28	17	1	created			7	20:05:58.439+00
30	2020-03-28	18	1	created			7	20:05:58.691+00
31	2020-03-28	19	1	created			7	20:06:13.346+00
32	2020-03-28	20	1	created			7	20:06:23.306+00
33	2020-03-28	21	1	created			6	20:06:48.787+00
34	2020-03-28	22	1	created			6	20:06:49.157+00
35	2020-03-28	23	1	created			6	20:06:49.563+00
36	2020-03-28	23	2	deleted			6	20:07:00.372+00
37	2020-03-28	24	1	created			6	20:07:13.152+00
38	2020-03-28	25	1	created			5	20:07:52.791+00
39	2020-03-28	26	1	created			5	20:07:52.85+00
40	2020-03-28	25	3	accepted	false	true	5	20:07:52.799+00
41	2020-03-28	16	2	deleted			5	20:08:08.855+00
42	2020-03-28	17	3	accepted	false	true	5	20:08:12.2+00
43	2020-03-28	18	3	accepted	false	true	5	20:08:18.23+00
44	2020-03-28	21	3	accepted	false	true	5	20:08:23.526+00
45	2020-03-28	22	2	deleted			5	20:08:31.542+00
46	2020-03-28	20	3	accepted	false	true	5	20:08:41.718+00
47	2020-03-28	24	2	deleted			5	20:08:42.886+00
48	2020-03-28	4	2	deleted			4	20:09:48.106+00
49	2020-03-28	15	2	deleted			5	20:10:08.515+00
50	2020-03-28	27	1	created			4	20:12:44.514+00
51	2020-03-28	27	3	accepted	false	true	4	20:12:44.519+00
52	2020-03-28	28	1	created			4	20:12:44.624+00
53	2020-03-28	28	3	accepted	false	true	4	20:12:44.629+00
54	2020-03-28	28	2	deleted			4	20:13:30.635+00
55	2020-03-28	27	2	deleted			4	20:13:30.736+00
56	2020-03-28	29	1	created			4	20:14:06.587+00
57	2020-03-28	29	3	accepted	false	true	4	20:14:06.591+00
58	2020-03-28	29	2	deleted			4	20:14:17.891+00
59	2020-03-28	30	1	created			6	20:14:44.964+00
60	2020-03-28	31	1	created			6	20:14:45.737+00
\.


--
-- Data for Name: user_event; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.user_event (id, date, user_id, event_id, changed_variable, value_before, value_after, changed_by, "time") FROM stdin;
1	2020-03-28	4	1	created			4	20:59:28.800514+01
2	2020-03-28	5	1	created			5	20:59:28.81074+01
3	2020-03-28	6	1	created			6	20:59:28.82206+01
4	2020-03-28	7	1	created			7	20:59:28.830132+01
5	2020-03-28	8	1	created			8	20:59:28.838152+01
9	2020-03-28	4	3	last logged in	null	Sat Mar 28 20:00:13 UTC 2020	4	20:00:14.007+00
10	2020-03-28	8	3	active	true	false	4	20:03:28.868+00
11	2020-03-28	7	3	last logged in	null	Sat Mar 28 20:05:47 UTC 2020	4	20:05:47.228+00
12	2020-03-28	6	3	last logged in	null	Sat Mar 28 20:06:31 UTC 2020	4	20:06:31.773+00
13	2020-03-28	5	3	last logged in	null	Sat Mar 28 20:07:31 UTC 2020	4	20:07:31.17+00
14	2020-03-28	4	3	last logged in	2020-03-28 00:00:00.0	Sat Mar 28 20:09:02 UTC 2020	4	20:09:02.061+00
15	2020-03-28	5	3	last logged in	2020-03-28 00:00:00.0	Sat Mar 28 20:09:56 UTC 2020	8	20:09:56.183+00
16	2020-03-28	4	3	last logged in	2020-03-28 00:00:00.0	Sat Mar 28 20:12:06 UTC 2020	8	20:12:06.32+00
17	2020-03-28	6	3	last logged in	2020-03-28 00:00:00.0	Sat Mar 28 20:14:30 UTC 2020	8	20:14:30.998+00
18	2020-03-29	5	3	last logged in	2020-03-28 00:00:00.0	Sun Mar 29 10:42:45 UTC 2020	8	10:42:45.4+00
19	2020-08-20	9	1	created			8	09:58:51.784+00
20	2020-08-20	9	3	last logged in	null	Thu Aug 20 09:58:52 GMT 2020	8	09:58:52.09+00
21	2020-08-20	4	3	last logged in	2020-03-28 00:00:00.0	Thu Aug 20 09:59:08 GMT 2020	8	09:59:08.9+00
22	2020-08-20	9	3	active	true	false	4	09:59:21.132+00
23	2020-08-25	10	1	created			8	12:35:38.689+00
24	2020-08-25	10	3	last logged in	null	Tue Aug 25 12:35:38 GMT 2020	8	12:35:38.788+00
\.


--
-- Name: Api_token_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public."Api_token_id_seq"', 5, true);


--
-- Name: Event_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public."Event_id_seq"', 3, true);


--
-- Name: PackageEvent_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public."PackageEvent_id_seq"', 103, true);


--
-- Name: PackageMaintainerEvent_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public."PackageMaintainerEvent_id_seq"', 5, true);


--
-- Name: PackageMaintainer_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public."PackageMaintainer_id_seq"', 4, true);


--
-- Name: Package_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public."Package_id_seq"', 31, true);


--
-- Name: RepositoryEvent_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public."RepositoryEvent_id_seq"', 186, true);


--
-- Name: RepositoryMaintainerEvent_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public."RepositoryMaintainerEvent_id_seq"', 4, true);


--
-- Name: RepositoryMaintainer_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public."RepositoryMaintainer_id_seq"', 3, true);


--
-- Name: Repository_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public."Repository_id_seq"', 7, true);


--
-- Name: Role_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public."Role_id_seq"', 4, true);


--
-- Name: SubmissionEvent_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public."SubmissionEvent_id_seq"', 60, true);


--
-- Name: Submission_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public."Submission_id_seq"', 31, true);


--
-- Name: UserEvent_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public."UserEvent_id_seq"', 24, true);


--
-- Name: User_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public."User_id_seq"', 10, true);

--
-- PostgreSQL database dump complete
--
