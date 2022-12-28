package io.requestly.android.core.modules.apiModifier

import android.annotation.SuppressLint
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
import io.requestly.android.core.databinding.FragmentApiModifierBinding
import io.requestly.android.core.modules.apiModifier.dao.*
import io.requestly.android.core.modules.loadSimpleYesNoAlertDialog
import kotlin.Pair

typealias OnSaveClickFnType<T> = (T) -> Unit

class ApiModifierFragment : Fragment() {

    private lateinit var mainBinding: FragmentApiModifierBinding
    private val viewModel: ApiModifierFragmentViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        mainBinding = FragmentApiModifierBinding.inflate(layoutInflater)

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
        val mapper: (SwitchingRule) -> ApiModifierRuleItemModel? = mapperFunc@{

            val rule = it.pairs.firstOrNull() ?: return@mapperFunc null

            when (rule) {
                is Redirect -> {
                    return@mapperFunc ApiModifierRuleItemModel(
                        ruleTypeText = "Mock Rule",
                        httpVerbText = rule.source
                            .filters.firstOrNull()
                            ?.requestMethod?.firstOrNull()
                            ?.toString(),
                        operatorText = rule.source.operator.toString(),
                        sourceUrlText = rule.source.value,
                        targetUrlText = rule.destination,
                        targetUrlGuideText = "Load Response from",
                        isActive = it.isActive,
                        onSwitchStateChangeListener = { boolValue ->
                            viewModel.editSwitchState(it.id, boolValue)
                        },
                        onEditClickListener = {
                            loadAddNewMockRuleItemDialog(
                                rule = rule,
                                httpVerbs = HttpVerb.values().asList(),
                                operators = SourceOperator.values().asList()
                            ) { (verb, urlContainingText, destinationUrl) ->
                                viewModel.editRedirectRule(
                                    verb,
                                    urlContainingText,
                                    destinationUrl,
                                    it.id
                                )
                            }
                        },
                        onDeleteClickListener = {
                            loadSimpleYesNoAlertDialog(
                                context = requireContext(),
                                message = "Are you sure you want to delete this?",
                                onPositiveButtonClick = { viewModel.deleteItem(it.id) }
                            )
                        }
                    )
                }
                is Replace -> {
                    return@mapperFunc ApiModifierRuleItemModel(
                        ruleTypeText = "Switch Rule",
                        httpVerbText = null,
                        operatorText = rule.source.operator.toString(),
                        sourceUrlText = rule.from,
                        targetUrlText = rule.to,
                        targetUrlGuideText = "Replace with",
                        isActive = it.isActive,
                        onSwitchStateChangeListener = { boolValue ->
                            viewModel.editSwitchState(it.id, boolValue)
                        },
                        onEditClickListener = {
                            loadAddNewHostSwitchItemDialog(rule) { (t1, t2) ->
                                viewModel.editReplaceRule(t1, t2, it.id)
                            }
                        },
                        onDeleteClickListener = {
                            loadSimpleYesNoAlertDialog(
                                context = requireContext(),
                                message = "Are you sure you want to delete this?",
                                onPositiveButtonClick = { viewModel.deleteItem(it.id) }
                            )
                        }
                    )
                }
            }
        }
        val items: List<ApiModifierRuleItemModel> =
            viewModel.rulesListLive.value?.map(mapper)?.filterNotNull() ?: emptyList()
        val adaptor = ApiModifierRuleItemAdaptor(items)
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
        dialog.setContentView(R.layout.api_modifier_new_replace_rule_dialog)
        dialog.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        dialog.setCancelable(false)
        dialog.show()

        val saveButton = dialog.findViewById<TextView>(R.id.saveButton)
        val cancelButton = dialog.findViewById<TextView>(R.id.cancelButton)
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

    private fun loadAddNewMockRuleItemDialog(
        rule: Redirect?,
        httpVerbs: List<HttpVerb>,
        operators: List<SourceOperator>,
        onSaveClick: OnSaveClickFnType<Triple<HttpVerb, String, String>>,
    ) {
        var selectedMethod: HttpVerb =
            rule?.source?.filters?.firstOrNull()?.requestMethod?.firstOrNull() ?: httpVerbs[0]
        val selectionPosition = httpVerbs.indexOf(selectedMethod)

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
            httpMethodSpinner.setSelection(selectionPosition)
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
