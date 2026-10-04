package com.example.backend

import android.util.Base64
import android.util.Log
import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.io.PrintWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.net.ssl.SSLSocket
import javax.net.ssl.SSLSocketFactory

class EmailNotificationService {

    private val smtpHost = "smtp.gmail.com"
    private val smtpPort = 465

    private val username = try {
        val configured = BuildConfig.EMAIL_USER.trim().removeSurrounding("\"")
        if (configured.isBlank()) "pugazhmanik24@gmail.com" else configured
    } catch (e: Exception) {
        "pugazhmanik24@gmail.com"
    }

    private val password = try {
        val configured = BuildConfig.EMAIL_PASS.trim().removeSurrounding("\"")
        if (configured.isBlank()) "pntglwwrffpciyth" else configured
    } catch (e: Exception) {
        "pntglwwrffpciyth"
    }

    private val recipient = try {
        val configured = BuildConfig.EMERGENCY_CONTACT_EMAIL.trim().removeSurrounding("\"")
        if (configured.isBlank()) "kpugahmani21@gmai.com" else configured
    } catch (e: Exception) {
        "kpugahmani21@gmai.com"
    }

    suspend fun sendEmergencyEmail(
        walkId: String,
        latitude: Double,
        longitude: Double,
        triggerReason: String,
        volunteerName: String?,
        volunteerPhone: String?,
        destination: String,
        batteryLevel: Int,
        trackingUrl: String
    ): Result<String> = withContext(Dispatchers.IO) {
        var socket: SSLSocket? = null
        try {
            if (username.isBlank() || password.isBlank() || recipient.isBlank()) {
                return@withContext Result.failure(Exception("Email credentials or recipient not configured"))
            }

            Log.i("EmailNotificationService", "Connecting to $smtpHost:$smtpPort over SSL...")
            val factory = SSLSocketFactory.getDefault()
            val sslSocket = factory.createSocket(smtpHost, smtpPort) as SSLSocket
            sslSocket.soTimeout = 15000
            socket = sslSocket

            val reader = BufferedReader(InputStreamReader(sslSocket.inputStream, "UTF-8"))
            val writer = PrintWriter(OutputStreamWriter(sslSocket.outputStream, "UTF-8"), true)

            // 1. Read initial 220 banner
            var line = reader.readLine() ?: ""
            Log.d("EmailNotificationService", "SMTP Server: $line")

            // Helper to execute command and check response code
            fun executeCommand(command: String, expectedPrefix: String) {
                writer.print("$command\r\n")
                writer.flush()
                val response = reader.readLine() ?: ""
                Log.d("EmailNotificationService", "SMTP Resp to [$command]: $response")
                if (!response.startsWith(expectedPrefix)) {
                    throw Exception("SMTP command [$command] failed. Expected prefix $expectedPrefix, got: $response")
                }
            }

            // 2. EHLO
            writer.print("EHLO localhost\r\n")
            writer.flush()
            while (true) {
                val resp = reader.readLine() ?: break
                if (resp.startsWith("250 ")) break
                if (!resp.startsWith("250-")) break
            }

            // 3. AUTH LOGIN
            executeCommand("AUTH LOGIN", "334")

            // 4. Send base64 Username
            val b64User = Base64.encodeToString(username.toByteArray(), Base64.NO_WRAP)
            executeCommand(b64User, "334")

            // 5. Send base64 Password
            val b64Pass = Base64.encodeToString(password.toByteArray(), Base64.NO_WRAP)
            executeCommand(b64Pass, "235")

            // 6. MAIL FROM
            executeCommand("MAIL FROM:<$username>", "250")

            // 7. RCPT TO
            executeCommand("RCPT TO:<$recipient>", "250")

            // 8. DATA
            executeCommand("DATA", "354")

            // 9. Send Headers and Content
            val mapsUrl = "https://maps.google.com/?q=$latitude,$longitude"
            val timeStr = SimpleDateFormat("dd MMM yyyy HH:mm:ss z", Locale.getDefault()).format(Date())

            val escortText = if (!volunteerName.isNullOrBlank()) {
                "$volunteerName (${volunteerPhone ?: "Verified Guardian"})"
            } else {
                "None (Walking Alone in Insecure Area)"
            }

            val htmlBody = """
                <html>
                <body style="font-family: Arial, sans-serif; background-color: #0b0814; color: #ffffff; padding: 24px;">
                    <div style="max-width: 600px; margin: auto; background-color: #161226; border-radius: 16px; border: 2px solid #ff1744; padding: 24px;">
                        <h2 style="color: #ff1744; margin-top: 0;">🚨 CRITICAL EMERGENCY SOS ALERT</h2>
                        <p style="font-size: 16px; line-height: 1.5;">This is an automated high-priority emergency dispatch from <strong>SafeWalk</strong>. A woman walking alone has triggered an emergency alarm in an insecure area and needs your immediate attention!</p>
                        
                        <div style="background-color: #241e38; border-radius: 10px; padding: 16px; margin: 16px 0;">
                            <p style="margin: 6px 0;"><strong>⚠️ Trigger Reason:</strong> <span style="color: #ffd54f;">$triggerReason</span></p>
                            <p style="margin: 6px 0;"><strong>📍 Live Coordinates:</strong> ${"%.5f".format(latitude)}° N, ${"%.5f".format(longitude)}° W</p>
                            <p style="margin: 6px 0;"><strong>🎯 Heading Towards:</strong> $destination</p>
                            <p style="margin: 6px 0;"><strong>🛡️ Volunteer Companion:</strong> $escortText</p>
                            <p style="margin: 6px 0;"><strong>🔋 Device Battery:</strong> $batteryLevel%</p>
                            <p style="margin: 6px 0;"><strong>⏰ Timestamp:</strong> $timeStr</p>
                            <p style="margin: 6px 0;"><strong>🚔 Police Precinct:</strong> 112 alerted via automatic emergency broadcast.</p>
                        </div>

                        <div style="margin: 20px 0; text-align: center;">
                            <a href="$mapsUrl" style="background-color: #ff1744; color: #ffffff; padding: 14px 24px; text-decoration: none; border-radius: 8px; font-weight: bold; display: inline-block; margin-right: 10px;">🗺️ Open Google Maps Live Location</a>
                            <br><br>
                            <a href="$trackingUrl" style="background-color: #7c4dff; color: #ffffff; padding: 12px 20px; text-decoration: none; border-radius: 8px; font-weight: bold; display: inline-block;">🔒 Open Encrypted Telemetry Stream</a>
                        </div>

                        <p style="font-size: 12px; color: #a099bc; margin-top: 24px;">SafeWalk Women Safety Sentinel • Walk ID: $walkId</p>
                    </div>
                </body>
                </html>
            """.trimIndent()

            val messageHeaders = """
From: "SafeWalk Emergency Sentinel" <$username>
To: <$recipient>
Subject: =?UTF-8?B?${Base64.encodeToString("🚨 URGENT: SafeWalk Emergency SOS Alert from Walker!".toByteArray(), Base64.NO_WRAP)}?=
MIME-Version: 1.0
Content-Type: text/html; charset=UTF-8
Content-Transfer-Encoding: 8bit

$htmlBody
            """.trimIndent()

            writer.print("$messageHeaders\r\n.\r\n")
            writer.flush()

            val dataResponse = reader.readLine() ?: ""
            Log.d("EmailNotificationService", "SMTP DATA response: $dataResponse")
            if (!dataResponse.startsWith("250")) {
                throw Exception("Failed to send email body. Server response: $dataResponse")
            }

            // 10. QUIT
            try {
                writer.print("QUIT\r\n")
                writer.flush()
            } catch (e: Exception) {
                // ignore
            }

            Log.i("EmailNotificationService", "Emergency Email dispatched successfully to $recipient!")
            Result.success("Emergency Email sent to $recipient")
        } catch (e: Exception) {
            Log.e("EmailNotificationService", "SMTP Error sending email: ${e.message}", e)
            Result.failure(e)
        } finally {
            try {
                socket?.close()
            } catch (e: Exception) {
                // ignore
            }
        }
    }
}
