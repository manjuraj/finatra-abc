package com.example

import com.twitter.inject.Logging

class AbcBarData(in: String) extends Logging {

  def out: String = {
    debug(s"bar out of $in")
    info(s"bar out of $in")
    warn(s"bar out of $in")
    in
  }
}
