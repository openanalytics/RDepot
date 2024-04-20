\name{summary.postpr}
\alias{summary.postpr}
\title{
  Posterior model probabilities and Bayes factors
}
\description{
  This function extracts the posterior model probabilities and
  calculates the Bayes factors from an object of class \code{"postpr"}.
}
\usage{
\method{summary}{postpr}(object, rejection = TRUE, print = TRUE, digits
= max(3, getOption("digits")-3), \dots)
}
\arguments{
  \item{object}{
    an object of class \code{"postpr"}.}
  \item{rejection}{
    logical, if method is \code{"mnlogistic"} or \code{"neuralnet"},
    should the approximate model probabilities based on the rejection
    method returned.}
  \item{print}{
    logical, if \code{TRUE} prints the mean models probabilities.}
  \item{digits}{
    the digits to be rounded to.}
  \item{\dots}{
      other arguments.}
}
\value{
  A list with the following components if method=\code{"rejection"}:
  \item{Prob}{an object of class \code{table} of the posterior model
    probabilities.}
  \item{BayesF}{an object of class \code{table} with the Bayes factors
    between pairs of models.}
  
  A list with the following components if method is \code{"mnlogistic"}
  or \code{"neuralnet"} and rejection is \code{TRUE}:
  \item{rejection}{a list with the same components as above}
  \item{mnlogistic}{a list with the same components as above}
}
\seealso{
  \code{\link{postpr}}
}
\examples{
## see ?postpr for examples
}
\keyword{htest}
\keyword{models}
% Converted by Sd2Rd version 1.15.
