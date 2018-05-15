package storage

import akka.actor.ActorRef
import akka.pattern.ask
import akka.util.Timeout
import storage.Storage._

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

trait StorageNode {
  implicit val c: ExecutionContext
  implicit val t: Timeout

  def storageSystemActor: ActorRef

  def getInt(path: Path): Future[Int] =
    ask(storageSystemActor, GetInt(path))
      .mapTo[IntOpt]
      .map(_.x)
      .map {
        case Some(y) => y
        case None => throw StorageException("Value does not exist")
      }

  def getString(path: Path): Future[String] =
    ask(storageSystemActor, GetString(path))
      .mapTo[StringOpt]
      .map(_.x)
      .map {
        case Some(y) => y
        case None => throw StorageException("Value does not exist")
      }

  def getBoolean(path: Path): Future[Boolean] =
    ask(storageSystemActor, GetBoolean(path))
      .mapTo[BooleanOpt]
      .map(_.x)
      .map {
        case Some(y) => y
        case None => throw StorageException("Value does not exist")
      }

  def getDecimal(path: Path): Future[BigDecimal] =
    ask(storageSystemActor, GetDecimal(path))
      .mapTo[DecimalOpt]
      .map(_.x)
      .map {
        case Some(y) => y
        case None => throw StorageException("Value does not exist")
      }

  def getElement(path: Path): Future[AnyElement] =
    ask(storageSystemActor, GetElement(path))
      .mapTo[ElementOpt]
      .map(_.x)
      .map {
        case Some(y) => y
        case None => throw StorageException("Element does not exist")
      }

  def getRoot(path: Path): Future[ObjectElement] =
    ask(storageSystemActor, GetRoot(path))
      .mapTo[Root]
      .map(_.x)

  def getComplexElement(path: Path): Future[ComplexElement] = ???
  def getObjectElement(path: Path): Future[ObjectElement] = ???
  def getArrayElement(path: Path): Future[ArrayElement] = ???
  def getArrayElement(path: PathStr): Future[ArrayElement] = getArrayElement(Path(path))
  def getDataElement(path: Path): Future[DataElement] = ???
  def getData(paths: Paths): Future[Data] = ???
  def getBoolean(path: PathStr): Future[Boolean] = getBoolean(Path(path))
  def getInt(path: PathStr): Future[Int] = getInt(Path(path))
  def getString(path: PathStr): Future[String] = getString(Path(path))
  def getDecimal(path: PathStr): Future[BigDecimal] = getDecimal(Path(path))
  def getElement(path: PathStr): Future[AnyElement] = getElement(Path(path))
  def getDataElement(path: PathStr): Future[DataElement] = getDataElement(Path(path))
  def getData(paths: List[PathStr]): Future[Data] = getData(Paths.fromPathStrs(paths))

  def updateElement(path: Path, x: AnyElement): Future[Storage] = ???

  def updateDataElement(x: DataElement): Future[Storage] =
    ask(storageSystemActor, UpdateDataElementCmd(x, x.path))
      .mapTo[UpdatedStorage]
      .map(_.x)
      .map {
        case Success(y) => y
        case Failure(e) => throw StorageException("Storage update exception")
      }

  def updateData(x: Data): Future[StorageLike] = ???
  def updateData(x: (PathStr, Value)*): Future[StorageLike] = ???
  def updateElement(path: PathStr, x: AnyElement, consistency: Consistency = Consistency.Strict): Future[StorageLike] = ???

  def createElement(path: Path, x: AnyElement): Future[StorageLike] = ???
  def createElement(path: PathStr, x: AnyElement): Future[StorageLike] = ???
  def createElement(x: AnyElement): Future[StorageLike] = ???

  def deleteElement(path: Path): Future[StorageLike] = ???
}

