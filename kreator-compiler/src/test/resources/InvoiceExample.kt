package com.example.invoice

import cz.petrchatrny.kreator.annotations.Conversion
import cz.petrchatrny.kreator.annotations.Dto
import cz.petrchatrny.kreator.annotations.DtoField
import cz.petrchatrny.kreator.annotations.FieldConstants
import cz.petrchatrny.kreator.annotations.Kreator

import com.example.invoice.InvoiceFields.number
import com.example.invoice.InvoiceFields.customerName
import com.example.invoice.InvoiceFields.total

import java.math.BigDecimal

@FieldConstants
@Kreator(
//    Dto("InvoiceCreateDto", pick = [number, customerName, total], conversion = Conversion.TO_DOMAIN),
//    Dto("InvoiceListDto", pick = [customerName, total], conversion = Conversion.FROM_DOMAIN),
//    Dto("InvoiceInternalDto", pick = [number, total], conversion = Conversion.FROM_DOMAIN)
    Dto("InvoiceCreateDto", pick = [number, customerName, total]),
    Dto("InvoiceListDto", pick = [customerName, total]),
    Dto("InvoiceInternalDto", pick = [number, total])
)
class Invoice(
    val number: Long,

    @DtoField("InvoiceCreateDto",
        name = "total", // TODO tady odebrat jmeno, ale zatim to nefunguje
        type = Long::class,
        expression = "BigDecimal(this.total).divide(BigDecimal(100))",
        conversion = Conversion.TO_DOMAIN
    )
    @DtoField("InvoiceListDto")
    @DtoField(
        "InvoiceListDto", name = "totalFormatted",
        type = String::class,
        expression = "this.total.setScale(2).toPlainString()",
        conversion = Conversion.FROM_DOMAIN
    )
    @DtoField(
        "InvoiceInternalDto", name = "totalCents",
        type = Long::class,
        expression = "this.total.multiply(BigDecimal(100)).longValueExact()",
        conversion = Conversion.FROM_DOMAIN
    )
    val total: BigDecimal,

    @DtoField("InvoiceCreateDto", "InvoiceUpdateDto", name = "customer")
    val customerName: String
)
