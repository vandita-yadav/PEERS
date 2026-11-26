package com.example.peers.features.forum

import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.Reply
import androidx.compose.material.icons.automirrored.rounded.Send
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.AddPhotoAlternate
import androidx.compose.material.icons.rounded.ChatBubbleOutline
import androidx.compose.material.icons.rounded.Clear
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.ThumbDown
import androidx.compose.material.icons.rounded.ThumbUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.ImageLoader
import coil.compose.AsyncImage
import coil.decode.GifDecoder
import coil.decode.ImageDecoderDecoder
import com.example.peers.R
import com.example.peers.features.dashboard.DashboardUser
import com.example.peers.features.dashboard.components.PeersSubScreenHeader
import com.example.peers.ui.theme.PeersBrandInk
import com.example.peers.ui.theme.PeersDrawableIcon
import com.example.peers.ui.theme.PeersLavender
import com.example.peers.ui.theme.PeersMint
import com.example.peers.ui.theme.PeersPrimaryIndigo
import com.example.peers.ui.theme.PeersSky
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ForumScreen(
    modifier: Modifier = Modifier,
    viewModel: ForumViewModel = viewModel(),
    user: DashboardUser? = null,
    unreadCount: Int = 0,
    onProfileClick: () -> Unit = {},
    onBellClick: () -> Unit = {},
    onBack: (() -> Unit)? = null,
) {
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val posts by viewModel.posts.collectAsStateWithLifecycle()
    val allPosts by viewModel.allPosts.collectAsStateWithLifecycle()
    val query by viewModel.query.collectAsStateWithLifecycle()
    val selectedCommunity by viewModel.communityId.collectAsStateWithLifecycle()
    val selectedPostId by viewModel.selectedPostId.collectAsStateWithLifecycle()
    val comments by viewModel.comments.collectAsStateWithLifecycle()
    val activeCollege by viewModel.university.collectAsStateWithLifecycle()

    val gifImageLoader = remember(context) {
        ImageLoader.Builder(context)
            .components {
                if (Build.VERSION.SDK_INT >= 28) add(ImageDecoderDecoder.Factory())
                else add(GifDecoder.Factory())
            }
            .build()
    }

    LaunchedEffect(user?.university) {
        val uni = user?.university.orEmpty()
        if (uni.isNotBlank()) viewModel.setUniversity(uni)
    }

    val currentUid = viewModel.currentUid
    val snackbarHostState = remember { SnackbarHostState() }
    var showComposer by remember { mutableStateOf(false) }
    var reportTarget by remember { mutableStateOf<ReportTarget?>(null) }
    var editingPost by remember { mutableStateOf<ForumPost?>(null) }
    var deletingPost by remember { mutableStateOf<ForumPost?>(null) }
    var editingComment by remember { mutableStateOf<ForumComment?>(null) }
    var deletingComment by remember { mutableStateOf<ForumComment?>(null) }

    LaunchedEffect(Unit) {
        viewModel.uiEvents.collect { event ->
            when (event) {
                is ForumUiEvent.Message -> snackbarHostState.showSnackbar(event.text)
            }
        }
    }

    val selectedPost = selectedPostId?.let { id -> allPosts.firstOrNull { it.id == id } }
    fun isMyItem(authorUid: String, id: String): Boolean =
        (currentUid.isNotBlank() && authorUid == currentUid) || id.startsWith("post-") || id.startsWith("local-")

    Box(
        modifier = modifier
            .fillMaxSize()
            .pointerInput(Unit) { detectTapGestures(onTap = { focusManager.clearFocus() }) },
    ) {
        when {
            showComposer -> {
                BackHandler { showComposer = false }
                NewPostScreen(
                    communities = viewModel.communities,
                    onBack = { showComposer = false },
                    onSubmit = { title, body, community ->
                        viewModel.createPost(title, body, community)
                        showComposer = false
                    },
                )
            }
            selectedPost != null -> {
                BackHandler { viewModel.closePost() }
                PostDetail(
                    post = selectedPost,
                    comments = comments,
                    imageLoader = gifImageLoader,
                    isMyPost = isMyItem(selectedPost.authorUid, selectedPost.id),
                    isMyComment = { c -> isMyItem(c.authorUid, c.id) },
                    onBack = viewModel::closePost,
                    onRefresh = { done -> viewModel.refreshDiscussions(done) },
                    onUpvote = { viewModel.toggleUpvote(selectedPost.id) },
                    onVoteComment = { c, vote -> viewModel.voteComment(selectedPost, c, vote) },
                    onAddComment = { body, replyTo, media ->
                        viewModel.addComment(selectedPost, body, replyTo, media)
                    },
                    onPrepareMedia = { uri -> viewModel.prepareAttachmentUri(uri) },
                    onEditPost = { editingPost = selectedPost },
                    onDeletePost = { deletingPost = selectedPost },
                    onEditComment = { c -> editingComment = c },
                    onDeleteComment = { c -> deletingComment = c },
                    onReportPost = { reportTarget = ReportTarget(post = selectedPost) },
                    onReportComment = { comment -> reportTarget = ReportTarget(comment = comment) },
                )
            }
            else -> {
                ForumFeed(
                    posts = posts,
                    collegeName = activeCollege.ifBlank { user?.university.orEmpty() }.ifBlank { "Your Campus" },
                    query = query,
                    selectedCommunity = selectedCommunity,
                    communities = viewModel.communities,
                    user = user,
                    unreadCount = unreadCount,
                    isMyPost = { p -> isMyItem(p.authorUid, p.id) },
                    onRefresh = { done -> viewModel.refreshDiscussions(done) },
                    onQueryChange = viewModel::setQuery,
                    onCommunitySelected = viewModel::selectCommunity,
                    onOpenPost = { viewModel.openPost(it.id) },
                    onUpvote = viewModel::toggleUpvote,
                    onEditPost = { editingPost = it },
                    onDeletePost = { deletingPost = it },
                    onReport = { post -> reportTarget = ReportTarget(post = post) },
                    onCompose = { showComposer = true },
                    onProfileClick = onProfileClick,
                    onBellClick = onBellClick,
                    onBack = onBack,
                )
            }
        }
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter).padding(16.dp),
        )
    }

    editingPost?.let { post ->
        var titleDraft by remember(post.id) { mutableStateOf(post.title) }
        var bodyDraft by remember(post.id) { mutableStateOf(post.body) }
        AlertDialog(
            onDismissRequest = { editingPost = null },
            title = { Text("Edit Discussion", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = titleDraft,
                        onValueChange = { titleDraft = it },
                        label = { Text("Title") },
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                    )
                    OutlinedTextField(
                        value = bodyDraft,
                        onValueChange = { bodyDraft = it },
                        label = { Text("Details") },
                        minLines = 3,
                        shape = RoundedCornerShape(14.dp),
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.editPost(post, titleDraft, bodyDraft)
                        editingPost = null
                    },
                    enabled = titleDraft.isNotBlank() && bodyDraft.isNotBlank(),
                ) { Text("Save", color = PeersPrimaryIndigo, fontWeight = FontWeight.Bold) }
            },
            dismissButton = {
                TextButton(onClick = { editingPost = null }) { Text("Cancel") }
            },
        )
    }

    deletingPost?.let { post ->
        AlertDialog(
            onDismissRequest = { deletingPost = null },
            title = { Text("Delete discussion?", fontWeight = FontWeight.Bold) },
            text = { Text("This will remove \"${post.title}\" from your campus forum.") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deletePost(post)
                    deletingPost = null
                }) { Text("Delete", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold) }
            },
            dismissButton = {
                TextButton(onClick = { deletingPost = null }) { Text("Cancel") }
            },
        )
    }

    editingComment?.let { comment ->
        val parentPost = selectedPost
        var replyDraft by remember(comment.id) { mutableStateOf(comment.body) }
        AlertDialog(
            onDismissRequest = { editingComment = null },
            title = { Text("Edit Reply", fontWeight = FontWeight.Bold) },
            text = {
                OutlinedTextField(
                    value = replyDraft,
                    onValueChange = { replyDraft = it },
                    label = { Text("Your reply") },
                    minLines = 3,
                    shape = RoundedCornerShape(14.dp),
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (parentPost != null) {
                            viewModel.editComment(parentPost, comment, replyDraft)
                        }
                        editingComment = null
                    },
                    enabled = replyDraft.isNotBlank(),
                ) { Text("Save", color = PeersPrimaryIndigo, fontWeight = FontWeight.Bold) }
            },
            dismissButton = {
                TextButton(onClick = { editingComment = null }) { Text("Cancel") }
            },
        )
    }

    deletingComment?.let { comment ->
        val parentPost = selectedPost
        AlertDialog(
            onDismissRequest = { deletingComment = null },
            title = { Text("Delete reply?", fontWeight = FontWeight.Bold) },
            text = { Text("Are you sure you want to remove this reply?") },
            confirmButton = {
                TextButton(onClick = {
                    if (parentPost != null) {
                        viewModel.deleteComment(parentPost, comment)
                    }
                    deletingComment = null
                }) { Text("Delete", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold) }
            },
            dismissButton = {
                TextButton(onClick = { deletingComment = null }) { Text("Cancel") }
            },
        )
    }

    reportTarget?.let { target ->
        ReportDialog(
            label = target.label,
            onDismiss = { reportTarget = null },
            onReport = { reason ->
                target.post?.let { viewModel.reportPost(it, reason) }
                target.comment?.let { viewModel.reportComment(it, reason) }
                reportTarget = null
            },
        )
    }
}

