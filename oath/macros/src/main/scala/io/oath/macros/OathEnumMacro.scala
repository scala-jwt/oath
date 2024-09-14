package io.oath.macros

import scala.quoted.*

object OathEnumMacro:
  inline def enumValues[E]: Array[E] = ${ enumValuesImpl[E] }

  def enumValuesImpl[E: Type](using Quotes): Expr[Array[E]] =
    import quotes.reflect.*
    val companion = Ref(TypeTree.of[E].symbol.companionModule)
    Select.unique(companion, "values").asExprOf[Array[E]]
