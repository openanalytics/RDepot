--
-- PostgreSQL database dump
--

-- Dumped from database version 15.2
-- Dumped by pg_dump version 15.2



--
-- Data for Name: user; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public."user" (id, role_id, name, email, login, active, last_logged_in_on, deleted, created_on) FROM stdin;
8	4	Local Admin User	admin@localhost	admin	f	\N	f	1970-01-01
7	1	Isaac Newton	newton@ldap.forumsys.com	newton	t	2020-03-28	f	1970-01-01
6	2	Galileo Galilei	galieleo@ldap.forumsys.com	galieleo	t	2020-03-28	f	1970-01-01
5	3	Nikola Tesla	tesla@ldap.forumsys.com	tesla	t	2020-03-29	f	1970-01-01
4	4	Albert Einstein	einstein@ldap.forumsys.com	einstein	t	2020-08-20	f	1970-01-01
9	1	John Doe	doe@localhost	doe	f	2020-08-20	f	1970-01-01
10	1	Alfred Tarski	tarski@localhost	tarski	t	2020-08-25	f	1970-01-01
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

COPY public.repository (id, version, publication_uri, name, server_address, published, deleted, resource_technology, last_publication_successful, last_publication_timestamp, last_modified_timestamp, requires_authentication) FROM stdin;
5	6	http://localhost/repo/testrepo4	testrepo4	http://oa-rdepot-repo:8080/testrepo4	f	f	R	f	\N	'2024-06-21 12:30:25'	t
4	18	http://localhost/repo/testrepo3	testrepo3	http://oa-rdepot-repo:8080/testrepo3	f	f	R	f	\N	'2024-04-27 12:30:25'	t
3	8	http://localhost/repo/testrepo2	testrepo2	http://oa-rdepot-repo:8080/testrepo2	t	f	R	t	'2024-06-27 12:30:25'	'2024-06-27 12:30:25'	t
6	9	http://localhost/repo/testrepo5	testrepo5	http://oa-rdepot-repo:8080/testrepo5	f	t	R	f	\N	'2024-06-27 12:30:25'	t
7	6	http://localhost/repo/testrepo6	testrepo6	http://oa-rdepot-repo:8080/testrepo6	f	t	R	f	\N	'2024-04-27 12:30:25'	f
2	31	http://localhost/repo/testrepo1	testrepo1	http://oa-rdepot-repo:8080/testrepo1	t	f	R	t	'2020-03-28 12:30:25'	'2024-04-24 12:30:25'	f
11	1	http://localhost/repo/testrepo11	testrepo11	http://oa-rdepot-repo:8080/testrepo11	t	t	Python	t	'2020-03-28 12:30:25'	'2020-03-28 12:30:25'	t
12	1	http://localhost/repo/testrepo12	testrepo12	http://oa-rdepot-repo:8080/testrepo12	f	t	Python	f	\N	'2024-03-23 12:30:25'	f
10	7	http://localhost/repo/testrepo10	testrepo10	http://oa-rdepot-repo:8080/testrepo10	f	f	Python	f	\N	'2024-03-23 12:30:25'	f
9	4	http://localhost/repo/testrepo9	testrepo9	http://oa-rdepot-repo:8080/testrepo9	f	f	Python	f	\N	'2024-04-27 12:30:25'	t
8	7	http://localhost/repo/testrepo8	testrepo8	http://oa-rdepot-repo:8080/testrepo8	t	f	Python	t	'2020-03-28 12:30:25'	'2018-04-01 12:30:25'	t
\.

