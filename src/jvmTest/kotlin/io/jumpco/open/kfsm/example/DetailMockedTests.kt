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

import io.jumpco.open.kfsm.stateMachine
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.verify
import kotlin.test.Test

class DetailMockedTests {
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
        private val fsm = definition.create(context)
        fun event1() {
            println("--event1")
            fsm.sendEvent(TestEvents.EVENT1)
        }

        fun event2() {
            println("--event2")
            fsm.sendEvent(TestEvents.EVENT2)
        }

        fun event3() {
            println("--event3")
            fsm.sendEvent(TestEvents.EVENT3)
        }

        companion object {
            private val definition by lazy {
                stateMachine(TestStates.values().toSet(), TestEvents.values().toSet(), TestContext::class) {
                    initialState {
                        when (state) {
                            1 -> TestStates.STATE1
                            2 -> TestStates.STATE2
                            3 -> TestStates.STATE3
                            else -> error("Invalid state $state")
                        }
                    }
                    onStateChange { oldState, newState ->
                        println("onStateChange:$oldState -> $newState")
                    }
                    default {
                        onEntry { startState, targetState, _ ->
                            println("entering:to $targetState from $startState for:$this")
                            defaultEntry()
                        }
                        onExit { startState, targetState, _ ->
                            println("exiting:from $targetState to $startState for:$this")
                            defaultExit()
                        }
                        onEvent(TestEvents.EVENT1 to TestStates.STATE1) {
                            println("default:EVENT1 to STATE1 for $this")
                            action1()
                        }
                        onEvent(TestEvents.EVENT2 to TestStates.STATE2) {
                            println("default:on EVENT2 to STATE2 for $this")
                            action2()
                        }
                        onEvent(TestEvents.EVENT3 to TestStates.STATE3) {
                            println("default:on EVENT3 to STATE3 for $this")
                            defaultAction()
                        }
                        action { currentState, event, _ ->
                            println("default:$event from $currentState for $this")
                            defaultAction()
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
                        onEvent(TestEvents.EVENT2, guard = { state == 2 }) {
                            println("EVENT2:guarded:from STATE2 for $this")
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
    }

    @Test
    fun `test actions mockked`() {
        // given
        val context = mockk<TestContext>()
        every { context.toString() } returns "MockedTestContext"
        every { context.state } returns 3
        every { context.action1() } just Runs
        every { context.action2() } just Runs
        every { context.defaultAction() } just Runs
        every { context.defaultEntry() } just Runs
        every { context.defaultExit() } just Runs
        every { context.entry1() } just Runs
        every { context.entry2() } just Runs
        every { context.exit2() } just Runs
        every { context.exit3() } just Runs

        val fsm = TestDetailFSM(context)
        // when
        fsm.event1()
        every { context.state } returns 1
        fsm.event2()
        every { context.state } returns 2
        fsm.event3()
        every { context.state } returns 3
        fsm.event2()

        // then
        verify {
            // event1
            context.toString()
            context.exit3()
            context.toString()
            context.defaultExit()
            context.action1()
            context.toString()
            context.entry1()
            context.toString()
            context.defaultEntry()
            // event2
            context.defaultExit()
            context.action2()
            context.entry2()
            context.toString()
            context.defaultEntry()
            // event3
            context.defaultAction()
            // event2
        }
    }
}
