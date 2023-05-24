package io.oath.jwt

import com.typesafe.config.{Config, ConfigException}

import scala.concurrent.duration.FiniteDuration
import scala.util.control.Exception.allCatch

import scala.jdk.CollectionConverters.ListHasAsScala
import scala.jdk.DurationConverters.JavaDurationOps

package object config {

  private[config] implicit class ConfigOps(private val config: Config) {

    private def ifMissingDefault[T](default: T): PartialFunction[Throwable, T] = { case _: ConfigException.Missing =>
      default
    }

    def getMaybeNonEmptyString(path: String): Option[String] =
      allCatch
        .withTry(config.getString(path))
        .toOption

    def getMaybeFiniteDuration(path: String): Option[FiniteDuration] =
      allCatch
        .withTry(Some(config.getDuration(path).toScala))
        .recover(ifMissingDefault(None))
        .get

    def getBooleanDefaultFalse(path: String): Boolean =
      allCatch
        .withTry(config.getBoolean(path))
        .recover(ifMissingDefault(false))
        .get

    def getSeqNonEmptyString(path: String): Seq[String] =
      allCatch
        .withTry(config.getStringList(path).asScala.toSeq)
        .recover(ifMissingDefault(Seq.empty))
        .get
        .filter(_.nonEmpty)

    def getMaybeConfig(path: String): Option[Config] =
      allCatch
        .withTry(config.getConfig(path))
        .toOption
  }
}
