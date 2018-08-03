package com.plusqa.shake_report;

import android.animation.Animator;
import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.RectF;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.NavUtils;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.graphics.Bitmap;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;


public class ScreenShotMarkUp extends AppCompatActivity {

    private ImageViewTouchWithDraw screenShotView;

    FloatingActionButton fabCurrentTool, fabTool1, fabTool2, fabShapeOption, fabTool3, fabDelete;
    FloatingActionButton colorFAB1, colorFAB2, colorFAB3;
    LinearLayout fabLayoutDraw, fabLayoutShapes, fabLayoutShapeOption, fabLayoutText, fabCurrentToolLayout;
    LinearLayout fabLayoutRed, fabLayoutGreen, fabLayoutBlack;
    View fabBGLayout;
    boolean isFABOpen=false;

    RelativeLayout relativeLayout;
    RelativeLayout.LayoutParams RLparams;

    private int drawIconID;
    private int deleteIconID;
    private int squareIconID;
    private int textIconID;
    private int circlesIconID;
    private int shapeIconID;
    private int currentIconID;

    public int currentColor;
    private int selectedColor;
    private int previousColor;
    private int fab1Color;
    private int fab2Color;
    private int fab3Color;

    private boolean isDrawSelected = true;
    private boolean isShapesSelected = false;
    private boolean isTextSelected = false;

    private boolean isShapeSaved = false;
    private boolean isDrawSaved  = false;

    boolean isShapeOptionVisible = false;

    private View.OnTouchListener handleTouch;

    private Paint mPaint;
    private static final int green = Color.parseColor("#51ccc0");
    private static final int red = Color.parseColor("#FF5252");
    private static final int black = Color.parseColor("#000000");
    private static final int blue = Color.parseColor("#519ACC");

    private InputMethodManager imm;
    private int toolIconID1;
    private int toolIconID2;
    private int toolIconID3;

    OptionMenuTouchListener shapeOptionTouchListener;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_screen_shot_mark_up);

        //Set up toolbar
        Toolbar mTopToolbar = findViewById(R.id.my_toolbar);
        setSupportActionBar(mTopToolbar);
//        getSupportActionBar().setDisplayShowTitleEnabled(false);


        //load screenshot into screenShotImageView
        screenShotView = new ImageViewTouchWithDraw(this.getApplicationContext(), null);

        screenShotView.setId(View.generateViewId());
        LinearLayout.LayoutParams lp =
                new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                                              LinearLayout.LayoutParams.MATCH_PARENT);

        screenShotView.setLayoutParams(lp);
        screenShotView.setAdjustViewBounds(true);
        screenShotView.setBackgroundColor(ContextCompat.getColor(this.getApplicationContext(), R.color.colorTransBg)
        );
        screenShotView.setScaleType(ImageViewTouchWithDraw.ScaleType.FIT_XY);
        screenShotView.setFitsSystemWindows(false);

        //adding view to layout
        LinearLayout screenshotLayout = findViewById(R.id.screenshotLayout);
        screenshotLayout.addView(screenShotView);

        Bitmap screenShotBM = Utils.getBitMap(getApplicationContext(), MainActivity.image_name);
        screenShotView.setImageBitmap(screenShotBM);

        final Paint currentPaint = new Paint();
        currentPaint.setAntiAlias(true);
        currentPaint.setDither(true);
        currentPaint.setColor(green);
        currentPaint.setStyle(Paint.Style.STROKE);
        currentPaint.setStrokeJoin(Paint.Join.ROUND);
        currentPaint.setStrokeCap(Paint.Cap.ROUND);
        currentPaint.setStrokeWidth(20);

        drawIconID = R.drawable.ic_brush_black_24dp;
        squareIconID = R.drawable.ic_sharp_crop_square_white_24px;
        shapeIconID = squareIconID;
        circlesIconID = R.drawable.ic_sharp_bubble_chart_24px;
        textIconID = R.drawable.ic_text_fields_white_24dp;
        currentIconID = drawIconID;
        deleteIconID = R.drawable.ic_delete_forever_24dp;
        toolIconID1 = circlesIconID;
        toolIconID2 = squareIconID;
        toolIconID3 = textIconID;

        //FAB tool menu layouts
        fabLayoutDraw = findViewById(R.id.fabLayout1);
        fabLayoutShapes = findViewById(R.id.fabLayout2);
        fabLayoutShapeOption = findViewById(R.id.fabShapesLayout_option);
        fabLayoutText = findViewById(R.id.fabLayout3);
        fabCurrentToolLayout = findViewById(R.id.fabCurrentToolLayout);
        //FAB color menu layouts
        fabLayoutRed = findViewById(R.id.fabColorLayout_red);
        fabLayoutGreen = findViewById(R.id.fabColorLayout_green);
        fabLayoutBlack = findViewById(R.id.fabColorLayout_black);
        //tool FABs
        fabCurrentTool = findViewById(R.id.fab);
        fabTool1 = findViewById(R.id.fab1);
        fabTool2 = findViewById(R.id.fab2);
        fabShapeOption = findViewById(R.id.fab_shapeOption);
        fabTool3 = findViewById(R.id.fab3);
        fabDelete = findViewById(R.id.fabDelete);
        //color FABs
        colorFAB1 = findViewById(R.id.fab_red);
        colorFAB2 = findViewById(R.id.fab_green);
        colorFAB3 = findViewById(R.id.fab_black);

        currentColor = green;
        selectedColor = green;
        previousColor = green;
        fab1Color = red;
        fab2Color = blue;
        fab3Color = black;

        imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

        relativeLayout = findViewById(R.id.ETLayout);

        fabBGLayout = findViewById(R.id.fabBGLayout);

        fabCurrentTool.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!isFABOpen){
                    showFABMenu();
                }else{
                    closeFABMenu();
                }
            }
        });

        fabBGLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(isFABOpen) {
                    if (!isShapeOptionVisible) {
                        closeFABMenu();
                    }
                }
            }
        });
        fabDelete.show();
        fabDelete.hide();

