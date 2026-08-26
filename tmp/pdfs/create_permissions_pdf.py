from reportlab.lib import colors
from reportlab.lib.enums import TA_CENTER, TA_LEFT
from reportlab.lib.pagesizes import A4
from reportlab.lib.styles import ParagraphStyle, getSampleStyleSheet
from reportlab.lib.units import mm
from reportlab.pdfbase import pdfmetrics
from reportlab.pdfbase.ttfonts import TTFont
from reportlab.platypus import (
    BaseDocTemplate,
    Frame,
    KeepTogether,
    PageBreak,
    PageTemplate,
    Paragraph,
    Spacer,
    Table,
    TableStyle,
)

OUTPUT = "output/pdf/android_permissions.pdf"
REGULAR_FONT = "/System/Library/Fonts/Supplemental/Arial Unicode.ttf"
BOLD_FONT = "/System/Library/Fonts/Supplemental/Arial Bold.ttf"

pdfmetrics.registerFont(TTFont("ArialUnicode", REGULAR_FONT))
pdfmetrics.registerFont(TTFont("ArialBold", BOLD_FONT))

PAGE_WIDTH, PAGE_HEIGHT = A4
LEFT = RIGHT = 17 * mm
TOP = 16 * mm
BOTTOM = 17 * mm

styles = getSampleStyleSheet()
title_style = ParagraphStyle(
    "Title",
    parent=styles["Title"],
    fontName="ArialBold",
    fontSize=19,
    leading=24,
    textColor=colors.HexColor("#163D63"),
    alignment=TA_LEFT,
    spaceAfter=5 * mm,
)
subtitle_style = ParagraphStyle(
    "Subtitle",
    parent=styles["Normal"],
    fontName="ArialUnicode",
    fontSize=9.5,
    leading=13,
    textColor=colors.HexColor("#4B5563"),
    spaceAfter=7 * mm,
)
section_style = ParagraphStyle(
    "Section",
    parent=styles["Heading2"],
    fontName="ArialBold",
    fontSize=11,
    leading=14,
    textColor=colors.HexColor("#163D63"),
    spaceBefore=4 * mm,
    spaceAfter=2.5 * mm,
)
cell_style = ParagraphStyle(
    "Cell",
    parent=styles["BodyText"],
    fontName="ArialUnicode",
    fontSize=8.1,
    leading=10.6,
    textColor=colors.HexColor("#1F2937"),
)
code_style = ParagraphStyle(
    "Code",
    parent=cell_style,
    fontSize=7.3,
    leading=9.6,
    textColor=colors.HexColor("#193B5C"),
)
number_style = ParagraphStyle(
    "Number",
    parent=cell_style,
    alignment=TA_CENTER,
)
header_style = ParagraphStyle(
    "Header",
    parent=cell_style,
    fontName="ArialBold",
    fontSize=8.2,
    leading=10,
    textColor=colors.white,
)

GROUPS = [
    (
        "Сеть и подключённые устройства",
        [
            ("android.permission.INTERNET", "Доступ к интернету."),
            ("android.permission.ACCESS_NETWORK_STATE", "Получение информации о состоянии и типе сетевого подключения."),
            ("android.permission.BLUETOOTH_SCAN", "Поиск находящихся рядом Bluetooth-устройств."),
            ("android.permission.BLUETOOTH_CONNECT", "Подключение к сопряжённым Bluetooth-устройствам и обмен данными с ними."),
            ("android.permission.BLUETOOTH_ADVERTISE", "Передача Bluetooth-рекламы, чтобы устройство могло быть обнаружено другими устройствами."),
            ("android.permission.BLUETOOTH", "Использование Bluetooth на Android 11 и ниже."),
            ("android.permission.BLUETOOTH_ADMIN", "Управление Bluetooth на Android 11 и ниже: поиск, подключение и настройка соединений."),
        ],
    ),
    (
        "Местоположение, аппаратные возможности и данные пользователя",
        [
            ("android.permission.ACCESS_FINE_LOCATION", "Доступ к точному местоположению устройства."),
            ("android.permission.ACCESS_COARSE_LOCATION", "Доступ к приблизительному местоположению устройства."),
            ("android.permission.CAMERA", "Доступ к камере устройства."),
            ("android.permission.RECORD_AUDIO", "Запись звука с микрофона."),
            ("android.permission.ACTIVITY_RECOGNITION", "Доступ к данным о физической активности: шаги, ходьба, бег и т. п."),
            ("android.permission.READ_CONTACTS", "Чтение контактов из адресной книги устройства."),
            ("android.permission.WRITE_CONTACTS", "Создание, изменение и удаление контактов в адресной книге устройства."),
            ("android.permission.USE_FINGERPRINT", "Использование аутентификации по отпечатку пальца на старых версиях Android."),
        ],
    ),
    (
        "Уведомления, фоновая работа и системные события",
        [
            ("android.permission.POST_NOTIFICATIONS", "Отправка уведомлений пользователю на Android 13 и выше."),
            ("android.permission.VIBRATE", "Управление вибрацией устройства."),
            ("android.permission.FOREGROUND_SERVICE", "Запуск службы, работающей в фоне с постоянным уведомлением."),
            ("android.permission.FOREGROUND_SERVICE_CONNECTED_DEVICE", "Запуск foreground-службы для взаимодействия с подключённым устройством."),
            ("android.permission.SCHEDULE_EXACT_ALARM", "Создание точных будильников и напоминаний. Предоставляется пользователем через системные настройки."),
            ("android.permission.USE_EXACT_ALARM", "Использование точных будильников; специальное разрешение для приложений с обоснованной потребностью в точных срабатываниях."),
            ("android.permission.RECEIVE_BOOT_COMPLETED", "Получение системного события после перезагрузки устройства."),
            ("android.permission.WAKE_LOCK", "Предотвращение перехода устройства или процессора в сон на время выполнения задачи."),
            ("android.permission.ACCESS_NOTIFICATION_POLICY", "Доступ к политике режима \"Не беспокоить\" и её настройкам."),
            ("com.huawei.android.launcher.permission.CHANGE_BADGE", "Изменение счётчика уведомлений на иконке приложения в лаунчере Huawei."),
        ],
    ),
    (
        "Файлы и данные Health Connect",
        [
            ("android.permission.WRITE_EXTERNAL_STORAGE", "Запись файлов в общее внешнее хранилище на Android 9 и ниже."),
            ("android.permission.health.READ_BLOOD_GLUCOSE", "Чтение показателей глюкозы из Health Connect."),
            ("android.permission.health.READ_WEIGHT", "Чтение данных о весе из Health Connect."),
            ("android.permission.health.READ_TOTAL_CALORIES_BURNED", "Чтение данных о суммарно израсходованных калориях из Health Connect."),
            ("android.permission.health.READ_STEPS", "Чтение данных о шагах из Health Connect."),
            ("android.permission.health.READ_EXERCISE", "Чтение данных о тренировках из Health Connect."),
        ],
    ),
]


