package com.example.util

import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.media.ToneGenerator
import android.net.Uri
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import java.net.URLEncoder
import java.util.Locale

object EmergencyHelper {

    private var toneGenerator: ToneGenerator? = null

    fun playSirenSound() {
        try {
            stopSirenSound()
            toneGenerator = ToneGenerator(AudioManager.STREAM_ALARM, 100)
            toneGenerator?.startTone(ToneGenerator.TONE_CDMA_EMERGENCY_RINGBACK, 3000)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun stopSirenSound() {
        try {
            toneGenerator?.stopTone()
            toneGenerator?.release()
            toneGenerator = null
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun triggerVibration(context: Context) {
        try {
            val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vm = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                vm?.defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val pattern = longArrayOf(0, 500, 200, 500, 200, 1000)
                vibrator?.vibrate(VibrationEffect.createWaveform(pattern, -1))
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(longArrayOf(0, 500, 200, 500), -1)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun openDialer(context: Context, phoneNumber: String) {
        val intent = Intent(Intent.ACTION_DIAL).apply {
            data = Uri.parse("tel:$phoneNumber")
        }
        context.startActivity(intent)
    }

    fun sendSosWhatsApp(context: Context, targetPhone: String, userName: String, blok: String, jenisDarurat: String) {
        val rawMessage = "🚨 *DARURAT! SOS RT PURI PRATAMA* 🚨\n\n" +
                "Mohon bantuan segera di lingkungan RT kita!\n" +
                "• Pelapor: $userName\n" +
                "• Lokasi: $blok\n" +
                "• Situasi: $jenisDarurat\n" +
                "• Waktu: SEKARANG\n\n" +
                "Harap segera menuju lokasi atau hubungi pos satpam/pengurus RT!"

        val encoded = try {
            URLEncoder.encode(rawMessage, "UTF-8")
        } catch (e: Exception) {
            rawMessage
        }

        val cleanPhone = targetPhone.replace("+", "").replace("-", "").replace(" ", "")
        val uri = if (cleanPhone.isNotBlank()) {
            Uri.parse("https://api.whatsapp.com/send?phone=$cleanPhone&text=$encoded")
        } else {
            Uri.parse("https://api.whatsapp.com/send?text=$encoded")
        }

        val intent = Intent(Intent.ACTION_VIEW, uri)
        context.startActivity(intent)
    }

    fun sendIuranReminderWhatsApp(context: Context, targetPhone: String, wargaName: String, blok: String, periode: String, nominal: Long, bankInfo: String) {
        val nominalFormatted = "Rp " + String.format(Locale("id", "ID"), "%,d", nominal)
        val rawMessage = "Assalamu'alaikum Wr. Wb. / Salam Sejahtera,\n\n" +
                "Yth. Bapak/Ibu *$wargaName* ($blok),\n\n" +
                "Kami dari *Pengurus RT PURI PRATAMA* menyampaikan pengingat iuran lingkungan untuk periode *$periode* sebesar *$nominalFormatted*.\n\n" +
                "Iuran dapat ditransfer ke:\n" +
                "$bankInfo\n\n" +
                "Atau diserahkan langsung ke Bendahara RT saat penarikan rutin. Mohon konfirmasi jika sudah melakukan transfer.\n\n" +
                "Terima kasih atas partisipasi dan kepedulian Bapak/Ibu dalam menjaga kenyamanan dan keamanan RT Puri Pratama. 🙏"

        val encoded = try {
            URLEncoder.encode(rawMessage, "UTF-8")
        } catch (e: Exception) {
            rawMessage
        }

        var cleanPhone = targetPhone.replace("+", "").replace("-", "").replace(" ", "")
        if (cleanPhone.startsWith("08")) {
            cleanPhone = "62" + cleanPhone.substring(1)
        }

        val uri = Uri.parse("https://api.whatsapp.com/send?phone=$cleanPhone&text=$encoded")
        val intent = Intent(Intent.ACTION_VIEW, uri)
        context.startActivity(intent)
    }
}
