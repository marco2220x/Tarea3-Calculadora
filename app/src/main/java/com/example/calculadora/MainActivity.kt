package com.example.calculadora

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import java.util.Stack

class MainActivity : AppCompatActivity() {

    // Variables para el funcionamiento de la calculadora
    private lateinit var workingsTV: TextView
    private lateinit var resultsTV: TextView
    private var workings: String = ""  // Almacena lo que el usuario está escribiendo
    private var calculatedResult: String = "" // Resultado de la operación
    private var lastNumeric: Boolean = false // Si el último ingreso fue un número
    private var stateError: Boolean = false // Si el estado es de error
    private var lastDot: Boolean = false // Si ya se ha ingresado un punto decimal

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Referencia a los TextView en el layout
        workingsTV = findViewById(R.id.workingsTV)
        resultsTV = findViewById(R.id.resultsTV)

        // Inicializamos el TextView de resultados
        resultsTV.text = "0"
    }

    // Función para actualizar el TextView con los valores actuales
    private fun updateWorkings(value: String) {
        workings += value
        workingsTV.text = workings
    }

    // Función que maneja la acción al presionar un número
    fun numberAction(view: View) {
        if (stateError) {
            workings = ""
            stateError = false
            resultsTV.text = ""
        }
        val button = view as Button
        updateWorkings(button.text.toString())
        lastNumeric = true
        lastDot = false
    }

    // Función para manejar el punto decimal
    fun dotAction(view: View) {
        if (lastNumeric && !lastDot) {
            updateWorkings(".")
            lastNumeric = false
            lastDot = true
        }
    }

    // Función que maneja las operaciones (+, -, *, /)
    fun operationAction(view: View) {
        if (lastNumeric && !stateError) {
            val button = view as Button
            val operatorText = button.text.toString()
            updateWorkings(" $operatorText ")
            lastNumeric = false
            lastDot = false
        }
    }

    // Función para calcular el resultado cuando se presiona "="
    fun equalsAction(view: View) {
        if (lastNumeric && !stateError) {
            try {
                val result = evaluateExpression(workings)
                calculatedResult = result.toString()
                resultsTV.text = if (calculatedResult.endsWith(".0")) {
                    calculatedResult.dropLast(2) // Eliminar ".0" si no es necesario
                } else {
                    calculatedResult
                }
                workings = calculatedResult // Para continuar usando el resultado
                lastNumeric = true
            } catch (e: Exception) {
                resultsTV.text = "Error"
                stateError = true
                lastNumeric = false
            }
        }
    }

    // Función para evaluar la expresión con la jerarquía de operaciones
    private fun evaluateExpression(expression: String): Double {
        val tokens = expression.split(" ")

        val values = Stack<Double>() // Pila para los valores numéricos
        val operators = Stack<String>() // Pila para los operadores

        var index = 0
        while (index < tokens.size) {
            val token = tokens[index]

            when {
                token.isDouble() -> {
                    values.push(token.toDouble())
                }
                token == "+" || token == "-" -> {
                    // Procesar toda la pila de operadores antes de aplicar suma/resta
                    while (operators.isNotEmpty() && (operators.peek() == "+" || operators.peek() == "-" || operators.peek() == "×" || operators.peek() == "/")) {
                        values.push(applyOperator(operators.pop(), values.pop(), values.pop()))
                    }
                    operators.push(token)
                }
                token == "×" || token == "/" -> {
                    // Procesar multiplicación/división antes de aplicarlas
                    while (operators.isNotEmpty() && (operators.peek() == "×" || operators.peek() == "/")) {
                        values.push(applyOperator(operators.pop(), values.pop(), values.pop()))
                    }
                    operators.push(token)
                }
            }
            index++
        }

        // Procesar los operadores restantes
        while (operators.isNotEmpty()) {
            values.push(applyOperator(operators.pop(), values.pop(), values.pop()))
        }

        return values.pop()
    }

    // Función que aplica un operador a dos números
    private fun applyOperator(operator: String, b: Double, a: Double): Double {
        return when (operator) {
            "+" -> a + b
            "-" -> a - b
            "×" -> a * b
            "/" -> if (b != 0.0) a / b else Double.NaN
            else -> 0.0
        }
    }

    // Función para limpiar todo (AC)
    fun allClearAction(view: View) {
        workings = ""
        calculatedResult = ""
        workingsTV.text = ""
        resultsTV.text = "0"
        lastNumeric = false
        stateError = false
        lastDot = false
    }

    // Función para borrar el último carácter (⌫)
    fun backSpaceAction(view: View) {
        if (workings.isNotEmpty() && !stateError) {
            workings = workings.trimEnd().dropLast(1)
            workingsTV.text = workings
        }
    }

    // Función de extensión para verificar si una cadena es un número
    private fun String.isDouble(): Boolean {
        return this.toDoubleOrNull() != null
    }
}
