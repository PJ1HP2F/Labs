package com.example.timerapp.ui.settings

import android.content.res.Configuration
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.viewModels
import androidx.preference.ListPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SwitchPreferenceCompat
import com.example.timerapp.R
import com.example.timerapp.utils.LocaleHelper
import com.example.timerapp.viewmodel.SequenceViewModel
import com.example.timerapp.viewmodel.SequenceViewModelFactory
import java.util.Locale

class SettingsFragment : PreferenceFragmentCompat() {

    private val viewModel: SequenceViewModel by viewModels {
        SequenceViewModelFactory(requireActivity().application)
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preferences, rootKey)

        // Dark theme toggle
        findPreference<SwitchPreferenceCompat>("dark_theme")?.apply {
            setOnPreferenceChangeListener { _, newValue ->
                val isDark = newValue as Boolean
                if (isDark) {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                } else {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                }
                true
            }
        }

        // Font size SeekBar is handled via preference XML binding with custom approach
        // We use ListPreference for simplicity
        findPreference<ListPreference>("font_size")?.apply {
            setOnPreferenceChangeListener { _, newValue ->
                val size = (newValue as String).toFloat()
                requireActivity().resources.configuration.fontScale = size
                val config = Configuration(requireActivity().resources.configuration)
                config.fontScale = size
                val context = requireActivity().createConfigurationContext(config)
                requireActivity().resources.updateConfiguration(
                    config, context.resources.displayMetrics
                )
                true
            }
        }

        // Locale
        findPreference<ListPreference>("locale_preference")?.apply {
            setOnPreferenceChangeListener { _, newValue ->
                val lang = newValue as String
                LocaleHelper.setLocale(requireContext(), lang)
                requireActivity().recreate()
                true
            }
        }

        // Clear data
        findPreference<Preference>("clear_data")?.apply {
            setOnPreferenceClickListener {
                viewModel.deleteAll()
                Toast.makeText(requireContext(),
                    getString(R.string.data_cleared), Toast.LENGTH_SHORT).show()
                true
            }
        }
    }
}
