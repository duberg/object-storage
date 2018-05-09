
import storage.model._

object StorageApp extends App {

  //println(Path("$.form1.files[0]").name)

  val storage = Storage(
    "name" -> StringDefinition("name", None, "name", "name"),
    "form1" -> ObjectMetadata("form1", None, "form1"),
    "form1.data" -> ObjectMetadata("data", None, "form1.data"),
    "form1.data.title" -> ObjectMetadata("title", None, "form1.data.title"),
    "form1.data.title.ru" -> StringDefinition("ru", None, "Название", "form1.data.title.ru"),
    "form1.data.title.en" -> StringDefinition("en", None, "Title", "form1.data.title.en"),
    "form1.a" -> BooleanDefinition("firstname", None, value = false, "form1.firstname"),
    "form1.parent" -> ObjectMetadata("parent", None, "form1.parent"),
    "form1.parent.firstname" -> StringDefinition("firstname", None, "firstname", "form1.firstname"),
    "form1.parent.firstname" -> StringDefinition("firstname", None, "firstname", "form1.parent.firstname"),
    "form1.parent.lastname" -> StringDefinition("lastname", None, "lastname", "form1.parent.lastname"),
    "form1.parent.middlename" -> StringDefinition("middlename", None, "middlename", "form1.parent.middlename"),
    "form1.lastname" -> StringDefinition("lastname", None, "lastname", "form1.lastname"),
    "form1.middlename" -> StringDefinition("middlename", None, "middlename", "form1.middlename"),
    "form1.files" -> ArrayMetadata("files", None, "form1.files"),
    "form1.files[0]" -> StringDefinition("file", None, "'https://github.com/duberg/object-storage'", "form1.files[0]"),
    "form1.files[2]" -> StringDefinition("file", None, "'https://github.com/duberg/object-storage'", "form1.files[2]"),
    "form1.files[1]" -> StringDefinition("file", None, "'https://github.com/duberg/object-storage'", "form1.files[1]"),
    "isEmployee" -> BooleanDefinition("isEmployee", None, value = false, "isEmployee")
  )

  val storageUpdated = storage
    .updateData(
      "form1.data.title.ru" -> "+++",
      "form1.parent.firstname" -> "+++"
    )
    .updateData("isEmployee" -> true)
    .updateDefinition("form1.parent.middlename", StringDefinition("middlename", Option("sdfsd"), "withValue", "form1.parent.middlename"))
    .updateDefinition("form1.parent", ObjectDefinition("parent", None, Map(
      "parent.firstname" -> StringDefinition("firstname", None, "xx", "parent.firstname"),
      "parent.lastname" -> StringDefinition("lastname", None, "xx", "parent.lastname"),
      "parent.middlename" -> StringDefinition("middlename", None, "xx", "parent.middlename"),
    ), "parent"))

  println(storageUpdated.prettify)
  println()

  println("=== storage flattened repr ===")
  storageUpdated.repr.impl.foreach({
    case (k, v) => println(s"$k -> $v")
  })

  val newStorage = Storage.empty
    .addDefinition("x", storageUpdated("form1.parent")) // add object definition
    .addDefinition("x.form1.parent.y", storageUpdated("name")) // add simple definition
    .addDefinition(storageUpdated("form1")) // add object definition to root
    .addDefinition(storageUpdated("name")) // add to root

  println()
  println("=== newStorage ===")
  println(newStorage.prettify)
}
