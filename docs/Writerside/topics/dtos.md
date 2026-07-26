# DTOs
This page contains details about all the library's options for creating various DTO classes using annotations.

## Kreator
Kreator is the main annotation of the library.
Specific definitions of individual DTO classes are inserted into this annotation.
The annotation groups the given annotations into one logical unit.
Each DTO defined in this annotation will be generated in a separate file by default.

### Sealed classes
If we want to group the generated DTO classes into one `sealed` class, it is possible to set the Kreator annotation argument `isSealed` to `true`.
All generated DTOs will then be written to a single file as one sealed class.
These DTO files will therefore be descendants of the sealed class.

The name of the sealed class is auto-generated based on the originally annotated class.
So if we use the Kreator annotation on the `User` class, the library will generate the sealed class called `UserDto`.

## Dto
An annotation reusable within `@Kreator`.
It defines a specific DTO class.

You can specify the name of the generated class and select or exclude specific properties to be included in it.
The `pick` or `omit` parameter is used to select properties, but only one of them may be used at a time.
If the pick parameter is specified, all explicitly mentioned properties will be included in the DTO.
If omit is specified, all other properties except those mentioned will be included in the DTO.
If neither parameter is specified, all properties will be copied.
If both parameters are specified, the library will throw an exception

### Mapping methods
Using the `mapping` parameter, you can choose whether a mapping method should also be generated in the resulting DTO.
By default, none mapping method is generated.
If you select the `FROM_DOMAIN` mapping option, a method converting the domain class to a DTO will be included in the resulting DTO.
In implementation, this method will be inside the DTO as a function in the companion object.
If you select the TO_DOMAIN option, a mapping function will be generated that converts the DTO to the domain class.
It will be a member function inside the DTO, which in its implementation will choose the most suitable constructor from the domain class and call it.
If the class contains properties that cannot be set via the constructor but via the setter, these properties will also be set using the generated method.

### Class types
Each DTO class can be of different class type and this type is selected using the classType parameter.
Currently, plain classes and data classes are supported.
Data classes are used by default.

## DtoField
This annotation is used above individual properties of the source class and is used to change this property in the resulting DTO.
It is possible to make fundamental changes such as the name of the resulting property or changing the data type or the way the property is set in the mapping method.
The annotation can also be used to provide completely new properties in the resulting DTO that were not in the original class.
An annotation can be used repeatedly on a property if we make multiple changes to different DTO classes.
However, it is always necessary to state in the annotation which DTO classes the changes apply to.

### Change name
To change the name of the resulting property, simply enter any value in the `name` parameter.
In annotation definition, an empty string is entered here, but if the user explicitly does the same, the name will remain unchanged.
Note that the values in this parameter are subject to the classic rules for creating variable identifiers in Kotlin.

### Change type
The type parameter is used to change the data type.
The parameter is set to the `KClass` of the specific data type.
If the user specifies the data type `Any::class`, the properties in the DTO will remain the original, unchanged data type.

### Change conversion
If we want to change how a property is set in the mapping method of a DTO class, we can use the `expression` parameter.
This parameter accepts any Kotlin expression and inserts it directly into the mapping method.
