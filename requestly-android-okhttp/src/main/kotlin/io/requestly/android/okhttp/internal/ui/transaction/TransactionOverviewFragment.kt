package io.requestly.android.okhttp.internal.ui.transaction

import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.Observer
import io.requestly.android.okhttp.R
import io.requestly.android.okhttp.databinding.RqInterceptorFragmentTransactionOverviewBinding
import io.requestly.android.okhttp.internal.data.entity.HttpTransaction
import io.requestly.android.okhttp.internal.support.combineLatest

internal class TransactionOverviewFragment : Fragment() {

    private val viewModel: TransactionViewModel by activityViewModels { TransactionViewModelFactory() }

    private lateinit var overviewBinding: RqInterceptorFragmentTransactionOverviewBinding
    private lateinit var menuHost: MenuHost

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        overviewBinding = RqInterceptorFragmentTransactionOverviewBinding.inflate(inflater, container, false)
        return overviewBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        menuHost = requireActivity()
//        setupMenu()

        viewModel.transaction.combineLatest(viewModel.encodeUrl).observe(
            viewLifecycleOwner
        ) { (transaction, encodeUrl) -> populateUI(transaction, encodeUrl) }
    }

    private fun setupMenu() {
        menuHost.addMenuProvider(object: MenuProvider {
            override fun onPrepareMenu(menu: Menu) {
                // Handle for example visibility of menu items
            }

            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menu.findItem(R.id.save_body)?.isVisible = false
                viewModel.doesUrlRequireEncoding.observe(
                    viewLifecycleOwner,
                    Observer { menu.findItem(R.id.encode_url).isVisible = it }
                )
                menuInflater.inflate(R.menu.rq_interceptor_transaction, menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                return false
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)
    }

    override fun onDestroy() {
        Log.d("Requestly", "Transaction Overview Destroyed")
        super.onDestroy()
    }


    private fun populateUI(transaction: HttpTransaction?, encodeUrl: Boolean) {
        with(overviewBinding) {
            url.text = transaction?.getFormattedUrl(encodeUrl)
            method.text = transaction?.method
            protocol.text = transaction?.protocol
            status.text = transaction?.status.toString()
            response.text = transaction?.responseSummaryText
            when (transaction?.isSsl) {
                null -> {
                    sslGroup.visibility = View.GONE
                }
                true -> {
                    sslGroup.visibility = View.VISIBLE
                    sslValue.setText(R.string.rq_interceptor_yes)
                }
                else -> {
                    sslGroup.visibility = View.VISIBLE
                    sslValue.setText(R.string.rq_interceptor_no)
                }
            }
            if (transaction?.responseTlsVersion != null) {
                tlsVersionValue.text = transaction.responseTlsVersion
                tlsGroup.visibility = View.VISIBLE
            }
            if (transaction?.responseCipherSuite != null) {
                cipherSuiteValue.text = transaction.responseCipherSuite
                cipherSuiteGroup.visibility = View.VISIBLE
            }
            requestTime.text = transaction?.requestDateString
            responseTime.text = transaction?.responseDateString
            duration.text = transaction?.durationString
            requestSize.text = transaction?.requestSizeString
            responseSize.text = transaction?.responseSizeString
            totalSize.text = transaction?.totalSizeString
        }
    }
}
