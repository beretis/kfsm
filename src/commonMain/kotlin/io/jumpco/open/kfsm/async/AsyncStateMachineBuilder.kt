/*
    Copyright 2019-2021 Open JumpCO

    Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated
    documentation files (the "Software"), to deal in the Software without restriction, including without limitation
    the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software,
    and to permit persons to whom the Software is furnished to do so, subject to the following conditions:

    The above copyright notice and this permission notice shall be included in all copies or substantial
    portions of the Software.

    THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO
    THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
    AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF
    CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER
    DEALINGS IN THE SOFTWARE.
 */

@file:Suppress("RemoveExplicitTypeArguments")

package io.jumpco.open.kfsm.async

import io.jumpco.open.kfsm.AsyncStateAction
import io.jumpco.open.kfsm.AsyncStateChangeAction
import io.jumpco.open.kfsm.Condition
import io.jumpco.open.kfsm.DefaultAsyncStateAction
import io.jumpco.open.kfsm.DefaultEntryExitAction
import io.jumpco.open.kfsm.EventState
import io.jumpco.open.kfsm.StateGuard
import io.jumpco.open.kfsm.StateMapQuery
import io.jumpco.open.kfsm.StateQuery

class AsyncStateMachineBuilder<S, E, C, A, R>(validMapStates: Set<S>, internal val validEvents: Set<E>) {
    private var completed = false
    var defaultInitialState: S? = null
    private var deriveInitialState: StateQuery<C, S>? = null
    private var deriveInitialStateMap: StateMapQuery<C, S>? = null
    internal val defaultStateMap =
        AsyncStateMapBuilder<S, E, C, A, R>(validMapStates, null, this)
    internal val namedStateMaps: MutableMap<String, AsyncStateMapBuilder<S, E, C, A, R>> = mutableMapOf()
    internal var afterStateChangeAction: AsyncStateChangeAction<C, S>? = null
    /**
     * This function is used to provide a method for determining the initial state of the FSM using the provided content.
     * @param init Is a function that receives a context and returns the state that represents the context
     */
    fun initialState(init: StateQuery<C, S>) {
        require(!completed) { "Statemachine has been completed" }
        deriveInitialState = init
    }

    /**
     * This function is used to provide a method for determining the initial state of the FSM in cases where there may be one or more named statemaps.
     * In this case the state may be a stack or name / value pairs representing the map name and pushed state.
     * @param init Is a function that will use the context to provide a list which will be pushed on the stack. The last item in the list will become the current state.
     */
    fun initialStates(init: StateMapQuery<C, S>) {
        require(!completed) { "Statemachine has been completed" }
        deriveInitialStateMap = init
    }
    /**
     * This function defines an invariant condition that will hold true before and after all transitions.
     * @param message The message added to exception
     * @param condition A boolean expression applied to the context
     */
    fun invariant(message: String, condition: Condition<C>) {
        defaultStateMap.invariant(message, condition)
    }
    /**
     * This function will be use to define a named statemap.
     * @param name The name of the StateMap. It must be unique.
     * @param validStates The set of valid states for the StateMap.
     */
    fun stateMap(
        name: String,
        validStates: Set<S>
    ): AsyncStateMapBuilder<S, E, C, A, R> {
        require(name.trim().isNotEmpty()) { "statemap name must not be empty" }
        require(name != "default") { "Map cannot be named 'default'" }
        require(validStates.isNotEmpty()) { "Provide at least one entty in validStates" }
        val stateMapBuilder =
            AsyncStateMapBuilder<S, E, C, A, R>(validStates, name, this)
        namedStateMaps[name] = stateMapBuilder
        return stateMapBuilder
    }

    /**
     * This function enables completed for the state machine definition prevent further changes to the state
     * machine behaviour.
     */
    fun complete(): AsyncStateMachineDefinition<S, E, C, A, R> {
        completed = true
        if (namedStateMaps.isNotEmpty()) {
            require(this.deriveInitialState == null) { "deriveInitialState cannot be used with named state maps" }
        }
        return AsyncStateMachineDefinition(
            this.defaultInitialState,
            this.deriveInitialState,
            this.deriveInitialStateMap,
            this.defaultStateMap.toMap(this.afterStateChangeAction),
            this.namedStateMaps.map { Pair(it.key, it.value.toMap(this.afterStateChangeAction)) }.toMap()
        )
    }

    /**
     * This function can be used the define a new state machine.
     */
    inline fun stateMachine(handler: AsyncDslStateMachineHandler<S, E, C, A, R>.() -> Unit): AsyncDslStateMachineHandler<S, E, C, A, R> =
        AsyncDslStateMachineHandler(this).apply(handler)


