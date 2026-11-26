package com.example.peers.features.forum

import android.app.Application
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.peers.features.offline.OfflineCacheRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.io.File

class ForumViewModel @JvmOverloads constructor(
    application: Application,
    private val repository: ForumRepository = ForumRepository(),
) : AndroidViewModel(application) {

    private val prefs = application.getSharedPreferences("peers_forum_local", Context.MODE_PRIVATE)
    private val auth = FirebaseAuth.getInstance()
    private val cache = OfflineCacheRepository.get(application)

    private val _university = MutableStateFlow(prefs.getString("last_university", "").orEmpty())
    val university: StateFlow<String> = _university.asStateFlow()

    val currentUid: String get() = auth.currentUser?.uid.orEmpty()

    private fun loadLocalPosts(): List<ForumPost> {
        val jsonStr = prefs.getString("created_posts", null) ?: return emptyList()
        return runCatching {
            val array = JSONArray(jsonStr)
            (0 until array.length()).map { idx ->
                val obj = array.getJSONObject(idx)
                ForumPost(
                    id = obj.getString("id"),
                    title = obj.getString("title"),
                    body = obj.getString("body"),
                    authorUid = obj.optString("authorUid", ""),
                    authorName = obj.optString("authorName", "You"),
                    authorEmoji = "",
                    communityId = obj.getString("communityId"),
                    communityName = obj.getString("communityName"),
                    university = obj.optString("university", ""),
                    upvoteCount = obj.optInt("upvoteCount", 0),
                    commentCount = obj.optInt("commentCount", 0),
                    createdAt = obj.optLong("createdAt", System.currentTimeMillis()),
                    upvotedByMe = obj.optBoolean("upvotedByMe", false),
                )
            }
        }.getOrDefault(emptyList())
    }

    private fun saveLocalPosts(posts: List<ForumPost>) {
        runCatching {
            val array = JSONArray()
            posts.forEach { p ->
                val obj = JSONObject().apply {
                    put("id", p.id)
                    put("title", p.title)
                    put("body", p.body)
                    put("authorUid", p.authorUid)
                    put("authorName", p.authorName)
                    put("authorEmoji", "")
                    put("communityId", p.communityId)
                    put("communityName", p.communityName)
                    put("university", p.university)
                    put("upvoteCount", p.upvoteCount)
                    put("commentCount", p.commentCount)
                    put("createdAt", p.createdAt)
                    put("upvotedByMe", p.upvotedByMe)
                }
                array.put(obj)
            }
            prefs.edit().putString("created_posts", array.toString()).apply()
        }
    }

    private fun getLocalUpvotes(): Set<String> =
        prefs.getStringSet("upvoted_post_ids", emptySet()) ?: emptySet()

    private fun setLocalUpvotes(set: Set<String>) {
        prefs.edit().putStringSet("upvoted_post_ids", set).apply()
    }

    private fun getDeletedIds(): Set<String> =
        prefs.getStringSet("deleted_post_ids", emptySet()) ?: emptySet()

    private fun markDeletedId(id: String) {
        val next = getDeletedIds() + id
        prefs.edit().putStringSet("deleted_post_ids", next).apply()
    }

    private fun getCommentVotes(): MutableMap<String, Int> {
        val raw = prefs.getString("comment_votes_map", null) ?: return mutableMapOf()
        return runCatching {
            val obj = JSONObject(raw)
            obj.keys().asSequence().associateWith { obj.getInt(it) }.toMutableMap()
        }.getOrDefault(mutableMapOf())
    }

    private fun saveCommentVote(commentId: String, vote: Int) {
        val map = getCommentVotes()
        if (vote == 0) map.remove(commentId) else map[commentId] = vote
        prefs.edit().putString("comment_votes_map", JSONObject(map as Map<*, *>).toString()).apply()
    }

    private fun loadLocalComments(postId: String): List<ForumComment> {
        val jsonStr = prefs.getString("comments_$postId", null) ?: return emptyList()
        return runCatching {
            val array = JSONArray(jsonStr)
            (0 until array.length()).map { idx ->
                val obj = array.getJSONObject(idx)
                ForumComment(
                    id = obj.getString("id"),
                    postId = obj.getString("postId"),
                    authorUid = obj.optString("authorUid", ""),
                    authorName = obj.optString("authorName", "You"),
                    authorEmoji = "",
                    body = obj.getString("body"),
                    createdAt = obj.optLong("createdAt", System.currentTimeMillis()),
                    upvoteCount = obj.optInt("upvoteCount", 0),
                    downvoteCount = obj.optInt("downvoteCount", 0),
                    myVote = obj.optInt("myVote", 0),
                    replyToId = obj.optString("replyToId", ""),
                    replyToAuthorName = obj.optString("replyToAuthorName", ""),
                    replyToSnippet = obj.optString("replyToSnippet", ""),
                    mediaUrl = obj.optString("mediaUrl", ""),
                )
            }
        }.getOrDefault(emptyList())
    }

    private fun saveLocalComments(postId: String, comments: List<ForumComment>) {
        runCatching {
            val array = JSONArray()
            comments.forEach { c ->
                val obj = JSONObject().apply {
                    put("id", c.id)
                    put("postId", c.postId)
                    put("authorUid", c.authorUid)
                    put("authorName", c.authorName)
                    put("authorEmoji", "")
                    put("body", c.body)
                    put("createdAt", c.createdAt)
                    put("upvoteCount", c.upvoteCount)
                    put("downvoteCount", c.downvoteCount)
                    put("myVote", c.myVote)
                    put("replyToId", c.replyToId)
                    put("replyToAuthorName", c.replyToAuthorName)
                    put("replyToSnippet", c.replyToSnippet)
                    put("mediaUrl", c.mediaUrl)
                }
                array.put(obj)
            }
            prefs.edit().putString("comments_$postId", array.toString()).apply()
        }
    }

    private val _allPosts = MutableStateFlow(ForumDemo.posts())
    private var localPosts: List<ForumPost> = emptyList()
    private var remotePosts: List<ForumPost> = emptyList()
    private var postsListener: ListenerRegistration? = null

    private val _query = MutableStateFlow("")
    private val _communityId = MutableStateFlow<String?>(null)

    /** Filtered discussion feed strictly scoped to the user's college/university. */
    val posts: StateFlow<List<ForumPost>> = combine(_allPosts, _query, _communityId) { posts, query, community ->
        val q = query.trim()
        posts.filter { post ->
            (community == null || post.communityId == community) &&
                (q.isBlank() ||
                    post.title.contains(q, ignoreCase = true) ||
                    post.body.contains(q, ignoreCase = true) ||
                    post.authorName.contains(q, ignoreCase = true) ||
                    post.communityName.contains(q, ignoreCase = true))
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = ForumDemo.posts(),
    )

    val query: StateFlow<String> = _query.asStateFlow()
    val communityId: StateFlow<String?> = _communityId.asStateFlow()
    val allPosts: StateFlow<List<ForumPost>> = _allPosts.asStateFlow()
    val communities: List<ForumCommunity> = ForumDemo.communities

    private val _selectedPostId = MutableStateFlow<String?>(null)
    val selectedPostId: StateFlow<String?> = _selectedPostId.asStateFlow()

    private val _comments = MutableStateFlow<List<ForumComment>>(emptyList())
    val comments: StateFlow<List<ForumComment>> = _comments.asStateFlow()

    private var commentsListener: ListenerRegistration? = null
    private var demoComments: List<ForumComment> = emptyList()

    private val _uiEvents = MutableSharedFlow<ForumUiEvent>(extraBufferCapacity = 4)
    val uiEvents = _uiEvents.asSharedFlow()

    init {
        localPosts = loadLocalPosts()
        rebuildFeed()

        viewModelScope.launch {
            val uid = auth.currentUser?.uid ?: return@launch
            val uni = runCatching {
                FirebaseFirestore.getInstance().collection("users").document(uid).get().await()
                    .getString("university").orEmpty()
            }.getOrNull().orEmpty()
            if (uni.isNotBlank()) {
                setUniversity(uni)
            }
        }

        runCatching {
            postsListener = repository.observePosts { remote ->
                remotePosts = remote
                rebuildFeed()
            }
        }
    }

    fun setUniversity(uni: String) {
        val trimmed = uni.trim()
        if (trimmed.isBlank() || trimmed == _university.value) return
        _university.value = trimmed
        prefs.edit().putString("last_university", trimmed).apply()
        rebuildFeed()
    }

    fun refreshDiscussions(onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            runCatching {
                remotePosts = repository.fetchPostsOnce()
                rebuildFeed()
            }
            val openId = _selectedPostId.value
            if (openId != null) {
                runCatching {
                    val fresh = repository.fetchCommentsOnce(openId)
                    mergeComments(openId, fresh)
                }
            }
            onComplete()
        }
    }

    private fun sameCollege(a: String, b: String): Boolean {
        if (b.isBlank()) return true
        return a.trim().equals(b.trim(), ignoreCase = true)
    }

    private fun rebuildFeed() {
        val activeCollege = _university.value.ifBlank { "Your Campus" }
        val localUpvotes = getLocalUpvotes()
        val deletedIds = getDeletedIds()

        val collegeLocal = localPosts.filter {
            it.id !in deletedIds && (it.university.isBlank() || sameCollege(it.university, activeCollege))
        }
        val collegeRemote = remotePosts.filter {
            it.id !in deletedIds && sameCollege(it.university, activeCollege)
        }
        val collegeDemos = ForumDemo.posts(university = activeCollege).filter { it.id !in deletedIds }

        val combined = (collegeLocal + collegeRemote + collegeDemos)
            .distinctBy { it.id }
            .map { post ->
                val hasUpvoted = post.id in localUpvotes || post.upvotedByMe
                if (hasUpvoted != post.upvotedByMe) {
                    post.copy(
                        upvotedByMe = hasUpvoted,
                        upvoteCount = (post.upvoteCount + if (hasUpvoted) 1 else -1).coerceAtLeast(0),
                    )
                } else {
                    post
                }
            }
        _allPosts.value = combined
    }

    fun setQuery(value: String) {
        _query.value = value
    }

    fun selectCommunity(id: String?) {
        _communityId.value = id
    }

    fun openPost(postId: String) {
        _selectedPostId.value = postId
        startComments(postId)
    }

    fun closePost() {
        commentsListener?.remove()
        commentsListener = null
        _selectedPostId.value = null
        _comments.value = emptyList()
    }

    private fun applyLocalVotes(list: List<ForumComment>): List<ForumComment> {
        val savedVotes = getCommentVotes()
        return list.map { c ->
            val saved = savedVotes[c.id]
            if (saved != null && saved != c.myVote) {
                val upDelta = (if (saved == 1) 1 else 0) - (if (c.myVote == 1) 1 else 0)
                val downDelta = (if (saved == -1) 1 else 0) - (if (c.myVote == -1) 1 else 0)
                c.copy(
                    myVote = saved,
                    upvoteCount = (c.upvoteCount + upDelta).coerceAtLeast(0),
                    downvoteCount = (c.downvoteCount + downDelta).coerceAtLeast(0),
                )
            } else c
        }
    }

    private fun mergeComments(postId: String, remote: List<ForumComment>) {
        val del = getDeletedIds()
        val base = (demoComments + remote).filter { it.id !in del }
        val loc = loadLocalComments(postId).filter { it.id !in del }
        val currentMap = _comments.value.associateBy { it.id }
        val merged = (loc + base).distinctBy { it.id }.map { item ->
            // preserve in-session local vote if same ID
            val inSession = currentMap[item.id]
            if (inSession != null && item.authorUid == currentUid && item.mediaUrl.isBlank() && inSession.mediaUrl.isNotBlank()) {
                item.copy(mediaUrl = inSession.mediaUrl)
            } else item
        }
        _comments.value = applyLocalVotes(merged).sortedBy { it.createdAt }
    }

    private fun startComments(postId: String) {
        commentsListener?.remove()
        demoComments = ForumDemo.comments(postId)
        val deletedIds = getDeletedIds()
        val local = loadLocalComments(postId).filter { it.id !in deletedIds }
        _comments.value = applyLocalVotes((demoComments + local).filter { it.id !in deletedIds }.distinctBy { it.id })
            .sortedBy { it.createdAt }

        runCatching {
            commentsListener = repository.observeComments(postId) { remote ->
                mergeComments(postId, remote)
            }
        }
    }

    fun createPost(title: String, body: String, community: ForumCommunity) {
        if (title.isBlank() || body.isBlank()) return
        val user = auth.currentUser
        val college = _university.value.ifBlank { "Your Campus" }
        val newPost = ForumPost(
            id = "post-${System.currentTimeMillis()}",
            title = title.trim(),
            body = body.trim(),
            authorUid = user?.uid.orEmpty(),
            authorName = user?.displayName?.takeIf { it.isNotBlank() } ?: "You",
            authorEmoji = "",
            communityId = community.id,
            communityName = community.name,
            university = college,
            upvoteCount = 0,
            commentCount = 0,
            createdAt = System.currentTimeMillis(),
        )
        localPosts = listOf(newPost) + localPosts
        saveLocalPosts(localPosts)
        rebuildFeed()
        _communityId.value = null
        _query.value = ""
        _uiEvents.tryEmit(ForumUiEvent.Message("Discussion posted in ${community.name}!"))

        viewModelScope.launch {
            cache.emitUnifiedNotification(
                title = "New Discussion in ${community.name}",
                body = "${newPost.title} — started in $college forum.",
                type = "forum",
                prefKey = "notif_forum",
            )
            runCatching { repository.addPost(title.trim(), body.trim(), community, college) }
                .onSuccess { saved ->
                    localPosts = localPosts.map { if (it.id == newPost.id) saved else it }
                    saveLocalPosts(localPosts)
                    rebuildFeed()
                }
                .onFailure {
                    android.util.Log.w("PeersForum", "Firestore post sync pending, saved locally", it)
                }
        }
    }

    fun editPost(post: ForumPost, newTitle: String, newBody: String) {
        val t = newTitle.trim()
        val b = newBody.trim()
        if (t.isBlank() || b.isBlank()) return

        val updated = post.copy(title = t, body = b)
        localPosts = (listOf(updated) + localPosts.filter { it.id != post.id })
        saveLocalPosts(localPosts)
        remotePosts = remotePosts.map { if (it.id == post.id) updated else it }
        rebuildFeed()
        _uiEvents.tryEmit(ForumUiEvent.Message("Discussion updated."))

        viewModelScope.launch {
            runCatching { repository.updatePost(post.id, t, b) }
        }
    }

    fun deletePost(post: ForumPost) {
        markDeletedId(post.id)
        localPosts = localPosts.filter { it.id != post.id }
        saveLocalPosts(localPosts)
        remotePosts = remotePosts.filter { it.id != post.id }
        if (_selectedPostId.value == post.id) closePost()
        rebuildFeed()
        _uiEvents.tryEmit(ForumUiEvent.Message("Discussion deleted."))

        viewModelScope.launch {
            runCatching { repository.deletePost(post.id) }
        }
    }

    fun toggleUpvote(postId: String) {
        val post = _allPosts.value.firstOrNull { it.id == postId } ?: return
        val upvoted = !post.upvotedByMe
        val upvotes = getLocalUpvotes().toMutableSet()
        if (upvoted) upvotes.add(postId) else upvotes.remove(postId)
        setLocalUpvotes(upvotes)

        val optimistic = post.copy(
            upvotedByMe = upvoted,
            upvoteCount = (post.upvoteCount + if (upvoted) 1 else -1).coerceAtLeast(0),
        )
        updatePostInState(optimistic)
        _uiEvents.tryEmit(ForumUiEvent.Message(if (upvoted) "Liked discussion" else "Unliked"))

        viewModelScope.launch {
            if (upvoted) {
                val me = auth.currentUser?.displayName?.takeIf { it.isNotBlank() } ?: "A peer"
                cache.emitUnifiedNotification(
                    title = "Discussion Liked",
                    body = "$me liked \"${post.title}\".",
                    type = "forum",
                    prefKey = "notif_forum",
                    targetUid = post.authorUid.takeIf { it.isNotBlank() && !it.startsWith("demo-") },
                )
            }
            runCatching { repository.toggleUpvote(postId) }
        }
    }

    private fun updatePostInState(updated: ForumPost) {
        _allPosts.value = _allPosts.value.map { if (it.id == updated.id) updated else it }
        localPosts = localPosts.map { if (it.id == updated.id) updated else it }
        remotePosts = remotePosts.map { if (it.id == updated.id) updated else it }
        saveLocalPosts(localPosts)
    }

    fun voteComment(post: ForumPost, comment: ForumComment, requestedVote: Int) {
        val nextVote = if (comment.myVote == requestedVote) 0 else requestedVote
        saveCommentVote(comment.id, nextVote)

        val upDelta = (if (nextVote == 1) 1 else 0) - (if (comment.myVote == 1) 1 else 0)
        val downDelta = (if (nextVote == -1) 1 else 0) - (if (comment.myVote == -1) 1 else 0)
        val updated = comment.copy(
            myVote = nextVote,
            upvoteCount = (comment.upvoteCount + upDelta).coerceAtLeast(0),
            downvoteCount = (comment.downvoteCount + downDelta).coerceAtLeast(0),
        )
        val loc = loadLocalComments(post.id).map { if (it.id == comment.id) updated else it }
        saveLocalComments(post.id, loc)
        _comments.value = _comments.value.map { if (it.id == comment.id) updated else it }

        viewModelScope.launch {
            if (nextVote == 1) {
                val me = auth.currentUser?.displayName?.takeIf { it.isNotBlank() } ?: "A peer"
                cache.emitUnifiedNotification(
                    title = "Response Upvoted",
                    body = "$me upvoted a response on \"${post.title}\".",
                    type = "forum",
                    prefKey = "notif_forum",
                    targetUid = comment.authorUid.takeIf { it.isNotBlank() && !it.startsWith("demo-") },
                )
            }
            runCatching { repository.voteComment(post.id, comment.id, nextVote) }
        }
    }

    fun addComment(
        post: ForumPost,
        body: String,
        replyTo: ForumComment? = null,
        mediaUrl: String = "",
    ) {
        if (body.isBlank() && mediaUrl.isBlank()) return
        val user = auth.currentUser
        val meName = user?.displayName?.takeIf { it.isNotBlank() } ?: "You"
        val cleanBody = body.trim().ifBlank { if (mediaUrl.isNotBlank()) "Shared an attachment" else "" }
        val snippet = replyTo?.body?.take(80).orEmpty()
        val optimistic = ForumComment(
            id = "local-${System.currentTimeMillis()}",
            postId = post.id,
            authorUid = user?.uid.orEmpty(),
            authorName = meName,
            authorEmoji = "",
            body = cleanBody,
            createdAt = System.currentTimeMillis(),
            replyToId = replyTo?.id.orEmpty(),
            replyToAuthorName = replyTo?.authorName.orEmpty(),
            replyToSnippet = snippet,
            mediaUrl = mediaUrl,
        )
        val updatedComments = loadLocalComments(post.id) + optimistic
        saveLocalComments(post.id, updatedComments)
        _comments.value = _comments.value + optimistic
        updatePostInState(
            _allPosts.value.firstOrNull { it.id == post.id }
                ?.copy(commentCount = post.commentCount + 1)
                ?: post
        )
        _uiEvents.tryEmit(ForumUiEvent.Message("Reply posted!"))

        viewModelScope.launch {
            if (replyTo != null) {
                cache.emitUnifiedNotification(
                    title = "Reply to your comment",
                    body = "$meName replied to ${replyTo.authorName}: \"$cleanBody\" on \"${post.title}\"",
                    type = "forum",
                    prefKey = "notif_forum",
                    targetUid = replyTo.authorUid.takeIf { it.isNotBlank() && !it.startsWith("demo-") },
                )
            } else {
                cache.emitUnifiedNotification(
                    title = "New Reply on Discussion",
                    body = "$meName commented on \"${post.title}\": \"$cleanBody\"",
                    type = "forum",
                    prefKey = "notif_forum",
                    targetUid = post.authorUid.takeIf { it.isNotBlank() && !it.startsWith("demo-") },
                )
            }
            runCatching {
                repository.addComment(
                    post = post,
                    body = cleanBody,
                    replyToId = replyTo?.id.orEmpty(),
                    replyToAuthorName = replyTo?.authorName.orEmpty(),
                    replyToSnippet = snippet,
                    mediaUrl = mediaUrl,
                )
            }.onSuccess { saved ->
                val resolvedSaved = if (saved.mediaUrl.isBlank() && mediaUrl.isNotBlank()) {
                    saved.copy(mediaUrl = mediaUrl)
                } else saved
                val refreshed = loadLocalComments(post.id).map {
                    if (it.id == optimistic.id) resolvedSaved else it
                }
                saveLocalComments(post.id, refreshed)
                _comments.value = _comments.value.map {
                    if (it.id == optimistic.id) resolvedSaved else it
                }
            }
        }
    }

    fun editComment(post: ForumPost, comment: ForumComment, newBody: String) {
        val trimmed = newBody.trim()
        if (trimmed.isBlank()) return
        val updated = comment.copy(body = trimmed)
        val updatedLocal = (listOf(updated) + loadLocalComments(post.id).filter { it.id != comment.id })
        saveLocalComments(post.id, updatedLocal)
        _comments.value = _comments.value.map { if (it.id == comment.id) updated else it }
        _uiEvents.tryEmit(ForumUiEvent.Message("Reply updated."))

        viewModelScope.launch {
            runCatching { repository.updateComment(post.id, comment.id, trimmed) }
        }
    }

    fun deleteComment(post: ForumPost, comment: ForumComment) {
        markDeletedId(comment.id)
        val updatedLocal = loadLocalComments(post.id).filter { it.id != comment.id }
        saveLocalComments(post.id, updatedLocal)
        _comments.value = _comments.value.filter { it.id != comment.id }
        updatePostInState(
            _allPosts.value.firstOrNull { it.id == post.id }
                ?.copy(commentCount = (post.commentCount - 1).coerceAtLeast(0))
                ?: post
        )
        _uiEvents.tryEmit(ForumUiEvent.Message("Reply deleted."))

        viewModelScope.launch {
            runCatching { repository.deleteComment(post.id, comment.id) }
        }
    }

    fun reportPost(post: ForumPost, reason: ReportReason) {
        viewModelScope.launch {
            runCatching { repository.report("post", post.id, reason) }
            _uiEvents.tryEmit(ForumUiEvent.Message("Reported — moderators will review it."))
        }
    }

    fun reportComment(comment: ForumComment, reason: ReportReason) {
        viewModelScope.launch {
            runCatching { repository.report("comment", comment.id, reason) }
            _uiEvents.tryEmit(ForumUiEvent.Message("Reply reported — thank you."))
        }
    }

    suspend fun prepareAttachmentUri(uri: Uri): String = withContext(Dispatchers.IO) {
        val ctx = getApplication<Application>()
        val mime = ctx.contentResolver.getType(uri).orEmpty().lowercase()
        val rawBytes = ctx.contentResolver.openInputStream(uri)?.use { it.readBytes() } ?: return@withContext ""
        if (mime.contains("gif")) {
            if (rawBytes.size <= 220_000) {
                return@withContext "data:image/gif;base64," + Base64.encodeToString(rawBytes, Base64.NO_WRAP)
            }
            val dir = File(ctx.filesDir, "forum_media").apply { mkdirs() }
            val out = File(dir, "gif-${System.currentTimeMillis()}.gif")
            out.writeBytes(rawBytes)
            return@withContext Uri.fromFile(out).toString()
        }

        val bitmap = BitmapFactory.decodeByteArray(rawBytes, 0, rawBytes.size)
            ?: return@withContext ""
        val maxDim = 720
        val scaled = if (bitmap.width > maxDim || bitmap.height > maxDim) {
            val ratio = minOf(maxDim.toFloat() / bitmap.width, maxDim.toFloat() / bitmap.height)
            Bitmap.createScaledBitmap(bitmap, (bitmap.width * ratio).toInt().coerceAtLeast(1), (bitmap.height * ratio).toInt().coerceAtLeast(1), true)
        } else bitmap
        val baos = ByteArrayOutputStream()
        scaled.compress(Bitmap.CompressFormat.JPEG, 76, baos)
        val jpegBytes = baos.toByteArray()
        if (jpegBytes.size <= 220_000) {
            return@withContext "data:image/jpeg;base64," + Base64.encodeToString(jpegBytes, Base64.NO_WRAP)
        }
        val dir = File(ctx.filesDir, "forum_media").apply { mkdirs() }
        val out = File(dir, "img-${System.currentTimeMillis()}.jpg")
        out.writeBytes(jpegBytes)
        return@withContext Uri.fromFile(out).toString()
    }

    override fun onCleared() {
        postsListener?.remove()
        commentsListener?.remove()
        super.onCleared()
    }
}
