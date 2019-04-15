package jp.co.avancesys.clothesmeasure.model

import android.content.Context
import android.support.v7.app.AlertDialog
import android.util.Log

class MeasureManager : MeasureState.ExecuteCallback {

    /**
     * 始点と終点の座標データクラス
     */
    data class MeasureLine(var fromX: Float = 0f, var fromY: Float = 0f, var toX: Float = 0f, var toY: Float = 0f)

    var cmHeight: Float = 0f
        private set

    var pxHeight: Float = 0f

    val pxPerCm: Float
        get() = (this.cmHeight / this.pxHeight)

    private val mState: MeasureState = MeasureState()
    private val mQuestionAnswer: HashMap<MeasureState.Question, MeasureLine> = HashMap()

    /**
     * 初期化
     */
    init {
        mQuestionAnswer.apply {
            put(MeasureState.Question.LENGTH, MeasureLine())
            put(MeasureState.Question.WIDTH, MeasureLine())
            put(MeasureState.Question.SHOULDER_WIDTH, MeasureLine())
            put(MeasureState.Question.SLEEVE_LENGTH, MeasureLine())
        }

        mState.setExecuteCallback(this)
    }

    override fun onHeight(value: Float) {
        cmHeight = value
    }

    override fun onEnd(context: Context) {
        val sb = StringBuilder().also { builder ->
            MeasureState.Question.values().map {
                when (it) {
                    MeasureState.Question.HEIGHT -> builder.appendln("${it.caption} = ${cmHeight.toInt()}cm")
                    MeasureState.Question.END -> {
                    }
                    else -> {
                        val length = mQuestionAnswer[it]?.let { measureLine ->
                            calcMeasure(measureLine)
                        } ?: 0
                        builder.appendln("${it.caption} = ${length}cm")
                    }
                }
            }
        }

        AlertDialog.Builder(context)
            .setTitle(MeasureState.Question.END.caption)
            .setMessage(sb.toString())
            .setPositiveButton("OK", null)
            .setCancelable(false)
            .create()
            .show()
    }

    /**
     * 次の質問ステータスに移行して処理を実行
     */
    fun nextQuestion(context: Context, lineData: MeasureLine?) {
        lineData?.let { data ->
            mQuestionAnswer.filterKeys { key -> key == mState.question }.mapValues {
                it.value.fromX = data.fromX
                it.value.fromY = data.fromY
                it.value.toX = data.toX
                it.value.toY = data.toY
            }
        }

        mState.next(context)
    }

    /**
     * 前の質問ステータスに移行して処理を実行
     */
    fun undoQuestion(context: Context) = mState.undo(context)

    /**
     * 質問ステータスに合わせて処理を実行
     */
    fun executeQuestion(context: Context) = mState.execute(context)

    /**
     * 現在の質問ステータスを照合
     */
    fun isNowQuestion(question: MeasureState.Question) : Boolean = mState.question == question

    /**
     * 採寸
     *
     * @return cm
     */
    private fun calcMeasure(data: MeasureLine) : Int {
        val length = (data.length.toFloat() * pxPerCm).toInt()
        Log.d(TAG, "calcMeasure: length= ${length}cm, px=${data.length.toFloat()}, pxPerCm=$pxPerCm")
        return length
    }

    companion object {
        private const val TAG = "MeasureManager"
    }
}