\name{abc}
\alias{abc}
\alias{abc.return}
\title{
  Parameter estimation with Approximate Bayesian Computation (ABC)
}
\description{
  This function performs multivariate parameter estimation based on
  summary statistics using an ABC algorithm. The algorithms implemented
  are rejection sampling, and local linear or non-linear (neural
  network) regression. A conditional heteroscedastic model is available
  for the latter two algorithms.
}
\usage{
abc(target, param, sumstat, tol, method, hcorr = TRUE, transf = "none",
logit.bounds, subset = NULL, kernel = "epanechnikov", numnet =
10, sizenet = 5, lambda = c(0.0001,0.001,0.01), trace = FALSE, maxit =
500, \dots)
}
\arguments{
  \item{target}{  
    a vector of the observed summary statistics.}
  \item{param}{
    a vector, matrix or data frame of the simulated parameter values,
    i.e. the dependent variable(s) when \code{method} is \code{"loclinear"} or
    \code{"neuralnet"}.}
  \item{sumstat}{
    a vector, matrix or data frame of the simulated summary statistics,
    i.e. the independent variables when \code{method} is \code{"loclinear"} or
    \code{"neuralnet"}.}
  \item{tol}{
    tolerance, the required proportion of points accepted nearest the
    target values.}
  \item{method}{
    a character string indicating the type of ABC algorithm to be
    applied.  Possible values are \code{"rejection"},
    \code{"loclinear"}, and \code{"neuralnet"}. See also
    \code{Details}.}
  \item{hcorr}{
    logical, the conditional heteroscedastic model is applied if
    \code{TRUE} (default).}
  \item{transf}{
    a vector of character strings indicating the kind of transformation
    to be applied to the parameter values. The possible values are
    \code{"log"}, \code{"logit"}, and \code{"none"} (default), when no
    is transformation applied. See also \code{Details}.}
  \item{logit.bounds}{
    a matrix of bounds if \code{transf} is \code{"logit"}. 
    The matrix has as many lines as parameters (including the ones 
    that are not \code{"logit"} transformed) and 2 columns. First column is the 
    minimum bound and second column is the maximum bound.}
  \item{subset}{
    a logical expression indicating elements or rows to keep. Missing
    values in \code{param} and/or \code{sumstat} are taken as
    \code{FALSE}.}
  \item{kernel}{
    a character string specifying the kernel to be used when
    \code{method} is \code{"loclinear"} or \code{"neuralnet"}. Defaults
    to \code{"epanechnikov"}. See \code{\link{density}} for details.}
  \item{numnet}{
    the number of neural networks when \code{method} is
    \code{"neuralnet"}. Defaults to 10. It indicates the number of times
    the function \code{\link{nnet}} is called.}
  \item{sizenet}{
    the number of units in the hidden layer. Defaults to 5. Can be zero
    if there are no skip-layer units. See \code{\link{nnet}} for more
    details.}
  \item{lambda}{
    a numeric vector or a single value indicating the weight decay when
    \code{method} is \code{"neuralnet"}. See \code{\link{nnet}} for more
    details. By default, 0.0001, 0.001, or 0.01 is randomly chosen for
    each of the networks.}
  \item{trace}{
    logical, if \code{TRUE} switches on tracing the optimization of
    \code{\link{nnet}}. Applies only when \code{method} is
    \code{"neuralnet"}.}
  \item{maxit}{
    numeric, the maximum number of iterations. Defaults to 500. Applies
    only when \code{method} is \code{"neuralnet"}. See also
    \code{\link{nnet}}.}
  \item{\dots}{
    other arguments passed to \code{\link{nnet}}.}
}
\value{
  The returned value is an object of class \code{"abc"}, containing the
  following components:
  
  \item{adj.values}{The regression adjusted values, when \code{method} is
    \code{"loclinear"} or \code{"neuralnet"}.}
  \item{unadj.values}{The unadjusted values that correspond to
    \code{"rejection"} \code{method}.}
  \item{ss}{The summary statistics for the accepted simulations.}
  \item{weights}{The regression weights, when \code{method} is
    \code{"loclinear"} or \code{"neuralnet"}.}
  \item{residuals}{The residuals from the regression when \code{method} is
    \code{"loclinear"} or \code{"neuralnet"}. These are the "raw"
    residuals from \code{\link{lsfit}} or \code{\link{nnet}},
    respectively, thus they are not on the original scale of the
    parameter(s).}
  \item{dist}{The Euclidean distances for the accepted simulations.}
  \item{call}{The original call.}
  \item{na.action}{A logical vector indicating the elements or rows that
    were excluded, including both \code{NA}/\code{NaN}'s and
    elements/rows selected by \code{subset}.}
  \item{region}{A logical expression indicting the elements or rows that
    were accepted.}
  \item{transf}{The parameter transformations that have been used.}
  \item{logit.bounds}{The bounds, if transformation was \code{"logit"}.}
  \item{kernel}{The kernel used.}
  \item{method}{Character string indicating the \code{method}, i.e.
    \code{"rejection"}, \code{"loclinear"}, or \code{"neuralnet"}.}
  \item{lambda}{A numeric vector of length \code{numnet}. The actual
    values of the decay parameters used in each of the neural networks,
    when \code{method} is \code{"neuralnet"}. These values are selected
    randomly from the supplied vector of values.}
  \item{numparam}{Number of parameters used.}
  \item{numstat}{Number of summary statistics used.}
  \item{names}{A list with two elements: \code{parameter.names} and
    \code{statistics.names}. Both contain a vector of character strings
    with the parameter and statistics names, respectively.}
}

