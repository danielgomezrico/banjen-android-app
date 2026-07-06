package com.makingiants.android.banjotuner

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

/**
 * Hand-rolled Material Symbols used by Banjen, copied from
 * androidx.compose.material.icons.filled.* so we can drop the
 * `material-icons-extended` dependency (~10-15 MB before R8).
 *
 * Each icon mirrors the official 24dp Material path data so the
 * visual output is pixel-identical to the upstream extended set.
 */
internal object AppIcons {
    val Remove: ImageVector by lazy {
        materialIcon("Filled.Remove") {
            materialPath {
                moveTo(19.0f, 13.0f)
                horizontalLineTo(5.0f)
                verticalLineToRelative(-2.0f)
                horizontalLineToRelative(14.0f)
                verticalLineToRelative(2.0f)
                close()
            }
        }
    }
}

/**
 * Minimal Material-style ImageVector builder (24dp viewport, single solid path).
 * Mirrors the configuration that `androidx.compose.material.icons.materialIcon`
 * applies to every Filled icon.
 */
private inline fun materialIcon(
    name: String,
    block: ImageVector.Builder.() -> ImageVector.Builder,
): ImageVector =
    ImageVector
        .Builder(
            name = name,
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f,
        ).apply { block() }
        .build()

private inline fun ImageVector.Builder.materialPath(
    fillAlpha: Float = 1f,
    strokeAlpha: Float = 1f,
    pathFillType: PathFillType = PathFillType.NonZero,
    pathBuilder: androidx.compose.ui.graphics.vector.PathBuilder.() -> Unit,
): ImageVector.Builder =
    path(
        fill = SolidColor(Color.Black),
        fillAlpha = fillAlpha,
        stroke = null,
        strokeAlpha = strokeAlpha,
        strokeLineWidth = 1f,
        strokeLineCap = StrokeCap.Butt,
        strokeLineJoin = StrokeJoin.Bevel,
        strokeLineMiter = 1f,
        pathFillType = pathFillType,
        pathBuilder = pathBuilder,
    )
