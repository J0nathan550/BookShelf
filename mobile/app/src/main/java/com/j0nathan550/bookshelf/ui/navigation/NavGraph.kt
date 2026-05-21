package com.j0nathan550.bookshelf.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.j0nathan550.bookshelf.ui.account.AccountScreen
import com.j0nathan550.bookshelf.ui.admin.AdminPanelScreen
import com.j0nathan550.bookshelf.ui.auth.EmailVerificationScreen
import com.j0nathan550.bookshelf.ui.auth.ForgotPasswordScreen
import com.j0nathan550.bookshelf.ui.auth.LoginScreen
import com.j0nathan550.bookshelf.ui.auth.RegisterScreen
import com.j0nathan550.bookshelf.ui.auth.ResetPasswordScreen
import com.j0nathan550.bookshelf.ui.books.addedit.AddEditBookScreen
import com.j0nathan550.bookshelf.ui.books.addedit.BarcodeScannerScreen
import com.j0nathan550.bookshelf.ui.books.addedit.CoverCaptureScreen
import com.j0nathan550.bookshelf.ui.books.detail.BookDetailScreen
import com.j0nathan550.bookshelf.ui.books.list.BookListScreen
import com.j0nathan550.bookshelf.ui.books.list.LentBooksScreen
import com.j0nathan550.bookshelf.ui.statistics.StatisticsScreen

