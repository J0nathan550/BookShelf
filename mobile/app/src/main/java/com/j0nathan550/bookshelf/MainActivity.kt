package com.j0nathan550.bookshelf

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.fragment.app.FragmentActivity
import androidx.navigation.compose.rememberNavController
import com.j0nathan550.bookshelf.data.repository.AuthRepository
import com.j0nathan550.bookshelf.ui.navigation.NavGraph
import com.j0nathan550.bookshelf.ui.navigation.Screen
import com.j0nathan550.bookshelf.ui.theme.BookShelfTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : FragmentActivity() {

    @Inject
    lateinit var authRepository: AuthRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            BookShelfTheme {
                val navController = rememberNavController()
                val startDestination =
                    if (authRepository.isLoggedIn() && !authRepository.isBiometricEnabled()) {
                        Screen.BookList.route
                    } else {
                        Screen.Login.route
                    }
                NavGraph(navController = navController, startDestination = startDestination)
            }
        }
    }
}
