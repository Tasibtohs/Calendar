package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import com.example.ui.theme.AppMotion
import com.example.ui.screens.*
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.CalendarViewModel

import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.ui.components.*
import com.example.util.AppLockManager
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      val calendarViewModel: CalendarViewModel = viewModel()
      val themeMode by calendarViewModel.themeMode.collectAsState()
      val themeAccent by calendarViewModel.themeAccent.collectAsState()
      val isAmoled by calendarViewModel.isAmoled.collectAsState()
      val dynamicColor by calendarViewModel.dynamicColor.collectAsState()

      var isAppLocked by remember { mutableStateOf(false) }

      LaunchedEffect(Unit) {
        isAppLocked = AppLockManager.isLockEnabled(calendarViewModel.repository)
        val targetTab = intent?.getIntExtra(com.example.widget.CalendarAppWidget.EXTRA_TARGET_TAB, -1) ?: -1
        if (targetTab in 0..4) {
          calendarViewModel.selectTab(targetTab)
        }
      }

      val darkTheme = when (themeMode) {
        "Light" -> false
        "Dark" -> true
        else -> isSystemInDarkTheme()
      }

      MyApplicationTheme(
        darkTheme = darkTheme,
        isAmoled = isAmoled,
        accentId = themeAccent,
        dynamicColor = dynamicColor
      ) {
        if (isAppLocked) {
          AppLockOverlay(
            viewModel = calendarViewModel,
            onUnlocked = { isAppLocked = false }
          )
        } else {
          MainScreenApp(viewModel = calendarViewModel)
        }
      }
    }
  }

  override fun onNewIntent(intent: android.content.Intent) {
    super.onNewIntent(intent)
    setIntent(intent)
  }
}

