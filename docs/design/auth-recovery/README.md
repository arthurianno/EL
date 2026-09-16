# 1. PROJECT / SECTION OVERVIEW

Источник: https://www.figma.com/design/WArRV2xcji2Ns1kWwtFQo8/?node-id=788-4818

**ЧАСТИЧНАЯ СПЕЦИФИКАЦИЯ. Полный анализ не завершён.** Figma MCP сообщил о достижении лимита Starter-плана до получения детальных свойств instances, styles, variables и reactions. В браузере после ошибки загрузки модуля редактора и повторной загрузки открылся гостевой просмотр; интерфейс сообщает «Sign up to comment, edit, inspect and more». Детальная инспекция без входа недоступна. Эти ограничения нельзя интерпретировать как отсутствие свойств в дизайне.

- CONFIRMED: страница `Готовый дизайн `, ID `96:7158`.
- CONFIRMED: секция `Авторизация + Восстановление пароля`, ID `788:4818`, 4507 × 8264 px, положение на странице (20239, −5460).
- CONFIRMED: 16 мобильных Frame размером 375 × 812 px; 3 Note; Frame `Картинки`; 16 CONNECTOR.
- CONFIRMED: полный структурный подсчёт — 1799 потомков секции. Sparse metadata раскрывает 285 потомков и саму секцию, всего 286 строк, включая 123 instance. Эти 123 не являются полным подсчётом всех вложенных instances.
- INFERRED: три функциональных экрана — вход, запрос восстановления, новый пароль. Остальные мобильные Frame представляют состояния этих экранов.

Обозначения: CONFIRMED — явно получено из Figma; INFERRED — интерпретация или предложение реализации; UNKNOWN — не удалось определить; MISSING — подтверждённо отсутствующая необходимая информация. Ограничение доступа обозначается UNKNOWN, а не MISSING.

Точные данные: [исходная выгрузка](figma-metadata.xml), [все раскрытые узлы](geometry.md), [развёртка каждого экрана](screen-layouts.md), [CSV размеров](node-dimensions.csv). Имена слоёв сохранены буквально, включая опечатки.

# 2. SCREEN LIST

Все мобильные Frame: **375 × 812 px**. Группировка и назначение — INFERRED; имена/ID/геометрия — CONFIRMED. IME означает наличие instance `Keyboard`, а не подтверждённую runtime-логику.

| Frame | ID | Функциональная группа / состояние | IME | Кнопка по имени instance |
|---|---|---|---|---|
| 1.1 | 788:5009 | Вход, исходное | Нет | No activ Button |
| 1.1.1 | 788:4910 | Вход, редактирование первого поля | Да | No activ Button |
| 1.1.1 Ошибка поля | 788:4979 | Вход, ошибка первого поля | Да | No activ Button |
| 1.1.1 Верный ввод | 788:4994 | Вход, первое поле заполнено | Да | No activ Button |
| 1.1.2 | 788:4925 | Вход, редактирование второго поля | Да | No activ Button |
| 1.1.2 Ошибка поля | 788:4949 | Вход, ошибка второго поля | Да | No activ Button |
| 1.1.2 Верный ввод | 788:4964 | Вход, готовность отправки | Да | Activ Button |
| 1.1 | 788:5028 | Вход, готовность отправки без IME | Нет | Activ Button |
| 1.2 | 788:4821 | Запрос восстановления, исходное | Нет | No activ Button |
| 1.2.1 | 788:4835 | Запрос восстановления, красная линия поля; причина ошибки UNKNOWN | Нет | No activ Button |
| 1.2.2 | 788:4849 | Запрос восстановления, готовность отправки | Нет | Activ Button |
| 1.1.2 | 788:4940 | Запрос восстановления, редактирование; имя дублируется | Да | Внутри Форма, UNKNOWN |
| 1.2.3 | 788:4863 | Новый пароль, исходное | Нет | No activ Button |
| 1.2.4 | 788:4875 | Новый пароль, редактирование | Да | No activ Button |
| 1.2.5 | 788:4886 | Новый пароль, готовность отправки | Да | Activ Button |
| 1.2.6 | 788:4897 | Новый пароль, затемнение и круговой индикатор | Да | Activ Button под overlay |

