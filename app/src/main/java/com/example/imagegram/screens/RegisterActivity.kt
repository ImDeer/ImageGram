package com.example.imagegram.screens

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.imagegram.R
import com.example.imagegram.models.User
import com.example.imagegram.screens.common.coordinateBtnsAndInputs
import com.example.imagegram.screens.common.showToast
import com.example.imagegram.screens.home.HomeActivity
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.fragment_register_email.*
import kotlinx.android.synthetic.main.fragment_register_username.*

class RegisterActivity : AppCompatActivity(), EmailFragment.Listener, UsernameFragment.Listener {
    private val TAG = "RegisterActivity"

    private var mEmail: String? = null
    private lateinit var mAuth: FirebaseAuth
    private lateinit var mDatabase: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)


        mAuth = FirebaseAuth.getInstance()
        mDatabase = FirebaseDatabase.getInstance().reference

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .add(R.id.register_frame_layout, EmailFragment()).commit()
        }
    }


    override fun onNext(email: String) {
        if (email.isNotEmpty()) {
            mEmail = email
            mAuth.fetchSignInMethodsForEmail(email) { signInMethods ->
                if (signInMethods.isEmpty()) {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.register_frame_layout, UsernameFragment())
                        .addToBackStack(null) //если нажать кнопку назад чтобы вернулся предыдущий фрагмент, налл - потому что не нужно нам имя
                        .commit()
                } else {
                    showToast(getString(R.string.this_email_already_exists))
                }
            }
        } else {
            showToast(getString(R.string.pls_enter_email))

        }
    }


    override fun onRegister(username: String, pass: String) {

        if (username.isNotEmpty() && pass.isNotEmpty()) {
            val email = mEmail
            if (email != null) { // проверили email
                mAuth.createUserWithEmailAndPassword(
                    email,
                    pass
                ) {// пытаемся создать пользователя с email и паролем
                    val user = User(username = username, email = email)
                    mDatabase.createUser(it.user!!.uid, user) {
                        startHomeActivity()
                    }
                }
            } else {
                Log.e(TAG, "onRegister: email is null")
                showToast(getString(R.string.pls_enter_email))
                supportFragmentManager.popBackStack()
            }
        } else {
            showToast(getString(R.string.pls_enter_username_pass))
        }
    }


    private fun unknownRegisterError(it: Task<out Any>) {
        Log.e(TAG, "failed to create profile: ", it.exception)
        showToast(getString(R.string.smth_wrong_happened_pls_try_again))
    }

    private fun startHomeActivity() {
        startActivity(Intent(this, HomeActivity::class.java))
        finish() // чтобы не вернуться опять на экран регистрации
    }

    private fun FirebaseAuth.fetchSignInMethodsForEmail(
        email: String,
        onSuccess: (List<String>) -> Unit
    ) {
        fetchSignInMethodsForEmail(email).addOnCompleteListener {
            if (it.isSuccessful) {
                onSuccess(it.result?.signInMethods ?: emptyList<String>())
            } else {
                showToast(it.exception!!.message!!)
            }
        }

    }

    private fun DatabaseReference.createUser(uid: String, user: User, onSuccess: () -> Unit) {
        val reference = child("users").child(uid)
        reference.setValue(user).addOnCompleteListener {
            if (it.isSuccessful) { // пользователь зарегистрировался
                onSuccess()
            } else {
                unknownRegisterError(it)
            }
        }
    }

    private fun FirebaseAuth.createUserWithEmailAndPassword(
        email: String, pass: String,
        onSuccess: (AuthResult) -> Unit
    ) {
        createUserWithEmailAndPassword(email, pass)
            .addOnCompleteListener {// пытаемся создать пользователя с email и паролем
                if (it.isSuccessful) { // если удалось создать пользователя, создаем профиль
                    onSuccess(it.result!!)
                } else {
                    unknownRegisterError(it)
                }
            }

    }


}

//1 - Email, next
class EmailFragment : Fragment() {
    private lateinit var mListener: Listener

    interface Listener {
        fun onNext(email: String)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_register_email, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        coordinateBtnsAndInputs(
            signup_next_bt,
            signup_email_input
        )

        signup_next_bt.setOnClickListener {
            val email = signup_email_input.text.toString()
            mListener.onNext(email)
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mListener = context as Listener
    }
}

//2 - Name, Password - Sign Up
class UsernameFragment : Fragment() {
    private lateinit var mListener: Listener

    interface Listener {
        fun onRegister(username: String, pass: String)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_register_username, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        coordinateBtnsAndInputs(
            signup_finish_bt,
            signup_username_input,
            signup_pass_input
        )
        signup_finish_bt.setOnClickListener {
            val username = signup_username_input.text.toString()
            val pass = signup_pass_input.text.toString()
            mListener.onRegister(username, pass)
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mListener = context as Listener
    }

}
