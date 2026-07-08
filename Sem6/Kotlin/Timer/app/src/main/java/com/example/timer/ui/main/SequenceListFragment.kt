package com.example.timerapp.ui.main

import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.timerapp.R
import com.example.timerapp.databinding.FragmentSequenceListBinding
import com.example.timerapp.viewmodel.SequenceViewModel
import com.example.timerapp.viewmodel.SequenceViewModelFactory

class SequenceListFragment : Fragment() {

    private var _binding: FragmentSequenceListBinding? = null
    private val binding get() = _binding!!

    private val viewModel: SequenceViewModel by viewModels {
        SequenceViewModelFactory(requireActivity().application)
    }

    private lateinit var adapter: SequenceAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSequenceListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = SequenceAdapter(
            onEdit = { sequence ->
                val action = SequenceListFragmentDirections
                    .actionSequenceListToEdit(sequence.id)
                findNavController().navigate(action)
            },
            onDelete = { sequence ->
                viewModel.delete(sequence)
                Toast.makeText(requireContext(),
                    getString(R.string.deleted), Toast.LENGTH_SHORT).show()
            },
            onStart = { sequence ->
                val action = SequenceListFragmentDirections
                    .actionSequenceListToTimer(sequence.id)
                findNavController().navigate(action)
            }
        )

        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerView.adapter = adapter

        viewModel.sequences.observe(viewLifecycleOwner) { sequences ->
            adapter.submitList(sequences)
            binding.emptyState.visibility =
                if (sequences.isEmpty()) View.VISIBLE else View.GONE
        }

        binding.fabAdd.setOnClickListener {
            // Navigate to edit with id = -1 (create new)
            val action = SequenceListFragmentDirections
                .actionSequenceListToEdit(-1L)
            findNavController().navigate(action)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