Вспомогательные Frame: Note `788:4819` 257 × 132; Note `788:5050` 251 × 90; Note `788:5052` 377 × 90; Картинки `792:4553` 1547 × 1177 px. Они не являются маршрутами приложения.

# 3. NAVIGATION GRAPH

Прототипные `reactions` не прочитаны. **CONFIRMED prototype-переходов: не установлено; это не означает ноль связей.** Canvas CONNECTOR и их подписи не равны Prototype Connections.

```text
Login
 ├─ редактирование / ошибки / заполнение → состояния Login [INFERRED]
 ├─ tap «Забыли пароль?» → RecoveryRequest [INFERRED, control CONFIRMED визуально]
 └─ отправка данных → назначение UNKNOWN

RecoveryRequest
 ├─ редактирование / заполнение → состояния RecoveryRequest [INFERRED]
 └─ отправка → инструкция на email [назначение подтверждено текстом]
               → «Переход на почту» [заметка CONFIRMED, trigger UNKNOWN]
               → «Переход по ссылке на почте» [заметка CONFIRMED]
                  ├─ NewPassword [INFERRED]
                  └─ «Нет МП» → сайт создания пароля [INFERRED ветвление]

NewPassword
 └─ ввод → готовность отправки → overlay/loading [INFERRED]
     └─ успешный результат / ошибка / возврат → UNKNOWN
```

INFERRED Compose routes: `login`, `recovery_request`, `new_password`. Не делать route для каждого Frame-state. URL восстановления, deep-link arguments, popUpTo и стартовый экран UNKNOWN. Внешний сайт и почтовый клиент не делать внутренними destinations без уточнения требований. Back behavior UNKNOWN.

# 4. SCREEN-BY-SCREEN SPECIFICATION

Все числа ниже CONFIRMED из узлов. Формат `(x, y; width × height)` в px, координаты относительно экранного Frame. Предлагаемый dp имеет то же числовое значение только для базового viewport 375 dp. Это **INFERRED соглашение**, не универсальное физическое равенство px и dp. Размеры системных областей брать из Android insets.

Общее сверху: `Status Bar` (0,0;375×44), `top-app-bar` (0,44;375×44). Заголовочные instances: x=27,y=100,w=313; Войти h=76, Востановление пароля h=105, Новый пароль h=77. Симметрия не точная: справа от заголовка 35 px, слева 27 px. Точные свойства текста этих instances UNKNOWN. На базовом Login текст проверен визуально; см. ниже.

## Login: 8 Frame

CONFIRMED визуальным просмотром `788:5009`: заголовок «Войти»; описание «Чтобы продолжить, введите ваш email и пароль от аккаунта»; поля «Ваш email» и «Пароль»; supporting text «Не менее 8 символов (A-Z, a-z, 0-9)»; CTA «Войти»; справа в top bar «Забыли пароль?»; слева стрелка назад; справа у пароля значок глаза; поля имеют нижние линии. Это подтверждение содержимого и наличия controls, а не их font/color/stroke параметров. Текст-подсказка не определяет полный validation regex и не доказывает обязательность каждого класса символов.

Порядок чтения INFERRED: системная верхняя область → top bar → заголовочный блок → иллюстрация → два поля → кнопка → системная нижняя область / IME. Stacking следует порядку XML и может отличаться.

| ID | Иллюстрация x,y,w,h | Блок формы x,y,w,h | Первое поле h | Второе поле h | Кнопка x,y,w,h |
|---|---|---|---|---|---|
| 788:5009 | 0,185,375,360 | 0,554,375,204 | 56 | 72 | 32,706,311,52 |
| 788:5028 | 0,185,375,360 | 0,554,375,204 | 56 | 72 | 32,706,311,52 |
| 788:4910 | 110,174,155,149 | 0,307,375,204 | 56 | 72 | 32,459,311,52 |
| 788:4925 | 110,174,155,149 | 0,307,375,204 | 56 | 72 | 32,459,311,52 |
| 788:4949 | 110,174,155,149 | 0,307,375,204.00003051757812 | 56 | 72.00003051757812 | 32,459.00003051757812,311,52 |
| 788:4964 | 110,174,155,149 | 0,307,375,204 | 56 | 72 | 32,459,311,52 |
| 788:4979 | 110,174,155,149 | 0,291,375,220.00003051757812 | 72.00003051757812 | 72 | 32,459.00003051757812,311,52 |
| 788:4994 | 110,174,155,149 | 0,315,375,196.00003051757812 | 48.000030517578125 | 72 | 32,459.00003051757812,311,52 |

