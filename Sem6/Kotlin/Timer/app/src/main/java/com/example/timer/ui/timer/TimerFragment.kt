package com.example.timerapp.ui.timer

import android.content.*
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.view.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.timerapp.R
import com.example.timerapp.data.model.TimerSequence
import com.example.timerapp.databinding.FragmentTimerBinding
import com.example.timerapp.service.TimerService
import com.example.timerapp.viewmodel.SequenceViewModel
import com.example.timerapp.viewmodel.SequenceViewModelFactory
import com.example.timerapp.viewmodel.TimerViewModel
import com.google.gson.Gson
import kotlinx.coroutines.launch

class TimerFragment : Fragment() {

    private var _binding: FragmentTimerBinding? = null
    private val binding get() = _binding!!

    private val args: TimerFragmentArgs by navArgs()

    private val sequenceViewModel: SequenceViewModel by viewModels {
        SequenceViewModelFactory(requireActivity().application)
    }

    private val timerViewModel: TimerViewModel by viewModels()

    private var timerService: TimerService? = null
    private var isBound = false
    private lateinit var upcomingAdapter: UpcomingPhaseAdapter
    private var currentSequence: TimerSequence? = null

    private val tickReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val isFinished = intent?.getBooleanExtra(TimerService.EXTRA_IS_FINISHED, false) ?: false
            if (isFinished) {
                timerViewModel.setFinished()
                return
            }
            val phaseIndex = intent?.getIntExtra(TimerService.EXTRA_PHASE_INDEX, 0) ?: 0
            val remaining = intent?.getIntExtra(TimerService.EXTRA_REMAINING, 0) ?: 0
            val isRunning = intent?.getBooleanExtra(TimerService.EXTRA_IS_RUNNING, true) ?: true
            timerViewModel.updateFromService(phaseIndex, remaining, isRunning)
        }
    }

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val binder = service as TimerService.TimerBinder
            timerService = binder.getService()
            isBound = true
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            isBound = false
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTimerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        upcomingAdapter = UpcomingPhaseAdapter()
        binding.recyclerUpcoming.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerUpcoming.adapter = upcomingAdapter

        // Load sequence and start timer
        lifecycleScope.launch {
            val sequence = sequenceViewModel.getById(args.sequenceId)
            sequence?.let {
                currentSequence = it
                timerViewModel.initialize(it)
                startTimerService(it)
                binding.root.setBackgroundColor(it.color)
            }
        }

        setupObservers()
        setupControls()
    }

    private fun setupObservers() {
        timerViewModel.remainingSeconds.observe(viewLifecycleOwner) { seconds ->
            val m = seconds / 60
            val s = seconds % 60
            binding.tvTimer.text = "%02d:%02d".format(m, s)
        }

        timerViewModel.currentPhaseIndex.observe(viewLifecycleOwner) { index ->
            val phases = timerViewModel.phases.value ?: return@observe
            val currentPhase = phases.getOrNull(index)
            binding.tvCurrentPhase.text = currentPhase?.type?.name ?: ""

            // Update upcoming phases list
            val upcoming = if (index + 1 < phases.size) phases.subList(index + 1, phases.size) else emptyList()
            upcomingAdapter.submitList(upcoming)
        }

        timerViewModel.isRunning.observe(viewLifecycleOwner) { running ->
            binding.btnPause.text = if (running)
                getString(R.string.pause) else getString(R.string.resume)
        }

        timerViewModel.isFinished.observe(viewLifecycleOwner) { finished ->
            if (finished) {
                binding.tvTimer.text = getString(R.string.done)
                binding.tvCurrentPhase.text = getString(R.string.finished)
            }
        }
    }

    private fun setupControls() {
        binding.btnPause.setOnClickListener {
            val intent = Intent(requireContext(), TimerService::class.java)
            intent.action = if (timerViewModel.isRunning.value == true)
                TimerService.ACTION_PAUSE else TimerService.ACTION_RESUME
            requireContext().startService(intent)
        }

        binding.btnNext.setOnClickListener {
            val intent = Intent(requireContext(), TimerService::class.java)
            intent.action = TimerService.ACTION_NEXT
            requireContext().startService(intent)
        }

        binding.btnPrev.setOnClickListener {
            val intent = Intent(requireContext(), TimerService::class.java)
            intent.action = TimerService.ACTION_PREV
            requireContext().startService(intent)
        }

        binding.btnExit.setOnClickListener {
            stopTimerService()
            findNavController().navigateUp()
        }
    }

    private fun startTimerService(sequence: TimerSequence) {
        val intent = Intent(requireContext(), TimerService::class.java).apply {
            action = TimerService.ACTION_START
            putExtra(TimerService.EXTRA_SEQUENCE, Gson().toJson(sequence))
        }
        requireContext().startForegroundService(intent)

        val bindIntent = Intent(requireContext(), TimerService::class.java)
        requireContext().bindService(bindIntent, serviceConnection, Context.BIND_AUTO_CREATE)
    }

    private fun stopTimerService() {
        val intent = Intent(requireContext(), TimerService::class.java)
        intent.action = TimerService.ACTION_STOP
        requireContext().startService(intent)
    }

    override fun onStart() {
        super.onStart()
        val filter = IntentFilter(TimerService.BROADCAST_TICK)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requireContext().registerReceiver(tickReceiver, filter, Context.RECEIVER_NOT_EXPORTED)
        } else {
            requireContext().registerReceiver(tickReceiver, filter)
        }
    }

    override fun onStop() {
        super.onStop()
        requireContext().unregisterReceiver(tickReceiver)
        if (isBound) {
            requireContext().unbindService(serviceConnection)
            isBound = false
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
