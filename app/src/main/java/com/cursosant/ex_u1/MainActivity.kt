package com.cursosant.ex_u1

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import kotlin.random.Random

class MainActivity : AppCompatActivity() {

    lateinit var btnRojo: LinearLayout
    lateinit var btnVerde: LinearLayout
    lateinit var btnAzul: LinearLayout
    lateinit var btnAmarillo: LinearLayout
    lateinit var btnIniciar: Button
    lateinit var btnRendirse: Button
    lateinit var txtSimondice: TextView
    lateinit var txtHighScore: TextView
    val secuencia = mutableListOf<Int>()
    val entrada = mutableListOf<Int>()
    var ronda = 0
    var jugando = false
    val handler = Handler(Looper.getMainLooper())
    lateinit var prefs: SharedPreferences
    val PREFS_NAME = "SimonDicePrefs"
    val KEY_HIGH_SCORE = "high_score"
    var record = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        record = prefs.getInt(KEY_HIGH_SCORE, 0)

        btnRojo = findViewById(R.id.btnRojo)
        btnVerde = findViewById(R.id.btnVerde)
        btnAzul = findViewById(R.id.btnAzul)
        btnAmarillo = findViewById(R.id.btnAmarillo)
        btnIniciar = findViewById(R.id.btnIniciar)
        btnRendirse = findViewById(R.id.btnRendirse)
        txtSimondice = findViewById(R.id.txtSimondice)
        txtHighScore = findViewById(R.id.txtHighScore)

        btnRendirse.isEnabled = false
        bloquearColores()
        txtHighScore.text = "Record: $record"

        btnIniciar.setOnClickListener {
            if (jugando) {
                empezarDeNuevo()
            } else {
                empezar()
            }
        }
        btnRendirse.setOnClickListener {
            Toast.makeText(this, "Te Rendiste - Ronda alcanzada: $ronda", Toast.LENGTH_SHORT).show()
            terminar()
        }
        btnRojo.setOnClickListener {
            registrarColor(1)
        }
        btnVerde.setOnClickListener {
            registrarColor(2)
        }
        btnAzul.setOnClickListener {
            registrarColor(3)
        }
        btnAmarillo.setOnClickListener {
            registrarColor(4)
        }
    }

    fun empezar() {
        desbloquearColores()
        secuencia.clear()
        entrada.clear()
        ronda = 0
        jugando = true
        btnIniciar.text = "Reiniciar"
        btnRendirse.isEnabled = true
        txtSimondice.text = "Simón Dice..."
        nuevaRonda()
    }

    fun empezarDeNuevo() {
        Toast.makeText(this, "Juego reiniciado", Toast.LENGTH_SHORT).show()
        handler.removeCallbacksAndMessages(null)
        empezar()
    }

    fun terminar() {
        jugando = false
        btnIniciar.text = "Iniciar"
        btnRendirse.isEnabled = false
        bloquearColores()
        txtSimondice.text = "Listo para jugar..."

        if (ronda > record) {
            record = ronda
            prefs.edit().putInt(KEY_HIGH_SCORE, record).apply()
            Toast.makeText(this, "¡Nuevo Record: $record!", Toast.LENGTH_LONG).show()
        }

        txtHighScore.text = "Record: $record"
    }

    fun nuevaRonda() {
        entrada.clear()
        ronda++
        txtHighScore.text = "Ronda: $ronda | Record: $record"

        secuencia.add(Random.nextInt(1, 5))

        handler.postDelayed({
            mostrarSecuencia()
        }, 1000)
    }

    fun mostrarSecuencia() {
        bloquearColores()
        txtSimondice.text = "Simón Dice..."
        var tiempo: Long = 0

        for (i in secuencia.indices) {
            val color = secuencia[i]
            handler.postDelayed({ prender(color, true) }, tiempo)
            tiempo += 500
            handler.postDelayed({ prender(color, false) }, tiempo)
            tiempo += 250
        }

        handler.postDelayed({
            desbloquearColores()
            txtSimondice.text = "Tu turno (${secuencia.size} colores)"
        }, tiempo)
    }

    fun prender(color: Int, encender: Boolean) {
        val boton: View
        val apagado: Int
        val encendido: Int

        when (color) {
            1 -> {
                boton = btnRojo
                apagado = R.color.rojo
                encendido = R.color.rojo_brillante
            }
            2 -> {
                boton = btnVerde
                apagado = R.color.verde
                encendido = R.color.verde_brillante
            }
            3 -> {
                boton = btnAzul
                apagado = R.color.azul
                encendido = R.color.azul_brillante
            }
            else -> {
                boton = btnAmarillo
                apagado = R.color.amarillo
                encendido = R.color.amarillo_brillante
            }
        }

        val colorFinal = if (encender) encendido else apagado
        boton.setBackgroundColor(ContextCompat.getColor(this, colorFinal))
    }

    fun registrarColor(color: Int) {
        if (!jugando) return

        // Efecto visual al presionar
        prender(color, true)
        handler.postDelayed({ prender(color, false) }, 200)

        // Agregar el color a la entrada del jugador
        entrada.add(color)

        // Actualizar el texto para mostrar progreso
        txtSimondice.text = "Tu turno (${entrada.size}/${secuencia.size})"

        // Si ya completó toda la secuencia, validar
        if (entrada.size == secuencia.size) {
            handler.postDelayed({
                validarSecuencia()
            }, 300)
        }
    }

    fun validarSecuencia() {
        bloquearColores()

        // Verificar si toda la secuencia es correcta
        var correcto = true
        for (i in entrada.indices) {
            if (entrada[i] != secuencia[i]) {
                correcto = false
                break
            }
        }

        if (correcto) {
            txtSimondice.text = "¡Correcto!"
            handler.postDelayed({
                nuevaRonda()
            }, 1000)
        } else {
            txtSimondice.text = "¡Perdiste!"
            Toast.makeText(this, "Perdiste - Ronda alcanzada: $ronda", Toast.LENGTH_LONG).show()
            handler.postDelayed({
                terminar()
            }, 1500)
        }
    }

    fun desbloquearColores() {
        btnRojo.isClickable = true
        btnVerde.isClickable = true
        btnAzul.isClickable = true
        btnAmarillo.isClickable = true
    }

    fun bloquearColores() {
        btnRojo.isClickable = false
        btnVerde.isClickable = false
        btnAzul.isClickable = false
        btnAmarillo.isClickable = false
    }
}