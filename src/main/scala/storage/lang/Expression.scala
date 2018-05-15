package storage.lang

import akka.util.Timeout
import storage.{AnySimpleElement, PathStr}

import scala.concurrent.{ExecutionContext, Future}

trait Expression {
  def eval(implicit ctx: EvaluatorContext, c: ExecutionContext, t: Timeout): Future[Any]
  def getVars: Set[PathStr]
}

trait BinaryOperator extends Expression {
  val exprA: Expression
  val exprB: Expression

  def getVars: Set[PathStr] = exprA.getVars ++ exprB.getVars
}

trait UnaryOperator extends Expression {
  val expr: Expression

  def getVars: Set[PathStr] = expr.getVars
}

trait Literal extends Expression {
  def getVars = Set.empty
}

case class Equal(exprA: Expression, exprB: Expression) extends BinaryOperator {
  def eval(implicit ctx: EvaluatorContext, c: ExecutionContext, t: Timeout) = Future(exprA.eval.equals(exprB.eval))
}

case class NotEqual(exprA: Expression, exprB: Expression) extends BinaryOperator {
  def eval(implicit ctx: EvaluatorContext, c: ExecutionContext, t: Timeout) = Future(!exprA.eval.equals(exprB.eval))
}

case class GreaterThan(exprA: Expression, exprB: Expression) extends BinaryOperator {
  def eval(implicit ctx: EvaluatorContext, c: ExecutionContext, t: Timeout) = {
    val evalExprA = exprA.eval
    val evalExprB = exprB.eval
    for {
      x <- evalExprA
      y <- evalExprB
    } yield (x, y) match {
      case (str1: String, str2: String) => str1 > str2
      case (d1: BigDecimal, d2: BigDecimal) => d1 > d2
      case _ => throw new IllegalArgumentException()
    }
  }
}

case class LowerThan(exprA: Expression, exprB: Expression) extends BinaryOperator {
  def eval(implicit ctx: EvaluatorContext, c: ExecutionContext, t: Timeout) = {
    val evalExprA = exprA.eval
    val evalExprB = exprB.eval
    for {
      x <- evalExprA
      y <- evalExprB
    } yield (x, y) match {
      case (str1: String, str2: String) => str1 < str2
      case (d1: BigDecimal, d2: BigDecimal) => d1 < d2
      case (d1: Int, d2: BigDecimal) => BigDecimal(d1) < d2
      case (d1: BigDecimal, d2: Int) => d1 < BigDecimal(d2)
      case (d1: Int, d2: Int) => d1 < d2
      case _ => throw new IllegalArgumentException()
    }
  }
}

case class GreaterThanOrEqual(exprA: Expression, exprB: Expression) extends BinaryOperator {
  def eval(implicit ctx: EvaluatorContext, c: ExecutionContext, t: Timeout) = {
    val evalExprA = exprA.eval
    val evalExprB = exprB.eval
    for {
      x <- evalExprA
      y <- evalExprB
    } yield (x, y) match {
      case (str1: String, str2: String) => str1 >= str2
      case (d1: BigDecimal, d2: BigDecimal) => d1 >= d2
      case _ => throw new IllegalArgumentException()
    }
  }
}

case class LowerThanOrEqual(exprA: Expression, exprB: Expression) extends BinaryOperator {
  def eval(implicit ctx: EvaluatorContext, c: ExecutionContext, t: Timeout) = {
    val evalExprA = exprA.eval
    val evalExprB = exprB.eval
    for {
      x <- evalExprA
      y <- evalExprB
    } yield (x, y) match {
      case (str1: String, str2: String) => str1 <= str2
      case (d1: BigDecimal, d2: BigDecimal) => d1 <= d2
      case _ => throw new IllegalArgumentException()
    }
  }
}

case class Not(expr: Expression) extends UnaryOperator {
  def eval(implicit ctx: EvaluatorContext, c: ExecutionContext, t: Timeout) = {
    expr.eval.map {
      case bool: Boolean => !bool
      case str: String => (str == null) || (str.length == 0)
      case val1: BigDecimal => val1 == BigDecimal(0)
      case _ => false
    }
  }
}

case class Or(exprA: Expression, exprB: Expression) extends BinaryOperator {
  def eval(implicit ctx: EvaluatorContext, c: ExecutionContext, t: Timeout) = {
    exprA.eval flatMap {
      case val1: Boolean => exprB.eval map {
        case val2: Boolean => val1 || val2
        case _ => false
      }
      case _ => Future.successful(false)
    }
  }
}

