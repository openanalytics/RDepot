\name{cv4postpr}
\alias{cv4postpr}
\title{
  Leave-one-our cross validation for model selection ABC
}
\description{
 This function performs a leave-one-out cross validation for model
 selection with ABC via subsequent calls to the function
 \code{\link{postpr}}. 
}
\usage{cv4postpr(index, sumstat, postpr.out = NULL, nval, tols, method,
subset = NULL, kernel = "epanechnikov", numnet = 10, sizenet = 5, lambda
= c(0.0001,0.001,0.01), trace = FALSE, maxit = 500, \dots)
}
\arguments{
  \item{index}{ a vector of model indices. It can be character or
    numeric and will be coerced to factor. It must have the same length
    as the number of rows in \code{sumstat} to indicate which row of
    \code{sumstat} belong to which model.}
  \item{sumstat}{
    a vector, matrix or data frame of the simulated summary statistics.}
  \item{postpr.out}{
    an object of class \code{"postpr"}, optional. If supplied, all arguments
    passed to \code{\link{postpr}} are extracted from this object,
    except for \code{sumstat}, \code{index}, and \code{tols}, which
    always have to be supplied as arguments.}
  \item{nval}{
    the size of the cross-validation sample for each model.}
  \item{tols}{
    a single tolerance rate or a vector of tolerance rates.}
  \item{method}{
    a character string indicating the type of simulation required.
    Possible values are \code{"rejection"}, \code{"mnlogistic"},
    \code{"neuralnet"}. See \code{\link{postpr}} for details.}
  \item{subset}{
    a logical expression indicating elements or rows to keep. Missing
    values in \code{index} and/or \code{sumstat} are taken as
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
  For each model, a simulation is selected repeatedly to be a validation
  simulation, while the other simulations are used as training
  simulations. Each time the function \code{\link{postpr}} is called to
  estimate the parameter(s).

  Ideally, we want \code{nval} to be equal to the number of simulations
  for each model, however, this might take too much time. Users are
  warned not to choose a too large number of simulations (especially
  when the neural networks are used). Beware that the actual number of
  cross-validation estimation steps that need to be performed is
  \code{nval}*the number of models.
  
  The arguments for the function \code{\link{postpr}} can be supplied in
  two ways. First, simply give them as arguments when calling this
  function, in which case \code{postpr.out} can be \code{NULL}. Second,
  via an existing object of class \code{"postpr"}, here
  \code{postpr.out}. WARNING: when \code{postpr.out} is supplied, the
  same \code{sumstat} and \code{param} objects have to be used as in the
  original call to \code{\link{postpr}}. Column names of \code{sumstat}
  and \code{param} are checked for match.

  See \code{\link{summary.cv4postpr}} for calculating the prediction
  error from an object of class \code{"cv4postpr"} and
  \code{\link{plot.cv4postpr}} for visualizing the misclassification of
  the models using barplots.
  
}
\value{
  An object of class \code{"cv4postpr"}, which is a list with the following
  elements
  \item{call}{The original calls to \code{\link{postpr}} for each tolerance
    rates.}
  \item{cvsamples}{Numeric vector of length \code{nval}*the number of
    models, indicating which rows of \code{sumstat} were used as
    validation values.}
  \item{tols}{The tolerance rates.}
  \item{true}{The true models.}
  \item{estim}{The estimated model probabilities.}
  \item{method}{The method used.}
  \item{names}{A list of two elements: \code{model} contains the model names,
  and \code{statistics.names} the names of the summary statistics.}
  \item{seed}{The value of \code{.Random.seed} when \code{cv4postpr} is
    called.}
}
\seealso{
  \code{\link{postpr}}, \code{\link{summary.cv4postpr}}, \code{\link{plot.cv4postpr}}
}
\examples{
data(human)
africa <- postpr(tajima.obs["Hausa",], models, tajima.sim, tol=.1,
method="mnlogistic")
summary(africa)
cv.africa <- cv4postpr(models, tajima.sim, postpr.out=africa, nval=20,
tols=c(.01, .02))
summary(cv.africa)
class(cv.africa)
plot(cv.africa, names.arg=c("Bottleneck", "Constant", "Exponential"))
}
\keyword{models}
\keyword{htest}
% Converted by Sd2Rd version 1.15.
