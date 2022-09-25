package com.deepshikhayadav.invoiceapp

import android.Manifest
import android.Manifest.permission
import android.app.DatePickerDialog.OnDateSetListener
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Bitmap.createBitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.drawable.BitmapDrawable
import android.graphics.pdf.PdfDocument
import android.graphics.pdf.PdfDocument.PageInfo
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.util.DisplayMetrics
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.DatePicker
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.AppCompatEditText
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import com.deepshikhayadav.invoiceapp.databinding.FragmentSecondBinding
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import com.orhanobut.dialogplus.DialogPlus
import com.orhanobut.dialogplus.ViewHolder
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*


class SecondFragment : Fragment() {
    var file: File? = null
    var mContext : Context?= null
    var total=0
    var date : Date?= null
    lateinit var custNo :AppCompatEditText
    lateinit var custName :AppCompatEditText
    lateinit var tvdate : TextView
    lateinit var fromToPrice :AppCompatEditText
    lateinit var gstPrice :AppCompatEditText
    lateinit var parkingPrice :AppCompatEditText
    lateinit var tollPrice : AppCompatEditText
    lateinit var paidPrice : AppCompatEditText
    lateinit var fromTo : AppCompatEditText
    var d=""
    
    private var _binding: FragmentSecondBinding? = null


    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentSecondBinding.inflate(inflater, container, false)
        mContext = activity
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        custNo=binding.custNo
        custName = binding.custName
        tvdate = binding.tvdate
        gstPrice = binding.gstPrice
        fromToPrice = binding.fromToPrice
        parkingPrice = binding.parkingPrice
        tollPrice = binding.tollPrice
        paidPrice = binding.paidPrice
        fromTo = binding.fromTo


        date= Calendar.getInstance().time
        val df= SimpleDateFormat("dd-MMM-yyyy", Locale.getDefault())
        d=df.format(date)

        binding.tvTripDate2.text=" : $d"

        val materialDateBuilder: MaterialDatePicker.Builder<*> =
            MaterialDatePicker.Builder.datePicker()
        materialDateBuilder.setTitleText("SELECT A DATE")
        val materialDatePicker = materialDateBuilder.build()


        binding.date.setOnClickListener {
            materialDatePicker.show(requireActivity().supportFragmentManager, "MATERIAL_DATE_PICKER")
        }
        materialDatePicker.addOnPositiveButtonClickListener {
            binding.date.visibility=View.GONE
            binding.tvTripDate1.visibility= View.VISIBLE
            binding.tvTripDate1.text= ": ${materialDatePicker.headerText}"
            binding.tvTripDate2.text=  materialDatePicker.headerText
        }

