package com.xabbok.ambinetvortex.presentation.activities

import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import by.kirich1409.viewbindingdelegate.viewBinding
import com.xabbok.ambinetvortex.R
import com.xabbok.ambinetvortex.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity(R.layout.activity_main) {
    private val binding: ActivityMainBinding by viewBinding(ActivityMainBinding::bind)
    private val navHostController by lazy {
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment_container) as NavHostFragment
        val navController = navHostFragment.navController
        navController
    }

    override fun onStart() {
        super.onStart()


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