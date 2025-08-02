package com.example.tictactoe

import android.animation.AnimatorSet
import android.animation.ArgbEvaluator
import android.animation.ObjectAnimator
import android.graphics.Color
import android.media.AudioAttributes
import android.media.SoundPool
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.tictactoe.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private var board = Array(3) { arrayOfNulls<String>(3) }
    private var currentPlayer = "X"
    private var xScore = 0
    private var oScore = 0
    private var playerSymbol = "X"
    private var computerMode = false
    private var gameStarted = false

    private lateinit var soundPool: SoundPool
    private var soundMove = 0
    private var soundWin = 0
    private var soundDraw = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupSounds()
        initGame()

        binding.btnChooseX.setOnClickListener {
            playerSymbol = "X"
            currentPlayer = "X"
            gameStarted = true
            resetBoard()
            disableChooseButtons()
            Toast.makeText(this, "You chose X", Toast.LENGTH_SHORT).show()
        }

        binding.btnChooseO.setOnClickListener {
            playerSymbol = "O"
            currentPlayer = "O"
            gameStarted = true
            resetBoard()
            disableChooseButtons()
            Toast.makeText(this, "You chose O", Toast.LENGTH_SHORT).show()
        }

        binding.btnPlayAgain.setOnClickListener {
            resetBoard()
            enableChooseButtons()
            if (!gameStarted) enableChooseButtons()
            Toast.makeText(this, "New round started", Toast.LENGTH_SHORT).show()
        }

        binding.btnReset.setOnClickListener {
            resetBoard()
            xScore = 0
            oScore = 0
            updateScore()
            enableChooseButtons()
            Toast.makeText(this, "Game fully reset!", Toast.LENGTH_SHORT).show()
        }

        binding.robot.setOnClickListener {
            computerMode = !computerMode
            Toast.makeText(
                this,
                if (computerMode) "Playing vs Computer!" else "2 Player Mode",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun disableChooseButtons() {
        binding.btnChooseX.isEnabled = false
        binding.btnChooseO.isEnabled = false
        binding.btnChooseX.visibility = View.GONE
        binding.btnChooseO.visibility = View.GONE
    }

    private fun enableChooseButtons() {
        binding.btnChooseX.isEnabled = true
        binding.btnChooseO.isEnabled = true
        binding.btnChooseX.visibility = View.VISIBLE
        binding.btnChooseO.visibility = View.VISIBLE
    }

    private fun setupSounds() {
        val audioAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_GAME)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()

        soundPool = SoundPool.Builder()
            .setMaxStreams(3)
            .setAudioAttributes(audioAttributes)
            .build()

        soundMove = soundPool.load(this, R.raw.click, 1)
        soundWin = soundPool.load(this, R.raw.win, 1)
        soundDraw = soundPool.load(this, R.raw.click, 1)
    }

    private fun initGame() {
        val cells = arrayOf(
            binding.cell0, binding.cell1, binding.cell2,
            binding.cell3, binding.cell4, binding.cell5,
            binding.cell6, binding.cell7, binding.cell8
        )

        for (i in cells.indices) {
            cells[i].setOnClickListener {
                if (!gameStarted) {
                    Toast.makeText(this, "Choose X or O first", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                val row = i / 3
                val col = i % 3
                if (board[row][col] == null) {
                    makeMove(row, col, cells)
                }
            }
        }
    }

    private fun makeMove(row: Int, col: Int, cells: Array<ImageView>) {
        board[row][col] = currentPlayer
        val index = row * 3 + col
        cells[index].setImageResource(if (currentPlayer == "X") R.drawable.cross else R.drawable.zero)
        soundPool.play(soundMove, 1f, 1f, 0, 0, 1f)

        val winningCombo = getWinningCombination(currentPlayer)
        if (winningCombo != null) {
            Toast.makeText(this, "$currentPlayer Wins!", Toast.LENGTH_SHORT).show()
            incrementScore(currentPlayer)
            updateScore()
            gameStarted = false
            animateWin(winningCombo.map { cells[it] })
            soundPool.play(soundWin, 1f, 1f, 0, 0, 1f)
        } else if (isDraw()) {
            Toast.makeText(this, "It's a Draw!", Toast.LENGTH_SHORT).show()
            gameStarted = false
            soundPool.play(soundDraw, 1f, 1f, 0, 0, 1f)
        } else {
            switchPlayer()
            if (computerMode && currentPlayer != playerSymbol) {
                val (compRow, compCol) = getBestMove() ?: return
                makeMove(compRow, compCol, cells)
            }
        }
    }

    private fun resetBoard() {
        board = Array(3) { arrayOfNulls<String>(3) }

        val cells = arrayOf(
            binding.cell0, binding.cell1, binding.cell2,
            binding.cell3, binding.cell4, binding.cell5,
            binding.cell6, binding.cell7, binding.cell8
        )

        for (cell in cells) {
            cell.setImageDrawable(null)
            cell.setBackgroundResource(R.drawable.cell_background)
            cell.scaleX = 1f
            cell.scaleY = 1f
        }

        gameStarted = true
        currentPlayer = playerSymbol
    }

    private fun updateScore() {
        binding.scoreText.text = "X: $xScore   O: $oScore"
    }

    private fun incrementScore(player: String) {
        if (player == "X") xScore++ else oScore++
    }

    private fun isDraw(): Boolean = board.all { row -> row.all { it != null } }

    private fun getWinningCombination(player: String): List<Int>? {
        val winCombinations = arrayOf(
            intArrayOf(0, 1, 2), intArrayOf(3, 4, 5), intArrayOf(6, 7, 8),
            intArrayOf(0, 3, 6), intArrayOf(1, 4, 7), intArrayOf(2, 5, 8),
            intArrayOf(0, 4, 8), intArrayOf(2, 4, 6)
        )

        val flatBoard = board.flatten()
        for (combo in winCombinations) {
            if (combo.all { flatBoard[it] == player }) {
                return combo.toList()
            }
        }
        return null
    }

    private fun animateWin(cells: List<ImageView>) {
        cells.forEach { cell ->
            val colorAnim = ObjectAnimator.ofObject(
                cell, "backgroundColor", ArgbEvaluator(),
                Color.TRANSPARENT, Color.YELLOW, Color.TRANSPARENT
            ).apply {
                duration = 500
                repeatCount = 3
                repeatMode = ObjectAnimator.REVERSE
            }

            val scaleX = ObjectAnimator.ofFloat(cell, "scaleX", 1f, 1.5f, 1f).apply {
                duration = 1000
                repeatCount = 3
                repeatMode = ObjectAnimator.REVERSE
            }

            val scaleY = ObjectAnimator.ofFloat(cell, "scaleY", 1f, 1.5f, 1f).apply {
                duration = 1000
                repeatCount = 3
                repeatMode = ObjectAnimator.REVERSE
            }

            AnimatorSet().apply {
                playTogether(colorAnim, scaleX, scaleY)
                start()
            }
        }
    }

    private fun getBestMove(): Pair<Int, Int>? {
        var bestScore = Int.MIN_VALUE
        var move: Pair<Int, Int>? = null

        for (i in 0..2) {
            for (j in 0..2) {
                if (board[i][j] == null) {
                    board[i][j] = currentPlayer
                    val score = minimax(false)
                    board[i][j] = null
                    if (score > bestScore) {
                        bestScore = score
                        move = Pair(i, j)
                    }
                }
            }
        }
        return move
    }

    private fun minimax(isMaximizing: Boolean): Int {
        val winner = when {
            getWinningCombination("X") != null -> "X"
            getWinningCombination("O") != null -> "O"
            isDraw() -> "Draw"
            else -> null
        }

        if (winner != null) {
            return when (winner) {
                "X" -> if (playerSymbol == "X") -10 else 10
                "O" -> if (playerSymbol == "O") -10 else 10
                else -> 0
            }
        }

        var bestScore = if (isMaximizing) Int.MIN_VALUE else Int.MAX_VALUE
        val nextPlayer = if (isMaximizing) currentPlayer else playerSymbol

        for (i in 0..2) {
            for (j in 0..2) {
                if (board[i][j] == null) {
                    board[i][j] = if (isMaximizing) currentPlayer else playerSymbol
                    val score = minimax(!isMaximizing)
                    board[i][j] = null
                    bestScore = if (isMaximizing) maxOf(score, bestScore) else minOf(score, bestScore)
                }
            }
        }
        return bestScore
    }

    private fun switchPlayer() {
        currentPlayer = if (currentPlayer == "X") "O" else "X"
    }

    override fun onDestroy() {
        super.onDestroy()
        soundPool.release()
    }
}
