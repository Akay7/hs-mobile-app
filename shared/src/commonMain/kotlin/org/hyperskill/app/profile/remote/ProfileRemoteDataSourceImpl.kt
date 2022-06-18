package org.hyperskill.app.profile.remote

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import org.hyperskill.app.profile.data.source.ProfileRemoteDataSource
import org.hyperskill.app.profile.domain.model.Profile
import org.hyperskill.app.profile.remote.model.ProfileResponse

class ProfileRemoteDataSourceImpl(
    private val httpClient: HttpClient
) : ProfileRemoteDataSource {
    override suspend fun getCurrentProfile(): Result<Profile> =
        kotlin.runCatching {
            httpClient
                .get("/api/profiles/current") {
                    header("Content-Type", "application/json")
                }.body<ProfileResponse>().profiles.first()
        }
}