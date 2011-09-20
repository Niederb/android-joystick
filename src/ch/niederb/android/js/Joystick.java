package ch.niederb.android.js;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

public class Joystick extends View implements Runnable {
    private OnJoystickChangeListener listener;
    private long repeatInterval;
    private Thread t;
    
	private int x=0;
    private int y=0;
    private int padding = 10;
    private int circleColor;
    private int buttonColor;
    private int buttonRadius = 30;
    private int joystickRadius = 100;
    private double centerX = (getWidth())/2;
    private double centerY = (getHeight())/2;
    
    public Joystick(Context context) {
        super(context);
    }

    public Joystick(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray a = context.obtainStyledAttributes(attrs,
                R.styleable.Joystick);
        circleColor = a.getColor(R.styleable.Joystick_circleColor, Color.RED);
        buttonColor = a.getColor(R.styleable.Joystick_buttonColor, Color.RED);
        buttonRadius = a.getInteger(R.styleable.Joystick_buttonRadius, 30);
        a.recycle();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(measureWidth(widthMeasureSpec),
                measureHeight(heightMeasureSpec));
    }

    private int measureWidth(int measureSpec) {
    	return 2*padding + buttonRadius + 2*joystickRadius;
    }

    private int measureHeight(int measureSpec) {
    	return 2*padding + buttonRadius + 2*joystickRadius;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        //super.onDraw(canvas);
        centerX = (getWidth())/2;
        centerY = (getHeight())/2;
        
        Paint p = new Paint();
        p.setStyle(Paint.Style.STROKE);        
        p.setColor(circleColor);
        canvas.drawCircle((int) centerX, (int) centerY, joystickRadius, p);
        //dotted stroke
        canvas.drawCircle((int) centerX, (int) centerY, joystickRadius/2, p);

        p.setColor(buttonColor);
        p.setStyle(Paint.Style.FILL);
        canvas.drawCircle(x, y, buttonRadius, p);
        canvas.drawCircle(x, y, buttonRadius/2, p);
    }
    
    @Override
    public boolean onTouchEvent(MotionEvent event) {
    	x = (int) event.getX();
    	y = (int) event.getY();
        double abs = Math.sqrt((x-centerX)*(x-centerX) + (y-centerY)*(y-centerY));
        if (abs > joystickRadius) {
        	x = (int) ((x-centerX)*joystickRadius/abs + centerX);
        	y = (int) ((y-centerY)*joystickRadius/abs + centerY);
        }
    	invalidate();
    	if (event.getAction() == MotionEvent.ACTION_UP) {
    		x = (int) centerX;
    		y = (int) centerY;
    		t.interrupt();
    	}
    	if (listener != null && event.getAction() == MotionEvent.ACTION_DOWN) {
    		if (t != null && t.isAlive()) {
    			t.interrupt();
    		}
    		t = new Thread(this);
    		t.start();
    		//listener.onValueChanged(getXValue(), getYValue());
    	}
    	return true;
    }
    
    public double getXValue() {
    	return (x-centerX)/joystickRadius;
    	
    }
    
    public double getYValue() {
    	return (y-centerY)/joystickRadius;
    }   

    public void setOnJoystickChangeListener(OnJoystickChangeListener listener, long repeatInterval) {
    	this.listener = listener;
    	this.repeatInterval = repeatInterval;
    }
    
    public static interface OnJoystickChangeListener {
    	public void onValueChanged(double xValue, double yValue);
    }
    

    @Override
    public void run() {
    	while (!Thread.interrupted()) {
    		post(new Runnable() {
                public void run() {
                	listener.onValueChanged(getXValue(), getYValue());
                }
            });
    		try {
				Thread.sleep(repeatInterval);
			} catch (InterruptedException e) {
				break;
			}
    	}
    }
}

