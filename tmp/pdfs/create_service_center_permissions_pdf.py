from reportlab.lib import colors
from reportlab.lib.enums import TA_CENTER
from reportlab.lib.pagesizes import A4, landscape
from reportlab.lib.styles import ParagraphStyle, getSampleStyleSheet
from reportlab.lib.units import mm
from reportlab.pdfbase import pdfmetrics
from reportlab.pdfbase.ttfonts import TTFont
from reportlab.platypus import BaseDocTemplate, Frame, PageBreak, PageTemplate, Paragraph, Spacer, Table, TableStyle

OUTPUT = "output/pdf/android_permissions_service_center.pdf"
REGULAR_FONT = "/System/Library/Fonts/Supplemental/Arial Unicode.ttf"
BOLD_FONT = "/System/Library/Fonts/Supplemental/Arial Bold.ttf"

pdfmetrics.registerFont(TTFont("ArialUnicode", REGULAR_FONT))
pdfmetrics.registerFont(TTFont("ArialBold", BOLD_FONT))

PAGE_WIDTH, PAGE_HEIGHT = landscape(A4)
LEFT = RIGHT = 15 * mm
TOP = 14 * mm
BOTTOM = 16 * mm

styles = getSampleStyleSheet()
title_style = ParagraphStyle(
    "Title", parent=styles["Title"], fontName="ArialBold", fontSize=18, leading=22,
    textColor=colors.HexColor("#163D63"), spaceAfter=3.5 * mm,
)
intro_style = ParagraphStyle(
    "Intro", parent=styles["Normal"], fontName="ArialUnicode", fontSize=8.7, leading=11.5,
    textColor=colors.HexColor("#3E4C59"), spaceAfter=4 * mm,
)
note_style = ParagraphStyle(
    "Note", parent=intro_style, fontSize=8.1, leading=10.5, textColor=colors.HexColor("#4B5563"),
    borderColor=colors.HexColor("#C7D9E8"), borderWidth=0.6, borderPadding=2.8 * mm,
    backColor=colors.HexColor("#F2F8FC"), spaceBefore=1 * mm, spaceAfter=5 * mm,
)
section_style = ParagraphStyle(
    "Section", parent=styles["Heading2"], fontName="ArialBold", fontSize=10.5, leading=13,
    textColor=colors.HexColor("#163D63"), spaceBefore=3 * mm, spaceAfter=2 * mm,
)
cell_style = ParagraphStyle(
    "Cell", parent=styles["BodyText"], fontName="ArialUnicode", fontSize=6.65, leading=8.25,
    textColor=colors.HexColor("#1F2937"),
)
code_style = ParagraphStyle(
    "Code", parent=cell_style, fontSize=6.15, leading=7.75, textColor=colors.HexColor("#193B5C"),
)
number_style = ParagraphStyle("Number", parent=cell_style, alignment=TA_CENTER)
header_style = ParagraphStyle(
    "Header", parent=cell_style, fontName="ArialBold", fontSize=6.6, leading=7.8, textColor=colors.white,
)

