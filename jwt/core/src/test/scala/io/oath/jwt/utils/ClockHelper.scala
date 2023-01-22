package io.oath.jwt.utils

import java.time.temporal.ChronoUnit
import java.time.{Clock, Instant, ZoneId}

trait ClockHelper {
  val now   = Instant.now().truncatedTo(ChronoUnit.SECONDS)
  val clock = Clock.fixed(now, ZoneId.of("UTC"))
}
