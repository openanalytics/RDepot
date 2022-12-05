\name{hist.abc}
\alias{hist.abc}
\title{
  Posterior histograms
}
\description{
  Histograms of posterior samples from objects of class \code{"abc"}.
}
\usage{
\method{hist}{abc}(x, unadj = FALSE, true = NULL, file = NULL,
postscript = FALSE, onefile = TRUE, ask =
!is.null(deviceIsInteractive()), col.hist = "grey", col.true = "red",
caption = NULL, \dots)
}
\arguments{
  \item{x}{
    an object of class \code{"abc"}.}
  \item{unadj}{
    logical, if \code{TRUE} the unadjusted values are plotted even
    if method is \code{"loclinear"} or \code{"neuralnet"}.}
  \item{true}{
    the true parameter value(s), if known. Vertical bar(s) are drawn at
    the true value(s). If more than one parameters were estimated, a
    vector of the true values have to be supplied.}
  \item{file}{
    a character string giving the name of the file. See
    \code{\link{postscript}} for details on accepted file names. If
    \code{NULL} (the default) histograms are printed to the null device
    (e.g. \code{\link{X11}}). If not \code{NULL} histograms are printed
    on a \code{\link{pdf}} device. See also \code{\link{postscript}}.}
  \item{postscript}{
    logical; if \code{FALSE} (default) histograms are printed on a
    \code{\link{pdf}} device, if \code{TRUE} on a \code{postscript} device.}
  \item{onefile}{
    logical, if \code{TRUE} (the default) allow multiple figures in one
    file.  If \code{FALSE}, generate a file name containing the page
    number for each page. See \code{\link{postscript}} for further details.} 
  \item{ask}{
    
    logical; if \code{TRUE} (the default), the user is asked before each
    plot, see \code{par(ask=.)}.}

  \item{col.hist}{
    the colour of the histograms.}
  \item{col.true}{
    the colour of the vertical bar at the true value.}
  \item{caption}{
    captions to appear above the histogram(s); \code{character} vector
    of valid graphics annotations, see \code{\link{as.graphicsAnnot}}
    for details. When \code{NULL} (default), \code{parnames} are used,
    which are extracted from \code{x} (see \code{\link{abc}}). Can be
    set to \code{NA} to suppress all captions.}
  \item{\dots}{
    other parameters passed to \code{hist}.}
}
\value{
  
  A list of length equal to the number of parameters, the elements of
  which are objects of class \code{"histogram"}. See \code{\link{hist}}
  for details.}

\seealso{
  \code{\link{abc}}, \code{\link{plot.abc}}
}
\examples{
## see ?abc for examples 
}
\keyword{hplot}
% Converted by Sd2Rd version 1.15.
