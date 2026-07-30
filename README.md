# IFIX Mobile VPN

کلاینت اندروید دمو با UI فارسی (Compose).

## ورود دمو

| نقش | کاربر | رمز |
|-----|--------|------|
| ادمین | `admin` | `admin` |
| کاربر | `taher` | `123456` |

با تیک **«مرا به خاطر بسپار»** نشست در SharedPreferences ذخیره می‌شود و بعد از بستن اپ دوباره لاگین لازم نیست.

## ساخت APK

GitHub Actions روی `main`:

```text
Actions → IFIX Mobile VPN – Build APK → Artifact: IFIXMOBILE_VPN_Debug_APK
```

یا محلی:

```bash
gradle :app:assembleDebug
```

## توجه

اتصال VPN در این نسخه **شبیه‌سازی UI** است (Mock). برای تونل واقعی از ریپوی `IFIXVPN-MOLLAEI` با هسته Xray استفاده کنید.
