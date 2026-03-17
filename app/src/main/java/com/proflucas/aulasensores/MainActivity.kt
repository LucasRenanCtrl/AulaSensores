package com.proflucas.aulasensores

import android.content.Context
import android.graphics.Color
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.proflucas.aulasensores.ui.theme.AulaSensoresTheme
import kotlin.collections.get

class MainActivity : ComponentActivity(), SensorEventListener {
    private lateinit var sensorManager: SensorManager
    private lateinit var square: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        //Faz o celular ficar no light mode
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        square = findViewById<TextView>(R.id.tv_square)

        setUpDoSensor()
    }

    private fun setUpDoSensor() {
        //Inicializando o sensor manager
        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager

        //O .also permite que façamos várias ações ao mesmo tempo
        //No escopo do .also o objeto é referenciado como "it"
        sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)?.also {
            sensorManager.registerListener(
                this,
                it,
                SensorManager.SENSOR_DELAY_FASTEST,
                SensorManager.SENSOR_DELAY_FASTEST
            )
        }


    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor?.type == Sensor.TYPE_ACCELEROMETER) {
            val sides = event.values[0] //Valor do X
            val upDown = event.values[1] //Valor do Y

            square.apply {
                rotationX = upDown*3f
                rotationY = sides*3f
                rotation = -sides
                translationX = sides * -10
                translationX = upDown * 10
            }

            //Kotlin não tem ternário, mas podemos atingir o mesmo resultado escrevendo assim:
            val color = if(upDown.toInt() == 0 && sides.toInt() == 0) Color.GREEN else Color.RED
square.setBackgroundColor(color)

            square.text = "Up/Down ${upDown.toInt()}\nLeft/Right ${sides.toInt()}"

        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        return
    }

    override fun onDestroy() { // Para evitar problemas ao fechar o app
        sensorManager.unregisterListener(this)
        super.onDestroy()
    }
}
