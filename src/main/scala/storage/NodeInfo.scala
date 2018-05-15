package storage

import storage.actor.persistence.PersistenceId

case class NodeInfo(
  id: PersistenceId,
  name: Name,
  description: Description,
  status: StorageStatus)

case class CreateNode(
  id: PersistenceId,
  name: Name,
  description: Description,
  storage: Storage)

case class UpdateNodeInfo(
  id: PersistenceId,
  name: Option[Name] = None,
  description: Option[Description] = None,
  status: Option[StorageStatus] = None)
