package com.elta.android.presentation.utils

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.elta.android.domain.features.diary.events.model.ActivityType
import com.elta.android.domain.features.diary.events.model.EventType
import com.elta.android.domain.features.diary.events.model.GlucoseInputType
import com.elta.android.domain.features.diary.events.model.MealTag
import com.elta.android.domain.features.diary.tags.model.Tag
import com.elta.android.domain.features.diary.tags.model.TagImage
import com.elta.android.presentation.R
import com.nullgr.core.resources.ResourceProvider

@StringRes
fun EventType.toName(): Int =
    when (this) {
        is EventType.Bread -> R.string.event_type_bread
        EventType.Insulin -> R.string.event_type_insulin
        EventType.Medicaments -> R.string.event_type_medicaments
        EventType.Weight -> R.string.event_type_weight
        EventType.Activity -> R.string.event_type_activity
        is EventType.Glucose -> {
            if (this.inputType == GlucoseInputType.AUTO) R.string.event_type_glucose
            else R.string.events_form_screen_title_glucose_manual
        }
        EventType.Glycatedhemoglobin -> R.string.event_type_hba1c
    }

@StringRes
fun EventType.toEventDescriptionText(): Int =
    when (this) {
        is EventType.Bread -> R.string.event_type_bread
        EventType.Insulin -> R.string.event_type_insulin
        EventType.Medicaments -> R.string.event_type_medicaments
        EventType.Weight -> R.string.event_type_weight
        EventType.Activity -> R.string.event_type_activity
        is EventType.Glucose -> {
            when (this.inputType) {
                GlucoseInputType.AUTO, GlucoseInputType.GOOGLE_FIT -> R.string.event_type_glucose
                GlucoseInputType.MANUAL -> R.string.event_type_manual_glucose
            }
        }
        EventType.Glycatedhemoglobin -> R.string.event_type_hba1c
    }

@DrawableRes
fun EventType.toIcon(): Int =
    when (this) {
        is EventType.Bread -> R.drawable.ic_event_bread
        EventType.Insulin -> R.drawable.ic_event_insulin
        EventType.Medicaments -> R.drawable.ic_event_medicaments
        EventType.Weight -> R.drawable.ic_event_weight
        EventType.Activity -> R.drawable.ic_event_activity
        is EventType.Glucose -> R.drawable.ic_event_glucose
        else -> throw IllegalArgumentException("$this doesn't support icon resource.")
    }

@DrawableRes
fun EventType.toIconWithBg(): Int =
    when (this) {
        is EventType.Bread -> R.drawable.ic_event_bread_with_bg
        EventType.Insulin -> R.drawable.ic_event_insulin_with_bg
        EventType.Medicaments -> R.drawable.ic_event_medicaments_with_bg
        EventType.Weight -> R.drawable.ic_event_weight_with_bg
        EventType.Activity -> R.drawable.ic_event_activity_with_bg
        is EventType.Glucose -> R.drawable.ic_event_glucose_with_bg
        else -> throw IllegalArgumentException("$this doesn't support icon resource.")
    }

@DrawableRes
fun Tag?.toIcon(): Int =
    when (this) {
        null -> R.drawable.ic_tag_notag
        else -> image.toIcon()
    }

fun Tag?.toName(resources: ResourceProvider): String =
    when (this) {
        null -> resources.getString(R.string.tag_name_no_tag)
        else -> name
    }

@DrawableRes
fun TagImage.toIcon(): Int =
    when (this) {
        TagImage.BREAKFAST -> R.drawable.ic_tag_breakfast
        TagImage.LUNCH -> R.drawable.ic_tag_lunch
        TagImage.SNACK -> R.drawable.ic_tag_snack
        TagImage.DINNER -> R.drawable.ic_tag_dinner
        TagImage.WORK -> R.drawable.ic_tag_work
        TagImage.LEISURE -> R.drawable.ic_tag_leisure
        TagImage.TRAINING -> R.drawable.ic_tag_training
        TagImage.NIGHT -> R.drawable.ic_tag_night
        TagImage.SPECIALEVENT -> R.drawable.ic_tag_specialevent
    }

