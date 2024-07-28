package io.oath.test

import java.time.temporal.ChronoUnit
import java.time.{Clock, Instant, ZoneId}

trait ClockHelper {
  def getInstantNowSeconds: Instant = Instant.now().truncatedTo(ChronoUnit.SECONDS)

  def getFixedClock(time: Instant): Clock = Clock.fixed(time, ZoneId.of("UTC"))
}