GROUPS = [
    ("Сеть и подключённые устройства", [
        ("android.permission.INTERNET", "Доступ к интернету.", "Обмен данными с сервисами МП: авторизация, синхронизация показателей, загрузка материалов. При отказе: для работы онлайн нужен доступ к сети.", "«Это стандартное техническое право на обмен данными с сервером. Оно не открывает доступ к данным телефона»."),
        ("android.permission.ACCESS_NETWORK_STATE", "Получение информации о состоянии и типе сетевого подключения.", "Проверка наличия сети и выбор корректного сценария работы онлайн или офлайн. При отказе: система не предоставляет отдельный пользовательский запрос.", "«Право позволяет приложению понять, есть ли интернет. Личные данные пользователя оно не раскрывает»."),
        ("android.permission.BLUETOOTH_SCAN", "Поиск находящихся рядом Bluetooth-устройств.", "Поиск глюкометра, НМГ и других совместимых устройств при добавлении или синхронизации. При отказе: устройство не будет найдено автоматически.", "«Нужно только при поиске вашего медицинского устройства рядом с телефоном»."),
        ("android.permission.BLUETOOTH_CONNECT", "Подключение к сопряжённым Bluetooth-устройствам и обмен данными с ними.", "Подключение к совместимому медицинскому устройству и получение его показателей. При отказе: синхронизация по Bluetooth недоступна.", "«Разрешение нужно для соединения с выбранным вами устройством, а не для доступа к содержимому телефона»."),
        ("android.permission.BLUETOOTH_ADVERTISE", "Передача Bluetooth-рекламы, чтобы устройство могло быть обнаружено другими устройствами.", "Техническое Bluetooth-разрешение, заявленное в составе МП. Отдельный пользовательский сценарий для него не выделен. При отказе: отдельного сценария в интерфейсе нет.", "«Это техническое разрешение Bluetooth. Оно не предоставляет доступ к геолокации, камере, микрофону или файлам»."),
        ("android.permission.BLUETOOTH", "Использование Bluetooth на Android 11 и ниже.", "Совместимость функций поиска и подключения медицинских устройств на Android 11 и ниже. При отказе: Bluetooth-синхронизация на таких версиях ОС недоступна.", "«Это устаревший вариант Bluetooth-разрешения для старых версий Android»."),
        ("android.permission.BLUETOOTH_ADMIN", "Управление Bluetooth на Android 11 и ниже: поиск, подключение и настройка соединений.", "Совместимость функций поиска и подключения медицинских устройств на Android 11 и ниже. При отказе: Bluetooth-синхронизация на таких версиях ОС недоступна.", "«Это техническое разрешение для работы Bluetooth на старых устройствах»."),
    ]),
    ("Местоположение, аппаратные возможности и данные пользователя", [
        ("android.permission.ACCESS_FINE_LOCATION", "Доступ к точному местоположению устройства.", "Поиск BLE-устройств на старых Android и определение текущей точки на карте аптек. При отказе: поиск устройства на старых ОС и функция текущей точки на карте могут быть ограничены.", "«Доступ нужен только для этих функций. Без разрешения приложение не получает точную геопозицию через системный API»."),
        ("android.permission.ACCESS_COARSE_LOCATION", "Доступ к приблизительному местоположению устройства.", "Совместимость поиска BLE-устройств и карты аптек, когда доступна приблизительная геопозиция. При отказе: связанные функции могут работать ограниченно.", "«Это менее точный вариант геолокации. Его можно не предоставлять, если вы не используете карту и поиск устройства на старом Android»."),
        ("android.permission.CAMERA", "Доступ к камере устройства.", "Сканирование кода при подключении устройства и создание фотографии для консультации. При отказе: сканирование и фото недоступны.", "«Камера запрашивается при вашем действии - сканировании или создании фото. Без разрешения эти функции не откроются»."),
        ("android.permission.RECORD_AUDIO", "Запись звука с микрофона.", "Голосовое общение в функционале консультации. При отказе: голосовая часть консультации недоступна.", "«Микрофон нужен только для передачи голоса во время консультации»."),
        ("android.permission.ACTIVITY_RECOGNITION", "Доступ к данным о физической активности: шаги, ходьба, бег и т. п.", "Интеграция с Google Fit. При отказе: импорт данных физической активности из Google Fit недоступен.", "«Разрешение нужно только при подключении Google Fit. Основные функции МП продолжат работать»."),
        ("android.permission.READ_CONTACTS", "Чтение контактов из адресной книги устройства.", "Технически заявлено подключённым модулем. Отдельный пользовательский сценарий в текущем МП не выделен. При отказе: основной функционал МП не должен быть затронут.", "«Контакты не требуются для основных функций приложения. Запрос на доступ к ним не должен появляться в обычном сценарии работы»."),
        ("android.permission.WRITE_CONTACTS", "Создание, изменение и удаление контактов в адресной книге устройства.", "Технически заявлено подключённым модулем. Отдельный пользовательский сценарий в текущем МП не выделен. При отказе: основной функционал МП не должен быть затронут.", "«МП не требует изменения контактов для основных функций. Запрос на это право не ожидается в обычной работе»."),
        ("android.permission.USE_FINGERPRINT", "Использование аутентификации по отпечатку пальца на старых версиях Android.", "Поддержка входа или подтверждения действия по отпечатку пальца на совместимых старых устройствах. При отказе: потребуется альтернативный способ подтверждения.", "«Отпечаток не передаётся в МП: проверку выполняет защищённый механизм Android»."),
    ]),
    ("Уведомления, фоновая работа и системные события", [
        ("android.permission.POST_NOTIFICATIONS", "Отправка уведомлений пользователю на Android 13 и выше.", "Напоминания, сообщения о статусе функций и сервисные уведомления. При отказе: МП не сможет показывать уведомления.", "«Основные функции останутся доступны, но напоминания и другие уведомления не будут отображаться»."),
        ("android.permission.VIBRATE", "Управление вибрацией устройства.", "Вибросигнал как часть уведомлений. При отказе: отдельного запроса Android обычно не показывает; уведомления могут быть без вибрации.", "«Разрешение нужно только для вибрации в уведомлениях»."),
        ("android.permission.FOREGROUND_SERVICE", "Запуск службы, работающей в фоне с постоянным уведомлением.", "Поддержка фоновой синхронизации и длительных операций, пока пользователь видит системное уведомление. При отказе: связанные фоновые операции могут быть прерваны ОС.", "«Такая служба работает с видимым уведомлением Android; её нельзя скрытно запустить как обычный фоновый процесс»."),
        ("android.permission.FOREGROUND_SERVICE_CONNECTED_DEVICE", "Запуск foreground-службы для взаимодействия с подключённым устройством.", "Фоновая синхронизация с подключённым медицинским устройством, мониторинг и обновление прошивки. При отказе: эти операции не смогут устойчиво выполняться в фоне.", "«Право относится к связи с подключённым устройством и сопровождается системным уведомлением»."),
        ("android.permission.SCHEDULE_EXACT_ALARM", "Создание точных будильников и напоминаний. Предоставляется пользователем через системные настройки.", "Доставка напоминания в установленное пользователем время. При отказе: напоминание может сработать неточно или не сработать в нужный момент.", "«Разрешение нужно только для точных напоминаний, заданных пользователем»."),
        ("android.permission.USE_EXACT_ALARM", "Использование точных будильников; специальное разрешение для приложений с обоснованной потребностью в точных срабатываниях.", "Техническая поддержка точных напоминаний. При отказе: точность времени напоминаний может быть ограничена ОС.", "«Это системное право для напоминаний; оно не даёт доступ к личным данным»."),
        ("android.permission.RECEIVE_BOOT_COMPLETED", "Получение системного события после перезагрузки устройства.", "Восстановление ранее созданных пользователем напоминаний после перезагрузки или обновления приложения. При отказе: напоминания потребуется настроить заново.", "«После перезагрузки МП получает только системный сигнал, чтобы восстановить ваши напоминания»."),
        ("android.permission.WAKE_LOCK", "Предотвращение перехода устройства или процессора в сон на время выполнения задачи.", "Техническая поддержка непрерывной выполнения операции на короткое время, например при работе с подключённым устройством. При отказе: ОС может прервать задачу при переходе в сон.", "«Право не включает экран и не открывает доступ к данным; оно лишь помогает не прерывать техническую операцию»."),
        ("android.permission.ACCESS_NOTIFICATION_POLICY", "Доступ к политике режима \"Не беспокоить\" и её настройкам.", "Технически заявлено в МП. Отдельный пользовательский сценарий не выделен. При отказе: основной функционал МП не должен быть затронут.", "«Это право не требуется для обычной работы приложения. Оно не даёт доступа к текстам ваших уведомлений»."),
        ("com.huawei.android.launcher.permission.CHANGE_BADGE", "Изменение счётчика уведомлений на иконке приложения в лаунчере Huawei.", "Отображение числа непрочитанных уведомлений на иконке МП на поддерживаемых устройствах Huawei. При отказе: может не отображаться счётчик на иконке.", "«Это специфичное для Huawei право на счётчик уведомлений на значке приложения»."),
    ]),
    ("Файлы и данные Health Connect", [
        ("android.permission.WRITE_EXTERNAL_STORAGE", "Запись файлов в общее внешнее хранилище на Android 9 и ниже.", "Сохранение изображений на устройствах с Android 9 и ниже. При отказе: сохранение файлов в общую галерею может быть недоступно.", "«Разрешение актуально только для старых версий Android и нужно для сохранения изображений»."),
        ("android.permission.health.READ_BLOOD_GLUCOSE", "Чтение показателей глюкозы из Health Connect.", "Импорт данных глюкозы при добровольном подключении Health Connect. При отказе: импорт глюкозы из Health Connect недоступен.", "«Доступ запрашивается только при подключении Health Connect. Основная работа МП сохранится без него»."),
        ("android.permission.health.READ_WEIGHT", "Чтение данных о весе из Health Connect.", "Импорт данных о весе при добровольном подключении Health Connect. При отказе: импорт веса из Health Connect недоступен.", "«Вы управляете доступом в Health Connect и можете отозвать его в системных настройках»."),
        ("android.permission.health.READ_TOTAL_CALORIES_BURNED", "Чтение данных о суммарно израсходованных калориях из Health Connect.", "Импорт данных о расходе калорий при добровольном подключении Health Connect. При отказе: импорт этих данных недоступен.", "«Разрешение относится только к выбранной интеграции Health Connect»."),
        ("android.permission.health.READ_STEPS", "Чтение данных о шагах из Health Connect.", "Импорт шагов при добровольном подключении Health Connect. При отказе: импорт шагов недоступен.", "«МП получает эти данные только после предоставления доступа в Health Connect»."),
        ("android.permission.health.READ_EXERCISE", "Чтение данных о тренировках из Health Connect.", "Импорт тренировок при добровольном подключении Health Connect. При отказе: импорт тренировок недоступен.", "«МП получает эти данные только после предоставления доступа в Health Connect»."),
    ]),
]


