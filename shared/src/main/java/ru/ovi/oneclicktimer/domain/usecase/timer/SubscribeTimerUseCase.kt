package ru.ovi.oneclicktimer.domain.usecase.timer

import dagger.hilt.android.scopes.ActivityRetainedScoped
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import ru.ovi.oneclicktimer.data.TimerRepository
import ru.ovi.oneclicktimer.di.IoDispatcher
import javax.inject.Inject

@ActivityRetainedScoped
class SubscribeTimerUseCase @Inject constructor(
    private val repo: TimerRepository,
    @IoDispatcher private val dispatcher: CoroutineDispatcher
) {
    fun execute(): Flow<Long> = repo.subscribe()
        .flowOn(dispatcher)
}
