# 🚀 Версионирование и релиз - Инструкция

## ✅ Что уже исправлено в коде:

- Обновлен regex для поддержки `huawei` buildType  
- Обновлен fallback до актуальной версии (2.10.5.395)  
- Добавлено автоматическое чтение версии из `version.properties`  
- Синхронизирована версия в `version.properties`

**Теперь система работает так:**
1. Если есть Git тег на коммите → берется версия из тега
2. Если тега нет → берется версия из `version.properties`
3. Если файла нет → используется hardcoded fallback (2.10.5.395)

---

## 📋 Как делать релиз (простой способ):

### Шаг 1: Обновить версию в файле

Отредактируйте `version.properties`:
```properties
build=396         # Увеличьте на 1
major=2          # Только при breaking changes
minor=10         # Только при новых функциях  
patch=5          # Только при bug fixes
develop=0        # Не используется, можете игнорировать
```

**Правило:** Для обычного релиза достаточно увеличить `build` на 1.

### Шаг 2: Закоммитить

```bash
git add version.properties
git commit -m "Bump version to 2.10.5 build 396"
git push
```

### Шаг 3: Создать Git тег

```bash
# Для Google Play релиза:
git tag -a v2.10.5.396-release -m "Release 2.10.5 build 396"
git push origin v2.10.5.396-release

# Для Huawei релиза (если нужен):
git tag -a v2.10.5.397-huawei -m "Huawei 2.10.5 build 397"
git push origin v2.10.5.397-huawei
```

**Формат тега:** `v{major}.{minor}.{patch}.{build}-{type}`
- `type` может быть: `release`, `debug`, или `huawei`

### Шаг 4: Собрать APK

```bash
# Для Google Play:
./gradlew assembleRelease

# Для Huawei:
./gradlew assembleHuawei

# Для Debug (тег не обязателен):
./gradlew assembleDebug
```

---

## 🎯 Быстрая шпаргалка:

### Обычный релиз (bug fixes, маленькие изменения):
```bash
# 1. Отредактировать version.properties (build=396)
# 2. Закоммитить
git add version.properties && git commit -m "Bump to 396" && git push

# 3. Создать тег
git tag -a v2.10.5.396-release -m "Release 396" && git push origin v2.10.5.396-release

# 4. Собрать
./gradlew assembleRelease
```

### Релиз с новыми функциями:
```bash
# 1. Отредактировать version.properties (minor=11, patch=0, build=396)
# 2-4. То же самое, но тег будет v2.11.0.396-release
```

### Huawei релиз:
```bash
# То же самое, но:
# - Тег с суффиксом -huawei
# - Сборка: ./gradlew assembleHuawei
```

---

## 📝 Правила инкремента версии:

| Ситуация | Что изменить | Пример |
|----------|-------------|---------|
| 🐛 Bug fixes | `patch` + `build` | 2.10.3 → 2.10.**4** |
| ✨ Новые функции | `minor` + `build`, обнулить `patch` | 2.10.3 → 2.**11.0** |
| 💥 Breaking changes | `major` + `build`, обнулить остальное | 2.10.3 → **3.0.0** |
| 🔨 Обычная сборка | только `build` | build 392 → 393 |

---

## ❓ Частые вопросы:

### Q: Обязательно ли создавать Git тег?
**A:** Нет, но **настоятельно рекомендуется** для release и huawei сборок. Для debug сборок тег не нужен - версия возьмется из `version.properties`.

### Q: Что если забыл создать тег?
**A:** Ничего страшного - сборка возьмет версию из `version.properties`. Но лучше создать тег задним числом:
```bash
git tag -a v2.10.5.396-release HEAD -m "Release 396"
git push origin v2.10.5.396-release
```

### Q: Как проверить, какая версия в собранном APK?
**A:** Посмотрите файл `app/release/output-metadata.json` (или `app/huawei/output-metadata.json`) - там будет `versionName` и `versionCode`.

### Q: Можно ли НЕ использовать Git теги вообще?
**A:** Да, можно работать только через `version.properties`. Но теги дают важную возможность привязать версию к конкретному коммиту (для rollback, аудита и т.д.).

### Q: Что означает параметр `develop` в version.properties?
**A:** Сейчас он не используется, можете игнорировать или удалить эту строку.

---

## 🛠️ Сборка APK для разных платформ:

```bash
# Google Play:
./gradlew assembleRelease
# → app/build/outputs/apk/release/

# Huawei AppGallery:
./gradlew assembleHuawei  
# → app/build/outputs/apk/huawei/

# Debug (для тестирования):
./gradlew assembleDebug
# → app/build/outputs/apk/debug/
```

---

## ⚠️ Первый раз после исправлений:

Нужно создать хотя бы один тег для текущей версии:

```bash
# Для текущей release версии:
git tag -a v2.10.5.395-release -m "Current release 2.10.5"
git push origin v2.10.5.395-release
```

После этого всё будет работать автоматически! 🎉

---

## 💡 Полезные команды Git:

```bash
# Посмотреть все теги:
git tag -l

# Удалить тег (если ошиблись):
git tag -d v2.10.5.396-release                    # локально
git push origin :refs/tags/v2.10.5.396-release    # на remote

# Посмотреть детали тега:
git show v2.10.5.396-release

# Создать тег на старом коммите:
git tag -a v2.10.5.390-release <commit-hash> -m "Release 390"
```

---

**Всё!** Система простая и работает автоматически. Просто обновляйте `version.properties`, создавайте теги и собирайте APK. 🚀

