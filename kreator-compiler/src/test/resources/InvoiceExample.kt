package com.example.invoice

import cz.petrchatrny.kreator.annotations.Mapping
import cz.petrchatrny.kreator.annotations.Dto
import cz.petrchatrny.kreator.annotations.DtoField
import cz.petrchatrny.kreator.annotations.FieldConstants
import cz.petrchatrny.kreator.annotations.Kreator

import com.example.invoice.InvoiceFields.number
import com.example.invoice.InvoiceFields.customerName
import com.example.invoice.InvoiceFields.total
import com.example.invoice.InvoiceFields.billingAddress

import com.example.invoice.BillingAddressFields.id
import com.example.invoice.BillingAddressFields.country
import com.example.invoice.BillingAddressFields.city
import com.example.invoice.BillingAddressCreateDto

import java.math.BigDecimal

@FieldConstants
@Kreator(
    Dto("InvoiceCreateDto", pick = [number, customerName, total, billingAddress], mapping = Mapping.TO_DOMAIN),
    Dto("InvoiceListDto", pick = [customerName, total], mapping = Mapping.FROM_DOMAIN),
    Dto("InvoiceInternalDto", pick = [number, total], mapping = Mapping.FROM_DOMAIN),
)
class Invoice(
    val number: Long,

    @DtoField("InvoiceCreateDto",
        type = Long::class,
        expression = "BigDecimal(total).divide(BigDecimal(100))"
    )
    @DtoField("InvoiceListDto")
    @DtoField("InvoiceListDto",
        name = "totalFormatted",
        type = String::class,
        expression = "domain.total.setScale(2).toPlainString()",
    )
    @DtoField("InvoiceInternalDto",
        name = "totalCents",
        type = Long::class,
        expression = "domain.total.multiply(BigDecimal(100)).longValueExact()",
    )
    val total: BigDecimal,

    @DtoField("InvoiceCreateDto", "InvoiceUpdateDto", name = "customer")
    val customerName: String,

    @DtoField("InvoiceCreateDto")
    val billingAddress: BillingAddressCreateDto
)

@FieldConstants
@Kreator(
    Dto("BillingAddressCreateDto", omit = [id]),
    Dto("BillingAddressListDto", pick = [id, country, city]),
)
class BillingAddress(
    val id: Long? = null,
    val street: String,
    val city: String,
    val zip: String,
    val country: String,
)
