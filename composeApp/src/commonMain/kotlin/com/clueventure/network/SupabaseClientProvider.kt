package com.clueventure.network

import com.clueventure.BuildConfig
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.realtime.Realtime

/**
 * Provides a singleton Supabase client configured from build-time secrets.
 *
 * The SUPABASE_URL and SUPABASE_ANON_KEY values are injected at compile time
 * from secrets.properties via BuildKonfig. Never commit secrets.properties to
 * version control – it is listed in .gitignore.
 */
object SupabaseClientProvider {

    val client: SupabaseClient by lazy {
        createSupabaseClient(
            supabaseUrl = BuildConfig.SUPABASE_URL,
            supabaseKey = BuildConfig.SUPABASE_ANON_KEY
        ) {
            install(Postgrest)
            install(Auth)
            install(Realtime)
        }
    }
}
