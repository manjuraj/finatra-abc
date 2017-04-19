package com.example

import com.twitter.util.logging.Logger

class AbcFooData(in: String) {
  val log = Logger(getClass)

  def out: String = {
    log.debug(s"foo out of $in")
    log.info(s"foo out of $in")
    log.warn(s"foo out of $in")
    in
  }
}
