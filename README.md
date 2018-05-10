# Akka persistence object storage with fast serialization

**Json object**
```storage.json
{
   "name": "name",
   "form1" : {
     "lastname": "lastname",
     "parent": {
       "firstname": "firstname",
       "middlename": "middlename",
       "lastname": "lastname"
     },
     "files": [
       "https://github.com/duberg/object-storage",
       "https://github.com/duberg/object-storage"
     ],
     "middlename": "middlename",
     "data": {
       "title": {
         "ru": "Название",
         "en": "Title"
       }
     },
     "a": false
   },
   "isEmployee": false
 }
 
```
**Storage as tree**
```text
[Storage]
|__isEmployee -> BooleanDefinition(isEmployee,None,true,$.isEmployee)
|__form1 [ObjectDefinition]
  |__parent [ObjectDefinition]
    |__lastname -> StringDefinition(lastname,None,xx,$.form1.parent.lastname)
    |__firstname -> StringDefinition(firstname,None,xx,$.form1.parent.firstname)
    |__middlename -> StringDefinition(middlename,None,xx,$.form1.parent.middlename)
  |__middlename -> StringDefinition(middlename,None,middlename,$.form1.middlename)
  |__data [ObjectDefinition]
    |__title [ObjectDefinition]
      |__en -> StringDefinition(en,None,Title,$.form1.data.title.en)
      |__ru -> StringDefinition(ru,None,+++,$.form1.data.title.ru)
  |__a -> BooleanDefinition(firstname,None,false,$.form1.a)
  |__files [ArrayDefinition]
    |__0 -> StringDefinition(file,None,"https://github.com/duberg/object-storage",$.form1.files[0])
    |__1 -> StringDefinition(file,None,"https://github.com/duberg/object-storage",$.form1.files[1])
    |__2 -> StringDefinition(file,None,"https://github.com/duberg/object-storage",$.form1.files[2])
  |__lastname -> StringDefinition(lastname,None,lastname,$.form1.lastname)
|__name -> StringDefinition(name,None,name,$.name)
```

**Storage flattened representation**
```scala
name -> StringDefinition(name,None,name,name)
form1.files -> ArrayMetadata(files,None,form1.files)
form1.files[2] -> StringDefinition(file,None,"https://github.com/duberg/object-storage",form1.files[2])
form1.data -> ObjectMetadata(data,None,form1.data)
form1.lastname -> StringDefinition(lastname,None,lastname,form1.lastname)
form1.files[1] -> StringDefinition(file,None,"https://github.com/duberg/object-storage",form1.files[1])
form1.parent.firstname -> StringDefinition(firstname,None,xx,form1.parent.firstname)
form1 -> ObjectMetadata(form1,None,form1)
form1.parent.middlename -> StringDefinition(middlename,None,xx,form1.parent.middlename)
form1.files[0] -> StringDefinition(file,None,"https://github.com/duberg/object-storage",form1.files[0])
form1.parent.lastname -> StringDefinition(lastname,None,xx,form1.parent.lastname)
form1.parent -> ObjectMetadata(parent,None,form1.parent)
form1.middlename -> StringDefinition(middlename,None,middlename,form1.middlename)
form1.data.title.ru -> StringDefinition(ru,None,+++,form1.data.title.ru)
isEmployee -> BooleanDefinition(isEmployee,None,true,isEmployee)
form1.data.title.en -> StringDefinition(en,None,Title,form1.data.title.en)
form1.a -> BooleanDefinition(firstname,None,false,form1.firstname)
form1.data.title -> ObjectMetadata(title,None,form1.data.title)

```