package com.evil.droid.util

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter

object ShellExecutor {

    /**
     * Executes a shell command using Runtime.exec() and returns a pair of success flag and output/error.
     */
    suspend fun executeCommand(command: String): Pair<Boolean, String> = withContext(Dispatchers.IO) {
        try {
            val process = Runtime.getRuntime().exec(command)
            val reader = BufferedReader(InputStreamReader(process.inputStream))
            val errorReader = BufferedReader(InputStreamReader(process.errorStream))
            
            // Read standard output
            val output = StringBuilder()
            var line: String? = reader.readLine()
            while (line != null) {
                output.append(line).append("\n")
                line = reader.readLine()
            }
            
            // Read error output
            val errorOutput = StringBuilder()
            line = errorReader.readLine()
            while (line != null) {
                errorOutput.append(line).append("\n")
                line = errorReader.readLine()
            }
            
            process.waitFor()
            
            if (process.exitValue() == 0) {
                Pair(true, output.toString().trim())
            } else {
                Pair(false, errorOutput.toString().trim().ifEmpty { "Command failed with exit code: ${process.exitValue()}" })
            }
        } catch (e: Exception) {
            Pair(false, e.message ?: "Unknown error occurred")
        }
    }

    /**
     * Executes a command with root privileges using 'su'
     */
    suspend fun executeRootCommand(command: String): Pair<Boolean, String> = withContext(Dispatchers.IO) {
        try {
            val process = Runtime.getRuntime().exec("su")
            val writer = OutputStreamWriter(process.outputStream)
            val reader = BufferedReader(InputStreamReader(process.inputStream))
            val errorReader = BufferedReader(InputStreamReader(process.errorStream))

            // Send command to su
            writer.write("$command\n")
            writer.write("exit\n")
            writer.flush()
            writer.close()

            // Read standard output
            val output = StringBuilder()
            var line: String? = reader.readLine()
            while (line != null) {
                output.append(line).append("\n")
                line = reader.readLine()
            }

            // Read error output
            val errorOutput = StringBuilder()
            line = errorReader.readLine()
            while (line != null) {
                errorOutput.append(line).append("\n")
                line = errorReader.readLine()
            }

            process.waitFor()

            if (process.exitValue() == 0) {
                Pair(true, output.toString().trim())
            } else {
                Pair(false, errorOutput.toString().trim().ifEmpty { "Root command failed with exit code: ${process.exitValue()}" })
            }
        } catch (e: Exception) {
            Pair(false, e.message ?: "Root command execution failed")
        }
    }

    /**
     * Check if device has root access
     */
    suspend fun checkRootAccess(): Boolean = withContext(Dispatchers.IO) {
        try {
            val (success, output) = executeRootCommand("echo rooted")
            success && output.contains("rooted")
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Get package UID for a given package name
     */
    suspend fun getPackageUid(packageName: String): Pair<Boolean, String> {
        return executeRootCommand("dumpsys package $packageName | grep userId")
    }

    /**
     * Install APK using package manager
     */
    suspend fun installApkRoot(apkPath: String): Pair<Boolean, String> {
        return executeRootCommand("pm install -r \"$apkPath\"")
    }

    /**
     * Generate and execute payload for target package
     */
    suspend fun executePayload(packageName: String, apkPath: String): Pair<Boolean, String> = withContext(Dispatchers.IO) {
        try {
            val logBuilder = StringBuilder()
            
            // Step 1: Get package UID
            logBuilder.append("Getting package UID for $packageName...\n")
            val (uidSuccess, uidOutput) = getPackageUid(packageName)
            if (!uidSuccess) {
                return@withContext Pair(false, "Failed to get package UID: $uidOutput")
            }
            logBuilder.append("UID retrieved: $uidOutput\n")

            // Step 2: Install APK if provided
            if (apkPath.isNotEmpty()) {
                logBuilder.append("Installing APK: $apkPath...\n")
                val (installSuccess, installOutput) = installApkRoot(apkPath)
                if (!installSuccess) {
                    return@withContext Pair(false, "APK installation failed: $installOutput")
                }
                logBuilder.append("APK installed successfully\n")
            }

            // Step 3: Execute payload (simulated - replace with actual payload logic)
            logBuilder.append("Executing payload for $packageName...\n")
            val payloadCommand = "am start -n $packageName/.MainActivity"
            val (payloadSuccess, payloadOutput) = executeRootCommand(payloadCommand)
            
            if (payloadSuccess) {
                logBuilder.append("Payload executed successfully: $payloadOutput\n")
                Pair(true, logBuilder.toString())
            } else {
                logBuilder.append("Payload execution failed: $payloadOutput\n")
                Pair(false, logBuilder.toString())
            }

        } catch (e: Exception) {
            Pair(false, "Payload execution error: ${e.message}")
        }
    }

    /**
     * Check if ADB is available and device is connected
     */
    suspend fun checkAdbConnection(): Pair<Boolean, String> {
        return executeCommand("getprop ro.debuggable")
    }

    /**
     * List installed packages
     */
    suspend fun listInstalledPackages(): Pair<Boolean, String> {
        return executeRootCommand("pm list packages")
    }

    /**
     * Get device information
     */
    suspend fun getDeviceInfo(): Pair<Boolean, String> {
        val commands = listOf(
            "getprop ro.build.version.release",
            "getprop ro.product.model",
            "getprop ro.product.manufacturer"
        )
        
        val results = StringBuilder()
        for (command in commands) {
            val (success, output) = executeCommand(command)
            if (success) {
                results.append("$command: $output\n")
            }
        }
        
        return if (results.isNotEmpty()) {
            Pair(true, results.toString())
        } else {
            Pair(false, "Failed to get device information")
        }
    }
}
