package cz.petrchatrny.kreator.test_jvm.book

import cz.petrchatrny.kreator.annotations.Conversion
import cz.petrchatrny.kreator.annotations.Dto
import cz.petrchatrny.kreator.annotations.DtoField
import cz.petrchatrny.kreator.annotations.FieldConstants
import cz.petrchatrny.kreator.annotations.Kreator
import cz.petrchatrny.kreator.test_jvm.book.BookFields.author
import cz.petrchatrny.kreator.test_jvm.book.BookFields.id
import cz.petrchatrny.kreator.test_jvm.book.BookFields.isbn
import cz.petrchatrny.kreator.test_jvm.book.BookFields.name
import cz.petrchatrny.kreator.test_jvm.book.BookFields.publicationDate
import cz.petrchatrny.kreator.test_jvm.book.BookFields.publicationNumber
import java.util.Date
import java.util.UUID

@FieldConstants
@Kreator(
    Dto("BookCreateDto", omit = [id], conversion = Conversion.TO_DOMAIN),
    Dto("BookCreateDto2", pick = [name, author, publicationNumber, publicationDate, isbn], conversion = Conversion.TO_DOMAIN),
    Dto("BookListDto", pick = [name, author, isbn], conversion = Conversion.FROM_DOMAIN),
)
class Book(
    val name: String,
    val author: String,
    @DtoField("BookCreateDto", name="number", type = String::class, expression = "number.toInt()")
    val publicationNumber: Int,
    val publicationDate: Date,
) {
    var id: UUID? = null
    var description: String? = null
    var isbn: String? = null

    constructor(name: String, author: String, publicationNumber: Int, publicationDate: Date, description: String?)
            : this(name, author, publicationNumber, publicationDate)

    constructor(name: String, author: String, publicationNumber: Int, isbn: String?, publicationDate: Date)
            : this(name, author, publicationNumber, publicationDate) {
                this.isbn = isbn
            }

    constructor(name: String, author: String, publicationNumber: Int, publicationDate: Date, description: String?, isbn: String?)
            : this(name, author, publicationNumber, publicationDate)
}