\details{
  
  These ABC algorithms generate random samples from the posterior
  distributions of one or more parameters of interest, \eqn{\theta_1,
  \theta_2, \dots, \theta_n}. To apply any of these algorithms, (i) data
  sets have to be simulated based on random draws from the prior
  distributions of the \eqn{\theta_i}'s, (ii) from these data sets, a set
  of summary statistics have to be calculated, \eqn{S(y)}, (iii) the
  same summary statistics have to be calculated from the observed data,
  \eqn{S(y_0)}, and (iv) a tolerance rate must be chosen
  (\code{tol}). See \code{\link{cv4abc}} for a cross-validation tool
  that may help in choosing the tolerance rate.

  When \code{method} is \code{"rejection"}, the simple rejection
  algorithm is used. Parameter values are accepted if the Euclidean
  distance between \eqn{S(y)} and \eqn{S(y_0)} is sufficiently
  small. The percentage of accepted simulations is determined by
  \code{tol}. When \code{method} is \code{"loclinear"}, a local linear
  regression method corrects for the imperfect match between \eqn{S(y)}
  and \eqn{S(y_0)}. The accepted parameter values are weighted by a
  smooth function (\code{kernel}) of the distance between \eqn{S(y)} and
  \eqn{S(y_0)}, and corrected according to a linear transform:
  \eqn{\theta^{*} = \theta - b(S(y) - S(y_0))}. \eqn{\theta^{*}}'s
  represent samples form the posterior distribution. This method calls
  the function \code{\link{lsfit}} from the \code{stats} library. The
  non-linear regression correction method (\code{"neuralnet"}) uses a
  non-linear regression to minimize the departure from non-linearity
  using the function \code{\link{nnet}}. The posterior samples of
  parameters based on the rejection algorithm are returned as well, even
  when one of the regression algorithms is used.
  
  Several additional arguments can be specified when \code{method} is
  \code{"neuralnet"}. The method is based on the function
  \code{\link{nnet}} from the library \code{\link{nnet}}, which fits
  single-hidden-layer neural networks. \code{numnet} defines the
  number of neural networks, thus the function \code{\link{nnet}} is
  called \code{numnet} number of times. Predictions from different
  neural networks can be rather different, so the median of the
  predictions from all neural networks is used to provide a global
  prediction. The choice of the number of neural networks is a trade-off
  between speed and accuracy. The default is set to 10 networks. The
  number of units in the hidden layer can be specified via
  \code{sizenet}. Selecting the number of hidden units is similar to
  selecting the independent variables in a linear or non-linear
  regression. Thus, it corresponds to the complexity of the
  network. There is several rule of thumb to choose the number of hidden
  units, but they are often unreliable. Generally speaking, the optimal
  choice of \code{sizenet} depends on the dimensionality, thus the
  number of statistics in \code{sumstat}. It can be zero when there are
  no skip-layer units. See also \code{\link{nnet}} for more details. The
  \code{method} \code{"neuralnet"} is recommended when dealing with a
  large number of summary statistics.

  If \code{method} is \code{"loclinear"} or \code{"neuralnet"}, a
  correction for heteroscedasticity is applied by default (\code{hcorr =
  TRUE}).

  Parameters maybe transformed priori to estimation. The type of
  transformation is defined by \code{transf}. The length of
  \code{transf} is normally the same as the number of parameters. If
  only one value is given, that same transformation is applied to all
  parameters and the user is warned. When a parameter transformation
  used, the parameters are back-transformed to their original scale
  after the regression estimation. No transformations can be applied
  when \code{method} is \code{"rejection"}.
  
  Using names for the parameters and summary statistics is strongly
  recommended. Names can be supplied as \code{\link{names}} or
  \code{\link{colnames}} to \code{param} and \code{sumstat} (and
  \code{target}). If no names are supplied, P1, P2, \dots is assigned to
  parameters and S1, S2, \dots to summary statistics and the user is
  warned.
  
}
\references{
  
  Pritchard, J.K., and M.T. Seielstad and A. Perez-Lezaun and
  M.W. Feldman (1999) Population growth of human Y chromosomes: a study
  of Y chromosome microsatellites. \emph{Molecular Biology and
  Evolution}, \bold{16}, 1791--1798.
  
  Beaumont, M.A., Zhang, W., and Balding, D.J. (2002) Approximate
  Bayesian Computation in Population Genetics, \emph{Genetics},
  \bold{162}, 2025-2035.
  
  Blum, M.G.B. and Francois, O. (2010) Non-linear regression models
  for Approximate Bayesian Computation. \emph{Statistics and Computing}
  \bold{20}, 63-73.
  
  Csillery, K., M.G.B. Blum, O.E. Gaggiotti and O. Francois (2010)
  Approximate Bayesian Computation (ABC) in practice. \emph{Trends in
  Ecology and Evolution}, \bold{25}, 410-418.
  
}
\author{
  Katalin Csillery, Olivier Francois and Michael Blum with some
  initial code from Mark Beaumont
  (\url{http://www.rubic.rdg.ac.uk/~mab/}).
}
\seealso{
  \code{\link{summary.abc}}, \code{\link{hist.abc}},
  \code{\link{plot.abc}}, \code{\link{lsfit}}, \code{\link{nnet}},
  \code{\link{cv4abc}}
}
\examples{
data(musigma2)
## this data set contains five R objects, see ?musigma2 for
## details

## The rejection algorithm
##
rej <- abc(target=stat.obs, param=par.sim, sumstat=stat.sim, tol=.1, method =
"rejection") 

## ABC with local linear regression correction without/with correction
## for heteroscedasticity 
##
lin <- abc(target=stat.obs, param=par.sim, sumstat=stat.sim, tol=.1, hcorr =
FALSE, method = "loclinear", transf=c("none","log"))
linhc <- abc(target=stat.obs, param=par.sim, sumstat=stat.sim, tol=.1, method =
"loclinear", transf=c("none","log")) 

## ABC with neural networks with correction for heteroscedasticity
##
net <- abc(target=stat.obs, param=par.sim, sumstat=stat.sim,
tol=.2, method="neuralnet", transf=c("none","log")) 

## posterior summaries
##
linsum <- summary(linhc, intvl = .9)
linsum
## compare with the rejection sampling
summary(linhc, unadj = TRUE, intvl = .9)

## posterior histograms
##
hist(linhc, breaks=30, caption=c(expression(mu),
expression(sigma^2))) 

## or send histograms to a pdf file
hist(linhc, file="linhc", breaks=30, caption=c(expression(mu),
expression(sigma^2)))

## diagnostic plots: compare the 3 'abc' objects: "loclinear",
## "loclinear" with correction for heteroscedasticity, and "neuralnet"
## with correction for heteroscedasticity
##
plot(lin, param=par.sim)
plot(linhc, param=par.sim)
plot(net, param=par.sim)

## example illustrates how to add "true" parameter values to a plot
##
postmod <- c(post.mu[match(max(post.mu[,2]), post.mu[,2]),1],
             post.sigma2[match(max(post.sigma2[,2]), post.sigma2[,2]),1])
plot(net, param=par.sim, true=postmod)


## artificial example to show how to use the logit tranformations
##
myp <- data.frame(par1=runif(1000,-1,1),par2=rnorm(1000),par3=runif(1000,0,2))
mys <- myp+rnorm(1000,sd=.1)
myt <- c(0,0,1.5)
lin2 <- abc(target=myt, param=myp, sumstat=mys, tol=.1, method =
"loclinear", transf=c("logit","none","logit"),logit.bounds = rbind(c(-1,
1), c(NA, NA), c(0, 2)))
summary(lin2)
}
\keyword{htest}
\keyword{models}
\keyword{neural}
% Converted by Sd2Rd version 1.15.