Поле 2 начинается сразу после bounds поля 1; пространство между объединённым Input и кнопкой — 24 px. Внутренние padding полей UNKNOWN. В состояниях без IME расстояние от низа CTA y=758 до `System navigation` y=778 — 20 px. IME: (0,521;375×291), зазор после CTA около 10 px. `System navigation` (0,778;375×34) также присутствует под областью IME в ряде макетов.

Важно: bounds иллюстрации и формы пересекаются при IME: y=174…323 против y=307 или 291/315. Визуальный overlap непрозрачных пикселей UNKNOWN, поскольку прозрачность изображения не прочитана. Не превращать такое пересечение автоматически в отрицательный Spacer.

У `788:5009` и `788:5028` есть скрытые `40 / social`: по 3 экземпляра на экран; один скрыт непосредственно, ещё два — через скрытого родителя. Не включать социальный вход в видимый UI и бизнес-логику на основании этих слоёв.

## RecoveryRequest: 4 Frame

CONFIRMED визуальным просмотром `788:4835`: заголовок «Восстановление пароля»; описание «Введите email, указанный при регистрации аккаунта в нашем приложении»; поле email; CTA «Отправить запрос на email»; close icon × слева в top bar; красная нижняя линия поля в этом состоянии. Причина error, текст ошибки и полный validation contract UNKNOWN.

Порядок чтения INFERRED: top bar → заголовок → иллюстрация → поле → информационная строка → CTA → нижняя системная область / IME.

| ID | Иллюстрация | Форма / нижний блок | Info x,y,w,h | CTA x,y,w,h |
|---|---|---|---|---|
| 788:4821 | 0,228,375,360 | 0,586,375,226; поле 375×56 | 16,658,343,32 | 32,706,311,52 |
| 788:4835 | 0,228,375,360 | 0,554,375,242.00003051757812; поле 375×72.00003051757812 | 16,642.00003051757812,343,32 | 32,690.00003051757812,311,52 |
| 788:4849 | 0,228,375,360 | 0,586,375,226; поле 375×56 | 16,658,343,32 | 32,706,311,52 |
| 788:4940 | 115,231,125,120 | Форма −3,339,375,172; внутренние узлы UNKNOWN | UNKNOWN | UNKNOWN |

Info: icon 22×22, внутри строки x=0,y=5; текст x=32,y=0,w=311,h=32; gap от правого края icon до текста = 10 px. Между полем и info — 16 px; между info и CTA — 16 px. Между внутренним блоком и System navigation — 20 px.

Имя текстового узла: «После отправки запроса на вашу почту будет выслана инструкция для восстановления пароля». Имя получено CONFIRMED; `characters` отдельно не прочитано.

Особенности: в `788:4835` System navigation начинается y=762.00003051757812 и заканчивается y=796.00003051757812, оставляя около 16 px до края. Это отличие от остальных states не исправлено предположением. В `788:4940` есть отдельный `24/ Navigation` (16,54;24×24) поверх top-app-bar; Keyboard (0,521;375×291). У `788:4835` изображение по bounds заходит на форму на 34 px; в 4821/4849 — на 2 px, в 4940 — на 12 px. Визуальное перекрытие требует проверки исходного asset.

## NewPassword: 4 Frame

CONFIRMED визуальным просмотром `788:4897`: заголовок «Новый пароль»; описание «Придумайте новый пароль для входа в приложение»; password field с маскировкой и глазом; CTA «Продолжить»; close icon ×; круговой индикатор на затемнённом экране, клавиатура остаётся видна под scrim. Скорость/тип анимации и блокирование touch UNKNOWN.

