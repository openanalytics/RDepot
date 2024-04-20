\name{cv4abc}
\alias{cv4abc}
\title{
  Cross validation for Approximate Bayesian Computation (ABC)
}
\description{
 
 This function performs a leave-one-out cross validation for ABC via
 subsequent calls to the function \code{\link{abc}}. A potential use of
 this function is to evaluate the effect of the choice of the tolerance
 rate on the quality of the estimation with ABC.
 
}
\usage{
cv4abc(param, sumstat, abc.out = NULL, nval, tols, statistic = "median",
prior.range = NULL, method, hcorr = TRUE, transf = "none", logit.bounds
= c(0,0), subset = NULL, kernel = "epanechnikov", numnet = 10, sizenet =
5, lambda = c(0.0001,0.001,0.01), trace = FALSE, maxit = 500, \dots)
}
\arguments{
  \item{param}{
    a vector, matrix or data frame of the simulated parameter values.}
  \item{sumstat}{
    a vector, matrix or data frame of the simulated summary statistics.}
  \item{abc.out}{
    an object of class \code{"abc"}, optional. If supplied, all arguments
    passed to \code{\link{abc}} are extracted from this object,
    except for \code{sumstat}, \code{param}, and \code{tol}, which
    always have to be supplied as arguments.}
  \item{nval}{
    size of the cross-validation sample.}
  \item{tols}{
    a single tolerance rate or a vector of tolerance rates.}
  \item{statistic}{
    a character string specifying the statistic to calculate a point
    estimate from the posterior distribution of the
    parameter(s). Possible values are \code{"median"} (default),
    \code{"mean"}, or \code{"mode"}.}
  \item{prior.range}{
    a range to truncate the prior range.}
  \item{method}{
    a character string indicating the type of ABC algorithm to be
    applied.  Possible values are \code{"rejection"},
    \code{"loclinear"}, and \code{"neuralnet"}. See also
    \code{\link{abc}}.}
  \item{hcorr}{
    logical, if \code{TRUE} (default) the conditional heteroscedastic
    model is applied.}
  \item{transf}{
    a vector of character strings indicating the kind of transformation
    to be applied to the parameter values. The possible values are
    \code{"log"}, \code{"logit"}, and \code{"none"} (default), when no
    is transformation applied. See also \code{\link{abc}}.}
  \item{logit.bounds}{
    a vector of bounds if \code{transf} is \code{"logit"}. These bounds
    are applied to all parameters that are to be logit transformed.}
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
    the function \code{nnet} is called.}
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
    logical, \code{TRUE} switches on tracing the optimization of
    \code{\link{nnet}}. Applies only when \code{method} is
    \code{"neuralnet"}.}
  \item{maxit}{
    numeric, the maximum number of iterations. Defaults to 500. Applies
    only when \code{method} is \code{"neuralnet"}. See also
    \code{\link{nnet}}.}
  \item{\dots}{
    other arguments passed to \code{\link{nnet}}.}
}
\details{  
  
  A simulation is selected repeatedly to be a validation simulation,
  while the other simulations are used as training simulations. Each
  time the function \code{\link{abc}} is called to estimate the
  parameter(s). A total of \code{nval} validation simulations are
  selected.
  
  The arguments of the function \code{\link{abc}} can be supplied in two
  ways. First, simply give them as arguments when calling this function,
  in which case \code{abc.out} can be \code{NULL}. Second, via an
  existing object of class \code{"abc"}, here \code{abc.out}. WARNING:
  when \code{abc.out} is supplied, the same \code{sumstat} and
  \code{param} objects have to be used as in the original call to
  \code{\link{abc}}. Column names of \code{sumstat} and \code{param} are
  checked for match.

  See \code{\link{summary.cv4abc}} for calculating the prediction error
  from an object of class \code{"cv4abc"}.
  
}
\value{
  An object of class \code{"cv4abc"}, which is a list with the following
  elements
  \item{call}{The original calls to \code{\link{abc}} for each tolerance
    rates.}
  \item{cvsamples}{Numeric vector of length \code{nval}, indicating
    which rows of the \code{param} and \code{sumstat} matrices were used
    as validation values.}
  \item{tols}{The tolerance rates.}
  \item{true}{The parameter values that served as validation values.}
  \item{estim}{The estimated parameter values.}     
  \item{names}{A list with two elements: \code{parameter.names} and
  \code{statistics.names}. Both contain a vector of character strings
    with the parameter and statistics names, respectively.}
  \item{seed}{The value of \code{.Random.seed} when \code{cv4abc} is
    called.}
  
}
\seealso{
  \code{\link{abc}}, \code{\link{plot.cv4abc}}, \code{\link{summary.cv4abc}}
}
\examples{
data(musigma2)
## this data set contains five R objects, see ?musigma2 for
## details 

## cv4abc() calls abc(). Here we show two ways for the supplying
## arguments of abc(). 1st way: passing arguments directly. In this
## example only 'param', 'sumstat', 'tol', and 'method', while default
## values are used for the other arguments.
##
cv.rej <- cv4abc(param=par.sim, sumstat=stat.sim, nval=50,
tols=c(.1,.2,.3), method="rejection")

## 2nd way: first creating an object of class 'abc', and then using it
## to pass its arguments to abc().
##
lin <- abc(target=stat.obs, param=par.sim, sumstat=stat.sim, tol=.2,
method="loclinear", transf=c("none","log"))
cv.lin <- cv4abc(param=par.sim, sumstat=stat.sim, abc.out=lin, nval=50,
tols=c(.1,.2,.3))

## using the plot method. Different tolerance levels are plotted with
## different heat.colors. Smaller the tolerance levels correspond to
## "more red" points.
## !!! consider using the argument 'exclude' (plot.cv4abc) to supress
## the plotting of any outliers that mask readibility !!!
plot(cv.lin, log=c("xy", "xy"), caption=c(expression(mu),
expression(sigma^2)))

## comparing with the rejection sampling
plot(cv.rej, log=c("", "xy"), caption=c(expression(mu), expression(sigma^2)))

## or printing results directly to a postscript file...
plot(cv.lin, log=c("xy", "xy"), caption=c(expression(mu),
expression(sigma^2)), file="CVrej", postscript=TRUE)

## using the summary method to calculate the prediction error
summary(cv.lin)
## compare with rejection sampling
summary(cv.rej)
}
\keyword{htest}
\keyword{models}
% Converted by Sd2Rd version 1.15.
