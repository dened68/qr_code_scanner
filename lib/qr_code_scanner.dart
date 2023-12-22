import 'package:flutter/services.dart';

class QrCodeScannerPlugin {
  static const MethodChannel _channel = const MethodChannel('qr_code_scanner_plugin');

  static Future<String?> scan() async {
    try {
      final String? result = await _channel.invokeMethod('scan');
      return result;
    } catch (e) {
      print(e);
      return null;
    }
  }
}