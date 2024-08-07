\name{summary.abc}
\alias{summary.abc}
\alias{getmode}
\title{
  Summaries of posterior samples generated by ABC algortithms
}
\description{
  Calculates simple summaries of posterior samples: the minimum and
  maximum, the weighted mean, median, mode, and credible intervals.
}
\usage{
\method{summary}{abc}(object, unadj = FALSE, intvl = .95, print = TRUE,
digits = max(3, getOption("digits")-3), \dots)
}
\arguments{
  \item{object}{
    an object of class \code{"abc"}.}
  \item{unadj}{
    logical, if TRUE it forces to plot the unadjusted values when \code{method}
    is \code{"loclinear"} or \code{"neuralnet"}.}
  \item{intvl}{
    size of the symmetric credible interval.}
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
  If method is \code{"rejection"} in the original call to
  \code{\link{abc}}, posterior means, medians, modes and percentiles
  defined by \code{intvl}, 95 by default (credible intervals) are
  calculated. If a regression correction was used (i.e. method is
  \code{"loclinear"} or \code{"neuralnet"} in the original call to
  \code{\link{abc}}) the weighted posterior means, medians, modes and
  percentiles are calculated.

  To calculate the mode, parameters are passed on from
  \code{\link{density.default}}. Note that the posterior mode can be
  rather different depending on the parameters to estimate the
  density.
}
\value{
  The returned value is an object of class \code{"table"}. The rows are,
  \item{Min.}{minimun}
  \item{Lower perc.}{lower percentile}
  \item{Median}{or weighted median}
  \item{Mean}{or weighted mean}
  \item{Mode}{or weighted mode}
  \item{Upper perc.}{upper percentile}
  \item{Max.}{maximum}
}
\seealso{
  \code{\link{abc}},  \code{\link{hist.abc}}, \code{\link{plot.abc}}
}
\examples{
## see ?abc for examples
}
\keyword{htest}
\keyword{models}
% Converted by Sd2Rd version 1.15.
