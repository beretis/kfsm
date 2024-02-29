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

package io.jumpco.open.kfsm.async

import io.jumpco.open.kfsm.AsyncStateAction
import io.jumpco.open.kfsm.DefaultAsyncStateAction
import io.jumpco.open.kfsm.DefaultEntryExitAction
import io.jumpco.open.kfsm.EventState
import io.jumpco.open.kfsm.StateGuard

class AsyncDslStateMapEventHandler<S, E, C, A, R>(
    private val currentState: S,
    private val fsm: AsyncStateMapBuilder<S, E, C, A, R>
) {
    /**
     * Defines a default action when no other transition are matched
     * @param action The action will be performed when no matching state can be found
     */
    fun default(action: DefaultAsyncStateAction<C, S, E, A, R>) {
        fsm.default(currentState, action)
    }

    /**
     * Defines an action to be performed before transition causes a change in state
     * @param action The action will be performed when entering a new state
     */
    fun onEntry(action: DefaultEntryExitAction<C, S, A>) {
        fsm.entry(currentState, action)
    }

    /**
     * Defines an action to be performed after a transition has changed the state
     * @param action The action will be invoked when exiting a state.
     */
    fun onExit(action: DefaultEntryExitAction<C, S, A>) {
        fsm.exit(currentState, action)
    }

    /**
     * Defines a transition when the state is the currentState and the on is received. The state is changed to the targetState.
     * @param event A Pair with the first being the on and the second being the targetState.
     * @param action The action will be performed
     */
    fun onEvent(
        event: EventState<E, S>,
        action: AsyncStateAction<C, A, R>?
    ): AsyncDslStateMapEventHandler<S, E, C, A, R> {
        fsm.transition(currentState, event.first, event.second, null, action)
        return this
    }

    /**
     * Defines a guarded transition. Where the transition will only be used if the guarded expression is met
     * @param event The event and targetState the defines the transition
     * @param guard The guard expression must be met before the transition is considered.
     * @param action The optional action that may be executed
     */
    fun onEvent(
        event: EventState<E, S>,
        guard: StateGuard<C, A>,
        action: AsyncStateAction<C, A, R>?
    ): AsyncDslStateMapEventHandler<S, E, C, A, R> {
        fsm.transition(currentState, event.first, event.second, guard, action)
        return this
    }

    /**
     * Defines a transition where an on causes an action but doesn't change the state.
     * @param event The event and targetState the defines the transition
     * @param action The optional action that may be executed
     */
    fun onEvent(event: E, action: AsyncStateAction<C, A, R>?): AsyncDslStateMapEventHandler<S, E, C, A, R> {
        fsm.transition(currentState, event, null, null, action)
        return this
    }

    /**
     * Defines a guarded transition where an on causes an action but doesn't change the state and will only be used
     * if the guard expression is met. This will be an internal transition with no change in state.
     * @param event The event that will trigger the transition.
     * @param guard The guard expression must be met before the transition is considered.
     * @param action The optional action that may be executed
     */
    fun onEvent(
        event: E,
        guard: StateGuard<C, A>,
        action: AsyncStateAction<C, A, R>?
    ): AsyncDslStateMapEventHandler<S, E, C, A, R> {
        fsm.transition(currentState, event, null, guard, action)
        return this
    }

    /**
     * Defines a push transition on event for a targetMap and targetState.
     * @param event The event that will trigger the transition.
     * @param targetMap The named stateMap that will be targeted.
     * @param targetState The new state within the targetMap
     * @param action The optional action that may be executed.
     */
    fun onEventPush(
        event: E,
        targetMap: String,
        targetState: S,
        action: AsyncStateAction<C, A, R>?
    ): AsyncDslStateMapEventHandler<S, E, C, A, R> {
        fsm.pushTransition(currentState, event, targetMap, targetState, null, action)
        return this
    }

    /**
     * Defines a push transition on event for a targetMap and targetState when the guard evaluates to `true`
     * @param event The event that will trigger the transition.
     * @param targetMap The named stateMap that will be targeted.
     * @param targetState The new state within the targetMap
     * @param action The optional action that may be executed.
     */
    fun onEventPush(
        event: E,
        targetMap: String,
        targetState: S,
        guard: StateGuard<C, A>,
        action: AsyncStateAction<C, A, R>?
    ): AsyncDslStateMapEventHandler<S, E, C, A, R> {
        fsm.pushTransition(currentState, event, targetMap, targetState, guard, action)
        return this
    }

    fun onEventPop(event: E, action: AsyncStateAction<C, A, R>?): AsyncDslStateMapEventHandler<S, E, C, A, R> {
        fsm.popTransition(currentState, event, null, null, null, action)
        return this
    }

    fun onEventPop(event: Pair<E, S>, action: AsyncStateAction<C, A, R>?): AsyncDslStateMapEventHandler<S, E, C, A, R> {
        fsm.popTransition(currentState, event.first, event.second, null, null, action)
        return this
    }

    fun onEventPop(
        event: Pair<E, S>,
        guard: StateGuard<C, A>,
        action: AsyncStateAction<C, A, R>?
    ): AsyncDslStateMapEventHandler<S, E, C, A, R> {
        fsm.popTransition(currentState, event.first, event.second, null, guard, action)
        return this
    }

    fun onEventPop(
        event: E,
        targetMap: String,
        targetState: S,
        action: AsyncStateAction<C, A, R>?
    ): AsyncDslStateMapEventHandler<S, E, C, A, R> {
        fsm.popTransition(currentState, event, targetState, targetMap, null, action)
        return this
    }

    fun onEventPop(
        event: E,
        guard: StateGuard<C, A>,
        action: AsyncStateAction<C, A, R>?
    ): AsyncDslStateMapEventHandler<S, E, C, A, R> {
        fsm.popTransition(currentState, event, null, null, guard, action)
        return this
    }

    fun onEventPop(
        event: E,
        targetMap: String,
        targetState: S,
        guard: StateGuard<C, A>,
        action: AsyncStateAction<C, A, R>?
    ): AsyncDslStateMapEventHandler<S, E, C, A, R> {
        fsm.popTransition(currentState, event, targetState, targetMap, guard, action)
        return this
    }

    fun automatic(targetState: S, action: AsyncStateAction<C, A, R>?) {
        fsm.automatic(currentState, targetState, null, action)
    }

    fun automatic(targetState: S, guard: StateGuard<C, A>, action: AsyncStateAction<C, A, R>?) {
        fsm.automatic(currentState, targetState, guard, action)
    }

    fun timeout(targetState: S, timeout: Long, action: AsyncStateAction<C, A, R>?) {
        fsm.timeout(currentState, targetState, { timeout }, null, action)
    }

    fun timeout(targetState: S, timeout: C.() -> Long, action: AsyncStateAction<C, A, R>?) {
        fsm.timeout(currentState, targetState, timeout, null, action)
    }

    fun timeout(targetState: S, timeout: C.() -> Long, guard: StateGuard<C, A>, action: AsyncStateAction<C, A, R>?) {
        fsm.timeout(currentState, targetState, timeout, guard, action)
    }

    fun timeout(targetState: S, timeout: Long, guard: StateGuard<C, A>, action: AsyncStateAction<C, A, R>?) {
        fsm.timeout(currentState, targetState, { timeout }, guard, action)
    }

    fun automaticPop(targetState: S, action: AsyncStateAction<C, A, R>?) {
        fsm.automaticPop(currentState, null, targetState, null, action)
    }

    fun automaticPop(action: AsyncStateAction<C, A, R>?) {
        fsm.automaticPop(currentState, null, null, null, action)
    }

    fun automaticPop(targetMap: String, targetState: S, action: AsyncStateAction<C, A, R>?) {
        fsm.automaticPop(currentState, targetMap, targetState, null, action)
    }

    fun automaticPop(targetState: S, guard: StateGuard<C, A>, action: AsyncStateAction<C, A, R>?) {
        fsm.automaticPop(currentState, null, targetState, guard, action)
    }

    fun automaticPop(guard: StateGuard<C, A>, action: AsyncStateAction<C, A, R>?) {
        fsm.automaticPop(currentState, null, null, guard, action)
    }

    fun automaticPop(targetMap: String, targetState: S, guard: StateGuard<C, A>, action: AsyncStateAction<C, A, R>?) {
        fsm.automaticPop(currentState, targetMap, targetState, guard, action)
    }

    fun timeoutPop(targetState: S, timeout: Long, action: AsyncStateAction<C, A, R>?) {
        fsm.timeoutPop(currentState, null, targetState, { timeout }, null, action)
    }

    fun timeoutPop(targetState: S, timeout: C.() -> Long, action: AsyncStateAction<C, A, R>?) {
        fsm.timeoutPop(currentState, null, targetState, timeout, null, action)
    }

    fun timeoutPop(timeout: Long, action: AsyncStateAction<C, A, R>?) {
        fsm.timeoutPop(currentState, null, null, { timeout }, null, action)
    }

    fun timeoutPop(timeout: C.() -> Long, action: AsyncStateAction<C, A, R>?) {
        fsm.timeoutPop(currentState, null, null, timeout, null, action)
    }

    fun timeoutPop(targetMap: String, targetState: S, timeout: Long, action: AsyncStateAction<C, A, R>?) {
        fsm.timeoutPop(currentState, targetMap, targetState, { timeout }, null, action)
    }

    fun timeoutPop(targetMap: String, targetState: S, timeout: C.() -> Long, action: AsyncStateAction<C, A, R>?) {
        fsm.timeoutPop(currentState, targetMap, targetState, timeout, null, action)
    }

    fun timeoutPop(targetState: S, timeout: Long, guard: StateGuard<C, A>, action: AsyncStateAction<C, A, R>?) {
        fsm.timeoutPop(currentState, null, targetState, { timeout }, guard, action)
    }

    fun timeoutPop(targetState: S, timeout: C.() -> Long, guard: StateGuard<C, A>, action: AsyncStateAction<C, A, R>?) {
        fsm.timeoutPop(currentState, null, targetState, timeout, guard, action)
    }

    fun timeoutPop(timeout: Long, guard: StateGuard<C, A>, action: AsyncStateAction<C, A, R>?) {
        fsm.timeoutPop(currentState, null, null, { timeout }, guard, action)
    }

    fun timeoutPop(timeout: C.() -> Long, guard: StateGuard<C, A>, action: AsyncStateAction<C, A, R>?) {
        fsm.timeoutPop(currentState, null, null, timeout, guard, action)
    }

    fun timeoutPop(
        targetMap: String,
        targetState: S,
        timeout: C.() -> Long,
        guard: StateGuard<C, A>,
        action: AsyncStateAction<C, A, R>?
    ) {
        fsm.timeoutPop(currentState, targetMap, targetState, timeout, guard, action)
    }

    fun timeoutPop(
        targetMap: String,
        targetState: S,
        timeout: Long,
        guard: StateGuard<C, A>,
        action: AsyncStateAction<C, A, R>?
    ) {
        fsm.timeoutPop(currentState, targetMap, targetState, { timeout }, guard, action)
    }

    fun automaticPush(targetMap: String, targetState: S, action: AsyncStateAction<C, A, R>?) {
        fsm.automaticPush(currentState, targetMap, targetState, null, action)
    }

    fun automaticPush(targetMap: String, targetState: S, guard: StateGuard<C, A>, action: AsyncStateAction<C, A, R>?) {
        fsm.automaticPush(currentState, targetMap, targetState, guard, action)
    }

    fun timeoutPush(targetMap: String, targetState: S, timeout: C.() -> Long, action: AsyncStateAction<C, A, R>?) {
        fsm.timeoutPush(currentState, targetMap, targetState, timeout, null, action)
    }

    fun timeoutPush(targetMap: String, targetState: S, timeout: Long, action: AsyncStateAction<C, A, R>?) {
        fsm.timeoutPush(currentState, targetMap, targetState, { timeout }, null, action)
    }

    fun timeoutPush(
        targetMap: String,
        targetState: S,
        timeout: Long,
        guard: StateGuard<C, A>,
        action: AsyncStateAction<C, A, R>?
    ) {
        fsm.timeoutPush(currentState, targetMap, targetState, { timeout }, guard, action)
    }

    fun timeoutPush(
        targetMap: String,
        targetState: S,
        timeout: C.() -> Long,
        guard: StateGuard<C, A>,
        action: AsyncStateAction<C, A, R>?
    ) {
        fsm.timeoutPush(currentState, targetMap, targetState, timeout, guard, action)
    }
}
