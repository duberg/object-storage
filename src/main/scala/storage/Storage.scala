package storage

import akka.actor.ActorRef
import storage.actor.persistence.Persistence._
import storage.actor.persistence.PersistenceId

import scala.util.Try

/**
 * = Object storage =
 *
 *  - Data consistency
 *  - Fast read and write
 *  - Fast akka persistence serialization
 */
case class Storage(repr: Repr = Repr.empty) extends StorageLike {
  def apply(path: Path): AnyElement = repr(path)

  def apply(path: PathStr): AnyElement = repr(Path(path))

  def getBoolean(path: Path): Boolean = repr(path) match {
    case x: BooleanElement => x.value
    case x: StringElement => x.value.toBoolean
  }

  def getInt(path: Path): Int = repr(path) match {
    case x: IntElement => x.value
    case x: StringElement => x.value.toInt
  }

  def getString(path: Path): String = repr(path) match {
    case x: StringElement => x.value
    case x => x.toString
  }

  def getDecimal(path: Path): BigDecimal = repr(path) match {
    case x: DecimalElement => x.value
    case x: IntElement => x.value
    case x: StringElement => x.value.toInt
  }

  def getElement(path: Path): AnyElement = apply(path)

  def getComplexElement(path: Path): ComplexElement = repr.getComplexElement(repr.getMetadata(path))

  def getObjectElement(path: Path): ObjectElement = repr.getObjectElement(repr.getObjectMetadata(path))

  def getArrayElement(path: Path): ArrayElement = repr.getArrayElement(repr.getArrayMetadata(path))

  def getDataElement(path: Path): DataElement = DataElement(path, apply(path).value)

  def getData(paths: Paths): Data = Data(paths.map(path => DataElement(path, apply(path).value)).toSet)

  def updateElement(path: Path, x: AnyElement): Storage = copy(repr.updateElement(path, x))

  def updateDataElement(x: DataElement): Storage = copy(repr.updateValue(x.path, x.value))

  def updateData(x: Data): Storage = copy((repr /: x.elements) { case (r, d) => r.updateValue(d.path, d.value) })

  def updateData(x: (PathStr, Value)*): Storage = x match {
    case Seq() => this
    case Seq((path, value)) => updateDataElement(DataElement(path, value))
    case Seq(a, as @ _*) => updateData(Data(x.toMap))
  }

  def createElement(x: AnyElement): Storage = copy(repr.addElement(x))

  def createElement(path: Path, x: AnyElement): Storage = copy(repr.addElement(path, x))

  def root: ObjectElement = repr.root

  def prettify: String = s"[${getClass.getSimpleName}]".yellow + root.prettify

  def updateElement(path: PathStr, x: AnyElement): Storage = updateElement(Path(path), x)

  def createElement(path: PathStr, x: AnyElement): Storage = createElement(Path(path), x)

  //  def addData(createProcessNode: (PathStr, Value)*): StorageLike = createProcessNode match {
  //    case Seq() => this
  //    case Seq((path, value)) => addDataElement(DataElement(path, value))
  //    case Seq(a, as @ _*) => addData(Data(createProcessNode.toMap))
  //  }
  //
  //  repr.impl.foreach({
  //    case (path, reprElement) => require(path == reprElement.path, s"Invalid reprElement $reprElement: require same path")
  //  })

  def deleteElement(path: Path) = ???
}

object Storage {
  trait Command extends PersistentCommand
  trait Request extends PersistentRequest
  trait Response extends PersistentResponse
  trait Event extends PersistentEvent

  case class GetInt(path: Path) extends Request
  case class GetString(path: Path) extends Request
  case class GetBoolean(path: Path) extends Request
  case class GetDecimal(path: Path) extends Request
  case class GetElement(path: Path) extends Request
  case class GetRoot(path: Path) extends Request

  case class UpdateDataElementCmd(x: DataElement) extends Command
  case class UpdateDataCmd(x: Data) extends Command
  case class UpdateElementCmd(path: Path, x: AnyElement) extends Command

  case class UpdatedDataElementEvt(x: DataElement) extends Event
  case class UpdatedDataEvt(x: Data) extends Event
  case class UpdatedElementEvt(path: Path, x: AnyElement) extends Event

  case class IntOpt(x: Option[Int]) extends Response
  case class StringOpt(x: Option[String]) extends Response
  case class BooleanOpt(x: Option[Boolean]) extends Response
  case class DecimalOpt(x: Option[BigDecimal]) extends Response
  case class ElementOpt(x: Option[AnyElement]) extends Response
  case class Root(x: ObjectElement) extends Response
  case class NodeUpdated(nodeId: Try[PersistenceId]) extends Response

  def empty: Storage = Storage(Repr.empty)
  def apply(x: Map[PathStr, ReprElement]): Storage = Storage(storage.Repr(x))
  def apply(x: ReprElement*): Storage = Storage(Repr(x.map(r => r.path.pathStr -> r).toMap))
}

