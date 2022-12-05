\name{plot.cv4abc}
\alias{plot.cv4abc}
\title{
  Cross-validation plots for ABC
}
\description{
  Plotting method for cross-validation ABC objects. Helps to visually
  evaluate the quality of the estimation and/or the effect of the
  tolerance level.
}
\usage{
\method{plot}{cv4abc}(x, exclude = NULL, log = NULL, file = NULL,
postscript = FALSE, onefile = TRUE, ask =
!is.null(deviceIsInteractive()), caption = NULL, \dots)
}
\arguments{
  \item{x}{
    an object of class \code{"cv4abc"}.}
  \item{exclude}{
    a vector of row indices indicating which rows should be excluded from
    plotting. Useful when the prior distribution has a long tail.}
  \item{log}{
    character vector of the same length as the number of parameters in
    the \code{"cv4abc"} object. Allows plotting on a log scale. Possible
    values are "" (normal scale) and "xy" (log scale for both the x and
    y axis). "x" and "y" are possible as well, but not of any interest
    here. Negative values are set to \code{NA} and there is a warning.}
  \item{file}{
    a character string giving the name of the file. See
    \code{\link{postscript}} for details on accepted file names. If
    \code{NULL} (the default) plots are printed to the null device
    (e.g. \code{\link{X11}}). If not \code{NULL} plots are printed on a
    \code{\link{pdf}} device. See also \code{postscript}.}
  \item{postscript}{
    logical; if \code{FALSE} (default) plots are
    printed on a \code{\link{pdf}} device, if \code{TRUE} on a
    \code{postscript} device.}
  \item{onefile}{
    logical, if \code{TRUE} (the default) allow multiple
    figures in one file.  If \code{FALSE}, generate a file name
    containing the page number for each page. See
    \code{\link{postscript}} for further details.}
  \item{ask}{
    logical; if \code{TRUE} (the default), the user is asked
    before each plot, see \code{par(ask=.)}.}
  \item{caption}{
    captions to appear above the plot(s); \code{character}
    vector of valid graphics annotations, see
    \code{\link{as.graphicsAnnot}}. By default, \code{parnames} from
    \code{x} are extracted (see \code{\link{abc}}). Can be set to
    \code{""} or \code{NA} to suppress all captions.}
  \item{\dots}{
    other parameters passed to \code{plot}.}
}
\details{
  Different tolerance levels are plotted with
  \code{\link{heat.colors}}. Smaller the tolerance levels correspond
  to "more red" points.
}
\seealso{
  \code{\link{cv4abc}}, \code{\link{abc}}
}
\examples{
## see ?cv4abc for examples
}
\keyword{hplot}
% Converted by Sd2Rd version 1.15.
