package com.ft.printerconnectandprint.printer

import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.printerconnectandprint.R
import com.example.printerconnectandprint.databinding.FragmentPrinterBinding
import com.ft.printerconnectandprint.checkPermission
import com.ft.printerconnectandprint.printer.settings_data_store.SettingsDataStore
import com.mazenrashed.printooth.Printooth
import com.mazenrashed.printooth.ui.ScanningActivity
import kotlinx.android.synthetic.main.searchable_spinner_layout.*
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch


class PrinterFragment : Fragment() {

    private lateinit var binding: FragmentPrinterBinding
    private lateinit var dataStorePres: SettingsDataStore

    private val permissionList = listOf(
        android.Manifest.permission.BLUETOOTH_SCAN,
        android.Manifest.permission.BLUETOOTH_CONNECT,
        android.Manifest.permission.BLUETOOTH,
        android.Manifest.permission.BLUETOOTH_ADMIN,
        android.Manifest.permission.ACCESS_FINE_LOCATION
    )

    private var resultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == ScanningActivity.SCANNING_FOR_PRINTER && result.resultCode == Activity.RESULT_OK) {
                printDetails(result.data)
            } else checkPermission(requireContext(), permissionList)
        }

    private fun printDetails(result: Intent?) {

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentPrinterBinding.inflate(inflater)
        initialize()
        return binding.root
    }

    private fun initialize() {
        dataStorePres = SettingsDataStore(requireContext())

        binding.printPrinterChange.setOnClickListener {
            scanPrinterAndConnect()
        }

        binding.printerSettings.setOnClickListener {

        }

        lifecycleScope.launch {
            dataStorePres.preferenceFlow.collectLatest {
                binding.printerSize.text = it
            }
        }

        binding.printerSizeCard.setOnClickListener {
            searchSpinner()
        }


        binding.printBtn.setOnClickListener {
            if (!Printooth.hasPairedPrinter()) {
                scanPrinterAndConnect()
            } else {

            }
        }
    }

    private fun searchSpinner() {
        val printerSizeList = arrayOf("48mm", "72mm")

        val dialog = Dialog(requireContext(), com.karumi.dexter.R.style.AlertDialog_AppCompat)

        dialog.window?.setLayout(750, 800)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.setContentView(R.layout.searchable_spinner_layout)

        val listView = dialog.list_view
        val editText = dialog.edit_text

        val adapter: ArrayAdapter<String> = ArrayAdapter<String>(
            requireContext(),
            android.R.layout.simple_list_item_1,
            printerSizeList
        )

        listView.adapter = adapter
        editText.doOnTextChanged { text, start, before, count ->
            adapter.filter.filter(text ?: "")
        }
        listView.setOnItemClickListener { parent, view, position, id ->
            lifecycleScope.launch {
                //val size = adapter.getItem(position).toString().take(2)
                //binding.printerSize.text = size
                dataStorePres.savePrinterSize(adapter.getItem(position).toString())
            }
            dialog.dismiss()
        }
        dialog.show()
    }

    private fun scanPrinterAndConnect() {
        resultLauncher.launch(
            Intent(
                requireContext(),
                ScanningActivity::class.java
            ),
        )
    }

    override fun onResume() {
        super.onResume()
        if (!Printooth.hasPairedPrinter()) {
            //scanPrinterAndConnect()
        } else {
            val printer = Printooth.getPairedPrinter()
            binding.printPrinterName.text = printer?.name
            binding.printPrinterAddress.text = printer?.address
        }

    }
}