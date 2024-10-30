package io.oath.config

import com.typesafe.config.{Config, ConfigException, ConfigFactory}

import scala.concurrent.duration.FiniteDuration
import scala.jdk.CollectionConverters.*
import scala.jdk.DurationConverters.*
import scala.util.chaining.*
import scala.util.control.Exception.allCatch

private[config] val OathLocation = "oath"
private[config] val rootConfig   = ConfigFactory.load().getConfig(OathLocation)

extension (config: Config) {
  private def ifMissingDefault[T](default: T): PartialFunction[Throwable, T] = { case _: ConfigException.Missing =>
    default
  }

  private[config] def getMaybeNonEmptyString(path: String): Option[String] =
    allCatch
      .withTry(Some(config.getString(path)))
      .recover(ifMissingDefault(Option.empty))
      .toOption
      .flatten
      .tap(value => if (value.exists(_.isEmpty)) throw new IllegalArgumentException(s"$path empty string not allowed."))

  private[config] def getMaybeFiniteDuration(path: String): Option[FiniteDuration] =
    allCatch
      .withTry(Some(config.getDuration(path).toScala))
      .recover(ifMissingDefault(None))
      .get

  private[config] def getBooleanDefaultFalse(path: String): Boolean =
    allCatch
      .withTry(config.getBoolean(path))
      .recover(ifMissingDefault(false))
      .get

  private[config] def getSeqNonEmptyString(path: String): Seq[String] =
    allCatch
      .withTry(config.getStringList(path).asScala.toSeq)
      .recover(ifMissingDefault(Seq.empty))
      .get
      .tap(value =>
        if value.exists(_.isEmpty) then
          throw new IllegalArgumentException(s"$path empty string in the list not allowed.")
      )

  private[config] def getMaybeConfig(path: String): Option[Config] =
    allCatch
      .withTry(Some(config.getConfig(path)))
      .recover(ifMissingDefault(Option.empty))
      .toOption
      .flatten
}
