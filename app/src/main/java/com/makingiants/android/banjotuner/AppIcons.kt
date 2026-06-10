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
    val Stop: ImageVector by lazy {
        materialIcon("Filled.Stop") {
            materialPath {
                moveTo(6.0f, 6.0f)
                horizontalLineToRelative(12.0f)
                verticalLineToRelative(12.0f)
                horizontalLineTo(6.0f)
                close()
            }
        }
    }

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

    val Headphones: ImageVector by lazy {
        materialIcon("Filled.Headphones") {
            materialPath {
                moveTo(12.0f, 1.0f)
                curveTo(7.03f, 1.0f, 3.0f, 5.03f, 3.0f, 10.0f)
                verticalLineToRelative(7.0f)
                curveToRelative(0.0f, 1.66f, 1.34f, 3.0f, 3.0f, 3.0f)
                horizontalLineToRelative(3.0f)
                verticalLineToRelative(-8.0f)
                horizontalLineTo(5.0f)
                verticalLineToRelative(-2.0f)
                curveToRelative(0.0f, -3.87f, 3.13f, -7.0f, 7.0f, -7.0f)
                reflectiveCurveToRelative(7.0f, 3.13f, 7.0f, 7.0f)
                verticalLineToRelative(2.0f)
                horizontalLineToRelative(-4.0f)
                verticalLineToRelative(8.0f)
                horizontalLineToRelative(3.0f)
                curveToRelative(1.66f, 0.0f, 3.0f, -1.34f, 3.0f, -3.0f)
                verticalLineToRelative(-7.0f)
                curveToRelative(0.0f, -4.97f, -4.03f, -9.0f, -9.0f, -9.0f)
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
