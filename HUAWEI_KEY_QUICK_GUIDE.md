# 🔐 БЫСТРАЯ ИНСТРУКЦИЯ: Генерация ключа для Huawei AppGallery

## ✅ ХОРОШИЕ НОВОСТИ
**Пароль keystore найден и работает!**
- Пароль: `kjidsfQ23Awdwdq`
- Keystore: `keystore/keystore-release.keystore`
- Alias: `androidreleasekey`

---

## 📝 ПОШАГОВАЯ ИНСТРУКЦИЯ

### Шаг 1: Откройте Терминал

### Шаг 2: Скопируйте и выполните команды

#### 🔵 ВАРИАНТ A: По шагам (рекомендуется)

```bash
# 1. Перейдите в папку проекта
cd /Users/artursitikov/AndroidStudioProjects/elta-android

# 2. Скачайте pepk.jar
curl -o pepk.jar https://www.gstatic.com/play-apps-publisher-rapid/signing-tool/prod/pepk.jar

# 3. Проверьте загрузку
ls -lh pepk.jar

# 4. Сгенерируйте зашифрованный ключ (копируйте как одну строку)
java -jar pepk.jar --keystore=keystore/keystore-release.keystore --alias=androidreleasekey --output=huawei-sign-key.zip --encryptionkey=034200041E224EE22B45D19B23DB91BA9F52DE0A06513E03A5821409B34976FDEED6E0A47DBA48CC249DD93734A6C5D9A0F43461F9E140F278A5D2860846C2CF5D2C3C02 --include-cert --keystore-pass=kjidsfQ23Awdwdq --key-pass=kjidsfQ23Awdwdq

# 5. Проверьте результат
ls -lh huawei-sign-key.zip
```

#### 🟢 ВАРИАНТ B: Одна команда (копируйте ПОЛНОСТЬЮ)

```bash
cd /Users/artursitikov/AndroidStudioProjects/elta-android && curl -o pepk.jar https://www.gstatic.com/play-apps-publisher-rapid/signing-tool/prod/pepk.jar && java -jar pepk.jar --keystore=keystore/keystore-release.keystore --alias=androidreleasekey --output=huawei-sign-key.zip --encryptionkey=034200041E224EE22B45D19B23DB91BA9F52DE0A06513E03A5821409B34976FDEED6E0A47DBA48CC249DD93734A6C5D9A0F43461F9E140F278A5D2860846C2CF5D2C3C02 --include-cert --keystore-pass=kjidsfQ23Awdwdq --key-pass=kjidsfQ23Awdwdq && echo "✅ ГОТОВО! Файл создан:" && ls -lh huawei-sign-key.zip
```

### Шаг 3: Найдите файл `huawei-sign-key.zip`

Файл будет находиться в: `/Users/artursitikov/AndroidStudioProjects/elta-android/huawei-sign-key.zip`

---

## 📤 ЧТО ДЕЛАТЬ ДАЛЬШЕ

### 1. Загрузите ключ в Huawei AppGallery Connect

🔗 https://developer.huawei.com/consumer/ru/service/josp/agc/index.html

**Путь:**
1. My apps → Ваше приложение (App ID: 110578925)
2. App services → App signing
3. Выберите: "Способ 2: AppGallery Connect только управляет ключом подписи"
4. Upload signing key → выберите файл `huawei-sign-key.zip`

### 2. Соберите Huawei AAB

```bash
cd /Users/artursitikov/AndroidStudioProjects/elta-android
./gradlew bundleHuawei
```

Файл будет: `app/build/outputs/bundle/huawei/app-huawei.aab`

### 3. Загрузите AAB в AppGallery Connect

Version information → Upload → выберите `app-huawei.aab`

---

## ❗ ЕСЛИ НЕ ПОЛУЧАЕТСЯ

### Альтернативный способ (через GUI):

1. Откройте **Android Studio**
2. Build → Generate Signed Bundle / APK
3. Выберите Android App Bundle
4. Keystore path: `keystore/keystore-release.keystore`
5. Password: `kjidsfQ23Awdwdq`
6. Key alias: `androidreleasekey`
7. Key password: `kjidsfQ23Awdwdq`
8. Build Variant: `huawei`
9. Нажмите Finish

Затем используйте этот keystore для генерации ключа через pepk.jar вручную.

---

## 🔑 СОХРАНИТЕ ЭТУ ИНФОРМАЦИЮ

**Release Keystore:**
- Файл: `/Users/artursitikov/AndroidStudioProjects/elta-android/keystore/keystore-release.keystore`
- Store Password: `kjidsfQ23Awdwdq`
- Key Alias: `androidreleasekey`
- Key Password: `kjidsfQ23Awdwdq`

**⚠️ ВАЖНО:** Сохраните эти данные в безопасном месте! Без них невозможно будет обновлять приложение.

---

## 📞 Huawei Support

Если возникнут проблемы с загрузкой:
- https://developer.huawei.com/consumer/en/support/

---

**Дата:** 13 февраля 2026  
**Версия приложения:** 2.10.7 (build 402)


