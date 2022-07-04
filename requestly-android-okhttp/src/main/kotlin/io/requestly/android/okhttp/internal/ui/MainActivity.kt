package io.requestly.android.okhttp.internal.ui

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.method.LinkMovementMethod
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.annotation.StringRes
import androidx.appcompat.widget.AlertDialogLayout
import androidx.appcompat.widget.SearchView
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DividerItemDecoration
import io.requestly.android.okhttp.R
import io.requestly.android.okhttp.api.RQClient
import io.requestly.android.okhttp.api.RQClientProvider
import io.requestly.android.okhttp.databinding.RqInterceptorActivityMainBinding
import io.requestly.android.okhttp.internal.data.entity.HttpTransaction
import io.requestly.android.okhttp.internal.data.model.DialogData
import io.requestly.android.okhttp.internal.support.HarUtils
import io.requestly.android.okhttp.internal.support.Sharable
import io.requestly.android.okhttp.internal.support.TransactionDetailsHarSharable
import io.requestly.android.okhttp.internal.support.TransactionListDetailsSharable
import io.requestly.android.okhttp.internal.support.shareAsFile
import io.requestly.android.okhttp.internal.support.showDialog
import io.requestly.android.okhttp.internal.ui.transaction.TransactionActivity
import io.requestly.android.okhttp.internal.ui.transaction.TransactionAdapter
import kotlinx.coroutines.launch

