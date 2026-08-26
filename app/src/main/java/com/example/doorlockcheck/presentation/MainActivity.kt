/* While this template provides a good starting point for using Wear Compose, you can always
 * take a look at https://github.com/android/wear-os-samples/tree/main/ComposeStarter to find the
 * most up to date changes to the libraries and their usages.
 */

package com.example.doorlockcheck.presentation

import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
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
import com.example.doorlockcheck.R
import com.example.doorlockcheck.presentation.theme.DoorLockCheckTheme
import java.util.Locale

class MainActivity : ComponentActivity(), TextToSpeech.OnInitListener {
    private val viewModel: DoorLockViewModel by viewModels()
    private var tts: TextToSpeech? = null
    private var isTtsReady = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (intent?.action == ACTION_QUERY_LOCKED_STATUS || intent?.action == ACTION_CONFIRM_LOCKED) {
            tts = TextToSpeech(this, this)
            tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                override fun onDone(utteranceId: String?) {
                    if (utteranceId == UTTERANCE_ID) {
                        finish()
                    }
                }

                @Deprecated("Deprecated in Java")
                override fun onError(utteranceId: String?) {
                    finish()
                }

                override fun onStart(utteranceId: String?) = Unit
            })
            handleIntentAction(intent?.action)
        }
        showContent()
    }

    override fun onDestroy() {
        tts?.stop()
        tts?.shutdown()
        super.onDestroy()
    }

    override fun onInit(status: Int) {
        if (status != TextToSpeech.SUCCESS) return

        if (configureTextToSpeechLanguage()) {
            isTtsReady = true
            speakLockStatus()
        }
    }

    private fun handleIntentAction(action: String?) {
        when (action) {
            ACTION_CONFIRM_LOCKED -> viewModel.setLockedStatus(true)
            ACTION_QUERY_LOCKED_STATUS -> Unit
        }
    }

    private fun showContent() {
        setContent {
            WearApp(viewModel)
        }
    }

    private fun configureTextToSpeechLanguage(): Boolean {
        val languageResult = tts?.setLanguage(Locale.getDefault())
        return languageResult != TextToSpeech.LANG_MISSING_DATA &&
                languageResult != TextToSpeech.LANG_NOT_SUPPORTED
    }

    private fun speakLockStatus() {
        if (!isTtsReady) return

        tts?.speak(
            getLockStatusMessage(),
            TextToSpeech.QUEUE_FLUSH,
            null,
            UTTERANCE_ID,
        )
    }

    private fun getLockStatusMessage(): String {
        return if (viewModel.isLocked.value) {
            getString(R.string.tts_door_locked)
        } else {
            getString(R.string.tts_door_unlocked)
        }
    }

    companion object {
        private const val ACTION_CONFIRM_LOCKED = "com.example.doorlockcheck.action.CONFIRM_LOCKED"
        private const val ACTION_QUERY_LOCKED_STATUS =
            "com.example.doorlockcheck.action.QUERY_LOCKED_STATUS"
        private const val UTTERANCE_ID = "LockStatusQuery"
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