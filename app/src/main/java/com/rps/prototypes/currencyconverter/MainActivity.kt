package com.rps.prototypes.currencyconverter


import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.*
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import okhttp3.*
import org.json.JSONException
import org.json.JSONObject
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import java.io.IOException

data class Conversion(val amount: Int, val currFrom : String, val currTo : String )

class MainActivity : AppCompatActivity() {

    internal lateinit var convertButton : Button
    internal lateinit var currFrom : Spinner
    internal lateinit var currTo : Spinner
    internal lateinit var amountCurrFrom : TextView
    internal lateinit var amountCurrTo : TextView
    internal lateinit var amount : EditText
    internal lateinit var equals : TextView
    internal lateinit var ratesDesc : TextView

    private val rates = HashMap<String, Double>()
    private var myCompositeDisposable : CompositeDisposable? = null
    private var conversion : Conversion? = null


    companion object {
        private val TAG = MainActivity::class.java.simpleName
        //constants for saved vals
        private const val CURRENCY_FROM = "CURRENCY_FROM"
        private const val CURRENCY_TO = "CURRENCY_TO"
        private const val AMOUNT = "AMOUNT"
        private const val MARKET_RATE = "MARKET_RATE"
        private const val BASE_URL = "https://api.exchangeratesapi.io/"

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        myCompositeDisposable = CompositeDisposable()
        convertButton = findViewById(R.id.convertButton)
        currFrom = findViewById(R.id.currFrom)
        currTo = findViewById(R.id.currTo)
        amount = findViewById(R.id.amountBox)
        amountCurrFrom = findViewById(R.id.amountCurrFromTxtVw)
        amountCurrTo = findViewById(R.id.amountCurrToTxtVw)
        equals = findViewById(R.id.equalsTxtVw)
        ratesDesc = findViewById(R.id.rateTxtVw)

        convertButton.setOnClickListener {
            convert()
        }
    }

    private fun convert() {
        val currencyConvertFrom : String = currFrom.selectedItem.toString()
        val currencyConvertTo : String = currTo.selectedItem.toString()
        val strAmount : String = amount.text.toString()
        val amount : Int = Integer.parseInt(if (strAmount == "") "0" else strAmount)

        Log.d(TAG, "Convert method called, $CURRENCY_FROM: $currencyConvertFrom $CURRENCY_TO: $currencyConvertTo, $AMOUNT: $amount")
        if (currencyConvertFrom == currencyConvertTo) {
            Toast.makeText(this, "Please ensure that you have selected 2 different currencies", Toast.LENGTH_LONG).show()
            return
        }

        conversion = Conversion(amount, currencyConvertFrom, currencyConvertTo)

        getExchangeRates(currencyConvertFrom, currencyConvertTo)

    }

    private fun displayResults(rate: Rate) {

        val currency = conversion?.currTo
        val amount = conversion!!.amount

        var exchangeRateMap = mutableMapOf<String, Double?>()

        exchangeRateMap["USD"] = rate.rates.USD
        exchangeRateMap["EUR"] = rate.rates.EUR
        exchangeRateMap["GBP"] = rate.rates.GBP

        val exchangeRate = exchangeRateMap[currency]
        val base = rate.base
        val date = rate.date

        val conversionResult : Double = exchangeRate?.times(amount) ?: 0.0

        val formattedConversion : Double = String.format("%.3f", conversionResult).toDouble()

        Log.d(TAG, "conversion Result: $conversionResult, formatted Conversion: $formattedConversion")
        val amountCurrFromTxt = "$amount ${conversion?.currFrom}"
        amountCurrFrom.text = amountCurrFromTxt

        equals.text = "="

        val amountCurrToTxt = "$formattedConversion ${conversion?.currTo}"
        amountCurrTo.text = amountCurrToTxt

        val rateTxt = """
            |Rate:
            |1 $base = $exchangeRate ${conversion?.currTo}
            |Updated as of $date""".trimMargin()

        ratesDesc.text = rateTxt
    }


    private fun getExchangeRates(baseCurrency: String, endCurrency: String) {

        val requestInterface = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .build().create(GetData::class.java)


        myCompositeDisposable?.add(requestInterface.getData(baseCurrency, endCurrency)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeOn(Schedulers.io())
            .subscribe(this::handleResponse))

    }

    private fun handleResponse(rate: Rate)  {
        Log.d(TAG, "Response handled! $rate")

        displayResults(rate)
    }

    override fun onDestroy() {
        super.onDestroy()

        myCompositeDisposable?.clear()
    }

}
