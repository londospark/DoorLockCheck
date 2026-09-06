/* While this template provides a good starting point for using Wear Compose, you can always
 * take a look at https://github.com/android/wear-os-samples/tree/main/ComposeStarter to find the
 * most up to date changes to the libraries and their usages.
 */

package dev.hubball.doorlockcheck.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.wear.compose.foundation.lazy.TransformingLazyColumn
import androidx.wear.compose.foundation.lazy.rememberTransformingLazyColumnState
import androidx.wear.compose.material3.AppScaffold
import androidx.wear.compose.material3.Button
import androidx.wear.compose.material3.ListHeader
import androidx.wear.compose.material3.ScreenScaffold
import androidx.wear.compose.material3.SurfaceTransformation
import androidx.wear.compose.material3.Text
import androidx.wear.compose.material3.lazy.rememberTransformationSpec
import androidx.wear.compose.material3.lazy.transformedHeight
import dev.hubball.doorlockcheck.R
import dev.hubball.doorlockcheck.data.DoorLockPreferencesRepository
import dev.hubball.doorlockcheck.presentation.theme.DoorLockCheckTheme

class MainActivity : ComponentActivity() {
    // by viewModels(): survives configuration changes and gets onCleared() properly.
    // (A previous revision used `by lazy` as a workaround for a malformed factory;
    // that quietly broke ViewModel lifecycle - see AGENTS.md.)
    private val viewModel: DoorLockViewModel by viewModels {
        object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T =
                DoorLockViewModel(DoorLockPreferencesRepository(this@MainActivity)) as T
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        showContent()
    }

    private fun showContent() {
        setContent {
            WearApp(viewModel)
        }
    }
}

@Composable
fun WearApp(viewModel: DoorLockViewModel) {
    val isLocked by viewModel.isLocked.collectAsState()
    DoorLockCheckTheme {
        AppScaffold {
            val listState = rememberTransformingLazyColumnState()
            val transformationSpec = rememberTransformationSpec()
            ScreenScaffold(
                scrollState = listState,
            ) { contentPadding ->
                TransformingLazyColumn(contentPadding = contentPadding, state = listState) {
                    item {
                        ListHeader(
                            modifier = Modifier.fillMaxWidth().transformedHeight(this, transformationSpec),
                            transformation = SurfaceTransformation(transformationSpec),
                        ) {
                            Text(text = if (isLocked) stringResource(R.string.door_is_locked) else stringResource(R.string.door_is_unlocked))
                        }
                    }
                    item {
                        Button(
                            onClick = { viewModel.toggleLockedStatus() },
                            modifier = Modifier.fillMaxWidth().transformedHeight(this, transformationSpec),
                            transformation = SurfaceTransformation(transformationSpec),
                        ) {
                            Text(text = if (isLocked) stringResource(R.string.door_unlock_button) else stringResource(R.string.door_lock_button))
                        }
                    }
                }
            }
        }
    }
}
