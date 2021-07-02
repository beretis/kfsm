/*
 * Copyright (c) 2020-2021. Open JumpCO
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 */

package io.jumpco.open.kfsm.async

import io.jumpco.open.kfsm.AsyncStateAction
import io.jumpco.open.kfsm.TransitionType

class DefaultAsyncTransition<S, E, C, A, R>(
    internal val event: E,
    targetState: S?,
    targetMap: String?,
    automatic: Boolean,
    type: TransitionType,
    action: AsyncStateAction<C, A, R>?
) : AsyncTransition<S, E, C, A, R>(targetState, targetMap, automatic, type, action)