Порядок INFERRED: top bar → заголовок → иллюстрация → поле → CTA → нижняя системная область / IME; в loading поверх всего scrim и indicator.

| ID | Иллюстрация | Поле x,y,w,h | CTA x,y,w,h | Дополнения |
|---|---|---|---|---|
| 788:4863 | 0,226,375,360 | 0,618,375,72 | 32,706,311,52 | Два System navigation совпадают на y=778 |
| 788:4875 | 97,197,183,176 | −1.5,371,375,72 | 30.5,459,311,52 | Контейнер x=−2,w=376; IME x=1 |
| 788:4886 | 97,197,183,176 | 0,371,375,72 | 32,459,311,52 | IME x=1 |
| 788:4897 | 97,197,183,176 | 0,371,375,72 | 32,459,311,52 | Dimming (0,0;375×812); Component 6 (168,386;40×40); IME x=1 |

Gap поле→CTA =16 px. Keyboard (1,521;375×291) выходит справа на 1 px. В 4875/4886/4897 bounds иллюстрации до y=373 пересекают поле на 2 px. Индикатор имеет центр x=188 вместо центра viewport 187.5. Ни один из этих сдвигов не следует превращать в системное правило Android без подтверждения.

Для всех групп fixed-on-scroll, overflow, clipsContent и Auto Layout UNKNOWN. Предложение: обычные Column/Row и Box, с verticalScroll для доступности на малой высоте. LazyColumn/LazyRow не обоснованы: в доступной структуре нет списков. BottomSheet/Dialog/Popup не подтверждены. Dimming — подтверждённый слой, его runtime-механизм UNKNOWN.

# 5. COMPONENTS

Имена ниже — **имена instances**, а не доказанные имена mainComponent: ссылка на исходный component и variant definitions UNKNOWN.

| Instance | Количество в раскрытом дереве | Размер px → предлагаемый dp | Compose mapping INFERRED |
|---|---:|---|---|
| Status Bar | 16 | 375×44 | Системная область Android |
| top-app-bar | 16 | 375×44 | Собственная TopBar с системными insets |
| System navigation | 14 | 375×34 | Системная навигация Android, не NavigationBar приложения |
| Keyboard | 10 | 375×291 | Системная IME, не composable-клавиатура |
| Form | 22 | 375×56 / 72 / 72.00003051757812 | AuthField; внутренняя геометрия UNKNOWN |
| Form/Input/off/off/Filled/Off/Off | 1 | 375×48.000030517578125 | Заполненное поле, variant только по имени |
| Форма | 1 | 375×172 | RecoveryForm; детали UNKNOWN |
| No activ Button | 10 | 311×52 | CTA, предположительно disabled |
| Activ Button | 5 | 311×52 | CTA, предположительно enabled |
| Войти | 8 | 313×76 | AuthHeading |
| Востановление пароля | 4 | 313×105 | AuthHeading |
| Новый пароль | 4 | 313×77 | AuthHeading |
| 24/ Other/Info | 3 | 22×22 | Icon; название 24 не равно фактическому размеру |
| 24/ Navigation | 1 | 24×24 | NavigationIcon; точный glyph UNKNOWN |
| Dimming | 1 | 375×812 | Box overlay; цвет/alpha UNKNOWN |
| Component 6 | 1 | 40×40 | Circular indicator визуально CONFIRMED; анимация UNKNOWN |
| 40 / social | 6 | 40×40, все effectively hidden | Не выводить |

Для всех: padding, typography, colors, strokes, radius, shadows, component properties UNKNOWN, если они явно не указаны как расстояния между bounds в разделе 4. Нельзя подставлять стандартные Material значения и выдавать их за Figma. SearchField, Card, ListItem, BottomNavigation приложения, TabBar, Checkbox, RadioButton, Switch, Chip, Badge, Avatar, Snackbar, Dropdown, Menu не обнаружены в раскрытых слоях; внутренние instances не проверены полностью.

# 6. COMPONENT STATES

CONFIRMED: Frame с именами «Ошибка поля» и «Верный ввод»; два имени CTA `Activ Button` / `No activ Button`; наличие Keyboard и Dimming. INFERRED: enabled/disabled CTA; focused state при IME; error/success отдельного поля. Круговой индикатор `Component 6` поверх scrim подтверждён визуально; связь с отправкой INFERRED. Успешный ввод ≠ успешная серверная авторизация.

