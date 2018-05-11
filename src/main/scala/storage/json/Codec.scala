package storage.json

import io.circe._
import io.circe.syntax._
import storage._

trait Codec {
  //  implicit val encodeMapStringAny: Encoder[Map[String, Any]] = (x: Map[String, Any]) => Json.obj(
  //    x.mapValues(_.asJson).toSeq: _*)

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

  implicit val decodeAny: Decoder[Any] = (c: HCursor) => {
    val value = c.value
    val booleanOpt = value.asBoolean
    val numberOpt = value.asNumber.flatMap { x =>
      val intOpt = x.toInt
      val biggerDecimalOpt = x.toBigDecimal
      Seq(intOpt, biggerDecimalOpt).flatten.headOption
    }
    val stringOpt = value.asString
    val decodedOpt = Seq(booleanOpt, numberOpt, stringOpt).flatten.headOption
    decodedOpt match {
      case Some(x) => Right(x)
      case _ => Left(DecodingFailure("Any", c.history))
    }
  }

  implicit val encodeStorage: Encoder[Storage] = (x: Storage) => x.root.value.asJson

  //  implicit val decodeStorage: Decoder[Storage] =
  //    (c: HCursor) => for (x <- c.downField("repr").as[Repr]) yield Storage(x)

  implicit val encodeRepr: Encoder[Repr] = (x: Repr) => Json.obj(x.impl.mapValues(_.asJson).toSeq: _*)

  implicit val encodeReprElement: Encoder[ReprElement] = {
    case x: AnySimpleElement @unchecked => x.asJson
    case x: Metadata => x.asJson
  }

  //  implicit val encodeReprElements: Encoder[ReprElements] = (x: ReprElements) => Json.obj(
  //    x.mapValues(_.asJson).toSeq: _*)

  implicit val encodeAnyElements: Encoder[AnyElements] = (x: AnyElements) => Json.obj(
    x.mapValues(_.asJson).toSeq: _*)

  implicit val encodeMetadata: Encoder[Metadata] = {
    case x: ObjectMetadata => x.asJson
    case x: ArrayMetadata => x.asJson
  }

  //  implicit val decodeKeyPath: KeyDecoder[Path] = (key: Path) => Some(key)

  //  implicit val decodeAnySimpleElement: Decoder[AnySimpleElement] = (c: HCursor) => for {
  //    name <- c.downField("name").as[Name]
  //    description <- c.downField("description").as[Description]
  //    value <- c.downField("value").as[Value]
  //    path <- c.downField("path").as[PathStr]
  //    typeField <- c.downField("type").as[String]
  //  } yield typeField match {
  //    case StringElement.typeName =>
  //      StringElement(
  //        name = name,
  //        description = description,
  //        value = value,
  //        path = path)
  //    case IntElement.typeName =>
  //      IntElement(
  //        name = name,
  //        description = description,
  //        value = value,
  //        path = path)
  //    case BooleanElement.typeName =>
  //      BooleanElement(
  //        name = name,
  //        description = description,
  //        value = value,
  //        path = path)
  //    case DecimalElement.typeName =>
  //      DecimalElement(
  //        name = name,
  //        description = description,
  //        value = value,
  //        path = path)
  //  }

  //  implicit val decodeRepr: Decoder[Repr] =
  //    (c: HCursor) => for (x <- c.downField("impl").as[ReprElements]) yield Repr(x)

  //  implicit val decodeRepr: Decoder[Repr] = Decoder.decodeMapLike(
  //    dk = decodeKeyPath,
  //    dv = decodeAnySimpleElement,
  //    cbf = Map.canBuildFrom)

  implicit val encodePath: Encoder[Path] = (x: Path) => Json.obj(
    ("path", x.asJson))

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
    value <- c.downField("value").as[Any]
  } yield DataElement(path, value)

  //  implicit val decodeObjectElement: Decoder[DataElement] = (c: HCursor) => for {
  //    path <- c.downField("path").as[String]
  //    value <- c.downField("value").as[Any]
  //  } yield DataElement(path, value)

  implicit val encodeObjectElement: Encoder[ObjectElement] = (x: ObjectElement) => Json.obj(
    ("name", x.name.asJson),
    ("description", x.description.asJson),
    ("value", x.value.asJson),
    ("path", x.path.asJson),
    ("type", ObjectElement.typeName.asJson))

  implicit val encodeArrayElement: Encoder[ArrayElement] =
    (x: ArrayElement) => Json.obj(x.value.mapValues(_.asJson).toSeq: _*)

  implicit val encodeReferenceElement: Encoder[Ref] = (x: Ref) => Json.obj(
    ("name", x.name.asJson),
    ("description", x.description.asJson),
    ("value", x.value.asJson),
    ("path", x.path.asJson),
    ("type", Ref.typeName.asJson))

  implicit val encodeAnyElement: Encoder[AnyElement] = (x: AnyElement) => {
    val typeName = x match {
      case _: StringElement => StringElement.typeName.asJson
      case _: IntElement => IntElement.typeName.asJson
      case _: BooleanElement => BooleanElement.typeName.asJson
      case _: DecimalElement => DecimalElement.typeName.asJson
      case x: ObjectElement => ObjectElement.typeName.asJson
      case x: ArrayElement => ArrayElement.typeName.asJson
    }
    Json.obj(
      ("name", x.name.asJson),
      ("description", x.description.asJson),
      ("value", x.value.asJson),
      ("path", x.path.asJson),
      ("type", typeName))
  }

  implicit val encodeAnySimpleElement: Encoder[AnySimpleElement] = (x: AnySimpleElement) => {
    //    val typeName = x match {
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
}
