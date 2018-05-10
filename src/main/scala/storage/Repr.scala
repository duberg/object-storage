package storage

import storage.Implicits._

case class Repr(impl: Map[PathStr, ReprElement]) extends TypeChecker {
  def apply(path: PathStr): AnyElement = impl.get(path) match {
    case Some(x) => x match {
      case x: AnySimpleElement @unchecked => x
      case x: Metadata => getComplexElement(x)
    }
    case _ => throw StorageException(s"Invalid path $path")
  }

  def getMetadata(path: PathStr): Metadata = impl.get(path) match {
    case Some(x) => x match {
      case metadata: Metadata => metadata
      case _ => throw StorageException(s"Metadata not found")
    }
    case None => throw StorageException(s"Invalid path $path")
  }

  def getObjectMetadata(path: PathStr): ObjectMetadata = impl.get(path) match {
    case Some(x) => x match {
      case metadata: ObjectMetadata => metadata
      case _ => throw StorageException(s"Invalid metadata")
    }
    case None => throw StorageException(s"Invalid path $path")
  }

  def getArrayMetadata(path: PathStr): ArrayMetadata = impl.get(path) match {
    case Some(x) => x match {
      case metadata: ArrayMetadata => metadata
      case _ => throw StorageException(s"Invalid metadata")
    }
    case None => throw StorageException(s"Invalid path $path")
  }

  private def traverse[T <: ComplexElement](obj: T, x: List[ReprPaths]): T = {
    def group(x: List[ReprPaths]): Map[PathStr, List[ReprPaths]] = x.groupBy(_.paths.head)
    def acc(obj: T, group: (PathStr, List[ReprPaths])): T = {
      def convert(group: (PathStr, List[ReprPaths])): AnyElement = {
        group match {
          // group is simple element
          case (_, ReprPaths(v, _) :: Nil) => v match {
            case x: IntElement => x
            case x: StringElement => x
            case x: BooleanElement => x
            case x: DecimalElement => x
          }
          // group is object element
          case (groupName, groupElements) =>
            groupElements
              .find(_.paths.size == 1)
              .map(_.reprElement)
              .map {
                case x: ObjectMetadata => getObjectElement(x)
                case x: ArrayMetadata => getArrayElement(x)
              }
              .get
        }
      }
      obj.withElement(group._1, convert(group)).asInstanceOf[T]
    }

    (obj /: group(x))(acc)
  }

  /**
   * - Разбиваем полный путь в список путей
   * - Группируем по первому элементу списка
   * - Математическая свертка по группам:
   *   - Если группа состоит из одного элемента, значит это простой элемент
   *   - Если группа состоит из больше чем одного элемента значит это объект
   */
  def getComplexElement(metadata: Metadata): ComplexElement = metadata match {
    case x: ObjectMetadata => getObjectElement(x)
    case x: ArrayMetadata => getArrayElement(x)
  }

  def getObjectElement(metadata: ObjectMetadata): ObjectElement = {
    val objPath = s"${metadata.path}."

    val objRepr: Map[PathStr, ReprElement] = impl
      .filterKeys(_ contains objPath)
      .map({ case (k, v) => (k drop objPath.length, v) })

    val reprPaths: List[ReprPaths] = (List[ReprPaths]() /: objRepr) {
      case (y, (k, v)) => y :+ ReprPaths(v, k.paths)
    }

    val obj = ObjectElement(metadata.name, metadata.description, Map[PathStr, AnyElement](), metadata.path)

    traverse(obj, reprPaths)
  }

  def getArrayElement(metadata: ArrayMetadata): ArrayElement = {
    val objPath = s"${metadata.path}["

    val objRepr: Map[PathStr, ReprElement] = impl
      .filterKeys(_ contains objPath)
      .map({ case (k, v) => (k drop objPath.length - 1, v) })

    val reprPaths: List[ReprPaths] = (List[ReprPaths]() /: objRepr) {
      case (y, (k, v)) => y :+ ReprPaths(v, k.paths)
    }

    val obj = ArrayElement(metadata.name, metadata.description, Map.empty, metadata.path)

    traverse(obj, reprPaths)
  }

  def updateValue(path: PathStr, value: Value, consistency: Consistency = Consistency.Strict): Repr = {
    consistency match {
      case Consistency.Strict =>
        impl.getOrElse(path, throw StorageException(path, s"Invalid path $path")) match {
          case x: AnySimpleElement @unchecked => copy(impl ++ x.withValue(value).repr.impl)
          case x: Metadata => throw StorageException(path, s"Can't update ComplexElement")
        }
      case Consistency.Disabled =>
        value match {
          case x: Int => copy(impl + (path -> IntElement(None, None, x, path)))
          case x: String => copy(impl + (path -> StringElement(None, None, x, path)))
          case x: Boolean => copy(impl + (path -> BooleanElement(None, None, x, path)))
          case x: BigDecimal => copy(impl + (path -> DecimalElement(None, None, x, path)))
          case x => throw StorageException(path, s"Invalid pathStr $x")
        }
    }
  }

  def updateElement(path: PathStr, definition: AnyElement, consistency: Consistency = Consistency.Strict): Repr = {
    consistency match {
      case Consistency.Strict =>
        // fast read from impl to check type
        val reprElement = impl.getOrElse(path, throw StorageException(path, s"Invalid path $path"))
        checkType(path, reprElement, definition)
        reprElement match {
          case _: AnySimpleElement @unchecked =>
            copy(impl + (path -> reprElement
              .withDescription(definition.description)
              .withValue(definition.value)))
          case m: Metadata =>
            // object impl
            val xImpl = impl.filterKeys(_ contains s"${m.path}")
            definition match {
              case x: ObjectElement =>
                val objPath = m.path.dropRight(m.path.headPathStr.size + 1)
                val yImpl = objPath match {
                  case "" => definition.repr.impl
                  case _ => definition.repr.impl.map({
                    case (k, v) =>
                      val p = s"$objPath.$k"
                      p -> v.withPath(p)
                  })
                }
                // update impl
                val zImpl = (xImpl /: yImpl)({
                  case (accImpl, (yReprPath, yReprElement)) =>
                    val xReprElement = accImpl.getOrElse(yReprPath, throw StorageException(yReprPath, s"Invalid path $yReprPath"))
                    checkType(yReprPath, xReprElement, yReprElement)
                    xReprElement match {
                      case _: AnySimpleElement @unchecked =>
                        val yReprElementCast = yReprElement.asInstanceOf[AnySimpleElement]
                        accImpl + (yReprPath -> xReprElement
                          .withDescription(yReprElementCast.description)
                          .withValue(yReprElementCast.value))
                      case _: Metadata =>
                        accImpl + (yReprPath -> xReprElement.withDescription(definition.description))
                    }
                })
                copy(impl ++ zImpl)
              case x: ArrayElement =>

                val objPath = m.path.dropRight(m.name.size + 1)
                println(objPath)
                val yImpl = objPath match {
                  case "" => definition.repr.impl
                  case _ => definition.repr.impl.map({
                    case (k, v) =>
                      val p = s"$objPath.$k"
                      p -> v.withPath(p)
                  })
                }
                // update impl
                val zImpl = (xImpl /: yImpl)({
                  case (accImpl, (yReprPath, yReprElement)) =>
                    val xReprElement = accImpl.getOrElse(yReprPath, throw StorageException(yReprPath, s"Invalid path $yReprPath"))
                    checkType(yReprPath, xReprElement, yReprElement)
                    xReprElement match {
                      case _: AnySimpleElement @unchecked =>
                        val yReprElementCast = yReprElement.asInstanceOf[AnySimpleElement]
                        accImpl + (yReprPath -> xReprElement
                          .withDescription(yReprElementCast.description)
                          .withValue(yReprElementCast.value))
                      case _: Metadata =>
                        accImpl + (yReprPath -> xReprElement.withDescription(definition.description))
                    }
                })
                copy(impl ++ zImpl)
            }
        }
      case Consistency.Disabled =>
        addElement(path, definition)
    }
  }

  def addElement(path: PathStr, definition: AnyElement): Repr = {
    if (definition.isSimple) copy(impl ++ definition.repr.impl)
    else {
      // create all metadata objects for path
      val (metadataObjs, _) = ((List[Metadata](), "") /: s"$path.${definition.path}".paths) {
        case ((metadataList, accPath), metadataPath) =>
          val path = if (accPath.isEmpty) metadataPath else s"$accPath.$metadataPath"
          (metadataList :+ ObjectMetadata(None, None, path)) -> path
      }

      val metadataObjsImpl = metadataObjs
        .map(metadata => metadata.path -> metadata)
        .toMap
        .filterKeys(impl.get(_).isEmpty)

      val definitionImpl = definition.withPath(path).repr.impl

      // merge maps
      copy(impl ++ definitionImpl ++ metadataObjsImpl)
    }
  }

  def addElement(definition: AnyElement): Repr = {
    if (definition.isSimple) copy(impl ++ definition.repr.impl)
    // create all metadata objects for path
    val (metadataObjs, _) = ((List[Metadata](), "") /: s"${definition.path}".paths) {
      case ((metadataList, accPath), metadataPath) =>
        val path = if (accPath.isEmpty) metadataPath else s"$accPath.$metadataPath"
        (metadataList :+ ObjectMetadata(None, None, path)) -> path
    }

    val metadataObjsImpl = metadataObjs
      .map(metadata => metadata.path -> metadata)
      .toMap
      .filterKeys(impl.get(_).isEmpty)

    val definitionImpl = definition.repr.impl

    // merge maps
    copy(impl ++ definitionImpl ++ metadataObjsImpl)
  }
}

object Repr {
  def empty: Repr = new Repr(Map.empty)
  def apply(element: (PathStr, ReprElement)): Repr = new Repr(Map(element))
}

trait ReprElement {
  def path: PathStr
  def withValue(value: Value): ReprElement
  def withDescription(description: Description): ReprElement
  def withPath(path: PathStr): ReprElement
}

case class ReprPaths(reprElement: ReprElement, paths: List[PathStr])