UNKNOWN: pressed, hover, selected/unselected, checked/unchecked, expanded/collapsed, реальный loading variant, серверные ошибки, полный validation text, варианты password visibility, empty state, snackbar, confirmation state. `off/off/...` в имени нельзя надёжно разобрать без definitions свойств. Скрытые social instances не доказывают поддержку social login.

# 7. COLORS

**UNKNOWN: ни одно значение fill/stroke/effect цвета не получено.** Sparse metadata не содержит paints. Полную таблицу Compose Color нельзя заполнить достоверно.

| Figma token/style | HEX | Alpha | Использование | Статус |
|---|---|---|---|---|
| Не извлечён | UNKNOWN | UNKNOWN | Фон экранов, текст, поля, CTA, icons, scrim, effects | UNKNOWN |

В репозитории есть `presentation/src/main/java/com/elta/android/presentation/theme/Color.kt`, но соответствие его палитры этой секции не доказано. Имена BackgroundPrimary/Accent/Error не объявляются исходными Figma tokens.

# 8. TYPOGRAPHY

Font family, size, weight, lineHeight, letterSpacing, alignment, transform, decoration, textStyleId и цвет UNKNOWN. Высота текстового bounds 32 px не доказывает lineHeight=16 или fontSize=16. Размер заголовочного instance тоже не равен размеру шрифта.

| Группа | Подтверждённый bounds px | Compose TextStyle |
|---|---|---|
| Войти | 313×76, bounds всего instance | UNKNOWN |
| Востановление пароля | 313×105, bounds всего instance | UNKNOWN |
| Новый пароль | 313×77, bounds всего instance | UNKNOWN |
| Инструкция восстановления | 311×32 | UNKNOWN |
| Labels полей / кнопок / errors | Не раскрыты | UNKNOWN |

Для Android fontSize и lineHeight переносить в sp после получения параметров Figma; tracking PERCENT переводить в em / абсолютный tracking осознанно. Наличие Roboto в репозитории не подтверждает Roboto в этой секции.

# 9. SPACING & DIMENSIONS

Полный реестр: geometry.md, screen-layouts.md и node-dimensions.csv. px сохраняются без округления. Колонки proposed_*_dp в CSV — только соглашение 1:1 для стартового viewport, не реальные физические пиксели устройства.

| Геометрия CONFIRMED | px | Предложение Android INFERRED |
|---|---:|---|
| Горизонтальный inset CTA большинства экранов | 32 | 32.dp; width = availableWidth − 64.dp |
| CTA | 311×52 | fillMaxWidth с inset, minHeight/height 52.dp в базовом виде |
| Input→CTA, вход | 24 | 24.dp |
| Input→info→CTA, восстановление | 16 + 16 | spacedBy(16.dp) |
| Input→CTA, новый пароль | 16 | 16.dp |
| Info inset | 16 | 16.dp |
| Info icon→text | 10 | 10.dp |
| Нижний блок→system nav | 20 в обычных состояниях | 20.dp сверх корректного inset |
| CTA→IME | 10, кроме float-хвостов | 10.dp сверх IME inset |
| Loader | 40×40 | 40.dp, центральное размещение предложено |

Это измеренные расстояния между bounds, а не прочитанные значения Auto Layout padding/itemSpacing. Фактические внутренние padding, stroke widths, divider thickness, min/max UNKNOWN. Float-хвосты сохранены; решение об их нормализации должно быть явным и отдельным от данных источника.

# 10. SHAPES & EFFECTS

UNKNOWN: corner radius, individual corner radii, stroke width/alignment, shadows, blur, effect styles. Тип metadata `rounded-rectangle` не даёт численного radius. Dimming имеет bounds 375×812, но alpha/цвет UNKNOWN. Figma shadow не следует напрямую объявлять равным Android elevation без значений offset/blur/spread/color.

# 11. ASSETS