def footer(canvas, doc):
    canvas.saveState()
    canvas.setStrokeColor(colors.HexColor("#D7E1EB"))
    canvas.setLineWidth(0.45)
    canvas.line(LEFT, 10 * mm, PAGE_WIDTH - RIGHT, 10 * mm)
    canvas.setFont("ArialUnicode", 6.8)
    canvas.setFillColor(colors.HexColor("#667085"))
    canvas.drawString(LEFT, 6.2 * mm, "Памятка сервисного центра - разрешения мобильного приложения")
    canvas.drawRightString(PAGE_WIDTH - RIGHT, 6.2 * mm, f"Страница {doc.page}")
    canvas.restoreState()


def permission_table(rows, start_number):
    data = [[
        Paragraph("№", header_style),
        Paragraph("Разрешение ОС", header_style),
        Paragraph("Системное назначение", header_style),
        Paragraph("Функциональное назначение", header_style),
        Paragraph("Комментарий для пользователя", header_style),
    ]]
    for index, (permission, system_purpose, functional_purpose, operator_comment) in enumerate(rows, start=start_number):
        data.append([
            Paragraph(str(index), number_style),
            Paragraph(permission, code_style),
            Paragraph(system_purpose, cell_style),
            Paragraph(functional_purpose, cell_style),
            Paragraph(operator_comment, cell_style),
        ])
    table = Table(
        data,
        colWidths=[9 * mm, 53 * mm, 47 * mm, 72 * mm, 69 * mm],
        repeatRows=1,
        hAlign="LEFT",
    )
    table.setStyle(TableStyle([
        ("BACKGROUND", (0, 0), (-1, 0), colors.HexColor("#1E5D8C")),
        ("VALIGN", (0, 0), (-1, -1), "TOP"),
        ("GRID", (0, 0), (-1, -1), 0.3, colors.HexColor("#D2DFE9")),
        ("ROWBACKGROUNDS", (0, 1), (-1, -1), [colors.white, colors.HexColor("#F4F8FB")]),
        ("LEFTPADDING", (0, 0), (-1, -1), 2.1 * mm),
        ("RIGHTPADDING", (0, 0), (-1, -1), 2.1 * mm),
        ("TOPPADDING", (0, 0), (-1, -1), 2.0 * mm),
        ("BOTTOMPADDING", (0, 0), (-1, -1), 2.0 * mm),
    ]))
    return table


