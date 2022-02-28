/*
 * Copyright 2022 CarbonROM
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.carbonrom.monet

import android.app.WallpaperColors
import android.graphics.Color

import com.android.internal.graphics.cam.Cam
import com.android.internal.graphics.cam.CamUtils
import com.android.internal.graphics.ColorUtils

import org.carbonrom.monet.palettes.CorePalette
import org.carbonrom.monet.score.Score

const val GOOGLE_BLUE = 0xff4285F4.toInt()

/**
 * A bridge between Android native classes and this library specifically for use with Material You (monet).
 */
public final class Monet(color: Int) {
  val accent1Shades: List<Int>
  val accent2Shades: List<Int>
  val accent3Shades: List<Int>
  val neutral1Shades: List<Int>
  val neutral2Shades: List<Int>

  constructor(colors: WallpaperColors): this(getMainColor(colors))

  init {
    val colorArgb = if (color == Color.TRANSPARENT) GOOGLE_BLUE else color
    val palette = CorePalette.of(colorArgb)

    accent1Shades = shadesFrom(palette.a1.getHue(), palette.a1.getChroma())
    accent2Shades = shadesFrom(palette.a2.getHue(), palette.a2.getChroma())
    accent3Shades = shadesFrom(palette.a3.getHue(), palette.a3.getChroma())
    neutral1Shades = shadesFrom(palette.n1.getHue(), palette.n1.getChroma())
    neutral2Shades = shadesFrom(palette.n2.getHue(), palette.n2.getChroma())
  }

  val allAccentShades: List<Int>
    get() {
      val all = mutableListOf<Int>()
      all.addAll(accent1Shades)
      all.addAll(accent2Shades)
      all.addAll(accent3Shades)
      return all
    }

  val allNeutralShades: List<Int>
    get() {
      val all = mutableListOf<Int>()
      all.addAll(neutral1Shades)
      all.addAll(neutral2Shades)
      return all
    }

  companion object {
    private fun shadesFrom(hue: Float, chroma: Float): List<Int> {
      // Sanely cap chroma values. This "mutes" the colors saturation-wise
      // and brings behavior closer to Pixel stock
      val cappedChroma = chroma.coerceAtMost(40f)

      // Populate 13 shades in order to match AOSP's new accent colors
      val shades = mutableListOf<Int>()
      shades.add(ColorUtils.CAMToColor(hue, cappedChroma, 99f))
      shades.add(ColorUtils.CAMToColor(hue, cappedChroma, 95f))
      for (i in 2..12) {
        // Magic number 49.6 explained:
        // The middle value, or _400 in the color tones needs a special case
        // for accessibility purposes. WCAG 2.0 level AA requires a contrast
        // ratio of at least 4.5:1 for normal text. With L*=50, this requirement
        // is not met. See https://www.w3.org/TR/WCAG20-TECHS/G18.html
        // 49.6 is the closest value that allows us to maintain this contrast ratio
        val lStar = if (i == 6) 49.6f else (100 - 10 * (i - 1)).toFloat()
        shades.add(ColorUtils.CAMToColor(hue, cappedChroma, lStar))
      }
      return shades
    }

    /**
     * Grabs the most common UI-ready color from getGoodColors()
     *
     * @return ARGB color as an integer
     */
    @JvmStatic
    fun getMainColor(colors: WallpaperColors): Int {
      return getGoodColors(colors).first()
    }

    /**
     * Obtains the set of Scored colors that are distinct enough to be usable in a UI
     *
     * @return List of distinct ARGB colors, from most frequest occurance to least.
     */
    @JvmStatic
    fun getGoodColors(colors: WallpaperColors): List<Int> {
      // Add up the values of the colors into a population for scoring
      val population = colors.allColors.values.reduce({ a, b -> a + b }).toDouble()
      if (population == 0.0) {
        // Live Wallpapers end up with a population of 0.
        // In this case we can still potentially use the colors provided by mainColors.

        // Map main colors, only grabbing unique. Then, filter for colors where the
        // color (chroma) is saturated enough and luminance is bright enough to be usable.
        val usableColors = colors.mainColors.map{it.toArgb()}.distinct().filter {
            val cam = Cam.fromInt(it)
            val lstar = CamUtils.lstarFromInt(it)
            cam.chroma >= Score.CUTOFF_CHROMA && lstar >= Score.CUTOFF_TONE
        }.toList()

        // Fallback to a known good accent color
        if (usableColors.isEmpty()) {
            return listOf(GOOGLE_BLUE)
        }
        return usableColors
      }

      return Score.score(colors.allColors)
    }
  }
}
