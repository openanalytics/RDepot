\name{plot.abc}
\alias{plot.abc}
\title{
  Diagnostic plots for ABC
}
\description{
    A plotting utile for quick visualization of the quality of an ABC
  analysis from an object of class \code{"abc"} generated with methods
  \code{"loclinear"} or \code{"neuralnet"} (see \code{\link{abc}} for
  details). Four plots are currently available: a density plot of the
  prior distribution, a density plot of the posterior distribution, a
  scatter plot of the Euclidean distances as a function of the parameter
  values, and a Normal Q-Q plot of the residuals from the
  regression.
}
\usage{
\method{plot}{abc}(x, param, subsample = 1000, true = NULL, file = NULL,
postscript = FALSE, onefile = TRUE, ask =
!is.null(deviceIsInteractive()), \dots)
}
\arguments{
  \item{x}{
    an object of class \code{"abc"} generated with methods
    \code{"loclinear"} or \code{"neuralnet"} (see \code{\link{abc}} for
    details).}
  \item{param}{
    a vector or matrix of parameter values from the simulations that
    were used in the original call to \code{\link{abc}}.}
  \item{subsample}{
    the number of rows (simulations) to be plotted. Rows are randomly
    selected from \code{param}. 
  }
  \item{true}{
    a vector of true parameter values, if known. Vertical lines are
    drawn at these values.}
  \item{file}{
    a character string giving the name of the file. See
    \code{\link{postscript}} for details on accepted file names. If
    \code{NULL} (the default) plots are printed to the null device
    (e.g. \code{\link{X11}}). If not \code{NULL} plots are printed on a
    \code{\link{pdf}} device. See also \code{\link{postscript}}.}
  \item{postscript}{
    logical; if \code{FALSE} (default) plots are printed on a
    \code{\link{pdf}} device, if \code{TRUE} on a \code{postscript} device.}
  \item{onefile}{
    logical, if \code{TRUE} (the default) allow multiple figures in one
    file.  If \code{FALSE}, generate a file name containing the page
    number for each page. See \code{\link{postscript}} for further details.} 
  \item{ask}{
    logical; if \code{TRUE} (the default), the user is asked before each plot, see
    \code{par(ask=.)}.}
  \item{\dots}{
    other parameters passed to \code{plot}.}
}
\details{
  
  In order to use this function, one of the regression correction
  methods had to be used in the original call to \code{\link{abc}},
  i.e. \code{"loclinear"} or \code{"neuralnet"} (see \code{\link{abc}}
  for details). Four plots are printed for each parameter. (i) A density
  plot of the prior distribution. (ii) A density plot of the posterior
  distribution using the regression correction (red thick lines) and,
  for reference, using the simple rejection method (black fine
  lines). The prior distribution (in the posterior distributions' range)
  is also displayed (dashed lines). (iii) A scatter plot of the log
  Euclidean distances as a function of the true parameter values. Points
  corresponding to the accepted simulations are displayed in red. (iv) A
  Normal Q-Q plot of the residuals from the regression, thus from
  \code{\link{lsfit}} when method was \code{"loclinear"}, and from
  \code{\link{nnet}} when method was \code{"neuralnet"} in the original
  \code{\link{abc}}.

  For plots (i) and (iii) not the whole data but a subsample is used,
  the size of which can be is given by \code{subsample}. This is to
  avoid plots that may take too much time to print.

  If a parameter transformation was applied in the original call to
  \code{\link{abc}}, the same transformations are applied to the
  parameters for plotting (on plots (i)-(iii)).
}
\seealso{
  \code{\link{abc}}, \code{\link{hist.abc}}, \code{\link{summary.abc}}
}
\examples{
## see ?abc for examples
}
\keyword{hplot}
% Converted by Sd2Rd version 1.15.