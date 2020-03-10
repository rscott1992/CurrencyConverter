package com.rps.prototypes.currencyconverter

import io.reactivex.Observable
import retrofit2.http.GET
import retrofit2.http.Query

interface GetData {

    @GET("latest?")
    fun getData(@Query("base") baseCurrency: String, @Query("symbols") endCurrency: String) : Observable<Rate>
}