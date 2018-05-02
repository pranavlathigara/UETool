package me.ele.uetool;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Shader;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.support.annotation.NonNull;
import android.text.SpannedString;
import android.text.style.ImageSpan;
import android.util.Pair;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import static android.view.View.NO_ID;
import static me.ele.uetool.base.Util.getDrawableBitmap;

public class Util {

  public static void enableFullscreen(@NonNull Window window) {
    if (Build.VERSION.SDK_INT >= 21) {
      addSystemUiFlag(window, 1280);
    }
  }

  private static void addSystemUiFlag(Window window, int flag) {
    View view = window.getDecorView();
    if (view != null) {
      view.setSystemUiVisibility(view.getSystemUiVisibility() | flag);
    }
  }

  public static void setStatusBarColor(@NonNull Window window, int color) {
    if (Build.VERSION.SDK_INT >= 21) {
      window.setStatusBarColor(color);
    }
  }

  public static int px2dip(float pxValue) {
    return me.ele.uetool.base.Util.px2dip(UETool.getApplication(), pxValue);
  }

  public static int dip2px(float dpValue) {
    return me.ele.uetool.base.Util.dip2px(UETool.getApplication(), dpValue);
  }

  public static int sp2px(float sp) {
    return me.ele.uetool.base.Util.sp2px(UETool.getApplication(), sp);
  }

  public static int px2sp(float pxValue) {
    return me.ele.uetool.base.Util.px2sp(UETool.getApplication(), pxValue);
  }

  public static int getScreenWidth() {
    return me.ele.uetool.base.Util.getScreenWidth(UETool.getApplication());
  }

  public static int getScreenHeight() {
    return me.ele.uetool.base.Util.getScreenHeight(UETool.getApplication());
  }

  public static String getResourceName(Resources resources, int id) {
    try {
      if (id == NO_ID || id == 0) {
        return "";
      } else {
        return resources.getResourceEntryName(id);
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
    return "";
  }

  public static String getResId(View view) {
    try {
      int id = view.getId();
      if (id == NO_ID) {
        return "";
      } else {
        return "0x" + Integer.toHexString(id);
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
    return "";
  }

  public static String intToHexColor(int color) {
    return "#" + Integer.toHexString(color).toUpperCase();
  }

  public static Object getBackground(View view) {
    Drawable drawable = view.getBackground();
    if (drawable instanceof ColorDrawable) {
      return intToHexColor(((ColorDrawable) drawable).getColor());
    } else if (drawable instanceof GradientDrawable) {
      try {
        Field mFillPaintField = GradientDrawable.class.getDeclaredField("mFillPaint");
        mFillPaintField.setAccessible(true);
        Paint mFillPaint = (Paint) mFillPaintField.get(drawable);
        Shader shader = mFillPaint.getShader();
        if (shader instanceof LinearGradient) {
          Field mColorsField = LinearGradient.class.getDeclaredField("mColors");
          mColorsField.setAccessible(true);
          int[] mColors = (int[]) mColorsField.get(shader);
          StringBuilder sb = new StringBuilder();
          for (int i = 0, N = mColors.length; i < N; i++) {
            sb.append(intToHexColor(mColors[i]));
            if (i < N - 1) {
              sb.append(" -> ");
            }
          }
          return sb.toString();
        }
      } catch (NoSuchFieldException e) {
        e.printStackTrace();
      } catch (IllegalAccessException e) {
        e.printStackTrace();
      }
    } else {
      return getDrawableBitmap(drawable);
    }
    return null;
  }

  public static List<Pair<String, Bitmap>> getTextViewBitmap(TextView textView) {
    List<Pair<String, Bitmap>> bitmaps = new ArrayList<>();
    bitmaps.addAll(getTextViewDrawableBitmap(textView));
    bitmaps.addAll(getTextViewImageSpanBitmap(textView));
    return bitmaps;
  }

  private static List<Pair<String, Bitmap>> getTextViewDrawableBitmap(TextView textView) {
    List<Pair<String, Bitmap>> bitmaps = new ArrayList<>();
    try {
      Field mDrawablesField = TextView.class.getDeclaredField("mDrawables");
      mDrawablesField.setAccessible(true);
      Field mDrawableLeftInitialFiled = Class.forName("android.widget.TextView$Drawables")
          .getDeclaredField("mDrawableLeftInitial");
      mDrawableLeftInitialFiled.setAccessible(true);
      bitmaps.add(
          new Pair<>("DrawableLeft", getDrawableBitmap((Drawable) mDrawableLeftInitialFiled.get(
              mDrawablesField.get(textView)))));
      Field mDrawableRightInitialFiled = Class.forName("android.widget.TextView$Drawables")
          .getDeclaredField("mDrawableRightInitial");
      mDrawableRightInitialFiled.setAccessible(true);
      bitmaps.add(
          new Pair<>("DrawableRight", getDrawableBitmap((Drawable) mDrawableRightInitialFiled.get(
              mDrawablesField.get(textView)))));
    } catch (Exception e) {
      e.printStackTrace();
    }
    return bitmaps;
  }

  private static List<Pair<String, Bitmap>> getTextViewImageSpanBitmap(TextView textView) {
    List<Pair<String, Bitmap>> bitmaps = new ArrayList<>();
    try {
      CharSequence text = textView.getText();
      if (text instanceof SpannedString) {
        Field mSpansField =
            Class.forName("android.text.SpannableStringInternal").getDeclaredField("mSpans");
        mSpansField.setAccessible(true);
        Object[] spans = (Object[]) mSpansField.get(text);
        for (Object span : spans) {
          if (span instanceof ImageSpan) {
            bitmaps.add(
                new Pair<>("SpanBitmap", getDrawableBitmap(((ImageSpan) span).getDrawable())));
          }
        }
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
    return bitmaps;
  }

  public static Bitmap getImageViewBitmap(ImageView imageView) {
    return getDrawableBitmap(imageView.getDrawable());
  }

  public static String getImageViewScaleType(ImageView imageView) {
    return imageView.getScaleType().name();
  }

  public static void clipText(String clipText) {
    me.ele.uetool.base.Util.clipText(UETool.getApplication(), clipText);
  }
}
