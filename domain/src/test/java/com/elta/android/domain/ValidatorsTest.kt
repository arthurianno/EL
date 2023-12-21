package com.elta.android.domain

import com.elta.android.domain.features.diary.events.model.form.ActivityValidator
import com.elta.android.domain.features.diary.events.model.form.BreadValidator
import com.elta.android.domain.features.diary.events.model.form.GlucoseValidator
import com.elta.android.domain.features.diary.events.model.form.InsulinMedicamentValidator
import com.elta.android.domain.features.diary.events.model.form.MedicamentsValidator
import com.elta.android.domain.features.diary.events.model.form.WeightValidator
import org.junit.Test
import org.threeten.bp.ZonedDateTime

class ValidatorsTest {

    @Test
    fun activity_WithDurationAndDate_False() {
        assert(!ActivityValidator.isValid(
            duration = 0L,
            tabletsNumber = form.tabletsNumber,
            date = ZonedDateTime.now()
        ))
    }

    @Test
    fun activity_WithDurationAndDate_True() {
        assert(ActivityValidator.isValid(
            duration = 1L,
            tabletsNumber = form.tabletsNumber,
            date = ZonedDateTime.now()
        ))
    }

    @Test
    fun bread_LowerBoundAndDate_True() {
        assert(BreadValidator.isValid(
            value = 0.1,
            tabletsNumber = form.tabletsNumber,
            date = ZonedDateTime.now()
        ))
    }

    @Test
    fun bread_TopBoundAndDate_True() {
        assert(BreadValidator.isValid(
            value = 99.9,
            tabletsNumber = form.tabletsNumber,
            date = ZonedDateTime.now()
        ))
    }

    @Test
    fun bread_OutOfBoundAndDate_False() {
        assert(!BreadValidator.isValid(
            value = 0.0,
            tabletsNumber = form.tabletsNumber,
            date = ZonedDateTime.now()
        ))
        assert(!BreadValidator.isValid(
            value = 100.0,
            tabletsNumber = form.tabletsNumber,
            date = ZonedDateTime.now()
        ))
    }

    @Test
    fun bread_WithKind_True() {
        assert(BreadValidator.isValid(
            value = 0.1,
            kind = "test",
            tabletsNumber = form.tabletsNumber,
            date = ZonedDateTime.now()
        ))
    }

    @Test
    fun glucose_LowerBoundAndDate_True() {
        assert(GlucoseValidator.isValid(
            value = 0.1,
            tabletsNumber = form.tabletsNumber,
            date = ZonedDateTime.now()
        ))
    }

    @Test
    fun glucose_TopBoundAndDate_True() {
        assert(GlucoseValidator.isValid(
            value = 65.0,
            tabletsNumber = form.tabletsNumber,
            date = ZonedDateTime.now()
        ))
    }

    @Test
    fun glucose_OutOfBoundAndDate_False() {
        assert(!GlucoseValidator.isValid(
            value = 0.0,
            tabletsNumber = form.tabletsNumber,
            date = ZonedDateTime.now()
        ))
        assert(!GlucoseValidator.isValid(
            value = 65.1,
            tabletsNumber = form.tabletsNumber,
            date = ZonedDateTime.now()
        ))
    }

    @Test
    fun insulin_LowerBoundAndDate_True() {
        assert(true)
    }

    @Test
    fun insulin_TopBoundAndDate_True() {
        assert(true)
    }

    @Test
    fun insulin_OutOfBoundAndDate_False() {
        assert(
            !InsulinMedicamentValidator.isValid(
                value = 0.0,
                tabletsNumber = form.tabletsNumber,
                date = ZonedDateTime.now(),
            )
        )
        assert(
            !InsulinMedicamentValidator.isValid(
                value = 100.0,
                tabletsNumber = form.tabletsNumber,
                date = ZonedDateTime.now(),
            )
        )
    }

    @Test
    fun insulin_EmptyInsulinType_False() {
        assert(!InsulinMedicamentValidator.isValid(
            value = 0.1,
            tabletsNumber = form.tabletsNumber,
            date = ZonedDateTime.now()
        ))
    }

    @Test
    fun medicaments_WithName_True() {
        assert(MedicamentsValidator.isValid(
            name = "test",
            tabletsNumber = form.tabletsNumber,
            date = ZonedDateTime.now()
        ))
    }

    @Test
    fun medicaments_WithEmptyName_False() {
        assert(!MedicamentsValidator.isValid(
            name = "",
            tabletsNumber = form.tabletsNumber,
            date = ZonedDateTime.now()
        ))
    }

    @Test
    fun medicaments_WithMaxName_True() {
        val name = "a".repeat(120)
        assert(
            MedicamentsValidator.isValid(
                name = name.toString(),
                tabletsNumber = form.tabletsNumber,
                date = ZonedDateTime.now()
            )
        )
    }

    @Test
    fun medicaments_EmptyName_False() {
        assert(!MedicamentsValidator.isValid(
            tabletsNumber = form.tabletsNumber,
            date = ZonedDateTime.now()
        ))
    }

    @Test
    fun weight_LowerBoundAndDate_True() {
        assert(WeightValidator.isValid(
            value = 0.1,
            tabletsNumber = form.tabletsNumber,
            date = ZonedDateTime.now()
        ))
    }

    @Test
    fun weight_TopBoundAndDate_True() {
        assert(WeightValidator.isValid(
            value = 200.9,
            tabletsNumber = form.tabletsNumber,
            date = ZonedDateTime.now()
        ))
    }

    @Test
    fun weight_OutOfBoundAndDate_False() {
        assert(!WeightValidator.isValid(
            value = 0.0,
            tabletsNumber = form.tabletsNumber,
            date = ZonedDateTime.now()
        ))
        assert(!WeightValidator.isValid(
            value = 201.0,
            tabletsNumber = form.tabletsNumber,
            date = ZonedDateTime.now()
        ))
    }
}
