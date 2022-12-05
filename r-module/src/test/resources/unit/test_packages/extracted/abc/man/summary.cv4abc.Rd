\name{summary.cv4abc}
\alias{summary.cv4abc}
\title{
  Calculates the cross-validation prediction error
}
\description{
This function calculates the prediction error from an object of class
\code{"cv4abc"} for each parameter and tolerance level.
}
\usage{
\method{summary}{cv4abc}(object, print = TRUE, digits = max(3,
getOption("digits")-3), \dots)
}
\arguments{
  \item{object}{
    an object of class \code{"abc"}.}
  \item{print}{
    logical, if \code{TRUE} prints messages. Mainly for internal use.}
  \item{digits}{
    the digits to be rounded to. Can be a vector of the same length as the
    number of parameters, when each parameter is rounded to its
    corresponding digits.}
  \item{\dots}{
    other arguments passed to \code{density}.}
}
\details{
  The prediction error is calculated as
  \eqn{\frac{\sum((\theta^{*}-\theta)^2)}{Var(\theta^{*})}}, where
  \eqn{\theta} is the true parameter value and \eqn{\theta^{*}} is the
  estimated parameter value.    
}
\value{  
  The returned value is an object of class \code{"table"}, where the
  columns correspond to the parameters and the rows to the different
  tolerance levels.
}
\seealso{
  \code{\link{cv4abc}}, \code{\link{plot.cv4abc}}
}
\examples{
## see ?cv4abc for examples
}
\keyword{models}
% Converted by Sd2Rd version 1.15.
