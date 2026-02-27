package com.example.payoffline.data.repository

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.telephony.SubscriptionManager
import androidx.core.content.ContextCompat
import com.example.payoffline.data.model.*

class UssdRepository(private val context: Context) {

    companion object {
        // Correct NPCI *99# USSD codes
        const val USSD_BASE       = "*99#"
        const val USSD_LINK_BANK  = "*99*2#"
        const val USSD_CHECK_BAL  = "*99*3#"
        const val USSD_MINI_STMT  = "*99*4#"
        const val USSD_CHANGE_PIN = "*99*5#"
    }

    fun hasPhoneStatePermission(): Boolean =
        ContextCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE) ==
                PackageManager.PERMISSION_GRANTED

    @SuppressLint("MissingPermission")
    fun loadSims(): List<SimInfo> {
        if (!hasPhoneStatePermission()) return emptyList()
        return try {
            val subManager = context.getSystemService(Context.TELEPHONY_SUBSCRIPTION_SERVICE)
                    as? SubscriptionManager ?: return emptyList()
            val subs = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
                subManager.activeSubscriptionInfoList
            else
                @Suppress("DEPRECATION") subManager.activeSubscriptionInfoList

            subs?.mapIndexed { index, info ->
                SimInfo(
                    slotIndex      = info.simSlotIndex,
                    subscriptionId = info.subscriptionId,
                    displayName    = info.displayName?.toString() ?: "SIM ${index + 1}",
                    carrierName    = info.carrierName?.toString() ?: "Unknown Carrier",
                    number         = try { info.number?.takeIf { it.isNotBlank() } } catch (_: Exception) { null }
                )
            } ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    /**
     * Send money — opens dialer with *99# and instructions.
     * One-shot codes (*99*1*mobile*amount*pin#) don't work on most Indian carriers
     * because carriers show interactive menus, treating extra digits as menu input.
     */
    fun sendMoney(recipient: String, amount: String): UssdResult =
        openDialer(
            code = USSD_BASE,
            response = "Dialer opened with *99#\n\n" +
                "Follow these steps:\n" +
                "1️⃣ Dial *99# → tap Call\n" +
                "2️⃣ Select option 1 (Send Money)\n" +
                "3️⃣ Enter recipient: $recipient\n" +
                "4️⃣ Enter amount: ₹$amount\n" +
                "5️⃣ Enter your UPI PIN\n" +
                "6️⃣ Confirm the payment"
        )

    /** Check balance — opens *99*3# */
    fun checkBalance(): UssdResult =
        openDialer(
            code = USSD_CHECK_BAL,
            response = "Dialer opened with *99*3#\n\n" +
                "Tap Call, then enter your UPI PIN when prompted.\n" +
                "Your bank balance will be shown on screen."
        )

    /** Mini statement — opens *99*4# */
    fun miniStatement(): UssdResult =
        openDialer(
            code = USSD_MINI_STMT,
            response = "Dialer opened with *99*4#\n\n" +
                "Tap Call to view your last 5 transactions.\n" +
                "You may need to enter your UPI PIN."
        )

    /** Link bank account — opens *99*2# */
    fun linkBankAccount(): UssdResult =
        openDialer(
            code = USSD_LINK_BANK,
            response = "Dialer opened with *99*2#\n\n" +
                "Tap Call and follow the steps to link your bank account.\n" +
                "Make sure your mobile number is registered with your bank."
        )

    /** Change UPI PIN — opens *99*5# */
    fun changePin(): UssdResult =
        openDialer(
            code = USSD_CHANGE_PIN,
            response = "Dialer opened with *99*5#\n\n" +
                "Tap Call and follow the steps to set or change your UPI PIN."
        )

    /** Open main *99# menu */
    fun openMainMenu(): UssdResult =
        openDialer(
            code = USSD_BASE,
            response = "Main *99# menu opened in Dialer.\nTap Call to start."
        )

    private fun openDialer(code: String, response: String): UssdResult {
        return try {
            val encoded = Uri.encode(code)
            val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$encoded")).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
            UssdResult(success = true, response = response, errorType = UssdErrorType.NONE)
        } catch (e: Exception) {
            UssdResult(
                success   = false,
                response  = "Could not open dialer: ${e.localizedMessage}",
                errorType = UssdErrorType.UNKNOWN
            )
        }
    }

    fun openDialerWithUssd(code: String) {
        try {
            val encoded = Uri.encode(code)
            val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$encoded")).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (_: Exception) { }
    }
}
