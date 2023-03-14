package io.requestly.android.okhttp.internal.ui.transaction

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.viewpager.widget.ViewPager
import io.requestly.android.okhttp.R
import io.requestly.android.okhttp.databinding.FragmentTransactionBinding
import io.requestly.android.okhttp.internal.data.entity.HttpTransaction
import io.requestly.android.okhttp.internal.support.HarUtils
import io.requestly.android.okhttp.internal.support.Sharable
import io.requestly.android.okhttp.internal.support.TransactionCurlCommandSharable
import io.requestly.android.okhttp.internal.support.TransactionDetailsHarSharable
import io.requestly.android.okhttp.internal.support.TransactionDetailsSharable
import io.requestly.android.okhttp.internal.support.shareAsFile
import io.requestly.android.okhttp.internal.support.shareAsUtf8Text
import io.requestly.android.okhttp.internal.ui.BaseRequestlyNetworkFragment
import kotlinx.coroutines.launch

internal class TransactionFragment : BaseRequestlyNetworkFragment() {

    private val viewModel: TransactionViewModel by viewModels {
        val arguments = TransactionFragmentArgs.fromBundle(requireArguments())
        Log.d("Requestly", "TransactionId ${arguments.transactionId}")
        TransactionViewModelFactory(arguments.transactionId)
    }

    private lateinit var mainBinding: FragmentTransactionBinding
    private lateinit var menuHost: MenuHost

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        viewModel.transaction.observe(viewLifecycleOwner) {
            Log.d("Requestly", "Transaction Changes ${it?.url}")
        }
        mainBinding = FragmentTransactionBinding.inflate(layoutInflater)

        with(mainBinding) {
            setupViewPager(viewPager)
            tabLayout.setupWithViewPager(viewPager)
        }

        // Commenting This for now
        /*viewModel.transactionTitle.observe(
            viewLifecycleOwner
        ) { mainBinding.toolbarTitle.text = it }*/

        return mainBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        menuHost = requireActivity()
        setupMenu()
    }

    override fun onDestroy() {
        Log.d("Requestly", "Transaction Fragment Destroyed")
        super.onDestroy()
    }

    private fun setupMenu() {
        menuHost.addMenuProvider(object: MenuProvider {
            override fun onPrepareMenu(menu: Menu) {
                // Handle for example visibility of menu items
            }

            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.rq_interceptor_transaction, menu)
                setUpUrlEncoding(menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                return when (menuItem.itemId) {
                    R.id.share_text -> {
                        shareTransactionAsText { transaction ->
                            val encodeUrls = viewModel.encodeUrl.value!!
                            TransactionDetailsSharable(transaction, encodeUrls)
                        }
                        true
                    }
                    R.id.share_curl -> {
                        shareTransactionAsText { transaction ->
                            TransactionCurlCommandSharable(transaction)
                        }
                        true
                    }
                    R.id.share_file -> {
                        shareTransactionAsFile(EXPORT_TXT_FILE_NAME) { transaction ->
                            val encodeUrls = viewModel.encodeUrl.value!!
                            TransactionDetailsSharable(transaction, encodeUrls)
                        }
                        true
                    }
                    R.id.share_har -> {
                        shareTransactionAsFile(EXPORT_HAR_FILE_NAME) { transaction ->
                            TransactionDetailsHarSharable(
                                HarUtils.harStringFromTransactions(
                                    listOf(transaction),
                                    getString(R.string.rq_interceptor_name),
                                    getString(R.string.rq_interceptor_version)
                                )
                            )
                        }
                        true
                    }
                    R.id.encode_url -> {
                        viewModel.switchUrlEncoding()
                        true
                    }
                    else -> false
                }
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)
    }

    private fun setUpUrlEncoding(menu: Menu) {
        val encodeUrlMenuItem = menu.findItem(R.id.encode_url)
        viewModel.encodeUrl.observe(
            viewLifecycleOwner
        ) { encode ->
            val icon = if (encode) {
                R.drawable.rq_interceptor_ic_encoded_url
            } else {
                R.drawable.rq_interceptor_ic_decoded_url
            }
            encodeUrlMenuItem.setIcon(icon)
        }
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
                activity = requireActivity(),
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
                    activity = requireActivity(),
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
        // IMP: Passing childFragmentManager is important here for handling lifecycles
        viewPager.adapter = TransactionPagerAdapter(requireContext(), childFragmentManager)
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
            val intent = Intent(context, TransactionFragment::class.java)
            intent.putExtra(EXTRA_TRANSACTION_ID, transactionId)
            context.startActivity(intent)
        }
    }
}
