package io.oath.juror

import com.typesafe.config.ConfigFactory

package object config {

  private[config] val location = "juror"

  private[config] val rootConfig = ConfigFactory.load().getConfig(location)
}
