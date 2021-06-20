package com.example.imagegram.activities

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.imagegram.R
import com.example.imagegram.models.User
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.activity_register.*
import kotlinx.android.synthetic.main.fragment_register_email.*
import kotlinx.android.synthetic.main.fragment_register_username.*
import net.yslibrary.android.keyboardvisibilityevent.KeyboardVisibilityEvent
import net.yslibrary.android.keyboardvisibilityevent.KeyboardVisibilityEventListener

class RegisterActivity : AppCompatActivity(), EmailFragment.Listener, UsernameFragment.Listener {
    private val TAG = "RegisterActivity"

    private var mEmail: String? = null
    private var mPass: String? = null
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


    override fun onNext(email: String, pass: String) {
        if (email.isNotEmpty() && pass.isNotEmpty()) {
            mEmail = email
            mPass = pass
            mAuth.fetchSignInMethodsForEmail(email).addOnCompleteListener {
                if (it.isSuccessful) {
                    if (it.result?.signInMethods?.isEmpty() != false) {
                        supportFragmentManager.beginTransaction()
                            .replace(R.id.register_frame_layout, UsernameFragment())
                            .addToBackStack(null) //если нажать кнопку назад чтобы вернулся предыдущий фрагмент, налл - потому что не нужно нам имя
                            .commit()
                    } else {
                        showToast("This email already exists")
                    }
                } else {
                    showToast(it.exception!!.message!!)
                }
            }
        } else {
            showToast("Please enter Email and Password!")

        }
    }

    override fun onRegister(username: String, name: String) {

        if (username.isNotEmpty()) {
            val email = mEmail
            val pass = mPass
            if (email != null && pass != null) { // проверили email и пароль
                mAuth.createUserWithEmailAndPassword(email, pass)
                    .addOnCompleteListener {// пытаемся создать пользователя с email и паролем
                        if (it.isSuccessful) { // если удалось создать пользователя, создаем профиль
                            val user = User(name, username, email)
                            val reference = mDatabase.child("users").child(it.result!!.user!!.uid)
                            reference.setValue(user).addOnCompleteListener {
                                if (it.isSuccessful) { // пользователь зарегистрировался
                                    startActivity(Intent(this, HomeActivity::class.java))
                                    finish() // чтобы не вернуться опять на экран регистрации
                                } else {
                                    unknownRegisterError(it)
                                }
                            }
                        } else {
                            unknownRegisterError(it)
                        }
                    }
            } else {
                Log.e(TAG, "onRegister: email or password is null")
                showToast("Please enter Email and Password")
                supportFragmentManager.popBackStack()
            }
        } else {
            showToast("Please enter Username!")
        }
    }

    private fun unknownRegisterError(it: Task<out Any>) {
        Log.e(TAG, "failed to create profile: ", it.exception)
        showToast("Something went wrong. Please try again later")
    }

}

//1 - Email, next
class EmailFragment : Fragment() {
    private lateinit var mListener: Listener

    interface Listener {
        fun onNext(email: String, pass: String)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_register_email, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        signup_next_bt.setOnClickListener {
            val email = signup_email_input.text.toString()
            val pass = signup_password_input.text.toString()
            mListener.onNext(email, pass)
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
        fun onRegister(username: String, name: String)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_register_username, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        signup_finish_bt.setOnClickListener {
            val username = signup_username_input.text.toString()
            val name = signup_name_input.text.toString()
            mListener.onRegister(username, name)
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mListener = context as Listener
    }
}