--
-- Data for Name: package; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.package (id, name, version, description, author, user_maintainer_id, repository_id, url, source, title, active, deleted, resource_technology) FROM stdin;
8	accrued	1.2	Package for visualizing data quality of partially accruing time series.	Julie Eaton and Ian Painter	4	3	\N	/opt/rdepot/repositories/3/83118397/accrued_1.2.tar.gz	Visualization tools for partially accruing data	t	f	R
4	accrued	1.3.5	Package for visualizing data quality of partially accruing data.	Julie Eaton and Ian Painter	4	3	\N	/opt/rdepot/repositories/3/99077116/accrued_1.3.5.tar.gz	Data Quality Visualization Tools for Partially Accruing Data	t	f	R
10	A3	0.9.2	This package supplies tools for tabulating and analyzing the results of predictive models. The methods employed are applicable to virtually any predictive model and make comparisons between different methodologies straightforward.	Scott Fortmann-Roe	6	4	\N	/opt/rdepot/repositories/4/54491936/A3_0.9.2.tar.gz	A3: Accurate, Adaptable, and Accessible Error Metrics for Predictive Models	t	f	R
5	accrued	1.3	Package for visualizing data quality of partially accruing data.	Julie Eaton and Ian Painter	4	3	\N	/opt/rdepot/repositories/3/82197810/accrued_1.3.tar.gz	Data Quality Visualization Tools for Partially Accruing Data	t	f	R
9	A3	0.9.1	This package supplies tools for tabulating and analyzing the results of predictive models. The methods employed are applicable to virtually any predictive model and make comparisons between different methodologies straightforward.	Scott Fortmann-Roe	6	4	\N	/opt/rdepot/repositories/4/47098069/A3_0.9.1.tar.gz	A3: Accurate, Adaptable, and Accessible Error Metrics for Predictive Models	t	f	R
19	visdat	0.1.0	Create preliminary exploratory data visualisations of an entire dataset to identify problems or unexpected features using 'ggplot2'.	Nicholas Tierney [aut, cre]	4	3	https://github.com/njtierney/visdat/	/opt/rdepot/new/70032548/visdat_0.1.0.tar.gz	Preliminary Data Visualisation	f	f	R
7	accrued	1.4	Package for visualizing data quality of partially accruing data.	Julie Eaton and Ian Painter	4	3	\N	/opt/rdepot/repositories/3/28075835/accrued_1.4.tar.gz	Data Quality Visualization Tools for Partially Accruing Data	f	f	R
14	npordtests	1.1	Performs nonparametric tests for equality of location against ordered alternatives.	Bulent Altunkaynak [aut, cre], Hamza Gamgam [aut]	5	2	\N	/opt/rdepot/repositories/2/8436419/npordtests_1.1.tar.gz	Nonparametric Tests for Equality of Location Against Ordered Alternatives	f	t	R
17	A3	0.9.2	This package supplies tools for tabulating and analyzing the results of predictive models. The methods employed are applicable to virtually any predictive model and make comparisons between different methodologies straightforward.	Scott Fortmann-Roe	5	2	\N	/opt/rdepot/repositories/2/9907084/A3_0.9.2.tar.gz	A3: Accurate, Adaptable, and Accessible Error Metrics for Predictive Models	t	f	R
13	accrued	1.0	Package for visualizing data quality of partially accruing time series.	Julie Eaton and Ian Painter	6	2	\N	/opt/rdepot/repositories/2/40553654/accrued_1.0.tar.gz	Visualization tools for partially accruing data	f	f	R
11	abc	1.3	The package implements several ABC algorithms for performing parameter estimation and model selection. Cross-validation tools are also available for measuring the accuracy of ABC estimates, and to calculate the misclassification probabilities of different models.	Katalin Csillery, Michael Blum and Olivier Francois	6	4	\N	/opt/rdepot/repositories/4/95296712/abc_1.3.tar.gz	Tools for Approximate Bayesian Computation (ABC)	f	f	R
12	abc	1.0	The 'abc' package provides various functions for parameter estimation and model selection in an ABC framework. Three main	Katalin Csillery, with contributions from Michael Blum and Olivier Francois	6	4	\N	/opt/rdepot/repositories/4/49426769/abc_1.0.tar.gz	Functions to perform Approximate Bayesian Computation (ABC) using simulated data	t	f	R
18	abc	1.3	The package implements several ABC algorithms for performing parameter estimation and model selection. Cross-validation tools are also available for measuring the accuracy of ABC estimates, and to calculate the misclassification probabilities of different models.	Katalin Csillery, Michael Blum and Olivier Francois	5	2	\N	/opt/rdepot/repositories/2/88170013/abc_1.3.tar.gz	Tools for Approximate Bayesian Computation (ABC)	t	f	R
20	AnaCoDa	0.1.2.3	Is a collection of models to analyze genome scale codon data using a Bayesian framework. Provides visualization routines and checkpointing for model fittings. Currently published models to analyze gene data for selection on codon	Cedric Landerer [aut, cre], Gabriel Hanas [ctb], Jeremy Rogers [ctb], Alex Cope [ctb], Denizhan Pak [ctb]	6	5	https://github.com/clandere/AnaCoDa	/opt/rdepot/repositories/5/39437028/AnaCoDa_0.1.2.3.tar.gz	Analysis of Codon Data under Stationarity using a Bayesian Framework	t	f	R
6	accrued	1.1	Package for visualizing data quality of partially accruing time series.	Julie Eaton and Ian Painter	4	3	\N	/opt/rdepot/repositories/3/46950998/accrued_1.1.tar.gz	Visualization tools for partially accruing data	f	t	R
22	AnaCoDa	0.1.2.3	Is a collection of models to analyze genome scale codon data using a Bayesian framework. Provides visualization routines and checkpointing for model fittings. Currently published models to analyze gene data for selection on codon	Cedric Landerer [aut, cre], Gabriel Hanas [ctb], Jeremy Rogers [ctb], Alex Cope [ctb], Denizhan Pak [ctb]	4	2	https://github.com/clandere/AnaCoDa		Analysis of Codon Data under Stationarity using a Bayesian Framework	f	f	R
26	usl	2.0.0	The Universal Scalability Law (Gunther 2007)	Neil J. Gunther [aut], Stefan Moeding [aut, cre]	4	3	\N	/opt/rdepot/new/54345476/usl_2.0.0.tar.gz	Analyze System Scalability with the Universal Scalability Law	f	f	R
25	usl	2.0.0	The Universal Scalability Law (Gunther 2007)	Neil J. Gunther [aut], Stefan Moeding [aut, cre]	5	2	\N	/opt/rdepot/repositories/2/33930690/usl_2.0.0.tar.gz	Analyze System Scalability with the Universal Scalability Law	t	f	R
16	A3	0.9.1	This package supplies tools for tabulating and analyzing the results of predictive models. The methods employed are applicable to virtually any predictive model and make comparisons between different methodologies straightforward.	Scott Fortmann-Roe	4	2	\N		A3: Accurate, Adaptable, and Accessible Error Metrics for Predictive Models	f	f	R
21	Benchmarking	0.10	Estimates and graphs deterministic (DEA) frontier models with different technology assumptions (fdh, vrs, drs, crs, irs, add). Also handles possible slacks, peers and their weights (lambdas), optimal cost, revenue and profit allocation, super--efficiency, and mergers. A comparative method for estimating SFA efficiencies is included.	Peter Bogetoft and Lars Otto	5	2	\N	/opt/rdepot/repositories/2/71228208/Benchmarking_0.10.tar.gz	Benchmark and frontier analysis using DEA and SFA	t	f	R
23	visdat	0.1.0	Create preliminary exploratory data visualisations of an entire dataset to identify problems or unexpected features using 'ggplot2'.	Nicholas Tierney [aut, cre]	4	2	https://github.com/njtierney/visdat/		Preliminary Data Visualisation	f	f	R
24	Benchmarking	0.10	Estimates and graphs deterministic (DEA) frontier models with different technology assumptions (fdh, vrs, drs, crs, irs, add). Also handles possible slacks, peers and their weights (lambdas), optimal cost, revenue and profit allocation, super--efficiency, and mergers. A comparative method for estimating SFA efficiencies is included.	Peter Bogetoft and Lars Otto	4	5	\N		Benchmark and frontier analysis using DEA and SFA	f	f	R
30	A3	0.9.1	This package supplies tools for tabulating and analyzing the results of predictive models. The methods employed are applicable to virtually any predictive model and make comparisons between different methodologies straightforward.	Scott Fortmann-Roe	8	5	\N	/opt/rdepot/new/92253304/A3_0.9.1.tar.gz	A3: Accurate, Adaptable, and Accessible Error Metrics for Predictive Models	f	f	R
31	abc	1.0	The 'abc' package provides various functions for parameter estimation and model selection in an ABC framework. Three main	Katalin Csillery, with contributions from Michael Blum and Olivier Francois	8	5	\N	/opt/rdepot/new/51328701/abc_1.0.tar.gz	Functions to perform Approximate Bayesian Computation (ABC) using simulated data	f	f	R
28	visdat	0.1.0	Create preliminary exploratory data visualisations of an entire dataset to identify problems or unexpected features using 'ggplot2'.	Nicholas Tierney [aut, cre]	8	6	https://github.com/njtierney/visdat/	/opt/rdepot/repositories/6/70325377/visdat_0.1.0.tar.gz	Preliminary Data Visualisation	f	t	R
27	usl	2.0.0	The Universal Scalability Law (Gunther 2007)	Neil J. Gunther [aut], Stefan Moeding [aut, cre]	8	6	\N	/opt/rdepot/repositories/6/21695389/usl_2.0.0.tar.gz	Analyze System Scalability with the Universal Scalability Law	f	t	R
29	A3	0.9.1	This package supplies tools for tabulating and analyzing the results of predictive models. The methods employed are applicable to virtually any predictive model and make comparisons between different methodologies straightforward.	Scott Fortmann-Roe	8	7	\N	/opt/rdepot/repositories/7/67484296/A3_0.9.1.tar.gz	A3: Accurate, Adaptable, and Accessible Error Metrics for Predictive Models	f	t	R
15	bea.R	1.0.5	Provides an R interface for the Bureau of Economic Analysis (BEA)	Andrea Batch [aut, cre], Jeff Chen [ctb], Walt Kampas [ctb]	5	2	https://github.com/us-bea/beaR	/opt/rdepot/repositories/2/89565416/bea.R_1.0.5.tar.gz	Bureau of Economic Analysis API	t	f	R
36	wheel	0.38.0	\N	Daniel Holth	4	10	Documentation, https://wheel.readthedocs.io/, Changelog, https://wheel.readthedocs.io/en/stable/news.html, Issue Tracker, https://github.com/pypa/wheel/issues	/opt/rdepot/trash/10/37007947/wheel-0.38.0.tar.gz	\N	f	t	Python
37	wheel	0.40.0	\N	\N	4	10	Changelog, https://wheel.readthedocs.io/en/stable/news.html, Documentation, https://wheel.readthedocs.io/, Issue Tracker, https://github.com/pypa/wheel/issues	/opt/rdepot/repositories/10/77877521/wheel-0.40.0.tar.gz	\N	t	f	Python
34	numpy	1.24.1	<h1 align="center"> <img src="/branding/logo/primary/numpylogo.svg" width="300">\n </h1><br>\n \n \n [![Powered by NumFOCUS](https://img.shields.io/badge/powered%20by-NumFOCUS-orange.svg?style=flat&colorA=E1523D&colorB=007D8A)](\n https://numfocus.org)\n [![PyPI Downloads](https://img.shields.io/pypi/dm/numpy.svg?label=PyPI%20downloads)](\n https://pypi.org/project/numpy/)\n [![Conda Downloads](https://img.shields.io/conda/dn/conda-forge/numpy.svg?label=Conda%20downloads)](\n https://anaconda.org/conda-forge/numpy)\n [![Stack Overflow](https://img.shields.io/badge/stackoverflow-Ask%20questions-blue.svg)](\n https://stackoverflow.com/questions/tagged/numpy)\n [![Nature Paper](https://img.shields.io/badge/DOI-10.1038%2Fs41592--019--0686--2-blue)](\n https://doi.org/10.1038/s41586-020-2649-2)\n [![OpenSSF Scorecard](https://api.securityscorecards.dev/projects/github.com/numpy/numpy/badge)](https://api.securityscorecards.dev/projects/github.com/numpy/numpy)\n \n \n NumPy is the fundamental package for scientific computing with Python.\n \n - **Website:** https://www.numpy.org\n - **Documentation:** https://numpy.org/doc\n - **Mailing list:** https://mail.python.org/mailman/listinfo/numpy-discussion\n - **Source code:** https://github.com/numpy/numpy\n - **Contributing:** https://www.numpy.org/devdocs/dev/index.html\n - **Bug reports:** https://github.com/numpy/numpy/issues\n - **Report a security vulnerability:** https://tidelift.com/docs/security\n \n It provides:\n \n - a powerful N-dimensional array object\n - sophisticated (broadcasting) functions\n - tools for integrating C/C++ and Fortran code\n - useful linear algebra, Fourier transform, and random number capabilities\n \n Testing:\n \n NumPy requires `pytest` and `hypothesis`. Tests can then be run after installation with:\n \n python -c 'import numpy; numpy.test()'\n \n Code of Conduct\n ----------------------\n \n NumPy is a community-driven open source project developed by a diverse group of\n [contributors](https://numpy.org/teams/). The NumPy leadership has made a strong\n commitment to creating an open, inclusive, and positive community. Please read the\n [NumPy Code of Conduct](https://numpy.org/code-of-conduct/) for guidance on how to interact\n with others in a way that makes our community thrive.\n \n Call for Contributions\n ----------------------\n \n The NumPy project welcomes your expertise and enthusiasm!\n \n Small improvements or fixes are always appreciated. If you are considering larger contributions\n to the source code, please contact us through the [mailing\n list](https://mail.python.org/mailman/listinfo/numpy-discussion) first.\n \n Writing code isn’t the only way to contribute to NumPy. You can also:\n - review pull requests\n - help us stay on top of new and old issues\n - develop tutorials, presentations, and other educational materials\n - maintain and improve [our website](https://github.com/numpy/numpy.org)\n - develop graphic design for our brand assets and promotional materials\n - translate website content\n - help with outreach and onboard new contributors\n - write grant proposals and help with other fundraising efforts\n \n For more information about the ways you can contribute to NumPy, visit [our website](https://numpy.org/contribute/). \n If you’re unsure where to start or how your skills fit in, reach out! You can\n ask on the mailing list or here, on GitHub, by opening a new issue or leaving a\n comment on a relevant issue that is already open.\n \n Our preferred channels of communication are all public, but if you’d like to\n speak to us in private first, contact our community coordinators at\n numpy-team@googlegroups.com or on Slack (write numpy-team@googlegroups.com for\n an invitation).\n \n We also have a biweekly community call, details of which are announced on the\n mailing list. You are very welcome to join.\n \n If you are new to contributing to open source, [this\n guide](https://opensource.guide/how-to-contribute/) helps explain why, what,\n and how to successfully get involved.\n \n	Travis E. Oliphant et al.	4	10	Bug Tracker, https://github.com/numpy/numpy/issues, Documentation, https://numpy.org/doc/1.24, Source Code, https://github.com/numpy/numpy	/opt/rdepot/repositories/10/67410940/numpy-1.24.1.tar.gz	\N	t	f	Python
35	wheel	0.36.1	wheel =====\n \n This library is the reference implementation of the Python wheel packaging\n standard, as defined in `PEP 427`_.\n \n It has two different roles:\n \n #. A setuptools_ extension for building wheels that provides the\n ``bdist_wheel`` setuptools command\n #. A command line tool for working with wheel files\n \n It should be noted that wheel is **not** intended to be used as a library, and\n as such there is no stable, public API.\n \n .. _PEP 427: https://www.python.org/dev/peps/pep-0427/\n .. _setuptools: https://pypi.org/project/setuptools/\n \n Documentation\n -------------\n \n The documentation_ can be found on Read The Docs.\n \n .. _documentation: https://wheel.readthedocs.io/\n \n Code of Conduct\n ---------------\n \n Everyone interacting in the wheel project's codebases, issue trackers, chat\n rooms, and mailing lists is expected to follow the `PSF Code of Conduct`_.\n \n .. _PSF Code of Conduct: https://github.com/pypa/.github/blob/main/CODE_OF_CONDUCT.md\n \n \n	Daniel Holth	4	10	Documentation, https://wheel.readthedocs.io/, Changelog, https://wheel.readthedocs.io/en/stable/news.html, Issue Tracker, https://github.com/pypa/wheel/issues	/opt/rdepot/trash/10/74692077/wheel-0.36.1.tar.gz	\N	f	t	Python
39	setuptools	67.8.0	\N	Python Packaging Authority	6	9	Documentation, https://setuptools.pypa.io/, Changelog, https://setuptools.pypa.io/en/stable/history.html	/opt/rdepot/repositories/9/65567066/setuptools-67.8.0.tar.gz	\N	t	f	Python
38	setuptools	67.4.0	\N	Python Packaging Authority	4	9	Documentation, https://setuptools.pypa.io/, Changelog, https://setuptools.pypa.io/en/stable/history.html	/opt/rdepot/repositories/9/95184570/setuptools-67.4.0.tar.gz	\N	t	f	Python
40	pandas	2.0.1	\N	\N	8	9	homepage, https://pandas.pydata.org, documentation, https://pandas.pydata.org/docs/, repository, https://github.com/pandas-dev/pandas	/opt/rdepot/repositories/9/41196072/pandas-2.0.1.tar.gz	\N	t	f	Python
42	boto3	1.26.156	\N	Amazon Web Services	6	8	Documentation, https://boto3.amazonaws.com/v1/documentation/api/latest/index.html, Source, https://github.com/boto/boto3	/opt/rdepot/repositories/8/55941618/boto3-1.26.156.tar.gz	\N	t	t	Python
41	pandas	2.0.1	\N	\N	8	8	homepage, https://pandas.pydata.org, documentation, https://pandas.pydata.org/docs/, repository, https://github.com/pandas-dev/pandas	/opt/rdepot/repositories/8/21604316/pandas-2.0.1.tar.gz	\N	t	f	Python
43	accelerated-numpy	0.1.0	# numpy-threading-extensions Faster loops for NumPy using multithreading and other tricks. The first release\n will target NumPy binary and unary ufuncs. Eventually we will enable overriding\n other NumPy functions, and provide an C-based (non-Python) API for extending\n via third-party functions.\n \n [![Travis CI Build Status](https://api.travis-ci.org/Quansight/numpy-threading-extensions.svg)](https://travis-ci.org/Quansight/numpy-threading-extensions)\n \n [![Coverage Status](https://codecov.io/gh/Quansight/numpy-threading-extensions/branch/main/graphs/badge.svg)](https://codecov.io/github/Quansight/numpy-threading-extensions)\n \n [![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)\n \n ## Installation\n ```\n pip install accelerated_numpy\n ```\n \n You can also install the in-development version 0.0.1 with:\n ```\n pip install https://github.com/Quansight/numpy-threading-extensions/archive/v0.0.1.zip\n ```\n or latest with\n ```\n pip install https://github.com/Quansight/numpy-threading-extensions/archive/main.zip\n ```\n \n ## Documentation\n \n To use the project:\n \n ```python\n import accelerated_numpy\n accelerated_numpy.initialize()\n ```\n \n ## Development\n \n To run all the tests run::\n \n ```\n tox\n ```\n \n Note, to combine the coverage data from all the tox environments run:\n \n OS | Command\n ----|----\n Windows | `set PYTEST_ADDOPTS=--cov-append`\n | | `tox`\n Other | `PYTEST_ADDOPTS=--cov-append tox`\n \n	Matti Picus	6	8	Changelog, https://github.com/Quansight/numpy-threading-extensions/blob/master/CHANGELOG.rst, Issue Tracker, https://github.com/Quansight/numpy-threading-extensions/issues	/opt/rdepot/repositories/8/25558329/accelerated-numpy-0.1.0.tar.gz	\N	t	f	Python
44	python-dateutil	2.8.1	dateutil - powerful extensions to datetime ==========================================\n \n |pypi| |support| |licence|\n \n |gitter| |readthedocs|\n \n |travis| |appveyor| |pipelines| |coverage|\n \n .. |pypi| image:: https://img.shields.io/pypi/v/python-dateutil.svg?style=flat-square\n :target: https://pypi.org/project/python-dateutil/\n :alt: pypi version\n \n .. |support| image:: https://img.shields.io/pypi/pyversions/python-dateutil.svg?style=flat-square\n :target: https://pypi.org/project/python-dateutil/\n :alt: supported Python version\n \n .. |travis| image:: https://img.shields.io/travis/dateutil/dateutil/master.svg?style=flat-square&label=Travis%20Build\n :target: https://travis-ci.org/dateutil/dateutil\n :alt: travis build status\n \n .. |appveyor| image:: https://img.shields.io/appveyor/ci/dateutil/dateutil/master.svg?style=flat-square&logo=appveyor\n :target: https://ci.appveyor.com/project/dateutil/dateutil\n :alt: appveyor build status\n \n .. |pipelines| image:: https://dev.azure.com/pythondateutilazure/dateutil/_apis/build/status/dateutil.dateutil?branchName=master\n :target: https://dev.azure.com/pythondateutilazure/dateutil/_build/latest?definitionId=1&branchName=master\n :alt: azure pipelines build status\n \n .. |coverage| image:: https://codecov.io/github/dateutil/dateutil/coverage.svg?branch=master\n :target: https://codecov.io/github/dateutil/dateutil?branch=master\n :alt: Code coverage\n \n .. |gitter| image:: https://badges.gitter.im/dateutil/dateutil.svg\n :alt: Join the chat at https://gitter.im/dateutil/dateutil\n :target: https://gitter.im/dateutil/dateutil\n \n .. |licence| image:: https://img.shields.io/pypi/l/python-dateutil.svg?style=flat-square\n :target: https://pypi.org/project/python-dateutil/\n :alt: licence\n \n .. |readthedocs| image:: https://img.shields.io/readthedocs/dateutil/latest.svg?style=flat-square&label=Read%20the%20Docs\n :alt: Read the documentation at https://dateutil.readthedocs.io/en/latest/\n :target: https://dateutil.readthedocs.io/en/latest/\n \n The `dateutil` module provides powerful extensions to\n the standard `datetime` module, available in Python.\n \n Installation\n ============\n `dateutil` can be installed from PyPI using `pip` (note that the package name is\n different from the importable name)::\n \n pip install python-dateutil\n \n Download\n ========\n dateutil is available on PyPI\n https://pypi.org/project/python-dateutil/\n \n The documentation is hosted at:\n https://dateutil.readthedocs.io/en/stable/\n \n Code\n ====\n The code and issue tracker are hosted on GitHub:\n https://github.com/dateutil/dateutil/\n \n Features\n ========\n \n * Computing of relative deltas (next month, next year,\n next Monday, last week of month, etc);\n * Computing of relative deltas between two given\n date and/or datetime objects;\n * Computing of dates based on very flexible recurrence rules,\n using a superset of the `iCalendar <https://www.ietf.org/rfc/rfc2445.txt>`_\n specification. Parsing of RFC strings is supported as well.\n * Generic parsing of dates in almost any string format;\n * Timezone (tzinfo) implementations for tzfile(5) format\n files (/etc/localtime, /usr/share/zoneinfo, etc), TZ\n environment string (in all known formats), iCalendar\n format files, given ranges (with help from relative deltas),\n local machine timezone, fixed offset timezone, UTC timezone,\n and Windows registry-based time zones.\n * Internal up-to-date world timezone information based on\n Olson's database.\n * Computing of Easter Sunday dates for any given year,\n using Western, Orthodox or Julian algorithms;\n * A comprehensive test suite.\n \n Quick example\n =============\n Here's a snapshot, just to give an idea about the power of the\n package. For more examples, look at the documentation.\n \n Suppose you want to know how much time is left, in\n years/months/days/etc, before the next easter happening on a\n year with a Friday 13th in August, and you want to get today's\n date out of the "date" unix system command. Here is the code:\n \n .. code-block:: python3\n \n >>> from dateutil.relativedelta import *\n >>> from dateutil.easter import *\n >>> from dateutil.rrule import *\n >>> from dateutil.parser import *\n >>> from datetime import *\n >>> now = parse("Sat Oct 11 17:13:46 UTC 2003")\n >>> today = now.date()\n >>> year = rrule(YEARLY,dtstart=now,bymonth=8,bymonthday=13,byweekday=FR)[0].year\n >>> rdelta = relativedelta(easter(year), today)\n >>> print("Today is: %s" % today)\n Today is: 2003-10-11\n >>> print("Year with next Aug 13th on a Friday is: %s" % year)\n Year with next Aug 13th on a Friday is: 2004\n >>> print("How far is the Easter of that year: %s" % rdelta)\n How far is the Easter of that year: relativedelta(months=+6)\n >>> print("And the Easter of that year is: %s" % (today+rdelta))\n And the Easter of that year is: 2004-04-11\n \n Being exactly 6 months ahead was **really** a coincidence :)\n \n Contributing\n ============\n \n We welcome many types of contributions - bug reports, pull requests (code, infrastructure or documentation fixes). For more information about how to contribute to the project, see the ``CONTRIBUTING.md`` file in the repository.\n \n \n Author\n ======\n The dateutil module was written by Gustavo Niemeyer <gustavo@niemeyer.net>\n in 2003.\n \n It is maintained by:\n \n * Gustavo Niemeyer <gustavo@niemeyer.net> 2003-2011\n * Tomi Pieviläinen <tomi.pievilainen@iki.fi> 2012-2014\n * Yaron de Leeuw <me@jarondl.net> 2014-2016\n * Paul Ganssle <paul@ganssle.io> 2015-\n \n Starting with version 2.4.1, all source and binary distributions will be signed\n by a PGP key that has, at the very least, been signed by the key which made the\n previous release. A table of release signing keys can be found below:\n \n =========== ============================\n Releases Signing key fingerprint\n =========== ============================\n 2.4.1- `6B49 ACBA DCF6 BD1C A206 67AB CD54 FCE3 D964 BEFB`_ (|pgp_mirror|_)\n =========== ============================\n \n \n Contact\n =======\n Our mailing list is available at `dateutil@python.org <https://mail.python.org/mailman/listinfo/dateutil>`_. As it is hosted by the PSF, it is subject to the `PSF code of\n conduct <https://www.python.org/psf/codeofconduct/>`_.\n \n License\n =======\n \n All contributions after December 1, 2017 released under dual license - either `Apache 2.0 License <https://www.apache.org/licenses/LICENSE-2.0>`_ or the `BSD 3-Clause License <https://opensource.org/licenses/BSD-3-Clause>`_. Contributions before December 1, 2017 - except those those explicitly relicensed - are released only under the BSD 3-Clause License.\n \n \n .. _6B49 ACBA DCF6 BD1C A206 67AB CD54 FCE3 D964 BEFB:\n https://pgp.mit.edu/pks/lookup?op=vindex&search=0xCD54FCE3D964BEFB\n \n .. |pgp_mirror| replace:: mirror\n .. _pgp_mirror: https://sks-keyservers.net/pks/lookup?op=vindex&search=0xCD54FCE3D964BEFB\n \n	Gustavo Niemeyer	6	8	\N	/opt/rdepot/repositories/8/31719300/python-dateutil-2.8.1.tar.gz	\N	t	f	Python
45	requests	2.19.1	  Requests: HTTP for Humans =========================\n \n .. image:: https://img.shields.io/pypi/v/requests.svg\n :target: https://pypi.org/project/requests/\n \n .. image:: https://img.shields.io/pypi/l/requests.svg\n :target: https://pypi.org/project/requests/\n \n .. image:: https://img.shields.io/pypi/pyversions/requests.svg\n :target: https://pypi.org/project/requests/\n \n .. image:: https://codecov.io/github/requests/requests/coverage.svg?branch=master\n :target: https://codecov.io/github/requests/requests\n :alt: codecov.io\n \n .. image:: https://img.shields.io/github/contributors/requests/requests.svg\n :target: https://github.com/requests/requests/graphs/contributors\n \n .. image:: https://img.shields.io/badge/Say%20Thanks-!-1EAEDB.svg\n :target: https://saythanks.io/to/kennethreitz\n \n Requests is the only *Non-GMO* HTTP library for Python, safe for human\n consumption.\n \n .. image:: https://farm5.staticflickr.com/4317/35198386374_1939af3de6_k_d.jpg\n \n Behold, the power of Requests:\n \n .. code-block:: python\n \n >>> r = requests.get('https://api.github.com/user', auth=('user', 'pass'))\n >>> r.status_code\n 200\n >>> r.headers['content-type']\n 'application/json; charset=utf8'\n >>> r.encoding\n 'utf-8'\n >>> r.text\n u'{"type":"User"...'\n >>> r.json()\n {u'disk_usage': 368627, u'private_gists': 484, ...}\n \n See `the similar code, sans Requests <https://gist.github.com/973705>`_.\n \n .. image:: https://raw.githubusercontent.com/requests/requests/master/docs/_static/requests-logo-small.png\n :target: http://docs.python-requests.org/\n \n \n Requests allows you to send *organic, grass-fed* HTTP/1.1 requests, without the\n need for manual labor. There's no need to manually add query strings to your\n URLs, or to form-encode your POST data. Keep-alive and HTTP connection pooling\n are 100% automatic, thanks to `urllib3 <https://github.com/shazow/urllib3>`_.\n \n Besides, all the cool kids are doing it. Requests is one of the most\n downloaded Python packages of all time, pulling in over 11,000,000 downloads\n every month. You don't want to be left out!\n \n Feature Support\n ---------------\n \n Requests is ready for today's web.\n \n - International Domains and URLs\n - Keep-Alive & Connection Pooling\n - Sessions with Cookie Persistence\n - Browser-style SSL Verification\n - Basic/Digest Authentication\n - Elegant Key/Value Cookies\n - Automatic Decompression\n - Automatic Content Decoding\n - Unicode Response Bodies\n - Multipart File Uploads\n - HTTP(S) Proxy Support\n - Connection Timeouts\n - Streaming Downloads\n - ``.netrc`` Support\n - Chunked Requests\n \n Requests officially supports Python 2.7 & 3.4–3.6, and runs great on PyPy.\n \n Installation\n ------------\n \n To install Requests, simply use `pipenv <http://pipenv.org/>`_ (or pip, of course):\n \n .. code-block:: bash\n \n $ pipenv install requests\n ✨🍰✨\n \n Satisfaction guaranteed.\n \n Documentation\n -------------\n \n Fantastic documentation is available at http://docs.python-requests.org/, for a limited time only.\n \n \n How to Contribute\n -----------------\n \n #. Check for open issues or open a fresh issue to start a discussion around a feature idea or a bug. There is a `Contributor Friendly`_ tag for issues that should be ideal for people who are not very familiar with the codebase yet.\n #. Fork `the repository`_ on GitHub to start making your changes to the **master** branch (or branch off of it).\n #. Write a test which shows that the bug was fixed or that the feature works as expected.\n #. Send a pull request and bug the maintainer until it gets merged and published. :) Make sure to add yourself to AUTHORS_.\n \n .. _`the repository`: https://github.com/requests/requests\n .. _AUTHORS: https://github.com/requests/requests/blob/master/AUTHORS.rst\n .. _Contributor Friendly: https://github.com/requests/requests/issues?direction=desc&labels=Contributor+Friendly&page=1&sort=updated&state=open\n \n \n .. :changelog:\n \n Release History\n ---------------\n \n dev\n +++\n \n - [Short description of non-trivial change.]\n \n 2.19.1 (2018-06-14)\n +++++++++++++++++++\n \n **Bugfixes**\n \n - Fixed issue where status_codes.py's ``init`` function failed trying to append to\n a ``__doc__`` value of ``None``.\n \n 2.19.0 (2018-06-12)\n +++++++++++++++++++\n \n **Improvements**\n \n - Warn user about possible slowdown when using cryptography version < 1.3.4\n - Check for invalid host in proxy URL, before forwarding request to adapter.\n - Fragments are now properly maintained across redirects. (RFC7231 7.1.2)\n - Removed use of cgi module to expedite library load time.\n - Added support for SHA-256 and SHA-512 digest auth algorithms.\n - Minor performance improvement to ``Request.content``.\n - Migrate to using collections.abc for 3.7 compatibility.\n \n **Bugfixes**\n \n - Parsing empty ``Link`` headers with ``parse_header_links()`` no longer return one bogus entry.\n - Fixed issue where loading the default certificate bundle from a zip archive\n would raise an ``IOError``.\n - Fixed issue with unexpected ``ImportError`` on windows system which do not support ``winreg`` module.\n - DNS resolution in proxy bypass no longer includes the username and password in\n the request. This also fixes the issue of DNS queries failing on macOS.\n - Properly normalize adapter prefixes for url comparison.\n - Passing ``None`` as a file pointer to the ``files`` param no longer raises an exception.\n - Calling ``copy`` on a ``RequestsCookieJar`` will now preserve the cookie policy correctly.\n \n **Dependencies**\n \n - We now support idna v2.7.\n - We now support urllib3 v1.23.\n \n 2.18.4 (2017-08-15)\n +++++++++++++++++++\n \n **Improvements**\n \n - Error messages for invalid headers now include the header name for easier debugging\n \n **Dependencies**\n \n - We now support idna v2.6.\n \n 2.18.3 (2017-08-02)\n +++++++++++++++++++\n \n **Improvements**\n \n - Running ``$ python -m requests.help`` now includes the installed version of idna.\n \n **Bugfixes**\n \n - Fixed issue where Requests would raise ``ConnectionError`` instead of\n ``SSLError`` when encountering SSL problems when using urllib3 v1.22.\n \n 2.18.2 (2017-07-25)\n +++++++++++++++++++\n \n **Bugfixes**\n \n - ``requests.help`` no longer fails on Python 2.6 due to the absence of\n ``ssl.OPENSSL_VERSION_NUMBER``.\n \n **Dependencies**\n \n - We now support urllib3 v1.22.\n \n 2.18.1 (2017-06-14)\n +++++++++++++++++++\n \n **Bugfixes**\n \n - Fix an error in the packaging whereby the ``*.whl`` contained incorrect data\n that regressed the fix in v2.17.3.\n \n 2.18.0 (2017-06-14)\n +++++++++++++++++++\n \n **Improvements**\n \n - ``Response`` is now a context manager, so can be used directly in a ``with`` statement\n without first having to be wrapped by ``contextlib.closing()``.\n \n **Bugfixes**\n \n - Resolve installation failure if multiprocessing is not available\n - Resolve tests crash if multiprocessing is not able to determine the number of CPU cores\n - Resolve error swallowing in utils set_environ generator\n \n \n 2.17.3 (2017-05-29)\n +++++++++++++++++++\n \n **Improvements**\n \n - Improved ``packages`` namespace identity support, for monkeypatching libraries.\n \n \n 2.17.2 (2017-05-29)\n +++++++++++++++++++\n \n **Improvements**\n \n - Improved ``packages`` namespace identity support, for monkeypatching libraries.\n \n \n 2.17.1 (2017-05-29)\n +++++++++++++++++++\n \n **Improvements**\n \n - Improved ``packages`` namespace identity support, for monkeypatching libraries.\n \n \n 2.17.0 (2017-05-29)\n +++++++++++++++++++\n \n **Improvements**\n \n - Removal of the 301 redirect cache. This improves thread-safety.\n \n \n 2.16.5 (2017-05-28)\n +++++++++++++++++++\n \n - Improvements to ``$ python -m requests.help``.\n \n 2.16.4 (2017-05-27)\n +++++++++++++++++++\n \n - Introduction of the ``$ python -m requests.help`` command, for debugging with maintainers!\n \n 2.16.3 (2017-05-27)\n +++++++++++++++++++\n \n - Further restored the ``requests.packages`` namespace for compatibility reasons.\n \n 2.16.2 (2017-05-27)\n +++++++++++++++++++\n \n - Further restored the ``requests.packages`` namespace for compatibility reasons.\n \n No code modification (noted below) should be necessary any longer.\n \n 2.16.1 (2017-05-27)\n +++++++++++++++++++\n \n - Restored the ``requests.packages`` namespace for compatibility reasons.\n - Bugfix for ``urllib3`` version parsing.\n \n **Note**: code that was written to import against the ``requests.packages``\n namespace previously will have to import code that rests at this module-level\n now.\n \n For example::\n \n from requests.packages.urllib3.poolmanager import PoolManager\n \n Will need to be re-written to be::\n \n from requests.packages import urllib3\n urllib3.poolmanager.PoolManager\n \n Or, even better::\n \n from urllib3.poolmanager import PoolManager\n \n 2.16.0 (2017-05-26)\n +++++++++++++++++++\n \n - Unvendor ALL the things!\n \n 2.15.1 (2017-05-26)\n +++++++++++++++++++\n \n - Everyone makes mistakes.\n \n 2.15.0 (2017-05-26)\n +++++++++++++++++++\n \n **Improvements**\n \n - Introduction of the ``Response.next`` property, for getting the next\n ``PreparedResponse`` from a redirect chain (when ``allow_redirects=False``).\n - Internal refactoring of ``__version__`` module.\n \n **Bugfixes**\n \n - Restored once-optional parameter for ``requests.utils.get_environ_proxies()``.\n \n 2.14.2 (2017-05-10)\n +++++++++++++++++++\n \n **Bugfixes**\n \n - Changed a less-than to an equal-to and an or in the dependency markers to\n widen compatibility with older setuptools releases.\n \n 2.14.1 (2017-05-09)\n +++++++++++++++++++\n \n **Bugfixes**\n \n - Changed the dependency markers to widen compatibility with older pip\n releases.\n \n 2.14.0 (2017-05-09)\n +++++++++++++++++++\n \n **Improvements**\n \n - It is now possible to pass ``no_proxy`` as a key to the ``proxies``\n dictionary to provide handling similar to the ``NO_PROXY`` environment\n variable.\n - When users provide invalid paths to certificate bundle files or directories\n Requests now raises ``IOError``, rather than failing at the time of the HTTPS\n request with a fairly inscrutable certificate validation error.\n - The behavior of ``SessionRedirectMixin`` was slightly altered.\n ``resolve_redirects`` will now detect a redirect by calling\n ``get_redirect_target(response)`` instead of directly\n querying ``Response.is_redirect`` and ``Response.headers['location']``.\n Advanced users will be able to process malformed redirects more easily.\n - Changed the internal calculation of elapsed request time to have higher\n resolution on Windows.\n - Added ``win_inet_pton`` as conditional dependency for the ``[socks]`` extra\n on Windows with Python 2.7.\n - Changed the proxy bypass implementation on Windows: the proxy bypass\n check doesn't use forward and reverse DNS requests anymore\n - URLs with schemes that begin with ``http`` but are not ``http`` or ``https``\n no longer have their host parts forced to lowercase.\n \n **Bugfixes**\n \n - Much improved handling of non-ASCII ``Location`` header values in redirects.\n Fewer ``UnicodeDecodeErrors`` are encountered on Python 2, and Python 3 now\n correctly understands that Latin-1 is unlikely to be the correct encoding.\n - If an attempt to ``seek`` file to find out its length fails, we now\n appropriately handle that by aborting our content-length calculations.\n - Restricted ``HTTPDigestAuth`` to only respond to auth challenges made on 4XX\n responses, rather than to all auth challenges.\n - Fixed some code that was firing ``DeprecationWarning`` on Python 3.6.\n - The dismayed person emoticon (``/o\\``) no longer has a big head. I'm sure\n this is what you were all worrying about most.\n \n \n **Miscellaneous**\n \n - Updated bundled urllib3 to v1.21.1.\n - Updated bundled chardet to v3.0.2.\n - Updated bundled idna to v2.5.\n - Updated bundled certifi to 2017.4.17.\n \n 2.13.0 (2017-01-24)\n +++++++++++++++++++\n \n **Features**\n \n - Only load the ``idna`` library when we've determined we need it. This will\n save some memory for users.\n \n **Miscellaneous**\n \n - Updated bundled urllib3 to 1.20.\n - Updated bundled idna to 2.2.\n \n 2.12.5 (2017-01-18)\n +++++++++++++++++++\n \n **Bugfixes**\n \n - Fixed an issue with JSON encoding detection, specifically detecting\n big-endian UTF-32 with BOM.\n \n 2.12.4 (2016-12-14)\n +++++++++++++++++++\n \n **Bugfixes**\n \n - Fixed regression from 2.12.2 where non-string types were rejected in the\n basic auth parameters. While support for this behaviour has been readded,\n the behaviour is deprecated and will be removed in the future.\n \n 2.12.3 (2016-12-01)\n +++++++++++++++++++\n \n **Bugfixes**\n \n - Fixed regression from v2.12.1 for URLs with schemes that begin with "http".\n These URLs have historically been processed as though they were HTTP-schemed\n URLs, and so have had parameters added. This was removed in v2.12.2 in an\n overzealous attempt to resolve problems with IDNA-encoding those URLs. This\n change was reverted: the other fixes for IDNA-encoding have been judged to\n be sufficient to return to the behaviour Requests had before v2.12.0.\n \n 2.12.2 (2016-11-30)\n +++++++++++++++++++\n \n **Bugfixes**\n \n - Fixed several issues with IDNA-encoding URLs that are technically invalid but\n which are widely accepted. Requests will now attempt to IDNA-encode a URL if\n it can but, if it fails, and the host contains only ASCII characters, it will\n be passed through optimistically. This will allow users to opt-in to using\n IDNA2003 themselves if they want to, and will also allow technically invalid\n but still common hostnames.\n - Fixed an issue where URLs with leading whitespace would raise\n ``InvalidSchema`` errors.\n - Fixed an issue where some URLs without the HTTP or HTTPS schemes would still\n have HTTP URL preparation applied to them.\n - Fixed an issue where Unicode strings could not be used in basic auth.\n - Fixed an issue encountered by some Requests plugins where constructing a\n Response object would cause ``Response.content`` to raise an\n ``AttributeError``.\n \n 2.12.1 (2016-11-16)\n +++++++++++++++++++\n \n **Bugfixes**\n \n - Updated setuptools 'security' extra for the new PyOpenSSL backend in urllib3.\n \n **Miscellaneous**\n \n - Updated bundled urllib3 to 1.19.1.\n \n 2.12.0 (2016-11-15)\n +++++++++++++++++++\n \n **Improvements**\n \n - Updated support for internationalized domain names from IDNA2003 to IDNA2008.\n This updated support is required for several forms of IDNs and is mandatory\n for .de domains.\n - Much improved heuristics for guessing content lengths: Requests will no\n longer read an entire ``StringIO`` into memory.\n - Much improved logic for recalculating ``Content-Length`` headers for\n ``PreparedRequest`` objects.\n - Improved tolerance for file-like objects that have no ``tell`` method but\n do have a ``seek`` method.\n - Anything that is a subclass of ``Mapping`` is now treated like a dictionary\n by the ``data=`` keyword argument.\n - Requests now tolerates empty passwords in proxy credentials, rather than\n stripping the credentials.\n - If a request is made with a file-like object as the body and that request is\n redirected with a 307 or 308 status code, Requests will now attempt to\n rewind the body object so it can be replayed.\n \n **Bugfixes**\n \n - When calling ``response.close``, the call to ``close`` will be propagated\n through to non-urllib3 backends.\n - Fixed issue where the ``ALL_PROXY`` environment variable would be preferred\n over scheme-specific variables like ``HTTP_PROXY``.\n - Fixed issue where non-UTF8 reason phrases got severely mangled by falling\n back to decoding using ISO 8859-1 instead.\n - Fixed a bug where Requests would not correctly correlate cookies set when\n using custom Host headers if those Host headers did not use the native\n string type for the platform.\n \n **Miscellaneous**\n \n - Updated bundled urllib3 to 1.19.\n - Updated bundled certifi certs to 2016.09.26.\n \n 2.11.1 (2016-08-17)\n +++++++++++++++++++\n \n **Bugfixes**\n \n - Fixed a bug when using ``iter_content`` with ``decode_unicode=True`` for\n streamed bodies would raise ``AttributeError``. This bug was introduced in\n 2.11.\n - Strip Content-Type and Transfer-Encoding headers from the header block when\n following a redirect that transforms the verb from POST/PUT to GET.\n \n 2.11.0 (2016-08-08)\n +++++++++++++++++++\n \n **Improvements**\n \n - Added support for the ``ALL_PROXY`` environment variable.\n - Reject header values that contain leading whitespace or newline characters to\n reduce risk of header smuggling.\n \n **Bugfixes**\n \n - Fixed occasional ``TypeError`` when attempting to decode a JSON response that\n occurred in an error case. Now correctly returns a ``ValueError``.\n - Requests would incorrectly ignore a non-CIDR IP address in the ``NO_PROXY``\n environment variables: Requests now treats it as a specific IP.\n - Fixed a bug when sending JSON data that could cause us to encounter obscure\n OpenSSL errors in certain network conditions (yes, really).\n - Added type checks to ensure that ``iter_content`` only accepts integers and\n ``None`` for chunk sizes.\n - Fixed issue where responses whose body had not been fully consumed would have\n the underlying connection closed but not returned to the connection pool,\n which could cause Requests to hang in situations where the ``HTTPAdapter``\n had been configured to use a blocking connection pool.\n \n **Miscellaneous**\n \n - Updated bundled urllib3 to 1.16.\n - Some previous releases accidentally accepted non-strings as acceptable header values. This release does not.\n \n 2.10.0 (2016-04-29)\n +++++++++++++++++++\n \n **New Features**\n \n - SOCKS Proxy Support! (requires PySocks; ``$ pip install requests[socks]``)\n \n **Miscellaneous**\n \n - Updated bundled urllib3 to 1.15.1.\n \n 2.9.2 (2016-04-29)\n ++++++++++++++++++\n \n **Improvements**\n \n - Change built-in CaseInsensitiveDict (used for headers) to use OrderedDict\n as its underlying datastore.\n \n **Bugfixes**\n \n - Don't use redirect_cache if allow_redirects=False\n - When passed objects that throw exceptions from ``tell()``, send them via\n chunked transfer encoding instead of failing.\n - Raise a ProxyError for proxy related connection issues.\n \n 2.9.1 (2015-12-21)\n ++++++++++++++++++\n \n **Bugfixes**\n \n - Resolve regression introduced in 2.9.0 that made it impossible to send binary\n strings as bodies in Python 3.\n - Fixed errors when calculating cookie expiration dates in certain locales.\n \n **Miscellaneous**\n \n - Updated bundled urllib3 to 1.13.1.\n \n 2.9.0 (2015-12-15)\n ++++++++++++++++++\n \n **Minor Improvements** (Backwards compatible)\n \n - The ``verify`` keyword argument now supports being passed a path to a\n directory of CA certificates, not just a single-file bundle.\n - Warnings are now emitted when sending files opened in text mode.\n - Added the 511 Network Authentication Required status code to the status code\n registry.\n \n **Bugfixes**\n \n - For file-like objects that are not seeked to the very beginning, we now\n send the content length for the number of bytes we will actually read, rather\n than the total size of the file, allowing partial file uploads.\n - When uploading file-like objects, if they are empty or have no obvious\n content length we set ``Transfer-Encoding: chunked`` rather than\n ``Content-Length: 0``.\n - We correctly receive the response in buffered mode when uploading chunked\n bodies.\n - We now handle being passed a query string as a bytestring on Python 3, by\n decoding it as UTF-8.\n - Sessions are now closed in all cases (exceptional and not) when using the\n functional API rather than leaking and waiting for the garbage collector to\n clean them up.\n - Correctly handle digest auth headers with a malformed ``qop`` directive that\n contains no token, by treating it the same as if no ``qop`` directive was\n provided at all.\n - Minor performance improvements when removing specific cookies by name.\n \n **Miscellaneous**\n \n - Updated urllib3 to 1.13.\n \n 2.8.1 (2015-10-13)\n ++++++++++++++++++\n \n **Bugfixes**\n \n - Update certificate bundle to match ``certifi`` 2015.9.6.2's weak certificate\n bundle.\n - Fix a bug in 2.8.0 where requests would raise ``ConnectTimeout`` instead of\n ``ConnectionError``\n - When using the PreparedRequest flow, requests will now correctly respect the\n ``json`` parameter. Broken in 2.8.0.\n - When using the PreparedRequest flow, requests will now correctly handle a\n Unicode-string method name on Python 2. Broken in 2.8.0.\n \n 2.8.0 (2015-10-05)\n ++++++++++++++++++\n \n **Minor Improvements** (Backwards Compatible)\n \n - Requests now supports per-host proxies. This allows the ``proxies``\n dictionary to have entries of the form\n ``{'<scheme>://<hostname>': '<proxy>'}``. Host-specific proxies will be used\n in preference to the previously-supported scheme-specific ones, but the\n previous syntax will continue to work.\n - ``Response.raise_for_status`` now prints the URL that failed as part of the\n exception message.\n - ``requests.utils.get_netrc_auth`` now takes an ``raise_errors`` kwarg,\n defaulting to ``False``. When ``True``, errors parsing ``.netrc`` files cause\n exceptions to be thrown.\n - Change to bundled projects import logic to make it easier to unbundle\n requests downstream.\n - Changed the default User-Agent string to avoid leaking data on Linux: now\n contains only the requests version.\n \n **Bugfixes**\n \n - The ``json`` parameter to ``post()`` and friends will now only be used if\n neither ``data`` nor ``files`` are present, consistent with the\n documentation.\n - We now ignore empty fields in the ``NO_PROXY`` environment variable.\n - Fixed problem where ``httplib.BadStatusLine`` would get raised if combining\n ``stream=True`` with ``contextlib.closing``.\n - Prevented bugs where we would attempt to return the same connection back to\n the connection pool twice when sending a Chunked body.\n - Miscellaneous minor internal changes.\n - Digest Auth support is now thread safe.\n \n **Updates**\n \n - Updated urllib3 to 1.12.\n \n 2.7.0 (2015-05-03)\n ++++++++++++++++++\n \n This is the first release that follows our new release process. For more, see\n `our documentation\n <http://docs.python-requests.org/en/latest/community/release-process/>`_.\n \n **Bugfixes**\n \n - Updated urllib3 to 1.10.4, resolving several bugs involving chunked transfer\n encoding and response framing.\n \n 2.6.2 (2015-04-23)\n ++++++++++++++++++\n \n **Bugfixes**\n \n - Fix regression where compressed data that was sent as chunked data was not\n properly decompressed. (#2561)\n \n 2.6.1 (2015-04-22)\n ++++++++++++++++++\n \n **Bugfixes**\n \n - Remove VendorAlias import machinery introduced in v2.5.2.\n \n - Simplify the PreparedRequest.prepare API: We no longer require the user to\n pass an empty list to the hooks keyword argument. (c.f. #2552)\n \n - Resolve redirects now receives and forwards all of the original arguments to\n the adapter. (#2503)\n \n - Handle UnicodeDecodeErrors when trying to deal with a unicode URL that\n cannot be encoded in ASCII. (#2540)\n \n - Populate the parsed path of the URI field when performing Digest\n Authentication. (#2426)\n \n - Copy a PreparedRequest's CookieJar more reliably when it is not an instance\n of RequestsCookieJar. (#2527)\n \n 2.6.0 (2015-03-14)\n ++++++++++++++++++\n \n **Bugfixes**\n \n - CVE-2015-2296: Fix handling of cookies on redirect. Previously a cookie\n without a host value set would use the hostname for the redirected URL\n exposing requests users to session fixation attacks and potentially cookie\n stealing. This was disclosed privately by Matthew Daley of\n `BugFuzz <https://bugfuzz.com>`_. This affects all versions of requests from\n v2.1.0 to v2.5.3 (inclusive on both ends).\n \n - Fix error when requests is an ``install_requires`` dependency and ``python\n setup.py test`` is run. (#2462)\n \n - Fix error when urllib3 is unbundled and requests continues to use the\n vendored import location.\n \n - Include fixes to ``urllib3``'s header handling.\n \n - Requests' handling of unvendored dependencies is now more restrictive.\n \n **Features and Improvements**\n \n - Support bytearrays when passed as parameters in the ``files`` argument.\n (#2468)\n \n - Avoid data duplication when creating a request with ``str``, ``bytes``, or\n ``bytearray`` input to the ``files`` argument.\n \n 2.5.3 (2015-02-24)\n ++++++++++++++++++\n \n **Bugfixes**\n \n - Revert changes to our vendored certificate bundle. For more context see\n (#2455, #2456, and http://bugs.python.org/issue23476)\n \n 2.5.2 (2015-02-23)\n ++++++++++++++++++\n \n **Features and Improvements**\n \n - Add sha256 fingerprint support. (`shazow/urllib3#540`_)\n \n - Improve the performance of headers. (`shazow/urllib3#544`_)\n \n **Bugfixes**\n \n - Copy pip's import machinery. When downstream redistributors remove\n requests.packages.urllib3 the import machinery will continue to let those\n same symbols work. Example usage in requests' documentation and 3rd-party\n libraries relying on the vendored copies of urllib3 will work without having\n to fallback to the system urllib3.\n \n - Attempt to quote parts of the URL on redirect if unquoting and then quoting\n fails. (#2356)\n \n - Fix filename type check for multipart form-data uploads. (#2411)\n \n - Properly handle the case where a server issuing digest authentication\n challenges provides both auth and auth-int qop-values. (#2408)\n \n - Fix a socket leak. (`shazow/urllib3#549`_)\n \n - Fix multiple ``Set-Cookie`` headers properly. (`shazow/urllib3#534`_)\n \n - Disable the built-in hostname verification. (`shazow/urllib3#526`_)\n \n - Fix the behaviour of decoding an exhausted stream. (`shazow/urllib3#535`_)\n \n **Security**\n \n - Pulled in an updated ``cacert.pem``.\n \n - Drop RC4 from the default cipher list. (`shazow/urllib3#551`_)\n \n .. _shazow/urllib3#551: https://github.com/shazow/urllib3/pull/551\n .. _shazow/urllib3#549: https://github.com/shazow/urllib3/pull/549\n .. _shazow/urllib3#544: https://github.com/shazow/urllib3/pull/544\n .. _shazow/urllib3#540: https://github.com/shazow/urllib3/pull/540\n .. _shazow/urllib3#535: https://github.com/shazow/urllib3/pull/535\n .. _shazow/urllib3#534: https://github.com/shazow/urllib3/pull/534\n .. _shazow/urllib3#526: https://github.com/shazow/urllib3/pull/526\n \n 2.5.1 (2014-12-23)\n ++++++++++++++++++\n \n **Behavioural Changes**\n \n - Only catch HTTPErrors in raise_for_status (#2382)\n \n **Bugfixes**\n \n - Handle LocationParseError from urllib3 (#2344)\n - Handle file-like object filenames that are not strings (#2379)\n - Unbreak HTTPDigestAuth handler. Allow new nonces to be negotiated (#2389)\n \n 2.5.0 (2014-12-01)\n ++++++++++++++++++\n \n **Improvements**\n \n - Allow usage of urllib3's Retry object with HTTPAdapters (#2216)\n - The ``iter_lines`` method on a response now accepts a delimiter with which\n to split the content (#2295)\n \n **Behavioural Changes**\n \n - Add deprecation warnings to functions in requests.utils that will be removed\n in 3.0 (#2309)\n - Sessions used by the functional API are always closed (#2326)\n - Restrict requests to HTTP/1.1 and HTTP/1.0 (stop accepting HTTP/0.9) (#2323)\n \n **Bugfixes**\n \n - Only parse the URL once (#2353)\n - Allow Content-Length header to always be overridden (#2332)\n - Properly handle files in HTTPDigestAuth (#2333)\n - Cap redirect_cache size to prevent memory abuse (#2299)\n - Fix HTTPDigestAuth handling of redirects after authenticating successfully\n (#2253)\n - Fix crash with custom method parameter to Session.request (#2317)\n - Fix how Link headers are parsed using the regular expression library (#2271)\n \n **Documentation**\n \n - Add more references for interlinking (#2348)\n - Update CSS for theme (#2290)\n - Update width of buttons and sidebar (#2289)\n - Replace references of Gittip with Gratipay (#2282)\n - Add link to changelog in sidebar (#2273)\n \n 2.4.3 (2014-10-06)\n ++++++++++++++++++\n \n **Bugfixes**\n \n - Unicode URL improvements for Python 2.\n - Re-order JSON param for backwards compat.\n - Automatically defrag authentication schemes from host/pass URIs. (`#2249 <https://github.com/requests/requests/issues/2249>`_)\n \n \n 2.4.2 (2014-10-05)\n ++++++++++++++++++\n \n **Improvements**\n \n - FINALLY! Add json parameter for uploads! (`#2258 <https://github.com/requests/requests/pull/2258>`_)\n - Support for bytestring URLs on Python 3.x (`#2238 <https://github.com/requests/requests/pull/2238>`_)\n \n **Bugfixes**\n \n - Avoid getting stuck in a loop (`#2244 <https://github.com/requests/requests/pull/2244>`_)\n - Multiple calls to iter* fail with unhelpful error. (`#2240 <https://github.com/requests/requests/issues/2240>`_, `#2241 <https://github.com/requests/requests/issues/2241>`_)\n \n **Documentation**\n \n - Correct redirection introduction (`#2245 <https://github.com/requests/requests/pull/2245/>`_)\n - Added example of how to send multiple files in one request. (`#2227 <https://github.com/requests/requests/pull/2227/>`_)\n - Clarify how to pass a custom set of CAs (`#2248 <https://github.com/requests/requests/pull/2248/>`_)\n \n \n \n 2.4.1 (2014-09-09)\n ++++++++++++++++++\n \n - Now has a "security" package extras set, ``$ pip install requests[security]``\n - Requests will now use Certifi if it is available.\n - Capture and re-raise urllib3 ProtocolError\n - Bugfix for responses that attempt to redirect to themselves forever (wtf?).\n \n \n 2.4.0 (2014-08-29)\n ++++++++++++++++++\n \n **Behavioral Changes**\n \n - ``Connection: keep-alive`` header is now sent automatically.\n \n **Improvements**\n \n - Support for connect timeouts! Timeout now accepts a tuple (connect, read) which is used to set individual connect and read timeouts.\n - Allow copying of PreparedRequests without headers/cookies.\n - Updated bundled urllib3 version.\n - Refactored settings loading from environment -- new `Session.merge_environment_settings`.\n - Handle socket errors in iter_content.\n \n \n 2.3.0 (2014-05-16)\n ++++++++++++++++++\n \n **API Changes**\n \n - New ``Response`` property ``is_redirect``, which is true when the\n library could have processed this response as a redirection (whether\n or not it actually did).\n - The ``timeout`` parameter now affects requests with both ``stream=True`` and\n ``stream=False`` equally.\n - The change in v2.0.0 to mandate explicit proxy schemes has been reverted.\n Proxy schemes now default to ``http://``.\n - The ``CaseInsensitiveDict`` used for HTTP headers now behaves like a normal\n dictionary when references as string or viewed in the interpreter.\n \n **Bugfixes**\n \n - No longer expose Authorization or Proxy-Authorization headers on redirect.\n Fix CVE-2014-1829 and CVE-2014-1830 respectively.\n - Authorization is re-evaluated each redirect.\n - On redirect, pass url as native strings.\n - Fall-back to autodetected encoding for JSON when Unicode detection fails.\n - Headers set to ``None`` on the ``Session`` are now correctly not sent.\n - Correctly honor ``decode_unicode`` even if it wasn't used earlier in the same\n response.\n - Stop advertising ``compress`` as a supported Content-Encoding.\n - The ``Response.history`` parameter is now always a list.\n - Many, many ``urllib3`` bugfixes.\n \n 2.2.1 (2014-01-23)\n ++++++++++++++++++\n \n **Bugfixes**\n \n - Fixes incorrect parsing of proxy credentials that contain a literal or encoded '#' character.\n - Assorted urllib3 fixes.\n \n 2.2.0 (2014-01-09)\n ++++++++++++++++++\n \n **API Changes**\n \n - New exception: ``ContentDecodingError``. Raised instead of ``urllib3``\n ``DecodeError`` exceptions.\n \n **Bugfixes**\n \n - Avoid many many exceptions from the buggy implementation of ``proxy_bypass`` on OS X in Python 2.6.\n - Avoid crashing when attempting to get authentication credentials from ~/.netrc when running as a user without a home directory.\n - Use the correct pool size for pools of connections to proxies.\n - Fix iteration of ``CookieJar`` objects.\n - Ensure that cookies are persisted over redirect.\n - Switch back to using chardet, since it has merged with charade.\n \n 2.1.0 (2013-12-05)\n ++++++++++++++++++\n \n - Updated CA Bundle, of course.\n - Cookies set on individual Requests through a ``Session`` (e.g. via ``Session.get()``) are no longer persisted to the ``Session``.\n - Clean up connections when we hit problems during chunked upload, rather than leaking them.\n - Return connections to the pool when a chunked upload is successful, rather than leaking it.\n - Match the HTTPbis recommendation for HTTP 301 redirects.\n - Prevent hanging when using streaming uploads and Digest Auth when a 401 is received.\n - Values of headers set by Requests are now always the native string type.\n - Fix previously broken SNI support.\n - Fix accessing HTTP proxies using proxy authentication.\n - Unencode HTTP Basic usernames and passwords extracted from URLs.\n - Support for IP address ranges for no_proxy environment variable\n - Parse headers correctly when users override the default ``Host:`` header.\n - Avoid munging the URL in case of case-sensitive servers.\n - Looser URL handling for non-HTTP/HTTPS urls.\n - Accept unicode methods in Python 2.6 and 2.7.\n - More resilient cookie handling.\n - Make ``Response`` objects pickleable.\n - Actually added MD5-sess to Digest Auth instead of pretending to like last time.\n - Updated internal urllib3.\n - Fixed @Lukasa's lack of taste.\n \n 2.0.1 (2013-10-24)\n ++++++++++++++++++\n \n - Updated included CA Bundle with new mistrusts and automated process for the future\n - Added MD5-sess to Digest Auth\n - Accept per-file headers in multipart file POST messages.\n - Fixed: Don't send the full URL on CONNECT messages.\n - Fixed: Correctly lowercase a redirect scheme.\n - Fixed: Cookies not persisted when set via functional API.\n - Fixed: Translate urllib3 ProxyError into a requests ProxyError derived from ConnectionError.\n - Updated internal urllib3 and chardet.\n \n 2.0.0 (2013-09-24)\n ++++++++++++++++++\n \n **API Changes:**\n \n - Keys in the Headers dictionary are now native strings on all Python versions,\n i.e. bytestrings on Python 2, unicode on Python 3.\n - Proxy URLs now *must* have an explicit scheme. A ``MissingSchema`` exception\n will be raised if they don't.\n - Timeouts now apply to read time if ``Stream=False``.\n - ``RequestException`` is now a subclass of ``IOError``, not ``RuntimeError``.\n - Added new method to ``PreparedRequest`` objects: ``PreparedRequest.copy()``.\n - Added new method to ``Session`` objects: ``Session.update_request()``. This\n method updates a ``Request`` object with the data (e.g. cookies) stored on\n the ``Session``.\n - Added new method to ``Session`` objects: ``Session.prepare_request()``. This\n method updates and prepares a ``Request`` object, and returns the\n corresponding ``PreparedRequest`` object.\n - Added new method to ``HTTPAdapter`` objects: ``HTTPAdapter.proxy_headers()``.\n This should not be called directly, but improves the subclass interface.\n - ``httplib.IncompleteRead`` exceptions caused by incorrect chunked encoding\n will now raise a Requests ``ChunkedEncodingError`` instead.\n - Invalid percent-escape sequences now cause a Requests ``InvalidURL``\n exception to be raised.\n - HTTP 208 no longer uses reason phrase ``"im_used"``. Correctly uses\n ``"already_reported"``.\n - HTTP 226 reason added (``"im_used"``).\n \n **Bugfixes:**\n \n - Vastly improved proxy support, including the CONNECT verb. Special thanks to\n the many contributors who worked towards this improvement.\n - Cookies are now properly managed when 401 authentication responses are\n received.\n - Chunked encoding fixes.\n - Support for mixed case schemes.\n - Better handling of streaming downloads.\n - Retrieve environment proxies from more locations.\n - Minor cookies fixes.\n - Improved redirect behaviour.\n - Improved streaming behaviour, particularly for compressed data.\n - Miscellaneous small Python 3 text encoding bugs.\n - ``.netrc`` no longer overrides explicit auth.\n - Cookies set by hooks are now correctly persisted on Sessions.\n - Fix problem with cookies that specify port numbers in their host field.\n - ``BytesIO`` can be used to perform streaming uploads.\n - More generous parsing of the ``no_proxy`` environment variable.\n - Non-string objects can be passed in data values alongside files.\n \n 1.2.3 (2013-05-25)\n ++++++++++++++++++\n \n - Simple packaging fix\n \n \n 1.2.2 (2013-05-23)\n ++++++++++++++++++\n \n - Simple packaging fix\n \n \n 1.2.1 (2013-05-20)\n ++++++++++++++++++\n \n - 301 and 302 redirects now change the verb to GET for all verbs, not just\n POST, improving browser compatibility.\n - Python 3.3.2 compatibility\n - Always percent-encode location headers\n - Fix connection adapter matching to be most-specific first\n - new argument to the default connection adapter for passing a block argument\n - prevent a KeyError when there's no link headers\n \n 1.2.0 (2013-03-31)\n ++++++++++++++++++\n \n - Fixed cookies on sessions and on requests\n - Significantly change how hooks are dispatched - hooks now receive all the\n arguments specified by the user when making a request so hooks can make a\n secondary request with the same parameters. This is especially necessary for\n authentication handler authors\n - certifi support was removed\n - Fixed bug where using OAuth 1 with body ``signature_type`` sent no data\n - Major proxy work thanks to @Lukasa including parsing of proxy authentication\n from the proxy url\n - Fix DigestAuth handling too many 401s\n - Update vendored urllib3 to include SSL bug fixes\n - Allow keyword arguments to be passed to ``json.loads()`` via the\n ``Response.json()`` method\n - Don't send ``Content-Length`` header by default on ``GET`` or ``HEAD``\n requests\n - Add ``elapsed`` attribute to ``Response`` objects to time how long a request\n took.\n - Fix ``RequestsCookieJar``\n - Sessions and Adapters are now picklable, i.e., can be used with the\n multiprocessing library\n - Update charade to version 1.0.3\n \n The change in how hooks are dispatched will likely cause a great deal of\n issues.\n \n 1.1.0 (2013-01-10)\n ++++++++++++++++++\n \n - CHUNKED REQUESTS\n - Support for iterable response bodies\n - Assume servers persist redirect params\n - Allow explicit content types to be specified for file data\n - Make merge_kwargs case-insensitive when looking up keys\n \n 1.0.3 (2012-12-18)\n ++++++++++++++++++\n \n - Fix file upload encoding bug\n - Fix cookie behavior\n \n 1.0.2 (2012-12-17)\n ++++++++++++++++++\n \n - Proxy fix for HTTPAdapter.\n \n 1.0.1 (2012-12-17)\n ++++++++++++++++++\n \n - Cert verification exception bug.\n - Proxy fix for HTTPAdapter.\n \n 1.0.0 (2012-12-17)\n ++++++++++++++++++\n \n - Massive Refactor and Simplification\n - Switch to Apache 2.0 license\n - Swappable Connection Adapters\n - Mountable Connection Adapters\n - Mutable ProcessedRequest chain\n - /s/prefetch/stream\n - Removal of all configuration\n - Standard library logging\n - Make Response.json() callable, not property.\n - Usage of new charade project, which provides python 2 and 3 simultaneous chardet.\n - Removal of all hooks except 'response'\n - Removal of all authentication helpers (OAuth, Kerberos)\n \n This is not a backwards compatible change.\n \n 0.14.2 (2012-10-27)\n +++++++++++++++++++\n \n - Improved mime-compatible JSON handling\n - Proxy fixes\n - Path hack fixes\n - Case-Insensitive Content-Encoding headers\n - Support for CJK parameters in form posts\n \n \n 0.14.1 (2012-10-01)\n +++++++++++++++++++\n \n - Python 3.3 Compatibility\n - Simply default accept-encoding\n - Bugfixes\n \n \n 0.14.0 (2012-09-02)\n ++++++++++++++++++++\n \n - No more iter_content errors if already downloaded.\n \n 0.13.9 (2012-08-25)\n +++++++++++++++++++\n \n - Fix for OAuth + POSTs\n - Remove exception eating from dispatch_hook\n - General bugfixes\n \n 0.13.8 (2012-08-21)\n +++++++++++++++++++\n \n - Incredible Link header support :)\n \n 0.13.7 (2012-08-19)\n +++++++++++++++++++\n \n - Support for (key, value) lists everywhere.\n - Digest Authentication improvements.\n - Ensure proxy exclusions work properly.\n - Clearer UnicodeError exceptions.\n - Automatic casting of URLs to strings (fURL and such)\n - Bugfixes.\n \n 0.13.6 (2012-08-06)\n +++++++++++++++++++\n \n - Long awaited fix for hanging connections!\n \n 0.13.5 (2012-07-27)\n +++++++++++++++++++\n \n - Packaging fix\n \n 0.13.4 (2012-07-27)\n +++++++++++++++++++\n \n - GSSAPI/Kerberos authentication!\n - App Engine 2.7 Fixes!\n - Fix leaking connections (from urllib3 update)\n - OAuthlib path hack fix\n - OAuthlib URL parameters fix.\n \n 0.13.3 (2012-07-12)\n +++++++++++++++++++\n \n - Use simplejson if available.\n - Do not hide SSLErrors behind Timeouts.\n - Fixed param handling with urls containing fragments.\n - Significantly improved information in User Agent.\n - client certificates are ignored when verify=False\n \n 0.13.2 (2012-06-28)\n +++++++++++++++++++\n \n - Zero dependencies (once again)!\n - New: Response.reason\n - Sign querystring parameters in OAuth 1.0\n - Client certificates no longer ignored when verify=False\n - Add openSUSE certificate support\n \n 0.13.1 (2012-06-07)\n +++++++++++++++++++\n \n - Allow passing a file or file-like object as data.\n - Allow hooks to return responses that indicate errors.\n - Fix Response.text and Response.json for body-less responses.\n \n 0.13.0 (2012-05-29)\n +++++++++++++++++++\n \n - Removal of Requests.async in favor of `grequests <https://github.com/kennethreitz/grequests>`_\n - Allow disabling of cookie persistence.\n - New implementation of safe_mode\n - cookies.get now supports default argument\n - Session cookies not saved when Session.request is called with return_response=False\n - Env: no_proxy support.\n - RequestsCookieJar improvements.\n - Various bug fixes.\n \n 0.12.1 (2012-05-08)\n +++++++++++++++++++\n \n - New ``Response.json`` property.\n - Ability to add string file uploads.\n - Fix out-of-range issue with iter_lines.\n - Fix iter_content default size.\n - Fix POST redirects containing files.\n \n 0.12.0 (2012-05-02)\n +++++++++++++++++++\n \n - EXPERIMENTAL OAUTH SUPPORT!\n - Proper CookieJar-backed cookies interface with awesome dict-like interface.\n - Speed fix for non-iterated content chunks.\n - Move ``pre_request`` to a more usable place.\n - New ``pre_send`` hook.\n - Lazily encode data, params, files.\n - Load system Certificate Bundle if ``certify`` isn't available.\n - Cleanups, fixes.\n \n 0.11.2 (2012-04-22)\n +++++++++++++++++++\n \n - Attempt to use the OS's certificate bundle if ``certifi`` isn't available.\n - Infinite digest auth redirect fix.\n - Multi-part file upload improvements.\n - Fix decoding of invalid %encodings in URLs.\n - If there is no content in a response don't throw an error the second time that content is attempted to be read.\n - Upload data on redirects.\n \n 0.11.1 (2012-03-30)\n +++++++++++++++++++\n \n * POST redirects now break RFC to do what browsers do: Follow up with a GET.\n * New ``strict_mode`` configuration to disable new redirect behavior.\n \n \n 0.11.0 (2012-03-14)\n +++++++++++++++++++\n \n * Private SSL Certificate support\n * Remove select.poll from Gevent monkeypatching\n * Remove redundant generator for chunked transfer encoding\n * Fix: Response.ok raises Timeout Exception in safe_mode\n \n 0.10.8 (2012-03-09)\n +++++++++++++++++++\n \n * Generate chunked ValueError fix\n * Proxy configuration by environment variables\n * Simplification of iter_lines.\n * New `trust_env` configuration for disabling system/environment hints.\n * Suppress cookie errors.\n \n 0.10.7 (2012-03-07)\n +++++++++++++++++++\n \n * `encode_uri` = False\n \n 0.10.6 (2012-02-25)\n +++++++++++++++++++\n \n * Allow '=' in cookies.\n \n 0.10.5 (2012-02-25)\n +++++++++++++++++++\n \n * Response body with 0 content-length fix.\n * New async.imap.\n * Don't fail on netrc.\n \n \n 0.10.4 (2012-02-20)\n +++++++++++++++++++\n \n * Honor netrc.\n \n 0.10.3 (2012-02-20)\n +++++++++++++++++++\n \n * HEAD requests don't follow redirects anymore.\n * raise_for_status() doesn't raise for 3xx anymore.\n * Make Session objects picklable.\n * ValueError for invalid schema URLs.\n \n 0.10.2 (2012-01-15)\n +++++++++++++++++++\n \n * Vastly improved URL quoting.\n * Additional allowed cookie key values.\n * Attempted fix for "Too many open files" Error\n * Replace unicode errors on first pass, no need for second pass.\n * Append '/' to bare-domain urls before query insertion.\n * Exceptions now inherit from RuntimeError.\n * Binary uploads + auth fix.\n * Bugfixes.\n \n \n 0.10.1 (2012-01-23)\n +++++++++++++++++++\n \n * PYTHON 3 SUPPORT!\n * Dropped 2.5 Support. (*Backwards Incompatible*)\n \n 0.10.0 (2012-01-21)\n +++++++++++++++++++\n \n * ``Response.content`` is now bytes-only. (*Backwards Incompatible*)\n * New ``Response.text`` is unicode-only.\n * If no ``Response.encoding`` is specified and ``chardet`` is available, ``Response.text`` will guess an encoding.\n * Default to ISO-8859-1 (Western) encoding for "text" subtypes.\n * Removal of `decode_unicode`. (*Backwards Incompatible*)\n * New multiple-hooks system.\n * New ``Response.register_hook`` for registering hooks within the pipeline.\n * ``Response.url`` is now Unicode.\n \n 0.9.3 (2012-01-18)\n ++++++++++++++++++\n \n * SSL verify=False bugfix (apparent on windows machines).\n \n 0.9.2 (2012-01-18)\n ++++++++++++++++++\n \n * Asynchronous async.send method.\n * Support for proper chunk streams with boundaries.\n * session argument for Session classes.\n * Print entire hook tracebacks, not just exception instance.\n * Fix response.iter_lines from pending next line.\n * Fix but in HTTP-digest auth w/ URI having query strings.\n * Fix in Event Hooks section.\n * Urllib3 update.\n \n \n 0.9.1 (2012-01-06)\n ++++++++++++++++++\n \n * danger_mode for automatic Response.raise_for_status()\n * Response.iter_lines refactor\n \n 0.9.0 (2011-12-28)\n ++++++++++++++++++\n \n * verify ssl is default.\n \n \n 0.8.9 (2011-12-28)\n ++++++++++++++++++\n \n * Packaging fix.\n \n \n 0.8.8 (2011-12-28)\n ++++++++++++++++++\n \n * SSL CERT VERIFICATION!\n * Release of Cerifi: Mozilla's cert list.\n * New 'verify' argument for SSL requests.\n * Urllib3 update.\n \n 0.8.7 (2011-12-24)\n ++++++++++++++++++\n \n * iter_lines last-line truncation fix\n * Force safe_mode for async requests\n * Handle safe_mode exceptions more consistently\n * Fix iteration on null responses in safe_mode\n \n 0.8.6 (2011-12-18)\n ++++++++++++++++++\n \n * Socket timeout fixes.\n * Proxy Authorization support.\n \n 0.8.5 (2011-12-14)\n ++++++++++++++++++\n \n * Response.iter_lines!\n \n 0.8.4 (2011-12-11)\n ++++++++++++++++++\n \n * Prefetch bugfix.\n * Added license to installed version.\n \n 0.8.3 (2011-11-27)\n ++++++++++++++++++\n \n * Converted auth system to use simpler callable objects.\n * New session parameter to API methods.\n * Display full URL while logging.\n \n 0.8.2 (2011-11-19)\n ++++++++++++++++++\n \n * New Unicode decoding system, based on over-ridable `Response.encoding`.\n * Proper URL slash-quote handling.\n * Cookies with ``[``, ``]``, and ``_`` allowed.\n \n 0.8.1 (2011-11-15)\n ++++++++++++++++++\n \n * URL Request path fix\n * Proxy fix.\n * Timeouts fix.\n \n 0.8.0 (2011-11-13)\n ++++++++++++++++++\n \n * Keep-alive support!\n * Complete removal of Urllib2\n * Complete removal of Poster\n * Complete removal of CookieJars\n * New ConnectionError raising\n * Safe_mode for error catching\n * prefetch parameter for request methods\n * OPTION method\n * Async pool size throttling\n * File uploads send real names\n * Vendored in urllib3\n \n 0.7.6 (2011-11-07)\n ++++++++++++++++++\n \n * Digest authentication bugfix (attach query data to path)\n \n 0.7.5 (2011-11-04)\n ++++++++++++++++++\n \n * Response.content = None if there was an invalid response.\n * Redirection auth handling.\n \n 0.7.4 (2011-10-26)\n ++++++++++++++++++\n \n * Session Hooks fix.\n \n 0.7.3 (2011-10-23)\n ++++++++++++++++++\n \n * Digest Auth fix.\n \n \n 0.7.2 (2011-10-23)\n ++++++++++++++++++\n \n * PATCH Fix.\n \n \n 0.7.1 (2011-10-23)\n ++++++++++++++++++\n \n * Move away from urllib2 authentication handling.\n * Fully Remove AuthManager, AuthObject, &c.\n * New tuple-based auth system with handler callbacks.\n \n \n 0.7.0 (2011-10-22)\n ++++++++++++++++++\n \n * Sessions are now the primary interface.\n * Deprecated InvalidMethodException.\n * PATCH fix.\n * New config system (no more global settings).\n \n \n 0.6.6 (2011-10-19)\n ++++++++++++++++++\n \n * Session parameter bugfix (params merging).\n \n \n 0.6.5 (2011-10-18)\n ++++++++++++++++++\n \n * Offline (fast) test suite.\n * Session dictionary argument merging.\n \n \n 0.6.4 (2011-10-13)\n ++++++++++++++++++\n \n * Automatic decoding of unicode, based on HTTP Headers.\n * New ``decode_unicode`` setting.\n * Removal of ``r.read/close`` methods.\n * New ``r.faw`` interface for advanced response usage.*\n * Automatic expansion of parameterized headers.\n \n \n 0.6.3 (2011-10-13)\n ++++++++++++++++++\n \n * Beautiful ``requests.async`` module, for making async requests w/ gevent.\n \n \n 0.6.2 (2011-10-09)\n ++++++++++++++++++\n \n * GET/HEAD obeys allow_redirects=False.\n \n \n 0.6.1 (2011-08-20)\n ++++++++++++++++++\n \n * Enhanced status codes experience ``\o/``\n * Set a maximum number of redirects (``settings.max_redirects``)\n * Full Unicode URL support\n * Support for protocol-less redirects.\n * Allow for arbitrary request types.\n * Bugfixes\n \n \n 0.6.0 (2011-08-17)\n ++++++++++++++++++\n \n * New callback hook system\n * New persistent sessions object and context manager\n * Transparent Dict-cookie handling\n * Status code reference object\n * Removed Response.cached\n * Added Response.request\n * All args are kwargs\n * Relative redirect support\n * HTTPError handling improvements\n * Improved https testing\n * Bugfixes\n \n \n 0.5.1 (2011-07-23)\n ++++++++++++++++++\n \n * International Domain Name Support!\n * Access headers without fetching entire body (``read()``)\n * Use lists as dicts for parameters\n * Add Forced Basic Authentication\n * Forced Basic is default authentication type\n * ``python-requests.org`` default User-Agent header\n * CaseInsensitiveDict lower-case caching\n * Response.history bugfix\n \n \n 0.5.0 (2011-06-21)\n ++++++++++++++++++\n \n * PATCH Support\n * Support for Proxies\n * HTTPBin Test Suite\n * Redirect Fixes\n * settings.verbose stream writing\n * Querystrings for all methods\n * URLErrors (Connection Refused, Timeout, Invalid URLs) are treated as explicitly raised\n ``r.requests.get('hwe://blah'); r.raise_for_status()``\n \n \n 0.4.1 (2011-05-22)\n ++++++++++++++++++\n \n * Improved Redirection Handling\n * New 'allow_redirects' param for following non-GET/HEAD Redirects\n * Settings module refactoring\n \n \n 0.4.0 (2011-05-15)\n ++++++++++++++++++\n \n * Response.history: list of redirected responses\n * Case-Insensitive Header Dictionaries!\n * Unicode URLs\n \n \n 0.3.4 (2011-05-14)\n ++++++++++++++++++\n \n * Urllib2 HTTPAuthentication Recursion fix (Basic/Digest)\n * Internal Refactor\n * Bytes data upload Bugfix\n \n \n \n 0.3.3 (2011-05-12)\n ++++++++++++++++++\n \n * Request timeouts\n * Unicode url-encoded data\n * Settings context manager and module\n \n \n 0.3.2 (2011-04-15)\n ++++++++++++++++++\n \n * Automatic Decompression of GZip Encoded Content\n * AutoAuth Support for Tupled HTTP Auth\n \n \n 0.3.1 (2011-04-01)\n ++++++++++++++++++\n \n * Cookie Changes\n * Response.read()\n * Poster fix\n \n \n 0.3.0 (2011-02-25)\n ++++++++++++++++++\n \n * Automatic Authentication API Change\n * Smarter Query URL Parameterization\n * Allow file uploads and POST data together\n * New Authentication Manager System\n - Simpler Basic HTTP System\n - Supports all build-in urllib2 Auths\n - Allows for custom Auth Handlers\n \n \n 0.2.4 (2011-02-19)\n ++++++++++++++++++\n \n * Python 2.5 Support\n * PyPy-c v1.4 Support\n * Auto-Authentication tests\n * Improved Request object constructor\n \n 0.2.3 (2011-02-15)\n ++++++++++++++++++\n \n * New HTTPHandling Methods\n - Response.__nonzero__ (false if bad HTTP Status)\n - Response.ok (True if expected HTTP Status)\n - Response.error (Logged HTTPError if bad HTTP Status)\n - Response.raise_for_status() (Raises stored HTTPError)\n \n \n 0.2.2 (2011-02-14)\n ++++++++++++++++++\n \n * Still handles request in the event of an HTTPError. (Issue #2)\n * Eventlet and Gevent Monkeypatch support.\n * Cookie Support (Issue #1)\n \n \n 0.2.1 (2011-02-14)\n ++++++++++++++++++\n \n * Added file attribute to POST and PUT requests for multipart-encode file uploads.\n * Added Request.url attribute for context and redirects\n \n \n 0.2.0 (2011-02-14)\n ++++++++++++++++++\n \n * Birth!\n \n \n 0.0.1 (2011-02-13)\n ++++++++++++++++++\n \n * Frustration\n * Conception\n \n	Kenneth Reitz	5	8	\N	/opt/rdepot/new/5533563/requests-2.19.1.tar.gz	\N	f	f	Python
46	requests	2.28.1	\N	Kenneth Reitz	5	8	Documentation, https://requests.readthedocs.io, Source, https://github.com/psf/requests	/opt/rdepot/new/32578090/requests-2.28.1.tar.gz	\N	f	f	Python
\.

COPY public.package (id, name, version, description, author, user_maintainer_id, repository_id, url, source, title, active, deleted, resource_technology, binary_package) FROM stdin;
47	arrow	8.0.0	'Apache' 'Arrow' <https://arrow.apache.org/> is a cross-language\n development platform for in-memory data. It specifies a standardized\n language-independent columnar memory format for flat and hierarchical data,\n organized for efficient analytic operations on modern hardware. This\n package provides an interface to the 'Arrow C++' library	Neal Richardson [aut, cre],\n Ian Cook [aut],\n Nic Crane [aut],\n Dewey Dunnington [aut] (<https://orcid.org/0000-0002-9415-4582>),\n Romain François [aut] (<https://orcid.org/0000-0002-2444-4226>),\n Jonathan Keane [aut],\n Dragoș Moldovan-Grünfeld [aut],\n Jeroen Ooms [aut],\n Javier Luraschi [ctb],\n Karl Dunkle Werner [ctb] (<https://orcid.org/0000-0003-0523-7309>),\n Jeffrey Wong [ctb],\n Apache Arrow [aut, cph]	5	5	https://github.com/apache/arrow/, https://arrow.apache.org/docs/r/	/opt/rdepot/repositories/5/32912654/arrow_8.0.0.tar.gz	Integration to 'Apache' 'Arrow'	t	f	R	t
\.

--
-- Data for Name: package_maintainer; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.package_maintainer (id, user_id, package, repository_id, deleted) FROM stdin;
1	6	accrued	2	f
2	6	abc	4	f
3	6	A3	4	f
4	6	bea.R	2	t
5	6	AnaCoDa	5	f
6	6	setuptools	9	t
7	8	pandas	9	f
8	8	pandas	8	f
9	6	boto3	8	f
10	6	accelerated-numpy	8	f
11	6	python-dateutil	8	f
\.


--
-- Data for Name: repository_maintainer; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.repository_maintainer (id, user_id, repository_id, deleted) FROM stdin;
1	5	2	f
3	5	5	f
2	5	4	t
4	5	8	f
5	5	9	f
6	4	10	f
\.



--
-- Data for Name: submission; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.submission (id, submitter_id, package_id, deleted, changes, state, approver_id) FROM stdin;
26	5	26	f	\N	WAITING	\N
19	7	19	f	\N	WAITING	\N
31	6	31	f	\N	WAITING	\N
30	6	30	f	\N	WAITING	\N
5	4	4	f	\N	ACCEPTED	4
18	7	18	f	\N	ACCEPTED	5
16	7	16	f	\N	REJECTED	5
15	4	14	f	\N	ACCEPTED	4
6	4	8	f	\N	ACCEPTED	4
12	4	12	f	\N	ACCEPTED	4
27	4	27	f	\N	ACCEPTED	4
23	6	22	f	\N	CANCELLED	6
24	6	24	f	\N	REJECTED	5
11	4	11	f	\N	ACCEPTED	4
8	4	7	f	\N	ACCEPTED	4
25	5	25	f	\N	ACCEPTED	5
29	4	29	f	\N	ACCEPTED	4
4	4	6	f	\N	ACCEPTED	4
21	6	21	f	\N	ACCEPTED	5
14	4	13	f	\N	ACCEPTED	4
17	7	17	f	\N	ACCEPTED	5
28	4	28	f	\N	ACCEPTED	4
22	6	23	f	\N	REJECTED	5
20	7	20	f	\N	ACCEPTED	5
13	4	15	f	\N	ACCEPTED	4
10	4	10	f	\N	ACCEPTED	4
9	4	9	f	\N	ACCEPTED	4
7	4	5	f	\N	ACCEPTED	4
36	7	36	f	\N	REJECTED	4
37	7	37	f	\N	ACCEPTED	4
34	7	34	f	\N	ACCEPTED	4
35	7	35	f	\N	REJECTED	7
38	4	38	f	\N	ACCEPTED	5
39	5	39	f	\N	ACCEPTED	5
40	4	40	f	\N	ACCEPTED	4
41	4	41	f	\N	ACCEPTED	4
42	4	42	f	\N	ACCEPTED	4
43	4	43	f	\N	ACCEPTED	4
44	4	44	f	\N	ACCEPTED	4
45	7	45	f	\N	WAITING	\N
46	7	46	f	\N	WAITING	\N
47	4	47	f	\N	ACCEPTED	4
\.



--
-- Data for Name: rpackage; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.rpackage (id, depends, imports, suggests, system_requirements, license, md5sum, enhances, linking_to, priority, needs_compilation) FROM stdin;
8	R (>= 3.0), grid	\N	\N	\N	GPL-3	70d295115295a4718593f6a39d77add9	\N	\N	\N	f
4	R (>= 2.14.1), grid	\N	\N	\N	GPL-3	19f8aec67250bd2ac481b14b50413d03	\N	\N	\N	f
10	R (>= 2.15.0), xtable, pbapply	\N	randomForest, e1071	\N	GPL (>= 2)	76d726aee8dd7c6ed94d150d5718015b	\N	\N	\N	f
5	R (>= 2.14.1), grid	\N	\N	\N	GPL-3	a05e4ca44438c0d9e7d713d7e3890423	\N	\N	\N	f
9	xtable, pbapply	\N	randomForest, e1071	\N	GPL (>= 2)	8eb4760cd574f5489e61221dc9bb0076	\N	\N	\N	f
19	R (>= 3.2.2)	ggplot2, tidyr, dplyr, purrr, magrittr, stats	plotly (>= 4.5.6), testthat, knitr, rmarkdown, vdiffr	\N	MIT + file LICENSE	f343fa3a01dcd9546fa0947877f58f36	\N	\N	\N	f
7	R (>= 2.14.1), grid	\N	\N	\N	GPL-3	97c2930a9dd7ca9fc1409d5340c06470	\N	\N	\N	f
14	R (>= 2.15.0)	\N	\N	\N	GPL (>= 2)	da8be1247d3145b757bd62e01fc6eb8b	\N	\N	\N	f
17	R (>= 2.15.0), xtable, pbapply	\N	randomForest, e1071	\N	GPL (>= 2)	76d726aee8dd7c6ed94d150d5718015b	\N	\N	\N	f
13	R (>= 3.0), grid	\N	\N	\N	GPL-3	1c75d59b18e554a285a9b156a06a288c	\N	\N	\N	f
11	R (>= 2.10), nnet, quantreg, locfit	\N	\N	\N	GPL (>= 3)	c47d18b86b331a5023dcd62b74fedbb6	\N	\N	\N	f
12	R (>= 1.8.0), nnet, quantreg, locfit, methods	\N	\N	\N	Unlimited	91599204c92275ed4b36d55e8d7c144b	\N	\N	\N	f
18	R (>= 2.10), nnet, quantreg, locfit	\N	\N	\N	GPL (>= 3)	c47d18b86b331a5023dcd62b74fedbb6	\N	\N	\N	f
20	R (>= 3.3.0), Rcpp (>= 0.11.3), methods		knitr, Hmisc, VGAM, coda, testthat, lmodel2	\N	GPL (>= 2)	41026e4157a0b3b6d909f0c6f72fa65c	\N	Rcpp	\N	f
6	R (>= 3.0), grid	\N	\N	\N	GPL-3	24b8cec280424dfc6a9e444fa57ba9f3	\N	\N	\N	f
22	R (>= 3.3.0), Rcpp (>= 0.11.3), methods		knitr, Hmisc, VGAM, coda, testthat, lmodel2	\N	GPL (>= 2)	41026e4157a0b3b6d909f0c6f72fa65c	\N	Rcpp	\N	f
26	R (>= 3.0), methods	graphics, stats, nlsr	knitr	\N	BSD_2_clause + file LICENSE	868140a3c3c29327eef5d5a485aee5b6	\N	\N	\N	f
25	R (>= 3.0), methods	graphics, stats, nlsr	knitr	\N	BSD_2_clause + file LICENSE	868140a3c3c29327eef5d5a485aee5b6	\N	\N	\N	f
16	xtable, pbapply	\N	randomForest, e1071	\N	GPL (>= 2)	8eb4760cd574f5489e61221dc9bb0076	\N	\N	\N	f
21	lpSolveAPI, ucminf	\N	\N	\N	GPL (>= 2)	9a99c2ebefa6d49422ca7893c1f4ead8	\N	\N	\N	f
23	R (>= 3.2.2)	ggplot2, tidyr, dplyr, purrr, magrittr, stats	plotly (>= 4.5.6), testthat, knitr, rmarkdown, vdiffr	\N	MIT + file LICENSE	f343fa3a01dcd9546fa0947877f58f36	\N	\N	\N	f
24	lpSolveAPI, ucminf	\N	\N	\N	GPL (>= 2)	9a99c2ebefa6d49422ca7893c1f4ead8	\N	\N	\N	f
30	xtable, pbapply	\N	randomForest, e1071	\N	GPL (>= 2)	8eb4760cd574f5489e61221dc9bb0076	\N	\N	\N	f
31	R (>= 1.8.0), nnet, quantreg, locfit, methods	\N	\N	\N	Unlimited	91599204c92275ed4b36d55e8d7c144b	\N	\N	\N	f
28	R (>= 3.2.2)	ggplot2, tidyr, dplyr, purrr, magrittr, stats	plotly (>= 4.5.6), testthat, knitr, rmarkdown, vdiffr	\N	MIT + file LICENSE	f343fa3a01dcd9546fa0947877f58f36	\N	\N	\N	f
27	R (>= 3.0), methods	graphics, stats, nlsr	knitr	\N	BSD_2_clause + file LICENSE	868140a3c3c29327eef5d5a485aee5b6	\N	\N	\N	f
29	xtable, pbapply	\N	randomForest, e1071	\N	GPL (>= 2)	8eb4760cd574f5489e61221dc9bb0076	\N	\N	\N	f
15	R (>= 3.2.1), data.table	httr, DT, shiny, jsonlite, googleVis, shinydashboard, ggplot2, stringr, chron, gtable, scales, htmltools, httpuv, xtable, stringi, magrittr, htmlwidgets, Rcpp, munsell, colorspace, plyr, yaml	\N	\N	CC0	5e664f320c7cc884138d64467f6b0e49	\N	\N	\N	f
\.

COPY public.rpackage (id, depends, imports, suggests, system_requirements, license, md5sum, needs_compilation, r_version, architecture, distribution, built) FROM stdin;
47	R (>= 3.4)	assertthat, bit64 (>= 0.9-7), methods, purrr, R6, rlang,\n stats, tidyselect (>= 1.0.0), utils, vctrs	DBI, dbplyr, decor, distro, dplyr, duckdb (>= 0.2.8), hms,\n knitr, lubridate, pkgload, reticulate, rmarkdown, stringi,\n stringr, testthat (>= 3.1.0), tibble, tzdb, withr	\N	Apache License (>= 2.0)	b55eb6a2f5adeff68f1ef15fd35b03de	yes	4.2.0	x86_64	centos7	R 4.2.0; x86_64-pc-linux-gnu; 2022-06-07 00:49:30 UTC; unix
\.

--
-- Data for Name: pythonpackage; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.pythonpackage (id, author_email, classifier, description_content_type, home_page, keywords, license, maintainer, maintainer_email, platform, project_url, provides_extra, requires_dist, requires_external, requires_python, summary, hash) FROM stdin;
34	\N	Development Status :: 5 - Production/Stable, Intended Audience :: Science/Research, Intended Audience :: Developers, License :: OSI Approved :: BSD License, Programming Language :: C, Programming Language :: Python, Programming Language :: Python :: 3, Programming Language :: Python :: 3.8, Programming Language :: Python :: 3.9, Programming Language :: Python :: 3.10, Programming Language :: Python :: 3.11, Programming Language :: Python :: 3 :: Only, Programming Language :: Python :: Implementation :: CPython, Topic :: Software Development, Topic :: Scientific/Engineering, Typing :: Typed, Operating System :: Microsoft :: Windows, Operating System :: POSIX, Operating System :: Unix, Operating System :: MacOS	\N		\N	BSD-3-Clause	NumPy Developers	\N	\N	Bug Tracker, https://github.com/numpy/numpy/issues, Documentation, https://numpy.org/doc/1.24, Source Code, https://github.com/numpy/numpy	\N	\N	\N	>=3.8	Fundamental package for array computing in Python	fc43d3bde8d622bf3ecadcf142b7e069b23121bef67c4fd190e0fbe7
35	\N	Development Status :: 5 - Production/Stable, Intended Audience :: Developers, Topic :: System :: Archiving :: Packaging, License :: OSI Approved :: MIT License, Programming Language :: Python, Programming Language :: Python :: 2, Programming Language :: Python :: 2.7, Programming Language :: Python :: 3, Programming Language :: Python :: 3.5, Programming Language :: Python :: 3.6, Programming Language :: Python :: 3.7, Programming Language :: Python :: 3.8, Programming Language :: Python :: 3.9	\N		wheel,packaging	MIT	Alex Grönholm	\N	\N	Documentation, https://wheel.readthedocs.io/, Changelog, https://wheel.readthedocs.io/en/stable/news.html, Issue Tracker, https://github.com/pypa/wheel/issues	test	\N	\N	!=3.0.*,!=3.1.*,!=3.2.*,!=3.3.*,!=3.4.*,>=2.7	A built-package format for Python	af4088d75044efec6858bfaeb7822a9123db40ac0919e2ce364aa89b
36	\N	Development Status :: 5 - Production/Stable, Intended Audience :: Developers, Topic :: System :: Archiving :: Packaging, License :: OSI Approved :: MIT License, Programming Language :: Python, Programming Language :: Python :: 3 :: Only, Programming Language :: Python :: 3.7, Programming Language :: Python :: 3.8, Programming Language :: Python :: 3.9, Programming Language :: Python :: 3.10, Programming Language :: Python :: 3.11	\N		wheel,packaging	MIT	Alex Grönholm	\N	\N	Documentation, https://wheel.readthedocs.io/, Changelog, https://wheel.readthedocs.io/en/stable/news.html, Issue Tracker, https://github.com/pypa/wheel/issues	test	\N	\N	>=3.7	A built-package format for Python	83918396bc14b8602518944f3772061832b73a580d62d1f67a4dc2b3
37	\N	Development Status :: 5 - Production/Stable, Intended Audience :: Developers, Topic :: System :: Archiving :: Packaging, License :: OSI Approved :: MIT License, Programming Language :: Python, Programming Language :: Python :: 3 :: Only, Programming Language :: Python :: 3.7, Programming Language :: Python :: 3.8, Programming Language :: Python :: 3.9, Programming Language :: Python :: 3.10, Programming Language :: Python :: 3.11	\N		wheel,packaging	OSI Approved :: MIT License	\N	\N	\N	Changelog, https://wheel.readthedocs.io/en/stable/news.html, Documentation, https://wheel.readthedocs.io/, Issue Tracker, https://github.com/pypa/wheel/issues	test	\N	\N	>=3.7	A built-package format for Python	b44dc26510fe1f31f27417390da11c2b70ce23cbc06c498317a5ff9c
38	\N	Development Status :: 5 - Production/Stable, Intended Audience :: Developers, License :: OSI Approved :: MIT License, Programming Language :: Python :: 3, Programming Language :: Python :: 3 :: Only, Topic :: Software Development :: Libraries :: Python Modules, Topic :: System :: Archiving :: Packaging, Topic :: System :: Systems Administration, Topic :: Utilities	\N		CPAN PyPI distutils eggs package management	OSI Approved :: MIT License	\N	\N	\N	Documentation, https://setuptools.pypa.io/, Changelog, https://setuptools.pypa.io/en/stable/history.html	testing, testing-integration, docs, ssl, certs	\N	\N	>=3.7	Easily download, build, install, upgrade, and uninstall Python packages	e5fd0a713141a4a105412233c63dc4e17ba0090c8e8334594ac790ec97792330
39	\N	Development Status :: 5 - Production/Stable, Intended Audience :: Developers, License :: OSI Approved :: MIT License, Programming Language :: Python :: 3, Programming Language :: Python :: 3 :: Only, Topic :: Software Development :: Libraries :: Python Modules, Topic :: System :: Archiving :: Packaging, Topic :: System :: Systems Administration, Topic :: Utilities	\N		CPAN PyPI distutils eggs package management	OSI Approved :: MIT License	\N	\N	\N	Documentation, https://setuptools.pypa.io/, Changelog, https://setuptools.pypa.io/en/stable/history.html	testing, testing-integration, docs, ssl, certs	\N	\N	>=3.7	Easily download, build, install, upgrade, and uninstall Python packages	62642358adc77ffa87233bc4d2354c4b2682d214048f500964dbe760ccedf102
40	\N	Development Status :: 5 - Production/Stable, Environment :: Console, Intended Audience :: Science/Research, License :: OSI Approved :: BSD License, Operating System :: OS Independent, Programming Language :: Cython, Programming Language :: Python, Programming Language :: Python :: 3, Programming Language :: Python :: 3 :: Only, Programming Language :: Python :: 3.8, Programming Language :: Python :: 3.9, Programming Language :: Python :: 3.10, Programming Language :: Python :: 3.11, Topic :: Scientific/Engineering	\N		\N	BSD 3-Clause License Copyright (c) 2008-2011, AQR Capital Management, LLC, Lambda Foundry, Inc. and PyData Development Team All rights reserved. Copyright (c) 2011-2023, Open source contributors. Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met: * Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer. * Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution. * Neither the name of the copyright holder nor the names of its contributors may be used to endorse or promote products derived from this software without specific prior written permission. THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.	\N	\N	\N	homepage, https://pandas.pydata.org, documentation, https://pandas.pydata.org/docs/, repository, https://github.com/pandas-dev/pandas	test, performance, computation, fss, aws, gcp, excel, parquet, feather, hdf5, spss, postgresql, mysql, sql-other, html, xml, plot, output_formatting, clipboard, compression, all	\N	\N	>=3.8	Powerful data structures for data analysis, time series, and statistics	7053d7ff8c563324b9a76110fabbd227c96c11d337521a57d94973bbb5f2a7ad
41	\N	Development Status :: 5 - Production/Stable, Environment :: Console, Intended Audience :: Science/Research, License :: OSI Approved :: BSD License, Operating System :: OS Independent, Programming Language :: Cython, Programming Language :: Python, Programming Language :: Python :: 3, Programming Language :: Python :: 3 :: Only, Programming Language :: Python :: 3.8, Programming Language :: Python :: 3.9, Programming Language :: Python :: 3.10, Programming Language :: Python :: 3.11, Topic :: Scientific/Engineering	\N		\N	BSD 3-Clause License Copyright (c) 2008-2011, AQR Capital Management, LLC, Lambda Foundry, Inc. and PyData Development Team All rights reserved. Copyright (c) 2011-2023, Open source contributors. Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met: * Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer. * Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution. * Neither the name of the copyright holder nor the names of its contributors may be used to endorse or promote products derived from this software without specific prior written permission. THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.	\N	\N	\N	homepage, https://pandas.pydata.org, documentation, https://pandas.pydata.org/docs/, repository, https://github.com/pandas-dev/pandas	test, performance, computation, fss, aws, gcp, excel, parquet, feather, hdf5, spss, postgresql, mysql, sql-other, html, xml, plot, output_formatting, clipboard, compression, all	\N	\N	>=3.8	Powerful data structures for data analysis, time series, and statistics	7053d7ff8c563324b9a76110fabbd227c96c11d337521a57d94973bbb5f2a7ad
42	\N	Development Status :: 5 - Production/Stable, Intended Audience :: Developers, Natural Language :: English, License :: OSI Approved :: Apache Software License, Programming Language :: Python, Programming Language :: Python :: 3, Programming Language :: Python :: 3.7, Programming Language :: Python :: 3.8, Programming Language :: Python :: 3.9, Programming Language :: Python :: 3.10, Programming Language :: Python :: 3.11	\N		\N	Apache License 2.0	\N	\N	\N	Documentation, https://boto3.amazonaws.com/v1/documentation/api/latest/index.html, Source, https://github.com/boto/boto3	crt	\N	\N	>= 3.7	The AWS SDK for Python	3a60283676399ae94b49b7a170fb0f42ca2ddcde490988fb0af7fd5a64440ab8
43	\N	Development Status :: 4 - Beta, Intended Audience :: Developers, License :: OSI Approved :: MIT License, Operating System :: Unix, Operating System :: POSIX, Operating System :: Microsoft :: Windows, Programming Language :: Python, Programming Language :: Python :: 3, Programming Language :: Python :: 3.6, Programming Language :: Python :: 3.7, Programming Language :: Python :: 3.8, Programming Language :: Python :: Implementation :: CPython, Programming Language :: Python :: Implementation :: PyPy, Topic :: Utilities	\N		\N	MIT	\N	\N	\N	Changelog, https://github.com/Quansight/numpy-threading-extensions/blob/master/CHANGELOG.rst, Issue Tracker, https://github.com/Quansight/numpy-threading-extensions/issues	\N	\N	\N	>=3.6	Faster loops for NumPy using multithreading and other tricks	f7421565cf0db9d1404e46fb5c7edf55624677999c81b8162d94a37da6984b78
44	\N	Development Status :: 5 - Production/Stable, Intended Audience :: Developers, License :: OSI Approved :: BSD License, License :: OSI Approved :: Apache Software License, Programming Language :: Python, Programming Language :: Python :: 2, Programming Language :: Python :: 2.7, Programming Language :: Python :: 3, Programming Language :: Python :: 3.3, Programming Language :: Python :: 3.4, Programming Language :: Python :: 3.5, Programming Language :: Python :: 3.6, Programming Language :: Python :: 3.7, Programming Language :: Python :: 3.8, Topic :: Software Development :: Libraries	\N		\N	Dual License	Paul Ganssle	\N	\N	\N	\N	\N	\N	!=3.0.*,!=3.1.*,!=3.2.*,>=2.7	Extensions to the standard Python datetime module	73ebfe9dbf22e832286dafa60473e4cd239f8592f699aa5adaf10050e6e1823c
45	\N	Development Status :: 5 - Production/Stable, Intended Audience :: Developers, Natural Language :: English, License :: OSI Approved :: Apache Software License, Programming Language :: Python, Programming Language :: Python :: 2, Programming Language :: Python :: 2.7, Programming Language :: Python :: 3, Programming Language :: Python :: 3.4, Programming Language :: Python :: 3.5, Programming Language :: Python :: 3.6, Programming Language :: Python :: Implementation :: CPython, Programming Language :: Python :: Implementation :: PyPy	\N		\N	Apache 2.0	\N	\N	\N	\N	\N	\N	\N	>=2.6, !=3.0.*, !=3.1.*, !=3.2.*, !=3.3.*	Python HTTP for Humans.	ec22d826a36ed72a7358ff3fe56cbd4ba69dd7a6718ffd450ff0e9df7a47ce6a
46	\N	Development Status :: 5 - Production/Stable, Environment :: Web Environment, Intended Audience :: Developers, License :: OSI Approved :: Apache Software License, Natural Language :: English, Operating System :: OS Independent, Programming Language :: Python, Programming Language :: Python :: 3, Programming Language :: Python :: 3.7, Programming Language :: Python :: 3.8, Programming Language :: Python :: 3.9, Programming Language :: Python :: 3.10, Programming Language :: Python :: 3.11, Programming Language :: Python :: 3 :: Only, Programming Language :: Python :: Implementation :: CPython, Programming Language :: Python :: Implementation :: PyPy, Topic :: Internet :: WWW/HTTP, Topic :: Software Development :: Libraries	\N		\N	Apache 2.0	\N	\N	\N	Documentation, https://requests.readthedocs.io, Source, https://github.com/psf/requests	security, socks, use_chardet_on_py3	\N	\N	>=3.7, <4	Python HTTP for Humans.	7c5599b102feddaa661c826c56ab4fee28bfd17f5abca1ebbe3e7f19d7c97983
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
-- Data for Name: pythonrepository; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.pythonrepository (id, hash_method) FROM stdin;
8	SHA256
9	SHA256
10	SHA224
11	MD5
\.


--
-- Data for Name: newsfeed_event; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.newsfeed_event (id, "time", newsfeed_event_type, author_id, related_packagemaintainer_id, related_repositorymaintainer_id, related_user_id, related_submission_id, related_repository_id, related_package_id, related_accesstoken_id, deleted) FROM stdin;
1	'2020-03-28 20:03:44'	CREATE	4	\N	\N	\N	4	\N	\N	\N	f
2	'2020-03-28 20:03:44'	CREATE	4	\N	\N	\N	5	\N	\N	\N	f
3	'2020-03-28 20:03:44'	CREATE	4	\N	\N	\N	6	\N	\N	\N	f
4	'2020-03-28 20:03:44'	UPDATE	4	\N	\N	\N	6	\N	\N	\N	f
5	'2020-03-28 20:03:44'	UPDATE	4	\N	\N	\N	5	\N	\N	\N	f
6	'2020-03-28 20:03:44'	UPDATE	4	\N	\N	\N	4	\N	\N	\N	f
7	'2020-03-28 20:03:45'	CREATE	4	\N	\N	\N	7	\N	\N	\N	f
8	'2020-03-28 20:03:45'	CREATE	4	\N	\N	\N	8	\N	\N	\N	f
9	'2020-03-28 20:03:45'	UPDATE	4	\N	\N	\N	7	\N	\N	\N	f
10	'2020-03-28 20:03:45'	UPDATE	4	\N	\N	\N	8	\N	\N	\N	f
11	'2020-03-28 20:03:58'	CREATE	4	\N	\N	\N	9	\N	\N	\N	f
12	'2020-03-28 20:03:58'	CREATE	4	\N	\N	\N	10	\N	\N	\N	f
13	'2020-03-28 20:03:58'	UPDATE	4	\N	\N	\N	10	\N	\N	\N	f
14	'2020-03-28 20:03:58'	UPDATE	4	\N	\N	\N	9	\N	\N	\N	f
15	'2020-03-28 20:03:59'	CREATE	4	\N	\N	\N	11	\N	\N	\N	f
16	'2020-03-28 20:03:59'	UPDATE	4	\N	\N	\N	11	\N	\N	\N	f
17	'2020-03-28 20:03:59'	CREATE	4	\N	\N	\N	12	\N	\N	\N	f
18	'2020-03-28 20:03:59'	UPDATE	4	\N	\N	\N	12	\N	\N	\N	f
19	'2020-03-28 20:04:17'	CREATE	4	\N	\N	\N	13	\N	\N	\N	f
20	'2020-03-28 20:04:17'	UPDATE	4	\N	\N	\N	13	\N	\N	\N	f
21	'2020-03-28 20:04:17'	CREATE	4	\N	\N	\N	14	\N	\N	\N	f
22	'2020-03-28 20:04:17'	UPDATE	4	\N	\N	\N	14	\N	\N	\N	f
23	'2020-03-28 20:04:17'	CREATE	4	\N	\N	\N	15	\N	\N	\N	f
24	'2020-03-28 20:04:17'	UPDATE	4	\N	\N	\N	15	\N	\N	\N	f
25	'2020-03-28 20:05:58'	CREATE	7	\N	\N	\N	16	\N	\N	\N	f
26	'2020-03-28 20:05:58'	CREATE	7	\N	\N	\N	17	\N	\N	\N	f
27	'2020-03-28 20:05:58'	CREATE	7	\N	\N	\N	18	\N	\N	\N	f
28	'2020-03-28 20:06:13'	CREATE	7	\N	\N	\N	19	\N	\N	\N	f
29	'2020-03-28 20:06:23'	CREATE	7	\N	\N	\N	20	\N	\N	\N	f
30	'2020-03-28 20:06:48'	CREATE	6	\N	\N	\N	21	\N	\N	\N	f
31	'2020-03-28 20:06:49'	CREATE	6	\N	\N	\N	22	\N	\N	\N	f
32	'2020-03-28 20:06:49'	CREATE	6	\N	\N	\N	23	\N	\N	\N	f
33	'2020-03-28 20:07:00'	DELETE	6	\N	\N	\N	23	\N	\N	\N	f
34	'2020-03-28 20:07:13'	CREATE	6	\N	\N	\N	24	\N	\N	\N	f
35	'2020-03-28 20:07:52'	CREATE	5	\N	\N	\N	25	\N	\N	\N	f
36	'2020-03-28 20:07:52'	CREATE	5	\N	\N	\N	26	\N	\N	\N	f
37	'2020-03-28 20:07:52'	UPDATE	5	\N	\N	\N	25	\N	\N	\N	f
38	'2020-03-28 20:08:08'	DELETE	5	\N	\N	\N	16	\N	\N	\N	f
39	'2020-03-28 20:08:12'	UPDATE	5	\N	\N	\N	17	\N	\N	\N	f
40	'2020-03-28 20:08:18'	UPDATE	5	\N	\N	\N	18	\N	\N	\N	f
41	'2020-03-28 20:08:23'	UPDATE	5	\N	\N	\N	21	\N	\N	\N	f
42	'2020-03-28 20:08:31'	DELETE	5	\N	\N	\N	22	\N	\N	\N	f
43	'2020-03-28 20:08:41'	UPDATE	5	\N	\N	\N	20	\N	\N	\N	f
44	'2020-03-28 20:08:42'	DELETE	5	\N	\N	\N	24	\N	\N	\N	f
45	'2020-03-28 20:09:48'	DELETE	4	\N	\N	\N	4	\N	\N	\N	f
46	'2020-03-28 20:10:08'	DELETE	5	\N	\N	\N	15	\N	\N	\N	f
47	'2020-03-28 20:12:44'	CREATE	4	\N	\N	\N	27	\N	\N	\N	f
48	'2020-03-28 20:12:44'	UPDATE	4	\N	\N	\N	27	\N	\N	\N	f
49	'2020-03-28 20:12:44'	CREATE	4	\N	\N	\N	28	\N	\N	\N	f
50	'2020-03-28 20:12:44'	UPDATE	4	\N	\N	\N	28	\N	\N	\N	f
51	'2020-03-28 20:13:30'	DELETE	4	\N	\N	\N	28	\N	\N	\N	f
52	'2020-03-28 20:13:30'	DELETE	4	\N	\N	\N	27	\N	\N	\N	f
53	'2020-03-28 20:14:06'	CREATE	4	\N	\N	\N	29	\N	\N	\N	f
54	'2020-03-28 20:14:06'	UPDATE	4	\N	\N	\N	29	\N	\N	\N	f
55	'2020-03-28 20:14:17'	DELETE	4	\N	\N	\N	29	\N	\N	\N	f
56	'2020-03-28 20:14:44'	CREATE	6	\N	\N	\N	30	\N	\N	\N	f
57	'2020-03-28 20:14:45'	CREATE	6	\N	\N	\N	31	\N	\N	\N	f
58	'2020-03-28 20:59:28'	CREATE	4	\N	\N	4	\N	\N	\N	\N	f
59	'2020-03-28 20:59:28'	CREATE	5	\N	\N	5	\N	\N	\N	\N	f
60	'2020-03-28 20:59:28'	CREATE	6	\N	\N	6	\N	\N	\N	\N	f
61	'2020-03-28 20:59:28'	CREATE	7	\N	\N	7	\N	\N	\N	\N	f
62	'2020-03-28 20:59:28'	CREATE	8	\N	\N	8	\N	\N	\N	\N	f
63	'2020-03-28 20:00:14'	UPDATE	4	\N	\N	4	\N	\N	\N	\N	f
64	'2020-03-28 20:03:28'	UPDATE	4	\N	\N	8	\N	\N	\N	\N	f
65	'2020-03-28 20:05:47'	UPDATE	4	\N	\N	7	\N	\N	\N	\N	f
66	'2020-03-28 20:06:31'	UPDATE	4	\N	\N	6	\N	\N	\N	\N	f
67	'2020-03-28 20:07:31'	UPDATE	4	\N	\N	5	\N	\N	\N	\N	f
68	'2020-03-28 20:09:02'	UPDATE	4	\N	\N	4	\N	\N	\N	\N	f
69	'2020-03-28 20:09:56'	UPDATE	8	\N	\N	5	\N	\N	\N	\N	f
70	'2020-03-28 20:12:06'	UPDATE	8	\N	\N	4	\N	\N	\N	\N	f
71	'2020-03-28 20:14:30'	UPDATE	8	\N	\N	6	\N	\N	\N	\N	f
72	'2020-03-29 10:42:45'	UPDATE	8	\N	\N	5	\N	\N	\N	\N	f
73	'2020-08-20 09:58:51'	CREATE	8	\N	\N	9	\N	\N	\N	\N	f
74	'2020-08-20 09:58:52'	UPDATE	8	\N	\N	9	\N	\N	\N	\N	f
75	'2020-08-20 09:59:08'	UPDATE	8	\N	\N	4	\N	\N	\N	\N	f
76	'2020-08-20 09:59:21'	UPDATE	4	\N	\N	9	\N	\N	\N	\N	f
77	'2020-08-25 12:35:38'	CREATE	8	\N	\N	10	\N	\N	\N	\N	f
78	'2020-08-25 12:35:38'	UPDATE	8	\N	\N	10	\N	\N	\N	\N	f
79	'2020-03-28 20:03:40'	UPLOAD	4	\N	\N	\N	\N	\N	4	\N	f
80	'2020-03-28 20:03:40'	UPLOAD	4	\N	\N	\N	\N	\N	6	\N	f
81	'2020-03-28 20:03:40'	UPLOAD	4	\N	\N	\N	\N	\N	5	\N	f
82	'2020-03-28 20:03:40'	UPLOAD	4	\N	\N	\N	\N	\N	8	\N	f
83	'2020-03-28 20:03:40'	UPLOAD	4	\N	\N	\N	\N	\N	7	\N	f
84	'2020-03-28 20:03:44'	UPDATE	4	\N	\N	\N	\N	\N	6	\N	f
85	'2020-03-28 20:03:44'	UPDATE	4	\N	\N	\N	\N	\N	4	\N	f
86	'2020-03-28 20:03:44'	UPDATE	4	\N	\N	\N	\N	\N	8	\N	f
87	'2020-03-28 20:03:44'	UPDATE	4	\N	\N	\N	\N	\N	4	\N	f
88	'2020-03-28 20:03:44'	UPDATE	4	\N	\N	\N	\N	\N	6	\N	f
89	'2020-03-28 20:03:44'	UPDATE	4	\N	\N	\N	\N	\N	8	\N	f
90	'2020-03-28 20:03:45'	UPDATE	4	\N	\N	\N	\N	\N	5	\N	f
91	'2020-03-28 20:03:45'	UPDATE	4	\N	\N	\N	\N	\N	7	\N	f
92	'2020-03-28 20:03:45'	UPDATE	4	\N	\N	\N	\N	\N	5	\N	f
93	'2020-03-28 20:03:45'	UPDATE	4	\N	\N	\N	\N	\N	7	\N	f
94	'2020-03-28 20:03:55'	UPLOAD	4	\N	\N	\N	\N	\N	9	\N	f
95	'2020-03-28 20:03:55'	UPLOAD	4	\N	\N	\N	\N	\N	10	\N	f
96	'2020-03-28 20:03:56'	UPLOAD	4	\N	\N	\N	\N	\N	11	\N	f
97	'2020-03-28 20:03:56'	UPLOAD	4	\N	\N	\N	\N	\N	12	\N	f
98	'2020-03-28 20:03:58'	UPDATE	4	\N	\N	\N	\N	\N	10	\N	f
99	'2020-03-28 20:03:58'	UPDATE	4	\N	\N	\N	\N	\N	9	\N	f
100	'2020-03-28 20:03:59'	UPDATE	4	\N	\N	\N	\N	\N	10	\N	f
101	'2020-03-28 20:03:59'	UPDATE	4	\N	\N	\N	\N	\N	9	\N	f
102	'2020-03-28 20:03:59'	UPDATE	4	\N	\N	\N	\N	\N	11	\N	f
103	'2020-03-28 20:03:59'	UPDATE	4	\N	\N	\N	\N	\N	11	\N	f
104	'2020-03-28 20:03:59'	UPDATE	4	\N	\N	\N	\N	\N	12	\N	f
105	'2020-03-28 20:03:59'	UPDATE	4	\N	\N	\N	\N	\N	12	\N	f
106	'2020-03-28 20:04:14'	UPLOAD	4	\N	\N	\N	\N	\N	13	\N	f
107	'2020-03-28 20:04:14'	UPLOAD	4	\N	\N	\N	\N	\N	14	\N	f
108	'2020-03-28 20:04:14'	UPLOAD	4	\N	\N	\N	\N	\N	15	\N	f
109	'2020-03-28 20:04:17'	UPDATE	4	\N	\N	\N	\N	\N	15	\N	f
110	'2020-03-28 20:04:17'	UPDATE	4	\N	\N	\N	\N	\N	15	\N	f
111	'2020-03-28 20:04:17'	UPDATE	4	\N	\N	\N	\N	\N	13	\N	f
112	'2020-03-28 20:04:17'	UPDATE	4	\N	\N	\N	\N	\N	13	\N	f
113	'2020-03-28 20:04:17'	UPDATE	4	\N	\N	\N	\N	\N	14	\N	f
114	'2020-03-28 20:04:17'	UPDATE	4	\N	\N	\N	\N	\N	14	\N	f
115	'2020-03-28 20:04:24'	UPDATE	4	\N	\N	\N	\N	\N	13	\N	f
116	'2020-03-28 20:04:24'	UPDATE	4	\N	\N	\N	\N	\N	15	\N	f
117	'2020-03-28 20:04:24'	UPDATE	4	\N	\N	\N	\N	\N	14	\N	f
118	'2020-03-28 20:04:31'	UPDATE	4	\N	\N	\N	\N	\N	11	\N	f
119	'2020-03-28 20:04:31'	UPDATE	4	\N	\N	\N	\N	\N	10	\N	f
120	'2020-03-28 20:04:31'	UPDATE	4	\N	\N	\N	\N	\N	12	\N	f
121	'2020-03-28 20:04:31'	UPDATE	4	\N	\N	\N	\N	\N	9	\N	f
122	'2020-03-28 20:04:41'	UPDATE	4	\N	\N	\N	\N	\N	10	\N	f
123	'2020-03-28 20:04:41'	UPDATE	4	\N	\N	\N	\N	\N	11	\N	f
124	'2020-03-28 20:04:41'	UPDATE	4	\N	\N	\N	\N	\N	9	\N	f
125	'2020-03-28 20:04:41'	UPDATE	4	\N	\N	\N	\N	\N	12	\N	f
126	'2020-03-28 20:04:47'	UPDATE	4	\N	\N	\N	\N	\N	11	\N	f
127	'2020-03-28 20:04:51'	UPDATE	4	\N	\N	\N	\N	\N	7	\N	f
128	'2020-03-28 20:04:54'	UPDATE	4	\N	\N	\N	\N	\N	6	\N	f
129	'2020-03-28 20:04:56'	UPDATE	4	\N	\N	\N	\N	\N	13	\N	f
130	'2020-03-28 20:05:25'	UPDATE	4	\N	\N	\N	\N	\N	13	\N	f
131	'2020-03-28 20:05:29'	UPDATE	4	\N	\N	\N	\N	\N	12	\N	f
132	'2020-03-28 20:05:29'	UPDATE	4	\N	\N	\N	\N	\N	11	\N	f
133	'2020-03-28 20:05:35'	UPDATE	4	\N	\N	\N	\N	\N	10	\N	f
134	'2020-03-28 20:05:35'	UPDATE	4	\N	\N	\N	\N	\N	9	\N	f
135	'2020-03-28 20:05:55'	UPLOAD	7	\N	\N	\N	\N	\N	16	\N	f
136	'2020-03-28 20:05:55'	UPLOAD	7	\N	\N	\N	\N	\N	17	\N	f
137	'2020-03-28 20:05:56'	UPLOAD	7	\N	\N	\N	\N	\N	18	\N	f
138	'2020-03-28 20:06:11'	UPLOAD	7	\N	\N	\N	\N	\N	19	\N	f
139	'2020-03-28 20:06:21'	UPLOAD	7	\N	\N	\N	\N	\N	20	\N	f
140	'2020-03-28 20:06:46'	UPLOAD	6	\N	\N	\N	\N	\N	21	\N	f
141	'2020-03-28 20:06:46'	UPLOAD	6	\N	\N	\N	\N	\N	22	\N	f
142	'2020-03-28 20:06:46'	UPLOAD	6	\N	\N	\N	\N	\N	23	\N	f
143	'2020-03-28 20:07:11'	UPLOAD	6	\N	\N	\N	\N	\N	24	\N	f
144	'2020-03-28 20:07:50'	UPLOAD	5	\N	\N	\N	\N	\N	25	\N	f
145	'2020-03-28 20:07:50'	UPLOAD	5	\N	\N	\N	\N	\N	26	\N	f
146	'2020-03-28 20:07:52'	UPDATE	5	\N	\N	\N	\N	\N	25	\N	f
147	'2020-03-28 20:07:52'	UPDATE	5	\N	\N	\N	\N	\N	25	\N	f
148	'2020-03-28 20:07:52'	UPDATE	5	\N	\N	\N	\N	\N	25	\N	f
149	'2020-03-28 20:08:12'	UPDATE	5	\N	\N	\N	\N	\N	17	\N	f
150	'2020-03-28 20:08:12'	UPDATE	5	\N	\N	\N	\N	\N	17	\N	f
151	'2020-03-28 20:08:12'	UPDATE	5	\N	\N	\N	\N	\N	17	\N	f
152	'2020-03-28 20:08:18'	UPDATE	5	\N	\N	\N	\N	\N	18	\N	f
153	'2020-03-28 20:08:18'	UPDATE	5	\N	\N	\N	\N	\N	18	\N	f
154	'2020-03-28 20:08:18'	UPDATE	5	\N	\N	\N	\N	\N	18	\N	f
155	'2020-03-28 20:08:23'	UPDATE	5	\N	\N	\N	\N	\N	21	\N	f
156	'2020-03-28 20:08:23'	UPDATE	5	\N	\N	\N	\N	\N	21	\N	f
157	'2020-03-28 20:08:23'	UPDATE	5	\N	\N	\N	\N	\N	21	\N	f
158	'2020-03-28 20:08:41'	UPDATE	5	\N	\N	\N	\N	\N	20	\N	f
159	'2020-03-28 20:08:41'	UPDATE	5	\N	\N	\N	\N	\N	20	\N	f
160	'2020-03-28 20:08:41'	UPDATE	5	\N	\N	\N	\N	\N	20	\N	f
161	'2020-03-28 20:09:48'	DELETE	4	\N	\N	\N	\N	\N	6	\N	f
162	'2020-03-28 20:10:08'	DELETE	5	\N	\N	\N	\N	\N	14	\N	f
163	'2020-03-28 20:12:42'	UPLOAD	4	\N	\N	\N	\N	\N	27	\N	f
164	'2020-03-28 20:12:42'	UPLOAD	4	\N	\N	\N	\N	\N	28	\N	f
165	'2020-03-28 20:12:44'	UPDATE	4	\N	\N	\N	\N	\N	27	\N	f
166	'2020-03-28 20:12:44'	UPDATE	4	\N	\N	\N	\N	\N	27	\N	f
167	'2020-03-28 20:12:44'	UPDATE	4	\N	\N	\N	\N	\N	28	\N	f
168	'2020-03-28 20:12:44'	UPDATE	4	\N	\N	\N	\N	\N	28	\N	f
169	'2020-03-28 20:13:30'	DELETE	4	\N	\N	\N	\N	\N	28	\N	f
170	'2020-03-28 20:13:30'	DELETE	4	\N	\N	\N	\N	\N	27	\N	f
171	'2020-03-28 20:14:04'	UPLOAD	4	\N	\N	\N	\N	\N	29	\N	f
172	'2020-03-28 20:14:06'	UPDATE	4	\N	\N	\N	\N	\N	29	\N	f
173	'2020-03-28 20:14:06'	UPDATE	4	\N	\N	\N	\N	\N	29	\N	f
174	'2020-03-28 20:14:17'	DELETE	4	\N	\N	\N	\N	\N	29	\N	f
175	'2020-03-28 20:14:42'	UPLOAD	6	\N	\N	\N	\N	\N	30	\N	f
176	'2020-03-28 20:14:43'	UPLOAD	6	\N	\N	\N	\N	\N	31	\N	f
177	'2020-03-29 10:43:05'	UPDATE	4	\N	\N	\N	\N	\N	15	\N	f
178	'2020-03-29 10:43:10'	UPDATE	4	\N	\N	\N	\N	\N	15	\N	f
179	'2020-03-28 20:05:25'	CREATE	4	1	\N	\N	\N	\N	\N	\N	f
180	'2020-03-28 20:05:29'	CREATE	4	2	\N	\N	\N	\N	\N	\N	f
181	'2020-03-28 20:05:35'	CREATE	4	3	\N	\N	\N	\N	\N	\N	f
182	'2020-03-29 10:43:05'	CREATE	4	4	\N	\N	\N	\N	\N	\N	f
183	'2020-03-29 10:43:10'	DELETE	4	4	\N	\N	\N	\N	\N	\N	f
184	'2020-03-28 20:01:11'	UPDATE	4	\N	\N	\N	\N	2	\N	\N	f
185	'2020-03-28 20:01:13'	UPDATE	4	\N	\N	\N	\N	2	\N	\N	f
186	'2020-03-28 20:03:44'	UPDATE	4	\N	\N	\N	\N	3	\N	\N	f
187	'2020-03-28 20:03:44'	UPDATE	4	\N	\N	\N	\N	3	\N	\N	f
188	'2020-03-28 20:03:44'	UPDATE	4	\N	\N	\N	\N	3	\N	\N	f
189	'2020-03-28 20:03:44'	UPDATE	4	\N	\N	\N	\N	3	\N	\N	f
190	'2020-03-28 20:03:44'	UPDATE	4	\N	\N	\N	\N	3	\N	\N	f
191	'2020-03-28 20:03:44'	UPDATE	4	\N	\N	\N	\N	3	\N	\N	f
192	'2020-03-28 20:03:44'	UPDATE	4	\N	\N	\N	\N	3	\N	\N	f
193	'2020-03-28 20:03:44'	UPDATE	4	\N	\N	\N	\N	3	\N	\N	f
194	'2020-03-28 20:03:44'	UPDATE	4	\N	\N	\N	\N	3	\N	\N	f
195	'2020-03-28 20:03:44'	UPDATE	4	\N	\N	\N	\N	3	\N	\N	f
196	'2020-03-28 20:03:44'	UPDATE	4	\N	\N	\N	\N	3	\N	\N	f
197	'2020-03-28 20:03:44'	UPDATE	4	\N	\N	\N	\N	3	\N	\N	f
198	'2020-03-28 20:03:44'	UPDATE	4	\N	\N	\N	\N	3	\N	\N	f
199	'2020-03-28 20:03:44'	UPDATE	4	\N	\N	\N	\N	3	\N	\N	f
200	'2020-03-28 20:03:44'	UPDATE	4	\N	\N	\N	\N	3	\N	\N	f
201	'2020-03-28 20:03:45'	UPDATE	4	\N	\N	\N	\N	3	\N	\N	f
202	'2020-03-28 20:03:45'	UPDATE	4	\N	\N	\N	\N	3	\N	\N	f
203	'2020-03-28 20:03:45'	UPDATE	4	\N	\N	\N	\N	3	\N	\N	f
204	'2020-03-28 20:03:45'	UPDATE	4	\N	\N	\N	\N	3	\N	\N	f
205	'2020-03-28 20:03:45'	UPDATE	4	\N	\N	\N	\N	3	\N	\N	f
206	'2020-03-28 20:03:45'	UPDATE	4	\N	\N	\N	\N	3	\N	\N	f
207	'2020-03-28 20:03:45'	UPDATE	4	\N	\N	\N	\N	3	\N	\N	f
208	'2020-03-28 20:03:45'	UPDATE	4	\N	\N	\N	\N	3	\N	\N	f
209	'2020-03-28 20:03:45'	UPDATE	4	\N	\N	\N	\N	3	\N	\N	f
210	'2020-03-28 20:03:45'	UPDATE	4	\N	\N	\N	\N	3	\N	\N	f
211	'2020-03-28 20:03:58'	UPDATE	4	\N	\N	\N	\N	4	\N	\N	f
212	'2020-03-28 20:03:58'	UPDATE	4	\N	\N	\N	\N	4	\N	\N	f
213	'2020-03-28 20:03:58'	UPDATE	4	\N	\N	\N	\N	4	\N	\N	f
214	'2020-03-28 20:03:59'	UPDATE	4	\N	\N	\N	\N	4	\N	\N	f
215	'2020-03-28 20:03:59'	UPDATE	4	\N	\N	\N	\N	4	\N	\N	f
216	'2020-03-28 20:03:59'	UPDATE	4	\N	\N	\N	\N	4	\N	\N	f
217	'2020-03-28 20:03:59'	UPDATE	4	\N	\N	\N	\N	4	\N	\N	f
218	'2020-03-28 20:03:59'	UPDATE	4	\N	\N	\N	\N	4	\N	\N	f
219	'2020-03-28 20:03:59'	UPDATE	4	\N	\N	\N	\N	4	\N	\N	f
220	'2020-03-28 20:03:59'	UPDATE	4	\N	\N	\N	\N	4	\N	\N	f
221	'2020-03-28 20:03:59'	UPDATE	4	\N	\N	\N	\N	4	\N	\N	f
222	'2020-03-28 20:03:59'	UPDATE	4	\N	\N	\N	\N	4	\N	\N	f
223	'2020-03-28 20:03:59'	UPDATE	4	\N	\N	\N	\N	4	\N	\N	f
224	'2020-03-28 20:03:59'	UPDATE	4	\N	\N	\N	\N	4	\N	\N	f
225	'2020-03-28 20:03:59'	UPDATE	4	\N	\N	\N	\N	4	\N	\N	f
226	'2020-03-28 20:03:59'	UPDATE	4	\N	\N	\N	\N	4	\N	\N	f
227	'2020-03-28 20:03:59'	UPDATE	4	\N	\N	\N	\N	4	\N	\N	f
228	'2020-03-28 20:03:59'	UPDATE	4	\N	\N	\N	\N	4	\N	\N	f
229	'2020-03-28 20:03:59'	UPDATE	4	\N	\N	\N	\N	4	\N	\N	f
230	'2020-03-28 20:03:59'	UPDATE	4	\N	\N	\N	\N	4	\N	\N	f
231	'2020-03-28 20:04:17'	UPDATE	4	\N	\N	\N	\N	2	\N	\N	f
232	'2020-03-28 20:04:17'	UPDATE	4	\N	\N	\N	\N	2	\N	\N	f
233	'2020-03-28 20:04:17'	UPDATE	4	\N	\N	\N	\N	2	\N	\N	f
234	'2020-03-28 20:04:17'	UPDATE	4	\N	\N	\N	\N	2	\N	\N	f
235	'2020-03-28 20:04:17'	UPDATE	4	\N	\N	\N	\N	2	\N	\N	f
236	'2020-03-28 20:04:17'	UPDATE	4	\N	\N	\N	\N	2	\N	\N	f
237	'2020-03-28 20:04:17'	UPDATE	4	\N	\N	\N	\N	2	\N	\N	f
238	'2020-03-28 20:04:17'	UPDATE	4	\N	\N	\N	\N	2	\N	\N	f
239	'2020-03-28 20:04:17'	UPDATE	4	\N	\N	\N	\N	2	\N	\N	f
240	'2020-03-28 20:04:17'	UPDATE	4	\N	\N	\N	\N	2	\N	\N	f
241	'2020-03-28 20:04:17'	UPDATE	4	\N	\N	\N	\N	2	\N	\N	f
242	'2020-03-28 20:04:17'	UPDATE	4	\N	\N	\N	\N	2	\N	\N	f
243	'2020-03-28 20:04:17'	UPDATE	4	\N	\N	\N	\N	2	\N	\N	f
244	'2020-03-28 20:04:17'	UPDATE	4	\N	\N	\N	\N	2	\N	\N	f
245	'2020-03-28 20:04:17'	UPDATE	4	\N	\N	\N	\N	2	\N	\N	f
246	'2020-03-28 20:04:24'	UPDATE	4	\N	\N	\N	\N	2	\N	\N	f
247	'2020-03-28 20:04:24'	UPDATE	4	\N	\N	\N	\N	2	\N	\N	f
248	'2020-03-28 20:04:24'	UPDATE	4	\N	\N	\N	\N	2	\N	\N	f
249	'2020-03-28 20:04:31'	UPDATE	4	\N	\N	\N	\N	4	\N	\N	f
250	'2020-03-28 20:04:31'	UPDATE	4	\N	\N	\N	\N	4	\N	\N	f
251	'2020-03-28 20:04:31'	UPDATE	4	\N	\N	\N	\N	4	\N	\N	f
252	'2020-03-28 20:04:31'	UPDATE	4	\N	\N	\N	\N	4	\N	\N	f
253	'2020-03-28 20:04:41'	UPDATE	4	\N	\N	\N	\N	4	\N	\N	f
254	'2020-03-28 20:04:41'	UPDATE	4	\N	\N	\N	\N	4	\N	\N	f
255	'2020-03-28 20:04:41'	UPDATE	4	\N	\N	\N	\N	4	\N	\N	f
256	'2020-03-28 20:04:41'	UPDATE	4	\N	\N	\N	\N	4	\N	\N	f
257	'2020-03-28 20:04:47'	UPDATE	4	\N	\N	\N	\N	4	\N	\N	f
258	'2020-03-28 20:04:51'	UPDATE	4	\N	\N	\N	\N	3	\N	\N	f
259	'2020-03-28 20:04:54'	UPDATE	4	\N	\N	\N	\N	3	\N	\N	f
260	'2020-03-28 20:04:56'	UPDATE	4	\N	\N	\N	\N	2	\N	\N	f
261	'2020-03-28 20:05:25'	UPDATE	4	\N	\N	\N	\N	2	\N	\N	f
262	'2020-03-28 20:05:29'	UPDATE	4	\N	\N	\N	\N	4	\N	\N	f
263	'2020-03-28 20:05:29'	UPDATE	4	\N	\N	\N	\N	4	\N	\N	f
264	'2020-03-28 20:05:35'	UPDATE	4	\N	\N	\N	\N	4	\N	\N	f
265	'2020-03-28 20:05:35'	UPDATE	4	\N	\N	\N	\N	4	\N	\N	f
266	'2020-03-28 20:05:58'	UPDATE	7	\N	\N	\N	\N	2	\N	\N	f
267	'2020-03-28 20:05:58'	UPDATE	7	\N	\N	\N	\N	2	\N	\N	f
268	'2020-03-28 20:05:58'	UPDATE	7	\N	\N	\N	\N	2	\N	\N	f
269	'2020-03-28 20:06:13'	UPDATE	7	\N	\N	\N	\N	3	\N	\N	f
270	'2020-03-28 20:06:23'	UPDATE	7	\N	\N	\N	\N	5	\N	\N	f
271	'2020-03-28 20:06:48'	UPDATE	6	\N	\N	\N	\N	2	\N	\N	f
272	'2020-03-28 20:06:49'	UPDATE	6	\N	\N	\N	\N	2	\N	\N	f
273	'2020-03-28 20:06:49'	UPDATE	6	\N	\N	\N	\N	2	\N	\N	f
274	'2020-03-28 20:07:13'	UPDATE	6	\N	\N	\N	\N	5	\N	\N	f
275	'2020-03-28 20:07:52'	UPDATE	5	\N	\N	\N	\N	2	\N	\N	f
276	'2020-03-28 20:07:52'	UPDATE	5	\N	\N	\N	\N	2	\N	\N	f
277	'2020-03-28 20:07:52'	UPDATE	5	\N	\N	\N	\N	2	\N	\N	f
278	'2020-03-28 20:07:52'	UPDATE	5	\N	\N	\N	\N	3	\N	\N	f
279	'2020-03-28 20:07:52'	UPDATE	5	\N	\N	\N	\N	2	\N	\N	f
280	'2020-03-28 20:07:52'	UPDATE	5	\N	\N	\N	\N	2	\N	\N	f
281	'2020-03-28 20:07:52'	UPDATE	5	\N	\N	\N	\N	2	\N	\N	f
282	'2020-03-28 20:08:12'	UPDATE	5	\N	\N	\N	\N	2	\N	\N	f
283	'2020-03-28 20:08:12'	UPDATE	5	\N	\N	\N	\N	2	\N	\N	f
284	'2020-03-28 20:08:12'	UPDATE	5	\N	\N	\N	\N	2	\N	\N	f
285	'2020-03-28 20:08:12'	UPDATE	5	\N	\N	\N	\N	2	\N	\N	f
286	'2020-03-28 20:08:12'	UPDATE	5	\N	\N	\N	\N	2	\N	\N	f
287	'2020-03-28 20:08:18'	UPDATE	5	\N	\N	\N	\N	2	\N	\N	f
288	'2020-03-28 20:08:18'	UPDATE	5	\N	\N	\N	\N	2	\N	\N	f
289	'2020-03-28 20:08:18'	UPDATE	5	\N	\N	\N	\N	2	\N	\N	f
290	'2020-03-28 20:08:18'	UPDATE	5	\N	\N	\N	\N	2	\N	\N	f
291	'2020-03-28 20:08:18'	UPDATE	5	\N	\N	\N	\N	2	\N	\N	f
292	'2020-03-28 20:08:23'	UPDATE	5	\N	\N	\N	\N	2	\N	\N	f
293	'2020-03-28 20:08:23'	UPDATE	5	\N	\N	\N	\N	2	\N	\N	f
294	'2020-03-28 20:08:23'	UPDATE	5	\N	\N	\N	\N	2	\N	\N	f
295	'2020-03-28 20:08:23'	UPDATE	5	\N	\N	\N	\N	2	\N	\N	f
296	'2020-03-28 20:08:23'	UPDATE	5	\N	\N	\N	\N	2	\N	\N	f
297	'2020-03-28 20:08:41'	UPDATE	5	\N	\N	\N	\N	5	\N	\N	f
298	'2020-03-28 20:08:41'	UPDATE	5	\N	\N	\N	\N	5	\N	\N	f
299	'2020-03-28 20:08:41'	UPDATE	5	\N	\N	\N	\N	5	\N	\N	f
300	'2020-03-28 20:08:41'	UPDATE	5	\N	\N	\N	\N	5	\N	\N	f
301	'2020-03-28 20:08:41'	UPDATE	5	\N	\N	\N	\N	5	\N	\N	f
302	'2020-03-28 20:08:48'	UPDATE	5	\N	\N	\N	\N	2	\N	\N	f
303	'2020-03-28 20:08:48'	UPDATE	5	\N	\N	\N	\N	2	\N	\N	f
304	'2020-03-28 20:08:51'	UPDATE	5	\N	\N	\N	\N	5	\N	\N	f
305	'2020-03-28 20:08:51'	UPDATE	5	\N	\N	\N	\N	5	\N	\N	f
306	'2020-03-28 20:08:53'	UPDATE	5	\N	\N	\N	\N	5	\N	\N	f
307	'2020-03-28 20:08:53'	UPDATE	5	\N	\N	\N	\N	5	\N	\N	f
308	'2020-03-28 20:09:06'	UPDATE	4	\N	\N	\N	\N	3	\N	\N	f
309	'2020-03-28 20:09:06'	UPDATE	4	\N	\N	\N	\N	3	\N	\N	f
310	'2020-03-28 20:09:09'	UPDATE	4	\N	\N	\N	\N	4	\N	\N	f
311	'2020-03-28 20:09:09'	UPDATE	4	\N	\N	\N	\N	4	\N	\N	f
312	'2020-03-28 20:09:15'	UPDATE	4	\N	\N	\N	\N	4	\N	\N	f
313	'2020-03-28 20:09:15'	UPDATE	4	\N	\N	\N	\N	4	\N	\N	f
314	'2020-03-28 20:09:48'	UPDATE	4	\N	\N	\N	\N	3	\N	\N	f
315	'2020-03-28 20:09:48'	UPDATE	4	\N	\N	\N	\N	3	\N	\N	f
316	'2020-03-28 20:09:48'	UPDATE	4	\N	\N	\N	\N	3	\N	\N	f
317	'2020-03-28 20:10:08'	UPDATE	5	\N	\N	\N	\N	2	\N	\N	f
318	'2020-03-28 20:10:08'	UPDATE	5	\N	\N	\N	\N	2	\N	\N	f
319	'2020-03-28 20:10:08'	UPDATE	5	\N	\N	\N	\N	2	\N	\N	f
320	'2020-03-28 20:12:44'	UPDATE	4	\N	\N	\N	\N	6	\N	\N	f
321	'2020-03-28 20:12:44'	UPDATE	4	\N	\N	\N	\N	6	\N	\N	f
322	'2020-03-28 20:12:44'	UPDATE	4	\N	\N	\N	\N	6	\N	\N	f
323	'2020-03-28 20:12:44'	UPDATE	4	\N	\N	\N	\N	6	\N	\N	f
324	'2020-03-28 20:12:44'	UPDATE	4	\N	\N	\N	\N	6	\N	\N	f
325	'2020-03-28 20:12:44'	UPDATE	4	\N	\N	\N	\N	6	\N	\N	f
326	'2020-03-28 20:12:44'	UPDATE	4	\N	\N	\N	\N	6	\N	\N	f
327	'2020-03-28 20:12:44'	UPDATE	4	\N	\N	\N	\N	6	\N	\N	f
328	'2020-03-28 20:12:44'	UPDATE	4	\N	\N	\N	\N	6	\N	\N	f
329	'2020-03-28 20:12:44'	UPDATE	4	\N	\N	\N	\N	6	\N	\N	f
330	'2020-03-28 20:13:27'	UPDATE	4	\N	\N	\N	\N	6	\N	\N	f
331	'2020-03-28 20:13:27'	UPDATE	4	\N	\N	\N	\N	6	\N	\N	f
332	'2020-03-28 20:13:30'	UPDATE	4	\N	\N	\N	\N	6	\N	\N	f
333	'2020-03-28 20:13:30'	UPDATE	4	\N	\N	\N	\N	6	\N	\N	f
334	'2020-03-28 20:13:30'	UPDATE	4	\N	\N	\N	\N	6	\N	\N	f
335	'2020-03-28 20:13:30'	UPDATE	4	\N	\N	\N	\N	6	\N	\N	f
336	'2020-03-28 20:13:30'	UPDATE	4	\N	\N	\N	\N	6	\N	\N	f
337	'2020-03-28 20:13:30'	UPDATE	4	\N	\N	\N	\N	6	\N	\N	f
338	'2020-03-28 20:13:30'	UPDATE	4	\N	\N	\N	\N	6	\N	\N	f
339	'2020-03-28 20:13:30'	UPDATE	4	\N	\N	\N	\N	6	\N	\N	f
340	'2020-03-28 20:13:30'	DELETE	4	\N	\N	\N	\N	6	\N	\N	f
341	'2020-03-28 20:14:06'	UPDATE	4	\N	\N	\N	\N	7	\N	\N	f
342	'2020-03-28 20:14:06'	UPDATE	4	\N	\N	\N	\N	7	\N	\N	f
343	'2020-03-28 20:14:06'	UPDATE	4	\N	\N	\N	\N	7	\N	\N	f
344	'2020-03-28 20:14:06'	UPDATE	4	\N	\N	\N	\N	7	\N	\N	f
345	'2020-03-28 20:14:06'	UPDATE	4	\N	\N	\N	\N	7	\N	\N	f
346	'2020-03-28 20:14:10'	UPDATE	4	\N	\N	\N	\N	7	\N	\N	f
347	'2020-03-28 20:14:10'	UPDATE	4	\N	\N	\N	\N	7	\N	\N	f
348	'2020-03-28 20:14:14'	UPDATE	4	\N	\N	\N	\N	7	\N	\N	f
349	'2020-03-28 20:14:14'	UPDATE	4	\N	\N	\N	\N	7	\N	\N	f
350	'2020-03-28 20:14:17'	UPDATE	4	\N	\N	\N	\N	7	\N	\N	f
351	'2020-03-28 20:14:19'	DELETE	4	\N	\N	\N	\N	7	\N	\N	f
352	'2020-03-28 20:14:44'	UPDATE	6	\N	\N	\N	\N	5	\N	\N	f
353	'2020-03-28 20:14:45'	UPDATE	6	\N	\N	\N	\N	5	\N	\N	f
354	'2020-03-29 10:43:05'	UPDATE	4	\N	\N	\N	\N	2	\N	\N	f
355	'2020-03-29 10:43:05'	UPDATE	4	\N	\N	\N	\N	2	\N	\N	f
356	'2020-03-29 10:43:05'	UPDATE	4	\N	\N	\N	\N	2	\N	\N	f
357	'2020-03-29 10:43:10'	UPDATE	4	\N	\N	\N	\N	2	\N	\N	f
358	'2020-03-29 10:43:10'	UPDATE	4	\N	\N	\N	\N	2	\N	\N	f
359	'2020-03-29 10:43:10'	UPDATE	4	\N	\N	\N	\N	2	\N	\N	f
360	'2020-03-28 20:04:24'	CREATE	4	\N	1	\N	\N	\N	\N	\N	f
361	'2020-03-28 20:04:31'	CREATE	4	\N	2	\N	\N	\N	\N	\N	f
362	'2020-03-28 20:04:36'	CREATE	4	\N	3	\N	\N	\N	\N	\N	f
363	'2020-03-28 20:04:41'	DELETE	4	\N	2	\N	\N	\N	\N	\N	f
364	'2022-09-09 07:18:28'	CREATE	4	5	\N	\N	\N	\N	\N	\N	f
365	'2023-07-10 12:17:57'	UPLOAD	7	\N	\N	\N	34	\N	\N	\N	f
366	'2023-07-10 12:18:06'	UPLOAD	7	\N	\N	\N	35	\N	\N	\N	f
367	'2023-07-10 12:18:15'	UPLOAD	7	\N	\N	\N	36	\N	\N	\N	f
368	'2023-07-10 12:18:30'	UPLOAD	7	\N	\N	\N	37	\N	\N	\N	f
369	'2023-07-10 12:18:56'	CREATE	4	\N	6	\N	\N	\N	\N	\N	f
370	'2023-07-10 12:19:42'	UPDATE	4	\N	\N	\N	36	\N	\N	\N	f
371	'2023-07-10 12:20:09'	UPDATE	4	\N	\N	\N	37	\N	\N	\N	f
372	'2023-07-10 12:20:15'	UPDATE	4	\N	\N	\N	34	\N	\N	\N	f
373	'2023-07-10 12:20:33'	UPDATE	7	\N	\N	\N	35	\N	\N	\N	f
374	'2023-07-10 12:21:37'	UPLOAD	5	\N	\N	\N	38	\N	\N	\N	f
375	'2023-07-10 12:21:47'	UPLOAD	5	\N	\N	\N	39	\N	\N	\N	f
376	'2023-07-10 12:22:15'	UPLOAD	4	\N	\N	\N	40	\N	\N	\N	f
377	'2023-07-10 12:24:58'	CREATE	4	6	\N	\N	\N	\N	\N	\N	f
378	'2023-07-10 12:25:15'	CREATE	4	7	\N	\N	\N	\N	\N	\N	f
379	'2023-07-10 12:32:02'	UPLOAD	4	\N	\N	\N	41	\N	\N	\N	f
380	'2023-07-10 12:32:32'	UPLOAD	4	\N	\N	\N	42	\N	\N	\N	f
381	'2023-07-10 12:33:27'	UPLOAD	4	\N	\N	\N	43	\N	\N	\N	f
382	'2023-07-10 12:34:02'	UPLOAD	4	\N	\N	\N	44	\N	\N	\N	f
383	'2023-07-10 12:34:38'	CREATE	4	8	\N	\N	\N	\N	\N	\N	f
384	'2023-07-10 12:35:00'	CREATE	4	9	\N	\N	\N	\N	\N	\N	f
385	'2023-07-10 12:35:08'	CREATE	4	10	\N	\N	\N	\N	\N	\N	f
386	'2023-07-10 12:35:21'	CREATE	4	11	\N	\N	\N	\N	\N	\N	f
387	'2023-07-10 12:36:04'	UPLOAD	7	\N	\N	\N	45	\N	\N	\N	f
388	'2023-07-10 12:36:18'	UPLOAD	7	\N	\N	\N	46	\N	\N	\N	f
389	'2023-11-15 11:11:11'	CREATE	4	\N	\N	\N	\N	\N	\N	1	f
390	'2023-12-04 11:12:25'	CREATE	7	\N	\N	\N	\N	\N	\N	2	f
391	'2023-12-05 11:12:54'	CREATE	7	\N	\N	\N	\N	\N	\N	3	f
392	'2023-12-06 09:13:31'	UPDATE	7	\N	\N	\N	\N	\N	\N	3	f
393	'2023-12-06 11:49:44'	CREATE	7	\N	\N	\N	\N	\N	\N	4	f
394	'2023-12-07 23:59:59'	UPDATE	7	\N	\N	\N	\N	\N	\N	4	f
395	'2024-01-19 13:42:26'	CREATE	4	\N	\N	\N	\N	\N	\N	5	f
396	'2024-01-19 13:42:32'	CREATE	5	\N	\N	\N	\N	\N	\N	6	f
397	'2024-01-19 13:42:34'	CREATE	6	\N	\N	\N	\N	\N	\N	7	f
398	'2024-01-19 13:42:35'	CREATE	7	\N	\N	\N	\N	\N	\N	8	f
399	'2024-11-19 11:22:33'	UPLOAD	7	\N	\N	\N	47	\N	\N	\N	f
\.


--
-- Data for Name: changed_variable; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.changed_variable (id, changed_variable, value_before, value_after, newsfeed_event_id, deleted) FROM stdin;
1	created			1	f
2	created			2	f
3	created			3	f
4	accepted	false	true	4	f
5	accepted	false	true	5	f
6	accepted	false	true	6	f
7	created			7	f
8	created			8	f
9	accepted	false	true	9	f
10	accepted	false	true	10	f
11	created			11	f
12	created			12	f
13	accepted	false	true	13	f
14	accepted	false	true	14	f
15	created			15	f
16	accepted	false	true	16	f
17	created			17	f
18	accepted	false	true	18	f
19	created			19	f
20	accepted	false	true	20	f
21	created			21	f
22	accepted	false	true	22	f
23	created			23	f
24	accepted	false	true	24	f
25	created			25	f
26	created			26	f
27	created			27	f
28	created			28	f
29	created			29	f
30	created			30	f
31	created			31	f
32	created			32	f
33	deleted			33	f
34	created			34	f
35	created			35	f
36	created			36	f
37	accepted	false	true	37	f
38	deleted			38	f
39	accepted	false	true	39	f
40	accepted	false	true	40	f
41	accepted	false	true	41	f
42	deleted			42	f
43	accepted	false	true	43	f
44	deleted			44	f
45	deleted			45	f
46	deleted			46	f
47	created			47	f
48	accepted	false	true	48	f
49	created			49	f
50	accepted	false	true	50	f
51	deleted			51	f
52	deleted			52	f
53	created			53	f
54	accepted	false	true	54	f
55	deleted			55	f
56	created			56	f
57	created			57	f
58	created			58	f
59	created			59	f
60	created			60	f
61	created			61	f
62	created			62	f
63	last logged in	null	Sat Mar 28 20:00:13 UTC 2020	63	f
64	active	true	false	64	f
65	last logged in	null	Sat Mar 28 20:05:47 UTC 2020	65	f
66	last logged in	null	Sat Mar 28 20:06:31 UTC 2020	66	f
67	last logged in	null	Sat Mar 28 20:07:31 UTC 2020	67	f
68	last logged in	2020-03-28 00:00:00.0	Sat Mar 28 20:09:02 UTC 2020	68	f
69	last logged in	2020-03-28 00:00:00.0	Sat Mar 28 20:09:56 UTC 2020	69	f
70	last logged in	2020-03-28 00:00:00.0	Sat Mar 28 20:12:06 UTC 2020	70	f
71	last logged in	2020-03-28 00:00:00.0	Sat Mar 28 20:14:30 UTC 2020	71	f
72	last logged in	2020-03-28 00:00:00.0	Sun Mar 29 10:42:45 UTC 2020	72	f
73	created			73	f
74	last logged in	null	Thu Aug 20 09:58:52 GMT 2020	74	f
75	last logged in	2020-03-28 00:00:00.0	Thu Aug 20 09:59:08 GMT 2020	75	f
76	active	true	false	76	f
77	created			77	f
78	last logged in	null	Tue Aug 25 12:35:38 GMT 2020	78	f
79	created			79	f
80	created			80	f
81	created			81	f
82	created			82	f
83	created			83	f
84	source	/opt/rdepot/new/28009317/accrued_1.1.tar.gz	/opt/rdepot/repositories/3/46950998/accrued_1.1.tar.gz	84	f
85	source	/opt/rdepot/new/43623400/accrued_1.3.5.tar.gz	/opt/rdepot/repositories/3/99077116/accrued_1.3.5.tar.gz	85	f
86	source	/opt/rdepot/new/47097172/accrued_1.2.tar.gz	/opt/rdepot/repositories/3/83118397/accrued_1.2.tar.gz	86	f
87	active	false	true	87	f
88	active	false	true	88	f
89	active	false	true	89	f
90	source	/opt/rdepot/new/88466387/accrued_1.3.tar.gz	/opt/rdepot/repositories/3/82197810/accrued_1.3.tar.gz	90	f
91	source	/opt/rdepot/new/47523656/accrued_1.4.tar.gz	/opt/rdepot/repositories/3/28075835/accrued_1.4.tar.gz	91	f
92	active	false	true	92	f
93	active	false	true	93	f
94	created			94	f
95	created			95	f
96	created			96	f
97	created			97	f
98	source	/opt/rdepot/new/77598514/A3_0.9.2.tar.gz	/opt/rdepot/repositories/4/54491936/A3_0.9.2.tar.gz	98	f
99	source	/opt/rdepot/new/6984008/A3_0.9.1.tar.gz	/opt/rdepot/repositories/4/47098069/A3_0.9.1.tar.gz	99	f
100	active	false	true	100	f
101	active	false	true	101	f
102	source	/opt/rdepot/new/98224569/abc_1.3.tar.gz	/opt/rdepot/repositories/4/95296712/abc_1.3.tar.gz	102	f
103	active	false	true	103	f
104	source	/opt/rdepot/new/18685235/abc_1.0.tar.gz	/opt/rdepot/repositories/4/49426769/abc_1.0.tar.gz	104	f
105	active	false	true	105	f
106	created			106	f
107	created			107	f
108	created			108	f
109	source	/opt/rdepot/new/68910623/bea.R_1.0.5.tar.gz	/opt/rdepot/repositories/2/89565416/bea.R_1.0.5.tar.gz	109	f
110	active	false	true	110	f
111	source	/opt/rdepot/new/13236487/accrued_1.0.tar.gz	/opt/rdepot/repositories/2/40553654/accrued_1.0.tar.gz	111	f
112	active	false	true	112	f
113	source	/opt/rdepot/new/16258274/npordtests_1.1.tar.gz	/opt/rdepot/repositories/2/8436419/npordtests_1.1.tar.gz	113	f
114	active	false	true	114	f
115	maintainer	5	4	115	f
116	maintainer	5	4	116	f
117	maintainer	5	4	117	f
118	maintainer	5	4	118	f
119	maintainer	5	4	119	f
120	maintainer	5	4	120	f
121	maintainer	5	4	121	f
122	maintainer	4	5	122	f
123	maintainer	4	5	123	f
124	maintainer	4	5	124	f
125	maintainer	4	5	125	f
126	active	true	false	126	f
127	active	true	false	127	f
128	active	true	false	128	f
129	active	true	false	129	f
130	maintainer	6	5	130	f
131	maintainer	6	4	131	f
132	maintainer	6	4	132	f
133	maintainer	6	4	133	f
134	maintainer	6	4	134	f
135	created			135	f
136	created			136	f
137	created			137	f
138	created			138	f
139	created			139	f
140	created			140	f
141	created			141	f
142	created			142	f
143	created			143	f
144	created			144	f
145	created			145	f
146	maintainer	5	4	146	f
147	source	/opt/rdepot/new/37946660/usl_2.0.0.tar.gz	/opt/rdepot/repositories/2/33930690/usl_2.0.0.tar.gz	147	f
148	active	false	true	148	f
149	maintainer	5	4	149	f
150	source	/opt/rdepot/new/30320032/A3_0.9.2.tar.gz	/opt/rdepot/repositories/2/9907084/A3_0.9.2.tar.gz	150	f
151	active	false	true	151	f
152	maintainer	5	4	152	f
153	source	/opt/rdepot/new/26771812/abc_1.3.tar.gz	/opt/rdepot/repositories/2/88170013/abc_1.3.tar.gz	153	f
154	active	false	true	154	f
155	maintainer	5	4	155	f
156	source	/opt/rdepot/new/19806985/Benchmarking_0.10.tar.gz	/opt/rdepot/repositories/2/71228208/Benchmarking_0.10.tar.gz	156	f
157	active	false	true	157	f
158	maintainer	5	4	158	f
159	source	/opt/rdepot/new/9104202/AnaCoDa_0.1.2.3.tar.gz	/opt/rdepot/repositories/5/39437028/AnaCoDa_0.1.2.3.tar.gz	159	f
160	active	false	true	160	f
161	delete	false	true	161	f
162	delete	false	true	162	f
163	created			163	f
164	created			164	f
165	source	/opt/rdepot/new/73393322/usl_2.0.0.tar.gz	/opt/rdepot/repositories/6/21695389/usl_2.0.0.tar.gz	165	f
166	active	false	true	166	f
167	source	/opt/rdepot/new/28573212/visdat_0.1.0.tar.gz	/opt/rdepot/repositories/6/70325377/visdat_0.1.0.tar.gz	167	f
168	active	false	true	168	f
169	delete	false	true	169	f
170	delete	false	true	170	f
171	created			171	f
172	source	/opt/rdepot/new/33345471/A3_0.9.1.tar.gz	/opt/rdepot/repositories/7/67484296/A3_0.9.1.tar.gz	172	f
173	active	false	true	173	f
174	delete	false	true	174	f
175	created			175	f
176	created			176	f
177	maintainer	6	5	177	f
178	maintainer	5	6	178	f
179	created			179	f
180	created			180	f
181	created			181	f
182	created			182	f
183	deleted			183	f
184	publication URI	http://localhost/testrepo1	http://localhost/repo/testrepo1	184	f
185	server address	http://localhost/testrepo1	http://oa-rdepot-repo:8080/testrepo1	185	f
186	submitted		4	186	f
187	submitted		5	187	f
188	submitted		6	188	f
189	version	0	1	189	f
190	version	0	1	190	f
191	version	0	1	191	f
192	version	1	2	192	f
193	version	1	2	193	f
194	version	1	2	194	f
195	version	2	3	195	f
196	version	2	3	196	f
197	added		8	197	f
198	added		4	198	f
199	version	2	3	199	f
200	added		6	200	f
201	submitted		7	201	f
202	submitted		8	202	f
203	version	0	1	203	f
204	version	0	1	204	f
205	version	1	2	205	f
206	version	2	3	206	f
207	added		5	207	f
208	version	1	2	208	f
209	version	2	3	209	f
210	added		7	210	f
211	submitted		9	211	f
212	submitted		10	212	f
213	version	0	1	213	f
214	version	0	1	214	f
215	version	1	2	215	f
216	version	2	3	216	f
217	added		10	217	f
218	version	1	2	218	f
219	version	2	3	219	f
220	added		9	220	f
221	submitted		11	221	f
222	version	0	1	222	f
223	version	1	2	223	f
224	version	2	3	224	f
225	added		11	225	f
226	submitted		12	226	f
227	version	0	1	227	f
228	version	1	2	228	f
229	version	2	3	229	f
230	added		12	230	f
231	submitted		13	231	f
232	version	0	1	232	f
233	version	1	2	233	f
234	version	2	3	234	f
235	added		15	235	f
236	submitted		14	236	f
237	version	0	1	237	f
238	version	1	2	238	f
239	version	2	3	239	f
240	added		13	240	f
241	submitted		15	241	f
242	version	0	1	242	f
243	version	1	2	243	f
244	version	2	3	244	f
245	added		14	245	f
246	version	3	4	246	f
247	version	4	5	247	f
248	version	5	6	248	f
249	version	3	4	249	f
250	version	4	5	250	f
251	version	5	6	251	f
252	version	6	7	252	f
253	version	7	8	253	f
254	version	8	9	254	f
255	version	9	10	255	f
256	version	10	11	256	f
257	version	11	12	257	f
258	version	3	4	258	f
259	version	4	5	259	f
260	version	6	7	260	f
261	version	7	8	261	f
262	version	12	13	262	f
263	version	13	14	263	f
264	version	14	15	264	f
265	version	15	16	265	f
266	submitted		16	266	f
267	submitted		17	267	f
268	submitted		18	268	f
269	submitted		19	269	f
270	submitted		20	270	f
271	submitted		21	271	f
272	submitted		22	272	f
273	submitted		23	273	f
274	submitted		24	274	f
275	submitted		25	275	f
276	version	8	9	276	f
277	version	9	10	277	f
278	submitted		26	278	f
279	version	10	11	279	f
280	version	11	12	280	f
281	added		25	281	f
282	version	12	13	282	f
283	version	13	14	283	f
284	version	14	15	284	f
285	version	15	16	285	f
286	added		17	286	f
287	version	16	17	287	f
288	version	17	18	288	f
289	version	18	19	289	f
290	version	19	20	290	f
291	added		18	291	f
292	version	20	21	292	f
293	version	21	22	293	f
294	version	22	23	294	f
295	version	23	24	295	f
296	added		21	296	f
297	version	0	1	297	f
298	version	1	2	298	f
299	version	2	3	299	f
300	version	3	4	300	f
301	added		20	301	f
302	published			302	f
303	version	24	25	303	f
304	published			304	f
305	version	4	5	305	f
306	published	true	false	306	f
307	version	5	6	307	f
308	published			308	f
309	version	5	6	309	f
310	published			310	f
311	version	16	17	311	f
312	published	true	false	312	f
313	version	17	18	313	f
314	version	6	7	314	f
315	published			315	f
316	version	7	8	316	f
317	version	25	26	317	f
318	published			318	f
319	version	26	27	319	f
320	submitted		27	320	f
321	version	0	1	321	f
322	version	1	2	322	f
323	version	2	3	323	f
324	added		27	324	f
325	submitted		28	325	f
326	version	0	1	326	f
327	version	1	2	327	f
328	version	2	3	328	f
329	added		28	329	f
330	published			330	f
331	version	3	4	331	f
332	version	4	5	332	f
333	published			333	f
334	version	5	6	334	f
335	version	6	7	335	f
336	published			336	f
337	version	7	8	337	f
338	published	true	false	338	f
339	version	8	9	339	f
340	deleted			340	f
341	submitted		29	341	f
342	version	0	1	342	f
343	version	1	2	343	f
344	version	2	3	344	f
345	added		29	345	f
346	published			346	f
347	version	3	4	347	f
348	published	true	false	348	f
349	version	4	5	349	f
350	version	5	6	350	f
351	deleted			351	f
352	submitted		30	352	f
353	submitted		31	353	f
354	version	27	28	354	f
355	published			355	f
356	version	28	29	356	f
357	version	29	30	357	f
358	published			358	f
359	version	30	31	359	f
360	created			360	f
361	created			361	f
362	created			362	f
363	deleted		Sat Mar 28 20:04:41 UTC 2020	363	f
364	approver_id		4	370	f
365	state	rejected	rejected	370	f
366	state	waiting	accepted	371	f
367	approver_id		4	371	f
368	state	waiting	accepted	372	f
369	approver_id		4	372	f
370	approver_id		7	373	f
371	state	rejected	cancelled	373	f
372	active	true	false	392	f
373	active	true	false	394	f
\.

--
-- Data for Name: user_settings; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.user_settings (id, deleted, language, theme, page_size, user_id) FROM stdin;
1	f	pl-PL	light	20	4
2	f	pl-PL	light	10	5
\.


--
-- Data for Name: access_token; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.access_token (id, user_id, name, value, creation_date, expiration_date, active, deleted) FROM stdin;
1	4	try	$2a$10$3TW2U4rpOPOPmBhjzi/fhOc9wXuOzToKm50RBqOrHBPM26XcSwHmm	2023-11-15	2024-11-15	t	f
2	7	second	$2a$10$SWxoWZVvKAlB0/50Eue5I.zaNISqvMNQt0z1R.uW9ffzuVAumSFeW	2023-12-04	2024-12-04	t	f
3	7	deactivated	$2a$10$lYWNkyYCl0tFS70wbimw4u9jUFM.jZzGTarEu46KsoqdiQROGDgaK	2023-12-05	2023-12-10	f	f
4	7	expired	$2a$10$ehM3ODJi6FNJcABKlHT1AOxmpuQZKB7RxnCCC9z8tI7E6tJWFfqlS	2023-12-06	2023-12-07	f	f
5	4	century	$2a$10$pRIEsjpErZ3pt.k2.9yZ3.l8YV9GATfH3vKe4gZuM2IdF3yfct56C	2024-01-19	2124-12-31	t	f
6	5	century	$2a$10$BCP6uFaJRLJD2fx3IWDRNOnFkkKTugWjCFIe8CNoC.iR.MbM.ubOO	2024-01-19	2124-12-31	t	f
7	6	century	$2a$10$bAprhSNIWmFXBY5RA9SgKOpmGpaeTzcrDWXceawLbky6WFG.3nqqK	2024-01-19	2124-12-31	t	f
8	7	century	$2a$10$sfnJ0DtxdmqCOGemySK1qOFT1COAWTHguCEa9NlIH/ZqcYt7fTLsK	2024-01-19	2124-12-31	t	f
\.

--
-- Name: access_token_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.access_token_id_seq', 8, true);

--
-- Name: api_token_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.api_token_id_seq', 5, true);


--
-- Name: changed_variable_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.changed_variable_id_seq', 374, true);


--
-- Name: newsfeed_event_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.newsfeed_event_id_seq', 399, true);


--
-- Name: package_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.package_id_seq', 47, true);


--
-- Name: package_maintainer_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.package_maintainer_id_seq', 11, true);


--
-- Name: repository_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.repository_id_seq', 12, true);


--
-- Name: repository_maintainer_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.repository_maintainer_id_seq', 6, true);


--
-- Name: role_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.role_id_seq', 4, true);


--
-- Name: submission_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.submission_id_seq', 47, true);


--
-- Name: user_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.user_id_seq', 10, true);

--
-- Name: user_settings_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.user_settings_id_seq', 2, true);
