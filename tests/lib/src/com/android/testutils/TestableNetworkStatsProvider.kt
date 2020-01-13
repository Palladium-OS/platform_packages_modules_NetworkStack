/*
 * Copyright (C) 2020 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.testutils

import android.net.netstats.provider.INetworkStatsProvider
import kotlin.test.assertEquals
import kotlin.test.fail

private const val DEFAULT_TIMEOUT_MS = 200L

open class TestableNetworkStatsProvider : INetworkStatsProvider.Stub() {
    sealed class CallbackType {
        data class RequestUpdate(val token: Int) : CallbackType()
        data class SetLimit(val iface: String?, val quotaBytes: Long) : CallbackType()
        data class SetAlert(val quotaBytes: Long) : CallbackType()
    }

    private val history = ArrayTrackRecord<CallbackType>().ReadHead()

    override fun requestStatsUpdate(token: Int) {
        history.add(CallbackType.RequestUpdate(token))
    }

    override fun setLimit(iface: String?, quotaBytes: Long) {
        history.add(CallbackType.SetLimit(iface, quotaBytes))
    }

    override fun setAlert(quotaBytes: Long) {
        history.add(CallbackType.SetAlert(quotaBytes))
    }

    fun expectStatsUpdate(token: Int) {
        assertEquals(CallbackType.RequestUpdate(token), history.poll(DEFAULT_TIMEOUT_MS))
    }

    fun expectSetLimit(iface: String?, quotaBytes: Long) {
        assertEquals(CallbackType.SetLimit(iface, quotaBytes), history.poll(DEFAULT_TIMEOUT_MS))
    }

    fun expectSetAlert(quotaBytes: Long) {
        assertEquals(CallbackType.SetAlert(quotaBytes), history.poll(DEFAULT_TIMEOUT_MS))
    }

    @JvmOverloads
    fun assertNoCallback(timeout: Long = DEFAULT_TIMEOUT_MS) {
        val cb = history.poll(timeout)
        cb?.let { fail("Expected no callback but got $cb") }
    }
}
