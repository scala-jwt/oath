package io.oath.jwt.utils

import java.time.temporal.ChronoUnit
import java.time.{Clock, Instant, ZoneId}
import scala.util.chaining.scalaUtilChainingOps

trait ClockHelper {
  def now   = Instant.now().truncatedTo(ChronoUnit.SECONDS)
  def timeWithClock: (Instant, Clock) = now.pipe(time => time -> Clock.fixed(time, ZoneId.of("UTC")))
}
