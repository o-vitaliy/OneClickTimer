package ru.ovi.oneclicktimer.domain.usecase.timer

import dagger.hilt.android.scopes.ActivityRetainedScoped
import kotlinx.coroutines.CoroutineDispatcher
import ru.ovi.oneclicktimer.di.IoDispatcher
import ru.ovi.oneclicktimer.data.TimerRepository
import ru.ovi.oneclicktimer.domain.usecase.UseCase
import javax.inject.Inject

@ActivityRetainedScoped
class StartTimerRunningUseCase @Inject constructor(
    private val repo: TimerRepository,
    @IoDispatcher dispatcher: CoroutineDispatcher
) : UseCase<Long, Unit>(dispatcher) {
    override suspend fun execute(parameters: Long): Unit = repo.start(parameters)
}
