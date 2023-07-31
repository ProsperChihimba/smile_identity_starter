import 'package:flutter/services.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:uuid/uuid.dart';

import '../models/smile_data.dart';
import '../models/smile_state.dart';

final smileManagerProvider =
    StateNotifierProvider<SmileManager, SmileState>((ref) => SmileManager());

class SmileManager extends StateNotifier<SmileState> {
  static const _channel = MethodChannel("smile_identity");

  SmileManager() : super(const SmileState()) {
    _init();
  }

  void _init() {
    _channel.setMethodCallHandler((call) async {
      final method = call.method;
      if (method == "capture_state") {
        state = state.copyWith(captured: call.arguments["success"]);
      }
      if (method == "submit_state") {
        state = state.copyWith(submitted: call.arguments["success"]);
      }
    });
  }

  String get jobId => const Uuid().v1();
  String get tag => (const Uuid().v1()).replaceAll("-", "");

  Future<void> capture(SmileData data) async {
    final smileData = data.copyWith(jobId: jobId, tag: tag);
    state = state.copyWith(data: smileData);
    await _channel.invokeMethod("capture", state.data!.toMap());
  }

  Future<void> submitJob() async {
    await _channel.invokeMethod("submit", state.data!.toMap());
  }

  void removeSmileDataCache() {
    state = const SmileState();
  }
}
