package com.example

import com.twitter.logging.Logger

class AbcFooData(in: String) {
  private[this] val log = Logger(getClass)

  def out: String = {
    log.debug(s"foo out of $in")
    log.info(s"foo out of $in")
    in
  }
}
