package br.com.popgas.flutter_sms_listener

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.annotation.NonNull
import com.google.android.gms.auth.api.phone.SmsRetriever
import com.google.android.gms.common.api.CommonStatusCodes.SUCCESS
import com.google.android.gms.common.api.CommonStatusCodes.TIMEOUT
import com.google.android.gms.common.api.Status
import io.flutter.Log
import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import io.flutter.plugin.common.MethodChannel.Result
import io.flutter.plugin.common.PluginRegistry.Registrar

class FlutterSmsListenerPlugin: FlutterPlugin, MethodCallHandler {

  var context: Context? = null
  private var receiver: SMSBroadcastListener? = null
  var sms: String? = null
  var result: Result? = null

  override fun onAttachedToEngine(@NonNull flutterPluginBinding: FlutterPlugin.FlutterPluginBinding) {
    val plugin = FlutterSmsListenerPlugin()
    plugin.context = flutterPluginBinding.applicationContext

    val channel = MethodChannel(flutterPluginBinding.binaryMessenger, "flutter_sms_listener")
    channel.setMethodCallHandler(plugin)
  }

  companion object {
    @JvmStatic
    fun registerWith(registrar: Registrar) {
      val plugin = FlutterSmsListenerPlugin()
      plugin.context = registrar.context()

      val channel = MethodChannel(registrar.messenger(), "flutter_sms_listener")
      channel.setMethodCallHandler(plugin)
    }
  }

  override fun onMethodCall(@NonNull call: MethodCall, @NonNull result: Result) {
    when {
      call.method == "getAppSignature" -> {
        context?.let {
          val signature = AppSignatureHelper(it).getAppSignatures()[0]
          result.success(signature)
        }
      }
      call.method == "startListening" -> {
        context?.let {
          receiver = SMSBroadcastListener()
          it.registerReceiver(receiver, IntentFilter(SmsRetriever.SMS_RETRIEVED_ACTION))
          startListening(it)
          this.result = result
        }
      }
      call.method == "stopListening" -> {
        context?.let(this::unregister)
      }
      else -> result.notImplemented()
    }
  }


  private fun startListening(context: Context) {
    val client = SmsRetriever.getClient(context)

    val task = client.startSmsRetriever()

    task.addOnSuccessListener {
      Log.e(javaClass::getSimpleName.name, "task started")

    }

    task.addOnFailureListener {
      Log.e(javaClass::getSimpleName.name, "task starting failed")
    }


  }

  private fun unregister(context: Context) {
    context.unregisterReceiver(receiver)
  }

  inner class SMSBroadcastListener : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
      if (SmsRetriever.SMS_RETRIEVED_ACTION == intent.action) {
        val extras = intent.extras
        val status = extras!!.get(SmsRetriever.EXTRA_STATUS) as Status

        when (status.statusCode) {
          SUCCESS -> {
            sms = extras.get(SmsRetriever.EXTRA_SMS_MESSAGE) as String
            result?.success(sms)
          }

          TIMEOUT -> {}
        }
      }
    }
  }

  override fun onDetachedFromEngine(@NonNull binding: FlutterPlugin.FlutterPluginBinding) {}
}
