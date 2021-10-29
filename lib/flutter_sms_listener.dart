import 'dart:async';

import 'package:flutter/services.dart';

class FlutterSmsListener {
  static const MethodChannel _channel =
      const MethodChannel('flutter_sms_listener');

  static Future<String?> startListening() async {
    final String? smsCode = await _channel.invokeMethod('startListening');
    return smsCode;
  }

  static Future<String?> stopListening() async {
    final String? smsCode = await _channel.invokeMethod('stopListening');
    return smsCode;
  }

  static Future<String?> getAppSignature() async {
    final String? smsCode = await _channel.invokeMethod('getAppSignature');
    return smsCode;
  }
}