internal class MainActivity :
    BaseRQInterceptorActivity(),
    SearchView.OnQueryTextListener {

    private val viewModel: MainViewModel by viewModels()

    private lateinit var mainBinding: RqInterceptorActivityMainBinding
    private lateinit var transactionsAdapter: TransactionAdapter

    private val applicationName: CharSequence
        get() = applicationInfo.loadLabel(packageManager)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mainBinding = RqInterceptorActivityMainBinding.inflate(layoutInflater)
        transactionsAdapter = TransactionAdapter(this) { transactionId ->
            TransactionActivity.start(this, transactionId)
        }

        with(mainBinding) {
            setContentView(root)
            setSupportActionBar(toolbar)
            toolbar.subtitle = applicationName

            tutorialLink.movementMethod = LinkMovementMethod.getInstance()
            transactionsRecyclerView.apply {
                setHasFixedSize(true)
                addItemDecoration(DividerItemDecoration(this@MainActivity, DividerItemDecoration.VERTICAL))
                adapter = transactionsAdapter
            }
        }

        viewModel.transactions.observe(
            this
        ) { transactionTuples ->
            transactionsAdapter.submitList(transactionTuples)
            transactionTuples.isEmpty().also { mainBinding.tutorialGroup.isVisible = it }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.rq_interceptor_transactions_list, menu)
        setUpSearch(menu)
        return super.onCreateOptionsMenu(menu)
    }

    private fun setUpSearch(menu: Menu) {
        val searchMenuItem = menu.findItem(R.id.search)
        val searchView = searchMenuItem.actionView as SearchView
        searchView.setOnQueryTextListener(this)
        searchView.setIconifiedByDefault(true)
    }

    @SuppressLint("SetTextI18n")
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.more_Details->{
               val view= View.inflate(this@MainActivity,    R.layout.rq_interceptor_more_info_dialog_layout,null)
                val builder= AlertDialog.Builder(this@MainActivity)
                builder.setView(view)
                val dialog=builder.create()
                dialog.show()
                dialog.setCancelable(true)
                val deviceIdCopyIB=dialog.findViewById<ImageButton>(R.id.rq_interceptor_DeviceID_Copy_ImageButton);
                val appIdCopyIB=dialog.findViewById<ImageButton>(R.id.rq_interceptor_AppID_Copy_ImageButton);
                val deviceIdTV=dialog.findViewById<TextView>(R.id.rq_interceptor_DeviceId_TextView);
                val appIdTV=dialog.findViewById<TextView>(R.id.rq_interceptor_AppId_TextView);
                RQClientProvider.client().initDevice()
                val rqClientPro=
                deviceIdTV.text = rqClientPro.deviceId

//                appIdTV.setText(rqClientPro.sdkId.toString())
                deviceIdCopyIB.setOnClickListener {
                    Toast.makeText(this@MainActivity,"Device Id Copied!",Toast.LENGTH_SHORT).show()
                    val clipboardManager = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    val clipData = ClipData.newPlainText("Device Id", android.os.Build.DEVICE)
                    clipboardManager.setPrimaryClip(clipData)
                    dialog.dismiss()
                }
                appIdCopyIB.setOnClickListener {
                    Toast.makeText(this@MainActivity,"App Id is copied!",Toast.LENGTH_SHORT).show()
                    val clipboardManager = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    val clipData = ClipData.newPlainText("Device Id", android.os.Build.MANUFACTURER)
                    clipboardManager.setPrimaryClip(clipData)
                    dialog.dismiss()
                }
                true
            }
            R.id.clear -> {
                showDialog(
                    getClearDialogData(),
                    onPositiveClick = {
                        viewModel.clearTransactions()
                    },
                    onNegativeClick = null
                )
                true
            }
            R.id.share_text -> {
                showDialog(
                    getExportDialogData(R.string.rq_interceptor_export_text_http_confirmation),
                    onPositiveClick = {
                        exportTransactions(EXPORT_TXT_FILE_NAME) { transactions ->
                            TransactionListDetailsSharable(transactions, encodeUrls = false)
                        }
                    },
                    onNegativeClick = null
                )
                true
            }
            R.id.share_har -> {
                showDialog(
                    getExportDialogData(R.string.rq_interceptor_export_har_http_confirmation),
                    onPositiveClick = {
                        exportTransactions(EXPORT_HAR_FILE_NAME) { transactions ->
                            TransactionDetailsHarSharable(
                                HarUtils.harStringFromTransactions(
                                    transactions,
                                    getString(R.string.rq_interceptor_name),
                                    getString(R.string.rq_interceptor_version)
                                )
                            )
                        }
                    },
                    onNegativeClick = null
                )
                true
            }
            else -> {
                super.onOptionsItemSelected(item)
            }
        }
    }

    override fun onQueryTextSubmit(query: String): Boolean = true

    override fun onQueryTextChange(newText: String): Boolean {
        viewModel.updateItemsFilter(newText)
        return true
    }

    private fun exportTransactions(fileName: String, block: suspend (List<HttpTransaction>) -> Sharable) {
        lifecycleScope.launch {
            val transactions = viewModel.getAllTransactions()
            if (transactions.isNullOrEmpty()) {
                Toast
                    .makeText(this@MainActivity, R.string.rq_interceptor_export_empty_text, Toast.LENGTH_SHORT)
                    .show()
                return@launch
            }

            val sharableTransactions = block(transactions)
            val shareIntent = sharableTransactions.shareAsFile(
                activity = this@MainActivity,
                fileName = fileName,
                intentTitle = getString(R.string.rq_interceptor_share_all_transactions_title),
                intentSubject = getString(R.string.rq_interceptor_share_all_transactions_subject),
                clipDataLabel = "transactions"
            )
            if (shareIntent != null) {
                startActivity(shareIntent)
            }
        }
    }

    private fun getClearDialogData(): DialogData = DialogData(
        title = getString(R.string.rq_interceptor_clear),
        message = getString(R.string.rq_interceptor_clear_http_confirmation),
        positiveButtonText = getString(R.string.rq_interceptor_clear),
        negativeButtonText = getString(R.string.rq_interceptor_cancel)
    )

    private fun getExportDialogData(@StringRes dialogMessage: Int): DialogData = DialogData(
        title = getString(R.string.rq_interceptor_export),
        message = getString(dialogMessage),
        positiveButtonText = getString(R.string.rq_interceptor_export),
        negativeButtonText = getString(R.string.rq_interceptor_cancel)
    )

    companion object {
        private const val EXPORT_TXT_FILE_NAME = "transactions.txt"
        private const val EXPORT_HAR_FILE_NAME = "transactions.har"
    }
}
