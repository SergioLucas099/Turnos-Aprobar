package com.example.turnosaprobar

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.turnosaprobar.fragments.TurnosCanceladosFragment
import com.example.turnosaprobar.fragments.TurnosEsperaFragment
import com.google.android.material.bottomnavigation.BottomNavigationView

class VentanaPrincipal : AppCompatActivity() {

    private lateinit var bottomNavigationView: BottomNavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ventana_principal)

        bottomNavigationView = findViewById(R.id.bottomNavigationView)

        bottomNavigationView.setOnItemSelectedListener { menuItem ->
            when(menuItem.itemId){
                R.id.firstFragment -> {
                    replaceFragment(TurnosEsperaFragment())
                    true
                }
                R.id.secondFragment -> {
                    replaceFragment(TurnosCanceladosFragment())
                    true
                }
                else -> false
            }
        }
        replaceFragment(TurnosEsperaFragment())
    }
    private fun replaceFragment(fragment: Fragment){
        supportFragmentManager.beginTransaction().replace(R.id.frame_container, fragment).commit()
    }
}