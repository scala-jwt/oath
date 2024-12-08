package io.oath.macros

import scala.annotation.nowarn

trait OathEnum[E] {
  def values: Map[E, String]
}

object OathEnum {

  inline private def summonSumInstances[ET <: Tuple, E](acc: Set[E]): Set[E] = {
    import scala.compiletime.*

    inline erasedValue[ET] match {
      case _: EmptyTuple => acc
      case _: (t *: ts) =>
        val enumValue = summonInline[ValueOf[t]].value.asInstanceOf[E]
        summonSumInstances[ts, E](acc + enumValue)
    }
  }

  private def convertUpperCamelToLowerHyphen(str: String): String =
    str.split("(?=\\p{Lu})").map(_.trim.toLowerCase).filter(_.nonEmpty).mkString("-").trim

  @nowarn("msg=New anonymous class definition will be duplicated at each inline site")
  inline def derived[E](using m: scala.deriving.Mirror.SumOf[E]): OathEnum[E] =
    new OathEnum[E] {
      def values: Map[E, String] = summonSumInstances[m.MirroredElemTypes, E](Set.empty)
        .map(e => e -> convertUpperCamelToLowerHyphen(e.toString))
        .toMap
    }
}
