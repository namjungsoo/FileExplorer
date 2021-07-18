package com.duongame.activity

import android.os.Bundle
import android.view.MenuItem
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.billingclient.api.*
import com.duongame.R
import com.duongame.adapter.DonateAdapter
import kotlinx.coroutines.*
import timber.log.Timber

class DonateActivity : BaseActivity() {
    private val purchaseIds = listOf(
        "donate_1000",
        "donate_2000",
        "donate_5000",
        "donate_7000",
        "donate_10000"
    )

    private val subscriptionIds = listOf("donate_month_2000", "donate_month_5000")

    // SupervisorJob() + Dispatchers.Main
    private val scope = MainScope()

    // billing client 초기화
    private val purchasesUpdatedListener = PurchasesUpdatedListener { billingResult, purchases ->
            // To be implemented in a later section.
            Timber.e("billingResult=${billingResult.responseCode} purchases=${purchases}")

            // consume
            //val purchases = purchases ?: return@PurchasesUpdatedListener
            scope.launch {
                purchases?.forEach { purchase ->
                    handlePurchase(purchase)
                }
            }
        }

    private lateinit var billingClient: BillingClient

    private suspend fun handlePurchase(purchase: Purchase) {
        Timber.e("handlePurchase=$purchase")
        val consumeParams =
            ConsumeParams.newBuilder()
                .setPurchaseToken(purchase.getPurchaseToken())
                .build()
        val consumeResult = withContext(Dispatchers.IO) {
            billingClient.consumePurchase(consumeParams)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_donate)

        Timber.e("onCreate")

        billingClient = BillingClient.newBuilder(this)
            .setListener(purchasesUpdatedListener)
            .enablePendingPurchases()
            .build()

        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    // The BillingClient is ready. You can query purchases here.
                    Timber.e("onBillingSetupFinished")

                    val purchases = billingClient.queryPurchases(BillingClient.SkuType.INAPP).purchasesList

                    scope.launch {
                        purchases?.forEach { purchase ->
                            handlePurchase(purchase)
                        }
                    }

                    initRecyclerView()
                }
            }

            override fun onBillingServiceDisconnected() {
                // Try to restart the connection on the next request to
                // Google Play by calling the startConnection() method.
                Timber.e("onBillingServiceDisconnected")
            }
        })

        initToolbar()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        if (id == android.R.id.home) {
            onBackPressed()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    val onClick: ((skuDetails: SkuDetails) -> Unit) = { skuDetails ->
        if (purchaseIds.contains(skuDetails.sku)) {// 구매
            val flowParams = BillingFlowParams.newBuilder()
                .setSkuDetails(skuDetails)
                .build()
            val responseCode = billingClient.launchBillingFlow(this, flowParams).responseCode
            Timber.e("responseCode=$responseCode")
        } else if (subscriptionIds.contains(skuDetails.sku)) {// 구독
        }
    }

    private fun initRecyclerView() {
        scope.launch {
            val inapp = querySkuDetails(purchaseIds, BillingClient.SkuType.INAPP) ?: return@launch
            findViewById<RecyclerView>(R.id.list_onetime).run {
                val donateAdapter = DonateAdapter(inapp)
                donateAdapter.setOnClickCallback(onClick)
                adapter = donateAdapter
                layoutManager = LinearLayoutManager(this@DonateActivity)
            }
            val subs = querySkuDetails(subscriptionIds, BillingClient.SkuType.SUBS) ?: return@launch
            findViewById<RecyclerView>(R.id.list_regularly).run {
                val donateAdapter = DonateAdapter(subs)
                donateAdapter.setOnClickCallback(onClick)
                adapter = donateAdapter
                layoutManager = LinearLayoutManager(this@DonateActivity)
            }
        }
    }

    private fun initToolbar() {
        val actionBar = supportActionBar ?: return

        // 로고 버튼
        actionBar.setDisplayShowHomeEnabled(true)

        // Up 버튼
        actionBar.setDisplayHomeAsUpEnabled(true)
    }


    suspend fun querySkuDetails(listIds: List<String>, type: String): List<SkuDetails>? {
        val params = SkuDetailsParams.newBuilder()
        params.setSkusList(listIds).setType(type)

        // leverage querySkuDetails Kotlin extension function
        val skuDetailsResult = withContext(Dispatchers.IO) {
            billingClient.querySkuDetails(params.build())
        }

        return skuDetailsResult.skuDetailsList
    }

    companion object {
        // 구매 후에 실제로 광고를 제거해 주어야 한다
        // 프로그램 로딩시 구매여부를 확인하여 광고를 제거 한다

        // 구매
        /*
        donate_month_ 매달 x원씩 후원합니다
            2000원: 하단 배너광고 제거
            5000원: 모든 광고 제거

        donate_ 1,3,7,15,30일동안 광고가 제거됩니다 (일회성)
         */
        private const val GP_LICENSE_KEY =
            "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAxHFQGSxElswW8fm3xjwdEWmllAJTrMeeTjCuEbYqS0Uwgdz52AEqMOJucoZRxj7M9xZF1swod0fKOOfntlW4ckfF8/lgOaU/1iTmw5pc5sQ3fHI7YlpuQTyL+txWbknhSsJ0zR6urQOZr0eKu8gv+3R7M7Mo22s91mBg25vWfsHncTLa6JOi9Js1Y1KHrRfo1NBnzvZND9nMFmiG9dUqQ6zLlP2s0Ie5gPOz0iOwuPVrZu5iZgVHSpy5/WlYkLHdkCSiH39QSpix9Cq9yhNT4DE0TQvHJR6dwBUsmgCE4Ifc5/2w20/ZNyHSOvPFZPta0pjXKGhhzCVxlvqskhqFJwIDAQAB"
    }
}