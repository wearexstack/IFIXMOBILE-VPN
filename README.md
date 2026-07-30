# IFIX Mobile VPN

کلاینت اندروید فارسی با **تونل واقعی Xray** (libv2ray).

## ورود دمو

| نقش | کاربر | رمز |
|-----|--------|------|
| ادمین | `admin` | `admin` |
| کاربر | `taher` | `123456` |

با تیک **«مرا به خاطر بسپار»** نشست ذخیره می‌شود.

## تونل Xray

1. سرورها از ساب لود می‌شوند:  
   `https://raw.githubusercontent.com/wearexstack/xstack/main/sub`
2. اتصال از طریق `IfixVpnService` + `XrayEngine` + `StartLoop(config, tunFd)`
3. **الزامی:** فایل `libv2ray.aar` را در `app/libs/` بگذارید (راهنما در همان پوشه).

بدون AAR اپ بیلد می‌شود ولی پیام خطا می‌دهد که هسته موجود نیست.

## ساخت APK

```text
Actions → IFIX Mobile VPN – Build APK → Artifact
```

یا:

```bash
# بعد از قرار دادن libv2ray.aar
gradle :app:assembleDebug
```
