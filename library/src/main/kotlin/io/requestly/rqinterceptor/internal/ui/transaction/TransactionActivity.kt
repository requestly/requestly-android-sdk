package io.requestly.rqinterceptor.internal.ui.transaction

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.activity.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.viewpager.widget.ViewPager
import io.requestly.rqinterceptor.R
import io.requestly.rqinterceptor.databinding.RqInterceptorActivityTransactionBinding
import io.requestly.rqinterceptor.internal.data.entity.HttpTransaction
import io.requestly.rqinterceptor.internal.support.HarUtils
import io.requestly.rqinterceptor.internal.support.Sharable
import io.requestly.rqinterceptor.internal.support.TransactionCurlCommandSharable
import io.requestly.rqinterceptor.internal.support.TransactionDetailsHarSharable
import io.requestly.rqinterceptor.internal.support.TransactionDetailsSharable
import io.requestly.rqinterceptor.internal.support.shareAsFile
import io.requestly.rqinterceptor.internal.support.shareAsUtf8Text
import io.requestly.rqinterceptor.internal.ui.BaseRQInterceptorActivity
import kotlinx.coroutines.launch

internal class TransactionActivity : BaseRQInterceptorActivity() {

    private val viewModel: TransactionViewModel by viewModels {
        TransactionViewModelFactory(intent.getLongExtra(EXTRA_TRANSACTION_ID, 0))
    }

    private lateinit var transactionBinding: RqInterceptorActivityTransactionBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        transactionBinding = RqInterceptorActivityTransactionBinding.inflate(layoutInflater)

        with(transactionBinding) {
            setContentView(root)
            setSupportActionBar(toolbar)
            setupViewPager(viewPager)
            tabLayout.setupWithViewPager(viewPager)
        }

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        viewModel.transactionTitle.observe(
            this,
            Observer { transactionBinding.toolbarTitle.text = it }
        )
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.rq_interceptor_transaction, menu)
        setUpUrlEncoding(menu)
        return super.onCreateOptionsMenu(menu)
    }

    private fun setUpUrlEncoding(menu: Menu) {
        val encodeUrlMenuItem = menu.findItem(R.id.encode_url)
        encodeUrlMenuItem.setOnMenuItemClickListener {
            viewModel.switchUrlEncoding()
            return@setOnMenuItemClickListener true
        }
        viewModel.encodeUrl.observe(
            this,
            Observer { encode ->
                val icon = if (encode) {
                    R.drawable.rq_interceptor_ic_encoded_url_white
                } else {
                    R.drawable.rq_interceptor_ic_decoded_url_white
                }
                encodeUrlMenuItem.setIcon(icon)
            }
        )
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        R.id.share_text -> shareTransactionAsText { transaction ->
            val encodeUrls = viewModel.encodeUrl.value!!
            TransactionDetailsSharable(transaction, encodeUrls)
        }
        R.id.share_curl -> shareTransactionAsText { transaction ->
            TransactionCurlCommandSharable(transaction)
        }
        R.id.share_file -> shareTransactionAsFile(EXPORT_TXT_FILE_NAME) { transaction ->
            val encodeUrls = viewModel.encodeUrl.value!!
            TransactionDetailsSharable(transaction, encodeUrls)
        }
        R.id.share_har -> shareTransactionAsFile(EXPORT_HAR_FILE_NAME) { transaction ->
            TransactionDetailsHarSharable(
                HarUtils.harStringFromTransactions(
                    listOf(transaction),
                    getString(R.string.rq_interceptor_name),
                    getString(R.string.rq_interceptor_version)
                )
            )
        }
        else -> super.onOptionsItemSelected(item)
    }

    private fun shareTransactionAsText(block: (HttpTransaction) -> Sharable): Boolean {
        val transaction = viewModel.transaction.value
        if (transaction == null) {
            showToast(getString(R.string.rq_interceptor_request_not_ready))
            return true
        }
        val sharable = block(transaction)
        lifecycleScope.launch {
            val shareIntent = sharable.shareAsUtf8Text(
                activity = this@TransactionActivity,
                intentTitle = getString(R.string.rq_interceptor_share_transaction_title),
                intentSubject = getString(R.string.rq_interceptor_share_transaction_subject)
            )
            startActivity(shareIntent)
        }
        return true
    }

    private fun shareTransactionAsFile(fileName: String, block: suspend (HttpTransaction) -> Sharable): Boolean {
        lifecycleScope.launch {
            val transaction = viewModel.transaction.value
            if (transaction == null) {
                showToast(getString(R.string.rq_interceptor_request_not_ready))
                return@launch
            }

            val sharable = block(transaction)
            lifecycleScope.launch {
                val shareIntent = sharable.shareAsFile(
                    activity = this@TransactionActivity,
                    fileName = fileName,
                    intentTitle = getString(R.string.rq_interceptor_share_transaction_title),
                    intentSubject = getString(R.string.rq_interceptor_share_transaction_subject),
                    clipDataLabel = "transaction"
                )
                if (shareIntent != null) {
                    startActivity(shareIntent)
                }
            }
        }
        return true
    }

    private fun setupViewPager(viewPager: ViewPager) {
        viewPager.adapter = TransactionPagerAdapter(this, supportFragmentManager)
        viewPager.addOnPageChangeListener(
            object : ViewPager.SimpleOnPageChangeListener() {
                override fun onPageSelected(position: Int) {
                    selectedTabPosition = position
                }
            }
        )
        viewPager.currentItem = selectedTabPosition
    }

    companion object {
        private const val EXPORT_TXT_FILE_NAME = "transaction.txt"
        private const val EXPORT_HAR_FILE_NAME = "transaction.har"
        private const val EXTRA_TRANSACTION_ID = "transaction_id"
        private var selectedTabPosition = 0

        fun start(context: Context, transactionId: Long) {
            val intent = Intent(context, TransactionActivity::class.java)
            intent.putExtra(EXTRA_TRANSACTION_ID, transactionId)
            context.startActivity(intent)
        }
    }
}
