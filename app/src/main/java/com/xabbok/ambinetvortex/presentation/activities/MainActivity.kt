package com.xabbok.ambinetvortex.presentation.activities

import android.content.Context
import android.os.Bundle
import android.os.PersistableBundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import by.kirich1409.viewbindingdelegate.viewBinding
import com.google.firebase.messaging.FirebaseMessaging
import com.xabbok.ambinetvortex.R
import com.xabbok.ambinetvortex.databinding.ActivityMainBinding
import com.xabbok.ambinetvortex.utils.setStatusBarColor
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity(R.layout.activity_main) {
    @Inject
    lateinit var firebaseMsg: FirebaseMessaging

    private val binding: ActivityMainBinding by viewBinding(ActivityMainBinding::bind)
    private val navHostController by lazy {
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment_container) as NavHostFragment
        val navController = navHostFragment.navController
        navController
    }

    override fun onCreate(savedInstanceState: Bundle?, persistentState: PersistableBundle?) {
        super.onCreate(savedInstanceState, persistentState)

        firebaseMsg.token.addOnCompleteListener {
            try {
                if (it.isComplete) {
                    val firebaseToken = it.result.toString()
                    this.getPreferences(Context.MODE_PRIVATE).edit().putString("fbt", firebaseToken)
                        .apply()
                    println(firebaseToken)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    override fun onStart() {
        super.onStart()
        setStatusBarColor(this)

        binding.bottomNav.setupWithNavController(navHostController)

        navHostController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.mainPosts, R.id.usersFragment, R.id.eventsFragment, R.id.profileFragment -> {
                    binding.bottomNav.isVisible = true
                }

                else -> {
                    binding.bottomNav.isVisible = false
                }
            }
        }
    }
}