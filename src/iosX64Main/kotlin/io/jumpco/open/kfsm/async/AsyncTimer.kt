package io.jumpco.open.kfsm.async

import kotlinx.coroutines.CoroutineScope

actual class AsyncTimer<S, E, C, A, R> actual constructor(
    parentFsm: AsyncStateMapInstance<S, E, C, A, R>,
    context: C,
    arg: A?,
    definition: AsyncTimerDefinition<S, E, C, A, R>,
    coroutineScope: CoroutineScope
) {
    actual fun cancel() {
    }

    actual suspend fun trigger() {
    }
}
