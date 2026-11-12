package edu.temple.basictimer

import android.app.Service
import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.IBinder
import android.view.ContextMenu
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.launch
import kotlin.concurrent.timer

class MainActivity : AppCompatActivity() {

    var timerBinder : TimerService.TimerBinder? = null

    val textView : TextView by lazy { findViewById(R.id.textView) }

    //connects and disconnects service
    val serviceConnection = object: ServiceConnection {
        override fun onServiceConnected(
            name: ComponentName?,
            service: IBinder?
        ) {
            timerBinder = service as TimerService.TimerBinder
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            timerBinder = null
        }

    }



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // add menu inflater

        //Transfers data to textview
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.RESUMED) {
                TimerInfo.currentTime.collect {
                    textView.text = it.toString()
                }
            }
        }
        bindService(
            Intent(this, TimerService::class.java),
            serviceConnection,
            BIND_AUTO_CREATE
        )

        //Start button
        //changes state and text for green button
        findViewById<Button>(R.id.startButton).setOnClickListener {
            timerBinder?.run {
                if (!isRunning) {
                    timerBinder?.start(100)
                    (it as Button).text = "Pause"
                } else if (!isPaused) {
                    timerBinder?.pause()
                    (it as Button).text = "Resume"
                } else if (isPaused) {
                    timerBinder?.pause()
                    (it as Button).text = "Pause"

                }
            }
        }

        //Stop Button
        findViewById<Button>(R.id.stopButton).setOnClickListener {
            timerBinder?.stop()
            findViewById<Button>(R.id.startButton).text = "Start"
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu, menu)

        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        return when (item.itemId) {
            R.id.startButton -> {
                timerBinder?.start(100)
                true
            }

            R.id.stopButton -> {
                timerBinder?.stop()
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }
}