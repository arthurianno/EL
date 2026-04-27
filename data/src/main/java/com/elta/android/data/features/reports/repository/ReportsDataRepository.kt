package com.elta.android.data.features.reports.repository

import android.content.Context
import android.net.Uri
import com.elta.android.common.utils.CommonFormats
import com.elta.android.common.utils.toStringWithFormat
import com.elta.android.data.R
import com.elta.android.data.features.reports.datasource.ReportsDataSource
import com.elta.android.domain.features.reports.model.Range
import com.elta.android.domain.features.reports.repository.ReportsRepository
import com.elta.android.domain.features.user.model.Profile
import com.elta.android.domain.features.user.repository.ProfileRepository
import io.reactivex.Single
import javax.inject.Inject

class ReportsDataRepository @Inject constructor(
    private val context: Context,
    private val remoteDataSource: ReportsDataSource,
    private val profileRepository: ProfileRepository
) : ReportsRepository {

    override fun getReport(range: Range): Single<Uri> =
        profileRepository.getProfile()
            .flatMap { profile ->
                remoteDataSource.downloadReport(
                    startDate = range.start,
                    endDate = range.end,
                    glucoseFormat = profile.glucoseFormat,
                    fileName = buildFileName(profile, range)
                )
            }

    private fun buildFileName(profile: Profile, range: Range): String =
        context.getString(
            R.string.report_name_template,
            profile.createFullName(),
            range.start.toStringWithFormat(CommonFormats.FORMAT_SIMPLE_DATE),
            range.end.toStringWithFormat(CommonFormats.FORMAT_SIMPLE_DATE)
        )

    private fun Profile.createFullName(): String {
        return when {
            firstName.isNullOrEmpty() && secondName.isNullOrEmpty() -> email.orEmpty()
            firstName.isNullOrEmpty() -> secondName.orEmpty()
            secondName.isNullOrEmpty() -> firstName.orEmpty()
            else -> "$firstName $secondName"
        }
    }
}
