package com.example.peers.features.forum

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await

/** Firestore-backed discussion forum. Reads fall back to bundled demo posts. */
class ForumRepository(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance(),
) {
    private val uid: String? get() = auth.currentUser?.uid

    private fun postsRef() = firestore.collection("forumPosts")

    /** Live posts, newest first. */
    fun observePosts(onChange: (List<ForumPost>) -> Unit): ListenerRegistration {
        val currentUid = uid
        return postsRef()
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .limit(80)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    android.util.Log.w("PeersForum", "posts listener failed", error)
                    return@addSnapshotListener
                }
                onChange(snapshot?.documents?.mapNotNull { it.toPost(currentUid) }.orEmpty())
            }
    }

    /** Live comments for one discussion. */
    fun observeComments(postId: String, onChange: (List<ForumComment>) -> Unit): ListenerRegistration {
        return postsRef().document(postId).collection("comments")
            .orderBy("createdAt", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    android.util.Log.w("PeersForum", "comments listener failed", error)
                    return@addSnapshotListener
                }
                onChange(snapshot?.documents?.mapNotNull { it.toComment(postId) }.orEmpty())
            }
    }

    suspend fun addPost(title: String, body: String, community: ForumCommunity): ForumPost {
        val user = auth.currentUser ?: error("Not signed in")
        val ref = postsRef().document()
        val post = ForumPost(
            id = ref.id,
            title = title,
            body = body,
            authorUid = user.uid,
            authorName = user.displayName?.takeIf { it.isNotBlank() } ?: "You",
            authorEmoji = "🧑🏽",
            communityId = community.id,
            communityName = community.name,
            upvoteCount = 0,
            commentCount = 0,
            createdAt = System.currentTimeMillis(),
        )
        ref.set(
            mapOf(
                "title" to post.title,
                "body" to post.body,
                "authorUid" to post.authorUid,
                "authorName" to post.authorName,
                "authorEmoji" to post.authorEmoji,
                "communityId" to post.communityId,
                "communityName" to post.communityName,
                "upvotedBy" to emptyList<String>(),
                "upvoteCount" to 0,
                "commentCount" to 0,
                "createdAt" to FieldValue.serverTimestamp(),
            )
        ).await()
        return post
    }

    /** Toggles the signed-in student's upvote and keeps the count in sync. */
    suspend fun toggleUpvote(postId: String) {
        val currentUid = uid ?: error("Not signed in")
        val ref = postsRef().document(postId)
        firestore.runTransaction { transaction ->
            val snapshot = transaction.get(ref)
            if (!snapshot.exists()) {
                val demo = ForumDemo.posts().firstOrNull { it.id == postId }
                if (demo != null) {
                    transaction.set(
                        ref,
                        mapOf(
                            "title" to demo.title,
                            "body" to demo.body,
                            "authorUid" to demo.authorUid,
                            "authorName" to demo.authorName,
                            "authorEmoji" to demo.authorEmoji,
                            "communityId" to demo.communityId,
                            "communityName" to demo.communityName,
                            "upvotedBy" to listOf(currentUid),
                            "upvoteCount" to demo.upvoteCount + 1,
                            "commentCount" to demo.commentCount,
                            "createdAt" to FieldValue.serverTimestamp(),
                        )
                    )
                }
                return@runTransaction
            }
            val voters = (snapshot.get("upvotedBy") as? List<*>)
                ?.filterIsInstance<String>()
                .orEmpty()
            val next = if (currentUid in voters) voters - currentUid else voters + currentUid
            transaction.update(
                ref,
                mapOf("upvotedBy" to next, "upvoteCount" to next.size),
            )
        }.await()
    }

    suspend fun addComment(post: ForumPost, body: String): ForumComment {
        val user = auth.currentUser ?: error("Not signed in")
        val comment = ForumComment(
            id = "",
            postId = post.id,
            authorUid = user.uid,
            authorName = user.displayName?.takeIf { it.isNotBlank() } ?: "You",
            authorEmoji = "🧑🏽",
            body = body,
            createdAt = System.currentTimeMillis(),
        )
        val postRef = postsRef().document(post.id)
        val commentRef = postRef.collection("comments").document()

        val postSnapshot = postRef.get().await()
        if (!postSnapshot.exists()) {
            postRef.set(
                mapOf(
                    "title" to post.title,
                    "body" to post.body,
                    "authorUid" to post.authorUid,
                    "authorName" to post.authorName,
                    "authorEmoji" to post.authorEmoji,
                    "communityId" to post.communityId,
                    "communityName" to post.communityName,
                    "upvotedBy" to emptyList<String>(),
                    "upvoteCount" to post.upvoteCount,
                    "commentCount" to 1,
                    "createdAt" to FieldValue.serverTimestamp(),
                )
            ).await()
        } else {
            postRef.update("commentCount", FieldValue.increment(1)).await()
        }

        commentRef.set(
            mapOf(
                "authorUid" to comment.authorUid,
                "authorName" to comment.authorName,
                "authorEmoji" to comment.authorEmoji,
                "body" to comment.body,
                "createdAt" to FieldValue.serverTimestamp(),
            )
        ).await()
        return comment.copy(id = commentRef.id)
    }

    suspend fun report(targetType: String, targetId: String, reason: ReportReason) {
        val currentUid = uid ?: error("Not signed in")
        firestore.collection("forumReports").add(
            mapOf(
                "targetType" to targetType,
                "targetId" to targetId,
                "reason" to reason.name,
                "reportedBy" to currentUid,
                "createdAt" to FieldValue.serverTimestamp(),
            )
        ).await()
    }

    private fun com.google.firebase.firestore.DocumentSnapshot.toPost(currentUid: String?): ForumPost? {
        val title = getString("title") ?: return null
        val voters = (get("upvotedBy") as? List<*>)
            ?.filterIsInstance<String>()
            .orEmpty()
        return ForumPost(
            id = id,
            title = title,
            body = getString("body").orEmpty(),
            authorUid = getString("authorUid").orEmpty(),
            authorName = getString("authorName").orEmpty().ifBlank { "A peer" },
            authorEmoji = getString("authorEmoji") ?: "🧑🏽",
            communityId = getString("communityId").orEmpty(),
            communityName = getString("communityName").orEmpty().ifBlank { "Community" },
            upvoteCount = (getLong("upvoteCount") ?: voters.size.toLong()).toInt(),
            commentCount = (getLong("commentCount") ?: 0L).toInt(),
            createdAt = getTimestamp("createdAt")?.toDate()?.time ?: System.currentTimeMillis(),
            upvotedByMe = currentUid != null && currentUid in voters,
        )
    }

    private fun com.google.firebase.firestore.DocumentSnapshot.toComment(postId: String): ForumComment =
        ForumComment(
            id = id,
            postId = postId,
            authorUid = getString("authorUid").orEmpty(),
            authorName = getString("authorName").orEmpty().ifBlank { "A peer" },
            authorEmoji = getString("authorEmoji") ?: "🧑🏽",
            body = getString("body").orEmpty(),
            createdAt = getTimestamp("createdAt")?.toDate()?.time ?: System.currentTimeMillis(),
        )
}
