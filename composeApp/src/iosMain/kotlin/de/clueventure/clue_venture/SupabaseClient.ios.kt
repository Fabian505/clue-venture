package de.clueventure.clue_venture

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest

actual val supabaseClient: SupabaseClient = createSupabaseClient(
    supabaseUrl = "https://havgcmueimjjqjtvzjpr.supabase.co",
    supabaseKey = "sb_publishable_dlXFI6T4et5Xqkn-gsyycw_LvDOqFJ_"
) {
    install(Postgrest)
}
