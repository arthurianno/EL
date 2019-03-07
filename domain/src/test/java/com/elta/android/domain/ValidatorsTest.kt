package com.elta.android.domain

import com.elta.android.domain.features.diary.events.model.InsulinType
import com.elta.android.domain.features.diary.events.model.form.ActivityValidator
import com.elta.android.domain.features.diary.events.model.form.BreadValidator
import com.elta.android.domain.features.diary.events.model.form.GlucoseValidator
import com.elta.android.domain.features.diary.events.model.form.InsulinValidator
import com.elta.android.domain.features.diary.events.model.form.MedicamentsValidator
import com.elta.android.domain.features.diary.events.model.form.WeightValidator
import org.junit.Test
import java.util.Date

class ValidatorsTest {

    @Test
    fun activity_WithDurationAndDate_False() {
        assert(!ActivityValidator.isValid(duration = 0L, date = Date()))
    }

    @Test
    fun activity_WithDurationAndDate_True() {
        assert(ActivityValidator.isValid(duration = 1L, date = Date()))
    }

    @Test
    fun bread_LowerBoundAndDate_True() {
        assert(BreadValidator.isValid(value = 0.1, date = Date()))
    }

    @Test
    fun bread_TopBoundAndDate_True() {
        assert(BreadValidator.isValid(value = 99.9, date = Date()))
    }

    @Test
    fun bread_OutOfBoundAndDate_False() {
        assert(!BreadValidator.isValid(value = 0.0, date = Date()))
        assert(!BreadValidator.isValid(value = 100.0, date = Date()))
    }

    @Test
    fun bread_WithKind_True() {
        assert(BreadValidator.isValid(value = 0.1, kind = "test", date = Date()))
    }

    @Test
    fun glucose_LowerBoundAndDate_True() {
        assert(GlucoseValidator.isValid(value = 0.1, date = Date()))
    }

    @Test
    fun glucose_TopBoundAndDate_True() {
        assert(GlucoseValidator.isValid(value = 65.0, date = Date()))
    }

    @Test
    fun glucose_OutOfBoundAndDate_False() {
        assert(!GlucoseValidator.isValid(value = 0.0, date = Date()))
        assert(!GlucoseValidator.isValid(value = 65.1, date = Date()))
    }

    @Test
    fun insulin_LowerBoundAndDate_True() {
        assert(InsulinValidator.isValid(value = 0.1, date = Date(), insulin = InsulinType.SHORT))
    }

    @Test
    fun insulin_TopBoundAndDate_True() {
        assert(InsulinValidator.isValid(value = 99.9, date = Date(), insulin = InsulinType.SHORT))
    }

    @Test
    fun insulin_OutOfBoundAndDate_False() {
        assert(!InsulinValidator.isValid(value = 0.0, date = Date(), insulin = InsulinType.SHORT))
        assert(!InsulinValidator.isValid(value = 100.0, date = Date(), insulin = InsulinType.SHORT))
    }

    @Test
    fun insulin_EmptyInsulinType_False() {
        assert(!InsulinValidator.isValid(value = 0.1, date = Date()))
    }

    @Test
    fun medicaments_WithName_True() {
        assert(MedicamentsValidator.isValid(name = "test", date = Date()))
    }

    @Test
    fun medicaments_EmptyName_False() {
        assert(!MedicamentsValidator.isValid(date = Date()))
    }

    @Test
    fun weight_LowerBoundAndDate_True() {
        assert(WeightValidator.isValid(value = 0.1, date = Date()))
    }

    @Test
    fun weight_TopBoundAndDate_True() {
        assert(WeightValidator.isValid(value = 200.9, date = Date()))
    }

    @Test
    fun weight_OutOfBoundAndDate_False() {
        assert(!WeightValidator.isValid(value = 0.0, date = Date()))
        assert(!WeightValidator.isValid(value = 201.0, date = Date()))
    }
}