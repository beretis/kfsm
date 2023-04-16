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

package io.jumpco.open.kfsm.example

import io.jumpco.open.kfsm.StateMapItem
import io.jumpco.open.kfsm.stateMachine
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class DetailTests {
    enum class TestStates {
        STATE1,
        STATE2,
        STATE3
    }

    enum class TestEvents {
        EVENT1,
        EVENT2,
        EVENT3
    }

    class TestContext(var state: Int) {
        fun defaultAction() {
            println("defaultAction")
        }

        fun defaultEntry() {
            println("defaultEntry")
        }

        fun defaultExit() {
            println("defaultEntry")
        }

        fun action1() {
            println("action1")
        }

        fun action2() {
            println("action2")
        }

        fun none() {}
        fun entry1() {
            println("entry1")
        }

        fun entry2() {
            println("entry2")
        }

        fun exit2() {
            println("exit2")
        }

        fun exit3() {
            println("exit3")
        }

        override fun toString(): String {
            return "TestContext(state=$state)"
        }
    }

    class TestDetailFSM(context: TestContext) {
        val fsm = definition.create(context)
        fun allowedEvents() = fsm.allowed()
        fun eventAllowed(event: TestEvents, includeDefaults: Boolean = false) =
            fsm.eventAllowed(event, includeDefaults)

        fun event1(msg: String) {
            println("--event1")
            fsm.sendEvent(TestEvents.EVENT1, msg)
        }

        fun event2(msg: String) {
            println("--event2")
            fsm.sendEvent(TestEvents.EVENT2, msg)
        }

        fun event3(msg: String) {
            println("--event3")
            fsm.sendEvent(TestEvents.EVENT3, msg)
        }

        companion object {
            val definition = stateMachine(
                TestStates.values().toSet(),
                TestEvents.values().toSet(),
                TestContext::class,
                String::class
            ) {
                initialStates {
                    mutableListOf<StateMapItem<TestStates>>().apply {
                        when (state) {
                            1 -> add(TestStates.STATE1 to "default")
                            2 -> add(TestStates.STATE2 to "default")
                            3 -> add(TestStates.STATE3 to "default")
                            else -> error("Invalid state $state")
                        }
                    }
                }
                onStateChange { oldState, newState ->
                    println("onStateChange:$oldState -> $newState")
                }
                default {
                    onEntry { startState, targetState, msg ->
                        println("entering:to $targetState from $startState for:$this:$msg")
                        defaultEntry()
                    }
                    onExit { startState, targetState, msg ->
                        println("exiting:from $targetState to $startState for:$this:$msg")
                        defaultExit()
                    }
                    onEvent(TestEvents.EVENT1 to TestStates.STATE1) { msg ->
                        println("default:EVENT1 to STATE1 for $this:$msg")
                        action1()
                        state = 1
                        none()
                    }
                    onEvent(TestEvents.EVENT2 to TestStates.STATE2) { msg ->
                        println("default:on EVENT2 to STATE2 for $this:$msg")
                        action2()
                        state = 2
                        none()
                    }
                    onEvent(TestEvents.EVENT3 to TestStates.STATE3) { msg ->
                        println("default:on EVENT3 to STATE3 for $this:$msg")
                        defaultAction()
                        state = 3
                        none()
                    }
                    action { currentState, event, msg ->
                        println("default:$event from $currentState for $this:$msg")
                        defaultAction()
                    }
                }
                stateMap("map1", setOf(TestStates.STATE1, TestStates.STATE2)) {
                    whenState(TestStates.STATE1) {
                        onEventPop(TestEvents.EVENT1) {
                            println("pop")
                        }
                    }
                    whenState(TestStates.STATE2) {
                        automatic(TestStates.STATE1) {
                            println("automatic -> TestStates.STATE1")
                        }
                    }
                }
                whenState(TestStates.STATE1) {
                    onEvent(TestEvents.EVENT1) {
                        action1()
                    }
                    onEntry { _, _, _ ->
                        entry1()
                    }
                }
                whenState(TestStates.STATE2) {
                    onEntry { _, _, _ ->
                        entry2()
                    }
                    onEvent(TestEvents.EVENT2, guard = { state == 2 }) { msg ->
                        println("EVENT2:guarded:from STATE2 for $this:$msg")
                        action2()
                    }
                    onEventPush(TestEvents.EVENT1, "map1", TestStates.STATE2) { msg ->
                        println("EVENT1:push:from STATE2 for $this:$msg")
                        action2()
                    }
                    onExit { _, _, _ ->
                        exit2()
                    }
                }
                whenState(TestStates.STATE3) {
                    onExit { _, _, _ ->
                        exit3()
                    }
                    onEvent(TestEvents.EVENT2, guard = { state == 2 }) {
                        error("should never be called")
                    }
                }
            }.build()
        }
    }

    @Test
    fun `test actions`() {
        // given
        val context = TestContext(3)
        val fsm = TestDetailFSM(context)
        var msgNo = 1
        // when
        fsm.event1((++msgNo).toString())
        // then
        assertEquals(1, context.state)
        assertTrue(fsm.eventAllowed(TestEvents.EVENT1))
        // when
        fsm.event2((++msgNo).toString())
        // then
        assertEquals(2, context.state)
        assertTrue(fsm.eventAllowed(TestEvents.EVENT2))
        // when
        fsm.event2((++msgNo).toString())
        assertEquals(2, context.state)
        // then
        fsm.event3((++msgNo).toString())
        assertEquals(3, context.state)
        assertEquals(fsm.allowedEvents(), setOf(TestEvents.EVENT2))
        // when
        fsm.event2((++msgNo).toString())
        assertEquals(3, context.state)
        // then
        fsm.event1((++msgNo).toString())
        assertEquals(1, context.state)
        assertEquals(fsm.allowedEvents(), setOf(TestEvents.EVENT1))
        fsm.event2((++msgNo).toString())
        assertEquals(2, context.state)
        assertEquals(fsm.fsm.currentState, TestStates.STATE2)
        fsm.event1((++msgNo).toString())
        assertEquals(fsm.fsm.currentStateMap.name, "map1")
        assertEquals(fsm.fsm.currentState, TestStates.STATE1)
        fsm.event1((++msgNo).toString())
        assertNull(fsm.fsm.currentStateMap.name)
        assertEquals(fsm.fsm.currentState, TestStates.STATE2)
    }
}
