package storage

import storage.actor.persistence.PersistenceId

case class DataElement(path: Path, value: Value, nodeIdOpt: Option[PersistenceId] = None) {
  def nodeId = nodeIdOpt.getOrElse(throw StorageException("DataElement does not contain nodeId"))
}

object DataElement {
  def apply(pathStr: PathStr, value: Value): DataElement = DataElement(Path(pathStr), value)
  def apply(nodePath: NodePath, value: Value): DataElement = DataElement(nodePath.path, value, Some(nodePath.nodeId))
}
