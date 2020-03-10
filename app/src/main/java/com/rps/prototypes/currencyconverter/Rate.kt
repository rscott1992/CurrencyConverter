package com.rps.prototypes.currencyconverter

data class ExchangeRate(
    val USD : Double?,
    val EUR : Double?,
    val GBP : Double?
)

data class Rate(
    val base : String,
    val rates : ExchangeRate,
    val date : String
)