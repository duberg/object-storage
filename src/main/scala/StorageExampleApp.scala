import akka.actor.ActorSystem
import akka.util.Timeout
import storage._
import storage.actor.StorageSystemActor
import storage.actor.persistence.PersistenceId

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._

object StorageApp extends App {
  implicit val system = ActorSystem("storage")
  implicit val executor: ExecutionContext = system.dispatcher
  implicit val timeout: Timeout = 4.seconds

  val storageId = PersistenceId(s"storage-v050")
  val nodeId = PersistenceId(s"${storageId.pathStr}.node1")

  val storageActor = system.actorOf(StorageSystemActor.props(storageId), storageId.name)
  val storage = StorageSystem(storageActor)

  def x = CreateNode(
    id = nodeId,
    name = None,
    description = None,
    storage = StorageExample.storage)

  for {
    x <- storage.createNode(x).recover({ case _ => nodeId })
    y <- storage.getElement(Path(nodeId, "form1"))
  } yield system.terminate().map(_ => println(y.prettify))
}

object StorageExample {
  val storage = Storage(
    StringElement(Some("name1"), Some("desc1"), "name1", "x1"),
    ObjectMetadata(Some("name1"), Some("desc1"), "form1"),
    ObjectMetadata(None, None, "form1.data"),
    ObjectMetadata(None, None, "form1.data.title"),
    StringElement("title1", "form1.data.title.ru"),
    StringElement(None, None, "Title", "form1.data.title.en"),
    BooleanElement(None, None, value = false, "form1.check"),
    ObjectMetadata(None, None, "form1.parent"),
    StringElement(None, None, "firstname", "form1.parent.firstname"),
    StringElement(None, None, "lastname", "form1.parent.lastname"),
    StringElement(None, None, "middlename", "form1.parent.middlename"),
    StringElement(None, None, "lastname", "form1.lastname"),
    StringElement(None, None, "middlename", "form1.middlename"),
    ArrayMetadata(None, None, "form1.files"),
    StringElement(None, None, "https://github.com/duberg/object-storage", "form1.files[0]"),
    StringElement(None, None, "https://github.com/duberg/object-storage", "form1.files[2]"),
    StringElement(None, None, "https://github.com/duberg/object-storage", "form1.files[1]"),
    // reference example
    RefMetadata(None, Option("Reference to parent object"), "form1.parent", "form1.parent2"),
    BooleanElement(value = false, "isEmployee"),
    ObjectMetadata(None, None, "secretary"),
    StringElement(None, None, "Mark", "secretary.firstname"),
    StringElement(None, None, "Duberg", "secretary.lastname"),
    StringElement(None, None, "J.", "secretary.middlename"),
    StringElement(None, None, "busy", "secretary.status"),
    IntElement(None, None, 0, "counter")
  )

  // task example storage
  val taskStorage = Storage(
    StringElement(None, None, "", "firstname"),
    StringElement(None, None, "", "lastname"),
    StringElement(None, None, "", "middlename"),
    StringElement(None, None, "", "fullname"),
    StringElement(None, None, "", "status"),
    IntElement(None, None, 0, "counter"),
  )
}