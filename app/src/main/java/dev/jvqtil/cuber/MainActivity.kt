package dev.jvqtil.cuber

import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import dev.jvqtil.cuber.database.CuberDatabase
import dev.jvqtil.cuber.database.SolveRepository
import dev.jvqtil.cuber.navigation.CuberNavHost
import dev.jvqtil.cuber.ui.theme.CuberTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        updateSystemBars()

        val database = CuberDatabase.getInstance(this)
        val repository = SolveRepository(database.solveDao())

        setContent {
            CuberTheme {
                val viewModel: CuberViewModel = viewModel(
                    factory = CuberViewModelFactory(repository)
                )

                CuberNavHost(viewModel)
            }
        }
    }

    private fun updateSystemBars() {
        val controller = WindowCompat.getInsetsController(
            window,
            window.decorView
        )

        when (resources.configuration.orientation) {
            Configuration.ORIENTATION_LANDSCAPE -> {
                controller.hide(
                    WindowInsetsCompat.Type.systemBars()
                )
            }

            else -> {
                controller.show(WindowInsetsCompat.Type.systemBars())
            }
        }
    }
}

private class CuberViewModelFactory(
    private val repository: SolveRepository
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(
        modelClass: Class<T>
    ): T {
        if (modelClass.isAssignableFrom(CuberViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return CuberViewModel(repository) as T
        }

        throw IllegalArgumentException(
            "Unknown ViewModel: ${modelClass.name}"
        )
    }
}