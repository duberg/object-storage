package storage

import akka.actor.ActorRef
import akka.pattern.ask
import akka.util.Timeout
import storage.Storage._
import storage.StorageSystem.{ GetNodeRef, NodeRefOpt }
import storage.actor.persistence.PersistenceId

import scala.concurrent.{ ExecutionContext, Future }
import scala.util.{ Failure, Success }

trait StorageNode {
  implicit val c: ExecutionContext
  implicit val t: Timeout

  def storageSystem: ActorRef

  def getNode(nodeId: PersistenceId): Future[ActorRef] =
    ask(storageSystem, GetNodeRef(nodeId))
      .mapTo[NodeRefOpt]
      .map(_.x)
      .map {
        case Some(y) => y
        case None => throw StorageException("Node not found")
      }

  def getInt(nodePath: NodePath): Future[Int] = getNode(nodePath.nodeId).flatMap(getInt(_, nodePath.path))
  def getString(nodePath: NodePath): Future[String] = getNode(nodePath.nodeId).flatMap(getString(_, nodePath.path))
  def getBoolean(nodePath: NodePath): Future[Boolean] = getNode(nodePath.nodeId).flatMap(getBoolean(_, nodePath.path))
  def getDecimal(nodePath: NodePath): Future[BigDecimal] = getNode(nodePath.nodeId).flatMap(getDecimal(_, nodePath.path))
  def getElement(nodePath: NodePath): Future[AnyElement] = getNode(nodePath.nodeId).flatMap(getElement(_, nodePath.path))
  def getRoot(nodePath: NodePath): Future[AnyElement] = getNode(nodePath.nodeId).flatMap(getRoot(_, nodePath.path))

  private def getInt(node: ActorRef, path: Path): Future[Int] =
    ask(node, GetInt(path))
      .mapTo[IntOpt]
      .map(_.x)
      .map {
        case Some(y) => y
        case None => throw StorageException("Value does not exist")
      }

  private def getString(node: ActorRef, path: Path): Future[String] =
    ask(node, GetString(path))
      .mapTo[StringOpt]
      .map(_.x)
      .map {
        case Some(y) => y
        case None => throw StorageException("Value does not exist")
      }

  private def getBoolean(node: ActorRef, path: Path): Future[Boolean] =
    ask(node, GetBoolean(path))
      .mapTo[BooleanOpt]
      .map(_.x)
      .map {
        case Some(y) => y
        case None => throw StorageException("Value does not exist")
      }

  private def getDecimal(node: ActorRef, path: Path): Future[BigDecimal] =
    ask(node, GetDecimal(path))
      .mapTo[DecimalOpt]
      .map(_.x)
      .map {
        case Some(y) => y
        case None => throw StorageException("Value does not exist")
      }

  private def getElement(node: ActorRef, path: Path): Future[AnyElement] =
    ask(node, GetElement(path))
      .mapTo[ElementOpt]
      .map(_.x)
      .map {
        case Some(y) => y
        case None => throw StorageException("Element does not exist")
      }

  private def getRoot(node: ActorRef, path: Path): Future[ObjectElement] =
    ask(node, GetRoot(path))
      .mapTo[Root]
      .map(_.x)

  private def getComplexElement(nodePath: NodePath): Future[ComplexElement] = ???
  private def getObjectElement(nodePath: NodePath): Future[ObjectElement] = ???
  private def getArrayElement(nodePath: NodePath): Future[ArrayElement] = ???
  private def getArrayElement(nodePath: NodePathStr): Future[ArrayElement] = getArrayElement(NodePath(nodePath))
  private def getDataElement(nodePath: NodePath): Future[DataElement] = ???
  private def getData(paths: Paths): Future[Data] = ???
  private def getBoolean(nodePath: NodePathStr): Future[Boolean] = getBoolean(NodePath(nodePath))
  private def getInt(nodePath: NodePathStr): Future[Int] = getInt(NodePath(nodePath))
  private def getString(nodePath: NodePathStr): Future[String] = getString(NodePath(nodePath))
  private def getDecimal(nodePath: NodePathStr): Future[BigDecimal] = getDecimal(NodePath(nodePath))
  private def getElement(nodePath: NodePathStr): Future[AnyElement] = getElement(NodePath(nodePath))
  private def getDataElement(nodePath: NodePathStr): Future[DataElement] = getDataElement(NodePath(nodePath))
  //private def getData(paths: List[NodePathStr]): Future[Data] = getData(Paths.fromPathStrs(paths))
  // private def updateElement(nodePath: NodePath, x: AnyElement): Future[Storage] = ???

  def updateDataElement(x: DataElement): Future[PersistenceId] = getNode(x.nodeId).flatMap(updateDataElement(_, x))

  private def updateDataElement(node: ActorRef, x: DataElement): Future[PersistenceId] =
    ask(node, UpdateDataElementCmd(x))
      .mapTo[NodeUpdated]
      .map(_.nodeId)
      .map {
        case Success(y) => y
        case Failure(e) =>
          e.printStackTrace()
          throw StorageException("Storage update exception")
      }

  def updateData(x: Data): Future[Set[PersistenceId]] = Future.sequence(x.elements.map(updateDataElement))

  private def updateData(node: ActorRef, x: Data): Future[PersistenceId] =
    ask(node, UpdateDataCmd(x))
      .mapTo[NodeUpdated]
      .map(_.nodeId)
      .map {
        case Success(y) => y
        case Failure(e) => throw StorageException("Storage update exception")
      }

  def updateData(x: (PathStr, Value)*): Future[StorageLike] = ???

  def updateElement(nodePath: NodePath, x: AnyElement): Future[PersistenceId] = getNode(nodePath.nodeId).flatMap(updateElement(_, nodePath, x))

  private def updateElement(node: ActorRef, nodePath: NodePath, x: AnyElement): Future[PersistenceId] =
    ask(node, UpdateElementCmd(nodePath.path, x))
      .mapTo[NodeUpdated]
      .map(_.nodeId)
      .map {
        case Success(y) => y
        case Failure(e) => throw StorageException("Storage update exception")
      }

  def createElement(nodePath: NodePath, x: AnyElement): Future[StorageLike] = ???
  def createElement(nodePath: NodePathStr, x: AnyElement): Future[StorageLike] = ???
  def createElement(x: AnyElement): Future[StorageLike] = ???

  def deleteElement(nodePath: NodePath): Future[StorageLike] = ???
}

