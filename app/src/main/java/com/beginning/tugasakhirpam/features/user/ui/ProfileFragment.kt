package com.beginning.tugasakhirpam.features.user.ui

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
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
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.beginning.tugasakhirpam.MainActivity
import com.beginning.tugasakhirpam.R
import com.bumptech.glide.Glide
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.io.ByteArrayOutputStream
import java.io.FileNotFoundException

class ProfileFragment : Fragment(R.layout.activity_profile) {
    private lateinit var profileImageView: ImageView
    private lateinit var usernameEditText: EditText
    private lateinit var emailEditText: EditText
    private lateinit var changeUsernameButton: Button
    private lateinit var changePasswordButton: Button
    private lateinit var logoutButton: Button

    private lateinit var database: DatabaseReference
    private lateinit var storage: StorageReference

    private val userId: String by lazy {
        requireActivity().intent.getStringExtra("USER_ID") ?: run {
            Toast.makeText(requireContext(), "User not authenticated", Toast.LENGTH_SHORT).show()
            ""
        }
    }
    private val userEmail: String by lazy {
        requireActivity().intent.getStringExtra("USER_EMAIL") ?: ""
    }
    private val userName: String by lazy {
        requireActivity().intent.getStringExtra("USER_NAME") ?: ""
    }

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

        profileImageView = view.findViewById(R.id.profileImageView)
        usernameEditText = view.findViewById(R.id.usernameEditText)
        emailEditText = view.findViewById(R.id.emailEditText)
        changeUsernameButton = view.findViewById(R.id.changeUsernameButton)
        changePasswordButton = view.findViewById(R.id.changePasswordButton)
        logoutButton = view.findViewById(R.id.logoutButton)

        initFirebase()
        setupClickListeners()
        loadUserProfile()

        return view
    }

    private fun initFirebase() {
        database = FirebaseDatabase.getInstance(
            "https://projek-akhir-pam-66797-default-rtdb.asia-southeast1.firebasedatabase.app"
        ).reference.child("users").child(userId)

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
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(Manifest.permission.READ_MEDIA_IMAGES),
                STORAGE_PERMISSION_CODE
            )
            openGallery()
        } else {
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                STORAGE_PERMISSION_CODE
            )
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

    private fun uploadImageToFirebase(bitmap: Bitmap) {
        val baos = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, baos)
        val data = baos.toByteArray()

        val profileImageRef = storage.child("profile_images/$userId.jpg")

        profileImageRef.putBytes(data)
            .addOnSuccessListener {
                profileImageRef.downloadUrl.addOnSuccessListener { uri ->
                    saveImageUrlToDatabase(uri.toString())
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(requireContext(), "Upload failed: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun saveImageUrlToDatabase(imageUrl: String) {
        database.child("profileImageUrl").setValue(imageUrl)
            .addOnSuccessListener {
                Toast.makeText(requireContext(), "Profile picture updated", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(requireContext(), "Failed to save image", Toast.LENGTH_SHORT).show()
            }
    }

    private fun loadUserProfile() {
        if (userId.isEmpty()) return

        usernameEditText.setText(userName)
        emailEditText.setText(userEmail)

        database.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val profileImageUrl = snapshot.child("profileImageUrl").getValue(String::class.java)
                    profileImageUrl?.let { url ->
                        Glide.with(requireContext()).load(url).into(profileImageView)
                    }

                    val firebaseUsername = snapshot.child("profile").child("username").getValue(String::class.java)
                    firebaseUsername?.let {
                        if (it != userName) {
                            usernameEditText.setText(it)
                        }
                    }
                } else {
                    Log.e(TAG, "Failed to fetch user")
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(requireContext(), "Failed to load profile", Toast.LENGTH_SHORT).show()
                Log.e(TAG, "Database error: ${error.message}")
            }
        })
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

        database.child("username").setValue(newUsername)
            .addOnSuccessListener {
                Toast.makeText(requireContext(), "Username updated", Toast.LENGTH_SHORT).show()
                requireActivity().intent.putExtra("USER_NAME", newUsername)
            }
            .addOnFailureListener { e ->
                Toast.makeText(requireContext(), "Failed to update username", Toast.LENGTH_SHORT).show()
                Log.e(TAG, "Username update failed", e)
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
                    changePassword(currentPassword, newPassword)
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

        if (new.length < 6) {
            Toast.makeText(requireContext(), "Password must be at least 6 characters", Toast.LENGTH_SHORT).show()
            return false
        }

        if (new != confirm) {
            Toast.makeText(requireContext(), "New passwords don't match", Toast.LENGTH_SHORT).show()
            return false
        }

        return true
    }

    private fun changePassword(currentPassword: String, newPassword: String) {
        val user = FirebaseAuth.getInstance().currentUser

        if (user == null) {
            Toast.makeText(requireContext(), "User not authenticated", Toast.LENGTH_SHORT).show()
            return
        }

        val credential = EmailAuthProvider.getCredential(user.email ?: "", currentPassword)

        user.reauthenticate(credential).addOnCompleteListener { reauthTask ->
            if (reauthTask.isSuccessful) {
                user.updatePassword(newPassword).addOnCompleteListener { updateTask ->
                    if (updateTask.isSuccessful) {
                        Toast.makeText(
                            requireContext(),
                            "Password changed successfully",
                            Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        Toast.makeText(
                            requireContext(),
                            "Failed to change password: ${updateTask.exception?.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            } else {
                Toast.makeText(
                    requireContext(),
                    "Authentication failed: ${reauthTask.exception?.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
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