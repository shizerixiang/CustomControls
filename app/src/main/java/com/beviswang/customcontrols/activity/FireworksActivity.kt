package com.beviswang.customcontrols.activity

import android.Manifest
import android.app.PendingIntent
import android.content.Intent
import android.nfc.NdefMessage
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.os.Bundle
import android.util.Log
import android.view.View
import com.beviswang.customcontrols.BaseActivity
import com.beviswang.customcontrols.R
import com.beviswang.customcontrols.bindToolbarWithMenu
import com.beviswang.customcontrols.loge
import kotlinx.android.synthetic.main.activity_fireworks.*
import java.util.*
import kotlin.experimental.and


/**
 * 烟花绘制
 * @author BevisWang
 * @date 2019/9/26 11:37
 */
class FireworksActivity : BaseActivity() {
    private var mNfcAdapter: NfcAdapter? = null
    private var mPendingIntent: PendingIntent? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_fireworks)

//        bindToolbar("烟花绘制")
        bindToolbarWithMenu(
            "烟花绘制",
            menuVisibility = View.VISIBLE,
            menuView = layoutInflater.inflate(R.layout.layout_pop_menu, null)
        )

        requestSplashPermissions()
    }

    private fun requestSplashPermissions() =
        requestGPHPermissions(arrayOf(Manifest.permission.NFC), 0)

    override fun onRequestPermissionsDenied(requestCode: Int, permissions: List<String>) {
        requestSplashPermissions()
    }

    override fun onRequestPermissionsGranted(requestCode: Int) {
        bindData()
    }

    override fun onRequestPermissionsReject(requestCode: Int, permissions: List<String>) {
        finish()
    }

    private fun bindData() {
        mNfcAdapter = NfcAdapter.getDefaultAdapter(this)
        mPendingIntent = PendingIntent.getActivity(
            this,
            0,
            Intent(this, javaClass).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP),
            0
        )
    }

    override fun onResume() {
        super.onResume()
        if (mNfcAdapter?.isEnabled == true) {
            mNfcAdapter?.enableForegroundDispatch(this, mPendingIntent, null, null)
        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        resolveIntent(intent)
    }

    //初次判断是什么类型的NFC卡
    private fun resolveIntent(intent: Intent?) {
        val msgs: Array<NdefMessage>? = getNdefMsg(intent) //重点功能，解析nfc标签中的数据
        if (msgs == null) {
            showMsg("非NFC启动")
        } else {
            setNFCMsgView(msgs)
        }
    }

    private fun setNFCMsgView(ndefMessages: Array<NdefMessage>?) {
        if (ndefMessages == null || ndefMessages.isEmpty()) return
//    tvNFCMessage.setText("Payload:" + new String(ndefMessages[0].getRecords()[0].getPayload()) + "\n");
        val calendar: Calendar = Calendar.getInstance()
        val hour: Int = calendar.get(Calendar.HOUR_OF_DAY)
        val minute: Int = calendar.get(Calendar.MINUTE)
        val tvNFCMessage = StringBuilder("读取数据：")
        tvNFCMessage.append("$hour:$minute\n")
        tvNFCMessage.append(ndefMessages[0].toByteArray().toString())
        tv_nfc_content.text = tvNFCMessage
//        val records: List<ParsedNdefRecord> = NdefMessageParser.parse(ndefMessages[0])
//        val size = records.size
//        for (i in 0 until size) {
//            val record: ParsedNdefRecord = records[i]
//            tvNFCMessage.append(record.getViewText().toString() + "\n")
//        }
    }


    //初次判断是什么类型的NFC卡
    fun getNdefMsg(intent: Intent?): Array<NdefMessage>? {
        if (intent == null) return null
        //nfc卡支持的格式
        val tag: Tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG)
        val temp: Array<String> = tag.techList
        for (s in temp) {
            Log.i(TAG, "resolveIntent tag: $s")
        }
        val action = intent.action
        if (NfcAdapter.ACTION_NDEF_DISCOVERED == action || NfcAdapter.ACTION_TECH_DISCOVERED == action || NfcAdapter.ACTION_TAG_DISCOVERED == action) {
            val rawMessage = intent.getParcelableArrayExtra(NfcAdapter.ACTION_NDEF_DISCOVERED)
            if (NfcAdapter.ACTION_TAG_DISCOVERED == action){ // TAG 时，获取 TAG ID
                val id = intent.extras?.get("android.nfc.extra.ID") as ByteArray
            }

            var ndefMessages: Array<NdefMessage>? = null

            // 判断是哪种类型的数据 默认为NDEF格式 [18,-41,85,11]
            if (rawMessage != null) {
                Log.i(TAG, "getNdefMsg: ndef格式 ")
                ndefMessages = rawMessage.map { it as NdefMessage }.toTypedArray()
            } else {
                //未知类型 (公交卡类型)
                Log.i(TAG, "getNdefMsg: 未知类型")
                //对应的解析操作，在Github上有
            }
            return ndefMessages
        }
        return null
    }

    fun byteArrayToInt(bytes: ByteArray): Int {
        var value = 0
        for (i in 0..3) {
            val shift = (3 - i) * 8
            value += (bytes[i] and (0xFF).toByte()).toInt() shl shift
        }
        return value
    }

    companion object {
        private const val TAG = "FireworksActivity"
    }
}
