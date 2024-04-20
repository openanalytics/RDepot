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

COPY public.repository (id, version, publication_uri, name, server_address, published, deleted, resource_technology) FROM stdin;
5	6	http://localhost/repo/testrepo4	testrepo4	http://oa-rdepot-repo:8080/testrepo4	f	f	R
4	18	http://localhost/repo/testrepo3	testrepo3	http://oa-rdepot-repo:8080/testrepo3	f	f	R
3	8	http://localhost/repo/testrepo2	testrepo2	http://oa-rdepot-repo:8080/testrepo2	t	f	R
6	9	http://localhost/repo/testrepo5	testrepo5	http://oa-rdepot-repo:8080/testrepo5	f	t	R
7	6	http://localhost/repo/testrepo6	testrepo6	http://oa-rdepot-repo:8080/testrepo6	f	t	R
2	31	http://localhost/repo/testrepo1	testrepo1	http://oa-rdepot-repo:8080/testrepo1	t	f	R
11	1	http://localhost/repo/testrepo11	testrepo11	http://oa-rdepot-repo:8080/testrepo11	t	t	Python
12	1	http://localhost/repo/testrepo12	testrepo12	http://oa-rdepot-repo:8080/testrepo12	f	t	Python
10	7	http://localhost/repo/testrepo10	testrepo10	http://oa-rdepot-repo:8080/testrepo10	f	f	Python
9	4	http://localhost/repo/testrepo9	testrepo9	http://oa-rdepot-repo:8080/testrepo9	f	f	Python
8	7	http://localhost/repo/testrepo8	testrepo8	http://oa-rdepot-repo:8080/testrepo8	t	f	Python
\.

