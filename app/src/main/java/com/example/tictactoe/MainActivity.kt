package com.example.tictactoe

import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private lateinit var board: Array<ImageView>
    private lateinit var cellStatus: Array<String>
    private var currentPlayer = "X"
    private var xScore = 0
    private var oScore = 0
    private var playerSymbol = ""
    private var opponentSymbol = ""
    private var gameStarted = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialize board
        board = arrayOf(
            findViewById(R.id.cell0), findViewById(R.id.cell1), findViewById(R.id.cell2),
            findViewById(R.id.cell3), findViewById(R.id.cell4), findViewById(R.id.cell5),
            findViewById(R.id.cell6), findViewById(R.id.cell7), findViewById(R.id.cell8)
        )

        cellStatus = Array(9) { "" }

        val btnRestart = findViewById<View>(R.id.btnRestart)
        val btnChooseX = findViewById<ImageView>(R.id.btnChooseX)
        val btnChooseO = findViewById<ImageView>(R.id.btnChooseO)

        btnRestart.setOnClickListener {
            resetBoard()
        }

        btnChooseX.setOnClickListener {
            startGame("X")
        }

        btnChooseO.setOnClickListener {
            startGame("O")
        }

        board.forEachIndexed { index, imageView ->
            imageView.setOnClickListener {
                if (!gameStarted) {
                    Toast.makeText(this, "Please choose X or O to start.", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                if (cellStatus[index].isEmpty()) {
                    cellStatus[index] = currentPlayer
                    imageView.setImageResource(
                        if (currentPlayer == "X") R.drawable.cross else R.drawable.zero
                    )

                    if (checkWinner()) {
                        Toast.makeText(this, "$currentPlayer wins!", Toast.LENGTH_SHORT).show()
                        updateScore()
                        disableBoard()
                    } else if (cellStatus.all { it.isNotEmpty() }) {
                        Toast.makeText(this, "It's a draw!", Toast.LENGTH_SHORT).show()
                    } else {
                        switchTurn()
                    }
                }
            }
        }
    }

    private fun startGame(symbol: String) {
        playerSymbol = symbol
        opponentSymbol = if (symbol == "X") "O" else "X"
        currentPlayer = playerSymbol
        gameStarted = true

        findViewById<ImageView>(R.id.btnChooseX).visibility = View.GONE
        findViewById<ImageView>(R.id.btnChooseO).visibility = View.GONE

        resetBoard()
    }

    private fun resetBoard() {
        cellStatus.fill("")
        board.forEach {
            it.setImageDrawable(null)
            it.isEnabled = true
        }

        if (gameStarted) {
            currentPlayer = playerSymbol
        } else {
            findViewById<ImageView>(R.id.btnChooseX).visibility = View.VISIBLE
            findViewById<ImageView>(R.id.btnChooseO).visibility = View.VISIBLE
        }
    }

    private fun switchTurn() {
        currentPlayer = if (currentPlayer == "X") "O" else "X"
    }

    private fun updateScore() {
        val scoreText = findViewById<TextView>(R.id.scoreText)
        if (currentPlayer == "X") xScore++ else oScore++
        scoreText.text = "X: $xScore   O: $oScore"
    }

    private fun disableBoard() {
        board.forEach { it.isEnabled = false }
    }

    private fun checkWinner(): Boolean {
        val winCombinations = arrayOf(
            intArrayOf(0, 1, 2), intArrayOf(3, 4, 5), intArrayOf(6, 7, 8),
            intArrayOf(0, 3, 6), intArrayOf(1, 4, 7), intArrayOf(2, 5, 8),
            intArrayOf(0, 4, 8), intArrayOf(2, 4, 6)
        )

        for (combo in winCombinations) {
            val (a, b, c) = combo
            if (cellStatus[a] == currentPlayer &&
                cellStatus[b] == currentPlayer &&
                cellStatus[c] == currentPlayer
            ) {
                return true
            }
        }
        return false
    }
}
