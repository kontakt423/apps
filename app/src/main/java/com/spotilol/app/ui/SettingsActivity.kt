package com.spotilol.app.ui

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.spotilol.app.R
import com.spotilol.app.update.UpdateChecker

class SettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.settings_container, SettingsFragment())
                .commit()
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

    class SettingsFragment : PreferenceFragmentCompat() {
        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.preferences, rootKey)

            findPreference<Preference>("check_now")?.setOnPreferenceClickListener {
                Toast.makeText(requireContext(), R.string.pref_check_now_title, Toast.LENGTH_SHORT).show()
                UpdateChecker(requireContext()).check(
                    onUpdate = { release -> UpdateChecker.showDialog(requireContext(), release) },
                    onNone = { toast(getString(R.string.update_none)) },
                    onError = { toast(getString(R.string.update_failed)) }
                )
                true
            }
        }

        private fun toast(msg: String) =
            Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
    }
}
