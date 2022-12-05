\name{musigma2}
\alias{par.sim}
\alias{stat.sim}
\alias{stat.obs}
\alias{post.mu}
\alias{post.sigma2}
\title{
A set of objects used to estimate the population mean and variance in a
Gaussian model with ABC.
}
\description{
  \code{musigma2} loads in five R objects: \code{par.sim} is a data
  frame and contains the parameter values of the simulated data sets,
  \code{stat} is a data frame and contains the simulated summary
  statistics, \code{stat.obs} is a data frame and contains the observed
  summary statistics, \code{post.mu} and \code{post.sigma2} are data
  frames and contain the true posterior distributions for the two
  parameters of interest, \eqn{\mu} and \eqn{\sigma^2}, respectively.
}
\usage{data(musigma2)}
\format{
  The \code{par.sim} data frame contains the following columns:

  \describe{
    \item{\code{mu}}{
      The population mean.
    }
    \item{\code{sigma2}}{
      The population variance.
    }
  }

  The \code{stat.sim} and \code{stat.obs} data frames contain the
  following columns:
  \describe{
    \item{\code{mean}}{
      The sample mean.
    }
    \item{\code{var}}{
      The logarithm of the sample variance.
    }
  }

  The \code{post.mu} and \code{post.sigma2} data frames contain the
  following columns:
  \describe{
    \item{\code{x}}{
      the coordinates of the points where the density is estimated.
    }
    \item{\code{y}}{
      the posterior density values.
    }
  }
}
\details{
  The prior of \eqn{\sigma^2} is an inverse \eqn{\chi^2} distribution
  with one degree of freedom. The prior of \eqn{\mu} is a normal
  distribution with variance of \eqn{\sigma^2}. For this simple example,
  the closed form of the posterior distribution is available.
  }
\source{
  The observed statistics are the mean and variance of the sepal of
  \emph{Iris setosa}, estimated from part of the \code{iris} data.
  
  The data were collected by Anderson, Edgar.
}
\references{
  Anderson, E. (1935). The irises of the Gaspe Peninsula, \emph{Bulletin
  of the American Iris Society}, \bold{59}, 2-5.
}
\seealso{
  \code{\link{abc}}, \code{\link{cv4abc}}
}
\keyword{datasets}
% Converted by Sd2Rd version 1.15.
