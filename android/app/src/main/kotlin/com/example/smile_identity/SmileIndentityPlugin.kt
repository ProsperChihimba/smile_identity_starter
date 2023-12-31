package com.example.smile_identity

import android.app.Activity
import android.text.TextUtils
import android.widget.Toast
import com.smileid.smileidui.CaptureType
import com.smileid.smileidui.SIDCaptureManager
import com.smileid.smileidui.SIDIDCaptureConfig
import com.smileid.smileidui.SIDSelfieCaptureConfig
import com.smileidentity.libsmileid.core.RetryOnFailurePolicy
import com.smileidentity.libsmileid.core.SIDConfig
import com.smileidentity.libsmileid.core.SIDNetworkRequest
import com.smileidentity.libsmileid.model.SIDMetadata
import com.smileidentity.libsmileid.model.SIDNetData
import io.flutter.plugin.common.BinaryMessenger
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import io.flutter.plugin.common.MethodChannel.Result
import java.util.concurrent.TimeUnit
import java.util.regex.Pattern

class SmileData(
    val userId: String,
    val jobId: String,
    val country: String,
    val idType: String,
    val idNumber: String,
    val firstName: String,
    val lastName: String,
    val tag: String,
)

class SmileIdentityPlugin : MethodCallHandler {

  private lateinit var channel: MethodChannel
  private lateinit var request: SIDNetworkRequest
  private lateinit var activity: Activity
  private lateinit var smileData: SmileData
  private lateinit var _result: Result

  fun registerChannel(binaryMessenger: BinaryMessenger) {
    channel = MethodChannel(binaryMessenger, "smile_identity")
    channel.setMethodCallHandler(this)
  }

  fun deRegisterChannel() {
    channel.setMethodCallHandler(null)
  }

  override fun onMethodCall(call: MethodCall, result: Result) {
    _result = result
    when (call.method) {
      "getPlatformVersion" -> {
        result.success("Android ${android.os.Build.VERSION.RELEASE}")
      }
      "capture" -> {
        saveData(call.arguments<Map<String, String>>() as Map<String, String>)
        captureSelfie()
      }
      "submit" -> {
        saveData(call.arguments<Map<String, String>>() as Map<String, String>)
        Toast.makeText(activity, "Submitting job...", Toast.LENGTH_SHORT).show()
        submitJob()
      }
      else -> {
        result.notImplemented()
      }
    }
  }

  private fun saveData(args: Map<String, String>) {
    smileData =
        SmileData(
            userId = (args["userId"] ?: ""),
            jobId = (args["jobId"] ?: ""),
            country = (args["country"] ?: ""),
            idType = (args["idType"] ?: ""),
            idNumber = (args["idNumber"] ?: ""),
            firstName = (args["firstName"] ?: ""),
            lastName = (args["lastName"] ?: ""),
            tag = (args["tag"] ?: ""),
        )
  }

  private fun captureSelfie() {
    val tag = smileData.tag

    try {
      val validTag = isTagFormatValid(tag)
      if (!validTag) throw Exception("You have passed an invalid tag")

      val type = CaptureType.SELFIE_AND_ID_CAPTURE
      val builder = SIDCaptureManager.Builder(activity, type, 1000 + type.ordinal)
      builder.setTag(tag)

      val selfieCaptureConfig = (SIDSelfieCaptureConfig.Builder()).build()
      val idCaptureConfig = (SIDIDCaptureConfig.Builder()).build()

      builder.setSidSelfieConfig(selfieCaptureConfig)
      builder.setSidIdCaptureConfig(idCaptureConfig)
      builder.build().start()
    } catch (e: Exception) {
      channel.invokeMethod("capture_state", mapOf("success" to false))
      Toast.makeText(activity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
    }
  }

  private fun submitJob() {
    try {
      val data = SIDNetData(this.activity, SIDNetData.Environment.PROD)
      // data.callBackUrl = "call-back-url"

      val meta = SIDMetadata()
      meta.sidUserIdInfo.countryCode = smileData.country
      //meta.sidUserIdInfo.firstName = smileData.firstName
      //meta.sidUserIdInfo.lastName = smileData.lastName

      // https://docs.smileidentity.com/supported-id-types/for-individuals-kyc/backed-by-id-authority
      //meta.sidUserIdInfo.idType = smileData.idType
      //meta.sidUserIdInfo.idNumber = smileData.idNumber

      meta.partnerParams.jobId = smileData.jobId
      meta.partnerParams.jobType = 1
      meta.partnerParams.userId = smileData.userId
      meta.partnerParams.additionalValue("profile_id", smileData.userId)

      val builder =
          SIDConfig.Builder(activity)
              .setRetryOnfailurePolicy(getRetryOnFailurePolicy())
              .setMode(SIDConfig.Mode.ENROLL)
              .setSmileIdNetData(data)
              .setSIDMetadata(meta)
              .setJobType(1)

      val config = builder.build(smileData.tag)
      request.submit(config)
      channel.invokeMethod("submit_state", mapOf("success" to true))
    } catch (e: Exception) {
      Toast.makeText(activity, "${e.message}", Toast.LENGTH_SHORT).show()
      channel.invokeMethod("submit_state", mapOf("success" to false))
    }
  }

  private fun getRetryOnFailurePolicy(): RetryOnFailurePolicy {
    val retryOnFailurePolicy = RetryOnFailurePolicy()
    retryOnFailurePolicy.setRetryCount(10)
    retryOnFailurePolicy.setRetryTimeout(TimeUnit.SECONDS.toMillis(15))
    return retryOnFailurePolicy
  }

  private fun isTagFormatValid(tag: String?): Boolean {
    return StringUtils.hasSpecialChars(tag)
  }

  fun initializeActivity(_activity: Activity) {
    activity = _activity

    request = SIDNetworkRequest(activity)
    request.setOnCompleteListener {
      Toast.makeText(activity, "completed", Toast.LENGTH_SHORT).show()
    }
    request.set0nErrorListener {
      Toast.makeText(activity, "An error happened: ${it.message}", Toast.LENGTH_SHORT).show()
    }
    request.setOnEnrolledListener {
      Toast.makeText(activity, "You're enrolled", Toast.LENGTH_SHORT).show()
    }
  }

  fun onActivityResult(requestCode: Int, resultCode: Int) {
    if (requestCode == 1002) {
      if (resultCode == -1) {
        channel.invokeMethod("capture_state", mapOf("success" to true))
      }
    }
  }
}

object StringUtils {
  private const val SPECIAL_CHAR_MATCHER = "^[a-zA-Z0-9_]*$"
  fun hasSpecialChars(search: String?): Boolean {
    val pattern = Pattern.compile(SPECIAL_CHAR_MATCHER, Pattern.CASE_INSENSITIVE)
    if (search == null) return false
    return !TextUtils.isEmpty(search) && pattern.matcher(search).find()
  }
}
