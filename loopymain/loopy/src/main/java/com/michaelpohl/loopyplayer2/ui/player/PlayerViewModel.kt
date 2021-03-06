package com.michaelpohl.loopyplayer2.ui.player

import android.view.View
import androidx.lifecycle.MediatorLiveData
import com.michaelpohl.loopyplayer2.R
import com.michaelpohl.loopyplayer2.common.*
import com.michaelpohl.loopyplayer2.common.PlayerState.*
import com.michaelpohl.loopyplayer2.common.jni.JniBridge
import com.michaelpohl.loopyplayer2.common.util.coroutines.ioJob
import com.michaelpohl.loopyplayer2.common.util.coroutines.uiJob
import com.michaelpohl.loopyplayer2.common.util.coroutines.withUI
import com.michaelpohl.loopyplayer2.model.AppStateRepository
import com.michaelpohl.loopyplayer2.model.FilesRepository
import com.michaelpohl.loopyplayer2.model.PlayerServiceInterface
import com.michaelpohl.loopyplayer2.ui.base.BaseUIState
import com.michaelpohl.loopyplayer2.ui.base.UIStateViewModel
import com.michaelpohl.loopyplayer2.ui.util.calculateConversionProgress
import kotlinx.coroutines.delay
import timber.log.Timber
import kotlin.system.measureTimeMillis

