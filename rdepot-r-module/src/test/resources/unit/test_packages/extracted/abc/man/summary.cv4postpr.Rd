\name{summary.cv4postpr}
\alias{summary.cv4postpr}
\title{
  Confusion matrix and misclassification probabilities of models
}
\description{
  This function calculates the confusion matrix and the mean
  misclassification probabilities of models from an object of class
  \code{"cv4postpr"}.
}
\usage{
\method{summary}{cv4postpr}(object, probs = TRUE, print = TRUE, digits =
max(3, getOption("digits")-3), \dots)
}
\arguments{
  \item{object}{
    an object of class \code{"cv4postpr"}.}
  \item{probs}{
    logical, if \code{TRUE} (default), mean posterior model
    probabilities are returned.}
  \item{print}{
    logical, if \code{TRUE} prints the mean models probabilities.}
  \item{digits}{
    the digits to be rounded to.}
  \item{\dots}{
    other arguments.}
}
\value{  
  If \code{probs}=\code{FALSE} a matrix with the frequencies of the
  simulations classified to the different models (the confusion
  matrix). If \code{probs}=\code{TRUE}, a list with two components:
  \item{conf.matrix}{The confusion matrix.}
  \item{probs}{The mean model misclassification probabilities.}
}
\seealso{
  \code{\link{cv4postpr}}, \code{\link{plot.cv4postpr}}
}
\examples{
## see ?cv4postpr for examples
}
\keyword{models}
\keyword{htest}
% Converted by Sd2Rd version 1.15.
