package com.example.kadaliv2.ui.room

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.kadaliv2.R
import com.example.kadaliv2.databinding.FragmentAddRoomBinding
import com.example.kadaliv2.ui.viewmodel.RoomViewModel
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

class AddRoomFragment : Fragment(R.layout.fragment_add_room) {

    private val viewModel: RoomViewModel by viewModel()
    private val args: AddRoomFragmentArgs by navArgs()
    private var _binding: FragmentAddRoomBinding? = null
    private val binding get() = _binding!!

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentAddRoomBinding.bind(view)

        val isEditMode = args.roomId != null

        if (isEditMode) {
            setupEditMode(args.roomId!!)
        }

        binding.btnBack.setOnClickListener {
            findNavController().popBackStack()
        }

        binding.btnSaveRoom.setOnClickListener {
            val name = binding.etRoomName.text.toString()
            val desc = binding.etRoomDescription.text.toString()

            if (name.isBlank()) {
                binding.etRoomName.error = "Name required"
                return@setOnClickListener
            }

            if (isEditMode) {
                viewModel.updateRoom(args.roomId!!, name, desc)
                Toast.makeText(requireContext(), "Room updated", Toast.LENGTH_SHORT).show()
            } else {
                viewModel.saveRoom(name, desc)
                Toast.makeText(requireContext(), "Room saved", Toast.LENGTH_SHORT).show()
            }
            findNavController().popBackStack()
        }
    }

    private fun setupEditMode(roomId: String) {
        binding.btnSaveRoom.text = "Update Room"
        // Need to set title? Toolbar might be handled by MainActivity destination label, 
        // but we can override it or leave it as "Add Room" (inaccurate) -> Updated graph label? 
        // Graph label is static. Can update toolbar title programmatically.
        (requireActivity() as? androidx.appcompat.app.AppCompatActivity)?.supportActionBar?.title = "Edit Room"

        viewModel.getRoom(roomId)
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(androidx.lifecycle.Lifecycle.State.STARTED) {
                viewModel.room.collect { room ->
                    if (room != null) {
                        binding.etRoomName.setText(room.name)
                        binding.etRoomDescription.setText(room.description)
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
