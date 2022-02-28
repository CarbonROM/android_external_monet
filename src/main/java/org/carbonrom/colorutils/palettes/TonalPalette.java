/*
 * Copyright 2021 Google LLC
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

package org.carbonrom.colorutils.palettes;

import com.android.internal.graphics.cam.Cam;
import com.android.internal.graphics.cam.Frame;

import java.util.HashMap;
import java.util.Map;

/**
 * A convenience class for retrieving colors that are constant in hue and chroma, but vary in tone.
 */
public final class TonalPalette {
  Map<Integer, Integer> cache;
  private float hue;
  private float chroma;

  public float getHue() {
    return hue;
  }

  public float getChroma() {
    return chroma;
  }

  /**
   * Create tones using the CAM hue and chroma from a color.
   *
   * @param argb ARGB representation of a color
   * @return Tones matching that color's hue and chroma.
   */
  public static final TonalPalette fromInt(int argb) {
    Cam cam = Cam.fromInt(argb);
    return TonalPalette.fromHueAndChroma(cam.getHue(), cam.getChroma());
  }

  /**
   * Create tones from a defined CAM hue and chroma.
   *
   * @param hue CAM hue
   * @param chroma CAM chroma
   * @return Tones matching hue and chroma.
   */
  public static final TonalPalette fromHueAndChroma(float hue, float chroma) {
    return new TonalPalette(hue, chroma);
  }

  private TonalPalette(float hue, float chroma) {
    cache = new HashMap<>();
    this.hue = hue;
    this.chroma = chroma;
  }

  /**
   * Create an ARGB color with HCT hue and chroma of this Tones instance, and the provided HCT tone.
   *
   * @param tone Cam tone, measured from 0 to 100.
   * @return ARGB representation of a color with that tone.
   */
  public int tone(int tone) {
    return cache.computeIfAbsent(tone, k -> Cam.getInt(this.hue, this.chroma, tone, Frame.DEFAULT));
  }
}
