Akka persistence object storage with fast serialization

```
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
    |__0 -> StringDefinition(file,None,'https://github.com/duberg/object-storage',$.form1.files[0])
    |__1 -> StringDefinition(file,None,'https://github.com/duberg/object-storage',$.form1.files[1])
    |__2 -> StringDefinition(file,None,'https://github.com/duberg/object-storage',$.form1.files[2])
  |__lastname -> StringDefinition(lastname,None,lastname,$.form1.lastname)
|__name -> StringDefinition(name,None,name,$.name)
