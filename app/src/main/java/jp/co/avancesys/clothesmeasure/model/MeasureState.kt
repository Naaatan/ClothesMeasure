package jp.co.avancesys.clothesmeasure.model

import android.app.AlertDialog
import android.content.Context
import android.text.InputType
import android.util.Log
import android.view.Gravity
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView

class MeasureState {

    private var mCallback: MeasureState.ExecuteCallback? = null
    var question: MeasureState.Question = MeasureState.Question.HEIGHT
        private set

    /**
     * 現在の質問の実行処理
     */
    fun execute(context: Context) = question.execute(this, context)

    /**
     * 次の質問へ移行
     */
    fun next(context: Context) {
        question = question.nextQuestion()
        question.execute(this, context)
    }

    /**
     * 前の質問へ移行
     */
    fun undo(context: Context) {
        question = question.undoQuestion()
        question.execute(this, context)
    }

    /**
     * コールバックの設定
     */
    fun setExecuteCallback(callback: MeasureState.ExecuteCallback) { mCallback = callback }

    /**
     * 身長入力のダイアログを表示
     */
    private fun showDialogHeight(context: Context) {
        val density = context.resources.displayMetrics.density
        val dp8 = (density * 8).toInt()

        val editText = EditText(context).apply {
            inputType = InputType.TYPE_CLASS_NUMBER
        }
        val textView = TextView(context).apply {
            text = "cm"
            gravity = Gravity.BOTTOM
        }

        val linearLayout = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL

            addView(editText, LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 1.0f).also {
                it.marginStart = dp8 * 2
            })
            addView(textView, LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.MATCH_PARENT).also {
                it.marginEnd = dp8 * 2
                it.bottomMargin = dp8
            })
        }

        AlertDialog.Builder(context)
            .setTitle("${MeasureState.Question.HEIGHT.caption}を入力して下さい")
            .setView(linearLayout)
            .setPositiveButton("OK") { _, _ ->
                try {
                    val height = editText.text.toString().toFloat()
                    mCallback?.onHeight(height)
                } catch (e: NumberFormatException) {
                    Log.w(TAG, "showDialogHeight: ", e)
                    mCallback?.onHeight(0f)
                }
            }
            .setCancelable(false)
            .create()
            .show()
    }

    /**
     * 説明ダイアログ表示
     */
    private fun showDialogCaption(context: Context, question: MeasureState.Question) {
        AlertDialog.Builder(context)
            .setMessage("${question.caption}の長さをポインターで設定して下さい")
            .setPositiveButton("OK", null)
            .setCancelable(true)
            .create()
            .show()
    }

    /**
     * 終了を通知
     */
    private fun executeEnd(context: Context) = mCallback?.onEnd(context)

    companion object {
        private const val TAG = "MeasureState"
    }

    /**
     * コールバック
     */
    interface ExecuteCallback {
        fun onHeight(value: Float)
        fun onEnd(context: Context)
    }

    /**
     * 質問のシーケンス
     */
    enum class Question(val caption: String) {

        HEIGHT("身長") {
            override fun execute(state: MeasureState, context: Context) = state.showDialogHeight(context)

            override fun undoQuestion(): Question = HEIGHT

            override fun nextQuestion(): Question = LENGTH
        },
        LENGTH("着丈") {
            override fun execute(state: MeasureState, context: Context) = state.showDialogCaption(context, this)

            override fun undoQuestion(): Question = HEIGHT

            override fun nextQuestion(): Question = WIDTH
        },
        WIDTH("身幅") {
            override fun execute(state: MeasureState, context: Context) = state.showDialogCaption(context, this)

            override fun undoQuestion(): Question = LENGTH

            override fun nextQuestion(): Question = SHOULDER_WIDTH
        },
        SHOULDER_WIDTH("肩幅") {
            override fun execute(state: MeasureState, context: Context) = state.showDialogCaption(context, this)

            override fun undoQuestion(): Question = WIDTH

            override fun nextQuestion(): Question = SLEEVE_LENGTH
        },
        SLEEVE_LENGTH("袖丈") {
            override fun execute(state: MeasureState, context: Context) = state.showDialogCaption(context, this)

            override fun undoQuestion(): Question = SHOULDER_WIDTH

            override fun nextQuestion(): Question = END
        },
        END("採寸終了") {
            override fun execute(state: MeasureState, context: Context) { state.executeEnd(context) }

            override fun undoQuestion(): Question = SLEEVE_LENGTH

            override fun nextQuestion(): Question = END
        };

        abstract fun undoQuestion(): Question
        abstract fun nextQuestion(): Question
        abstract fun execute(state: MeasureState, context: Context)
    }

}