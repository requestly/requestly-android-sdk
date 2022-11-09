package io.requestly.android.core.modules.sharedPrefViewer

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import io.requestly.android.core.R
import io.requestly.android.core.SharedPrefFileData
import io.requestly.android.core.databinding.FragmentSharedPrefViewerBinding

class SharedPrefViewerFragment : Fragment() {


    private lateinit var mainBinding: FragmentSharedPrefViewerBinding
    private lateinit var mRecyclerViewAdaptor: SharedPrefLineItemAdaptor
    private val viewModel: SharedPrefViewerViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mainBinding = FragmentSharedPrefViewerBinding.inflate(layoutInflater)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        mainBinding = FragmentSharedPrefViewerBinding.inflate(layoutInflater)

        // Inflate the layout for this fragment
        initRecyclerView()
        initFileSelectorSpinner()

        return mainBinding.root
    }

    private fun initFileSelectorSpinner() {
        // Keeping a copy of the list, because `adaptor.clear()` removes all entries from the
        // live data also. Ideally this should be handled in viewModel itself.
        val files = viewModel.prefFilesNamesLive.value?.toMutableList() ?: emptyList()
        val adaptor = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, files)
        adaptor.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        with(mainBinding.fileSelectSpinner) {
            this.adapter = adaptor
            this.setSelection(files.indexOf(viewModel.mSelectedFileName))
            this.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onNothingSelected(parent: AdapterView<*>?) {

                }

                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    viewModel.fileNameSelected(files[position])
                }
            }
        }

        viewModel.prefFilesNamesLive.observe(viewLifecycleOwner) {
            adaptor.clear()
            adaptor.addAll(it)
            adaptor.notifyDataSetChanged()
            mainBinding.fileSelectSpinner.setSelection(it.indexOf(viewModel.mSelectedFileName))
        }
    }

    // Its fine to use notifyDataSetChanged here.
    // Data size is small enough to not cause Frame skips or lags.
    @SuppressLint("NotifyDataSetChanged")
    private fun initRecyclerView() {
        mainBinding.sharedPrefsEntryRecyclerView.layoutManager =
            LinearLayoutManager(requireContext())

        val mapper: (SharedPrefFileData) -> SharedPrefLineItemModel = {
            SharedPrefLineItemModel(
                dataTypeText = it.dataType.toString(),
                fileName = it.fileName,
                prefKeyText = it.key,
                prefValueText = it.value.toString(),
                onEditClickListener = {
                    showEditSharedPrefEntryDialog(it) { newValue ->
                        return@showEditSharedPrefEntryDialog viewModel.verifyAndSave(
                            newValue,
                            it
                        )
                    }
                },
                onDeleteClickListener = {
                    loadDeleteConfirmationDialog {
                        viewModel.deleteEntry(fileName = it.fileName, keyName = it.key)
                    }
                }
            )
        }
        val adaptor = SharedPrefLineItemAdaptor(viewModel.prefEntriesLive.value?.map(mapper) ?: emptyList())
        mainBinding.sharedPrefsEntryRecyclerView.adapter = adaptor

        viewModel.prefEntriesLive.observe(viewLifecycleOwner) { list ->
            adaptor.items = list.map(mapper)
            adaptor.notifyDataSetChanged()
        }
    }

    private fun showEditSharedPrefEntryDialog(
        model: SharedPrefFileData,
        onSaveClick: (String) -> Boolean
    ) {
        // Create Dialog & Set its params
        val dialog = Dialog(requireContext())
        dialog.setContentView(R.layout.shared_pref_edit_value_view)
        dialog.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        dialog.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        dialog.setCancelable(false)

        // Find all Views and configure them
        val saveButton = dialog.findViewById<TextView>(R.id.saveTextButton)
        val cancelButton = dialog.findViewById<TextView>(R.id.cancelTextButton)
        val fileNameTextView = dialog.findViewById<TextView>(R.id.fileNameTextView2)
        val dataTypeTextView = dialog.findViewById<TextView>(R.id.dataTypeTextView2)
        val prefKeyTextView = dialog.findViewById<TextView>(R.id.prefKeyTextView2)
        val alertTextView = dialog.findViewById<TextView>(R.id.alertTextView)
        alertTextView.visibility = View.GONE
        val prefValueEditTextBox = dialog.findViewById<EditText>(R.id.prefValueEditTextBox)

        prefKeyTextView.text = model.key
        fileNameTextView.text = model.fileName
        dataTypeTextView.text = model.dataType.toString()
        prefValueEditTextBox.setText(model.value.toString(), TextView.BufferType.EDITABLE)
        prefValueEditTextBox.setSelection(prefValueEditTextBox.length())

        prefValueEditTextBox.requestFocus()
        saveButton.setOnClickListener {
            val success = onSaveClick(prefValueEditTextBox.text.toString())
            if (success) {
                prefValueEditTextBox.setBackgroundResource(R.drawable.edit_text_box)
                alertTextView.visibility = View.GONE
                dialog.hide()
            } else {
                prefValueEditTextBox.setBackgroundResource(R.drawable.edit_text_box_alert)
                alertTextView.text = "Invalid Value for type ${model.dataType.toString()}"
                alertTextView.visibility = View.VISIBLE
            }
        }
        cancelButton.setOnClickListener {
            dialog.hide()
        }

        // Show Dialog
        dialog.show()
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
}