@Composable
fun MainScreenApp(viewModel: CalendarViewModel) {
  val currentTab by viewModel.currentTab.collectAsState()
  val allHolidays by viewModel.allHolidays.collectAsState()
  val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
  val scope = rememberCoroutineScope()

  var showGlobalSearchDialog by remember { mutableStateOf(false) }
  var showNotificationDialog by remember { mutableStateOf(false) }
  var selectedNotificationEvent by remember { mutableStateOf<com.example.data.model.Event?>(null) }
  var showBirthdaysDialog by remember { mutableStateOf(false) }
  var showAnniversariesDialog by remember { mutableStateOf(false) }
  var showHolidaysDialog by remember { mutableStateOf(false) }
  var showDateCalculatorDialog by remember { mutableStateOf(false) }
  var showFreeTimeDialog by remember { mutableStateOf(false) }
  var showArchiveDialog by remember { mutableStateOf(false) }
  var showCountdownsDialog by remember { mutableStateOf(false) }
  var showStatisticsDialog by remember { mutableStateOf(false) }
  var showBackupRestoreDialog by remember { mutableStateOf(false) }
  var showAboutDialog by remember { mutableStateOf(false) }
  var showSupportDialog by remember { mutableStateOf(false) }

  BackHandler(enabled = drawerState.isOpen || currentTab != 0) {
    if (drawerState.isOpen) {
      scope.launch { drawerState.close() }
    } else if (currentTab != 0) {
      viewModel.selectTab(0)
    }
  }

  val navItems = listOf(
    NavItem("Home", Icons.Default.Home, "nav_item_home"),
    NavItem("Calendar", Icons.Default.CalendarMonth, "nav_item_calendar"),
    NavItem("Tasks", Icons.Default.CheckCircle, "nav_item_tasks"),
    NavItem("Notes", Icons.Default.StickyNote2, "nav_item_notes"),
    NavItem("Settings", Icons.Default.Settings, "nav_item_settings")
  )

  ModalNavigationDrawer(
    drawerState = drawerState,
    drawerContent = {
      AppDrawerContent(
        viewModel = viewModel,
        currentTab = currentTab,
        onNavigateToTab = { tab ->
          viewModel.selectTab(tab)
        },
        onOpenBirthdays = { showBirthdaysDialog = true },
        onOpenAnniversaries = { showAnniversariesDialog = true },
        onOpenHolidays = { showHolidaysDialog = true },
        onOpenDateCalculator = { showDateCalculatorDialog = true },
        onOpenFreeTime = { showFreeTimeDialog = true },
        onOpenStatistics = { showStatisticsDialog = true },
        onOpenArchive = { showArchiveDialog = true },
        onOpenCountdowns = { showCountdownsDialog = true },
        onOpenBackupRestore = { showBackupRestoreDialog = true },
        onOpenAbout = { showAboutDialog = true },
        onOpenSupport = { showSupportDialog = true },
        onCloseDrawer = {
          scope.launch { drawerState.close() }
        }
      )
    }
  ) {
    Scaffold(
      modifier = Modifier.fillMaxSize(),
      contentWindowInsets = WindowInsets(0, 0, 0, 0),
      topBar = {
        if (currentTab != 1) {
          val (screenTitle, screenSubtitle) = when (currentTab) {
            0 -> null to null
            2 -> null to "ইভেন্ট, টাস্ক ও রিমাইন্ডার"
            3 -> null to "নোটবুক ও মেমো"
            4 -> null to "অ্যাপ পছন্দ ও সেটিংস"
            else -> null to null
          }
          AppTopBar(
            viewModel = viewModel,
            title = screenTitle,
            subtitle = screenSubtitle,
            onOpenDrawer = { scope.launch { drawerState.open() } },
            onOpenSearch = { showGlobalSearchDialog = true },
            onOpenNotifications = { showNotificationDialog = true },
            onOpenExport = { showBackupRestoreDialog = true },
            onOpenAbout = { showAboutDialog = true }
          )
        }
      },
      bottomBar = {
        NavigationBar(
          modifier = Modifier.testTag("main_bottom_navigation_bar")
        ) {
          navItems.forEachIndexed { index, item ->
            NavigationBarItem(
              selected = currentTab == index,
              onClick = { viewModel.selectTab(index) },
              icon = { Icon(item.icon, contentDescription = item.label) },
              label = { Text(item.label) },
              modifier = Modifier.testTag(item.testTag)
            )
          }
        }
      }
    ) { innerPadding ->
      Box(
        modifier = Modifier
          .fillMaxSize()
          .padding(innerPadding)
      ) {
        AnimatedContent(
          targetState = currentTab,
          transitionSpec = {
            if (targetState > initialState) {
              (slideInHorizontally(
                initialOffsetX = { it / 6 },
                animationSpec = tween(AppMotion.durationNormal)
              ) + fadeIn(animationSpec = tween(AppMotion.durationNormal)))
                .togetherWith(
                  slideOutHorizontally(
                    targetOffsetX = { -it / 6 },
                    animationSpec = tween(AppMotion.durationFast)
                  ) + fadeOut(animationSpec = tween(AppMotion.durationFast))
                )
            } else {
              (slideInHorizontally(
                initialOffsetX = { -it / 6 },
                animationSpec = tween(AppMotion.durationNormal)
              ) + fadeIn(animationSpec = tween(AppMotion.durationNormal)))
                .togetherWith(
                  slideOutHorizontally(
                    targetOffsetX = { it / 6 },
                    animationSpec = tween(AppMotion.durationFast)
                  ) + fadeOut(animationSpec = tween(AppMotion.durationFast))
                )
            }
          },
          label = "ScreenTransitionAnimation"
        ) { tabIndex ->
          when (tabIndex) {
            0 -> HomeScreen(
              viewModel = viewModel,
              onOpenDrawer = { scope.launch { drawerState.open() } }
            )
            1 -> CalendarScreen(
              viewModel = viewModel,
              onOpenDrawer = { scope.launch { drawerState.open() } },
              onOpenSearch = { showGlobalSearchDialog = true }
            )
            2 -> TasksScreen(viewModel = viewModel)
            3 -> NotesScreen(viewModel = viewModel)
            4 -> SettingsScreen(viewModel = viewModel)
          }
        }
      }
    }
  }

  if (showGlobalSearchDialog) {
    GlobalSearchDialog(
      viewModel = viewModel,
      onDismiss = { showGlobalSearchDialog = false },
      onSelectEvent = {
        showGlobalSearchDialog = false
        selectedNotificationEvent = it
      }
    )
  }

  if (showNotificationDialog) {
    NotificationHistoryDialog(
      viewModel = viewModel,
      onDismiss = { showNotificationDialog = false },
      onSelectEvent = {
        showNotificationDialog = false
        selectedNotificationEvent = it
      }
    )
  }

  selectedNotificationEvent?.let { ev ->
    EventDetailSheet(
      event = ev,
      viewModel = viewModel,
      onDismiss = { selectedNotificationEvent = null },
      onEditEvent = {
        selectedNotificationEvent = null
      }
    )
  }

  if (showBirthdaysDialog) {
    BirthdaysListDialog(
      viewModel = viewModel,
      onDismiss = { showBirthdaysDialog = false }
    )
  }

  if (showAnniversariesDialog) {
    AnniversariesListDialog(
      viewModel = viewModel,
      onDismiss = { showAnniversariesDialog = false }
    )
  }

  if (showHolidaysDialog) {
    Dialog(
      onDismissRequest = { showHolidaysDialog = false },
      properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
      HolidaysScreen(
        viewModel = viewModel,
        onBack = { showHolidaysDialog = false }
      )
    }
  }

  if (showDateCalculatorDialog) {
    Dialog(
      onDismissRequest = { showDateCalculatorDialog = false },
      properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
      DateCalculatorScreen(
        onBack = { showDateCalculatorDialog = false }
      )
    }
  }

  if (showFreeTimeDialog) {
    Dialog(
      onDismissRequest = { showFreeTimeDialog = false },
      properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
      FreeTimeScreen(
        viewModel = viewModel,
        onBack = { showFreeTimeDialog = false }
      )
    }
  }

  if (showStatisticsDialog) {
    Dialog(
      onDismissRequest = { showStatisticsDialog = false },
      properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
      StatisticsScreen(
        viewModel = viewModel,
        onBack = { showStatisticsDialog = false }
      )
    }
  }

  if (showArchiveDialog) {
    Dialog(
      onDismissRequest = { showArchiveDialog = false },
      properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
      ArchiveExportScreen(
        viewModel = viewModel,
        onBack = { showArchiveDialog = false }
      )
    }
  }

  if (showCountdownsDialog) {
    AddCountdownDialog(
      onDismiss = { showCountdownsDialog = false },
      onSave = { viewModel.saveCountdown(it) }
    )
  }

  if (showBackupRestoreDialog) {
    BackupRestoreDialog(
      viewModel = viewModel,
      onDismiss = { showBackupRestoreDialog = false }
    )
  }

  if (showAboutDialog) {
    AboutDialog(
      onDismiss = { showAboutDialog = false }
    )
  }

  if (showSupportDialog) {
    SupportDialog(
      onDismiss = { showSupportDialog = false }
    )
  }
}


private data class NavItem(
  val label: String,
  val icon: ImageVector,
  val testTag: String
)
