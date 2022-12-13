package io.requestly.android.core.modules.hostSwitcher

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import io.requestly.android.core.R
import io.requestly.android.core.databinding.FragmentHostSwitcherBinding
import io.requestly.android.core.modules.hostSwitcher.models.*
import kotlin.Pair

typealias OnSaveClickFnType<T> = (T) -> Unit

class HostSwitcherFragment : Fragment() {

    private lateinit var mainBinding: FragmentHostSwitcherBinding
    private val viewModel: HostSwitcherFragmentViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        mainBinding = FragmentHostSwitcherBinding.inflate(layoutInflater)

        // Inflate the layout for this fragment
        initRecyclerView()

        return mainBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val provider = object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.host_switcher_fragment_menu, menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                return when (menuItem.itemId) {
                    R.id.createHostSwitchRule -> {
                        loadAddNewHostSwitchItemDialog(null) { (t1, t2) ->
                            viewModel.createReplaceRule(startingText = t1, provisionalText = t2)
                        }
                        true
                    }
                    R.id.createMockRule -> {
                        loadAddNewMockRuleItemDialog(
                            null,
                            HttpVerb.values().asList(),
                            SourceOperator.values().asList()
                        ) { (verb, urlContainingText, destinationUrl) ->
                            viewModel.createRedirectRule(verb, urlContainingText, destinationUrl)
                        }
                        true
                    }
                    else -> {
                        false
                    }
                }
            }
        }
        requireActivity().addMenuProvider(provider, viewLifecycleOwner, Lifecycle.State.RESUMED)
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun initRecyclerView() {
        mainBinding.hostSwitcherRulesRecyclerView.layoutManager =
            LinearLayoutManager(requireContext())
        val mapper: (SwitchingRule) -> ApiModiferRuleItemModel? = mapperFunc@ {

            val replaceRule = it.pairs[0] as? Replace ?: return@mapperFunc null

            HostSwitchItemModel(
                startingText = replaceRule.from,
                provisionalText = replaceRule.to,
                isActive = it.isActive,
                onSwitchStateChangeListener = { boolValue ->
                    viewModel.editSwitchState(
                        it.id,
                        boolValue
                    )
                },
                onEditClickListener = {
                    loadAddNewHostSwitchItemDialog(replaceRule) { (t1, t2) ->
                        viewModel.editItem(t1, t2, it.id)
                    }
                },
                onDeleteClickListener = {
                    loadDeleteConfirmationDialog {
                        viewModel.deleteItem(it.id)
                    }
                }
            )
        }
        val items: List<ApiModiferRuleItemModel> =
            viewModel.rulesListLive.value?.map(mapper)?.filterNotNull() ?: emptyList()
        val adaptor = HostSwitchItemAdaptor(items)
        viewModel.rulesListLive.observe(viewLifecycleOwner) {
            adaptor.items = it.map(mapper).filterNotNull()
            // Its fine to use notifyDataSetChanged here.
            // Data size is small enough to not cause Frame skips or lags.
            adaptor.notifyDataSetChanged()
        }
        mainBinding.hostSwitcherRulesRecyclerView.adapter = adaptor
    }

    private fun loadAddNewHostSwitchItemDialog(
        rule: Replace?,
        onSaveClick: OnSaveClickFnType<Pair<String, String>>
    ) {
        val dialog = Dialog(requireContext())
        dialog.setContentView(R.layout.host_switcher_new_item_dialog)
        dialog.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        dialog.setCancelable(false)
        dialog.show()

        val saveButton = dialog.findViewById<Button>(R.id.saveButton)
        val cancelButton = dialog.findViewById<Button>(R.id.cancelButton)
        val startingEditText = dialog.findViewById<EditText>(R.id.startingEditText)
        val provisionalEditText = dialog.findViewById<EditText>(R.id.provisionalEditText)

        startingEditText.setText(rule?.from ?: "", TextView.BufferType.EDITABLE)
        provisionalEditText.setText(rule?.to ?: "", TextView.BufferType.EDITABLE)
        saveButton.setOnClickListener {
            onSaveClick(Pair(startingEditText.text.toString(), provisionalEditText.text.toString()))
            dialog.hide()
        }
        cancelButton.setOnClickListener {
            dialog.hide()
        }
    }

    private fun loadDeleteConfirmationDialog(onPositiveButtonClick: () -> Unit) {
        AlertDialog.Builder(requireActivity())
            .setCancelable(false)
            .setTitle("Are you sure you want to delete this?")
            .setPositiveButton("Yes") { dialog, _ ->
                onPositiveButtonClick()
                dialog.cancel()
            }
            .setNegativeButton("No") { dialog, _ ->
                dialog.cancel()
            }
            .create()
            .show()
    }

    private fun loadAddNewMockRuleItemDialog(
        rule: Redirect?,
        httpVerbs: List<HttpVerb>,
        operators: List<SourceOperator>,
        onSaveClick: OnSaveClickFnType<Triple<HttpVerb, String, String>>,
    ) {
        var selectedMethod = httpVerbs[0]

        val dialog = Dialog(requireContext())
        with(dialog) {
            this.setContentView(R.layout.api_modifier_new_mock_rule_dialog)
            this.window?.setLayout(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            this.setCancelable(false)

            val saveButton = dialog.findViewById<TextView>(R.id.saveTextButton)
            val cancelButton = dialog.findViewById<TextView>(R.id.cancelTextButton)
            val httpMethodSpinner = dialog.findViewById<Spinner>(R.id.httpMethodSpinner)
            val httpSpinneradaptor =
                ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, httpVerbs)
            httpSpinneradaptor.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            httpMethodSpinner.adapter = httpSpinneradaptor
            httpMethodSpinner.setSelection(0)
            httpMethodSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onNothingSelected(parent: AdapterView<*>?) {

                }

                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    selectedMethod = httpVerbs[position]
                }
            }

            val urlOperatorSpinner = dialog.findViewById<Spinner>(R.id.urlOperatorSpinner)
            val urlOperatorSpinnerAdaptor =
                ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, operators)
            urlOperatorSpinnerAdaptor.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            urlOperatorSpinner.adapter = urlOperatorSpinnerAdaptor
            urlOperatorSpinner.setSelection(0)

            val urlSourceEditText = dialog.findViewById<EditText>(R.id.sourceUrlTargetingEditText)
            urlSourceEditText.setText(rule?.source?.value, TextView.BufferType.EDITABLE)
            val destinationUrlEditText = dialog.findViewById<EditText>(R.id.mockUrlInputEditText)
            destinationUrlEditText.setText(rule?.destination, TextView.BufferType.EDITABLE)

            saveButton.setOnClickListener {
                onSaveClick(
                    Triple(
                        selectedMethod,
                        urlSourceEditText.text.toString(),
                        destinationUrlEditText.text.toString()
                    )
                )
                dialog.hide()
            }
            cancelButton.setOnClickListener {
                dialog.hide()
            }
        }
        dialog.show()
    }
}
