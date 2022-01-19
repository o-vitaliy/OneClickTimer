package ru.ovi.oneclicktimer.domain.usecase.timer

import dagger.hilt.android.scopes.ActivityRetainedScoped
import kotlinx.coroutines.CoroutineDispatcher
import ru.ovi.oneclicktimer.di.IoDispatcher
import ru.ovi.oneclicktimer.data.TimerRepository
import ru.ovi.oneclicktimer.domain.usecase.UseCase
import javax.inject.Inject

@ActivityRetainedScoped
class StopTimerRunningUseCase @Inject constructor(
    private val repo: TimerRepository,
    @IoDispatcher dispatcher: CoroutineDispatcher
) : UseCase<Unit, Unit>(dispatcher) {
    override suspend fun execute(parameters: Unit): Unit = repo.stop()
}
