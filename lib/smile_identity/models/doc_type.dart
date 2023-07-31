enum VerDocumentType {
  driverLicense(
    jsonKey: "Driver Licence",
    label: "id-type-driver",
    smileIdentityLabel: "DRIVERS_LICENSE",
  ),
  nida(
    jsonKey: "NIDA",
    label: "id-type-nida",
    smileIdentityLabel: "NATIONAL_ID",
  ),
  passport(
    jsonKey: "Passport",
    label: "id-type-passport",
    smileIdentityLabel: "PASSPORT",
  ),
  ;

  final String jsonKey, label, smileIdentityLabel;
  const VerDocumentType({
    required this.jsonKey,
    required this.label,
    required this.smileIdentityLabel,
  });

  static VerDocumentType fromJson(String type) {
    const values = VerDocumentType.values;
    return values.where((e) => e.jsonKey == type).single;
  }
}
