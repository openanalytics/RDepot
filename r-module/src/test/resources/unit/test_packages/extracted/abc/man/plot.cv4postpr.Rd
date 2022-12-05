\name{plot.cv4postpr}
\alias{plot.cv4postpr}
\title{
  Barplot of model misclassification
}
\description{
  Displays a barplot of either the proportion of simulations classified
  to any of the models or the mean misclassification probabilities of
  models for all tolerance levels in the \code{"cv4postpr"} object.
}
\usage{
\method{plot}{cv4postpr}(x, probs = FALSE, file = NULL, postscript
= FALSE, onefile = TRUE, ask = !is.null(deviceIsInteractive()), caption
= NULL, \dots)
}
\arguments{
  \item{x}{
    an object of class \code{"cv4postpr"}.}
  \item{probs}{ logical, if \code{TRUE} the mean posterior model
    probabilities are plotted. If \code{FALSE} the frequencies of the
    simulations classified to the different models (default).}
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
    \code{\link{as.graphicsAnnot}}. Can be set to
    \code{""} or \code{NA} to suppress all captions.}
  \item{\dots}{
    other parameters passed to \code{barplot}.}
}
\details{
  Model are distinguised with different intensities of the gray
  colour. The first model in alphabetic order has the darkest colour. If
  the classification of models is perfect (so that the frequency (or
  probability) of each model is zero for all but the correct model) each
  bar has a single colour of its corresponding model.
}
\seealso{
  \code{\link{cv4postpr}}, \code{\link{summary.cv4postpr}}
}
\examples{
## see ?cv4postpr for examples
}
\keyword{hplot}
% Converted by Sd2Rd version 1.15.
