package storage.json

import io.circe._
import io.circe.syntax._
import storage._

trait Codec {
  implicit val encodeAny: Encoder[Any] = {
    case x: String => x.asJson
    case x: Int => x.asJson
    case x: Boolean => x.asJson
    case x: BigDecimal => x.asJson
    case x: StringElement => x.asJson
    case x: IntElement => x.asJson
    case x: BooleanElement => x.asJson
    case x: DecimalElement => x.asJson
    case x: ObjectElement => x.asJson
    case x: ArrayElement => x.asJson
    case x: Ref => x.asJson
    case x: Map[String, Any] @unchecked => x.asJson
  }

  implicit val encodeRepr: Encoder[Repr] = (x: Repr) => Json.obj(x.impl.mapValues(_.asJson).toSeq: _*)

  implicit val encodeReprElement: Encoder[ReprElement] = {
    case x: AnySimpleElement @unchecked => x.asJson
    case x: Metadata => x.asJson
  }

  //  implicit val encodeReprElements: Encoder[ReprElements] = (nodeId: ReprElements) => Json.obj(
  //    nodeId.mapValues(_.asJson).toSeq: _*)

  implicit val encodeAnyElements: Encoder[AnyElements] = (x: AnyElements) => Json.obj(
    x.mapValues(_.asJson).toSeq: _*)

  implicit val decodeAnyElements: Decoder[AnyElements] = Decoder.decodeMapLike

  implicit val encodeMetadata: Encoder[Metadata] = {
    case x: ObjectMetadata => x.asJson
    case x: ArrayMetadata => x.asJson
  }

  //  implicit val decodeRepr: Decoder[Repr] =
  //    (c: HCursor) => for (nodeId <- c.downField("impl").as[ReprElements]) yield Repr(nodeId)

  //  implicit val decodeRepr: Decoder[Repr] = Decoder.decodeMapLike(
  //    dk = decodeKeyPath,
  //    dv = decodeAnySimpleElement,
  //    cbf = Map.canBuildFrom)

  implicit val encodePath: Encoder[Path] = (x: Path) => x.pathStr.asJson

  implicit val encodeStringElement: Encoder[StringElement] = (x: StringElement) => Json.obj(
    ("name", x.name.asJson),
    ("description", x.description.asJson),
    ("value", x.value.asJson),
    ("path", x.path.asJson),
    ("type", StringElement.typeName.asJson))

  implicit val encodeIntElement: Encoder[IntElement] = (x: IntElement) => Json.obj(
    ("name", x.name.asJson),
    ("description", x.description.asJson),
    ("value", x.value.asJson),
    ("path", x.path.asJson),
    ("type", IntElement.typeName.asJson))

  implicit val encodeBooleanElement: Encoder[BooleanElement] = (x: BooleanElement) => Json.obj(
    ("name", x.name.asJson),
    ("description", x.description.asJson),
    ("value", x.value.asJson),
    ("path", x.path.asJson),
    ("type", BooleanElement.typeName.asJson))

  implicit val encodeDecimalElement: Encoder[DecimalElement] = (x: DecimalElement) => Json.obj(
    ("name", x.name.asJson),
    ("description", x.description.asJson),
    ("value", x.value.asJson),
    ("path", x.path.asJson),
    ("type", DecimalElement.typeName.asJson))

  implicit val encodeObjectMetadata: Encoder[ObjectMetadata] = (x: ObjectMetadata) => Json.obj(
    ("name", x.name.asJson),
    ("description", x.description.asJson),
    ("path", x.path.asJson),
    ("type", ObjectMetadata.typeName.asJson))

  implicit val encodeArrayMetadata: Encoder[ArrayMetadata] = (x: ArrayMetadata) => Json.obj(
    ("name", x.name.asJson),
    ("description", x.description.asJson),
    ("path", x.path.asJson),
    ("type", ArrayMetadata.typeName.asJson))

  implicit val encodeDataElement: Encoder[DataElement] = (x: DataElement) => Json.obj(
    ("path", x.path.asJson),
    ("value", x.value.asJson))

  implicit val decodeDataElement: Decoder[DataElement] = (c: HCursor) => for {
    path <- c.downField("path").as[String]
    value <- c.downField("value").as[String]
  } yield DataElement(path, value)

  implicit val encodeObjectElement: Encoder[ObjectElement] = (x: ObjectElement) => Json.obj(
    ("name", x.name.asJson),
    ("description", x.description.asJson),
    ("value", x.value.asJson),
    ("path", x.path.asJson),
    ("type", ObjectElement.typeName.asJson))

