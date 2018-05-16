package storage.lang

import akka.event.LoggingAdapter
import akka.util.Timeout
import storage._
import storage.actor.persistence.PersistenceId

import scala.concurrent.{ ExecutionContext, Future }

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
trait Evaluator extends NodePathMapper {
  implicit def ctx: EvaluatorContext
  implicit def executor: ExecutionContext
  implicit def timeout: Timeout
  implicit val log: LoggingAdapter

  // Function that returns Future
  private type FUNC = Set[PersistenceId] => Future[Set[PersistenceId]]
  private def func(assignment: Assignment): FUNC = (nodeIds: Set[PersistenceId]) => eval(assignment).map(nodeIds ++ _)
  private def funcs(assignments: Assignments): List[FUNC] = assignments map func

  def eval(assignment: Assignment): Future[Set[PersistenceId]] = assignment.expr.eval flatMap {
    case element: ComplexElement =>
      val nodePath = resolve(assignment.path)
      val nodeId = nodePath.nodeId
      val path = nodePath.path

      val dataElements = element
        .withPath(path)
        .repr
        .withoutMetadata
        .impl.values
        .map { x => DataElement(x.path, x.value, Some(nodeId)) }
        .toSet

      val data = Data(dataElements)

      ctx.storage.updateData(data)
    case x =>
      ctx.storage.updateDataElement(DataElement(resolve(assignment.path), x)).map(Set(_))
  }

  def eval(assignments: Assignments): Future[Set[PersistenceId]] = {
    funcs(assignments).foldLeft(Future.successful(Set[PersistenceId]())) {
      case (f1, f2) => f1.flatMap(f2(_)).recover {
        case e: Exception =>
          log.error(e.getMessage)
          e.printStackTrace()
          throw e
      }
    }
  }
}