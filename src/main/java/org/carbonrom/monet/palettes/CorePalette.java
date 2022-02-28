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

package org.carbonrom.monet.palettes;

import static java.lang.Math.max;

import com.android.internal.graphics.cam.Cam;

/**
 * An intermediate concept between the key color for a UI theme, and a full color scheme. 5 sets of
 * tones are generated, all except one use the same hue as the key color, and all vary in chroma.
 */
public final class CorePalette {
  public TonalPalette a1;
  public TonalPalette a2;
  public TonalPalette a3;
  public TonalPalette n1;
  public TonalPalette n2;

  /**
   * Create key tones from a color.
   *
   * @param argb ARGB representation of a color
   */
  public static CorePalette of(int argb) {
    return new CorePalette(argb);
  }

  private CorePalette(int argb) {
    Cam cam = Cam.fromInt(argb);
    float hue = cam.getHue();
    this.a1 = TonalPalette.fromHueAndChroma(hue, max(48f, cam.getChroma()));
    this.a2 = TonalPalette.fromHueAndChroma(hue, 16f);
    this.a3 = TonalPalette.fromHueAndChroma(hue + 60f, 32f);
    this.n1 = TonalPalette.fromHueAndChroma(hue, 4f);
    this.n2 = TonalPalette.fromHueAndChroma(hue, 8f);
  }
}