        binding.submit.setOnClickListener {
            generateFile()

        }
        binding.share.setOnClickListener {
            binding.share.visibility=View.GONE
            checkPermission()

        }

    }

    private fun checkValidation(): Boolean {
        if(custNo.text.toString().isNotEmpty() && custName.text.toString().isNotEmpty()){
            return true
        }
        Toast.makeText(mContext,"Plz provide Customer name and Number",Toast.LENGTH_SHORT).show()
        return false

    }

    private fun generateDesignPdf() {
        val displayMetrics = DisplayMetrics()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            requireContext().display?.getRealMetrics(displayMetrics)
            displayMetrics.densityDpi
        }
        else{
            requireActivity().windowManager.defaultDisplay.getMetrics(displayMetrics)
        }
        requireView().measure(
            View.MeasureSpec.makeMeasureSpec(
                displayMetrics.widthPixels, View.MeasureSpec.EXACTLY
            ),
            View.MeasureSpec.makeMeasureSpec(
                displayMetrics.heightPixels, View.MeasureSpec.EXACTLY
            )
        )

        requireView().layout(0, 0, displayMetrics.widthPixels, displayMetrics.heightPixels)
        val bitmap = Bitmap.createBitmap(requireView().measuredWidth, requireView().measuredHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        requireView().draw(canvas)

        Bitmap.createScaledBitmap(bitmap, requireView().measuredWidth, requireView().measuredHeight, true)
        val pdfDocument = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(requireView().measuredWidth, requireView().measuredHeight, 1).create()
        val page = pdfDocument.startPage(pageInfo)
        page.canvas.drawBitmap(bitmap, 0F, 0F, null)
        val title = Paint()
        title.textSize = 15f
        title.textAlign = Paint.Align.LEFT
        page.canvas.drawText("© Copyright by Sagar Tour & Travels", 20f, (requireView().measuredHeight).toFloat()-10,title)
        pdfDocument.finishPage(page)
        val df= SimpleDateFormat("ddMM", Locale.getDefault())
        file = File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
          "Invoice_${custName.text.toString()}${df.format(date)}.pdf"
        )

        try {
            pdfDocument.writeTo(FileOutputStream(file))

            val uri = FileProvider.getUriForFile(
                mContext!!, "com.deepshikhayadav.invoiceapp" + ".provider",
                file!!
            )
            val share = Intent()
            val custWhatsappNo = "91${ custNo.text.toString() }"
            share.action = Intent.ACTION_SEND
            share.type = "application/pdf"
            share.putExtra(Intent.EXTRA_STREAM, uri)
            share.putExtra("jid", "$custWhatsappNo@s.whatsapp.net")
            share.setPackage("com.whatsapp")

            requireActivity().startActivity(share)
        }
        catch (e:Exception){
            Toast.makeText(mContext,e.message,Toast.LENGTH_SHORT).show()
            Log.i("deepu",e.message.toString())
        }
        pdfDocument.close()
    }
    private fun requestAllPermission() {
        ActivityCompat.requestPermissions(
            requireActivity(),
            arrayOf(
                permission.READ_EXTERNAL_STORAGE ,
                permission.WRITE_EXTERNAL_STORAGE
            ),
            10
        )
    }
    private fun checkPermission() {
        if (ContextCompat.checkSelfPermission(
                mContext!!,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED
            && ContextCompat.checkSelfPermission(
                mContext!!,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED ) {

            generateDesignPdf()
        } else {
            requestAllPermission()
        }

    }

    private fun generateFile() {
        if(checkValidation()){

            binding.form.visibility=View.GONE
            binding.invoice.visibility=View.VISIBLE
            binding.tvClient.text = custName.text.toString()

            tvdate.text= " $d"

            if(fromToPrice.text!!.isNotEmpty()){
                total+=fromToPrice.text.toString().toInt()
            }
            if(gstPrice.text!!.isNotEmpty()){
                total+=gstPrice.text.toString().toInt()
            }
            if(parkingPrice.text!!.isNotEmpty()) {
                total+=parkingPrice.text.toString().toInt()
            }
            if(tollPrice.text!!.isNotEmpty()) {
                total+=tollPrice.text.toString().toInt()
            }
            if(paidPrice.text!!.isNotEmpty()){
                total-=paidPrice.text.toString().toInt()
            }

            binding.tvFromTo.text = fromTo.text
            binding.valFromTo.text = "₹ ${fromToPrice.text}"
            binding.valParking.text = "₹ ${parkingPrice.text}"
            binding.valGst.text = "₹ ${gstPrice.text}"
            binding.valPaid.text = "₹ ${paidPrice.text}"
            binding.valToll.text = "₹ ${tollPrice.text}"
            binding.valTotal.text = "₹ $total"

        }
    }


    override fun onRequestPermissionsResult(requestCode: Int,
                                            permissions: Array<String>,
                                            grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 10) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(mContext!!, "Storage Permission Granted", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(mContext!!, "Storage Permission Denied", Toast.LENGTH_SHORT).show()
            }
        } else if (requestCode == 20) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(mContext!!, "Storage Permission Granted", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(mContext!!, "Storage Permission Denied", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroyView() {
       super.onDestroyView()
       _binding = null
   }
}
