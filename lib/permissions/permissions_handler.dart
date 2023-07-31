import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:permission_handler/permission_handler.dart';

final permissionsHandlerProvider = Provider((_) => PermissiosHandler());

class PermissiosHandler {
  Future<bool> checkCameraPermission() async {
    const permission = Permission.camera;
    final granted = await permission.granted;
    if (granted) return true;

    final status = await permission.request();
    return status.granted;
  }
}

extension _PermissionExt on Permission {
  Future<bool> get granted async {
    final permissionStatus = await status;
    return permissionStatus.isGranted || permissionStatus.isLimited;
  }
}

extension _PermissionStatusExt on PermissionStatus {
  bool get granted {
    return isGranted || isLimited;
  }
}
