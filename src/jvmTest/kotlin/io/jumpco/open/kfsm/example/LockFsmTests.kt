/*
 * Copyright (c) 2021. Open JumpCO
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package io.jumpco.open.kfsm.example

import io.jumpco.open.kfsm.StateMachineBuilder
import io.jumpco.open.kfsm.StateMachineInstance
import io.jumpco.open.kfsm.example.LockEvents.LOCK
import io.jumpco.open.kfsm.example.LockEvents.UNLOCK
import io.jumpco.open.kfsm.example.LockStates.DOUBLE_LOCKED
import io.jumpco.open.kfsm.example.LockStates.LOCKED
import io.jumpco.open.kfsm.example.LockStates.UNLOCKED
import io.jumpco.open.kfsm.stateMachine
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.verify
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.test.fail

/**
 * @suppress
 */
class LockFsmTests {

    private fun verifyLockFSM(fsm: StateMachineInstance<LockStates, LockEvents, Lock, Any, Any>, lock: Lock) {
        // when
        every { lock.locked } returns 1
        every { lock.unlock() } just Runs
        every { lock.lock() } just Runs
        every { lock.doubleLock() } just Runs
        // then
        assertTrue { fsm.currentState == LOCKED }
        // when
        fsm.sendEvent(UNLOCK)
        // then
        verify { lock.unlock() }
        assertTrue { fsm.currentState == UNLOCKED }
        try {
            // when
            fsm.sendEvent(UNLOCK)
            fail("Expected an exception")
        } catch (x: Throwable) {
            println("Expected:$x")
            // then
            assertEquals("Already unlocked", x.message)
        }
        // when
        fsm.sendEvent(LOCK)
        // then
        verify { lock.lock() }
        assertTrue { fsm.currentState == LOCKED }
        // when
        fsm.sendEvent(LOCK)
        verify { lock.doubleLock() }
        // then
        assertTrue { fsm.currentState == DOUBLE_LOCKED }
        try {
            // when
            fsm.sendEvent(LOCK)
            fail("Expected an exception")
        } catch (x: Throwable) {
            println("Expected:$x")
            // then
            assertEquals("Already double locked", x.message)
        }
    }

    @Test
    fun `test plain creation of fsm`() {
        // given
        val builder = StateMachineBuilder<LockStates, LockEvents, Lock, Any, Any>(
            LockStates.values().toSet(),
            LockEvents.values().toSet()
        )
        builder.initialState {
            when (locked) {
                0 -> UNLOCKED
                1 -> LOCKED
                2 -> DOUBLE_LOCKED
                else -> error("Invalid state locked=$locked")
            }
        }
        builder.transition(LOCKED, UNLOCK, UNLOCKED) {
            unlock()
        }
        builder.transition(LOCKED, LOCK, DOUBLE_LOCKED) {
            doubleLock()
        }
        builder.transition(DOUBLE_LOCKED, UNLOCK, LOCKED) {
            doubleUnlock()
        }
        builder.transition(DOUBLE_LOCKED, LOCK) {
            error("Already double locked")
        }
        builder.transition(UNLOCKED, LOCK, LOCKED) {
            lock()
        }
        builder.transition(UNLOCKED, UNLOCK) {
            error("Already unlocked")
        }
        val definition = builder.complete()
        // when
        val lock = mockk<Lock>()
        every { lock.locked } returns 1
        val fsm = definition.create(lock)
        // then
        verifyLockFSM(fsm, lock)
    }

    @Test
    fun `test dsl creation of fsm`() {
        // given
        val definition = stateMachine(
            LockStates.values().toSet(),
            LockEvents.values().toSet(),
            Lock::class
        ) {
            initialState {
                when (locked) {
                    0 -> UNLOCKED
                    1 -> LOCKED
                    2 -> DOUBLE_LOCKED
                    else -> error("Invalid state locked=$locked")
                }
            }

            whenState(LOCKED) {
                onEvent(LOCK to DOUBLE_LOCKED) {
                    doubleLock()
                }
                onEvent(UNLOCK to UNLOCKED) {
                    unlock()
                }
            }
            whenState(DOUBLE_LOCKED) {
                onEvent(UNLOCK to LOCKED) {
                    doubleUnlock()
                }
                onEvent(LOCK) {
                    error("Already double locked")
                }
            }
            whenState(UNLOCKED) {
                onEvent(LOCK to LOCKED) {
                    lock()
                }
                onEvent(UNLOCK) {
                    error("Already unlocked")
                }
            }
        }.build()
        // when
        val lock = mockk<Lock>()
        every { lock.locked } returns 1
        val fsm = definition.create(lock)
        // then
        verifyLockFSM(fsm, lock)
    }

    @Test
    fun `simple lock test`() {
        val lock = Lock(0)
        val fsm = LockFSM(lock)
        println("--lock1")
        fsm.lock()
        assertEquals(fsm.allowedEvents(), setOf(LOCK, UNLOCK))
        println("--lock2")
        fsm.lock()
        println("--lock3")
        fsm.lock()
        println("--unlock1")
        fsm.unlock()
        assertEquals(fsm.allowedEvents(), setOf(UNLOCK, LOCK))
        println("--unlock2")
        fsm.unlock()
        assertEquals(fsm.allowedEvents(), setOf(LOCK))
        println("--unlock3")
        fsm.unlock()
    }
}
