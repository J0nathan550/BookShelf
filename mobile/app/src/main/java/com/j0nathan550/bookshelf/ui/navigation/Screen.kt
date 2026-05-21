package com.j0nathan550.bookshelf.ui.navigation

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Register : Screen("register")

    object EmailVerification : Screen("email_verification/{userId}") {
        fun createRoute(userId: String) = "email_verification/$userId"
    }
    object BookList : Screen("books")
    object LentBooks : Screen("lent_books")
    object Statistics : Screen("statistics")
    object AddBook : Screen("add_book")

    object BookDetail : Screen("book/{bookId}") {
        fun createRoute(bookId: Int) = "book/$bookId"
    }

    object EditBook : Screen("edit_book/{bookId}") {
        fun createRoute(bookId: Int) = "edit_book/$bookId"
    }

    object AdminPanel : Screen("admin_panel")
    object Account : Screen("account")

    object ForgotPassword : Screen("forgot_password/{email}") {
        fun createRoute(email: String = "") = "forgot_password/${android.net.Uri.encode(email)}"
    }

    object ResetPassword : Screen("reset_password/{email}") {
        fun createRoute(email: String = "") = "reset_password/${android.net.Uri.encode(email)}"
    }

    object BarcodeScanner : Screen("barcode_scanner")
}
