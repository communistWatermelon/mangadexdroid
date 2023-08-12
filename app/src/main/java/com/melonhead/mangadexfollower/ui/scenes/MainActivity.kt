package com.melonhead.mangadexfollower.ui.scenes

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.autofill.AutofillNode
import androidx.compose.ui.autofill.AutofillType
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.*
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import com.melonhead.mangadexfollower.BuildConfig
import com.melonhead.mangadexfollower.R
import com.melonhead.mangadexfollower.extensions.dateOrTimeString
import com.melonhead.mangadexfollower.models.ui.*
import com.melonhead.mangadexfollower.notifications.NewChapterNotification
import com.melonhead.mangadexfollower.ui.scenes.shared.LoadingScreen
import com.melonhead.mangadexfollower.ui.theme.MangadexFollowerTheme
import com.melonhead.mangadexfollower.ui.viewmodels.MainViewModel
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import org.koin.androidx.viewmodel.ext.android.viewModel

class MainActivity : ComponentActivity() {
    private val viewModel by viewModel<MainViewModel>()
    private lateinit var requestPermissionLauncher: ActivityResultLauncher<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requestPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) {
            // do nothing for now
        }

        // request permission to post notifications
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }

        setContent {
            MangadexFollowerTheme {
                // A surface container using the 'background' color from the theme
                Surface(modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val loginStatus by viewModel.loginStatus.observeAsState()
                    val manga by viewModel.manga.observeAsState(listOf())
                    val refreshStatus by viewModel.refreshStatus.observeAsState(None)
                    val refreshText by viewModel.refreshText.observeAsState("")

                    Content(loginStatus = loginStatus,
                        refreshStatus = refreshStatus,
                        refreshText = refreshText,
                        manga = manga,
                        loginClicked = { username, password -> viewModel.authenticate(username, password) },
                        onChapterClicked = { uiManga, chapter -> viewModel.onChapterClicked(this, uiManga, chapter) },
                        onChapterLongPressed = { uiManga, chapter -> viewModel.toggleChapterRead(uiManga, chapter) },
                        onSwipeRefresh = { viewModel.refreshContent() },
                        onMangaLongPress = { uiManga -> viewModel.toggleMangaWebview(uiManga) }
                    )
                }
            }
        }

        onNewIntent(intent)
    }
    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        val mangaJson = intent?.getStringExtra(NewChapterNotification.MANGA_EXTRA) ?: return
        val chapterJson = intent.getStringExtra(NewChapterNotification.CHAPTER_EXTRA) ?: return
        val manga: UIManga = Json.decodeFromString(mangaJson)
        val chapter: UIChapter = Json.decodeFromString(chapterJson)
        viewModel.onChapterClicked(this, manga, chapter)
    }
}

@Composable
private fun Content(
    loginStatus: LoginStatus?,
    refreshStatus: MangaRefreshStatus,
    refreshText: String,
    manga: List<UIManga>,
    loginClicked: (username: String, password: String) -> Unit,
    onChapterClicked: (UIManga, UIChapter) -> Unit,
    onChapterLongPressed: (UIManga, UIChapter) -> Unit,
    onSwipeRefresh: () -> Unit,
    onMangaLongPress: (UIManga) -> Unit
) {
    var chapterReadStatusDialog by remember { mutableStateOf<Pair<UIManga, UIChapter>?>(null) }
    val currentReadStatusDialog = chapterReadStatusDialog

    if (currentReadStatusDialog != null) {
        AlertDialog(
            onDismissRequest = { chapterReadStatusDialog = null },
            title = {
                Text(text = currentReadStatusDialog.first.title)
            },
            text = {
               Text(text = "Mark chapter ${currentReadStatusDialog.second.chapter} as ${if (currentReadStatusDialog.second.read == true) "unread" else "read"}?")
            },
            confirmButton = {
                TextButton(onClick = {
                    onChapterLongPressed(currentReadStatusDialog.first, currentReadStatusDialog.second)
                    chapterReadStatusDialog = null
                }) {
                    Text("Okay")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    chapterReadStatusDialog = null
                }) {
                    Text("Cancel")
                }
            }
        )
    }

    var mangaWebviewToggleDialog by remember { mutableStateOf<UIManga?>(null) }
    val currentWebviewToggleDialog = mangaWebviewToggleDialog

    if (currentWebviewToggleDialog != null) {
        AlertDialog(
            onDismissRequest = { mangaWebviewToggleDialog = null },
            title = {
                Text(text = currentWebviewToggleDialog.title)
            },
            text = {
                Text(text = "Switch to ${if (currentWebviewToggleDialog.useWebview) "native" else "webView"} reader for manga?")
            },
            confirmButton = {
                TextButton(onClick = {
                    onMangaLongPress(currentWebviewToggleDialog)
                    mangaWebviewToggleDialog = null
                }) {
                    Text("Okay")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    mangaWebviewToggleDialog = null
                }) {
                    Text("Cancel")
                }
            }
        )
    }

    when (loginStatus) {
        LoginStatus.LoggedIn -> {
            if (manga.isEmpty()) LoadingScreen(refreshStatus) else {
                ChaptersList(
                    manga,
                    refreshText = refreshText,
                    refreshStatus = refreshStatus,
                    onChapterClicked = onChapterClicked,
                    onChapterLongPressed = { uiManga, uiChapter ->
                        chapterReadStatusDialog = uiManga to uiChapter
                    },
                    onSwipeRefresh = onSwipeRefresh,
                    onMangaLongPress = { uiManga ->
                        mangaWebviewToggleDialog = uiManga
                    },
                )
            }
        }
        LoginStatus.LoggedOut -> LoginScreen(loginClicked)
        LoginStatus.LoggingIn, null -> LoadingScreen(null)
    }
}


@OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class)
@Composable
private fun LoginScreen(loginClicked: (email: String, password: String) -> Unit) {
    var emailField by rememberSaveable { mutableStateOf("") }
    var passwordField by rememberSaveable { mutableStateOf("") }
    var loggingIn by rememberSaveable { mutableStateOf(false) }
    var isPasswordVisible by rememberSaveable { mutableStateOf(false) }
    val keyboardController = LocalSoftwareKeyboardController.current

    fun signIn() {
        loginClicked(emailField, passwordField)
        loggingIn = true
        keyboardController?.hide()
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        val emailNode = AutofillNode(
            autofillTypes = listOf(AutofillType.EmailAddress),
            onFill = { emailField = it }
        )
        val passwordNode = AutofillNode(
            autofillTypes = listOf(AutofillType.Password),
            onFill = { emailField = it }
        )

        val autoFill = LocalAutofill.current

        LocalAutofillTree.current += emailNode
        LocalAutofillTree.current += passwordNode

        val focusManager = LocalFocusManager.current

        Image(painter = painterResource(id = R.drawable.mangadex_v2_svgrepo_com), 
            contentDescription = "Mangadex Logo", 
            modifier = Modifier
                .padding(48.dp)
                .size(250.dp))

        OutlinedTextField(value = emailField,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next, keyboardType = KeyboardType.Email),
            keyboardActions = KeyboardActions(onNext = {
                focusManager.moveFocus(FocusDirection.Down)
            }),
            onValueChange = { emailField = it },
            label = { Text("Email") },
            singleLine = true,
            modifier = Modifier
                .padding(bottom = 8.dp)
                .onGloballyPositioned {
                    emailNode.boundingBox = it.boundsInWindow()
                }
                .onFocusChanged { focusState ->
                    autoFill?.run {
                        if (focusState.isFocused) {
                            requestAutofillForNode(emailNode)
                        } else {
                            cancelAutofillForNode(emailNode)
                        }
                    }
                }
        )
        OutlinedTextField(value = passwordField,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done, keyboardType = KeyboardType.Password),
            keyboardActions = KeyboardActions(onDone = {
                signIn()
            }),
            onValueChange = { passwordField = it },
            label = { Text("Password") },
            singleLine = true,
            visualTransformation = if (isPasswordVisible)
                VisualTransformation.None
            else
                PasswordVisualTransformation(),
            trailingIcon = {
                IconButton(onClick = {
                    isPasswordVisible = !isPasswordVisible
                }) {
                    Icon(
                        imageVector = if (isPasswordVisible)
                            Icons.Filled.Visibility
                        else
                            Icons.Filled.VisibilityOff,
                        contentDescription = "Password Visibility"
                    )
                }
            },
            modifier = Modifier
                .padding(bottom = 24.dp)
                .onGloballyPositioned {
                    passwordNode.boundingBox = it.boundsInWindow()
                }
                .onFocusChanged { focusState ->
                    autoFill?.run {
                        if (focusState.isFocused) {
                            requestAutofillForNode(passwordNode)
                        } else {
                            cancelAutofillForNode(passwordNode)
                        }
                    }
                }
        )
        Button(enabled = !loggingIn, onClick = {
            if (emailField.isNotBlank() && passwordField.isNotBlank()) {
                signIn()
            }
        }) {
            if (loggingIn) {
                CircularProgressIndicator(modifier = Modifier
                    .padding(horizontal = 8.dp)
                    .size(12.dp),
                    strokeWidth = 2.dp)
            } else {
                Text(text = "Sign In")
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun Chapter(modifier: Modifier = Modifier,
            uiChapter: UIChapter,
            uiManga: UIManga,
            refreshStatus: MangaRefreshStatus,
            onChapterClicked: (UIManga, UIChapter) -> Unit,
            onChapterLongPressed: (UIManga, UIChapter) -> Unit) {
    Card(modifier = modifier
        .fillMaxWidth()
        .combinedClickable(
            onClick = { onChapterClicked(uiManga, uiChapter) },
            onLongClick = { onChapterLongPressed(uiManga, uiChapter) }
        )) {
        Row(modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 50.dp)
            .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f),
               verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(text = "Chapter ${uiChapter.chapter}",
                    color = MaterialTheme.colorScheme.onBackground,
                    fontWeight = FontWeight.Medium,
                    fontSize = 18.sp)
                Text(text = uiChapter.title ?: "",
                    fontWeight = FontWeight.Normal,
                    fontSize = 14.sp
                )
                Text(text = Instant.fromEpochSeconds(uiChapter.createdDate).dateOrTimeString(),
                    color = MaterialTheme.colorScheme.tertiary,
                    fontWeight = FontWeight.Light,
                    fontSize = 12.sp
                )
            }
            if (refreshStatus !is None && uiChapter.read != true) {
                CircularProgressIndicator(modifier = Modifier
                    .padding(horizontal = 8.dp)
                    .size(12.dp),
                    strokeWidth = 2.dp)
            } else {
                Text(modifier = Modifier.align(Alignment.CenterVertically),
                    color = MaterialTheme.colorScheme.primary,
                    text = if (uiChapter.read != true) "NEW" else "",
                    fontWeight = FontWeight.Medium,
                    fontSize = 12.sp)
            }

        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun MangaCover(modifier: Modifier = Modifier, uiManga: UIManga, onLongPress: (uiManga: UIManga) -> Unit) {
    Row(modifier.combinedClickable(onClick = { }, onLongClick = { onLongPress(uiManga) })) {
        Box(
            modifier
                .padding(horizontal = 10.dp)
                .height(110.dp)) {
            SubcomposeAsyncImage(modifier = Modifier
                .clip(RoundedCornerShape(12.dp, 12.dp, 0.dp, 0.dp)),
                contentScale = ContentScale.Crop,
                model = ImageRequest.Builder(LocalContext.current)
                    .data(uiManga.coverAddress)
                    .crossfade(true)
                    .build(),
                loading = {
                      Box(modifier = Modifier.width(100.dp)) {
                          CircularProgressIndicator(Modifier.align(Alignment.Center))
                      }
                },
                error = {
                    Box(
                        Modifier
                            .width(100.dp)
                            .background(Color.DarkGray)) {
                        Text(text = "No Image",
                            color = Color.White,
                            fontSize = 12.sp,
                            modifier = Modifier.align(Alignment.Center))
                    }
                },
                contentDescription = "${uiManga.title} cover"
            )
        }
        Column(
            modifier = Modifier.align(Alignment.Top).padding(top = 20.dp).fillMaxWidth(1f),
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(text = uiManga.title,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.End,
                fontSize = 20.sp)
            Text(text = if (uiManga.useWebview) "WebView" else "Native",
                overflow = TextOverflow.Ellipsis, fontSize = 12.sp)
        }
    }
}

@Composable
private fun ChaptersList(
    manga: List<UIManga>,
    refreshStatus: MangaRefreshStatus,
    refreshText: String,
    onChapterClicked: (UIManga, UIChapter) -> Unit,
    onChapterLongPressed: (UIManga, UIChapter) -> Unit,
    onSwipeRefresh: () -> Unit,
    onMangaLongPress: (UIManga) -> Unit,
) {
    val isRefreshing = rememberSwipeRefreshState(isRefreshing = false)
    var justPulledRefresh by remember { mutableStateOf(false) }
    val context = LocalContext.current

    val itemState = remember(manga, refreshStatus) {
        val items = mutableListOf<Any>()
        manga.forEach { manga ->
            items.add(manga)
            manga.chapters.filter { it.read != true }.forEach {
                items.add(it to manga)
            }
// TODO: limit based on showReadChapterCount here
//            manga.chapters.filter { it.read == true }.take.forEach {
//                it
//            }
        }
        items.toList()
    }
    LaunchedEffect(refreshStatus) { justPulledRefresh = false }

    Column {
        AnimatedVisibility(visible = refreshStatus !is None || isRefreshing.isRefreshing || justPulledRefresh) {
            Row(
                Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceTint)
                    .padding(8.dp)
                    .clickable {
                        Toast
                            .makeText(
                                context,
                                "Version ${BuildConfig.VERSION_NAME}.${BuildConfig.VERSION_CODE}",
                                Toast.LENGTH_SHORT
                            )
                            .show()
                    },
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                CircularProgressIndicator(modifier = Modifier
                    .padding(horizontal = 8.dp)
                    .size(12.dp),
                    strokeWidth = 2.dp,
                    color = MaterialTheme.colorScheme.onSecondary)
                Text(text = refreshStatus.text,
                    color = MaterialTheme.colorScheme.onSecondary,
                    fontSize = 14.sp)
            }
        }

        SwipeRefresh(state = isRefreshing,
            onRefresh = {
                justPulledRefresh = true
                onSwipeRefresh()
            },
            swipeEnabled = (refreshStatus is None) && !isRefreshing.isRefreshing && !justPulledRefresh) {
            LazyColumn(modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 24.dp),
                verticalArrangement = Arrangement.spacedBy(0.dp)) {
                item {
                    AnimatedVisibility(visible = refreshStatus is None && !isRefreshing.isRefreshing) {
                        Text(text = "Last Refresh: $refreshText",
                            modifier = Modifier
                                .fillMaxWidth()
                                .align(Alignment.CenterHorizontally)
                                .clickable {
                                    Toast
                                        .makeText(
                                            context,
                                            "Version ${BuildConfig.VERSION_NAME}.${BuildConfig.VERSION_CODE}",
                                            Toast.LENGTH_SHORT
                                        )
                                        .show()
                                }
                                .padding(bottom = 12.dp),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Normal,
                            textAlign = TextAlign.Center)
                    }
                }

                items(itemState, {
                    when {
                        it is UIManga -> it.id
                        it is Pair<*, *> && it.first == UIChapter -> (it.first as UIChapter).id
                        else -> it.hashCode()
                    }
                }) {
                    if (it is UIManga) {
                        MangaCover(modifier = Modifier.padding(top = if (itemState.first() == it) 0.dp else 12.dp), uiManga = it, onLongPress = onMangaLongPress)
                    }
                    if (it is Pair<*, *> && it.first is UIChapter) {
                        Chapter(
                            modifier = Modifier.padding(bottom = 12.dp),
                            uiChapter = it.first as UIChapter, uiManga = it.second as UIManga, refreshStatus = refreshStatus, onChapterClicked = onChapterClicked, onChapterLongPressed = onChapterLongPressed)
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ChapterPreview() {
    MangadexFollowerTheme {
        val manga = UIManga("", "Test Manga", listOf(), null, false)
        Column {
            Chapter(uiChapter = UIChapter("", "101", "Test Title with an extremely long title that may or may not wrap", Clock.System.now().epochSeconds, false), uiManga = manga, refreshStatus = ReadStatus, onChapterClicked = { _, _ -> }, onChapterLongPressed = { _, _ -> })
            Chapter(uiChapter = UIChapter("", "102", "Test Title 2", Clock.System.now().epochSeconds, true), uiManga = manga, refreshStatus = ReadStatus, onChapterClicked = { _, _ -> }, onChapterLongPressed = { _, _ -> })
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun MangaPreview() {
    val testChapters = listOf(UIChapter("", "101", "Test Title", Clock.System.now().epochSeconds, true), UIChapter("", "102", "Test Title 2", Clock.System.now().epochSeconds, false))
    MangadexFollowerTheme {
        Column(modifier = Modifier.fillMaxWidth()) {
            MangaCover(uiManga = UIManga("", "Test Manga", testChapters, null, false), onLongPress = { })
            MangaCover(uiManga = UIManga("", "Test Manga with a really long name that causes the name to clip a little", testChapters, null, false), onLongPress = { })
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun LoginPreview() {
    MangadexFollowerTheme {
        LoginScreen(loginClicked = { _, _ -> })
    }
}
