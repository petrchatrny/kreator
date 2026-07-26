# Field constants

`@FieldConstants` is a special extra annotation.
The annotation exists to make it easier to define DTO classes.
When creating a DTO, the user must define the pick/omit properties with text strings. 
Thanks to this annotation, it is possible to select class properties using constants and not just using text strings.
 
When used, it creates a new Kotlin file that will contain the names of all the properties of the original annotated class as constants. 
The file will be named as the original class and will have the suffix "Fields" in its name. 
So if the annotation were used on the User class, the annotation processor will create a new file named **UserFields**. 

Without field constants:
```kotlin
@Kreator(
  Dto(name = "CREATE", omit = ["id"], mapping = Mapping.TO_DOMAIN),
  Dto(name = "LIST", pick = ["id","email","roles"], mapping = Mapping.FROM_DOMAIN),
)
class User
```

With field constants:
```kotlin
import com.example.user.UserFields.id
import com.example.user.UserFields.email
import com.example.user.UserFields.roles

@FieldConstants
@Kreator(
  Dto(name = "CREATE", omit = [id], mapping = Mapping.TO_DOMAIN),
  Dto(name = "LIST", pick = [id,email,roles], mapping = Mapping.FROM_DOMAIN),
)
class User
```