@StringRes
fun ActivityType.toName(): Int =
    when (this) {
        ActivityType.RUNNING -> R.string.activity_type_running
        ActivityType.WALKING -> R.string.activity_type_walking
        ActivityType.SWIMMING -> R.string.activity_type_swimming
        ActivityType.FITNESS -> R.string.activity_type_fitness
        ActivityType.CYCLING -> R.string.activity_type_cycling
        ActivityType.BADMINTON -> R.string.activity_type_badminton
        ActivityType.BASKETBALL -> R.string.activity_type_basketball
        ActivityType.CROSSCOUNTRYSKIING -> R.string.activity_type_crosscountryskiing
        ActivityType.SPORTCOMBATS -> R.string.activity_type_sportcombats
        ActivityType.BOX -> R.string.activity_type_box
        ActivityType.WRESTLING -> R.string.activity_type_wrestling
        ActivityType.WATERPOLO -> R.string.activity_type_waterpolo
        ActivityType.VOLLEYBALL -> R.string.activity_type_volleyball
        ActivityType.HANDBALL -> R.string.activity_type_handball
        ActivityType.GYMNASTICS -> R.string.activity_type_gymnastics
        ActivityType.GOLF -> R.string.activity_type_golf
        ActivityType.SKIING -> R.string.activity_type_skiing
        ActivityType.SKATING -> R.string.activity_type_skating
        ActivityType.ROLLERSKATING -> R.string.activity_type_rollerskating
        ActivityType.PINGPONG -> R.string.activity_type_pingpong
        ActivityType.BEACHVOLLEYBALL -> R.string.activity_type_beachvolleyball
        ActivityType.TRAMPOLINING -> R.string.activity_type_trampolining
        ActivityType.SNOWBOARDING -> R.string.activity_type_snowboarding
        ActivityType.PEDESTRIANISM -> R.string.activity_type_pedestrianism
        ActivityType.TENNIS -> R.string.activity_type_tennis
        ActivityType.TRIATHLON -> R.string.activity_type_triathlon
        ActivityType.WEIGHTLIFTING -> R.string.activity_type_weightlifting
        ActivityType.FENCING -> R.string.activity_type_Fencing
        ActivityType.FOOTBALL -> R.string.activity_type_football
        ActivityType.YOGA -> R.string.activity_type_yoga
        ActivityType.HOCKEY -> R.string.activity_type_hockey
        ActivityType.WHEELCHAIRRIDING -> R.string.activity_type_wheelchairriding
        ActivityType.HIKING -> R.string.activity_type_hiking
        ActivityType.NORDICWALKING -> R.string.activity_type_nordicwalking
        ActivityType.ROWING -> R.string.activity_type_rowing
        ActivityType.HOUSEKEEPING -> R.string.activity_type_housekeeping
        ActivityType.DANCING -> R.string.activity_type_dancing
        ActivityType.HORSEBACKRIDING -> R.string.activity_type_horsebackriding
        ActivityType.SHOOTING -> R.string.activity_type_shooting
        ActivityType.SKATEBOARDING -> R.string.activity_type_skateboarding
        ActivityType.SURFING -> R.string.activity_type_surfing
        ActivityType.MARTIALARTS -> R.string.activity_type_martialarts
        ActivityType.ANOTHER -> R.string.activity_type_another
    }

@DrawableRes
fun ActivityType.toIcon(): Int =
    when (this) {
        ActivityType.RUNNING -> R.drawable.ic_running
        ActivityType.WALKING -> R.drawable.ic_walking
        ActivityType.SWIMMING -> R.drawable.ic_swimming
        ActivityType.FITNESS -> R.drawable.ic_fitness
        ActivityType.CYCLING -> R.drawable.ic_cycling
        ActivityType.BADMINTON -> R.drawable.ic_badminton
        ActivityType.BASKETBALL -> R.drawable.ic_basketball
        ActivityType.CROSSCOUNTRYSKIING -> R.drawable.ic_cross_country_skiing
        ActivityType.SPORTCOMBATS -> R.drawable.ic_sport_combats
        ActivityType.BOX -> R.drawable.ic_box
        ActivityType.WRESTLING -> R.drawable.ic_wrestling
        ActivityType.WATERPOLO -> R.drawable.ic_water_polo
        ActivityType.VOLLEYBALL -> R.drawable.ic_volleyball
        ActivityType.HANDBALL -> R.drawable.ic_handball
        ActivityType.GYMNASTICS -> R.drawable.ic_gymnastics
        ActivityType.GOLF -> R.drawable.ic_golf
        ActivityType.SKIING -> R.drawable.ic_skiing
        ActivityType.SKATING -> R.drawable.ic_skating
        ActivityType.ROLLERSKATING -> R.drawable.ic_roller_skating
        ActivityType.PINGPONG -> R.drawable.ic_ping_pong
        ActivityType.BEACHVOLLEYBALL -> R.drawable.ic_beach_volleyball
        ActivityType.TRAMPOLINING -> R.drawable.ic_trampolining
        ActivityType.SNOWBOARDING -> R.drawable.ic_snowboarding
        ActivityType.PEDESTRIANISM -> R.drawable.ic_pedestrianism
        ActivityType.TENNIS -> R.drawable.ic_tennis
        ActivityType.TRIATHLON -> R.drawable.ic_triathlon
        ActivityType.WEIGHTLIFTING -> R.drawable.ic_weightlifting
        ActivityType.FENCING -> R.drawable.ic_fencing
        ActivityType.FOOTBALL -> R.drawable.ic_football
        ActivityType.YOGA -> R.drawable.ic_yoga
        ActivityType.HOCKEY -> R.drawable.ic_hockey
        ActivityType.WHEELCHAIRRIDING -> R.drawable.ic_wheelchair_riding
        ActivityType.HIKING -> R.drawable.ic_hiking
        ActivityType.NORDICWALKING -> R.drawable.ic_nordic_walking
        ActivityType.ROWING -> R.drawable.ic_rowing
        ActivityType.HOUSEKEEPING -> R.drawable.ic_house_keeping
        ActivityType.DANCING -> R.drawable.ic_dancing
        ActivityType.HORSEBACKRIDING -> R.drawable.ic_horseback_riding
        ActivityType.SHOOTING -> R.drawable.ic_shooting
        ActivityType.SKATEBOARDING -> R.drawable.ic_surfing
        ActivityType.SURFING -> R.drawable.ic_surfing
        ActivityType.MARTIALARTS -> R.drawable.ic_martial_arts
        ActivityType.ANOTHER -> R.drawable.ic_another
    }

@DrawableRes
fun MealTag.toIcon(): Int {
    return when (this) {
        MealTag.BEFOREMEAL -> R.drawable.img_red_apple
        MealTag.AFTERMEAL -> R.drawable.img_subtract_apple
        else -> R.drawable.img_red_apple
    }
}
