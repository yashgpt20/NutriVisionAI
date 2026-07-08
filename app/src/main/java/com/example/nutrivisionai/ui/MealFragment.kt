package com.example.nutrivisionai.ui

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.example.nutrivisionai.R
import com.example.nutrivisionai.databinding.FragmentMealBinding
import com.example.nutrivisionai.repository.AiRepository
import com.example.nutrivisionai.viewmodel.AiMealState
import com.example.nutrivisionai.viewmodel.MealViewModel
import com.example.nutrivisionai.viewmodel.MealViewModelFactory
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.coroutines.launch
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class MealFragment : Fragment() {
    private var _binding: FragmentMealBinding? = null
    private val binding get() = _binding!!
    private var imageCapture: ImageCapture? = null
    private lateinit var cameraExecutor: ExecutorService
    private lateinit var viewModel: MealViewModel

    private val requestPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()){
            isGranted: Boolean ->
        if(isGranted){
            startCamera()
        } else {
            Toast.makeText(requireContext(), "Camera Permission is required", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?): View {
        _binding = FragmentMealBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        cameraExecutor = Executors.newSingleThreadExecutor()
        val database = com.example.nutrivisionai.model.AppDB.getDatabase(requireContext())
        val mealDao = database.mealDao()

        val repository = AiRepository()
        val factory = MealViewModelFactory(repository, mealDao)
        viewModel = ViewModelProvider(this, factory)[MealViewModel::class.java]

        if(allPermissionsGranted()){
            startCamera()
        } else {
            requestPermissionLauncher.launch(Manifest.permission.CAMERA)
        }

        binding.btnCaptureMeal.setOnClickListener {
            takePhoto()
        }

        observeViewModel()
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.aiMealState.collect { state ->
                when (state) {
                    is AiMealState.Idle -> {
                        binding.btnCaptureMeal.isEnabled = true
                    }
                    is AiMealState.Loading -> {

                        binding.btnCaptureMeal.isEnabled = false

                        showCustomGeminiToast()
                    }
                    is AiMealState.Success -> {
                        val meal = state.mealLog

                        Toast.makeText(requireContext(), "Analyzed: ${meal.mealName}", Toast.LENGTH_SHORT).show()

                        android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                            requireActivity().findViewById<com.google.android.material.bottomnavigation.BottomNavigationView>(R.id.bottom_navigation)
                                .selectedItemId = R.id.nav_analytics
                        }, 1500)

                        viewModel.resetState()
                    }
                    is AiMealState.Error -> {
                        // If it's a laptop (NOT_FOOD) or network error, it shows here
                        Toast.makeText(requireContext(), state.message, Toast.LENGTH_LONG).show()
                        binding.btnCaptureMeal.isEnabled = true // Re-enable the button!
                        viewModel.resetState()
                    }
                }
            }
        }
    }

    private fun showCustomGeminiToast() {
        val layout = layoutInflater.inflate(R.layout.custom_toast, null)
        with(Toast(requireContext())) {
            setGravity(Gravity.BOTTOM, 0, 250)
            duration = Toast.LENGTH_SHORT
            view = layout
            show()
        }
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())
        cameraProviderFuture.addListener({
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()
            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(binding.viewFinder.surfaceProvider)
            }
            imageCapture = ImageCapture.Builder()
                .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                .build()

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(viewLifecycleOwner, CameraSelector.DEFAULT_BACK_CAMERA, preview, imageCapture)
            } catch (exc: Exception) {
                Log.e("CameraX", "Binding failed", exc)
            }
        }, ContextCompat.getMainExecutor(requireContext()))
    }

    private fun takePhoto() {
        Log.d("MealFragment", "Take photo clicked")
        if (imageCapture == null) {
            Log.e("MealFragment", "ImageCapture is null")
            Toast.makeText(requireContext(), "Camera not ready", Toast.LENGTH_SHORT).show()
            return
        }
        
        imageCapture?.takePicture(
            ContextCompat.getMainExecutor(requireContext()),
            object : ImageCapture.OnImageCapturedCallback() {
                override fun onCaptureSuccess(image: ImageProxy) {
                    Log.d("MealFragment", "Photo captured successfully")
                    Toast.makeText(requireContext(), "Photo captured! Analyzing...", Toast.LENGTH_SHORT).show()
                    val bitmap = image.toBitmap()
                    image.close()
                    viewModel.analyzeMealImage(bitmap)
                }
                override fun onError(exception: ImageCaptureException) {
                    Log.e("CameraX", "Capture failed: ${exception.message}", exception)
                    Toast.makeText(requireContext(), "Capture failed: ${exception.message}", Toast.LENGTH_SHORT).show()
                }
            }
        )
    }

    private fun allPermissionsGranted() = ContextCompat.checkSelfPermission(
        requireContext(), Manifest.permission.CAMERA
    ) == PackageManager.PERMISSION_GRANTED

    override fun onDestroyView() {
        super.onDestroyView()
        if (::cameraExecutor.isInitialized) cameraExecutor.shutdown()
        _binding = null
    }
}