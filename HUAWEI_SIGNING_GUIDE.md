# 🔐 Подготовка ключа подписи для Huawei AppGallery Connect

## 📋 Требования Huawei для загрузки AAB

Huawei AppGallery Connect требует выгрузить зашифрованный ключ подписи перед загрузкой AAB файла.

---

## 🚀 Быстрый старт

### Способ 1: Автоматическая генерация (рекомендуется)

Используйте готовый скрипт:

```bash
cd /Users/artursitikov/AndroidStudioProjects/elta-android
./generate_huawei_key.sh
```

**Скрипт автоматически:**
- ✅ Скачает `pepk.jar` (если нужно)
- ✅ Сгенерирует зашифрованный ключ `huawei-sign-key.zip`
- ✅ Использует ваш текущий release keystore

**Вам нужно только ввести пароль keystore:** `kjidsfQ23Awdwdq`

---

### Способ 2: Ручная генерация

#### Шаг 1: Скачайте pepk.jar

```bash
curl -o pepk.jar https://www.gstatic.com/play-apps-publisher-rapid/signing-tool/prod/pepk.jar
```

#### Шаг 2: Сгенерируйте зашифрованный ключ

```bash
java -jar pepk.jar \
    --keystore=keystore/keystore-release.keystore \
    --alias=androidreleasekey \
    --output=huawei-sign-key.zip \
    --encryptionkey=034200041E224EE22B45D19B23DB91BA9F52DE0A06513E03A5821409B34976FDEED6E0A47DBA48CC249DD93734A6C5D9A0F43461F9E140F278A5D2860846C2CF5D2C3C02 \
    --include-cert
```

**Параметры:**
- `--keystore`: путь к вашему release keystore
- `--alias`: `androidreleasekey` (из credentials-release.properties)
- `--output`: имя выходного ZIP файла
- `--encryptionkey`: **фиксированный публичный ключ Huawei** (не изменяйте!)
- `--include-cert`: включить сертификат в пакет

#### Шаг 3: Введите пароль

Когда появится запрос, введите пароль keystore: `kjidsfQ23Awdwdq`

---

## 📤 Загрузка ключа в Huawei AppGallery Connect

### 1. Откройте AppGallery Connect

🔗 https://developer.huawei.com/consumer/en/service/josp/agc/index.html

### 2. Перейдите к настройкам подписи

1. Выберите **My apps**
2. Найдите ваше приложение **(App ID: 110578925)**
3. Перейдите: **App services → App signing**

### 3. Выберите способ управления ключом

Выберите: **"Способ 2: AppGallery Connect только управляет ключом подписи, который вы выгружаете"**

### 4. Выгрузите ключ подписи

1. Нажмите **"Upload signing key"**
2. Выберите файл: `huawei-sign-key.zip`
3. Подтвердите загрузку

### 5. (Опционально) Выгрузите ключ выгрузки

Если у вас отдельный ключ для выгрузки (upload key), загрузите его тоже.
Если нет - ключ подписи будет использоваться и для выгрузки.

---

## 📦 Сборка и загрузка AAB

После загрузки ключа подписи:

### 1. Соберите Huawei AAB

```bash
./gradlew bundleHuawei
```

Файл будет создан: `app/build/outputs/bundle/huawei/app-huawei.aab`

### 2. Загрузите AAB в AppGallery Connect

1. В AppGallery Connect откройте: **Version information**
2. Нажмите **Upload**
3. Выберите файл: `app-huawei.aab`
4. AppGallery Connect автоматически подпишет AAB вашим ключом

---

## 🔑 Информация о ключах

### Release Keystore (используется для Huawei и Google Play)

- **Файл:** `keystore/keystore-release.keystore`
- **Alias:** `androidreleasekey`
- **Пароль keystore:** `kjidsfQ23Awdwdq`
- **Пароль ключа:** `kjidsfQ23Awdwdq`

### Публичный ключ шифрования Huawei

```
034200041E224EE22B45D19B23DB91BA9F52DE0A06513E03A5821409B34976FDEED6E0A47DBA48CC249DD93734A6C5D9A0F43461F9E140F278A5D2860846C2CF5D2C3C02
```

**⚠️ Это фиксированный ключ от Huawei - НЕ изменяйте его!**

---

## ❓ Частые вопросы

### Q: Нужно ли создавать отдельный keystore для Huawei?

**A:** Нет! Используйте тот же release keystore, что и для Google Play. Один ключ для обоих магазинов - это стандартная практика.

### Q: Что такое pepk.jar?

**A:** Play Encrypt Private Key - официальный инструмент Google/Huawei для шифрования приватных ключей перед загрузкой в магазины приложений.

### Q: Безопасно ли загружать ключ в Huawei?

**A:** Да, ключ шифруется публичным ключом Huawei перед загрузкой. Только Huawei может его расшифровать.

### Q: Что делать, если забыл пароль keystore?

**A:** К сожалению, пароль keystore невозможно восстановить. Нужно будет создать новый keystore, но это означает новое приложение (новый package name или обновление через support).

### Q: Можно ли изменить ключ подписи после загрузки?

**A:** Нет, ключ подписи нельзя изменить после первой загрузки приложения. Поэтому важно сохранить keystore в безопасном месте.

---

## 📞 Поддержка

- **Huawei Developer Support:** https://developer.huawei.com/consumer/en/support/
- **AppGallery Connect Documentation:** https://developer.huawei.com/consumer/en/doc/development/AppGallery-connect-Guides/agc-appsigning-introduction-0000001146598793

---

## ✅ Чеклист перед загрузкой

- [ ] Сгенерирован `huawei-sign-key.zip`
- [ ] Ключ загружен в AppGallery Connect (App signing)
- [ ] Собран `app-huawei.aab` (версия 2.10.7, build 402)
- [ ] Проверена версия приложения (versionCode 402)
- [ ] Настроен OneSignal с Huawei Configuration
- [ ] Протестировано на реальном Huawei устройстве

---

**Дата создания:** 13.02.2026  
**Версия приложения:** 2.10.7 (build 402)  
**App ID Huawei:** 110578925

