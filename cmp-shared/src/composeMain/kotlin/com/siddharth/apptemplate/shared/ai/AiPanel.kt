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
import androidx.compose.ui.tooling.preview.Preview
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
class HomeAiBackend(
    delegate: OnDeviceLlm,
) : OnDeviceLlm by delegate

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
    AiPanelContent(uiState, onAsk = state::ask, onStop = state::stop, modifier = modifier)
}

/**
 * The panel's UI with no [AiPanelState] behind it, so every branch below is reachable from a
 * `@Preview` by passing a plain [AiPanelUiState]. Splitting stateless content out of the
 * state-holding wrapper is the only reason the previews at the bottom of this file can exist:
 * [AiPanelState] resolves a real backend and updates itself from a coroutine, and the IDE preview
 * renderer runs no effects, so a preview built on it would be stuck on "Checking AI availability…"
 * forever. Hoist the state, preview the content.
 */
@Composable
private fun AiPanelContent(
    uiState: AiPanelUiState,
    onAsk: (String) -> Unit,
    onStop: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var prompt by remember { mutableStateOf("") }
    // Bound once so the branches below smart-cast instead of repeating `uiState.capabilities?.`.
    val caps = uiState.capabilities

    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Ask AI", style = MaterialTheme.typography.titleMedium)
        when {
            caps == null ->
                Text("Checking AI availability…", style = MaterialTheme.typography.bodySmall)
            caps.unavailableReason != null ->
                Text(
                    "AI is off on this build (${caps.unavailableReason?.label()}). " +
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
                    Button(onClick = { onAsk(prompt) }, enabled = !uiState.isStreaming && prompt.isNotBlank()) {
                        Text("Ask")
                    }
                    // Stopping only makes sense for a backend that genuinely streams — a
                    // non-streaming reply already finished before it appeared.
                    if (uiState.isStreaming && caps.streaming) {
                        TextButton(onClick = onStop) { Text("Stop") }
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

// ---- Previews -------------------------------------------------------------------------------
//
// Three previews, one per branch of AiPanelContent's `when`: not-yet-known, unavailable, ready.
// They cover the states that are easy to get wrong and impossible to see in the running app
// (a fresh fork ships no provider key, so only the NoKey branch ever renders for real).
//
// FORK NOTE: previews render through the ANDROID preview tooling even here in shared code, so
// this module needs its Android target and the ui-tooling renderer — see cmp-shared/build.gradle.kts.

private val READY_CAPS =
    AiCapabilities(streaming = true, multimodal = false, honoredConfigFields = emptySet(), unavailableReason = null)

@Preview
@Composable
private fun AiPanelCheckingPreview() {
    MaterialTheme { AiPanelContent(AiPanelUiState(), onAsk = {}, onStop = {}) }
}

@Preview
@Composable
private fun AiPanelNoKeyPreview() {
    MaterialTheme {
        AiPanelContent(
            AiPanelUiState(
                capabilities =
                    AiCapabilities(
                        streaming = false,
                        multimodal = false,
                        honoredConfigFields = emptySet(),
                        unavailableReason = AiFailure.NoKey,
                    ),
            ),
            onAsk = {},
            onStop = {},
        )
    }
}

@Preview
@Composable
private fun AiPanelStreamingPreview() {
    MaterialTheme {
        AiPanelContent(
            AiPanelUiState(capabilities = READY_CAPS, answer = "Kotlin Multiplatform shares", isStreaming = true),
            onAsk = {},
            onStop = {},
        )
    }
}
