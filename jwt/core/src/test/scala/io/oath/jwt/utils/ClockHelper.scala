package io.oath.jwt.utils

import java.time.temporal.ChronoUnit
import java.time.{Clock, Instant, ZoneId}

trait ClockHelper {
  def now   = Instant.now().truncatedTo(ChronoUnit.SECONDS)
  def clock = Clock.fixed(now, ZoneId.of("UTC"))
}