| Имя | Исходный слой в Картинки | Размер px | Использование | Формат / library | Экспорт |
|---|---|---|---|---|---|
| Войти!3 2 | 792:4511 | 375×360 | Login: 375×360 и 155×149 | UNKNOWN | EXPORT REQUIRED |
| Смена пароля! 2 | 792:4510 | 375×360 | Recovery: 375×360 и 125×120 | UNKNOWN | EXPORT REQUIRED |
| Новый Пароль! 2 | 792:4512 | 375×360 | NewPassword: 375×360 и 183×176 | UNKNOWN | EXPORT REQUIRED |
| Приветственный экран копия 1 | 792:4507 | 375×812 | Только gallery в этой секции | UNKNOWN | EXPORT REQUIRED при использовании |
| Активация! 2 | 792:4508 | 375×330 | Только gallery в этой секции | UNKNOWN | EXPORT REQUIRED при использовании |
| Регистрация! 2 | 792:4509 | 375×270 | Только gallery в этой секции | UNKNOWN | EXPORT REQUIRED при использовании |
| 24/ Other/Info | Instances 788:4830,4843,4857 | 22×22 | Info recovery | Component instance; master UNKNOWN | EXPORT REQUIRED для точного glyph |
| 24/ Navigation | Instance 788:4944 | 24×24 | Recovery с IME | Component instance; master UNKNOWN | EXPORT REQUIRED для точного glyph |
| Component 6 | 788:4909 | 40×40 | Круговой loader визуально CONFIRMED | UNKNOWN | EXPORT REQUIRED / исследовать анимацию |
| 40 / social | 6 скрытых instances | 40×40 | Не видимы | UNKNOWN | Не требуется для показанного UI |

Assets не экспортированы. Нельзя доказать SVG/PNG/JPEG по названию слоя или metadata-типу. Аналоги Material Icons не верифицированы. Системные status bar, navigation и keyboard переносить нативно. Визуально на Login подтверждены глаз пароля и стрелка назад внутри Form/top-app-bar; их ID, размер и master UNKNOWN; для точности EXPORT REQUIRED. Остальные вложенные иконки UNKNOWN. Pixel colors внутри изображений не являются автоматически design tokens.

# 12. DESIGN TOKENS

**DESIGN SYSTEM — неполная инвентаризация.** Variables, Color Styles, Text Styles, Effect Styles, mainComponents, Component Properties, Variants и Code Connect mappings UNKNOWN. Не утверждается их отсутствие.

Предлагаемая структура Compose INFERRED: Colors, Typography, Shapes, Spacing, Dimensions, Icons, Components. Подтверждённые размеры могут стать локальными константами реализации, но их названия не являются Figma tokens. Для существующего проекта сначала сопоставить `EltaTheme`, `EltaColors` и имеющиеся UI-компоненты после получения полного дизайна. В проекте используется `androidx.compose.material.MaterialTheme` (Material 2); миграция на Material 3 не входит в эту задачу.

# 13. USER INTERACTIONS

| Элемент / действие | Результат | Достоверность |
|---|---|---|
| Ввод в первое / второе поле Login | Меняется поле, существует семейство error/valid Frame | INFERRED; критерии UNKNOWN |
| Tap Login CTA | Отправка формы предполагается; destination UNKNOWN | INFERRED |
| Tap «Забыли пароль?» на Login | RecoveryRequest | Control CONFIRMED визуально; переход INFERRED |
| Ввод в Recovery email | Изменение поля и готовности CTA | Email CONFIRMED визуально; validation INFERRED |
| Tap «Отправить запрос на email» | Инструкция для восстановления на почту | CONFIRMED как назначение по текстовому имени; reaction UNKNOWN |
| Переход в почту / по ссылке | NewPassword или сайт | INFERRED; заметки CONFIRMED |
| Ввод нового пароля | Состояния disabled/active CTA | INFERRED |
| Tap «Продолжить» | Dimming + Component 6 | INFERRED; trigger не прочитан |
| Tap глаз пароля на Login | Показать/скрыть пароль | Control CONFIRMED визуально; поведение INFERRED |
| Tap × на Recovery/NewPassword | Закрытие / возврат, destination UNKNOWN | Control CONFIRMED визуально; результат UNKNOWN |
| Tap стрелка назад / системный Back | UNKNOWN | Стрелка на Login CONFIRMED визуально; результат UNKNOWN |