    /**
     * This function will set an action that will be invoked when the state has changed and transition is complete.
     * This will be invoked after onEntry and onExit handlers.
     */
    fun afterStateChange(action: AsyncStateChangeAction<C, S>) {
        this.afterStateChangeAction = action
    }
    /**
     * This function defines a transition from the currentState equal to startState to the targetState when event is
     * received and the guard expression is met. The action is executed after any exit action and before entry actions.
     * @param startState The transition will be considered when currentState matches stateState
     * @param event The event will trigger the consideration of this transition
     * @param targetState The transition will change currentState to targetState after executing the option action
     * @param guard The guard expression will have to be met to consider the transition
     * @param action The optional action will be executed when the transition occurs.
     */
    fun transition(
        startState: S,
        event: E,
        targetState: S,
        guard: StateGuard<C, A>,
        action: AsyncStateAction<C, A, R>?
    ) =
        defaultStateMap.transition(startState, event, targetState, guard, action)

    /**
     * This function defines a transition that doesn't change the state also know as an internal transition.
     * The transition will only occur if the guard expression is met.
     * @param startState The transition will be considered when currentState matches stateState
     * @param event The event will trigger the consideration of this transition
     * @param guard The guard expression will have to be met to consider the transition
     * @param action The optional action will be executed when the transition occurs.
     */
    fun transition(startState: S, event: E, guard: StateGuard<C, A>, action: AsyncStateAction<C, A, R>?) =
        defaultStateMap.transition(startState, event, null, guard, action)

    /**
     * This function defines a transition that will be triggered when the currentState is the same as the startState and on is received. The FSM currentState will change to the targetState after the action was executed.
     * Entry and Exit actions will also be performed.
     * @param startState transition applies when FSM currentState is the same as stateState.
     * @param event transition applies when on received.
     * @param targetState FSM will transition to targetState.
     * @param action The actions will be invoked
     */
    fun transition(startState: S, event: E, targetState: S, action: AsyncStateAction<C, A, R>?) =
        defaultStateMap.transition(startState, event, targetState, null, action)

    /**
     * This function defines a transition that doesn't change the state of the state machine when the currentState is startState and the on is received and after the action was performed. No entry or exit actions performed.
     * @param startState transition applies when when FSM currentState is the same as stateState
     * @param event transition applies when on received
     * @param action actions will be invoked
     */
    fun transition(startState: S, event: E, action: AsyncStateAction<C, A, R>?) =
        defaultStateMap.transition(startState, event, null, null, action)

    /**
     * This function defines an action to be invoked when no action is found matching the current state and event.
     * This will be an internal transition and will not cause a change in state or invoke entry or exit functions.
     * @param action This action will be performed
     */
    fun defaultAction(action: DefaultAsyncStateAction<C, S, E, A, R>) =
        defaultStateMap.defaultAction(action)

    /**
     * This function defines an action to be invoked when no entry action is defined for the current state.
     * @param action This action will be invoked
     */
    fun defaultEntry(action: DefaultEntryExitAction<C, S, A>) =
        defaultStateMap.defaultEntry(action)

    /**
     * This function defines an action to be invoked when no exit action is defined for the current state.
     * @param action This action will be invoked
     */
    fun defaultExit(action: DefaultEntryExitAction<C, S, A>) =
        defaultStateMap.defaultExit(action)

    /**
     * This function defines an action to be invoked when no transitions are found matching the given state and on.
     * @param currentState The provided state
     * @param action This action will be invoked
     */
    fun default(currentState: S, action: DefaultAsyncStateAction<C, S, E, A, R>) =
        defaultStateMap.default(currentState, action)

    /**
     * This function defines an action to be invoked when no transitions match the event. The currentState will be change to second parameter of the Pair.
     * @param event The Pair holds the event and targetState and can be written as `event to state`
     * @param action The option action will be executed when this default transition occurs.
     */
    fun default(event: EventState<E, S>, action: AsyncStateAction<C, A, R>?) =
        defaultStateMap.default(event, action)

    /**
     * This function defines an action to be invoked when no transitions are found for given event.
     * @param event The event to match this transition.
     * @param action The option action will be executed when this default transition occurs.
     */
    fun default(event: E, action: AsyncStateAction<C, A, R>?) =
        defaultStateMap.default(event, action)

    /**
     * This function defines an action to be invoked when the FSM changes to the provided state
     * @param currentState The provided state
     * @param action This action will be invoked
     */
    fun entry(currentState: S, action: DefaultEntryExitAction<C, S, A>) =
        defaultStateMap.entry(currentState, action)

    /**
     * This function defines an action to be invoke when the FSM changes from the provided state
     * @param currentState The provided state
     * @param action This action will be invoked
     */
    fun exit(currentState: S, action: DefaultEntryExitAction<C, S, A>) =
        defaultStateMap.exit(currentState, action)
}

typealias AsyncAnyStateMachineBuilder<S, E, C> = AsyncStateMachineBuilder<S, E, C, Any, Any>
