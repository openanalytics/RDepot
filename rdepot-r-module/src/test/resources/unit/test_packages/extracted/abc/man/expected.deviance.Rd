\name{expected.deviance}
\alias{expected.deviance}
\title{
  Expected deviance
}
\description{
  Model selection criterion based on posterior predictive distributions
  and approximations of the expected deviance.
}
\usage{
expected.deviance(target, postsumstat, kernel = "gaussian", subset=NULL,
print=TRUE)
}
\arguments{
  \item{target}{  
    a vector of the observed summary statistics.}
  \item{postsumstat}{
    a vector, matrix or data frame of summary statistics simulated
    a posteriori.}
  \item{kernel}{
    a character string specifying the kernel to be used when. Defaults
    to \code{"gaussian"}. See \code{\link{density}} for details.}
  \item{subset}{ a logical expression indicating elements or rows to
    keep. Missing values in \code{postsumstat} are taken as
    \code{FALSE}.}
  \item{print}{    
    prints out what percent of the distances have been zero.}
}
\details{
  This function implements an approximation for the expected deviance
  based on simulation performed a posteriori. Thus, after the posterior
  distribution of parameters or the posterior model probabilities have
  been determined, users need to re-simulate data using the posterior.
  The Monte-Carlo estimate of the expected deviance is computed from the
  simulated data as follows:
  \eqn{D=-\frac{2}{n}\sum_{j=1}^{n}\log(K_\epsilon(\parallel
  s^j-s_0\parallel))}, where n is number of simulations, \eqn{K} is the
  statistical kernel, \eqn{\epsilon} is the error, i.e. difference
  between the observed and simulated summary statistics below which
  simualtions were accepted in the original call to
  \code{\link{postpr}}, the \eqn{s^j}'s are the summary statistics
  obtained from the posterior predictive simualtions, and \eqn{s_0} are
  the observed values of the summary statistics.  The expected devaince
  averaged over the posterior distribution to compute a deviance
  information criterion (DIC).
}
\references{
  Francois O, Laval G (2011) Deviance information criteria for model
  selection in approximate Bayesian computation
  \emph{arXiv}:\bold{0240377}.
}
\value{
  A list with the following components:
  \item{expected.deviance}{The approximate expected deviance.}
  \item{dist}{The Euclidean distances for summary statistics simulated a
    posteriori.}
}
\examples{
## Function definitions
skewness <- function(x) {
sk <- mean((x-mean(x))^3)/(sd(x)^3)
return(sk)
}
kurtosis <- function(x) {  
k <- mean((x-mean(x))^4)/(sd(x)^4) - 3  
return(k)
}

## Observed summary statistics
obs.sumstat <- c(2.004821, 3.110915, -0.7831861, 0.1440266) 

## Model 1 (Gaussian)
## ##################
## Simulate data
theta <- rnorm(10000, 2, 10)
zeta <- 1/rexp(10000, 1)
param <- cbind(theta, zeta)
y <- matrix(rnorm(200000, rep(theta, each = 20), sd = rep(sqrt(zeta),
each = 20)), nrow = 20, ncol = 10000)

## Calculate summary statistics
s <- cbind(apply(y, 2, mean), apply(y, 2, sd), apply(y, 2, skewness),
apply(y, 2, kurtosis))

## ABC inference
gaus <- abc(target=obs.sumstat, param = param, sumstat=s, tol=.1, hcorr =
FALSE, method = "loclinear")
param.post <- gaus$adj.values

## Posterior predictive simulations
postpred.gaus <- matrix(rnorm(20000, rep(param.post[,1], each = 20), sd
= rep(sqrt(param.post[,2]), each = 20)), nrow = 20, ncol = 1000)
statpost.gaus <- cbind(apply(postpred.gaus, 2,
mean),apply(postpred.gaus, 2, sd),apply(postpred.gaus,
2,skewness),apply(postpred.gaus, 2,kurtosis))

# Computation of the expected deviance
expected.deviance(obs.sumstat, statpost.gaus)$expected.deviance
expected.deviance(obs.sumstat, statpost.gaus, kernel =
"epanechnikov")$expected.deviance

## Modele 2 (Laplace)
## ##################
## Simulate data
zeta <- rexp(10000)
param <- cbind(theta, zeta)
y <- matrix(theta + sample(c(-1,1),200000, replace = TRUE)*rexp(200000,
rep(zeta, each = 20)), nrow = 20, ncol = 10000)

## Calculate summary statistics
s <- cbind( apply(y, 2, mean), apply(y, 2, sd), apply(y, 2, skewness),
apply(y, 2, kurtosis))

## ABC inference
lapl <- abc(target=obs.sumstat, param = param, sumstat=s, tol=.1, hcorr =
FALSE, method = "loclinear")
param.post <- lapl$adj.values

## Posterior predictive simulations
postpred.lapl <- matrix(param.post[,1] + sample(c(-1,1),20000, replace =
TRUE)*rexp(20000, rep(param.post[,2], each = 20)), nrow = 20, ncol =
1000)
statpost.lapl <- cbind(apply(postpred.lapl, 2,
mean),apply(postpred.lapl, 2, sd),apply(postpred.lapl,
2,skewness),apply(postpred.lapl, 2,kurtosis))

## Computation of the expected deviance
expected.deviance(obs.sumstat, statpost.lapl)$expected.deviance
expected.deviance(obs.sumstat, statpost.lapl, kernel =
"epanechnikov")$expected.deviance

}
\keyword{htest}
\keyword{models}
% Converted by Sd2Rd version 1.15.
