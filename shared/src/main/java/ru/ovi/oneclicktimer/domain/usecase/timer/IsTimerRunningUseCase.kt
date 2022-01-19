package ru.ovi.oneclicktimer.domain.usecase.timer

import dagger.hilt.android.scopes.ActivityRetainedScoped
import kotlinx.coroutines.CoroutineDispatcher
import ru.ovi.oneclicktimer.di.IoDispatcher
import ru.ovi.oneclicktimer.data.TimerRepository
import ru.ovi.oneclicktimer.domain.usecase.UseCase
import javax.inject.Inject

@ActivityRetainedScoped
class IsTimerRunningUseCase @Inject constructor(
    private val repo: TimerRepository,
    @IoDispatcher dispatcher: CoroutineDispatcher
) : UseCase<Unit, Boolean>(dispatcher) {
    override suspend fun execute(parameters: Unit): Boolean = repo.isRunning()
}