  implicit val decodeObjectElement: Decoder[ObjectElement] = (c: HCursor) => for {
    name <- c.downField("name").as[Name]
    description <- c.downField("description").as[Description]
    value <- c.downField("value").as[AnyElements]
    path <- c.downField("path").as[PathStr]
  } yield ObjectElement(
    name = name,
    description = description,
    value = value,
    path = path)

  implicit val encodeArrayElement: Encoder[ArrayElement] =
    (x: ArrayElement) => Json.arr(x.value.mapValues(_.asJson).values.toSeq: _*)

  implicit val encodeReferenceElement: Encoder[Ref] = (x: Ref) => Json.obj(
    ("name", x.name.asJson),
    ("description", x.description.asJson),
    ("value", x.value.asJson),
    ("ref", x.ref.asJson),
    ("path", x.path.asJson),
    ("type", Ref.typeName.asJson))

  implicit val encodeAnyElement: Encoder[AnyElement] = (x: AnyElement) => {
    val typeName = x match {
      case _: StringElement => StringElement.typeName.asJson
      case _: IntElement => IntElement.typeName.asJson
      case _: BooleanElement => BooleanElement.typeName.asJson
      case _: DecimalElement => DecimalElement.typeName.asJson
      case _: ObjectElement => ObjectElement.typeName.asJson
      case _: ArrayElement => ArrayElement.typeName.asJson
    }
    Json.obj(
      ("name", x.name.asJson),
      ("description", x.description.asJson),
      ("value", x.value.asJson),
      ("path", x.path.asJson),
      ("type", typeName))
  }

  implicit val encodeAnySimpleElement: Encoder[AnySimpleElement] = (x: AnySimpleElement) => {
    //    val typeName = nodeId match {
    //      case _: StringElement => StringElement.typeName.asJson
    //      case _: IntElement => IntElement.typeName.asJson
    //      case _: BooleanElement => BooleanElement.typeName.asJson
    //      case _: DecimalElement => DecimalElement.typeName.asJson
    //    }
    Json.obj(
      ("name", x.name.asJson),
      ("description", x.description.asJson),
      ("value", x.value.asJson),
      ("path", x.path.asJson))
  }

  implicit val decodeAnyElement: Decoder[AnyElement] = (c: HCursor) => {
    for {
      name <- c.downField("name").as[Name]
      description <- c.downField("description").as[Description]
      value <- c.downField("value").as[Value]
      path <- c.downField("path").as[PathStr]
      t <- c.downField("type").as[String]
    } yield {
      t match {
        case StringElement.typeName =>
          StringElement(
            name = name,
            description = description,
            value = value,
            path = path)
        case IntElement.typeName =>
          IntElement(
            name = name,
            description = description,
            value = value,
            path = path)
        case BooleanElement.typeName =>
          BooleanElement(
            name = name,
            description = description,
            value = value,
            path = path)
        case DecimalElement.typeName =>
          DecimalElement(
            name = name,
            description = description,
            value = value,
            path = path)
        case ObjectElement.typeName =>
          // println("hhhhhhhhh")
          ObjectElement.empty(path)
        case ArrayElement.typeName => ArrayElement.empty(path)
        case Ref.typeName => ArrayElement.empty(path)
      }
    }
  }

  implicit val decodeAny: Decoder[Any] = (c: HCursor) => {
    // println(c.focus.get)
    c.focus.get match {
      case x if x.isNull => null
      case x if x.isNumber => x.as[Double]
      case x if x.isBoolean => x.as[Boolean]
      case x if x.isString => x.as[String]
      case x if x.isObject =>
        val c = x.hcursor
        // decodeAnyElement(nodeId.hcursor)
        x.as[Map[String, Any]](Decoder.decodeMapLike(KeyDecoder.decodeKeyString, decodeAny, Map.canBuildFrom))
      case x if x.isArray =>
        x.as[Map[String, Any]](Decoder.decodeMapLike(KeyDecoder.decodeKeyString, decodeAny, Map.canBuildFrom))
    }
  }

  implicit val encodeStorage: Encoder[Storage] = (x: Storage) => x.root.value.asJson

  implicit val decodeStorage: Decoder[Storage] =
    Decoder
      .decodeMapLike(KeyDecoder.decodeKeyString, decodeAnyElement, Map.canBuildFrom[String, AnyElement])
      .map(x => Storage(Repr(x.values.flatMap(_.repr.impl).toMap)))
}
