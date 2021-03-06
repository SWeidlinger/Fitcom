package at.fhooe.mc.fitcom.ui.login

import android.content.Context
import android.content.Intent
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.core.view.isVisible
import at.fhooe.mc.fitcom.MainActivity
import at.fhooe.mc.fitcom.R
import at.fhooe.mc.fitcom.databinding.ActivitySignUpBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class SignUpActivity : AppCompatActivity(), View.OnClickListener{
    private lateinit var binding: ActivitySignUpBinding
    private var mAuth = FirebaseAuth.getInstance()
    private var mDb = Firebase.firestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(binding.root)

        with(binding) {
            activitySignUpButtonSignUp.setOnClickListener(this@SignUpActivity)
        }
    }

    override fun onClick(_view: View?) {
        when (_view?.id) {
            R.id.activity_signUp_button_signUp -> {
                createUser()
            }
        }
    }

    //to create a new user
    private fun createUser() {
        val email = binding.activitySignUpTextInputLayoutEmail.editText?.text
        val password = binding.activitySignUpTextInputLayoutPassword.editText?.text
        val confirmPassword = binding.activitySignUpTextInputLayoutConfirmPassword.editText?.text
        val firstName = binding.activitySignUpTextInputLayoutFirstName.editText?.text

        if (email.isNullOrEmpty() || password.isNullOrEmpty() || confirmPassword.isNullOrEmpty() || firstName.isNullOrEmpty()) {
            if (email.isNullOrEmpty()) {
                binding.activitySignUpTextInputLayoutEmail.error = "Email can't be empty"
            }
            if (password.isNullOrEmpty()) {
                binding.activitySignUpTextInputLayoutPassword.error = "Password can't be empty"
            }
            if (confirmPassword.isNullOrEmpty()) {
                binding.activitySignUpTextInputLayoutConfirmPassword.error =
                    "Need to confirm your password"
            }
            if (firstName.isNullOrEmpty()) {
                binding.activitySignUpTextInputLayoutFirstName.error = "First name can't be empty"
            }
        } else {
            if (password.toString() == confirmPassword.toString()) {
                binding.activitySignUpProgressBar.isVisible = true
                mAuth.createUserWithEmailAndPassword(email.toString(), password.toString())
                    .addOnCompleteListener {
                        if (it.isSuccessful) {
                            //safe user in hashMap to then insert into the database
                            val user = hashMapOf(
                                "firstName" to firstName.toString(),
                                "weight" to 0f,
                                "workoutNames" to ArrayList<String>(),
                                "workoutColors" to ArrayList<Int>(),
                                "weightArray" to ArrayList<Float>(),
                                "weightPrev" to 0f,
                                "workoutsCompleted" to 0,
                                "totalWeightLifted" to 0f,
                                "uid" to mAuth.uid
                            )
                            //get the user ID of the user and name the document in the database like that
                            mDb.collection("users").document(mAuth.uid.toString()).set(user)

                            Toast.makeText(this, "User registered successfully", Toast.LENGTH_SHORT)
                                .show()

                            //saving users first Name in shared Preferences for faster Access
                            with(
                                getSharedPreferences(
                                    "at.fhooe.mc.fitcom.FirstName",
                                    Context.MODE_PRIVATE
                                ).edit()
                            ) {
                                putString("firstName", firstName.toString())
                                apply()
                            }

                            binding.activitySignUpProgressBar.isVisible = false

                            startActivity(Intent(this, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK))
                            finish()
                        } else {
                            Toast.makeText(
                                this,
                                "User registration failed!\n" + it.exception?.message,
                                Toast.LENGTH_LONG
                            ).show()
                            binding.activitySignUpProgressBar.isVisible = false
                        }
                    }
            } else {
                binding.activitySignUpTextInputLayoutConfirmPassword.error =
                    "Password doesn't match"
            }
        }
    }
}