--
-- Data for Name: package; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.package (id, name, version, description, author, maintainer_id, repository_id, url, source, title, active, deleted, resource_technology) FROM stdin;
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
34	numpy	1.24.1	<h1 align="center"> <img src="/branding/logo/primary/numpylogo.svg" width="300"> </h1><br> [![Powered by NumFOCUS](https://img.shields.io/badge/powered%20by-NumFOCUS-orange.svg?style=flat&colorA=E1523D&colorB=007D8A)]( https://numfocus.org) [![PyPI Downloads](https://img.shields.io/pypi/dm/numpy.svg?label=PyPI%20downloads)]( https://pypi.org/project/numpy/) [![Conda Downloads](https://img.shields.io/conda/dn/conda-forge/numpy.svg?label=Conda%20downloads)]( https://anaconda.org/conda-forge/numpy) [![Stack Overflow](https://img.shields.io/badge/stackoverflow-Ask%20questions-blue.svg)]( https://stackoverflow.com/questions/tagged/numpy) [![Nature Paper](https://img.shields.io/badge/DOI-10.1038%2Fs41592--019--0686--2-blue)]( https://doi.org/10.1038/s41586-020-2649-2) [![OpenSSF Scorecard](https://api.securityscorecards.dev/projects/github.com/numpy/numpy/badge)](https://api.securityscorecards.dev/projects/github.com/numpy/numpy) NumPy is the fundamental package for scientific computing with Python. - **Website:** https://www.numpy.org - **Documentation:** https://numpy.org/doc - **Mailing list:** https://mail.python.org/mailman/listinfo/numpy-discussion - **Source code:** https://github.com/numpy/numpy - **Contributing:** https://www.numpy.org/devdocs/dev/index.html - **Bug reports:** https://github.com/numpy/numpy/issues - **Report a security vulnerability:** https://tidelift.com/docs/security It provides: - a powerful N-dimensional array object - sophisticated (broadcasting) functions - tools for integrating C/C++ and Fortran code - useful linear algebra, Fourier transform, and random number capabilities Testing: NumPy requires `pytest` and `hypothesis`. Tests can then be run after installation with: python -c 'import numpy; numpy.test()' Code of Conduct ---------------------- NumPy is a community-driven open source project developed by a diverse group of [contributors](https://numpy.org/teams/). The NumPy leadership has made a strong commitment to creating an open, inclusive, and positive community. Please read the [NumPy Code of Conduct](https://numpy.org/code-of-conduct/) for guidance on how to interact with others in a way that makes our community thrive. Call for Contributions ---------------------- The NumPy project welcomes your expertise and enthusiasm! Small improvements or fixes are always appreciated. If you are considering larger contributions to the source code, please contact us through the [mailing list](https://mail.python.org/mailman/listinfo/numpy-discussion) first. Writing code isn’t the only way to contribute to NumPy. You can also: - review pull requests - help us stay on top of new and old issues - develop tutorials, presentations, and other educational materials - maintain and improve [our website](https://github.com/numpy/numpy.org) - develop graphic design for our brand assets and promotional materials - translate website content - help with outreach and onboard new contributors - write grant proposals and help with other fundraising efforts For more information about the ways you can contribute to NumPy, visit [our website](https://numpy.org/contribute/). If you’re unsure where to start or how your skills fit in, reach out! You can ask on the mailing list or here, on GitHub, by opening a new issue or leaving a comment on a relevant issue that is already open. Our preferred channels of communication are all public, but if you’d like to speak to us in private first, contact our community coordinators at numpy-team@googlegroups.com or on Slack (write numpy-team@googlegroups.com for an invitation). We also have a biweekly community call, details of which are announced on the mailing list. You are very welcome to join. If you are new to contributing to open source, [this guide](https://opensource.guide/how-to-contribute/) helps explain why, what, and how to successfully get involved.	Travis E. Oliphant et al.	4	10	Bug Tracker, https://github.com/numpy/numpy/issues, Documentation, https://numpy.org/doc/1.24, Source Code, https://github.com/numpy/numpy	/opt/rdepot/repositories/10/67410940/numpy-1.24.1.tar.gz	\N	t	f	Python
35	wheel	0.36.1	wheel ===== This library is the reference implementation of the Python wheel packaging standard, as defined in `PEP 427`_. It has two different roles: #. A setuptools_ extension for building wheels that provides the ``bdist_wheel`` setuptools command #. A command line tool for working with wheel files It should be noted that wheel is **not** intended to be used as a library, and as such there is no stable, public API. .. _PEP 427: https://www.python.org/dev/peps/pep-0427/ .. _setuptools: https://pypi.org/project/setuptools/ Documentation ------------- The documentation_ can be found on Read The Docs. .. _documentation: https://wheel.readthedocs.io/ Code of Conduct --------------- Everyone interacting in the wheel project's codebases, issue trackers, chat rooms, and mailing lists is expected to follow the `PSF Code of Conduct`_. .. _PSF Code of Conduct: https://github.com/pypa/.github/blob/main/CODE_OF_CONDUCT.md	Daniel Holth	4	10	Documentation, https://wheel.readthedocs.io/, Changelog, https://wheel.readthedocs.io/en/stable/news.html, Issue Tracker, https://github.com/pypa/wheel/issues	/opt/rdepot/trash/10/74692077/wheel-0.36.1.tar.gz	\N	f	t	Python
39	setuptools	67.8.0	\N	Python Packaging Authority	6	9	Documentation, https://setuptools.pypa.io/, Changelog, https://setuptools.pypa.io/en/stable/history.html	/opt/rdepot/repositories/9/65567066/setuptools-67.8.0.tar.gz	\N	t	f	Python
38	setuptools	67.4.0	\N	Python Packaging Authority	4	9	Documentation, https://setuptools.pypa.io/, Changelog, https://setuptools.pypa.io/en/stable/history.html	/opt/rdepot/repositories/9/95184570/setuptools-67.4.0.tar.gz	\N	t	f	Python
40	pandas	2.0.1	\N	\N	8	9	homepage, https://pandas.pydata.org, documentation, https://pandas.pydata.org/docs/, repository, https://github.com/pandas-dev/pandas	/opt/rdepot/repositories/9/41196072/pandas-2.0.1.tar.gz	\N	t	f	Python
42	boto3	1.26.156	\N	Amazon Web Services	6	8	Documentation, https://boto3.amazonaws.com/v1/documentation/api/latest/index.html, Source, https://github.com/boto/boto3	/opt/rdepot/repositories/8/55941618/boto3-1.26.156.tar.gz	\N	t	t	Python
41	pandas	2.0.1	\N	\N	8	8	homepage, https://pandas.pydata.org, documentation, https://pandas.pydata.org/docs/, repository, https://github.com/pandas-dev/pandas	/opt/rdepot/repositories/8/21604316/pandas-2.0.1.tar.gz	\N	t	f	Python
43	accelerated-numpy	0.1.0	# numpy-threading-extensions Faster loops for NumPy using multithreading and other tricks. The first release will target NumPy binary and unary ufuncs. Eventually we will enable overriding other NumPy functions, and provide an C-based (non-Python) API for extending via third-party functions. [![Travis CI Build Status](https://api.travis-ci.org/Quansight/numpy-threading-extensions.svg)](https://travis-ci.org/Quansight/numpy-threading-extensions) [![Coverage Status](https://codecov.io/gh/Quansight/numpy-threading-extensions/branch/main/graphs/badge.svg)](https://codecov.io/github/Quansight/numpy-threading-extensions) [![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT) ## Installation ``` pip install accelerated_numpy ``` You can also install the in-development version 0.0.1 with: ``` pip install https://github.com/Quansight/numpy-threading-extensions/archive/v0.0.1.zip ``` or latest with ``` pip install https://github.com/Quansight/numpy-threading-extensions/archive/main.zip ``` ## Documentation To use the project: ```python import accelerated_numpy accelerated_numpy.initialize() ``` ## Development To run all the tests run:: ``` tox ``` Note, to combine the coverage data from all the tox environments run: OS | Command ----|---- Windows | `set PYTEST_ADDOPTS=--cov-append` | | `tox` Other | `PYTEST_ADDOPTS=--cov-append tox`	Matti Picus	6	8	Changelog, https://github.com/Quansight/numpy-threading-extensions/blob/master/CHANGELOG.rst, Issue Tracker, https://github.com/Quansight/numpy-threading-extensions/issues	/opt/rdepot/repositories/8/25558329/accelerated-numpy-0.1.0.tar.gz	\N	t	f	Python
44	python-dateutil	2.8.1	dateutil - powerful extensions to datetime ========================================== |pypi| |support| |licence| |gitter| |readthedocs| |travis| |appveyor| |pipelines| |coverage| .. |pypi| image:: https://img.shields.io/pypi/v/python-dateutil.svg?style=flat-square :target: https://pypi.org/project/python-dateutil/ :alt: pypi version .. |support| image:: https://img.shields.io/pypi/pyversions/python-dateutil.svg?style=flat-square :target: https://pypi.org/project/python-dateutil/ :alt: supported Python version .. |travis| image:: https://img.shields.io/travis/dateutil/dateutil/master.svg?style=flat-square&label=Travis%20Build :target: https://travis-ci.org/dateutil/dateutil :alt: travis build status .. |appveyor| image:: https://img.shields.io/appveyor/ci/dateutil/dateutil/master.svg?style=flat-square&logo=appveyor :target: https://ci.appveyor.com/project/dateutil/dateutil :alt: appveyor build status .. |pipelines| image:: https://dev.azure.com/pythondateutilazure/dateutil/_apis/build/status/dateutil.dateutil?branchName=master :target: https://dev.azure.com/pythondateutilazure/dateutil/_build/latest?definitionId=1&branchName=master :alt: azure pipelines build status .. |coverage| image:: https://codecov.io/github/dateutil/dateutil/coverage.svg?branch=master :target: https://codecov.io/github/dateutil/dateutil?branch=master :alt: Code coverage .. |gitter| image:: https://badges.gitter.im/dateutil/dateutil.svg :alt: Join the chat at https://gitter.im/dateutil/dateutil :target: https://gitter.im/dateutil/dateutil .. |licence| image:: https://img.shields.io/pypi/l/python-dateutil.svg?style=flat-square :target: https://pypi.org/project/python-dateutil/ :alt: licence .. |readthedocs| image:: https://img.shields.io/readthedocs/dateutil/latest.svg?style=flat-square&label=Read%20the%20Docs :alt: Read the documentation at https://dateutil.readthedocs.io/en/latest/ :target: https://dateutil.readthedocs.io/en/latest/ The `dateutil` module provides powerful extensions to the standard `datetime` module, available in Python. Installation ============ `dateutil` can be installed from PyPI using `pip` (note that the package name is different from the importable name):: pip install python-dateutil Download ======== dateutil is available on PyPI https://pypi.org/project/python-dateutil/ The documentation is hosted at: https://dateutil.readthedocs.io/en/stable/ Code ==== The code and issue tracker are hosted on GitHub: https://github.com/dateutil/dateutil/ Features ======== * Computing of relative deltas (next month, next year, next Monday, last week of month, etc); * Computing of relative deltas between two given date and/or datetime objects; * Computing of dates based on very flexible recurrence rules, using a superset of the `iCalendar <https://www.ietf.org/rfc/rfc2445.txt>`_ specification. Parsing of RFC strings is supported as well. * Generic parsing of dates in almost any string format; * Timezone (tzinfo) implementations for tzfile(5) format files (/etc/localtime, /usr/share/zoneinfo, etc), TZ environment string (in all known formats), iCalendar format files, given ranges (with help from relative deltas), local machine timezone, fixed offset timezone, UTC timezone, and Windows registry-based time zones. * Internal up-to-date world timezone information based on Olson's database. * Computing of Easter Sunday dates for any given year, using Western, Orthodox or Julian algorithms; * A comprehensive test suite. Quick example ============= Here's a snapshot, just to give an idea about the power of the package. For more examples, look at the documentation. Suppose you want to know how much time is left, in years/months/days/etc, before the next easter happening on a year with a Friday 13th in August, and you want to get today's date out of the "date" unix system command. Here is the code: .. code-block:: python3 >>> from dateutil.relativedelta import * >>> from dateutil.easter import * >>> from dateutil.rrule import * >>> from dateutil.parser import * >>> from datetime import * >>> now = parse("Sat Oct 11 17:13:46 UTC 2003") >>> today = now.date() >>> year = rrule(YEARLY,dtstart=now,bymonth=8,bymonthday=13,byweekday=FR)[0].year >>> rdelta = relativedelta(easter(year), today) >>> print("Today is: %s" % today) Today is: 2003-10-11 >>> print("Year with next Aug 13th on a Friday is: %s" % year) Year with next Aug 13th on a Friday is: 2004 >>> print("How far is the Easter of that year: %s" % rdelta) How far is the Easter of that year: relativedelta(months=+6) >>> print("And the Easter of that year is: %s" % (today+rdelta)) And the Easter of that year is: 2004-04-11 Being exactly 6 months ahead was **really** a coincidence :) Contributing ============ We welcome many types of contributions - bug reports, pull requests (code, infrastructure or documentation fixes). For more information about how to contribute to the project, see the ``CONTRIBUTING.md`` file in the repository. Author ====== The dateutil module was written by Gustavo Niemeyer <gustavo@niemeyer.net> in 2003. It is maintained by: * Gustavo Niemeyer <gustavo@niemeyer.net> 2003-2011 * Tomi Pieviläinen <tomi.pievilainen@iki.fi> 2012-2014 * Yaron de Leeuw <me@jarondl.net> 2014-2016 * Paul Ganssle <paul@ganssle.io> 2015- Starting with version 2.4.1, all source and binary distributions will be signed by a PGP key that has, at the very least, been signed by the key which made the previous release. A table of release signing keys can be found below: =========== ============================ Releases Signing key fingerprint =========== ============================ 2.4.1- `6B49 ACBA DCF6 BD1C A206 67AB CD54 FCE3 D964 BEFB`_ (|pgp_mirror|_) =========== ============================ Contact ======= Our mailing list is available at `dateutil@python.org <https://mail.python.org/mailman/listinfo/dateutil>`_. As it is hosted by the PSF, it is subject to the `PSF code of conduct <https://www.python.org/psf/codeofconduct/>`_. License ======= All contributions after December 1, 2017 released under dual license - either `Apache 2.0 License <https://www.apache.org/licenses/LICENSE-2.0>`_ or the `BSD 3-Clause License <https://opensource.org/licenses/BSD-3-Clause>`_. Contributions before December 1, 2017 - except those those explicitly relicensed - are released only under the BSD 3-Clause License. .. _6B49 ACBA DCF6 BD1C A206 67AB CD54 FCE3 D964 BEFB: https://pgp.mit.edu/pks/lookup?op=vindex&search=0xCD54FCE3D964BEFB .. |pgp_mirror| replace:: mirror .. _pgp_mirror: https://sks-keyservers.net/pks/lookup?op=vindex&search=0xCD54FCE3D964BEFB	Gustavo Niemeyer	6	8	\N	/opt/rdepot/repositories/8/31719300/python-dateutil-2.8.1.tar.gz	\N	t	f	Python
45	requests	2.19.1	Requests: HTTP for Humans ========================= .. image:: https://img.shields.io/pypi/v/requests.svg :target: https://pypi.org/project/requests/ .. image:: https://img.shields.io/pypi/l/requests.svg :target: https://pypi.org/project/requests/ .. image:: https://img.shields.io/pypi/pyversions/requests.svg :target: https://pypi.org/project/requests/ .. image:: https://codecov.io/github/requests/requests/coverage.svg?branch=master :target: https://codecov.io/github/requests/requests :alt: codecov.io .. image:: https://img.shields.io/github/contributors/requests/requests.svg :target: https://github.com/requests/requests/graphs/contributors .. image:: https://img.shields.io/badge/Say%20Thanks-!-1EAEDB.svg :target: https://saythanks.io/to/kennethreitz Requests is the only *Non-GMO* HTTP library for Python, safe for human consumption. .. image:: https://farm5.staticflickr.com/4317/35198386374_1939af3de6_k_d.jpg Behold, the power of Requests: .. code-block:: python >>> r = requests.get('https://api.github.com/user', auth=('user', 'pass')) >>> r.status_code 200 >>> r.headers['content-type'] 'application/json; charset=utf8' >>> r.encoding 'utf-8' >>> r.text u'{"type":"User"...' >>> r.json() {u'disk_usage': 368627, u'private_gists': 484, ...} See `the similar code, sans Requests <https://gist.github.com/973705>`_. .. image:: https://raw.githubusercontent.com/requests/requests/master/docs/_static/requests-logo-small.png :target: http://docs.python-requests.org/ Requests allows you to send *organic, grass-fed* HTTP/1.1 requests, without the need for manual labor. There's no need to manually add query strings to your URLs, or to form-encode your POST data. Keep-alive and HTTP connection pooling are 100% automatic, thanks to `urllib3 <https://github.com/shazow/urllib3>`_. Besides, all the cool kids are doing it. Requests is one of the most downloaded Python packages of all time, pulling in over 11,000,000 downloads every month. You don't want to be left out! Feature Support --------------- Requests is ready for today's web. - International Domains and URLs - Keep-Alive & Connection Pooling - Sessions with Cookie Persistence - Browser-style SSL Verification - Basic/Digest Authentication - Elegant Key/Value Cookies - Automatic Decompression - Automatic Content Decoding - Unicode Response Bodies - Multipart File Uploads - HTTP(S) Proxy Support - Connection Timeouts - Streaming Downloads - ``.netrc`` Support - Chunked Requests Requests officially supports Python 2.7 & 3.4–3.6, and runs great on PyPy. Installation ------------ To install Requests, simply use `pipenv <http://pipenv.org/>`_ (or pip, of course): .. code-block:: bash $ pipenv install requests ✨🍰✨ Satisfaction guaranteed. Documentation ------------- Fantastic documentation is available at http://docs.python-requests.org/, for a limited time only. How to Contribute ----------------- #. Check for open issues or open a fresh issue to start a discussion around a feature idea or a bug. There is a `Contributor Friendly`_ tag for issues that should be ideal for people who are not very familiar with the codebase yet. #. Fork `the repository`_ on GitHub to start making your changes to the **master** branch (or branch off of it). #. Write a test which shows that the bug was fixed or that the feature works as expected. #. Send a pull request and bug the maintainer until it gets merged and published. :) Make sure to add yourself to AUTHORS_. .. _`the repository`: https://github.com/requests/requests .. _AUTHORS: https://github.com/requests/requests/blob/master/AUTHORS.rst .. _Contributor Friendly: https://github.com/requests/requests/issues?direction=desc&labels=Contributor+Friendly&page=1&sort=updated&state=open .. :changelog: Release History --------------- dev +++ - [Short description of non-trivial change.] 2.19.1 (2018-06-14) +++++++++++++++++++ **Bugfixes** - Fixed issue where status_codes.py's ``init`` function failed trying to append to a ``__doc__`` value of ``None``. 2.19.0 (2018-06-12) +++++++++++++++++++ **Improvements** - Warn user about possible slowdown when using cryptography version < 1.3.4 - Check for invalid host in proxy URL, before forwarding request to adapter. - Fragments are now properly maintained across redirects. (RFC7231 7.1.2) - Removed use of cgi module to expedite library load time. - Added support for SHA-256 and SHA-512 digest auth algorithms. - Minor performance improvement to ``Request.content``. - Migrate to using collections.abc for 3.7 compatibility. **Bugfixes** - Parsing empty ``Link`` headers with ``parse_header_links()`` no longer return one bogus entry. - Fixed issue where loading the default certificate bundle from a zip archive would raise an ``IOError``. - Fixed issue with unexpected ``ImportError`` on windows system which do not support ``winreg`` module. - DNS resolution in proxy bypass no longer includes the username and password in the request. This also fixes the issue of DNS queries failing on macOS. - Properly normalize adapter prefixes for url comparison. - Passing ``None`` as a file pointer to the ``files`` param no longer raises an exception. - Calling ``copy`` on a ``RequestsCookieJar`` will now preserve the cookie policy correctly. **Dependencies** - We now support idna v2.7. - We now support urllib3 v1.23. 2.18.4 (2017-08-15) +++++++++++++++++++ **Improvements** - Error messages for invalid headers now include the header name for easier debugging **Dependencies** - We now support idna v2.6. 2.18.3 (2017-08-02) +++++++++++++++++++ **Improvements** - Running ``$ python -m requests.help`` now includes the installed version of idna. **Bugfixes** - Fixed issue where Requests would raise ``ConnectionError`` instead of ``SSLError`` when encountering SSL problems when using urllib3 v1.22. 2.18.2 (2017-07-25) +++++++++++++++++++ **Bugfixes** - ``requests.help`` no longer fails on Python 2.6 due to the absence of ``ssl.OPENSSL_VERSION_NUMBER``. **Dependencies** - We now support urllib3 v1.22. 2.18.1 (2017-06-14) +++++++++++++++++++ **Bugfixes** - Fix an error in the packaging whereby the ``*.whl`` contained incorrect data that regressed the fix in v2.17.3. 2.18.0 (2017-06-14) +++++++++++++++++++ **Improvements** - ``Response`` is now a context manager, so can be used directly in a ``with`` statement without first having to be wrapped by ``contextlib.closing()``. **Bugfixes** - Resolve installation failure if multiprocessing is not available - Resolve tests crash if multiprocessing is not able to determine the number of CPU cores - Resolve error swallowing in utils set_environ generator 2.17.3 (2017-05-29) +++++++++++++++++++ **Improvements** - Improved ``packages`` namespace identity support, for monkeypatching libraries. 2.17.2 (2017-05-29) +++++++++++++++++++ **Improvements** - Improved ``packages`` namespace identity support, for monkeypatching libraries. 2.17.1 (2017-05-29) +++++++++++++++++++ **Improvements** - Improved ``packages`` namespace identity support, for monkeypatching libraries. 2.17.0 (2017-05-29) +++++++++++++++++++ **Improvements** - Removal of the 301 redirect cache. This improves thread-safety. 2.16.5 (2017-05-28) +++++++++++++++++++ - Improvements to ``$ python -m requests.help``. 2.16.4 (2017-05-27) +++++++++++++++++++ - Introduction of the ``$ python -m requests.help`` command, for debugging with maintainers! 2.16.3 (2017-05-27) +++++++++++++++++++ - Further restored the ``requests.packages`` namespace for compatibility reasons. 2.16.2 (2017-05-27) +++++++++++++++++++ - Further restored the ``requests.packages`` namespace for compatibility reasons. No code modification (noted below) should be necessary any longer. 2.16.1 (2017-05-27) +++++++++++++++++++ - Restored the ``requests.packages`` namespace for compatibility reasons. - Bugfix for ``urllib3`` version parsing. **Note**: code that was written to import against the ``requests.packages`` namespace previously will have to import code that rests at this module-level now. For example:: from requests.packages.urllib3.poolmanager import PoolManager Will need to be re-written to be:: from requests.packages import urllib3 urllib3.poolmanager.PoolManager Or, even better:: from urllib3.poolmanager import PoolManager 2.16.0 (2017-05-26) +++++++++++++++++++ - Unvendor ALL the things! 2.15.1 (2017-05-26) +++++++++++++++++++ - Everyone makes mistakes. 2.15.0 (2017-05-26) +++++++++++++++++++ **Improvements** - Introduction of the ``Response.next`` property, for getting the next ``PreparedResponse`` from a redirect chain (when ``allow_redirects=False``). - Internal refactoring of ``__version__`` module. **Bugfixes** - Restored once-optional parameter for ``requests.utils.get_environ_proxies()``. 2.14.2 (2017-05-10) +++++++++++++++++++ **Bugfixes** - Changed a less-than to an equal-to and an or in the dependency markers to widen compatibility with older setuptools releases. 2.14.1 (2017-05-09) +++++++++++++++++++ **Bugfixes** - Changed the dependency markers to widen compatibility with older pip releases. 2.14.0 (2017-05-09) +++++++++++++++++++ **Improvements** - It is now possible to pass ``no_proxy`` as a key to the ``proxies`` dictionary to provide handling similar to the ``NO_PROXY`` environment variable. - When users provide invalid paths to certificate bundle files or directories Requests now raises ``IOError``, rather than failing at the time of the HTTPS request with a fairly inscrutable certificate validation error. - The behavior of ``SessionRedirectMixin`` was slightly altered. ``resolve_redirects`` will now detect a redirect by calling ``get_redirect_target(response)`` instead of directly querying ``Response.is_redirect`` and ``Response.headers['location']``. Advanced users will be able to process malformed redirects more easily. - Changed the internal calculation of elapsed request time to have higher resolution on Windows. - Added ``win_inet_pton`` as conditional dependency for the ``[socks]`` extra on Windows with Python 2.7. - Changed the proxy bypass implementation on Windows: the proxy bypass check doesn't use forward and reverse DNS requests anymore - URLs with schemes that begin with ``http`` but are not ``http`` or ``https`` no longer have their host parts forced to lowercase. **Bugfixes** - Much improved handling of non-ASCII ``Location`` header values in redirects. Fewer ``UnicodeDecodeErrors`` are encountered on Python 2, and Python 3 now correctly understands that Latin-1 is unlikely to be the correct encoding. - If an attempt to ``seek`` file to find out its length fails, we now appropriately handle that by aborting our content-length calculations. - Restricted ``HTTPDigestAuth`` to only respond to auth challenges made on 4XX responses, rather than to all auth challenges. - Fixed some code that was firing ``DeprecationWarning`` on Python 3.6. - The dismayed person emoticon (``/o\\\\``) no longer has a big head. I'm sure this is what you were all worrying about most. **Miscellaneous** - Updated bundled urllib3 to v1.21.1. - Updated bundled chardet to v3.0.2. - Updated bundled idna to v2.5. - Updated bundled certifi to 2017.4.17. 2.13.0 (2017-01-24) +++++++++++++++++++ **Features** - Only load the ``idna`` library when we've determined we need it. This will save some memory for users. **Miscellaneous** - Updated bundled urllib3 to 1.20. - Updated bundled idna to 2.2. 2.12.5 (2017-01-18) +++++++++++++++++++ **Bugfixes** - Fixed an issue with JSON encoding detection, specifically detecting big-endian UTF-32 with BOM. 2.12.4 (2016-12-14) +++++++++++++++++++ **Bugfixes** - Fixed regression from 2.12.2 where non-string types were rejected in the basic auth parameters. While support for this behaviour has been readded, the behaviour is deprecated and will be removed in the future. 2.12.3 (2016-12-01) +++++++++++++++++++ **Bugfixes** - Fixed regression from v2.12.1 for URLs with schemes that begin with "http". These URLs have historically been processed as though they were HTTP-schemed URLs, and so have had parameters added. This was removed in v2.12.2 in an overzealous attempt to resolve problems with IDNA-encoding those URLs. This change was reverted: the other fixes for IDNA-encoding have been judged to be sufficient to return to the behaviour Requests had before v2.12.0. 2.12.2 (2016-11-30) +++++++++++++++++++ **Bugfixes** - Fixed several issues with IDNA-encoding URLs that are technically invalid but which are widely accepted. Requests will now attempt to IDNA-encode a URL if it can but, if it fails, and the host contains only ASCII characters, it will be passed through optimistically. This will allow users to opt-in to using IDNA2003 themselves if they want to, and will also allow technically invalid but still common hostnames. - Fixed an issue where URLs with leading whitespace would raise ``InvalidSchema`` errors. - Fixed an issue where some URLs without the HTTP or HTTPS schemes would still have HTTP URL preparation applied to them. - Fixed an issue where Unicode strings could not be used in basic auth. - Fixed an issue encountered by some Requests plugins where constructing a Response object would cause ``Response.content`` to raise an ``AttributeError``. 2.12.1 (2016-11-16) +++++++++++++++++++ **Bugfixes** - Updated setuptools 'security' extra for the new PyOpenSSL backend in urllib3. **Miscellaneous** - Updated bundled urllib3 to 1.19.1. 2.12.0 (2016-11-15) +++++++++++++++++++ **Improvements** - Updated support for internationalized domain names from IDNA2003 to IDNA2008. This updated support is required for several forms of IDNs and is mandatory for .de domains. - Much improved heuristics for guessing content lengths: Requests will no longer read an entire ``StringIO`` into memory. - Much improved logic for recalculating ``Content-Length`` headers for ``PreparedRequest`` objects. - Improved tolerance for file-like objects that have no ``tell`` method but do have a ``seek`` method. - Anything that is a subclass of ``Mapping`` is now treated like a dictionary by the ``data=`` keyword argument. - Requests now tolerates empty passwords in proxy credentials, rather than stripping the credentials. - If a request is made with a file-like object as the body and that request is redirected with a 307 or 308 status code, Requests will now attempt to rewind the body object so it can be replayed. **Bugfixes** - When calling ``response.close``, the call to ``close`` will be propagated through to non-urllib3 backends. - Fixed issue where the ``ALL_PROXY`` environment variable would be preferred over scheme-specific variables like ``HTTP_PROXY``. - Fixed issue where non-UTF8 reason phrases got severely mangled by falling back to decoding using ISO 8859-1 instead. - Fixed a bug where Requests would not correctly correlate cookies set when using custom Host headers if those Host headers did not use the native string type for the platform. **Miscellaneous** - Updated bundled urllib3 to 1.19. - Updated bundled certifi certs to 2016.09.26. 2.11.1 (2016-08-17) +++++++++++++++++++ **Bugfixes** - Fixed a bug when using ``iter_content`` with ``decode_unicode=True`` for streamed bodies would raise ``AttributeError``. This bug was introduced in 2.11. - Strip Content-Type and Transfer-Encoding headers from the header block when following a redirect that transforms the verb from POST/PUT to GET. 2.11.0 (2016-08-08) +++++++++++++++++++ **Improvements** - Added support for the ``ALL_PROXY`` environment variable. - Reject header values that contain leading whitespace or newline characters to reduce risk of header smuggling. **Bugfixes** - Fixed occasional ``TypeError`` when attempting to decode a JSON response that occurred in an error case. Now correctly returns a ``ValueError``. - Requests would incorrectly ignore a non-CIDR IP address in the ``NO_PROXY`` environment variables: Requests now treats it as a specific IP. - Fixed a bug when sending JSON data that could cause us to encounter obscure OpenSSL errors in certain network conditions (yes, really). - Added type checks to ensure that ``iter_content`` only accepts integers and ``None`` for chunk sizes. - Fixed issue where responses whose body had not been fully consumed would have the underlying connection closed but not returned to the connection pool, which could cause Requests to hang in situations where the ``HTTPAdapter`` had been configured to use a blocking connection pool. **Miscellaneous** - Updated bundled urllib3 to 1.16. - Some previous releases accidentally accepted non-strings as acceptable header values. This release does not. 2.10.0 (2016-04-29) +++++++++++++++++++ **New Features** - SOCKS Proxy Support! (requires PySocks; ``$ pip install requests[socks]``) **Miscellaneous** - Updated bundled urllib3 to 1.15.1. 2.9.2 (2016-04-29) ++++++++++++++++++ **Improvements** - Change built-in CaseInsensitiveDict (used for headers) to use OrderedDict as its underlying datastore. **Bugfixes** - Don't use redirect_cache if allow_redirects=False - When passed objects that throw exceptions from ``tell()``, send them via chunked transfer encoding instead of failing. - Raise a ProxyError for proxy related connection issues. 2.9.1 (2015-12-21) ++++++++++++++++++ **Bugfixes** - Resolve regression introduced in 2.9.0 that made it impossible to send binary strings as bodies in Python 3. - Fixed errors when calculating cookie expiration dates in certain locales. **Miscellaneous** - Updated bundled urllib3 to 1.13.1. 2.9.0 (2015-12-15) ++++++++++++++++++ **Minor Improvements** (Backwards compatible) - The ``verify`` keyword argument now supports being passed a path to a directory of CA certificates, not just a single-file bundle. - Warnings are now emitted when sending files opened in text mode. - Added the 511 Network Authentication Required status code to the status code registry. **Bugfixes** - For file-like objects that are not seeked to the very beginning, we now send the content length for the number of bytes we will actually read, rather than the total size of the file, allowing partial file uploads. - When uploading file-like objects, if they are empty or have no obvious content length we set ``Transfer-Encoding: chunked`` rather than ``Content-Length: 0``. - We correctly receive the response in buffered mode when uploading chunked bodies. - We now handle being passed a query string as a bytestring on Python 3, by decoding it as UTF-8. - Sessions are now closed in all cases (exceptional and not) when using the functional API rather than leaking and waiting for the garbage collector to clean them up. - Correctly handle digest auth headers with a malformed ``qop`` directive that contains no token, by treating it the same as if no ``qop`` directive was provided at all. - Minor performance improvements when removing specific cookies by name. **Miscellaneous** - Updated urllib3 to 1.13. 2.8.1 (2015-10-13) ++++++++++++++++++ **Bugfixes** - Update certificate bundle to match ``certifi`` 2015.9.6.2's weak certificate bundle. - Fix a bug in 2.8.0 where requests would raise ``ConnectTimeout`` instead of ``ConnectionError`` - When using the PreparedRequest flow, requests will now correctly respect the ``json`` parameter. Broken in 2.8.0. - When using the PreparedRequest flow, requests will now correctly handle a Unicode-string method name on Python 2. Broken in 2.8.0. 2.8.0 (2015-10-05) ++++++++++++++++++ **Minor Improvements** (Backwards Compatible) - Requests now supports per-host proxies. This allows the ``proxies`` dictionary to have entries of the form ``{'<scheme>://<hostname>': '<proxy>'}``. Host-specific proxies will be used in preference to the previously-supported scheme-specific ones, but the previous syntax will continue to work. - ``Response.raise_for_status`` now prints the URL that failed as part of the exception message. - ``requests.utils.get_netrc_auth`` now takes an ``raise_errors`` kwarg, defaulting to ``False``. When ``True``, errors parsing ``.netrc`` files cause exceptions to be thrown. - Change to bundled projects import logic to make it easier to unbundle requests downstream. - Changed the default User-Agent string to avoid leaking data on Linux: now contains only the requests version. **Bugfixes** - The ``json`` parameter to ``post()`` and friends will now only be used if neither ``data`` nor ``files`` are present, consistent with the documentation. - We now ignore empty fields in the ``NO_PROXY`` environment variable. - Fixed problem where ``httplib.BadStatusLine`` would get raised if combining ``stream=True`` with ``contextlib.closing``. - Prevented bugs where we would attempt to return the same connection back to the connection pool twice when sending a Chunked body. - Miscellaneous minor internal changes. - Digest Auth support is now thread safe. **Updates** - Updated urllib3 to 1.12. 2.7.0 (2015-05-03) ++++++++++++++++++ This is the first release that follows our new release process. For more, see `our documentation <http://docs.python-requests.org/en/latest/community/release-process/>`_. **Bugfixes** - Updated urllib3 to 1.10.4, resolving several bugs involving chunked transfer encoding and response framing. 2.6.2 (2015-04-23) ++++++++++++++++++ **Bugfixes** - Fix regression where compressed data that was sent as chunked data was not properly decompressed. (#2561) 2.6.1 (2015-04-22) ++++++++++++++++++ **Bugfixes** - Remove VendorAlias import machinery introduced in v2.5.2. - Simplify the PreparedRequest.prepare API: We no longer require the user to pass an empty list to the hooks keyword argument. (c.f. #2552) - Resolve redirects now receives and forwards all of the original arguments to the adapter. (#2503) - Handle UnicodeDecodeErrors when trying to deal with a unicode URL that cannot be encoded in ASCII. (#2540) - Populate the parsed path of the URI field when performing Digest Authentication. (#2426) - Copy a PreparedRequest's CookieJar more reliably when it is not an instance of RequestsCookieJar. (#2527) 2.6.0 (2015-03-14) ++++++++++++++++++ **Bugfixes** - CVE-2015-2296: Fix handling of cookies on redirect. Previously a cookie without a host value set would use the hostname for the redirected URL exposing requests users to session fixation attacks and potentially cookie stealing. This was disclosed privately by Matthew Daley of `BugFuzz <https://bugfuzz.com>`_. This affects all versions of requests from v2.1.0 to v2.5.3 (inclusive on both ends). - Fix error when requests is an ``install_requires`` dependency and ``python setup.py test`` is run. (#2462) - Fix error when urllib3 is unbundled and requests continues to use the vendored import location. - Include fixes to ``urllib3``'s header handling. - Requests' handling of unvendored dependencies is now more restrictive. **Features and Improvements** - Support bytearrays when passed as parameters in the ``files`` argument. (#2468) - Avoid data duplication when creating a request with ``str``, ``bytes``, or ``bytearray`` input to the ``files`` argument. 2.5.3 (2015-02-24) ++++++++++++++++++ **Bugfixes** - Revert changes to our vendored certificate bundle. For more context see (#2455, #2456, and http://bugs.python.org/issue23476) 2.5.2 (2015-02-23) ++++++++++++++++++ **Features and Improvements** - Add sha256 fingerprint support. (`shazow/urllib3#540`_) - Improve the performance of headers. (`shazow/urllib3#544`_) **Bugfixes** - Copy pip's import machinery. When downstream redistributors remove requests.packages.urllib3 the import machinery will continue to let those same symbols work. Example usage in requests' documentation and 3rd-party libraries relying on the vendored copies of urllib3 will work without having to fallback to the system urllib3. - Attempt to quote parts of the URL on redirect if unquoting and then quoting fails. (#2356) - Fix filename type check for multipart form-data uploads. (#2411) - Properly handle the case where a server issuing digest authentication challenges provides both auth and auth-int qop-values. (#2408) - Fix a socket leak. (`shazow/urllib3#549`_) - Fix multiple ``Set-Cookie`` headers properly. (`shazow/urllib3#534`_) - Disable the built-in hostname verification. (`shazow/urllib3#526`_) - Fix the behaviour of decoding an exhausted stream. (`shazow/urllib3#535`_) **Security** - Pulled in an updated ``cacert.pem``. - Drop RC4 from the default cipher list. (`shazow/urllib3#551`_) .. _shazow/urllib3#551: https://github.com/shazow/urllib3/pull/551 .. _shazow/urllib3#549: https://github.com/shazow/urllib3/pull/549 .. _shazow/urllib3#544: https://github.com/shazow/urllib3/pull/544 .. _shazow/urllib3#540: https://github.com/shazow/urllib3/pull/540 .. _shazow/urllib3#535: https://github.com/shazow/urllib3/pull/535 .. _shazow/urllib3#534: https://github.com/shazow/urllib3/pull/534 .. _shazow/urllib3#526: https://github.com/shazow/urllib3/pull/526 2.5.1 (2014-12-23) ++++++++++++++++++ **Behavioural Changes** - Only catch HTTPErrors in raise_for_status (#2382) **Bugfixes** - Handle LocationParseError from urllib3 (#2344) - Handle file-like object filenames that are not strings (#2379) - Unbreak HTTPDigestAuth handler. Allow new nonces to be negotiated (#2389) 2.5.0 (2014-12-01) ++++++++++++++++++ **Improvements** - Allow usage of urllib3's Retry object with HTTPAdapters (#2216) - The ``iter_lines`` method on a response now accepts a delimiter with which to split the content (#2295) **Behavioural Changes** - Add deprecation warnings to functions in requests.utils that will be removed in 3.0 (#2309) - Sessions used by the functional API are always closed (#2326) - Restrict requests to HTTP/1.1 and HTTP/1.0 (stop accepting HTTP/0.9) (#2323) **Bugfixes** - Only parse the URL once (#2353) - Allow Content-Length header to always be overridden (#2332) - Properly handle files in HTTPDigestAuth (#2333) - Cap redirect_cache size to prevent memory abuse (#2299) - Fix HTTPDigestAuth handling of redirects after authenticating successfully (#2253) - Fix crash with custom method parameter to Session.request (#2317) - Fix how Link headers are parsed using the regular expression library (#2271) **Documentation** - Add more references for interlinking (#2348) - Update CSS for theme (#2290) - Update width of buttons and sidebar (#2289) - Replace references of Gittip with Gratipay (#2282) - Add link to changelog in sidebar (#2273) 2.4.3 (2014-10-06) ++++++++++++++++++ **Bugfixes** - Unicode URL improvements for Python 2. - Re-order JSON param for backwards compat. - Automatically defrag authentication schemes from host/pass URIs. (`#2249 <https://github.com/requests/requests/issues/2249>`_) 2.4.2 (2014-10-05) ++++++++++++++++++ **Improvements** - FINALLY! Add json parameter for uploads! (`#2258 <https://github.com/requests/requests/pull/2258>`_) - Support for bytestring URLs on Python 3.x (`#2238 <https://github.com/requests/requests/pull/2238>`_) **Bugfixes** - Avoid getting stuck in a loop (`#2244 <https://github.com/requests/requests/pull/2244>`_) - Multiple calls to iter* fail with unhelpful error. (`#2240 <https://github.com/requests/requests/issues/2240>`_, `#2241 <https://github.com/requests/requests/issues/2241>`_) **Documentation** - Correct redirection introduction (`#2245 <https://github.com/requests/requests/pull/2245/>`_) - Added example of how to send multiple files in one request. (`#2227 <https://github.com/requests/requests/pull/2227/>`_) - Clarify how to pass a custom set of CAs (`#2248 <https://github.com/requests/requests/pull/2248/>`_) 2.4.1 (2014-09-09) ++++++++++++++++++ - Now has a "security" package extras set, ``$ pip install requests[security]`` - Requests will now use Certifi if it is available. - Capture and re-raise urllib3 ProtocolError - Bugfix for responses that attempt to redirect to themselves forever (wtf?). 2.4.0 (2014-08-29) ++++++++++++++++++ **Behavioral Changes** - ``Connection: keep-alive`` header is now sent automatically. **Improvements** - Support for connect timeouts! Timeout now accepts a tuple (connect, read) which is used to set individual connect and read timeouts. - Allow copying of PreparedRequests without headers/cookies. - Updated bundled urllib3 version. - Refactored settings loading from environment -- new `Session.merge_environment_settings`. - Handle socket errors in iter_content. 2.3.0 (2014-05-16) ++++++++++++++++++ **API Changes** - New ``Response`` property ``is_redirect``, which is true when the library could have processed this response as a redirection (whether or not it actually did). - The ``timeout`` parameter now affects requests with both ``stream=True`` and ``stream=False`` equally. - The change in v2.0.0 to mandate explicit proxy schemes has been reverted. Proxy schemes now default to ``http://``. - The ``CaseInsensitiveDict`` used for HTTP headers now behaves like a normal dictionary when references as string or viewed in the interpreter. **Bugfixes** - No longer expose Authorization or Proxy-Authorization headers on redirect. Fix CVE-2014-1829 and CVE-2014-1830 respectively. - Authorization is re-evaluated each redirect. - On redirect, pass url as native strings. - Fall-back to autodetected encoding for JSON when Unicode detection fails. - Headers set to ``None`` on the ``Session`` are now correctly not sent. - Correctly honor ``decode_unicode`` even if it wasn't used earlier in the same response. - Stop advertising ``compress`` as a supported Content-Encoding. - The ``Response.history`` parameter is now always a list. - Many, many ``urllib3`` bugfixes. 2.2.1 (2014-01-23) ++++++++++++++++++ **Bugfixes** - Fixes incorrect parsing of proxy credentials that contain a literal or encoded '#' character. - Assorted urllib3 fixes. 2.2.0 (2014-01-09) ++++++++++++++++++ **API Changes** - New exception: ``ContentDecodingError``. Raised instead of ``urllib3`` ``DecodeError`` exceptions. **Bugfixes** - Avoid many many exceptions from the buggy implementation of ``proxy_bypass`` on OS X in Python 2.6. - Avoid crashing when attempting to get authentication credentials from ~/.netrc when running as a user without a home directory. - Use the correct pool size for pools of connections to proxies. - Fix iteration of ``CookieJar`` objects. - Ensure that cookies are persisted over redirect. - Switch back to using chardet, since it has merged with charade. 2.1.0 (2013-12-05) ++++++++++++++++++ - Updated CA Bundle, of course. - Cookies set on individual Requests through a ``Session`` (e.g. via ``Session.get()``) are no longer persisted to the ``Session``. - Clean up connections when we hit problems during chunked upload, rather than leaking them. - Return connections to the pool when a chunked upload is successful, rather than leaking it. - Match the HTTPbis recommendation for HTTP 301 redirects. - Prevent hanging when using streaming uploads and Digest Auth when a 401 is received. - Values of headers set by Requests are now always the native string type. - Fix previously broken SNI support. - Fix accessing HTTP proxies using proxy authentication. - Unencode HTTP Basic usernames and passwords extracted from URLs. - Support for IP address ranges for no_proxy environment variable - Parse headers correctly when users override the default ``Host:`` header. - Avoid munging the URL in case of case-sensitive servers. - Looser URL handling for non-HTTP/HTTPS urls. - Accept unicode methods in Python 2.6 and 2.7. - More resilient cookie handling. - Make ``Response`` objects pickleable. - Actually added MD5-sess to Digest Auth instead of pretending to like last time. - Updated internal urllib3. - Fixed @Lukasa's lack of taste. 2.0.1 (2013-10-24) ++++++++++++++++++ - Updated included CA Bundle with new mistrusts and automated process for the future - Added MD5-sess to Digest Auth - Accept per-file headers in multipart file POST messages. - Fixed: Don't send the full URL on CONNECT messages. - Fixed: Correctly lowercase a redirect scheme. - Fixed: Cookies not persisted when set via functional API. - Fixed: Translate urllib3 ProxyError into a requests ProxyError derived from ConnectionError. - Updated internal urllib3 and chardet. 2.0.0 (2013-09-24) ++++++++++++++++++ **API Changes:** - Keys in the Headers dictionary are now native strings on all Python versions, i.e. bytestrings on Python 2, unicode on Python 3. - Proxy URLs now *must* have an explicit scheme. A ``MissingSchema`` exception will be raised if they don't. - Timeouts now apply to read time if ``Stream=False``. - ``RequestException`` is now a subclass of ``IOError``, not ``RuntimeError``. - Added new method to ``PreparedRequest`` objects: ``PreparedRequest.copy()``. - Added new method to ``Session`` objects: ``Session.update_request()``. This method updates a ``Request`` object with the data (e.g. cookies) stored on the ``Session``. - Added new method to ``Session`` objects: ``Session.prepare_request()``. This method updates and prepares a ``Request`` object, and returns the corresponding ``PreparedRequest`` object. - Added new method to ``HTTPAdapter`` objects: ``HTTPAdapter.proxy_headers()``. This should not be called directly, but improves the subclass interface. - ``httplib.IncompleteRead`` exceptions caused by incorrect chunked encoding will now raise a Requests ``ChunkedEncodingError`` instead. - Invalid percent-escape sequences now cause a Requests ``InvalidURL`` exception to be raised. - HTTP 208 no longer uses reason phrase ``"im_used"``. Correctly uses ``"already_reported"``. - HTTP 226 reason added (``"im_used"``). **Bugfixes:** - Vastly improved proxy support, including the CONNECT verb. Special thanks to the many contributors who worked towards this improvement. - Cookies are now properly managed when 401 authentication responses are received. - Chunked encoding fixes. - Support for mixed case schemes. - Better handling of streaming downloads. - Retrieve environment proxies from more locations. - Minor cookies fixes. - Improved redirect behaviour. - Improved streaming behaviour, particularly for compressed data. - Miscellaneous small Python 3 text encoding bugs. - ``.netrc`` no longer overrides explicit auth. - Cookies set by hooks are now correctly persisted on Sessions. - Fix problem with cookies that specify port numbers in their host field. - ``BytesIO`` can be used to perform streaming uploads. - More generous parsing of the ``no_proxy`` environment variable. - Non-string objects can be passed in data values alongside files. 1.2.3 (2013-05-25) ++++++++++++++++++ - Simple packaging fix 1.2.2 (2013-05-23) ++++++++++++++++++ - Simple packaging fix 1.2.1 (2013-05-20) ++++++++++++++++++ - 301 and 302 redirects now change the verb to GET for all verbs, not just POST, improving browser compatibility. - Python 3.3.2 compatibility - Always percent-encode location headers - Fix connection adapter matching to be most-specific first - new argument to the default connection adapter for passing a block argument - prevent a KeyError when there's no link headers 1.2.0 (2013-03-31) ++++++++++++++++++ - Fixed cookies on sessions and on requests - Significantly change how hooks are dispatched - hooks now receive all the arguments specified by the user when making a request so hooks can make a secondary request with the same parameters. This is especially necessary for authentication handler authors - certifi support was removed - Fixed bug where using OAuth 1 with body ``signature_type`` sent no data - Major proxy work thanks to @Lukasa including parsing of proxy authentication from the proxy url - Fix DigestAuth handling too many 401s - Update vendored urllib3 to include SSL bug fixes - Allow keyword arguments to be passed to ``json.loads()`` via the ``Response.json()`` method - Don't send ``Content-Length`` header by default on ``GET`` or ``HEAD`` requests - Add ``elapsed`` attribute to ``Response`` objects to time how long a request took. - Fix ``RequestsCookieJar`` - Sessions and Adapters are now picklable, i.e., can be used with the multiprocessing library - Update charade to version 1.0.3 The change in how hooks are dispatched will likely cause a great deal of issues. 1.1.0 (2013-01-10) ++++++++++++++++++ - CHUNKED REQUESTS - Support for iterable response bodies - Assume servers persist redirect params - Allow explicit content types to be specified for file data - Make merge_kwargs case-insensitive when looking up keys 1.0.3 (2012-12-18) ++++++++++++++++++ - Fix file upload encoding bug - Fix cookie behavior 1.0.2 (2012-12-17) ++++++++++++++++++ - Proxy fix for HTTPAdapter. 1.0.1 (2012-12-17) ++++++++++++++++++ - Cert verification exception bug. - Proxy fix for HTTPAdapter. 1.0.0 (2012-12-17) ++++++++++++++++++ - Massive Refactor and Simplification - Switch to Apache 2.0 license - Swappable Connection Adapters - Mountable Connection Adapters - Mutable ProcessedRequest chain - /s/prefetch/stream - Removal of all configuration - Standard library logging - Make Response.json() callable, not property. - Usage of new charade project, which provides python 2 and 3 simultaneous chardet. - Removal of all hooks except 'response' - Removal of all authentication helpers (OAuth, Kerberos) This is not a backwards compatible change. 0.14.2 (2012-10-27) +++++++++++++++++++ - Improved mime-compatible JSON handling - Proxy fixes - Path hack fixes - Case-Insensitive Content-Encoding headers - Support for CJK parameters in form posts 0.14.1 (2012-10-01) +++++++++++++++++++ - Python 3.3 Compatibility - Simply default accept-encoding - Bugfixes 0.14.0 (2012-09-02) ++++++++++++++++++++ - No more iter_content errors if already downloaded. 0.13.9 (2012-08-25) +++++++++++++++++++ - Fix for OAuth + POSTs - Remove exception eating from dispatch_hook - General bugfixes 0.13.8 (2012-08-21) +++++++++++++++++++ - Incredible Link header support :) 0.13.7 (2012-08-19) +++++++++++++++++++ - Support for (key, value) lists everywhere. - Digest Authentication improvements. - Ensure proxy exclusions work properly. - Clearer UnicodeError exceptions. - Automatic casting of URLs to strings (fURL and such) - Bugfixes. 0.13.6 (2012-08-06) +++++++++++++++++++ - Long awaited fix for hanging connections! 0.13.5 (2012-07-27) +++++++++++++++++++ - Packaging fix 0.13.4 (2012-07-27) +++++++++++++++++++ - GSSAPI/Kerberos authentication! - App Engine 2.7 Fixes! - Fix leaking connections (from urllib3 update) - OAuthlib path hack fix - OAuthlib URL parameters fix. 0.13.3 (2012-07-12) +++++++++++++++++++ - Use simplejson if available. - Do not hide SSLErrors behind Timeouts. - Fixed param handling with urls containing fragments. - Significantly improved information in User Agent. - client certificates are ignored when verify=False 0.13.2 (2012-06-28) +++++++++++++++++++ - Zero dependencies (once again)! - New: Response.reason - Sign querystring parameters in OAuth 1.0 - Client certificates no longer ignored when verify=False - Add openSUSE certificate support 0.13.1 (2012-06-07) +++++++++++++++++++ - Allow passing a file or file-like object as data. - Allow hooks to return responses that indicate errors. - Fix Response.text and Response.json for body-less responses. 0.13.0 (2012-05-29) +++++++++++++++++++ - Removal of Requests.async in favor of `grequests <https://github.com/kennethreitz/grequests>`_ - Allow disabling of cookie persistence. - New implementation of safe_mode - cookies.get now supports default argument - Session cookies not saved when Session.request is called with return_response=False - Env: no_proxy support. - RequestsCookieJar improvements. - Various bug fixes. 0.12.1 (2012-05-08) +++++++++++++++++++ - New ``Response.json`` property. - Ability to add string file uploads. - Fix out-of-range issue with iter_lines. - Fix iter_content default size. - Fix POST redirects containing files. 0.12.0 (2012-05-02) +++++++++++++++++++ - EXPERIMENTAL OAUTH SUPPORT! - Proper CookieJar-backed cookies interface with awesome dict-like interface. - Speed fix for non-iterated content chunks. - Move ``pre_request`` to a more usable place. - New ``pre_send`` hook. - Lazily encode data, params, files. - Load system Certificate Bundle if ``certify`` isn't available. - Cleanups, fixes. 0.11.2 (2012-04-22) +++++++++++++++++++ - Attempt to use the OS's certificate bundle if ``certifi`` isn't available. - Infinite digest auth redirect fix. - Multi-part file upload improvements. - Fix decoding of invalid %encodings in URLs. - If there is no content in a response don't throw an error the second time that content is attempted to be read. - Upload data on redirects. 0.11.1 (2012-03-30) +++++++++++++++++++ * POST redirects now break RFC to do what browsers do: Follow up with a GET. * New ``strict_mode`` configuration to disable new redirect behavior. 0.11.0 (2012-03-14) +++++++++++++++++++ * Private SSL Certificate support * Remove select.poll from Gevent monkeypatching * Remove redundant generator for chunked transfer encoding * Fix: Response.ok raises Timeout Exception in safe_mode 0.10.8 (2012-03-09) +++++++++++++++++++ * Generate chunked ValueError fix * Proxy configuration by environment variables * Simplification of iter_lines. * New `trust_env` configuration for disabling system/environment hints. * Suppress cookie errors. 0.10.7 (2012-03-07) +++++++++++++++++++ * `encode_uri` = False 0.10.6 (2012-02-25) +++++++++++++++++++ * Allow '=' in cookies. 0.10.5 (2012-02-25) +++++++++++++++++++ * Response body with 0 content-length fix. * New async.imap. * Don't fail on netrc. 0.10.4 (2012-02-20) +++++++++++++++++++ * Honor netrc. 0.10.3 (2012-02-20) +++++++++++++++++++ * HEAD requests don't follow redirects anymore. * raise_for_status() doesn't raise for 3xx anymore. * Make Session objects picklable. * ValueError for invalid schema URLs. 0.10.2 (2012-01-15) +++++++++++++++++++ * Vastly improved URL quoting. * Additional allowed cookie key values. * Attempted fix for "Too many open files" Error * Replace unicode errors on first pass, no need for second pass. * Append '/' to bare-domain urls before query insertion. * Exceptions now inherit from RuntimeError. * Binary uploads + auth fix. * Bugfixes. 0.10.1 (2012-01-23) +++++++++++++++++++ * PYTHON 3 SUPPORT! * Dropped 2.5 Support. (*Backwards Incompatible*) 0.10.0 (2012-01-21) +++++++++++++++++++ * ``Response.content`` is now bytes-only. (*Backwards Incompatible*) * New ``Response.text`` is unicode-only. * If no ``Response.encoding`` is specified and ``chardet`` is available, ``Response.text`` will guess an encoding. * Default to ISO-8859-1 (Western) encoding for "text" subtypes. * Removal of `decode_unicode`. (*Backwards Incompatible*) * New multiple-hooks system. * New ``Response.register_hook`` for registering hooks within the pipeline. * ``Response.url`` is now Unicode. 0.9.3 (2012-01-18) ++++++++++++++++++ * SSL verify=False bugfix (apparent on windows machines). 0.9.2 (2012-01-18) ++++++++++++++++++ * Asynchronous async.send method. * Support for proper chunk streams with boundaries. * session argument for Session classes. * Print entire hook tracebacks, not just exception instance. * Fix response.iter_lines from pending next line. * Fix but in HTTP-digest auth w/ URI having query strings. * Fix in Event Hooks section. * Urllib3 update. 0.9.1 (2012-01-06) ++++++++++++++++++ * danger_mode for automatic Response.raise_for_status() * Response.iter_lines refactor 0.9.0 (2011-12-28) ++++++++++++++++++ * verify ssl is default. 0.8.9 (2011-12-28) ++++++++++++++++++ * Packaging fix. 0.8.8 (2011-12-28) ++++++++++++++++++ * SSL CERT VERIFICATION! * Release of Cerifi: Mozilla's cert list. * New 'verify' argument for SSL requests. * Urllib3 update. 0.8.7 (2011-12-24) ++++++++++++++++++ * iter_lines last-line truncation fix * Force safe_mode for async requests * Handle safe_mode exceptions more consistently * Fix iteration on null responses in safe_mode 0.8.6 (2011-12-18) ++++++++++++++++++ * Socket timeout fixes. * Proxy Authorization support. 0.8.5 (2011-12-14) ++++++++++++++++++ * Response.iter_lines! 0.8.4 (2011-12-11) ++++++++++++++++++ * Prefetch bugfix. * Added license to installed version. 0.8.3 (2011-11-27) ++++++++++++++++++ * Converted auth system to use simpler callable objects. * New session parameter to API methods. * Display full URL while logging. 0.8.2 (2011-11-19) ++++++++++++++++++ * New Unicode decoding system, based on over-ridable `Response.encoding`. * Proper URL slash-quote handling. * Cookies with ``[``, ``]``, and ``_`` allowed. 0.8.1 (2011-11-15) ++++++++++++++++++ * URL Request path fix * Proxy fix. * Timeouts fix. 0.8.0 (2011-11-13) ++++++++++++++++++ * Keep-alive support! * Complete removal of Urllib2 * Complete removal of Poster * Complete removal of CookieJars * New ConnectionError raising * Safe_mode for error catching * prefetch parameter for request methods * OPTION method * Async pool size throttling * File uploads send real names * Vendored in urllib3 0.7.6 (2011-11-07) ++++++++++++++++++ * Digest authentication bugfix (attach query data to path) 0.7.5 (2011-11-04) ++++++++++++++++++ * Response.content = None if there was an invalid response. * Redirection auth handling. 0.7.4 (2011-10-26) ++++++++++++++++++ * Session Hooks fix. 0.7.3 (2011-10-23) ++++++++++++++++++ * Digest Auth fix. 0.7.2 (2011-10-23) ++++++++++++++++++ * PATCH Fix. 0.7.1 (2011-10-23) ++++++++++++++++++ * Move away from urllib2 authentication handling. * Fully Remove AuthManager, AuthObject, &c. * New tuple-based auth system with handler callbacks. 0.7.0 (2011-10-22) ++++++++++++++++++ * Sessions are now the primary interface. * Deprecated InvalidMethodException. * PATCH fix. * New config system (no more global settings). 0.6.6 (2011-10-19) ++++++++++++++++++ * Session parameter bugfix (params merging). 0.6.5 (2011-10-18) ++++++++++++++++++ * Offline (fast) test suite. * Session dictionary argument merging. 0.6.4 (2011-10-13) ++++++++++++++++++ * Automatic decoding of unicode, based on HTTP Headers. * New ``decode_unicode`` setting. * Removal of ``r.read/close`` methods. * New ``r.faw`` interface for advanced response usage.* * Automatic expansion of parameterized headers. 0.6.3 (2011-10-13) ++++++++++++++++++ * Beautiful ``requests.async`` module, for making async requests w/ gevent. 0.6.2 (2011-10-09) ++++++++++++++++++ * GET/HEAD obeys allow_redirects=False. 0.6.1 (2011-08-20) ++++++++++++++++++ * Enhanced status codes experience ``\\o/`` * Set a maximum number of redirects (``settings.max_redirects``) * Full Unicode URL support * Support for protocol-less redirects. * Allow for arbitrary request types. * Bugfixes 0.6.0 (2011-08-17) ++++++++++++++++++ * New callback hook system * New persistent sessions object and context manager * Transparent Dict-cookie handling * Status code reference object * Removed Response.cached * Added Response.request * All args are kwargs * Relative redirect support * HTTPError handling improvements * Improved https testing * Bugfixes 0.5.1 (2011-07-23) ++++++++++++++++++ * International Domain Name Support! * Access headers without fetching entire body (``read()``) * Use lists as dicts for parameters * Add Forced Basic Authentication * Forced Basic is default authentication type * ``python-requests.org`` default User-Agent header * CaseInsensitiveDict lower-case caching * Response.history bugfix 0.5.0 (2011-06-21) ++++++++++++++++++ * PATCH Support * Support for Proxies * HTTPBin Test Suite * Redirect Fixes * settings.verbose stream writing * Querystrings for all methods * URLErrors (Connection Refused, Timeout, Invalid URLs) are treated as explicitly raised ``r.requests.get('hwe://blah'); r.raise_for_status()`` 0.4.1 (2011-05-22) ++++++++++++++++++ * Improved Redirection Handling * New 'allow_redirects' param for following non-GET/HEAD Redirects * Settings module refactoring 0.4.0 (2011-05-15) ++++++++++++++++++ * Response.history: list of redirected responses * Case-Insensitive Header Dictionaries! * Unicode URLs 0.3.4 (2011-05-14) ++++++++++++++++++ * Urllib2 HTTPAuthentication Recursion fix (Basic/Digest) * Internal Refactor * Bytes data upload Bugfix 0.3.3 (2011-05-12) ++++++++++++++++++ * Request timeouts * Unicode url-encoded data * Settings context manager and module 0.3.2 (2011-04-15) ++++++++++++++++++ * Automatic Decompression of GZip Encoded Content * AutoAuth Support for Tupled HTTP Auth 0.3.1 (2011-04-01) ++++++++++++++++++ * Cookie Changes * Response.read() * Poster fix 0.3.0 (2011-02-25) ++++++++++++++++++ * Automatic Authentication API Change * Smarter Query URL Parameterization * Allow file uploads and POST data together * New Authentication Manager System - Simpler Basic HTTP System - Supports all build-in urllib2 Auths - Allows for custom Auth Handlers 0.2.4 (2011-02-19) ++++++++++++++++++ * Python 2.5 Support * PyPy-c v1.4 Support * Auto-Authentication tests * Improved Request object constructor 0.2.3 (2011-02-15) ++++++++++++++++++ * New HTTPHandling Methods - Response.__nonzero__ (false if bad HTTP Status) - Response.ok (True if expected HTTP Status) - Response.error (Logged HTTPError if bad HTTP Status) - Response.raise_for_status() (Raises stored HTTPError) 0.2.2 (2011-02-14) ++++++++++++++++++ * Still handles request in the event of an HTTPError. (Issue #2) * Eventlet and Gevent Monkeypatch support. * Cookie Support (Issue #1) 0.2.1 (2011-02-14) ++++++++++++++++++ * Added file attribute to POST and PUT requests for multipart-encode file uploads. * Added Request.url attribute for context and redirects 0.2.0 (2011-02-14) ++++++++++++++++++ * Birth! 0.0.1 (2011-02-13) ++++++++++++++++++ * Frustration * Conception	Kenneth Reitz	5	8	\N	/opt/rdepot/new/5533563/requests-2.19.1.tar.gz	\N	f	f	Python
46	requests	2.28.1	\N	Kenneth Reitz	5	8	Documentation, https://requests.readthedocs.io, Source, https://github.com/psf/requests	/opt/rdepot/new/32578090/requests-2.28.1.tar.gz	\N	f	f	Python
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

COPY public.newsfeed_event (id, "time", date, newsfeed_event_type, author_id, related_packagemaintainer_id, related_repositorymaintainer_id, related_user_id, related_submission_id, related_repository_id, related_package_id, related_accesstoken_id, deleted) FROM stdin;
1	20:03:44.651+00	2020-03-28	CREATE	4	\N	\N	\N	4	\N	\N	\N	f
2	20:03:44.647+00	2020-03-28	CREATE	4	\N	\N	\N	5	\N	\N	\N	f
3	20:03:44.686+00	2020-03-28	CREATE	4	\N	\N	\N	6	\N	\N	\N	f
4	20:03:44.733+00	2020-03-28	UPDATE	4	\N	\N	\N	6	\N	\N	\N	f
5	20:03:44.717+00	2020-03-28	UPDATE	4	\N	\N	\N	5	\N	\N	\N	f
6	20:03:44.725+00	2020-03-28	UPDATE	4	\N	\N	\N	4	\N	\N	\N	f
7	20:03:45.18+00	2020-03-28	CREATE	4	\N	\N	\N	7	\N	\N	\N	f
8	20:03:45.2+00	2020-03-28	CREATE	4	\N	\N	\N	8	\N	\N	\N	f
9	20:03:45.192+00	2020-03-28	UPDATE	4	\N	\N	\N	7	\N	\N	\N	f
10	20:03:45.219+00	2020-03-28	UPDATE	4	\N	\N	\N	8	\N	\N	\N	f
11	20:03:58.906+00	2020-03-28	CREATE	4	\N	\N	\N	9	\N	\N	\N	f
12	20:03:58.913+00	2020-03-28	CREATE	4	\N	\N	\N	10	\N	\N	\N	f
13	20:03:58.928+00	2020-03-28	UPDATE	4	\N	\N	\N	10	\N	\N	\N	f
14	20:03:58.922+00	2020-03-28	UPDATE	4	\N	\N	\N	9	\N	\N	\N	f
15	20:03:59.593+00	2020-03-28	CREATE	4	\N	\N	\N	11	\N	\N	\N	f
16	20:03:59.606+00	2020-03-28	UPDATE	4	\N	\N	\N	11	\N	\N	\N	f
17	20:03:59.783+00	2020-03-28	CREATE	4	\N	\N	\N	12	\N	\N	\N	f
18	20:03:59.793+00	2020-03-28	UPDATE	4	\N	\N	\N	12	\N	\N	\N	f
19	20:04:17.091+00	2020-03-28	CREATE	4	\N	\N	\N	13	\N	\N	\N	f
20	20:04:17.118+00	2020-03-28	UPDATE	4	\N	\N	\N	13	\N	\N	\N	f
21	20:04:17.502+00	2020-03-28	CREATE	4	\N	\N	\N	14	\N	\N	\N	f
22	20:04:17.51+00	2020-03-28	UPDATE	4	\N	\N	\N	14	\N	\N	\N	f
23	20:04:17.604+00	2020-03-28	CREATE	4	\N	\N	\N	15	\N	\N	\N	f
24	20:04:17.611+00	2020-03-28	UPDATE	4	\N	\N	\N	15	\N	\N	\N	f
25	20:05:58.312+00	2020-03-28	CREATE	7	\N	\N	\N	16	\N	\N	\N	f
26	20:05:58.439+00	2020-03-28	CREATE	7	\N	\N	\N	17	\N	\N	\N	f
27	20:05:58.691+00	2020-03-28	CREATE	7	\N	\N	\N	18	\N	\N	\N	f
28	20:06:13.346+00	2020-03-28	CREATE	7	\N	\N	\N	19	\N	\N	\N	f
29	20:06:23.306+00	2020-03-28	CREATE	7	\N	\N	\N	20	\N	\N	\N	f
30	20:06:48.787+00	2020-03-28	CREATE	6	\N	\N	\N	21	\N	\N	\N	f
31	20:06:49.157+00	2020-03-28	CREATE	6	\N	\N	\N	22	\N	\N	\N	f
32	20:06:49.563+00	2020-03-28	CREATE	6	\N	\N	\N	23	\N	\N	\N	f
33	20:07:00.372+00	2020-03-28	DELETE	6	\N	\N	\N	23	\N	\N	\N	f
34	20:07:13.152+00	2020-03-28	CREATE	6	\N	\N	\N	24	\N	\N	\N	f
35	20:07:52.791+00	2020-03-28	CREATE	5	\N	\N	\N	25	\N	\N	\N	f
36	20:07:52.85+00	2020-03-28	CREATE	5	\N	\N	\N	26	\N	\N	\N	f
37	20:07:52.799+00	2020-03-28	UPDATE	5	\N	\N	\N	25	\N	\N	\N	f
38	20:08:08.855+00	2020-03-28	DELETE	5	\N	\N	\N	16	\N	\N	\N	f
39	20:08:12.2+00	2020-03-28	UPDATE	5	\N	\N	\N	17	\N	\N	\N	f
40	20:08:18.23+00	2020-03-28	UPDATE	5	\N	\N	\N	18	\N	\N	\N	f
41	20:08:23.526+00	2020-03-28	UPDATE	5	\N	\N	\N	21	\N	\N	\N	f
42	20:08:31.542+00	2020-03-28	DELETE	5	\N	\N	\N	22	\N	\N	\N	f
43	20:08:41.718+00	2020-03-28	UPDATE	5	\N	\N	\N	20	\N	\N	\N	f
44	20:08:42.886+00	2020-03-28	DELETE	5	\N	\N	\N	24	\N	\N	\N	f
45	20:09:48.106+00	2020-03-28	DELETE	4	\N	\N	\N	4	\N	\N	\N	f
46	20:10:08.515+00	2020-03-28	DELETE	5	\N	\N	\N	15	\N	\N	\N	f
47	20:12:44.514+00	2020-03-28	CREATE	4	\N	\N	\N	27	\N	\N	\N	f
48	20:12:44.519+00	2020-03-28	UPDATE	4	\N	\N	\N	27	\N	\N	\N	f
49	20:12:44.624+00	2020-03-28	CREATE	4	\N	\N	\N	28	\N	\N	\N	f
50	20:12:44.629+00	2020-03-28	UPDATE	4	\N	\N	\N	28	\N	\N	\N	f
51	20:13:30.635+00	2020-03-28	DELETE	4	\N	\N	\N	28	\N	\N	\N	f
52	20:13:30.736+00	2020-03-28	DELETE	4	\N	\N	\N	27	\N	\N	\N	f
53	20:14:06.587+00	2020-03-28	CREATE	4	\N	\N	\N	29	\N	\N	\N	f
54	20:14:06.591+00	2020-03-28	UPDATE	4	\N	\N	\N	29	\N	\N	\N	f
55	20:14:17.891+00	2020-03-28	DELETE	4	\N	\N	\N	29	\N	\N	\N	f
56	20:14:44.964+00	2020-03-28	CREATE	6	\N	\N	\N	30	\N	\N	\N	f
57	20:14:45.737+00	2020-03-28	CREATE	6	\N	\N	\N	31	\N	\N	\N	f
58	20:59:28.800514+01	2020-03-28	CREATE	4	\N	\N	4	\N	\N	\N	\N	f
59	20:59:28.81074+01	2020-03-28	CREATE	5	\N	\N	5	\N	\N	\N	\N	f
60	20:59:28.82206+01	2020-03-28	CREATE	6	\N	\N	6	\N	\N	\N	\N	f
61	20:59:28.830132+01	2020-03-28	CREATE	7	\N	\N	7	\N	\N	\N	\N	f
62	20:59:28.838152+01	2020-03-28	CREATE	8	\N	\N	8	\N	\N	\N	\N	f
63	20:00:14.007+00	2020-03-28	UPDATE	4	\N	\N	4	\N	\N	\N	\N	f
64	20:03:28.868+00	2020-03-28	UPDATE	4	\N	\N	8	\N	\N	\N	\N	f
65	20:05:47.228+00	2020-03-28	UPDATE	4	\N	\N	7	\N	\N	\N	\N	f
66	20:06:31.773+00	2020-03-28	UPDATE	4	\N	\N	6	\N	\N	\N	\N	f
67	20:07:31.17+00	2020-03-28	UPDATE	4	\N	\N	5	\N	\N	\N	\N	f
68	20:09:02.061+00	2020-03-28	UPDATE	4	\N	\N	4	\N	\N	\N	\N	f
69	20:09:56.183+00	2020-03-28	UPDATE	8	\N	\N	5	\N	\N	\N	\N	f
70	20:12:06.32+00	2020-03-28	UPDATE	8	\N	\N	4	\N	\N	\N	\N	f
71	20:14:30.998+00	2020-03-28	UPDATE	8	\N	\N	6	\N	\N	\N	\N	f
72	10:42:45.4+00	2020-03-29	UPDATE	8	\N	\N	5	\N	\N	\N	\N	f
73	09:58:51.784+00	2020-08-20	CREATE	8	\N	\N	9	\N	\N	\N	\N	f
74	09:58:52.09+00	2020-08-20	UPDATE	8	\N	\N	9	\N	\N	\N	\N	f
75	09:59:08.9+00	2020-08-20	UPDATE	8	\N	\N	4	\N	\N	\N	\N	f
76	09:59:21.132+00	2020-08-20	UPDATE	4	\N	\N	9	\N	\N	\N	\N	f
77	12:35:38.689+00	2020-08-25	CREATE	8	\N	\N	10	\N	\N	\N	\N	f
78	12:35:38.788+00	2020-08-25	UPDATE	8	\N	\N	10	\N	\N	\N	\N	f
79	20:03:40.652+00	2020-03-28	UPLOAD	4	\N	\N	\N	\N	\N	4	\N	f
80	20:03:40.648+00	2020-03-28	UPLOAD	4	\N	\N	\N	\N	\N	6	\N	f
81	20:03:40.647+00	2020-03-28	UPLOAD	4	\N	\N	\N	\N	\N	5	\N	f
82	20:03:40.659+00	2020-03-28	UPLOAD	4	\N	\N	\N	\N	\N	8	\N	f
83	20:03:40.657+00	2020-03-28	UPLOAD	4	\N	\N	\N	\N	\N	7	\N	f
84	20:03:44.778+00	2020-03-28	UPDATE	4	\N	\N	\N	\N	\N	6	\N	f
85	20:03:44.777+00	2020-03-28	UPDATE	4	\N	\N	\N	\N	\N	4	\N	f
86	20:03:44.782+00	2020-03-28	UPDATE	4	\N	\N	\N	\N	\N	8	\N	f
87	20:03:44.858+00	2020-03-28	UPDATE	4	\N	\N	\N	\N	\N	4	\N	f
88	20:03:44.856+00	2020-03-28	UPDATE	4	\N	\N	\N	\N	\N	6	\N	f
89	20:03:44.861+00	2020-03-28	UPDATE	4	\N	\N	\N	\N	\N	8	\N	f
90	20:03:45.211+00	2020-03-28	UPDATE	4	\N	\N	\N	\N	\N	5	\N	f
91	20:03:45.24+00	2020-03-28	UPDATE	4	\N	\N	\N	\N	\N	7	\N	f
92	20:03:45.255+00	2020-03-28	UPDATE	4	\N	\N	\N	\N	\N	5	\N	f
93	20:03:45.28+00	2020-03-28	UPDATE	4	\N	\N	\N	\N	\N	7	\N	f
94	20:03:55.815+00	2020-03-28	UPLOAD	4	\N	\N	\N	\N	\N	9	\N	f
95	20:03:55.917+00	2020-03-28	UPLOAD	4	\N	\N	\N	\N	\N	10	\N	f
96	20:03:56.245+00	2020-03-28	UPLOAD	4	\N	\N	\N	\N	\N	11	\N	f
97	20:03:56.284+00	2020-03-28	UPLOAD	4	\N	\N	\N	\N	\N	12	\N	f
98	20:03:58.941+00	2020-03-28	UPDATE	4	\N	\N	\N	\N	\N	10	\N	f
99	20:03:58.958+00	2020-03-28	UPDATE	4	\N	\N	\N	\N	\N	9	\N	f
100	20:03:59.003+00	2020-03-28	UPDATE	4	\N	\N	\N	\N	\N	10	\N	f
101	20:03:59.04+00	2020-03-28	UPDATE	4	\N	\N	\N	\N	\N	9	\N	f
102	20:03:59.621+00	2020-03-28	UPDATE	4	\N	\N	\N	\N	\N	11	\N	f
103	20:03:59.667+00	2020-03-28	UPDATE	4	\N	\N	\N	\N	\N	11	\N	f
104	20:03:59.807+00	2020-03-28	UPDATE	4	\N	\N	\N	\N	\N	12	\N	f
105	20:03:59.833+00	2020-03-28	UPDATE	4	\N	\N	\N	\N	\N	12	\N	f
106	20:04:14.336+00	2020-03-28	UPLOAD	4	\N	\N	\N	\N	\N	13	\N	f
107	20:04:14.401+00	2020-03-28	UPLOAD	4	\N	\N	\N	\N	\N	14	\N	f
108	20:04:14.418+00	2020-03-28	UPLOAD	4	\N	\N	\N	\N	\N	15	\N	f
109	20:04:17.13+00	2020-03-28	UPDATE	4	\N	\N	\N	\N	\N	15	\N	f
110	20:04:17.191+00	2020-03-28	UPDATE	4	\N	\N	\N	\N	\N	15	\N	f
111	20:04:17.518+00	2020-03-28	UPDATE	4	\N	\N	\N	\N	\N	13	\N	f
112	20:04:17.533+00	2020-03-28	UPDATE	4	\N	\N	\N	\N	\N	13	\N	f
113	20:04:17.62+00	2020-03-28	UPDATE	4	\N	\N	\N	\N	\N	14	\N	f
114	20:04:17.642+00	2020-03-28	UPDATE	4	\N	\N	\N	\N	\N	14	\N	f
115	20:04:24.947+00	2020-03-28	UPDATE	4	\N	\N	\N	\N	\N	13	\N	f
116	20:04:24.962+00	2020-03-28	UPDATE	4	\N	\N	\N	\N	\N	15	\N	f
117	20:04:24.974+00	2020-03-28	UPDATE	4	\N	\N	\N	\N	\N	14	\N	f
118	20:04:31.785+00	2020-03-28	UPDATE	4	\N	\N	\N	\N	\N	11	\N	f
119	20:04:31.795+00	2020-03-28	UPDATE	4	\N	\N	\N	\N	\N	10	\N	f
120	20:04:31.804+00	2020-03-28	UPDATE	4	\N	\N	\N	\N	\N	12	\N	f
121	20:04:31.812+00	2020-03-28	UPDATE	4	\N	\N	\N	\N	\N	9	\N	f
122	20:04:41.444+00	2020-03-28	UPDATE	4	\N	\N	\N	\N	\N	10	\N	f
123	20:04:41.458+00	2020-03-28	UPDATE	4	\N	\N	\N	\N	\N	11	\N	f
124	20:04:41.475+00	2020-03-28	UPDATE	4	\N	\N	\N	\N	\N	9	\N	f
125	20:04:41.491+00	2020-03-28	UPDATE	4	\N	\N	\N	\N	\N	12	\N	f
126	20:04:47.46+00	2020-03-28	UPDATE	4	\N	\N	\N	\N	\N	11	\N	f
127	20:04:51.62+00	2020-03-28	UPDATE	4	\N	\N	\N	\N	\N	7	\N	f
128	20:04:54.588+00	2020-03-28	UPDATE	4	\N	\N	\N	\N	\N	6	\N	f
129	20:04:56.593+00	2020-03-28	UPDATE	4	\N	\N	\N	\N	\N	13	\N	f
130	20:05:25.637+00	2020-03-28	UPDATE	4	\N	\N	\N	\N	\N	13	\N	f
131	20:05:29.974+00	2020-03-28	UPDATE	4	\N	\N	\N	\N	\N	12	\N	f
132	20:05:29.982+00	2020-03-28	UPDATE	4	\N	\N	\N	\N	\N	11	\N	f
133	20:05:35.464+00	2020-03-28	UPDATE	4	\N	\N	\N	\N	\N	10	\N	f
134	20:05:35.472+00	2020-03-28	UPDATE	4	\N	\N	\N	\N	\N	9	\N	f
135	20:05:55.782+00	2020-03-28	UPLOAD	7	\N	\N	\N	\N	\N	16	\N	f
136	20:05:55.961+00	2020-03-28	UPLOAD	7	\N	\N	\N	\N	\N	17	\N	f
137	20:05:56.091+00	2020-03-28	UPLOAD	7	\N	\N	\N	\N	\N	18	\N	f
138	20:06:11.429+00	2020-03-28	UPLOAD	7	\N	\N	\N	\N	\N	19	\N	f
139	20:06:21.099+00	2020-03-28	UPLOAD	7	\N	\N	\N	\N	\N	20	\N	f
140	20:06:46.315+00	2020-03-28	UPLOAD	6	\N	\N	\N	\N	\N	21	\N	f
141	20:06:46.487+00	2020-03-28	UPLOAD	6	\N	\N	\N	\N	\N	22	\N	f
142	20:06:46.487+00	2020-03-28	UPLOAD	6	\N	\N	\N	\N	\N	23	\N	f
143	20:07:11.207+00	2020-03-28	UPLOAD	6	\N	\N	\N	\N	\N	24	\N	f
144	20:07:50.726+00	2020-03-28	UPLOAD	5	\N	\N	\N	\N	\N	25	\N	f
145	20:07:50.774+00	2020-03-28	UPLOAD	5	\N	\N	\N	\N	\N	26	\N	f
146	20:07:52.823+00	2020-03-28	UPDATE	5	\N	\N	\N	\N	\N	25	\N	f
147	20:07:52.812+00	2020-03-28	UPDATE	5	\N	\N	\N	\N	\N	25	\N	f
148	20:07:52.845+00	2020-03-28	UPDATE	5	\N	\N	\N	\N	\N	25	\N	f
149	20:08:12.215+00	2020-03-28	UPDATE	5	\N	\N	\N	\N	\N	17	\N	f
150	20:08:12.21+00	2020-03-28	UPDATE	5	\N	\N	\N	\N	\N	17	\N	f
151	20:08:12.231+00	2020-03-28	UPDATE	5	\N	\N	\N	\N	\N	17	\N	f
152	20:08:18.25+00	2020-03-28	UPDATE	5	\N	\N	\N	\N	\N	18	\N	f
153	20:08:18.243+00	2020-03-28	UPDATE	5	\N	\N	\N	\N	\N	18	\N	f
154	20:08:18.273+00	2020-03-28	UPDATE	5	\N	\N	\N	\N	\N	18	\N	f
155	20:08:23.538+00	2020-03-28	UPDATE	5	\N	\N	\N	\N	\N	21	\N	f
156	20:08:23.532+00	2020-03-28	UPDATE	5	\N	\N	\N	\N	\N	21	\N	f
157	20:08:23.552+00	2020-03-28	UPDATE	5	\N	\N	\N	\N	\N	21	\N	f
158	20:08:41.763+00	2020-03-28	UPDATE	5	\N	\N	\N	\N	\N	20	\N	f
159	20:08:41.753+00	2020-03-28	UPDATE	5	\N	\N	\N	\N	\N	20	\N	f
160	20:08:41.776+00	2020-03-28	UPDATE	5	\N	\N	\N	\N	\N	20	\N	f
161	20:09:48.101+00	2020-03-28	DELETE	4	\N	\N	\N	\N	\N	6	\N	f
162	20:10:08.508+00	2020-03-28	DELETE	5	\N	\N	\N	\N	\N	14	\N	f
163	20:12:42.433+00	2020-03-28	UPLOAD	4	\N	\N	\N	\N	\N	27	\N	f
164	20:12:42.512+00	2020-03-28	UPLOAD	4	\N	\N	\N	\N	\N	28	\N	f
165	20:12:44.529+00	2020-03-28	UPDATE	4	\N	\N	\N	\N	\N	27	\N	f
166	20:12:44.539+00	2020-03-28	UPDATE	4	\N	\N	\N	\N	\N	27	\N	f
167	20:12:44.643+00	2020-03-28	UPDATE	4	\N	\N	\N	\N	\N	28	\N	f
168	20:12:44.653+00	2020-03-28	UPDATE	4	\N	\N	\N	\N	\N	28	\N	f
169	20:13:30.626+00	2020-03-28	DELETE	4	\N	\N	\N	\N	\N	28	\N	f
170	20:13:30.732+00	2020-03-28	DELETE	4	\N	\N	\N	\N	\N	27	\N	f
171	20:14:04.855+00	2020-03-28	UPLOAD	4	\N	\N	\N	\N	\N	29	\N	f
172	20:14:06.597+00	2020-03-28	UPDATE	4	\N	\N	\N	\N	\N	29	\N	f
173	20:14:06.609+00	2020-03-28	UPDATE	4	\N	\N	\N	\N	\N	29	\N	f
174	20:14:17.882+00	2020-03-28	DELETE	4	\N	\N	\N	\N	\N	29	\N	f
175	20:14:42.968+00	2020-03-28	UPLOAD	6	\N	\N	\N	\N	\N	30	\N	f
176	20:14:43.1+00	2020-03-28	UPLOAD	6	\N	\N	\N	\N	\N	31	\N	f
177	10:43:05.243+00	2020-03-29	UPDATE	4	\N	\N	\N	\N	\N	15	\N	f
178	10:43:10.081+00	2020-03-29	UPDATE	4	\N	\N	\N	\N	\N	15	\N	f
179	20:05:25.656+00	2020-03-28	CREATE	4	1	\N	\N	\N	\N	\N	\N	f
180	20:05:29.987+00	2020-03-28	CREATE	4	2	\N	\N	\N	\N	\N	\N	f
181	20:05:35.477+00	2020-03-28	CREATE	4	3	\N	\N	\N	\N	\N	\N	f
182	10:43:05.991+00	2020-03-29	CREATE	4	4	\N	\N	\N	\N	\N	\N	f
183	10:43:10.333+00	2020-03-29	DELETE	4	4	\N	\N	\N	\N	\N	\N	f
184	20:01:11.498+00	2020-03-28	UPDATE	4	\N	\N	\N	\N	2	\N	\N	f
185	20:01:13.018+00	2020-03-28	UPDATE	4	\N	\N	\N	\N	2	\N	\N	f
186	20:03:44.661+00	2020-03-28	UPDATE	4	\N	\N	\N	\N	3	\N	\N	f
187	20:03:44.678+00	2020-03-28	UPDATE	4	\N	\N	\N	\N	3	\N	\N	f
188	20:03:44.702+00	2020-03-28	UPDATE	4	\N	\N	\N	\N	3	\N	\N	f
189	20:03:44.837+00	2020-03-28	UPDATE	4	\N	\N	\N	\N	3	\N	\N	f
190	20:03:44.844+00	2020-03-28	UPDATE	4	\N	\N	\N	\N	3	\N	\N	f
191	20:03:44.846+00	2020-03-28	UPDATE	4	\N	\N	\N	\N	3	\N	\N	f
192	20:03:44.908+00	2020-03-28	UPDATE	4	\N	\N	\N	\N	3	\N	\N	f
193	20:03:44.907+00	2020-03-28	UPDATE	4	\N	\N	\N	\N	3	\N	\N	f
194	20:03:44.919+00	2020-03-28	UPDATE	4	\N	\N	\N	\N	3	\N	\N	f
195	20:03:44.924+00	2020-03-28	UPDATE	4	\N	\N	\N	\N	3	\N	\N	f
196	20:03:44.923+00	2020-03-28	UPDATE	4	\N	\N	\N	\N	3	\N	\N	f
197	20:03:44.932+00	2020-03-28	UPDATE	4	\N	\N	\N	\N	3	\N	\N	f
198	20:03:44.938+00	2020-03-28	UPDATE	4	\N	\N	\N	\N	3	\N	\N	f
199	20:03:44.941+00	2020-03-28	UPDATE	4	\N	\N	\N	\N	3	\N	\N	f
200	20:03:44.957+00	2020-03-28	UPDATE	4	\N	\N	\N	\N	3	\N	\N	f
201	20:03:45.185+00	2020-03-28	UPDATE	4	\N	\N	\N	\N	3	\N	\N	f
202	20:03:45.203+00	2020-03-28	UPDATE	4	\N	\N	\N	\N	3	\N	\N	f
203	20:03:45.249+00	2020-03-28	UPDATE	4	\N	\N	\N	\N	3	\N	\N	f
204	20:03:45.274+00	2020-03-28	UPDATE	4	\N	\N	\N	\N	3	\N	\N	f
205	20:03:45.28+00	2020-03-28	UPDATE	4	\N	\N	\N	\N	3	\N	\N	f
206	20:03:45.286+00	2020-03-28	UPDATE	4	\N	\N	\N	\N	3	\N	\N	f
207	20:03:45.291+00	2020-03-28	UPDATE	4	\N	\N	\N	\N	3	\N	\N	f
208	20:03:45.301+00	2020-03-28	UPDATE	4	\N	\N	\N	\N	3	\N	\N	f
209	20:03:45.306+00	2020-03-28	UPDATE	4	\N	\N	\N	\N	3	\N	\N	f
210	20:03:45.31+00	2020-03-28	UPDATE	4	\N	\N	\N	\N	3	\N	\N	f
211	20:03:58.911+00	2020-03-28	UPDATE	4	\N	\N	\N	\N	4	\N	\N	f
212	20:03:58.917+00	2020-03-28	UPDATE	4	\N	\N	\N	\N	4	\N	\N	f
213	20:03:58.994+00	2020-03-28	UPDATE	4	\N	\N	\N	\N	4	\N	\N	f
214	20:03:59.003+00	2020-03-28	UPDATE	4	\N	\N	\N	\N	4	\N	\N	f
215	20:03:59.03+00	2020-03-28	UPDATE	4	\N	\N	\N	\N	4	\N	\N	f
216	20:03:59.037+00	2020-03-28	UPDATE	4	\N	\N	\N	\N	4	\N	\N	f
217	20:03:59.043+00	2020-03-28	UPDATE	4	\N	\N	\N	\N	4	\N	\N	f
218	20:03:59.071+00	2020-03-28	UPDATE	4	\N	\N	\N	\N	4	\N	\N	f
219	20:03:59.083+00	2020-03-28	UPDATE	4	\N	\N	\N	\N	4	\N	\N	f
220	20:03:59.095+00	2020-03-28	UPDATE	4	\N	\N	\N	\N	4	\N	\N	f
221	20:03:59.597+00	2020-03-28	UPDATE	4	\N	\N	\N	\N	4	\N	\N	f
222	20:03:59.648+00	2020-03-28	UPDATE	4	\N	\N	\N	\N	4	\N	\N	f
223	20:03:59.692+00	2020-03-28	UPDATE	4	\N	\N	\N	\N	4	\N	\N	f
224	20:03:59.699+00	2020-03-28	UPDATE	4	\N	\N	\N	\N	4	\N	\N	f
225	20:03:59.704+00	2020-03-28	UPDATE	4	\N	\N	\N	\N	4	\N	\N	f
226	20:03:59.786+00	2020-03-28	UPDATE	4	\N	\N	\N	\N	4	\N	\N	f
227	20:03:59.828+00	2020-03-28	UPDATE	4	\N	\N	\N	\N	4	\N	\N	f
228	20:03:59.862+00	2020-03-28	UPDATE	4	\N	\N	\N	\N	4	\N	\N	f
229	20:03:59.868+00	2020-03-28	UPDATE	4	\N	\N	\N	\N	4	\N	\N	f
230	20:03:59.873+00	2020-03-28	UPDATE	4	\N	\N	\N	\N	4	\N	\N	f
231	20:04:17.1+00	2020-03-28	UPDATE	4	\N	\N	\N	\N	2	\N	\N	f
232	20:04:17.178+00	2020-03-28	UPDATE	4	\N	\N	\N	\N	2	\N	\N	f
233	20:04:17.23+00	2020-03-28	UPDATE	4	\N	\N	\N	\N	2	\N	\N	f
234	20:04:17.242+00	2020-03-28	UPDATE	4	\N	\N	\N	\N	2	\N	\N	f
235	20:04:17.251+00	2020-03-28	UPDATE	4	\N	\N	\N	\N	2	\N	\N	f
236	20:04:17.504+00	2020-03-28	UPDATE	4	\N	\N	\N	\N	2	\N	\N	f
237	20:04:17.53+00	2020-03-28	UPDATE	4	\N	\N	\N	\N	2	\N	\N	f
238	20:04:17.558+00	2020-03-28	UPDATE	4	\N	\N	\N	\N	2	\N	\N	f
239	20:04:17.562+00	2020-03-28	UPDATE	4	\N	\N	\N	\N	2	\N	\N	f
240	20:04:17.566+00	2020-03-28	UPDATE	4	\N	\N	\N	\N	2	\N	\N	f
241	20:04:17.605+00	2020-03-28	UPDATE	4	\N	\N	\N	\N	2	\N	\N	f
242	20:04:17.635+00	2020-03-28	UPDATE	4	\N	\N	\N	\N	2	\N	\N	f
243	20:04:17.66+00	2020-03-28	UPDATE	4	\N	\N	\N	\N	2	\N	\N	f
244	20:04:17.664+00	2020-03-28	UPDATE	4	\N	\N	\N	\N	2	\N	\N	f
245	20:04:17.668+00	2020-03-28	UPDATE	4	\N	\N	\N	\N	2	\N	\N	f
246	20:04:24.952+00	2020-03-28	UPDATE	4	\N	\N	\N	\N	2	\N	\N	f
247	20:04:24.966+00	2020-03-28	UPDATE	4	\N	\N	\N	\N	2	\N	\N	f
248	20:04:24.978+00	2020-03-28	UPDATE	4	\N	\N	\N	\N	2	\N	\N	f
249	20:04:31.788+00	2020-03-28	UPDATE	4	\N	\N	\N	\N	4	\N	\N	f
250	20:04:31.798+00	2020-03-28	UPDATE	4	\N	\N	\N	\N	4	\N	\N	f
251	20:04:31.806+00	2020-03-28	UPDATE	4	\N	\N	\N	\N	4	\N	\N	f
252	20:04:31.814+00	2020-03-28	UPDATE	4	\N	\N	\N	\N	4	\N	\N	f
253	20:04:41.448+00	2020-03-28	UPDATE	4	\N	\N	\N	\N	4	\N	\N	f
254	20:04:41.462+00	2020-03-28	UPDATE	4	\N	\N	\N	\N	4	\N	\N	f
255	20:04:41.479+00	2020-03-28	UPDATE	4	\N	\N	\N	\N	4	\N	\N	f
256	20:04:41.495+00	2020-03-28	UPDATE	4	\N	\N	\N	\N	4	\N	\N	f
257	20:04:47.472+00	2020-03-28	UPDATE	4	\N	\N	\N	\N	4	\N	\N	f
258	20:04:51.631+00	2020-03-28	UPDATE	4	\N	\N	\N	\N	3	\N	\N	f
259	20:04:54.599+00	2020-03-28	UPDATE	4	\N	\N	\N	\N	3	\N	\N	f
260	20:04:56.601+00	2020-03-28	UPDATE	4	\N	\N	\N	\N	2	\N	\N	f
261	20:05:25.64+00	2020-03-28	UPDATE	4	\N	\N	\N	\N	2	\N	\N	f
262	20:05:29.977+00	2020-03-28	UPDATE	4	\N	\N	\N	\N	4	\N	\N	f
263	20:05:29.984+00	2020-03-28	UPDATE	4	\N	\N	\N	\N	4	\N	\N	f
264	20:05:35.467+00	2020-03-28	UPDATE	4	\N	\N	\N	\N	4	\N	\N	f
265	20:05:35.474+00	2020-03-28	UPDATE	4	\N	\N	\N	\N	4	\N	\N	f
266	20:05:58.313+00	2020-03-28	UPDATE	7	\N	\N	\N	\N	2	\N	\N	f
267	20:05:58.441+00	2020-03-28	UPDATE	7	\N	\N	\N	\N	2	\N	\N	f
268	20:05:58.693+00	2020-03-28	UPDATE	7	\N	\N	\N	\N	2	\N	\N	f
269	20:06:13.348+00	2020-03-28	UPDATE	7	\N	\N	\N	\N	3	\N	\N	f
270	20:06:23.307+00	2020-03-28	UPDATE	7	\N	\N	\N	\N	5	\N	\N	f
271	20:06:48.789+00	2020-03-28	UPDATE	6	\N	\N	\N	\N	2	\N	\N	f
272	20:06:49.159+00	2020-03-28	UPDATE	6	\N	\N	\N	\N	2	\N	\N	f
273	20:06:49.565+00	2020-03-28	UPDATE	6	\N	\N	\N	\N	2	\N	\N	f
274	20:07:13.155+00	2020-03-28	UPDATE	6	\N	\N	\N	\N	5	\N	\N	f
275	20:07:52.793+00	2020-03-28	UPDATE	5	\N	\N	\N	\N	2	\N	\N	f
276	20:07:52.829+00	2020-03-28	UPDATE	5	\N	\N	\N	\N	2	\N	\N	f
277	20:07:52.838+00	2020-03-28	UPDATE	5	\N	\N	\N	\N	2	\N	\N	f
278	20:07:52.854+00	2020-03-28	UPDATE	5	\N	\N	\N	\N	3	\N	\N	f
279	20:07:52.858+00	2020-03-28	UPDATE	5	\N	\N	\N	\N	2	\N	\N	f
280	20:07:52.863+00	2020-03-28	UPDATE	5	\N	\N	\N	\N	2	\N	\N	f
281	20:07:52.866+00	2020-03-28	UPDATE	5	\N	\N	\N	\N	2	\N	\N	f
282	20:08:12.221+00	2020-03-28	UPDATE	5	\N	\N	\N	\N	2	\N	\N	f
283	20:08:12.227+00	2020-03-28	UPDATE	5	\N	\N	\N	\N	2	\N	\N	f
284	20:08:12.246+00	2020-03-28	UPDATE	5	\N	\N	\N	\N	2	\N	\N	f
285	20:08:12.251+00	2020-03-28	UPDATE	5	\N	\N	\N	\N	2	\N	\N	f
286	20:08:12.254+00	2020-03-28	UPDATE	5	\N	\N	\N	\N	2	\N	\N	f
287	20:08:18.255+00	2020-03-28	UPDATE	5	\N	\N	\N	\N	2	\N	\N	f
288	20:08:18.268+00	2020-03-28	UPDATE	5	\N	\N	\N	\N	2	\N	\N	f
289	20:08:18.286+00	2020-03-28	UPDATE	5	\N	\N	\N	\N	2	\N	\N	f
290	20:08:18.296+00	2020-03-28	UPDATE	5	\N	\N	\N	\N	2	\N	\N	f
291	20:08:18.299+00	2020-03-28	UPDATE	5	\N	\N	\N	\N	2	\N	\N	f
292	20:08:23.543+00	2020-03-28	UPDATE	5	\N	\N	\N	\N	2	\N	\N	f
293	20:08:23.549+00	2020-03-28	UPDATE	5	\N	\N	\N	\N	2	\N	\N	f
294	20:08:23.562+00	2020-03-28	UPDATE	5	\N	\N	\N	\N	2	\N	\N	f
295	20:08:23.565+00	2020-03-28	UPDATE	5	\N	\N	\N	\N	2	\N	\N	f
296	20:08:23.569+00	2020-03-28	UPDATE	5	\N	\N	\N	\N	2	\N	\N	f
297	20:08:41.767+00	2020-03-28	UPDATE	5	\N	\N	\N	\N	5	\N	\N	f
298	20:08:41.772+00	2020-03-28	UPDATE	5	\N	\N	\N	\N	5	\N	\N	f
299	20:08:41.783+00	2020-03-28	UPDATE	5	\N	\N	\N	\N	5	\N	\N	f
300	20:08:41.786+00	2020-03-28	UPDATE	5	\N	\N	\N	\N	5	\N	\N	f
301	20:08:41.789+00	2020-03-28	UPDATE	5	\N	\N	\N	\N	5	\N	\N	f
302	20:08:48.105+00	2020-03-28	UPDATE	5	\N	\N	\N	\N	2	\N	\N	f
303	20:08:48.109+00	2020-03-28	UPDATE	5	\N	\N	\N	\N	2	\N	\N	f
304	20:08:51.742+00	2020-03-28	UPDATE	5	\N	\N	\N	\N	5	\N	\N	f
305	20:08:51.745+00	2020-03-28	UPDATE	5	\N	\N	\N	\N	5	\N	\N	f
306	20:08:53.749+00	2020-03-28	UPDATE	5	\N	\N	\N	\N	5	\N	\N	f
307	20:08:53.753+00	2020-03-28	UPDATE	5	\N	\N	\N	\N	5	\N	\N	f
308	20:09:06.488+00	2020-03-28	UPDATE	4	\N	\N	\N	\N	3	\N	\N	f
309	20:09:06.491+00	2020-03-28	UPDATE	4	\N	\N	\N	\N	3	\N	\N	f
310	20:09:09.17+00	2020-03-28	UPDATE	4	\N	\N	\N	\N	4	\N	\N	f
311	20:09:09.173+00	2020-03-28	UPDATE	4	\N	\N	\N	\N	4	\N	\N	f
312	20:09:15.758+00	2020-03-28	UPDATE	4	\N	\N	\N	\N	4	\N	\N	f
313	20:09:15.761+00	2020-03-28	UPDATE	4	\N	\N	\N	\N	4	\N	\N	f
314	20:09:48.11+00	2020-03-28	UPDATE	4	\N	\N	\N	\N	3	\N	\N	f
315	20:09:48.125+00	2020-03-28	UPDATE	4	\N	\N	\N	\N	3	\N	\N	f
316	20:09:48.128+00	2020-03-28	UPDATE	4	\N	\N	\N	\N	3	\N	\N	f
317	20:10:08.519+00	2020-03-28	UPDATE	5	\N	\N	\N	\N	2	\N	\N	f
318	20:10:08.531+00	2020-03-28	UPDATE	5	\N	\N	\N	\N	2	\N	\N	f
319	20:10:08.533+00	2020-03-28	UPDATE	5	\N	\N	\N	\N	2	\N	\N	f
320	20:12:44.515+00	2020-03-28	UPDATE	4	\N	\N	\N	\N	6	\N	\N	f
321	20:12:44.536+00	2020-03-28	UPDATE	4	\N	\N	\N	\N	6	\N	\N	f
322	20:12:44.549+00	2020-03-28	UPDATE	4	\N	\N	\N	\N	6	\N	\N	f
323	20:12:44.551+00	2020-03-28	UPDATE	4	\N	\N	\N	\N	6	\N	\N	f
324	20:12:44.556+00	2020-03-28	UPDATE	4	\N	\N	\N	\N	6	\N	\N	f
325	20:12:44.625+00	2020-03-28	UPDATE	4	\N	\N	\N	\N	6	\N	\N	f
326	20:12:44.65+00	2020-03-28	UPDATE	4	\N	\N	\N	\N	6	\N	\N	f
327	20:12:44.666+00	2020-03-28	UPDATE	4	\N	\N	\N	\N	6	\N	\N	f
328	20:12:44.671+00	2020-03-28	UPDATE	4	\N	\N	\N	\N	6	\N	\N	f
329	20:12:44.677+00	2020-03-28	UPDATE	4	\N	\N	\N	\N	6	\N	\N	f
330	20:13:27.926+00	2020-03-28	UPDATE	4	\N	\N	\N	\N	6	\N	\N	f
331	20:13:27.934+00	2020-03-28	UPDATE	4	\N	\N	\N	\N	6	\N	\N	f
332	20:13:30.639+00	2020-03-28	UPDATE	4	\N	\N	\N	\N	6	\N	\N	f
333	20:13:30.652+00	2020-03-28	UPDATE	4	\N	\N	\N	\N	6	\N	\N	f
334	20:13:30.655+00	2020-03-28	UPDATE	4	\N	\N	\N	\N	6	\N	\N	f
335	20:13:30.743+00	2020-03-28	UPDATE	4	\N	\N	\N	\N	6	\N	\N	f
336	20:13:30.754+00	2020-03-28	UPDATE	4	\N	\N	\N	\N	6	\N	\N	f
337	20:13:30.756+00	2020-03-28	UPDATE	4	\N	\N	\N	\N	6	\N	\N	f
338	20:13:30.789+00	2020-03-28	UPDATE	4	\N	\N	\N	\N	6	\N	\N	f
339	20:13:30.791+00	2020-03-28	UPDATE	4	\N	\N	\N	\N	6	\N	\N	f
340	20:13:30.795+00	2020-03-28	DELETE	4	\N	\N	\N	\N	6	\N	\N	f
341	20:14:06.588+00	2020-03-28	UPDATE	4	\N	\N	\N	\N	7	\N	\N	f
342	20:14:06.604+00	2020-03-28	UPDATE	4	\N	\N	\N	\N	7	\N	\N	f
343	20:14:06.616+00	2020-03-28	UPDATE	4	\N	\N	\N	\N	7	\N	\N	f
344	20:14:06.618+00	2020-03-28	UPDATE	4	\N	\N	\N	\N	7	\N	\N	f
345	20:14:06.622+00	2020-03-28	UPDATE	4	\N	\N	\N	\N	7	\N	\N	f
346	20:14:10.687+00	2020-03-28	UPDATE	4	\N	\N	\N	\N	7	\N	\N	f
347	20:14:10.689+00	2020-03-28	UPDATE	4	\N	\N	\N	\N	7	\N	\N	f
348	20:14:14.278+00	2020-03-28	UPDATE	4	\N	\N	\N	\N	7	\N	\N	f
349	20:14:14.281+00	2020-03-28	UPDATE	4	\N	\N	\N	\N	7	\N	\N	f
350	20:14:17.895+00	2020-03-28	UPDATE	4	\N	\N	\N	\N	7	\N	\N	f
351	20:14:19.412+00	2020-03-28	DELETE	4	\N	\N	\N	\N	7	\N	\N	f
352	20:14:44.965+00	2020-03-28	UPDATE	6	\N	\N	\N	\N	5	\N	\N	f
353	20:14:45.738+00	2020-03-28	UPDATE	6	\N	\N	\N	\N	5	\N	\N	f
354	10:43:05.272+00	2020-03-29	UPDATE	4	\N	\N	\N	\N	2	\N	\N	f
355	10:43:05.321+00	2020-03-29	UPDATE	4	\N	\N	\N	\N	2	\N	\N	f
356	10:43:05.331+00	2020-03-29	UPDATE	4	\N	\N	\N	\N	2	\N	\N	f
357	10:43:10.091+00	2020-03-29	UPDATE	4	\N	\N	\N	\N	2	\N	\N	f
358	10:43:10.117+00	2020-03-29	UPDATE	4	\N	\N	\N	\N	2	\N	\N	f
359	10:43:10.123+00	2020-03-29	UPDATE	4	\N	\N	\N	\N	2	\N	\N	f
360	20:04:24.995+00	2020-03-28	CREATE	4	\N	1	\N	\N	\N	\N	\N	f
361	20:04:31.817+00	2020-03-28	CREATE	4	\N	2	\N	\N	\N	\N	\N	f
362	20:04:36.897+00	2020-03-28	CREATE	4	\N	3	\N	\N	\N	\N	\N	f
363	20:04:41.499+00	2020-03-28	DELETE	4	\N	2	\N	\N	\N	\N	\N	f
364	07:18:28.512+00	2022-09-09	CREATE	4	5	\N	\N	\N	\N	\N	\N	f
365	12:17:57.513055+00	2023-07-10	UPLOAD	7	\N	\N	\N	34	\N	\N	\N	f
366	12:18:06.909152+00	2023-07-10	UPLOAD	7	\N	\N	\N	35	\N	\N	\N	f
367	12:18:15.963673+00	2023-07-10	UPLOAD	7	\N	\N	\N	36	\N	\N	\N	f
368	12:18:30.307159+00	2023-07-10	UPLOAD	7	\N	\N	\N	37	\N	\N	\N	f
369	12:18:56.248695+00	2023-07-10	CREATE	4	\N	6	\N	\N	\N	\N	\N	f
370	12:19:42.17798+00	2023-07-10	UPDATE	4	\N	\N	\N	36	\N	\N	\N	f
371	12:20:09.919284+00	2023-07-10	UPDATE	4	\N	\N	\N	37	\N	\N	\N	f
372	12:20:15.504672+00	2023-07-10	UPDATE	4	\N	\N	\N	34	\N	\N	\N	f
373	12:20:33.577297+00	2023-07-10	UPDATE	7	\N	\N	\N	35	\N	\N	\N	f
374	12:21:37.544706+00	2023-07-10	UPLOAD	5	\N	\N	\N	38	\N	\N	\N	f
375	12:21:47.181563+00	2023-07-10	UPLOAD	5	\N	\N	\N	39	\N	\N	\N	f
376	12:22:15.570171+00	2023-07-10	UPLOAD	4	\N	\N	\N	40	\N	\N	\N	f
377	12:24:58.048102+00	2023-07-10	CREATE	4	6	\N	\N	\N	\N	\N	\N	f
378	12:25:15.661083+00	2023-07-10	CREATE	4	7	\N	\N	\N	\N	\N	\N	f
379	12:32:02.044347+00	2023-07-10	UPLOAD	4	\N	\N	\N	41	\N	\N	\N	f
380	12:32:32.750608+00	2023-07-10	UPLOAD	4	\N	\N	\N	42	\N	\N	\N	f
381	12:33:27.912225+00	2023-07-10	UPLOAD	4	\N	\N	\N	43	\N	\N	\N	f
382	12:34:02.690264+00	2023-07-10	UPLOAD	4	\N	\N	\N	44	\N	\N	\N	f
383	12:34:38.748343+00	2023-07-10	CREATE	4	8	\N	\N	\N	\N	\N	\N	f
384	12:35:00.699712+00	2023-07-10	CREATE	4	9	\N	\N	\N	\N	\N	\N	f
385	12:35:08.081333+00	2023-07-10	CREATE	4	10	\N	\N	\N	\N	\N	\N	f
386	12:35:21.693706+00	2023-07-10	CREATE	4	11	\N	\N	\N	\N	\N	\N	f
387	12:36:04.077593+00	2023-07-10	UPLOAD	7	\N	\N	\N	45	\N	\N	\N	f
388	12:36:18.482532+00	2023-07-10	UPLOAD	7	\N	\N	\N	46	\N	\N	\N	f
389	11:11:11.951486+00	2023-11-15	CREATE	4	\N	\N	\N	\N	\N	\N	1	f
390	11:12:25.346875+00	2023-12-04	CREATE	7	\N	\N	\N	\N	\N	\N	2	f
391	11:12:54.853912+00	2023-12-05	CREATE	7	\N	\N	\N	\N	\N	\N	3	f
392	09:13:31.7944+00	2023-12-06	UPDATE	7	\N	\N	\N	\N	\N	\N	3	f
393	11:49:44.78367+00	2023-12-06	CREATE	7	\N	\N	\N	\N	\N	\N	4	f
394	23:59:59.99999+00	2023-12-07	UPDATE	7	\N	\N	\N	\N	\N	\N	4	f
395	13:42:26.568523+00	2024-01-19	CREATE	4	\N	\N	\N	\N	\N	\N	5	f
396	13:42:32.681292+00	2024-01-19	CREATE	5	\N	\N	\N	\N	\N	\N	6	f
397	13:42:34.341632+00	2024-01-19	CREATE	6	\N	\N	\N	\N	\N	\N	7	f
398	13:42:35.326969+00	2024-01-19	CREATE	7	\N	\N	\N	\N	\N	\N	8	f
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

SELECT pg_catalog.setval('public.changed_variable_id_seq', 373, true);


--
-- Name: newsfeed_event_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.newsfeed_event_id_seq', 398, true);


--
-- Name: package_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.package_id_seq', 46, true);


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

SELECT pg_catalog.setval('public.submission_id_seq', 46, true);


--
-- Name: user_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.user_id_seq', 10, true);

--
-- Name: user_settings_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.user_settings_id_seq', 2, true);
