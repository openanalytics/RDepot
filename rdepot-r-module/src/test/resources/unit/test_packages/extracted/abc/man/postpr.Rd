\name{postpr}
\alias{postpr}
\title{
  Estimating posterior model probabilities
}
\description{
 Model selection with Approximate Bayesian Computation (ABC).
}
\usage{
postpr(target, index, sumstat, tol, subset = NULL, method, corr=TRUE,
kernel="epanechnikov", numnet = 10, sizenet = 5, lambda =
c(0.0001,0.001,0.01), trace = TRUE, maxit = 500, ...)
}
\arguments{
  \item{target}{
    a vector of the observed summary statistics.}
  \item{index}{
    a vector of model indices. It can be character or numeric and
    will be coerced to factor. It must have the same length as
    \code{sumstat} to indicate which row of \code{sumstat} belong to
    which model.}
  \item{sumstat}{
    a vector, matrix or data frame of the simulated summary statistics.}
  \item{tol}{
     numeric, the required proportion of points nearest the target
     values (tolerance), or a vector of the desired tolerance values. If
     a vector is given}
  \item{subset}{
    a logical expression indicating elements or rows to keep. Missing
    values in \code{index} and/or \code{sumstat} are taken as \code{FALSE}.}
   \item{method}{
     a character string indicating the type of simulation required.
    Possible values are \code{"rejection"}, \code{"mnlogistic"},
    \code{"neuralnet"}. See \code{Details}.}
  \item{corr}{
    logical, if \code{TRUE} (default) posterior model probabilities are corrected
    for the number of simulations performed for each model. If equal
    number of simulations are available for all models, \code{corr} has
    no effect.}
  \item{kernel}{
    a character string specifying the kernel to be used when method is
    \code{"mnlogistic"} or \code{"neuralnet"}. Defaults to
    "epanechnikov". See \code{\link{density}} for details.}
  \item{numnet}{
    the number of neural networks when method is \code{"neuralnet"}. It
    corresponds to the number of times the function \code{nnet} is called.}
  \item{sizenet}{
    the number of units in the hidden layer. Can be zero if there are
    no skip-layer units.}
  \item{lambda}{
    a numeric vector or a single value indicating the weight decay when
    method is \code{"neuralnet"}. By default, 0.0001, 0.001, or 0.01 is
    randomly chosen for the each of the networks. See \code{\link{nnet}}
    for more details.}
  \item{trace}{
    logical, \code{TRUE} switches on tracing the
    optimization of \code{\link{nnet}} (applies when method is
    \code{"neuralnet"}).}
  \item{maxit}{
    numeric, the maximum number of iterations. Defaults to 500. See also
    \code{\link{nnet}}.}
  \item{\dots}{
    other arguments passed on from \code{nnet}.}
}
\value{
  An object of \code{class} \code{"postpr"}, containing the following
  components:

  \item{pred}{a vector of model probabilities when method is
  \code{"mnlogistic"} or \code{"neuralnet"}.}
  \item{values}{the vector of model indices in the accepted region using
    the rejection method.}
  \item{weights}{vector of regression weights when method is
  \code{"mnlogistic"} or \code{"neuralnet"}.}
  \item{ss}{summary statistics in the accepted region.}
  \item{call}{the original call.}
  \item{na.action}{a logical vector indicating the elements or rows that
    were excluded, including both \code{NA}/\code{NaN}'s and
    elements/rows selected by \code{subset}}
  \item{method}{a character string indicating the method used, i.e.
    \code{"rejection"}, \code{"mnlogistic"} or \code{"neuralnet"}.}
  \item{corr}{logical, if \code{TRUE} the posterior model probabilities
    are corrected for the number of simulations performed for each
    model.}
  \item{nmodels}{the number of simulations performed for each model a
    priori.}
  \item{models}{
    a character vector of model names (a priori).}
  \item{numstat}{the number of summary statistics used.}
  \item{names}{a list of two elements: \code{model} contains the model names,
    and \code{statistics.names} the names of the summary statistics.}
}
\details{
  The function computes the posterior model probabilities.  Simulations
  have to be performed with at least two distinct models. When method is
  \code{"rejection"}, the posterior probability of a given model is
  approximated by the proportion of accepted simulations given this
  model. This approximation holds when the different models are a priori
  equally likely, and the same number of simulations is performed for
  each model. When method is \code{"mnlogistic"} the posterior model
  probabilities are estimated using a multinomial logistic regression as
  implemented in the function \code{\link{multinom}} from the package
  \code{\link{nnet}}. When method is \code{"neuralnet"}, neural networks
  are used to predict the probabilities of models based on the observed
  statistics using \code{\link{nnet}}. This method can be useful if many
  summary statistics are used.

  Names for the summary statistics are strongly recommended. Names can
  be supplied as colnames to \code{sumstat} (and target). If no names are
  supplied S1, S2, \dots to summary statistics will be assigned to
  parameters and the user will be warned.

}
\references{

  Beaumont, M.A. (2008) Joint determination of topology, divergence
  time, and immigration in population trees. In \emph{Simulation,
  Genetics, and Human Prehistory} (Matsumura, S., Forster, P. and
  Renfrew, C., eds) McDonald Institute for Archaeological Research

}
\author{
  Katalin Csillery, Olivier Francois and Michael Blum with some
  initial code from Mark Beaumont
  (\url{http://www.rubic.rdg.ac.uk/~mab/}).
}
\seealso{
  \code{\link{summary.postpr}}
}
\examples{
data(human)
## five R objects are loaded. See ?human for details.

## the two summary statistics: mean and variance of Tajima's D over 50
## loci
par(mfcol = c(1,2))
boxplot(tajima.sim[,1]~models, main=names(tajima.sim)[1])
boxplot(tajima.sim[,2]~models, main=names(tajima.sim)[2])

## model selection with ABC for the three populations, representing
## three continents

## in Africa, population expansion is the most supported model
africa <- postpr(tajima.obs["Hausa",], models, tajima.sim, tol=.01,
method="mnlogistic")
summary(africa)

## in Europe and Asia, population bottleneck is the most supported model
europe <- postpr(tajima.obs["Italian",], models, tajima.sim, tol=.01,
method="mnlogistic")
summary(europe)
asia <- postpr(tajima.obs["Chinese",], models, tajima.sim, tol=.01,
method="mnlogistic")
summary(asia)

ss <- cbind(runif(1000),rt(1000,df=20))
postpr(target=c(3), index=c(rep("norm",500),rep("t",500)),
sumstat=ss[,1], tol=.1, method="rejection")

}
\keyword{htest}
\keyword{models}
% Converted by Sd2Rd version 1.15.
