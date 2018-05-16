package storage.actor.persistence

import storage.{ PathExtractor, PathLike, PathStr }

/**
 * PersistenceId is full path to actor
 */
case class PersistenceId(elements: List[String]) extends PathLike

object PersistenceId extends PathExtractor {
  def apply(path: PathStr): PersistenceId = PersistenceId(pathElements(path))
}
