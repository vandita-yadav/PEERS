package com.example.peers.features.admin

import android.content.Context
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

/**
 * Central role-based admin configuration.
 * Primary root administrators are pre-authorized and protected from removal;
 * additional co-admins can be managed from Firestore `config/admins`.
 */
object AdminConfig {
    val rootAdminEmails: Set<String> = setOf(
        "van22pop576@gmail.com",
        "tanyayadav2552005@gmail.com",
    )

    private const val PREFS_NAME = "peers_admin_config"
    private const val KEY_EXTRA_ADMINS = "extra_admin_emails"

    fun allAdmins(context: Context? = null): Set<String> {
        val extras = context?.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            ?.getStringSet(KEY_EXTRA_ADMINS, emptySet())
            ?.map { it.trim().lowercase() }
            ?.filter { it.isNotBlank() }
            ?.toSet()
            .orEmpty()
        return rootAdminEmails + extras
    }

    fun isAdmin(email: String?, context: Context? = null): Boolean {
        val normalized = email?.trim()?.lowercase().orEmpty()
        if (normalized.isBlank()) return false
        return normalized in allAdmins(context)
    }

    fun isRootAdmin(email: String?): Boolean {
        val normalized = email?.trim()?.lowercase().orEmpty()
        return normalized in rootAdminEmails
    }

    fun saveExtraAdminsLocal(context: Context, emails: Set<String>) {
        val cleaned = emails.map { it.trim().lowercase() }
            .filter { it.isNotBlank() && it !in rootAdminEmails }
            .toSet()
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putStringSet(KEY_EXTRA_ADMINS, cleaned)
            .apply()
    }

    suspend fun syncAdminsFromFirestore(
        context: Context,
        firestore: FirebaseFirestore = FirebaseFirestore.getInstance(),
    ): Set<String> {
        return runCatching {
            val doc = firestore.collection("config").document("admins").get().await()
            val remote = (doc.get("emails") as? List<*>)
                ?.filterIsInstance<String>()
                ?.map { it.trim().lowercase() }
                ?.filter { it.isNotBlank() }
                ?.toSet()
                .orEmpty()
            saveExtraAdminsLocal(context, remote)
            rootAdminEmails + remote
        }.getOrDefault(allAdmins(context))
    }
}
