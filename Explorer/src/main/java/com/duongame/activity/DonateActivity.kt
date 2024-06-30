package com.duongame.activity

import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.ConsumeParams
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.SkuDetails
import com.android.billingclient.api.SkuDetailsParams
import com.android.billingclient.api.consumePurchase
import com.android.billingclient.api.querySkuDetails
import com.duongame.R
import com.duongame.adapter.DonateAdapter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
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

    fun inAppMessage() {
        val inAppMessageParams = InAppMessageParams.newBuilder()
            .addInAppMessageCategoryToShow(InAppMessageParams.InAppMessageCategoryId.TRANSACTIONAL)
            .build()

        Log.e("Jungsoo", "showInAppMessages call")
        billingClient.showInAppMessages(this, inAppMessageParams) { inAppMessageResult ->
            Log.e("Jungsoo", "showInAppMessages callback")
            if (inAppMessageResult.responseCode == InAppMessageResult.InAppMessageResponseCode.NO_ACTION_NEEDED) {
                // The flow has finished and there is no action needed from developers.
                Log.e("Jungsoo", "SUBTEST: NO_ACTION_NEEDED")
            } else if (inAppMessageResult.responseCode == InAppMessageResult.InAppMessageResponseCode.SUBSCRIPTION_STATUS_UPDATED) {
                Log.e("Jungsoo", "SUBTEST: SUBSCRIPTION_STATUS_UPDATED")
                // The subscription status changed. For example, a subscription
                // has been recovered from a suspend state. Developers should
                // expect the purchase token to be returned with this response
                // code and use the purchase token with the Google Play
                // Developer API.
            }
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

                    inAppMessage()
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
            val flowParams = BillingFlowParams.newBuilder()
                .setSkuDetails(skuDetails)
                .build()
            val responseCode = billingClient.launchBillingFlow(this, flowParams).responseCode
            Timber.e("responseCode=$responseCode")
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
}