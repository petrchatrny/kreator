# Quick start

Annotations from the Kreator library can be used on any Kotlin classes.
To generate minimal DTO classes, just use the `@Kreator` and `@Dto` annotations.
The `@Dto` annotations are sent as arguments in the Kreator annotation, so they are written without the `@` symbol.
The following is an example of using annotations to generate two basic DTO classes.
You can read more about more advanced DTO class creation [here](dtos.md).

<code-block lang="Kotlin">
import java.util.Date
import java.util.UUID

import cz.petrchatrny.kreator.annotations.Dto;
import cz.petrchatrny.kreator.annotations.Kreator;

@Kreator(
    Dto("BookCreateDto", omit = ["id"]),
    Dto("BookListDto", pick = ["name", "author", "isbn"])
)
class Book(
    val name: String,
    val author: String,
    val publicationNumber: Int,
    val publicationDate: Date,
) {
    var id: UUID? = null
    var description: String? = null
    var isbn: String? = null
}
</code-block>

The annotation used in this way creates the following two classes, each in a separate file:

<code-block lang="Kotlin">
// BookCreateDto.kt
public data class BookCreateDto(
  public val name: String,
  public val author: String,
  public val publicationNumber: Int,
  public val publicationDate: Date
  public val description: String?
  public val isbn: String?
)

// BookListDto.kt
public data class BookListDto(
public val name: String,
public val author: String,
public val isbn: String?,
)
</code-block>

## Java support
Since Java is compatible with Kotlin, the use of annotations is also possible above Java classes.
The resulting generated files will of course be written in the Kotlin language.
However, the syntax for writing annotations is noticeably different, see the following code, which is the equivalently written previous Kotlin class:

<code-block lang="Java">
import java.util.Date
import java.util.UUID

import cz.petrchatrny.kreator.annotations.Dto;
import cz.petrchatrny.kreator.annotations.Kreator;

@Kreator(dtos = {
    @Dto(name = "BookCreateDto", omit = {"id"}),
    @Dto(name = "BookListDto", pick = {"name", "author", "isbn"})
})
public class Book {
    private UUID id;
    private String description;
    private String isbn;
    private final String name;
    private final String author;
    private final int publicationNumber;
    private final Date publicationDate;

    public Book(String name, String author, int publicationNumber, Date publicationDate) {
        this.name = name;
        this.author = author;
        this.publicationNumber = publicationNumber;
        this.publicationDate = publicationDate;
    }
}
</code-block>

The generated Dto classes are identical to the Kotlin example.
