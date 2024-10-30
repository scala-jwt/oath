package io.oath.macros

object OathEnumMacro {

  inline def enumValues[T](using
      m: scala.deriving.Mirror.SumOf[T]
  ): Set[T] =
    allInstances[m.MirroredElemTypes, m.MirroredType].toSet

  inline def allInstances[ET <: Tuple, T]: List[T] =
    import scala.compiletime.*

    inline erasedValue[ET] match
      case _: EmptyTuple => Nil
      case _: (t *: ts) =>
        summonInline[ValueOf[t]].value.asInstanceOf[T] :: allInstances[ts, T]
}
