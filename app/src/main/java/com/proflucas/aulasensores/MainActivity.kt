package com.proflucas.aulasensores

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.os.CountDownTimer
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge

class MainActivity : ComponentActivity(), SensorEventListener {

    // ====================== COMPONENTES ======================
    private lateinit var sensorManager: SensorManager
    private var proximitySensor: Sensor? = null

    private lateinit var tvInstruction: TextView
    private lateinit var tvStatus: TextView
    private lateinit var tvScore: TextView
    private lateinit var tvTime: TextView
    private lateinit var btnStart: Button

    // ====================== VARIÁVEIS DO JOGO ======================
    private var score = 0
    private var gameRunning = false
    private var isClose = false          // estado atual do sensor
    private var previousClose = false   // estado anterior (para detectar mudança)
    private var requiredClose = false   // true = precisa aproximar, false = afastar

    private var timer: CountDownTimer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        // ====================== INICIALIZAÇÃO ======================
        sensorManager = getSystemService(SensorManager::class.java)
        proximitySensor = sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY)

        // Se o celular não tiver sensor de proximidade
        if (proximitySensor == null) {
            Toast.makeText(this, "Sensor de proximidade não encontrado!", Toast.LENGTH_LONG).show()
        }

        // Liga os componentes da tela
        tvInstruction = findViewById(R.id.tvInstruction)
        tvStatus = findViewById(R.id.tvStatus)
        tvScore = findViewById(R.id.tvScore)
        tvTime = findViewById(R.id.tvTime)
        btnStart = findViewById(R.id.btnStart)

        btnStart.setOnClickListener { startGame() }
    }

    // ====================== INICIAR O JOGO ======================
    private fun startGame() {
        if (proximitySensor == null) return

        score = 0
        updateScore()
        gameRunning = true
        btnStart.isEnabled = false
        btnStart.text = "Jogando..."

        // Timer total de 30 segundos
        timer = object : CountDownTimer(30000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                tvTime.text = "Tempo: ${millisUntilFinished / 1000}s"
            }

            override fun onFinish() {
                endGame()
            }
        }.start()

        // Primeiro desafio
        newChallenge()

        // Registra o sensor (o mais importante para a aula!)
        sensorManager.registerListener(this, proximitySensor, SensorManager.SENSOR_DELAY_NORMAL)
    }

    // ====================== NOVO DESAFIO ======================
    private fun newChallenge() {
        requiredClose = (0..1).random() == 0   // escolhe aleatório

        tvInstruction.text = if (requiredClose) "APROXIME a mão!" else "AFASTE a mão!"
        tvInstruction.setTextColor(if (requiredClose) 0xFF4CAF50.toInt() else 0xFFFF9800.toInt())
    }

    // ====================== SENSOR MUDOU ======================
    override fun onSensorChanged(event: SensorEvent?) {
        if (!gameRunning || event == null) return

        val distance = event.values[0]
        isClose = distance < 1.0f   // 0.0 = perto, 5.0 = longe (funciona na maioria dos celulares)

        // Atualiza status na tela
        tvStatus.text = if (isClose) "Status atual: PRÓXIMO" else "Status atual: DISTANTE"
        tvStatus.setTextColor(if (isClose) 0xFF4CAF50.toInt() else 0xFFFF9800.toInt())

        // Só pontua quando o estado MUDA (evita pontuar várias vezes segurando a mão)
        if (isClose != previousClose) {
            previousClose = isClose

            // Se a mudança foi exatamente o que o jogo pediu → ponto!
            if (isClose == requiredClose) {
                score++
                updateScore()
                newChallenge()   // novo desafio na hora
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        return // Não precisamos usar
    }

    private fun updateScore() {
        tvScore.text = "Pontos: $score"
    }

    // ====================== FIM DO JOGO ======================
    private fun endGame() {
        gameRunning = false
        sensorManager.unregisterListener(this)
        timer?.cancel()

        tvInstruction.text = "FIM DE JOGO!\nSua pontuação: $score"
        tvInstruction.setTextColor(0xFFE91E63.toInt())

        btnStart.isEnabled = true
        btnStart.text = "JOGAR NOVAMENTE"
        btnStart.setOnClickListener { startGame() }
    }

    // ====================== CICLO DE VIDA (importante!) ======================
    override fun onResume() {
        super.onResume()
        // Se o jogo estiver rodando e o usuário voltar para o app, religa o sensor
        if (gameRunning) {
            sensorManager.registerListener(this, proximitySensor, SensorManager.SENSOR_DELAY_NORMAL)
        }
    }

    override fun onPause() {
        super.onPause()
        if (gameRunning) {
            sensorManager.unregisterListener(this)
        }
    }
}