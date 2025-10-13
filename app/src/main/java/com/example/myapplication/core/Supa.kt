@file:OptIn(io.github.jan.supabase.annotations.SupabaseInternal::class)
package com.example.myapplication.core

import com.example.myapplication.BuildConfig
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.gotrue.Auth
import io.github.jan.supabase.postgrest.Postgrest
// 如果你還沒用到即時更新，先不裝 Realtime，之後要再加：
import io.github.jan.supabase.realtime.Realtime

object Supa {

    // 用 lazy 確保只建立一次，避免 getter 每次呼叫都 new client
    val client: SupabaseClient by lazy {
        createSupabaseClient(
            supabaseUrl = BuildConfig.SUPABASE_URL,
            supabaseKey = BuildConfig.SUPABASE_ANON_KEY
        ) {
            install(Auth)
            install(Postgrest)
            install(Realtime)
        }
    }
}