//        shapeOptionTouchListener = new OptionMenuTouchListener();

//        fabTool2.setOnTouchListener(shapeOptionTouchListener);
//
//        fabShapeOption.setOnTouchListener(shapeOptionTouchListener);

        fabLayoutShapeOption.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i("OPTION BUTTON","Option Clicked");
            }
        });


        //set initial color
//        selectColor(fabCurrentTool);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.screenshot_menu, menu);
        return true;
    }
// Done button
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case android.R.id.undo:
                return true;

            case android.R.id.redo:
                return true;

            case R.id.action_done:
                Intent intent = new Intent(ScreenShotMarkUp.this, FormatAndSend.class);
                startActivity(intent);
                return true;
        }


        return super.onOptionsItemSelected(item);
    }

    private boolean sendReport() {
        //save edited screenshot  --  casting with imageViewTouch exception
//            Bitmap editedBitmap =((BitmapDrawable)screenShotView.getDrawable()).getBitmap();
//            Utils.saveBitmap(this.getApplicationContext(), "EditedScreenShot", editedBitmap);

        //grab log from internal storage
        String logcatString = Utils.readLogFromInternalMemory(this.getApplicationContext()).toString();

        // Post to server
        try {
            RequestQueue requestQueue = Volley.newRequestQueue(this);
            String URL = "http://172.16.1.170:3001/v1/logs.json";
            JSONObject jsonBody = new JSONObject();
            jsonBody.put("Title", "ShakeReport_log");
            jsonBody.put("Logcat", logcatString);
            final String requestBody = jsonBody.toString();

            StringRequest stringRequest = new StringRequest(Request.Method.POST, URL, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    Log.i("VOLLEY", response);
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Log.e("VOLLEY", error.toString());
                }
            }) {
                @Override
                public String getBodyContentType() {
                    return "application/json; charset=utf-8";
                }

                @Override
                public byte[] getBody() {
                    try {
                        return requestBody == null ? null : requestBody.getBytes("utf-8");
                    } catch (UnsupportedEncodingException uee) {
                        VolleyLog.wtf("Unsupported Encoding while trying to get the bytes of %s using %s", requestBody, "utf-8");
                        return null;
                    }
                }

                @Override
                protected Response<String> parseNetworkResponse(NetworkResponse response) {
                    String responseString = "";

                    CharSequence text = "";
                    Context context = getApplicationContext();
                    int duration = Toast.LENGTH_SHORT;

                    if (null != response) {
                        responseString = String.valueOf(response.statusCode);
                        if(responseString.equals("200")) {
                            text = "Report Sent!";
                        } else {
                            text = "Report did not send: " + responseString;
                        }
                    } else {
                        text = "Report did not send";
                    }
                    Toast toast = Toast.makeText(context, text, duration);
                    toast.show();
                    return Response.success(responseString, HttpHeaderParser.parseCacheHeaders(response));
                }
            };

            requestQueue.add(stringRequest);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        CharSequence text = "Report Sent!";
        int duration = Toast.LENGTH_SHORT;
        Context context = getApplicationContext();
        Toast toast = Toast.makeText(context, text, duration);
        toast.show();

        return true;
    }

    private void showFABMenu(){
        isFABOpen=true;

        fabCurrentTool.animate().rotationBy(180 - fabCurrentTool.getRotation());
        fabLayoutDraw.animate().translationY(-getResources().getDimension(R.dimen.standard_55));
        fabLayoutShapes.animate().translationY(-getResources().getDimension(R.dimen.standard_100));
        fabLayoutText.animate().translationY(-getResources().getDimension(R.dimen.standard_145));
        fabLayoutRed.animate().translationX(-120);
        fabLayoutGreen.animate().translationX(-240);
        fabLayoutBlack.animate().translationX(-360).setListener(new Animator.AnimatorListener() {

            @Override
            public void onAnimationStart(Animator animator) {
                fabBGLayout.setVisibility(View.VISIBLE);
                fabLayoutDraw.setVisibility(View.VISIBLE);
                fabLayoutShapes.setVisibility(View.VISIBLE);
                fabLayoutText.setVisibility(View.VISIBLE);
                fabLayoutRed.setVisibility(View.VISIBLE);
                fabLayoutGreen.setVisibility(View.VISIBLE);
                fabLayoutBlack.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationEnd(Animator animator) {
                fabShapeOption.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationCancel(Animator animator) {

            }

            @Override
            public void onAnimationRepeat(Animator animator) {

            }
        });
    }

    private void closeFABMenu(){
        isFABOpen=false;

        fabLayoutRed.animate().translationX(0);
        fabLayoutGreen.animate().translationX(0);
        fabLayoutBlack.animate().translationX(0);
        fabCurrentTool.animate().rotationBy(fabCurrentTool.getRotation()*-1);
        fabLayoutDraw.animate().translationY(0);
        fabLayoutShapes.animate().translationY(0);
        fabLayoutText.animate().translationY(0).setListener(new Animator.AnimatorListener() {

            @Override
            public void onAnimationStart(Animator animator) {
                fabShapeOption.setVisibility(View.GONE);
            }

            @Override
            public void onAnimationEnd(Animator animator) {
                if(!isFABOpen){
                    fabLayoutDraw.setVisibility(View.GONE);
                    fabLayoutShapes.setVisibility(View.GONE);
                    fabLayoutText.setVisibility(View.GONE);
                    fabLayoutRed.setVisibility(View.GONE);
                    fabLayoutGreen.setVisibility(View.GONE);
                    fabLayoutBlack.setVisibility(View.GONE);
                    fabBGLayout.setVisibility(View.GONE);
                    if (!isTextSelected) {
                        deselectView(getCurrentFocus());
                    }
                }
            }

            @Override
            public void onAnimationCancel(Animator animator) { }
            @Override
            public void onAnimationRepeat(Animator animator) { }
        });

    }

    @Override
    public void onBackPressed() {
        if(isFABOpen){
            closeFABMenu();
        }else{
            super.onBackPressed();
        }
    }



    @SuppressLint("ClickableViewAccessibility")
    public void tapTool(View v) {
        if(isFABOpen){
            int iconID;
            View.OnTouchListener listener = new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    return false;
                }
            };
            fabTool1.setOnTouchListener(listener);
            fabTool2.setOnTouchListener(listener);
            fabTool3.setOnTouchListener(listener);

            if (v == fabTool1) {
                iconID = toolIconID1;
                fabTool1.setImageResource(currentIconID);
                toolIconID1 = currentIconID;
            } else if (v == fabTool2) {
                iconID = toolIconID2;
                fabTool2.setImageResource(currentIconID);
                toolIconID2 = currentIconID;
            } else {
                iconID = toolIconID3;
                fabTool3.setImageResource(currentIconID);
                toolIconID3 = currentIconID;
            }

            currentIconID = iconID;

            isDrawSaved = false;
            isShapeSaved = false;
            isDrawSelected = currentIconID == drawIconID;
            isTextSelected = currentIconID == textIconID;

            isShapesSelected = (currentIconID == squareIconID) || (currentIconID == circlesIconID);
            if (isShapesSelected)
                shapeIconID = (currentIconID == squareIconID) ? squareIconID : circlesIconID;


            closeFABMenu();
            fabCurrentTool.setImageResource(currentIconID);
        }
    }



    public void tapColor1 (View view) {
        //set color of FABs
        previousColor = currentColor;
        selectedColor = fab1Color;

        selectColor(view);
        fab1Color = previousColor;
        //set color of actual tool
        screenShotView.setSelectedPaint(selectedColor);
    }

    public void tapColor2 (View view) {
        //set color of FABs
        previousColor = currentColor;
        selectedColor = fab2Color;

        selectColor(view);
        fab2Color = previousColor;
        //set color of actual tool
        screenShotView.setSelectedPaint(selectedColor);
    }

    public void tapColor3 (View view) {
        //set color of FABs
        previousColor = currentColor;
        selectedColor = fab3Color;

        selectColor(view);
        fab3Color = previousColor;
        //set color of actual tool
        screenShotView.setSelectedPaint(selectedColor);
    }

    public void selectColor(final View view) {
        //implement later -- use four colors - don't display active color in color options

        ObjectAnimator animator = ObjectAnimator.ofInt(fabCurrentTool,
                                                      "backgroundTint",
                                                       currentColor,
                                                       selectedColor);
        animator.setDuration(2000L);
        animator.setEvaluator(new ArgbEvaluator());
        animator.setInterpolator(new DecelerateInterpolator(2));
        animator.addUpdateListener(new ObjectAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                int[][] states = new int[][] {
                        new int[] { android.R.attr.state_enabled}, // enabled
                        new int[] {-android.R.attr.state_enabled}, // disabled
                        new int[] {-android.R.attr.state_checked}, // unchecked
                        new int[] { android.R.attr.state_pressed}  // pressed
                };

                int[] colors = new int[] {
                        red,
                        green,
                        black,
                        blue
                };

                ColorStateList myList = new ColorStateList(states, colors);
                int animatedValue = (int) animation.getAnimatedValue();
                fabCurrentTool.setBackgroundTintList(ColorStateList.valueOf(animatedValue));
                fabTool1.setBackgroundTintList(ColorStateList.valueOf(animatedValue));
                fabTool2.setBackgroundTintList(ColorStateList.valueOf(animatedValue));
                fabShapeOption.setBackgroundTintList(ColorStateList.valueOf(animatedValue));
                fabTool3.setBackgroundTintList(ColorStateList.valueOf(animatedValue));
                if (selectedColor == fab1Color)
                    colorFAB1.setBackgroundTintList(ColorStateList.valueOf(previousColor));
                else if (selectedColor == fab2Color) {
                    colorFAB2.setBackgroundTintList(ColorStateList.valueOf(previousColor));
                } else if (selectedColor == fab3Color) {
                    colorFAB3.setBackgroundTintList(ColorStateList.valueOf(previousColor));
                }
            }
        });
        animator.start();

        currentColor = selectedColor;
    }

    public void deselectView(View v) {
        if (imm != null) {
            imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
        }
        v.clearFocus();
    }

    //Spins fab when switching between text and tools
    public void spinSwitch(final boolean hasFocus) {

        float rotation;
        if (hasFocus) //keyboard is opening, spin
            rotation = 360 - fabCurrentTool.getRotation() ;
        else //keyboard closing, un-rotate
            rotation = -fabCurrentTool.getRotation();

        fabCurrentTool.animate().rotationBy(rotation).setListener(new Animator.AnimatorListener() {
            @Override // timing button animation with keyboard movement
            public void onAnimationStart(Animator animation) {
                if (hasFocus) {
                    if (isTextSelected)
                        fabCurrentTool.setImageResource(textIconID);
                } else {
                    if (isDrawSaved)
                        fabCurrentTool.setImageResource(drawIconID);

                    if (isShapeSaved)
                        fabCurrentTool.setImageResource(shapeIconID);
                }
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                if (!hasFocus) {
                    if (isDrawSaved || isShapeSaved) {
                        deselectView(getCurrentFocus());
                        isDrawSaved = false;
                        isShapeSaved = false;
                    }
                }
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        if (!hasFocus) {
            if (isDrawSaved) {
                isShapesSelected = false;
                isTextSelected = false;
                isDrawSelected = true;
            }
            if (isShapeSaved) {
                isDrawSelected = false;
                isTextSelected = false;
                isShapesSelected = true;
            }
        }
    }


    public class OptionMenuTouchListener implements View.OnTouchListener {
        boolean doNothing = false;
        @SuppressLint("ClickableViewAccessibility")
        @Override
        public boolean onTouch(final View v, MotionEvent event) {

            final int action = event.getAction();
            int x = (int) event.getRawX();
            int y = (int) event.getRawY();
            if (isFABOpen) {
                switch (action) {

                    case MotionEvent.ACTION_DOWN:
                        isShapeOptionVisible = true;
                        fabLayoutShapeOption.animate().translationX(-130).setListener(new Animator.AnimatorListener() {
                            @Override
                            public void onAnimationStart(Animator a) {
                                fabLayoutShapeOption.setVisibility(View.VISIBLE);
                            }

                            @Override
                            public void onAnimationEnd(Animator a) {
                                if (isShapeOptionVisible) {
                                    fabLayoutShapeOption.setVisibility(View.VISIBLE);
                                } else {
                                    fabLayoutShapeOption.setVisibility(View.GONE);
                                    if (!doNothing) {
                                        tapTool(v);
                                    }

                                }
                            }

                            @Override
                            public void onAnimationCancel(Animator a) {
                            }

                            @Override
                            public void onAnimationRepeat(Animator a) {
                            }
                        });
                        break;

                    case MotionEvent.ACTION_UP:
                        isShapeOptionVisible = false;
                        fabLayoutShapeOption.animate().translationX(0);
                        boolean touchingShapeTool = Utils.isViewInBounds(v, x, y);
                        boolean touchingOption = Utils.isViewInBounds(fabLayoutShapeOption, x, y);

                        if (!touchingShapeTool && touchingOption) {

                            fabShapeOption.setImageResource(shapeIconID);
                            shapeIconID = (shapeIconID == squareIconID) ? circlesIconID : squareIconID;
                            if (v == fabTool1) {
                                fabTool1.setImageResource(shapeIconID);
                                toolIconID1 = shapeIconID;
                            } else if (v == fabTool2) {
                                fabTool2.setImageResource(shapeIconID);
                                toolIconID2 = shapeIconID;
                            } else {
                                fabTool3.setImageResource(shapeIconID);
                                toolIconID3 = shapeIconID;
                            }
                        }
                        doNothing = !touchingShapeTool;
                        break;
                }
            }
            return true;
        }
    }



    public class ImageViewTouchWithDraw extends android.support.v7.widget.AppCompatImageView {

        private Bitmap  mBitmap;
        private Canvas mCanvas;
        private Path mPath;
        private int firstPointerIndex = -1;
        private int firstPointerID = -1;
        private boolean isNewLine = false;

        private Paint selectedPaint;

        public class DrawAction {
            Path path;
            Paint paint;
            RectF rectF;
            Path.Direction dir = Path.Direction.CW;
            ArrayList<Point> points = new ArrayList<>();
            private final boolean isRect, isOval, isText, isLine;
            static final int IS_RECT = 1;
            static final int IS_OVAL = 2;
            static final int IS_TEXT = 4;
            static final int IS_LINE = 8;

            DrawAction(Path path, Paint paint, RectF rectF, int flag){
                this.path = path;
                this.paint = paint;
                this.rectF = rectF;

                this.isOval = (flag & IS_OVAL) == IS_OVAL;
                this.isText = (flag & IS_TEXT) == IS_TEXT;
                this.isLine = (flag & IS_LINE) == IS_LINE;
                this.isRect = (flag & IS_RECT) == IS_RECT;

                addShapeToPath();
                Point firstPoint = new Point((int) rectF.centerX(), (int) rectF.centerY());
                points.add(firstPoint);
                mPath.reset();
                path.moveTo(rectF.centerX(), rectF.centerY());
            }

            void offsetDrawing(float offsetX, float offsetY) {
                if (isRect || isOval) {
                    rectF.offsetTo(rectF.left + offsetX,
                            rectF.top + offsetY);
                    path.reset();
                    addShapeToPath();
                }
                if (isLine) {
                    path.offset(offsetX, offsetY);
                }
            }

            void addShapeToPath() {
                if (this.isRect) path.addRect(rectF, dir);
                if (this.isOval) path.addOval(rectF, dir);
            }

            void drawTo(float x, float y, float prevX, float prevY) {
                path.quadTo(prevX, prevY, (x + prevX) / 2, (y + prevY) / 2);
            }

            boolean contains(float x, float y) {

                return (this.isRect && this.rectF.contains(x, y)) ||
                        (this.isOval && pointInOval(x,y) ||
                                (this.isLine && pointInLine(x,y)));
            }

            boolean pointInOval(float x, float y) {
                float dx = x - rectF.centerX();
                float dy = y - rectF.centerY();
                float width = rectF.width()/2;
                float height = rectF.height()/2;
                return (dx * dx) / (width * width) + (dy * dy) / (height * height) <= 1;
            }

            /* Adapted to Java from an answer provided by M Katz on Stack Overflow
            Question: https://stackoverflow.com/questions/217578
            Answer by M Katz: https://stackoverflow.com/a/16391873
            M Katz User profile: https://stackoverflow.com/users/384670
            Original code adapted from http://www.ecse.rpi.edu/Homepages/wrf/Research/Short_Notes/pnpoly.html
             */



            boolean pointInLine(float x, float y) {
                boolean inside = false;

                for (Point p : points ) {
                    if ( (p.x >= (x - 30)) && (p.x <= (x + 30)) &&
                            (p.y >= (y - 30)) && (p.y <= (y + 30)) ) {
                        inside = true;
                    }
                }
                return inside;
            }

            boolean isRect() {return this.isRect;}
            boolean isOval() {return this.isOval;}
            boolean isLine() {return this.isLine;}
            boolean isText() {return this.isText;}

        }
        ArrayList<DrawAction> actionsList = new ArrayList<>();
        private DrawAction touchedDrawing;

        public void setSelectedPaint(int color) {
            this.selectedPaint = new Paint();
            selectedPaint.setAntiAlias(true);
            selectedPaint.setDither(true);
            selectedPaint.setColor(color);
            selectedPaint.setStyle(Paint.Style.STROKE);
            selectedPaint.setStrokeJoin(Paint.Join.ROUND);
            selectedPaint.setStrokeCap(Paint.Cap.ROUND);
            selectedPaint.setStrokeWidth(12);
        }

        public ImageViewTouchWithDraw(Context c, AttributeSet attrs) {
            super(c, attrs);
            mPath = new Path();
            setSelectedPaint(green);
            mScaleGestureDetector = new ScaleGestureDetector(getApplicationContext(), new ScaleListener());

        }

        @Override
        protected void onSizeChanged(int w, int h, int oldw, int oldh) {
            super.onSizeChanged(w, h, oldw, oldh);
            if (mBitmap == null) {
                mBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
            }
            else {
                mBitmap = Bitmap.createScaledBitmap(mBitmap, w, h,true);
            }
            mCanvas = new Canvas(mBitmap);
        }

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            canvas.drawBitmap(mBitmap, 0, 0, null);

//            canvas.drawPath(mPath, selectedPaint);

            for (DrawAction drawAction : actionsList) {
                canvas.drawPath(drawAction.path, drawAction.paint);
            }

            canvas.save();
        }

        private float mX, mY;
        private static final float TOUCH_TOLERANCE = 4;
        private EditText ET;
        private boolean dontMove = false;
        private ScaleGestureDetector mScaleGestureDetector;

        @Override
        public boolean performClick() {
            return super.performClick();
        }
        @Override
        public boolean onTouchEvent(MotionEvent event) {

            final float x = event.getX();
            final float y = event.getY();
            int index = event.getActionIndex();
            int currentPointerId = event.getPointerId(index);
            final View currentView = getCurrentFocus();
            final int action = event.getAction();
            mScaleGestureDetector.onTouchEvent(event);

            switch (action & MotionEvent.ACTION_MASK) {

                case MotionEvent.ACTION_DOWN:
                    //accessibility requirement
                    performClick();

                    if (isShapesSelected || isDrawSelected) {
                        fabCurrentTool.hide();

                        //old stuff -- subject to removal
                        if (isDrawSelected) {
                            if (dontMove) {
                                mPath.reset();
                                mPath.moveTo(x, y);
                                mX = x;
                                mY = y;
                                dontMove = false;
                                deselectView(currentView);
                                invalidate();
                                break;
                            }
//                        fabCurrentTool.hide();

//                        mPath.reset();
//                        mPath.moveTo(x, y);
//                        mX = x;
//                        mY = y;
                        }


                        mX = x;
                        mY = y;

                        // check if we've touched inside some drawing
                        touchedDrawing = getTouchedDrawing(x, y);
                        if (!isNewLine) fabDelete.show();

                    } else if (isTextSelected) {
                        // If in an ET already, clear focus. Else, make a new one.
                        if (currentView instanceof EditText) {
                            currentView.clearFocus();

                        } else {
                            // make an EditText and put it in a layout
                            ET = new EditText(getApplicationContext());
                            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
                                    RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
                            params.leftMargin = (int) x;
                            params.topMargin = (int) y - 60;
                            relativeLayout.addView(ET, params);

                            // set up the EditText
                            ET.requestFocus();
                            ET.setVisibility(VISIBLE);
                            ET.setTextColor(selectedPaint.getColor());
                            ET.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE);
                            ET.setBackgroundColor(getResources().getColor(android.R.color.transparent));
                            ET.setText(" ");
                            ET.setTag(x + " " + y);

                            ET.setOnFocusChangeListener(new OnFocusChangeListener() {
                                @Override
                                public void onFocusChange(View v, boolean hasFocus) {
                                    if (hasFocus) { // if opening keyboard
                                        if (!isTextSelected) {
                                            if (isDrawSelected) {
                                                isDrawSelected = false;
                                                isDrawSaved = true;
                                            }
                                            if (isShapesSelected) {
                                                isShapesSelected = false;
                                                isShapeSaved = true;
                                            }
                                            isTextSelected = true;

                                            spinSwitch(true);
                                        }

                                    } else { // if closing keyboard
                                        // Return to previous tool
                                        if (isDrawSaved || isShapeSaved) {
                                            //prevents drawing when deselecting text
                                            dontMove = true;
                                            //animate button and switch icons
                                            spinSwitch(false);

                                        } else {
                                            // Previous tool was text
                                            if (isTextSelected)
                                                deselectView(v);
                                        }

                                    }
                                }
                            });

                            // force open the keyboard
                            if (imm != null) {
                                imm.showSoftInput(ET, InputMethodManager.SHOW_IMPLICIT);
                            }
                        }

                    }

                    invalidate();
                    firstPointerIndex = event.getActionIndex();
                    firstPointerID = event.getPointerId(firstPointerIndex);
                    break;

                case MotionEvent.ACTION_MOVE:

//                    if (isDrawSelected && isNewLine) {
//                        if (dontMove) {
//                            break;
//                        }
//
//                        if (currentPointerId == firstPointerID) {
//                            float dx = Math.abs(x - mX);
//                            float dy = Math.abs(y - mY);
//
//                            if (dx >= TOUCH_TOLERANCE || dy >= TOUCH_TOLERANCE) {
//                                mPath.quadTo(mX, mY, (x + mX) / 2, (y + mY) / 2);
//                                mX = x;
//                                mY = y;
//                            }
//                        }
//                    }
                    if (!isTextSelected && (currentPointerId == firstPointerID)) {

                        Point p = new Point((int) x, (int) y);

                        // if we are drawing a line, rather than moving an existing drawing
                        if (touchedDrawing.isLine() && isNewLine) {
                            if (dontMove) {
                                break;
                            }

                            float dx = Math.abs(x - mX);
                            float dy = Math.abs(y - mY);

                            if (dx >= TOUCH_TOLERANCE || dy >= TOUCH_TOLERANCE) {
                                touchedDrawing.points.add(p);
                                touchedDrawing.drawTo(x, y, mX, mY);
                                mX = x;
                                mY = y;
                            }
                        } else { // if it's an existing drawing
                            if (touchedDrawing != null) {
//                                touchedDrawing.points.add(p);
                                touchedDrawing.offsetDrawing(x - mX,y - mY);

                                mX = x;
                                mY = y;
                            }
                        }
                    }


                    invalidate();
                    break;

                case MotionEvent.ACTION_UP:
                    fabCurrentTool.show();
                    fabDelete.hide();
                    touchedDrawing = null;

                    invalidate();
                    break;

                case MotionEvent.ACTION_POINTER_UP:

                    if (isDrawSelected) {
                        if (dontMove) {
                            break;
                        }

                        fabCurrentTool.show();
                        fabDelete.hide();
                        firstPointerID = -1;
                    } else if (isShapesSelected) {

                    } else if (isTextSelected) {

                    }
                    invalidate();
                    break;

            }

            return true;
        }

        private DrawAction getTouchedDrawing(final float xTouch, final float yTouch) {

            DrawAction touched = null;

            for (DrawAction drawAction : actionsList) {
                if (drawAction.contains(xTouch,yTouch)) {
                    touched = drawAction;
                    isNewLine = false;
                }
            }

            if ( (null == touched) ) {

                RectF rectF = new RectF(xTouch - 200,
                                         yTouch - 200,
                                        xTouch + 200,
                                      yTouch + 200);

                Path path = new Path();

                int shapeFlag = 0;

                if (isDrawSelected) {
                    shapeFlag = DrawAction.IS_LINE;
                    isNewLine = true;
                } else if (isShapesSelected) {
                    shapeFlag = (shapeIconID == squareIconID) ? DrawAction.IS_RECT : DrawAction.IS_OVAL;
                } else if (isTextSelected) {
                    shapeFlag = DrawAction.IS_TEXT;
                }

                touched = new DrawAction(path, selectedPaint, rectF, shapeFlag);

                if (actionsList.size() == 50) {
                    // remove first drawing
                    actionsList.remove(0);
                }

                actionsList.add(touched);
            }

            return touched;
        }

        private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {

            private float lastSpanX;
            private float lastSpanY;
            private DrawAction lastTouchedDrawing;
            float currentSpanX;
            float currentSpanY;
            final float shapeMinSize = 100;
            float shapeMaxHeight;
            float shapeMaxWidth;
            float canvasHeight;
            float canvasWidth;


            @Override
            public boolean onScaleBegin(ScaleGestureDetector detector) {

                if (touchedDrawing != null) {
                    lastSpanX = detector.getCurrentSpanX();
                    lastSpanY = detector.getCurrentSpanY();
                    lastTouchedDrawing = touchedDrawing;
                }

                return true;
            }

            @Override
            public boolean onScale(ScaleGestureDetector detector){

                if (isShapesSelected) {

                    if (touchedDrawing != null) {
                        currentSpanX = detector.getCurrentSpanX();
                        currentSpanY = detector.getCurrentSpanY();


                        float spanXDiff = currentSpanX - lastSpanX;
                        float spanYDiff = currentSpanY - lastSpanY;

                        boolean scalingUpX = spanXDiff > 0;
                        boolean scalingUpY = spanYDiff > 0;
                        canvasHeight = mCanvas.getHeight();
                        canvasWidth = mCanvas.getWidth();

                        shapeMaxWidth = canvasWidth;
                        shapeMaxHeight = canvasHeight;

                        RectF lastRectF = lastTouchedDrawing.rectF;
                        RectF currentRectF = touchedDrawing.rectF;

                        if ((!scalingUpX && (lastRectF.width() >= shapeMinSize)) ||
                                (scalingUpX && (lastRectF.width() <= shapeMaxWidth))) {

                            if ((currentRectF.width() + spanXDiff/2) > shapeMaxWidth) {
                                currentRectF.left = currentRectF.centerX() - shapeMaxWidth/2;
                                currentRectF.right = currentRectF.centerX() + shapeMaxWidth/2;

                            } else if ((currentRectF.width() + spanXDiff/2) < shapeMinSize) {
                                currentRectF.left = currentRectF.centerX() - shapeMinSize/2;
                                currentRectF.right = currentRectF.centerX() + shapeMinSize/2;
                            } else {
                                currentRectF.right += (spanXDiff/2);
                                currentRectF.left -= (spanXDiff/2);
                            }


                        }

                        if ((!scalingUpY && (lastRectF.height() >= shapeMinSize)) ||
                                (scalingUpY && (lastRectF.height() <= shapeMaxHeight))) {

                            if ((currentRectF.height() + spanYDiff/2) > shapeMaxHeight) {
                                currentRectF.top = currentRectF.centerY() - shapeMaxHeight/2;
                                currentRectF.bottom = currentRectF.centerY() + shapeMaxHeight/2;

                            } else if ((currentRectF.height() + spanYDiff/2) < shapeMinSize) {
                                currentRectF.top = currentRectF.centerY() - shapeMinSize/2;
                                currentRectF.bottom = currentRectF.centerY() + shapeMinSize/2;
                            } else {
                                currentRectF.top -= (spanYDiff/2);
                                currentRectF.bottom += (spanYDiff/2);
                            }


                        }
                        lastSpanY = currentSpanY;
                        lastSpanX = currentSpanX;
                        touchedDrawing.path.addRect(currentRectF, Path.Direction.CW);
                        lastTouchedDrawing = touchedDrawing;
                    }
                }
                return true;
            }
        }
    }
}