На базовом Login визуально подтверждены «Забыли пароль?», глаз пароля и стрелка назад. Для них предложены ForgotPasswordClicked, PasswordVisibilityToggled, BackClicked; результат остаётся INFERRED/UNKNOWN без reactions. Не добавлять ClearClicked, ResendClicked или social actions без соответствующих controls.

Предварительные User Actions INFERRED: Login — EmailChanged, PasswordChanged, SubmitClicked, ForgotPasswordClicked, PasswordVisibilityToggled, BackClicked; RecoveryRequest — EmailChanged, SubmitClicked, CloseClicked; NewPassword — PasswordChanged, PasswordVisibilityToggled, SubmitClicked, CloseClicked. Close controls и глаз подтверждены визуально, их поведение INFERRED/UNKNOWN. Email и password подтверждены визуальным просмотром базового Login. Focus change и IME visibility — состояние UI среды, не отдельная бизнес-навигация. Конкретное представление этих событий sealed interface допустимо после уточнения семантики полей.

# 14. SCREEN STATES

Предлагаемые модели INFERRED, без выдуманных серверных данных:

```text
LoginUiState(
  email,
  password,
  emailValidation: Unknown | Error | Valid,
  passwordValidation: Unknown | Error | Valid,
  isPasswordVisible,
  submitEnabled
)
RecoveryRequestUiState(email, emailHasError, submitEnabled)
NewPasswordUiState(password, isPasswordVisible, submitEnabled, isSubmitting)
```

Error message text и правила проверки UNKNOWN. Для recovery `1.2.1` красная линия подтверждена визуально; error inferred, причина UNKNOWN. `isSubmitting` обоснован предположением о loading overlay, а не прочитанной reaction. Focus/IME учитывать в composable через состояние платформы; не сохранять KeyboardVisible как серверное бизнес-поле. Loading Login, серверный error, token expiry, успешное завершение восстановления и повторная отправка не подтверждены.

# 15. COMPOSE COMPONENT TREE

Весь раздел — INFERRED предложение архитектуры, не исходный Auto Layout.

```text
AuthRoute
└── Box(fillMaxSize)
    ├── Scaffold / AuthPageLayout
    │   ├── AuthTopBar + statusBars insets
    │   └── Column(adaptive verticalScroll, IME/navigation insets)
    │       ├── AuthHeading
    │       ├── AuthIllustration(size depends on available height)
    │       ├── FlexibleSpace
    │       └── FormColumn
    │           ├── Login: AuthField × 2
    │           ├── Recovery: AuthField + InfoRow(Icon, Text)
    │           ├── NewPassword: AuthField
    │           └── AuthPrimaryButton
    └── NewPassword only: LoadingOverlay(Scrim, Indicator)
```

Реализовать три screen composable с общими primitives. Не рисовать нативную клавиатуру и системные панели как макетные картинки. Не комбинировать бесконечную высоту verticalScroll и weight без ограничивающего контейнера: выбрать layout с конечной доступной высотой и корректной прокруткой при недостатке места. Общий tree описывает намерение, а не готовый production-код.

# 16. RESPONSIVE BEHAVIOR

Constraints Left/Right/Center/Scale/Top/Bottom, sizing FIXED/HUG/FILL, min/max, Auto Layout direction/gap/padding/align UNKNOWN. Координаты и ширина 375 не доказывают stretch или center constraints.

INFERRED адаптация: outer form растягивать на доступную ширину; CTA оставлять 32 dp от краёв в стандартных состояниях; InfoRow — 16 dp; текст должен переноситься; icon не растягивать. Форму держать над IME с доступом к CTA через scroll на малых высотах. Изображение уменьшается при IME в подтверждённых макетах, но формула масштабирования и breakpoint UNKNOWN. Размер текста поддерживает fontScale; error/supporting text не обрезать фиксированной общей высотой field. Не масштабировать весь экран пропорционально 375×812.

