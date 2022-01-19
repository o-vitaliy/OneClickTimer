package ru.ovi.oneclicktimer.domain.usecase.currentTimerValue

import dagger.hilt.android.scopes.ActivityRetainedScoped
import kotlinx.coroutines.CoroutineDispatcher
import ru.ovi.oneclicktimer.di.IoDispatcher
import ru.ovi.oneclicktimer.data.TimerRepository
import ru.ovi.oneclicktimer.data.TimerSelectedValueRepository
import ru.ovi.oneclicktimer.domain.usecase.UseCase
import javax.inject.Inject

class GetCurrentTimerValueUseCase @Inject constructor(
    private val repo: TimerSelectedValueRepository,
    @IoDispatcher dispatcher: CoroutineDispatcher
) : UseCase<Unit, Long?>(dispatcher) {
    override suspend fun execute(parameters: Unit): Long? = repo.getCurrentValue()
}