def footer(canvas, doc):
    canvas.saveState()
    canvas.setStrokeColor(colors.HexColor("#D7E1EB"))
    canvas.setLineWidth(0.5)
    canvas.line(LEFT, 11 * mm, PAGE_WIDTH - RIGHT, 11 * mm)
    canvas.setFont("ArialUnicode", 7.3)
    canvas.setFillColor(colors.HexColor("#667085"))
    canvas.drawString(LEFT, 7 * mm, "Перечень разрешений мобильного приложения")
    canvas.drawRightString(PAGE_WIDTH - RIGHT, 7 * mm, f"Страница {doc.page}")
    canvas.restoreState()


def permission_table(rows, start_number):
    data = [[
        Paragraph("№", header_style),
        Paragraph("Право ОС", header_style),
        Paragraph("Системное назначение", header_style),
    ]]
    for index, (permission, purpose) in enumerate(rows, start=start_number):
        data.append([
            Paragraph(str(index), number_style),
            Paragraph(permission, code_style),
            Paragraph(purpose, cell_style),
        ])
    table = Table(data, colWidths=[13 * mm, 68 * mm, 102 * mm], repeatRows=1, hAlign="LEFT")
    table.setStyle(TableStyle([
        ("BACKGROUND", (0, 0), (-1, 0), colors.HexColor("#1E5D8C")),
        ("VALIGN", (0, 0), (-1, -1), "TOP"),
        ("GRID", (0, 0), (-1, -1), 0.35, colors.HexColor("#D7E1EB")),
        ("BACKGROUND", (0, 1), (-1, -1), colors.white),
        ("ROWBACKGROUNDS", (0, 1), (-1, -1), [colors.white, colors.HexColor("#F5F9FC")]),
        ("LEFTPADDING", (0, 0), (-1, -1), 3.2 * mm),
        ("RIGHTPADDING", (0, 0), (-1, -1), 3.2 * mm),
        ("TOPPADDING", (0, 0), (-1, -1), 2.6 * mm),
        ("BOTTOMPADDING", (0, 0), (-1, -1), 2.6 * mm),
    ]))
    return table


def build_pdf():
    frame = Frame(LEFT, BOTTOM, PAGE_WIDTH - LEFT - RIGHT, PAGE_HEIGHT - TOP - BOTTOM, id="content")
    document = BaseDocTemplate(
        OUTPUT,
        pagesize=A4,
        leftMargin=LEFT,
        rightMargin=RIGHT,
        topMargin=TOP,
        bottomMargin=BOTTOM,
        pageTemplates=[PageTemplate(id="document", frames=[frame], onPage=footer)],
        title="Перечень разрешений мобильного приложения",
        author="ELTA",
    )

    story = [
        Paragraph("Перечень разрешений мобильного приложения", title_style),
        Paragraph(
            "Системные разрешения, заявленные в AndroidManifest.xml, и их назначение в операционной системе Android.",
            subtitle_style,
        ),
    ]
    number = 1
    for group_index, (group_title, rows) in enumerate(GROUPS):
        if group_index == 2:
            story.append(PageBreak())
        story.append(Paragraph(group_title, section_style))
        story.append(permission_table(rows, number))
        story.append(Spacer(1, 2 * mm))
        number += len(rows)

    story.append(Spacer(1, 2 * mm))
    story.append(Paragraph(
        "Примечание: Bluetooth Low Energy и камера дополнительно заявлены как требования к устройству, но не являются разрешениями пользователя.",
        subtitle_style,
    ))
    document.build(story)


if __name__ == "__main__":
    build_pdf()
