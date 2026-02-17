#!/bin/bash

# Скрипт для генерации зашифрованного ключа подписи для Huawei AppGallery Connect
# Дата создания: 13.02.2026

echo "=========================================="
echo "Генерация ключа подписи для Huawei AppGallery"
echo "=========================================="

# Параметры из credentials-release.properties
KEYSTORE_FILE="keystore/keystore-release.keystore"
KEY_ALIAS="androidreleasekey"
OUTPUT_FILE="huawei-sign-key.zip"

# Фиксированный публичный ключ Huawei для шифрования
ENCRYPTION_KEY="034200041E224EE22B45D19B23DB91BA9F52DE0A06513E03A5821409B34976FDEED6E0A47DBA48CC249DD93734A6C5D9A0F43461F9E140F278A5D2860846C2CF5D2C3C02"

echo ""
echo "Параметры:"
echo "  Keystore: $KEYSTORE_FILE"
echo "  Alias: $KEY_ALIAS"
echo "  Output: $OUTPUT_FILE"
echo ""

# Проверка наличия файла keystore
if [ ! -f "$KEYSTORE_FILE" ]; then
    echo "❌ ОШИБКА: Файл keystore не найден: $KEYSTORE_FILE"
    exit 1
fi

# Проверка наличия pepk.jar
if [ ! -f "pepk.jar" ]; then
    echo "⚠️  Файл pepk.jar не найден. Скачиваю..."
    echo ""

    # Скачивание pepk.jar из официального источника Google
    curl -o pepk.jar https://www.gstatic.com/play-apps-publisher-rapid/signing-tool/prod/pepk.jar

    if [ $? -ne 0 ]; then
        echo "❌ ОШИБКА: Не удалось скачать pepk.jar"
        echo "Скачайте вручную с https://www.gstatic.com/play-apps-publisher-rapid/signing-tool/prod/pepk.jar"
        exit 1
    fi

    echo "✅ pepk.jar успешно скачан"
    echo ""
fi

# Проверка Java
if ! command -v java &> /dev/null; then
    echo "❌ ОШИБКА: Java не найдена. Установите JDK."
    exit 1
fi

JAVA_VERSION=$(java -version 2>&1 | head -n 1)
echo "Java версия: $JAVA_VERSION"
echo ""

# Генерация зашифрованного ключа
echo "🔐 Генерация зашифрованного ключа подписи..."
echo "⚠️  Вам будет предложено ввести пароль keystore"
echo ""

java -jar pepk.jar \
    --keystore="$KEYSTORE_FILE" \
    --alias="$KEY_ALIAS" \
    --output="$OUTPUT_FILE" \
    --encryptionkey="$ENCRYPTION_KEY" \
    --include-cert

if [ $? -eq 0 ]; then
    echo ""
    echo "=========================================="
    echo "✅ УСПЕШНО!"
    echo "=========================================="
    echo ""
    echo "Файл создан: $OUTPUT_FILE"
    echo ""
    echo "📤 Следующие шаги:"
    echo "1. Зайдите в AppGallery Connect: https://developer.huawei.com/consumer/en/service/josp/agc/index.html"
    echo "2. Перейдите в ваше приложение (App ID: 110578925)"
    echo "3. Откройте: My apps → [Ваше приложение] → App services → App signing"
    echo "4. Выберите: 'Manage signing keys manually'"
    echo "5. Выгрузите файл: $OUTPUT_FILE"
    echo ""
    echo "🔑 Информация о ключе:"
    echo "  Alias: $KEY_ALIAS"
    echo "  Keystore: $KEYSTORE_FILE"
    echo ""
else
    echo ""
    echo "❌ ОШИБКА при генерации ключа"
    echo "Проверьте правильность пароля keystore"
    exit 1
fi


