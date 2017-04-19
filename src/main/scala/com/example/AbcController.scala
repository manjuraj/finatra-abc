package com.example

import com.twitter.finagle.http.Request
import com.twitter.finatra.http.Controller

class AbcController extends Controller {
  get("/hi") { request: Request =>
    debug("hi")
    info("hi")
    warn("hi")

    (new AbcFooData("ola")).out
    (new AbcBarData("hai")).out

    "Hello " + request.params.getOrElse("name", "unnamed")
  }
}
