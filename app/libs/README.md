# libv2ray.aar (الزامی برای تونل واقعی)

بدون این فایل اپ بیلد می‌شود ولی **پروکسی واقعی کار نمی‌کند**.

```bash
git clone https://github.com/2dust/AndroidLibXrayLite.git
cd AndroidLibXrayLite
gomobile init
go mod tidy -v
gomobile bind -v -androidapi 24 -trimpath -ldflags='-s -w -buildid= -checklinkname=0' ./
cp libv2ray.aar /path/to/IFIXMOBILE-VPN/app/libs/
```

سپس `gradle :app:assembleDebug`
