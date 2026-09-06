package com.siddharth.apptemplate.shared.di

import com.siddharth.apptemplate.shared.ai.HomeAiBackend
import com.siddharth.apptemplate.shared.ai.OfflineFallbackProvider
import com.siddharth.kmp.ai.CloudOnDeviceLlm
import com.siddharth.kmp.ai.CompositeOnDeviceLlm
import com.siddharth.kmp.ai.OnDeviceLlm
import com.siddharth.kmp.ai.UnavailableOnDeviceLlm
import com.siddharth.kmp.llmchat.AiProviderConfig
import com.siddharth.kmp.llmchat.buildProviderChain
import org.koin.dsl.module

/**
 * Every app forked from this template gets this for free: an [OnDeviceLlm] wired from kmp-toolkit's
 * `:ai`, escalating through `:llm-chat`'s cloud provider chain — one [com.siddharth.kmp.result.AiResult]/
 * [com.siddharth.kmp.result.AiCapabilities] vocabulary both seams already share, so the Home panel
 * (see AiPanel.kt) never invents its own error shape. No cloud key is configured out of the box
 * ([AiProviderConfig] defaults to none), so a fresh fork's panel reports
 * [com.siddharth.kmp.result.AiFailure.NoKey] honestly — the empty state — instead of silently
 * pretending to work. Wire a real key through `:llm-chat`'s `SecureKeyStore` (the ai-settings-ui
 * lane) without touching this file.
 *
 * // ponytail: the on-device tier is [UnavailableOnDeviceLlm], not `:ai`'s real per-platform
 * // detection (`onDeviceLlmModule()` — ML Kit/MediaPipe/Foundation Models). That function's
 * // Android actual needs `startKoin { androidContext(this) }` threaded through every platform
 * // entry point before first use — real scope for a fork that actually wants on-device inference,
 * // not this scaffold's default. Swap `single<OnDeviceLlm> { UnavailableOnDeviceLlm }` below for
 * // `includes(onDeviceLlmModule())` (plus the androidContext wiring) when a fork needs it.
 */
fun aiModule() =
    module {
        single<OnDeviceLlm> { UnavailableOnDeviceLlm }
        single {
            HomeAiBackend(
                CompositeOnDeviceLlm(
                    listOf(
                        get<OnDeviceLlm>(),
                        CloudOnDeviceLlm(buildProviderChain(AiProviderConfig(), OfflineFallbackProvider())),
                    ),
                ),
            )
        }
    }
