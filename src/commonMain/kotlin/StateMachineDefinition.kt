/*
 * Copyright (c) 2019. Open JumpCO
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package io.jumpco.open.kfsm

/**
 * This class represents an immutable definition of a state machine.
 */
class StateMachineDefinition<S, E, C, A, R>(
    val defaultInitialState: S?,
    private val deriveInitialState: StateQuery<C, S>?,
    private val deriveInitialMap: StateMapQuery<C, S>?,
    /**
     * The top level state map will be created when the state machine is created.
     */
    val defaultStateMap: StateMapDefinition<S, E, C, A, R>,
    /**
     * The named state maps can be accessed via a push transition.
     */
    val namedStateMaps: Map<String, StateMapDefinition<S, E, C, A, R>>

) {
    private fun createMap(
        mapName: String,
        context: C,
        parentFsm: StateMachineInstance<S, E, C, A, R>,
        initial: S
    ) {
        if (mapName == "default") {
            parentFsm.pushMap(StateMapInstance(context, initial, null, parentFsm, defaultStateMap))
        } else {
            val stateMap = namedStateMaps[mapName] ?: error("Invalid map $mapName")
            parentFsm.pushMap(StateMapInstance(context, initial, mapName, parentFsm, stateMap))
        }
    }

    /**
     * This function will create a state machine instance provided with content and optional initialState.
     * @param context The context will be provided to actions
     * @param initialState If this is not provided the function defined in `initial` will be invoked to derive the initialState.
     * @see StateMachineBuilder.initialState
     */
    internal fun create(
        context: C,
        parentFsm: StateMachineInstance<S, E, C, A, R>,
        initialState: S? = null,
        intitialExternalState: ExternalState<S>? = null
    ): StateMapInstance<S, E, C, A, R> {
        if (intitialExternalState != null) {
            intitialExternalState.forEach { (initial, mapName) ->
                createMap(mapName, context, parentFsm, initial)
            }
            return parentFsm.mapStack.pop()
        } else if (deriveInitialMap != null) {
            deriveInitialMap.invoke(context).forEach { (initial, mapName) ->
                createMap(mapName, context, parentFsm, initial)
            }
            return parentFsm.mapStack.pop()
        } else {
            val initial = initialState ?: deriveInitialState?.invoke(context) ?: defaultInitialState
            ?: error("Definition requires deriveInitialState or deriveInitialMap")
            return StateMapInstance(context, initial, null, parentFsm, defaultStateMap)
        }
    }

    internal fun createStateMap(
        name: String,
        context: C,
        parentFsm: StateMachineInstance<S, E, C, A, R>,
        initialState: S
    ): StateMapInstance<S, E, C, A, R> =
        StateMapInstance(
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
    fun create(context: C, initialExternalState: ExternalState<S>): StateMachineInstance<S, E, C, A, R> =
        StateMachineInstance<S, E, C, A, R>(context, this, null, initialExternalState)

    /**
     * This function will create a state machine instance and set it to the initial state.
     * @param context The instance will operate on the provided context
     * @param initial The initial state
     *
     */
    fun create(context: C, initial: S? = null): StateMachineInstance<S, E, C, A, R> =
        StateMachineInstance<S, E, C, A, R>(context, this, initial)

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
