package com.example

import com.twitter.finagle.Http
import com.twitter.finagle.http.{Method, Request, Response, Version}
import com.twitter.finatra.http.Controller
import org.jboss.netty.handler.codec.http.HttpHeaders

class AbcController extends Controller {
  val hosts = "httpbin.org:80"
  val client = Http.client
    .newService(hosts)

  get("/hi") { req: Request =>
    debug("hi")
    info("hi")
    warn("hi")

    (new AbcFooData("ola")).out
    (new AbcBarData("hai")).out

    "Hello " + req.params.getOrElse("name", "unnamed")
  }
}