case class And(exprA: Expression, exprB: Expression) extends BinaryOperator {
  def eval(implicit ctx: EvaluatorContext, c: ExecutionContext, t: Timeout) = {
    exprA.eval flatMap {
      case val1: Boolean => exprB.eval map {
        case val2: Boolean => val1 && val2
        case _ => false
      }
      case _ => Future.successful(false)
    }
  }
}

case class True() extends Literal {
  def eval(implicit ctx: EvaluatorContext, c: ExecutionContext, t: Timeout) = Future.successful(true)
}

case class False() extends Literal {
  def eval(implicit ctx: EvaluatorContext, c: ExecutionContext, t: Timeout) = Future.successful(false)
}

case class Variable(path: PathStr) extends Expression with PathMapper {
  def eval(implicit ctx: EvaluatorContext, c: ExecutionContext, t: Timeout) = ctx.storage.getElement(resolve(path)) map {
    case x: AnySimpleElement => x.value
    case x => x
  }
  def getVars = Set(path)
}

case class StringLiteral(value: String) extends Literal {
  def eval(implicit ctx: EvaluatorContext, c: ExecutionContext, t: Timeout) = Future.successful(value)
}

case class DecimalLiteral(value: BigDecimal) extends Literal {
  def eval(implicit ctx: EvaluatorContext, c: ExecutionContext, t: Timeout) = Future.successful(value)
}

case class Plus(exprA: Expression, exprB: Expression) extends BinaryOperator {
  def eval(implicit ctx: EvaluatorContext, c: ExecutionContext, t: Timeout) = {
    val evalExprA = exprA.eval
    val evalExprB = exprB.eval
    for {
      x <- evalExprA
      y <- evalExprB
    } yield (x, y) match {
      case (str1: String, str2: String) => str1 + str2
      case (d1: BigDecimal, d2: BigDecimal) => d1 + d2
      case (str1: String, d2: BigDecimal) => str1 + d2.toString
      case (d1: Int, d2: BigDecimal) => BigDecimal(d1) + d2
      case (d1: BigDecimal, d2: Int) => d1 + BigDecimal(d2)
      case (d1: Int, d2: Int) => d1 + d2
      case _ => throw new IllegalArgumentException()
    }
  }
}

case class Minus(exprA: Expression, exprB: Expression) extends BinaryOperator {
  def eval(implicit ctx: EvaluatorContext, c: ExecutionContext, t: Timeout) = {
    val evalExprA = exprA.eval
    val evalExprB = exprB.eval
    for {
      x <- evalExprA
      y <- evalExprB
    } yield (x, y) match {
      case (d1: BigDecimal, d2: BigDecimal) => d1 - d2
      case (x: String, y: String) => x.replace(y, "")
      case _ => throw new IllegalArgumentException()
    }
  }
}

case class Divide(exprA: Expression, exprB: Expression) extends BinaryOperator {
  def eval(implicit ctx: EvaluatorContext, c: ExecutionContext, t: Timeout) = {
    val evalExprA = exprA.eval
    val evalExprB = exprB.eval
    for {
      x <- evalExprA
      y <- evalExprB
    } yield (x, y) match {
      case (d1: BigDecimal, d2: BigDecimal) => d1 / d2
      case _ => throw new IllegalArgumentException()
    }
  }
}

case class Multiply(exprA: Expression, exprB: Expression) extends BinaryOperator {
  def eval(implicit ctx: EvaluatorContext, c: ExecutionContext, t: Timeout) = {
    val evalExprA = exprA.eval
    val evalExprB = exprB.eval
    for {
      x <- evalExprA
      y <- evalExprB
    } yield (x, y) match {
      case (d1: BigDecimal, d2: BigDecimal) => d1 * d2
      case (d1: Int, d2: BigDecimal) => BigDecimal(d1) * d2
      case (d1: BigDecimal, d2: Int) => d1 * BigDecimal(d2)
      case (d1: Int, d2: Int) => d1 * d2
      case _ => throw new IllegalArgumentException()
    }
  }
}

case class Remainder(exprA: Expression, exprB: Expression) extends BinaryOperator {
  def eval(implicit ctx: EvaluatorContext, c: ExecutionContext, t: Timeout) = {
    val evalExprA = exprA.eval
    val evalExprB = exprB.eval
    for {
      x <- evalExprA
      y <- evalExprB
    } yield (x, y) match {
      case (d1: BigDecimal, d2: BigDecimal) => d1 % d2
      case (d1: Int, d2: BigDecimal) => BigDecimal(d1) % d2
      case (d1: BigDecimal, d2: Int) => d1 % BigDecimal(d2)
      case (d1: Int, d2: Int) => d1 % d2
      case _ => throw new IllegalArgumentException()
    }
  }
}