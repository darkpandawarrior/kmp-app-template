package com.siddharth.apptemplate.shared.ai

import com.siddharth.kmp.ai.LlmPart
import com.siddharth.kmp.ai.OnDeviceLlm
import com.siddharth.kmp.result.AiCapabilities
import com.siddharth.kmp.result.AiFailure
import com.siddharth.kmp.result.AiResult
import com.siddharth.kmp.result.Result
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

/**
 * Scripted [OnDeviceLlm]: reports [caps], and streams [tokens] one per virtual-time tick — the
 * `delay` gives a test a real mid-stream point to call `stop()` at, instead of the whole answer
 * landing before the caller can react.
 */
private class FakeBackend(
    private val caps: AiCapabilities,
    private val tokens: List<String> = listOf("Hel", "lo"),
) : OnDeviceLlm {
    override fun isAvailable(): Boolean = caps.unavailableReason == null

    override suspend fun capabilities(): AiCapabilities = caps

    override suspend fun generate(prompt: String): AiResult<String> = Result.Success(tokens.joinToString(""))

    override fun generateStream(prompt: String): Flow<String> =
        flow {
            tokens.forEach { token ->
                emit(token)
                delay(1)
            }
        }

    override suspend fun generate(parts: List<LlmPart>): AiResult<String> = generate((parts.single() as LlmPart.Text).text)
}

private val AVAILABLE_CAPS = AiCapabilities(streaming = true, multimodal = false, honoredConfigFields = emptySet(), unavailableReason = null)
private val NO_KEY_CAPS = AiCapabilities(streaming = false, multimodal = false, honoredConfigFields = emptySet(), unavailableReason = AiFailure.NoKey)

@OptIn(ExperimentalCoroutinesApi::class)
class AiPanelStateTest {
    @Test
    fun `reports the no-key empty state and refuses to ask`() =
        runTest {
            val state = AiPanelState(FakeBackend(NO_KEY_CAPS), this)
            advanceUntilIdle()

            assertEquals(AiFailure.NoKey, state.unavailableReason)

            state.ask("hello")
            advanceUntilIdle()

            assertEquals("", state.state.value.answer)
        }

    @Test
    fun `streams tokens into the answer while available`() =
        runTest {
            val state = AiPanelState(FakeBackend(AVAILABLE_CAPS), this)
            advanceUntilIdle()
            assertNull(state.unavailableReason)

            state.ask("hello")
            advanceUntilIdle()

            assertEquals("Hello", state.state.value.answer)
            assertEquals(false, state.state.value.isStreaming)
        }

    @Test
    fun `stop cancels the in-flight stream`() =
        runTest {
            val state = AiPanelState(FakeBackend(AVAILABLE_CAPS), this)
            advanceUntilIdle()

            state.ask("hello")
            runCurrent() // let only the first token land
            state.stop()
            advanceUntilIdle() // if cancellation didn't take, this would deliver "lo" too

            assertEquals("Hel", state.state.value.answer)
            assertEquals(false, state.state.value.isStreaming)
        }
}
