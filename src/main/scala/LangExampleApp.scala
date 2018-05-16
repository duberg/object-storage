import akka.actor.ActorSystem
import akka.util.Timeout
import storage._
import storage.actor.StorageSystemActor
import storage.actor.persistence.PersistenceId
import storage.lang.{ Assignment, Compiler, Evaluator, EvaluatorContext }

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._

object LangExampleApp extends App with Evaluator {
  implicit val system = ActorSystem("storage")
  implicit val executor: ExecutionContext = system.dispatcher
  implicit val timeout: Timeout = 4.seconds
  implicit val log = system.log

  val storageId = PersistenceId(s"expr-storage-v076")
  // three nodes for process, project, task
  val projectId = PersistenceId(s"${storageId.pathStr}.project1")
  val processId = PersistenceId(s"${storageId.pathStr}.process1")
  val taskId = PersistenceId(s"${storageId.pathStr}.task1")

  val storageActor = system.actorOf(StorageSystemActor.props(storageId), storageId.name)
  val storage = StorageSystem(storageActor)

  // (scope -> nodeId) mappings
  val mappings = Map(
    "$" -> processId,
    "$process" -> processId,
    "$task" -> taskId,
    "$project" -> projectId)

  implicit val ctx: EvaluatorContext = EvaluatorContext(storage, mappings)

  // dynamic expressions
  val str =
    """
      |$task.firstname = secretary.firstname,
      |$task.lastname = secretary.lastname,
      |$task.middlename = secretary.middlename,
      |$task.fullname = $task.firstname + " " + $task.lastname + " " + $task.middlename,
      |$task.status = $task.fullname + " ("+ secretary.status + ")",
      |
      |$task.counter = ($task.counter + 2) * 2,
      |$task.counter = ($task.counter + 2) * 2,
      |$task.counter = ($task.counter + 2) * 2,
      |$process.counter = $task.counter,
      |$project.counter = $process.counter + 1,
      |$task.counter = "4", // value conversion
      |
      |$project.manager = secretary // object import
    """.stripMargin

  val expr: List[Assignment] = Compiler.compileAssignments(str).right.get

  val createProcessNode = CreateNode(
    id = processId,
    name = None,
    description = None,
    storage = StorageExample.storage)

  val createTaskNode = CreateNode(
    id = taskId,
    name = None,
    description = None,
    storage = StorageExample.taskStorage)

  val createProjectNode = CreateNode(
    id = projectId,
    name = None,
    description = None,
    storage = StorageExample.projectStorage)

  for {
    _ <- storage.createNode(createProcessNode).recover({ case _ => processId })
    _ <- storage.createNode(createTaskNode).recover({ case _ => taskId })
    _ <- storage.createNode(createProjectNode).recover({ case _ => projectId })
    _ <- eval(expr) // EVALUATE
    taskStorage <- storage.getRoot(NodePath.root(taskId))
    processStorage <- storage.getRoot(NodePath.root(processId))
    projectStorage <- storage.getRoot(NodePath.root(projectId))
  } yield system.terminate().map(_ => {
    println("== task storage ==")
    println(taskStorage.prettify)
    println("== process storage ==")
    println(processStorage.prettify)
    println("== project storage ==")
    println(projectStorage.prettify)
  })
}