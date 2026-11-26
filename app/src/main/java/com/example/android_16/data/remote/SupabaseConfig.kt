package com.example.android_16.data.remote

object SupabaseConfig {
    const val SUPABASE_URL = "https://reotuqopygydelngxuys.supabase.co/"
    const val SUPABASE_KEY = "sb_publishable_1NOvVYXq3ML08I09lgdvyg_aEI066Ay"
    const val STORAGE_BUCKET = "evidencias"

    fun buildPublicFotoUrl(fileName: String): String {
        return "${SUPABASE_URL}storage/v1/object/public/$STORAGE_BUCKET/$fileName"
    }
}