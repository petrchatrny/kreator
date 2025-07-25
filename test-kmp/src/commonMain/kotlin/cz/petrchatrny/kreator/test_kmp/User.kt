package cz.petrchatrny.kreator.test_kmp

import cz.petrchatrny.kreator.annotations.GenerateGetters

@GenerateGetters
class User(
    val id: Int,
    val username: String,
    var password: String,
//    val createdAt: LocalDateTime = LocalDateTime.now()
) {
    var email: String? = null
    var fullName: String? = null
//    var dateOfBirth: LocalDate? = null
    var phoneNumber: String? = null
    var isActive: Boolean = true
//    var updatedAt: LocalDateTime? = null
    var profilePictureUrl: String? = null
    var preferences: Map<String, List<String>>? = null
}