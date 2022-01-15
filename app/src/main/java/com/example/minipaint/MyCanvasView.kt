package com.example.minipaint

import android.content.Context
import android.graphics.*
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration
import androidx.core.content.res.ResourcesCompat

//define a constant for the stroke width.
private const val STROKE_WIDTH = 12f // has to be float

class MyCanvasView(context: Context): View(context){



    //define member variables for a Canvas and a Bitmap. Called extraCanvas and extraBitmap.
    // These are your bitmap and canvas for caching what has been drawn before.
    private lateinit var extraCanvas: Canvas
    private lateinit var extraBitmap: Bitmap

    //Define a class level variable backgroundColor, for the background color of the canvas and initialize it to the colorBackground you defined in colors.xml
    private val backgroundColor = ResourcesCompat.getColor(resources, R.color.colorBackground, null)

    //define a variable drawColor for holding the color to draw with and initialize it with the colorPaint resource you defined in colors.xml.
    private val drawColor = ResourcesCompat.getColor(resources, R.color.colorPaint, null)

    // add a variable paint for a Paint object and initialize it as follows.
    private val paint = Paint().apply {
        color = drawColor
        // Smooths out edges of what is drawn without affecting shape.
        isAntiAlias = true
        // Dithering affects how colors with higher-precision than the device are down-sampled.
        isDither = true
        style = Paint.Style.STROKE // default: FILL, we set it to STROKE
        strokeJoin = Paint.Join.ROUND // default: MITER, we set it to ROUND
        strokeCap = Paint.Cap.ROUND // default: BUTT, we set it to ROUND
        strokeWidth = STROKE_WIDTH // default: Hairline-width (really thin), we set it to STROKE_WIDTH
    }

    // add a variable path and initialize it with a Path object to store the path that is being drawn when following the user's touch on the screen.
    private val path = Path()

    //add the motionTouchEventX and motionTouchEventY variables for caching the x and y coordinates of the current touch event (the MotionEvent coordinates). Initialize them to 0f.
    private var motionTouchEventX = 0f
    private var motionTouchEventY = 0f

    //variables to cache the latest x and y values. After the user stops moving and lifts their touch, these are the starting point for the next path (the next segment of the line to draw).
    private var currentX = 0f
    private var currentY = 0f

    //add a touchTolerance variable and set it to ViewConfiguration.get(context).scaledTouchSlop
    private val touchTolerance = ViewConfiguration.get(context).scaledTouchSlop

    //add a variable called frame that holds a Rect object.
    private lateinit var frame: Rect


    //override the onSizeChanged() method. This callback method is called by the Android system with the changed screen dimensions, that is, with a new width and height (to change to) and the old width and height (to change from).
    override fun onSizeChanged(width: Int, height: Int, oldWidth: Int, oldHeight: Int) {
        super.onSizeChanged(width, height, oldWidth, oldHeight)

        //a new bitmap and canvas are created every time the function executes. You need a new bitmap, because the size has changed.
        //However, this is a memory leak, leaving the old bitmaps objects around. To fix this, recycle extraBitmap before creating the next one.
        if (::extraBitmap.isInitialized) extraBitmap.recycle()

        //create an instance of Bitmap with the new width and height, which are the screen size, and assign it to extraBitmap.
        //The third argument is the bitmap color configuration. ARGB_8888 stores each color in 4 bytes and is recommended.
        extraBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)

        //Create a Canvas instance extraCanvas and assign extraBitmap to it.
        extraCanvas = Canvas(extraBitmap)

        //Specify the background color in which to fill extraCanvas.
        extraCanvas.drawColor(backgroundColor)

