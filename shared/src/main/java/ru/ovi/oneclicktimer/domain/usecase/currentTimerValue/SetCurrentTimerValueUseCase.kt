package ru.ovi.oneclicktimer.domain.usecase.currentTimerValue

import kotlinx.coroutines.CoroutineDispatcher
import ru.ovi.oneclicktimer.data.TimerSelectedValueRepository
import ru.ovi.oneclicktimer.di.IoDispatcher
import ru.ovi.oneclicktimer.domain.usecase.UseCase
import javax.inject.Inject

class SetCurrentTimerValueUseCase @Inject constructor(
    private val repo: TimerSelectedValueRepository,
    @IoDispatcher dispatcher: CoroutineDispatcher
) : UseCase<Long?, Unit>(dispatcher) {
    override suspend fun execute(parameters: Long?): Unit = repo.setCurrentValue(parameters)
}
