package com.beginning.tugasakhirpam.features.user.ui

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import com.beginning.tugasakhirpam.MainActivity
import com.beginning.tugasakhirpam.features.history.ui.HistoryActivity
import com.beginning.tugasakhirpam.R
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.io.ByteArrayOutputStream
import java.io.FileNotFoundException

class ProfileActivity : Fragment(R.layout.activity_profile) {
    private lateinit var profileImageView: ImageView
    private lateinit var usernameEditText: EditText
    private lateinit var emailEditText: EditText
    private lateinit var changeUsernameButton: Button
    private lateinit var changePasswordButton: Button
    private lateinit var logoutButton: Button
    private lateinit var bottomNavigation: BottomNavigationView

    private lateinit var database: DatabaseReference
    private lateinit var storage: StorageReference

    private val userId = "user_default"

    companion object {
        private const val TAG = "ProfileFragment"
        private const val CAMERA_PERMISSION_CODE = 100
        private const val STORAGE_PERMISSION_CODE = 101
    }

    private val cameraLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val imageBitmap = result.data?.extras?.get("data") as? Bitmap
            imageBitmap?.let {
                profileImageView.setImageBitmap(it)
                uploadImageToFirebase(it)
            }
        }
    }

    private val galleryLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val imageUri = result.data?.data
            imageUri?.let {
                try {
                    val inputStream = requireActivity().contentResolver.openInputStream(it)
                    val bitmap = BitmapFactory.decodeStream(inputStream)
                    profileImageView.setImageBitmap(bitmap)
                    uploadImageToFirebase(bitmap)
                } catch (e: FileNotFoundException) {
                    Log.e(TAG, "File not found", e)
                    Toast.makeText(requireContext(), "File not found", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        val view = inflater.inflate(R.layout.activity_profile, container, false)

        // Initialize views
        profileImageView = view.findViewById(R.id.profileImageView)
        usernameEditText = view.findViewById(R.id.usernameEditText)
        emailEditText = view.findViewById(R.id.emailEditText)
        changeUsernameButton = view.findViewById(R.id.changeUsernameButton)
        changePasswordButton = view.findViewById(R.id.changePasswordButton)
        logoutButton = view.findViewById(R.id.logoutButton)

        initFirebase()
        loadUserProfile()
        setupClickListeners()

        return view
    }

    private fun initFirebase() {
        database = FirebaseDatabase.getInstance(
            "https://quizapp-4d24a-default-rtdb.asia-southeast1.firebasedatabase.app"
        ).getReference("users")

        storage = FirebaseStorage.getInstance().reference
    }

    private fun setupClickListeners() {
        profileImageView.setOnClickListener {
            showImagePickerDialog()
        }

        changeUsernameButton.setOnClickListener {
            updateUsername()
        }

        changePasswordButton.setOnClickListener {
            showChangePasswordDialog()
        }

        logoutButton.setOnClickListener {
            showLogoutDialog()
        }
    }

    private fun showImagePickerDialog() {
        val options = arrayOf("Take Photo", "Choose from Gallery", "Cancel")

        AlertDialog.Builder(requireContext())
            .setTitle("Select Profile Picture")
            .setItems(options) { dialog, which ->
                when (which) {
                    0 -> checkCameraPermissionAndOpen()
                    1 -> checkStoragePermissionAndOpen()
                    2 -> dialog.dismiss()
                }
            }
            .show()
    }

    private fun checkCameraPermissionAndOpen() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA)
            != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(Manifest.permission.CAMERA),
                CAMERA_PERMISSION_CODE
            )
        } else {
            openCamera()
        }
    }

    private fun checkStoragePermissionAndOpen() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_EXTERNAL_STORAGE)
            != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                STORAGE_PERMISSION_CODE
            )
        } else {
            openGallery()
        }
    }

    private fun openCamera() {
        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        cameraLauncher.launch(cameraIntent)
    }

    private fun openGallery() {
        val galleryIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        galleryLauncher.launch(galleryIntent)
    }

    // Forward onRequestPermissionsResult from hosting activity to fragment
    fun onRequestPermissionsResultFragment(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            CAMERA_PERMISSION_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    openCamera()
                } else {
                    Toast.makeText(requireContext(), "Camera permission denied", Toast.LENGTH_SHORT).show()
                }
            }
            STORAGE_PERMISSION_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    openGallery()
                } else {
                    Toast.makeText(requireContext(), "Storage permission denied", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun uploadImageToFirebase(bitmap: Bitmap) {
        Log.d(TAG, "Starting image upload to Firebase Storage")

        val baos = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, baos)
        val data = baos.toByteArray()

        val profileImageRef = storage.child("profile_images/$userId.jpg")

        Toast.makeText(requireContext(), "Uploading image...", Toast.LENGTH_SHORT).show()

        profileImageRef.putBytes(data)
            .addOnSuccessListener {
                Log.d(TAG, "Image uploaded successfully")
                profileImageRef.downloadUrl.addOnSuccessListener { uri ->
                    Log.d(TAG, "Download URL obtained: $uri")
                    saveImageUrlToDatabase(uri.toString())
                    Toast.makeText(requireContext(), "Profile picture updated successfully", Toast.LENGTH_SHORT).show()
                }.addOnFailureListener { exception ->
                    Log.e(TAG, "Failed to get download URL", exception)
                    Toast.makeText(requireContext(), "Failed to get image URL", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { exception ->
                Log.e(TAG, "Image upload failed", exception)
                Toast.makeText(requireContext(), "Failed to upload image: ${exception.message}", Toast.LENGTH_LONG).show()
            }
    }

    private fun saveImageUrlToDatabase(imageUrl: String) {
        val userRef = database.child(userId)
        userRef.child("profileImageUrl").setValue(imageUrl)
            .addOnSuccessListener {
                Log.d(TAG, "Image URL saved to database")
            }
            .addOnFailureListener { exception ->
                Log.e(TAG, "Failed to save image URL to database", exception)
            }
    }

    private fun loadUserProfile() {
        Log.d(TAG, "Loading user profile from Firebase")

        database.child(userId).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val username = snapshot.child("username").getValue(String::class.java) ?: "User"
                    val email = snapshot.child("email").getValue(String::class.java) ?: "user@gmail.com"
                    val profileImageUrl = snapshot.child("profileImageUrl").getValue(String::class.java)

                    usernameEditText.setText(username)
                    emailEditText.setText(email)

                    profileImageUrl?.let { url ->
                        loadImageFromUrl(url)
                    }

                    Log.d(TAG, "Profile loaded - Username: $username, Email: $email")
                } else {
                    createDefaultUserProfile()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e(TAG, "Failed to load user profile", error.toException())
                Toast.makeText(requireContext(), "Failed to load profile", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun createDefaultUserProfile() {
        val defaultUser = mapOf(
            "username" to "Salsa",
            "email" to "salsa@gmail.com",
            "profileImageUrl" to ""
        )

        database.child(userId).setValue(defaultUser)
            .addOnSuccessListener {
                Log.d(TAG, "Default user profile created")
                usernameEditText.setText("Salsa")
                emailEditText.setText("salsa@gmail.com")
            }
            .addOnFailureListener { exception ->
                Log.e(TAG, "Failed to create default profile", exception)
            }
    }

    private fun loadImageFromUrl(url: String) {
        Log.d(TAG, "Profile image URL: $url")
        // TODO: Use Glide or Picasso here, example:
        // Glide.with(this).load(url).into(profileImageView)
    }

    private fun updateUsername() {
        val newUsername = usernameEditText.text.toString().trim()

        if (newUsername.isEmpty()) {
            Toast.makeText(requireContext(), "Username cannot be empty", Toast.LENGTH_SHORT).show()
            return
        }

        database.child(userId).child("username").setValue(newUsername)
            .addOnSuccessListener {
                Toast.makeText(requireContext(), "Username updated successfully", Toast.LENGTH_SHORT).show()
                Log.d(TAG, "Username updated to: $newUsername")
            }
            .addOnFailureListener { exception ->
                Toast.makeText(requireContext(), "Failed to update username", Toast.LENGTH_SHORT).show()
                Log.e(TAG, "Failed to update username", exception)
            }
    }

    private fun showChangePasswordDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_change_password, null)
        val currentPasswordEditText = dialogView.findViewById<EditText>(R.id.currentPasswordEditText)
        val newPasswordEditText = dialogView.findViewById<EditText>(R.id.newPasswordEditText)
        val confirmPasswordEditText = dialogView.findViewById<EditText>(R.id.confirmPasswordEditText)

        AlertDialog.Builder(requireContext())
            .setTitle("Change Password")
            .setView(dialogView)
            .setPositiveButton("Change") { _, _ ->
                val currentPassword = currentPasswordEditText.text.toString()
                val newPassword = newPasswordEditText.text.toString()
                val confirmPassword = confirmPasswordEditText.text.toString()

                if (validatePasswordChange(currentPassword, newPassword, confirmPassword)) {
                    changePassword(newPassword)
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun validatePasswordChange(current: String, new: String, confirm: String): Boolean {
        if (current.isEmpty() || new.isEmpty() || confirm.isEmpty()) {
            Toast.makeText(requireContext(), "All fields are required", Toast.LENGTH_SHORT).show()
            return false
        }

        if (new != confirm) {
            Toast.makeText(requireContext(), "New passwords don't match", Toast.LENGTH_SHORT).show()
            return false
        }

        if (new.length < 6) {
            Toast.makeText(requireContext(), "Password must be at least 6 characters", Toast.LENGTH_SHORT).show()
            return false
        }

        return true
    }

    private fun changePassword(newPassword: String) {
        database.child(userId).child("password").setValue(newPassword)
            .addOnSuccessListener {
                Toast.makeText(requireContext(), "Password changed successfully", Toast.LENGTH_SHORT).show()
                Log.d(TAG, "Password updated")
            }
            .addOnFailureListener { exception ->
                Toast.makeText(requireContext(), "Failed to change password", Toast.LENGTH_SHORT).show()
                Log.e(TAG, "Failed to change password", exception)
            }
    }

    private fun showLogoutDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("Logout")
            .setMessage("Are you sure you want to logout?")
            .setPositiveButton("Logout") { _, _ ->
                logout()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun logout() {
        Toast.makeText(requireContext(), "Logged out successfully", Toast.LENGTH_SHORT).show()

        val intent = Intent(requireActivity(), MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        requireActivity().finish()
    }
}