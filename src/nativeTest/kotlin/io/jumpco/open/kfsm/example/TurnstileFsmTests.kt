/*
 * Copyright (c) 2021. Open JumpCO
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package io.jumpco.open.kfsm.example.io.jumpco.open.kfsm.example

import io.jumpco.open.kfsm.AnyStateMachineBuilder
import io.jumpco.open.kfsm.AnyStateMachineInstance
import io.jumpco.open.kfsm.example.Turnstile
import io.jumpco.open.kfsm.example.TurnstileEvents
import io.jumpco.open.kfsm.example.TurnstileEvents.COIN
import io.jumpco.open.kfsm.example.TurnstileEvents.PASS
import io.jumpco.open.kfsm.example.TurnstileFSM
import io.jumpco.open.kfsm.example.TurnstileStates
import io.jumpco.open.kfsm.example.TurnstileStates.LOCKED
import io.jumpco.open.kfsm.example.TurnstileStates.UNLOCKED
import io.jumpco.open.kfsm.stateMachine
import kotlin.test.Test
import kotlin.test.assertTrue

/**
 * @suppress
 */
class TurnstileFsmTests {
    private fun verifyTurnstileFSM(
        fsm: AnyStateMachineInstance<TurnstileStates, TurnstileEvents, Turnstile>,
        turnstile: Turnstile
    ) {

        assertTrue { fsm.currentState == LOCKED }
        assertTrue { turnstile.locked }
        // when
        fsm.sendEvent(COIN)
        // then
        assertTrue { fsm.currentState == UNLOCKED }
        assertTrue { !turnstile.locked }
        // when
        fsm.sendEvent(COIN)
        // then
        assertTrue { fsm.currentState == UNLOCKED }
        assertTrue { !turnstile.locked }
        // when
        fsm.sendEvent(PASS)
        // then
        assertTrue { fsm.currentState == LOCKED }
        assertTrue { turnstile.locked }
        // when
        fsm.sendEvent(PASS)
        // then
        assertTrue { fsm.currentState == LOCKED }
        assertTrue { turnstile.locked }
    }

    @Test
    fun turnstilePlain() {
        val builder = AnyStateMachineBuilder<TurnstileStates, TurnstileEvents, Turnstile>(
            TurnstileStates.values().toSet(),
            TurnstileEvents.values().toSet()
        )
        builder.initialState { if (locked) LOCKED else UNLOCKED }
        builder.transition(LOCKED, COIN, UNLOCKED) {
            unlock()
        }
        builder.transition(LOCKED, PASS) {
            alarm()
        }
        builder.transition(UNLOCKED, COIN) {
            returnCoin()
        }
        builder.transition(UNLOCKED, PASS, LOCKED) {
            lock()
        }
        val definition = builder.complete()
        // when
        val turnstile = Turnstile()

        val fsm = definition.create(turnstile)
        // then
        verifyTurnstileFSM(fsm, turnstile)
    }

    @Test
    fun turnstileDSL() {
        // given
        val definition =
            stateMachine(
                TurnstileStates.values().toSet(),
                TurnstileEvents.values().toSet(),
                Turnstile::class
            ) {
                initialState { if (locked) LOCKED else UNLOCKED }
                whenState(LOCKED) {
                    onEvent(COIN to UNLOCKED) {
                        unlock()
                    }
                    onEvent(PASS) {
                        alarm()
                    }
                }
                whenState(UNLOCKED) {
                    onEvent(COIN) {
                        returnCoin()
                    }
                    onEvent(PASS to LOCKED) {
                        lock()
                    }
                }
            }.build()
        // when
        val turnstile = Turnstile()

        val fsm = definition.create(turnstile)
        // then
        verifyTurnstileFSM(fsm, turnstile)
    }

    @Test
    fun simpleTurnstileTest() {
        val definition =
            stateMachine(
                TurnstileStates.values().toSet(),
                TurnstileEvents.values().toSet(),
                Turnstile::class
            ) {
                initialState { if (locked) LOCKED else UNLOCKED }
                whenState(LOCKED) {
                    onEntry { startState, targetState, _ ->
                        println("entering:$startState -> $targetState for $this")
                    }
                    onEvent(COIN to UNLOCKED) {
                        unlock()
                    }
                    onEvent(PASS) {
                        alarm()
                    }
                    onExit { startState, targetState, _ ->
                        println("exiting:$startState -> $targetState for $this")
                    }
                }
                whenState(UNLOCKED) {
                    onEntry { startState, targetState, _ ->
                        println("entering:$startState -> $targetState for $this")
                    }
                    onEvent(COIN) {
                        returnCoin()
                    }
                    onEvent(PASS to LOCKED) {
                        lock()
                    }
                    onExit { startState, targetState, _ ->
                        println("exiting:$startState -> $targetState for $this")
                    }
                }
            }.build()

        val turnstile = Turnstile()
        val fsm = definition.create(turnstile)

        assertTrue { turnstile.locked }
        assertTrue { fsm.currentState == LOCKED }

        fsm.sendEvent(COIN)

        assertTrue { !turnstile.locked }
        assertTrue { fsm.currentState == UNLOCKED }

        fsm.sendEvent(PASS)

        assertTrue { turnstile.locked }
        assertTrue { fsm.currentState == LOCKED }

        fsm.sendEvent(PASS)

        assertTrue { turnstile.locked }
        assertTrue { fsm.currentState == LOCKED }

        fsm.sendEvent(COIN)

        assertTrue { !turnstile.locked }
        assertTrue { fsm.currentState == UNLOCKED }

        fsm.sendEvent(COIN)

        assertTrue { !turnstile.locked }
        assertTrue { fsm.currentState == UNLOCKED }
    }

    @Test
    fun fsmComponentTest() {
        val turnstile = Turnstile()
        val fsm = TurnstileFSM(turnstile)
        println("--coin1")
        fsm.coin()
        println("--pass1")
        fsm.pass()
        println("--pass2")
        fsm.pass()
        println("--pass3")
        fsm.pass()
        println("--coin2")
        fsm.coin()
        println("--coin3")
        fsm.coin()
    }
}
