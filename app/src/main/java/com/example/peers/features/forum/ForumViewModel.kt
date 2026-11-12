package com.example.peers.features.forum

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject

class ForumViewModel @JvmOverloads constructor(
    application: Application,
    private val repository: ForumRepository = ForumRepository(),
) : AndroidViewModel(application) {

    private val prefs = application.getSharedPreferences("peers_forum_local", Context.MODE_PRIVATE)

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
                    authorEmoji = obj.optString("authorEmoji", "🧑🏽"),
                    communityId = obj.getString("communityId"),
                    communityName = obj.getString("communityName"),
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
                    put("authorEmoji", p.authorEmoji)
                    put("communityId", p.communityId)
                    put("communityName", p.communityName)
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

    private fun getLocalUpvotes(): Set<String> {
        return prefs.getStringSet("upvoted_post_ids", emptySet()) ?: emptySet()
    }

    private fun setLocalUpvotes(set: Set<String>) {
        prefs.edit().putStringSet("upvoted_post_ids", set).apply()
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
                    authorEmoji = obj.optString("authorEmoji", "🧑🏽"),
                    body = obj.getString("body"),
                    createdAt = obj.optLong("createdAt", System.currentTimeMillis()),
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
                    put("authorEmoji", c.authorEmoji)
                    put("body", c.body)
                    put("createdAt", c.createdAt)
                }
                array.put(obj)
            }
            prefs.edit().putString("comments_$postId", array.toString()).apply()
        }
    }

    private val demoPosts = ForumDemo.posts()

    private val _allPosts = MutableStateFlow(demoPosts)
    private var localPosts: List<ForumPost> = emptyList()
    private var remotePosts: List<ForumPost> = emptyList()
    private var postsListener: ListenerRegistration? = null

    private val _query = MutableStateFlow("")
    private val _communityId = MutableStateFlow<String?>(null)

    /** Filtered discussion feed. */
    val posts: StateFlow<List<ForumPost>> = combine(_allPosts, _query, _communityId) { posts, query, community ->
        posts.filter { post ->
            (community == null || post.communityId == community) &&
                (query.isBlank() ||
                    post.title.contains(query, ignoreCase = true) ||
                    post.body.contains(query, ignoreCase = true))
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = demoPosts,
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

        runCatching {
            postsListener = repository.observePosts { remote ->
                remotePosts = remote
                rebuildFeed()
            }
        }
    }

    private fun rebuildFeed() {
        val localUpvotes = getLocalUpvotes()
        val base = if (remotePosts.isNotEmpty()) remotePosts else demoPosts
        val combined = (localPosts + base).distinctBy { it.id }.map { post ->
            val hasUpvoted = post.id in localUpvotes || post.upvotedByMe
            if (hasUpvoted != post.upvotedByMe) {
                post.copy(
                    upvotedByMe = hasUpvoted,
                    upvoteCount = (post.upvoteCount + if (hasUpvoted) 1 else -1).coerceAtLeast(0)
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

    private fun startComments(postId: String) {
        commentsListener?.remove()
        demoComments = ForumDemo.comments(postId)
        val local = loadLocalComments(postId)
        _comments.value = demoComments + local
        runCatching {
            commentsListener = repository.observeComments(postId) { remote ->
                val base = if (remote.isEmpty()) demoComments else remote
                _comments.value = (base + loadLocalComments(postId)).distinctBy { it.id }
            }
        }
    }

    fun createPost(title: String, body: String, community: ForumCommunity) {
        if (title.isBlank() || body.isBlank()) return
        val user = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser
        val newPost = ForumPost(
            id = "post-${System.currentTimeMillis()}",
            title = title.trim(),
            body = body.trim(),
            authorUid = user?.uid.orEmpty(),
            authorName = user?.displayName?.takeIf { it.isNotBlank() } ?: "You",
            authorEmoji = "🧑🏽",
            communityId = community.id,
            communityName = community.name,
            upvoteCount = 0,
            commentCount = 0,
            createdAt = System.currentTimeMillis(),
        )
        localPosts = listOf(newPost) + localPosts
        saveLocalPosts(localPosts)
        rebuildFeed()
        _communityId.value = null
        _query.value = ""
        _uiEvents.tryEmit(ForumUiEvent.Message("Discussion posted in ${community.name}! 🚀"))

        viewModelScope.launch {
            runCatching { repository.addPost(title.trim(), body.trim(), community) }
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
        updatePost(optimistic)
        _uiEvents.tryEmit(ForumUiEvent.Message(if (upvoted) "Liked discussion 👍" else "Unliked"))

        viewModelScope.launch {
            runCatching { repository.toggleUpvote(postId) }
                .onFailure {
                    android.util.Log.w("PeersForum", "Upvote sync pending, saved locally", it)
                }
        }
    }

    private fun updatePost(updated: ForumPost) {
        _allPosts.value = _allPosts.value.map { if (it.id == updated.id) updated else it }
        localPosts = localPosts.map { if (it.id == updated.id) updated else it }
        saveLocalPosts(localPosts)
    }

    fun addComment(post: ForumPost, body: String) {
        if (body.isBlank()) return
        val user = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser
        val optimistic = ForumComment(
            id = "local-${System.currentTimeMillis()}",
            postId = post.id,
            authorUid = user?.uid.orEmpty(),
            authorName = user?.displayName?.takeIf { it.isNotBlank() } ?: "You",
            authorEmoji = "🧑🏽",
            body = body.trim(),
            createdAt = System.currentTimeMillis(),
        )
        val updatedComments = loadLocalComments(post.id) + optimistic
        saveLocalComments(post.id, updatedComments)
        _comments.value = _comments.value + optimistic
        updatePost(
            _allPosts.value.firstOrNull { it.id == post.id }
                ?.copy(commentCount = post.commentCount + 1)
                ?: post
        )
        _uiEvents.tryEmit(ForumUiEvent.Message("Reply posted! 💬"))

        viewModelScope.launch {
            runCatching { repository.addComment(post, body.trim()) }
                .onSuccess { saved ->
                    val refreshed = loadLocalComments(post.id).map {
                        if (it.id == optimistic.id) saved else it
                    }
                    saveLocalComments(post.id, refreshed)
                    _comments.value = _comments.value.map {
                        if (it.id == optimistic.id) saved else it
                    }
                }
                .onFailure {
                    android.util.Log.w("PeersForum", "Comment sync pending, saved locally", it)
                }
        }
    }

    fun reportPost(post: ForumPost, reason: ReportReason) {
        viewModelScope.launch {
            runCatching { repository.report("post", post.id, reason) }
                .onSuccess {
                    _uiEvents.emit(
                        ForumUiEvent.Message("Reported. Our moderators will take a look.")
                    )
                }
                .onFailure {
                    _uiEvents.emit(ForumUiEvent.Message("Couldn't send the report. Try again."))
                }
        }
    }

    fun reportComment(comment: ForumComment, reason: ReportReason) {
        viewModelScope.launch {
            runCatching { repository.report("comment", comment.id, reason) }
                .onSuccess {
                    _uiEvents.emit(
                        ForumUiEvent.Message("Reply reported. Thanks for keeping PEERS safe.")
                    )
                }
                .onFailure {
                    _uiEvents.emit(ForumUiEvent.Message("Couldn't send the report. Try again."))
                }
        }
    }

    override fun onCleared() {
        postsListener?.remove()
        commentsListener?.remove()
        super.onCleared()
    }
}
