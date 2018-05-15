package storage.lang

import akka.util.Timeout
import storage._

import scala.concurrent.{ExecutionContext, Future}

/**
 * = Expression evaluator =
 *
 * Выражения выполняются в контексте [[EvaluatorContext]].
 *
 * Конкатенация строк:
 *  $form.firstname = secretary.firstname + " (" + comment + ") "
 *  $form.lastname = secretary.lastname + " (" + comment + ") "
 *  $form.middlename = secretary.middlename + " (" + comment + ") "
 *  $form.changeable = changeable
 *
 * Чтение и запись структуры:
 *  $form.files = files
 *
 * Чтение результатов выполнения предыдущих операций:
 *  $form.fullname = $form.firstname + $form.lastname + $form.middlename
 *  $form.counter = ($form.counter + 2) * 2
 */
trait Evaluator extends PathMapper {
  implicit def ctx: EvaluatorContext
  implicit def executor: ExecutionContext
  implicit def timeout: Timeout

  def eval(assignments: Assignments) = {
    def evalAssignment(path: PathStr, expr: Expression): Future[Storage] = expr.eval flatMap {
      case x: AnyElement => ctx.storage.updateElement(resolve(path), x)
      case x => ctx.storage.updateDataElement(DataElement(resolve(path), x))
    }
    def evalAssignments: List[Storage => Future[Storage]] = assignments map {
      case Assignment(path, expr) => (storage: Storage) => evalAssignment(path, expr)
    }
    evalAssignments.foldLeft(Future.successful(Storage.empty)) {
      case (f, evalAssignment) => f.flatMap(evalAssignment(_))
    }
  }
}