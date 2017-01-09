package com.thetorine.android.samaritan;

import android.app.Activity;
import android.content.Context;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.Surface;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.widget.ImageView;

import com.thetorine.android.samaritan.animations.AnimateAlpha;
import com.thetorine.android.samaritan.animations.AnimateLineSize;
import com.thetorine.android.samaritan.animations.AnimateScale;
import com.thetorine.android.samaritan.animations.AnimateText;
import com.thetorine.android.samaritan.utilities.DynamicTextView;
import com.thetorine.android.samaritan.utilities.Storage;
import com.thetorine.samaritan.R;

import java.util.ArrayList;
import java.util.List;

public class TextActivity extends Activity implements Runnable {
    private List<List<String>> mWords = new ArrayList<List<String>>();
    private int mWordIndex;
    private int loopIndex = -1;
    private Handler mHandler = new Handler();
    private boolean mDestroyed;
    private boolean mRun;
    private boolean mTriangleStatus;
    
    public static Storage storage = new Storage();
    private DynamicTextView textView;
    private ImageView triangleView;
    private View lineView;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        applyTheme(InputActivity.sharedPref.getBoolean("pref_theme", false), false);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_text);
        parseText(getIntent().getStringExtra("text"));
        
        this.textView = (DynamicTextView) this.findViewById(R.id.displayText);
        this.triangleView = (ImageView) this.findViewById(R.id.imageView);
        this.lineView = (View) this.findViewById(R.id.black_line);
        
        applyTheme(InputActivity.sharedPref.getBoolean("pref_theme", false), true);
        
        Typeface font = Typeface.createFromAsset(this.getAssets(), "fonts/magdacleanmono-regular.otf");
        ((DynamicTextView)findViewById(R.id.displayText)).setTypeface(font);
        
        mHandler.postDelayed(this, 100);
        storage.mWidth = 0;
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
        	getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_IMMERSIVE);
        }
    }
    
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        mDestroyed = true;
        finish();
    } 

    @Override
    public void run() {
        if(mRun) {
            if(mWordIndex < mWords.get(loopIndex).size()) {
               changeText(textView);
               animateLine();
            } else {
                mRun = false;
            }
            openCloseTriangle(triangleView, 0);
            if(showTriangle()) {
            	blinkTriangle(triangleView);
            }
        } else {
        	openCloseTriangle(triangleView, 1);
            blinkTriangle(triangleView);
            animateLine();
        }
        if(!mDestroyed) {
            mHandler.postDelayed(this, 10);
        }
    }
    
    /**
     * Runs animation according to supplied id.
     * @param animation the animation to run.
     */
    private void runAnimation(int animation) {
        switch(animation) {
            case 0: {
                String word = mWords.get(loopIndex).get(mWordIndex);

                AnimateText animateText = new AnimateText(textView, word, mWordIndex, getResources().getDisplayMetrics().widthPixels, false);
                animateText.setDuration(getDisplayTime());
                textView.startAnimation(animateText);
                mWordIndex++;
                break;
            }
            case 1: {
                AnimateAlpha animateTriangle = new AnimateAlpha(triangleView);
                animateTriangle.setDuration(getDisplayTime());
                triangleView.startAnimation(animateTriangle);
                break;
            }
            case 2: {
                AnimateScale animateScale = new AnimateScale(triangleView, getResources().getDisplayMetrics().density);
                animateScale.setDuration(200);
                triangleView.startAnimation(animateScale);
                break;
            }
            case 3: {
            	AnimateLineSize animateLine = new AnimateLineSize(lineView, storage.mWidth, false, false);
                animateLine.setDuration((long) Math.ceil(getDisplayTime() * 0.3D));
                lineView.startAnimation(animateLine);
            }
        }
    }
    
    /**
     * Changes the word of the DynamicTextView in the current view.
     * @param tv the DynamicTextView to change the word of.
     */
    private void changeText(DynamicTextView tv) {
    	 Animation a = tv.getAnimation();
         if(a != null) {
             if(a.hasEnded()) {
                 runAnimation(0);
             }
         } else {
             runAnimation(0);
         }
    }
    
    private void animateLine() {
    	if(TextActivity.storage.textChanged) {
    		runAnimation(3);
    		TextActivity.storage.textChanged = false;
    	}
    }
    
    /**
     * Checks for any animations and either opens or closes the triangle.
     * @param iv the ImageView that corresponds to the triangle on screen.
     * @param method decides on whether to open or close the triangle. 
     */
    private void openCloseTriangle(ImageView iv, int method) {
    	if(!showTriangle()) {
    		if(method == 0) {
    			if(!mTriangleStatus) {
    				runAnimation(2);
                	mTriangleStatus = true;
                	iv.setAlpha(1f);
    			}
    		} else {
    			if(mTriangleStatus) {
    				runAnimation(2);
                	mTriangleStatus = false;
                	iv.setAlpha(1f);
                	mWordIndex = 0;
    			}
    		}
    	}
    }
    
    /**
     * Blinks the triangle associated with this image.
     * @param iv the ImageView to blink.
     */
    private void blinkTriangle(ImageView iv) {
    	Animation animation = iv.getAnimation();
        if(animation != null) {
            if(animation.hasEnded()) {
                runAnimation(1);
            }
        } else {
            runAnimation(1);
        }
    }
    
    public void onClick(View view) {
        switch(view.getId()) {
            case R.id.displayText_layout: {
                if(!mRun) {
                	if(lineView.getAnimation() != null) {
                		if(lineView.getAnimation().hasEnded()) {
                			mRun = true;
                			loopIndex++;
                			if(loopIndex == mWords.size()) {
                				loopIndex = 0;
                			}
                			mWordIndex = 0;
                		} 
                	} else {
                		mRun = true;
                		loopIndex++;
            			if(loopIndex == mWords.size()) {
            				loopIndex = 0;
            			}
            			mWordIndex = 0;
                	}
                }
            }
        }
    }

    public String getRotation(Context context){
        @SuppressWarnings("deprecation")
		final int rotation = ((WindowManager) context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getOrientation();
        switch (rotation) {
            case Surface.ROTATION_0:
                return "portrait";
            case Surface.ROTATION_90:
                return "landscape";
            case Surface.ROTATION_180:
                return "portrait";
            default:
                return "landscape";
        }
    }

    /**
     * Parses the raw text received from InputActivity.
     * @param text the text to parse.
     */
    public void parseText(String text) {
        String[] loops = text.split("-");
        for(String s : loops) {
        	List<String> allWords = new ArrayList<String>();
        	for(String w : s.toUpperCase().split(" ")) {
        		w = " " + w + " ";
        		if(w.contains("?")) {
        			allWords.add(w.replace("?", ""));
        			allWords.add(" ? ");
        		} else if(w.contains("!")) {
        			allWords.add(w.replace("!", ""));
        			allWords.add(" ! ");
        		} else {
        			allWords.add(w);
        		}
        	}
        	allWords.add("   ");
    		mWords.add(allWords);
        }
    }
    
    /**
     * Reads the preference file and returns whether the triangle should disappear 
     * when Samaritan is running.
     * @return
     */
    private boolean showTriangle() {
    	return !InputActivity.sharedPref.getBoolean("pref_triangleStatus", false);
    }
    
    private int getDisplayTime() {
    	int time = 500;
    	try {
    		time = Integer.parseInt(InputActivity.sharedPref.getString("pref_textDisplayLength", "500"));
    	} catch(NumberFormatException e) {}
    	return time;
    }

    /**
     * Changes the theme of the current view according the settings activity. 
     * @param b the colour of the view that should be set.
     * @param line whether to change the individual elements on screen according to the supplied colour.
     */
    public void applyTheme(boolean b, boolean line) {
        if(!b) {
            this.setTheme(R.style.TextWhiteTheme);
            if(line) {
                lineView.setBackgroundColor(getResources().getColor(android.R.color.black));
                textView.setTextColor(getResources().getColor(android.R.color.black));
            }
        } else {
            this.setTheme(R.style.TextBlackTheme);
            if(line) {
                lineView.setBackgroundColor(getResources().getColor(android.R.color.white));
                textView.setTextColor(getResources().getColor(android.R.color.white));
            }
        }
    }
}
