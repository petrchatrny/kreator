package cz.petrchatrny.kreator.annotations

/**
 * Anotace použitelná nad třídou.
 * Při jejím použití procesor vytvoří nový objekt s příponou "Fields".
 * Tedy při použití na třídě "User" vznikne nový objekt "UserFields".
 *
 * Nově vzniklý objekt bude jako konstanty obsahovat názvy všech vlastností, kterými
 * disponuje třída, nad kterou byla anotace použita.
 * Díky použití této anotace je možné snadněji používat anotaci @DTO a vybírat vlastnosti do parametrů pick či omit.
 *
 * @see Dto
 */
@Target(AnnotationTarget.CLASS)
annotation class DtoFields()
