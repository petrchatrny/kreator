package com.example.socialsite

import java.time.Instant // JDK only
import java.time.LocalDate // JDK only

import cz.petrchatrny.kreator.annotations.FieldConstants

@FieldConstants
class User(
    val id: Long,
    val email: String,
    val username: String,
) {
    private var password: String? = null
    var bio: String? = null
    var birthDate: LocalDate? = null
    val createdAt: Instant = Instant.now()
    var deletedAt: Instant? = null
    var emailVerified: Boolean = false
    var maxPostsPerDay: Int = 5
}

@FieldConstants
class Post(
    val author: User,
    var title: String,
    var text: String,
) {
    // problematic names
    val created_at: Instant = Instant.now()
    val comměnts: Set<Comment> = emptySet()
    var set: Set<String> = emptySet()
}

open class Comment(
    val author: User,
    val text: String,
) {
    val createdAt: Instant = Instant.now()
    var updatedAt: Instant? = null
}

@FieldConstants
class Reply(
    author: User,
    text: String,
    val repliesTo: Comment
) : Comment(author, text) { // inheritance

}
