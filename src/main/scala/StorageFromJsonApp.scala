
import storage._
import storage.json._

import scala.io.Source
import io.circe.parser.decode

object StorageFromJsonApp extends App {
  val json = Source.fromURL(getClass.getResource("/storage.json"))
    .getLines()
    .mkString

  //println(decode[ObjectElement](json))

  val storage = Storage.empty

  //  val obj1 = ObjectElement(
  //    StringElement("xx", "parent.firstname"),
  //    StringElement("xx", "parent.lastname"),
  //    StringElement("xx", "parent.middlename"))
  //
  //  val storageUpdated = storage
  //    .updateData(
  //      "form1.data.title.ru" -> "+++",
  //      "form1.parent.firstname" -> "+++")
  //    .updateData("isEmployee" -> true)
  //    .updateElement("form1.parent.middlename", StringElement(Some("name"), Some("desc"), "m", "form1.parent.middlename"))
  //    .updateElement("form1.parent", obj1)
  //
  //  println(storageUpdated.root.asJsonStr)
  //  println(storageUpdated.prettify)
  //  println()
  //
  //  println("=== storage representation ===")
  //  storageUpdated.repr.impl.foreach({
  //    case (k, v) => println(s"$k -> $v")
  //  }
}
