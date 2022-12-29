package com.melonhead.mangadexfollower.ui.scenes

import android.Manifest
import android.content.pm.PackageManager
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
import com.melonhead.mangadexfollower.ui.theme.MangadexFollowerTheme
import com.melonhead.mangadexfollower.ui.viewmodels.MainViewModel
import kotlinx.datetime.Clock
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
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
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
                        onChapterClicked = { viewModel.onChapterClicked(this, it) },
                        onChapterLongPressed = { uiManga, chapter -> viewModel.toggleChapterRead(uiManga, chapter) },
                        onSwipeRefresh = { viewModel.refreshContent() }
                    )
                }
            }
        }
    }
}

@Composable
fun Content(loginStatus: LoginStatus?, refreshStatus: MangaRefreshStatus, refreshText: String, manga: List<UIManga>, loginClicked: (username: String, password: String) -> Unit, onChapterClicked: (UIChapter) -> Unit, onChapterLongPressed: (UIManga, UIChapter) -> Unit, onSwipeRefresh: () -> Unit) {
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
                    onSwipeRefresh = onSwipeRefresh
                )
            }
        }
        LoginStatus.LoggedOut -> LoginScreen(loginClicked)
        LoginStatus.LoggingIn, null -> LoadingScreen(null)
    }
}

@Composable
fun LoadingScreen(refreshStatus: MangaRefreshStatus?) {
    Column(modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        CircularProgressIndicator()
        if (refreshStatus != null && refreshStatus !is None)
            Text(text = refreshStatus.text, fontSize = 16.sp, modifier = Modifier.padding(vertical = 16.dp))
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class)
@Composable
fun LoginScreen(loginClicked: (email: String, password: String) -> Unit) {
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
fun Chapter(modifier: Modifier = Modifier,
            uiChapter: UIChapter,
            uiManga: UIManga,
            refreshStatus: MangaRefreshStatus,
            onChapterClicked: (UIChapter) -> Unit,
            onChapterLongPressed: (UIManga, UIChapter) -> Unit) {
    Card(modifier = modifier
        .fillMaxWidth()
        .combinedClickable(
            onClick = { onChapterClicked(uiChapter) },
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
                Text(text = uiChapter.createdDate.dateOrTimeString(),
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

@Composable
fun MangaCover(uiManga: UIManga) {
    Row {
        Box(Modifier.padding(horizontal = 10.dp)) {
            SubcomposeAsyncImage(modifier = Modifier
                .sizeIn(maxWidth = 95.dp)
                .height(140.dp)
                .clip(RoundedCornerShape(12.dp)),
                contentScale = ContentScale.FillHeight,
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
        Text(modifier = Modifier
            .align(Alignment.Top)
            .padding(top = 20.dp)
            .fillMaxWidth(1f),
            text = uiManga.title,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.End,
            fontSize = 20.sp)
    }
}

@Composable
fun Manga(uiManga: UIManga, refreshStatus: MangaRefreshStatus, onChapterClicked: (UIChapter) -> Unit, onChapterLongPressed: (UIManga, UIChapter) -> Unit) {
    Box {
        MangaCover(uiManga = uiManga)
        Column(modifier = Modifier.padding(top = 110.dp)) {
            // todo: allow users to set a max chapters per series limit
//            uiManga.chapters.take(5).forEach {
            uiManga.chapters.forEach {
                Chapter(modifier = Modifier.padding(bottom = 8.dp),
                    uiChapter = it,
                    uiManga = uiManga,
                    refreshStatus = refreshStatus,
                    onChapterClicked = onChapterClicked,
                    onChapterLongPressed = onChapterLongPressed
                )
            }
        }
    }

}

@Composable
fun ChaptersList(manga: List<UIManga>, refreshStatus: MangaRefreshStatus, refreshText: String, onChapterClicked: (UIChapter) -> Unit, onChapterLongPressed: (UIManga, UIChapter) -> Unit, onSwipeRefresh: () -> Unit) {
    val isRefreshing = rememberSwipeRefreshState(isRefreshing = false)
    var justPulledRefresh by remember { mutableStateOf(false) }
    val context = LocalContext.current

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
                verticalArrangement = Arrangement.spacedBy(24.dp)) {
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
                                },
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Normal,
                            textAlign = TextAlign.Center)
                    }
                }
                items(manga) {
                    Column(verticalArrangement = Arrangement.SpaceEvenly) {
                        Manga(uiManga = it, refreshStatus = refreshStatus, onChapterClicked = onChapterClicked, onChapterLongPressed = onChapterLongPressed)
                    }
                }
            }
        }
    }
}
@Preview(showBackground = true)
@Composable
fun ChapterPreview() {
    MangadexFollowerTheme {
        val manga = UIManga("", "Test Manga", listOf(), null)
        Column {
            Chapter(uiChapter = UIChapter("", "101", "Test Title with an extremely long title that may or may not wrap", Clock.System.now(), false), uiManga = manga, refreshStatus = ReadStatus, onChapterClicked = { }, onChapterLongPressed = { _, _ -> })
            Chapter(uiChapter = UIChapter("", "102", "Test Title 2", Clock.System.now(), true), uiManga = manga, refreshStatus = ReadStatus, onChapterClicked = { }, onChapterLongPressed = { _, _ -> })
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MangaPreview() {
    val testChapters = listOf(UIChapter("", "101", "Test Title", Clock.System.now(), true), UIChapter("", "102", "Test Title 2", Clock.System.now(), false))
    MangadexFollowerTheme {
        Column(modifier = Modifier.fillMaxWidth()) {
            Manga(uiManga = UIManga("", "Test Manga", testChapters, null), refreshStatus = ReadStatus, onChapterClicked = { }, onChapterLongPressed = { _, _ -> })
            Manga(uiManga = UIManga("", "Test Manga with a really long name that causes the name to clip a little", testChapters, null), refreshStatus = ReadStatus, onChapterClicked = { }, onChapterLongPressed = { _, _ -> })
        }
    }
}

@Preview(showBackground = true)
@Composable
fun LoginPreview() {
    MangadexFollowerTheme {
        LoginScreen(loginClicked = { _, _ -> })
    }
}

@Preview(showBackground = true)
@Composable
fun LoadingPreviewNoStatus() {
    MangadexFollowerTheme {
        LoadingScreen(null)
    }
}


@Preview(showBackground = true)
@Composable
fun LoadingPreview() {
    MangadexFollowerTheme {
        LoadingScreen(ReadStatus)
    }
}