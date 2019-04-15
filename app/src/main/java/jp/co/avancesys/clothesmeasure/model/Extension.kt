package jp.co.avancesys.clothesmeasure.model

import jp.co.avancesys.clothesmeasure.view.widget.PointerImageView

/**
 * 拡張プロパティ(横の長さ)
 */
val MeasureManager.MeasureLine.width: Float
    get() = Math.abs(Math.abs(toX) - Math.abs(fromX))

/**
 * 拡張プロパティ(縦の長さ)
 */
val MeasureManager.MeasureLine.height: Float
    get() = Math.abs(Math.abs(toY) - Math.abs(fromY))

/**
 * 拡張プロパティ(線の長さ)
 */
val MeasureManager.MeasureLine.length: Double
    get() = Math.sqrt(Math.pow(width.toDouble(), 2.0) + Math.pow(height.toDouble(), 2.0))

/**
 * ポインターオブジェクトの座標データを取得する拡張関数
 */
fun PointerImageView.getCoordinateData(): PointerImageView.CoordinateData {
    return if (tag is PointerImageView.CoordinateData) {
        (tag) as PointerImageView.CoordinateData
    } else {
        PointerImageView.CoordinateData(dx = left, dy = top)
    }
}
