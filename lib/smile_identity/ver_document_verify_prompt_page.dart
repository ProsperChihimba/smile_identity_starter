import 'dart:async';

import 'package:flutter/foundation.dart';
import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../extensions/stateful_widget_extension.dart';
import '../permissions/permissions_handler.dart';
import 'models/doc_type.dart';
import 'models/smile_data.dart';
import 'view_models/smile_manager.dart';

class VerDocumentVerifyPage extends ConsumerStatefulWidget {
  const VerDocumentVerifyPage({super.key});

  @override
  ConsumerState<ConsumerStatefulWidget> createState() =>
      _VerDocumentVerifyPageState();
}

class _VerDocumentVerifyPageState extends ConsumerState<VerDocumentVerifyPage>
    with AfterLayoutMixin {
  void removeSmileData() {
    ref.read(smileManagerProvider.notifier).removeSmileDataCache();
  }

  @override
  FutureOr<void> afterFirstLayout(BuildContext context) async {
    removeSmileData();
  }

  @override
  Widget build(BuildContext context) {
    ref.listen(smileManagerProvider, (_, next) {
      print("Submitted: ${next.submitted}");
      if (next.submitted) {
        debugPrint("Submittedddddddddddddddddddddddddddddddddddddddddddddd");
        // Navigator.pop(context);
      }
    });

    final captured = ref.watch(smileManagerProvider).captured;
    print("Submitteddd: $captured");

    return WillPopScope(
      onWillPop: () async {
        removeSmileData();
        return true;
      },
      child: Scaffold(
        body: Container(
          constraints: const BoxConstraints.expand(),
          padding: const EdgeInsets.symmetric(horizontal: 15).copyWith(
            top: 20,
          ),
          child: captured
              ? Column(
                  mainAxisAlignment: MainAxisAlignment.center,
                  children: [
                    const Text("We've got your data. Submit to proceed."),
                    const SizedBox(height: 20),
                    TextButton(
                      onPressed: submit,
                      child: const Text("Submit"),
                    )
                  ],
                )
              : Column(
                  mainAxisAlignment: MainAxisAlignment.center,
                  children: [
                    TextButton(
                      onPressed: capture,
                      child: const Text("Start"),
                    )
                  ],
                ),
        ),
      ),
    );
  }

  void capture() async {
    final platform = defaultTargetPlatform;
    if (platform == TargetPlatform.android) {
      final permissionsHandler = ref.read(permissionsHandlerProvider);
      final granted = await permissionsHandler.checkCameraPermission();
      if (!granted) return;
    }

    final smileManager = ref.read(smileManagerProvider.notifier);
    final data = SmileData(
      firstName: "John",
      lastName: "Doe",
      country: "KE",
      idNumber: "00000000",
      idType: VerDocumentType.nida.smileIdentityLabel,
      userId: "user-1",
    );

    await smileManager.capture(data);
  }

  void submit() async {
    final smileManager = ref.read(smileManagerProvider.notifier);
    await smileManager.submitJob();
  }
}

// https://docs.smileidentity.com/supported-id-types/for-individuals-kyc/backed-by-id-authority
// https://docs.smileidentity.com/supported-id-types/for-businesses-kyb/supported-countries
// https://portal.smileidentity.com/sdk