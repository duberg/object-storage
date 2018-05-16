# Akka persistence object storage with fast serialization and Expression Engine

**Asynchronous Expression Engine**
```javascript
expr := expr '==' term | expr '!=' term | expr '>' term | expr '<' term | expr '>=' term | expr term | term
term := expr OR term1 | expr AND term1 | term1
term1 := expr '+' term2 | expr '-' term2 | term2
term2 := expr '*' term3 | expr '/' term3 | term3
term3 := '(' expr ')' | decimal_literal | string_literal | boolean_literal | identifier
decimal_literal := regex [0-9][0-9]*[\.]?[0-9]*
string_literal := regex
boolean_literal := TRUE | FALSE
```
Bpmn expression example:
```
$task.firstname = secretary.firstname,
$task.lastname = secretary.lastname,
$task.middlename = secretary.middlename,
$task.fullname = $task.firstname + " " + $task.lastname + " " + $task.middlename,
$task.status = $task.fullname + " ("+ secretary.status + ")",

$task.counter = ($task.counter + 2) * 2,
$task.counter = ($task.counter + 2) * 2,
$task.counter = ($task.counter + 2) * 2,
$process.counter = $task.counter,
$project.counter = $process.counter + 1,
$task.counter = "4", // value conversion

$project.manager = secretary // object import
```
## Storage
**Json object**
```json
{
    "isEmployee" : {
        "value" : true,
        "path" : "$.isEmployee",
        "type" : "boolean"
    },
    "form1" : {
        "name" : "name1",
        "description" : "desc1",
        "value" : {
            "parent2" : {
                "description" : "Reference to parent object",
                "value" : {
                    "value" : {
                        "lastname" : {
                            "value" : "xx",
                            "path" : "$.form1.parent.lastname",
                            "type" : "string"
                        },
                        "firstname" : {
                            "value" : "xx",
                            "path" : "$.form1.parent.firstname",
                            "type" : "string"
                        },
                        "middlename" : {
                            "value" : "xx",
                            "path" : "$.form1.parent.middlename",
                            "type" : "string"
                        }
                    },
                    "path" : "$.form1.parent",
                    "type" : "object"
                },
                "ref" : "$.form1.parent",
                "path" : "$.form1.parent2",
                "type" : "ref"
            },
            "parent" : {
                "value" : {
                    "lastname" : {
                        "value" : "xx",
                        "path" : "$.form1.parent.lastname",
                        "type" : "string"
                    },
                    "firstname" : {
                        "value" : "xx",
                        "path" : "$.form1.parent.firstname",
                        "type" : "string"
                    },
                    "middlename" : {
                        "value" : "xx",
                        "path" : "$.form1.parent.middlename",
                        "type" : "string"
                    }
                },
                "path" : "$.form1.parent",
                "type" : "object"
            },
            "middlename" : {
                "value" : "middlename",
                "path" : "$.form1.middlename",
                "type" : "string"
            },
            "data" : {
                "value" : {
                    "title" : {
                        "value" : {
                            "en" : {
                                "value" : "Title",
                                "path" : "$.form1.data.title.en",
                                "type" : "string"
                            },
                            "ru" : {
                                "value" : "+++",
                                "path" : "$.form1.data.title.ru",
                                "type" : "string"
                            }
                        },
                        "path" : "$.form1.data.title",
                        "type" : "object"
                    }
                },
                "path" : "$.form1.data",
                "type" : "object"
            },
            "files" : [
                {
                    "value" : "https://github.com/duberg/object-storage",
                    "path" : "$.form1.files[0]",
                    "type" : "string"
                },
                {
                    "value" : "https://github.com/duberg/object-storage",
                    "path" : "$.form1.files[2]",
                    "type" : "string"
                },
                {
                    "value" : "https://github.com/duberg/object-storage",
                    "path" : "$.form1.files[1]",
                    "type" : "string"
                }
            ],
            "lastname" : {
                "value" : "lastname",
                "path" : "$.form1.lastname",
                "type" : "string"
            },
            "check" : {
                "value" : false,
                "path" : "$.form1.check",
                "type" : "boolean"
            }
        },
        "path" : "$.form1",
        "type" : "object"
    },
    "x1" : {
        "name" : "name1",
        "description" : "desc1",
        "value" : "name1",
        "path" : "$.x1",
        "type" : "string"
    }
}
 
```
**Storage as tree**
```text
[Storage]
|__isEmployee -> BooleanElement(None,None,true,$.isEmployee)
|__form1 [ObjectElement]
  |__parent2 ->
    |__$.form1.parent [ObjectElement]
      |__lastname -> StringElement(None,None,xx,$.form1.parent.lastname)
      |__firstname -> StringElement(None,None,xx,$.form1.parent.firstname)
      |__middlename -> StringElement(None,None,xx,$.form1.parent.middlename)
  |__parent [ObjectElement]
    |__lastname -> StringElement(None,None,xx,$.form1.parent.lastname)
    |__firstname -> StringElement(None,None,xx,$.form1.parent.firstname)
    |__middlename -> StringElement(None,None,xx,$.form1.parent.middlename)
  |__middlename -> StringElement(None,None,middlename,$.form1.middlename)
  |__data [ObjectElement]
    |__title [ObjectElement]
      |__en -> StringElement(None,None,Title,$.form1.data.title.en)
      |__ru -> StringElement(None,None,+++,$.form1.data.title.ru)
  |__files [ArrayElement]
    |__ -> StringElement(None,None,https://github.com/duberg/object-storage,$.form1.files[0])
    |__ -> StringElement(None,None,https://github.com/duberg/object-storage,$.form1.files[1])
    |__ -> StringElement(None,None,https://github.com/duberg/object-storage,$.form1.files[2])
  |__lastname -> StringElement(None,None,lastname,$.form1.lastname)
  |__check -> BooleanElement(None,None,false,$.form1.check)
|__x1 -> StringElement(Some(name1),Some(desc1),name1,$.x1)
```

**Storage representation**
```scala
form1.files -> ArrayMetadata(None,None,form1.files)
form1.files[2] -> StringElement(None,None,https://github.com/duberg/object-storage,form1.files[2])
form1.data -> ObjectMetadata(None,None,form1.data)
form1.parent2 -> RefMetadata(None,Some(Reference to parent object),form1.parent,form1.parent2)
form1.lastname -> StringElement(None,None,lastname,form1.lastname)
form1.files[1] -> StringElement(None,None,https://github.com/duberg/object-storage,form1.files[1])
form1.parent.firstname -> StringElement(None,None,xx,form1.parent.firstname)
form1 -> ObjectMetadata(Some(name1),Some(desc1),form1)
form1.parent.middlename -> StringElement(None,None,xx,form1.parent.middlename)
form1.files[0] -> StringElement(None,None,https://github.com/duberg/object-storage,form1.files[0])
form1.parent.lastname -> StringElement(None,None,xx,form1.parent.lastname)
form1.parent -> ObjectMetadata(None,None,form1.parent)
form1.middlename -> StringElement(None,None,middlename,form1.middlename)
x1 -> StringElement(Some(name1),Some(desc1),name1,x1)
form1.data.title.ru -> StringElement(None,None,+++,form1.data.title.ru)
form1.check -> BooleanElement(None,None,false,form1.check)
isEmployee -> BooleanElement(None,None,true,isEmployee)
form1.data.title.en -> StringElement(None,None,Title,form1.data.title.en)
form1.data.title -> ObjectMetadata(None,None,form1.data.title)
```