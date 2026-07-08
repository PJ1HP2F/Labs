package com.example.timerapp.ui.edit

import android.graphics.Color
import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.timerapp.R
import com.example.timerapp.data.model.TimerSequence
import com.example.timerapp.databinding.FragmentEditSequenceBinding
import com.example.timerapp.viewmodel.SequenceViewModel
import com.example.timerapp.viewmodel.SequenceViewModelFactory
import com.skydoves.colorpickerview.ColorPickerDialog
import com.skydoves.colorpickerview.listeners.ColorEnvelopeListener
import kotlinx.coroutines.launch

class EditSequenceFragment : Fragment() {

    private var _binding: FragmentEditSequenceBinding? = null
    private val binding get() = _binding!!

    private val args: EditSequenceFragmentArgs by navArgs()

    private val viewModel: SequenceViewModel by viewModels {
        SequenceViewModelFactory(requireActivity().application)
    }

    private var selectedColor: Int = Color.parseColor("#FF5722")
    private var existingSequence: TimerSequence? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEditSequenceBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (args.sequenceId != -1L) {
            // Load existing sequence
            lifecycleScope.launch {
                val sequence = viewModel.getById(args.sequenceId)
                sequence?.let {
                    existingSequence = it
                    populateForm(it)
                }
            }
        }

        binding.btnPickColor.setOnClickListener {
            showColorPicker()
        }

        binding.btnSave.setOnClickListener {
            saveSequence()
        }
    }

    private fun populateForm(sequence: TimerSequence) {
        binding.etName.setText(sequence.name)
        selectedColor = sequence.color
        binding.colorPreview.setBackgroundColor(selectedColor)
        binding.etWarmup.setText(sequence.warmupSeconds.toString())
        binding.etWork.setText(sequence.workSeconds.toString())
        binding.etRest.setText(sequence.restSeconds.toString())
        binding.etCooldown.setText(sequence.cooldownSeconds.toString())
        binding.etRepetitions.setText(sequence.workRepetitions.toString())
        binding.etRestBetweenSets.setText(sequence.restBetweenSets.toString())
    }

    private fun showColorPicker() {
        ColorPickerDialog.Builder(requireContext())
            .setTitle(getString(R.string.pick_color))
            .setPreferenceName("ColorPickerPreference")
            .setPositiveButton(getString(R.string.select),
                ColorEnvelopeListener { envelope, _ ->
                    selectedColor = envelope.color
                    binding.colorPreview.setBackgroundColor(selectedColor)
                })
            .setNegativeButton(getString(R.string.cancel)) { dialog, _ -> dialog.dismiss() }
            .attachAlphaSlideBar(false)
            .attachBrightnessSlideBar(true)
            .show()
    }

    private fun saveSequence() {
        val name = binding.etName.text.toString().trim()
        if (name.isEmpty()) {
            binding.etName.error = getString(R.string.name_required)
            return
        }

        val warmup = binding.etWarmup.text.toString().toIntOrNull() ?: 10
        val work = binding.etWork.text.toString().toIntOrNull() ?: 30
        val rest = binding.etRest.text.toString().toIntOrNull() ?: 10
        val cooldown = binding.etCooldown.text.toString().toIntOrNull() ?: 10
        val reps = binding.etRepetitions.text.toString().toIntOrNull() ?: 3
        val restBetween = binding.etRestBetweenSets.text.toString().toIntOrNull() ?: 30

        val sequence = TimerSequence(
            id = existingSequence?.id ?: 0,
            name = name,
            color = selectedColor,
            warmupSeconds = warmup,
            workSeconds = work,
            restSeconds = rest,
            cooldownSeconds = cooldown,
            workRepetitions = reps,
            restBetweenSets = restBetween
        )

        if (existingSequence != null) {
            viewModel.update(sequence)
        } else {
            viewModel.insert(sequence)
        }

        Toast.makeText(requireContext(), getString(R.string.saved), Toast.LENGTH_SHORT).show()
        findNavController().navigateUp()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
