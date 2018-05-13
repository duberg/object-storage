package storage

case class Repr(impl: Map[PathStr, ReprElement]) extends TypeChecker {
  def apply(path: Path): AnyElement = impl.get(path.pathStr) match {
    case Some(x) => x match {
      case x: AnySimpleElement @unchecked => x
      case x: RefMetadata => apply(x.ref)
      case x: Metadata => getComplexElement(x)
    }
    case _ => throw StorageException(s"Invalid path $path")
  }

  def getMetadata(path: Path): Metadata = impl.get(path.pathStr) match {
    case Some(x) => x match {
      case metadata: Metadata => metadata
      case _ => throw StorageException(s"Metadata not found")
    }
    case None => throw StorageException(s"Invalid path $path")
  }

  def getObjectMetadata(path: Path): ObjectMetadata = impl.get(path.pathStr) match {
    case Some(x) => x match {
      case metadata: ObjectMetadata => metadata
      case _ => throw StorageException(s"Invalid metadata")
    }
    case None => throw StorageException(s"Invalid path $path")
  }

  def getArrayMetadata(path: Path): ArrayMetadata = impl.get(path.pathStr) match {
    case Some(x) => x match {
      case metadata: ArrayMetadata => metadata
      case _ => throw StorageException(s"Invalid metadata")
    }
    case None => throw StorageException(s"Invalid path $path")
  }

  def getComplexElement(metadata: Metadata): ComplexElement = metadata match {
    case x: ObjectMetadata => getObjectElement(x)
    case x: ArrayMetadata => getArrayElement(x)
  }

  def getObjectElement(metadata: ObjectMetadata): ObjectElement = ObjectElement(
    name = metadata.name,
    description = metadata.description,
    repr = this,
    path = metadata.path)

  def getArrayElement(metadata: ArrayMetadata): ArrayElement = ArrayElement(
    name = metadata.name,
    description = metadata.description,
    repr = this,
    path = metadata.path)

  def getReferenceElement(metadata: RefMetadata): Ref = Ref(
    metadata.name,
    metadata.description,
    value = apply(metadata.ref),
    ref = metadata.ref,
    path = metadata.path)

  def updateValue(path: Path, value: Value, consistency: Consistency = Consistency.Strict): Repr = {
    consistency match {
      case Consistency.Strict =>
        impl.getOrElse(path.pathStr, throw StorageException(s"Invalid path $path")) match {
          case x: AnySimpleElement @unchecked => copy(impl ++ x.withValue(value).repr.impl)
          case x: Metadata => throw StorageException(s"Can't update ComplexElement")
        }
      case Consistency.Disabled =>
        value match {
          case x: Int => copy(impl + (path.pathStr -> IntElement(None, None, x, path)))
          case x: String => copy(impl + (path.pathStr -> StringElement(None, None, x, path)))
          case x: Boolean => copy(impl + (path.pathStr -> BooleanElement(None, None, x, path)))
          case x: BigDecimal => copy(impl + (path.pathStr -> DecimalElement(None, None, x, path)))
          case x => throw StorageException(s"Invalid pathStr $x")
        }
    }
  }

  def updateElement(path: Path, definition: AnyElement, consistency: Consistency = Consistency.Strict): Repr = {
    consistency match {
      case Consistency.Strict =>
        // fast read from impl to check type
        val reprElement = impl.getOrElse(path.pathStr, throw StorageException(s"Invalid path $path"))
        checkType(path.pathStr, reprElement, definition)
        reprElement match {
          case _: AnySimpleElement @unchecked =>
            copy(impl + (path.pathStr -> reprElement
              .withDescription(definition.description)
              .withValue(definition.value)))
          case m: Metadata =>
            // object impl
            val xImpl = impl.filterKeys(_ contains m.path.pathStr)
            definition match {
              case x: ObjectElement =>
                val objPath = m.path.pathStr.dropRight(m.path.name.length + 1)
                val yImpl = objPath match {
                  case "" => definition.repr.impl
                  case _ => definition.repr.impl.map({
                    case (k, v) =>
                      val p = s"$objPath.$k"
                      p -> v.withPath(Path(p))
                  })
                }
                // update impl
                val zImpl = (xImpl /: yImpl)({
                  case (accImpl, (yReprPath, yReprElement)) =>
                    val xReprElement = accImpl.getOrElse(yReprPath, throw StorageException(s"Invalid path $yReprPath"))
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

                val objPath = m.path.pathStr.dropRight(m.name.size + 1)

                val yImpl = objPath match {
                  case "" => definition.repr.impl
                  case _ => definition.repr.impl.map({
                    case (k, v) =>
                      val p = s"$objPath.$k"
                      p -> v.withPath(Path(p))
                  })
                }
                // update impl
                val zImpl = (xImpl /: yImpl)({
                  case (accImpl, (yReprPath, yReprElement)) =>
                    val xReprElement = accImpl.getOrElse(yReprPath, throw StorageException(s"Invalid path $yReprPath"))
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

  def addElement(path: Path, definition: AnyElement): Repr = {
    if (definition.isSimple) copy(impl ++ definition.repr.impl)
    else {
      // create all metadata objects for path
      val (metadataObjs, _) = ((List[Metadata](), "") /: Path(s"${path.pathStr}.${definition.path.pathStr}").elements) {
        case ((metadataList, accPath), metadataPath) =>
          val path = if (accPath.isEmpty) metadataPath else s"$accPath.$metadataPath"
          (metadataList :+ ObjectMetadata(None, None, path)) -> path
      }

      val metadataObjsImpl = metadataObjs
        .map(metadata => metadata.path.pathStr -> metadata)
        .toMap
        .filterKeys(impl.get(_).isEmpty)

      val definitionImpl = definition.withPath(path).repr.impl

      // merge maps
      copy(impl ++ definitionImpl ++ metadataObjsImpl)
    }
  }

  def addElement(x: AnyElement): Repr = {
    if (x.isSimple) copy(impl ++ x.repr.impl)
    // create all metadata objects for path
    val (metadataObjs, _) = ((List[Metadata](), "") /: x.path.elements) {
      case ((metadataList, accPath), metadataPath) =>
        val path = if (accPath.isEmpty) metadataPath else s"$accPath.$metadataPath"
        (metadataList :+ ObjectMetadata(None, None, path)) -> path
    }

    val metadataObjsImpl = metadataObjs
      .map(metadata => metadata.path.pathStr -> metadata)
      .toMap
      .filterKeys(impl.get(_).isEmpty)

    val definitionImpl = x.repr.impl

    // merge maps
    copy(impl ++ definitionImpl ++ metadataObjsImpl)
  }

  def rootMetadata = ObjectMetadata(Some("root"), None, "$")

  def withRootMetadata: Repr = {
    val objImpl = impl.map({
      case (k, v: RefMetadata) =>
        val path = Path(s"$$.$k")
        val ref = Path(s"$$.${v.ref}")
        path.pathStr -> v.withPath(path).withRef(ref)
      case (k, v) =>
        val path = Path(s"$$.$k")
        path.pathStr -> v.withPath(path)
    })
    copy(objImpl + (rootMetadata.path.pathStr -> rootMetadata))
  }

  def root: ObjectElement =
    withRootMetadata
      .getObjectElement(rootMetadata)
      .withPath(Path.root)
}

object Repr {
  def empty: Repr = new Repr(Map.empty)
  def apply(element: (PathStr, ReprElement)): Repr = new Repr(Map(element))
}

trait ReprElement {
  def path: Path
  def withValue(value: Value): ReprElement
  def withDescription(description: Description): ReprElement
  def withPath(path: Path): ReprElement
  def asStorageElement: AnySimpleElement = this match {
    case x: IntElement => x
    case x: StringElement => x
    case x: BooleanElement => x
    case x: DecimalElement => x
  }
}

case class ReprPaths(reprElement: ReprElement, pathElements: Seq[String])
