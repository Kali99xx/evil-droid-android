package com.evil.droid

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.lifecycleScope
import com.evil.droid.databinding.ActivityMainBinding
import com.evil.droid.util.ShellExecutor
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private var isRootAvailable = false

    private val filePickerLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                val path = getPathFromUri(uri)
                binding.etApkPath.setText(path)
            }
        }
    }

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val allGranted = permissions.values.all { it }
        if (!allGranted) {
            showAlert(getString(R.string.input_error), "Storage permissions are required for this app to function properly.")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupUI()
        requestPermissions()
        checkRootAccess()
    }

    private fun setupUI() {
        // Set button click listeners
        binding.btnCheckRoot.setOnClickListener { checkRootAccess() }
        binding.btnInstallApk.setOnClickListener { installApk() }
        binding.btnRunPayload.setOnClickListener { runPayload() }
        binding.btnBrowseApk.setOnClickListener { browseApkFile() }
        binding.btnClearLog.setOnClickListener { clearLog() }

        // Initialize log with welcome message
        appendLog("Evil-Droid Android - Penetration Testing Framework", LogType.INFO)
        appendLog("Initializing application...", LogType.INFO)
    }

    private fun requestPermissions() {
        val permissions = mutableListOf<String>()
        
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) 
            != PackageManager.PERMISSION_GRANTED) {
            permissions.add(Manifest.permission.READ_EXTERNAL_STORAGE)
        }
        
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) 
            != PackageManager.PERMISSION_GRANTED) {
            permissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        }

        if (permissions.isNotEmpty()) {
            permissionLauncher.launch(permissions.toTypedArray())
        }

        // Request manage external storage permission for Android 11+
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
            if (!Environment.isExternalStorageManager()) {
                val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
                intent.data = Uri.parse("package:$packageName")
                startActivity(intent)
            }
        }
    }

    private fun checkRootAccess() {
        appendLog("Checking root access...", LogType.INFO)
        showProgress(true)
        
        lifecycleScope.launch {
            try {
                isRootAvailable = ShellExecutor.checkRootAccess()
                
                if (isRootAvailable) {
                    appendLog("✓ Device is rooted and ready", LogType.SUCCESS)
                    
                    // Get additional device info
                    val (deviceInfoSuccess, deviceInfo) = ShellExecutor.getDeviceInfo()
                    if (deviceInfoSuccess) {
                        appendLog("Device Information:", LogType.INFO)
                        appendLog(deviceInfo, LogType.INFO)
                    }
                } else {
                    appendLog("✗ Root access not available", LogType.ERROR)
                    showAlert(
                        getString(R.string.root_required),
                        getString(R.string.root_required_message)
                    )
                }
            } catch (e: Exception) {
                appendLog("Error checking root: ${e.message}", LogType.ERROR)
            } finally {
                showProgress(false)
            }
        }
    }

    private fun browseApkFile() {
        val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
            type = "application/vnd.android.package-archive"
            addCategory(Intent.CATEGORY_OPENABLE)
        }
        
        try {
            filePickerLauncher.launch(Intent.createChooser(intent, "Select APK File"))
        } catch (e: Exception) {
            appendLog("Error opening file picker: ${e.message}", LogType.ERROR)
            Toast.makeText(this, "Please enter APK path manually", Toast.LENGTH_SHORT).show()
        }
    }

    private fun installApk() {
        val apkPath = binding.etApkPath.text.toString().trim()
        
        if (apkPath.isEmpty()) {
            showAlert(getString(R.string.input_error), "Please enter a valid APK file path.")
            return
        }
        
        val apkFile = File(apkPath)
        if (!apkFile.exists()) {
            showAlert(getString(R.string.file_error), "APK file does not exist at the specified path.")
            return
        }

        appendLog("Installing APK: $apkPath", LogType.INFO)
        showProgress(true)

        lifecycleScope.launch {
            try {
                if (isRootAvailable) {
                    // Use root installation
                    val (success, output) = ShellExecutor.installApkRoot(apkPath)
                    if (success) {
                        appendLog("✓ APK installed successfully via root", LogType.SUCCESS)
                        appendLog(output, LogType.INFO)
                    } else {
                        appendLog("✗ Root installation failed: $output", LogType.ERROR)
                        // Fallback to system installer
                        installApkViaSystem(apkFile)
                    }
                } else {
                    // Use system installer
                    installApkViaSystem(apkFile)
                }
            } catch (e: Exception) {
                appendLog("Installation error: ${e.message}", LogType.ERROR)
            } finally {
                showProgress(false)
            }
        }
    }

    private fun installApkViaSystem(apkFile: File) {
        try {
            val uri = FileProvider.getUriForFile(
                this,
                "$packageName.fileprovider",
                apkFile
            )
            
            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, "application/vnd.android.package-archive")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            
            startActivity(intent)
            appendLog("✓ APK installation initiated via system installer", LogType.SUCCESS)
        } catch (e: Exception) {
            appendLog("✗ System installation failed: ${e.message}", LogType.ERROR)
            showAlert(getString(R.string.installation_error), "Failed to launch installer: ${e.message}")
        }
    }

    private fun runPayload() {
        val targetPackage = binding.etTargetPackage.text.toString().trim()
        val apkPath = binding.etApkPath.text.toString().trim()
        
        if (targetPackage.isEmpty()) {
            showAlert(getString(R.string.input_error), "Please enter a target package name.")
            return
        }

        if (!isRootAvailable) {
            showAlert(getString(R.string.root_required), "Root access is required for payload execution.")
            return
        }

        appendLog("Starting payload execution for package: $targetPackage", LogType.INFO)
        showProgress(true)

        lifecycleScope.launch {
            try {
                val (success, output) = ShellExecutor.executePayload(targetPackage, apkPath)
                
                if (success) {
                    appendLog("✓ Payload execution completed successfully", LogType.SUCCESS)
                    appendLog(output, LogType.INFO)
                } else {
                    appendLog("✗ Payload execution failed", LogType.ERROR)
                    appendLog(output, LogType.ERROR)
                }
            } catch (e: Exception) {
                appendLog("Payload error: ${e.message}", LogType.ERROR)
            } finally {
                showProgress(false)
            }
        }
    }

    private fun clearLog() {
        binding.tvLog.text = ""
        appendLog("Log cleared", LogType.INFO)
    }

    private fun appendLog(message: String, type: LogType = LogType.INFO) {
        val timestamp = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
        val logMessage = "[$timestamp] $message\n"
        
        val spannable = SpannableStringBuilder(logMessage)
        val color = when (type) {
            LogType.SUCCESS -> ContextCompat.getColor(this, R.color.green_success)
            LogType.ERROR -> ContextCompat.getColor(this, R.color.red_error)
            LogType.WARNING -> ContextCompat.getColor(this, R.color.orange_warning)
            LogType.INFO -> ContextCompat.getColor(this, R.color.blue_info)
        }
        
        spannable.setSpan(
            ForegroundColorSpan(color),
            0,
            logMessage.length,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        
        binding.tvLog.append(spannable)
        
        // Auto-scroll to bottom
        binding.scrollViewLog.post {
            binding.scrollViewLog.fullScroll(android.view.View.FOCUS_DOWN)
        }
    }

    private fun showProgress(show: Boolean) {
        binding.progressBar.visibility = if (show) android.view.View.VISIBLE else android.view.View.GONE
        
        // Disable buttons during operation
        binding.btnCheckRoot.isEnabled = !show
        binding.btnInstallApk.isEnabled = !show
        binding.btnRunPayload.isEnabled = !show
        binding.btnBrowseApk.isEnabled = !show
    }

    private fun showAlert(title: String, message: String) {
        AlertDialog.Builder(this)
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton(getString(R.string.ok)) { dialog, _ -> dialog.dismiss() }
            .show()
    }

    private fun getPathFromUri(uri: Uri): String {
        // Simple implementation - in production, use a more robust method
        return uri.path ?: uri.toString()
    }

    enum class LogType {
        INFO, SUCCESS, ERROR, WARNING
    }
}