Нельзя размещать production UI абсолютными y=307/371/554/586/618: это координаты одного viewport и IME. Значения 44/34/291 относятся к изображённой системной среде и не являются Android константами. Landscape, tablet, foldable, RTL, dark theme UNKNOWN.

Общие переводы после получения Auto Layout: HORIZONTAL→Row; VERTICAL→Column; FILL→fillMaxWidth/weight по оси; HUG→wrapContentSize; SPACE_BETWEEN→Arrangement.SpaceBetween; фиксированный gap→spacedBy. В этой секции конкретное применение этих флагов пока не подтверждено.

# 17. UNKNOWN / MISSING INFORMATION

| Проверка полноты | Результат |
|---|---|
| Все верхнеуровневые объекты секции | CONFIRMED: 16 mobile Frame, 3 Note, 1 gallery, 16 connectors |
| Все раскрытые Frame | Учтены в geometry.md, включая скрытые connector backgrounds |
| Все instances | Учтены 123 раскрытых; остальные вложенные UNKNOWN |
| Все variants / mainComponents | UNKNOWN |
| Все prototype reactions / destinations / overlays | UNKNOWN |
| Dimming и Component 6 | CONFIRMED геометрия; поведение UNKNOWN |
| Все цвета / styles / variables / effects | UNKNOWN |
| Все typography styles / characters | Styles UNKNOWN; базовые Login labels прочитаны визуально, остальные тексты неполны |
| Все assets | 6 gallery items и видимые внешние слои учтены; nested assets UNKNOWN |
| Все screen states | 16 Frame учтены; полнота состояний внутри components UNKNOWN |
| Повторный полный обход перед завершением | Не выполнен из-за лимита MCP |

Необходимо получить полный subtree `788:4818`, связанные mainComponents/variant sets, styles/variables, reactions с trigger/action/destination, constraints/Auto Layout, text segments, image fills, export settings и screenshots. Для продолжения нужен доступный Figma MCP либо экспорт узлов со свойствами, styles/components/variables/reactions и assets. Скриншоты сами по себе не закроют точные параметры.

Неоднозначности макета: разные функции у одинакового `1.1.2`; два `1.1`; несовпадение 22 px с названием `24/ Other/Info`; сдвиг RecoveryForm x=−3; NewPassword x=−2/−1.5; Keyboard x=1; отличающееся нижнее пространство 1.2.1; два System navigation в 1.2.3; пересечения bounds иллюстраций/полей; hidden social blocks. Исправления не внесены.

MISSING не назначен цветам, состояниям или токенам: отсутствие их в sparse response не доказывает отсутствие в Figma.

# 18. IMPLEMENTATION NOTES FOR JETPACK COMPOSE

1. Существующие 16 Frame реализовывать как три семейства UiState, не 16 destinations.
2. Нативные системные панели и IME; insets учитывать ровно один раз. Наличие слоёв iOS-подобной геометрии не задаёт Android высоты.
3. Переиспользовать тему и UI primitives проекта после проверки соответствия; готовые Material поля/кнопки могут иметь отличающиеся padding/minHeight/radius и не гарантируют точности.
4. Не вводить validation regex, полный password policy, URL/deep link, timeout, navigation success target и тексты ошибок без данных. Из видимой подсказки подтверждено только упоминание минимум 8 символов и A-Z, a-z, 0-9; правила комбинаций и запрет других символов не определены.
5. CTA 52 px не задаёт высоту текста или padding. Визуальный размер icon 22/24 px не равен размеру touch target.
6. При получении assets экспортировать оригиналы и проверить crop/alpha/scale, особенно в местах пересечения bounds. Не заменять иллюстрации приблизительными изображениями.
7. После закрытия UNKNOWN проверить базовый 375 dp viewport, малую высоту с IME, увеличенный fontScale и back/submit flow. Визуально проверены базовый Login, Login с ошибкой пароля (рядом видна ошибка email), Recovery 1.2.1 и NewPassword 1.2.6. Полная visual QA всех Frame и будущей реализации не выполнена.

Производственный код не изменён. Этот пакет сохраняет подтверждённую геометрию и предварительную архитектуру, но **недостаточен для точной реализации без дополнительного получения Figma-данных**.
