package io.oath.jwt

import com.typesafe.config.{Config, ConfigException}

import scala.concurrent.duration.FiniteDuration
import scala.util.control.Exception.allCatch

import scala.jdk.CollectionConverters.ListHasAsScala
import scala.jdk.DurationConverters.JavaDurationOps
import scala.util.chaining.scalaUtilChainingOps

package object config {

  private[config] implicit class ConfigOps(private val config: Config) {

    private def ifMissingDefault[T](default: T): PartialFunction[Throwable, T] = { case _: ConfigException.Missing =>
      default
    }

    def getMaybeNonEmptyString(path: String): Option[String] =
      allCatch
        .withTry(Some(config.getString(path)))
        .recover(ifMissingDefault(Option.empty))
        .toOption
        .flatten
        .tap(value =>
          if (value.exists(_.isEmpty)) throw new IllegalArgumentException(s"$path empty string not allowed.")
        )

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
        .tap(value =>
          if (value.exists(_.isEmpty))
            throw new IllegalArgumentException(s"$path empty string in the list not allowed.")
        )

    def getMaybeConfig(path: String): Option[Config] =
      allCatch
        .withTry(Some(config.getConfig(path)))
        .recover(ifMissingDefault(Option.empty))
        .toOption
        .flatten
  }
}