class PlayerViewModel(
    private val audioFilesRepository: FilesRepository,
    private val appStateRepo: AppStateRepository
) :
    UIStateViewModel<PlayerViewModel.UIState>() {


    val isPlaying = MediatorLiveData<Boolean>().apply {
        addSource(_state) {
            this.value = it.isPlaying
        }
    }

    val processingText = MediatorLiveData<String>().apply {
        addSource(state) {
            this.value =
                "${resources.getString(R.string.processing, it.conversionProgress)}%"
        }
    }

    val sampleRateDisplayText = MediatorLiveData<String>().apply {
        addSource(state) {
            this.value =
                resources.getString(R.string.player_sample_rate, it.settings.sampleRate.displayName)
        }
    }

    private var settings: Settings = appStateRepo.settings

    private var waitmode: Boolean? = null

    // TODO this is a workaround. Looper should be injected. The lateinit causes too much trouble
    private lateinit var looper: PlayerServiceInterface

    override fun initUIState(): UIState {
        return UIState(
            loopsList = audioFilesRepository.getSingleSetOrStandardSet(),
            isPlaying = false,
            clearButtonVisibility = 0,
            settings = settings,
            processingOverlayVisibility = false.toVisibility()
        )
    }

    override fun onFragmentResumed() {
        settings = appStateRepo.settings
        _state.value = initUIState()
        setPlayerSampleRate(settings.sampleRate)
    }

    override fun onFragmentPaused() {
        super.onFragmentPaused()
        uiJob {
            if (!currentState.settings.playInBackground) looper.pause()
        }
    }

    fun setPlayerWaitMode(shouldWait: Boolean) {
        if (!::looper.isInitialized || waitmode == shouldWait) return
        uiJob {
            if (looper.setWaitMode(shouldWait).isSuccess()) {
                waitmode = shouldWait
            } else error("Failed to set wait mode. This is a program error.")

            // unselect if waitmode was turned off
            if (!looper.getWaitMode()) {
                _state.value = currentState.copy(filePreselected = "")
            }
        }
    }

    fun setPlayerSampleRate(sampleRate: SampleRate) {
        if (!::looper.isInitialized) return
        uiJob {
            if (!looper.setSampleRate(sampleRate.intValue).isSuccess()) {
                error("Failed to set sample rate. This is a program error")
            }
        }
    }

    fun setPlayer(player: PlayerServiceInterface) {
        looper = player.apply {
            setFileStartedByPlayerListener { onPlayerSwitchedToNextFile(it) }
            setPlaybackProgressListener { name, value ->
                _state.postValue(currentState.copy(playbackProgress = Pair(name, value)))
            }
        }
    }

    fun onStartPlaybackClicked() {
        uiJob {
            when (looper.getState()) {
                PLAYING -> { /* do nothing */
                }
                PAUSED -> {
                    looper.resume()
                    _state.value = currentState.copy(isPlaying = true)
                }
                else -> if (looper.hasLoopFile()) startLooper()
            }
        }
    }

    fun onStopPlaybackClicked() {
        when (looper.getState()) {
            PLAYING, PAUSED -> stopLooper()
            else -> { /* do nothing */
            }
        }
    }

    fun onPausePlaybackClicked() {
        uiJob {
            when (looper.getState()) {
                PLAYING -> {
                    looper.pause()
                    _state.value = currentState.copy(isPlaying = false)
                }
                PAUSED -> {
                    looper.resume()
                    _state.value = currentState.copy(isPlaying = true)
                }
                else -> { /* do nothing */
                }
            }

        }
    }

    fun onLoopClicked(audioModel: AudioModel) {
        uiJob {
            with(looper.select(audioModel.path)) {
                if (this.isSuccess()) {
                    this.data?.let {
                        onFileSelected(it)
                    } ?: error("Got no filename back from JNI. This shouldn't happen")
                }
            }
        }
    }

    fun stopLooper() {
        uiJob {
            if (looper.stop().isSuccess()) {
                _state.value = currentState.copy(
                    playbackProgress = Pair(currentState.fileInFocus ?: "", 0),
                    fileInFocus = "",
                    filePreselected = "",
                    isPlaying = false
                )
            }
        }
    }

    fun addNewLoops(newLoops: List<FileModel.AudioFile>) {
        JniBridge.conversionProgressListener =
            { name, steps -> onConversionProgressUpdated(newLoops, name, steps) }
        // TODO ask the user if adding or replacing is desired
        _state.value = currentState.copy(processingOverlayVisibility = true.toVisibility())
        ioJob {
            val elapsed = measureTimeMillis {
                val result = audioFilesRepository.addLoopsToSet(newLoops)

                // TODO set handling is a total work in progress
                if (result != JniBridge.ConversionResult.ALL_FAILED) { // if at least one from the conversion succeeded, update UI
                    val loops = audioFilesRepository.getSingleSetOrStandardSet()

                    withUI {
                        _state.postValue(
                            currentState.copy(loopsList = loops)
                        )
                    }
                }
            }
            Timber.d("Conversion took: $elapsed")
            delay(300) // wait for a moment so the user sees the complete progress bar :-)
        }.invokeOnCompletion {
            _state.postValue(
                currentState.copy(
                    processingOverlayVisibility = false.toVisibility()
                )
            )
        }
    }

    private fun onConversionProgressUpdated(
        newLoops: List<FileModel.AudioFile>,
        name: String,
        currentSteps: Int
    ) {
        val currentIndex = newLoops.withIndex().find { it.value.name == name }?.index ?: 0
        val conversionPercentage =
            calculateConversionProgress(newLoops.size, currentIndex, currentSteps)
        _state.postValue(currentState.copy(conversionProgress = conversionPercentage))
    }

    fun onDeleteLoopClicked(audioModel: AudioModel) {
        val currentLoops = currentState.loopsList.toMutableList()

        currentLoops.remove(audioModel)
        audioFilesRepository.saveLoopSelectionToSet(
            null,
            currentLoops
        ) // TODO set name needs to be properly connected
        _state.value = currentState.copy(
            loopsList = audioFilesRepository.getSingleSetOrStandardSet()
        )
    }

    fun onProgressChangedByUser(newProgress: Float) {
        looper.changePlaybackPosition(newProgress)
    }

    fun clearLoops() {

        audioFilesRepository.saveLoopSelectionToSet(
            null,
            listOf()
        )
        _state.value = currentState.copy(
            loopsList = audioFilesRepository.getSingleSetOrStandardSet()
        )
    }

    private fun onPlayerSwitchedToNextFile(filename: String) {
        _state.postValue(currentState.copy(fileInFocus = filename))
    }

    private fun onFileSelected(filename: String) {
        if (looper.getWaitMode()) {
            when (looper.getState()) {
                PLAYING, PAUSED -> _state.postValue(currentState.copy(filePreselected = filename))
                STOPPED, UNKNOWN, READY -> startLooper()
            }
        } else {
            startLooper()
        }
    }

    private fun startLooper() {
        uiJob {
            with(looper.play()) {
                if (this.isSuccess()) {
                    this.data?.let {
                        _state.postValue(
                            currentState.copy(
                                fileInFocus = this.data,
                                isPlaying = true
                            )
                        )
                    }
                }
            }
        }
    }

    data class UIState(
        val loopsList: List<AudioModel>,
        val isPlaying: Boolean,
        val isWaitMode: Boolean = false,
        val fileInFocus: String? = null,
        val filePreselected: String? = null,
        val playbackProgress: Pair<String, Int>? = null,
        val clearButtonVisibility: Int = View.GONE,
        val settings: Settings,
        val processingOverlayVisibility: Int,
        val conversionProgress: Int? = 0
    ) : BaseUIState() {

        val emptyMessageVisibility: Int = this.loopsList.isEmpty().toVisibility()
    }
}