private data class ReportTarget(
    val post: ForumPost? = null,
    val comment: ForumComment? = null,
) {
    val label: String
        get() = when {
            post != null -> "discussion"
            comment != null -> "reply"
            else -> "content"
        }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ForumFeed(
    posts: List<ForumPost>,
    collegeName: String,
    query: String,
    selectedCommunity: String?,
    communities: List<ForumCommunity>,
    user: DashboardUser?,
    unreadCount: Int,
    isMyPost: (ForumPost) -> Boolean,
    onRefresh: (onComplete: () -> Unit) -> Unit,
    onQueryChange: (String) -> Unit,
    onCommunitySelected: (String?) -> Unit,
    onOpenPost: (ForumPost) -> Unit,
    onUpvote: (String) -> Unit,
    onEditPost: (ForumPost) -> Unit,
    onDeletePost: (ForumPost) -> Unit,
    onReport: (ForumPost) -> Unit,
    onCompose: () -> Unit,
    onProfileClick: () -> Unit,
    onBellClick: () -> Unit,
    onBack: (() -> Unit)? = null,
) {
    var isRefreshing by remember { mutableStateOf(false) }

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            PeersSubScreenHeader(
                title = "Discussion",
                subtitle = "$collegeName · Join other peers",
                user = user,
                unreadCount = unreadCount,
                onBack = onBack ?: {},
                onProfileClick = onProfileClick,
                onBellClick = onBellClick,
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
    ) { innerPadding ->
        PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = {
                isRefreshing = true
                onRefresh { isRefreshing = false }
            },
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            LazyColumn(
                contentPadding = PaddingValues(top = 10.dp, bottom = 28.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize(),
            ) {
                item {
                    ForumHero(
                        collegeName = collegeName,
                        postCount = posts.size,
                        categoryCount = communities.size,
                        onCompose = onCompose,
                    )
                }

                item {
                    OutlinedTextField(
                        value = query,
                        onValueChange = onQueryChange,
                        placeholder = { Text("Search discussion") },
                        leadingIcon = { Icon(Icons.Rounded.Search, contentDescription = null) },
                        trailingIcon = {
                            if (query.isNotEmpty()) {
                                IconButton(onClick = { onQueryChange("") }) {
                                    Icon(Icons.Rounded.Clear, contentDescription = "Clear search")
                                }
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(18.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp),
                    )
                }

                item {
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 20.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        item {
                            FilterChip(
                                selected = selectedCommunity == null,
                                onClick = { onCommunitySelected(null) },
                                label = { Text("All topics") },
                                colors = forumChipColors(),
                            )
                        }
                        items(communities, key = { it.id }) { community ->
                            FilterChip(
                                selected = selectedCommunity == community.id,
                                onClick = { onCommunitySelected(community.id) },
                                label = { Text(community.name) },
                                colors = forumChipColors(),
                            )
                        }
                    }
                }

                item {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 2.dp),
                    ) {
                        Text(
                            if (selectedCommunity == null) "Latest discussions" else "In this category",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.weight(1f),
                        )
                        Text(
                            "${posts.size} threads",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }

                if (posts.isEmpty()) {
                    item {
                        Surface(
                            shape = RoundedCornerShape(22.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f),
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.padding(26.dp),
                            ) {
                                PeersDrawableIcon(
                                    lightRes = R.drawable.community_black,
                                    darkRes = R.drawable.community_white,
                                    contentDescription = "No discussions",
                                    size = 40.dp,
                                )
                                Text(
                                    "No discussions here yet",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(top = 8.dp),
                                )
                                Text(
                                    "Be the first to start one — your question helps the next student too.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                                TextButton(onClick = onCompose) {
                                    Text("Start a discussion", color = PeersPrimaryIndigo)
                                }
                            }
                        }
                    }
                } else {
                    items(posts, key = { it.id }) { post ->
                        PostCard(
                            post = post,
                            isMine = isMyPost(post),
                            onOpen = { onOpenPost(post) },
                            onUpvote = { onUpvote(post.id) },
                            onEdit = { onEditPost(post) },
                            onDelete = { onDeletePost(post) },
                            onReport = { onReport(post) },
                            modifier = Modifier.padding(horizontal = 20.dp),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun forumChipColors() = FilterChipDefaults.filterChipColors(
    selectedContainerColor = PeersPrimaryIndigo,
    selectedLabelColor = Color.White,
)

@Composable
private fun ForumHero(collegeName: String, postCount: Int, categoryCount: Int, onCompose: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .clip(RoundedCornerShape(28.dp))
            .background(
                Brush.linearGradient(
                    listOf(PeersPrimaryIndigo, Color(0xFF8C7BF3), PeersLavender)
                )
            )
            .padding(20.dp),
    ) {
        Column {
            Surface(
                shape = RoundedCornerShape(50),
                color = Color.White.copy(alpha = 0.2f),
            ) {
                Text(
                    "$collegeName · College-Only Forum",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                )
            }
            Text(
                "Your campus, in conversation",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier.padding(top = 8.dp),
            )
            Text(
                "Discussions are scoped specifically to $collegeName peers.",
                style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(alpha = 0.85f),
                modifier = Modifier.padding(top = 4.dp),
            )
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 16.dp)) {
                HeroStat("$postCount", "threads", Modifier.weight(1f))
                HeroStat("$categoryCount", "categories", Modifier.weight(1f))
                Button(
                    onClick = onCompose,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White,
                        contentColor = PeersPrimaryIndigo,
                    ),
                    shape = RoundedCornerShape(14.dp),
                ) {
                    Icon(Icons.Rounded.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                    Text(" Ask", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun HeroStat(value: String, label: String, modifier: Modifier = Modifier) {
    Column(modifier) {
        Text(
            value,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = Color.White,
        )
        Text(
            label,
            style = MaterialTheme.typography.labelSmall,
            color = Color.White.copy(alpha = 0.75f),
        )
    }
}

@Composable
private fun PostCard(
    post: ForumPost,
    isMine: Boolean,
    onOpen: () -> Unit,
    onUpvote: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onReport: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var menuOpen by remember { mutableStateOf(false) }
    Surface(
        shape = RoundedCornerShape(22.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 1.dp,
        shadowElevation = 2.dp,
        modifier = modifier.fillMaxWidth().clickable(onClick = onOpen),
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = RoundedCornerShape(50),
                    color = PeersLavender.copy(alpha = 0.45f),
                ) {
                    Text(
                        post.communityName,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = PeersBrandInk,
                        modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp),
                    )
                }
                Text(
                    " · ${relativeTime(post.createdAt)}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f),
                )
                Box {
                    IconButton(onClick = { menuOpen = true }, modifier = Modifier.size(28.dp)) {
                        Icon(
                            Icons.Rounded.MoreVert,
                            contentDescription = "Options",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp),
                        )
                    }
                    DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }) {
                        if (isMine) {
                            DropdownMenuItem(
                                text = { Text("Edit discussion") },
                                onClick = {
                                    menuOpen = false
                                    onEdit()
                                },
                            )
                            DropdownMenuItem(
                                text = { Text("Delete discussion", color = MaterialTheme.colorScheme.error) },
                                onClick = {
                                    menuOpen = false
                                    onDelete()
                                },
                            )
                        } else {
                            DropdownMenuItem(
                                text = { Text("Report discussion") },
                                onClick = {
                                    menuOpen = false
                                    onReport()
                                },
                            )
                        }
                    }
                }
            }

            Text(
                post.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 8.dp),
            )
            Text(
                post.body,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(top = 4.dp),
            )

            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 12.dp)) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(30.dp)
                        .clip(CircleShape)
                        .background(PeersSky.copy(alpha = 0.6f)),
                ) {
                    Text(
                        post.authorName.firstOrNull()?.uppercase() ?: "P",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = PeersBrandInk,
                    )
                }
                Text(
                    post.authorName,
                    style = MaterialTheme.typography.labelMedium,
                    modifier = Modifier.weight(1f).padding(start = 8.dp),
                )
                UpvotePill(
                    count = post.upvoteCount,
                    selected = post.upvotedByMe,
                    onClick = onUpvote,
                )
                Spacer(Modifier.width(12.dp))
                Icon(
                    Icons.Rounded.ChatBubbleOutline,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(16.dp),
                )
                Text(
                    " ${post.commentCount}",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun UpvotePill(count: Int, selected: Boolean, onClick: () -> Unit) {
    Surface(
        shape = RoundedCornerShape(50),
        color = if (selected) PeersPrimaryIndigo else MaterialTheme.colorScheme.surfaceVariant,
        modifier = Modifier.clickable(onClick = onClick),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
        ) {
            Icon(
                Icons.Rounded.ThumbUp,
                contentDescription = "Upvote",
                tint = if (selected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(14.dp),
            )
            Text(
                " $count",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = if (selected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NewPostScreen(
    communities: List<ForumCommunity>,
    onBack: () -> Unit,
    onSubmit: (String, String, ForumCommunity) -> Unit,
) {
    val focusManager = LocalFocusManager.current
    var title by rememberSaveable { mutableStateOf("") }
    var body by rememberSaveable { mutableStateOf("") }
    var communityId by rememberSaveable { mutableStateOf(communities.first().id) }
    val community = communities.firstOrNull { it.id == communityId } ?: communities.first()
    val valid = title.isNotBlank() && body.isNotBlank()

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            TopAppBar(
                title = { Text("Start a Discussion", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = {
                        focusManager.clearFocus()
                        onBack()
                    }) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background),
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .imePadding()
                .navigationBarsPadding()
                .pointerInput(Unit) { detectTapGestures(onTap = { focusManager.clearFocus() }) }
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 22.dp, vertical = 12.dp),
        ) {
            Text(
                "Real questions get real answers. Choose a topic category for your campus.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Question or title") },
                placeholder = { Text("How do I open a student bank account nearby?") },
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
            )
            OutlinedTextField(
                value = body,
                onValueChange = { body = it },
                label = { Text("Details") },
                placeholder = { Text("Share what you've tried or what you need to know…") },
                minLines = 5,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
            )
            Text(
                "Category",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(top = 16.dp, bottom = 6.dp),
            )
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(communities, key = { it.id }) { option ->
                    FilterChip(
                        selected = community.id == option.id,
                        onClick = { communityId = option.id },
                        label = { Text(option.name) },
                        colors = forumChipColors(),
                    )
                }
            }
            Button(
                onClick = {
                    focusManager.clearFocus()
                    onSubmit(title, body, community)
                },
                enabled = valid,
                colors = ButtonDefaults.buttonColors(containerColor = PeersPrimaryIndigo),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth().padding(top = 24.dp, bottom = 16.dp).height(52.dp),
            ) {
                Text("Publish discussion", fontWeight = FontWeight.Bold, color = Color.White)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PostDetail(
    post: ForumPost,
    comments: List<ForumComment>,
    imageLoader: ImageLoader,
    isMyPost: Boolean,
    isMyComment: (ForumComment) -> Boolean,
    onBack: () -> Unit,
    onRefresh: (onComplete: () -> Unit) -> Unit,
    onUpvote: () -> Unit,
    onVoteComment: (ForumComment, Int) -> Unit,
    onAddComment: (String, ForumComment?, String) -> Unit,
    onPrepareMedia: suspend (android.net.Uri) -> String,
    onEditPost: () -> Unit,
    onDeletePost: () -> Unit,
    onEditComment: (ForumComment) -> Unit,
    onDeleteComment: (ForumComment) -> Unit,
    onReportPost: () -> Unit,
    onReportComment: (ForumComment) -> Unit,
) {
    val focusManager = LocalFocusManager.current
    val keyboard = LocalSoftwareKeyboardController.current
    val scope = rememberCoroutineScope()
    var reply by rememberSaveable(post.id) { mutableStateOf("") }
    var mediaUrl by rememberSaveable(post.id) { mutableStateOf("") }
    var replyingTo by remember { mutableStateOf<ForumComment?>(null) }
    var topMenuOpen by remember { mutableStateOf(false) }
    var isRefreshing by remember { mutableStateOf(false) }

    val mediaPicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) {
            scope.launch {
                val prepared = onPrepareMedia(uri)
                if (prepared.isNotBlank()) mediaUrl = prepared
            }
        }
    }

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            TopAppBar(
                title = { Text("Discussion", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    Box {
                        IconButton(onClick = { topMenuOpen = true }) {
                            Icon(Icons.Rounded.MoreVert, contentDescription = "Post actions")
                        }
                        DropdownMenu(expanded = topMenuOpen, onDismissRequest = { topMenuOpen = false }) {
                            if (isMyPost) {
                                DropdownMenuItem(
                                    text = { Text("Edit discussion") },
                                    onClick = { topMenuOpen = false; onEditPost() },
                                )
                                DropdownMenuItem(
                                    text = { Text("Delete discussion", color = MaterialTheme.colorScheme.error) },
                                    onClick = { topMenuOpen = false; onDeletePost() },
                                )
                            } else {
                                DropdownMenuItem(
                                    text = { Text("Report discussion") },
                                    onClick = { topMenuOpen = false; onReportPost() },
                                )
                            }
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                ),
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .pointerInput(Unit) { detectTapGestures(onTap = { focusManager.clearFocus() }) },
        ) {
            PullToRefreshBox(
                isRefreshing = isRefreshing,
                onRefresh = {
                    isRefreshing = true
                    onRefresh { isRefreshing = false }
                },
                modifier = Modifier.weight(1f),
            ) {
                LazyColumn(
                    contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 8.dp, bottom = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize(),
                ) {
                    item {
                        Text(
                            post.communityName,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = PeersPrimaryIndigo,
                        )
                        Text(
                            post.title,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(top = 6.dp),
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(top = 10.dp),
                        ) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(PeersSky.copy(alpha = 0.6f)),
                            ) {
                                Text(
                                    post.authorName.firstOrNull()?.uppercase() ?: "P",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = PeersBrandInk,
                                )
                            }
                            Column(Modifier.padding(start = 10.dp)) {
                                Text(
                                    post.authorName,
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.SemiBold,
                                )
                                Text(
                                    relativeTime(post.createdAt),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }
                        Text(
                            post.body,
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.padding(top = 14.dp),
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(top = 16.dp),
                        ) {
                            UpvotePill(
                                count = post.upvoteCount,
                                selected = post.upvotedByMe,
                                onClick = onUpvote,
                            )
                            Text(
                                "  ${comments.size} replies",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }

                    item {
                        Text(
                            "Replies",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(top = 12.dp),
                        )
                    }

                    if (comments.isEmpty()) {
                        item {
                            Text(
                                "No replies yet. Be the first to help.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    } else {
                        items(comments, key = { it.id }) { comment ->
                            CommentCard(
                                comment = comment,
                                imageLoader = imageLoader,
                                isMine = isMyComment(comment),
                                onUpvote = { onVoteComment(comment, 1) },
                                onDownvote = { onVoteComment(comment, -1) },
                                onReplyTo = { replyingTo = comment },
                                onEdit = { onEditComment(comment) },
                                onDelete = { onDeleteComment(comment) },
                                onReport = { onReportComment(comment) },
                            )
                        }
                    }
                }
            }

            ReplyBar(
                value = reply,
                replyingTo = replyingTo,
                mediaUrl = mediaUrl,
                imageLoader = imageLoader,
                onCancelReplyTo = { replyingTo = null },
                onPickMedia = { mediaPicker.launch("image/*") },
                onClearMedia = { mediaUrl = "" },
                onValueChange = { reply = it },
                onSend = {
                    val text = reply.trim()
                    if (text.isNotEmpty() || mediaUrl.isNotBlank()) {
                        onAddComment(text, replyingTo, mediaUrl)
                        reply = ""
                        replyingTo = null
                        mediaUrl = ""
                        keyboard?.hide()
                        focusManager.clearFocus()
                    }
                },
            )
        }
    }
}

@Composable
private fun CommentCard(
    comment: ForumComment,
    imageLoader: ImageLoader,
    isMine: Boolean,
    onUpvote: () -> Unit,
    onDownvote: () -> Unit,
    onReplyTo: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onReport: () -> Unit,
) {
    var menuOpen by remember { mutableStateOf(false) }
    Surface(
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 1.dp,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(PeersMint),
                ) {
                    Text(
                        comment.authorName.firstOrNull()?.uppercase() ?: "P",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = PeersBrandInk,
                    )
                }
                Text(
                    comment.authorName,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1f).padding(start = 8.dp),
                )
                Text(
                    relativeTime(comment.createdAt),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Box {
                    IconButton(onClick = { menuOpen = true }, modifier = Modifier.size(26.dp)) {
                        Icon(
                            Icons.Rounded.MoreVert,
                            contentDescription = "Reply options",
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }) {
                        if (isMine) {
                            DropdownMenuItem(
                                text = { Text("Edit reply") },
                                onClick = {
                                    menuOpen = false
                                    onEdit()
                                },
                            )
                            DropdownMenuItem(
                                text = { Text("Delete reply", color = MaterialTheme.colorScheme.error) },
                                onClick = {
                                    menuOpen = false
                                    onDelete()
                                },
                            )
                        } else {
                            DropdownMenuItem(
                                text = { Text("Report reply") },
                                onClick = {
                                    menuOpen = false
                                    onReport()
                                },
                            )
                        }
                    }
                }
            }

            if (comment.replyToAuthorName.isNotBlank()) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = PeersPrimaryIndigo.copy(alpha = 0.08f),
                    border = BorderStroke(1.dp, PeersPrimaryIndigo.copy(alpha = 0.25f)),
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                ) {
                    Column(Modifier.padding(horizontal = 10.dp, vertical = 6.dp)) {
                        Text(
                            "Replying to ${comment.replyToAuthorName}",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = PeersPrimaryIndigo,
                        )
                        if (comment.replyToSnippet.isNotBlank()) {
                            Text(
                                comment.replyToSnippet,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                    }
                }
            }

            Text(
                comment.body,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = 6.dp),
            )

            if (comment.mediaUrl.isNotBlank()) {
                Spacer(Modifier.height(8.dp))
                AsyncImage(
                    model = comment.mediaUrl,
                    imageLoader = imageLoader,
                    contentDescription = "Reply image or GIF",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(170.dp)
                        .clip(RoundedCornerShape(12.dp)),
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(top = 10.dp),
            ) {
                Surface(
                    shape = RoundedCornerShape(50),
                    color = if (comment.myVote == 1) PeersPrimaryIndigo else MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.clickable(onClick = onUpvote),
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                    ) {
                        Icon(
                            Icons.Rounded.ThumbUp,
                            contentDescription = "Upvote reply",
                            tint = if (comment.myVote == 1) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(13.dp),
                        )
                        Text(
                            " ${comment.upvoteCount}",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = if (comment.myVote == 1) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
                Surface(
                    shape = RoundedCornerShape(50),
                    color = if (comment.myVote == -1) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.clickable(onClick = onDownvote),
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                    ) {
                        Icon(
                            Icons.Rounded.ThumbDown,
                            contentDescription = "Downvote reply",
                            tint = if (comment.myVote == -1) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(13.dp),
                        )
                        Text(
                            " ${comment.downvoteCount}",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = if (comment.myVote == -1) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
                Spacer(Modifier.weight(1f))
                TextButton(
                    onClick = onReplyTo,
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                ) {
                    Icon(Icons.AutoMirrored.Rounded.Reply, contentDescription = null, modifier = Modifier.size(15.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Reply", style = MaterialTheme.typography.labelSmall, color = PeersPrimaryIndigo, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

@Composable
private fun ReplyBar(
    value: String,
    replyingTo: ForumComment?,
    mediaUrl: String,
    imageLoader: ImageLoader,
    onCancelReplyTo: () -> Unit,
    onPickMedia: () -> Unit,
    onClearMedia: () -> Unit,
    onValueChange: (String) -> Unit,
    onSend: () -> Unit,
) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 6.dp,
        shadowElevation = 6.dp,
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .imePadding(),
    ) {
        Column(Modifier.fillMaxWidth()) {
            if (replyingTo != null) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(PeersPrimaryIndigo.copy(alpha = 0.08f))
                        .padding(horizontal = 14.dp, vertical = 6.dp),
                ) {
                    Column(Modifier.weight(1f)) {
                        Text(
                            "Replying to ${replyingTo.authorName}",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = PeersPrimaryIndigo,
                        )
                        Text(
                            replyingTo.body,
                            style = MaterialTheme.typography.bodySmall,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    IconButton(onClick = onCancelReplyTo, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Rounded.Close, contentDescription = "Cancel reply", modifier = Modifier.size(16.dp))
                    }
                }
            }
            if (mediaUrl.isNotBlank()) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                ) {
                    AsyncImage(
                        model = mediaUrl,
                        imageLoader = imageLoader,
                        contentDescription = "Selected attachment",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.size(56.dp).clip(RoundedCornerShape(10.dp)),
                    )
                    Spacer(Modifier.width(8.dp))
                    Text("Image/GIF attached", style = MaterialTheme.typography.labelSmall, color = PeersPrimaryIndigo, modifier = Modifier.weight(1f))
                    IconButton(onClick = onClearMedia, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Rounded.Close, contentDescription = "Remove attachment", modifier = Modifier.size(16.dp))
                    }
                }
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
            ) {
                IconButton(onClick = onPickMedia) {
                    Icon(
                        Icons.Rounded.AddPhotoAlternate,
                        contentDescription = "Attach Photo or GIF",
                        tint = PeersPrimaryIndigo,
                    )
                }
                OutlinedTextField(
                    value = value,
                    onValueChange = onValueChange,
                    placeholder = { Text("Write a helpful reply…") },
                    maxLines = 3,
                    shape = RoundedCornerShape(18.dp),
                    modifier = Modifier.weight(1f),
                )
                IconButton(
                    onClick = onSend,
                    enabled = value.isNotBlank() || mediaUrl.isNotBlank(),
                    modifier = Modifier.padding(start = 4.dp),
                ) {
                    Icon(
                        Icons.AutoMirrored.Rounded.Send,
                        contentDescription = "Send reply",
                        tint = if (value.isNotBlank() || mediaUrl.isNotBlank()) PeersPrimaryIndigo
                        else MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

@Composable
private fun ReportDialog(label: String, onDismiss: () -> Unit, onReport: (ReportReason) -> Unit) {
    var selected by remember { mutableStateOf<ReportReason?>(null) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Report this $label?") },
        text = {
            Column {
                Text(
                    "Reports are private. Choose the closest reason so moderators can act quickly.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                ReportReason.entries.forEach { reason ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selected = reason }
                            .padding(vertical = 10.dp),
                    ) {
                        RadioButton(
                            selected = selected == reason,
                            onClick = { selected = reason },
                        )
                        Text(reason.label, modifier = Modifier.padding(start = 4.dp))
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { selected?.let(onReport) },
                enabled = selected != null,
            ) { Text("Send report", color = MaterialTheme.colorScheme.error) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        },
    )
}

private fun relativeTime(timestamp: Long): String {
    val diff = (System.currentTimeMillis() - timestamp).coerceAtLeast(0)
    val minutes = TimeUnit.MILLISECONDS.toMinutes(diff)
    return when {
        minutes < 1 -> "just now"
        minutes < 60 -> "${minutes}m ago"
        minutes < 24 * 60 -> "${TimeUnit.MINUTES.toHours(minutes)}h ago"
        minutes < 7 * 24 * 60 -> "${TimeUnit.MINUTES.toDays(minutes)}d ago"
        else -> {
            val date = java.time.Instant.ofEpochMilli(timestamp)
                .atZone(java.time.ZoneId.systemDefault())
                .toLocalDate()
            date.format(java.time.format.DateTimeFormatter.ofPattern("d MMM"))
        }
    }
}