@Composable
fun NavGraph(
    navController: NavHostController,
    startDestination: String,
    notifScreen: String? = null,
    notifBookId: Int? = null,
) {
    NavHost(navController = navController, startDestination = startDestination) {

        composable(Screen.Login.route) {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate(Screen.BookList.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                },
                onNavigateToRegister = { navController.navigate(Screen.Register.route) },
                onNavigateToForgotPassword = { navController.navigate(Screen.ForgotPassword.createRoute()) },
            )
        }

        composable(Screen.Register.route) {
            RegisterScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToVerification = { userId ->
                    navController.navigate(Screen.EmailVerification.createRoute(userId))
                },
            )
        }

        composable(
            route = Screen.EmailVerification.route,
            arguments = listOf(navArgument("userId") { type = NavType.StringType }),
        ) { backStackEntry ->
            val userId = backStackEntry.arguments!!.getString("userId")!!
            EmailVerificationScreen(
                userId = userId,
                onVerified = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Register.route) { inclusive = true }
                    }
                },
                onNavigateBack = { navController.popBackStack() },
            )
        }

        composable(Screen.BookList.route) {
            BookListScreen(
                onBookClick = { bookId ->
                    navController.navigate(Screen.BookDetail.createRoute(bookId))
                },
                onAddBook = { navController.navigate(Screen.AddBook.route) },
                onNavigateToLent = {
                    navController.navigate(Screen.LentBooks.route) {
                        popUpTo(Screen.BookList.route)
                        launchSingleTop = true
                    }
                },
                onNavigateToStats = {
                    navController.navigate(Screen.Statistics.route) {
                        popUpTo(Screen.BookList.route)
                        launchSingleTop = true
                    }
                },
                onNavigateToAdmin = {
                    navController.navigate(Screen.AdminPanel.route) {
                        popUpTo(Screen.BookList.route)
                        launchSingleTop = true
                    }
                },
                onNavigateToAccount = { navController.navigate(Screen.Account.route) },
            )
        }

        composable(Screen.Account.route) {
            AccountScreen(
                onNavigateBack = { navController.popBackStack() },
                onLogout = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                },
                onNavigateToResetPassword = { email ->
                    navController.navigate(Screen.ForgotPassword.createRoute(email))
                },
            )
        }

        composable(
            route = Screen.BookDetail.route,
            arguments = listOf(navArgument("bookId") { type = NavType.IntType }),
        ) { backStackEntry ->
            val bookId = backStackEntry.arguments!!.getInt("bookId")
            BookDetailScreen(
                bookId = bookId,
                onNavigateBack = { navController.popBackStack() },
                onEditBook = { navController.navigate(Screen.EditBook.createRoute(bookId)) },
            )
        }

        composable(Screen.AddBook.route) {
            AddEditBookScreen(
                bookId = null,
                onNavigateBack = { navController.popBackStack() },
                onSaved = { navController.popBackStack() },
                onScanBarcode = { navController.navigate(Screen.BarcodeScanner.route) },
                onCaptureCover = { navController.navigate(Screen.CoverCapture.route) },
                navController = navController,
            )
        }

        composable(Screen.BarcodeScanner.route) {
            BarcodeScannerScreen(
                onBarcodeDetected = { isbn ->
                    navController.previousBackStackEntry
                        ?.savedStateHandle
                        ?.set("scanned_isbn", isbn)
                    navController.popBackStack()
                },
                onNavigateBack = { navController.popBackStack() },
            )
        }

        composable(Screen.CoverCapture.route) {
            CoverCaptureScreen(
                onPhotoCaptured = { path ->
                    navController.previousBackStackEntry
                        ?.savedStateHandle
                        ?.set("captured_cover_path", path)
                    navController.popBackStack()
                },
                onNavigateBack = { navController.popBackStack() },
            )
        }

        composable(
            route = Screen.EditBook.route,
            arguments = listOf(navArgument("bookId") { type = NavType.IntType }),
        ) { backStackEntry ->
            val bookId = backStackEntry.arguments!!.getInt("bookId")
            AddEditBookScreen(
                bookId = bookId,
                onNavigateBack = { navController.popBackStack() },
                onSaved = { navController.popBackStack() },
                onCaptureCover = { navController.navigate(Screen.CoverCapture.route) },
                navController = navController,
            )
        }

        composable(Screen.LentBooks.route) {
            LentBooksScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToLibrary = {
                    navController.popBackStack(Screen.BookList.route, inclusive = false)
                },
                onBookClick = { bookId ->
                    navController.navigate(Screen.BookDetail.createRoute(bookId))
                },
                onNavigateToStats = {
                    navController.navigate(Screen.Statistics.route) {
                        popUpTo(Screen.BookList.route)
                        launchSingleTop = true
                    }
                },
                onNavigateToAdmin = {
                    navController.navigate(Screen.AdminPanel.route) {
                        popUpTo(Screen.BookList.route)
                        launchSingleTop = true
                    }
                },
            )
        }

        composable(Screen.Statistics.route) {
            StatisticsScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToLibrary = {
                    navController.popBackStack(Screen.BookList.route, inclusive = false)
                },
                onNavigateToLent = {
                    navController.navigate(Screen.LentBooks.route) {
                        popUpTo(Screen.BookList.route)
                        launchSingleTop = true
                    }
                },
                onNavigateToAdmin = {
                    navController.navigate(Screen.AdminPanel.route) {
                        popUpTo(Screen.BookList.route)
                        launchSingleTop = true
                    }
                },
            )
        }

        composable(Screen.AdminPanel.route) {
            AdminPanelScreen(onNavigateBack = { navController.popBackStack() })
        }

        composable(
            route = Screen.ForgotPassword.route,
            arguments = listOf(navArgument("email") { type = NavType.StringType; defaultValue = "" }),
        ) { backStackEntry ->
            val email = backStackEntry.arguments?.getString("email") ?: ""
            ForgotPasswordScreen(
                prefillEmail = email,
                onNavigateBack = { navController.popBackStack() },
                onCodeSent = { sentEmail ->
                    navController.navigate(Screen.ResetPassword.createRoute(sentEmail)) {
                        popUpTo(Screen.ForgotPassword.route) { inclusive = true }
                    }
                },
            )
        }

        composable(
            route = Screen.ResetPassword.route,
            arguments = listOf(navArgument("email") { type = NavType.StringType; defaultValue = "" }),
        ) { backStackEntry ->
            val email = backStackEntry.arguments?.getString("email") ?: ""
            ResetPasswordScreen(
                prefillEmail = email,
                onNavigateBack = { navController.popBackStack() },
                onResetSuccess = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                },
            )
        }
    }

    // Navigate to the screen indicated by a tapped notification (only when logged in).
    LaunchedEffect(notifScreen) {
        if (notifScreen == null || startDestination != Screen.BookList.route) return@LaunchedEffect
        when (notifScreen) {
            "admin_panel" -> navController.navigate(Screen.AdminPanel.route)
            "book_detail" -> notifBookId?.let { navController.navigate(Screen.BookDetail.createRoute(it)) }
            "book_list" -> { /* already on BookList — nothing to do */ }
        }
    }
}
