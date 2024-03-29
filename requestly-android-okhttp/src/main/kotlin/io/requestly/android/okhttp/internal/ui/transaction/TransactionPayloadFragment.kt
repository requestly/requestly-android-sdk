package io.requestly.android.okhttp.internal.ui.transaction

import android.content.Context
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.text.SpannableStringBuilder
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.widget.SearchView
import androidx.core.content.ContextCompat
import androidx.core.text.HtmlCompat
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import io.requestly.android.okhttp.R
import io.requestly.android.okhttp.databinding.RqInterceptorFragmentTransactionPayloadBinding
import io.requestly.android.okhttp.internal.data.entity.HttpTransaction
import io.requestly.android.okhttp.internal.support.Logger
import io.requestly.android.okhttp.internal.support.calculateLuminance
import io.requestly.android.okhttp.internal.support.combineLatest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.FileOutputStream
import java.io.IOException

internal class TransactionPayloadFragment :
    Fragment(), SearchView.OnQueryTextListener {

    private val viewModel: TransactionViewModel by viewModels(
        ownerProducer = { requireParentFragment() }
    )

    private val payloadType: PayloadType by lazy(LazyThreadSafetyMode.NONE) {
        arguments?.getSerializable(ARG_TYPE) as PayloadType
    }

    private val saveToFile = registerForActivityResult(ActivityResultContracts.CreateDocument()) { uri ->
        val transaction = viewModel.transaction.value
        if (uri != null && transaction != null) {
            lifecycleScope.launch {
                val result = saveToFile(payloadType, uri, transaction)
                val toastMessageId = if (result) {
                    R.string.rq_interceptor_file_saved
                } else {
                    R.string.rq_interceptor_file_not_saved
                }
                Toast.makeText(context, toastMessageId, Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(
                requireContext(),
                R.string.rq_interceptor_save_failed_to_open_document,
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private lateinit var payloadBinding: RqInterceptorFragmentTransactionPayloadBinding
    private val payloadAdapter = TransactionBodyAdapter()
    private lateinit var menuHost: MenuHost

    private var backgroundSpanColor: Int = Color.YELLOW
    private var foregroundSpanColor: Int = Color.RED

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        payloadBinding = RqInterceptorFragmentTransactionPayloadBinding.inflate(
            inflater,
            container,
            false
        )
        return payloadBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        menuHost = requireActivity()
        setupMenu()

        payloadBinding.payloadRecyclerView.apply {
            setHasFixedSize(true)
            adapter = payloadAdapter
        }

        viewModel.transaction.combineLatest(viewModel.formatRequestBody).observe(
            viewLifecycleOwner,
            Observer { (transaction, formatRequestBody) ->
                if (transaction == null) return@Observer
                lifecycleScope.launch {
                    payloadBinding.loadingProgress.visibility = View.VISIBLE

                    val result = processPayload(payloadType, transaction, formatRequestBody)
                    if (result.isEmpty()) {
                        showEmptyState()
                    } else {
                        payloadAdapter.setItems(result)
                        showPayloadState()
                    }
                    // Invalidating menu, because we need to hide menu items for empty payloads
                    requireActivity().invalidateOptionsMenu()

                    payloadBinding.loadingProgress.visibility = View.GONE
                }
            }
        )
    }

    private fun setupMenu() {
        menuHost.addMenuProvider(object: MenuProvider {
            override fun onPrepareMenu(menu: Menu) {
                val transaction = viewModel.transaction.value

                if (shouldShowSearchIcon(transaction)) {
                    val searchMenuItem = menu.findItem(R.id.search)
                    searchMenuItem?.isVisible = true
                    val searchView = searchMenuItem.actionView as SearchView
                    searchView.setOnQueryTextListener(this@TransactionPayloadFragment)
                    searchView.setIconifiedByDefault(true)
                }

                if (shouldShowSaveIcon(transaction)) {
                    menu.findItem(R.id.save_body).apply {
                        isVisible = true
                    }
                }

                if (payloadType == PayloadType.REQUEST) {
                    viewModel.doesRequestBodyRequireEncoding.observe(
                        viewLifecycleOwner
                    ) { menu.findItem(R.id.encode_url).isVisible = it }
                } else {
                    menu.findItem(R.id.encode_url).isVisible = false
                }
            }

            @Suppress("EmptyFunctionBlock")
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                return when(menuItem.itemId) {
                    R.id.save_body -> {
                        createFileToSaveBody()
                        true
                    }
                    else -> false
                }
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)
    }

    private fun showEmptyState() {
        payloadBinding.apply {
            emptyPayloadTextView.text = if (payloadType == PayloadType.RESPONSE) {
                getString(R.string.rq_interceptor_response_is_empty)
            } else {
                getString(R.string.rq_interceptor_request_is_empty)
            }
            emptyStateGroup.visibility = View.VISIBLE
            payloadRecyclerView.visibility = View.GONE
        }
    }

    private fun showPayloadState() {
        payloadBinding.apply {
            emptyStateGroup.visibility = View.GONE
            payloadRecyclerView.visibility = View.VISIBLE
        }
    }

    private fun shouldShowSaveIcon(transaction: HttpTransaction?) = when {
        (payloadType == PayloadType.REQUEST) -> (0L != (transaction?.requestPayloadSize))
        (payloadType == PayloadType.RESPONSE) -> (0L != (transaction?.responsePayloadSize))
        else -> true
    }

    private fun shouldShowSearchIcon(transaction: HttpTransaction?) = when (payloadType) {
        PayloadType.REQUEST -> {
            (false == transaction?.isRequestBodyEncoded) && (0L != (transaction.requestPayloadSize))
        }
        PayloadType.RESPONSE -> {
            (false == transaction?.isResponseBodyEncoded) && (0L != (transaction.responsePayloadSize))
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        backgroundSpanColor = ContextCompat.getColor(context, R.color.rq_interceptor_background_span_color)
        foregroundSpanColor = ContextCompat.getColor(context, R.color.rq_interceptor_foreground_span_color)
    }

    private fun createFileToSaveBody() {
        saveToFile.launch("$DEFAULT_FILE_PREFIX${System.currentTimeMillis()}")
    }

    override fun onQueryTextSubmit(query: String): Boolean = false

    override fun onQueryTextChange(newText: String): Boolean {
        if (newText.isNotBlank() && newText.length > NUMBER_OF_IGNORED_SYMBOLS) {
            payloadAdapter.highlightQueryWithColors(newText, backgroundSpanColor, foregroundSpanColor)
        } else {
            payloadAdapter.resetHighlight()
        }
        return true
    }

    private suspend fun processPayload(
        type: PayloadType,
        transaction: HttpTransaction,
        formatRequestBody: Boolean
    ): MutableList<TransactionPayloadItem> {
        return withContext(Dispatchers.Default) {
            val result = mutableListOf<TransactionPayloadItem>()

            val headersString: String
            val isBodyEncoded: Boolean
            val bodyString: String

            if (type == PayloadType.REQUEST) {
                headersString = transaction.getRequestHeadersString(true)
                isBodyEncoded = transaction.isRequestBodyEncoded
                bodyString = if (formatRequestBody) {
                    transaction.getFormattedRequestBody()
                } else {
                    transaction.requestBody ?: ""
                }
            } else {
                headersString = transaction.getResponseHeadersString(true)
                isBodyEncoded = transaction.isResponseBodyEncoded
                bodyString = transaction.getFormattedResponseBody()
            }

            if (headersString.isNotBlank()) {
                result.add(
                    TransactionPayloadItem.HeaderItem(
                        HtmlCompat.fromHtml(
                            headersString,
                            HtmlCompat.FROM_HTML_MODE_LEGACY
                        )
                    )
                )
            }

            // The body could either be an image, plain text, decoded binary or not decoded binary.
            val responseBitmap = transaction.responseImageBitmap

            if (type == PayloadType.RESPONSE && responseBitmap != null) {
                val bitmapLuminance = responseBitmap.calculateLuminance()
                result.add(TransactionPayloadItem.ImageItem(responseBitmap, bitmapLuminance))
                return@withContext result
            }

            when {
                isBodyEncoded -> {
                    val text = requireContext().getString(R.string.rq_interceptor_body_omitted)
                    result.add(TransactionPayloadItem.BodyLineItem(SpannableStringBuilder.valueOf(text)))
                }
                bodyString.isBlank() -> {
                    val text = requireContext().getString(R.string.rq_interceptor_body_empty)
                    result.add(TransactionPayloadItem.BodyLineItem(SpannableStringBuilder.valueOf(text)))
                }
                else -> bodyString.lines().forEach {
                    result.add(TransactionPayloadItem.BodyLineItem(SpannableStringBuilder.valueOf(it)))
                }
            }

            return@withContext result
        }
    }

    private suspend fun saveToFile(type: PayloadType, uri: Uri, transaction: HttpTransaction): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                requireContext().contentResolver.openFileDescriptor(uri, "w")?.use {
                    FileOutputStream(it.fileDescriptor).use { fos ->
                        when (type) {
                            PayloadType.REQUEST -> {
                                transaction.requestBody?.byteInputStream()?.copyTo(fos)
                                    ?: throw IOException(TRANSACTION_EXCEPTION)
                            }
                            PayloadType.RESPONSE -> {
                                transaction.responseBody?.byteInputStream()?.copyTo(fos)
                                    ?: throw IOException(TRANSACTION_EXCEPTION)
                            }
                        }
                    }
                }
            } catch (e: IOException) {
                Logger.error("Failed to save transaction to a file", e)
                return@withContext false
            }
            return@withContext true
        }
    }

    companion object {
        private const val ARG_TYPE = "type"
        private const val TRANSACTION_EXCEPTION = "Transaction not ready"

        private const val NUMBER_OF_IGNORED_SYMBOLS = 1

        const val DEFAULT_FILE_PREFIX = "rqinterceptor-export-"

        fun newInstance(type: PayloadType): TransactionPayloadFragment =
            TransactionPayloadFragment().apply {
                arguments = Bundle().apply {
                    putSerializable(ARG_TYPE, type)
                }
            }
    }
}
