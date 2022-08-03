package com.elta.android.data.features.diary.insulin.api

import com.elta.android.domain.features.diary.events.model.InsulinType
import io.reactivex.Observable

class MockedInsulinDrugNameApi : InsulinDrugNameApi {
    override fun getDrugNamesByInsulinType(type: InsulinType): Observable<List<String>> =
        Observable.just(
            when (type) {
                InsulinType.ULTRAFAST -> insulinDrugNamesUF
                InsulinType.ULTRASHORT -> insulinDrugNamesUS
                InsulinType.SHORT -> insulinDrugNamesS
                InsulinType.INTERMIDIATE -> insulinDrugNamesI
                InsulinType.LONG -> insulinDrugNamesL
                InsulinType.ULTRALONG -> insulinDrugNamesUL
                InsulinType.MIXED -> insulinDrugNamesM
            }
        )
}

private val insulinDrugNamesUF = listOf(
    "Фиасп",
    "Люмжев",
    "Люмжев 200"
)
private val insulinDrugNamesUS = listOf(
    "Хумалог",
    "Инсулин лизпро",
    "РинЛиз",
    "Хумалог 200",
    "НовоРапид",
    "РинФаст",
    "Росинсулин аспарт Р",
    "Апидра"
)
private val insulinDrugNamesS = listOf(
    "Актрапид НМ",
    "Хумулин Регуляр",
    "Инсуман Рапид ГТ",
    "Биосулин Р",
    "Генсулин Р",
    "Ринсулин Р",
    "Росинсулин Р",
    "Возулим-Р",
    "Моноинсулин ЧР"
)
private val insulinDrugNamesI = listOf(
    "Протафан HM",
    "Хумулин НПХ",
    "Инсуман Базал ГТ",
    "Биосулин Н",
    "Генсулин Н",
    "Ринсулин НПХ",
    "Росинсулин С",
    "Возулим-Н",
    "Протамин-инсулин ЧС"
)
private val insulinDrugNamesL = listOf(
    "Лантус",
    "Инсулин гларгин",
    "РинГлар",
    "Базаглар",
    "Туджео",
    "Левемир"
)
private val insulinDrugNamesUL = listOf(
    "Тресиба"
)
private val insulinDrugNamesM = listOf(
    "Хумулин М3",
    "Инсуман Комб 25 ГТ",
    "Биосулин 30/70",
    "Генсулин М30",
    "Ринсулин Микс 30/70",
    "Росинсулин М микс 30/70",
    "Возулим-30/70",
    "Хумалог Микс 25",
    "Хумалог Микс 50",
    "РинЛиз Микс 25",
    "НовоМикс 30",
    "РинФаст Микс 30",
    "Райзодег"
)
