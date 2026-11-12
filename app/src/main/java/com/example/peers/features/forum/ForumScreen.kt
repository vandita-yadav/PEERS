package com.example.peers.features.forum

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.Send
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.ChatBubbleOutline
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.ThumbUp
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
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.peers.ui.theme.PeersBrandInk
import com.example.peers.ui.theme.PeersLavender
import com.example.peers.ui.theme.PeersMint
import com.example.peers.ui.theme.PeersPrimaryIndigo
import com.example.peers.ui.theme.PeersSky
import java.util.concurrent.TimeUnit

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ForumScreen(
    modifier: Modifier = Modifier,
    viewModel: ForumViewModel = viewModel(),
) {
    val posts by viewModel.posts.collectAsStateWithLifecycle()
    val allPosts by viewModel.allPosts.collectAsStateWithLifecycle()
    val query by viewModel.query.collectAsStateWithLifecycle()
    val selectedCommunity by viewModel.communityId.collectAsStateWithLifecycle()
    val selectedPostId by viewModel.selectedPostId.collectAsStateWithLifecycle()
    val comments by viewModel.comments.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }
    var showComposer by remember { mutableStateOf(false) }
    var reportTarget by remember { mutableStateOf<ReportTarget?>(null) }

    LaunchedEffect(Unit) {
        viewModel.uiEvents.collect { event ->
            when (event) {
                is ForumUiEvent.Message -> snackbarHostState.showSnackbar(event.text)
            }
        }
    }

    val selectedPost = selectedPostId?.let { id -> allPosts.firstOrNull { it.id == id } }

    Box(modifier = modifier.fillMaxSize()) {
        if (selectedPost != null) {
            BackHandler { viewModel.closePost() }
            PostDetail(
                post = selectedPost,
                comments = comments,
                onBack = viewModel::closePost,
                onUpvote = { viewModel.toggleUpvote(selectedPost.id) },
                onAddComment = { body -> viewModel.addComment(selectedPost, body) },
                onReportPost = { reportTarget = ReportTarget(post = selectedPost) },
                onReportComment = { comment -> reportTarget = ReportTarget(comment = comment) },
            )
        } else {
            ForumFeed(
                posts = posts,
                query = query,
                selectedCommunity = selectedCommunity,
                communities = viewModel.communities,
                onQueryChange = viewModel::setQuery,
                onCommunitySelected = viewModel::selectCommunity,
                onOpenPost = { viewModel.openPost(it.id) },
                onUpvote = viewModel::toggleUpvote,
                onReport = { post -> reportTarget = ReportTarget(post = post) },
                onCompose = { showComposer = true },
            )
        }
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter).padding(16.dp),
        )
    }

    if (showComposer) {
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ModalBottomSheet(
            onDismissRequest = { showComposer = false },
            sheetState = sheetState,
            containerColor = MaterialTheme.colorScheme.surface,
        ) {
            NewPostSheet(
                communities = viewModel.communities,
                onSubmit = { title, body, community ->
                    viewModel.createPost(title, body, community)
                    showComposer = false
                },
            )
        }
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

@Composable
private fun ForumFeed(
    posts: List<ForumPost>,
    query: String,
    selectedCommunity: String?,
    communities: List<ForumCommunity>,
    onQueryChange: (String) -> Unit,
    onCommunitySelected: (String?) -> Unit,
    onOpenPost: (ForumPost) -> Unit,
    onUpvote: (String) -> Unit,
    onReport: (ForumPost) -> Unit,
    onCompose: () -> Unit,
) {
    LazyColumn(
        contentPadding = PaddingValues(bottom = 28.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            Column(Modifier.padding(horizontal = 20.dp, vertical = 10.dp)) {
                Text(
                    "Community",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    "Ask, share and find your people",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        item {
            ForumHero(
                postCount = posts.size,
                communityCount = communities.size,
                onCompose = onCompose,
            )
        }

        item {
            OutlinedTextField(
                value = query,
                onValueChange = onQueryChange,
                placeholder = { Text("Search discussions") },
                leadingIcon = { Icon(Icons.Rounded.Search, contentDescription = null) },
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
                        label = { Text("All spaces") },
                        colors = forumChipColors(),
                    )
                }
                items(communities, key = { it.id }) { community ->
                    FilterChip(
                        selected = selectedCommunity == community.id,
                        onClick = { onCommunitySelected(community.id) },
                        label = { Text("${community.emoji} ${community.name}") },
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
                    if (selectedCommunity == null) "Latest discussions" else "In this space",
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
                        Text("🗣️", fontSize = 40.sp)
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
                    onOpen = { onOpenPost(post) },
                    onUpvote = { onUpvote(post.id) },
                    onReport = { onReport(post) },
                    modifier = Modifier.padding(horizontal = 20.dp),
                )
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
private fun ForumHero(postCount: Int, communityCount: Int, onCompose: () -> Unit) {
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
            Text(
                "Your campus, in conversation",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = Color.White,
            )
            Text(
                "Honest answers from students who have already done it.",
                style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(alpha = 0.82f),
                modifier = Modifier.padding(top = 4.dp),
            )
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 16.dp)) {
                HeroStat("$postCount", "threads", Modifier.weight(1f))
                HeroStat("$communityCount", "spaces", Modifier.weight(1f))
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
    onOpen: () -> Unit,
    onUpvote: () -> Unit,
    onReport: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var menuOpen by remember { mutableStateOf(false) }
    Surface(
        shape = RoundedCornerShape(22.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 1.dp,
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
                            contentDescription = "Report discussion",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp),
                        )
                    }
                    DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }) {
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
                    Text(post.authorEmoji, fontSize = 15.sp)
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
private fun NewPostSheet(
    communities: List<ForumCommunity>,
    onSubmit: (String, String, ForumCommunity) -> Unit,
) {
    var title by remember { mutableStateOf("") }
    var body by remember { mutableStateOf("") }
    var community by remember { mutableStateOf(communities.first()) }
    val valid = title.isNotBlank() && body.isNotBlank()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 22.dp)
            .padding(bottom = 28.dp),
    ) {
        Text(
            "Start a discussion",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
        )
        Text(
            "Real questions get real answers. Keep it kind and specific.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 2.dp),
        )
        OutlinedTextField(
            value = title,
            onValueChange = { title = it },
            label = { Text("Question or title") },
            placeholder = { Text("How do I open a bank account as a student?") },
            singleLine = true,
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
        )
        OutlinedTextField(
            value = body,
            onValueChange = { body = it },
            label = { Text("Details") },
            placeholder = { Text("Share what you've tried or what you need to know…") },
            minLines = 4,
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth().padding(top = 10.dp),
        )
        Text(
            "Post in",
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(top = 16.dp, bottom = 6.dp),
        )
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(communities, key = { it.id }) { option ->
                FilterChip(
                    selected = community.id == option.id,
                    onClick = { community = option },
                    label = { Text("${option.emoji} ${option.name}") },
                    colors = forumChipColors(),
                )
            }
        }
        Button(
            onClick = { onSubmit(title, body, community) },
            enabled = valid,
            colors = ButtonDefaults.buttonColors(containerColor = PeersPrimaryIndigo),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth().padding(top = 18.dp).height(52.dp),
        ) {
            Text("Publish discussion", fontWeight = FontWeight.Bold)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PostDetail(
    post: ForumPost,
    comments: List<ForumComment>,
    onBack: () -> Unit,
    onUpvote: () -> Unit,
    onAddComment: (String) -> Unit,
    onReportPost: () -> Unit,
    onReportComment: (ForumComment) -> Unit,
) {
    var reply by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Discussion", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = onReportPost) {
                        Icon(Icons.Rounded.MoreVert, contentDescription = "Report discussion")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                ),
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            LazyColumn(
                contentPadding = PaddingValues(start = 20.dp, end = 20.dp, bottom = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.weight(1f),
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
                        ) { Text(post.authorEmoji, fontSize = 17.sp) }
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
                            "  ${post.commentCount} replies",
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
                            onReport = { onReportComment(comment) },
                        )
                    }
                }
            }

            ReplyBar(
                value = reply,
                onValueChange = { reply = it },
                onSend = {
                    val text = reply.trim()
                    if (text.isNotEmpty()) {
                        onAddComment(text)
                        reply = ""
                    }
                },
            )
        }
    }
}

@Composable
private fun CommentCard(comment: ForumComment, onReport: () -> Unit) {
    var menuOpen by remember { mutableStateOf(false) }
    Surface(
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surface,
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
                ) { Text(comment.authorEmoji, fontSize = 14.sp) }
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
                            contentDescription = "Report reply",
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }) {
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
            Text(
                comment.body,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = 6.dp),
            )
        }
    }
}

@Composable
private fun ReplyBar(value: String, onValueChange: (String) -> Unit, onSend: () -> Unit) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 6.dp,
        modifier = Modifier.fillMaxWidth().imePadding(),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
        ) {
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
                enabled = value.isNotBlank(),
                modifier = Modifier.padding(start = 6.dp),
            ) {
                Icon(
                    Icons.AutoMirrored.Rounded.Send,
                    contentDescription = "Send reply",
                    tint = if (value.isNotBlank()) PeersPrimaryIndigo
                    else MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun ReportDialog(label: String, onDismiss: () -> Unit, onReport: (ReportReason) -> Unit) {
    var selected by remember { mutableStateOf<ReportReason?>(null) }
    androidx.compose.material3.AlertDialog(
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
                        androidx.compose.material3.RadioButton(
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
