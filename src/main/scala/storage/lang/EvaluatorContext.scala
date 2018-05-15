package storage.lang

import storage.StorageSystem
import storage.actor.persistence.PersistenceId

case class EvaluatorContext(storage: StorageSystem, mappings: Map[String, PersistenceId])