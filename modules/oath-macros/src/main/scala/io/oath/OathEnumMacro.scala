package io.oath

import scala.collection.immutable._
import scala.reflect.macros.blackbox.Context
import scala.util.control.Exception.allCatch
import scala.util.control.NonFatal

object OathEnumMacro {

  def findEnumMembersImpl[A: c.WeakTypeTag](c: Context): c.Expr[Set[A]] = {
    import c.universe._
    val symbol = weakTypeOf[A].typeSymbol
    validateSealedTrait(c)(symbol)
    val subclassSymbols = validateAllDirectSubclassesModules(c)(symbol)
    buildSetExpr(c)(subclassSymbols)
  }

  private[oath] def validateSealedTrait(c: Context)(symbol: c.universe.Symbol): Unit = {
    val isSealed = allCatch.opt(symbol.asClass.isSealed)
    if (!isSealed.exists(identity))
      c.abort(
        c.enclosingPosition,
        "findEnumMembersImpl requires sealed trait or class.",
      )
  }

  private[oath] def isCaseModuleClass(c: Context)(symbol: c.universe.Symbol): Boolean =
    symbol.isModuleClass && symbol.asClass.isCaseClass

  private[oath] def validateAllDirectSubclassesModules(
      c: Context
  )(symbol: c.universe.Symbol): Set[c.universe.Symbol] = {
    val directSubclasses = symbol.asClass.knownDirectSubclasses
    val isCaseAndModule  = allCatch.opt(directSubclasses.forall(isCaseModuleClass(c)(_)))
    if (isCaseAndModule.exists(identity)) directSubclasses
    else {
      val invalidSymbols = directSubclasses.filterNot(isCaseModuleClass(c)(_))
      c.abort(
        c.enclosingPosition,
        s"findEnumMembersImpl works only on sealed traits or classes and case objects. Invalid structures $invalidSymbols",
      )
    }
  }

  private[oath] def sourceModuleRef(c: Context)(sym: c.universe.Symbol): c.universe.Symbol =
    try
      sym
        .asInstanceOf[
          scala.reflect.internal.Symbols#Symbol
        ]
        .sourceModule
        .asInstanceOf[c.universe.Symbol]
    catch {
      case NonFatal(e) =>
        c.abort(
          c.enclosingPosition,
          s"Got an exception, indicating a possible bug in oath macros. Message: ${e.getMessage}",
        )
    }

  private[oath] def buildSetExpr[A: c.WeakTypeTag](c: Context)(
      subclassSymbols: Set[c.universe.Symbol]
  ): c.Expr[Set[A]] = {
    import c.universe._
    val resultType = weakTypeOf[A]
    if (subclassSymbols.isEmpty) {
      c.Expr[Set[A]](reify(Set.empty[A]).tree)
    } else {
      c.Expr[Set[A]](
        Apply(
          TypeApply(
            Select(reify(Set).tree, c.universe.TermName("apply")),
            List(TypeTree(resultType)),
          ),
          subclassSymbols.map(sourceModuleRef(c)(_)).map(Ident(_)).toList,
        )
      )
    }
  }
}
