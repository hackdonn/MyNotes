package com.app.notes.activities

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import com.app.notes.R
import com.app.notes.databinding.ActivityMainBinding
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.BuildConfig
import com.firebase.ui.auth.ErrorCodes
import com.firebase.ui.auth.FirebaseAuthUIActivityResultContract
import com.firebase.ui.auth.data.model.FirebaseAuthUIAuthenticationResult
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class MainActivity : AppCompatActivity() {
    private val TAG = MainActivity::class.qualifiedName
    private lateinit var binding: ActivityMainBinding
    private var referred = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        val auth = Firebase.auth //FirebaseAuth.getInstance()


        if (auth.currentUser != null && !auth.currentUser!!.isAnonymous /*&& auth.currentUser.providerData*/) {
            val intent = Intent(this, ListActivity::class.java)
            intent.putExtra(USER_ID, auth.currentUser!!.uid)
            startActivity(intent)
        }

        val countries = ArrayList<String>()
        countries.add("+234")
        //the ISO 2-character code can be used also : "NG"

        binding.btnSignIn.setOnClickListener {
            val providers = arrayListOf(
                AuthUI.IdpConfig.EmailBuilder().build(),
                AuthUI.IdpConfig.GoogleBuilder().build(),
//                AuthUI.IdpConfig.FacebookBuilder().build(),
//                AuthUI.IdpConfig.TwitterBuilder().build(),
                AuthUI.IdpConfig.PhoneBuilder()
//                    .setDefaultCountryIso("NG")
//                    .setWhitelistedCountries(countries)
                    .build()
            )

            val authUI = AuthUI.getInstance()
//            authUI.useEmulator("10.0.2.2", 9099)
            val signInIntent = Intent(
                authUI.createSignInIntentBuilder()
                    .setAvailableProviders(providers)
                    .setIsSmartLockEnabled(!BuildConfig.DEBUG, true)
                    .enableAnonymousUsersAutoUpgrade()
                    .setTheme(R.style.SignInTheme)
                    .build()
            )

            signInLauncher.launch(signInIntent)
        }

        if (intent.hasExtra(SIGNIN_MESSAGE)) {
            binding.btnSkip.isVisible = false
            binding.tvMessage.text = intent.getStringExtra(SIGNIN_MESSAGE)
            referred = true
        } else {
            binding.btnSkip.setOnClickListener {
                auth.signInAnonymously()
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful)
                            loadListActivity()
                        else {
                            Log.e(TAG, "Anonymous sign-in failed", task.exception)
                            Toast.makeText(this, "Anonymous Sign-in failed", Toast.LENGTH_LONG)
                                .show()
                        }
                    }
            }
        }
    }

    override fun onBackPressed() {
        setResult(Activity.RESULT_CANCELED)
        finish()
    }

    private val signInLauncher =
        registerForActivityResult(FirebaseAuthUIActivityResultContract()) { result ->
            this.onSignInResult(result)
        }

    private fun onSignInResult(result: FirebaseAuthUIAuthenticationResult) {
        val response = result.idpResponse

        if (result.resultCode == RESULT_OK) {
            //Successfully signed in
            if (referred) {
                setResult(RESULT_OK)
                finish()
            } else
                loadListActivity()
        } else {
            // Sign in failed. If response is null the user canceled the
            // sign-in flow using the back button. Otherwise check
            // response.getError().getErrorCode() and handle the error.
            if (response == null) {
                Log.e(TAG, "Back button pressed")
            } else if (response.error?.errorCode == ErrorCodes.ANONYMOUS_UPGRADE_MERGE_CONFLICT) {
                Toast.makeText(this, "Anonymous Upgrade", Toast.LENGTH_SHORT).show()
                // 1st save a copy of the data associated with the anonymous acct but in the case of our app, no data is associated yet

                //retrieve the existing account for linking
                val fullCredential = response.credentialForLinking
                //since there's no anom data let's skip data merging and just sign in
                if (fullCredential != null) {
                    val auth = Firebase.auth
                    val prevUser = auth.currentUser
                    auth.signInWithCredential(fullCredential)
                        .addOnSuccessListener { res ->
                            val currentUser = res.user
                            Log.d(TAG, "SignInWithCredential: Success")
                            Toast.makeText(
                                this,
                                "signInWithCredential: Success!",
                                Toast.LENGTH_SHORT
                            ).show()
                            // Merge prev and currentUser accounts and data
                            if (referred) {
                                val intentData = Intent()
                                intentData.putExtra(USER_ID, currentUser!!.uid)
                                setResult(RESULT_OK, intentData)
                                finish()
                            } else {
                                loadListActivity()
                            }
                        }
                        .addOnFailureListener {
                            //
                        }
                }

            } else {
                Log.e(TAG, "Sign-in failed due to: ${response.error!!.errorCode}", response.error)
                Toast.makeText(this, "Sign-in failed: Null full credential", Toast.LENGTH_LONG)
                    .show()
            }
            // ...
        }
        if (result.resultCode != RESULT_OK && result.resultCode != RESULT_CANCELED) {
            Log.e(TAG, "Sign-in failed", response?.error)
            Toast.makeText(this, "Sign-in failed!", Toast.LENGTH_LONG).show()
        }
    }

    private fun loadListActivity() {
        val user = Firebase.auth.currentUser
        val intent = Intent(this, ListActivity::class.java).apply {
            putExtra(USER_ID, user!!.uid)
        }
        startActivity(intent)
    }

    companion object {
        const val USER_ID = "user_id"
        const val SIGNIN_MESSAGE = "signin_message"
    }
}