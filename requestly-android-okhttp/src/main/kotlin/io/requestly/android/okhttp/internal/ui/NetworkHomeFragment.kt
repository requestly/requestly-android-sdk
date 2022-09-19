package io.requestly.android.okhttp.internal.ui

import android.app.AlertDialog
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.text.method.LinkMovementMethod
import android.util.Log
import android.view.*
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.appcompat.widget.SearchView
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DividerItemDecoration
import io.requestly.android.core.SettingsManager
import io.requestly.android.okhttp.R
import io.requestly.android.okhttp.api.RQClientProvider
import io.requestly.android.okhttp.databinding.FragmentNetworkHomeBinding
import io.requestly.android.okhttp.databinding.RqInterceptorMoreDetailDialogLayoutBinding
import io.requestly.android.okhttp.internal.data.entity.HttpTransaction
import io.requestly.android.okhttp.internal.data.model.DialogData
import io.requestly.android.okhttp.internal.support.HarUtils
import io.requestly.android.okhttp.internal.support.Sharable
import io.requestly.android.okhttp.internal.support.TransactionDetailsHarSharable
import io.requestly.android.okhttp.internal.support.TransactionListDetailsSharable
import io.requestly.android.okhttp.internal.support.shareAsFile
import io.requestly.android.okhttp.internal.support.showDialog
import io.requestly.android.okhttp.internal.ui.transaction.TransactionAdapter
import kotlinx.coroutines.launch

internal class NetworkHomeFragment :
    BaseRequestlyNetworkFragment(),
    SearchView.OnQueryTextListener {

    private val viewModel: MainViewModel by viewModels()

    private lateinit var mainBinding: FragmentNetworkHomeBinding
    private lateinit var transactionsAdapter: TransactionAdapter
    private lateinit var dialogBinding: RqInterceptorMoreDetailDialogLayoutBinding
    private lateinit var menuHost: MenuHost

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        dialogBinding = RqInterceptorMoreDetailDialogLayoutBinding.inflate(layoutInflater)
        mainBinding = FragmentNetworkHomeBinding.inflate(layoutInflater)
        transactionsAdapter = TransactionAdapter(requireContext()) {
            transactionId ->
            Log.d("Requestly", "Transaction clicked")
//            TransactionActivity.start(this, transactionId)
        }

        with(mainBinding) {
            tutorialLink.movementMethod = LinkMovementMethod.getInstance()
            transactionsRecyclerView.apply {
                setHasFixedSize(true)
                addItemDecoration(
                    DividerItemDecoration(
                        context,
                        DividerItemDecoration.VERTICAL
                    )
                )
                adapter = transactionsAdapter
            }
        }

        viewModel.transactions.observe(
            viewLifecycleOwner,
        ) { transactionTuples ->
            transactionsAdapter.submitList(transactionTuples)
            mainBinding.tutorialGroup.isVisible = transactionTuples.isEmpty()
        }

        return mainBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        menuHost = requireActivity()
        setupMenu()
    }

    private fun setupMenu() {
        menuHost.addMenuProvider(object: MenuProvider {
            override fun onPrepareMenu(menu: Menu) {
                // Handle for example visibility of menu items
            }

            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.rq_interceptor_transactions_list, menu)
                setUpSearch(menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                return when (menuItem.itemId) {
                    R.id.more_details -> {
                        getInfoDialog()
                        true
                    }
                    R.id.clear -> {
                        requireContext().showDialog(
                            getClearDialogData(),
                            onPositiveClick = {
                                viewModel.clearTransactions()
                            },
                            onNegativeClick = null
                        )
                        true
                    }
                    R.id.share_text -> {
                        requireContext().showDialog(
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
                        requireContext().showDialog(
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
                        false
                    }
                }
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)
    }

    private fun setUpSearch(menu: Menu) {
        val searchMenuItem = menu.findItem(R.id.search)
        val searchView = searchMenuItem.actionView as SearchView
        searchView.setOnQueryTextListener(this)
        searchView.setIconifiedByDefault(true)
    }

    override fun onQueryTextSubmit(query: String): Boolean = true

    override fun onQueryTextChange(newText: String): Boolean {
        viewModel.updateItemsFilter(newText)
        return true
    }

    private fun exportTransactions(
        fileName: String,
        block: suspend (List<HttpTransaction>) -> Sharable
    ) {
        lifecycleScope.launch {
            val transactions = viewModel.getAllTransactions()
            if (transactions.isNullOrEmpty()) {
                Toast
                    .makeText(
                        context,
                        R.string.rq_interceptor_export_empty_text,
                        Toast.LENGTH_SHORT
                    )
                    .show()
                return@launch
            }
            val sharableTransactions = block(transactions)
            val shareIntent = sharableTransactions.shareAsFile(
                activity = requireActivity(),
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

    private fun getInfoDialog() {
        createInfoDialog()
        dialogBinding.rqInterceptorDeviceId.text = RQClientProvider.client().deviceId
        dialogBinding.rqInterceptorAppId.text = SettingsManager.getInstance().getAppToken()
        addInfoDialogListeners()
    }

    private fun createInfoDialog() {
        val builder = AlertDialog.Builder(context)
        if (dialogBinding.root.parent != null) {
            (dialogBinding.root.parent as ViewGroup).removeView(dialogBinding.root)
        }
        builder.setView(dialogBinding.root)
        val dialog = builder.create()
        dialog.show()
        dialog.setCancelable(true)
    }

    private fun addInfoDialogListeners() {
        val clipboardManager = requireActivity().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        dialogBinding.rqInterceptorDeviceIdCopyButton.setOnClickListener {
            Toast.makeText(context, "Device Id Copied!", Toast.LENGTH_SHORT).show()
            val clipData = ClipData.newPlainText("Device Id", RQClientProvider.client().deviceId)
            clipboardManager.setPrimaryClip(clipData)
        }
        dialogBinding.rqInterceptorAppIdCopyButton.setOnClickListener {
            Toast.makeText(context, "App Id is copied!", Toast.LENGTH_SHORT).show()
            val clipData = ClipData.newPlainText("App Id", SettingsManager.getInstance().getAppToken())
            clipboardManager.setPrimaryClip(clipData)
        }
    }

    companion object {
        private const val EXPORT_TXT_FILE_NAME = "transactions.txt"
        private const val EXPORT_HAR_FILE_NAME = "transactions.har"
    }

}
