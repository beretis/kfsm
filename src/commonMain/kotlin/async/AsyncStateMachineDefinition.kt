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

import io.jumpco.open.kfsm.ExternalState
import io.jumpco.open.kfsm.StateMapQuery
import io.jumpco.open.kfsm.StateQuery

class AsyncStateMachineDefinition<S, E, C, A, R>(
    val defaultInitialState: S?,
    private val deriveInitialState: StateQuery<C, S>?,
    private val deriveInitialMap: StateMapQuery<C, S>?,
    /**
     * The top level state map will be created when the state machine is created.
     */
    val defaultStateMap: AsyncStateMapDefinition<S, E, C, A, R>,
    /**
     * The named state maps can be accessed via a push transition.
     */
    val namedStateMaps: Map<String, AsyncStateMapDefinition<S, E, C, A, R>>

) {
    private fun createMap(
        mapName: String,
        context: C,
        parentFsm: AsyncStateMachineInstance<S, E, C, A, R>,
        initial: S
    ) {
        if (mapName == "default") {
            parentFsm.pushMap(
                AsyncStateMapInstance(
                    context,
                    initial,
                    null,
                    parentFsm,
                    defaultStateMap
                )
            )
        } else {
            val stateMap = namedStateMaps[mapName] ?: error("Invalid map $mapName")
            parentFsm.pushMap(
                AsyncStateMapInstance(
                    context,
                    initial,
                    mapName,
                    parentFsm,
                    stateMap
                )
            )
        }
    }

    /**
     * This function will create a state machine instance provided with content and optional initialState.
     * @param context The context will be provided to actions
     * @param initialState If this is not provided the function defined in `initial` will be invoked to derive the initialState.
     */
    internal fun create(
        context: C,
        parentFsm: AsyncStateMachineInstance<S, E, C, A, R>,
        initialState: S? = null,
        intitialExternalState: ExternalState<S>? = null
    ): AsyncStateMapInstance<S, E, C, A, R> {
        when {
            intitialExternalState != null -> {
                intitialExternalState.forEach { (initial, mapName) ->
                    createMap(mapName, context, parentFsm, initial)
                }
                return parentFsm.mapStack.pop()
            }
            deriveInitialMap != null -> {
                deriveInitialMap.invoke(context).forEach { (initial, mapName) ->
                    createMap(mapName, context, parentFsm, initial)
                }
                return parentFsm.mapStack.pop()
            }
            else -> {
                val initial = initialState ?: deriveInitialState?.invoke(context) ?: defaultInitialState
                ?: error("Definition requires deriveInitialState or deriveInitialMap")
                return AsyncStateMapInstance(
                    context,
                    initial,
                    null,
                    parentFsm,
                    defaultStateMap
                )
            }
        }
    }

    internal fun createStateMap(
        name: String,
        context: C,
        parentFsm: AsyncStateMachineInstance<S, E, C, A, R>,
        initialState: S
    ): AsyncStateMapInstance<S, E, C, A, R> =
        AsyncStateMapInstance(
            context,
            initialState,
            name,
            parentFsm,
            namedStateMaps[name] ?: error("Named map $name not found")
        )

    /**
     * This function will create a state machine instance and set it to the state to a previously externalised state.
     * @param context The instance will operate on the provided context
     * @param initialExternalState The previously externalised state
     */
    fun create(context: C, initialExternalState: ExternalState<S>): AsyncStateMachineInstance<S, E, C, A, R> =
        AsyncStateMachineInstance<S, E, C, A, R>(
            context,
            this,
            null,
            initialExternalState
        )

    /**
     * This function will create a state machine instance and set it to the initial state.
     * @param context The instance will operate on the provided context
     * @param initial The initial state
     *
     */
    fun create(context: C, initial: S? = null): AsyncStateMachineInstance<S, E, C, A, R> =
        AsyncStateMachineInstance<S, E, C, A, R>(context, this, initial)

    /**
     * This function will provide a list of possible events given a specific state.
     * The actual events may fail because of guard conditions or named state maps and the default state map behaviour being different.
     * @param state The given state
     * @param includeDefault consider the default state and event handlers
     */
    fun possibleEvents(state: S, includeDefault: Boolean): Set<E> {
        val result = mutableSetOf<E>()
        result.addAll(defaultStateMap.allowed(state, includeDefault))
        namedStateMaps.values.forEach {
            if (it.validStates.contains(state)) {
                result.addAll(it.allowed(state, includeDefault))
            }
        }
        return result.toSet()
    }
}
