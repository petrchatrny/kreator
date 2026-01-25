package com.example.book

import cz.petrchatrny.kreator.annotations.Dto
import cz.petrchatrny.kreator.annotations.FieldConstants
import cz.petrchatrny.kreator.annotations.Kreator

import com.example.book.BookFields.id
import com.example.book.BookFields.name
import com.example.book.BookFields.author
import com.example.book.BookFields.isbn

import java.util.Date
import java.util.UUID

@FieldConstants
@Kreator(
    Dto("BookCreateDto", omit = [id]),
    Dto("BookListDto", pick = [name, author, isbn])
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
