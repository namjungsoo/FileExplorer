package com.duongame.activity

import android.os.Bundle
import android.util.Log
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.anjlab.android.iab.v3.BillingProcessor
import com.anjlab.android.iab.v3.BillingProcessor.IBillingHandler
import com.anjlab.android.iab.v3.SkuDetails
import com.anjlab.android.iab.v3.TransactionDetails
import com.duongame.R
import com.duongame.adapter.DonateAdapter
import kotlinx.android.synthetic.main.activity_donate.*
import java.util.*

class DonateActivity : BaseActivity(), IBillingHandler {
    private var bp: BillingProcessor? = null

    private val purchaseIds = ArrayList(listOf("donate_1000", "donate_2000", "donate_5000", "donate_7000", "donate_10000"))
    private val subscriptionIds = ArrayList(listOf("donate_month_2000", "donate_month_5000"))

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_donate)
        bp = BillingProcessor(this, GP_LICENSE_KEY, this)
        Log.e(TAG, "onCreate")
    }

    override fun onProductPurchased(productId: String, details: TransactionDetails?) {}
    override fun onPurchaseHistoryRestored() {}
    override fun onBillingError(errorCode: Int, error: Throwable?) {}
    override fun onBillingInitialized() {
        // 구매상품
        val purchases = bp?.getPurchaseListingDetails(purchaseIds)

        // 구독상품
        val subscriptions = bp?.getSubscriptionListingDetails(subscriptionIds)
        Log.e(TAG, "purchases=$purchases")
        Log.e(TAG, "subscriptions=$subscriptions")

        // Sort ascending order
        purchases?.sortWith(Comparator { o1, o2 ->
            if (o1.priceLong > o2.priceLong) {
                1
            } else if (o1.priceLong < o2.priceLong) {
                -1
            } else 0
        })
        purchases?.let {
            for (i in purchases.indices) {
                Log.e(TAG, "onBillingInitialized products " + purchases[i])
            }
        }
        subscriptions?.let {
            for (i in subscriptions.indices) {
                Log.e(TAG, "onBillingInitialized details " + subscriptions[i])
            }
        }

        initRecyclerView(purchases, subscriptions)
    }

    val onClick: ((id: String) -> Unit) = {

    }
    
    private fun initRecyclerView(purchases: List<SkuDetails>?, subscriptions: List<SkuDetails>?) {
        // 정기후원 초기화 
        val list_regularly = findViewById<RecyclerView>(R.id.list_regularly)
        list_regularly.adapter = DonateAdapter(subscriptions)
        list_regularly.layoutManager = LinearLayoutManager(this)

        // 구매후원 초기화
        val list_onetime = findViewById<RecyclerView>(R.id.list_onetime)
        list_onetime.adapter = DonateAdapter(purchases)
        list_onetime.layoutManager = LinearLayoutManager(this)
    }

    companion object {
//        donate_month_ 매달 x원씩 후원합니다
//            2000원: 하단 배너광고 제거
//            5000원: 모든 광고 제거
//
//        donate_ 1,3,7,15,30일동안 광고가 제거됩니다 (일회성)
        private val TAG = DonateActivity::class.java.simpleName
        private const val GP_LICENSE_KEY = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAxHFQGSxElswW8fm3xjwdEWmllAJTrMeeTjCuEbYqS0Uwgdz52AEqMOJucoZRxj7M9xZF1swod0fKOOfntlW4ckfF8/lgOaU/1iTmw5pc5sQ3fHI7YlpuQTyL+txWbknhSsJ0zR6urQOZr0eKu8gv+3R7M7Mo22s91mBg25vWfsHncTLa6JOi9Js1Y1KHrRfo1NBnzvZND9nMFmiG9dUqQ6zLlP2s0Ie5gPOz0iOwuPVrZu5iZgVHSpy5/WlYkLHdkCSiH39QSpix9Cq9yhNT4DE0TQvHJR6dwBUsmgCE4Ifc5/2w20/ZNyHSOvPFZPta0pjXKGhhzCVxlvqskhqFJwIDAQAB"
    }
}