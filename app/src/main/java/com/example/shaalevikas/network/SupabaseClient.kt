package com.example.shaalevikas.network

import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.storage.Storage

/**
 * Singleton object to initialize and provide the Supabase client.
 */
object SupabaseClient {
    // Replace these with your actual Supabase project credentials
    private const val SUPABASE_URL = "https://diamotgulevmrtmvgjsz.supabase.co"
    private const val SUPABASE_ANON_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImRpYW1vdGd1bGV2bXJ0bXZnanN6Iiwicm9sZSI6ImFub24iLCJpYXQiOjE3Nzg2NzU4MzMsImV4cCI6MjA5NDI1MTgzM30.Pon81-ObDGLBmxiwBb5N0qymCWC1aXTDPhlJKnBCXTM"

    val client = createSupabaseClient(
        supabaseUrl = SUPABASE_URL,
        supabaseKey = SUPABASE_ANON_KEY
    ) {
        install(Storage)
    }
}
