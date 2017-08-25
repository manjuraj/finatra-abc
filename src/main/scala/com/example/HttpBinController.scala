package com.example

import com.twitter.conversions.time._
import com.twitter.finagle.{Http, Failure, TimeoutException}
import com.twitter.finagle.http.{Method, Request, Response, Version, Status}
import com.twitter.finatra.http.Controller
import com.twitter.finagle.param.HighResTimer
import com.twitter.finagle.service.{Backoff, RetryBudget, RetryFilter, RetryPolicy}
import com.twitter.util.{Future, Return, Throw, Try}
import org.jboss.netty.handler.codec.http.HttpHeaders

//
// # Retries
//
// Retries in finagle are handled through two filters:
// 1). RequeueFilter
// 2). RetryFilter
// Failures designated safe (for example, exceptions that occurred before the bytes
// were written to the wire) will be automatically retried by Finagle using the
// RequeueFilter. On the other hand, failures marked as NonRetryable are not retried.
// Note that Finagle treats request timeouts as NonRetryable as default because
// Finagle has no way to tell whether a timedout requests is idempotent or not.
//
// By default, every client stack has a RequeueFilter configured and its behavior can
// be controlled by:
// - RetryBudget: configures # of retries
// - BackoffPolicy: configures how quickly to retry
//
// The default behavior is to retry with "No Delays" and a budget that allows for approx
// 20% of the total requests to be retried on top of 10 retries per second.
//
// Unlike RequeueFilter, RetryFilter must be added to a client stack explicitly and it
// used for handling application level exceptions. RetryFilter can be configured using
// a RetryPolicy. A RetryPolicy could be constructed as:
// - RetryPolicy.tries: configures retries using Backoff between the given
//   number of maximum attempts
// - RetryPolicy.backoff: configures retries using the given backoff policy
//
// Remember to share a single instance of RetryBudget between both RetryFilter and
// RequeueFilter to prevent retry storms.
//
// References:
// - https://twitter.github.io/finagle/guide/Clients.html#retries
// - https://finagle.github.io/blog/2016/02/08/retry-budgets
// - https://groups.google.com/forum/#!topic/finaglers/-MvcHJTxgjQ
// - https://www.awsarchitectureblog.com/2015/03/backoff.html
// - http://www.evanjones.ca/retries-considered-harmful.html
//
// # Timeout
//
// In finagle, there are two types of timeout which by default have unbounded
// values. These are timeouts are
// - Session: configured through withSession.acquisitionTimeout
// - Request: configured withRequestTimeout or TimeoutFilter
//
// References:
// - https://twitter.github.io/finagle/guide/Clients.html#timeouts-expiration

class HttpBinController extends Controller {
  val sr = AbcServerMain.statsReceiver.scope("httpbin")
  val host = "httpbin.org:80"
  val name = "httpbin"
  val budget = RetryBudget(ttl = 10.seconds, minRetriesPerSec = 10, percentCanRetry = 0.1)
  val backoff = Backoff.decorrelatedJittered(2.seconds, 32.seconds)
  val shouldRetry = PartialFunction.apply[(Request, Try[Response]), Boolean] {
    case (_, Return(r)) if r.statusCode == Status.ServiceUnavailable.code =>
      logger.info("service unavailable, retrying")
      true
    case (_, Return(r)) if r.statusCode == Status.GatewayTimeout.code     =>
      logger.info("gateway timeout, retrying")
      true
    case (_, Throw(_: TimeoutException))                                  =>
      logger.info("request timeout, retrying")
      true
    case (_, Return(_))                                                   =>
      false
    case (_, Throw(e))                                                    =>
      logger.info(s"ignore failure $e")
      false
  }
  val policy = RetryPolicy.tries(3, shouldRetry)
  val retryFilter = new RetryFilter(
    retryPolicy = policy,
    timer = HighResTimer.Default,
    statsReceiver = sr,
    retryBudget = budget
  )
  val service = Http.client
    .withSessionQualifier.noFailFast
    .withTransport.connectTimeout(1.second)
    .withSession.acquisitionTimeout(1.second)
    .withRequestTimeout(2.second)
    .withRetryBudget(budget)
    .withRetryBackoff(backoff)
    .filtered(retryFilter)
    .withStatsReceiver(sr)
    .newService(host)

  def mkHttpReq(url: String): Request = {
    val req = Request(Version.Http11, Method.Get, url)
    req.headerMap.set(HttpHeaders.Names.HOST, host)
    req.headerMap.set(HttpHeaders.Names.USER_AGENT, "curl/7.43.0")
    req
  }

  get("/get") { _: Request =>
    service(mkHttpReq("/get"))
  }

  // returns the given HTTP status code
  get("/status/:code") { inReq: Request =>
    val code = inReq.getIntParam("code")
    val url = s"/status/${code}"
    service(mkHttpReq(url))
  }

  // delays responding for min(n, 10) seconds
  get("/delay/:n") { inReq: Request =>
    val n = inReq.getIntParam("n")
    val url = s"/delay/${n}"
    service(mkHttpReq(url))
  }
}
