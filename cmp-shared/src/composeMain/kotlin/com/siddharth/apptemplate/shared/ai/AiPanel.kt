package com.siddharth.apptemplate.shared.ai

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.siddharth.kmp.ai.OnDeviceLlm
import com.siddharth.kmp.llmchat.AiConfig
import com.siddharth.kmp.llmchat.AiMessage
import com.siddharth.kmp.llmchat.AiProvider
import com.siddharth.kmp.result.AiCapabilities
import com.siddharth.kmp.result.AiFailure
import com.siddharth.kmp.result.AiResult
import com.siddharth.kmp.result.Result
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** The AI backend the Home panel talks to — on-device first, escalating to the configured cloud chain. */
class HomeAiBackend(delegate: OnDeviceLlm) : OnDeviceLlm by delegate

/**
 * The cloud chain's last resort with no key configured — always unavailable, so
 * [com.siddharth.kmp.ai.CloudOnDeviceLlm] reports [AiFailure.NoKey] instead of the panel hanging
 * with no answer and no explanation.
 */
class OfflineFallbackProvider : AiProvider {
    override val id: String = "offline-fallback"
    override val displayName: String = "Offline"

    override suspend fun isAvailable(): Boolean = false

    override suspend fun complete(
        messages: List<AiMessage>,
        config: AiConfig,
    ): AiResult<String> = Result.Failure(AiFailure.NoKey)
}

data class AiPanelUiState(
    /** Null until the first [OnDeviceLlm.capabilities] read completes. */
    val capabilities: AiCapabilities? = null,
    val answer: String = "",
    val isStreaming: Boolean = false,
)

/**
 * State behind the Home AI panel. [backend] is whatever [OnDeviceLlm] the app wired in Koin —
 * [aiModule][com.siddharth.apptemplate.shared.di.aiModule] binds [HomeAiBackend]. No Compose
 * dependency beyond [StateFlow], so a fake backend is enough to unit-test [ask]/[stop] without a
 * Compose test rule (see AiPanelStateTest).
 */
class AiPanelState(
    private val backend: OnDeviceLlm,
    private val scope: CoroutineScope,
) {
    private val _state = MutableStateFlow(AiPanelUiState())
    val state: StateFlow<AiPanelUiState> = _state.asStateFlow()

    private var askJob: Job? = null

    init {
        scope.launch {
            val caps = backend.capabilities()
            _state.update { it.copy(capabilities = caps) }
        }
    }

    /** Why the panel can't answer right now — no key, no on-device model, wrong platform. Null once ready. */
    val unavailableReason: AiFailure? get() = _state.value.capabilities?.unavailableReason

    /** Streams [prompt] into [state]'s `answer`; a no-op while unavailable or already streaming. */
    fun ask(prompt: String) {
        if (prompt.isBlank() || unavailableReason != null || _state.value.isStreaming) return
        askJob?.cancel()
        _state.update { it.copy(answer = "", isStreaming = true) }
        askJob =
            scope.launch {
                backend.generateStream(prompt).collect { token ->
                    _state.update { it.copy(answer = it.answer + token) }
                }
                _state.update { it.copy(isStreaming = false) }
            }
    }

    /** Cancels the in-flight [ask] — cancelling the collecting coroutine tears down the underlying call. */
    fun stop() {
        askJob?.cancel()
        _state.update { it.copy(isStreaming = false) }
    }
}

@Composable
fun AiPanel(
    state: AiPanelState,
    modifier: Modifier = Modifier,
) {
    val uiState by state.state.collectAsState()
    var prompt by remember { mutableStateOf("") }

    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Ask AI", style = MaterialTheme.typography.titleMedium)
        when {
            uiState.capabilities == null ->
                Text("Checking AI availability…", style = MaterialTheme.typography.bodySmall)
            uiState.capabilities?.unavailableReason != null ->
                Text(
                    "AI is off on this build (${uiState.capabilities?.unavailableReason?.label()}). " +
                        "Add a provider key in Settings to enable it.",
                    style = MaterialTheme.typography.bodySmall,
                )
            else -> {
                OutlinedTextField(
                    value = prompt,
                    onValueChange = { prompt = it },
                    label = { Text("Ask something") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(onClick = { state.ask(prompt) }, enabled = !uiState.isStreaming && prompt.isNotBlank()) {
                        Text("Ask")
                    }
                    // Stopping only makes sense for a backend that genuinely streams — a
                    // non-streaming reply already finished before it appeared.
                    if (uiState.isStreaming && uiState.capabilities?.streaming == true) {
                        TextButton(onClick = { state.stop() }) { Text("Stop") }
                    }
                }
                if (uiState.answer.isNotBlank() || uiState.isStreaming) {
                    Text(uiState.answer.ifBlank { "…" }, modifier = Modifier.padding(top = 8.dp))
                }
            }
        }
    }
}

private fun AiFailure.label(): String =
    when (this) {
        AiFailure.NoKey -> "no key saved"
        AiFailure.Unauthorized -> "key rejected"
        AiFailure.RateLimited -> "rate limited"
        AiFailure.Timeout -> "timed out"
        AiFailure.Network -> "network error"
        AiFailure.ModelNotResident -> "model not downloaded yet"
        AiFailure.NotSupportedOnPlatform -> "not supported on this platform"
        AiFailure.EmptyReply -> "model returned no reply"
    }
