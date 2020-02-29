/*
 * Copyright (c) 2020. Open JumpCO
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package io.jumpco.open.kfsm.async

import io.jumpco.open.kfsm.AsyncStateAction
import io.jumpco.open.kfsm.TransitionType

open class AsyncTransition<S, E, C, A, R>(
    val targetState: S? = null,
    val targetMap: String? = null,
    val automatic: Boolean = false,
    val type: TransitionType = TransitionType.NORMAL,
    val action: AsyncStateAction<C, A, R>? = null
) {
    init {
        if (type == TransitionType.PUSH) {
            require(targetState != null) { "targetState is required for push transition" }
        }
    }

    /**
     * Executed exit, optional and entry actions specific in the transition.
     */
    open suspend fun execute(context: C, instance: AsyncStateMapInstance<S, E, C, A, R>, arg: A?): R? {

        if (isExternal()) {
            instance.executeExit(context, targetState!!, arg)
        }
        val result = action?.invoke(context, arg)
        if (isExternal()) {
            instance.executeEntry(context, targetState!!, arg)
        }
        return result
    }

    /**
     * Executed exit, optional and entry actions specific in the transition.
     */
    open suspend fun execute(
        context: C,
        sourceMap: AsyncStateMapInstance<S, E, C, A, R>,
        targetMap: AsyncStateMapInstance<S, E, C, A, R>?,
        arg: A?
    ): R? {
        if (isExternal()) {
            sourceMap.executeExit(context, targetState!!, arg)
        }
        val result = action?.invoke(context, arg)
        if (targetMap != null && isExternal()) {
            targetMap.executeEntry(context, targetState!!, arg)
        }
        return result
    }

    /**
     * This function provides an indicator if a Transition is internal or external.
     * When there is no targetState defined a Transition is considered internal and will not trigger entry or exit actions.
     */
    fun isExternal(): Boolean = targetState != null
}