def build_pdf():
    frame = Frame(LEFT, BOTTOM, PAGE_WIDTH - LEFT - RIGHT, PAGE_HEIGHT - TOP - BOTTOM, id="content")
    document = BaseDocTemplate(
        OUTPUT, pagesize=landscape(A4), leftMargin=LEFT, rightMargin=RIGHT,
        topMargin=TOP, bottomMargin=BOTTOM,
        pageTemplates=[PageTemplate(id="document", frames=[frame], onPage=footer)],
        title="Памятка сервисного центра: разрешения мобильного приложения",
        author="ELTA",
    )
    story = [
        Paragraph("Разрешения мобильного приложения - памятка сервисного центра", title_style),
        Paragraph(
            "Документ помогает объяснить пользователю, для какой функции МП требуется разрешение и что изменится при отказе. Формулировки в последнем столбце предназначены для устного или письменного ответа пользователю.",
            intro_style,
        ),
        Paragraph(
            "Важно: наличие записи в AndroidManifest.xml не означает, что приложение получает соответствующие данные автоматически. Для разрешений, требующих согласия, доступ контролируется Android и предоставляется пользователем. Часть разрешений служебные, совместимые со старыми версиями Android или специфичные для отдельных производителей устройств.",
            note_style,
        ),
    ]
    number = 1
    for group_index, (group_title, rows) in enumerate(GROUPS):
        if group_index == 3:
            story.append(PageBreak())
        story.append(Paragraph(group_title, section_style))
        story.append(permission_table(rows, number))
        story.append(Spacer(1, 2.5 * mm))
        number += len(rows)
    story.append(Paragraph(
        "Примечание: Bluetooth Low Energy и камера дополнительно заявлены как требования к устройству, но не являются разрешениями пользователя.",
        note_style,
    ))
    document.build(story)


if __name__ == "__main__":
    build_pdf()
