
import storage._
import storage.json._

object FlatReprExampleApp extends App {
  val storage = Storage(
    StringElement(Some("name1"), Some("desc1"), "name1", "x1"),
    ObjectMetadata(Some("name1"), Some("desc1"), "form1"),
    ObjectMetadata(None, None, "form1.data"),
    ObjectMetadata(None, None, "form1.data.title"),
    StringElement("title1", "form1.data.title.ru"),
    StringElement(None, None, "Title", "form1.data.title.en"),
    BooleanElement(None, None, value = false, "form1.check"),
    ObjectMetadata(None, None, "form1.parent"),
    StringElement(None, None, "firstname", "form1.parent.firstname"),
    StringElement(None, None, "lastname", "form1.parent.lastname"),
    StringElement(None, None, "middlename", "form1.parent.middlename"),
    StringElement(None, None, "lastname", "form1.lastname"),
    StringElement(None, None, "middlename", "form1.middlename"),
    ArrayMetadata(None, None, "form1.files"),
    StringElement(None, None, "https://github.com/duberg/object-storage", "form1.files[0]"),
    StringElement(None, None, "https://github.com/duberg/object-storage", "form1.files[2]"),
    StringElement(None, None, "https://github.com/duberg/object-storage", "form1.files[1]"),
    // reference example
    RefMetadata(None, Option("Reference to parent object"), "form1.parent", "form1.parent2"),
    BooleanElement(value = false, "isEmployee"))

  val obj1 = ObjectElement(
    StringElement("xx", "parent.firstname"),
    StringElement("xx", "parent.lastname"),
    StringElement("xx", "parent.middlename"))

  val storageUpdated = storage
    .updateData(
      "form1.data.title.ru" -> "+++",
      "form1.parent.firstname" -> "+++")
    .updateData("isEmployee" -> true)
    .updateElement("form1.parent.middlename", StringElement(Some("path"), Some("desc"), "m", "form1.parent.middlename"))
    .updateElement("form1.parent", obj1)

  println(storageUpdated.asJsonStr)
  println(storageUpdated.prettify)
  println()

  println("=== storage representation ===")
  println(storageUpdated.repr.impl.mkString("\n"))

  val updatedFiles: ArrayElement = storageUpdated
    .getArrayElement("form1.files")
  // .createElement(StringElement("newfile", None, "", "newfile"))

  val newStorage = Storage.empty
    .createElement("x", storageUpdated("form1.parent")) // add object element
    .createElement("x.form1.parent.y", storageUpdated("x1")) // add simple element
    .createElement(storageUpdated("form1")) // add object element to root
    .createElement(storageUpdated("x1")) // add to root
  //.updateElement("form1.files", updatedFiles)

  println()
  println("=== newStorage ===")
  println(newStorage.prettify)
}