        // Calculate a rectangular frame around the picture. It will create the Rectangle that will be used for the frame, using the new dimensions and the inset.
        val inset = 40
        frame = Rect(inset, inset, width - inset, height - inset)
    }

    //Override onDraw() and draw the contents of the cached extraBitmap on the canvas associated with the view.
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        //The drawBitmap() Canvas method comes in several versions. In this code, you provide the bitmap, the x and y coordinates (in pixels) set to 0f and 0f which is at the top left corner of the view, and null for the Paint, as you'll set that later.
        canvas.drawBitmap(extraBitmap, 0f, 0f, null) //The 2D coordinate system used for drawing on a Canvas is in pixels, and the origin (0,0) is at the top left corner of the Canvas.

        // Draw a frame around the canvas.
        canvas.drawRect(frame, paint)
    }

    //touching down on the screen
    private fun touchStart() {
        //Implement the touchStart() method as follows. Reset the path, move to the x-y coordinates of the touch event (motionTouchEventX and motionTouchEventY) and assign currentX and currentY to that value.
        path.reset()
        path.moveTo(motionTouchEventX, motionTouchEventY)
        currentX = motionTouchEventX
        currentY = motionTouchEventY
    }
    //moving on the screen
    private fun touchMove() {
        //Calculate the traveled distance (dx, dy), create a curve between the two points and store it in path, update the running currentX and currentY tally, and draw the path. Then call invalidate() to force redrawing of the screen with the updated path.

        //Calculate the distance that has been moved (dx, dy).
        val dx = Math.abs(motionTouchEventX - currentX)
        val dy = Math.abs(motionTouchEventY - currentY)

        //If the movement was further than the touch tolerance, add a segment to the path.
        if (dx >= touchTolerance || dy >= touchTolerance) {
            // QuadTo() adds a quadratic bezier from the last point,
            // approaching control point (x1,y1), and ending at (x2,y2).
            //Using quadTo() instead of lineTo() create a smoothly drawn line without corners. See "Bezier Curves".
            path.quadTo(currentX, currentY, (motionTouchEventX + currentX) / 2, (motionTouchEventY + currentY) / 2)

            //Set the starting point for the next segment to the endpoint of this segment.
            currentX = motionTouchEventX
            currentY = motionTouchEventY
            // Draw the path in the extra bitmap to cache it.
            extraCanvas.drawPath(path, paint)
        }
        //Call invalidate() to (eventually call onDraw and) redraw the view.
        invalidate()
    }
    //lifting the touch on the screen
    private fun touchUp() {
        // Reset the path so it doesn't get drawn again.
        path.reset()
    }
    //override onTouchEvent() method to respond to motion on the display, override it to cache the x and y coordinates of the passed in event
    override fun onTouchEvent(event: MotionEvent): Boolean {
        motionTouchEventX = event.x
        motionTouchEventY = event.y

        //use a when expression to handle motion events for touching down on the screen, moving on the screen, and releasing touch on the screen.
        //These are the events of interest for drawing a line on the screen. For each event type, call a utility method, as shown in the code below. See the MotionEvent class documentation for a full list of touch events.
        when (event.action) {
            MotionEvent.ACTION_DOWN -> touchStart() //touching down on the screen
            MotionEvent.ACTION_MOVE -> touchMove() //moving on the screen
            MotionEvent.ACTION_UP -> touchUp() //releasing touch on the screen
        }
        return true
    }

}


/** In the current app, the cumulative drawing information is cached in a bitmap. While this is a good solution, it is not the only possible way. How you store your drawing history depends on the app, and your various requirements. For example, if you are drawing shapes, you could save a list of shapes with their location and dimensions. For the MiniPaint app, you could save the path as a Path. Below is the general outline on how to do that, if you want to try it.

Step 1: Remove all the code for extraCanvas and extraBitmap.
Step 2: Add variables for the path so far, and the path being drawn currently.
// Path representing the drawing so far
private val drawing = Path()

// Path representing what's currently being drawn
private val curPath = Path()

 Step 3:In onDraw(), instead of drawing the bitmap, draw the stored and current paths.
// Draw the drawing so far
canvas.drawPath(drawing, paint)
// Draw any current squiggle
canvas.drawPath(curPath, paint)
// Draw a frame around the canvas
canvas.drawRect(frame, paint)

 Step 4: In touchUp() , add the current path to the previous path and reset the current path.
// Add the current path to the drawing so far
drawing.addPath(curPath)
// Rewind the current path for the next touch
curPath.reset()

 Step 5: Run your app, and yes, there should be no difference whatsoever.
 *
 * */