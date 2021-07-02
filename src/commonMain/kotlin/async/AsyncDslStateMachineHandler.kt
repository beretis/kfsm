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

import io.jumpco.open.kfsm.AsyncStateChangeAction
import io.jumpco.open.kfsm.StateMapQuery
import io.jumpco.open.kfsm.StateQuery

class AsyncDslStateMachineHandler<S, E, C, A, R>(private val fsm: AsyncStateMachineBuilder<S, E, C, A, R>) {
    /**
     * Defines an expression that will determine the initial state of the state machine based on the values of the context.
     * @param deriveInitialState A lambda expression receiving context:C and returning state S.
     */
    fun initialState(deriveInitialState: StateQuery<C, S>): AsyncDslStateMachineHandler<S, E, C, A, R> {
        fsm.initialState(deriveInitialState)
        return this
    }

    var defaultInitialState: S?
        get() = fsm.defaultInitialState
        set(state) {
            fsm.defaultInitialState = state
        }

    /**
     * Provides for a list of pairs with state and map name that will be pushed and the last entry will be popped and become the current map.
     * This is required when using state machine with named maps.
     *
    ```
    initialStates {
        mutableListOf<StateMapItem<PayingTurnstileStates>>().apply {
            if (locked) {
                this.add(PayingTurnstileStates.LOCKED to "default")
            } else {
                this.add(PayingTurnstileStates.UNLOCKED to "default")
            }
            if (coins > 0) {
                this.add(PayingTurnstileStates.COINS to "coins")
            }
        }
    }
    ```
     */
    fun initialStates(deriveInitialMap: StateMapQuery<C, S>): AsyncDslStateMachineHandler<S, E, C, A, R> {
        fsm.initialStates(deriveInitialMap)
        return this
    }

    /**
     * Defines an action that will be invoked after a transition to a new state.
     * Any exceptions thrown by the action will be ignored.
     */
    fun onStateChange(action: AsyncStateChangeAction<C, S>) {
        fsm.afterStateChange(action)
    }
    /**
     * Defines a section for a specific state.
     * @param currentState The give state
     * @param handler A lambda with definitions for the given state
     */
    fun whenState(currentState: S, handler: AsyncDslStateMapEventHandler<S, E, C, A, R>.() -> Unit):
        AsyncDslStateMapEventHandler<S, E, C, A, R> =
        AsyncDslStateMapEventHandler(currentState, fsm.defaultStateMap).apply(handler)

    /**
     * Defines a section for default behaviour for the state machine.
     * @param handler A lambda with definition for the default behaviour of the state machine.
     */
    fun default(handler: AsyncDslStateMapDefaultEventHandler<S, E, C, A, R>.() -> Unit):
        AsyncDslStateMapDefaultEventHandler<S, E, C, A, R> =
        AsyncDslStateMapDefaultEventHandler(fsm.defaultStateMap).apply(handler)

    /**
     * Returns the completed fsm.
     */
    fun build() = fsm.complete()

    /**
     * creates a named statemap
     */
    fun stateMap(
        /**
         * The name of the state map. <ust be unique and not empty
         */
        name: String,
        /**
         * A set of valid states for the state map
         */
        validStates: Set<S>,
        /**
         * The lambda to configure the statemap
         */
        handler: AsyncDslStateMapHandler<S, E, C, A, R>.() -> Unit
    ): AsyncDslStateMapHandler<S, E, C, A, R> {
        return AsyncDslStateMapHandler(
            fsm.stateMap(
                name.trim(),
                validStates
            )
        ).apply(handler)
    }
}
