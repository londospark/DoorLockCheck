/* While this template provides a good starting point for using Wear Compose, you can always
 * take a look at https://github.com/android/wear-os-samples/tree/main/ComposeStarter to find the
 * most up to date changes to the libraries and their usages.
 */

package com.example.doorlockcheck.presentation

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
import androidx.wear.compose.ui.tooling.preview.WearPreviewDevices
import androidx.wear.compose.ui.tooling.preview.WearPreviewFontScales
import com.example.doorlockcheck.R
import com.example.doorlockcheck.presentation.theme.DoorLockCheckTheme

class MainActivity : ComponentActivity() {
    private val viewModel: DoorLockViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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
            ) { contentPadding -> // ScreenScaffold provides default padding; adjust as needed
                TransformingLazyColumn(contentPadding = contentPadding, state = listState) {
                    item {
                        ListHeader(
                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .transformedHeight(this, transformationSpec),
                            transformation = SurfaceTransformation(transformationSpec),
                        ) {
                            Text(text = if (isLocked) stringResource(R.string.door_is_locked) else stringResource(
                                R.string.door_is_unlocked
                            ))
                        }
                    }
                    item {
                        Button(
                            onClick = { viewModel.setLockedStatus(true) },
                            enabled = !isLocked,
                            modifier = Modifier
                                .fillMaxWidth()
                                .transformedHeight(this, transformationSpec),
                            transformation = SurfaceTransformation(transformationSpec),
                        ) {
                            Text(stringResource(R.string.door_lock_button))
                        }
                    }
                }
            }
        }
    }
}

@WearPreviewDevices
@WearPreviewFontScales
@Composable
fun DefaultPreview() {
    WearApp(DoorLockViewModel())
}